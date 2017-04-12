package com.mobimvp.privacybox.ui.filelock;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.service.filelocker.FileLocker;
import com.mobimvp.privacybox.service.filelocker.FileLocker.OperateListener;
import com.mobimvp.privacybox.service.filelocker.internal.EncryptItem;
import com.mobimvp.privacybox.ui.widgets.AlertDialogLite;

import java.io.File;
import java.util.List;

public class FileOperationUI implements OperateListener {

    private Context mContext;
    private FileLocker mLocker;
    private FileLockerListener mListener;
    private AlertDialogLite mDialog;
    private TextView mTitle;
    private ProgressBar mProgress;
    private TextView mProgressText;
    private List<EncryptItem> mFileList;
    private Operation mOp;
    private int successCount;
    private int failCount;

    private FileOperationUI(Context context, List<EncryptItem> fileList,
                            Operation operation, FileLockerListener listener) {

        mContext = context;
        mFileList = fileList;
        mOp = operation;
        mListener = listener;

        mLocker = FileLocker.getInstance();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.progress_dialog, null);
        mTitle = (TextView) view.findViewById(R.id.text);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mProgressText = (TextView) view.findViewById(R.id.progress_text);

        mDialog = new AlertDialogLite.Builder(mContext).setView(view)
                .setCancelable(false).create();
    }

    public static FileOperationUI CreateEncryptUI(Context context,
                                                  List<EncryptItem> fileList, FileLockerListener listener) {
        return new FileOperationUI(context, fileList, Operation.Encrypt,
                listener);
    }

    public static FileOperationUI CreateDecryptUI(Context context,
                                                  List<EncryptItem> fileList, FileLockerListener listener) {
        return new FileOperationUI(context, fileList, Operation.Decrypt,
                listener);
    }

    public static FileOperationUI CreateTempDecryptUI(Context context,
                                                      List<EncryptItem> fileList, FileLockerListener listener) {
        return new FileOperationUI(context, fileList, Operation.TempDecryptFile,
                listener);
    }

    public static FileOperationUI CreatePreviewVideoUI(Context context,
                                                       List<EncryptItem> fileList, FileLockerListener listener) {
        return new FileOperationUI(context, fileList, Operation.PreviewVideo,
                listener);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            mDialog.dismiss();
        } catch (Exception e) {
        }
        super.finalize();
    }

    public void start() {
        try {
            mDialog.show();
            if (mOp == Operation.Encrypt) {
                mLocker.encryptFile(mFileList, this);
            } else if (mOp == Operation.Decrypt) {
                mLocker.decryptFile(mFileList, this);
            } else if (mOp == Operation.TempDecryptFile || mOp == Operation.PreviewVideo) {
                mLocker.decryptTemp(mFileList, this);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onStart() {
        if (mOp == Operation.Encrypt) {
            mTitle.setText(mContext.getString(R.string.encrypting));
        } else if (mOp == Operation.Decrypt) {
            mTitle.setText(mContext.getString(R.string.decrypting));
        } else if (mOp == Operation.TempDecryptFile) {
            mTitle.setText(mContext.getString(R.string.decrypting_temp));
        } else if (mOp == Operation.PreviewVideo) {
            mTitle.setText(mContext.getString(R.string.preview_video));
        }
        mProgress.setProgress(0);
        mProgressText.setText(mProgress.getProgress() + "%");
    }

    @Override
    public void onProgress(EncryptItem ei, int progress) {
        if (mOp == Operation.Encrypt) {
            mTitle.setText(mContext.getString(R.string.encrypting) + ei.getName());
        } else if (mOp == Operation.Decrypt) {
            mTitle.setText(mContext.getString(R.string.decrypting) + ei.getName());
        } else if (mOp == Operation.TempDecryptFile) {
            mTitle.setText(mContext.getString(R.string.decrypting_temp) + ei.getName());
        } else if (mOp == Operation.PreviewVideo) {
            mTitle.setText(mContext.getString(R.string.preview_video));
        }
        mProgress.setProgress(progress);
        mProgressText.setText(mProgress.getProgress() + "%");
    }

    @Override
    public void onError(final EncryptItem ei, final int error) {
        failCount++;
    }

    @Override
    public void onSuccess(EncryptItem ei) {
        successCount++;
        if (mOp == Operation.TempDecryptFile || mOp == Operation.PreviewVideo) {
            openFile(new File(ei.getTempPath()));
        }
    }

    @Override
    public void onFinish() {
        mDialog.dismiss();
        if (mOp == Operation.Encrypt) {
            if (failCount > 0) {
                Toast.makeText(mContext,
                        String.format(mContext.getString(R.string.encryption_success_fail_toast),
                                successCount, failCount),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext, String.format(mContext.getString(R.string.encryption_success_toast), successCount),
                        Toast.LENGTH_LONG).show();
            }
            if (mListener != null) {
                mListener.OnRefreshUI();
            }
        } else if (mOp == Operation.Decrypt) {
            if (failCount > 0) {
                Toast.makeText(mContext, String.format(mContext.getString(R.string.decryption_success_fail_toast),
                        successCount, failCount),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext,
                        String.format(mContext.getString(R.string.decryption_success_toast), successCount),
                        Toast.LENGTH_LONG).show();
            }
            if (mListener != null) {
                mListener.OnRefreshUI();
            }
        }
    }

    private void openFile(File f) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        /* 调用getMIMEType()来取得MimeType */
        String type = getMIMEType(f);
		/* 设置intent的file与MimeType */
        intent.setDataAndType(Uri.fromFile(f), type);
        try {
            mContext.startActivity(intent);
        } catch (Exception e) {
        }
    }

    /* 判断文件MimeType的method */
    private String getMIMEType(File f) {
        String fileName = f.getName();
        String fileExt = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt);
        if (type == null) {
            if (mOp == Operation.PreviewVideo) {
                type = "video/*";
            } else {
                type = "*/*";
            }
        }

        return type;
    }

    private static enum Operation {
        Encrypt, Decrypt, TempDecryptFile, PreviewVideo;
    }
}
