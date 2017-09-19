package com.mobimvp.privacybox.ui.widgets;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mobimvp.privacybox.R;

public class ListViewEx extends ListView {
	
	private DataSetObserver mObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			refreshUI();
		}
	};
	
	private View mLoadingView;
	private TextView mLoadingText;
	private TextView mEmptyText;
	private boolean mLoading;
	
	public void setEmptyText(int resId) {
		setEmptyText(getContext().getString(resId));
	}
	
	public void setEmptyText(CharSequence text) {
		if (mEmptyText == null) {
			mEmptyText = new TextView(getContext());
			mEmptyText.setTextAppearance(getContext(), R.style.TextAppearance_Medium);
			mEmptyText.setGravity(Gravity.CENTER);
			mEmptyText.setFocusable(true);
			mEmptyText.setClickable(true);
		}
		mEmptyText.setText(text);
		refreshUI();
	}
	
	public void showLoadingScreen(CharSequence loadingText) {
		if (mLoadingView == null) {
			mLoadingView = LayoutInflater.from(getContext()).inflate(R.layout.widget_progress_dialog, null);
			mLoadingText = (TextView) mLoadingView.findViewById(R.id.message);
		}
		mLoadingText.setText(loadingText);
		mLoading = true;
		refreshUI();
	}
	
	public void hideLoadingScreen() {
		mLoading = false;
		refreshUI();
	}
	
	public boolean isLoading() {
		return mLoading;
	}
	

	public ListViewEx(Context context) {
		super(context);
	}
	

	public ListViewEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	
	public ListViewEx(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (getAdapter() != null) {
			getAdapter().unregisterDataSetObserver(mObserver);
		}
		if (adapter != null) {
			adapter.registerDataSetObserver(mObserver);
		}
		super.setAdapter(adapter);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		refreshUI();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		refreshUI();
	}

	private void setupAndAdd(View child) {
		ViewParent viewParent = getParent();
		if (viewParent != null) {
			ViewGroup parent = (ViewGroup) viewParent;
			ViewParent childParent = child.getParent();
			if (childParent != null && childParent != viewParent) {
				((ViewGroup)childParent).removeView(child);
			}
			if (parent.indexOfChild(child) < 0) {
				int index = parent.indexOfChild(this);
				ViewGroup.LayoutParams layoutParams = getLayoutParams();
				if (layoutParams != null) {
					parent.addView(child, index, layoutParams);
				} else {
					parent.addView(child, index);
				}
			}
		}
	}
	
	private void refreshUI() {
		boolean overriden = false;
		if (mEmptyText != null) {
			setupAndAdd(mEmptyText);
			mEmptyText.setVisibility(GONE);
		}
		if (mLoadingView != null) {
			setupAndAdd(mLoadingView);
			mLoadingView.setVisibility(GONE);
		}
		int count = getCount();
		if (getAdapter() != null) {
			count = getAdapter().getCount();
		}
		if (count == 0 && mEmptyText != null) {
			mEmptyText.setVisibility(VISIBLE);
			overriden = true;
		}
		if (mLoading && mLoadingView != null) {
			if (mEmptyText != null) {
				mEmptyText.setVisibility(GONE);
			}
			mLoadingView.setVisibility(VISIBLE);
			overriden = true;
		}
		if (overriden) {
			setVisibility(GONE);
		} else {
			setVisibility(VISIBLE);
		}
		
	}
}
