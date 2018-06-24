package cn.rongcloud.im.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.dbcapp.club.R;

import java.util.ArrayList;
import java.util.List;


public class DialogListSelect implements OnItemClickListener {
    private Dialog dialog;
    private View view;
    private ListView list_channel;
    private ICallBackListener backListener;
    private Context context;
    private List<String> mRegionList = new ArrayList<>();

    public DialogListSelect(Context context) {
        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null);

        dialog = new Dialog(context, R.style.dialog_layout);
        list_channel = (ListView) view.findViewById(R.id.list_channel);

        initData();

    }

    private void initData() {
        mRegionList.add("7s");
        mRegionList.add("10s");
        mRegionList.add("15s");
        mRegionList.add("20s");
        mRegionList.add("30s");
        mRegionList.add("60s");
        setDialogListInfo(mRegionList);
    }

    public void show() {
        dialog.getWindow().setContentView(view);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        dialog.show();

    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void setDialogListInfo(List<String> datas) {
        DialogListSelectAdapter adDialogListSelectAdapter = new DialogListSelectAdapter(context, datas);
        list_channel.setAdapter(adDialogListSelectAdapter);
    }

    public void setOnItemClickCallBack(ICallBackListener backListener) {
        list_channel.setOnItemClickListener(this);
        this.backListener = backListener;
    }

    public boolean isShowing() {
        if (dialog != null)
            return dialog.isShowing();
        return false;
    }

    public void setCancelable(boolean flag) {
        dialog.setCancelable(flag);
    }

    public void setCancelableOnTouchOutside(boolean flag) {
        dialog.setCanceledOnTouchOutside(flag);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (backListener != null) {
            backListener.doWork(position, mRegionList.get(position));
        }
    }

}
