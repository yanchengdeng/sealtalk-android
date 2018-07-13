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

import cn.rongcloud.im.server.response.AccountHistoryResponse;
import cn.rongcloud.im.ui.activity.AcountHistoryActivity;
import cn.rongcloud.im.utils.CommonUtils;

public class AccountHistoryAdapter extends RecyclerView.Adapter<AccountHistoryAdapter.ViewHoldHistory> {


    private List<AccountHistoryResponse.ResultEntity> mDatas = new ArrayList<>();
    private Context context;
    public AccountHistoryAdapter(AcountHistoryActivity acountHistoryActivity) {
        this.context = acountHistoryActivity;
    }

    @Override
    public ViewHoldHistory onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transfer_history, parent, false);
        return new ViewHoldHistory(view);
    }

    @Override
    public void onBindViewHolder(ViewHoldHistory holder, int position) {
        ViewHoldHistory viewHolder = (ViewHoldHistory) holder;
        AccountHistoryResponse.ResultEntity data = mDatas.get(position);
        viewHolder.mTvTime.setText(CommonUtils.longToDate(data.getCreateTime1(), "yyyy-MM-dd HH:mm:ss"));
        viewHolder.mTvName.setText(data.getTrxType());
        viewHolder.mTvAmount.setText(
                String.format(viewHolder.mTvAmount.getContext().getString(R.string.baojia_transfer_history_amount),
                        CommonUtils.twoDecimalFormat(data.getAmount())));

    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public void addData(List<AccountHistoryResponse.ResultEntity> datas, boolean isEmpty) {
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

    class ViewHoldHistory extends RecyclerView.ViewHolder{

        private TextView mTvName;
        private TextView mTvTime;
        private TextView mTvAmount;

        public ViewHoldHistory(View itemView) {
            super(itemView);

            mTvName = itemView.findViewById(R.id.tv_user_name);
            mTvTime = itemView.findViewById(R.id.tv_time);
            mTvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }
}
