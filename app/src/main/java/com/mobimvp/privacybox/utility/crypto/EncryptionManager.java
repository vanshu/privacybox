package com.mobimvp.privacybox.utility.crypto;

import java.io.File;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionManager {

	private static final String ALGORITHM = "AES";
	private static final String SRALGORITHM = "SHA1PRNG";
	private static final String KGALGORITHM = "AES";

	public class Encryptor {

		protected Cipher cipher;

		public Encryptor() throws Exception {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secureSpec);
		}

		/**
		 * 加密数据
		 * 
		 * @param rawData
		 *            需要加密的原始数据
		 * @param isFinal
		 *            当传送最后一组数据时为真，当随后需要继续加密数据时，为假
		 * @return 加密后的数据
		 * @throws Exception
		 *             加密出现错误
		 */
		public byte[] encrypt(byte[] rawData, boolean isFinal) throws Exception {
			if (isFinal) {
				if (rawData != null) {
					return cipher.doFinal(rawData);
				} else {
					return cipher.doFinal();
				}
			} else {
				return cipher.update(rawData);
			}
		}

		/**
		 * 加密数据
		 * 
		 * @param rawData
		 *            需要加密的原始数据
		 * @param start
		 *            start offset
		 * @param length
		 *            length
		 * @param isFinal
		 *            当传送最后一组数据时为真，当随后需要继续加密数据时，为假
		 * @return 加密后的数据
		 * @throws Exception
		 *             加密出现错误
		 */
		public byte[] encrypt(byte[] rawData, int start, int length, boolean isFinal) throws Exception {
			if (isFinal) {
				return cipher.doFinal(rawData, start, length);
			} else {
				return cipher.update(rawData, start, length);
			}
		}
	}

	public class Decryptor {

		protected Cipher cipher;

		public Decryptor() throws Exception {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secureSpec);
		}

		/**
		 * 解密数据
		 * 
		 * @param encryptData
		 *            需要解密的数据
		 * @param isFinal
		 *            当传送最后一组数据时为真，当随后需要继续加密数据时，为假
		 * @return 解密后的数据
		 * @throws Exception
		 *             解密出现错误
		 */
		public byte[] decrypt(byte[] encryptData, boolean isFinal) throws Exception {
			if (isFinal) {
				if (encryptData != null) {
					return cipher.doFinal(encryptData);
				} else {
					return cipher.doFinal();
				}
			} else {
				return cipher.update(encryptData);
			}
		}

		/**
		 * 解密数据
		 * 
		 * @param encryptData
		 *            需要解密的数据
		 * @param start
		 *            start offset
		 * @param length
		 *            length
		 * @param isFinal
		 *            当传送最后一组数据时为真，当随后需要继续加密数据时，为假
		 * @return 解密后的数据
		 * @throws Exception
		 *             解密出现错误
		 */
		public byte[] decrypt(byte[] encryptData, int start, int length, boolean isFinal) throws Exception {
			if (isFinal) {
				return cipher.doFinal(encryptData, start, length);
			} else {
				return cipher.update(encryptData, start, length);
			}
		}
	}

	private String secureKey;
	private SecretKeySpec secureSpec;

	/**
	 * 初始化加解密引擎
	 * 
	 * @param password
	 *            密码
	 * @param secKey
	 *            由initEncryptionManager生成的安全密钥（可以从文件中读取）
	 * @throws Exception
	 */
	public EncryptionManager(String password, byte[] secKey) throws Exception {
		checkPassword(password, secKey);
	}

	/**
	 * 校验密码是否正确
	 * 
	 * @param password
	 *            密码
	 * @param secKey
	 *            initEncryptionManager生成的内部密钥
	 * @throws Exception
	 *             如果校验失败，则会抛出异常
	 */
	public void checkPassword(String password, byte[] secKey) throws Exception {
		// Step 1: generate secret key based on password
		KeyGenerator kgen = KeyGenerator.getInstance(KGALGORITHM);
		SecureRandom sr = SecureRandom.getInstance(SRALGORITHM, "Crypto");
		sr.setSeed(password.getBytes());
		kgen.init(128, sr);
		SecretKey skey = kgen.generateKey();
		byte[] raw = skey.getEncoded();

		// Step 2: try decrypt secKey with password
		SecretKeySpec skeySpec = new SecretKeySpec(raw, KGALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(secKey);

		secureKey = new String(decrypted);

		// Step 3: validate decrypted secure key
		if (secureKey.startsWith(RAWKEY_PREFIX) && secureKey.endsWith(RAWKEY_SUFFIX)) {
			// match
			kgen = KeyGenerator.getInstance(KGALGORITHM);
			sr = SecureRandom.getInstance(SRALGORITHM, "Crypto");
			sr.setSeed(secureKey.getBytes());
			kgen.init(128, sr);
			skey = kgen.generateKey();
			raw = skey.getEncoded();
			secureSpec = new SecretKeySpec(raw, KGALGORITHM);
		} else {
			throw new Exception("Password mismatch");
		}
	}

	/**
	 * 更新密码
	 * 
	 * @param password
	 *            新的密码明文
	 * @return 新的内部密钥，程序需妥善保存
	 */
	public byte[] updatePassword(String password) throws Exception {
		return updatePassword(password, secureKey);
	}

	/**
	 * 获取底层加密接口
	 * 
	 * @return 底层加密接口
	 * @throws Exception
	 *             失败
	 */
	public Encryptor getEncryptor() throws Exception {
		return new Encryptor();
	}

	/**
	 * 获取底层解密接口
	 * 
	 * @return 底层解密接口
	 * @throws Exception
	 *             失败
	 */
	public Decryptor getDecryptor() throws Exception {
		return new Decryptor();
	}

	/**
	 * 加密一串字节流
	 * 
	 * @param rawData
	 *            需要加密的字节流
	 * @return 加密后的字节流
	 * @throws Exception
	 *             失败
	 */
	public byte[] encryptBuffer(byte[] rawData) throws Exception {
		return getEncryptor().encrypt(rawData, true);
	}

	/**
	 * 解密一串字节流
	 * 
	 * @param encryptData
	 *            加密的字节流
	 * @return 解密后的字节流
	 * @throws Exception
	 *             失败
	 */
	public byte[] decryptBuffer(byte[] encryptData) throws Exception {
		return getDecryptor().decrypt(encryptData, true);
	}

	public int decryptStream(File in, OutputStream out, EncryptListener listener) throws Exception {
		return FileEncryptor.streamDecryptor(getDecryptor(), in, out, listener);
	}

	/**
	 * 快速加密文件（在原文件上直接进行修改），此函数为同步操作 加密方式为改写文件首部4KB为指定内容，同时在文件末尾写入加密的头部4KB
	 * 
	 * @param in
	 *            需要加密的原始文件（文件将会被直接加密）
	 * @param listener
	 *            加密回调函数（可选）
	 * @throws Exception
	 *             加密失败
	 */
	public int encryptFileFast(File in, EncryptListener listener) throws Exception {
		return FileEncryptor.fastEncryptor(getEncryptor(), in, listener);
	}

	/**
	 * 完整加密文件（必须将加密后的文件保存至新文件中），此函数为同步操作 加密格式：头部为4kb的文件头，后续为文件内容
	 * 
	 * @param in
	 *            需要加密的原始文件
	 * @param out
	 *            加密后的新文件
	 * @param listener
	 *            加密回调函数（可选）
	 * @throws Exception
	 *             加密失败
	 */
	public int encryptFileFull(File in, File out, EncryptListener listener) throws Exception {
		return FileEncryptor.fullEncryptor(getEncryptor(), in, out, listener);
	}

	/**
	 * 解密文件，会自动根据文件的类型选择完整解密或者快速解密。快速解密时，out参数将会被忽略，文件将会直接在原地解密，此函数为异步操作
	 * 目前没有办法判断文件的加密类型为快速加密还是完整加密，必须根据解密成功后的返回值进行判断
	 * 
	 * @param in
	 *            需要解密的原始文件
	 * @param out
	 *            解密后的文件（如果文件是快速加密，则此参数不会被使用）
	 * @param listener
	 *            加密回调函数（可选），如果置为null，则您将不会获知此文件为快速加密，还是完整加密
	 * @throws Exception
	 *             加密失败
	 */
	public int decryptFile(File in, File out, EncryptListener listener) throws Exception {
		return FileEncryptor.fileDecryptor(getDecryptor(), in, out, listener);
	}

	private static final String RAWKEY_PREFIX = "MOBIMVP_";
	private static final String RAWKEY_SUFFIX = "_CRYPT";

	/**
	 * 首次使用此类，需要进行初始化，产生内部随机密钥，并且保存起来，用于后续使用。
	 * 此方法只在首次使用EncryptionManager时调用，以后运行时使用标准的构造函数即可。
	 * 程序需将返回的内部密钥妥善保存，若密钥丢失，则无法进行解密。
	 * 返回经过加密处理的内部密钥，此密钥可以明文保存，并且在EncryptionManager初始化函数中使用
	 * 
	 * @param password
	 *            用户提供的密码
	 * @return 经过加密处理的内部密钥
	 * @throws Exception
	 *             初始化失败
	 */
	public static byte[] initEncryptionManager(String password) {
		// Step 1: generate random internal key, used by internal encryption and
		// decryption routine
		String secureKey = RAWKEY_PREFIX + UUID.randomUUID().toString() + RAWKEY_SUFFIX;
		try {
			return updatePassword(password, secureKey);
		} catch (Exception e) {
			return null;
		}
	}

	private static byte[] updatePassword(String password, String secureKey) throws Exception {
		// Step 1: generate secret key based on password
		KeyGenerator kgen = KeyGenerator.getInstance(KGALGORITHM);
		SecureRandom sr = SecureRandom.getInstance(SRALGORITHM, "Crypto");
		sr.setSeed(password.getBytes());
		kgen.init(128, sr);
		SecretKey skey = kgen.generateKey();
		byte[] raw = skey.getEncoded();

		// Step 2: encrypt random secure key with internal key
		SecretKeySpec skeySpec = new SecretKeySpec(raw, KGALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		return cipher.doFinal(secureKey.getBytes());
	}
}
