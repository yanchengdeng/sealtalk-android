package cn.rongcloud.im.message.plugins;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;

import com.dbcapp.club.R;

import java.util.HashMap;

import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.utils.NToast;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ContactNotificationMessage;

/**
 * Created by star1209 on 2018/5/18.
 */

public class DeleteAfterReadPlugin implements IPluginModule {
    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.baojia_delete_after_read_plugin_bg);
    }

    @Override
    public String obtainTitle(Context context) {
        return context.getString(R.string.baojia_delete_after_delete_title);
    }

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
        if (SealAppContext.getInstance().isDeleteAfterReadFlag()){
            SealAppContext.getInstance().setDeleteAfterReadFlag(false);
            NToast.shortToast(fragment.getContext(), R.string.baojia_undelete_after_read);
        }else {
            NToast.shortToast(fragment.getContext(), R.string.baojia_delete_after_read);
            SealAppContext.getInstance().setDeleteAfterReadFlag(true);
        }

//        BroadcastManager.getInstance(fragment.getContext()).sendBroadcast(SealConst.BAOJIA_DELETE_AFTER_READ);
    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {

    }
}
