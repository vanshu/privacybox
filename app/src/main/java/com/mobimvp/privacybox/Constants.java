package com.mobimvp.privacybox;

public class Constants {
    public static final String PACKAGE_NAME = "com.mobimvp.privacybox";

    // 关闭空白activity的广播
    public static final String MSG_BROADCAST_CLOSEDUMMYACTIVITY = "broadcast_closedummyactivity";

    public static final String BROADCAST_GRAPH_TRAIN = "broadcast_graph_train";
    public static final String BROADCAST_FIRST_TRAIN = "broadcast_first_train";
    public static final String BROADCAST_LOCKSCREEN_APP = "msg_applock_screen";
    public static final String BROADCAST_LOCKSCREEN_FILE = "msg_filelock_screen";
    public static final String BROADCAST_LOCKSCREEN_PHOTO = "msg_imglock_screen";
    public static final String BROADCAST_LOCKSCREEN_VIDEO = "msg_videolock_screen";
    // 记录NFCID号
    public static final String BROADCAST_NFC_ID = "broadcast_nfc_id";
    public static final String BROADCAST_UNLOCK_RETRY = "msg_retry_unlock";
    public static final String BROADCAST_CLOSE_GUIDEACTIVITY = "broadcast_close_guideactivity";

    // 程序锁是否启用
    public static final String PRIVACY_LOCK_ENABLE = "privacy_lock_enable";
    public static final boolean PRIVACY_LOCK_ENABLE_DEFAULT = true;

    // 重置密码
    public static final String PRIVACY_LOCKTYPE = "privacy_unlocktype";

    public static final String PRIVACY_UNLOCK_HAPTIC = "HapticFeedback";
    // 锁屏方式
    public static final String PRIVACY_LOCKEDTYPE = "privacy_locktype";
    public static final int PRIVACY_LOCKEDTYPE_EXITAPP = 0;
    public static final int PRIVACY_LOCKEDTYPE_SCREENOFF = 1;
    public static final int PRIVACY_LOCKEDTYPE_DEFAULT = 0;// 0-exitapp,1-screenoff

    public static final String PRIVACY_PIN_PASSWORD = "privacy_pin_password";

    // patterPassword
    public static final String PRIVACY_PATTERN_PASSWORD = "privacy_pattern_password";

    public static final String PRIVACY_PATTERN_LEVEL = "pattern_level";
    public static final String PRIVACY_CURRENT_LEVEL = "current_level";
    public static final int DEFAULT_LEVEL = 3;
    public static final int DEFAULT_MAX_LEVEL = 5;

    public static final String PRIVACY_FILE_FULL = "privacy_filelock_full";
    // 是否是第一次使用
    public static final String PRIVACY_FIRST_RUN = "privacy_first_run";

    // json格式的结构，存储已经被加锁的包名
    public static final String PRIVACY_DB_LOCKPKGNAME = "privacy_db_lockpkgname";
    // 在service中调用showTrain的类型
    public static final int TYPE_SERVICE_SHOWTRAIN_GRAPH = 2;
    public static final int TYPE_SERVICE_SHOWTRAIN_SETTINGSFORTRAIN = 3;
    public static final int TYPE_SERVICE_SHOWTRAIN_FIRST = 4;

    public static final String FILE_PATH = "/.mobimvp/privacybox";
    public static final String ENCRYPT_FILE_PATH = FILE_PATH + "/e";
    public static final String FILE_TEMPORARY_PATH = FILE_PATH + "/temp";
    public static final String FILE_DB_PATH = FILE_PATH + "/pri.db";
    public static final String FILE_THUMB_PATH = FILE_PATH + "/thumb";

    public static final int TYPE_FILE = 1;
    public static final int TYPE_PHOTO = 2;
    public static final int TYPE_VIDEO = 3;

    // 用于保存NFCID
    public static final String PRIVACY_NFC_ID = "privacy_nfc_id";

    // 是否设置NFCID
    public static final String PRIVACY_NFC_UNLOCK = "privacy_nfc_unlock";

    // 是否隐藏桌面图标
    public static final String PRIVACY_HIDE_LANUCHER = "privacy_hide_lanucher";

    // 保存拨号号码
    public static final String PRIVACY_DIAL_PHONE_NUMBER = "privacy_dial_phone_number";

    //获取当前解锁模式 1:普通模式,2:NFC解锁模式,3:拨号模式,4:穿戴式设备解锁模式
    public static final String PRIVACY_UNLOCKMODE = "privacy_unlockmode";
}
