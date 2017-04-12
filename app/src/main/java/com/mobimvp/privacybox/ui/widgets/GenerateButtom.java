package com.mobimvp.privacybox.ui.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.mobimvp.privacybox.R;

public class GenerateButtom extends LinearLayout implements
        View.OnClickListener {
    private View.OnClickListener l;
    private View.OnClickListener l2;
    private View.OnClickListener l3;
    private LayoutInflater inflater;
    private View buttomView;

    public GenerateButtom(Context context) {
        super(context);
        initView(context);
    }

    public void add(GenerateButtomButton... button) {
        if (button.length == 1) {
            this.l = button[0].l;
            Button btn1 = (Button) buttomView.findViewById(R.id.btn1);
            btn1.setText(button[0].mText);
            btn1.setVisibility(View.VISIBLE);
            btn1.setOnClickListener(this);
        } else if (button.length == 2) {
            this.l = button[0].l;
            Button btn1 = (Button) buttomView.findViewById(R.id.btn1);
            btn1.setText(button[0].mText);
            btn1.setVisibility(View.VISIBLE);
            btn1.setOnClickListener(this);
            this.l2 = button[1].l;
            Button btn2 = (Button) buttomView.findViewById(R.id.btn2);
            btn2.setText(button[1].mText);
            btn2.setVisibility(View.VISIBLE);
            btn2.setOnClickListener(this);
            Button btn_divider1 = (Button) findViewById(R.id.btn_divider1);
            btn_divider1.setVisibility(View.VISIBLE);
        } else if (button.length == 3) {
            this.l = button[0].l;
            Button btn1 = (Button) buttomView.findViewById(R.id.btn1);
            btn1.setText(button[0].mText);
            btn1.setVisibility(View.VISIBLE);
            btn1.setOnClickListener(this);
            this.l2 = button[1].l;
            Button btn2 = (Button) buttomView.findViewById(R.id.btn2);
            btn2.setText(button[1].mText);
            btn2.setVisibility(View.VISIBLE);
            btn2.setOnClickListener(this);
            this.l3 = button[2].l;
            Button btn3 = (Button) buttomView.findViewById(R.id.btn3);
            btn3.setText(button[2].mText);
            btn3.setVisibility(View.VISIBLE);
            btn3.setOnClickListener(this);
            Button btn_divider1 = (Button) findViewById(R.id.btn_divider1);
            btn_divider1.setVisibility(View.VISIBLE);
            Button btn_divider2 = (Button) findViewById(R.id.btn_divider2);
            btn_divider2.setVisibility(View.VISIBLE);
        } else {
            try {
                throw new Exception("can not add more than 3 items");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void initView(Context context) {
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        buttomView = inflater.inflate(R.layout.generatebottom, this, true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                l.onClick(v);
                break;
            case R.id.btn2:
                l2.onClick(v);
                break;
            case R.id.btn3:
                l3.onClick(v);
                break;
            default:
                break;
        }
    }

    public static class GenerateButtomButton {
        public View.OnClickListener l;
        public String mText;
        public int mStyle;

        public GenerateButtomButton(View.OnClickListener listener, String mText) {
            this.l = listener;
            this.mText = mText;
        }
    }

}
