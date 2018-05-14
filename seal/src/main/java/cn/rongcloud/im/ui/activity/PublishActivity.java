package cn.rongcloud.im.ui.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.dbcapp.club.R;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.PublishCircleResponse;
import cn.rongcloud.im.server.utils.CommonUtils;
import cn.rongcloud.im.server.utils.NLog;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.photo.PhotoUtils;
import cn.rongcloud.im.server.widget.BottomMenuDialog;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.utils.UpLoadImgManager;

/**
 * Created by star1209 on 2018/5/14.
 */

public class PublishActivity extends BaseActivity implements View.OnClickListener {

    public static final int REQUEST_CODE_ASK_PERMISSIONS = 101;

    private static final int PUBLISH_CIRCLE = 88;

    private BottomMenuDialog dialog;
    private PhotoUtils photoUtils;
    private UpLoadImgManager mUpLoadImgManager;
    private Button mBtnSelect;
    private ImageView mIvImage;
    private EditText mEtContent;

    private String mUploadPath;
    private String mTakePath;
    private UpLoadImgManager mUploadManager;
    private SharedPreferences mSp;
    private List<File> mFiles = new ArrayList<>();
    private String mPublishContent;

    private List<Uri> mSelectUriList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        mUploadManager = new UpLoadImgManager();
        mSp = getSharedPreferences("config", MODE_PRIVATE);

        initView();
    }

    private void initView() {
        setTitle(R.string.bapjia_publish_title);
        mHeadRightText.setVisibility(View.VISIBLE);
        mBtnSelect = findViewById(R.id.btn_add_photo_publish);
        mIvImage = findViewById(R.id.iv_photo_publish);
        mEtContent = findViewById(R.id.et_content_publish);
        mHeadRightText.setText(R.string.baojia_publish_submit);

        mBtnSelect.setOnClickListener(this);
        mIvImage.setOnClickListener(this);
        mHeadRightText.setOnClickListener(this);
        setPortraitChangeListener();
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

                mTakePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + CommonUtils.dateToString("yyyyMMddHHmmss") + ".jpg";
                photoUtils.takePhoto(PublishActivity.this, mTakePath);
            }
        });
        dialog.setMiddleListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                photoUtils.selectPicture(PublishActivity.this);
            }
        });
        dialog.show();
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
        String syncName = mSp.getString(SealConst.BAOJIA_USER_SYNCNAME, "");
        Map<String, String> params = new HashMap<>();
        try {
            String content = URLEncoder.encode(mPublishContent, "utf-8");
            return mUploadManager.uploadForm(params, "fileArr", mFiles,
                    String.format("http://api.baojia.co/circle/publish?content=%s&username=%s", content, syncName));
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode){
            case PUBLISH_CIRCLE:
                PublishCircleResponse response = (PublishCircleResponse) result;
                LoadDialog.dismiss(mContext);
                if (response != null && response.getCode() == 100000){
                    NLog.i("PublishActivity","publish circle success!");
                    NToast.shortToast(this, "发布成功！");
                    BroadcastManager.getInstance(this).sendBroadcast(SealConst.BAOJIA_PUBLISH_CIRCLE);
                    finish();
                }else {
                    if (response != null){
                        NToast.shortToast(this, response.getMessage());
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
            case R.id.btn_add_photo_publish: //选择照片
            case R.id.iv_photo_publish: //选择照片
                showPhotoDialog();
                break;
            case R.id.text_right: //确定
                mPublishContent = mEtContent.getText().toString().trim();
                if (TextUtils.isEmpty(mPublishContent) && mFiles.size() == 0){
                    NToast.shortToast(this, "图片或内容不可为空");
                    return;
                }
                LoadDialog.show(mContext);
                request(PUBLISH_CIRCLE);
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
                    mBtnSelect.setVisibility(View.GONE);
                    mIvImage.setVisibility(View.VISIBLE);
                    displayImage(mTakePath);
                }
                break;
            case PhotoUtils.INTENT_SELECT:
                if (data != null && data.getData() != null){
                    Uri uri = data.getData();
                    mBtnSelect.setVisibility(View.GONE);
                    mIvImage.setVisibility(View.VISIBLE);
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
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            mIvImage.setImageBitmap(bitmap);
            mUploadPath = imagePath;
            mFiles.add(new File(imagePath));
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
}
