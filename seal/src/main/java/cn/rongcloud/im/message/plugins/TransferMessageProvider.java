package cn.rongcloud.im.message.plugins;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dbcapp.club.R;

import org.json.JSONException;
import org.json.JSONObject;

import cn.rongcloud.im.ui.activity.TransferDetailActivity;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.Message;

/**
 * Created by star1209 on 2018/5/15.
 */

@ProviderTag(messageContent = TransferMessage.class, showProgress = false, showReadState = true)
public class TransferMessageProvider extends IContainerItemProvider.MessageProvider<TransferMessage> {

    @Override
    public void bindView(View view, int i, TransferMessage transferMessage, UIMessage uiMessage) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        if (uiMessage.getMessageDirection() == Message.MessageDirection.RECEIVE)
            viewHolder.layoutBg.setBackgroundResource(R.drawable.baojia_transfer_msg_item_receive);
        else
            viewHolder.layoutBg.setBackgroundResource(R.drawable.baojia_transfer_msg_item_send);

        if (TextUtils.isEmpty(transferMessage.getLeaveWord())){
            try {
                JSONObject jsonObject = new JSONObject(transferMessage.getExtra());
                viewHolder.tvLeave.setText(jsonObject.optString("content"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            viewHolder.tvLeave.setText(transferMessage.getLeaveWord());
        }
    }

    @Override
    public Spannable getContentSummary(TransferMessage transferMessage) {
        return null;
    }

    @Override
    public void onItemClick(final View view, int i, final TransferMessage transferMessage, UIMessage uiMessage) {
        Intent intent = new Intent(view.getContext(), TransferDetailActivity.class);
        try {
            JSONObject jsonObject = new JSONObject(transferMessage.getExtra());
            intent.putExtra("username", jsonObject.optString("name"));
            intent.putExtra("transfer_leave", jsonObject.optString("content"));
            intent.putExtra("amount", jsonObject.optDouble("money"));
            intent.putExtra("portrait", jsonObject.optString("portraitUri"));
            intent.putExtra("content", jsonObject.optString("content"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        view.getContext().startActivity(intent);
    }

    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trasnfer_msg, null);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.tvLeave = view.findViewById(R.id.tv_transfer_leave);
        viewHolder.layoutBg = view.findViewById(R.id.ll_transfer_item_bg);
        view.setTag(viewHolder);
        return view;
    }

    public static class ViewHolder {
        private TextView tvLeave;
        private LinearLayout layoutBg;
    }

    @Override
    public Spannable getSummary(UIMessage data) {
        return super.getSummary(data);
    }

    @Override
    public Spannable getContentSummary(Context context, TransferMessage data) {
//        if (data != null && !TextUtils.isEmpty(data.getSyncName())
//                && !TextUtils.isEmpty(data.getUserName())) {
//            if (data.getSyncName().equals(RongIM.getInstance().getCurrentUserId())) {
//                String str_RecommendClause = context.getResources().getString(cn.rongcloud.contactcard.R.string.rc_recommend_clause_to_others);
//                return new SpannableString(String.format(str_RecommendClause, data.getUserName()));
//            } else {
//                String str_RecommendClause = context.getResources().getString(cn.rongcloud.contactcard.R.string.rc_recommend_clause_to_me);
//                return new SpannableString(String.format(str_RecommendClause, data.getUserName(), data.getUserName()));
//            }
//        }
        return super.getContentSummary(context, data);
    }
}
