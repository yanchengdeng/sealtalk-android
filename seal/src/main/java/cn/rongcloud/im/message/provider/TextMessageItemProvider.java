package cn.rongcloud.im.message.provider;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.ClipboardManager;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dbcapp.club.R;

import cn.rongcloud.im.model.ReadBean;
import cn.rongcloud.im.ui.activity.SelectFriendsActivity;
import cn.rongcloud.im.utils.JsonUtils;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.utilities.OptionsPopupDialog;
import io.rong.imkit.widget.AutoLinkTextView;
import io.rong.imkit.widget.ILinkClickListener;
import io.rong.imkit.widget.LinkTextViewMovementMethod;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

/**
 * Created by lzs on 2018/6/3.
 */


@ProviderTag(
        messageContent = TextMessage.class,
        showReadState = true
)

public class TextMessageItemProvider extends IContainerItemProvider.MessageProvider<TextMessage> {
    private static final String TAG = "TextMessageItemProvider";
    private Context mCtx;

    public TextMessageItemProvider() {
    }

    public View newView(Context context, ViewGroup group) {
        mCtx = context;
        View view = LayoutInflater.from(context).inflate(cn.rongcloud.contactcard.R.layout.rc_item_text_message, null);
        TextMessageItemProvider.ViewHolder holder = new TextMessageItemProvider.ViewHolder();
        holder.message = (AutoLinkTextView) view.findViewById(android.R.id.text1);
        holder.delete_img = (ImageView) view.findViewById(R.id.delete_im);
        holder.timeLayout = (LinearLayout) view.findViewById(R.id.time_layout);
        holder.time = (TextView) view.findViewById(R.id.time);
        holder.mClickk_img = (ImageView) view.findViewById(R.id.click_im);
        view.setTag(holder);
        return view;
    }

    public Spannable getContentSummary(TextMessage data) {
        return null;
    }

    public Spannable getContentSummary(Context context, TextMessage data) {
        if (data == null) {
            return null;
        } else {
            String content = data.getContent();
            if (content != null) {
                if (content.length() > 100) {
                    content = content.substring(0, 100);
                }

                return new SpannableString(AndroidEmoji.ensure(content));
            } else {
                return null;
            }
        }
    }

    @Override
    public void onItemClick(View view, int position, final TextMessage content, final UIMessage message) {
        TextMessageItemProvider.ViewHolder holder = (TextMessageItemProvider.ViewHolder) view.getTag();
        if (message.getMessageDirection() == Message.MessageDirection.RECEIVE) {

        }


    }

    @Override
    public void onItemLongClick(final View view, final int position, final TextMessage content, final UIMessage message) {

        TextMessageItemProvider.ViewHolder holder = (TextMessageItemProvider.ViewHolder) view.getTag();
        holder.longClick = true;
        if (view instanceof TextView) {
            CharSequence text = ((TextView) view).getText();
            if (text != null && text instanceof Spannable)
                Selection.removeSelection((Spannable) text);
        }

        String[] items;

        long deltaTime = RongIM.getInstance().getDeltaTime();
        long normalTime = System.currentTimeMillis() - deltaTime;
        boolean enableMessageRecall = false;
        int messageRecallInterval = -1;
        boolean hasSent = (!message.getSentStatus().equals(Message.SentStatus.SENDING)) && (!message.getSentStatus().equals(Message.SentStatus.FAILED));

        try {
            enableMessageRecall = RongContext.getInstance().getResources().getBoolean(io.rong.imkit.R.bool.rc_enable_message_recall);
            messageRecallInterval = RongContext.getInstance().getResources().getInteger(io.rong.imkit.R.integer.rc_message_recall_interval);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        if (message.getContent() instanceof TextMessage) {
            if (hasSent
                    && enableMessageRecall
                    && (normalTime - message.getSentTime()) <= messageRecallInterval * 1000
                    && message.getSenderUserId().equals(RongIM.getInstance().getCurrentUserId())
                    && !message.getConversationType().equals(Conversation.ConversationType.CUSTOMER_SERVICE)
                    && !message.getConversationType().equals(Conversation.ConversationType.APP_PUBLIC_SERVICE)
                    && !message.getConversationType().equals(Conversation.ConversationType.PUBLIC_SERVICE)
                    && !message.getConversationType().equals(Conversation.ConversationType.SYSTEM)
                    && !message.getConversationType().equals(Conversation.ConversationType.CHATROOM)) {
                //                items = new String[]{(R.string.baojia_delete), context.getString(R.string.baojia_copy), context.getString(R.string.baojia_relay), context.getString(R.string.baojia_recall)};
                items = new String[]{mCtx.getString(R.string.baojia_delete), mCtx.getString(R.string.baojia_copy), mCtx.getString(R.string.baojia_relay), mCtx.getString(R.string.baojia_recall)};

            } else {
                items = new String[]{mCtx.getString(R.string.baojia_delete), mCtx.getString(R.string.baojia_copy), mCtx.getString(R.string.baojia_relay)};
            }

            OptionsPopupDialog.newInstance(view.getContext(), items).setOptionsPopupDialogListener(new OptionsPopupDialog.OnOptionsItemClickedListener() {
                @Override
                public void onOptionsItemClicked(int which) {
                    if (which == 0) {
                        RongIM.getInstance().deleteMessages(new int[]{message.getMessageId()}, null);
                    } else if (which == 1) {
                        @SuppressWarnings("deprecation")
                        ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(((TextMessage) message.getContent()).getContent());
                    } else if (which == 2) {
                        //转发
                        relay(message.getMessage());
                    } else if (which == 3) {

                        RongIM.getInstance().recallMessage(message.getMessage(), "撤回了一条消息");
                    }
                }
            }).show();
        }
    }

    private void relay(Message message) {

        //        Intent intent = new Intent(mContext, ContactListActivity.class);
        Intent intent = new Intent(mCtx, SelectFriendsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("relay", true);
        intent.putExtra("relay_message", message);
        mCtx.startActivity(intent);
        //        RongIM.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
        //            @Override
        //            public void onSuccess(List<Conversation> conversations) {
        //
        //            }
        //
        //            @Override
        //            public void onError(RongIMClient.ErrorCode errorCode) {
        //
        //            }
        //        });
    }


    ReadBean bean;

    public void bindView(final View v, final int position, final TextMessage content, final UIMessage data) {
        final TextMessageItemProvider.ViewHolder holder = (TextMessageItemProvider.ViewHolder) v.getTag();

        if (data.getMessageDirection() == Message.MessageDirection.SEND) {
            holder.mClickk_img.setVisibility(View.GONE);
            holder.timeLayout.setVisibility(View.GONE);
            holder.message.setBackgroundResource(cn.rongcloud.contactcard.R.drawable.rc_ic_bubble_right);

            if (!TextUtils.isEmpty(content.getExtra())) {

                //                bean = JsonUtils.objectFromJson(content.getExtra(), ReadBean.class);

                if ("delete".equals(content.getExtra())) {
                    //                    if (data.getReceivedStatus().isRead()) {
                    //                        holder.delete_img.setVisibility(View.GONE);
                    //                    } else {
                    holder.delete_img.setVisibility(View.VISIBLE);
                    //                    }
                }
            } else {
                holder.delete_img.setVisibility(View.GONE);
            }


            final AutoLinkTextView textView = holder.message;
            if (data.getTextMessageContent() != null) {
                int len = data.getTextMessageContent().length();
                if (v.getHandler() != null && len > 500) {
                    v.getHandler().postDelayed(new Runnable() {
                        public void run() {
                            textView.setText(data.getTextMessageContent());
                        }
                    }, 50L);
                } else {
                    textView.setText(data.getTextMessageContent());
                }
            }
        } else {
            holder.delete_img.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(content.getExtra())) {
                holder.message.setClickable(true);
                holder.message.setEnabled(true);
                //                if (bean != null && bean.getSecond() != SealAppContext.getInstance().getmTimer()) {
                //                } else {
                //                bean = JsonUtils.objectFromJson(content.getExtra(), ReadBean.class);
                holder.mClickk_img.setVisibility(View.VISIBLE);
                holder.timeLayout.setVisibility(View.GONE);


                final AutoLinkTextView textView = holder.message;
                if (data.getTextMessageContent() != null) {
                    int len = data.getTextMessageContent().length();
                    if (v.getHandler() != null && len > 500) {
                        v.getHandler().postDelayed(new Runnable() {
                            public void run() {
                                textView.setText(data.getTextMessageContent());
                            }
                        }, 50L);
                    } else {
                        textView.setText(data.getTextMessageContent());
                    }
                }

                //                holder.message.setText("点击查看消息");
                //                }

                //                holder.message.setOnClickListener(new View.OnClickListener() {
                //                    @Override
                //                    public void onClick(View view) {
                //                        holder.message.setClickable(false);
                //                        holder.message.setEnabled(false);
                //                        holder.mClickk_img.setVisibility(View.GONE);
                //                        holder.timeLayout.setVisibility(View.VISIBLE);
                //                        final AutoLinkTextView textView = holder.message;
                //                        if (data.getTextMessageContent() != null) {
                //                            int len = data.getTextMessageContent().length();
                //                            if (v.getHandler() != null && len > 500) {
                //                                v.getHandler().postDelayed(new Runnable() {
                //                                    public void run() {
                //                                        textView.setText(data.getTextMessageContent());
                //                                    }
                //                                }, 50L);
                //                            } else {
                //                                textView.setText(data.getTextMessageContent());
                //                            }
                //                        }
                //
                //
                //                        CountDownTimer timer = new CountDownTimer(bean.getSecond() * 1000, 1000) {
                //                            int num = bean.getSecond();
                //
                //                            @Override
                //                            public void onTick(long millisUntilFinished) {
                //                                holder.time.setText(String.valueOf((num) / 1000));
                //                                bean.setSecond((int) millisUntilFinished / 1000);
                //                            }
                //
                //                            @Override
                //                            public void onFinish() {
                //                                bean.setSecond(0);
                //                                //                                JSONObject jsonObject = new JSONObject();
                //                                //                                try {
                //                                //                                    jsonObject.put("id", data.getMessageId());
                //                                //                                    jsonObject.put("targetId", data.getTargetId());
                //                                //                                } catch (org.json.JSONException e) {
                //                                //                                    e.printStackTrace();
                //                                //                                }
                //                                //                                BroadcastManager.getInstance(mCtx).sendBroadcast(SealConst.BAOJIA_DELETE_AFTER_READ, jsonObject.toString());
                //                                RongIM.getInstance().deleteMessages(new int[]{data.getMessageId()}, null);
                //                            }
                //                        };
                //                        timer.start();
                //                        Log.e("ziji", data.getSentTime() + "");
                //                        Log.e("ziji", data.getMessageId() + "");
                //
                //                        //                        EventBus.getDefault().post(data.getMessageId());
                //                        RongIMClient.getInstance().sendReadReceiptMessage(Conversation.ConversationType.PRIVATE, data.getTargetId(), data.getSentTime());
                //                        //                        TextMessage textMessage = TextMessage.obtain("查看消息");
                //                        //                        ReadBean bean = new ReadBean();
                //                        //                        bean.setType(1002);
                //                        //                        bean.setMessageId(data.getMessageId());
                //                        //                        String extra = JsonUtils.toJson(bean);
                //                        //                        textMessage.setExtra(extra);
                //                        //                        //                        message.setContent(textMessage);
                //                        //                        Message MyMessage = Message.obtain(data.getTargetId(), Conversation.ConversationType.PRIVATE, textMessage);
                //                        //                        EventBus.getDefault().post(MyMessage);
                //                        //
                //                        //                        RongIM.getInstance().sendMessage(MyMessage, null, null, new IRongCallback.ISendMediaMessageCallback() {
                //                        //                            @Override
                //                        //                            public void onProgress(Message message, int i) {
                //                        //
                //                        //                            }
                //                        //
                //                        //                            @Override
                //                        //                            public void onCanceled(Message message) {
                //                        //
                //                        //                            }
                //                        //
                //                        //                            @Override
                //                        //                            public void onAttached(Message message) {
                //                        //
                //                        //                            }
                //                        //
                //                        //                            @Override
                //                        //                            public void onSuccess(Message message) {
                //                        //                                int id = message.getMessageId();
                //                        //                                RongIM.getInstance().deleteMessages(new int[]{id}, null);
                //                        //                            }
                //                        //
                //                        //                            @Override
                //                        //                            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                //                        //                            }
                //                        //                        });
                //
                //
                //                        //                        RongIMClient.getInstance().sendReadReceiptMessage(data.getConversationType(), data.getMessageId() + "", data.getSentTime());
                //                        //                        Message message = new Message();
                //                        //                        ReadBean bean = new ReadBean();
                //                        //                        bean.setType(1002);
                //                        //                        bean.setMessageId(2003);
                //                        //                        String extra = JsonUtils.toJson(bean);
                //                        //                        //            textMessage.setExtra("delete");
                //                        //                        TextMessage textMessage = (TextMessage) message.getContent();
                //                        //                        textMessage.setContent("查看消息");
                //                        //                        textMessage.setExtra(extra);
                //                        //                        message.setContent(textMessage);
                //                        //                        RongIM.getInstance().sendMessage(message, "", "", new IRongCallback.ISendMediaMessageCallback() {
                //                        //                            @Override
                //                        //                            public void onProgress(Message message, int i) {
                //                        //
                //                        //                            }
                //                        //
                //                        //                            @Override
                //                        //                            public void onCanceled(Message message) {
                //                        //
                //                        //                            }
                //                        //
                //                        //                            @Override
                //                        //                            public void onAttached(Message message) {
                //                        //
                //                        //                            }
                //                        //
                //                        //                            @Override
                //                        //                            public void onSuccess(Message message) {
                //                        //
                //                        //                            }
                //                        //
                //                        //                            @Override
                //                        //                            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                //                        //
                //                        //                            }
                //                        //                        });
                //
                //                        //
                //                    }
                //                });
            } else {
                holder.mClickk_img.setVisibility(View.GONE);
                holder.timeLayout.setVisibility(View.GONE);
                final AutoLinkTextView textView = holder.message;
                if (data.getTextMessageContent() != null) {
                    int len = data.getTextMessageContent().length();
                    if (v.getHandler() != null && len > 500) {
                        v.getHandler().postDelayed(new Runnable() {
                            public void run() {
                                textView.setText(data.getTextMessageContent());
                            }
                        }, 50L);
                    } else {
                        textView.setText(data.getTextMessageContent());
                    }
                }


            }
            holder.message.setBackgroundResource(cn.rongcloud.contactcard.R.drawable.rc_ic_bubble_left);
        }

        holder.message.setLongClickable(true);

        holder.message.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onItemLongClick(v, position, content, data);
                return false;
            }
        });

        holder.message.setMovementMethod(new LinkTextViewMovementMethod(new ILinkClickListener() {
            public boolean onLinkClick(String link) {
                RongIM.ConversationBehaviorListener listener = RongContext.getInstance().getConversationBehaviorListener();
                RongIM.ConversationClickListener clickListener = RongContext.getInstance().getConversationClickListener();
                boolean result = false;
                if (listener != null) {
                    result = listener.onMessageLinkClick(v.getContext(), link);
                } else if (clickListener != null) {
                    result = clickListener.onMessageLinkClick(v.getContext(), link, data.getMessage());
                }

                if (listener == null && clickListener == null || !result) {
                    String str = link.toLowerCase();
                    if (str.startsWith("http") || str.startsWith("https")) {
                        Intent intent = new Intent("io.rong.imkit.intent.action.webview");
                        intent.setPackage(v.getContext().getPackageName());
                        intent.putExtra("url", link);
                        v.getContext().startActivity(intent);
                        result = true;
                    }
                }

                return result;
            }
        }));
    }

    private static class ViewHolder {
        AutoLinkTextView message;
        ImageView delete_img;
        LinearLayout timeLayout;
        TextView time;
        ImageView mClickk_img;
        boolean longClick;

        private ViewHolder() {
        }
    }

    private Handler mHandler = new Handler();
    private Runnable mRefreshCheck = new Runnable() {
        int count;


        @Override
        public void run() {
            if (count > 0) {
                count--;
                mHandler.postDelayed(this, 1000);
            } else {
                count = 0;
                //                btnGetCheckNum.setOnClickListener(ForgetPasswordActivity.this);
            }
        }
    };


}
