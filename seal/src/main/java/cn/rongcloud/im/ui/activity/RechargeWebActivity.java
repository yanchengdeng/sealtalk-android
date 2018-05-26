package cn.rongcloud.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dbcapp.club.R;

import java.util.UUID;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GetRechargeStatusResponse;
import cn.rongcloud.im.server.utils.MD5;
import cn.rongcloud.im.server.utils.NToast;
import io.rong.common.RLog;

/**
 * Created by star1209 on 2018/5/16.
 */

public class RechargeWebActivity extends BaseActivity {

    private static final int LOOPER_WHAT = 99;
    private static final int REQUEST_STATUS = 99;
    private static final int LOOPER_INTERVAL = 1000 * 3;

    private String mUrl = "https://api.vip2u.co/checkepin.aspx?username=%s&transid=%s&i=%s";
    private String mTransId;
    private String mSyncName;
    private SharedPreferences mSp;

    private WebView mWebView;
    private double mRechargeAmmount;
    private String mLoginName;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            request(REQUEST_STATUS, true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_recharge);

        mSp = getSharedPreferences("config", MODE_PRIVATE);
        mSyncName = mSp.getString(SealConst.BAOJIA_USER_SYNCNAME, "");
        mLoginName = getIntent().getStringExtra("login_name");
        mTransId = UUID.randomUUID().toString();
        mRechargeAmmount = getIntent().getDoubleExtra("recharge_ammount", 0);
        initView();
    }

    private void initView() {
        setTitle(R.string.baojia_recharge_web_title);

        mWebView = findViewById(R.id.web_recharge);
        initWebView();

        loadWebUrl();
        getStatus();
    }

    private void loadWebUrl(){
        //获取transid并存储
        mTransId = UUID.randomUUID().toString();
        String url = String.format(mUrl, mLoginName, mTransId,
                MD5.encrypt(mLoginName + mTransId + SealConst.BAOJIA_SECRET).toLowerCase());
        RLog.v("RechargeWebActivity", url);
        mWebView.loadUrl(url);
    }

    private void initWebView() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAppCacheEnabled(true);
        //设置 缓存模式
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        // 开启 DOM storage API 功能
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
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
    private void getStatus() {
        mHandler.sendEmptyMessageDelayed(LOOPER_WHAT, 3000);//延迟3秒开始轮询
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode){
            case REQUEST_STATUS:
                return mAction.getRechargeStatus(mRechargeAmmount, mTransId, mSyncName);
            default:
                return null;
        }
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode){
            case REQUEST_STATUS:
                GetRechargeStatusResponse response = (GetRechargeStatusResponse) result;
                if (response.getCode() == 100000){
                    mHandler.removeMessages(LOOPER_WHAT);
                    Intent intent = new Intent();
                    intent.putExtra("wallet_remain", response.getData());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }else {
                    mHandler.sendEmptyMessageDelayed(LOOPER_WHAT, LOOPER_INTERVAL);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        super.onFailure(requestCode, state, result);
        mHandler.sendEmptyMessageDelayed(LOOPER_WHAT, LOOPER_INTERVAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(LOOPER_WHAT);
    }
}
