package cn.rongcloud.im.message.plugins;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;

import com.dbcapp.club.R;

import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.utils.DialogListSelect;
import io.rong.imkit.RongExtension;
import io.rong.imkit.plugin.IPluginModule;

/**
 * Created by star1209 on 2018/5/18.
 */

public class DeleteAfterReadPlugin implements IPluginModule {

    private DialogListSelect mDialog;

    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.baojia_delete_after_read_plugin_bg);
    }

    @Override
    public String obtainTitle(Context context) {
        return context.getString(R.string.baojia_delete_after_delete_title);
    }

    private Click click;

    public void setListener(Click click) {
        this.click = click;
    }

    public interface Click {
        void pos();
    }

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
        if (SealAppContext.getInstance().isDeleteAfterReadFlag()) {
            SealAppContext.getInstance().setDeleteAfterReadFlag(false);
            SealAppContext.getInstance().setmTimer(0);
            //            NToast.shortToast(fragment.getContext(), R.string.baojia_undelete_after_read);
            if (click != null) {
                click.pos();
            }
        } else {
            //            NToast.shortToast(fragment.getContext(), R.string.baojia_delete_after_read);
            SealAppContext.getInstance().setDeleteAfterReadFlag(true);
            if (click != null)
                click.pos();
            //            if (mDialog == null) {
            //                mDialog = new DialogListSelect(fragment.getContext());
            //            }
            //            mDialog.setCancelable(false);
            //            mDialog.setOnItemClickCallBack(new ICallBackListener() {
            //                @Override
            //                public void doWork(int flag, Object object) {
            //                    SealAppContext.getInstance().setDeleteAfterReadFlag(true);
            //                    SealAppContext.getInstance().setmTimer(Integer.valueOf(((String) object).replace("s", "")));
            //                    mDialog.dismiss();
            //                }
            //            });
            //            mDialog.show();

            //            BroadcastManager.getInstance(fragment.getContext()).sendBroadcast(SealConst.BAOJIA_DELETE_AFTER_READ);
        }

    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {

    }
}
