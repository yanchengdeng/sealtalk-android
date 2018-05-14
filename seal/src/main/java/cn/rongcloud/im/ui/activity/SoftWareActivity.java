package cn.rongcloud.im.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dbcapp.club.R;

import cn.rongcloud.im.SealConst;

/**
 * Created by star1209 on 2018/5/9.
 */

public class SoftWareActivity extends BaseActivity {

    private static final String SOFTWARE_URL = "http://xcx.baojia.co/index.html?username=%s";

    private WebView mWebSofrware;

    private SharedPreferences mSp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baojia_software);
        setTitle(getString(R.string.baojia_software_title));
        mSp = getSharedPreferences("config", MODE_PRIVATE);
        String syncName = mSp.getString(SealConst.BAOJIA_USER_SYNCNAME, "");

        mWebSofrware = findViewById(R.id.web_software);
        initWebView();
        mWebSofrware.loadUrl(String.format(SOFTWARE_URL, syncName));
    }

    private void initWebView() {
        mWebSofrware.getSettings().setJavaScriptEnabled(true);
        mWebSofrware.getSettings().setAppCacheEnabled(true);
        //设置 缓存模式
        mWebSofrware.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        // 开启 DOM storage API 功能
        mWebSofrware.getSettings().setDomStorageEnabled(true);
        mWebSofrware.setWebViewClient(new WebViewClient() {
            //覆盖shouldOverrideUrlLoading 方法
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }
}
