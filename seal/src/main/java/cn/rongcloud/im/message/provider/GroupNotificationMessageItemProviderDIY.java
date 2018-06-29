//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.rongcloud.im.message.provider;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dbcapp.club.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import io.rong.imkit.R.id;
import io.rong.imkit.R.layout;
import io.rong.imkit.R.string;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.GroupNotificationMessageData;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.model.UserInfo;
import io.rong.message.GroupNotificationMessage;

@ProviderTag(
        messageContent = GroupNotificationMessage.class,
        showPortrait = false,
        centerInHorizontal = true,
        showProgress = false,
        showSummaryWithName = false
)
public class GroupNotificationMessageItemProviderDIY extends MessageProvider<GroupNotificationMessage> {
    public GroupNotificationMessageItemProviderDIY() {
    }

    public void bindView(View view, int i, GroupNotificationMessage groupNotificationMessage, UIMessage uiMessage) {
        GroupNotificationMessageItemProviderDIY.ViewHolder viewHolder = (GroupNotificationMessageItemProviderDIY.ViewHolder) view.getTag();

        try {
            if (groupNotificationMessage != null && uiMessage != null) {
                if (groupNotificationMessage != null && groupNotificationMessage.getData() == null) {
                    return;
                }

                GroupNotificationMessageData data;
                try {
                    data = this.jsonToBean(groupNotificationMessage.getData());
                } catch (Exception var21) {
                    var21.printStackTrace();
                    return;
                }

                String operation = groupNotificationMessage.getOperation();
                String operatorNickname = data.getOperatorNickname();
                String operatorUserId = groupNotificationMessage.getOperatorUserId();
                String currentUserId = RongIM.getInstance().getCurrentUserId();
                if (operatorNickname == null) {
                    UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(operatorUserId);
                    if (userInfo != null) {
                        operatorNickname = userInfo.getName();
                        if (operatorNickname == null) {
                            operatorNickname = groupNotificationMessage.getOperatorUserId();
                        }
                    }
                }

                List<String> memberList = data.getTargetUserDisplayNames();
                List<String> memberIdList = data.getTargetUserIds();
                String memberName = null;
                String memberUserId = null;
                Context context = RongContext.getInstance();
                if (memberIdList != null && memberIdList.size() == 1) {
                    memberUserId = (String) memberIdList.get(0);
                }

                String groupName;
                if (memberList != null) {
                    if (memberList.size() == 1) {
                        memberName = (String) memberList.get(0);
                    } else if (memberIdList.size() > 1) {
                        StringBuilder sb = new StringBuilder();
                        Iterator var17 = memberList.iterator();

                        while (var17.hasNext()) {
                            String s = (String) var17.next();
                            sb.append(s);
                            sb.append(context.getString(string.rc_item_divided_string));
                        }

                        groupName = sb.toString();
                        memberName = groupName.substring(0, groupName.length() - 1);
                    }
                }

                if (!TextUtils.isEmpty(operation)) {
                    String operator;
                    if (operation.equals("invitationRequest")) {
                        if (operatorUserId.equals(memberUserId)) {
                            viewHolder.contentTextView.setText(memberName + context.getString(string.rc_item_join_group));
                        } else {
                            if (!groupNotificationMessage.getOperatorUserId().equals(RongIM.getInstance().getCurrentUserId())) {
                                operator = operatorNickname;
                                groupName = memberName;
                            } else {
                                operator = context.getString(string.rc_item_you);
                                groupName = memberName;
                            }
                            viewHolder.contentTextView.setText(context.getString(string.rc_item_invitation, new Object[]{operator, groupName}));
                        }
                        viewHolder.contentTextView.setText(context.getString(R.string.your_friends_invite_you));
                    } else if (operation.equals("kickOutGrpRequest")) {
                        if (memberIdList != null) {
                            Iterator var26 = memberIdList.iterator();

                            while (var26.hasNext()) {
                                String userId = (String) var26.next();
                                if (currentUserId.equals(userId)) {
                                    groupName = context.getString(string.rc_item_you);
                                    viewHolder.contentTextView.setText(context.getString(string.rc_item_remove_self, new Object[]{groupName, operatorNickname}));
                                } else {
                                    if (!operatorUserId.equals(currentUserId)) {
                                        operator = operatorNickname;
                                        groupName = memberName;
                                    } else {
                                        operator = context.getString(string.rc_item_you);
                                        groupName = memberName;
                                    }

                                    viewHolder.contentTextView.setText(context.getString(string.rc_item_remove_group_member, new Object[]{operator, groupName}));
                                }
                                viewHolder.contentTextView.setText(R.string.tickout_from_group);
                            }
                        }
                    } else if (operation.equals("Create")) {
                        new GroupNotificationMessageData();

                        try {
                            this.jsonToBean(groupNotificationMessage.getData());
                        } catch (Exception var20) {
                            var20.printStackTrace();
                            return;
                        }

                        if (!operatorUserId.equals(currentUserId)) {
                            groupName = operatorNickname;
                        } else {
                            groupName = context.getString(string.rc_item_you);
                        }

                        viewHolder.contentTextView.setText(context.getString(string.rc_item_created_group, new Object[]{groupName}));
                    } else if (operation.equals("dismissGrpRequest")) {
                        viewHolder.contentTextView.setText(operatorNickname + context.getString(string.rc_item_dismiss_groups));
                    } else if (operation.equals("quitGrpRequest")) {
                        viewHolder.contentTextView.setText(operatorNickname + context.getString(string.rc_item_quit_groups));
                    } else if (operation.equals("Rename")) {
                        if (!operatorUserId.equals(currentUserId)) {
                            operator = operatorNickname;
                            groupName = data.getTargetGroupName();
                        } else {
                            operator = context.getString(string.rc_item_you);
                            groupName = data.getTargetGroupName();
                        }

                        viewHolder.contentTextView.setText(context.getString(string.rc_item_change_group_name, new Object[]{operator, groupName}));
                    } else if (operation.equals("joinRequest")) {
                        //申请加入群组
                        viewHolder.contentTextView.setText(operatorNickname + context.getString(string.rc_item_join_group));
                    } else if (operation.equals("rejectJoinGrpRequest")) {
                        viewHolder.contentTextView.setText(context.getString(R.string.mangage_not_agree));
                    } else if (operation.equals("acceptJoinGrpRequest")) {
                        viewHolder.contentTextView.setText(context.getString(R.string.mangage_agree_you_join));
                    }else if (operation.equals("rejectInvitationGrpRequest")) {
                        viewHolder.contentTextView.setText(context.getString(R.string.friends_refuse_your_request));
                    } else if (operation.equals("acceptInvitationGrpRequest")) {
                        viewHolder.contentTextView.setText(context.getString(R.string.friends_agree_your_request));
                    }
                }
            }
        } catch (Exception var22) {
            var22.printStackTrace();
        }

    }

    public Spannable getContentSummary(GroupNotificationMessage groupNotificationMessage) {
        return null;
    }

    public Spannable getContentSummary(Context context, GroupNotificationMessage groupNotificationMessage) {
        try {
            if (groupNotificationMessage != null && groupNotificationMessage.getData() == null) {
                return null;
            } else {
                GroupNotificationMessageData data;
                try {
                    data = this.jsonToBean(groupNotificationMessage.getData());
                } catch (Exception var18) {
                    var18.printStackTrace();
                    return null;
                }

                String operation = groupNotificationMessage.getOperation();
                String operatorNickname = data.getOperatorNickname();
                String operatorUserId = groupNotificationMessage.getOperatorUserId();
                String currentUserId = RongIM.getInstance().getCurrentUserId();
                if (operatorNickname == null) {
                    UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(operatorUserId);
                    if (userInfo != null) {
                        operatorNickname = userInfo.getName();
                    }

                    if (operatorNickname == null) {
                        operatorNickname = groupNotificationMessage.getOperatorUserId();
                    }
                }

                List<String> memberList = data.getTargetUserDisplayNames();
                List<String> memberIdList = data.getTargetUserIds();
                String memberName = null;
                String memberUserId = null;
                if (memberIdList != null && memberIdList.size() == 1) {
                    memberUserId = (String) memberIdList.get(0);
                }

                String groupName;
                String operator;
                if (memberList != null) {
                    if (memberList.size() == 1) {
                        memberName = (String) memberList.get(0);
                    } else if (memberIdList.size() > 1) {
                        StringBuilder sb = new StringBuilder();
                        Iterator var13 = memberList.iterator();

                        while (var13.hasNext()) {
                            groupName = (String) var13.next();
                            sb.append(groupName);
                            sb.append(context.getString(string.rc_item_divided_string));
                        }

                        operator = sb.toString();
                        memberName = operator.substring(0, operator.length() - 1);
                    }
                }

                SpannableString spannableStringSummary = new SpannableString("");
                if (operation.equals("invitationRequest")) {
                    try {
                        if (operatorUserId.equals(memberUserId)) {
                            spannableStringSummary = new SpannableString(operatorNickname + context.getString(string.rc_item_join_group));
                        } else {
                            if (!operatorUserId.equals(currentUserId)) {
                                operator = operatorNickname;
                                groupName = memberName;
                            } else {
                                operator = context.getString(string.rc_item_you);
                                groupName = memberName;
                            }
                            spannableStringSummary = new SpannableString(context.getString(R.string.your_friends_invite_you));
//                            spannableStringSummary = new SpannableString(context.getString(string.rc_item_invitation, new Object[]{operator, groupName}));
                        }
                    } catch (Exception var17) {
                        var17.printStackTrace();
                    }
                } else if (operation.equals("kickOutGrpRequest")) {
                    if (memberIdList != null) {
                        Iterator var15 = memberIdList.iterator();

                        while (var15.hasNext()) {
                            String userId = (String) var15.next();
                            if (currentUserId.equals(userId)) {
                                groupName = context.getString(string.rc_item_you);
                                spannableStringSummary = new SpannableString(context.getString(string.rc_item_remove_self, new Object[]{groupName, operatorNickname}));
                            } else {
                                if (!operatorUserId.equals(currentUserId)) {
                                    operator = operatorNickname;
                                    groupName = memberName;
                                } else {
                                    operator = context.getString(string.rc_item_you);
                                    groupName = memberName;
                                }

                                spannableStringSummary = new SpannableString(context.getString(string.rc_item_remove_group_member, new Object[]{operator, groupName}));
                            }
                            spannableStringSummary = new SpannableString(context.getString(R.string.tickout_from_group));

                        }
                    }
                } else if (operation.equals("quitGrpRequest")) {
                    if (!operatorUserId.equals(currentUserId)) {
                        operator = operatorNickname;
                    } else {
                        operator = context.getString(string.rc_item_you);
                    }

//                    spannableStringSummary = new SpannableString(context.getString(string.rc_item_created_group, new Object[]{operator}));
                    spannableStringSummary = new SpannableString(context.getString(R.string.members_quit_group));
                } else if (operation.equals("dismissGrpRequest")) {
                    spannableStringSummary = new SpannableString(operatorNickname + context.getString(string.rc_item_dismiss_groups));
                } else if (operation.equals("quitGrpRequest")) {
                    spannableStringSummary = new SpannableString(operatorNickname + context.getString(string.rc_item_quit_groups));
                } else if (operation.equals("joinRequest")) {
                    //申请加入群组
                    spannableStringSummary = new SpannableString(operatorNickname + context.getString(R.string.friend_request_join_group));
                } else if (operation.equals("rejectJoinGrpRequest")) {
                    spannableStringSummary = new SpannableString(context.getString(R.string.mangage_not_agree));
                } else if (operation.equals("acceptJoinGrpRequest")) {
                    spannableStringSummary = new SpannableString(context.getString(R.string.mangage_agree_you_join));

                }else if (operation.equals("rejectInvitationGrpRequest")) {
                    spannableStringSummary = new SpannableString(context.getString(R.string.friends_refuse_your_request));
                } else if (operation.equals("acceptInvitationGrpRequest")) {
                    spannableStringSummary = new SpannableString(context.getString(R.string.friends_agree_your_request));

                } else if (operation.equals("Rename")) {
                    if (!operatorUserId.equals(currentUserId)) {
                        operator = operatorNickname;
                        groupName = data.getTargetGroupName();
                    } else {
                        operator = context.getString(string.rc_item_you);
                        groupName = data.getTargetGroupName();
                    }

                    spannableStringSummary = new SpannableString(context.getString(string.rc_item_change_group_name, new Object[]{operator, groupName}));
                }

                return spannableStringSummary;
            }
        } catch (Exception var19) {
            var19.printStackTrace();
            return new SpannableString(context.getString(string.rc_item_group_notification_summary));
        }
    }

    public void onItemClick(View view, int i, GroupNotificationMessage groupNotificationMessage, UIMessage uiMessage) {
    }

    public void onItemLongClick(View view, int i, GroupNotificationMessage groupNotificationMessage, UIMessage uiMessage) {
    }

    public View newView(Context context, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(layout.rc_item_group_information_notification_message, (ViewGroup) null);
        GroupNotificationMessageItemProviderDIY.ViewHolder viewHolder = new GroupNotificationMessageItemProviderDIY.ViewHolder();
        viewHolder.contentTextView = (TextView) view.findViewById(id.rc_msg);
        viewHolder.contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
        view.setTag(viewHolder);
        return view;
    }

    private GroupNotificationMessageData jsonToBean(String data) {
        GroupNotificationMessageData dataEntity = new GroupNotificationMessageData();

        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has("operatorNickname")) {
                dataEntity.setOperatorNickname(jsonObject.getString("operatorNickname"));
            }

            if (jsonObject.has("targetGroupName")) {
                dataEntity.setTargetGroupName(jsonObject.getString("targetGroupName"));
            }

            if (jsonObject.has("timestamp")) {
                dataEntity.setTimestamp(jsonObject.getLong("timestamp"));
            }

            JSONArray jsonArray;
            int i;
            if (jsonObject.has("targetUserIds")) {
                jsonArray = jsonObject.getJSONArray("targetUserIds");

                for (i = 0; i < jsonArray.length(); ++i) {
                    dataEntity.getTargetUserIds().add(jsonArray.getString(i));
                }
            }

            if (jsonObject.has("targetUserDisplayNames")) {
                jsonArray = jsonObject.getJSONArray("targetUserDisplayNames");

                for (i = 0; i < jsonArray.length(); ++i) {
                    dataEntity.getTargetUserDisplayNames().add(jsonArray.getString(i));
                }
            }

            if (jsonObject.has("oldCreatorId")) {
                dataEntity.setOldCreatorId(jsonObject.getString("oldCreatorId"));
            }

            if (jsonObject.has("oldCreatorName")) {
                dataEntity.setOldCreatorName(jsonObject.getString("oldCreatorName"));
            }

            if (jsonObject.has("newCreatorId")) {
                dataEntity.setNewCreatorId(jsonObject.getString("newCreatorId"));
            }

            if (jsonObject.has("newCreatorName")) {
                dataEntity.setNewCreatorName(jsonObject.getString("newCreatorName"));
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return dataEntity;
    }

    private static class ViewHolder {
        TextView contentTextView;

        private ViewHolder() {
        }
    }
}
