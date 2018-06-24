package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dbcapp.club.R;

import cn.rongcloud.im.App;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.FriendResponse;
import cn.rongcloud.im.server.response.getAddFriendResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import io.rong.common.RLog;
import io.rong.imageloader.core.ImageLoader;

/**
 * Created by lzs on 2018/6/7.
 */

public class AddFriendActivity extends BaseActivity {
    private static final int ADD_FRIEND = 21;
    private boolean isDebug;
    private SelectableRoundedImageView searchImage;
    private TextView searchName;
    private RelativeLayout ac_set_exit;
    private FriendResponse friendResponse;
    private String userId = "";
    private String friendId = "";
    private String img = "";
    private String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        isDebug = getSharedPreferences("config", MODE_PRIVATE).getBoolean("isDebug", false);
        setTitle(R.string.search_friend);
        initViews();
        setListener();
    }

    private void initViews() {
        searchImage = (SelectableRoundedImageView) findViewById(R.id.search_header);
        searchName = (TextView) findViewById(R.id.search_name);
        ac_set_exit = (RelativeLayout) findViewById(R.id.ac_set_exit);
        userId = getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_ID, "");
        img = getIntent().getStringExtra("friend_img");
        name = getIntent().getStringExtra("friend_name");
        friendId = getIntent().getStringExtra("friend_id");
        ImageLoader.getInstance().displayImage(img, searchImage, App.getOptions());
        searchName.setText(name);
    }

    private void setListener() {
        ac_set_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadDialog.show(mContext);
                request(ADD_FRIEND);
            }
        });
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {

            case ADD_FRIEND:

                return mAction.addFriend(userId, friendId);
        }
        return super.doInBackground(requestCode, id);
    }


    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case ADD_FRIEND:
                    getAddFriendResponse response1 = (getAddFriendResponse) result;
                    if (response1.getCode() == 100000) {
                        NToast.shortToast(mContext, getString(R.string.request_success));
                        LoadDialog.dismiss(mContext);
                        finish();
                    } else {
                        RLog.w("SearchFriendActivity", "请求失败 错误码:" + response1.getCode());
                        NToast.shortToast(mContext, response1.getMessage());
                        LoadDialog.dismiss(mContext);
                    }
                    break;
            }

            //            finish();
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case ADD_FRIEND:

                NToast.shortToast(mContext, result.toString());
                LoadDialog.dismiss(mContext);
                break;
        }
        //        finish();
    }
}
