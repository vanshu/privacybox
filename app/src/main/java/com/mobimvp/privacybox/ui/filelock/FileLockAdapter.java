package com.mobimvp.privacybox.ui.filelock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.service.filelocker.internal.EncryptItem;

public class FileLockAdapter extends BaseAdapter {
	
	private static class ItemHolder {
		public ImageView type;
		public TextView name;
		public TextView size;
		public CheckBox selected;
		public int position;
	}
	
	private View.OnClickListener rowClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			ItemHolder holder = (ItemHolder) v.getTag();
			if (mEditMode) {
				EncryptItem ei = mList.get(holder.position);
				if (mSelectedFiles.contains(ei)) {
					mSelectedFiles.remove(ei);
				} else {
					mSelectedFiles.add(ei);
				}
				notifyDataSetChanged();
			} else {
				if (mListener != null) {
					mListener.OnPreviewItem(holder.position);
				}
			}
		}
	};
	
	private View.OnLongClickListener rowLongClickListener = new View.OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			if (!mEditMode) {
				ItemHolder holder = (ItemHolder) v.getTag();
				toggleEditMode();
				mSelectedFiles.add(mList.get(holder.position));
				notifyDataSetChanged();
				if (mListener != null) {
					mListener.onEnterEditMode(holder.position);
				}
			}
			return true;
		}
	};
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<EncryptItem> mList;
	private HashSet<EncryptItem> mSelectedFiles;
	private FileLockerListener mListener;
	
	private boolean mEditMode;

	public FileLockAdapter(Context context, List<EncryptItem> list) {
		mContext = context;
		mList = list;
		mInflater = LayoutInflater.from(mContext);
		mSelectedFiles = new HashSet<EncryptItem>();
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
	
	public void reverseSelect(){
		for(EncryptItem ei:mList){
			if(mSelectedFiles.contains(ei)){
				mSelectedFiles.remove(ei);
			}else{
				mSelectedFiles.add(ei);
			}
		}
		notifyDataSetChanged();
	}
	public List<EncryptItem> getSelectedFiles() {
		return new ArrayList<EncryptItem>(mSelectedFiles);
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
			convertView = mInflater.inflate(R.layout.filebrowser_item, null);
			holder = new ItemHolder();
			holder.type = (ImageView) convertView.findViewById(R.id.type);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.size = (TextView) convertView.findViewById(R.id.size);
			holder.selected = (CheckBox) convertView.findViewById(R.id.selected);
			holder.selected.setClickable(false);
			holder.selected.setFocusable(false);
			convertView.setTag(holder);
			convertView.setOnClickListener(rowClickListener);
			convertView.setOnLongClickListener(rowLongClickListener);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}
		
		holder.position = position;
		
		EncryptItem ei = mList.get(position);
		holder.type.setImageResource(R.drawable.ic_file);
		holder.name.setText(ei.getName());
		holder.size.setText(Formatter.formatFileSize(mContext, ei.getSize()));
		
		if (mEditMode) {
			holder.selected.setVisibility(View.VISIBLE);
			holder.selected.setChecked(mSelectedFiles.contains(ei));
		} else {
			holder.selected.setVisibility(View.GONE);
		}
		
		return convertView;
	}
}
