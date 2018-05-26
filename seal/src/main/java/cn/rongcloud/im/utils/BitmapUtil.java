package cn.rongcloud.im.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Administrator on 2017/7/17.
 */

public class BitmapUtil {

    /**
     * 获取控件中的位图
     * @param view
     * @return
     */
    public static Bitmap getBitmapOfView(View view){
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * 压缩bitmap
     * @param path 图片路径
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap getSimpleBitmap(String path, int reqWidth, int reqHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = getSimpleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 根据控件进行压缩
     * @param path
     * @param view
     * @return
     */
    public static Bitmap getSimpleBitmap(String path, ImageView view){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = getSimpleSize(options, view.getWidth(), view.getHeight());
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 根据图片的options获取缩放比例
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int getSimpleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        int scale = 1;
        int widthScale = 0;
        int heightScale = 0;
        if (reqWidth != 0){
            widthScale = options.outWidth / reqWidth;
        }

        if (reqHeight != 0){
            heightScale = options.outHeight / reqHeight;
        }

        if (widthScale > heightScale && widthScale > 1){
            scale = widthScale;
        }else if (heightScale > widthScale && heightScale > 1){
            scale = heightScale;
        }

        return scale;
    }

    /**
     * 根据矩阵处理图片
     * @param originalBit
     * @param colorMatrixArray
     * @return
     */
    public static Bitmap getMetricBitmap(Bitmap originalBit, float[] colorMatrixArray){
        Bitmap bitmap = Bitmap.createBitmap(originalBit.getWidth(), originalBit.getHeight(), originalBit.getConfig());
        ColorMatrix colorMatrix = new ColorMatrix(colorMatrixArray);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        Paint paint = new Paint();
        paint.setColorFilter(filter);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(originalBit, 0, 0, paint);
        return bitmap;
    }

    /**
     * 缩放图片
     *
     * @param src       源图片
     * @param newWidth  新宽度
     * @param newHeight 新高度
     * @param recycle   是否回收
     * @return 缩放后的图片
     */
    public static Bitmap scale(final Bitmap src, final int newWidth, final int newHeight, final boolean recycle) {
        Bitmap ret = Bitmap.createScaledBitmap(src, newWidth, newHeight, true);
        if (recycle && !src.isRecycled()) src.recycle();
        return ret;
    }
}
