package cn.rongcloud.im.ui.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.blankj.utilcode.util.LogUtils;
import com.dbcapp.club.R;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GetQiNiuTokenResponse;
import cn.rongcloud.im.server.response.PublishCircleResponse;
import cn.rongcloud.im.server.utils.NLog;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.photo.PhotoUtils;
import cn.rongcloud.im.server.widget.BottomMenuDialog;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.adapter.PublishImageAdapter;
import cn.rongcloud.im.ui.widget.linkpreview.GridItemDecoration;
import cn.rongcloud.im.utils.CommonUtils;
import cn.rongcloud.im.utils.PermissionUtils;
import cn.rongcloud.im.utils.UpLoadImgManager;

/**
 * Created by star1209 on 2018/5/14.
 */

public class PublishActivity extends BaseActivity implements View.OnClickListener {

    public static final int REQUEST_CODE_ASK_PERMISSIONS = 101;

    public static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PUBLISH_CIRCLE = 88;
    private static final int GET_QI_NIU_TOKEN = 102;

    private BottomMenuDialog dialog;
    private PhotoUtils photoUtils;
    private UpLoadImgManager mUpLoadImgManager;
    private EditText mEtContent;
    private RecyclerView mLvAddImage;
    private PublishImageAdapter mPublishImageAdapter;

    private String mUploadPath;
    private String mTakePath;
    private SharedPreferences mSp;
    private List<File> mFiles = new ArrayList<>();
    private String mPublishContent;
    private UploadManager mUploadManager;
    private String mSyncName;
    private String mToken;

    private List<Uri> mSelectUriList = new ArrayList<>();
    private List<String> mImageUrlList = new ArrayList<>();

    private int mIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        mSp = getSharedPreferences("config", MODE_PRIVATE);
        mSyncName = mSp.getString(SealConst.BAOJIA_USER_SYNCNAME, "");

        initView();
    }

    private void initView() {
        setTitle(R.string.bapjia_publish_title);
        mHeadRightText.setVisibility(View.VISIBLE);
        mEtContent = findViewById(R.id.et_content_publish);
        mLvAddImage = findViewById(R.id.lv_add_image);
        mHeadRightText.setText(R.string.baojia_publish_submit);

        mHeadRightText.setOnClickListener(this);
        setPortraitChangeListener();

        GridLayoutManager manager = new GridLayoutManager(this, 3);
        mLvAddImage.setLayoutManager(manager);
        mPublishImageAdapter = new PublishImageAdapter();
        mLvAddImage.setAdapter(mPublishImageAdapter);
        mPublishImageAdapter.setOnAddImageListener(new PublishImageAdapter.OnAddImageListener() {
            @Override
            public void onAddImage() {
                showPhotoDialog();
            }
        });

        mLvAddImage.addItemDecoration(new GridItemDecoration(3,
                getResources().getDimensionPixelOffset(R.dimen.baojia_publish_decoration),
                true));
    }

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

                PermissionUtils.requestPermissions(PublishActivity.this, REQUEST_CODE_ASK_PERMISSIONS, PERMISSIONS,
                        new PermissionUtils.OnPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                mTakePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + CommonUtils.dateToString("yyyyMMddHHmmss") + ".jpg";
                                photoUtils.takePhoto(PublishActivity.this, mTakePath);
                            }

                            @Override
                            public void onPermissionDenied(String[] deniedPermissions) {
                                NToast.shortToast(PublishActivity.this, "权限已被拒绝");
                            }
                        }, new PermissionUtils.RationaleHandler() {
                            @Override
                            protected void showRationale() {
                                NToast.shortToast(PublishActivity.this, "权限未打开");
                            }
                        });

            }
        });
        dialog.setMiddleListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }

                PermissionUtils.requestPermissions(PublishActivity.this, REQUEST_CODE_ASK_PERMISSIONS, PERMISSIONS,
                        new PermissionUtils.OnPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                photoUtils.selectPicture(PublishActivity.this);
                            }

                            @Override
                            public void onPermissionDenied(String[] deniedPermissions) {
                                NToast.shortToast(PublishActivity.this, "权限已被拒绝");
                            }
                        }, new PermissionUtils.RationaleHandler() {
                            @Override
                            protected void showRationale() {
                                NToast.shortToast(PublishActivity.this, "权限未打开");
                            }
                        });
            }
        });
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(this, REQUEST_CODE_ASK_PERMISSIONS, PERMISSIONS);
    }

    private void setPortraitChangeListener() {
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri) {
                if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                    mSelectUriList.add(uri);
                    LoadDialog.show(mContext);
                    if (mUpLoadImgManager == null){
                        mUpLoadImgManager = new UpLoadImgManager();
                    }
                }
            }

            @Override
            public void onPhotoCancel() {

            }
        });
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode){
            case GET_QI_NIU_TOKEN:
                return mAction.getQiNiuToken(GetQiNiuTokenResponse.CIRCLE_TYPE, mSyncName);
            case PUBLISH_CIRCLE:
                try {
                    String content = URLEncoder.encode(mPublishContent, "utf-8");
                    return mAction.publishCircle(content, mSyncName, mImageUrlList);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            default:
                return null;
        }
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode){
            case GET_QI_NIU_TOKEN:
                GetQiNiuTokenResponse qiNiuTokenResponse = (GetQiNiuTokenResponse) result;
                if (qiNiuTokenResponse.getCode() == 100000){
                    mToken = qiNiuTokenResponse.getData().getToken();
                    uploadImage(mToken, mFiles.get(mIndex));
                }else {
                    LoadDialog.dismiss(mContext);
                    NToast.shortToast(this, qiNiuTokenResponse.getMessage());
                }
                break;
            case PUBLISH_CIRCLE:
                PublishCircleResponse response = (PublishCircleResponse) result;
                LoadDialog.dismiss(mContext);
                if (response != null && response.getCode() == 100000){
                    NLog.i("PublishActivity","publish circle success!");
                    NToast.shortToast(this, "发布成功！");
                    BroadcastManager.getInstance(this).sendBroadcast(SealConst.BAOJIA_PUBLISH_CIRCLE);
                    mFiles.clear();
                    finish();
                }else {
                    if (response != null){
                        NToast.shortToast(this, response.getMessage());
                    }else {
                        NToast.shortToast(this, "发布失败！");
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        LoadDialog.dismiss(mContext);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_right: //确定
                mPublishContent = mEtContent.getText().toString().trim();
                if (TextUtils.isEmpty(mPublishContent) && mFiles.size() == 0){
                    NToast.shortToast(this, "图片或内容不可为空");
                    return;
                }

                LoadDialog.show(mContext);
                if (mFiles.size() == 0){
                    request(PUBLISH_CIRCLE);
                    return;
                }

                request(GET_QI_NIU_TOKEN);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PhotoUtils.INTENT_TAKE:
                if (!TextUtils.isEmpty(mTakePath)) {
                    displayImage(mTakePath);
                }
                break;
            case PhotoUtils.INTENT_SELECT:
                if (data != null && data.getData() != null){
                    Uri uri = data.getData();
                    handleImageOnKitkat(data.getData());
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleImageOnKitkat(Uri uri) {
        String imagePath = null;
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri
                    .getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri
                    .getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果不是document类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        }
        displayImage(imagePath);
    }

    private void displayImage(String imagePath) {
        File file = new File(imagePath);
        if (file!=null && file.length()>0) {
            mFiles.add(new File(imagePath));
            mPublishImageAdapter.addData(imagePath);
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null,
                null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    public void uploadImage(String imageToken, File imageFile) {

        if (this.mUploadManager == null) {
            this.mUploadManager = new UploadManager();
        }
        this.mUploadManager.put(imageFile, null, imageToken, new UpCompletionHandler() {

            @Override
            public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
                if (responseInfo.isOK()) {
                    try {
                        String key = (String) jsonObject.get("key");
                        String imageurl = "http://" + getString(R.string.baojia_qiniu_domain) + "/" + key;
                        Log.e("uploadImage", imageurl);
                        mImageUrlList.add(imageurl);
                        mIndex ++;
                        if (mImageUrlList.size() >= mFiles.size()){
                            request(PUBLISH_CIRCLE);
                        }else {
                            uploadImage(mToken, mFiles.get(mIndex));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        LoadDialog.dismiss(mContext);
                    }
                }else {
                    NToast.shortToast(mContext, getString(R.string.upload_portrait_failed));
//                    NToast.shortToast(mContext, "code:" + responseInfo.statusCode +
//                    "==" + "error" + responseInfo.error);
                    LoadDialog.dismiss(mContext);
                }
            }
        }, null);
    }
}
