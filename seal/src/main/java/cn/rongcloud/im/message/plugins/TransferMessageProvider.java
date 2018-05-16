package cn.rongcloud.im.message.plugins;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dbcapp.club.R;

import cn.rongcloud.im.ui.activity.TransferDetailActivity;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider;

/**
 * Created by star1209 on 2018/5/15.
 */

@ProviderTag(messageContent = TransferMessage.class, showProgress = false, showReadState = true)
public class TransferMessageProvider extends IContainerItemProvider.MessageProvider<TransferMessage> {

    @Override
    public void bindView(View view, int i, TransferMessage transferMessage, UIMessage uiMessage) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.tvLeave.setText(transferMessage.getLeaveWord());
    }

    @Override
    public Spannable getContentSummary(TransferMessage transferMessage) {
        return null;
    }

    @Override
    public void onItemClick(final View view, int i, final TransferMessage transferMessage, UIMessage uiMessage) {
        Intent intent = new Intent(view.getContext(), TransferDetailActivity.class);
        intent.putExtra("username", transferMessage.getUserName());
        intent.putExtra("transfer_leave", transferMessage.getLeaveWord());
        intent.putExtra("amount", transferMessage.getMoney());
        intent.putExtra("portrait", transferMessage.getPortrait());
        view.getContext().startActivity(intent);
    }

    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trasnfer_msg, null);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.tvLeave = view.findViewById(R.id.tv_transfer_leave);
        view.setTag(viewHolder);
        return view;
    }

    public static class ViewHolder {
        private TextView tvLeave;
    }
}
