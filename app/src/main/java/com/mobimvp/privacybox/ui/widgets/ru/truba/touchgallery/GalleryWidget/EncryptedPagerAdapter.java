/*
 Copyright (c) 2012 Roman Truba

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial
 portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.mobimvp.privacybox.ui.widgets.ru.truba.touchgallery.GalleryWidget;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.service.filelocker.FileLocker;
import com.mobimvp.privacybox.service.filelocker.internal.EncryptItem;
import com.mobimvp.privacybox.ui.widgets.ru.truba.touchgallery.TouchView.TouchImageView;
import com.mobimvp.privacybox.utility.BitmapUtility;
import com.mobimvp.privacybox.utility.ImageWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Class wraps URLs to adapter, then it instantiates <b>UrlTouchImageView</b>
 * objects to paging up through them.
 */
public class EncryptedPagerAdapter extends PagerAdapter {

    private List<EncryptItem> mResources;
    private List<ViewHolder> mHolders;
    private FileLocker mLock;
    private EncryptedImageLoader mImageLoader;
    private DisplayMetrics mDM;
    private Context mContext;
    private LayoutInflater mInflater;

    public EncryptedPagerAdapter(Context context, DisplayMetrics dm, List<EncryptItem> resources) {
        this.mResources = resources;
        mHolders = new ArrayList<ViewHolder>();
        for (int i = 0; i < mResources.size(); i++) {
            mHolders.add(new ViewHolder());
        }
        this.mContext = context;
        this.mDM = dm;
        this.mLock = FileLocker.getInstance();
        this.mImageLoader = new EncryptedImageLoader();
        this.mImageLoader.setLoadingImage(R.drawable.progress_bar);
        this.mInflater = LayoutInflater.from(mContext);
    }

    public void recycle() {
        this.mImageLoader.recycle();
    }

    public void removeItem(int position) {
        if (position < mResources.size())
            mResources.remove(position);
        if (position < mHolders.size())
            mHolders.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        ViewHolder holder = (ViewHolder) object;
        ((GalleryViewPager) container).mCurrentView = holder.tiv;
    }

    @Override
    public Object instantiateItem(View collection, int position) {
        ViewHolder holder = mHolders.get(position);
        holder.child = mInflater.inflate(R.layout.preview_item, null);
        holder.tiv = (TouchImageView) holder.child.findViewById(R.id.image);
        mImageLoader.loadImage(mResources.get(position), holder.tiv);
        ((ViewPager) collection).addView(holder.child, 0);
        return holder;
    }

    @Override
    public void destroyItem(View collection, int position, Object object) {
        ViewHolder holder = (ViewHolder) object;
        ((ViewPager) collection).removeView(holder.child);
        holder.child = null;
        holder.tiv = null;
    }

    @Override
    public int getCount() {
        return mResources.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        ViewHolder holder = (ViewHolder) object;
        return holder != null && view.equals(holder.child);
    }

    @Override
    public int getItemPosition(Object object) {
        for (int i = 0; i < mHolders.size(); i++) {
            if (mHolders.get(i).equals(object)) {
                return i;
            }
        }
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public void finishUpdate(View arg0) {
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(View arg0) {
    }

    private static class ViewHolder {
        public View child;
        public TouchImageView tiv;
    }

    private class EncryptedImageLoader extends ImageWorker {

        private int resizeSize;

        public EncryptedImageLoader() {
            super(mContext);
            setImageFadeIn(false);
            resizeSize = Math.max(mDM.widthPixels, mDM.heightPixels);
        }

        @Override
        protected Bitmap processBitmap(Object data) {
            final EncryptItem ei = (EncryptItem) data;
            final CountDownLatch latch = new CountDownLatch(1);
            FileLocker.OperateListener listener = new FileLocker.OperateListener() {
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
                    latch.countDown();
                }

                @Override
                public void onError(EncryptItem ei, int error) {
                }
            };

            mLock.decryptTemp(ei, listener);
            try {
                latch.await();
            } catch (Exception e) {
            }

            try {
                return BitmapUtility.createThumbnail(new File(ei.getTempPath()), resizeSize);
            } catch (Exception e) {
                return null;
            }
        }

    }
}