package cn.rongcloud.im.message.plugins;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.blankj.utilcode.util.LogUtils;

import io.rong.imkit.R.drawable;
import io.rong.imkit.R.string;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.plugin.IPluginRequestPermissionResultCallback;
import io.rong.imkit.plugin.location.AMapLocationActivity;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.LocationMessage;

public class DefaultLocationPlugin implements IPluginModule, IPluginRequestPermissionResultCallback {
    private String targetId;
    private Conversation.ConversationType conversationType;

    public DefaultLocationPlugin() {
    }

    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(drawable.rc_ext_plugin_location_selector);
    }

    public String obtainTitle(Context context) {
        return context.getString(string.rc_plugin_location);
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
        LogUtils.w("dyc",requestCode+"--"+data);

        double latitude = data.getDoubleExtra("lat",0.00);
        double longitude = data.getDoubleExtra("lng",0.00);
        String address = data.getStringExtra("poi");
        String uri = data.getStringExtra("thumb");

        LocationMessage locationMessage = LocationMessage.obtain(latitude,longitude,address, Uri.parse(uri));
        RongIM.getInstance().sendLocationMessage(Message.obtain(targetId, conversationType, locationMessage), null, null, null);


    }

    private void startLocationActivity(Fragment fragment, RongExtension extension) {
        Intent intent = new Intent(fragment.getActivity(), AMapLocationActivity.class);
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