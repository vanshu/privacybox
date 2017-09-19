package com.mobimvp.privacybox.utility.crypto;

public interface EncryptListener {
	public static final int RESULT_ENCRYPT_OK = 0;
	public static final int RESULT_ENCRYPT_ERROR = 6;

	public static final int RESULT_DECRYPT_OK = 1;
	public static final int RESULT_DECRYPT_ERROR = 7;

	public static final int RESULT_FILE_NOT_ENCRYPTED = 3;
	public static final int RESULT_FILE_ALREADY_ENCRYPTED = 4;
	public static final int RESULT_FILE_ERROR = 5;
	public static final int RESULT_CANCEL = 8;
	public static final int RESULT_UNSURPORT_ERROR = 9;

	/**
	 * 文件操作过程中每更新128kb内容后会触发此回调
	 * 
	 * @param progress
	 *            进度百分比，从0至100
	 * @return 返回true，取消后续操作，返回false，继续处理
	 */
	public boolean OnOperationProgress(int progress);
}
