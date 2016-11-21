package cn.rongcloud.im;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import cn.rongcloud.im.message.provider.ContactNotificationMessageProvider;
import cn.rongcloud.im.server.utils.NLog;
import cn.rongcloud.im.utils.SharedPreferencesContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.provider.FileMessageItemProvider;
import io.rong.imkit.widget.provider.GroupNotificationMessageItemProvider;
import io.rong.imkit.widget.provider.RealTimeLocationMessageProvider;
import io.rong.imlib.ipc.RongExceptionHandler;
import io.rong.message.FileMessage;
import io.rong.message.GroupNotificationMessage;
import io.rong.push.RongPushClient;
import io.rong.push.common.RongException;


public class App extends MultiDexApplication {

    private static DisplayImageOptions options;

    @Override
    public void onCreate() {

        super.onCreate();

        if (getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext()))) {

            //LeakCanary.install(this);//内存泄露检测
            RongPushClient.registerHWPush(this);
            RongPushClient.registerMiPush(this, "2882303761517473625", "5451747338625");
            try {
                RongPushClient.registerGCM(this);
            } catch (RongException e) {
                e.printStackTrace();
            }

            /**
             * 注意：
             *
             * IMKit SDK调用第一步 初始化
             *
             * context上下文
             *
             * 只有两个进程需要初始化，主进程和 push 进程
             */
            //RongIM.setServerInfo("nav.cn.ronghub.com", "img.cn.ronghub.com");
            RongIM.init(this);
            NLog.setDebug(true);//Seal Module Log 开关
            SealAppContext.init(this);
            SharedPreferencesContext.init(this);
            Thread.setDefaultUncaughtExceptionHandler(new RongExceptionHandler(this));

            try {
                RongIM.registerMessageTemplate(new ContactNotificationMessageProvider());
                RongIM.registerMessageTemplate(new RealTimeLocationMessageProvider());
            } catch (Exception e) {
                e.printStackTrace();
            }

            openSealDBIfHasCachedToken();

            options = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.de_default_portrait)
            .showImageOnFail(R.drawable.de_default_portrait)
            .showImageOnLoading(R.drawable.de_default_portrait)
            .displayer(new FadeInBitmapDisplayer(300))
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();

            //初始化图片下载组件
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
            .threadPriority(Thread.NORM_PRIORITY - 2)
            .denyCacheImageMultipleSizesInMemory()
            .diskCacheSize(50 * 1024 * 1024)
            .diskCacheFileCount(200)
            .diskCacheFileNameGenerator(new Md5FileNameGenerator())
            .defaultDisplayImageOptions(options)
            .build();

            //Initialize ImageLoader with configuration.
            ImageLoader.getInstance().init(config);
        }
    }

    public static DisplayImageOptions getOptions() {
        return options;
    }

    private void openSealDBIfHasCachedToken() {
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        String cachedToken = sp.getString("loginToken", "");
        if (!TextUtils.isEmpty(cachedToken)) {
            String current = getCurProcessName(this);
            String mainProcessName = getPackageName();
            if (mainProcessName.equals(current)) {
                SealUserInfoManager.getInstance().openDB();
            }
        }
    }

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

}
