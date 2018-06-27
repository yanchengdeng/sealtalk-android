package cn.rongcloud.im.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.dbcapp.club.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.rongcloud.im.utils.PerfectClickListener;
import io.rong.common.RLog;

public class AmapAndGoogleLoactionActivity extends FragmentActivity implements View.OnClickListener,
        AMap.OnCameraChangeListener, OnMapReadyCallback, GoogleMap.OnCameraMoveListener,
        AMapLocationListener, CompoundButton.OnCheckedChangeListener, GeocodeSearch.OnGeocodeSearchListener {
    private ToggleButton mcheckbtn;
    private Button mapbtn;
    private LinearLayout mContainerLayout;
    private LinearLayout.LayoutParams mParams;
    private TextureMapView mAmapView;
    private MapView mGoogleMapView;
    private float zoom = 10;
    private double latitude;
    private double longitude;
    //    private boolean mIsAmapDisplay = true;
    private boolean mIsAuto = true;
    private GoogleMap googlemap;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private AlphaAnimation anappear;
    private AlphaAnimation andisappear;
    private IntentFilter mIntentFilter;


    private TextView mLocationTip;
    private String mPoiResult;
    private Marker mMarker;
    private ValueAnimator animator;
    //    private double mLatResult;
//    private double mLngResult;、
//    private Handler mHandler;、
    private GeocodeSearch mGeocodeSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amap_and_google_loaction);
//        this.mHandler = new Handler();
        init();
        initLocation();

        findViewById(R.id.btn_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mContainerLayout = (LinearLayout) findViewById(R.id.map_container);
        mAmapView = new TextureMapView(this);
        mLocationTip = findViewById(R.id.tv_ext_location_marker);
        mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mContainerLayout.addView(mAmapView, mParams);

        mAmapView.onCreate(savedInstanceState);

        mAmapView.getMap().setOnCameraChangeListener(this);

        anappear = new AlphaAnimation(0, 1);
        andisappear = new AlphaAnimation(1, 0);
        anappear.setDuration(5000);
        andisappear.setDuration(5000);

        // 注册广播，监听应用必须谷歌服务安装情况
        mIntentFilter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        mIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        mIntentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        mIntentFilter.addDataScheme("package");
        registerReceiver(mInstallReciver, mIntentFilter);
        this.mGeocodeSearch = new GeocodeSearch(this);
        this.mGeocodeSearch.setOnGeocodeSearchListener(this);


        //显示当前定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(io.rong.imkit.R.drawable.rc_ext_my_locator));
        myLocationStyle.strokeWidth(0.0F);
        myLocationStyle.strokeColor(io.rong.imkit.R.color.rc_main_theme);
        myLocationStyle.radiusFillColor(0);
        mAmapView.getMap().setMyLocationStyle(myLocationStyle);
//        mAmapView.getMap().setLocationSource(this);
//        mAmapView.getMap().setMyLocationEnabled(true);

        //发送位置
        findViewById(R.id.text_right).setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                if (AmapAndGoogleLoactionActivity.this.latitude == 0.0D && AmapAndGoogleLoactionActivity.this.longitude == 0.0D && TextUtils.isEmpty(AmapAndGoogleLoactionActivity.this.mPoiResult)) {
                    Toast.makeText(AmapAndGoogleLoactionActivity.this, AmapAndGoogleLoactionActivity.this.getString(io.rong.imkit.R.string.rc_location_temp_failed), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent();
                    if (isInArea(latitude, longitude)) {
                        intent.putExtra("thumb", AmapAndGoogleLoactionActivity.this.getMapUrl(AmapAndGoogleLoactionActivity.this.latitude, AmapAndGoogleLoactionActivity.this.longitude));
                    } else {
                        intent.putExtra("thumb", AmapAndGoogleLoactionActivity.this.getGoogleMapUrl(AmapAndGoogleLoactionActivity.this.latitude, AmapAndGoogleLoactionActivity.this.longitude));
                    }
                    intent.putExtra("lat", AmapAndGoogleLoactionActivity.this.latitude);
                    intent.putExtra("lng", AmapAndGoogleLoactionActivity.this.longitude);
                    intent.putExtra("poi", AmapAndGoogleLoactionActivity.this.mPoiResult);
                    AmapAndGoogleLoactionActivity.this.setResult(-1, intent);
                    AmapAndGoogleLoactionActivity.this.finish();
                }
            }
        });
    }

    private void initLocation() {
        //初始化client
        mlocationClient = new AMapLocationClient(this.getApplicationContext());
        // 设置定位监听
        mlocationClient.setLocationListener(this);
        //定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置为高精度定位模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setOnceLocation(true);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        mlocationClient.startLocation();
    }

    private void init() {
        mContainerLayout = (LinearLayout) findViewById(R.id.map_container);
        mapbtn = (Button) findViewById(R.id.button);
        mcheckbtn = (ToggleButton) findViewById(R.id.auto);
        mapbtn.setOnClickListener(this);
        mcheckbtn.setOnClickListener(this);
        mcheckbtn.setOnCheckedChangeListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.auto:
                mIsAuto = mcheckbtn.isChecked();
                break;
            case R.id.button:
                mcheckbtn.setChecked(false);
                mIsAuto = false;
//                if (mIsAmapDisplay) {
//                    changeToGoogleMapView();
//                } else {
//                    changeToAmapView();
//                }
                break;
        }
    }

    /**
     * 切换为高德地图显示
     */
    private void changeToAmapView() {
        if (googlemap != null && googlemap.getCameraPosition() != null) {
            zoom = googlemap.getCameraPosition().zoom;
            latitude = googlemap.getCameraPosition().target.latitude;
            longitude = googlemap.getCameraPosition().target.longitude;
            mAmapView = new TextureMapView(this, new AMapOptions()
                    .camera(new com.amap.api.maps.model.CameraPosition(new LatLng(latitude, longitude), zoom, 0, 0)));
        } else {
            mlocationClient.startLocation();
            mAmapView = new TextureMapView(this);

        }
        mAmapView.onCreate(null);
        mAmapView.onResume();
        mContainerLayout.addView(mAmapView, mParams);

        mGoogleMapView.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mGoogleMapView != null) {
                    mGoogleMapView.setVisibility(View.GONE);
                    mContainerLayout.removeView(mGoogleMapView);
                    mGoogleMapView.onDestroy();
                }
            }
        });
        mAmapView.getMap().setOnCameraChangeListener(this);
//        mIsAmapDisplay = true;
        mapbtn.setText("To Google");
    }


    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            mAmapView.setVisibility(View.GONE);
            mContainerLayout.removeView(mAmapView);
            if (mAmapView != null) {
                mAmapView.onDestroy();
            }
        }
    };

    /**
     * 切换为google地图显示
     */
    private void changeToGoogleMapView() {
        if (!checkGooglePlayServices()) {
            return;
        }
        if (googlemap != null && googlemap.getCameraPosition() != null) {
            zoom = googlemap.getCameraPosition().zoom;
            latitude = googlemap.getCameraPosition().target.latitude;
            longitude = googlemap.getCameraPosition().target.longitude;
        }

        mapbtn.setText("To Amap");
//        mIsAmapDisplay = false;
        mGoogleMapView = new com.google.android.gms.maps.MapView(this, new GoogleMapOptions()
                .camera(new com.google.android.gms.maps.model
                        .CameraPosition(new com.google.android.gms.maps.model.LatLng(latitude, longitude), zoom, 0, 0)));
        mGoogleMapView.onCreate(null);
        mGoogleMapView.onResume();
        mContainerLayout.addView(mGoogleMapView, mParams);
        mGoogleMapView.getMapAsync(this);
        handler.sendEmptyMessageDelayed(0, 500);
    }

    @Override
    public void onCameraChange(com.amap.api.maps.model.CameraPosition cameraPosition) {

        if (Build.VERSION.SDK_INT < 11) {
            if (this.mMarker != null) {
                this.mMarker.setPosition(cameraPosition.target);
            }
        }
    }

    /**
     * 高德地图移动完成回调
     *
     * @param cameraPosition 地图移动结束的中心点位置信息
     */
    @Override
    public void onCameraChangeFinish(com.amap.api.maps.model.CameraPosition cameraPosition) {
//        longitude = cameraPosition.target.longitude;
//        latitude = cameraPosition.target.latitude;
// Point(223065007, 114012764)  Point(223069344, 114021728)
//        addLocatedMarker(cameraPosition.target,"");


        zoom = cameraPosition.zoom;

        LatLonPoint point = new LatLonPoint(cameraPosition.target.latitude, cameraPosition.target.longitude);
        RegeocodeQuery query = new RegeocodeQuery(point, 50.0F, "autonavi");
        this.mGeocodeSearch.getFromLocationAsyn(query);
        if (this.mMarker != null) {
            animMarker(cameraPosition);

        }


        if (!isInArea(latitude, longitude) && mIsAuto) {
            changeToGoogleMapView();
        }
    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if (regeocodeResult != null) {
            RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
            this.latitude = regeocodeResult.getRegeocodeQuery().getPoint().getLatitude();
            this.longitude = regeocodeResult.getRegeocodeQuery().getPoint().getLongitude();
            String formatAddress = regeocodeResult.getRegeocodeAddress().getFormatAddress();
            this.mPoiResult = formatAddress.replace(regeocodeAddress.getProvince(), "").replace(regeocodeAddress.getCity(), "").replace(regeocodeAddress.getDistrict(), "");
            this.mLocationTip.setText(this.mPoiResult);
            LatLng latLng = new LatLng(this.latitude, this.longitude);
            if (this.mMarker != null) {
                this.mMarker.setPosition(latLng);
            }
        } else {
            Toast.makeText(this, this.getString(io.rong.imkit.R.string.rc_location_fail), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @TargetApi(11)
    private void animMarker(final com.amap.api.maps.model.CameraPosition target) {
        if (Build.VERSION.SDK_INT > 11) {
            if (this.animator != null) {
                this.animator.start();
                return;
            }

            this.animator = ValueAnimator.ofFloat(new float[]{(float) (this.mAmapView.getHeight() / 2), (float) (this.mAmapView.getHeight() / 2 - 30)});
            this.animator.setInterpolator(new DecelerateInterpolator());
            this.animator.setDuration(150L);
            this.animator.setRepeatCount(1);
            this.animator.setRepeatMode(ValueAnimator.REVERSE);
            this.animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    RLog.d("AMapLocationActivity", "onAnimationUpdate");
                    Float value = (Float) animation.getAnimatedValue();
                    mMarker.setPositionByPixels(mAmapView.getWidth() / 2, Math.round(value));
                }
            });
            this.animator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    mMarker.setIcon(mBitmapDescriptor);
//                    addLocatedMarker(target.target,"");
                }
            });
            this.animator.start();
        }

    }

    /**
     * 粗略判断当前屏幕显示的地图中心点是否在国内
     *
     * @param latitude   纬度
     * @param longtitude 经度
     * @return 屏幕中心点是否在国内
     */
    private boolean isInArea(double latitude, double longtitude) {
        if ((latitude > 3.837031) && (latitude < 53.563624)
                && (longtitude < 135.095670) && (longtitude > 73.502355)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAmapView != null) {
            mAmapView.onResume();
        }
        if (mGoogleMapView != null) {
            try {
                mGoogleMapView.onResume();
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAmapView != null) {
            mAmapView.onPause();
        }
        if (mGoogleMapView != null) {
            try {
                mGoogleMapView.onPause();
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAmapView != null) {
            mAmapView.onSaveInstanceState(outState);
        }
        if (mGoogleMapView != null) {
            try {
                mGoogleMapView.onSaveInstanceState(outState);
            } catch (Exception e) {
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyLocation();
        if (mAmapView != null) {
            mAmapView.onDestroy();
        }
        if (mGoogleMapView != null) {
            try {
                mGoogleMapView.onDestroy();
            } catch (Exception e) {
            }
        }

        if (mInstallReciver != null) {
            unregisterReceiver(mInstallReciver);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googlemap = googleMap;

        com.google.android.gms.maps.model.BitmapDescriptor googleBitmaps = com.google.android.gms.maps.model.BitmapDescriptorFactory.fromResource(io.rong.imkit.R.drawable.rc_ext_location_marker);
        if (googlemap != null) {
            googlemap.setOnCameraMoveListener(this);
            com.google.android.gms.maps.model.LatLng sydney = new com.google.android.gms.maps.model.LatLng(latitude, longitude);
            googlemap.addMarker(new com.google.android.gms.maps.model.MarkerOptions().position(sydney).icon(googleBitmaps));
            googlemap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLng(sydney));
        }
    }

    /**
     * google地图移动回调
     */
    @Override
    public void onCameraMove() {
        CameraPosition cameraPosition = googlemap.getCameraPosition();
        longitude = cameraPosition.target.longitude;
        latitude = cameraPosition.target.latitude;
        zoom = cameraPosition.zoom;
        if (isInArea(latitude, longitude) && mIsAuto) {
            changeToAmapView();
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null
                && aMapLocation.getErrorCode() == 0) {
            longitude = aMapLocation.getLongitude();
            latitude = aMapLocation.getLatitude();
            mPoiResult = aMapLocation.getAddress();
            if (!aMapLocation.getCountry().equals("中国")) {
                changeToGoogleMapView();
            } else {
                mAmapView.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
                com.amap.api.maps.CameraUpdate update = com.amap.api.maps.CameraUpdateFactory.zoomTo(17.0F);
                mAmapView.getMap().animateCamera(update, new com.amap.api.maps.AMap.CancelableCallback() {
                    public void onFinish() {
                        mAmapView.getMap().setOnCameraChangeListener(AmapAndGoogleLoactionActivity.this);
                    }

                    public void onCancel() {
                    }
                });

                //设置当前位置
                addLocatedMarker(mPoiResult);
            }
//            Toast.makeText(AmapAndGoogleLoactionActivity.this, aMapLocation.getCountry(), Toast.LENGTH_LONG).show();
            mIsAuto = false;
            mcheckbtn.setChecked(false);

        } else {
            String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
            Log.e("AmapErr", errText);
            Toast.makeText(AmapAndGoogleLoactionActivity.this, errText, Toast.LENGTH_LONG).show();
        }
    }


    private BitmapDescriptor mBitmapDescriptor;

    private void addLocatedMarker(String poi) {
        this.mBitmapDescriptor = BitmapDescriptorFactory.fromResource(io.rong.imkit.R.drawable.rc_ext_location_marker);
        MarkerOptions markerOptions = (new MarkerOptions()).position(new LatLng(latitude, longitude)).icon(mBitmapDescriptor);
        this.mMarker = this.mAmapView.getMap().addMarker(markerOptions);
        this.mMarker.setPositionByPixels(this.mAmapView.getWidth() / 2, this.mAmapView.getHeight() / 2);
        this.mLocationTip.setText(String.format("%s", poi));

        //添加原点
//        Marker positionMark;
//        locaionDescriptro =  BitmapDescriptorFactory.fromResource(io.rong.imkit.R.drawable.rc_ext_my_locator);
//        MarkerOptions locationMarketOptions = (new MarkerOptions()).position(new LatLng(longitude, longitude)).icon(locaionDescriptro).visible(true);
//        this.mMarker = this.mAmapView.getMap().addMarker(locationMarketOptions);
//        this.mMarker.setPositionByPixels(this.mAmapView.getWidth() / 2, this.mAmapView.getHeight() / 2);
//        positionMark = mAmapView.getMap().addMarker(locationMarketOptions);
//        positionMark.setDraggable(false);
//        positionMark.setMarkerOptions(locationMarketOptions);
//        positionMark.setPositionByPixels(this.mAmapView.getWidth() / 2, this.mAmapView.getHeight() / 2);


    }

    /**
     * 停止定位
     *
     * @since 2.8.0
     */
    private void stopLocation() {
        // 停止定位
        mlocationClient.stopLocation();
    }

    /**
     * 销毁定位
     */
    private void destroyLocation() {
        if (null != mlocationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            mlocationClient.onDestroy();
            mlocationClient = null;
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.auto) {
            mIsAuto = isChecked;
        }
    }

    private boolean checkGooglePlayServices() {
        int result = MapsInitializer.initialize(this);
        switch (result) {
            case ConnectionResult.SUCCESS:
                return true;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Toast.makeText(getApplicationContext(), "SERVICE_VERSION_UPDATE_REQUIRED", Toast.LENGTH_SHORT).show();
                GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED, this, 0).show();
                break;
            case ConnectionResult.SERVICE_INVALID:
                AlertDialog.Builder m = new AlertDialog.Builder(this)
                        .setMessage("使用谷歌地图，需要安装谷歌相关服务")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        installAPK("Google_Play_services.apk");
                                    }
                                });
                m.show();
                break;
            case ConnectionResult.SERVICE_MISSING:
                AlertDialog.Builder m1 = new AlertDialog.Builder(this)
                        .setMessage("使用谷歌地图，需要安装谷歌相关服务")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        installAPK("Google_Play_services.apk");
                                    }
                                });
                m1.show();
                break;

        }
        return false;


    }


    /**
     * 安装应用
     */
    private void installAPK(String apkName) {
        InputStream is;
        try {
            is = getApplicationContext().getAssets().open(apkName);
            File file = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/" + apkName);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(
                    Uri.parse("file://"
                            + Environment.getExternalStorageDirectory()
                            .getAbsolutePath() + "/" + apkName),
                    "application/vnd.android.package-archive");

            getApplicationContext().startActivity(intent);
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    /**
     * 监听应用安装完成的广播
     */
    private BroadcastReceiver mInstallReciver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction()
                    .equals("android.intent.action.PACKAGE_ADDED")
                    || intent.getAction()
                    .equals(Intent.ACTION_PACKAGE_REPLACED)) {
                String packageName = intent.getDataString();
                if (packageName.equals("package:com.google.android.gms")) {
                    installAPK("Google_Play.apk");
                } else if (packageName.equals("package:com.android.vending")) {
                    changeToGoogleMapView();
                }
            }
        }
    };


    private String getMapUrl(double latitude, double longitude) {
        return "http://restapi.amap.com/v3/staticmap?location=" + longitude + "," + latitude + "&zoom=16&scale=2&size=408*240&markers=mid,,A:" + longitude + "," + latitude + "&key=e09af6a2b26c02086e9216bd07c960ae";
    }


    private String getGoogleMapUrl(double latitude, double longitude) {
        return "http://maps.google.com/maps/api/staticmap?zoom=13&size=256x256&markers=" + longitude + "," + latitude + "&maptype=roadmap&sensor=false";
    }


}
