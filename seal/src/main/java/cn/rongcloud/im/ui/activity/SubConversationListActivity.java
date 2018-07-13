package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.blankj.utilcode.util.ToastUtils;
import com.dbcapp.club.R;

import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.FriendInvitationResponse;
import cn.rongcloud.im.server.response.GetRelationFriendResponse;
import cn.rongcloud.im.server.response.GroupDetailBaoResponse;
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


    private static final int INVITAION_JOIN_GROUP = 23;
    private static final int INVITATION_REFUSE_JOIN_GROUP = 24;
    private static final int GET_GROUPINFO_BY_TOKEN = 25;

    private GetRelationFriendResponse userRelationshipResponse;
    private String mSyncName,mFriendSync,groupToken,membersname;
    private String mFriedndsName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rong);
        mSyncName = getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.BAOJIA_USER_SYNCNAME, "");
        SubConversationListFragmentNew fragment = new SubConversationListFragmentNew();
        adapterDYC =   new SubConversationListAdapterDYC(RongContext.getInstance());
        fragment.setAdapter(adapterDYC);

        adapterDYC.setActionLisener(new SubConversationListAdapterDYC.DoActionLisener() {
            @Override
            public void onAcitonLisener(int action, UIConversation data) {
                MessageContent messageContent = data.getMessageContent();
                if (messageContent instanceof ContactNotificationMessage) {
                    mFriendSync = ((ContactNotificationMessage) messageContent).getSourceUserId();
                    mFriedndsName = ((ContactNotificationMessage) messageContent).getExtra();
                    if (action==0){
                        request(REUFSE_FRIENDS);
                    }else{
                        request(AGREE_FRIENDS);
                    }
                }else if (messageContent instanceof GroupNotificationMessage){
                    GroupNotificationMessage groupNotificationMessage = (GroupNotificationMessage) messageContent;
                    groupToken = groupNotificationMessage.getExtra();
                    membersname = groupNotificationMessage.getOperatorUserId();
                    if (groupNotificationMessage.getOperation().equals("joinRequest")) {
                        if (action==0){
                            request(REFUSE_JOIN_GROUP);
                        }else{
                            request(ACCEPT_JOIN_GROUP);
                        }
                    }else if (groupNotificationMessage.getOperation().equals("invitationRequest")) {
                        if (action==0){
                            request(INVITATION_REFUSE_JOIN_GROUP);
                        }else {
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
            case AGREE_FRIENDS:
                return mAction.agreeFriend(mSyncName, mFriendSync);
            case REUFSE_FRIENDS:
                return mAction.refuseFriend(mSyncName,mFriendSync);
            case ACCEPT_JOIN_GROUP:
                return mAction.acceptetFriendInvitation(membersname, groupToken, mSyncName);
            case REFUSE_JOIN_GROUP:
                return mAction.refuseFriendInvitation(membersname, groupToken, mSyncName);
            case INVITAION_JOIN_GROUP:
                return mAction.invitationFriendInvitation(groupToken, mSyncName);
            case INVITATION_REFUSE_JOIN_GROUP:
                return mAction.invitaitonRefuseFriendInvitation(groupToken, mSyncName);
            case GET_GROUPINFO_BY_TOKEN:
                return mAction.getGroupInfo(groupToken,mSyncName);
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case AGREE_FRIENDS:
//                    AgreeFriendResponse response = (AgreeFriendResponse) result;
//                    GetRelationFriendResponse.ResultEntity bean = userRelationshipResponse.getData().get(index);
//                    SealUserInfoManager.getInstance().addFriend(new Friend(bean.getSyncName(),
//                            bean.getUserName(),
//                            Uri.parse(bean.getPortrait() + ""),
//                            bean.getUserName(),
//                            String.valueOf(bean.getStatus()),
//                            null,
//                            null,
//                            null,
//                            CharacterParser.getInstance().getSpelling(bean.getUserName()),
//                            CharacterParser.getInstance().getSpelling(bean.getUserName())));
                    // 通知好友列表刷新数据
                    NToast.shortToast(mContext, R.string.agreed_friend);
                    LoadDialog.dismiss(mContext);
                    BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATE_FRIEND);
                    RongIM.getInstance().startPrivateChat(SubConversationListActivity.this,mFriendSync,mFriedndsName);
                    finish();


                    break;
                case REUFSE_FRIENDS:
//                    GetRelationFriendResponse.ResultEntity bean1 = userRelationshipResponse.getData().get(index);
//                    SealUserInfoManager.getInstance().deleteFriend(new Friend(bean1.getSyncName(),
//                            bean1.getUserName(),
//                            Uri.parse(bean1.getPortrait() + ""),
//                            bean1.getUserName(),
//                            String.valueOf(bean1.getStatus()),
//                            null,
//                            null,
//                            null,
//                            CharacterParser.getInstance().getSpelling(bean1.getUserName()),
//                            CharacterParser.getInstance().getSpelling(bean1.getUserName())));

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
                        GroupDetailBaoResponse.ResultEntity bean = response3.getData();
                        RongIM.getInstance().refreshGroupInfoCache(new Group(groupToken, bean.getGroupName(), Uri.parse(bean.getGroupIcon())));
                        RongIM.getInstance().startGroupChat(SubConversationListActivity.this,groupToken,bean.getGroupName());
                        finish();
                    }else{
                        ToastUtils.showShort(""+response3.getMessage());
                    }
                    return;
            }
        }
    }


    @Override
    public void onFailure(int requestCode, int state, Object result) {
        LoadDialog.dismiss(mContext);

    }
}
