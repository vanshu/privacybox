package com.mobimvp.privacybox.service.filelocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mobimvp.privacybox.Constants;
import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.keyguard.KeyGuardInterface;
import com.mobimvp.privacybox.service.LockScreenService;
import com.mobimvp.privacybox.service.LockScreenService.OnLockScreenFinish;
import com.mobimvp.privacybox.service.filelocker.AsyncOperations.AsyncDecryptor;
import com.mobimvp.privacybox.service.filelocker.AsyncOperations.AsyncEncryptor;
import com.mobimvp.privacybox.service.filelocker.AsyncOperations.AsyncTempDecryptor;
import com.mobimvp.privacybox.service.filelocker.internal.DBAction;
import com.mobimvp.privacybox.service.filelocker.internal.EncryptItem;
import com.mobimvp.privacybox.service.filelocker.internal.PasswordItem;
import com.mobimvp.privacybox.utility.SystemInfo;
import com.mobimvp.privacybox.utility.SystemModalDialog;
import com.mobimvp.privacybox.utility.crypto.EncryptListener;
import com.mobimvp.privacybox.utility.crypto.EncryptionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileLocker {

    private static final String TAG = "FileLocker";
    private static FileLocker _instance;
    private boolean initialed = false;
    private SharedPreferences preference;
    private Context context;
    private HashMap<String, EncryptionManager> encryptionManagerMap = new HashMap<String, EncryptionManager>();
    private View importDialog = null;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//			Log.w(TAG, "<BroadcastReceiver> SD:" + action);
            if (Intent.ACTION_MEDIA_REMOVED.equals(action) || Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action) || Intent.ACTION_MEDIA_EJECT.equals(action) || Intent.ACTION_MEDIA_UNMOUNTED.equals(action)
                    || Intent.ACTION_MEDIA_SHARED.equals(action)) {
                encryptionManagerMap.clear();
            } else if (Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())) {
                String password = preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null);
                if (password != null) {
                    createEncryption(password);
                }
            }
        }
    };
    private OnSharedPreferenceChangeListener preferenceChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (Constants.PRIVACY_PATTERN_PASSWORD.equals(key)) {
                String password = preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null);
//				Log.w(TAG, "<preferenceChangeListener> new password set: " + password);
                if (password == null)
                    return;
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    LockScreenService.getInstance().reset();
                    LockScreenService.getInstance().showFirstUse();
                    encryptionManagerMap.clear();
                    importDialog = null;
                    return;
                }
                // 首次运行+密码不匹配+数据库有数据，弹出数据导入对话框
                if (preference.getBoolean(Constants.PRIVACY_FIRST_RUN, true)) {
                    if (showImportDialog(null, password))
                        return;
                }
                updatePassword();
                LockScreenService.getInstance().showFirstUse();
            }
        }
    };

    private FileLocker(Context context) {
        this.context = context;
//		Log.w(TAG, "<FileLocker> construct ... ");
        preference = PreferenceManager.getDefaultSharedPreferences(context);
        preference.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    public static void initInstance(Context context) {
        _instance = new FileLocker(context);
    }

    public static FileLocker getInstance() {
        _instance.initial();
        return _instance;
    }

    /**
     * 获取指定类型加密数据列表。SD卡存在密码不符合的旧数据时，会自动弹出导入/清空提示框
     *
     * @param type == Constants.Type_File / Constants.Type_Video /
     *             Constants.Type_Photo
     */
    public List<EncryptItem> getEncryptedItems(int type) {
        if (encryptionManagerMap.containsKey(DBAction.getSdcardRootPath())) {
            return DBAction.selectAll(type);
        }
        showImportDialog(null, preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null));
        return new ArrayList<EncryptItem>();
    }

    /**
     * 完全加密，将队列中的加密项一一加密。SD卡存在密码不符合的旧数据时，会自动弹出导入/清空提示框，加密操作被取消.
     * observer连续触发onError()/onFinish()
     *
     * @param items    待加密文件列表
     * @param observer 加密操作回调
     */
    public void encryptFile(List<EncryptItem> items, OperateListener observer) {
        if (encryptionManagerMap.containsKey(DBAction.getSdcardRootPath())) {
            boolean fullEncrypt = preference.getBoolean(Constants.PRIVACY_FILE_FULL, false);
            AsyncEncryptor enc = new AsyncEncryptor(items, observer, encryptionManagerMap.get(DBAction.getSdcardRootPath()), fullEncrypt);
            AsyncOperations.exeucteOperation(enc);
            return;
        }
        showImportDialog(observer, preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null));
    }

    public void encryptFile(EncryptItem item, OperateListener observer) {
        if (encryptionManagerMap.containsKey(DBAction.getSdcardRootPath())) {
            boolean fullEncrypt = preference.getBoolean(Constants.PRIVACY_FILE_FULL, false);
            AsyncEncryptor enc = new AsyncEncryptor(item, observer, encryptionManagerMap.get(DBAction.getSdcardRootPath()), fullEncrypt);
            AsyncOperations.exeucteOperation(enc);
            return;
        }
        showImportDialog(observer, preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null));
    }

    /**
     * 取消完全加密，所有加密队列全部取消。成功取消后，所有正在执行的加密操作，对应回调observer都会收到onError() /
     * onFinish()。 SD卡存在密码不符合的旧数据时，会自动弹出导入/清空提示框，取消操作无效。
     */
    public void cancelEncrypt() {
        // TODO implement this
        // showImportDialog(null, preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null));
    }

    /**
     * 完全解密，将队列中的解密项一一还原。SD卡存在密码不符合的旧数据时，会自动弹出导入/清空提示框，解密操作被取消。
     * observer连续触发onError()/onFinish()。
     *
     * @param items    待解密列表
     * @param observer 解密操作回调
     */
    public void decryptFile(List<EncryptItem> items, OperateListener observer) {
        if (encryptionManagerMap.containsKey(DBAction.getSdcardRootPath())) {
            AsyncDecryptor dec = new AsyncDecryptor(items, observer, encryptionManagerMap.get(DBAction.getSdcardRootPath()));
            AsyncOperations.exeucteOperation(dec);
            return;
        }
        showImportDialog(observer, preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null));
    }

    public void decryptFile(EncryptItem item, OperateListener observer) {
        if (encryptionManagerMap.containsKey(DBAction.getSdcardRootPath())) {
            AsyncDecryptor dec = new AsyncDecryptor(item, observer, encryptionManagerMap.get(DBAction.getSdcardRootPath()));
            AsyncOperations.exeucteOperation(dec);
            return;
        }
        showImportDialog(observer, preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null));
    }

    /**
     * 取消完全解密，所有解密队列全部取消。成功取消后，所有正在执行的解密操作，对应回调observer都会收到onError() /
     * onFinish()。 SD卡存在密码不符合的旧数据时，会自动弹出导入/清空提示框，取消操作无效。
     */
    public void cancelDecrypt() {
        // TODO implement this
        // showImportDialog(null, preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null));
    }

    /**
     * 临时解密，将队列中的待解密项，还原到临时文件中。SD卡存在密码不符合的旧数据时，会自动弹出导入/清空提示框，临时解密操作被取消。
     * observer连续触发onError()/onFinish()。
     *
     * @param items    待解密列表
     * @param observer 解密操作回调
     */
    public void decryptTemp(List<EncryptItem> items, OperateListener observer) {
        if (encryptionManagerMap.containsKey(DBAction.getSdcardRootPath())) {
            AsyncTempDecryptor dec = new AsyncTempDecryptor(items, observer, encryptionManagerMap.get(DBAction.getSdcardRootPath()));
            AsyncOperations.exeucteOperation(dec);
            return;
        }
        showImportDialog(observer, preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null));
    }

    public void decryptTemp(EncryptItem item, OperateListener observer) {
        if (encryptionManagerMap.containsKey(DBAction.getSdcardRootPath())) {
            AsyncTempDecryptor dec = new AsyncTempDecryptor(item, observer, encryptionManagerMap.get(DBAction.getSdcardRootPath()));
            AsyncOperations.exeucteOperation(dec);
            return;
        }
        showImportDialog(observer, preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null));
    }

    /**
     * 取消临时解密，所有临时解密队列全部取消。成功取消后，所有正在执行的临时解密操作，对应回调observer都会收到onError() /
     * onFinish()。 SD卡存在密码不符合的旧数据时，会自动弹出导入/清空提示框，取消操作无效。
     */
    public void cancelDecryptTmp() {
        // TODO implement this
        // showImportDialog(null, preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null));
    }

    /**
     * 取出item项中的缩略图.SD卡存在密码不符合的旧数据时，会自动弹出导入/清空提示框，返回null
     *
     * @param item
     * @return 缩略图Bitmap 或 null,
     */
    public Bitmap decryptThumb(EncryptItem item) {
        if (encryptionManagerMap.containsKey(DBAction.getSdcardRootPath())) {
            return FileLockerHelper.decryptThumb(encryptionManagerMap.get(DBAction.getSdcardRootPath()), item);
        }
        showImportDialog(null, preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null));
        return null;
    }

    /**
     * @param type =Constants.TYPE_PHOTO / Constants.TYPE_FILE /
     *             Constants.TYPE_VIDEO
     * @return long[], ret[0] = 总文件数， ret[1] = 总字节数
     */
    public long[] getSummary(int type) {
        if (importDialog != null) {
            return new long[2];
        }
        if (encryptionManagerMap.containsKey(DBAction.getSdcardRootPath())) {
            return DBAction.getTableInfo(type);
        }
        showImportDialog(null, preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null));
        return new long[2];
    }

    public long[] getAllSummary(int type) {
        if (importDialog != null) {
            return new long[2];
        }
        long[] summary = new long[2];
        if (encryptionManagerMap.containsKey(DBAction.getSdcardRootPath())) {
            long[] sum = DBAction.getTableInfo(type);
            summary[0] += sum[0];
            summary[1] += sum[1];
        }
        return summary;
    }

    /**
     * 删除指定item，同步操作，不建议在UI线程执行
     *
     * @param items
     */
    public void deleteEncryptItem(List<EncryptItem> items) {
        if (items != null) {
            for (EncryptItem i : items) {
                DBAction.delete(i);
                if (i.getThumbPath() != null && !i.getThumbPath().equals("")) {
                    new File(i.getThumbPath()).delete();
                }
                if (i.getFilePath() != null && !i.getFilePath().equals("")) {
                    new File(i.getFilePath()).delete();
                }
                if (i.getTempPath() != null && !i.getTempPath().equals("")) {
                    new File(i.getTempPath()).delete();
                }
            }
        }
    }

    /**
     * 还原临时解密的数据，UI退出时手动调用
     */
    public void gc() {
        EncryptionManager em = encryptionManagerMap.get(DBAction.getSdcardRootPath());
        if (em != null) {
            AsyncOperations.scheduleGC(em, DBAction.getTempList());
        }
    }

    ;

    private void initial() {
        if (!initialed) {
            //预备多卡支持
            DBAction.initDatabases();
            String password = preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null);
            if (password != null) {
                createEncryption(password);
            } else {
                // 防止首次使用时，preference中无password的KEY时，某些机型/ROM第一次onSharedPreferenceChanged无法监听到的bug
                preference.edit().putString(Constants.PRIVACY_PATTERN_PASSWORD, null).commit();
            }

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_REMOVED);
            filter.addAction(Intent.ACTION_MEDIA_SHARED);
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
            filter.addDataScheme("file");
            context.registerReceiver(receiver, filter);
            initialed = true;
//			Log.w("FileLocker", "initial  ok ... ");
            showImportDialog(null, password);
        }
    }

    private boolean showImportDialog(OperateListener observer, String password) {
        if (observer != null) {
            observer.onError(null, EncryptListener.RESULT_CANCEL);
            observer.onFinish();
        }

        if (password != null && checkExistPassword(password) != null) {
            if (importDialog != null) {
                SystemModalDialog.getInstance().hideDialog(importDialog);
                importDialog = null;
            }
            LockScreenService.getInstance().showFirstUse();
            return false;
        }

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            LockScreenService.getInstance().reset();
            LockScreenService.getInstance().showFirstUse();
            return false;
        }

        long[] size = DBAction.getAllTableInfo();
        if (size[0] + size[1] == 0) {
            DBAction.clearPassword();
            LockScreenService.getInstance().showFirstUse();
            return false;
        }


        new Handler(Looper.getMainLooper()).post(new ShowImportDialogRun());

        return true;
    }

    ;

    private boolean importPassword(String oldpassword) {
        byte[] seckey = null;
        EncryptionManager em = checkExistPassword(oldpassword);
        if (em != null) {
            try {
                String currentpassword = preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null);
                seckey = em.updatePassword(currentpassword);
            } catch (Exception e) {
            }
        }
        if (seckey != null) {
            int level = Integer.parseInt(preference.getString(Constants.PRIVACY_CURRENT_LEVEL, "" + Constants.DEFAULT_LEVEL));
            DBAction.updatePassword(new PasswordItem(seckey, level));
            encryptionManagerMap.put(DBAction.getSdcardRootPath(), em);
//			Log.w("importPassword", DBAction.getSdcardRootPath() + ":" + password + "_" + seckey);
            return true;
        }
        return false;
    }

    private void updatePassword() {
        String currentpassword = preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null);
        int level = Integer.parseInt(preference.getString(Constants.PRIVACY_CURRENT_LEVEL, "" + Constants.DEFAULT_LEVEL));
        String root = DBAction.getSdcardDBPath();
        byte[] seckey = null;
        EncryptionManager em = encryptionManagerMap.get(root);
        if (em != null) {
            try {
                seckey = em.updatePassword(currentpassword);// 更新密钥
            } catch (Exception e) {
            }
        }
        if (seckey != null) {
            DBAction.updatePassword(new PasswordItem(seckey, level));
        }
    }

    private boolean createEncryption(String password) {
        if (password == null)
            password = preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null);
        EncryptionManager em = null;
        String sdroot = DBAction.getSdcardRootPath();
        try {
            byte[] seckey = null;
            PasswordItem pi = DBAction.queryPassword();
            if (pi == null || pi.getSeckey() == null) {
                seckey = EncryptionManager.initEncryptionManager(password);// 生成密钥
                int level = Constants.DEFAULT_MAX_LEVEL;
                try {
                    level = Integer.parseInt(preference.getString(Constants.PRIVACY_CURRENT_LEVEL, "" + Constants.DEFAULT_MAX_LEVEL));
                } catch (Exception e) {
                }
                DBAction.updatePassword(new PasswordItem(seckey, level));
//				Log.w(TAG, "<createEncryption> Write new Encypt Keyfile OK...");
            } else {
                seckey = pi.getSeckey();
            }
            // password 与 seckey 不匹配的话，抛出异常
            Log.w("createEncryption", sdroot + ":" + password + "_" + seckey);
            em = new EncryptionManager(password, seckey);
        } catch (Exception e) {
            Log.e("createEncryption", sdroot + ":" + password + " fail");
//			e.printStackTrace();
        }
        if (em == null)
            encryptionManagerMap.remove(sdroot);
        else
            encryptionManagerMap.put(sdroot, em);
        return em != null;
    }


    private EncryptionManager checkExistPassword(String password) {
        PasswordItem pi = DBAction.queryPassword();
        EncryptionManager em = null;
        if (pi != null) {
            try {
                em = new EncryptionManager(password, pi.getSeckey());
            } catch (Exception e) {
            }
        }
        return em;
    }

    public interface OperateListener {
        public void onStart();

        public void onProgress(EncryptItem ei, int progress);

        public void onError(EncryptItem ei, int error);

        public void onSuccess(EncryptItem ei);

        public void onFinish();
    }

    class ShowImportDialogRun implements Runnable {
        @Override
        public void run() {
            if (importDialog != null) {
                return;
            }
            importDialog = SystemModalDialog.getInstance().createGenericDialog(null, context.getString(R.string.File_Lock_OldDb), context.getString(R.string.File_Lock_OldDb_Exist, DBAction.getSdcardRootPath()), context.getString(R.string.File_Lock_OldDb_Import),
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            importDialog = null;
                            if (!encryptionManagerMap.containsKey(DBAction.getSdcardRootPath())) {
                                int level = 0;
                                PasswordItem pi = DBAction.queryPassword();
                                if (pi != null) {
                                    level = pi.getLevel();
                                }
                                LockScreenService.getInstance().retryUnlock(new OnLockScreenFinish() {
                                    @Override
                                    public void onUnlockSuccess() {
                                    }

                                    @Override
                                    public void onUnlockFailed(int reason) {
                                        if (reason == KeyGuardInterface.Unlock_Fail_UserQuit)
                                            showImportDialog(null, null);
                                    }

                                    @Override
                                    public boolean onUnlockPassword(String password) {
                                        try {
                                            if (importPassword(password)) {    //更新当前SD卡数据库
                                                //createEncryption(null);	//基于初始SD卡数据库 + 已保存的当前设置密码创建加密管理
                                                Toast.makeText(context, R.string.File_Lock_Import_OK, Toast.LENGTH_LONG).show();
                                                LockScreenService.getInstance().hideLockScreen();
                                                LockScreenService.getInstance().showFirstUse();
                                                String current = DBAction.getSdcardRootPath();
//													Log.w("onUnlockPassword", current + " import OK");
                                                String currentpassword = preference.getString(Constants.PRIVACY_PATTERN_PASSWORD, null);
                                                if (!encryptionManagerMap.containsKey(DBAction.getSdcardDBPath())) {
                                                    if (!importPassword(password)) {
                                                        showImportDialog(null, currentpassword);
                                                        //new Handler(Looper.getMainLooper()).post(new ShowImportDialog());
                                                        //return true;
//																	Log.w("onUnlockPassword", path + " import error");
                                                    }
                                                }
//													Log.w("onUnlockPassword", "current = " + DBAction.getSdcardRootPath());
                                                //getAllSummary(0);
                                                return true;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }// 更新密钥

                                        return false;
                                    }
                                }, context.getString(R.string.File_Lock_Import_Password), level);
                            }
                        }
                    }, context.getString(R.string.File_Lock_OldDb_Clear), new View.OnClickListener() {
                        public void onClick(View v) {
                            importDialog = null;
                            final View progressDialog = SystemModalDialog.getInstance().showProgressDialog(context.getText(R.string.File_Lock_Cleaning));
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    SystemInfo.rmdir(DBAction.getSdcardRootPath() + Constants.FILE_THUMB_PATH);
                                    SystemInfo.rmdir(DBAction.getSdcardRootPath() + Constants.ENCRYPT_FILE_PATH);
                                    SystemInfo.rmdir(DBAction.getSdcardRootPath() + Constants.FILE_TEMPORARY_PATH);
                                    DBAction.clearAllData();
                                    createEncryption(null);
                                    getAllSummary(0);
                                    new Handler(context.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            SystemModalDialog.getInstance().hideDialog(progressDialog);
                                            LockScreenService.getInstance().reset();
                                            LockScreenService.getInstance().showFirstUse();
                                        }
                                    });
                                }
                            }).start();

                        }
                    }, null, null);
        }
    }

}
