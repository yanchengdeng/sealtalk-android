package cn.rongcloud.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.dbcapp.club.R;

import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.ClearWriteEditText;

/**
 * Created by star1209 on 2018/5/16.
 */

public class UpdateDiscussionNameActivity extends BaseActivity implements View.OnClickListener {

    private ClearWriteEditText mNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_discuss_name);

        initView();
    }

    private void initView() {
        setTitle(R.string.baojia_update_discuss_name, false);

        mNameEditText = findViewById(R.id.update_name);
        mHeadRightText.setVisibility(View.VISIBLE);
        mHeadRightText.setText(getString(R.string.confirm));
        mHeadRightText.setOnClickListener(this);

        mNameEditText.setText(getIntent().getStringExtra("discussion_name"));
    }

    @Override
    public void onClick(View v) {
        String newName = mNameEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(newName)) {
            updateName(newName);
        } else {
            NToast.shortToast(mContext, "名字不能为空");
            mNameEditText.setShakeAnimation();
        }
    }

    private void updateName(String newName) {
        Intent intent = new Intent();
        intent.putExtra("discussion_name", newName);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
