package com.mobimvp.privacybox.service.filelocker;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;

import com.mobimvp.privacybox.Constants;
import com.mobimvp.privacybox.PBApplication;
import com.mobimvp.privacybox.service.filelocker.FileLocker.OperateListener;
import com.mobimvp.privacybox.service.filelocker.internal.DBAction;
import com.mobimvp.privacybox.service.filelocker.internal.EncryptItem;
import com.mobimvp.privacybox.utility.AsyncTask;
import com.mobimvp.privacybox.utility.BitmapUtility;
import com.mobimvp.privacybox.utility.crypto.EncryptListener;
import com.mobimvp.privacybox.utility.crypto.EncryptionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class AsyncOperations {

    public static final int ENCRYPT = 0;
    public static final int DECRYPT = 1;
    public static final int TEMPDECRYPT = 2;
    private static final int GC = 3;

    private static final int THUMBNAIL_SIZE = 128;
    private static final int MSG_NEW_OPERATION = 0;
    private static AsyncOperations _instance;
    private Handler mOperationHandler;

    private AsyncOperations() {
        mOperationHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_NEW_OPERATION: {
                        if (msg.obj != null) {
                            ((BaseAsyncOperation) msg.obj).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                        }
                        break;
                    }
                }
            }
        };
    }

    private static synchronized AsyncOperations getInstance() {
        if (_instance == null) {
            _instance = new AsyncOperations();
        }
        return _instance;
    }

    public static void exeucteOperation(BaseAsyncOperation operation) {
        getInstance().mOperationHandler.obtainMessage(MSG_NEW_OPERATION, operation).sendToTarget();
    }

    public static void scheduleGC(EncryptionManager em, List<EncryptItem> list) {
        AsyncGC operation = new AsyncGC(list, new FileLocker.OperateListener() {

            @Override
            public void onSuccess(EncryptItem ei) {
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onProgress(EncryptItem ei, int progress) {
            }

            @Override
            public void onFinish() {
            }

            @Override
            public void onError(EncryptItem ei, int error) {
            }
        }, em);
        getInstance().mOperationHandler.obtainMessage(MSG_NEW_OPERATION, operation).sendToTarget();
    }

    private static class Progress {
        public ProgressType type;
        public EncryptItem ei;
        public int progress;
        public int result;

        public Progress(ProgressType type, EncryptItem ei, int progress, int result) {
            this.type = type;
            this.ei = ei;
            this.progress = progress;
            this.result = result;
        }

        private static enum ProgressType {
            Start, Progress, Success, Error, Finish
        }
    }

    public static abstract class BaseAsyncOperation extends AsyncTask<Void, Progress, Void> implements EncryptListener {
        protected EncryptionManager mEncryptionManager;
        protected EncryptItem currentItem;
        int mType;
        private List<EncryptItem> mList;
        private FileLocker.OperateListener mListener;

        private BaseAsyncOperation(FileLocker.OperateListener listener, EncryptionManager em, int type) {
            mType = type;
            FileLocker.getInstance();
            mList = new ArrayList<EncryptItem>();
            mListener = listener;
            mEncryptionManager = em;
        }

        protected BaseAsyncOperation(Collection<EncryptItem> itemList, FileLocker.OperateListener listener, EncryptionManager em, int type) {
            this(listener, em, type);
            mList.addAll(itemList);
        }

        protected BaseAsyncOperation(EncryptItem item, FileLocker.OperateListener listener, EncryptionManager em, int type) {
            this(listener, em, type);
            mList.add(item);
        }

        protected abstract void processEncryptItem(EncryptItem ei);

        private void broadcastStatus(Progress progress) {
            switch (progress.type) {
                case Start:
                    mListener.onStart();
                    break;

                case Progress:
                    mListener.onProgress(progress.ei, progress.progress);
                    break;

                case Success:
                    mListener.onSuccess(progress.ei);
                    break;

                case Error:
                    mListener.onError(progress.ei, progress.result);
                    break;

                case Finish:
                    mListener.onFinish();
                    break;
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            publishProgress(new Progress(Progress.ProgressType.Start, null, 0, EncryptListener.RESULT_ENCRYPT_OK));
            for (int i = 0; i < mList.size(); i++) {
                currentItem = mList.get(i);
                if (isCancelled()) {
                    publishProgress(new Progress(Progress.ProgressType.Error, currentItem, 100, EncryptListener.RESULT_CANCEL));
                } else {
                    publishProgress(new Progress(Progress.ProgressType.Progress, currentItem, 0, EncryptListener.RESULT_ENCRYPT_OK));
                    processEncryptItem(currentItem);
                }
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            broadcastStatus(new Progress(Progress.ProgressType.Finish, null, 100, EncryptListener.RESULT_CANCEL));
        }

        @Override
        protected void onPostExecute(Void result) {
            broadcastStatus(new Progress(Progress.ProgressType.Finish, null, 100, EncryptListener.RESULT_ENCRYPT_OK));
        }

        @Override
        protected void onProgressUpdate(Progress... values) {
            if (values.length > 0) {
                broadcastStatus(values[0]);
            }
        }

        @Override
        public boolean OnOperationProgress(int progress) {
            publishProgress(new Progress(Progress.ProgressType.Progress, currentItem, progress, EncryptListener.RESULT_ENCRYPT_OK));
            return isCancelled();
        }

        protected File getEncryptedFileName(String path) throws IOException {
            File result = new File(path, UUID.randomUUID().toString());
            result.getParentFile().mkdirs();
            result.createNewFile();
            return result;
        }

        protected File getLockFileName(File fileName) throws IOException {
            File result = new File(fileName.getAbsolutePath() + ".lck");
            result.getParentFile().mkdirs();
            result.createNewFile();
            return result;
        }
    }

    public static class AsyncEncryptor extends BaseAsyncOperation {

        boolean mFullEncrypt = false;

        public AsyncEncryptor(Collection<EncryptItem> itemList, OperateListener listener, EncryptionManager em, boolean fullEncrypt) {
            super(itemList, listener, em, ENCRYPT);
            mFullEncrypt = fullEncrypt;
        }

        public AsyncEncryptor(EncryptItem item, OperateListener listener, EncryptionManager em, boolean fullEncrypt) {
            super(item, listener, em, ENCRYPT);
            mFullEncrypt = fullEncrypt;
        }

        protected void deleteSystemThumb(String file) {
            Cursor cursor = null;
            ContentResolver cr = PBApplication.getApplication().getContentResolver();
            try {
                String[] projection = {Images.Media._ID, Images.Media.DATA};
                cursor = cr.query(Images.Media.EXTERNAL_CONTENT_URI, projection, String.format("LOWER(%s) = LOWER(\'%s\')", Images.Media.DATA, new File(file).toString()), null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int imgid = cursor.getInt(cursor.getColumnIndex(Images.Media._ID));
                    cr.delete(Images.Media.EXTERNAL_CONTENT_URI, String.format("%s=%d", Images.Media._ID, imgid), null);
                    cursor.close();
                    cursor = null;
                    String[] projection2 = {Images.Thumbnails.IMAGE_ID, Images.Thumbnails.DATA};
                    cursor = cr.query(Images.Thumbnails.EXTERNAL_CONTENT_URI, projection2, Images.Thumbnails.IMAGE_ID + "=" + imgid, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        String thumbpath = cursor.getString(cursor.getColumnIndex(Images.Thumbnails.DATA));
                        new File(thumbpath).delete();
                        cr.delete(Images.Thumbnails.EXTERNAL_CONTENT_URI, String.format("%s=%d", Images.Thumbnails.IMAGE_ID, imgid), null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            try {
                String[] projection = {Video.Media._ID, Video.Media.DATA};
                cursor = cr.query(Video.Media.EXTERNAL_CONTENT_URI, projection, String.format("LOWER(%s) = LOWER(\'%s\')", Video.Media.DATA, new File(file).toString()), null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int imgid = cursor.getInt(cursor.getColumnIndex(Video.Media._ID));
                    cr.delete(Images.Media.EXTERNAL_CONTENT_URI, String.format("%s=%d", Images.Media._ID, imgid), null);
                    cursor.close();
                    cursor = null;
                    String[] projection2 = {Images.Thumbnails.IMAGE_ID, Images.Thumbnails.DATA};
                    cursor = cr.query(Images.Thumbnails.EXTERNAL_CONTENT_URI, projection2, Images.Thumbnails.IMAGE_ID + "=" + imgid, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        String thumbpath = cursor.getString(cursor.getColumnIndex(Images.Thumbnails.DATA));
                        new File(thumbpath).delete();
                        cr.delete(Images.Thumbnails.EXTERNAL_CONTENT_URI, String.format("%s=%d", Images.Thumbnails.IMAGE_ID, imgid), null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }
        }

        protected int encryptThumb(EncryptItem ei) throws Exception {
            Bitmap thumb = null;
            try {
                if (ei.getFileType() == Constants.TYPE_PHOTO) {
                    thumb = BitmapUtility.createThumbnail(new File(ei.getOriginPath()), THUMBNAIL_SIZE);
                } else if (ei.getFileType() == Constants.TYPE_VIDEO) {
                    thumb = BitmapUtility.createVideoThumb(new File(ei.getOriginPath()));
                }
                if (thumb != null) {
                    File thumbFile = getEncryptedFileName(DBAction.getSdcardRootPath() + Constants.FILE_THUMB_PATH);
                    ei.setThumbPath(thumbFile.getAbsolutePath());
                    FileOutputStream fOut = new FileOutputStream(thumbFile);
                    thumb.compress(Bitmap.CompressFormat.JPEG, 60, fOut);
                    fOut.close();
                    return mEncryptionManager.encryptFileFast(thumbFile, null);
                } else {
                    return EncryptListener.RESULT_ENCRYPT_ERROR;
                }
            } finally {
                if (thumb != null) {
                    thumb.recycle();
                }
            }
        }


        @Override
        protected void processEncryptItem(EncryptItem ei) {
            try {
                File outFile = getEncryptedFileName(DBAction.getSdcardRootPath() + Constants.ENCRYPT_FILE_PATH);
                File inFile = new File(ei.getOriginPath());
                int result = EncryptListener.RESULT_ENCRYPT_OK;
//				Log.w("AsyncEncryptor", inFile.toString() + " "+ inFile.length() + " --> " + outFile.toString());
                if (inFile.exists()) {
                    ei.setDate(System.currentTimeMillis());
                    ei.setSize(inFile.length());
                    if (ei.getFileType() == Constants.TYPE_PHOTO || ei.getFileType() == Constants.TYPE_VIDEO) {
                        result = encryptThumb(ei);
                    }

                    //if (result == EncryptListener.RESULT_ENCRYPT_OK) {
                    try {
                        if (mFullEncrypt) {
                            ei.setFullEncrypt(true);
                            result = mEncryptionManager.encryptFileFull(inFile, outFile, this);
                        } else {
                            inFile.renameTo(outFile);
                            result = mEncryptionManager.encryptFileFast(outFile, this);
                        }
                        ei.setFilePath(outFile.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        result = EncryptListener.RESULT_ENCRYPT_ERROR;
                    }
                    //}
                } else {
                    result = EncryptListener.RESULT_FILE_ERROR;
                    return;
                }

                if (result != EncryptListener.RESULT_ENCRYPT_OK) {
                    if (mFullEncrypt) {
                        outFile.delete();
                    } else if (outFile.exists() && !inFile.exists()) {
                        outFile.renameTo(inFile);
                    }
                    publishProgress(new Progress(Progress.ProgressType.Error, ei, 100, result));
                    if (ei.getThumbPath() != null) {
                        new File(ei.getThumbPath()).delete();
                    }
                } else {
                    deleteSystemThumb(ei.getOriginPath());//删除系统缩略图
                    ei.setSize(outFile.length());
                    if (DBAction.insert(ei)) {
                        inFile.delete();
                        publishProgress(new Progress(Progress.ProgressType.Success, ei, 100, result));
                    } else {
                        if (mFullEncrypt) {
                            outFile.delete();
                        } else {
                            outFile.renameTo(inFile);
                            mEncryptionManager.decryptFile(inFile, inFile, this);
                        }
                        publishProgress(new Progress(Progress.ProgressType.Error, ei, 100, result));
                    }
                }
            } catch (Exception e) {
                publishProgress(new Progress(Progress.ProgressType.Error, ei, 100, EncryptListener.RESULT_ENCRYPT_ERROR));
            }
        }
    }

    private static class AsyncGC extends BaseAsyncOperation {

        public AsyncGC(Collection<EncryptItem> itemList, OperateListener listener, EncryptionManager em) {
            super(itemList, listener, em, GC);
        }

        public AsyncGC(EncryptItem item, OperateListener listener, EncryptionManager em) {
            super(item, listener, em, GC);
        }

        @Override
        protected void processEncryptItem(EncryptItem ei) {
            File tmpFile = new File(ei.getTempPath());
            File originalFile = new File(ei.getFilePath());

            int result = EncryptListener.RESULT_ENCRYPT_OK;

            if (ei.getTemp() && tmpFile.exists()) {
                if (!originalFile.exists()) {
                    try {
                        if (ei.isFullEncrypt()) {
                            result = mEncryptionManager.encryptFileFull(tmpFile, originalFile, this);
                        } else {
                            tmpFile.renameTo(originalFile);
                            result = mEncryptionManager.encryptFileFast(originalFile, this);
                        }
                    } catch (Exception e) {
                        result = EncryptListener.RESULT_ENCRYPT_ERROR;
                    }
                }
            } else {
                result = EncryptListener.RESULT_FILE_ERROR;
            }

            if (result == EncryptListener.RESULT_ENCRYPT_OK) {
                DBAction.updateTempMode(ei, false);
                tmpFile.delete();
                publishProgress(new Progress(Progress.ProgressType.Success, ei, 100, result));
            } else {
                publishProgress(new Progress(Progress.ProgressType.Error, ei, 100, result));
            }
        }
    }

    public static class AsyncDecryptor extends BaseAsyncOperation {

        public AsyncDecryptor(Collection<EncryptItem> itemList, OperateListener listener, EncryptionManager em) {
            super(itemList, listener, em, DECRYPT);
        }

        public AsyncDecryptor(EncryptItem item, OperateListener listener, EncryptionManager em) {
            super(item, listener, em, DECRYPT);
        }

        @Override
        protected void processEncryptItem(EncryptItem ei) {
            File inFile = new File(ei.getFilePath());
            File outfile = new File(ei.getOriginPath());
            File tmpFile = new File(ei.getTempPath());
//			Log.w("AsyncDecryptor", inFile.toString() + " --> " + outfile.toString() + " >>" + tmpFile.toString());
            try {
                int result = EncryptListener.RESULT_DECRYPT_OK;

                if (inFile.exists()) {
                    try {
                        outfile.delete();
                        outfile.getParentFile().mkdirs();
                        mEncryptionManager.decryptFile(inFile, outfile, this);
                    } catch (Exception e) {
                        result = EncryptListener.RESULT_DECRYPT_ERROR;
                        e.printStackTrace();
                    }
                } else if (tmpFile.exists()) {
                    outfile.delete();
                    outfile.getParentFile().mkdirs();
                    tmpFile.renameTo(outfile);
                } else {
                    result = EncryptListener.RESULT_FILE_ERROR;
                }

                if (result == EncryptListener.RESULT_DECRYPT_OK) {
                    if (!outfile.exists()) {
                        inFile.renameTo(outfile);
                    } else {
                        inFile.delete();
                    }

                    if (ei.getThumbPath() != null) {
                        new File(ei.getThumbPath()).delete();
                    }
                    DBAction.delete(ei);
                    publishProgress(new Progress(Progress.ProgressType.Success, ei, 100, result));
                } else {
                    publishProgress(new Progress(Progress.ProgressType.Error, ei, 100, result));
                }
            } catch (Exception e) {
                publishProgress(new Progress(Progress.ProgressType.Success, ei, 100, EncryptListener.RESULT_DECRYPT_ERROR));
            }
        }
    }

    public static class AsyncTempDecryptor extends BaseAsyncOperation {

        public AsyncTempDecryptor(Collection<EncryptItem> itemList, OperateListener listener, EncryptionManager em) {
            super(itemList, listener, em, TEMPDECRYPT);
        }

        public AsyncTempDecryptor(EncryptItem item, OperateListener listener, EncryptionManager em) {
            super(item, listener, em, TEMPDECRYPT);
        }

        @Override
        protected void processEncryptItem(EncryptItem ei) {
            File inFile = new File(ei.getFilePath());
            File outfile = new File(ei.getTempPath());

            try {
                int result = EncryptListener.RESULT_DECRYPT_OK;

                if (inFile.exists()) {
                    try {
                        outfile.delete();
                        outfile.getParentFile().mkdirs();
                        mEncryptionManager.decryptFile(inFile, outfile, this);
                    } catch (Exception e) {
                        result = EncryptListener.RESULT_DECRYPT_ERROR;
                        e.printStackTrace();
                    }
                } else {
                    result = EncryptListener.RESULT_FILE_ERROR;
                }

                if (result == EncryptListener.RESULT_DECRYPT_OK) {
                    if (!outfile.exists()) {
                        inFile.renameTo(outfile);
                    }
                    DBAction.updateTempMode(ei, true);
                    publishProgress(new Progress(Progress.ProgressType.Success, ei, 100, result));
                } else {
                    publishProgress(new Progress(Progress.ProgressType.Error, ei, 100, result));
                }
            } catch (Exception e) {
                publishProgress(new Progress(Progress.ProgressType.Error, ei, 100, EncryptListener.RESULT_DECRYPT_ERROR));
            }
        }
    }
}
