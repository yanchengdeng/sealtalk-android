package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dbcapp.club.R;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.server.response.ContactNotificationMessageData;
import cn.rongcloud.im.server.utils.NLog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.PerfectClickListener;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MessageContent;
import io.rong.message.ContactNotificationMessage;
import io.rong.message.GroupNotificationMessage;

//TOdo  系统消息二级列表
public class SubConversationListAdapterDYC  extends ConversationListAdapter {
    LayoutInflater mInflater;
    Context mContext;
    private DoActionLisener doActionLisener;
    private String mSyncName;

    public long getItemId(int position) {
        UIConversation conversation = (UIConversation) this.getItem(position);
        return conversation == null ? 0L : (long) conversation.hashCode();
    }

    public SubConversationListAdapterDYC(Context context) {
        super(context);
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
        this.mSyncName  = context.getSharedPreferences("config", context.MODE_PRIVATE).getString(SealConst.BAOJIA_USER_SYNCNAME, "");;
    }

    protected View newView(Context context, int position, ViewGroup group) {
        View convertView = this.mInflater.inflate(R.layout.adapter_subconversation_view, group, false);
        SubConversationListAdapterDYC.ViewHolder holder = new SubConversationListAdapterDYC.ViewHolder();
        holder.mName = (TextView) convertView.findViewById(R.id.tv_user_name);
        holder.mHead = (SelectableRoundedImageView) convertView.findViewById(R.id.new_header);
        holder.mState = (TextView) convertView.findViewById(R.id.ship_state);
        holder.mRefuseState = convertView.findViewById(R.id.ship_state_refuse);
        holder.mMessage = convertView.findViewById(R.id.tv_request_content);
        holder.llUIView = convertView.findViewById(R.id.ll_ui_view);
        holder.unReadMsgCount = convertView.findViewById(R.id.rc_unread_message);
        holder.unReadMsgCountIcon = convertView.findViewById(R.id.rc_unread_message_icon);
        holder.tvShipTips = convertView.findViewById(R.id.tv_ship_tips);

        convertView.setTag(holder);

        return convertView;
    }

    protected void bindView(View contentView, int position, final UIConversation data) {
        final SubConversationListAdapterDYC.ViewHolder holder = (SubConversationListAdapterDYC.ViewHolder) contentView.getTag();
//        IContainerItemProvider provider = RongContext.getInstance().getConversationTemplate(data.getConversationType().getName());
//        View view = holder.contentView.inflate(provider);
//        provider.bindView(view, position, data);

        MessageContent messageContent = data.getMessageContent();



        if (data.getUnReadMessageCount() > 0) {
            holder.unReadMsgCountIcon.setVisibility(View.VISIBLE);
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


        if (data.getIconUrl()!=null) {
            ImageLoader.getInstance().displayImage(data.getIconUrl().toString(), holder.mHead);
        }

        if (messageContent instanceof ContactNotificationMessage) {
            holder.mName.setText(((ContactNotificationMessage)messageContent).getExtra());
            holder.mMessage.setText(((ContactNotificationMessage)messageContent).getMessage());
            ContactNotificationMessage contactNotificationMessage = (ContactNotificationMessage) messageContent;
            if (contactNotificationMessage.getOperation().equals("acceptRequest")) {
                holder.mState.setVisibility(View.GONE);
                holder.mRefuseState.setVisibility(View.GONE);
                holder.tvShipTips.setVisibility(View.VISIBLE);
                holder.tvShipTips.setText("已接受");
                // 被加方同意请求后
                if (contactNotificationMessage.getExtra() != null) {
                    ContactNotificationMessageData bean = null;
                    try {
                        bean = new ContactNotificationMessageData();
                        bean.setSourceUserNickname(contactNotificationMessage.getExtra());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    final ContactNotificationMessageData finalBean = bean;
                    holder.llUIView.setOnClickListener(new PerfectClickListener() {
                        @Override
                        protected void onNoDoubleClick(View v) {
                            RongIM.getInstance().startPrivateChat(mContext, data.getConversationSenderId(), finalBean.getSourceUserNickname());
                        }
                    });
                }
            }else if(contactNotificationMessage.getOperation().equals("friendRequest")){


                if (doActionLisener!=null){
                    holder.mState.setVisibility(View.VISIBLE);
                    holder.mRefuseState.setVisibility(View.VISIBLE);
                    holder.mRefuseState.setOnClickListener(new PerfectClickListener() {
                        @Override
                        protected void onNoDoubleClick(View v) {
                            doActionLisener.onAcitonLisener(0,data);
                        }
                    });

                    holder.mState.setOnClickListener(new PerfectClickListener() {
                        @Override
                        protected void onNoDoubleClick(View v) {
                            doActionLisener.onAcitonLisener(1,data);
                        }
                    });
                }
                if (SealUserInfoManager.getInstance().isFriendsRelationship(contactNotificationMessage.getSourceUserId())){
                    holder.mState.setVisibility(View.GONE);
                    holder.mRefuseState.setVisibility(View.GONE);
                    holder.tvShipTips.setVisibility(View.VISIBLE);
                    holder.tvShipTips.setText("已接受");
                    holder.llUIView.setOnClickListener(new PerfectClickListener() {
                        @Override
                        protected void onNoDoubleClick(View v) {
                            RongIM.getInstance().startPrivateChat(mContext, data.getConversationSenderId(), data.getExtra());
                        }
                    });
                }else{
                    holder.tvShipTips.setVisibility(View.GONE);
                }
            }else if (contactNotificationMessage.getOperation().equals("relieveRequest")){
                holder.mState.setVisibility(View.GONE);
                holder.mRefuseState.setVisibility(View.GONE);
                holder.tvShipTips.setVisibility(View.VISIBLE);
                holder.tvShipTips.setText("已解除");
            }else {
                holder.tvShipTips.setText("已拒绝");
                holder.mState.setVisibility(View.GONE);
                holder.mRefuseState.setVisibility(View.GONE);
            }
        } else if (messageContent instanceof GroupNotificationMessage) {
            GroupNotificationMessage groupNotificationMessage = (GroupNotificationMessage) messageContent;
            NLog.e("onReceived:" + groupNotificationMessage.getMessage());
            holder.mName.setText("群消息");
            holder.mMessage.setText(((GroupNotificationMessage)messageContent).getMessage());
            holder.mHead.setImageResource(R.drawable.rc_default_group_portrait);
            if (groupNotificationMessage.getOperation().equals("joinRequest")) {
                holder.mState.setVisibility(View.VISIBLE);
                holder.mRefuseState.setVisibility(View.VISIBLE);
//                Intent intent = new Intent(mContext, AllowGroupRequestActivity.class);
//                Bundle bundle= new Bundle();
//                groupNotificationMessage.setOperatorUserId(data.getConversationTargetId());
//                bundle.putParcelable("data",groupNotificationMessage);
//                intent.putExtras(bundle);
//                mContext.startActivity(intent);
                if (doActionLisener!=null){
                    holder.mRefuseState.setOnClickListener(new PerfectClickListener() {
                        @Override
                        protected void onNoDoubleClick(View v) {
                            doActionLisener.onAcitonLisener(0,data);
                        }
                    });

                    holder.mState.setOnClickListener(new PerfectClickListener() {
                        @Override
                        protected void onNoDoubleClick(View v) {
                            doActionLisener.onAcitonLisener(1,data);
                        }
                    });
                }

            } else if (groupNotificationMessage.getOperation().equals("invitationRequest")) {
                holder.mState.setVisibility(View.VISIBLE);
                holder.mRefuseState.setVisibility(View.VISIBLE);
//                Intent intent = new Intent(mContext, AllowGroupRequestActivity.class);
//                Bundle bundle= new Bundle();
//                groupNotificationMessage.setOperatorUserId(data.getConversationTargetId());
//                bundle.putParcelable("data",groupNotificationMessage);
//                intent.putExtras(bundle);
//                mContext.startActivity(intent);

               /* SealUserInfoManager.getInstance().getGroupMembers(groupNotificationMessage.getExtra(), new SealUserInfoManager.ResultCallback<List<GroupMember>>() {
                    @Override
                    public void onSuccess(List<GroupMember> groupMembers) {
                        LogUtils.w("dyc",groupMembers);
                        boolean isExist = false;
                        if (groupMembers!=null && groupMembers.size()>0){
                            for (GroupMember groupItme:groupMembers){
                                if (mSyncName.equals(groupItme.getName())){
                                    isExist = true;
                                }
                            }
                        }

                        if (isExist){
                            holder.mState.setVisibility(View.GONE);
                            holder.mRefuseState.setVisibility(View.GONE);
                            holder.tvShipTips.setVisibility(View.VISIBLE);
                            holder.tvShipTips.setText("已同意");
                        }else{
                            holder.mState.setVisibility(View.VISIBLE);
                            holder.mRefuseState.setVisibility(View.VISIBLE);
                            holder.tvShipTips.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(String errString) {
                        LogUtils.w("dyc",errString);

                    }
                });*/
                if (doActionLisener!=null){
                    holder.mRefuseState.setOnClickListener(new PerfectClickListener() {
                        @Override
                        protected void onNoDoubleClick(View v) {
                            doActionLisener.onAcitonLisener(0,data);
                        }
                    });

                    holder.mState.setOnClickListener(new PerfectClickListener() {
                        @Override
                        protected void onNoDoubleClick(View v) {
                            doActionLisener.onAcitonLisener(1,data);
                        }
                    });
                }
            }else{
                holder.mState.setVisibility(View.GONE);
                holder.mRefuseState.setVisibility(View.GONE);
            }
        }else if (data.getConversationType().equals(Conversation.ConversationType.DISCUSSION)){
            data.setUnreadType(UIConversation.UnreadRemindType.REMIND_ONLY);
            holder.mState.setVisibility(View.GONE);
            holder.mRefuseState.setVisibility(View.GONE);
            holder.mName.setText("我的讨论组");
            holder.mHead.setImageResource(R.drawable.rc_default_discussion_portrait);
            holder.mMessage.setText(data.getUIConversationTitle());
            holder.llUIView.setOnClickListener(new PerfectClickListener() {
                @Override
                protected void onNoDoubleClick(View v) {
                    RongIM.getInstance().startDiscussionChat(mContext,data.getConversationTargetId(),data.getUIConversationTitle());
                }
            });
        }else{
            holder.mState.setVisibility(View.GONE);
            holder.mRefuseState.setVisibility(View.GONE);

        }
    }


    @Override
    public void setOnPortraitItemClick(OnPortraitItemClick onPortraitItemClick) {
        super.setOnPortraitItemClick(null);
    }

    public void setActionLisener(DoActionLisener  doActionLisener){
        this.doActionLisener = doActionLisener;
    }

    class ViewHolder {
        View  llUIView;
        SelectableRoundedImageView mHead;
        TextView mName;
        TextView mState, mRefuseState;
        //        TextView mtime;
        TextView mMessage;
        public TextView unReadMsgCount;
        public ImageView unReadMsgCountIcon;
        TextView tvShipTips;

    }

    public interface  DoActionLisener{
        //action  0   左边拒绝    1 右边 接受
        void onAcitonLisener(int action,UIConversation data);
    }
}