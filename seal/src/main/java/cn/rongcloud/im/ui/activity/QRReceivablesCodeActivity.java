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

/**
 * Created by star1209 on 2018/5/6.
 */

public class QRReceivablesCodeActivity extends BaseActivity {

    private ImageView mIvQrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);
        setHeadRightButtonVisibility(View.GONE);
        setTitle(R.string.baojia_qrcode_receivables_title, false);

        mIvQrCode = findViewById(R.id.iv_qrcode);
        setQRCode();
    }

    private void setQRCode() {
        QRCodeBean bean = new QRCodeBean();
        bean.setType(1);
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
