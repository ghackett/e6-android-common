package com.episode6.android.common.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.provider.MediaStore;


public class ImageUtils {
	
	public static Uri putImageFileIntoGalleryAndGetUri(Context c, File imageFile, boolean deleteImageFileAfter) {
		if (imageFile.exists() && imageFile.isFile()) {
			try {
				Uri dataUri = Uri.parse(MediaStore.Images.Media.insertImage(c.getContentResolver(), imageFile.getAbsolutePath(), null, null));
				if (deleteImageFileAfter)
					imageFile.delete();
				return dataUri;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}	
		}
		return null;
	}
	
	public static Uri putBitmapIntoGalleryAndGetUri(Context c, Bitmap image, boolean recycleOriginal) {
		if (image != null) {
			Uri dataUri = Uri.parse(MediaStore.Images.Media.insertImage(c.getContentResolver(), image, null, null));
			if (recycleOriginal)
				image.recycle();
			return dataUri;	
		}
		return null;
	}
	
    public static Bitmap scaleDownBitmap(Bitmap original, int maxDimension, boolean recycleOriginal) {
    	int origWidth = original.getWidth();
    	int origHeight = original.getHeight();
    	
    	if (origWidth <= maxDimension && origHeight <= maxDimension) {
    		Bitmap b = Bitmap.createBitmap(original);
    		if (recycleOriginal)
    			original.recycle();
    		return b;
    	}
    	
    	int newWidth = 0;
    	int newHeight = 0;
    	
    	float ratio = (float)origHeight / (float)origWidth;
    	
    	if (origWidth > origHeight) {
    		newWidth = maxDimension;
    		newHeight = (int)((float)newWidth * ratio);
    	} else {
    		newHeight = maxDimension;
    		newWidth = (int)((float)newHeight / ratio);
    	}
    	
    	Bitmap rtr = Bitmap.createScaledBitmap(original, newWidth, newHeight, false);
    	if (recycleOriginal)
    		original.recycle();
    	return rtr;
    }
    
    public static void scaleDownImageFile(File originalImageFile, int maxDimension, CompressFormat format, int quality) {
    	Bitmap b = BitmapFactory.decodeFile(originalImageFile.getAbsolutePath());
    	if (b == null)
    		throw new RuntimeException("Original image could not be decoded.");
    	
    	try {
	    	b = scaleDownBitmap(b, maxDimension, true);
	    	originalImageFile.delete();
	    	originalImageFile.createNewFile();
	    	BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(originalImageFile));
	    	b.compress(format, quality, outputStream);
	    	outputStream.close();
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
    public static Bitmap scaleDownImageUriToBitmap(Context c, Uri imageUri, int maxDimension, boolean deleteOriginal) {
    	try {
    		InputStream mediaStream = c.getContentResolver().openInputStream(imageUri);
        	Bitmap b = BitmapFactory.decodeStream(mediaStream);
        	mediaStream.close();
        	b = scaleDownBitmap(b, maxDimension, true);
        	
        	if (deleteOriginal)
        		c.getContentResolver().delete(imageUri, null, null);
        	return b;
        	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
    public static File scaleDownImageUriToTmpFile(Context c, Uri imageUri, int maxDimension, CompressFormat format, int quality, boolean deleteOriginal) {
//    	if (!StorageTricks.checkExtStorage())
//    		return null;

    	Bitmap b = scaleDownImageUriToBitmap(c, imageUri, maxDimension, deleteOriginal);
    	if (b == null) 
    		return null;
    	
    	try {
        	File tmpFile = File.createTempFile("scaledImage", (format == CompressFormat.JPEG ? ".jpg" : ".png"), c.getFilesDir());
//        	File tmpFile = new File(StorageTricks.CAMERA_TEMP_DIR, "scaledImage." + (format == CompressFormat.JPEG ? "jpg" : "png"));
        	tmpFile.createNewFile();
        	BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile));
        	b.compress(format, quality, outputStream);
        	
        	outputStream.close();
        	b.recycle();
        	
        	
        	return tmpFile;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    	
    }
    
    public static Uri scaleDownImageUri(Context c, Uri imageUri, int maxDimension, CompressFormat format, int quality, boolean deleteOriginal) {
    	try {
    		Bitmap b = scaleDownImageUriToBitmap(c, imageUri, maxDimension, deleteOriginal);
//	    	File tmpFile = scaleDownImageUriToTmpFile(imageUri, maxDimension, format, quality, deleteOriginal);
	    	
	    	if (b == null)
	    		return null;
	    	
	    	Uri rtr = Uri.parse(MediaStore.Images.Media.insertImage(c.getContentResolver(), b, null, null));
	    	b.recycle();
	    	return rtr;
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
}
