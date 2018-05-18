package cn.rongcloud.im.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dbcapp.club.R;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.server.response.GetTransferHistoryResponse;
import cn.rongcloud.im.utils.CommonUtils;

/**
 * Created by star1209 on 2018/5/17.
 */

public class TransferHistoryAdapter extends RecyclerView.Adapter {

    private List<GetTransferHistoryResponse.ResultEntity> mDatas = new ArrayList<>();

    public void addData(List<GetTransferHistoryResponse.ResultEntity> datas, boolean isEmpty){
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transfer_history, parent, false);
        return new HistoryHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        HistoryHolder viewHolder = (HistoryHolder) holder;
        GetTransferHistoryResponse.ResultEntity data = mDatas.get(position);
        viewHolder.mTvTime.setText(CommonUtils.longToDate(data.getAcceptTime1(), "yyyy-MM-dd HH:mm:ss"));
        viewHolder.mTvName.setText(data.getUserName());
        viewHolder.mTvAmount.setText(
                String.format(viewHolder.mTvAmount.getContext().getString(R.string.baojia_transfer_history_amount),
                        CommonUtils.twoDecimalFormat(data.getAmount())));
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public static class HistoryHolder extends RecyclerView.ViewHolder {

        private TextView mTvName;
        private TextView mTvTime;
        private TextView mTvAmount;

        public HistoryHolder(View itemView) {
            super(itemView);

            mTvName = itemView.findViewById(R.id.tv_user_name);
            mTvTime = itemView.findViewById(R.id.tv_time);
            mTvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }
}
