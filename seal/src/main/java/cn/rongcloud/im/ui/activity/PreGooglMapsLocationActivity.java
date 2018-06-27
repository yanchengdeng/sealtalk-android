package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.dbcapp.club.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import io.rong.message.LocationMessage;

/**
*
* Author: 邓言诚  Create at : 2018/6/25  00:04
* Email: yanchengdeng@gmail.com
* Describle: 查看google  经纬度信息
*/
public class PreGooglMapsLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    double lat;
    double lng;
    String poi;
    private TextView tvlocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_googl_maps_location);
        tvlocation = findViewById(R.id.tv_locaion);


        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = this.getIntent();
        LocationMessage locationMessage = (LocationMessage)intent.getParcelableExtra("location");
         lat = locationMessage.getLat();
         lng = locationMessage.getLng();
         poi = locationMessage.getPoi();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title(poi));
    }
}
