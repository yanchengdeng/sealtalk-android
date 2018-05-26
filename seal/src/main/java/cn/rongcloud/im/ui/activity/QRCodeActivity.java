package cn.rongcloud.im.ui.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.dbcapp.club.R;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.utils.QRUtils;

/**
 * Created by star1209 on 2018/5/6.
 */

public class QRCodeActivity extends BaseActivity {

    private ImageView mIvQrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);
        setHeadRightButtonVisibility(View.GONE);
        setTitle(R.string.baojia_qrcode_title, false);

        mIvQrCode = findViewById(R.id.iv_qrcode);
        setQRCode();
    }

    private void setQRCode() {
        int width = getResources().getDisplayMetrics().widthPixels - 120;
        int height = width;
        String username = getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_NAME, "");
        Bitmap qrCodeBitmap = QRUtils.createQRImage(username, width, height);
        mIvQrCode.setImageBitmap(qrCodeBitmap);
    }
}
