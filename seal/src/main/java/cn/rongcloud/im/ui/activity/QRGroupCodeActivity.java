package cn.rongcloud.im.ui.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.dbcapp.club.R;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.model.QRCodeBean;
import cn.rongcloud.im.server.utils.QRUtils;
import cn.rongcloud.im.utils.JsonUtils;

public class QRGroupCodeActivity extends BaseActivity {


    private ImageView mIvQrCode;
    private String fromConversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrgroup_code);


        fromConversationId = getIntent().getStringExtra("TargetId");
        setHeadRightButtonVisibility(View.GONE);
        setTitle(R.string.baojia_qrcode_group, false);

        mIvQrCode = findViewById(R.id.iv_qrcode);
        setQRCode();
    }

    /**
     * {"groupId":"9815d0783b804429a5c8be3fc094ce8a","type":2,"userId":"Sms413"}
     */
    private void setQRCode() {
        QRCodeBean bean = new QRCodeBean();
        bean.setType(2);
        bean.setGroupId(fromConversationId);
        int width = getResources().getDisplayMetrics().widthPixels - 120;
        int height = width;
        String username = getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_NAME, "");
        String userId = getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_ID, "");
        bean.setUserId(userId);
        String json = JsonUtils.toJson(bean);
        Bitmap qrCodeBitmap = QRUtils.createQRImage("baojia:" + json, width, height);
        mIvQrCode.setImageBitmap(qrCodeBitmap);
    }
}
