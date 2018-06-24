package cn.rongcloud.im.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dbcapp.club.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import cn.rongcloud.im.server.response.UpdateVersionResponse;
import cn.rongcloud.im.server.utils.NToast;


/**
 * 应用程序更新工具包
 */
public class UpdateManager {

    private static final int DOWN_NOSDCARD = 0;

    private static final int DOWN_UPDATE = 1;

    private static final int DOWN_OVER = 2;

    private static final int DIALOG_TYPE_LATEST = 0;

    private static final int DIALOG_TYPE_FAIL = 1;

    private static final String DEFAULT_FOLDER_NAME = Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/seal/update/";

    private static UpdateManager updateManager;

    private Context mContext;

    // 下载对话框
    private AlertDialog downloadDialog;


    // 进度条
    private ProgressBar mProgress;

    // 显示下载数值
    private TextView mProgressText;


    // 进度值
    private int progress;

    // 终止标记
    private boolean interceptFlag;

    // 提示语
    private String updateMsg = "";

    // 返回的安装包url
    private String apkUrl = "";

    // 下载包保存路径
    private String savePath = "";

    // apk保存完整路径
    private String apkFilePath = "";

    // 临时下载文件路径
    private String tmpFilePath = "";
    // 下载文件大小
    private String apkFileSize;
    // 已下载文件大小
    private String tmpFileSize;
    // 下载线程
    private Thread downLoadThread;

    private String curVersionName = "";
    private int curVersionCode = 0;
    private UpdateVersionResponse updateVersionResponse;


    private boolean isShowDialog = true;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);

                    if (tmpFileSize.equals(apkFileSize)) {
                        mProgress.setProgress(100);
                        downloadDialog.dismiss();
                        installApk();
                    }
                    mProgressText.setText(tmpFileSize + "/" + apkFileSize);
                    break;
                case DOWN_OVER:

                    break;
                case DOWN_NOSDCARD:
                    downloadDialog.dismiss();
                    break;
            }
        }

        ;
    };

    public static UpdateManager getUpdateManager() {
        if (updateManager == null) {
            updateManager = new UpdateManager();
        }
        updateManager.interceptFlag = false;
        return updateManager;
    }

    /**
     * 检查App更新
     *
     * @param context
     */
    public void checkAppUpdate(Context context, UpdateVersionResponse response) {
        this.mContext = context;
        this.updateVersionResponse = response;
        getCurrentVersion();
        if (response.getData() != null) {
            updateMsg = response.getData().getVersionTitle();
            apkUrl = response.getData().getAppUrl();
            if (response.getData().getVersionCode() > curVersionCode) {
                showNoticeDialog();
            }
        }
    }


    public void dismissDialog() {
        isShowDialog = false;
    }

    /**
     * 获取当前客户端版本信息
     */
    private void getCurrentVersion() {
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 0);
            curVersionName = info.versionName;
            curVersionCode = info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * 显示版本更新通知对话框
     */
    private void showNoticeDialog() {
        if (mContext == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(updateMsg).setMessage("发现新版本！请及时更新").
                setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        showDownloadDialog();
                    }
                });
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        // 显示对话框
        alertDialog.show();
    }

    /**** 强制升级标志位 ***/
    private final int FORCE_UPDATE = 1;

    /**
     * 显示下载对话框
     */
    private void showDownloadDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("正在下载新版本");
        //		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.update_progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
        mProgressText = (TextView) v.findViewById(R.id.update_progress_text);
        mProgress.setProgress(0);
        builder.setView(v);
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        // 显示对话框
        downloadDialog = alertDialog;
        alertDialog.show();
        downloadApk();

    }

    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                String saveApkName = mContext.getResources().getString(
                        R.string.app_name)
                        + "_";
                String apkName = saveApkName + curVersionName + ".apk";
                String tmpApk = saveApkName + updateVersionResponse.getData().getId() + ".apk";
                // 判断是否挂载了SD卡
                String storageState = Environment.getExternalStorageState();
                if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                    savePath = DEFAULT_FOLDER_NAME;
                    File file = new File(savePath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    apkFilePath = savePath + apkName;
                    tmpFilePath = savePath + tmpApk;
                }

                // 没有挂载SD卡，无法下载文件
                if (apkFilePath == null || apkFilePath == "") {
                    mHandler.sendEmptyMessage(DOWN_NOSDCARD);
                    return;
                }

                File ApkFile = new File(apkFilePath);


                // 输出临时下载文件
                File tmpFile = new File(tmpFilePath);
                FileOutputStream fos = new FileOutputStream(ApkFile);

                URL url = new URL(apkUrl);
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                // 显示文件大小格式：2个小数点显示
                DecimalFormat df = new DecimalFormat("0.00");
                // 进度条下面显示的总文件大小
                apkFileSize = df.format((float) length / 1024 / 1024) + "MB";

                int count = 0;
                byte buf[] = new byte[1024];

                do {
                    int numread = is.read(buf);
                    count += numread;
                    // 进度条下面显示的当前下载文件大小
                    tmpFileSize = df.format((float) count / 1024 / 1024) + "MB";
                    // 当前进度值
                    progress = (int) (((float) count / length) * 100);
                    // 更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0) {
                        // 下载完成 - 将临时下载文件转成APK文件
                        if (tmpFile.renameTo(ApkFile)) {
                            // 通知安装
                            mHandler.sendEmptyMessage(DOWN_OVER);
                        }
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (!interceptFlag);// 点击取消就停止下载

                fos.close();
                is.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    /**
     * 下载apk
     *
     * @param
     */
    private void downloadApk() {
        downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }

    /**
     * 安装apk
     *
     * @param
     */
    private void installApk() {
        File apkfile = new File(apkFilePath);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }
}
