package com.episode6.android.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.os.Environment;

public class DataUtils {
//	private static final String TAG = "DataUtils";
	
	public static final int DEFAULT_BUFFER_SIZE = 2048;
	public static final String DEFAULT_CHARSET_NAME = "UTF-8";
	
	public interface ProgressListener {
		public void onProgressUpdate(int totalBytesRead);
	}
	
	
	
	
	
	/*
	 * BASIC METHODS
	 */
	
	public static void closeStreams(InputStream from, OutputStream to, boolean closeInput, boolean closeOutput) {
		try {
			if (to != null)
				to.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			if (closeInput && from != null)
				from.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			if (closeOutput && to != null)
				to.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void copyInputStreamToOutputStream(InputStream from, OutputStream to, int bufferSize, boolean closeInput, boolean closeOutput, ProgressListener progressListener) {
		try {
			int totalBytesRead = 0;
			int bytesRead = 0;
			int offset = 0;
			byte[] data = new byte[bufferSize];
			
			while((bytesRead = from.read(data, offset, bufferSize)) > 0) {
				totalBytesRead += bytesRead;
				to.write(data, offset, bytesRead);
				if (progressListener != null)
					progressListener.onProgressUpdate(totalBytesRead);
//				Log.d(TAG, "Copied " + totalBytesRead + " bytes");
			}
			closeStreams(from, to, closeInput, closeOutput);
		} catch (Exception e) {
			closeStreams(from, to, closeInput, closeOutput);
			throw new RuntimeException(e);
		}
	}
	
	public static void copyInputStreamToFile(InputStream from, File to, int bufferSize, boolean closeInput, boolean overwrite, ProgressListener progressListener) {
		if (!to.getParentFile().exists())
			to.getParentFile().mkdirs();
		if (overwrite && to.exists()) {
			recursiveDelete(to);
		}
		if (!to.exists()) {
			try {
				to.createNewFile();
				copyInputStreamToOutputStream(from, new BufferedOutputStream(new FileOutputStream(to), bufferSize), bufferSize, closeInput, true, progressListener);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
	
	public static void copyFileToOutputStream(File from, OutputStream to, boolean closeOutput) {
		if (!from.exists())
			return;
		try {
			copyInputStreamToOutputStream(new FileInputStream(from), to, DEFAULT_BUFFER_SIZE, true, closeOutput, null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static void copyFileToFile(File from, File to, boolean overwrite, boolean deleteOriginal) {
		if ((!from.exists()) || from.isDirectory())
			return;
		try {
			copyInputStreamToFile(new FileInputStream(from), to, DEFAULT_BUFFER_SIZE, true, overwrite, null);
			if (deleteOriginal) {
				from.delete();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void copyTextToFile(String text, File to, boolean overwrite) {
		ByteArrayInputStream from = new ByteArrayInputStream(text.getBytes());
		copyInputStreamToFile(from, to, DEFAULT_BUFFER_SIZE, true, overwrite, null);
	}
	

	
	
	
	
	
	/*
	 * RECURSIVE METHODS
	 */
	
	public static void recursiveDelete(File file) {
		if (!file.exists())
			return;
		if (file.isDirectory()) {
			File[] contents = file.listFiles();
			for (int i = 0; i<contents.length; i++)
				recursiveDelete(contents[i]);
		}
		try {
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void recursiveDelete(String filePath) {
		recursiveDelete(new File(filePath));
	}
	
	public static void recursiveCopy(File from, File targetDirectory, boolean overwrite, boolean deleteOriginal) {
		if (!from.exists())
			return;
		
		if (!targetDirectory.exists())
			targetDirectory.mkdirs();
		
		File newTo = new File(targetDirectory.getAbsolutePath() + "/" + from.getName());
		if (from.isDirectory()) {
			newTo.mkdirs();
			File[] contents = from.listFiles();
			for (int i = 0; i<contents.length; i++) {
				recursiveCopy(contents[i], newTo, overwrite, deleteOriginal);
			}
			if (deleteOriginal)
				from.delete();
		} else {
			copyFileToFile(from, newTo, overwrite, deleteOriginal);
		}
	}
	
	
	
	
	
	
	
	/*
	 * GET TEXT METHODS
	 */
	
	public static String getTextFromStream(InputStream from, String charSetName, int bufferSize, boolean closeInput, ProgressListener progressListener) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		copyInputStreamToOutputStream(from, bos, bufferSize, closeInput, true, progressListener);
		try {
			return bos.toString(charSetName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static String getTextFromStream(InputStream from, boolean closeInput, ProgressListener progressListener) {
		return getTextFromStream(from, DEFAULT_CHARSET_NAME, DEFAULT_BUFFER_SIZE, closeInput, progressListener);
	}
	
	public static String getTextFromFile(File from) {
		try {
			return getTextFromStream(new FileInputStream(from), true, null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static String getTextFromFile(String filePath) {
		return getTextFromFile(new File(filePath));
	}
	
	public static String getTextFromResource(Context c, int resId) {
		return getTextFromStream(c.getResources().openRawResource(resId), true, null);
	}
	
	
	
	
	
	
	
	/*
	 * RESOURCE METHODS
	 */
	
	public static void copyRawResourceToOutputStream(Context c, int fromResId, OutputStream to) {
		copyRawResourceToOutputStream(c, fromResId, to, DEFAULT_BUFFER_SIZE);
	}
	
	public static void copyRawResourceToOutputStream(Context c, int fromResId, OutputStream to, int bufferSize) {
		InputStream from = c.getResources().openRawResource(fromResId);
		copyInputStreamToOutputStream(from, to, bufferSize, true, true, null);
	}
	
	public static void copyRawResourceToFile(Context c, int fromResId, File destFile, boolean overwrite) {
		copyRawResourceToFile(c, fromResId, destFile, DEFAULT_BUFFER_SIZE, overwrite);
	}
	
	public static void copyRawResourceToFile(Context c, int fromResId, File destFile, int bufferSize, boolean overwrite) {
		InputStream from = c.getResources().openRawResource(fromResId);
		copyInputStreamToFile(from, destFile, bufferSize, true, overwrite, null);
	}
	
	
	
	
	
	
	/*
	 * ZIP METHODS
	 */
	
	public static boolean extractZipArchive(File zipArchive, File targetDirectory, boolean maintainFolderStructure, boolean overwrite) {
		if ((!zipArchive.exists()) || (!zipArchive.isFile()))
			return false;
		if (!targetDirectory.exists())
			targetDirectory.mkdirs();
		if (!targetDirectory.isDirectory())
			return false;
		
		ZipInputStream zipIn = null;
		BufferedOutputStream output = null;
		String targetPath = targetDirectory.getAbsolutePath() + "/";
		
		try {
			zipIn = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipArchive)));
			
			ZipEntry entry = null;
			
			while ((entry = zipIn.getNextEntry()) != null) {
				
				String entryName = entry.getName();
				
				if (entry.isDirectory()) {
					if (maintainFolderStructure) {
						File dir = new File(targetPath + entryName);
						dir.mkdirs();
					}
				} else {
					File targetFile = null;
					if (maintainFolderStructure) {
						targetFile = new File(targetPath + entryName);
						if (!targetFile.getParentFile().exists()) {
							targetFile.getParentFile().mkdirs();
						}
					} else {
						targetFile = new File(targetPath + entryName.substring(entryName.lastIndexOf("/")+1));
					}
					
					copyInputStreamToFile(zipIn, targetFile, DEFAULT_BUFFER_SIZE, false, overwrite, null);					
				}
			}
			
			zipIn.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			closeStreams(zipIn, output, true, true);
			return false;
		}
		
	}
	
	
	
	
	
	
	
	/**
	 * DEVELOPMENT FUNCTION
	 * @param text
	 */
	public static void DEV_copyTextToPublicTempFile(String text, String prefix) {
		if (text == null)
			return;
		File dest = new File(Environment.getExternalStorageDirectory(), "e6tmpfile_" + prefix + "_" + System.currentTimeMillis() + ".tmp");
		copyTextToFile(text, dest, true);
	}
}
