package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.dbcapp.club.R;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.db.Groups;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.CreateGroupBaoResponse;
import cn.rongcloud.im.server.response.GetQiNiuTokenResponse;
import cn.rongcloud.im.server.response.SetGroupPortraitResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.photo.PhotoUtils;
import cn.rongcloud.im.server.widget.BottomMenuDialog;
import cn.rongcloud.im.server.widget.ClearWriteEditText;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.utils.UpLoadImgManager;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.Conversation;

/**
 * Created by AMing on 16/1/25.
 * Company RongCloud
 */
public class CreateGroupActivity extends BaseActivity implements View.OnClickListener {

    private static final int GET_QI_NIU_TOKEN = 131;
    private static final int CREATE_GROUP = 16;
    private static final int SET_GROUP_PORTRAIT_URI = 17;
    public static final String REFRESH_GROUP_UI = "REFRESH_GROUP_UI";
    private AsyncImageView asyncImageView;
    private PhotoUtils photoUtils;
    private BottomMenuDialog dialog;
    private String mGroupName, mGroupId, mGroupBrief;
    private ClearWriteEditText mGroupNameEdit, mGroupBriefEdit;
    private List<String> groupIds = new ArrayList<>();
    private Uri selectUri;
    private UploadManager uploadManager;
    private String imageUrl;
    private String mSyncName;
    private String mToken;
    private String mImageUrl;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        setTitle(R.string.rc_item_create_group);
        List<Friend> memberList = (List<Friend>) getIntent().getSerializableExtra("GroupMember");
        initView();
        mSyncName = getSharedPreferences("config", MODE_PRIVATE).
                getString(SealConst.BAOJIA_USER_SYNCNAME, "");
        setPortraitChangeListener();
        if (memberList != null && memberList.size() > 0) {
            groupIds.add(getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_ID, ""));
            for (Friend f : memberList) {
                groupIds.add(f.getUserId());
            }
        }
    }

    private void setPortraitChangeListener() {
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri) {
                if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                    selectUri = uri;
                    LoadDialog.show(mContext);
                    request(GET_QI_NIU_TOKEN);
                }
            }

            @Override
            public void onPhotoCancel() {

            }
        });
    }

    private void initView() {
        asyncImageView = (AsyncImageView) findViewById(R.id.img_Group_portrait);
        asyncImageView.setOnClickListener(this);
        Button mButton = (Button) findViewById(R.id.create_ok);
        mButton.setOnClickListener(this);
        mGroupNameEdit = (ClearWriteEditText) findViewById(R.id.create_groupname);
        mGroupBriefEdit = findViewById(R.id.create_group_brief);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_Group_portrait:
                if (mUpLoadImgManager == null) {
                    mUpLoadImgManager = new UpLoadImgManager();
                }
                showPhotoDialog();
                break;
            case R.id.create_ok:
                mGroupName = mGroupNameEdit.getText().toString().trim();
                mGroupBrief = mGroupBriefEdit.getText().toString().trim();
                if (TextUtils.isEmpty(mGroupName)) {
                    NToast.shortToast(mContext, getString(R.string.group_name_not_is_null));
                    break;
                }
                if (mGroupName.length() == 1) {
                    NToast.shortToast(mContext, getString(R.string.group_name_size_is_one));
                    return;
                }
                if (AndroidEmoji.isEmoji(mGroupName)) {
                    if (mGroupName.length() <= 2) {
                        NToast.shortToast(mContext, getString(R.string.group_name_size_is_one));
                        return;
                    }
                }
//                if (groupIds.size() > 1) {
                LoadDialog.show(mContext);
                request(CREATE_GROUP, true);
//                }

                break;
        }
    }


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case CREATE_GROUP:
                return mAction.createGroup(mSyncName, mGroupName, mImageUrl, mGroupBrief);
//            case SET_GROUP_PORTRAIT_URI:
//                return mAction.modifyGroupIcon(groupToken,mGroupName, imageUrl);
            case GET_QI_NIU_TOKEN:
                return mAction.getQiNiuToken(GetQiNiuTokenResponse.PORTRAIT_TYPE, mSyncName);
        }
        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case CREATE_GROUP:
                    CreateGroupBaoResponse createGroupResponse = (CreateGroupBaoResponse) result;
                    if (createGroupResponse.getCode() == 100000) {
//                        mGroupId = createGroupResponse.getResult().getId(); //id == null
//                        if (TextUtils.isEmpty(imageUrl)) {
                        mGroupId = createGroupResponse.getData();
                        SealUserInfoManager.getInstance().addGroup(new Groups(mGroupId, mGroupName, imageUrl, String.valueOf(0)));
                        BroadcastManager.getInstance(mContext).sendBroadcast(REFRESH_GROUP_UI);
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, getString(R.string.create_group_success));
                        RongIM.getInstance().startConversation(mContext, Conversation.ConversationType.GROUP, mGroupId, mGroupName);
                        finish();

//                        } else {
//                            if (!TextUtils.isEmpty(mGroupId)) {
//                                request(SET_GROUP_PORTRAIT_URI);
//                            }
//                        }
                    }
                    break;
                case SET_GROUP_PORTRAIT_URI:
                    SetGroupPortraitResponse groupPortraitResponse = (SetGroupPortraitResponse) result;
                    if (groupPortraitResponse.getCode() == 200) {
                        SealUserInfoManager.getInstance().addGroup(new Groups(mGroupId, mGroupName, imageUrl, String.valueOf(0)));
                        BroadcastManager.getInstance(mContext).sendBroadcast(REFRESH_GROUP_UI);
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, getString(R.string.create_group_success));
                        RongIM.getInstance().startConversation(mContext, Conversation.ConversationType.GROUP, mGroupId, mGroupName);
                        finish();
                    }
                case GET_QI_NIU_TOKEN:

                    GetQiNiuTokenResponse qiNiuTokenResponse = (GetQiNiuTokenResponse) result;
                    if (qiNiuTokenResponse.getCode() == 100000) {
                        mToken = qiNiuTokenResponse.getData().getToken();
                        uploadImage(mToken, selectUri);
                    } else {
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(this, qiNiuTokenResponse.getMessage());
                    }


                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case CREATE_GROUP:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, getString(R.string.group_create_api_fail));
                break;
            case GET_QI_NIU_TOKEN:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, getString(R.string.upload_portrait_failed));
                break;
            case SET_GROUP_PORTRAIT_URI:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, getString(R.string.group_header_api_fail));
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        hintKbTwo();
        finish();
        return super.onOptionsItemSelected(item);
    }


    /**
     * 弹出底部框
     */
    private void showPhotoDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new BottomMenuDialog(mContext);
        dialog.setConfirmListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                photoUtils.takePicture(CreateGroupActivity.this);
            }
        });
        dialog.setMiddleListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                photoUtils.selectPicture(CreateGroupActivity.this);
            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PhotoUtils.INTENT_CROP:
            case PhotoUtils.INTENT_TAKE:
            case PhotoUtils.INTENT_SELECT:
                photoUtils.onActivityResult(CreateGroupActivity.this, requestCode, resultCode, data);
                break;
        }
    }

    private UpLoadImgManager mUpLoadImgManager;
    private UploadManager mUploadManager;

    public void uploadImage(String imageToken, Uri imagePath) {
        File imageFile = new File(imagePath.getPath());

        if (this.mUploadManager == null) {
            this.mUploadManager = new UploadManager();
        }
        this.mUploadManager.put(imageFile, null, imageToken, new UpCompletionHandler() {

            @Override
            public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
                if (responseInfo.isOK()) {
                    try {
                        String key = (String) jsonObject.get("key");
                        mImageUrl = "http://" + getString(R.string.baojia_qiniu_domain) + "/" + key;
                        Log.e("uploadImage", mImageUrl);
                        LoadDialog.dismiss(mContext);
                        if (!TextUtils.isEmpty(mImageUrl)) {
//                            request(SET_GROUP_PORTRAIT_URI);
                            ImageLoader.getInstance().displayImage(mImageUrl,asyncImageView);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    NToast.shortToast(mContext, getString(R.string.upload_portrait_failed));
                    LoadDialog.dismiss(mContext);
                }
            }
        }, null);
    }

    private void hintKbTwo() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
