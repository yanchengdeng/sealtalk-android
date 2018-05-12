package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.dbcapp.club.R;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.CSCustomServiceInfo;


public class PublicServiceActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pub_list);
        setTitle(R.string.de_actionbar_set_public);
        Button rightButton = getHeadRightButton();
        rightButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.de_ic_add));
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PublicServiceActivity.this, PublicServiceSearchActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.re_chatroom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //首先需要构造使用客服者的用户信息
                CSCustomServiceInfo.Builder csBuilder = new CSCustomServiceInfo.Builder();
                CSCustomServiceInfo csInfo = csBuilder.nickName("客服").build();
                RongIM.getInstance().startCustomerServiceChat(PublicServiceActivity.this, "KEFU152578489679075", "VIP客服", null);
            }
        });
    }
}
