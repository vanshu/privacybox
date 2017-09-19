package com.mobimvp.privacybox.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video.Thumbnails;

public class BitmapUtility {
	
	public static Bitmap createVideoThumb(File file) {
		Bitmap thumb=ThumbnailUtils.createVideoThumbnail(file.toString(), Thumbnails.MINI_KIND);
		System.out.println("thumb-width:"+thumb.getWidth()+",thumb-height:"+thumb.getHeight());
		return thumb;
	}
	
	public static Bitmap createThumbnail(File file, int size) throws IOException {
		// 取得图片
		InputStream temp =new FileInputStream(file);
		BitmapFactory.Options options = new BitmapFactory.Options();
		// 这个参数代表，不为bitmap分配内存空间，只记录一些该图片的信息（例如图片大小），说白了就是为了内存优化
		options.inJustDecodeBounds = true;
		// 通过创建图片的方式，取得options的内容（这里就是利用了java的地址传递来赋值）
		try {
			BitmapFactory.decodeStream(temp, null, options);
		} catch (Throwable t) {
		}
		// 关闭流
		temp.close();

		// 生成压缩的图片
		int i = 0;
		Bitmap bitmap = null;
		while (true) {
			// 这一步是根据要设置的大小，使宽和高都能满足
			if ((options.outWidth >> i <= size)
					&& (options.outHeight >> i <= size)) {
				// 重新取得流，注意：这里一定要再次加载，不能二次使用之前的流！
				temp = new FileInputStream(file);
				// 这个参数表示 新生成的图片为原始图片的几分之一。
				options.inSampleSize = (int) Math.pow(2.0D, i);
				// 这里之前设置为了true，所以要改为false，否则就创建不出图片
				options.inJustDecodeBounds = false;

				bitmap = BitmapFactory.decodeStream(temp, null, options);
				break;
			}
			i += 1;
		}
		return bitmap;
	}
}
