package com.mobimvp.privacybox.monitor;


public class MonitorInterface {

    public interface MonitorListener {
        void onTopPackageChange(String packageName);
    }

    public interface PackageMonitor {
        public void start();

        public void stop();
    }
}
