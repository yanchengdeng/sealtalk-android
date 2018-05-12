package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.net.Uri;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dbcapp.club.R;

import cn.rongcloud.im.App;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.server.BaojiaAction;
import cn.rongcloud.im.server.network.async.AsyncTaskManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.SearchContactResponse;
import cn.rongcloud.im.server.response.getAddFriendResponse;
import cn.rongcloud.im.server.utils.CommonUtils;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.DialogWithYesOrNoUtils;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import io.rong.common.RLog;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imlib.model.UserInfo;

public class SearchFriendActivity extends BaseActivity {

    private static final int CLICK_CONVERSATION_USER_PORTRAIT = 1;
    private static final int SEARCH_CONTACT = 10;
    private static final int ADD_FRIEND = 11;

    private BaojiaAction mAction;

    private Button mBtnSearch;
    private EditText mEtSearch;
    private LinearLayout searchItem;
    private TextView searchName;
    private SelectableRoundedImageView searchImage;
    private String mPhone;
    private String addFriendMessage;
    private String mFriendId;
    private String mContactSyncName; //要添加的好友
    private String mInputSyncName; //输入
    private String mSelfSyncName; //当前用户

    private Friend mFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setTitle((R.string.search_friend));

        mAction = new BaojiaAction(this);
        mSelfSyncName = getSharedPreferences("config", MODE_PRIVATE).
                getString(SealConst.BAOJIA_USER_SYNCNAME, "");

        mEtSearch = (EditText) findViewById(R.id.search_edit);
        searchItem = (LinearLayout) findViewById(R
                .id.search_result);
        searchName = (TextView) findViewById(R.id.search_name);
        searchImage = (SelectableRoundedImageView) findViewById(R.id.search_header);
        mBtnSearch = findViewById(R.id.btn_contact_search);

        mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() == 11) {
//                    mPhone = s.toString().trim();
//                    if (!AMUtils.isMobile(mPhone)) {
//                        NToast.shortToast(mContext, "非法手机号");
//                        return;
//                    }
//                    hintKbTwo();
//                    LoadDialog.show(mContext);
//                    request(SEARCH_CONTACT, true);
//                } else {
//                    searchItem.setVisibility(View.GONE);
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputSyncName = mEtSearch.getText().toString().trim();
                if (TextUtils.isEmpty(mInputSyncName)){
                    NToast.shortToast(SearchFriendActivity.this, R.string.baojia_search_empty_tip);
                    return;
                }
                LoadDialog.show(mContext);
                request(SEARCH_CONTACT, true);
            }
        });
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case SEARCH_CONTACT:
                return mAction.searchContact(mInputSyncName);
            case ADD_FRIEND:
                return mAction.addFriend(mSelfSyncName, mContactSyncName);
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case SEARCH_CONTACT:
                    final SearchContactResponse searchResponse = (SearchContactResponse) result;
                    if (searchResponse.getCode() == 100000) {
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, "success");
                        mContactSyncName = searchResponse.getData().getSyncName();
                        searchItem.setVisibility(View.VISIBLE);
                        String portraitUri = null;
                        if (searchResponse.getData() != null) {
                            SearchContactResponse.ResultEntity contactInfo = searchResponse.getData();
                            UserInfo userInfo = new UserInfo(String.valueOf(contactInfo.getId()),
                                    contactInfo.getUserName(),
                                    Uri.parse(""));
                            portraitUri = SealUserInfoManager.getInstance().getPortraitUri(userInfo);
                        }
                        ImageLoader.getInstance().displayImage(portraitUri, searchImage, App.getOptions());
                        searchName.setText(searchResponse.getData().getSyncName());
                        searchItem.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (isFriendOrSelf(mContactSyncName)) {
                                    Intent intent = new Intent(SearchFriendActivity.this, UserDetailActivity.class);
                                    intent.putExtra("friend", mFriend);
                                    intent.putExtra("type", CLICK_CONVERSATION_USER_PORTRAIT);
                                    startActivity(intent);
                                    SealAppContext.getInstance().pushActivity(SearchFriendActivity.this);
                                    return;
                                }

                                DialogWithYesOrNoUtils.getInstance().showEditDialog(mContext, getString(R.string.add_text), getString(R.string.add_friend), new DialogWithYesOrNoUtils.DialogCallBack() {
                                    @Override
                                    public void executeEvent() {

                                    }

                                    @Override
                                    public void updatePassword(String oldPassword, String newPassword) {

                                    }

                                    @Override
                                    public void executeEditEvent(String editText) {
                                        if (!CommonUtils.isNetworkConnected(mContext)) {
                                            NToast.shortToast(mContext, R.string.network_not_available);
                                            return;
                                        }
                                        addFriendMessage = editText;
                                        if (TextUtils.isEmpty(editText)) {
                                            addFriendMessage = "我是" + getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_NAME, "");
                                        }
                                        if (!TextUtils.isEmpty(mContactSyncName)) {
                                            LoadDialog.show(mContext);
                                            request(ADD_FRIEND);
                                        } else {
                                            NToast.shortToast(mContext, "id is null");
                                        }
                                    }
                                });
                            }
                        });
                    }else {
                        NToast.shortToast(mContext, searchResponse.getMessage());
                        LoadDialog.dismiss(mContext);
                    }
                    break;
                case ADD_FRIEND:
                    getAddFriendResponse response = (getAddFriendResponse) result;
                    if (response.getCode() == 100000) {
                        NToast.shortToast(mContext, getString(R.string.request_success));
                        LoadDialog.dismiss(mContext);
                    } else {
                        RLog.w("SearchFriendActivity", "请求失败 错误码:" + response.getCode());
                        NToast.shortToast(mContext, response.getMessage());
                        LoadDialog.dismiss(mContext);
                    }
                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case ADD_FRIEND:
                NToast.shortToast(mContext, "你们已经是好友");
                LoadDialog.dismiss(mContext);
                break;
            case SEARCH_CONTACT:
                if (state == AsyncTaskManager.HTTP_ERROR_CODE || state == AsyncTaskManager.HTTP_NULL_CODE) {
                    super.onFailure(requestCode, state, result);
                } else {
                    NToast.shortToast(mContext, "用户不存在");
                }
                LoadDialog.dismiss(mContext);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        hintKbTwo();
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void hintKbTwo() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    private boolean isFriendOrSelf(String syncName){
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        if (mSelfSyncName.equals(syncName)){
            mFriend = new Friend(mSelfSyncName,
                        sp.getString(SealConst.SEALTALK_LOGIN_NAME, ""),
                        Uri.parse(sp.getString(SealConst.SEALTALK_LOGING_PORTRAIT, "")));
            //是自己
            return true;
        }else {
            // TODO: 2018/5/9 判断是否已是朋友
        }

        return false;
    }
}
