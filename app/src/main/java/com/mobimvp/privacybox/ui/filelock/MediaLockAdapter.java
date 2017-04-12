package com.mobimvp.privacybox.ui.filelock;

import android.content.Context;
import android.graphics.Bitmap;
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
import com.mobimvp.privacybox.service.filelocker.FileLocker;
import com.mobimvp.privacybox.service.filelocker.internal.EncryptItem;
import com.mobimvp.privacybox.ui.browser.MediaBrowserAdapter.MediaType;
import com.mobimvp.privacybox.utility.ImageWorker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MediaLockAdapter extends BaseAdapter {

    private MediaType mType;
    private LayoutInflater mInflater;
    private List<EncryptItem> mList;
    private HashSet<EncryptItem> mSelectedFiles;
    private FileLockerListener mListener;
    private Context mContext;
    private boolean mEditMode;
    private EncryptedThumbLoader mThumbLoader;
    private FileLocker mLocker;

    public MediaLockAdapter(Context context, MediaType type, List<EncryptItem> list) {
        mContext = context;
        mType = type;
        mList = list;
        mInflater = LayoutInflater.from(mContext);
        mSelectedFiles = new HashSet<EncryptItem>();
        mLocker = FileLocker.getInstance();
        mThumbLoader = new EncryptedThumbLoader();
    }

    public void recycle() {
        mThumbLoader.recycle();
    }

    public void setFileLockerListener(FileLockerListener listener) {
        mListener = listener;
    }

    public void toggleEditMode() {
        mEditMode = !mEditMode;
        mSelectedFiles.clear();

        notifyDataSetChanged();
    }

    public boolean isEditMode() {
        return mEditMode;
    }

    public List<EncryptItem> getSelectedFiles() {
        return new ArrayList<EncryptItem>(mSelectedFiles);
    }

    public void select(int position) {
        if (mEditMode) {
            EncryptItem ei = mList.get(position);
            if (mSelectedFiles.contains(ei)) {
                mSelectedFiles.remove(ei);
            } else {
                mSelectedFiles.add(ei);
            }
            notifyDataSetChanged();
        } else if (mListener != null) {
            mListener.OnPreviewItem(position);
        }
    }

    public void reverseSelect() {
        for (EncryptItem ei : mList) {
            if (mSelectedFiles.contains(ei)) {
                mSelectedFiles.remove(ei);
            } else {
                mSelectedFiles.add(ei);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.mediabrowser_item, null);
            holder = new ItemHolder();
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            holder.container = (RelativeLayout) convertView.findViewById(R.id.thumbnail_container);
            holder.folderInfo = (LinearLayout) convertView.findViewById(R.id.folderinfo);
            holder.videoIndicator = (ImageView) convertView.findViewById(R.id.video);
            holder.selected = (CheckBox) convertView.findViewById(R.id.selected);
            holder.fileName = (TextView) convertView.findViewById(R.id.filename);
            holder.folderInfo.setVisibility(View.GONE);
            holder.container.setBackgroundResource(R.drawable.ic_media_background);
            if (mType == MediaType.Video) {
                holder.videoIndicator.setVisibility(View.VISIBLE);
            } else {
                holder.videoIndicator.setVisibility(View.GONE);
            }
            convertView.setTag(holder);
        } else {
            holder = (ItemHolder) convertView.getTag();
        }

        EncryptItem ei = mList.get(position);
        mThumbLoader.loadImage(ei, holder.thumbnail);
        holder.fileName.setText(ei.getName());

        if (mEditMode) {
            holder.selected.setVisibility(View.VISIBLE);
            holder.selected.setChecked(mSelectedFiles.contains(ei));
        } else {
            holder.selected.setVisibility(View.GONE);
        }

        return convertView;
    }

    private static class ItemHolder {
        public ImageView thumbnail;
        public RelativeLayout container;
        public LinearLayout folderInfo;
        public ImageView videoIndicator;
        public CheckBox selected;
        public TextView fileName;
    }

    private class EncryptedThumbLoader extends ImageWorker {

        public EncryptedThumbLoader() {
            super(mContext);
        }

        @Override
        protected Bitmap processBitmap(Object data) {
            EncryptItem ei = (EncryptItem) data;
            return mLocker.decryptThumb(ei);
        }
    }

}
