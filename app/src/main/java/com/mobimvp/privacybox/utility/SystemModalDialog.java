package com.mobimvp.privacybox.utility;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.mobimvp.privacybox.PBApplication;
import com.mobimvp.privacybox.R;

public class SystemModalDialog {

    private static SystemModalDialog _instance;
    private WindowManager mWindowManager;
    private LayoutInflater mLayoutInflater;

    private SystemModalDialog(Context context) {
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mLayoutInflater = LayoutInflater.from(context);
    }

    public static synchronized SystemModalDialog getInstance() {
        if (_instance == null) {
            _instance = new SystemModalDialog(PBApplication.getApplication());
        }

        return _instance;
    }

    private LayoutParams populateFloatWindow() {
        LayoutParams mLayoutParams = new LayoutParams();
        mLayoutParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.gravity = Gravity.CENTER;
        mLayoutParams.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;

        return mLayoutParams;
    }

    private void populateDialog(View dialog, CharSequence title, CharSequence message, CharSequence positive, View.OnClickListener positiveListener, CharSequence negative,
                                View.OnClickListener negativeListener, CharSequence neutral, View.OnClickListener neutralListener) {
        LayoutParams params = populateFloatWindow();
        TextView titleView = (TextView) dialog.findViewById(R.id.alert_title);
        titleView.setText(title);
        TextView messageView = (TextView) dialog.findViewById(R.id.message);
        messageView.setText(message);
        Button positiveButton = (Button) dialog.findViewById(R.id.button1);
        if (positive != null) {
            positiveButton.setVisibility(View.VISIBLE);
            positiveButton.setText(positive);
            positiveButton.setOnClickListener(positiveListener);
        } else {
            positiveButton.setVisibility(View.GONE);
        }

        Button negativeButton = (Button) dialog.findViewById(R.id.button2);
        if (negative != null) {
            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setText(negative);
            negativeButton.setOnClickListener(negativeListener);
        } else {
            negativeButton.setVisibility(View.GONE);
        }

        Button neutralButton = (Button) dialog.findViewById(R.id.button3);
        if (neutral != null) {
            neutralButton.setVisibility(View.VISIBLE);
            neutralButton.setText(neutral);
            neutralButton.setOnClickListener(neutralListener);
        } else {
            neutralButton.setVisibility(View.GONE);
        }

        try {
            mWindowManager.addView(dialog, params);
        } catch (Exception e) {
        }
    }

    public View createGenericDialog(View attachDialog, CharSequence title, CharSequence message, CharSequence positive, final View.OnClickListener positiveListener, CharSequence negative,
                                    final View.OnClickListener negativeListener, CharSequence neutral, final View.OnClickListener neutralListener) {

        final View dialog = attachDialog == null ? mLayoutInflater.inflate(R.layout.privacy_dialog, null) : attachDialog;
        View.OnClickListener wrappedPositive = null, wrappedNegative = null, wrappedNeutral = null;
        if (positive != null) {
            wrappedPositive = new View.OnClickListener() {
                public void onClick(View v) {
                    if (positiveListener != null) {
                        positiveListener.onClick(v);
                    }
                    try {
                        mWindowManager.removeView(dialog);
                    } catch (Exception e) {
                    }
                }
            };
        }
        if (negative != null) {
            wrappedNegative = new View.OnClickListener() {
                public void onClick(View v) {
                    if (negativeListener != null) {
                        negativeListener.onClick(v);
                    }
                    try {
                        mWindowManager.removeView(dialog);
                    } catch (Exception e) {
                    }
                }
            };
        }
        if (neutral != null) {
            wrappedNeutral = new View.OnClickListener() {
                public void onClick(View v) {
                    if (neutralListener != null) {
                        neutralListener.onClick(v);
                    }
                    try {
                        mWindowManager.removeView(dialog);
                    } catch (Exception e) {
                    }
                }
            };
        }
        populateDialog(dialog, title, message, positive, wrappedPositive, negative, wrappedNegative, neutral, wrappedNeutral);

        return dialog;
    }

    /**
     * 显示正在加载对话框
     *
     * @param message 消息
     * @return 对话框的View，可用在hideProgressDialog中
     */
    public View showProgressDialog(CharSequence message) {
        final View dialog = mLayoutInflater.inflate(R.layout.widget_progress_dialog, null);
        TextView msgView = (TextView) dialog.findViewById(R.id.message);
        msgView.setText(message);
        LayoutParams params = populateFloatWindow();
        try {
            mWindowManager.addView(dialog, params);
        } catch (Exception e) {
        }

        return dialog;
    }

    /**
     * 隐藏对话框
     *
     * @param dialog
     */
    public void hideDialog(View dialog) {
        try {
            mWindowManager.removeView(dialog);
        } catch (Exception e) {
        }
    }
}
