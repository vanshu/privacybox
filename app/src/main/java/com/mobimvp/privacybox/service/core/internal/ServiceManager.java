/**
 *
 */
package com.mobimvp.privacybox.service.core.internal;

import android.os.IBinder;

import java.lang.reflect.Method;

public class ServiceManager {

    private static ServiceManager _instance = null;
    private Class<?> mServiceManager;
    private Method mGetService;
    private Method mAddService;
    private Method mCheckService;
    private Method mListServices;

    private ServiceManager() {
    }

    public final static ServiceManager getServiceManager() {
        if (_instance == null) {
            try {
                _instance = new ServiceManager();
                _instance.mServiceManager = Class.forName("android.os.ServiceManager");
                _instance.mGetService = _instance.mServiceManager.getMethod("getService", String.class);
                _instance.mAddService = _instance.mServiceManager.getMethod("addService", String.class, IBinder.class);
                _instance.mCheckService = _instance.mServiceManager.getMethod("checkService", String.class);
                _instance.mListServices = _instance.mServiceManager.getMethod("listServices");
            } catch (Exception e) {
                _instance = null;
            }
        }
        return _instance;
    }

    public void addService(String name, IBinder service) {
        try {
            mAddService.invoke(null, name, service);
        } catch (Exception e) {
        }
    }

    public IBinder checkService(String name) {
        try {
            IBinder ret = (IBinder) mCheckService.invoke(null, name);
            return ret;
        } catch (Exception e) {
            return null;
        }
    }

    public IBinder getService(String name) {
        try {
            IBinder ret = (IBinder) mGetService.invoke(null, name);
            return ret;
        } catch (Exception e) {
            return null;
        }
    }

    public String[] listService() {
        try {
            String[] list = (String[]) mListServices.invoke(null);
            return list;
        } catch (Exception e) {
            return new String[0];
        }
    }
}