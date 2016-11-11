package io.rong.callkit;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import io.rong.calllib.IRongCallListener;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.imkit.utilities.PermissionCheckUtil;

/**
 * Created by weiqinxiao on 16/3/9.
 */
public class BaseCallActivity extends Activity implements IRongCallListener {
    private final static long DELAY_TIME = 1000;
    static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 100;
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

    private MediaPlayer mMediaPlayer;
    private int time = 0;
    private boolean shouldShowFloat;
    private boolean shouldRestoreFloat;
    private Handler handler;
    private BroadcastReceiver mHomeKeyReceiver;
    protected boolean isFinishing;

    static final String[] VIDEO_CALL_PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
    static final String[] AUDIO_CALL_PERMISSIONS = {Manifest.permission.RECORD_AUDIO};

    public void setShouldShowFloat(boolean shouldShowFloat) {
        this.shouldShowFloat = shouldShowFloat;
    }

    public void showShortToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void postRunnableDelay(Runnable runnable) {
        handler.postDelayed(runnable, DELAY_TIME);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        RongCallProxy.getInstance().setCallListener(this);
        shouldRestoreFloat = true;

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        if (!isScreenOn) {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
            wl.acquire();
            wl.release();
        }
        handler = new Handler();
        mHomeKeyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                    String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                    if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                        finish();
                    }
                }
            }
        };
        try {
            registerReceiver(mHomeKeyReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        } catch (Exception e) {
            e.printStackTrace();
        }
        isFinishing = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("floatbox");
        if (shouldRestoreFloat)
            onRestoreFloatBox(bundle);
        shouldRestoreFloat = true;
    }

    public void onOutgoingCallRinging() {
        mMediaPlayer = MediaPlayer.create(this, R.raw.voip_outgoing_ring);

        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
    }

    public void onIncomingCallRinging() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(this, uri);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setupTime(final TextView timeView) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        time++;
                        if (time >= 3600) {
                            timeView.setText(String.format("%d:%02d:%02d", time / 3600, (time % 3600) / 60, (time % 60)));
                        } else {
                            timeView.setText(String.format("%02d:%02d", (time % 3600) / 60, (time % 60)));
                        }
                    }
                });
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 0, 1000);
    }

    public int getTime() {
        return time;
    }

    public void stopRing() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onCallOutgoing(RongCallSession callProfile, SurfaceView localVideo) {
    }

    @Override
    public void onRemoteUserRinging(String userId) {

    }

    @Override
    public void onCallDisconnected(RongCallSession callProfile, RongCallCommon.CallDisconnectedReason reason) {
        shouldShowFloat = false;

        String text = null;
        switch (reason) {
            case CANCEL:
                text = getString(R.string.rc_voip_mo_cancel);
                break;
            case REJECT:
                text = getString(R.string.rc_voip_mo_reject);
                break;
            case NO_RESPONSE:
            case BUSY_LINE:
                text = getString(R.string.rc_voip_mo_no_response);
                break;
            case REMOTE_BUSY_LINE:
                text = getString(R.string.rc_voip_mt_busy);
                break;
            case REMOTE_CANCEL:
                text = getString(R.string.rc_voip_mt_cancel);
                break;
            case REMOTE_REJECT:
                text = getString(R.string.rc_voip_mt_reject);
                break;
            case REMOTE_NO_RESPONSE:
                text = getString(R.string.rc_voip_mt_no_response);
                break;
            case REMOTE_HANGUP:
            case HANGUP:
            case NETWORK_ERROR:
                text = getString(R.string.rc_voip_call_terminalted);
                break;
        }
        if (text != null) {
            showShortToast(text);
        }
        RongCallProxy.getInstance().setCallListener(null);
    }

    @Override
    public void onRemoteUserInvited(String userId, RongCallCommon.CallMediaType mediaType) {

    }

    @Override
    public void onRemoteUserJoined(String userId, RongCallCommon.CallMediaType mediaType, SurfaceView remoteVideo) {

    }

    @Override
    public void onRemoteUserLeft(String userId, RongCallCommon.CallDisconnectedReason reason) {

    }

    @Override
    public void onMediaTypeChanged(String userId, RongCallCommon.CallMediaType mediaType, SurfaceView video) {

    }

    @Override
    public void onError(RongCallCommon.CallErrorCode errorCode) {
    }

    @Override
    public void onCallConnected(RongCallSession callProfile, SurfaceView localVideo) {
        shouldShowFloat = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
//        RongCallSession session = RongCallClient.getInstance().getCallSession();
//        if (session == null) {
//            finish();
//            return;
//        }
        if (shouldShowFloat) {
            Bundle bundle = new Bundle();
            String action = onSaveFloatBoxState(bundle);
            if (action != null) {
                bundle.putString("action", action);
                CallFloatBoxView.showFloatBox(getApplicationContext(), bundle, time);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        RongCallProxy.getInstance().setCallListener(this);
//        RongCallSession session = RongCallClient.getInstance().getCallSession();
//        if (session == null) {
//            finish();
//            return;
//        }
        time = CallFloatBoxView.hideFloatBox();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        shouldRestoreFloat = false;
    }

    @Override
    protected void onDestroy() {
        try {
            if (mHomeKeyReceiver != null) {
                unregisterReceiver(mHomeKeyReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isFinishing = false;
        super.onDestroy();
    }

    @Override
    public void onRemoteCameraDisabled(String userId, boolean muted) {

    }

    public void onRestoreFloatBox(Bundle bundle) {

    }

    public String onSaveFloatBoxState(Bundle bundle) {
        return null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @TargetApi(23)
    boolean requestCallPermissions(RongCallCommon.CallMediaType type, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        String[] permissions;
        if (type.equals(RongCallCommon.CallMediaType.VIDEO)) {
            permissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        } else if (type.equals(RongCallCommon.CallMediaType.AUDIO)) {
            permissions = new String[] {Manifest.permission.RECORD_AUDIO};
        } else {
            return true;
        }
        return PermissionCheckUtil.requestPermissions(this, permissions, requestCode);
    }

}
