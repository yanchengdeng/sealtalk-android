package cn.rongcloud.im.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.dbcapp.club.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealCSEvaluateInfo;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.message.plugins.DeleteAfterReadPlugin;
import cn.rongcloud.im.model.ReadBean;
import cn.rongcloud.im.model.SealCSEvaluateItem;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.ui.activity.ReadReceiptDetailActivity;
import cn.rongcloud.im.ui.widget.BottomEvaluateDialog;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.plugin.ImagePlugin;
import io.rong.imkit.utilities.ExtensionHistoryUtil;
import io.rong.imlib.CustomServiceConfig;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.cs.CustomServiceManager;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.ReadReceiptMessage;
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
    private List<ReadBean> mBean = new ArrayList<>();
    private EditText editText;
    private ImageView rc_emoticon_toggle;
    private ImageView rc_emoticon_toggle1;
    private ImageView rc_voice_toggle;
    private ImageView rc_voice_toggle1;
    private ImageView rc_plugin_toggle;
    private View mVoiceInputToggle;
    private ViewGroup mPluginLayout;
    private FrameLayout mSendToggle;

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
        editText = (EditText) v.findViewById(io.rong.imkit.R.id.rc_edit_text);
        rc_plugin_toggle = (ImageView) v.findViewById(io.rong.imkit.R.id.rc_plugin_toggle);
        rc_emoticon_toggle = (ImageView) v.findViewById(io.rong.imkit.R.id.rc_emoticon_toggle);
        rc_emoticon_toggle1 = (ImageView) v.findViewById(io.rong.imkit.R.id.rc_emoticon_toggle1);
        rc_voice_toggle = (ImageView) v.findViewById(io.rong.imkit.R.id.rc_voice_toggle);
        rc_voice_toggle1 = (ImageView) v.findViewById(io.rong.imkit.R.id.rc_voice_toggle1);
        mPluginLayout = (ViewGroup) v.findViewById(io.rong.imkit.R.id.rc_plugin_layout);
        View messageListView = findViewById(v, io.rong.imkit.R.id.rc_layout_msg_list);
        mVoiceInputToggle = (View) v.findViewById(io.rong.imkit.R.id.rc_audio_input_toggle);
        mSendToggle = (FrameLayout) v.findViewById(io.rong.imkit.R.id.rc_send_toggle);
        mVoiceInputToggle.setVisibility(View.GONE);
        listView = findViewById(messageListView, io.rong.imkit.R.id.rc_list);
        setListener();
        return v;
    }

    private void setListener() {

        rc_emoticon_toggle1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mType == 1) {
                    List<IPluginModule> modules = rongExtension.getPluginModules();
                    for (IPluginModule module : modules) {
                        if (module instanceof ImagePlugin) {
                            module.onClick(ConversationFragmentEx.this, rongExtension);
                            rongExtension.collapseExtension();
                            return;
                        }
                    }
                }
            }
        });

        rc_voice_toggle1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mType == 1) {

                    if (mVoiceInputToggle.getVisibility() == View.GONE) {
                        editText.setVisibility(View.GONE);
                        mSendToggle.setVisibility(View.GONE);
                        mPluginLayout.setVisibility(View.VISIBLE);
                        hideInputKeyBoard();
                        showVoiceInputToggle();
                        //                        mContainerLayout.setClickable(true);
                        //                        mContainerLayout.setSelected(false);
                    } else {
                        editText.setVisibility(View.VISIBLE);
                        hideVoiceInputToggle();
                        if (editText.getText().length() > 0) {
                            mSendToggle.setVisibility(View.VISIBLE);
                            mPluginLayout.setVisibility(View.GONE);
                        } else {
                            mSendToggle.setVisibility(View.GONE);
                            mPluginLayout.setVisibility(View.VISIBLE);
                        }

                        showInputKeyBoard();
                        //                        mContainerLayout.setSelected(true);
                    }

                    //                    hidePluginBoard();
                    //                    hideEmoticonBoard();

                    rongExtension.collapseExtension();

                }
            }
        });


        //        rc_voice_toggle.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View view) {
        //                if (mType == 1) {
        //                    rc_voice_toggle.setImageResource(R.drawable.jj2j);
        //                }else {
        //
        //                }
        //                onSwitchToggleClick(view, rongExtension);
        //            }
        //        });

        //        List<IPluginModule> modules = rongExtension.getPluginModules();
        //        //        modules.get(modules.size() - 1).onClick(this,);
        //
        //        for (IPluginModule module : modules) {
        //            if (module instanceof DeleteAfterReadPlugin) {
        //                ((DeleteAfterReadPlugin) module).setListener(new DeleteAfterReadPlugin.Click() {
        //                    @Override
        //                    public void pos() {
        //                        if (SealAppContext.getInstance().isDeleteAfterReadFlag()) {
        //                            NToast.shortToast(getContext(), R.string.baojia_undelete_after_read);
        //
        //                        } else {
        //                            NToast.shortToast(getContext(), R.string.baojia_delete_after_read);
        //                        }
        //                    }
        //                });
        //                break;
        //            }
        //        }

        //        rongExtension.setExtensionClickListener(new IExtensionClickListener() {
        //            @Override
        //            public void onSendToggleClick(View view, String s) {
        //                NToast.shortToast(getContext(), "44441111");
        //            }
        //
        //            @Override
        //            public void onImageResult(List<Uri> list, boolean b) {
        //
        //            }
        //
        //            @Override
        //            public void onLocationResult(double v, double v1, String s, Uri uri) {
        //
        //            }
        //
        //            @Override
        //            public void onSwitchToggleClick(View view, ViewGroup viewGroup) {
        //
        //            }
        //
        //            @Override
        //            public void onVoiceInputToggleTouch(View view, MotionEvent motionEvent) {
        //
        //            }
        //
        //            @Override
        //            public void onEmoticonToggleClick(View view, ViewGroup viewGroup) {
        //
        //            }
        //
        //            @Override
        //            public void onPluginToggleClick(View view, ViewGroup viewGroup) {
        //            }
        //
        //            @Override
        //            public void onMenuClick(int i, int i1) {
        //            }
        //
        //            @Override
        //            public void onEditTextClick(EditText editText) {
        //
        //            }
        //
        //            @Override
        //            public boolean onKey(View view, int i, KeyEvent keyEvent) {
        //                return false;
        //            }
        //
        //            @Override
        //            public void onExtensionCollapsed() {
        //
        //            }
        //
        //            @Override
        //            public void onExtensionExpanded(int i) {
        //
        //            }
        //
        //            @Override
        //            public void onPluginClicked(IPluginModule iPluginModule, int i) {
        //                NToast.shortToast(getContext(), "333");
        //                if (iPluginModule instanceof DeleteAfterReadExtensionModule) {
        //                    if (SealAppContext.getInstance().isDeleteAfterReadFlag()) {
        //                        NToast.shortToast(getContext(), R.string.baojia_undelete_after_read);
        //                    } else {
        //                        NToast.shortToast(getContext(), R.string.baojia_delete_after_read);
        //                    }
        //                }
        //            }
        //
        //            @Override
        //            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //
        //            }
        //
        //            @Override
        //            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //
        //            }
        //
        //            @Override
        //            public void afterTextChanged(Editable editable) {
        //
        //            }
        //        });

    }

    @Override
    protected void initFragment(Uri uri) {
        super.initFragment(uri);
        if (uri != null) {
            mTargetId = uri.getQueryParameter("targetId");
        }
        RongIM.getInstance().setSendMessageListener(this);


        //        RongIM.getInstance().setMessageInterceptor(new RongIM.MessageInterceptor() {
        //            @Override
        //            public boolean intercept(Message message) {
        //                if (message.getContent() instanceof TextMessage) {
        //                    TextMessage textMessage = (TextMessage) message.getContent();
        //                    if (!TextUtils.isEmpty(textMessage.getExtra())) {
        //                        ReadBean bean = JsonUtils.objectFromJson(textMessage.getExtra(), ReadBean.class);
        //                        if (bean.getType() == 1002) {
        //                            RongIM.getInstance().deleteMessages(new int[]{bean.getMessageId()}, null);
        //
        //                            return true;
        //                        }
        //                    }
        //
        //                }
        //                return false;
        //            }
        //        });

        RongIMClient.getInstance().setReadReceiptListener(new RongIMClient.ReadReceiptListener() {
            @Override
            public void onReadReceiptReceived(Message message) {
                if (mTargetId.equals(message.getTargetId()) && message.getConversationType() == Conversation.ConversationType.PRIVATE) {
                    if (message.getContent() instanceof ReadReceiptMessage) {

                    }

                }
            }

            @Override
            public void onMessageReceiptRequest(Conversation.ConversationType conversationType, String s, String s1) {

            }

            @Override
            public void onMessageReceiptResponse(Conversation.ConversationType conversationType, String s, String s1, HashMap<String, Long> hashMap) {

            }
        });


        RongIM.getInstance().getTotalUnreadCount(new RongIMClient.ResultCallback<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                RongIM.getInstance().getHistoryMessages(Conversation.ConversationType.PRIVATE,
                        mTargetId, -1, 100, new RongIMClient.ResultCallback<List<Message>>() {
                            @Override
                            public void onSuccess(List<Message> messages) {
                                for (Message message : messages) {
                                    if (message.getContent() instanceof TextMessage) {
                                        TextMessage textMessage = (TextMessage) message.getContent();

                                        if (!TextUtils.isEmpty(textMessage.getExtra())) {
                                            //                                            ReadBean bean = JsonUtils.objectFromJson(textMessage.getExtra(), ReadBean.class);
                                            //                                            if (1001 == bean.getType()) {
                                            if ("delete".equals(textMessage.getExtra())) {
                                                mIds.add(message.getMessageId());
                                            }
                                            //                                                mBean.add(bean);
                                            //                                                setDeleteId(message.getMessageId());
                                            //                                            }

                                        }
                                    } else if (message.getContent() instanceof ImageMessage) {
                                        ImageMessage imageMessage = (ImageMessage) message.getContent();
                                        if (!TextUtils.isEmpty(imageMessage.getExtra())) {
                                            //                                            ReadBean bean = JsonUtils.objectFromJson(imageMessage.getExtra(), ReadBean.class);
                                            //                                            if (1001 == bean.getType()) {
                                            //                                                mIds.add(message.getMessageId());
                                            //                                                //                                                setDeleteId(message.getMessageId());
                                            //                                            }

                                            if ("delete".equals(imageMessage.getExtra())) {
                                                mIds.add(message.getMessageId());
                                            }
                                        }
                                    } else if (message.getContent() instanceof VoiceMessage) {
                                        VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
                                        if (!TextUtils.isEmpty(voiceMessage.getExtra())) {
                                            //                                            ReadBean bean = JsonUtils.objectFromJson(voiceMessage.getExtra(), ReadBean.class);
                                            //                                            if (1001 == bean.getType()) {
                                            //                                                mIds.add(message.getMessageId());
                                            //                                                //                                                setDeleteId(message.getMessageId());
                                            //                                            }

                                            if ("delete".equals(voiceMessage.getExtra())) {
                                                mIds.add(message.getMessageId());
                                            }
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

        //        BroadcastManager.getInstance(getContext()).addAction(SealConst.BAOJIA_DELETE_AFTER_READ,
        //                new BroadcastReceiver() {
        //                    @Override
        //                    public void onReceive(Context context, Intent intent) {
        //                        try {
        //                            JSONObject jsonObject = new JSONObject(intent.getStringExtra("String"));
        //                            if (jsonObject.optString("targetId").equals(mTargetId)) {
        //                                //                                mIds.add(Integer.parseInt(jsonObject.optString("id")));
        //                                //                                RongIM.getInstance().deleteMessages(Conversation.ConversationType.PRIVATE, jsonObject.optString("id"), new RongIMClient.ResultCallback<Boolean>() {
        //                                //                                    @Override
        //                                //                                    public void onSuccess(Boolean aBoolean) {
        //                                //
        //                                //                                    }
        //                                //
        //                                //                                    @Override
        //                                //                                    public void onError(RongIMClient.ErrorCode errorCode) {
        //                                //
        //                                //                                    }
        //                                //                                });
        //                                //                                for (Integer mId : mIds) {
        //                                //                                    if (jsonObject.optInt("id") == mId) {
        //                                //                                        RongIM.getInstance().deleteMessages(new int[]{mId}, null);
        //                                //                                        break;
        //                                //                                    }
        //                                //
        //                                //                                }
        //                                RongIM.getInstance().deleteMessages(new int[]{jsonObject.optInt("id")}, null);
        //                            }
        //                        } catch (JSONException e) {
        //                            e.printStackTrace();
        //                        }
        //                    }
        //                });

        BroadcastManager.getInstance(getContext()).addAction(SealConst.BAOJIA_DELETE_AFTER_READ,
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        try {
                            JSONObject jsonObject = new JSONObject(intent.getStringExtra("String"));
                            if (jsonObject.optString("targetId").equals(mTargetId)) {
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


    private void setDeleteId(int id) {
        //        RongIM.getInstance().deleteMessages(Conversation.ConversationType.PRIVATE, id + "", new RongIMClient.ResultCallback<Boolean>() {
        //            @Override
        //            public void onSuccess(Boolean aBoolean) {
        //
        //            }
        //
        //            @Override
        //            public void onError(RongIMClient.ErrorCode errorCode) {
        //            }
        //        });

        RongIM.getInstance().deleteMessages(new int[]{id}, null);
    }


    @Override
    public void onReadReceiptStateClick(io.rong.imlib.model.Message message) {
        if (message.getConversationType() == Conversation.ConversationType.GROUP) { //目前只适配了群组会话
            Intent intent = new Intent(getActivity(), ReadReceiptDetailActivity.class);
            intent.putExtra("message", message);
            getActivity().startActivity(intent);
        } else if (message.getConversationType() == Conversation.ConversationType.PRIVATE) {
            ReadReceiptMessage content = (ReadReceiptMessage) message.getContent();
            long ntfTime = content.getLastMessageSendTime();    //获取发送时间戳
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
        if (mType == 1) {
            if (!rongExtension.isExtensionExpanded()) {
                listView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listView.requestFocusFromTouch();
                        listView.setSelection(listView.getCount() - listView.getFooterViewsCount() - listView.getHeaderViewsCount());
                    }
                }, 100);
            }
            mType = 0;
            rc_plugin_toggle.setImageResource(R.drawable.j);
            SealAppContext.getInstance().setDeleteAfterReadFlag(false);
            rc_voice_toggle.setImageResource(R.drawable.rc_voice_toggle_selector);
            rc_emoticon_toggle.setImageResource(R.drawable.r);
            editText.setBackgroundResource(R.drawable.rc_edit_text_background_selector);
            hideVoiceInputToggle();
            showInputKeyBoard();
            super.onPluginToggleClick(v, extensionBoard);
        }
    }

    @Override
    public void onEmoticonToggleClick(View v, ViewGroup extensionBoard) {
        if (mType == 1) {
            //            this.conversationType = rongExtension.getConversationType();
            //            this.targetId = rongExtension.getTargetId();
            //            String[] permissions = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.CAMERA"};
            //            if (PermissionCheckUtil.checkPermissions(getContext(), permissions)) {
            //                Intent intent = new Intent(getActivity(), PictureSelectorActivity.class);
            //                rongExtension.startActivityForPluginResult(intent, 23, ImagePlugin.);
            //            } else {
            //                rongExtension.requestPermissionForPluginResult(permissions, 255, this);
            //            }

            return;

            //            return;
        } else {
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
    }

    private int mType = 0;

    @Override
    public void onExtensionCollapsed() {
        //        if (mType == 1) {
        //            //            NToast.shortToast(getContext(), "1");
        //            if (editText.getVisibility() == View.VISIBLE)
        //                rc_voice_toggle.setImageResource(R.drawable.s2_2);
        //            else {
        //                rc_voice_toggle.setImageResource(R.drawable.jj2j);
        //            }
        //            return;
        //        }
        super.onExtensionCollapsed();
    }

    @Override
    public void onExtensionExpanded(int h) {
        if (mType == 1) {
            //            rongExtension.collapseExtension();
            rc_voice_toggle1.setImageResource(R.drawable.s2);
            rc_plugin_toggle.setImageResource(R.drawable.x);
            rc_emoticon_toggle1.setImageResource(R.drawable.p);
            rc_emoticon_toggle.setVisibility(View.GONE);
            rc_emoticon_toggle1.setVisibility(View.VISIBLE);

            rc_voice_toggle.setVisibility(View.GONE);
            rc_voice_toggle1.setVisibility(View.VISIBLE);
            //            return;
        } else {
            rc_emoticon_toggle.setVisibility(View.VISIBLE);
            rc_emoticon_toggle1.setVisibility(View.GONE);
//            mVoiceInputToggle.setBackgroundResource(R.drawable.);
            rc_voice_toggle.setVisibility(View.VISIBLE);
            rc_voice_toggle1.setVisibility(View.GONE);
        }
        super.onExtensionExpanded(h);
    }

    @Override
    public void onEditTextClick(EditText editText) {
        if (mType == 1) {
            //                    NToast.shortToast(getContext(), "3");
            //                    return;
            rc_emoticon_toggle.setImageResource(R.drawable.p);
            return;
        }
        super.onEditTextClick(editText);
    }

    @Override
    public void onVoiceInputToggleTouch(View v, MotionEvent event) {


        super.onVoiceInputToggleTouch(v, event);
    }

    @Override
    public void onSwitchToggleClick(View v, ViewGroup inputBoard) {

        super.onSwitchToggleClick(v, inputBoard);
        //        if (mType == 1) {
        //            ((ImageView) v).setImageResource(R.drawable.jj2j);
        //
        //            return;
        //        }

    }

    @Override
    public void onPluginClicked(IPluginModule pluginModule, int position) {
        if (pluginModule instanceof DeleteAfterReadPlugin) {
            if (!SealAppContext.getInstance().isDeleteAfterReadFlag()) {
                mType = 1;
                rongExtension.collapseExtension();
                rc_voice_toggle1.setImageResource(R.drawable.s2);
                editText.setBackgroundResource(R.drawable.baojia_conersation_input_bg);
                rc_plugin_toggle.setImageResource(R.drawable.x);
                rc_emoticon_toggle1.setImageResource(R.drawable.p);
                rc_emoticon_toggle.setVisibility(View.GONE);
                rc_emoticon_toggle1.setVisibility(View.VISIBLE);
                rc_voice_toggle.setVisibility(View.GONE);
                rc_voice_toggle1.setVisibility(View.VISIBLE);
                //                mVoiceInputToggle.setBackgroundResource(R.drawable.baojia_voice_input_bg);
                //                rc_plugin_toggle.setOnClickListener(new View.OnClickListener() {
                //                    @Override
                //                    public void onClick(View view) {
                //
                //                        super.
                //                    }
                //                });

            } else {
                mType = 0;
                rc_voice_toggle.setImageResource(R.drawable.rc_voice_toggle_selector);
                editText.setBackgroundResource(io.rong.imkit.R.drawable.rc_edit_text_background_selector);
                rc_emoticon_toggle.setImageResource(R.drawable.r);
                rc_plugin_toggle.setImageResource(R.drawable.j);
                rongExtension.setMenuVisibility(View.GONE);
                rc_emoticon_toggle.setVisibility(View.VISIBLE);
                rc_emoticon_toggle1.setVisibility(View.GONE);
                rc_voice_toggle.setVisibility(View.VISIBLE);
                rc_voice_toggle1.setVisibility(View.GONE);
                //                mVoiceInputToggle.setBackgroundResource(R.drawable.rc_voice_input_selector);
            }
            return;
        }
        super.onPluginClicked(pluginModule, position);

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mType == 1) {
            rc_emoticon_toggle.setImageResource(R.drawable.p);
            return;
        }
        super.afterTextChanged(s);
    }

    @Override
    public boolean showMoreClickItem() {
        return true;
    }

    @Override
    public void onDestroy() {
        int[] messageIds = new int[mIds.size()];
        for (int i = 0; i < mIds.size(); i++) {
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
        if (mIsDeleted) {
            NToast.shortToast(getContext(), R.string.baojia_delete_by_contact);
            return;
        }
        super.onSendToggleClick(v, text);
    }


    @Override
    public Message onSend(Message message) {
        if (!SealAppContext.getInstance().isDeleteAfterReadFlag()) {
            return message;
        }
        if (message.getContent() instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message.getContent();

            textMessage.setExtra("delete");
            message.setContent(textMessage);
        } else if (message.getContent() instanceof ImageMessage) {
            ImageMessage imageMessage = (ImageMessage) message.getContent();
            imageMessage.setExtra("delete");
            message.setContent(imageMessage);
        } else if (message.getContent() instanceof VoiceMessage) {
            VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
            voiceMessage.setExtra("delete");
            //            String extra = JsonUtils.toJson(bean);
            //            textMessage.setExtra("delete");
            //            voiceMessage.setExtra(extra);
            message.setContent(voiceMessage);
        }

        return message;
    }

    @Override
    public boolean onSent(Message message, RongIM.SentMessageErrorCode sentMessageErrorCode) {
        if (message.getContent() instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message.getContent();
            if (!TextUtils.isEmpty(textMessage.getExtra())) {
                //                ReadBean bean = JsonUtils.objectFromJson(textMessage.getExtra(), ReadBean.class);
                if ("delete".equals(textMessage.getExtra())) {
                    mIds.add(message.getMessageId());
                }
                //                if (bean.getAction().equals("delete")) {
                //                    mIds.add(message.getMessageId());
                //                }
            }
        } else if (message.getContent() instanceof ImageMessage) {
            ImageMessage imageMessage = (ImageMessage) message.getContent();
            //            if ("delete".equals(imageMessage.getExtra())) {
            //                mIds.add(message.getMessageId());
            //            }

            if (!TextUtils.isEmpty(imageMessage.getExtra())) {
                //                ReadBean bean = JsonUtils.objectFromJson(message.getExtra(), ReadBean.class);
                if ("delete".equals(imageMessage.getExtra())) {
                    mIds.add(message.getMessageId());
                }
                //                if (bean.getAction().equals("delete")) {
                //                    mIds.add(message.getMessageId());
                //                }
            }

        } else if (message.getContent() instanceof VoiceMessage) {
            VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
            //            if ("delete".equals(voiceMessage.getExtra())) {
            //                mIds.add(message.getMessageId());
            //            }

            if (!TextUtils.isEmpty(voiceMessage.getExtra())) {
                //                ReadBean bean = JsonUtils.objectFromJson(message.getExtra(), ReadBean.class);
                if ("delete".equals(voiceMessage.getExtra())) {
                    mIds.add(message.getMessageId());
                }
                //                if (bean.getAction().equals("delete")) {
                //                    mIds.add(message.getMessageId());
                //                }
            }
        }
        return false;
    }

    //    public void onEventMainThread(Integer readBean) {
    //        Log.e("ziji", readBean + "   readBean");
    //        if (readBean != 0) {
    //            RongIM.getInstance().deleteMessages(new int[]{readBean}, new RongIMClient.ResultCallback<Boolean>() {
    //                @Override
    //                public void onSuccess(Boolean aBoolean) {
    //                    Log.e("ziji", "aBoolean:" + aBoolean);
    //                }
    //
    //                @Override
    //                public void onError(RongIMClient.ErrorCode errorCode) {
    //                    Log.e("ziji", errorCode.getMessage());
    //                }
    //            });
    //        }
    //
    //    }

    private void hideInputKeyBoard() {
        InputMethodManager imm = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        editText.clearFocus();
    }

    private void showVoiceInputToggle() {
        this.mVoiceInputToggle.setVisibility(View.VISIBLE);
        editText.setVisibility(View.GONE);
        mVoiceInputToggle.setBackgroundResource(R.drawable.baojia_voice_input_bg);
        rc_voice_toggle1.setImageResource(R.drawable.jj2j);
        ExtensionHistoryUtil.setExtensionBarState(this.getContext(), mTargetId, Conversation.ConversationType.PRIVATE, ExtensionHistoryUtil.ExtensionBarState.VOICE);
    }

    private void showInputKeyBoard() {
        editText.setVisibility(View.VISIBLE);
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);
        //        this.mEmoticonToggle.setSelected(false);
        //        this.isKeyBoardActive = true;
    }

    private void hideVoiceInputToggle() {
        rc_voice_toggle1.setImageResource(R.drawable.s2_2);
        this.mVoiceInputToggle.setVisibility(View.GONE);
        mVoiceInputToggle.setBackgroundResource(R.drawable.rc_voice_input_selector);
        ExtensionHistoryUtil.setExtensionBarState(this.getContext(), mTargetId, Conversation.ConversationType.PRIVATE, ExtensionHistoryUtil.ExtensionBarState.NORMAL);
    }

}
