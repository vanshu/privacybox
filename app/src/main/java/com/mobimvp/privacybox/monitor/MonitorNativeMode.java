package com.mobimvp.privacybox.monitor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.util.Log;

import com.mobimvp.privacybox.monitor.MonitorInterface.MonitorListener;

public class MonitorNativeMode implements MonitorInterface.PackageMonitor{

	private MonitorListener listener;

	MonitorNativeMode(Context context, MonitorListener listener){
		this.listener = listener;
	}
		
	@Override
	public void stop() {
		if(monitor != null){
			monitor.close();
			monitor = null;
			listener = null;
		}
	}

	@Override
	public void start() {
		if(monitor == null){
			monitor = new LogMonitor();
			monitor.start();
		}
	}

	
	private LogMonitor monitor;

	class LogMonitor extends Thread {
		private boolean stop = false;
		public void close(){
			stop = true;
		}
		@Override
		public void run() {
			Process process = null;
			InputStream inputstream;
			BufferedReader bufferedreader;
			try {
				//radio — 查看缓冲区的相关的信息.
				//events — 查看和事件相关的的缓冲区.
				//main — 查看主要的日志缓冲区
				//-c 清楚屏幕上的日志. -c只能放在最后输入 次序都不能颠倒
				process = Runtime.getRuntime().exec("logcat -b events -c");
				process.waitFor();
			} catch (Exception e) {
			}
			try {
				process = Runtime.getRuntime().exec("logcat -b events");
				inputstream = process.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
				bufferedreader = new BufferedReader(inputstreamreader);
				String str = "";
				while ((str = bufferedreader.readLine()) != null) {
					if(stop)
						break;
					Log.w("LogMonitor", str + " :"+listener);
					String cs[] = str.split(",|/|[|]");
					if(cs.length > 2){
						if(cs[1].startsWith("am_create_activity")){
							if(listener != null && cs.length > 3 ){
								listener.onTopPackageChange(cs[3]);
								Log.w("LogMonitor", "am_create_activity:"+cs[3]);
							}
						}else if(cs[1].startsWith("am_proc_start")){
							if(listener != null && cs.length > 4 && cs[4].equals("activity")){
								listener.onTopPackageChange(cs[3]);
								Log.w("LogMonitor", "am_proc_start:" + cs[3]);
							}
						}else if(cs[1].startsWith("am_resume_activity")){
							if(listener != null && cs.length > 3){
								listener.onTopPackageChange(cs[3]);
								Log.w("LogMonitor", "am_resume_activity:" + cs[3]);
							}
						}else if(cs[1].startsWith("am_restart_activity")){
							if(listener != null && cs.length > 3){
								listener.onTopPackageChange(cs[3]);
								Log.w("LogMonitor", "am_restart_activity:" + cs[3]);
							}
						}
					}
				}
			} catch (Exception e) {
			}
			try {
				process.destroy();
			} catch (Exception e) {
			}
		}
	}
}
