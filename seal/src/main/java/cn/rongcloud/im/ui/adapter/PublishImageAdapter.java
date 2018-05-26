package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.dbcapp.club.R;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.utils.BitmapUtil;
import cn.rongcloud.im.utils.CommonUtils;
import io.rong.imageloader.core.ImageLoader;

/**
 * Created by star1209 on 2018/5/18.
 */

public class PublishImageAdapter extends RecyclerView.Adapter {

    private static final int IMAGE_TYPE = 0;
    private static final int BUTTON_TYPE = 1;

    private List<String> mDatas = new ArrayList<>();

    private OnAddImageListener mOnAddImageListener;

    public void setOnAddImageListener(OnAddImageListener listener){
        mOnAddImageListener = listener;
    }

    public void addData(String data){
        mDatas.add(data);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        if (IMAGE_TYPE == viewType){
            ImageView imageView = new ImageView(context);
            int size = (CommonUtils.getScreenWidth(context) -
                    (context.getResources().getDimensionPixelOffset(R.dimen.baojia_publish_decoration) * 4)) / 3;
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size, size);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ImageHolder(imageView);
        }else {
            Button button = new Button(context);
            int size = (CommonUtils.getScreenWidth(context) -
                    (context.getResources().getDimensionPixelOffset(R.dimen.baojia_publish_decoration) * 4)) / 3;
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size, size);
            button.setLayoutParams(params);
            return new ButtonHolder(button);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageHolder){
            ImageHolder imageHolder = (ImageHolder) holder;
            imageHolder.imageView.setImageBitmap(BitmapUtil.getSimpleBitmap(mDatas.get(position), imageHolder.imageView));
        }else if (holder instanceof ButtonHolder){
            ((ButtonHolder) holder).button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnAddImageListener != null){
                        mOnAddImageListener.onAddImage();
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mDatas.size() == 9){
            return IMAGE_TYPE;
        }else if (mDatas.size() == 0) {
            return BUTTON_TYPE;
        }else {
            if (position < mDatas.size()){
                return IMAGE_TYPE;
            }else {
                return BUTTON_TYPE;
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mDatas.size() == 9){
            return mDatas.size();
        }else {
            return mDatas.size() + 1;
        }
    }

    public static class ImageHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public ImageHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }

    public static class ButtonHolder extends RecyclerView.ViewHolder {

        private Button button;

        public ButtonHolder(View itemView) {
            super(itemView);
            button = (Button) itemView;
            button.setBackgroundResource(R.drawable.baojia_publish_add_image);
        }
    }

    public interface OnAddImageListener{
        void onAddImage();
    }
}
