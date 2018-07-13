package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dbcapp.club.R;

import cn.rongcloud.im.App;
import io.rong.imageloader.core.ImageLoader;

public class CircleUserDetailActivity extends BaseActivity {

    private String name, icon;
    private ImageView mUserPortrait;
    private TextView tvUserName;

    private View mLayoutImagebg;
    private ImageView mIvShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_user_detail);


        mUserPortrait =  findViewById(R.id.ac_iv_user_portrait);
        mIvShow = findViewById(R.id.iv_image_show);
        tvUserName = findViewById(R.id.contact_name);
        name = getIntent().getStringExtra("name");
        icon = getIntent().getStringExtra("icon");
        setTitle("详细资料");
        mLayoutImagebg = findViewById(R.id.fl_image_bg);

        ImageLoader.getInstance().displayImage(icon, mUserPortrait, App.getOptions());
        ImageLoader.getInstance().displayImage(icon, mIvShow, App.getOptions());

        if (!TextUtils.isEmpty(name)) {
            tvUserName.setText(name);
        }



        mUserPortrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutImagebg.setVisibility(View.VISIBLE);
            }
        });


        mIvShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutImagebg.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (mLayoutImagebg.getVisibility() == View.VISIBLE) {
            mLayoutImagebg.setVisibility(View.GONE);
            return;
        } else {
            super.onBackPressed();
        }
    }
}
