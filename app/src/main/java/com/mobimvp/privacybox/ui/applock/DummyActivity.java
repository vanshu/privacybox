package com.mobimvp.privacybox.ui.applock;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.mobimvp.privacybox.Constants;
import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.ui.widgets.RevealColorView;

public class DummyActivity extends Activity {
	private MyReceiver receiver;
	private boolean isRegist;
	private RevealColorView mRevealColorView;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parent);
		System.out.println("DummyActivity");
		mRevealColorView=(RevealColorView)findViewById(R.id.reveal_view);
		receiver=new MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(Constants.MSG_BROADCAST_CLOSEDUMMYACTIVITY);
        LocalBroadcastManager.getInstance(DummyActivity.this).registerReceiver(receiver,filter);
        isRegist = true;
	}
	
	public void onDestroy(){
		super.onDestroy();
		if(isRegist){
			LocalBroadcastManager.getInstance(DummyActivity.this).unregisterReceiver(receiver);
			isRegist = false;
		}
	}
	@Override
	protected void onResume() {
        super.onResume();
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
	private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	DummyActivity.this.finish();
        //	mRevealColorView.reveal(0, 0, Color.parseColor("#8bc34a"), mAnimationListener);
        }
	}
	
	private AnimatorListener mAnimationListener=new AnimatorListener() {
		
		@Override
		public void onAnimationStart(Animator animation) {
			
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
			
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			DummyActivity.this.finish();
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
			
		}
	};
}
