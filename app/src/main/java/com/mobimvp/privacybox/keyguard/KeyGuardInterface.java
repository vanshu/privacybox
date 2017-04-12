package com.mobimvp.privacybox.keyguard;

public class KeyGuardInterface {


    public static final int General_Fail = -1;
    public static final int Unlock_Fail_UserQuit = 0;
    public static final int Unlock_Fail_InitFail = 1;
    public static final int Unlock_Fail_NoTrain = 2;
    public static final int Unlock_Fail_InputError = 3;
    public static final int Unlock_Fail_ChangeMode = 4;


    public static final int Train_Fail_UserQuit = 0;
    public static final int Train_Fail_InitFail = 1;
    public static final int Train_Fail_InputError = 3;

    public interface KeyGuardUnlockListener {
        void reportFailedUnlockAttempt(int reason);

        void reportSuccessfulUnlockAttempt();

        boolean reportResult(String password);

        boolean checkDefaultPasswd();
    }

    public interface KeyGuardTrainListener {
        void reportTrainingSuccess();

        void reportTrainingFailed(int reason);
    }

    public interface KeyGuardAsyncInitListener {
        void reportInitOver();
    }

    public interface KeyGuard {
        public void showTrainingScreen(KeyGuardTrainListener listener);

        public void showUnlockScreen(KeyGuardUnlockListener listener, String tip, int arg0);

        public void hideTrainingScreen();

        public void hideUnlockScreen();

        public void userQuit();

        public boolean trained();

        public void release();

        public void reset();
    }
}
