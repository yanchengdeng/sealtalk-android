package cn.rongcloud.im.ui.activity;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dbcapp.club.R;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import cn.rongcloud.im.App;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.server.BaojiaAction;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GetQiNiuTokenResponse;
import cn.rongcloud.im.server.response.ModifyNameResponse;
import cn.rongcloud.im.server.response.ModifyPortraitResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.photo.PhotoUtils;
import cn.rongcloud.im.server.widget.BottomMenuDialog;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.UpLoadImgManager;
import io.rong.common.RLog;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.UserInfo;


public class MyAccountActivity extends BaseActivity implements View.OnClickListener {

    private static final int UP_LOAD_PORTRAIT = 8;
    private static final int GET_QI_NIU_TOKEN = 128;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private SelectableRoundedImageView mImageView;
    private TextView mName;
    private PhotoUtils photoUtils;
    private BottomMenuDialog dialog;
    private Uri selectUri;
    private String mSyncName;
    private String mImageUrl;

    private UpLoadImgManager mUpLoadImgManager;
    private UploadManager mUploadManager;
    private BaojiaAction mAction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myaccount);
        setTitle(R.string.de_actionbar_myacc);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        mAction = new BaojiaAction(this);
        mSyncName = sp.getString(SealConst.BAOJIA_USER_SYNCNAME, "");
        editor = sp.edit();
        initView();
    }

    private void initView() {
        TextView mPhone = (TextView) findViewById(R.id.tv_my_phone);
        RelativeLayout portraitItem = (RelativeLayout) findViewById(R.id.rl_my_portrait);
        RelativeLayout nameItem = (RelativeLayout) findViewById(R.id.rl_my_username);
        mImageView = (SelectableRoundedImageView) findViewById(R.id.img_my_portrait);
        mName = (TextView) findViewById(R.id.tv_my_username);
        portraitItem.setOnClickListener(this);
        nameItem.setOnClickListener(this);
        String cacheName = sp.getString(SealConst.SEALTALK_LOGIN_NAME, "");
        String cachePortrait = sp.getString(SealConst.SEALTALK_LOGING_PORTRAIT, "");
        String cachePhone = sp.getString(SealConst.SEALTALK_LOGING_PHONE, "");
        if (!TextUtils.isEmpty(cachePhone)) {
            mPhone.setText("+86 " + cachePhone);
        }
        if (!TextUtils.isEmpty(cacheName)) {
            mName.setText(cacheName);
            String cacheId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "a");
            String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(new UserInfo(
                    cacheId, cacheName, Uri.parse(cachePortrait)));
            ImageLoader.getInstance().displayImage(portraitUri, mImageView, App.getOptions());
        }
        setPortraitChangeListener();
        BroadcastManager.getInstance(mContext).addAction(SealConst.CHANGEINFO, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mName.setText(sp.getString(SealConst.SEALTALK_LOGIN_NAME, ""));
            }
        });
    }

    private void setPortraitChangeListener() {
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri) {
                if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                    selectUri = uri;
                    LoadDialog.show(mContext);
                    if (mUpLoadImgManager == null){
                        mUpLoadImgManager = new UpLoadImgManager();
                    }
                    request(GET_QI_NIU_TOKEN);
                }
            }

            @Override
            public void onPhotoCancel() {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_my_portrait:
                showPhotoDialog();
                break;
            case R.id.rl_my_username:
                startActivity(new Intent(this, UpdateNameActivity.class));
                break;
        }
    }


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case UP_LOAD_PORTRAIT:
                return mAction.modifyPortrait(mSyncName, mImageUrl);
            case GET_QI_NIU_TOKEN:
                return mAction.getQiNiuToken(GetQiNiuTokenResponse.PORTRAIT_TYPE, mSyncName);
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case UP_LOAD_PORTRAIT:
                    ModifyPortraitResponse response = (ModifyPortraitResponse) result;
                    if (response.getCode() == 100000) {
                        editor.putString(SealConst.SEALTALK_LOGING_PORTRAIT, response.getData());
                        editor.commit();
                        ImageLoader.getInstance().displayImage(response.getData(), mImageView, App.getOptions());
                        if (RongIM.getInstance() != null) {
                            RongIM.getInstance().setCurrentUserInfo(
                                    new UserInfo(sp.getString(SealConst.SEALTALK_LOGIN_ID, ""),
                                            sp.getString(SealConst.SEALTALK_LOGIN_NAME, ""),
                                            Uri.parse(response.getData())));
                        }
                        BroadcastManager.getInstance(mContext).sendBroadcast(SealConst.CHANGEINFO);
                        NToast.shortToast(mContext, getString(R.string.portrait_update_success));
                    }else {
                        NToast.shortToast(this, response.getMessage());
                    }
                    LoadDialog.dismiss(mContext);
                    break;
                case GET_QI_NIU_TOKEN:
                    GetQiNiuTokenResponse qiNiuTokenResponse = (GetQiNiuTokenResponse) result;
                    if (qiNiuTokenResponse.getCode() == 100000){
                        uploadImage(qiNiuTokenResponse.getData().getToken(), selectUri);
                    }
                    break;
            }
        }
    }


    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case GET_QI_NIU_TOKEN:
            case UP_LOAD_PORTRAIT:
                NToast.shortToast(mContext, "设置头像请求失败");
                LoadDialog.dismiss(mContext);
                break;
        }
    }

    static public final int REQUEST_CODE_ASK_PERMISSIONS = 101;

    /**
     * 弹出底部框
     */
    @TargetApi(23)
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
                if (Build.VERSION.SDK_INT >= 23) {

                    int checkPermission = checkSelfPermission(Manifest.permission.CAMERA);
                    if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_ASK_PERMISSIONS);
                        } else {
                            new AlertDialog.Builder(mContext)
                                    .setMessage("您需要在设置里打开相机权限。")
                                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_ASK_PERMISSIONS);
                                        }
                                    })
                                    .setNegativeButton("取消", null)
                                    .create().show();
                        }
                        return;
                    }
                }
                photoUtils.takePicture(MyAccountActivity.this);
            }
        });
        dialog.setMiddleListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                photoUtils.selectPicture(MyAccountActivity.this);
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
                photoUtils.onActivityResult(MyAccountActivity.this, requestCode, resultCode, data);
                break;
        }
    }

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
                        if (!TextUtils.isEmpty(mImageUrl)) {
                            request(UP_LOAD_PORTRAIT);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    NToast.shortToast(mContext, getString(R.string.upload_portrait_failed));
                    LoadDialog.dismiss(mContext);
                }
            }
        }, null);
    }
}
