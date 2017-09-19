package com.mobimvp.privacybox.keyguard;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.mobimvp.privacybox.Constants;
import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.keyguard.KeyGuardInterface.KeyGuardAsyncInitListener;
import com.mobimvp.privacybox.keyguard.KeyGuardInterface.KeyGuardTrainListener;
import com.mobimvp.privacybox.keyguard.KeyGuardInterface.KeyGuardUnlockListener;
import com.mobimvp.privacybox.ui.widgets.FloatView;
import com.mobimvp.privacybox.ui.widgets.FloatView.BackPressListener;
import com.mobimvp.privacybox.ui.widgets.LockPatternView.Cell;
import com.mobimvp.privacybox.ui.widgets.LockPatternView.DisplayMode;
import com.mobimvp.privacybox.ui.widgets.LockPatternView.OnPatternListener;

public class KeyGuardPattern implements KeyGuardInterface.KeyGuard {


	
	private Runnable clearPatternAction = new Runnable() {
		@Override
		public void run() {
			if (view != null) {
				view.pattern.clearPattern();
				showTip(R.string.Privacy_lock_promptGraphTipNormal);
			}
		}
	};

	private Context context;
	private WindowManager wm;
	private SharedPreferences preference;
	private Handler handler;

	private FloatView view;

	private int retryCnt;
	private int retryDelay;
	private int retriedCnt;
	private int retriedDelay;

	public KeyGuardPattern(Context context, KeyGuardAsyncInitListener listener) {
		this.context = context;
		this.wm = (WindowManager) this.context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		this.preference = PreferenceManager.getDefaultSharedPreferences(context);
		this.handler = new Handler(Looper.getMainLooper());
		if (listener != null) {
			listener.reportInitOver();
		}
		reset();

	}
	@Override
	public void userQuit() {
		if(view != null)
			view.userQuit();
		
	}
	@Override
	public boolean trained() {
		return preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null) != null;
	}

	@Override
	public void showTrainingScreen(KeyGuardTrainListener listener) {
		hide();
		showFirstTrainPage(listener);
	}

	private String userTip;

	@Override
	public void showUnlockScreen(KeyGuardUnlockListener listener, String tip, int level) {
		hide();
		userTip = tip;
		try {
			retryCnt=5;
		//	retryCnt = Integer.parseInt(preference.getString(Constants.PRIVACY_PATTERN_RETRYCNT, Constants.DEFAULT_RETRYCNT));
		} catch (Exception e) {
			retryCnt = 5;
		}
		try {
			retryDelay=30;
		//	retryDelay = Integer.parseInt(preference.getString(Constants.PRIVACY_PATTERN_RETRYDELAY, Constants.DEFAULT_RETRYDELAY));
		} catch (NumberFormatException e) {
			retryDelay = 30;
		}
		showUnlockPage(listener);
	}

	@Override
	public void hideTrainingScreen() {
		hide();
	}

	@Override
	public void hideUnlockScreen() {
		hide();
	}

	@Override
	public void release() {

	}

	@Override
	public void reset() {
		retriedCnt = retriedDelay = 0;
	}

	private void show() {
		if (view == null) {
			view = new FloatView(new ContextThemeWrapper(context, R.style.PRIVACYBOX_Theme));
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			lp.width = WindowManager.LayoutParams.MATCH_PARENT;
			lp.height = WindowManager.LayoutParams.MATCH_PARENT;
			lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

			try {
				wm.addView(view, lp);
			} catch (Exception e) {
			}
		}
		try {
			if (preference.getBoolean(Constants.PRIVACY_UNLOCK_HAPTIC, true)) {
				boolean flag = Settings.System.getInt(context.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) != 0;
				view.pattern.setHapticFeedbackEnabled(flag);
				view.pattern.setTactileFeedbackEnabled(flag);
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			view.pattern.setHapticFeedbackEnabled(false);
			view.pattern.setTactileFeedbackEnabled(false);
		}
	}

	private void hide() {
		try {
			view.setBackPressListener(null);
			view.pattern.setOnPatternListener(null);
			wm.removeView(view);
		} catch (Exception e) {
		}

		view = null;
	}

	private void rollbackConfig() {
		String currentLevel = preference.getString(Constants.PRIVACY_CURRENT_LEVEL, "" + Constants.DEFAULT_LEVEL);
		String newlevel = preference.getString(Constants.PRIVACY_PATTERN_LEVEL, "" + Constants.DEFAULT_LEVEL);
		if (!currentLevel.equals(newlevel)) {
			preference.edit().putString(Constants.PRIVACY_PATTERN_LEVEL, currentLevel).commit();
		}
	}

	private void showFirstTrainPage(final KeyGuardTrainListener trainListener) {
		show();
		view.cancel.setVisibility(View.INVISIBLE);
		showTip(R.string.Privacy_lock_promptGraphReset);
		view.setBackPressListener(new BackPressListener() {
			@Override
			public boolean onBackPressed() {
				try {
					rollbackConfig();
					trainListener.reportTrainingFailed(KeyGuardInterface.Train_Fail_UserQuit);
				} catch (Exception e) {
				}
				return true;
			}
		});
		view.pattern.clearPattern();
		view.pattern.setDisplayMode(DisplayMode.Wrong);
		view.pattern.setOnPatternListener(new OnPatternListener() {

			@Override
			public void onPatternStart() {
				handler.removeCallbacksAndMessages(null);
				showTip(R.string.Privacy_lock_promptGraphTipNormal);
				view.pattern.setDisplayMode(DisplayMode.Wrong);
			}

			@Override
			public void onPatternCleared() {
			}

			@Override
			public void onPatternCellAdded(List<Cell> pattern) {
				if (pattern.size() >= 4) {
					view.pattern.setDisplayMode(DisplayMode.Correct);
				}
			}

			@Override
			public void onPatternDetected(List<Cell> pattern) {
				if (pattern.size() < 4) {
					handler.postDelayed(clearPatternAction, 1000);
					view.tip.setText(R.string.Privacy_lock_promptGraphTipShort);
				} else {
					showSecondTrainPage(trainListener, view.encodePattern(pattern));
				}
			}

		});
	}

	private void showSecondTrainPage(final KeyGuardTrainListener trainListener, final String password) {
		show();
		view.cancel.setVisibility(View.VISIBLE);
		view.cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				rollbackConfig();
				showFirstTrainPage(trainListener);
			}
		});
		view.tip.setText(R.string.Privacy_lock_promptGraphResetConfirm);
		view.setBackPressListener(new BackPressListener() {
			@Override
			public boolean onBackPressed() {
				rollbackConfig();
				showFirstTrainPage(trainListener);
				return true;
			}
		});
		view.pattern.clearPattern();
		view.pattern.setDisplayMode(DisplayMode.Wrong);
		view.pattern.setOnPatternListener(new OnPatternListener() {

			@Override
			public void onPatternStart() {
				handler.removeCallbacksAndMessages(null);
				view.tip.setText(R.string.Privacy_lock_promptGraphTipNormal);
				view.pattern.setDisplayMode(DisplayMode.Wrong);
			}

			@Override
			public void onPatternCleared() {
			}

			@Override
			public void onPatternCellAdded(List<Cell> pattern) {
				if (pattern.size() >= 4) {
					view.pattern.setDisplayMode(DisplayMode.Correct);
				}
			}

			@Override
			public void onPatternDetected(List<Cell> pattern) {
				if (pattern.size() < 4) {
					handler.postDelayed(clearPatternAction, 1000);
					view.tip.setText(R.string.Privacy_lock_promptGraphTipShort);
				} else {
					String passwordConfirm = view.encodePattern(pattern);
					if (!passwordConfirm.equals(password)) {
						view.pattern.setDisplayMode(DisplayMode.Wrong);
						view.tip.setText(R.string.Privacy_lock_promptGraphTipNotConfirm);
						handler.postDelayed(clearPatternAction, 1000);
					} else {
						Toast.makeText(context, R.string.Privacy_lock_promptGraphResetOk, Toast.LENGTH_SHORT).show();
						Editor editor = preference.edit();
						editor.putString(Constants.PRIVACY_PATTERN_PASSWORD, password);
						editor.commit();
						hide();
						try {
							trainListener.reportTrainingSuccess();
						} catch (Exception e) {
						}
					}
				}
			}
		});
	}

	private void showTip(int id) {
		if (userTip == null) {
			view.tip.setText(id);
		} else {
			view.tip.setText(userTip);
		}
	}

	private void showUnlockPage(final KeyGuardUnlockListener unlockListener) {
		show();
		view.setBackPressListener(new BackPressListener() {
			@Override
			public boolean onBackPressed() {
				try {
					rollbackConfig();
					unlockListener.reportFailedUnlockAttempt(KeyGuardInterface.Unlock_Fail_UserQuit);
				} catch (Exception e) {
				}
				return true;
			}
		});
		if (retryCnt > retriedCnt)
			showTip(R.string.Privacy_lock_promptGraphTipNormal);
		else {
			view.tip.setText(context.getString(R.string.Privacy_lock_promptGraphFailed, retryCnt, retryDelay));
			view.pattern.setEnabled(false);
		}
		view.pattern.clearPattern();
		view.pattern.setDisplayMode(DisplayMode.Wrong);
		view.pattern.setOnPatternListener(new OnPatternListener() {

			@Override
			public void onPatternStart() {
				handler.removeCallbacksAndMessages(null);
				view.tip.setText(R.string.Privacy_lock_promptGraphTipNormal);
				view.pattern.setDisplayMode(DisplayMode.Wrong);
			}

			@Override
			public void onPatternCleared() {
			}

			@Override
			public void onPatternCellAdded(List<Cell> pattern) {
				if (pattern.size() >= 4) {
					view.pattern.setDisplayMode(DisplayMode.Correct);
				}
			}

			@Override
			public void onPatternDetected(List<Cell> pattern) {
				if (retryCnt < 0) {
					view.pattern.clearPattern();
				} else if (pattern.size() < 4) {
					handler.postDelayed(clearPatternAction, 1000);
					view.tip.setText(R.string.Privacy_lock_promptGraphTipShort);
				} else {
					String password = view.encodePattern(pattern);
					try {
						if (unlockListener.reportResult(password))
							return;
					} catch (Exception e) {
					}
					try {
						if (unlockListener.checkDefaultPasswd()) {
							if (password.equals(preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null))) {
								reset();
								hide();
								try {
									unlockListener.reportSuccessfulUnlockAttempt();
								} catch (Exception e) {
								}
								return;
							}
						}
					} catch (Exception e) {
					}

					retriedCnt++;
					if (retryCnt <= retriedCnt) {
						view.tip.setText(context.getString(R.string.Privacy_lock_promptGraphFailed, retryCnt, retryDelay));
						view.pattern.setDisplayMode(DisplayMode.Wrong);
						view.pattern.clearPattern();
						view.pattern.setEnabled(false);
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								retriedDelay++;
								if (retryDelay > retriedDelay && retryCnt <= retriedCnt) {
									if (view != null)
										view.tip.setText(context.getString(R.string.Privacy_lock_promptGraphFailed, retryCnt, retryDelay - retriedDelay));
									handler.postDelayed(this, 1000);
								} else {
									reset();
									if (view != null) {
										showTip(R.string.Privacy_lock_promptGraphTipNormal);
										view.pattern.setEnabled(true);
									}
								}
							}
						}, 1000);
					} else {
						view.pattern.setDisplayMode(DisplayMode.Wrong);
						view.tip.setText(R.string.Privacy_lock_promptGraphTipFailed);
						handler.postDelayed(clearPatternAction, 1000);
					}

					try {
						unlockListener.reportFailedUnlockAttempt(KeyGuardInterface.Unlock_Fail_InputError);
					} catch (Exception e) {
					}
				}
			}

		});
	}

}
