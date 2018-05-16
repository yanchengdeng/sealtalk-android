package cn.rongcloud.im.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dbcapp.club.R;

import cn.rongcloud.im.App;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.activity.AccountSettingActivity;
import cn.rongcloud.im.ui.activity.MineWalletActivity;
import cn.rongcloud.im.ui.activity.MyAccountActivity;
import cn.rongcloud.im.ui.activity.QRCodeActivity;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imlib.model.UserInfo;

/**
 * Created by AMing on 16/6/21.
 * Company RongCloud
 */
public class MineFragment extends Fragment implements View.OnClickListener {
    public static final String SHOW_RED = "SHOW_RED";
    private SharedPreferences sp;
    private boolean isDebug;

    private SelectableRoundedImageView imageView;
    private TextView mTvName;
    private RelativeLayout mLayoutQrcode;
    private RelativeLayout mLayoutSettings;
    private LinearLayout mLayoutMineInfo;
    private RelativeLayout mLayoutWallet;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.seal_mine_fragment, container, false);
        isDebug = getContext().getSharedPreferences("config", getContext().MODE_PRIVATE).getBoolean("isDebug", false);
        initViews(mView);
        initData();
        BroadcastManager.getInstance(getActivity()).addAction(SealConst.CHANGEINFO, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUserInfo();
            }
        });
        return mView;
    }

    private void initData() {
        sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        updateUserInfo();
    }

    private void initViews(View mView) {
        imageView = (SelectableRoundedImageView) mView.findViewById(R.id.mine_header);
        mTvName = mView.findViewById(R.id.mine_name);
        mLayoutQrcode = mView.findViewById(R.id.rl_mine_qrcode);
        mLayoutSettings = mView.findViewById(R.id.rl_mine_settings);
        mLayoutMineInfo = mView.findViewById(R.id.start_user_profile);
        mLayoutWallet = mView.findViewById(R.id.rl_mine_wallet);

        mLayoutQrcode.setOnClickListener(this);
        mLayoutSettings.setOnClickListener(this);
        mLayoutMineInfo.setOnClickListener(this);
        mLayoutWallet.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_mine_qrcode://二维码
                gotoQrCode();
                break;
            case R.id.rl_mine_wallet://钱包
                gotoWallet();
                break;
            case R.id.rl_mine_settings: //设置
                startActivity(new Intent(getActivity(), AccountSettingActivity.class));
                break;
            case R.id.start_user_profile: //我的信息
                startActivity(new Intent(getActivity(), MyAccountActivity.class));
                break;
            default:
                break;
        }
    }

    private void gotoWallet() {
        Intent intent = new Intent(getContext(), MineWalletActivity.class);
        startActivity(intent);
    }

    //跳转二维码
    private void gotoQrCode() {
        Intent intent = new Intent(getContext(), QRCodeActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void updateUserInfo() {
        String userId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "");
        String username = sp.getString(SealConst.SEALTALK_LOGIN_NAME, "");
        String userPortrait = sp.getString(SealConst.SEALTALK_LOGING_PORTRAIT, "");
        mTvName.setText(username);
        if (!TextUtils.isEmpty(userId)) {
            String portraitUri = SealUserInfoManager.getInstance().getPortraitUri
                                 (new UserInfo(userId, username, Uri.parse(userPortrait)));
            ImageLoader.getInstance().displayImage(portraitUri, imageView, App.getOptions());
        }
    }

    private String[] getVersionInfo() {
        String[] version = new String[2];

        PackageManager packageManager = getActivity().getPackageManager();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getActivity().getPackageName(), 0);
            version[0] = String.valueOf(packageInfo.versionCode);
            version[1] = packageInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }
}
