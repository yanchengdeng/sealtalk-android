package cn.rongcloud.im.message.module;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;

import cn.rongcloud.im.ui.activity.AmapAndGoogleLoactionActivity;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.plugin.IPluginRequestPermissionResultCallback;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.LocationMessage;

/**
 * Author: 邓言诚  Create at : 2018/6/21  00:05
 * Email: yanchengdeng@gmail.com
 * Describle: 基于谷歌 高德地图的 插件
 */
public class AmapAndGoogleMapLocationPlugin implements IPluginModule, IPluginRequestPermissionResultCallback {
    private String targetId;
    private Conversation.ConversationType conversationType;

    public AmapAndGoogleMapLocationPlugin() {
    }

    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(io.rong.imkit.R.drawable.rc_ext_plugin_location_selector);
    }

    public String obtainTitle(Context context) {
        return context.getString(io.rong.imkit.R.string.rc_plugin_location_mix);
    }

    public void onClick(Fragment currentFragment, RongExtension extension) {
        targetId = extension.getTargetId();
        conversationType = extension.getConversationType();
        String[] permissions = new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_NETWORK_STATE"};
        if (PermissionCheckUtil.checkPermissions(currentFragment.getActivity(), permissions)) {
            this.startLocationActivity(currentFragment, extension);
        } else {
            extension.requestPermissionForPluginResult(permissions, 255, this);
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.w("dyc", requestCode + "" + data);

        /**
         *  intent.putExtra("thumb", AMapLocationActivity.this.getMapUrl(AMapLocationActivity.this.mLatResult, AMapLocationActivity.this.mLngResult));
         intent.putExtra("lat", AMapLocationActivity.this.mLatResult);
         intent.putExtra("lng", AMapLocationActivity.this.mLngResult);
         intent.putExtra("poi", AMapLocationActivity.this.mPoiResult);
         */


        if (data != null && data.getExtras() != null && !TextUtils.isEmpty(data.getStringExtra("poi"))) {
            double latitude = data.getDoubleExtra("lat", 0.00);
            double longitude = data.getDoubleExtra("lng", 0.00);
            String address = data.getStringExtra("poi");
            String uri = data.getStringExtra("thumb");
            LocationMessage locationMessage = LocationMessage.obtain(latitude, longitude, address, Uri.parse(uri));
            RongIM.getInstance().sendLocationMessage(Message.obtain(targetId, conversationType, locationMessage), null, null, null);
        }
    }

    private void startLocationActivity(Fragment fragment, RongExtension extension) {
        Intent intent = new Intent(fragment.getActivity(), AmapAndGoogleLoactionActivity.class);
        extension.startActivityForPluginResult(intent, 1, this);
    }

    public boolean onRequestPermissionResult(Fragment fragment, RongExtension extension, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PermissionCheckUtil.checkPermissions(fragment.getActivity(), permissions)) {
            this.startLocationActivity(fragment, extension);
        } else {
            extension.showRequestPermissionFailedAlter(PermissionCheckUtil.getNotGrantedPermissionMsg(fragment.getActivity(), permissions, grantResults));
        }

        return true;
    }
}
