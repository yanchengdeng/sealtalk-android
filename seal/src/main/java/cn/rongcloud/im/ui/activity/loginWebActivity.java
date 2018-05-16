package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dbcapp.club.R;

import java.util.UUID;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GetLoginStatusResponse;
import cn.rongcloud.im.server.utils.MD5;
import cn.rongcloud.im.server.utils.NLog;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.RongGenerate;
import io.rong.common.RLog;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;

/**
 * Created by star1209 on 2018/5/7.
 */

public class loginWebActivity extends BaseActivity {

    private static final int LOOPER_INTERVAL = 1000 * 3;
    private static final int LOOPER_WHAT = 10;
    private static final int NOT_COMPLETE_STATUS = 1; //用户信息不完整，需补充信息

    private static final int LOOPER_REQUEST = 8;

    private SharedPreferences.Editor editor;

    private static final String SECRET_KEY = "***!@#!@#&*%jmanhelmirjuujasd89172!@#$$%%Aams0";
    private String mLoginUrl = "https://api.vip2u.co/checklogin.aspx?transid=%s&i=%s";

    private WebView mWebLogin;
    private SharedPreferences mSp;

    private String mTransId;
    private String connectResultId;
    private GetLoginStatusResponse.ResultEntiry mUserData;

    //用以轮训获取服务端返回登录状态
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            request(LOOPER_REQUEST, true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_web);
        mSp = getSharedPreferences("config", MODE_PRIVATE);
        editor = mSp.edit();

        mWebLogin = findViewById(R.id.web_login);
        initWebView();
        loadWebUrl();

        getLoginStatus();
    }

    private void initWebView() {
        mWebLogin.getSettings().setJavaScriptEnabled(true);
        mWebLogin.getSettings().setAppCacheEnabled(true);
        //设置 缓存模式
        mWebLogin.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        // 开启 DOM storage API 功能
        mWebLogin.getSettings().setDomStorageEnabled(true);
        mWebLogin.setWebViewClient(new WebViewClient() {
            //覆盖shouldOverrideUrlLoading 方法
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    /**
     * 轮询获取登录状态
     */
    private void getLoginStatus() {
        mHandler.sendEmptyMessageDelayed(LOOPER_WHAT, 3000);//延迟3秒开始轮询
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode){
            case LOOPER_REQUEST:
                return mAction.getLoginStatus(mTransId);
            default:
                break;
        }
        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode){
            case LOOPER_REQUEST:
                GetLoginStatusResponse response = (GetLoginStatusResponse) result;
                RLog.v("loginWebActivity", "code:" + response.getCode());
                if (response.getCode() == 100000){
                    //终止轮询
                    mHandler.removeMessages(LOOPER_WHAT);
                    //获取到登录状态，再判断是否信息完整
                    if (response.getData().getStatus() == NOT_COMPLETE_STATUS){
                        mUserData = response.getData();
                        editor.putString(SealConst.BAOJIA_USER_SYNCNAME, mUserData.getSyncName()).commit();
                        gotoComplete(((GetLoginStatusResponse) result).getData());
                    }else {
                        mUserData = response.getData();
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
                }else if (response.getCode() == 110002){
                    NToast.shortToast(this, response.getMessage());
                    loadWebUrl();
                    mHandler.sendEmptyMessageDelayed(LOOPER_WHAT, LOOPER_INTERVAL);
                }else {
                    //没有登录成功，继续轮询
                    mHandler.sendEmptyMessageDelayed(LOOPER_WHAT, LOOPER_INTERVAL);
                }
                break;
        }
    }

    private void loadWebUrl(){
        //获取transid并存储
        mTransId = UUID.randomUUID().toString();
        mSp.edit().putString("transid", mTransId);
        String url = String.format(mLoginUrl, mTransId, MD5.encrypt(mTransId + SECRET_KEY));
        RLog.v("loginWebActivity", url);
        mWebLogin.loadUrl(url);
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

    private void gotoComplete(GetLoginStatusResponse.ResultEntiry data) {
        Intent intent = new Intent(this, CompleteInfoActivity.class);
        intent.putExtra("user_info", data);
        startActivity(intent);
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        super.onFailure(requestCode, state, result);
        mHandler.sendEmptyMessageDelayed(LOOPER_WHAT, LOOPER_INTERVAL);
    }

    private void gotoMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
