package cn.rongcloud.im.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.dbcapp.club.R;
import com.jrmf360.rylib.common.util.ToastUtil;

import cn.rongcloud.im.ui.activity.SoftWareActivity;


public class DiscoverFragment extends Fragment implements View.OnClickListener {

    private static final int GETDEFCONVERSATION = 333;

    private RelativeLayout mLayoutCircle;
    private RelativeLayout mLayoutSoftware;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatroom_list, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mLayoutCircle = view.findViewById(R.id.rl_discover_circle);
        mLayoutSoftware = view.findViewById(R.id.rl_discover_software);

        mLayoutCircle.setOnClickListener(this);
        mLayoutSoftware.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_discover_software: //TODO 小程序
                gotoSoftware();
                break;
            case R.id.rl_discover_circle: //TODO 圈圈
                ToastUtil.showToast(getContext(), "后续开放");
                break;
            default:
                break;
        }
    }

    private void gotoSoftware() {
        Intent intent = new Intent(getContext(), SoftWareActivity.class);
        startActivity(intent);
    }
}
