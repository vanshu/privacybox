package com.mobimvp.privacybox.ui.guide;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;

import com.mobimvp.privacybox.Constants;
import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.ui.BaseActivity;
import com.mobimvp.privacybox.ui.widgets.ClearEditText;
import com.mobimvp.privacybox.ui.widgets.ClearEditText.OnClearListener;
import com.mobimvp.privacybox.utility.SystemInfo;

public class DialPhoneGuideActivity extends BaseActivity {
    private static final int STR_LENGHT = 4;
    private ClearEditText mEditTextContent;
    private GridView mGridView;
    private Button mBtnBottom;
    private String[] numbers = new String[]{"1", "2", "3", "4", "5", "6",
            "7", "8", "9", "*", "0", "#"};
    private StringBuffer currentString = new StringBuffer();
    private SharedPreferences preference;
    private CheckBox mHideLanucher;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    NumberPanelAdapter adapter = new NumberPanelAdapter(msg.arg1);
                    mGridView.setAdapter(adapter);
                    break;

                default:
                    break;
            }
        }

        ;
    };
    private OnItemClickListener listener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                long arg3) {
            switch (position) {
                case 0:
                    currentString.append("1");
                    break;
                case 1:
                    currentString.append("2");
                    break;
                case 2:
                    currentString.append("3");
                    break;
                case 3:
                    currentString.append("4");
                    break;
                case 4:
                    currentString.append("5");
                    break;
                case 5:
                    currentString.append("6");
                    break;
                case 6:
                    currentString.append("7");
                    break;
                case 7:
                    currentString.append("8");
                    break;
                case 8:
                    currentString.append("9");
                    break;
                case 9:
                    currentString.append("*");
                    break;
                case 10:
                    currentString.append("0");
                    break;
                case 11:
                    currentString.append("#");
                    break;
                default:
                    break;
            }
            mEditTextContent.setText(currentString.toString());
            if (currentString.toString().length() >= STR_LENGHT) {
                mBtnBottom.setBackgroundResource(R.drawable.btn_green);
                mBtnBottom.setText("确定");
                mBtnBottom.setTextColor(getResources().getColor(
                        R.color.textcolor_white));
            }
        }
    };
    private View.OnClickListener mBottomListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (currentString.toString().length() >= STR_LENGHT) {
                preference = PreferenceManager.getDefaultSharedPreferences(DialPhoneGuideActivity.this);
                preference.edit().putString(Constants.PRIVACY_DIAL_PHONE_NUMBER, currentString.toString()).commit();
                Intent intent = new Intent();
                intent.setAction(Constants.BROADCAST_FIRST_TRAIN);
                LocalBroadcastManager.getInstance(DialPhoneGuideActivity.this).sendBroadcast(intent);
                intent = new Intent();
                intent.setAction(Constants.BROADCAST_CLOSE_GUIDEACTIVITY);
                LocalBroadcastManager.getInstance(DialPhoneGuideActivity.this).sendBroadcast(intent);
                if (mHideLanucher.isChecked()) {  //是否勾选隐藏桌面图标
                    SystemInfo.setComponentEnable(DialPhoneGuideActivity.this, false);
                    preference.edit().putBoolean(Constants.PRIVACY_HIDE_LANUCHER, true).commit();
                }
                //增加模式值
                preference.edit().putString(Constants.PRIVACY_UNLOCKMODE, "2").commit();
                finish();
            } else {
                DialPhoneGuideActivity.this.finish();
            }
        }
    };
    private OnClearListener onClearListener = new OnClearListener() {
        @Override
        public void onClick() {
            currentString = new StringBuffer("");
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPrivacyActionBar().setTitle("拨号解锁");
        setContentView(R.layout.dialphoneguide);
        preference = PreferenceManager.getDefaultSharedPreferences(this);
        mEditTextContent = (ClearEditText) findViewById(R.id.et_content);
        mHideLanucher = (CheckBox) findViewById(R.id.ck_hide_lanucher);
        mEditTextContent.setOnClearListener(onClearListener);
        mGridView = (GridView) findViewById(R.id.gv_number_panel);
        mBtnBottom = (Button) findViewById(R.id.btn_bottom);
        mGridView.setOnItemClickListener(listener);
        mBtnBottom.setOnClickListener(mBottomListener);
        ViewTreeObserver viewTreeObserver = mGridView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver
                    .addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mGridView.getViewTreeObserver()
                                    .removeOnGlobalLayoutListener(this);
                            int viewWidth = mGridView.getWidth();
                            int viewHeight = mGridView.getHeight();
                            System.out.println("px: width:" + viewWidth
                                    + ",height: " + viewHeight);
                            Message message = new Message();
                            message.arg1 = viewHeight;
                            message.what = 0;
                            mHandler.sendMessage(message);
                        }
                    });

        }
    }

    private class NumberPanelAdapter extends BaseAdapter {
        private int gridViewHeight = 0;

        public NumberPanelAdapter(int gridviewHeight) {
            this.gridViewHeight = gridviewHeight;
        }

        @Override
        public int getCount() {
            return numbers.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Button button = new Button(DialPhoneGuideActivity.this);
            button.setClickable(false);
            button.setFocusable(false);
            button.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    (gridViewHeight - (int) SystemInfo.dip2px(
                            DialPhoneGuideActivity.this, 6 * 3)) // 6dp
                            // 是verticalSpacing设置的值，因为有3个，所以*3.
                            / (numbers.length / mGridView.getNumColumns())));
            button.setText(numbers[position]);
            return button;
        }

    }

}
