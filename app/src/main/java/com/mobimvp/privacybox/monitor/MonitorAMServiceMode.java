package com.mobimvp.privacybox.monitor;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.mobimvp.privacybox.monitor.MonitorInterface.MonitorListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MonitorAMServiceMode implements MonitorInterface.PackageMonitor {

    private MonitorListener listener;
    private Context context;
    private CountDownLatch downlatch;
    private boolean stop = false;
    private String memorypackage;
    private MonitorThread monitorThread;
    private ActivityManager am;
    private Handler monitorHandler;
    private int i = 0;

    MonitorAMServiceMode(Context context, MonitorListener listener) {
        this.listener = listener;
        this.context = context;
    }

    @Override
    public void start() {
        memorypackage = "";
        downlatch = new CountDownLatch(1);
        am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        monitorThread = new MonitorThread();
        monitorThread.start();

        try {
            downlatch.await(3, TimeUnit.SECONDS);
            monitorHandler.sendEmptyMessage(0);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void stop() {
        stop = true;
        if (monitorHandler != null) {
            //某些机型上，结束消息无法送达，只好用停止标志位二次处理
            monitorHandler.removeMessages(0);
            monitorHandler.sendEmptyMessage(1);
        }
        listener = null;
    }

    private class MonitorThread extends Thread {
        public void run() {
            Looper.prepare();
            monitorHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 0:
                            if (stop) {
                                //某些机型上，结束消息无法送达，只好用停止标志位二次处理
                                Looper.myLooper().quit();
                                break;
                            }
                            try {
                                String name = am.getRunningTasks(1).get(0).topActivity.getPackageName();
                                if (!name.equals(memorypackage)) {
                                    listener.onTopPackageChange(name);
                                    memorypackage = name;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            sendEmptyMessageDelayed(0, 500 * 1);
                            break;
                        case 1:
                            Looper.myLooper().quit();
                            break;
                    }
                }
            };
            if (downlatch != null)
                downlatch.countDown();
            Looper.loop();
            monitorHandler = null;
            monitorThread = null;
        }
    }


}
