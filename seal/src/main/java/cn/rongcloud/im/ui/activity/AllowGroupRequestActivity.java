package cn.rongcloud.im.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.dbcapp.club.R;

import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.FriendInvitationResponse;
import cn.rongcloud.im.server.response.GroupDetailBaoResponse;
import cn.rongcloud.im.server.utils.CommonUtils;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.utils.PerfectClickListener;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Group;
import io.rong.message.GroupNotificationMessage;

/**
 * Author: 邓言诚  Create at : 2018/6/28  01:45
 * Email: yanchengdeng@gmail.com
 * Describle: 允许  或  拒绝  其他人加入群
 */
public class AllowGroupRequestActivity extends BaseActivity {

    private static final int REFUSE_JOIN_GROUP = 20;
    private static final int ACCEPT_JOIN_GROUP = 21;


    private static final int INVITAION_JOIN_GROUP = 23;
    private static final int INVITATION_REFUSE_JOIN_GROUP = 24;
    private static final int GET_GROUPINFO_BY_TOKEN = 25;
    private GroupNotificationMessage groupNotificationMessage;
    private TextView tvContent;
    private String groupToken, membersname;
    private String mSyncName;
    private boolean isMyselfAction;//是否是 用户被邀请入群操作

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allow_group_request);
        groupNotificationMessage = getIntent().getParcelableExtra("data");
        tvContent = findViewById(R.id.ship_name);
        setTitle(getString(R.string.system_msg));

        mSyncName = getSharedPreferences("config", MODE_PRIVATE).
                getString(SealConst.BAOJIA_USER_SYNCNAME, "");

        if (!CommonUtils.isNetworkConnected(mContext)) {
            NToast.shortToast(mContext, R.string.check_network);
            return;
        }

        if (groupNotificationMessage == null) {
            return;
        }

        if (groupNotificationMessage != null && TextUtils.isEmpty(groupNotificationMessage.getExtra())) {
            return;
        }

        isMyselfAction = groupNotificationMessage.getOperation().equals("invitationRequest");

        groupToken = groupNotificationMessage.getExtra();
        membersname = groupNotificationMessage.getOperatorUserId();
        if (!TextUtils.isEmpty(groupNotificationMessage.getMessage())) {
            tvContent.setText(groupNotificationMessage.getMessage());
        }


        //拒绝
        findViewById(R.id.ship_state_refuse).setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                LoadDialog.show(AllowGroupRequestActivity.this);
                if (isMyselfAction) {
                    request(INVITATION_REFUSE_JOIN_GROUP);
                } else {
                    request(REFUSE_JOIN_GROUP);
                }
            }
        });

        //接受
        findViewById(R.id.ship_state).setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                LoadDialog.show(AllowGroupRequestActivity.this);
                if (isMyselfAction) {
                    request(INVITAION_JOIN_GROUP);
                } else {
                    request(ACCEPT_JOIN_GROUP);
                }
            }
        });


    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
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
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode) {
            case REFUSE_JOIN_GROUP:
                FriendInvitationResponse friendInvitationResponse = (FriendInvitationResponse) result;
                if (friendInvitationResponse.getCode() == 100000) {
                    ToastUtils.showShort(friendInvitationResponse.getMessage());
                } else {
                    ToastUtils.showShort(friendInvitationResponse.getMessage());
                }
                BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATE_FRIEND);
                LoadDialog.dismiss(AllowGroupRequestActivity.this);
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
                LoadDialog.dismiss(AllowGroupRequestActivity.this);
                return;
            case INVITAION_JOIN_GROUP:
                FriendInvitationResponse friendInvitationResponse2 = (FriendInvitationResponse) result;
                if (friendInvitationResponse2.getCode() == 100000) {
                    ToastUtils.showShort(friendInvitationResponse2.getMessage());
                    request(GET_GROUPINFO_BY_TOKEN);
                } else {
                    ToastUtils.showShort(friendInvitationResponse2.getMessage());
                }
                LoadDialog.dismiss(AllowGroupRequestActivity.this);
                return;
            case INVITATION_REFUSE_JOIN_GROUP:
                FriendInvitationResponse friendInvitationResponse3 = (FriendInvitationResponse) result;
                if (friendInvitationResponse3.getCode() == 100000) {
                    ToastUtils.showShort(friendInvitationResponse3.getMessage());
                } else {
                    ToastUtils.showShort(friendInvitationResponse3.getMessage());
                }
                LoadDialog.dismiss(AllowGroupRequestActivity.this);
                finish();
                return;
            case GET_GROUPINFO_BY_TOKEN:
                GroupDetailBaoResponse response3 = (GroupDetailBaoResponse) result;
                if (response3.getCode() == 100000) {
                    GroupDetailBaoResponse.ResultEntity bean = response3.getData();
                    RongIM.getInstance().refreshGroupInfoCache(new Group(groupToken, bean.getGroupName(), Uri.parse(bean.getGroupIcon())));
                    finish();
                }else{
                    ToastUtils.showShort(""+response3.getMessage());
                }
                return;
        }
        super.onSuccess(requestCode, result);
    }


}
