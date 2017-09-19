package com.mobimvp.privacybox.utility;

import android.app.Activity;

/**
 * 提供转屏、更新进度支持的AsyncLoader，接口类似于AsyncTask
 * 使用方式：直接继承此类即可。为了避免Activity传递至作用域之外导致内存泄露，请避免在子类中保存Activity
 * 如果需要处理转屏，则需要在Activity的onRetainNonConfigurationInstance中保存此Loader，在随后的onUICreate中重新获取此Loader
 * execute方法会自动处理转屏等情况，避免重复加载的问题
 * 
 * @param <T>	同此AsyncLoader关联的Activity，用于显示结果。
 * @param <D>  结果，例如ArrayList<APKFile>
 * @param <P>  用于提供进度，进度由您自己定义，通常为Integer, 范围从0-100
 */
public abstract class AsyncLoader<T extends Activity, D, P> {
	private T activity;
	private P progress;
	private D result;
	private boolean canceled;
	private boolean broadcasted;
	private boolean reAttached;
	
	private Thread loaderThread = new Thread() {
		@Override
		public void run() {
			result = doInBackground();
			if (canceled) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						synchronized (AsyncLoader.this) {
							onCancelled(result);
							broadcasted = true;
							reAttached = false;
						}
					}
					
				});
			} else {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						synchronized (AsyncLoader.this) {
							onPostExecute(result);
							broadcasted = true;
							reAttached = false;
						}
					}
				});
			}
		}
	};
	
	public AsyncLoader() {
		this.canceled = false;
		this.broadcasted = false;
		this.reAttached = false;
	}
	
	public final boolean cancel() {
		if (canceled || loaderThread.getState() == Thread.State.TERMINATED) {
			return false;
		}
		
		canceled = true;
		return true;
	}
	
	public final boolean isCanceled() {
		return canceled;
	}
	
	public void sync(D result) {
		this.result = this.onSyncWithActivity(result);
	}
	
	protected final T getActivity() {
		return this.activity;
	}
	
	public final void execute(T activity) {
		synchronized (this) {
			if (this.activity != activity && this.activity != null) {
				this.reAttached = true;
			}
			this.activity = activity;
			
			if (loaderThread.getState() == Thread.State.NEW) {
				if (!canceled) {
					onPreExecute();
					loaderThread.start();
				} else {
					throw new IllegalStateException();
				}
			} else {
				if (reAttached) {
					if (progress != null) {
						onProgressUpdate(progress);
					}
					if (broadcasted) {
						if (canceled) {
							onCancelled(result);
						} else {
							onPostExecute(result);
						}
						reAttached = false;
					}
				}
			}
		}
	}
	
	protected final void publishProgress(final P newProgress) {
		if (newProgress != null) {
			this.progress = newProgress;
			
			this.activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onProgressUpdate(newProgress);
				}
			});
		}
	}
	
	protected D onSyncWithActivity(D result) {
		return result;
	}
	
	protected abstract D doInBackground();
	
	protected void onPostExecute(D loadResult) {
	}
	
	protected void onPreExecute() {
	}
	
	protected void onCancelled(D result) {
	}
	
	protected void onProgressUpdate(P progress) {
	}
}
