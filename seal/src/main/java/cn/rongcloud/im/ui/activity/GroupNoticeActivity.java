package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.blankj.utilcode.util.ToastUtils;
import com.dbcapp.club.R;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.FriendInvitationResponse;
import cn.rongcloud.im.server.widget.DialogWithYesOrNoUtils;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

@SuppressWarnings("deprecation")
public class GroupNoticeActivity extends BaseActivity implements View.OnClickListener, TextWatcher {
    private static final int UPDATE_GROUP_NOTICE = 10;
    EditText mEdit;
    Conversation.ConversationType mConversationType;
    String mTargetId;
    private String mSyncName;
    private String content ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_notice);
        mEdit = (EditText) findViewById(R.id.edit_area);
        Intent intent = getIntent();
        content = getIntent().getStringExtra("content");
        if (!TextUtils.isEmpty(content)){
            mEdit.setText(content);
        }
        mSyncName = getSharedPreferences("config", MODE_PRIVATE).
                getString(SealConst.BAOJIA_USER_SYNCNAME, "");
        mConversationType = Conversation.ConversationType.setValue(intent.getIntExtra("conversationType", 0));
        mTargetId = getIntent().getStringExtra("targetId");
        setTitle(R.string.group_announcement);
        Button rightButton = getHeadRightButton();
        rightButton.setVisibility(View.GONE);
        mHeadRightText.setVisibility(View.VISIBLE);
        mHeadRightText.setText(R.string.Done);
        mHeadRightText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        mHeadRightText.setClickable(false);
        mHeadRightText.setOnClickListener(this);
        mEdit.addTextChangedListener(this);
    }

    @Override
    public void onHeadLeftButtonClick(View v) {
        DialogWithYesOrNoUtils.getInstance().showDialog(this, getString(R.string.group_notice_exist_confirm), new DialogWithYesOrNoUtils.DialogCallBack() {
            @Override
            public void executeEvent() {
                finish();
            }

            @Override
            public void executeEditEvent(String editText) {

            }

            @Override
            public void updatePassword(String oldPassword, String newPassword) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_right:
                DialogWithYesOrNoUtils.getInstance().showDialog(this, getString(R.string.group_notice_post_confirm), new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void executeEvent() {
                        TextMessage textMessage = TextMessage.obtain(RongContext.getInstance().getString(R.string.group_notice_prefix) + mEdit.getText().toString());
                        MentionedInfo mentionedInfo = new MentionedInfo(MentionedInfo.MentionedType.ALL, null, null);
                        textMessage.setMentionedInfo(mentionedInfo);

                        RongIM.getInstance().sendMessage(Message.obtain(mTargetId, mConversationType, textMessage), null, null, new IRongCallback.ISendMessageCallback() {
                            @Override
                            public void onAttached(Message message) {

                            }

                            @Override
                            public void onSuccess(Message message) {

                            }

                            @Override
                            public void onError(Message message, RongIMClient.ErrorCode errorCode) {

                            }
                        });


                        request(UPDATE_GROUP_NOTICE);

                    }

                    @Override
                    public void executeEditEvent(String editText) {

                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });


                break;
        }
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        if (requestCode==UPDATE_GROUP_NOTICE) {
            return mAction.updateGroupNotice(mEdit.getText().toString(), mTargetId, mSyncName);
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        FriendInvitationResponse  friendInvitationResponse = (FriendInvitationResponse) result;
        if (friendInvitationResponse.getCode()==100000){
            ToastUtils.showShort("修改成功");
        }
        finish();
        super.onSuccess(requestCode, result);
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        super.onFailure(requestCode, state, result);
        ToastUtils.showShort("修改失败");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().length() > 0) {
            mHeadRightText.setClickable(true);
            mHeadRightText.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            mHeadRightText.setClickable(false);
            mHeadRightText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if ( s != null) {
            int start = mEdit.getSelectionStart();
            int end = mEdit.getSelectionEnd();
            mEdit.removeTextChangedListener(this);
            mEdit.setText(AndroidEmoji.ensure(s.toString()));
            mEdit.addTextChangedListener(this);
            mEdit.setSelection(start, end);
        }
    }
}
