package com.mobimvp.privacybox.ui.browser;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.utility.BitmapUtility;
import com.mobimvp.privacybox.utility.ImageWorker;
import com.mobimvp.privacybox.utility.SystemInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class MediaBrowserAdapter extends BaseAdapter {
    private static final int MSG_NEW_FOLDER = 0;

    private static final int THUMBNAIL_SIZE = 128;
    private HashSet<String> extSet;
    private Context mContext;
    private LayoutInflater mInflater;
    private ThumbLoader mThumbLoader;
    private MediaScannerThread mScanThread;
    ;
    private MediaType mType;
    private BrowseMode mMode;
    private MediaBrowserListener mListener;
    private ArrayList<MediaFolder> folderList;
    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NEW_FOLDER:
                    folderList.add((MediaFolder) msg.obj);
                    notifyDataSetChanged();
                    break;
            }
        }
    };
    private MediaFolder currentFolder;
    private HashSet<File> selectedFileList;
    private File root;

    public MediaBrowserAdapter(Context context, MediaType type, File sdRoot) {
        root = sdRoot;
        mContext = context;
        extSet = new HashSet<String>();
        mInflater = LayoutInflater.from(mContext);

        mType = type;

        String[] extList;
        if (type == MediaType.Image) {
            extList = mContext.getResources().getStringArray(R.array.imgsuffix);
        } else if (type == MediaType.Video) {
            extList = mContext.getResources().getStringArray(R.array.videosuffix);
        } else {
            extList = new String[0];
        }

        for (int i = 0; i < extList.length; i++) {
            extSet.add(extList[i]);
        }

        mMode = BrowseMode.FolderList;
        folderList = new ArrayList<MediaFolder>();
        currentFolder = null;
        selectedFileList = new HashSet<File>();
        mThumbLoader = new ThumbLoader();
        mScanThread = new MediaScannerThread(sdRoot);
    }

    public void recycle() {
        mScanThread.exit();
        mThumbLoader.recycle();
    }

    public void startScan() {
        mScanThread.start();
        if (mListener != null) {
            mListener.OnBrowse(mMode, null);
        }
    }

    public void stopScan() {
        System.out.println("mScanThread:stop");
        mScanThread.exit();
    }

    public void setBrowserListener(MediaBrowserListener listener) {
        mListener = listener;
    }

    public void select(int position) {
        if (mMode == BrowseMode.FolderList) {
            mMode = BrowseMode.FileList;
            currentFolder = folderList.get(position);
            selectedFileList.clear();

            mScanThread.pauseScan();

            notifyDataSetChanged();

            if (mListener != null) {
                mListener.OnBrowse(mMode, currentFolder.folder);
            }
        } else {
            File file = currentFolder.mediaFileList.get(position);
            if (selectedFileList.contains(file)) {
                selectedFileList.remove(file);
            } else {
                selectedFileList.add(file);
            }
        }
        notifyDataSetChanged();
    }

    public BrowseMode getMode() {
        return mMode;
    }

    public void exitFileListMode() {
        currentFolder = null;
        mMode = BrowseMode.FolderList;

        mScanThread.resumeScan();
        notifyDataSetChanged();

        if (mListener != null) {
            mListener.OnBrowse(mMode, null);
        }
    }

    public File getCurrentFolder() {
        if (mMode == BrowseMode.FolderList) {
            return root;
        } else {
            return currentFolder.folder;
        }
    }

    public ArrayList<String> getSelectedFiles() {
        ArrayList<String> result = new ArrayList<String>();
        if (mMode == BrowseMode.FileList) {
            for (File file : selectedFileList) {
                result.add(file.getAbsolutePath());
            }
        }

        return result;
    }

    public void selectAll() {
        if (mMode == BrowseMode.FileList) {
            if (selectedFileList.size() < currentFolder.mediaFileList.size()) {
                selectedFileList.addAll(currentFolder.mediaFileList);
            } else {
                selectedFileList.clear();
            }

            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        if (mMode == BrowseMode.FolderList) {
            return folderList.size();
        } else {
            return currentFolder.mediaFileList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (mMode == BrowseMode.FolderList) {
            return folderList.get(position);
        } else {
            return currentFolder.mediaFileList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BrowserItemHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.mediabrowser_item, null);
            holder = new BrowserItemHolder();
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            holder.container = (RelativeLayout) convertView.findViewById(R.id.thumbnail_container);
            holder.folderInfo = (LinearLayout) convertView.findViewById(R.id.folderinfo);
            holder.folderName = (TextView) convertView.findViewById(R.id.name);
            holder.fileCount = (TextView) convertView.findViewById(R.id.filecount);
            holder.videoIndicator = (ImageView) convertView.findViewById(R.id.video);
            holder.selected = (CheckBox) convertView.findViewById(R.id.selected);
            holder.fileName = (TextView) convertView.findViewById(R.id.filename);
            convertView.setTag(holder);
        } else {
            holder = (BrowserItemHolder) convertView.getTag();
        }
        if (mMode == BrowseMode.FolderList) {
            MediaFolder folder = folderList.get(position);
            holder.fileName.setVisibility(View.GONE);
            holder.selected.setVisibility(View.GONE);
            holder.folderInfo.setVisibility(View.VISIBLE);
            holder.container.setBackgroundResource(R.drawable.ic_folder_background);
            holder.folderName.setText(folder.folder.getName());
            holder.fileCount.setText(Integer.toString(folder.mediaFileList.size()));
            mThumbLoader.loadImage(folder.mediaFileList.get(0), holder.thumbnail);
        } else {
            File file = currentFolder.mediaFileList.get(position);
            holder.fileName.setVisibility(View.VISIBLE);
            holder.selected.setVisibility(View.VISIBLE);
            holder.folderInfo.setVisibility(View.GONE);
            holder.container.setBackgroundResource(R.drawable.ic_media_background);

            holder.fileName.setText(file.getName());
            holder.selected.setChecked(selectedFileList.contains(file));
            mThumbLoader.loadImage(file, holder.thumbnail);
        }

        if (mType == MediaType.Video) {
            holder.videoIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.videoIndicator.setVisibility(View.GONE);
        }

        return convertView;
    }

    public static enum MediaType {
        Video, Image
    }

    public static enum BrowseMode {
        FolderList, FileList
    }

    private static class BrowserItemHolder {
        public ImageView thumbnail;
        public RelativeLayout container;
        public LinearLayout folderInfo;
        public TextView folderName;
        public TextView fileCount;
        public ImageView videoIndicator;
        public CheckBox selected;
        public TextView fileName;
    }

    private static class MediaFolder {
        public File folder;
        public ArrayList<File> mediaFileList;
    }

    private class MediaScannerThread extends Thread {

        private boolean pendingExit;
        private ConditionVariable cv;
        private File root;

        public MediaScannerThread(File root) {
            cv = new ConditionVariable();
            cv.open();
            this.root = root;
        }

        public void exit() {
            pendingExit = true;
        }

        public void pauseScan() {
            cv.close();
        }

        public void resumeScan() {
            cv.open();
        }

        @Override
        public void run() {
            LinkedList<String> pendingList = new LinkedList<String>();
            pendingList.add(root.getAbsolutePath());

            while (pendingList.size() > 0 && !pendingExit) {
                try {
                    cv.block();
                } catch (Exception e) {
                }

                if (pendingExit) {
                    break;
                }

                File folder = new File(pendingList.removeFirst());
                if (!folder.exists() || !folder.isDirectory()) {
                    continue;
                }
                if (folder.getName().startsWith(".")) {
                    continue;
                }
                if (new File(folder, ".nomedia").exists()) {
                    continue;
                }

                String[][] list = SystemInfo.listFiles(folder.getAbsolutePath());
                if (list == null || list.length != 2) {
                    continue;
                }

                if (list[0] != null) {
                    for (int i = 0; i < list[0].length; i++) {
                        pendingList.add(list[0][i]);
                    }
                }

                ArrayList<File> fileList = new ArrayList<File>();

                if (list[1] != null) {
                    for (int i = 0; i < list[1].length; i++) {
                        String fileName = list[1][i];
                        String fileExt = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
                        if (extSet.contains(fileExt)) {
                            fileList.add(new File(fileName));
                        }
                    }
                }

                if (fileList.size() > 0) {
                    MediaFolder mf = new MediaFolder();
                    mf.folder = folder;
                    mf.mediaFileList = fileList;
                    mUIHandler.obtainMessage(MSG_NEW_FOLDER, mf).sendToTarget();
                }
            }
            if (mListener != null) {
                mListener.OnScanFinished(pendingExit);
            }
        }
    }

    private class ThumbLoader extends ImageWorker {

        public ThumbLoader() {
            super(mContext);
        }

        @Override
        protected Bitmap processBitmap(Object data) {
            try {
                File file = (File) data;
                if (mType == MediaType.Image) {
                    return BitmapUtility.createThumbnail(file, THUMBNAIL_SIZE);
                } else if (mType == MediaType.Video) {
                    return BitmapUtility.createVideoThumb(file);
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }
    }
}
