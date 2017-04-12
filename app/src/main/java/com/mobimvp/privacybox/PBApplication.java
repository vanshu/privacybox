package com.mobimvp.privacybox;

import android.app.Application;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;

import com.mobimvp.privacybox.service.AppLockService;
import com.mobimvp.privacybox.service.LockScreenService;
import com.mobimvp.privacybox.service.core.internal.ServiceData;
import com.mobimvp.privacybox.service.core.internal.ServiceManager;
import com.mobimvp.privacybox.service.filelocker.FileLocker;

public class PBApplication extends Application {
    private static final int SET_FOREGROUND_PROCESS = ServiceData.getIPCOpcode("android.app.IActivityManager", "SET_PROCESS_FOREGROUND_TRANSACTION", -1);
    public static boolean FIRST_RUN = false;
    private static PBApplication _instance;
    private IBinder token;

    public static PBApplication getApplication() {
        return _instance;
    }

    private void initApplication() {
        _instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        doSelfProtect();
        initApplication();
        LockScreenService.initInstance(getApplication());  //锁屏服务
        FileLocker.initInstance(getApplication());  //文件锁服务
        startService(new Intent(getApplication(), AppLockService.class));  //程序锁服务
    }

    private void doSelfProtect() {
        if (SET_FOREGROUND_PROCESS < 0)
            return;

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            token = new Binder();
            IBinder activityService = ServiceManager.getServiceManager().checkService("activity");
            data.writeInterfaceToken("android.app.IActivityManager");
            data.writeStrongBinder(token);
            data.writeInt(android.os.Process.myPid());
            data.writeInt(1);
            activityService.transact(SET_FOREGROUND_PROCESS, data, reply, 0);
            reply.readException();
        } catch (Exception e) {
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

}
