package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dbcapp.club.R;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.server.response.GetCustomerListResponse;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;

/**
 * Created by star1209 on 2018/5/26.
 */

public class CustomerAdapter extends RecyclerView.Adapter {

    private List<GetCustomerListResponse.ResultEntity> mDatas = new ArrayList<>();
    private Context mContext;

    public void setData(List<GetCustomerListResponse.ResultEntity> datas){
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    public void addData(List<GetCustomerListResponse.ResultEntity> datas, boolean isEmpty){
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
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_customer_list, parent, false);
        return new CustomerHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CustomerHolder customerHolder = (CustomerHolder) holder;
        final GetCustomerListResponse.ResultEntity entity = mDatas.get(position);
        customerHolder.tv.setText(entity.getCustomerName());
        ImageLoader.getInstance().displayImage(entity.getPortrait(), customerHolder.iv, App.getOptions());

        customerHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RongIM.getInstance().startCustomerServiceChat(mContext, entity.getCustomerID(), entity.getCustomerName(), null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public static class CustomerHolder extends RecyclerView.ViewHolder {

        private TextView tv;
        private SelectableRoundedImageView iv;

        public CustomerHolder(View itemView) {
            super(itemView);

            tv = itemView.findViewById(R.id.tv_customer_name);
            iv = itemView.findViewById(R.id.iv_customer_portrait);
        }
    }
}
