package com.mining.app.zxing.decoding;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class QRCodeUtil {

	/**
     * 生成二维码Bitmap
     *
     * @param content   内容
     * @param widthPix  图片宽度
     * @param heightPix 图片高度
     * @param logoBm    二维码中心的Logo图标（可以为null）
     * @param filePath  用于存储二维码图片的文件路径
     * @return 生成二维码及保存文件是否成功
     */ 
    public static boolean createQRImage(String content, int widthPix, int heightPix, Bitmap logoBm, String filePath) { 
        try { 
            if (content == null || "".equals(content)) { 
                return false; 
            } 
   
            //配置参数 
            Map<EncodeHintType, Object> hints = new HashMap<>(); 
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8"); 
            //容错级别 
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); 
            //设置空白边距的宽度 
            hints.put(EncodeHintType.MARGIN, 1); //default is 4 
   
            // 图像数据转换，使用了矩阵转换 
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints); 
            int[] pixels = new int[widthPix * heightPix]; 
            // 下面这里按照二维码的算法，逐个生成二维码的图片， 
            // 两个for循环是图片横列扫描的结果 
            for (int y = 0; y < heightPix; y++) { 
                for (int x = 0; x < widthPix; x++) { 
                    if (bitMatrix.get(x, y)) { 
                        pixels[y * widthPix + x] = 0xff000000; 
                    } else { 
                        pixels[y * widthPix + x] = 0xffffffff; 
                    } 
                } 
            } 
   
            // 生成二维码图片的格式，使用ARGB_8888 
            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888); 
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix); 
   
            if (logoBm != null) { 
                bitmap = addLogo(bitmap, logoBm); 
            } 
   
            //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！ 
            return bitmap != null && bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(filePath)); 
        } catch (WriterException | IOException e) { 
            e.printStackTrace(); 
        } 
   
        return false; 
    } 
   
    /**
     * 在二维码中间添加Logo图案
     */ 
    private static Bitmap addLogo(Bitmap src, Bitmap logo) { 
        if (src == null) { 
            return null; 
        } 
   
        if (logo == null) { 
            return src; 
        } 
   
        //获取图片的宽高 
        int srcWidth = src.getWidth(); 
        int srcHeight = src.getHeight(); 
        int logoWidth = logo.getWidth(); 
        int logoHeight = logo.getHeight(); 
   
        if (srcWidth == 0 || srcHeight == 0) { 
            return null; 
        } 
   
        if (logoWidth == 0 || logoHeight == 0) { 
            return src; 
        } 
   
        //logo大小为二维码整体大小的1/5 
        float scaleFactor = srcWidth * 1.0f / 6 / logoWidth; 
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888); 
        try { 
            Canvas canvas = new Canvas(bitmap); 
            canvas.drawBitmap(src, 0, 0, null); 
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2); 
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null); 
   
            canvas.save(Canvas.ALL_SAVE_FLAG); 
            canvas.restore(); 
        } catch (Exception e) { 
            bitmap = null; 
            e.getStackTrace(); 
        } 
   
        return bitmap; 
    } 
    
    public static final boolean isChineseCharacter(String chineseStr) {
		char[] charArray = chineseStr.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			// 是否是Unicode编码,除了"�"这个字符.这个字符要另外处理
			if ((charArray[i] >= '\u0000' && charArray[i] < '\uFFFD')
					|| ((charArray[i] > '\uFFFD' && charArray[i] < '\uFFFF'))) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @author paulburke
	 */
	public static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/"
							+ split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection,
						selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri,
			String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}
}
