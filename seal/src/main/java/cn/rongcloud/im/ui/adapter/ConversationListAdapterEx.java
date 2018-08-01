package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.LogUtils;
import com.dbcapp.club.R;

import io.rong.common.RLog;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ConversationProviderTag;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.Conversation;

/**
 * Created by weiqinxiao on 15/11/5.
 */
public class ConversationListAdapterEx extends ConversationListAdapter {
    private Context mContext;
    public ConversationListAdapterEx(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected View newView(Context context, int position, ViewGroup group) {
        return super.newView(context, position, group);
    }



    @Override
    protected void bindView(View v, int position, UIConversation data) {


//        if (data != null) {
//            if (data.getConversationType().equals(Conversation.ConversationType.DISCUSSION))
//                data.setUnreadType(UIConversation.UnreadRemindType.REMIND_ONLY);
//            if (data.getIconUrl()!=null) {
//                data.setIconUrl(data.getIconUrl());
//            }
//        }

     LogUtils.w("dyc--", "xxxx"+RongIM.getInstance().getUnreadCount(Conversation.ConversationType.SYSTEM));

            ConversationListAdapter.ViewHolder holder = (ConversationListAdapter.ViewHolder)v.getTag();
            if (data != null) {
                IContainerItemProvider provider = RongContext.getInstance().getConversationTemplate(data.getConversationType().getName());
                if (provider == null) {
                    RLog.e("ConversationListAdapter", "provider is null");
                } else {
                    View view = holder.contentView.inflate(provider);
                    provider.bindView(view, position, data);
                    if (data.isTop()) {
                        holder.layout.setBackgroundDrawable(this.mContext.getResources().getDrawable(io.rong.imkit.R.drawable.rc_item_top_list_selector));
                    } else {
                        holder.layout.setBackgroundDrawable(this.mContext.getResources().getDrawable(io.rong.imkit.R.drawable.rc_item_list_selector));
                    }

                    ConversationProviderTag tag = RongContext.getInstance().getConversationProviderTag(data.getConversationType().getName());
                    int defaultId;
                    if (data.getConversationType().equals(Conversation.ConversationType.GROUP)) {
                        defaultId = io.rong.imkit.R.drawable.rc_default_group_portrait;
                    } else if (data.getConversationType().equals(Conversation.ConversationType.DISCUSSION)) {
                        defaultId = io.rong.imkit.R.drawable.rc_default_discussion_portrait;
                    } else if (data.getConversationType().equals(Conversation.ConversationType.SYSTEM)) {
                        defaultId = R.drawable.system_notice;
                    } else {
                        defaultId = io.rong.imkit.R.drawable.rc_default_portrait;
                    }

                   /* if (tag.portraitPosition() == 1) {
                        holder.leftImageLayout.setVisibility(0);
                        holder.leftImageLayout.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                if (ConversationListAdapter.this.mOnPortraitItemClick != null) {
                                    ConversationListAdapter.this.mOnPortraitItemClick.onPortraitItemClick(v, data);
                                }

                            }
                        });
                        holder.leftImageLayout.setOnLongClickListener(new OnLongClickListener() {
                            public boolean onLongClick(View v) {
                                if (ConversationListAdapter.this.mOnPortraitItemClick != null) {
                                    ConversationListAdapter.this.mOnPortraitItemClick.onPortraitItemLongClick(v, data);
                                }

                                return true;
                            }
                        });*/
                    if (data.getConversationGatherState()) {
                        holder.leftImageView.setAvatar((String) null, defaultId);
                    } else if (data.getIconUrl() != null) {
                        holder.leftImageView.setAvatar(data.getIconUrl().toString(), defaultId);
                    } else {
                        holder.leftImageView.setAvatar((String) null, defaultId);
                    }

                    if (data.getUnReadMessageCount() > 0) {
                        holder.unReadMsgCountIcon.setVisibility(View.VISIBLE);
                        this.setUnReadViewLayoutParams(holder.leftUnReadView, data.getUnReadType());
                        if (data.getUnReadType().equals(UIConversation.UnreadRemindType.REMIND_WITH_COUNTING)) {
                            if (data.getUnReadMessageCount() > 99) {
                                holder.unReadMsgCount.setText(this.mContext.getResources().getString(io.rong.imkit.R.string.rc_message_unread_count));
                            } else {
                                holder.unReadMsgCount.setText(Integer.toString(data.getUnReadMessageCount()));
                            }

                            holder.unReadMsgCount.setVisibility(View.VISIBLE);
                            holder.unReadMsgCountIcon.setImageResource(io.rong.imkit.R.drawable.rc_unread_count_bg);
                        } else {
                            holder.unReadMsgCount.setVisibility(View.GONE);
                            holder.unReadMsgCountIcon.setImageResource(io.rong.imkit.R.drawable.rc_unread_remind_list_count);
                        }
                    } else {
                        holder.unReadMsgCountIcon.setVisibility(View.GONE);
                        holder.unReadMsgCount.setVisibility(View.GONE);
                    }

                    holder.rightImageLayout.setVisibility(View.GONE);
//                    } else if (tag.portraitPosition() == 2) {
//                        holder.rightImageLayout.setVisibility(0);
//                        holder.rightImageLayout.setOnClickListener(new View.OnClickListener() {
//                            public void onClick(View v) {
//                                if (ConversationListAdapter.this.mOnPortraitItemClick != null) {
//                                    ConversationListAdapter.this.mOnPortraitItemClick.onPortraitItemClick(v, data);
//                                }
//
//                            }
//                        });
//                        holder.rightImageLayout.setOnLongClickListener(new OnLongClickListener() {
//                            public boolean onLongClick(View v) {
//                                if (ConversationListAdapter.this.mOnPortraitItemClick != null) {
//                                    ConversationListAdapter.this.mOnPortraitItemClick.onPortraitItemLongClick(v, data);
//                                }
//
//                                return true;
//                            }
//                        });
//                        if (data.getConversationGatherState()) {
//                            holder.rightImageView.setAvatar((String)null, defaultId);
//                        } else if (data.getIconUrl() != null) {
//                            holder.rightImageView.setAvatar(data.getIconUrl().toString(), defaultId);
//                        } else {
//                            holder.rightImageView.setAvatar((String)null, defaultId);
//                        }
//
                    holder.rightImageLayout.setVisibility(View.GONE);

//                      if (data.getUnReadMessageCount() > 0) {
//                            holder.unReadMsgCountRightIcon.setVisibility(View.VISIBLE);
//                            this.setUnReadViewLayoutParams(holder.rightUnReadView, data.getUnReadType());
//                            if (data.getUnReadType().equals(UIConversation.UnreadRemindType.REMIND_WITH_COUNTING)) {
//                                holder.unReadMsgCount.setVisibility(View.VISIBLE);
//                                if (data.getUnReadMessageCount() > 99) {
//                                    holder.unReadMsgCountRight.setText(this.mContext.getResources().getString(io.rong.imkit.R.string.rc_message_unread_count));
//                                } else {
//                                    holder.unReadMsgCountRight.setText(Integer.toString(data.getUnReadMessageCount()));
//                                }
//
//                                holder.unReadMsgCountRightIcon.setImageResource(io.rong.imkit.R.drawable.rc_unread_count_bg);
//                            } else {
//                                holder.unReadMsgCount.setVisibility(View.GONE);
//                                holder.unReadMsgCountRightIcon.setImageResource(io.rong.imkit.R.drawable.rc_unread_remind_without_count);
//                            }
//                        } else {
//                            holder.unReadMsgCountIcon.setVisibility(View.GONE);
//                            holder.unReadMsgCount.setVisibility(View.GONE);
//                        }

//                        holder.leftImageLayout.setVisibility(View.GONE);
//                    } else {

//                        if (tag.portraitPosition() != 3) {
//                            throw new IllegalArgumentException("the portrait position is wrong!");
//                        }
//
//                        holder.rightImageLayout.setVisibility(8);
//                        holder.leftImageLayout.setVisibility(8);*//*
//                    }

//                }

                    if (data.getUnReadMessageCount() > 0) {
                        holder.unReadMsgCountIcon.setVisibility(View.VISIBLE);
                        this.setUnReadViewLayoutParams(holder.leftUnReadView, data.getUnReadType());
                        if (data.getUnReadType().equals(UIConversation.UnreadRemindType.REMIND_WITH_COUNTING)) {
                            if (data.getUnReadMessageCount() > 99) {
                                holder.unReadMsgCount.setText(this.mContext.getResources().getString(io.rong.imkit.R.string.rc_message_unread_count));
                            } else {
                                holder.unReadMsgCount.setText(Integer.toString(data.getUnReadMessageCount()));
                            }

                            holder.unReadMsgCount.setVisibility(View.VISIBLE);
                            holder.unReadMsgCountIcon.setImageResource(io.rong.imkit.R.drawable.rc_unread_count_bg);
                        } else {
                            holder.unReadMsgCount.setVisibility(View.GONE);
                            holder.unReadMsgCountIcon.setImageResource(io.rong.imkit.R.drawable.rc_unread_remind_list_count);
                        }
                    } else {
                        holder.unReadMsgCountIcon.setVisibility(View.GONE);
                        holder.unReadMsgCount.setVisibility(View.GONE);
                    }
                }
            }
//        super.bindView(v, position, data);
    }
}
