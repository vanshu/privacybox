package com.mobimvp.privacybox;

public class Intents {
    /**
     * 加密批量处理文件开始的广播Action
     */
    public static final String ACTION_FILE_ENCRYPTION_START = "action_encryption_file_start";
    /**
     * 加密批量处理文件完毕的广播Action
     */
    public static final String ACTION_FILE_ENCRYPTION_END = "action_encryption_file_end";
    /**
     * 加密开始处理文件的广播Action
     */
    public static final String ACTION_FILE_ENCRYPTION_ALONE_START = "action_alone_encryption_file_start";
    /**
     * 加密文件处理完毕的广播Action
     */
    public static final String ACTION_FILE_ENCRYPTION_ALONE_END = "action_alone_encryption_file_end";
    /**
     * 加密文件处理中的广播Action
     */
    public static final String ACTION_FILE_ENCRYPTION_PROGRESS = "action_encryption_file_progress";
    /**
     * 加密文件处理error广播的Action
     */
    public static final String ACTION_FILE_ENCRYPTION_ERROR = "action_encryption_file_error";

    /**
     * 解密批量处理文件开始的广播Action
     */
    public static final String ACTION_FILE_DECIPHERING_START = "action_deciphering_file_start";
    /**
     * 解密批量处理文件完毕的广播Action
     */
    public static final String ACTION_FILE_DECIPHERING_END = "action_deciphering_file_end";
    /**
     * 解密开始处理文件的广播Action
     */
    public static final String ACTION_FILE_DECIPHERING_ALONE_START = "action_alone_deciphering_file_start";
    /**
     * 解密文件处理完毕的广播Action
     */
    public static final String ACTION_FILE_DECIPHERING_ALONE_END = "action_alone_deciphering_file_end";
    /**
     * 解密文件处理中的广播Action
     */
    public static final String ACTION_FILE_DECIPHERING_PROGRESS = "action_deciphering_file_progress";
    /**
     * 解密文件处理error广播的Action
     */
    public static final String ACTION_FILE_DECIPHERING_ERROR = "action_deciphering_file_error";


    /**
     * 临时解密文件重新加密批量处理文件开始的广播Action
     */
    public static final String ACTION_FILE_TEMPORARY_ENCRYPTION_START = "action_deciphering_encryption_file_start";
    /**
     * 临时解密文件重新加密批量处理文件完毕的广播Action
     */
    public static final String ACTION_FILE_TEMPORARY_ENCRYPTION_END = "action_deciphering_encryption_file_end";
    /**
     * 临时解密文件重新加密开始处理文件的广播Action
     */
    public static final String ACTION_FILE_TEMPORARY_ENCRYPTION_ALONE_START = "action_alone_deciphering_encryption_file_start";
    /**
     * 临时解密文件重新加密文件处理完毕的广播Action
     */
    public static final String ACTION_FILE_TEMPORARY_ENCRYPTION_ALONE_END = "action_alone_deciphering_encryption_file_end";
    /**
     * 临时解密文件重新加密文件处理中的广播Action
     */
    public static final String ACTION_FILE_TEMPORARY_ENCRYPTION_PROGRESS = "action_file_deciphering_encryption_progress";
    /**
     * 临时解密文件重新加密文件处理error广播的Action
     */
    public static final String ACTION_FILE_TEMPORARY_ENCRYPTION_ERROR = "action_deciphering_encryption_file_error";

    /**
     * 临时解密批量处理文件开始的广播Action
     */
    public static final String ACTION_FILE_TEMPORARY_DECIPHERING_START = "action_tmporary_deciphering_file_start";
    /**
     * 临时解密批量处理文件完毕的广播Action
     */
    public static final String ACTION_FILE_TEMPORARY_DECIPHERING_END = "action_tmporary_deciphering_file_end";
    /**
     * 临时解密开始处理文件的广播Action
     */
    public static final String ACTION_FILE_TEMPORARY_DECIPHERING_ALONE_START = "action_tmporary_alone_deciphering_file_start";
    /**
     * 临时解密文件处理完毕的广播Action
     */
    public static final String ACTION_FILE_TEMPORARY_DECIPHERING_ALONE_END = "action_alone_tmporary_deciphering_file_end";
    /**
     * 临时解密文件处理中的广播Action
     */
    public static final String ACTION_FILE_TEMPORARY_DECIPHERING_PROGRESS = "action_tmporary_deciphering_file_progress";
    /**
     * 临时解密文件处理error广播的Action
     */
    public static final String ACTION_FILE_TEMPORARY_DECIPHERING_ERROR = "action_tmporary_deciphering_file_error";

    /**
     * 销毁文件开始处理广播Action
     */
    public static final String ACTION_FILE_DELETE_START = "action_file_delete_start";
    /**
     * 销毁单个文件开始处理广播Action
     */
    public static final String ACTION_FILE_DELETE_ALONE_START = "action_file_delete_alone_start";
    /**
     * 销毁文件结束广播Action
     */
    public static final String ACTION_FILE_DELETE_END = "action_file_delete_end";
    /**
     * 销毁单个文件结束广播Action
     */
    public static final String ACTION_FILE_DELETE_ALONE_END = "action_file_deete_alone_end";


    /**
     * 文件id
     */
    public static final String EXTRAS_FILE_ID = "file_id";
    /**
     * 文件名称
     */
    public static final String EXTRAS_FILE_NAME = "file_name";
    /**
     * 加密后的文件地址
     */
    public static final String EXTRAS_FILE_PATH = "file_path";
    /**
     * 解密临时文件地址
     */
    public static final String EXTRAS_FILE_TEMPORARY_PATH = "temporary_path";
    /**
     * 原始文件地址
     */
    public static final String EXTRAS_FILE_ORIGINAL_PATH = "priginal_path";
    /**
     * 缩略图地址
     */
    public static final String EXTRAS_FILE_SMALL_PHOTO_PATH = "smallPhoto_path";
    /**
     * 文件创建时间
     */
    public static final String EXTRAS_FILE_DATE = "file_date";
    /**
     * 文件大小
     */
    public static final String EXTRAS_FILE_SIZE = "file_size";
    /**
     * 文件类型
     */
    public static final String EXTRAS_FILE_TYPE = "file_type";
    /**
     * 是否深度加密
     */
    public static final String EXTRAS_FILE_DEPTH = "file_depth";

    /**
     * 处理中文件下标
     */
    public static final String EXTRAS_FILE_INDEX = "file_index";
    /**
     * 处理中文件进度
     */
    public static final String EXTRAS_FILE_PROGRESS = "file_progress";
    /**
     * 需要处理文件的总数
     */
    public static final String EXTRAS_FILE_COUNT = "file_count";
    /**
     * 文件处理错误信息
     */
    public static final String EXTRAS_FILE_ERROR = "file_error";
    /**
     * 文件处理结果 成功还是失败
     */
    public static final String EXTRAS_FILE_RESULT = "file_result";


    public static final String ACTION_FAILED_UNLOCK_ATTEMPT = "FailedUnlockAttempt";
    public static final String ACTION_SUCCESS_UNLOCK_ATTEMPT = "SuccessfulUnlockAttempt";
}
