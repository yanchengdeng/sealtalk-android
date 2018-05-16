package cn.rongcloud.im.message.plugins;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;

import com.dbcapp.club.R;

import cn.rongcloud.im.ui.activity.TransferActivity;
import io.rong.imkit.RongExtension;
import io.rong.imkit.plugin.IPluginModule;

/**
 * Created by star1209 on 2018/5/15.
 */

public class TransferPlugin implements IPluginModule {

    private static final int TRANSFER_CODE = 100;

    private Context mContext;
    private String mTargetId;

    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.baojia_transfer_plugin);
    }

    @Override
    public String obtainTitle(Context context) {
        return context.getString(R.string.baojia_transfer_title);
    }

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
        mTargetId = rongExtension.getTargetId();
        mContext = fragment.getActivity();
        Intent intent = new Intent(mContext, TransferActivity.class);
        intent.putExtra("targetId", mTargetId);
        rongExtension.startActivityForPluginResult(intent, TRANSFER_CODE, this);
    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {
        if (i == TRANSFER_CODE && i1 == Activity.RESULT_OK){

        }
    }
}
