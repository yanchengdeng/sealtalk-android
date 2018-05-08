package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jrmf360.rylib.common.util.ToastUtil;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.server.BaojiaAction;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.CompleteInfoResponse;
import cn.rongcloud.im.server.response.GetUserInfoByIdResponse;
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

    private static final int COMPLETE_INFO = 2;

    private static final String TEMP_PSW = "syncname"; //todo 暂时固定密码

    private BaojiaAction mAction;

    private EditText mEtName;
    private EditText mEtPhone;
    private Button mBtnComplete;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private String mNickName;
    private String mPhoneNumber;
    private String connectResultId;
    private CompleteInfoResponse.ResultEntity mUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_info);
        mAction = new BaojiaAction(this);

        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();
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
        mPhoneNumber = mEtPhone.getText().toString().trim();

        if (TextUtils.isEmpty(mNickName)){
            ToastUtil.showToast(this, "昵称不能为空！");
            return;
        }

        if (TextUtils.isEmpty(mPhoneNumber)){
            ToastUtil.showToast(this, "号码不能为空！");
            return;
        }

        LoadDialog.show(this);
        request(COMPLETE_INFO, true);
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode){
            case COMPLETE_INFO:
                return mAction.completeInfo(mNickName,  mPhoneNumber);
            default:
                break;
        }

        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode){
            case COMPLETE_INFO:
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
                }
                break;
            default:
                break;
        }
    }

    private void setUserInfo() {
        //todo 头像暂不处理
        GetUserInfoByIdResponse.ResultEntity userinfo = new GetUserInfoByIdResponse.ResultEntity();
        if (TextUtils.isEmpty(userinfo.getPortraitUri())) {
            userinfo.setPortraitUri(RongGenerate.generateDefaultAvatar(mUserData.getUserName(), mUserData.getId() + ""));
        }
        String nickName = userinfo.getNickname();
        String portraitUri = userinfo.getPortraitUri();
        editor.putString(SealConst.SEALTALK_LOGIN_NAME, nickName);
        editor.putString(SealConst.SEALTALK_LOGING_PORTRAIT, portraitUri);
        editor.commit();
        RongIM.getInstance().refreshUserInfoCache(new UserInfo(connectResultId, mUserData.getUserName(), Uri.parse(userinfo.getPortraitUri())));
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
        }
    }

    private void gotoMain() {
        editor.putString(SealConst.SEALTALK_LOGING_PHONE, mPhoneNumber);
        editor.putString(SealConst.SEALTALK_LOGING_PASSWORD, TEMP_PSW);
        editor.commit();
        LoadDialog.dismiss(this);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
