package com.example.week11_assignment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.week11_assignment.databinding.ActivityMapsBinding;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    Geocoder coder;
    double distance;
    int btnClickcnt = 0;
    int markerCnt = 0;
    double sum = 0;

    ArrayList<Double> LatLngList = new ArrayList<>();
    ArrayList<String> timeList = new ArrayList<>();
    ArrayList<Double> nowLatlist = new ArrayList<>(); // 현재 위도 저장
    ArrayList<Double> nowLnglist = new ArrayList<>(); // 현대 경도 저장
    ArrayList<Double> distanceList = new ArrayList<>(); // 이전 마커로부터 현재 마커까지의 이동 거리 기록
    ArrayList<Double> distanceSumList = new ArrayList<>(); // 총 이동거리 기록

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        coder = new Geocoder(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        TextView txtView = (TextView) findViewById(R.id.distance);
        TextView txtView2 = (TextView) findViewById(R.id.DistanceSum);

        txtView.setText("산책 경로 저장을 시작하시려면 아래 버튼을 눌러주세요!");

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();

                String currentPosTitle = "현재 위치";
                LatLng currentPos = new LatLng(lat, lng);

                Button button = (Button)findViewById(R.id.btn);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 18));
                        mMap.addMarker(new MarkerOptions().position(currentPos).title(currentPosTitle));

                        // 현재 시각 구하기
                        long currentTime = System.currentTimeMillis();
                        Date date = new Date(currentTime);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
                        String getTime = dateFormat.format(date);

                        btnClickcnt++;

                        nowLatlist.add(lat); // 현재 위도 arraylist에 저장
                        nowLnglist.add(lng); // 현대 경도 arraylist에 저장

                        if(btnClickcnt == 1) {
                            // 최초로 클릭했을 때
                            timeList.add(getTime); // 버튼 누른 시점의 시간 리스트에 str 형태로 저장
                            LatLngList.add(lat); // 최초 클릭 시 현재 위도와 경도 순서대로 arraylist에 저장
                            LatLngList.add(lng);
                            txtView.setText("경로 저장을 시작합니다!");
                            button.setText("현재 위치에 마커 추가");
                            markerCnt++;
                        }
                        else if(btnClickcnt >= 2) {
                            timeList.add(getTime); // 버튼 누른 시점의 시간 리스트에 str 형태로 저장

                            LatLng previousPos = new LatLng(LatLngList.get(0), LatLngList.get(1));
                            // 이전에 LatLngList에 저장했던 위도와 경도 불러와서 이전 위치로 지정 및 저장
                            LatLng currentPosition = new LatLng(lat, lng); // 새롭게 갱신된 현재의 위도와 경도를 currentPosition에 저장

                            LatLngList.clear(); // 그렇게 '직전 마커의 위치'로서 역할을 다 한 위도 경도를 arraylist에서 지워주고

                            LatLngList.add(lat); // 현재 위치를 다음 클릭 시에 previousPos로 사용하기 위해 arraylist에 저장
                            LatLngList.add(lng);

                            Location locationA = new Location("A"); // 내가 지금 도착한 위치
                            locationA.setLatitude(lat);
                            locationA.setLongitude(lng);

                            Location locationB = new Location("B"); // 내가 직전에 찍었던 마커의 좌표
                            locationB.setLatitude(previousPos.latitude);
                            locationB.setLongitude(previousPos.longitude);

                            distance = locationA.distanceTo(locationB);
                            distanceList.add(distance);
                            txtView.setText("이전 마커에서 현재 마커까지 이동거리 : " + String.format("%.3f", distance * 0.001) + "km");
                            // distanceTo를 통해 구하는 거리는 m 단위

                            // 총 이동거리 계산해서 출력
                            sum += distance;
                            distanceSumList.add(sum);
                            txtView2.setText("경로 개시 시점부터의 총 이동거리 : " + String.format("%.3f", sum * 0.001) + "km");
                            // 거리를 소수점 4번째 자리에서 반올림해서 셋째자리까지 출력

                            if (distance > 0) {
                                markerCnt++;
                            } // 사용자가 이동해서 마커가 생긴 경우에만 markerCnt가 늘어나도록 조건 설정

                            if ((markerCnt >= 10)&&(sum * 0.001 >= 2)) {
                                txtView2.setText("경로 개시 시점부터의 총 이동거리 : " + String.format("%.3f", sum * 0.001) + "km" +
                                        "\n\n축하합니다! 오늘의 목표를 달성했어요!");
                                // 마커가 10개 이상 찍혔으면서 총 이동 거리가 2km 이상이면 오늘의 목표를 달성했다는 메세지를 추가로 띄워줌
                                // 걸으면서 마커 몇 개 찍었나 일일이 기억하는 게 사용자에게 조금 번거로울 것 같아 조건과 메세지를 추가해줌
                            }
                        }
                    }
                });
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    // 앱 실행 시 구글맵 초기 위치 인하대로 지정
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        double inha_lat = 37.44965000; // 위도
        double inha_lon = 126.65304444; // 경도
        LatLng inhaPos = new LatLng(inha_lat, inha_lon);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(inhaPos, 15));
    }
}