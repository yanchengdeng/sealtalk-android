package cn.rongcloud.im.ui.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bm.library.PhotoView;
import com.dbcapp.club.R;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.utils.CommonUtils;
import io.rong.imageloader.core.ImageLoader;

/**
 * Created by star1209 on 2018/5/14.
 */

public class CirclePhotoAdapter extends RecyclerView.Adapter {

    private Dialog mDlgImageShow;
    private Context mContext;
    private OnImageClickListener mOnImageClickListener;

    private List<String> mPhotosUri = new ArrayList<>();

    public void setOnImageClickListener(OnImageClickListener listener){
        mOnImageClickListener = listener;
    }

//    public CirclePhotoAdapter(Context context){
//        mContext = context;
//        if (mDlgImageShow == null){
//            mDlgImageShow = new Dialog(mContext);
//            View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_circle_image_gallery, null);
//            ViewPager vpGallery = view.findViewById(R.id.vp_gallery);
//
//            vpGallery.setAdapter(new PagerAdapter() {
//                @Override
//                public int getCount() {
//                    if (mPhotos != null) {
//                        return mPhotos.size();
//                    }
//                    return 0;
//                }
//
//                @Override
//                public boolean isViewFromObject(View view, Object object) {
//                    return view == object;
//                }
//
//                @Override
//                public void destroyItem(ViewGroup container, int position, Object object) {
//                    container.removeView((View) object);
//                }
//
//                @Override
//                public Object instantiateItem(ViewGroup container, int position) {
//                    PhotoView photoView = new PhotoView(mContext);
//                    photoView.setBackgroundColor(Color.BLACK);
//                    photoView.enable();
//                    photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                    photoView.setBackgroundColor(Color.BLACK);
//                    container.addView(photoView);
//                    ImageLoader.getInstance().displayImage(mContext, photoView, mPhotosUri.get(position).getPath());
//
//                    photoView.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            mDlgImageShow.dismiss();
//                        }
//                    });
//                    return photoView;
//                }
//            });
//
//            mDlgImageShow.setContentView(view);
//        }
//
//        mDlgImageShow.show();
//
//        Window window = mDlgImageShow.getWindow();
//        WindowManager.LayoutParams lp = window.getAttributes();
//        window.setGravity(Gravity.CENTER);
//        lp.gravity = Gravity.CENTER;
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//        window.setAttributes(lp);
//        window.getDecorView().setPadding(0,0,0,0);
//        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//    }

    public void setData(List<String> datas){
        if (datas == null){
            return;
        }

        mPhotosUri = datas;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView imageView = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_circle_photo, parent, false);
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.height = ((CommonUtils.getScreenWidth((Activity) parent.getContext()) -
                parent.getContext().getResources().getDimensionPixelOffset(R.dimen.baojia_circle_container_paddingleft) -
                parent.getContext().getResources().getDimensionPixelOffset(R.dimen.baojia_circle_container_paddingright) -
                parent.getContext().getResources().getDimensionPixelOffset(R.dimen.baojia_circle_portrait_size) -
                parent.getContext().getResources().getDimensionPixelOffset(R.dimen.baojia_circle_container_paddingleft) -
                parent.getContext().getResources().getDimensionPixelOffset(R.dimen.baojia_circle_photo_decoration) * 4)) / 3;
        imageView.setLayoutParams(params);
        return new PhotoHolder(imageView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        PhotoHolder photoHolder = (PhotoHolder) holder;
        ImageLoader.getInstance().displayImage(mPhotosUri.get(position), photoHolder.imageView, App.getOptions());

        photoHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnImageClickListener != null){
                    mOnImageClickListener.onImageClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPhotosUri.size();
    }

    public static class PhotoHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public PhotoHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView;
        }
    }

    public interface OnImageClickListener{
        void onImageClick(int index);
    }
}
