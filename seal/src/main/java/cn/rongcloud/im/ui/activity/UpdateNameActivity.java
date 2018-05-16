package cn.rongcloud.im.ui.activity;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.dbcapp.club.R;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.ModifyNameResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.ClearWriteEditText;
import cn.rongcloud.im.server.widget.LoadDialog;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.UserInfo;

/**
 * Created by AMing on 16/6/23.
 * Company RongCloud
 */
public class UpdateNameActivity extends BaseActivity implements View.OnClickListener {

    private static final int UPDATE_NAME = 7;

    private ClearWriteEditText mNameEditText;
    private String newName;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String mSyncName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_name);
        setTitle(getString(R.string.update_name));
        Button rightButton = getHeadRightButton();
        rightButton.setVisibility(View.GONE);
        mHeadRightText.setVisibility(View.VISIBLE);
        mHeadRightText.setText(getString(R.string.confirm));
        mHeadRightText.setOnClickListener(this);
        mNameEditText = (ClearWriteEditText) findViewById(R.id.update_name);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        mNameEditText.setText(sp.getString(SealConst.SEALTALK_LOGIN_NAME, ""));
        mNameEditText.setSelection(sp.getString(SealConst.SEALTALK_LOGIN_NAME, "").length());
        editor = sp.edit();
        mSyncName = sp.getString(SealConst.BAOJIA_USER_SYNCNAME, "");
    }


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        return mAction.modifyName(mSyncName, newName);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        ModifyNameResponse response = (ModifyNameResponse) result;
        if (response.getCode() == 100000) {
            editor.putString(SealConst.SEALTALK_LOGIN_NAME, newName);
            editor.commit();

            BroadcastManager.getInstance(mContext).sendBroadcast(SealConst.CHANGEINFO);

            RongIM.getInstance().refreshUserInfoCache(new UserInfo(sp.getString(SealConst.SEALTALK_LOGIN_ID, ""), newName, Uri.parse(sp.getString(SealConst.SEALTALK_LOGING_PORTRAIT, ""))));
            RongIM.getInstance().setCurrentUserInfo(new UserInfo(sp.getString(SealConst.SEALTALK_LOGIN_ID, ""), newName, Uri.parse(sp.getString(SealConst.SEALTALK_LOGING_PORTRAIT, ""))));

            NToast.shortToast(mContext, "昵称更改成功");
            finish();
        }else {
            NToast.shortToast(mContext, response.getMessage());
        }
        LoadDialog.dismiss(mContext);
    }

    @Override
    public void onClick(View v) {
        newName = mNameEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(newName)) {
            LoadDialog.show(mContext);
            request(UPDATE_NAME, true);
        } else {
            NToast.shortToast(mContext, "昵称不能为空");
            mNameEditText.setShakeAnimation();
        }
    }
}
