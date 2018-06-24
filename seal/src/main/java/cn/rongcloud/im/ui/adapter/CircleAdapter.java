package cn.rongcloud.im.ui.adapter;

import android.app.Activity;
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
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.ui.widget.linkpreview.GridItemDecoration;
import cn.rongcloud.im.utils.CommonUtils;
import cn.rongcloud.im.utils.PerfectClickListener;
import io.rong.imageloader.core.ImageLoader;

/**
 * Created by star1209 on 2018/5/13.
 */

public class CircleAdapter extends RecyclerView.Adapter {

    private List<GetCircleResponse.ResultEntity> mDatas = new ArrayList<>();
    private String mSyncName;
    private OnDeleteListener mOnDeleteListener;
    private OnImageClickListener mOnImageClickListener;
    private OnLikeClickListerner mOnLikeClickListener;//喜欢
    private OnComplainClickListerner mOnComplainClickListener;//投诉

    private OnCollectedClickListerner onCollectedClickListerner;//收藏
    private Activity context;



    public CircleAdapter(Activity context, String mSyncName) {
        this.mSyncName = mSyncName;
        this.context = context;

    }

    public void setOnDeleteListener(OnDeleteListener listener){
        mOnDeleteListener = listener;
    }

    public void setOnImageClickListener(OnImageClickListener listener){
        mOnImageClickListener = listener;
    }

    public void setLikeClickListener(OnLikeClickListerner listener){
        mOnLikeClickListener = listener;
    }

    public void setmOnComplainClickListener(OnComplainClickListerner listener){
        mOnComplainClickListener = listener;
    }

    public void setOnCollectedClickListerner(OnCollectedClickListerner listener){
        onCollectedClickListerner = listener;
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

    public List<GetCircleResponse.ResultEntity>  getDatas(){
        return mDatas;
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_circle, parent, false);
        return new CircleHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
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

        circleHolder.tvLike.setText(""+data.getLikeCount());
        circleHolder.tvComplain.setText(""+data.getComplaintCount());
        circleHolder.tvCollected.setText(""+data.getCollectCount());

        circleHolder.tvLike.setOnClickListener(new PerfectClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                if (getDatas().get(position).isLike()){
                    if (context!=null) {
                        NToast.shortToast(context, R.string.has_like_circle);
                    }
                    return;
                }
                if (mOnLikeClickListener!=null){
                    mOnLikeClickListener.onLike(position);
                }

            }
        });

        circleHolder.tvComplain.setOnClickListener(new PerfectClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                if (getDatas().get(position).isComplaint()){
                    if (context!=null) {
                        NToast.shortToast(context, R.string.has_complaint_circle);
                    }
                    return;
                }
                if (mOnComplainClickListener!=null){
                    mOnComplainClickListener.onComplain(position);
                }
            }
        });


        circleHolder.tvCollected.setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                if (getDatas().get(position).isCollect()){
                    if (context!=null) {
                        NToast.shortToast(context, R.string.has_collect_circle);
                    }
                    return;
                }
                if (onCollectedClickListerner!=null){
                    onCollectedClickListerner.onColleced(position);
                }
            }
        });

        circleHolder.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mOnDeleteListener != null){
                    mOnDeleteListener.onDelete(data.getId());
                }
            }
        });

        circleHolder.adapter.setOnImageClickListener(new CirclePhotoAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(int index) {
                mOnImageClickListener.onImageClick(data.getCircleImagePath(), index);
            }
        });


    }

    //喜欢
    private void doLike(int position) {

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
        private TextView tvLike,tvComplain,tvCollected;

        public CircleHolder(View itemView) {
            super(itemView);
            Context context = itemView.getContext();

            ivPortrait = itemView.findViewById(R.id.iv_portrait_circle);
            tvContent = itemView.findViewById(R.id.tv_content_circle);
            lvPhotos = itemView.findViewById(R.id.lv_photo_circle);
            tvTime = itemView.findViewById(R.id.tv_time_circle);
            tvName = itemView.findViewById(R.id.tv_username_circle);
            tvDelete = itemView.findViewById(R.id.tv_delete_circle);
            tvLike = itemView.findViewById(R.id.tv_like);
            tvComplain = itemView.findViewById(R.id.tv_complain);
            tvCollected = itemView.findViewById(R.id.tv_collect);

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


    public interface OnLikeClickListerner{
        void onLike(int id);
    }

    public interface OnComplainClickListerner{
        void onComplain(int id);
    }

    public interface  OnCollectedClickListerner{
        void onColleced(int id);
    }

    public interface OnImageClickListener{
        void onImageClick(List<String> urls, int position);
    }
}
