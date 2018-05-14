package cn.rongcloud.im.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

    private List<String> mPhotosUri = new ArrayList<>();

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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PhotoHolder photoHolder = (PhotoHolder) holder;
        ImageLoader.getInstance().displayImage(mPhotosUri.get(position), photoHolder.imageView, App.getOptions());
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
}
