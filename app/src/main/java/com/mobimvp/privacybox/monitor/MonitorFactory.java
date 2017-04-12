package com.mobimvp.privacybox.monitor;

import android.content.Context;

import com.mobimvp.privacybox.monitor.MonitorInterface.MonitorListener;
import com.mobimvp.privacybox.monitor.MonitorInterface.PackageMonitor;

public class MonitorFactory {

    public static final int MONITOR_NATIVE = 0x0;
    public static final int MONITOR_AMSERVICE = 0x1;

    public static PackageMonitor createPackageMonitor(Context context, int type, MonitorListener listener) {
        switch (type) {
            case MONITOR_NATIVE:
                return new MonitorNativeMode(context, listener);
            case MONITOR_AMSERVICE:
                return new MonitorAMServiceMode(context, listener);
        }
        return null;
    }
}
