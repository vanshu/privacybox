package com.mobimvp.privacybox.service.filelocker;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mobimvp.privacybox.service.filelocker.internal.EncryptItem;
import com.mobimvp.privacybox.utility.crypto.EncryptionManager;

public class FileLockerHelper {
	public static Bitmap decryptThumb(EncryptionManager em, EncryptItem item) {
		File inFile = new File(item.getThumbPath());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			em.decryptStream(inFile, baos, null);
		} catch (Exception e) {
			return null;
		}
		byte[] byteArray = baos.toByteArray();
		return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
	}

}
