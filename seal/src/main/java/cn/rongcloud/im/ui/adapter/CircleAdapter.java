package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dbcapp.club.R;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.server.response.GetCircleResponse;
import cn.rongcloud.im.ui.widget.linkpreview.GridItemDecoration;
import cn.rongcloud.im.utils.CommonUtils;
import io.rong.imageloader.core.ImageLoader;

/**
 * Created by star1209 on 2018/5/13.
 */

public class CircleAdapter extends RecyclerView.Adapter {

    private List<GetCircleResponse.ResultEntity> mDatas = new ArrayList<>();
    private String mSyncName;
    private OnDeleteListener mOnDeleteListener;

    public CircleAdapter(String mSyncName) {
        this.mSyncName = mSyncName;
    }

    public void setOnDeleteListener(OnDeleteListener listener){
        mOnDeleteListener = listener;
    }

    public void addData(List<GetCircleResponse.ResultEntity> datas, boolean isEmpty){
        if (datas == null){
            return;
        }

        if (isEmpty){
            mDatas = datas;
            notifyDataSetChanged();
        }else {
            mDatas.addAll(datas);
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_circle, parent, false);
        return new CircleHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CircleHolder circleHolder = (CircleHolder) holder;
        final GetCircleResponse.ResultEntity data = mDatas.get(position);
        ImageLoader.getInstance().displayImage(data.getPortrait(), circleHolder.ivPortrait, App.getOptions());
        circleHolder.tvContent.setText(data.getContent());
        circleHolder.tvTime.setText(CommonUtils.longToDate(data.getPublishTime(), "yyyy-MM-dd HH:mm"));
        circleHolder.tvName.setText(data.getUserName());
        if (data.getCircleImagePath() == null || data.getCircleImagePath().size() == 0){
            circleHolder.lvPhotos.setVisibility(View.GONE);
        }else {
            circleHolder.lvPhotos.setVisibility(View.VISIBLE);
            circleHolder.adapter.setData(data.getCircleImagePath());
        }

        if (data.getSyncName().equals(mSyncName)){
            circleHolder.tvDelete.setVisibility(View.VISIBLE);
        }else {
            circleHolder.tvDelete.setVisibility(View.GONE);
        }

        circleHolder.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnDeleteListener != null){
                    mOnDeleteListener.onDelete(data.getId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public static class CircleHolder extends RecyclerView.ViewHolder {

        private ImageView ivPortrait;
        private TextView tvContent;
        private RecyclerView lvPhotos;
        private TextView tvTime;
        private CirclePhotoAdapter adapter;
        private TextView tvName;
        private TextView tvDelete;

        public CircleHolder(View itemView) {
            super(itemView);
            Context context = itemView.getContext();

            ivPortrait = itemView.findViewById(R.id.iv_portrait_circle);
            tvContent = itemView.findViewById(R.id.tv_content_circle);
            lvPhotos = itemView.findViewById(R.id.lv_photo_circle);
            tvTime = itemView.findViewById(R.id.tv_time_circle);
            tvName = itemView.findViewById(R.id.tv_username_circle);
            tvDelete = itemView.findViewById(R.id.tv_delete_circle);

            GridLayoutManager manager = new GridLayoutManager(context, 3);
            lvPhotos.setLayoutManager(manager);
            adapter = new CirclePhotoAdapter();
            lvPhotos.setAdapter(adapter);
            lvPhotos.addItemDecoration(new GridItemDecoration(3,
                    itemView.getContext().getResources().getDimensionPixelOffset(R.dimen.baojia_circle_photo_decoration),
                    true));
        }
    }

    public interface OnDeleteListener{
        void onDelete(long id);
    }
}
