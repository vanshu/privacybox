package com.mobimvp.privacybox.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.mobimvp.privacybox.Constants;
import com.mobimvp.privacybox.keyguard.KeyGuardFactory;
import com.mobimvp.privacybox.keyguard.KeyGuardInterface;
import com.mobimvp.privacybox.keyguard.KeyGuardInterface.KeyGuard;
import com.mobimvp.privacybox.keyguard.KeyGuardInterface.KeyGuardTrainListener;
import com.mobimvp.privacybox.keyguard.KeyGuardInterface.KeyGuardUnlockListener;
import com.mobimvp.privacybox.ui.MainActivity;
import com.mobimvp.privacybox.ui.applock.DummyActivity;
import com.mobimvp.privacybox.ui.guide.GuideActivity;

import java.lang.ref.WeakReference;

public class LockScreenService {

    private static LockScreenService _instance;
    private WeakReference<OnLockScreenFinish> firstObserver;
    private Context context;
    private KeyGuard GraphKeyguard;
    private SharedPreferences preference;
    private boolean isFirst;
    private boolean isTrain;
    private TrainBroadcastReceiver receiver;
    private OnLockScreenFinish lockSelf = new OnLockScreenFinish() {
        @Override
        public void onUnlockSuccess() {
            Intent intent = new Intent(getInstance().context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            getInstance().context.startActivity(intent);
        }

        @Override
        public void onUnlockFailed(int reason) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            getInstance().context.startActivity(intent);
        }

        @Override
        public boolean onUnlockPassword(String password) {
            return false;
        }
    };
    private BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                GraphKeyguard.userQuit();
            }
        }
    };

    private LockScreenService(Context context) {
        this.context = context;
        preference = PreferenceManager.getDefaultSharedPreferences(context);
        GraphKeyguard = KeyGuardFactory.createKeyGuard(context, KeyGuardFactory.KEYGUARD_PATTERN, null);
        receiver = new TrainBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BROADCAST_GRAPH_TRAIN);
        filter.addAction(Constants.BROADCAST_FIRST_TRAIN);
        filter.addAction(Constants.BROADCAST_LOCKSCREEN_APP);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        context.registerReceiver(screenOffReceiver, intentFilter);

        isFirst = preference.getBoolean(Constants.PRIVACY_FIRST_RUN, true);
        if (isFirst && preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null) != null) {
            preference.edit().putBoolean(Constants.PRIVACY_FIRST_RUN, false).commit();
            isFirst = false;
        }
    }

    public static void initInstance(Context context) {
        _instance = new LockScreenService(context);
    }

    public static LockScreenService getInstance() {
        return _instance;
    }

    public void lockScreen(OnLockScreenFinish observer) {
        if (isFirst) {
            firstObserver = new WeakReference<OnLockScreenFinish>(observer);
            Intent intent = new Intent(context, GuideActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            context.startActivity(intent);
        } else {
            firstObserver = null;
            showDummyActivity();
            GraphKeyguard.showUnlockScreen(new UnlockListener(new WeakReference<OnLockScreenFinish>(observer)), null, 0);
        }
    }

    public void retryUnlock(OnLockScreenFinish observer, String tips, int level) {
        showDummyActivity();
        GraphKeyguard.showUnlockScreen(new RetryUnlockListener(new WeakReference<OnLockScreenFinish>(observer)), tips, level);
    }

    public void hideLockScreen() {
        GraphKeyguard.hideUnlockScreen();
        Intent intent = new Intent();
        intent.setAction(Constants.MSG_BROADCAST_CLOSEDUMMYACTIVITY);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void reset() {
        if (GraphKeyguard != null)
            GraphKeyguard.reset();
    }

    private void showTrainScreen(int type, WeakReference<OnLockScreenFinish> observer) {
        showDummyActivity();
        switch (type) {
            case Constants.TYPE_SERVICE_SHOWTRAIN_GRAPH:
                GraphKeyguard.showTrainingScreen(new TrainListener(observer));
                break;
            case Constants.TYPE_SERVICE_SHOWTRAIN_SETTINGSFORTRAIN:
                isTrain = true;
                GraphKeyguard.showUnlockScreen(new UnlockListener(observer), null, 0);
                break;
            case Constants.TYPE_SERVICE_SHOWTRAIN_FIRST:
                break;
        }
    }

    private void showDummyActivity() {
        Intent intent = new Intent(context, DummyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void showFirstUse() {
        if (isFirst) {
            isFirst = false;
            preference.edit().putBoolean(Constants.PRIVACY_FIRST_RUN, false).commit();
        }
    }

    public interface OnLockScreenFinish {
        public void onUnlockFailed(int reason);

        public void onUnlockSuccess();

        public boolean onUnlockPassword(String password);
    }

    private class RetryUnlockListener implements KeyGuardUnlockListener {
        WeakReference<OnLockScreenFinish> observer;

        public RetryUnlockListener(WeakReference<OnLockScreenFinish> observer) {
            this.observer = observer;
        }

        @Override
        public void reportFailedUnlockAttempt(int reason) {
            if (reason == KeyGuardInterface.Unlock_Fail_UserQuit) {
                hideLockScreen();
                observer.get().onUnlockFailed(reason);

            }
        }

        @Override
        public void reportSuccessfulUnlockAttempt() {
        }

        @Override
        public boolean reportResult(String password) {
            if (observer != null && observer.get() != null) {
                return observer.get().onUnlockPassword(password);
            }
            return false;
        }

        @Override
        public boolean checkDefaultPasswd() {
            return false;
        }
    }

    private class UnlockListener implements KeyGuardUnlockListener {
        WeakReference<OnLockScreenFinish> observer;

        public UnlockListener(WeakReference<OnLockScreenFinish> observer) {
            this.observer = observer;
        }

        @Override
        public void reportFailedUnlockAttempt(int reason) {
            if (reason == KeyGuardInterface.Unlock_Fail_ChangeMode || reason == KeyGuardInterface.General_Fail || reason == KeyGuardInterface.Unlock_Fail_InitFail) {
                GraphKeyguard.showUnlockScreen(new UnlockListener(observer), null, 0);
            } else if (reason == KeyGuardInterface.Unlock_Fail_UserQuit) {
                hideLockScreen();
                isTrain = false;
                if (observer != null && observer.get() != null)
                    observer.get().onUnlockFailed(reason);
            }
            //其他的失败不处理，等待用户重试或手动退出
        }

        @Override
        public void reportSuccessfulUnlockAttempt() {
            if (isTrain) {
                showTrainScreen(Constants.TYPE_SERVICE_SHOWTRAIN_GRAPH, observer);
                isTrain = false;
                return;
            }
            hideLockScreen();
            if (observer != null && observer.get() != null)
                observer.get().onUnlockSuccess();
        }

        @Override
        public boolean reportResult(String password) {
            if (observer != null && observer.get() != null)
                return observer.get().onUnlockPassword(password);
            return false;
        }

        @Override
        public boolean checkDefaultPasswd() {
            return true;
        }
    }

    private class TrainListener implements KeyGuardTrainListener {
        WeakReference<OnLockScreenFinish> observer;

        public TrainListener(WeakReference<OnLockScreenFinish> observer) {
            this.observer = observer;
        }

        @Override
        public void reportTrainingSuccess() {
            hideLockScreen();
            if (observer != null && observer.get() != null)
                observer.get().onUnlockSuccess();
        }

        @Override
        public void reportTrainingFailed(int reason) {
            if (reason == KeyGuardInterface.Train_Fail_UserQuit) {
                hideLockScreen();
            }
            //其他 fail，等待用户重试或手动退出
        }
    }

    private class TrainBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.BROADCAST_GRAPH_TRAIN.equals(intent.getAction())) {
                showTrainScreen(Constants.TYPE_SERVICE_SHOWTRAIN_SETTINGSFORTRAIN, null); //设置里面的重新训练
            } else if (Constants.BROADCAST_FIRST_TRAIN.equals(intent.getAction())) {
                showTrainScreen(Constants.TYPE_SERVICE_SHOWTRAIN_GRAPH, firstObserver);  //初次导向训练
            } else if (Constants.BROADCAST_LOCKSCREEN_APP.equals(intent.getAction())) {
                LockScreenService.getInstance().lockScreen(lockSelf);   //锁屏服务，如果是初次，就进入GuideActivity
            }
        }
    }

}
