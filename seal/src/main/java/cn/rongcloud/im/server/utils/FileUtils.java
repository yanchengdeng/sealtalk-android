package cn.rongcloud.im.server.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2017/6/7.
 */

public class FileUtils {

    /**
     * 读取assets下的文件内容
     * @param context
     * @param fileName 目标文件名
     * @return 返回字符串内容
     */
    public static String getJsonFromAssets(Context context, String fileName){
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            int length = inputStream.available();
            byte[] buffer = new byte[length];
            inputStream.read(buffer);
            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
        }

        return "";
    }

    /**
     * 获取外部存储
     * @return
     */
    public static String getSdcard(){
        return Environment.getExternalStorageDirectory().getPath();

    }

    /**
     * 创建文件
     * @param path
     * @return
     */
    public static File createFile(String path){
        File file = new File(path);
        if (file.exists()){
            file.delete();
        }
        try {
            if (createOrExistsDir(file.getParentFile())){
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    /**
     * 判断目录是否存在，不存在则判断是否创建成功
     *
     * @param file 文件
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsDir(final File file) {
        // 如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 文件路径转uri
     * @param path
     * @return
     */
    public static Uri fileToUri(Context context, String path){
        File file = new File(path);
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID }, MediaStore.Images.Media.DATA + "=? ",
                new String[] { path }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (file.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, path);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * uri转文件路径
     * @param selectedVideoUri
     * @param contentResolver
     * @return
     */
    public static String getFilePathFromContentUri(Context context, Uri selectedVideoUri, ContentResolver contentResolver) {
        String path = null;
        // 通过uri和selection来获取真实的图片路径
        Cursor cursor = context.getContentResolver().query(selectedVideoUri, null, null, null,
                null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
}
