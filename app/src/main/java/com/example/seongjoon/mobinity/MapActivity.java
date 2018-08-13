package com.example.seongjoon.mobinity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.kakao.auth.Session;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback{

    private GoogleMap mMap;
    private Geocoder geocoder;
    private Button buttSearch;
    private Button buttBeforeItem;
    private Button buttNextItem;
    private Button buttBeforeReceiver;
    private Button buttNextReceiver;
    private Button buttCancelCheck;
    private Button buttOkCheck;
    private Button buttCancelRequest;
    private EditText startingPoint;
    private EditText destination;
    private ImageView userImage;
    private Location lastKnownLocation;
    private static final LatLng DEFAULT_LOCATION = new LatLng(-34, 151);
    private static final int DEFAULT_ZOOM = 15;

    private LinearLayout topPathLayout;
    private LinearLayout topMatchingLayout;
    private LinearLayout topStartDeliveryLayout;
    private LinearLayout topCompleteDeliveryLayout;
    private TableLayout tableLayoutItem;
    private TableLayout tableLayoutReceiver;
    private TableLayout tableLayoutCheck;
    private TableLayout tableLayoutMatching;
    private TableLayout tableLayoutMessage;
    private TableLayout tableLayoutInfo;
    private FloatingActionButton fab;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 사용자 프로필 및 이름 적용. //
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View navHeaderMap = navigationView.getHeaderView(0);

        TextView userName = navHeaderMap.findViewById(R.id.user_name);
        userImage = navHeaderMap.findViewById(R.id.user_img);

        userName.setText(User.getInstance().getUserName());
        LinkImage();
        //////////////////////////////////

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startingPoint = findViewById(R.id.starting_point);
        destination = findViewById(R.id.destination);
        buttSearch = findViewById(R.id.buttSearch);
        buttBeforeItem = findViewById(R.id.butt_before_item);
        buttNextItem = findViewById(R.id.butt_next_item);
        buttBeforeReceiver = findViewById(R.id.butt_before_receiver);
        buttNextReceiver = findViewById(R.id.butt_next_receiver);
        buttCancelCheck = findViewById(R.id.butt_cancel_check);
        buttOkCheck = findViewById(R.id.butt_ok_check);
        buttCancelRequest = findViewById(R.id.butt_cancel_request);
        tableLayoutItem = findViewById(R.id.table_layout_item);
        tableLayoutReceiver = findViewById(R.id.table_layout_receiver);
        tableLayoutCheck = findViewById(R.id.table_layout_check);
        tableLayoutMatching = findViewById(R.id.table_layout_matching);
        tableLayoutMessage = findViewById(R.id.table_layout_message);
        tableLayoutInfo = findViewById(R.id.table_layout_info);
        topPathLayout = findViewById(R.id.top_path_layout);
        topMatchingLayout = findViewById(R.id.top_matching_layout);
        topStartDeliveryLayout = findViewById(R.id.top_start_delivery_layout);
        topCompleteDeliveryLayout = findViewById(R.id.top_complete_delivery_layout);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "알림입니다.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // SupportMapFragment 생성 후, 맵이 사용될 준비가 됐다는 것을 알림.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //프로필 연동을 위한 부분//

    Handler handler = new Handler();    //카카오톡 이미지 연동 시 사용할 핸들러!

    //이미지 연동
    public void LinkImage(){
        String pImage = User.getInstance().getProfileImagePath();

        if(pImage.equals(""));
        else {
            new ImageDownload().execute(pImage);
            // 인터넷 상의 이미지 보여주기

            // 1. 권한을 획득한다 (인터넷에 접근할수 있는 권한을 획득한다)  - 메니페스트 파일
            // 2. Thread 에서 웹의 이미지를 가져오기
            // 3. 외부쓰레드에서 메인 UI에 접근위해 Handler 사용

            //Thread t = new Thread(Runnable 객체 생성);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {    // 오래 걸릴 작업 구현
                    // TODO Auto-generated method stub
                    try {
                        URL url = new URL(pImage);
                        InputStream is = url.openStream();
                        final Bitmap bm = BitmapFactory.decodeStream(is);
                        handler.post(new Runnable() {

                            @Override
                            public void run() {  // 화면에 그려줄 작업
                                userImage.setImageBitmap(bm);
                            }
                        });
                        userImage.setImageBitmap(bm); //비트맵 객체로 보여주기
                    } catch (Exception e) {
                    }
                }
            });
            t.start();
        }
    }
    //url 이미지 다운로드 (카카오프로필 이미지)
    private class ImageDownload extends AsyncTask<String, Void, Void> {
        private String fileName;/* 파일명 */
        private final String SAVE_FOLDER = "/Goal_Profile";/* 저장할 폴더 */

        @Override
        protected Void doInBackground(String... params) {
            //다운로드 경로를 지정
            String savePath = Environment.getExternalStorageDirectory().toString() + SAVE_FOLDER;

            File dir = new File(savePath);
            //상위 디렉토리가 존재하지 않을 경우 생성
            if (!dir.exists()) {
                dir.mkdirs();
            }

            fileName = String.valueOf(User.getInstance().getUserID());

            //웹 서버 쪽 파일이 있는 경로
            String fileUrl = params[0];

            //다운로드 폴더에 동일한 파일명이 존재하는지 확인
            /*if (new File(savePath + "/" + fileName).exists() == false) {
            } else {}*/

            String localPath = savePath + "/" + fileName + ".jpg";

            try {
                URL imgUrl = new URL(fileUrl);
                //서버와 접속하는 클라이언트 객체 생성
                HttpURLConnection conn = (HttpURLConnection)imgUrl.openConnection();
                int len = conn.getContentLength();
                byte[] tmpByte = new byte[len];
                //입력 스트림을 구한다
                InputStream is = conn.getInputStream();
                File file = new File(localPath);
                //파일 저장 스트림 생성
                FileOutputStream fos = new FileOutputStream(file);
                int read;
                //입력 스트림을 파일로 저장
                for (;;) {
                    read = is.read(tmpByte);
                    if (read <= 0) {
                        break;
                    }
                    fos.write(tmpByte, 0, read); //file 생성
                }

                is.close();
                fos.close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // 돌아가기
            topMatchingLayout.setVisibility(navigationView.INVISIBLE);
            topStartDeliveryLayout.setVisibility(navigationView.INVISIBLE);
            topCompleteDeliveryLayout.setVisibility(navigationView.INVISIBLE);
            tableLayoutItem.setVisibility(navigationView.INVISIBLE);
            tableLayoutReceiver.setVisibility(navigationView.INVISIBLE);
            tableLayoutCheck.setVisibility(navigationView.INVISIBLE);
            tableLayoutMatching.setVisibility(navigationView.INVISIBLE);
            tableLayoutMessage.setVisibility(navigationView.INVISIBLE);
            tableLayoutInfo.setVisibility(navigationView.INVISIBLE);

            topPathLayout.setVisibility(navigationView.VISIBLE);
            fab.setVisibility(navigationView.VISIBLE);

        } else if (id == R.id.nav_gallery) {
            // 배송출발
            topPathLayout.setVisibility(navigationView.INVISIBLE);
            topMatchingLayout.setVisibility(navigationView.INVISIBLE);
            topCompleteDeliveryLayout.setVisibility(navigationView.INVISIBLE);
            tableLayoutItem.setVisibility(navigationView.INVISIBLE);
            tableLayoutReceiver.setVisibility(navigationView.INVISIBLE);
            tableLayoutCheck.setVisibility(navigationView.INVISIBLE);
            tableLayoutMatching.setVisibility(navigationView.INVISIBLE);
            tableLayoutInfo.setVisibility(navigationView.INVISIBLE);
            fab.setVisibility(navigationView.INVISIBLE);

            topStartDeliveryLayout.setVisibility(navigationView.VISIBLE);
            tableLayoutMessage.setVisibility(navigationView.VISIBLE);

        } else if (id == R.id.nav_slideshow) {
            // 배송완료
            topPathLayout.setVisibility(navigationView.INVISIBLE);
            topMatchingLayout.setVisibility(navigationView.INVISIBLE);
            topStartDeliveryLayout.setVisibility(navigationView.INVISIBLE);
            tableLayoutItem.setVisibility(navigationView.INVISIBLE);
            tableLayoutReceiver.setVisibility(navigationView.INVISIBLE);
            tableLayoutCheck.setVisibility(navigationView.INVISIBLE);
            tableLayoutMatching.setVisibility(navigationView.INVISIBLE);
            tableLayoutMessage.setVisibility(navigationView.INVISIBLE);
            fab.setVisibility(navigationView.INVISIBLE);

            topCompleteDeliveryLayout.setVisibility(navigationView.VISIBLE);
            tableLayoutInfo.setVisibility(navigationView.VISIBLE);

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_setting) {

        }else if (id == R.id.nav_logout) {
            // 카카오톡으로 로그인 된 건지 확인.
            if (Session.getCurrentSession().isOpened()) {
                // 로그인 상태
                UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() {
                        // 로그아웃 시 로그인 페이지로 이동.
                        Intent intent = new Intent(MapActivity.this, LoginActivity.class);
                        startActivity(intent); // Move the next page.
                    }
                });
            } else {
                // 다른 경로로 로그인한 상태
                Intent intent = new Intent(MapActivity.this, LoginActivity.class);
                startActivity(intent); // Move the next page.
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        geocoder = new Geocoder(this);

        // 현위치에 마커 표시 후, 카메라 이동.
        getMyLocation();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();

                if(marker.getTitle().equals("도착지")){
                    View view = findViewById(R.id.nav_view);
                    Snackbar.make(view, "입력된 경로가 맞습니까?", Snackbar.LENGTH_LONG)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    fab.setVisibility(v.INVISIBLE);
                                    tableLayoutItem.setVisibility(v.VISIBLE);
                                }}).show();
                }
                return true;
            }
        });

        // 맵 터치 이벤트 구현 //
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng point) {
                MarkerOptions mOptions = new MarkerOptions();
                // 마커 타이틀
                mOptions.title("마커 좌표");
                Double latitude = point.latitude; // 위도
                Double longitude = point.longitude; // 경도
                // 마커의 스니펫(간단한 텍스트) 설정
                mOptions.snippet(latitude.toString() + ", " + longitude.toString());
                // LatLng: 위도 경도 쌍을 나타냄
                mOptions.position(new LatLng(latitude, longitude));
                // 마커(핀) 추가
                //googleMap.addMarker(mOptions);
            }
        });


        // (1). 물품 레이아웃 BEFORE 버튼 이벤트
        buttBeforeItem.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                tableLayoutItem.setVisibility(view.INVISIBLE);
                fab.setVisibility(view.VISIBLE);
            }
        });

        // (1). 물품 레이아웃 NEXT 버튼 이벤트
        buttNextItem.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                tableLayoutItem.setVisibility(view.INVISIBLE);
                tableLayoutReceiver.setVisibility(view.VISIBLE);
            }
        });

        // (2). 받는이 레이아웃 BEFORE 버튼 이벤트
        buttBeforeReceiver.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                tableLayoutReceiver.setVisibility(view.INVISIBLE);
                tableLayoutItem.setVisibility(view.VISIBLE);
            }
        });

        // (2). 받는이 레이아웃 NEXT 버튼 이벤트
        buttNextReceiver.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                tableLayoutReceiver.setVisibility(view.INVISIBLE);
                tableLayoutCheck.setVisibility(view.VISIBLE);
            }
        });

        // (3). 확인창 레이아웃 CANCEL 버튼 이벤트
        buttCancelCheck.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                tableLayoutCheck.setVisibility(view.INVISIBLE);
                fab.setVisibility(view.VISIBLE);
            }
        });

        // (3). 확인창 레이아웃 OK 버튼 이벤트
        buttOkCheck.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                topPathLayout.setVisibility(view.INVISIBLE);
                tableLayoutCheck.setVisibility(view.INVISIBLE);
                topMatchingLayout.setVisibility(view.VISIBLE);
                tableLayoutMatching.setVisibility(view.VISIBLE);
            }
        });

        // (4). 매칭중 레이아웃 요청취소 버튼 이벤트
        buttCancelRequest.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                topMatchingLayout.setVisibility(view.INVISIBLE);
                tableLayoutMatching.setVisibility(view.INVISIBLE);
                topPathLayout.setVisibility(view.VISIBLE);
                fab.setVisibility(view.VISIBLE);
            }
        });

        // 검색 버튼 이벤트
        buttSearch.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                if(startingPoint.getText().toString().equals("")){

                }

                // 키보드 내리기
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                String str=destination.getText().toString();
                List<Address> addressList = null;
                try {
                    // destination에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
                    addressList = geocoder.getFromLocationName(
                            str, // 주소
                            10); // 최대 검색 결과 개수
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println(addressList.get(0).toString());
                // 콤마를 기준으로 split
                String []splitStr = addressList.get(0).toString().split(",");
                String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2); // 주소
                System.out.println(address);

                String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
                String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도
                System.out.println(latitude);
                System.out.println(longitude);

                // 좌표(위도, 경도) 생성
                LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                // 마커 생성
                MarkerOptions mOptions2 = new MarkerOptions();
                mOptions2.title("도착지");
                mOptions2.snippet(address);
                mOptions2.position(point);

                // 마커 추가
                mMap.addMarker(mOptions2);
                // 해당 좌표로 화면 줌
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15));
            }
        });
    }

    private void getMyLocation() {
        if(ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(this);
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                lastKnownLocation = location;

                // 좌표(위도, 경도) 생성
                LatLng currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                String address = "해당되는 주소 정보는 없습니다"; // 주소

                // 위도,경도 입력 후 변환 버튼 클릭
                List<Address> list = null;
                try {
                    list = geocoder.getFromLocation(
                            lastKnownLocation.getLatitude(), // 위도
                            lastKnownLocation.getLongitude(), // 경도
                            10); // 얻어올 값의 개수
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("test", "입출력 오류 : 위경도-주소변환 에러발생");
                }
                if (list != null) {
                    if (list.size()!=0) {
                        String []splitStr = list.get(0).toString().split(",");
                        address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2); // 주소
                    }
                }

                // 마커 생성
                MarkerOptions mOptions3 = new MarkerOptions();
                mOptions3.title("현위치");
                mOptions3.snippet(address);
                mOptions3.position(currentLocation);
                // 마커 추가
                mMap.addMarker(mOptions3);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        });
    }
}
