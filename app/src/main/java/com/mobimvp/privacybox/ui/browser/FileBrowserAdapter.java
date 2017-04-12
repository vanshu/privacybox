package com.mobimvp.privacybox.ui.browser;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.utility.SystemInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class FileBrowserAdapter extends BaseAdapter {

    private static final int MSG_BROWSE_FILE = 0;
    private File mPath;
    private ArrayList<FileInfo> mFileList;
    private View.OnClickListener selectListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag();
            FileInfo fi = mFileList.get(position);
            fi.selected = !fi.selected;
            notifyDataSetChanged();
        }
    };
    private int reentranceCount;
    private View.OnClickListener rowClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            BrowserItemHolder holder = (BrowserItemHolder) v.getTag();
            select(holder.position);
        }
    };
    private Context mContext;
    private LayoutInflater mInflater;
    private FileBrowserListener mListener;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BROWSE_FILE: {
                    Pair<File, ArrayList<FileInfo>> result = (Pair<File, ArrayList<FileInfo>>) msg.obj;
                    mFileList.clear();
                    mFileList.addAll(result.second);
                    mPath = result.first;
                    reentranceCount--;
                    notifyDataSetChanged();
                    if (mListener != null) {
                        mListener.OnFinishLoadFolder(mPath);
                    }
                    break;
                }
            }
        }
    };

    public FileBrowserAdapter(Context context, File path) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mPath = path;
        mFileList = new ArrayList<FileInfo>();
        mListener = null;
    }

    public void setBrowserListener(FileBrowserListener listener) {
        mListener = listener;
    }

    public void browseFolder(File path) {
        reentranceCount++;
        new Thread(new BrowseAction(path)).start();
    }

    public void browseParent() {
        browseFolder(mPath.getParentFile());
    }

    public File getCurrentFolder() {
        return mPath;
    }

    public boolean isBusy() {
        return reentranceCount != 0;
    }

    public void select(int position) {
        FileInfo info = mFileList.get(position);
        if (info.type == FileInfo.TYPE_DIR) {
            browseFolder(info.file);
        } else {
            info.selected = !info.selected;
            notifyDataSetChanged();
        }
    }

    public void selectAll() {
        int selectCount = 0;
        for (FileInfo info : mFileList) {
            if (info.selected) {
                selectCount++;
            }
        }

        if (selectCount < mFileList.size()) {
            for (FileInfo info : mFileList) {
                info.selected = true;
            }
        } else {
            for (FileInfo info : mFileList) {
                info.selected = false;
            }
        }

        notifyDataSetChanged();
    }

    public ArrayList<String> getSelectedFiles() {
        ArrayList<String> result = new ArrayList<String>();
        for (FileInfo info : mFileList) {
            if (info.selected && info.file.exists()) {
                if (info.file.isDirectory()) {
                    String[][] list = SystemInfo.listFiles(info.file
                            .getAbsolutePath());
                    if (list != null && list.length == 2 && list[1] != null) {
                        for (int i = 0; i < list[1].length; i++) {
                            result.add(list[1][i]);
                        }
                    }
                } else {
                    result.add(info.file.getAbsolutePath());
                }
            }
        }

        return result;
    }

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BrowserItemHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.filebrowser_item, null);
            holder = new BrowserItemHolder();
            holder.type = (ImageView) convertView.findViewById(R.id.type);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.size = (TextView) convertView.findViewById(R.id.size);
            holder.selected = (CheckBox) convertView
                    .findViewById(R.id.selected);
            holder.selected.setOnClickListener(selectListener);
            convertView.setTag(holder);
            convertView.setOnClickListener(rowClickListener);
        } else {
            holder = (BrowserItemHolder) convertView.getTag();
        }

        FileInfo fi = mFileList.get(position);
        if (fi.type == FileInfo.TYPE_DIR) {
            holder.type.setImageResource(R.drawable.ic_folder);
            holder.size.setVisibility(View.GONE);
        } else {
            holder.type.setImageResource(R.drawable.ic_file);
            holder.size.setVisibility(View.VISIBLE);
            holder.size.setText(Formatter.formatFileSize(mContext, fi.size));
        }
        holder.name.setText(fi.file.getName());
        holder.selected.setChecked(fi.selected);
        holder.position = position;
        holder.selected.setTag(position);

        return convertView;
    }

    private static class BrowserItemHolder {
        public ImageView type;
        public TextView name;
        public TextView size;
        public CheckBox selected;
        public int position;
    }

    private static class FileInfo implements Comparable<FileInfo> {

        public static final int TYPE_DIR = 0;
        public static final int TYPE_FILE = 1;

        public File file;
        public int type;
        public long size;
        public boolean selected;

        public FileInfo(File file) {
            this.file = file;
            this.type = file.isDirectory() ? TYPE_DIR : TYPE_FILE;
            this.size = file.length();
            this.selected = false;
        }

        public FileInfo(String fileName) {
            this(new File(fileName));
        }

        @Override
        public int compareTo(FileInfo another) {
            return file.getName().compareTo(another.file.getName());
        }

    }

    private class BrowseAction implements Runnable {
        private File folder;

        public BrowseAction(File folder) {
            this.folder = folder;
        }

        @Override
        public void run() {
            if (mListener != null) {
                mListener.OnStartLoadFolder(folder);
            }
            ArrayList<FileInfo> folderList = new ArrayList<FileInfo>();
            ArrayList<FileInfo> fileList = new ArrayList<FileInfo>();

            String[][] list = SystemInfo.listFiles(folder
                    .getAbsolutePath());
            if (list != null && list.length == 2) {
                if (list[0] != null) {
                    for (int i = 0; i < list[0].length; i++) {
                        if (list[0][i] == null) {
                            continue;
                        }
                        File f = new File(list[0][i]);
                        if (f.getName().startsWith(".")) {
                            continue;
                        }
                        folderList.add(new FileInfo(f));
                    }
                }
                if (list[1] != null) {
                    for (int i = 0; i < list[1].length; i++) {
                        if (list[1][i] == null) {
                            continue;
                        }
                        File f = new File(list[1][i]);
                        if (f.getName().startsWith(".")) {
                            continue;
                        }
                        fileList.add(new FileInfo(f));
                    }
                }
            }

            Collections.sort(folderList);
            Collections.sort(fileList);

            ArrayList<FileInfo> result = new ArrayList<FileInfo>();
            result.addAll(folderList);
            result.addAll(fileList);

            mHandler.obtainMessage(MSG_BROWSE_FILE,
                    new Pair<File, ArrayList<FileInfo>>(folder, result))
                    .sendToTarget();
        }
    }
}
