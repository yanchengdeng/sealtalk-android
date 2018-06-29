package cn.rongcloud.im.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.dbcapp.club.R;

import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.server.BaojiaAction;
import cn.rongcloud.im.server.network.async.AsyncTaskManager;
import cn.rongcloud.im.server.network.async.OnDataListener;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.AdResponse;
import cn.rongcloud.im.utils.PerfectClickListener;
import cn.rongcloud.im.utils.PermissionUtils;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;

/**
 * Created by AMing on 16/8/5.
 * Company RongCloud
 */
public class SplashActivity extends Activity implements OnDataListener {

    private Context context;
    private android.os.Handler handler = new android.os.Handler();
    static public final int REQUEST_CODE_ASK_PERMISSIONS = 101;
    private ImageView ivAd;
    public static final String CURRENT_AD_URL = "curren_ad_url";

    public static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private AsyncTaskManager mAsyncTaskManager;

    private static final int GET_AD = 30;
    private BaojiaAction mAction;
    private CountDownTimer countDownTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        context = this;
        mAsyncTaskManager = AsyncTaskManager.getInstance(getApplicationContext());
        mAction = new BaojiaAction(this);
        ivAd = findViewById(R.id.iv_ad);

        countDownTimer = new CountDownTimer(1000, 3500) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
                String cacheToken = sp.getString("loginToken", "");
                if (!TextUtils.isEmpty(cacheToken)) {
                    RongIM.connect(cacheToken, SealAppContext.getInstance().getConnectCallback());
                    goToMain(true);
                } else {
                    goToLogin(true);
                }
            }
        };

        findViewById(R.id.tv_jump).setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                if (countDownTimer!=null){
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
                SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
                String cacheToken = sp.getString("loginToken", "");
                mAction.httpManager.cancelRequests(SplashActivity.this, true);
                if (!TextUtils.isEmpty(cacheToken)) {
                    RongIM.connect(cacheToken, SealAppContext.getInstance().getConnectCallback());
                    goToMain(true);
                } else {
                    goToLogin(true);
                }
            }
        });


        if (!TextUtils.isEmpty(SPUtils.getInstance().getString(CURRENT_AD_URL, ""))) {
            ImageLoader.getInstance().displayImage(SPUtils.getInstance().getString(CURRENT_AD_URL, ""), ivAd);
        }


        doRequestQeuston();
    }

    private void doRequestQeuston() {
        PermissionUtils.requestPermissions(SplashActivity.this, 101, PERMISSIONS,
                new PermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {

                        LogUtils.w("dyc", "onPermissionGranted");
                        request(GET_AD);
                    }

                    @Override
                    public void onPermissionDenied(String[] deniedPermissions) {
                        LogUtils.w("dyc", "onPermissionDenied");
                    }


                }, new PermissionUtils.RationaleHandler() {
                    @Override
                    protected void showRationale() {
                        LogUtils.w("dyc", "showRationale");
                        requestPermissionsAgain();
                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults != null) {
            boolean isGrant = true;
            for (Integer grantR : grantResults) {
                if (grantR == -1) {
                    isGrant = false;
                }
            }

//            if (!isGrant){
//                doRequestQeuston();
//            }else{
            request(GET_AD);
//            }
        }
//        setData();


    }


    public void request(int requestCode) {
        if (mAsyncTaskManager != null) {
            mAsyncTaskManager.request(requestCode, this);
        }
    }

    private void setData() {
        countDownTimer.start();

    }


    private void goToMain(boolean flag) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("flag", flag);
        startActivity(intent);
        finish();
    }

    private void goToLogin(boolean flag) {
        Intent intent = new Intent(context, loginWebActivity.class);
        intent.putExtra("flag", flag);
        startActivity(intent);
        finish();
    }

    private boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    @Override
    public Object doInBackground(int requestCode, String parameter) throws HttpException {
        switch (requestCode) {
            case GET_AD:
                return mAction.getLoadingAd();
        }
        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {

        switch (requestCode) {
            case GET_AD:
                AdResponse adResponse = (AdResponse) result;
                if (adResponse.getCode() == 100000) {
                    if (adResponse.getData() != null) {
                        if (!TextUtils.isEmpty(adResponse.getData().getImagesPath())) {
                            SPUtils.getInstance().put(CURRENT_AD_URL, adResponse.getData().getImagesPath());
                        }
                    }
                }
                setData();
                break;
        }

    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        if (!TextUtils.isEmpty(SPUtils.getInstance().getString(CURRENT_AD_URL, ""))) {
            ImageLoader.getInstance().displayImage(SPUtils.getInstance().getString(CURRENT_AD_URL, ""), ivAd);
        }
        setData();

    }


    @Override
    protected void onDestroy() {
        if (countDownTimer!=null){
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}
