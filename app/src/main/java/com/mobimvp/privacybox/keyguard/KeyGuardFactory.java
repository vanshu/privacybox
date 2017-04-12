package com.mobimvp.privacybox.keyguard;

import android.content.Context;

import com.mobimvp.privacybox.keyguard.KeyGuardInterface.KeyGuard;
import com.mobimvp.privacybox.keyguard.KeyGuardInterface.KeyGuardAsyncInitListener;

public class KeyGuardFactory {
    public static final int KEYGUARD_PATTERN = 0x0;

    public static KeyGuard createKeyGuard(Context context, int type, KeyGuardAsyncInitListener listener) {
        switch (type) {
            case KEYGUARD_PATTERN:
                return new KeyGuardPattern(context, listener);
        }
        return null;
    }

}
