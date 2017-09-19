package com.mobimvp.privacybox.utility.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

import com.mobimvp.privacybox.utility.crypto.EncryptionManager.Decryptor;
import com.mobimvp.privacybox.utility.crypto.EncryptionManager.Encryptor;

public class FileEncryptor {

	private static final int HEADER_SIZE = 4 * 1024;
	private static final int REPORT_COUNT = 1024 * 1024 / HEADER_SIZE;
	private static final int CURRENT_VERSION = 1;

	public static final int FAST_ENCRYPTION = 1;
	public static final int FULL_ENCRYPTION = 2;

	private static class EncryptionHeader {
		static final String MAGIC = "MOBIMVP_CRYPTO";
		public int version;
		public int type;
		public int padding;

		EncryptionHeader() {
		}

		EncryptionHeader(byte[] header) throws Exception {
			if (header == null || header.length != HEADER_SIZE) {
				throw new Exception("Invalid header length");
			}

			ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(header));
			String magic = is.readUTF();
			if (MAGIC.equals(magic)) {
				version = is.readInt();
				type = is.readInt();
				padding = is.readInt();
			} else {
				throw new Exception("Invalid header magic");
			}
		}

		byte[] toByteArray() throws Exception {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(baos);
			os.writeUTF(MAGIC);
			os.writeInt(version);
			os.writeInt(type);
			os.writeInt(padding);
			os.flush();
			baos.flush();

			byte[] written = baos.toByteArray();
			byte[] result = new byte[HEADER_SIZE];
			Arrays.fill(result, (byte) 0x30);
			System.arraycopy(written, 0, result, 0, written.length);

			return result;
		}
	}

	static int fastEncryptor(Encryptor encryptor, File in, EncryptListener listener) {
		if (!in.exists() || !in.canRead() || !in.canWrite()) {
			return EncryptListener.RESULT_FILE_ERROR;
		}
		byte[] header = new byte[HEADER_SIZE];
		try {
			if (listener != null) {
				listener.OnOperationProgress(0);
			}
			
			RandomAccessFile raf = new RandomAccessFile(in, "rw");
			int length = raf.read(header);
			if (length < 0) {
				return EncryptListener.RESULT_FILE_ERROR;
			}
			try {
				new EncryptionHeader(header);
				return EncryptListener.RESULT_FILE_ALREADY_ENCRYPTED;
			} catch (Exception e) {
			}

			header = encryptor.encrypt(header, 0, length, true);

			EncryptionHeader eh = new EncryptionHeader();
			eh.version = CURRENT_VERSION;
			eh.type = FAST_ENCRYPTION;
			eh.padding = header.length;

			raf.seek(0);
			raf.write(eh.toByteArray());
			raf.setLength(raf.length() + header.length);
			raf.seek(raf.length() - header.length);
			raf.write(header);
			raf.close();
			if (listener != null) {
				listener.OnOperationProgress(100);
			}

			return EncryptListener.RESULT_ENCRYPT_OK;
		} catch (IOException e) {
			e.printStackTrace();
			return EncryptListener.RESULT_FILE_ERROR;
		} catch (Exception e) {
			e.printStackTrace();
			return EncryptListener.RESULT_ENCRYPT_ERROR;
		}

	}

	static int fullEncryptor(Encryptor encryptor, File in, File out, EncryptListener listener) {
		if (!in.exists() || !in.canRead() || !in.canWrite()) {
			return EncryptListener.RESULT_FILE_ERROR;
		}

		byte[] block = new byte[HEADER_SIZE];
		try {
			if (listener != null) {
				listener.OnOperationProgress(0);
			}
			
			RandomAccessFile raf = new RandomAccessFile(in, "r");
			int blockLength = raf.read(block), blockCount = 0;
			if (blockLength < 0) {
				return EncryptListener.RESULT_FILE_ERROR;
			}
			try {
				new EncryptionHeader(block);
				return EncryptListener.RESULT_FILE_ALREADY_ENCRYPTED;
			} catch (Exception e) {
			}

			RandomAccessFile rafOut = new RandomAccessFile(out, "rw");

			EncryptionHeader eh = new EncryptionHeader();
			eh.version = CURRENT_VERSION;
			eh.type = FULL_ENCRYPTION;
			eh.padding = 0;

			rafOut.write(eh.toByteArray());

			while (blockLength >= 0) {
				rafOut.write(encryptor.encrypt(block, 0, blockLength, false));
				blockLength = raf.read(block);

				if ((++blockCount) % REPORT_COUNT == 0 && listener != null) {
					if (listener.OnOperationProgress((int) (raf.getFilePointer() * 100 / raf.length()))) {
						raf.close();
						rafOut.close();
						return EncryptListener.RESULT_CANCEL;
					}
				}
			}

			rafOut.write(encryptor.encrypt(null, true));
			raf.close();
			rafOut.close();
			
			if (listener != null) {
				listener.OnOperationProgress(100);
			}

			return EncryptListener.RESULT_ENCRYPT_OK;
		} catch (IOException e) {
			return EncryptListener.RESULT_FILE_ERROR;
		} catch (Exception e) {
			return EncryptListener.RESULT_ENCRYPT_ERROR;
		}
	}

	static int fileDecryptor(Decryptor decryptor, File in, File out, EncryptListener listener) {
		if (!in.exists() || !in.canRead() || !in.canWrite()) {
			return EncryptListener.RESULT_FILE_ERROR;
		}

		byte[] block = new byte[HEADER_SIZE];
		try {
			if (listener != null) {
				listener.OnOperationProgress(0);
			}
			
			RandomAccessFile raf = new RandomAccessFile(in, "rw");
			int blockLength = raf.read(block);
			if (blockLength < 0) {
				raf.close();
				return EncryptListener.RESULT_FILE_ERROR;
			}

			EncryptionHeader eh = null;
			try {
				eh = new EncryptionHeader(block);
			} catch (Exception e) {
				raf.close();
				return EncryptListener.RESULT_FILE_NOT_ENCRYPTED;
			}

			if (eh.type == FAST_ENCRYPTION) {
				byte[] header = new byte[eh.padding];
				long offset = raf.length() - eh.padding;
				raf.seek(offset);
				if (raf.read(header) != header.length) {
					raf.close();
					return EncryptListener.RESULT_FILE_ERROR;
				}
				raf.seek(0);
				header = decryptor.decrypt(header, true);
				raf.write(header);
				raf.setLength(raf.length() - eh.padding - HEADER_SIZE + header.length);
				
				if (listener != null) {
					listener.OnOperationProgress(100);
				}
			} else if (eh.type == FULL_ENCRYPTION) {
				RandomAccessFile rafOut = new RandomAccessFile(out, "rw");
				int blockCount = 0;
				while ((blockLength = raf.read(block)) >= 0) {
					rafOut.write(decryptor.decrypt(block, 0, blockLength, false));
					if ((++blockCount) % REPORT_COUNT == 0 && listener != null) {
						if (listener.OnOperationProgress((int) (raf.getFilePointer() * 100 / raf.length()))) {
							rafOut.close();
							raf.close();
							out.delete();
							return EncryptListener.RESULT_CANCEL;
						}
					}
				}
				rafOut.write(decryptor.decrypt(null, true));
				rafOut.close();
			} else {
				raf.close();
				return EncryptListener.RESULT_UNSURPORT_ERROR;
			}
			raf.close();
			return EncryptListener.RESULT_DECRYPT_OK;
		} catch (IOException e) {
			e.printStackTrace();
			return EncryptListener.RESULT_FILE_ERROR;
		} catch (Exception e) {
			e.printStackTrace();
			return EncryptListener.RESULT_DECRYPT_ERROR;
		}
	}

	static int streamDecryptor(Decryptor decryptor, File in, OutputStream out, EncryptListener listener) {
		if (!in.exists() || !in.canRead() || !in.canWrite()) {
			return EncryptListener.RESULT_FILE_ERROR;
		}

		byte[] block = new byte[HEADER_SIZE];
		try {
			if (listener != null) {
				listener.OnOperationProgress(0);
			}
			
			RandomAccessFile raf = new RandomAccessFile(in, "rw");
			int blockLength = raf.read(block);
			if (blockLength < 0) {
				return EncryptListener.RESULT_FILE_ERROR;
			}

			EncryptionHeader eh = null;
			try {
				eh = new EncryptionHeader(block);
			} catch (Exception e) {
				return EncryptListener.RESULT_FILE_NOT_ENCRYPTED;
			}

			if (eh.type == FAST_ENCRYPTION) {
				byte[] header = new byte[eh.padding];
				long filesize = raf.length() - eh.padding;
				raf.seek(filesize);
				if (raf.read(header) != header.length) {
					return EncryptListener.RESULT_FILE_ERROR;
				}
				out.write(decryptor.decrypt(header, true));

				raf.seek(HEADER_SIZE);

				byte[] body = new byte[(int) (filesize - HEADER_SIZE)];
				if (raf.read(body) != body.length) {
					return EncryptListener.RESULT_FILE_ERROR;
				}
				out.write(body);
				raf.close();
				
				if (listener != null) {
					listener.OnOperationProgress(0);
				}
				return EncryptListener.RESULT_DECRYPT_OK;
			} else if (eh.type == FULL_ENCRYPTION) {
				int blockCount = 0;
				while ((blockLength = raf.read(block)) >= 0) {
					out.write(decryptor.decrypt(block, 0, blockLength, false));
					if ((++blockCount) % REPORT_COUNT == 0 && listener != null) {
						if (listener.OnOperationProgress((int) (raf.getFilePointer() * 100 / raf.length()))) {
							out.flush();
							raf.close();
							return EncryptListener.RESULT_CANCEL;
						}
					}
				}
				out.write(decryptor.decrypt(null, true));
				out.flush();
				return EncryptListener.RESULT_DECRYPT_OK;
			} else {
				raf.close();
				return EncryptListener.RESULT_UNSURPORT_ERROR;
			}
		} catch (IOException e) {
			return EncryptListener.RESULT_FILE_ERROR;
		} catch (Exception e) {
			return EncryptListener.RESULT_DECRYPT_ERROR;
		}
	}
}
