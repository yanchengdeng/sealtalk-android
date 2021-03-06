package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dbcapp.club.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GetUserInfosResponse;
import cn.rongcloud.im.server.response.SearchContactListResponse;
import cn.rongcloud.im.server.response.SearchContactResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.OperationRong;
import cn.rongcloud.im.server.widget.DialogWithYesOrNoUtils;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.widget.DemoGridView;
import cn.rongcloud.im.ui.widget.switchbutton.SwitchButton;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.utilities.PromptPopupDialog;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.UserInfo;


/**
 * Created by AMing on 16/5/5.
 * Company RongCloud
 */
public class DiscussionDetailActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private static final int FIND_USER_INFO = 10;
    private static final int GET_USER_INFO = 22;
    //后台获取 剩余用户数
    private static final int GET_LEFT_INFO = 32;

    private static final int UPDATE_DISCUSS_NAME = 55;

    //TODO 如果总共的请求用户数  大于20 则分步请求  第一次请求20   第二次请求剩余全部 ；反之则一次性请求全部
    private static final int PAGE_SIZE_SIZE = 20;

    private String targetId;
    private String createId;
    private Discussion mDiscussion;
    private TextView memberSize;
    private List<UserInfo> memberList = new ArrayList<>();
    private DemoGridView mGridView;
    private GridAdapter adapter;
    private boolean isCreated;
    private SwitchButton discussionTop, discussionNof;
    private List<String> ids;
    private List<String> mUnknowIds = new ArrayList<>();

    private TextView mTvDiscussionName;
    private LinearLayout mLayoutDiscussName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_discussion);
        setTitle("讨论组详情");
        targetId = getIntent().getStringExtra("TargetId");
        if (TextUtils.isEmpty(targetId)) {
            return;
        }

        initView();
        RongIM.getInstance().getDiscussion(targetId, new RongIMClient.ResultCallback<Discussion>() {
            @Override
            public void onSuccess(Discussion discussion) {
                mDiscussion = discussion;
                if (mDiscussion != null) {
                    initData(mDiscussion);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {

            }
        });

    }


    private void initView() {
        memberSize = (TextView) findViewById(R.id.discu_member_size);
        mGridView = (DemoGridView) findViewById(R.id.discu_gridview);
        discussionTop = (SwitchButton) findViewById(R.id.sw_discu_top);
        discussionNof = (SwitchButton) findViewById(R.id.sw_discu_notfaction);
        LinearLayout discussionClean = (LinearLayout) findViewById(R.id.discu_clean);
        Button deleteDiscussion = (Button) findViewById(R.id.discu_quit);
        mTvDiscussionName = findViewById(R.id.tv_discusstion_name);
        mLayoutDiscussName = findViewById(R.id.ll_discussion_name);
        discussionTop.setOnCheckedChangeListener(this);
        discussionNof.setOnCheckedChangeListener(this);
        discussionClean.setOnClickListener(this);
        deleteDiscussion.setOnClickListener(this);
        mLayoutDiscussName.setOnClickListener(this);
        RongIM.getInstance().getConversation(Conversation.ConversationType.DISCUSSION, targetId, new RongIMClient.ResultCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                if (conversation == null) {
                    return;
                }
                if (conversation.isTop()) {
                    discussionTop.setChecked(true);
                } else {
                    discussionTop.setChecked(false);
                }

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });

        RongIM.getInstance().getConversationNotificationStatus(Conversation.ConversationType.DISCUSSION, targetId, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {

                if (conversationNotificationStatus == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB) {
                    discussionNof.setChecked(true);
                } else {
                    discussionNof.setChecked(false);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }


    private void initData(Discussion mDiscussion) {
        memberSize.setText("讨论组成员(" + mDiscussion.getMemberIdList().size() + ")");
        createId = mDiscussion.getCreatorId();
        mTvDiscussionName.setText(mDiscussion.getName());
        ids = mDiscussion.getMemberIdList();
        if (ids != null) {
            for (String id : ids) {
                UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo((id));
                if (userInfo == null) {
                    mUnknowIds.add(id);
//                    userInfo = new UserInfo(id, "", Uri.parse(""));
                } else {
                    memberList.add(userInfo);
                }
            }


//            }else {
            String loginId = getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_ID, "");
            if (loginId.equals(createId)) {
                isCreated = true;
            }
            if (memberList != null && memberList.size() > 1) {
                if (adapter == null) {
                    adapter = new GridAdapter(mContext, memberList);
                    mGridView.setAdapter(adapter);
                } else {
                    adapter.updateListView(memberList);
                }
            }

            if (mUnknowIds.size() > 0) {
//                LoadDialog.show(this);
                request(GET_USER_INFO);
            }

//            }
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sw_discu_top:
                if (isChecked) {
                    OperationRong.setConversationTop(mContext, Conversation.ConversationType.DISCUSSION, targetId, true);
                } else {
                    OperationRong.setConversationTop(mContext, Conversation.ConversationType.DISCUSSION, targetId, false);
                }
                break;
            case R.id.sw_discu_notfaction:
                if (isChecked) {
                    OperationRong.setConverstionNotif(mContext, Conversation.ConversationType.DISCUSSION, targetId, true);
                } else {
                    OperationRong.setConverstionNotif(mContext, Conversation.ConversationType.DISCUSSION, targetId, false);
                }
                break;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.discu_clean:
                PromptPopupDialog.newInstance(mContext,
                        getString(R.string.clean_discussion_chat_history)).setLayoutRes(io.rong.imkit.R.layout.rc_dialog_popup_prompt_warning)
                        .setPromptButtonClickedListener(new PromptPopupDialog.OnPromptButtonClickedListener() {
                            @Override
                            public void onPositiveButtonClicked() {
                                if (RongIM.getInstance() != null) {
                                    RongIM.getInstance().clearMessages(Conversation.ConversationType.DISCUSSION, targetId, new RongIMClient.ResultCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean aBoolean) {
                                            NToast.shortToast(mContext, getString(R.string.clear_success));
                                        }

                                        @Override
                                        public void onError(RongIMClient.ErrorCode errorCode) {
                                            NToast.shortToast(mContext, getString(R.string.clear_failure));
                                        }
                                    });
                                    RongIMClient.getInstance().cleanRemoteHistoryMessages(Conversation.ConversationType.DISCUSSION, targetId, System.currentTimeMillis(), null);
                                }
                            }
                        }).show();
                break;
            case R.id.discu_quit:
                DialogWithYesOrNoUtils.getInstance().showDialog(mContext, "是否退出并删除当前讨论组?", new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void executeEvent() {
                        RongIM.getInstance().quitDiscussion(targetId, new RongIMClient.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                RongIM.getInstance().removeConversation(Conversation.ConversationType.DISCUSSION, targetId);
                                Intent i = new Intent();
                                i.putExtra("disFinish", "disFinish");
                                setResult(112, i);
                                finish();
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {

                            }
                        });
                    }

                    @Override
                    public void executeEditEvent(String editText) {

                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });

                break;
            case R.id.ll_discussion_name: //改名
                Intent intent = new Intent(this, UpdateDiscussionNameActivity.class);
                intent.putExtra("discussion_name", mDiscussion.getName());
                startActivityForResult(intent, UPDATE_DISCUSS_NAME);
                break;
        }
    }


    private class GridAdapter extends BaseAdapter {

        private List<UserInfo> list;
        Context context;


        public GridAdapter(Context context, List<UserInfo> list) {
            this.list = list;
            this.context = context;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.social_chatsetting_gridview_item, parent, false);
            }
            SelectableRoundedImageView iv_avatar = (SelectableRoundedImageView) convertView.findViewById(R.id.iv_avatar);
            TextView tv_username = (TextView) convertView.findViewById(R.id.tv_username);
            ImageView badge_delete = (ImageView) convertView.findViewById(R.id.badge_delete);

            // 最后一个item，减人按钮
            if (position == getCount() - 1 && isCreated) {
                tv_username.setText("");
                badge_delete.setVisibility(View.GONE);
                iv_avatar.setImageResource(R.drawable.icon_btn_deleteperson);

                iv_avatar.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DiscussionDetailActivity.this, SelectFriendsActivity.class);
                        intent.putExtra("DeleteDiscuMember", (Serializable) memberList);
                        intent.putExtra("DeleteDiscuId", targetId);
                        startActivityForResult(intent, SealConst.DISCUSSION_REMOVE_MEMBER_REQUEST_CODE);
                    }

                });
            } else if ((isCreated && position == getCount() - 2) || (!isCreated && position == getCount() - 1)) {
                tv_username.setText("");
                badge_delete.setVisibility(View.GONE);
                iv_avatar.setImageResource(R.drawable.jy_drltsz_btn_addperson);

                iv_avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DiscussionDetailActivity.this, SelectFriendsActivity.class);
                        intent.putExtra("AddDiscuMember", (Serializable) memberList);
                        intent.putExtra("AddDiscuId", targetId);
                        startActivityForResult(intent, SealConst.DISCUSSION_ADD_MEMBER_REQUEST_CODE);

                    }
                });
            } else { // 普通成员
                UserInfo bean = list.get(position);
                if (!TextUtils.isEmpty(bean.getName())) {
                    tv_username.setText(bean.getName());
                }

                String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(bean);
                ImageLoader.getInstance().displayImage(portraitUri, iv_avatar, App.getOptions());
                iv_avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }

                });

            }

            return convertView;
        }

        @Override
        public int getCount() {
            if (isCreated) {
                return list.size() + 2;
            } else {
                return list.size() + 1;
            }
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * 传入新的数据 刷新UI的方法
         */
        public void updateListView(List<UserInfo> list) {
            this.list = list;
            notifyDataSetChanged();
        }

    }


    // 拿到新增的成员刷新adapter
    @Override
    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SealConst.DISCUSSION_ADD_MEMBER_REQUEST_CODE:
                    final List<String> addMember = (List<String>) data.getSerializableExtra("addDiscuMember");
                    RongIMClient.getInstance().addMemberToDiscussion(targetId, addMember, new RongIMClient.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            SealUserInfoManager.getInstance().getFriends(new SealUserInfoManager.ResultCallback<List<Friend>>() {
                                @Override
                                public void onSuccess(List<Friend> friendList) {
                                    if (friendList != null && friendList.size() > 0) {
                                        for (Friend friend : friendList) {
                                            for (String userId : addMember) {
                                                if (userId.equals(friend.getUserId()))
                                                    memberList.add(new UserInfo(userId, friend.getName(), friend.getPortraitUri()));
                                            }
                                        }
                                        adapter.updateListView(memberList);
                                    }
                                }

                                @Override
                                public void onError(String errString) {

                                }
                            });
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
                    break;
                case SealConst.DISCUSSION_REMOVE_MEMBER_REQUEST_CODE:
                    List<String> deleteMember = (List<String>) data.getSerializableExtra("deleteDiscuMember");
                    List<UserInfo> filtered = new ArrayList<>();
                    for (String id : deleteMember) {
                        int count = memberList.size();
                        for (int i = 0; i < count; i++) {
                            if (memberList.get(i).getUserId().equals(id))
                                filtered.add(memberList.get(i));
                        }
                    }
                    for (UserInfo userInfo : filtered) {
                        RongIMClient.getInstance().removeMemberFromDiscussion(targetId, userInfo.getUserId(), null);
                        memberList.remove(userInfo);
                    }
                    adapter.updateListView(memberList);
                    break;
                case UPDATE_DISCUSS_NAME:
                    final String newName = data.getStringExtra("discussion_name");
                    mDiscussion.setName(newName);
                    RongIM.getInstance().setDiscussionName(targetId, newName, new RongIMClient.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            NToast.shortToast(DiscussionDetailActivity.this, R.string.baojia_update_discuss_name_success);
                            mTvDiscussionName.setText(newName);
                            BroadcastManager.getInstance(DiscussionDetailActivity.this).
                                    sendBroadcast(SealConst.BAOJIA_UPDATE_DISCUSS_NAME, newName);
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {
                            NToast.shortToast(DiscussionDetailActivity.this, R.string.baojia_update_discuss_name_fail);
                        }
                    });

                    break;
            }
        }
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case FIND_USER_INFO:
                return action.getUserInfos(ids);
            case GET_USER_INFO:
                return mAction.searchContact(mUnknowIds.size()>PAGE_SIZE_SIZE?mUnknowIds.subList(0,PAGE_SIZE_SIZE):mUnknowIds);
            case GET_LEFT_INFO:
                return mAction.searchContact(mUnknowIds);
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode) {
            case FIND_USER_INFO:
                GetUserInfosResponse response = (GetUserInfosResponse) result;
                if (response.getCode() == 200) {
                    List<GetUserInfosResponse.ResultEntity> infos = response.getResult();
                    memberList.clear();
                    for (GetUserInfosResponse.ResultEntity g : infos) {
                        memberList.add(new UserInfo(g.getId(), g.getNickname(), Uri.parse(g.getPortraitUri())));
                    }
                    String loginId = getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_ID, "");
                    if (loginId.equals(createId)) {
                        isCreated = true;
                    }
                    if (memberList != null && memberList.size() > 1) {
                        if (adapter == null) {
                            adapter = new GridAdapter(mContext, memberList);
                            mGridView.setAdapter(adapter);
                        } else {
                            adapter.updateListView(memberList);
                        }
                    }
                    LoadDialog.dismiss(mContext);
                }
                break;
            case GET_LEFT_INFO:
                SearchContactListResponse contactResponseLeft = (SearchContactListResponse) result;
                if (contactResponseLeft.getCode() == 100000) {
                    try {
                        SearchContactListResponse getCustomerListResponse = contactResponseLeft;
                        if (getCustomerListResponse != null && getCustomerListResponse.getData() != null && getCustomerListResponse.getData().size() > 0) {

                            List<UserInfo> userInfos = new ArrayList<>();
                            for (SearchContactResponse.ResultEntity entity : getCustomerListResponse.getData()) {
                                UserInfo userInfo = new UserInfo(entity.getSyncName(), entity.getUserName(), TextUtils.isEmpty(entity.getPortrait()) ? Uri.parse("") : Uri.parse(entity.getPortrait()));
                                userInfos.add(userInfo);
                            }
                            memberList.addAll(userInfos);
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                break;
            case GET_USER_INFO:
                SearchContactListResponse contactResponse = (SearchContactListResponse) result;
                if (contactResponse.getCode() == 100000) {
//                    SearchContactResponse.ResultEntity entity = contactResponse.getData();
//                    UserInfo userInfo = new UserInfo(entity.getSyncName(), entity.getUserName(), TextUtils.isEmpty(entity.getPortrait())?Uri.parse(""):Uri.parse(entity.getPortrait()));
//                    memberList.add(userInfo);
//                }else {
//                    UserInfo userInfo = new UserInfo(mUnknowIds.get(0), "", Uri.parse(""));
//                    memberList.add(userInfo);
//                }

//                mUnknowIds.remove(0);

                    if (adapter == null) {
                        adapter = new GridAdapter(mContext, memberList);
                        mGridView.setAdapter(adapter);
                    }
                    try {
                        SearchContactListResponse getCustomerListResponse = contactResponse;
                        if (getCustomerListResponse != null && getCustomerListResponse.getData() != null && getCustomerListResponse.getData().size() > 0) {

                            List<UserInfo> userInfos = new ArrayList<>();
                            for (SearchContactResponse.ResultEntity entity : getCustomerListResponse.getData()) {
                                UserInfo userInfo = new UserInfo(entity.getSyncName(), entity.getUserName(), TextUtils.isEmpty(entity.getPortrait()) ? Uri.parse("") : Uri.parse(entity.getPortrait()));
                                userInfos.add(userInfo);
                            }
                            if (mUnknowIds.size() > PAGE_SIZE_SIZE) {
                                mUnknowIds.removeAll(mUnknowIds.subList(0, PAGE_SIZE_SIZE));
                                request(GET_LEFT_INFO);
                            }

                            memberList.addAll(userInfos);
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }


               /* if (mUnknowIds.size() == 0){
                    LoadDialog.dismiss(this);
                    String loginId = getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_ID, "");
                    if (loginId.equals(createId)) {
                        isCreated = true;
                    }
                    if (memberList != null && memberList.size() > 1) {
                       adapter.updateListView(memberList);
                    }
                }else {
//                    request(GET_USER_INFO);

                    try {
                        SearchContactListResponse getCustomerListResponse = mAction.searchContact(mUnknowIds);
                        if (getCustomerListResponse!=null && getCustomerListResponse.getData()!=null && getCustomerListResponse.getData().size()>0){

                            List<UserInfo> userInfos = new ArrayList<>();
                            for (SearchContactResponse.ResultEntity entity:getCustomerListResponse.getData()){
                                UserInfo userInfo = new UserInfo(entity.getSyncName(), entity.getUserName(), TextUtils.isEmpty(entity.getPortrait())?Uri.parse(""):Uri.parse(entity.getPortrait()));
                                userInfos.add(userInfo);
                            }
                            memberList.addAll(userInfos);
                            adapter.notifyDataSetChanged();
                        }
                    } catch (HttpException e) {
                        e.printStackTrace();
                    }

                }

                adapter.notifyDataSetChanged();*/
                break;
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        super.onFailure(requestCode, state, result);
        LoadDialog.dismiss(this);
    }
}
