package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dbcapp.club.R;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.CompleteInfoResponse;
import cn.rongcloud.im.server.utils.CommonUtils;
import cn.rongcloud.im.server.utils.NLog;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.widget.LoadDialog;
import io.rong.common.RLog;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;

/**
 * Created by star1209 on 2018/5/8.
 */

public class CompleteInfoActivity extends BaseActivity implements View.OnClickListener {

    private static final String REG = "^[a-zA-Z0-9]{8,15}$";
    private static final int COMPLETE_INFO = 2;

    private static final String TEMP_PSW = "syncname"; //todo 暂时固定密码

    private EditText mEtName;
    private EditText mEtPhone;
    private Button mBtnComplete;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private String mNickName;
//    private String mPhoneNumber;
    private String connectResultId;
    private String mSyncName;
    private CompleteInfoResponse.ResultEntity mUserData;
    private String mLoginName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_info);

        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();
//        mSyncName = sp.getString(SealConst.BAOJIA_USER_SYNCNAME, "");
        mLoginName = sp.getString(SealConst.BAOJIA_LOGIN_NAME, "");
        setTitle(R.string.baojia_complete_title, false);
        initView();
    }

    private void initView() {
        mBtnComplete = findViewById(R.id.btn_complete);
        mEtName = findViewById(R.id.et_complete_name);
        mEtPhone = findViewById(R.id.et_complete_phone);

        mBtnComplete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_complete: //完成
                completeInfo();
                break;
            default:
                break;
        }
    }

    private void completeInfo() {
        mNickName = mEtName.getText().toString().trim();
        mSyncName = mEtPhone.getText().toString().trim();

        if (TextUtils.isEmpty(mNickName)){
            NToast.shortToast(this, "昵称不能为空！");
            return;
        }

        if (TextUtils.isEmpty(mSyncName)){
            NToast.shortToast(this, "所填ID不能为空！");
            return;
        }

        if (!mSyncName.matches(REG)){
            NToast.shortToast(this, R.string.baojia_input_id_hint);
            return;
        }

        LoadDialog.show(this);
        request(COMPLETE_INFO, true);
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode){
            case COMPLETE_INFO:
                return mAction.completeInfo(mLoginName, mSyncName, mNickName);
            default:
                break;
        }

        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode){
            case COMPLETE_INFO:
                LoadDialog.dismiss(this);
                CompleteInfoResponse response = (CompleteInfoResponse) result;
                RLog.v("CompleteInfoActivity", "code:" + response.getCode());
                if (response.getCode() == 100000){
                    mUserData = response.getData();
                    if (mUserData == null){
                        return;
                    }

                    RongIM.connect(mUserData.getImToken(), new RongIMClient.ConnectCallback() {
                        @Override
                        public void onTokenIncorrect() {
                            NLog.e("connect", "onTokenIncorrect");
                        }

                        @Override
                        public void onSuccess(String s) {
                            connectResultId = s;
                            NLog.e("connect", "onSuccess userid:" + s);
                            editor.putString(SealConst.SEALTALK_LOGIN_ID, s);
                            editor.commit();
                            SealUserInfoManager.getInstance().openDB();
                            setUserInfo();
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {
                            NLog.e("connect", "onError errorcode:" + errorCode.getValue());
                        }
                    });
                }else {
                    NToast.shortToast(this, response.getMessage());
                }
                break;
            default:
                break;
        }
    }

    private void setUserInfo() {
        //todo 头像暂不处理
        if (TextUtils.isEmpty(mUserData.getPortrait())) {
            mUserData.setPortrait(RongGenerate.generateDefaultAvatar(mUserData.getUserName(), mUserData.getSyncName()));
        }
        String nickName = mUserData.getUserName();
        String portraitUri = mUserData.getPortrait();
        editor.putString(SealConst.BAOJIA_USER_SYNCNAME, mUserData.getSyncName());
        editor.putString(SealConst.SEALTALK_LOGIN_NAME, nickName);
        editor.putString(SealConst.SEALTALK_LOGING_PORTRAIT, portraitUri);
        editor.putString("loginToken", mUserData.getImToken());
        editor.commit();
        RongIM.getInstance().refreshUserInfoCache(new UserInfo(connectResultId, mUserData.getUserName(), Uri.parse(mUserData.getPortrait())));
        //不继续在login界面同步好友,群组,群组成员信息
        SealUserInfoManager.getInstance().getAllUserInfo();
        gotoMain();
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        if (!CommonUtils.isNetworkConnected(mContext)) {
            LoadDialog.dismiss(mContext);
            NToast.shortToast(mContext, getString(R.string.network_not_available));
            return;
        }else {
            NToast.shortToast(mContext, "code: " + state);
        }
    }

    private void gotoMain() {
//        editor.putString(SealConst.SEALTALK_LOGING_PHONE, mPhoneNumber);
        editor.putString(SealConst.SEALTALK_LOGING_PASSWORD, TEMP_PSW);
        editor.commit();
        LoadDialog.dismiss(this);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
