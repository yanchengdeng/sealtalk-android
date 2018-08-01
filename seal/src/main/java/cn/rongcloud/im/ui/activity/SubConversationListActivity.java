package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.dbcapp.club.R;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.response.AgreeFriendResponse;
import cn.rongcloud.im.server.response.FriendInvitationResponse;
import cn.rongcloud.im.server.response.FriendResponse;
import cn.rongcloud.im.server.response.GetGroupMemberResponse;
import cn.rongcloud.im.server.response.GroupDetailBaoResponse;
import cn.rongcloud.im.server.response.GroupNumbersBaoResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.adapter.SubConversationListAdapterDYC;
import cn.rongcloud.im.ui.fragment.SubConversationListFragmentNew;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.UIConversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.MessageContent;
import io.rong.message.ContactNotificationMessage;
import io.rong.message.GroupNotificationMessage;

/**
 * Created by Bob on 15/11/3.
 * 聚合会话列表
 */
public class SubConversationListActivity extends BaseActivity {

    SubConversationListAdapterDYC adapterDYC;

    private static final int AGREE_FRIENDS = 12;
    private static final int REUFSE_FRIENDS = 13;


    private static final int REFUSE_JOIN_GROUP = 20;
    private static final int ACCEPT_JOIN_GROUP = 21;


    //获取好友信息
    private static final int SYN_USER_INFO_ADD = 10087;
    private static final int SYN_USER_INFO_DELATE = 10088;


    private static final int INVITAION_JOIN_GROUP = 23;
    private static final int INVITATION_REFUSE_JOIN_GROUP = 24;
    private static final int GET_GROUPINFO_BY_TOKEN = 25;

    private static final int GET_GROUP_NUMBERS = 41;


    private String mSyncName, mFriendSync, groupToken, membersname;
    private String mFriedndsName;
    private MessageContent messageContent;

    private Friend friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rong);
        mSyncName = getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.BAOJIA_USER_SYNCNAME, "");
        SubConversationListFragmentNew fragment = new SubConversationListFragmentNew();
        adapterDYC = new SubConversationListAdapterDYC(RongContext.getInstance());
        fragment.setAdapter(adapterDYC);

        adapterDYC.setActionLisener(new SubConversationListAdapterDYC.DoActionLisener() {
            @Override
            public void onAcitonLisener(int action, UIConversation data) {
                messageContent = data.getMessageContent();
                if (messageContent instanceof ContactNotificationMessage) {
                    mFriendSync = ((ContactNotificationMessage) messageContent).getSourceUserId();
                    mFriedndsName = ((ContactNotificationMessage) messageContent).getExtra();
                    LoadDialog.show(mContext);
                    if (action == 0) {
                        request(SYN_USER_INFO_DELATE);
                    } else {
                        request(SYN_USER_INFO_ADD);
                    }
                } else if (messageContent instanceof GroupNotificationMessage) {
                    GroupNotificationMessage groupNotificationMessage = (GroupNotificationMessage) messageContent;
                    groupToken = groupNotificationMessage.getExtra();
                    membersname = groupNotificationMessage.getOperatorUserId();
                    if (groupNotificationMessage.getOperation().equals("joinRequest")) {
                        LoadDialog.show(mContext);
                        if (action == 0) {
                            request(REFUSE_JOIN_GROUP);
                        } else {
                            request(ACCEPT_JOIN_GROUP);
                        }
                    } else if (groupNotificationMessage.getOperation().equals("invitationRequest")) {
                        LoadDialog.show(mContext);
                        if (action == 0) {
                            request(INVITATION_REFUSE_JOIN_GROUP);
                        } else {
                            request(INVITAION_JOIN_GROUP);
                        }

                    }

                }
            }
        });

//        SubConversationListFragment fragment = new SubConversationListFragment();
//        fragment.setAdapter(new SubConversationListAdapterEx(RongContext.getInstance()));
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.rong_content, fragment);
        transaction.commit();

        Intent intent = getIntent();
        if (intent.getData() == null) {
            return;
        }
        //聚合会话参数
        String type = intent.getData().getQueryParameter("type");

        if (type == null)
            return;

        if (type.equals("group")) {
            setTitle(R.string.de_actionbar_sub_group);
        } else if (type.equals("private")) {
            setTitle(R.string.de_actionbar_sub_private);
        } else if (type.equals("discussion")) {
            setTitle(R.string.de_actionbar_sub_discussion);
        } else if (type.equals("system")) {
            setTitle(R.string.de_actionbar_sub_system);
        } else {
            setTitle(R.string.de_actionbar_sub_defult);
        }
    }


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case SYN_USER_INFO_DELATE:
                return mAction.userDetail(mSyncName, ((ContactNotificationMessage) messageContent).getSourceUserId());
            case SYN_USER_INFO_ADD:
                return mAction.userDetail(mSyncName, ((ContactNotificationMessage) messageContent).getSourceUserId());
            case AGREE_FRIENDS:
                return mAction.agreeFriend(mSyncName, mFriendSync);
            case REUFSE_FRIENDS:
                return mAction.refuseFriend(mSyncName, mFriendSync);
            case ACCEPT_JOIN_GROUP:
                return mAction.acceptetFriendInvitation(membersname, groupToken, mSyncName);
            case REFUSE_JOIN_GROUP:
                return mAction.refuseFriendInvitation(membersname, groupToken, mSyncName);
            case INVITAION_JOIN_GROUP:
                return mAction.invitationFriendInvitation(groupToken, mSyncName);
            case INVITATION_REFUSE_JOIN_GROUP:
                return mAction.invitaitonRefuseFriendInvitation(groupToken, mSyncName);
            case GET_GROUPINFO_BY_TOKEN:
                return mAction.getGroupInfo(groupToken, mSyncName);
            case GET_GROUP_NUMBERS:
                return mAction.getGroupNumbers(groupToken, mSyncName);
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                //用于删除好友
                case SYN_USER_INFO_DELATE:
                    FriendResponse friendResponseREFUSE = (FriendResponse) result;
                    FriendResponse.ResultEntity beanRE = friendResponseREFUSE.getData();
                    if (beanRE == null) {
                        return;
                    }
                    friend = new Friend(beanRE.getSyncName(),
                            beanRE.getUserName(),
                            Uri.parse(beanRE.getPortrait() + ""),
                            beanRE.getUserName(),
                            String.valueOf(beanRE.getStatus()),
                            null,
                            null,
                            null,
                            CharacterParser.getInstance().getSpelling(beanRE.getUserName()),
                            CharacterParser.getInstance().getSpelling(beanRE.getUserName()));
                    request(REUFSE_FRIENDS);
                    break;
                //获取  添加好友
                case SYN_USER_INFO_ADD:
                    FriendResponse friendResponse = (FriendResponse) result;
                    FriendResponse.ResultEntity bean = friendResponse.getData();
                    if (bean == null) {
                        return;
                    }
                    friend = new Friend(bean.getSyncName(),
                            bean.getUserName(),
                            Uri.parse(bean.getPortrait() + ""),
                            bean.getUserName(),
                            String.valueOf(bean.getStatus()),
                            null,
                            null,
                            null,
                            CharacterParser.getInstance().getSpelling(bean.getUserName()),
                            CharacterParser.getInstance().getSpelling(bean.getUserName()));
                    request(AGREE_FRIENDS);

                    break;
                case AGREE_FRIENDS:
                    AgreeFriendResponse agreeFriendResponse = (AgreeFriendResponse) result;
                    LogUtils.w("dyc", agreeFriendResponse);
                    SealUserInfoManager.getInstance().addFriend(friend);

                    // 通知好友列表刷新数据
                    NToast.shortToast(mContext, R.string.agreed_friend);
                    LoadDialog.dismiss(mContext);
                    BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATE_FRIEND);
                    RongIM.getInstance().startPrivateChat(SubConversationListActivity.this, mFriendSync, mFriedndsName);
                    finish();
                    break;
                case REUFSE_FRIENDS:
                    AgreeFriendResponse response1 = (AgreeFriendResponse) result;


                    SealUserInfoManager.getInstance().deleteFriend(friend);
                    // 通知好友列表刷新数据
                    NToast.shortToast(mContext, R.string.reject_friend);
                    LoadDialog.dismiss(mContext);
                    BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATE_FRIEND);
                    finish();

                    break;
                case REFUSE_JOIN_GROUP:
                    FriendInvitationResponse friendInvitationResponse = (FriendInvitationResponse) result;
                    if (friendInvitationResponse.getCode() == 100000) {
                        ToastUtils.showShort(friendInvitationResponse.getMessage());
                    } else {
                        ToastUtils.showShort(friendInvitationResponse.getMessage());
                    }
                    BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATE_FRIEND);
                    LoadDialog.dismiss(SubConversationListActivity.this);
                    finish();
                    return;
                case ACCEPT_JOIN_GROUP:
                    FriendInvitationResponse friendInvitationResponse1 = (FriendInvitationResponse) result;
                    if (friendInvitationResponse1.getCode() == 100000) {
                        ToastUtils.showShort(friendInvitationResponse1.getMessage());
                        request(GET_GROUPINFO_BY_TOKEN);
                    } else {
                        ToastUtils.showShort(friendInvitationResponse1.getMessage());
                    }
                    BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATE_FRIEND);
                    LoadDialog.dismiss(SubConversationListActivity.this);
                    return;
                case INVITAION_JOIN_GROUP:
                    FriendInvitationResponse friendInvitationResponse2 = (FriendInvitationResponse) result;
                    if (friendInvitationResponse2.getCode() == 100000) {
                        ToastUtils.showShort(friendInvitationResponse2.getMessage());
                        request(GET_GROUPINFO_BY_TOKEN);
                    } else {
                        ToastUtils.showShort(friendInvitationResponse2.getMessage());
                    }
                    LoadDialog.dismiss(SubConversationListActivity.this);
                    return;
                case INVITATION_REFUSE_JOIN_GROUP:
                    FriendInvitationResponse friendInvitationResponse3 = (FriendInvitationResponse) result;
                    if (friendInvitationResponse3.getCode() == 100000) {
                        ToastUtils.showShort(friendInvitationResponse3.getMessage());
                    } else {
                        ToastUtils.showShort(friendInvitationResponse3.getMessage());
                    }
                    LoadDialog.dismiss(SubConversationListActivity.this);
                    finish();
                    return;
                case GET_GROUPINFO_BY_TOKEN:
                    GroupDetailBaoResponse response3 = (GroupDetailBaoResponse) result;
                    if (response3.getCode() == 100000) {
                        GroupDetailBaoResponse.ResultEntity beantoken = response3.getData();
                        RongIM.getInstance().refreshGroupInfoCache(new Group(groupToken, beantoken.getGroupName(), Uri.parse(beantoken.getGroupIcon())));
                        RongIM.getInstance().startGroupChat(SubConversationListActivity.this,groupToken,beantoken.getGroupName());
                        finish();
                    }else{
                        ToastUtils.showShort(""+response3.getMessage());
                    }

                    break;
                case GET_GROUP_NUMBERS:
                    GroupNumbersBaoResponse groupNumbersBaoResponse = (GroupNumbersBaoResponse) result;
                    if (groupNumbersBaoResponse.getCode() == 100000) {
                        if (groupNumbersBaoResponse.getData() != null && groupNumbersBaoResponse.getData().size() > 0) {
                            ArrayList<GroupNumbersBaoResponse.ResultEntity> mGroupMember = (ArrayList<GroupNumbersBaoResponse.ResultEntity>) groupNumbersBaoResponse.getData();


                            List<GetGroupMemberResponse.ResultEntity> list = new ArrayList<>();
                            if (mGroupMember != null && mGroupMember.size() > 0) {
                                for (GroupNumbersBaoResponse.ResultEntity item : mGroupMember) {
                                    GetGroupMemberResponse.ResultEntity resultEntity = new GetGroupMemberResponse.ResultEntity();
                                    GetGroupMemberResponse.ResultEntity.UserEntity userEntity = new GetGroupMemberResponse.ResultEntity.UserEntity();
                                    resultEntity.setCreatedAt(item.getCreateTime1());
                                    resultEntity.setRole(Integer.parseInt(item.getMemberRole()));
                                    userEntity.setId(item.getId());
                                    resultEntity.setDisplayName(item.getUserName());
                                    userEntity.setNickname(item.getSyncName());
                                    userEntity.setPortraitUri(item.getPortrait());
                                    resultEntity.setUser(userEntity);
                                    list.add(resultEntity);

                                }
                            }

                            SealUserInfoManager.getInstance().addGroupMembers(list, groupToken);
                            request(GET_GROUPINFO_BY_TOKEN);
                        }
                    }
                    break;
            }
        }
    }


    @Override
    public void onFailure(int requestCode, int state, Object result) {
        LoadDialog.dismiss(mContext);

    }
}
