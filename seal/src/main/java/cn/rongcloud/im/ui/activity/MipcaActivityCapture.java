package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Toast;

import com.dbcapp.club.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.mining.app.zxing.camera.CameraManager;
import com.mining.app.zxing.decoding.CaptureActivityHandler;
import com.mining.app.zxing.decoding.InactivityTimer;
import com.mining.app.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.model.QRCodeBean;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.FriendResponse;
import cn.rongcloud.im.server.response.getAddFriendResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.utils.JsonUtils;
import io.rong.common.RLog;

/**
 * Initial the camera
 */
public class MipcaActivityCapture extends BaseActivity implements Callback {

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet = "ISO-8859-1";
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private static final int REQUEST_CODE = 234;
    private String photo_path;
    private Bitmap scanBitmap;
    private int from;
    private boolean mLight = false;
    private static final int USER_DETAILS = 20;
    private static final int ADD_FRIEND = 21;
    private String userId = "";
    private String friendId = "";
    private String addFriendMessage = "";
    private QRCodeBean bean;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        // ViewUtil.addTopView(getApplicationContext(), this,
        // R.string.scan_card);
        CameraManager.init(getApplication());
        //        manager = (android.hardware.camera2.CameraManager) getSystemService(Context.CAMERA_SERVICE);
        //        try {
        //            String[] camerList = manager.getCameraIdList();
        //            for (String str : camerList
        //                    ) {
        //                Log.d("List", str);
        //            }
        //        } catch (CameraAccessException e) {
        //            Log.e("error", e.getMessage());
        //        }

        initTitle();
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);

        //小贝说扫码取号要跟外面的扫一扫一样，都要有相册和我的二维码
        //		if (from == SwitchConstant.SWITCH_FUNCTION_OFFER_WAIT) {
        //			otherFunctionView.setVisibility(View.GONE);
        //		}
    }

    private void initTitle() {
        setTitle(R.string.scan, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        inactivityTimer.shutdown();
    }

    /**
     * 扫码结果
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        if (resultString.equals("")) {
            Toast.makeText(MipcaActivityCapture.this, "Scan failed!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            resultDeal(resultString);
        }
        //        MipcaActivityCapture.this.finish();
    }

    private void resultDeal(String resultString) {
        if (resultString.startsWith("baojia:")) {
            resultString = resultString.replace("baojia:", "");
            try {
                bean = JsonUtils.objectFromJson(resultString, QRCodeBean.class);
                userId = getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_ID, "");

                if (bean != null) {
                    friendId = bean.getUserId();

                    request(USER_DETAILS);

                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                finish();
            }
        } else {
            finish();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case USER_DETAILS:
                return mAction.userDetail(userId, friendId);
            case ADD_FRIEND:

                return mAction.addFriend(userId, friendId);
        }
        return super.doInBackground(requestCode, id);
    }


    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case USER_DETAILS:
                    FriendResponse response = (FriendResponse) result;
                    LoadDialog.dismiss(mContext);
                    if (response.getCode() == 100000) {
                        if (!TextUtils.isEmpty(response.getData().getIsFriend()) && response.getData().getIsFriend().equals("true")) {
                            if (bean != null) {
                                if (bean.getType() == 0) {
                                    Friend friend = null;
                                    if (!TextUtils.isEmpty(friendId)) {
                                        friend = SealUserInfoManager.getInstance().getFriendByID(friendId);
                                    }
                                    Intent intent = new Intent(mContext, UserDetailActivity.class);
                                    intent.putExtra("friend", friend);
                                    intent.putExtra("type", 1);
                                    startActivity(intent);
                                } else if (bean.getType() == 1) {
                                    Intent intent = new Intent(mContext, TransferActivity.class);
                                    intent.putExtra("targetId", friendId);
                                    startActivity(intent);
                                }


                            }
                        } else {

                            Intent intent = new Intent(MipcaActivityCapture.this, AddFriendActivity.class);
                            intent.putExtra("friend_img", response.getData().getPortrait());
                            intent.putExtra("friend_name", response.getData().getUserName());
                            intent.putExtra("friend_id", friendId);
                            startActivity(intent);

                            //                            DialogWithYesOrNoUtils.getInstance().showEditDialog(mContext, getString(R.string.add_text), getString(R.string.add_friend), new DialogWithYesOrNoUtils.DialogCallBack() {
                        }

                    } else {
                        RLog.w("SearchFriendActivity", "请求失败 错误码:" + response.getCode());
                        NToast.shortToast(mContext, response.getMessage());
                        LoadDialog.dismiss(mContext);
                    }
                    finish();
                    break;
                case ADD_FRIEND:
                    getAddFriendResponse response1 = (getAddFriendResponse) result;
                    if (response1.getCode() == 100000) {
                        NToast.shortToast(mContext, getString(R.string.request_success));
                        LoadDialog.dismiss(mContext);
                    } else {
                        RLog.w("SearchFriendActivity", "请求失败 错误码:" + response1.getCode());
                        NToast.shortToast(mContext, response1.getMessage());
                        LoadDialog.dismiss(mContext);
                    }
                    break;
            }

            //            finish();
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case USER_DETAILS:
                LoadDialog.dismiss(mContext);
                finish();
                break;
            case ADD_FRIEND:

                NToast.shortToast(mContext, "你们已经是好友");
                LoadDialog.dismiss(mContext);
                break;
        }
        //        finish();
    }
}