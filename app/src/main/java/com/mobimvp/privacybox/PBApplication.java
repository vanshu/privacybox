package com.mobimvp.privacybox;

import android.app.Application;
import android.content.Intent;


import com.mobimvp.privacybox.service.AppLockService;
import com.mobimvp.privacybox.service.LockScreenService;
import com.mobimvp.privacybox.service.filelocker.FileLocker;

public class PBApplication extends Application {
    private static PBApplication _instance;
    public static boolean FIRST_RUN = false;

    private void initApplication() {
        _instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initApplication();
        LockScreenService.initInstance(getApplication());  //锁屏服务
        FileLocker.initInstance(getApplication());  //文件锁服务
        startService(new Intent(getApplication(), AppLockService.class));  //程序锁服务
    }

    public static PBApplication getApplication() {
        return _instance;
    }


}
