package cn.rongcloud.im.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.dbcapp.club.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealCSEvaluateInfo;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.model.SealCSEvaluateItem;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.ui.activity.ReadReceiptDetailActivity;
import cn.rongcloud.im.ui.widget.BottomEvaluateDialog;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imlib.CustomServiceConfig;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.cs.CustomServiceManager;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * 会话 Fragment 继承自ConversationFragment
 * onResendItemClick: 重发按钮点击事件. 如果返回 false,走默认流程,如果返回 true,走自定义流程
 * onReadReceiptStateClick: 已读回执详情的点击事件.
 * 如果不需要重写 onResendItemClick 和 onReadReceiptStateClick ,可以不必定义此类,直接集成 ConversationFragment 就可以了
 */
public class ConversationFragmentEx extends ConversationFragment implements
        RongIM.OnSendMessageListener {
    private OnShowAnnounceListener onShowAnnounceListener;
    private BottomEvaluateDialog dialog;
    private List<SealCSEvaluateItem> mEvaluateList;
    private String mTargetId = "";
    private RongExtension rongExtension;
    private ListView listView;
    private boolean mIsDeleted;

    private List<Integer> mIds = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RongIMClient.getInstance().setCustomServiceHumanEvaluateListener(new CustomServiceManager.OnHumanEvaluateListener() {
            @Override
            public void onHumanEvaluate(JSONObject evaluateObject) {
                JSONObject jsonObject = evaluateObject;
                SealCSEvaluateInfo sealCSEvaluateInfo = new SealCSEvaluateInfo(jsonObject);
                mEvaluateList = sealCSEvaluateInfo.getSealCSEvaluateInfoList();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        rongExtension = (RongExtension) v.findViewById(io.rong.imkit.R.id.rc_extension);
        View messageListView = findViewById(v, io.rong.imkit.R.id.rc_layout_msg_list);
        listView = findViewById(messageListView, io.rong.imkit.R.id.rc_list);
        return v;
    }

    @Override
    protected void initFragment(Uri uri) {
        super.initFragment(uri);
        if (uri != null) {
            mTargetId = uri.getQueryParameter("targetId");
        }
        RongIM.getInstance().setSendMessageListener(this);

        RongIM.getInstance().getTotalUnreadCount(new RongIMClient.ResultCallback<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                RongIM.getInstance().getHistoryMessages(Conversation.ConversationType.PRIVATE,
                        mTargetId, -1, 100, new RongIMClient.ResultCallback<List<Message>>() {
                            @Override
                            public void onSuccess(List<Message> messages) {
                                for (Message message : messages){
                                    if(message.getContent() instanceof TextMessage){
                                        TextMessage textMessage = (TextMessage) message.getContent();
                                        if ("delete".equals(textMessage.getExtra())){
                                            mIds.add(message.getMessageId());
                                        }
                                    }else if (message.getContent() instanceof ImageMessage){
                                        ImageMessage imageMessage = (ImageMessage) message.getContent();
                                        if ("delete".equals(imageMessage.getExtra())){
                                            mIds.add(message.getMessageId());
                                        }
                                    }else if (message.getContent() instanceof VoiceMessage){
                                        VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
                                        if ("delete".equals(voiceMessage.getExtra())){
                                            mIds.add(message.getMessageId());
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {

                            }
                        });
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });

        BroadcastManager.getInstance(getContext()).addAction(SealConst.BAOJIA_DELETE_AFTER_READ,
                new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    JSONObject jsonObject = new JSONObject(intent.getStringExtra("String"));
                    if (jsonObject.optString("targetId").equals(mTargetId)){
                        mIds.add(Integer.parseInt(jsonObject.optString("id")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        //被删除好友通知
        BroadcastManager.getInstance(getContext()).addAction(SealConst.BAOJIA_DELELTE_CONTACT, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NToast.shortToast(getContext(), R.string.baojia_delete_by_contact);
                mIsDeleted = true;
            }
        });
    }

    @Override
    public void onReadReceiptStateClick(io.rong.imlib.model.Message message) {
        if (message.getConversationType() == Conversation.ConversationType.GROUP) { //目前只适配了群组会话
            Intent intent = new Intent(getActivity(), ReadReceiptDetailActivity.class);
            intent.putExtra("message", message);
            getActivity().startActivity(intent);
        }
    }

    public void onWarningDialog(String msg) {
        String typeStr = getUri().getLastPathSegment();
        if (!typeStr.equals("chatroom")) {
            super.onWarningDialog(msg);
        }
    }

    @Override
    public void onShowAnnounceView(String announceMsg, String announceUrl) {
        if (onShowAnnounceListener != null) {
            onShowAnnounceListener.onShowAnnounceView(announceMsg, announceUrl);
        }
    }

    /**
     * 显示通告栏的监听器
     */
    public interface OnShowAnnounceListener {

        /**
         * 展示通告栏的回调
         *
         * @param announceMsg 通告栏展示内容
         * @param annouceUrl  通告栏点击链接地址，若此参数为空，则表示不需要点击链接，否则点击进入链接页面
         * @return
         */
        void onShowAnnounceView(String announceMsg, String annouceUrl);
    }

    public void setOnShowAnnounceBarListener(OnShowAnnounceListener listener) {
        onShowAnnounceListener = listener;
    }

    public void showStartDialog(final String dialogId) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = new BottomEvaluateDialog(getActivity(), mEvaluateList);
        dialog.show();
        dialog.setEvaluateDialogBehaviorListener(new BottomEvaluateDialog.OnEvaluateDialogBehaviorListener() {
            @Override
            public void onSubmit(int source, String tagText, CustomServiceConfig.CSEvaSolveStatus resolveStatus, String suggestion) {
                RongIMClient.getInstance().evaluateCustomService(mTargetId, source, resolveStatus, tagText,
                        suggestion, dialogId, null);
                if (dialog != null && getActivity() != null) {
                    getActivity().finish();
                }
            }

            @Override
            public void onCancel() {
                if (dialog != null && getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }

    @Override
    public void onShowStarAndTabletDialog(String dialogId) {
        showStartDialog(dialogId);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null && getActivity().isFinishing()) {
            RongIMClient.getInstance().setCustomServiceHumanEvaluateListener(null);
        }
    }

    @Override
    public void onPluginToggleClick(View v, ViewGroup extensionBoard) {
        if (!rongExtension.isExtensionExpanded()) {
            listView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    listView.requestFocusFromTouch();
                    listView.setSelection(listView.getCount() - listView.getFooterViewsCount() - listView.getHeaderViewsCount());
                }
            }, 100);
        }
    }

    @Override
    public void onEmoticonToggleClick(View v, ViewGroup extensionBoard) {
        if (!rongExtension.isExtensionExpanded()) {
            listView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    listView.requestFocusFromTouch();
                    listView.setSelection(listView.getCount() - listView.getFooterViewsCount() - listView.getHeaderViewsCount());
                }
            }, 100);
        }
    }

    @Override
    public boolean showMoreClickItem() {
        return true;
    }

    @Override
    public void onDestroy() {
        int[] messageIds = new int[mIds.size()];
        for (int i = 0; i < mIds.size(); i ++){
            messageIds[i] = mIds.get(i);
        }

        RongIM.getInstance().deleteMessages(messageIds, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
        super.onDestroy();
    }

    @Override
    public void onSendToggleClick(View v, String text) {
        if (mIsDeleted){
            NToast.shortToast(getContext(), R.string.baojia_delete_by_contact);
            return;
        }
        super.onSendToggleClick(v, text);
    }

    @Override
    public Message onSend(Message message) {
        if (!SealAppContext.getInstance().isDeleteAfterReadFlag()){
            return message;
        }

        if (message.getContent() instanceof TextMessage){
            TextMessage textMessage = (TextMessage) message.getContent();
            textMessage.setExtra("delete");
            message.setContent(textMessage);
        }else if (message.getContent() instanceof ImageMessage){
            ImageMessage imageMessage = (ImageMessage) message.getContent();
            imageMessage.setExtra("delete");
            message.setContent(imageMessage);
        }else if (message.getContent() instanceof VoiceMessage){
            VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
            voiceMessage.setExtra("delete");
            message.setContent(voiceMessage);
        }

        return message;
    }

    @Override
    public boolean onSent(Message message, RongIM.SentMessageErrorCode sentMessageErrorCode) {
        if (message.getContent() instanceof TextMessage){
            TextMessage textMessage = (TextMessage) message.getContent();
            if ("delete".equals(textMessage.getExtra())){
                mIds.add(message.getMessageId());
            }
        }else if (message.getContent() instanceof ImageMessage){
            ImageMessage imageMessage = (ImageMessage) message.getContent();
            if ("delete".equals(imageMessage.getExtra())){
                mIds.add(message.getMessageId());
            }
        }else if (message.getContent() instanceof VoiceMessage){
            VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
            if ("delete".equals(voiceMessage.getExtra())){
                mIds.add(message.getMessageId());
            }
        }
        return false;
    }
}
