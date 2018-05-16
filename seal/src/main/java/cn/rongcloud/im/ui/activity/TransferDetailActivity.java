package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.dbcapp.club.R;

import cn.rongcloud.im.App;
import cn.rongcloud.im.utils.CommonUtils;
import io.rong.imageloader.core.ImageLoader;

/**
 * Created by star1209 on 2018/5/15.
 */

public class TransferDetailActivity extends BaseActivity {

    private ImageView mIvPortrait;
    private TextView mTvSender;
    private TextView mTvLeave;
    private TextView mTvAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_detail);

        initView();
        initData();
    }

    private void initData() {
        ImageLoader.getInstance().displayImage(getIntent().getStringExtra("portrait"), mIvPortrait, App.getOptions());
        mTvLeave.setText(getIntent().getStringExtra("transfer_leave"));
        mTvAmount.setText(String.format(getString(R.string.baojia_transfer_amount),
                CommonUtils.twoDecimalFormat(getIntent().getDoubleExtra("amount", 0))));
        mTvSender.setText(getIntent().getStringExtra("username"));
    }

    private void initView() {
        setTitle(R.string.baojia_transfer_detail_title);

        mIvPortrait = findViewById(R.id.iv_portrait_transfer_detail);
        mTvSender = findViewById(R.id.tv_sender_transfer_detail);
        mTvLeave = findViewById(R.id.tv_leave_transfer_detail);
        mTvAmount = findViewById(R.id.tv_amount_transfer_detail);
    }
}
