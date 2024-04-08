package com.example.tokyo2;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final GeoApiContext geoApiContext = new GeoApiContext.Builder()
            .apiKey("Google APIキー")
            .build();
    private final Map<Marker, Integer> markerTapCounts = new HashMap<>();
    private LatLng start;//出発地点
    public LatLng destination;//到着地点
    public List routePoints;
    public DirectionsResult result;
    public DirectionsRoute route;
    public GoogleMap Map;
    public List<LatLng> LatLngList;
    public List<LatLng> selectedPinsList;
    public MapView mView;
    public Polyline routePolyline; // 経路のPolyline
    public Marker tappedMarker;
    public List<LatLng> shareCycleLocations;
    private Map<Marker, MarkerInfo> markerInfoMap = new HashMap<>();
    private static final int PERMISSIONS_REQUEST_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationClient;

    public int Anime_select_id;
    private  String csvFilePath =null;

    public Marker location_marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        Intent intent = getIntent();
        String animeselect_str = intent.getStringExtra("id");
        Anime_select_id = Integer.parseInt(animeselect_str);
        Log.d("Directions API", "選択key" +Anime_select_id);

        select_csv();


        // markerInfoMapの初期化
        markerInfoMap = new HashMap<>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 位置情報のパーミッションをリクエストするコード
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        } else {
            // パーミッションが既に許可されている場合、位置情報を取得する処理へ進む
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }

        //ボタンを押したらgenerateRouteが起動
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRoutes();//以前のルートを削除
                selectedPinsList = readDataFromLocal("selected_pins.csv"); // 別のCSVファイル名を指定
                for (LatLng latLng : selectedPinsList) {
                    Log.d("CSV LatLng", "緯度経度 " + latLng);
                }
                // DirectionAPIのdestinationの緯度経度をログに出力
                Log.d("Directions API", "generateRoute()の前:" + destination.latitude + "," + destination.longitude);
                generateRoute(); // ルート生成処理を呼び出す
                // DirectionAPIのdestinationの緯度経度をログに出力
                Log.d("Directions API", "generateRoute()実行後:" + destination.latitude + "," + destination.longitude);

                deleteAllCSVFiles();// 内部ディレクトリのCSVファイルを削除

                try {
                    long totalDurationMinutes = 0;

                    // 各中継地点までの所要時間を計算
                    for (int i = 0; i < route.legs.length; i++) {
                        long segmentDurationSeconds = route.legs[i].duration.inSeconds;
                        long segmentDurationMinutes = segmentDurationSeconds / 60;
                        totalDurationMinutes += segmentDurationMinutes;

                        // Log.d("Directions API", "Segment " + (i + 1) + " Duration: " + segmentDurationMinutes + " minutes");
                    }

                    // 最終的な目的地までの合計所要時間を計算
                    long totalHours = totalDurationMinutes / 60;
                    long totalMinutes = totalDurationMinutes % 60;
                    // Log.d("Directions API", "Total Duration: " + totalHours + " hours " + totalMinutes + " minutes");

                    // 合計所要時間を Toast で表示
                    // Toast.makeText(getApplicationContext(), "Total Duration: " + totalHours + " hours " + totalMinutes + " minutes", Toast.LENGTH_SHORT).show();
                    // 合計所要時間とメッセージをカスタムダイアログで表示
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this); // ここで明示的にMainActivity.thisを指定する
                    builder.setTitle("Total Duration")
                            .setMessage(totalHours + " hours " + totalMinutes + " minutes\n\nEnjoy your pilgrimage to sacred anime sites in Tokyo based on the suggested routes!")
                            .setPositiveButton("OK", null);
                    Dialog dialog = builder.create();
                    dialog.show();

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("Directions API", "Failed to retrieve route.");
                }
            }
        });
        mView = findViewById(R.id.mapView);
        mView.onCreate(savedInstanceState);
        mView.getMapAsync(this);

    }


    public String select_csv() {
        if(Anime_select_id == 1) {
            csvFilePath = "anime_location_test.csv" ; // CSVファイルのパスを指定
        }else  if(Anime_select_id == 2){
            csvFilePath = "museum_location.csv" ; // CSVファイルのパスを指定
        }else  if(Anime_select_id == 3) {
            csvFilePath = "evacuation_location.csv"; // CSVファイルのパスを指定
        }else{
            Log.d("Directions API", "csvが変数に読み込まれていません。");
        }
        Log.d("Directions API",csvFilePath );
        return csvFilePath;

    }

    // CSVファイルから3列目の1行目から順にタイトルを取得するメソッド
    private List<String> readMarkerTitlesFromCSV() {
        List<String> markerTitles = new ArrayList<>();
        try {
            InputStream inputStream = getAssets().open(csvFilePath);// CSVファイル名を指定
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(","); // CSVのカラム区切り文字に合わせて変更
                if (parts.length >= 3) { // 3列目が存在するか確認
                    String markerTitle = parts[2].trim(); // 3列目のデータを取得
                    markerTitles.add(markerTitle);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return markerTitles;
    }

    // 既存のCSVファイルから緯度経度を取得してリストに格納するメソッド
    private List<LatLng> readLatLngFromCSV(String fileName) {
        List<LatLng> latLngList = new ArrayList<>();
        try {
            InputStream inputStream = getAssets().open(fileName);// CSVファイル名を指定
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(","); // CSVのカラム区切り文字に合わせて変更
                double latitude = Double.parseDouble(parts[0]); // CSVの緯度カラムに合わせて変更
                double longitude = Double.parseDouble(parts[1]); // CSVの経度カラムに合わせて変更
                LatLng latLng = new LatLng(latitude, longitude);
                latLngList.add(latLng);
            }
            // ファイルを閉じる
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return latLngList;
    }

    // 内部ディレクトリからcsvを呼び出して読み込むコード
    private List<LatLng> readDataFromLocal(String fileName) {
        List<LatLng> latLngList = new ArrayList<>();
        try {
            // 内部ディレクトリからファイルを読み込む
            File file = new File(getFilesDir(), fileName);
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(","); // CSVのカラム区切り文字に合わせて変更
                double latitude = Double.parseDouble(parts[0]); // CSVの緯度カラムに合わせて変更
                double longitude = Double.parseDouble(parts[1]); // CSVの経度カラムに合わせて変更
                LatLng latLng = new LatLng(latitude, longitude);
                latLngList.add(latLng);
            }

            // ファイルを閉じる
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return latLngList;
    }


    private void generateRoute() {
        String S1 = "35.70484674,139.6511684";
        String G1 = "35.7190537,139.7053959";
        String L1 = "35.69717596,139.7058908";
        String L2 = "35.7177348,139.7138699";

        //directionAPI経由でgoogleから経路データがjsonで帰ってきて、それを基にルートポリゴン生成
        if (selectedPinsList == null) {
            try {
                //下はあらかじめ2点を決めておいたらうまくいったバージョンのコード。
                result = DirectionsApi.newRequest(geoApiContext)
                        .origin(S1)
                        .waypoints(L1, L2)
                        .destination(G1)
                        .mode(TravelMode.BICYCLING)
                        .await();

                route = result.routes[0];
                routePoints = new ArrayList<>();
                for (com.google.maps.model.LatLng step : route.overviewPolyline.decodePath()) {
                    routePoints.add(new LatLng(step.lat, step.lng));
                }
                Log.d("Directions API", "経路が取得されました。");
                Log.d("Directions API", routePoints.toString());
                Log.d("Directions API", route.toString());
                Log.d("Directions API", "Route Response: " + result.toString());

                routePolyline = Map.addPolyline(new PolylineOptions()
                        .addAll(routePoints)
                        .width(10)
                        .color(Color.BLUE));

            } catch (ApiException e) {
                e.printStackTrace();
                Log.d("Directions API", "Directions APIエラー: " + e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d("Directions API", "InterruptedException: " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Directions API", "IOException: " + e.getMessage());
            }
        } else {
            //ピンを選択肢してボタンを押した場合
            try {
                // CSVファイルから目的地の緯度経度とタイトルを取得
                List<LatLng> relayLocations = readLatLngFromCSV(csvFilePath);
                // シェアサイクル場所の緯度経度リストを取得
                List<LatLng> shareCycleLocations = readLatLngFromCSV("cycling_location.csv");
                // hotelの緯度経度リストを取得
                List<LatLng> hotelLocation = readLatLngFromCSV("hotel_location.csv");
                // selectedPinsList リスト内の LatLng オブジェクトを com.google.maps.model.LatLng に変換
                List<com.google.maps.model.LatLng> waypoints = new ArrayList<>();

                // 最も近い中継地点の緯度経度を計算するメソッドを呼び出し
                LatLng nearestShareCycleLocation = calculateNearestLocation(shareCycleLocations, start);

                // 最初に shareCycleLocations の要素を追加
                waypoints.add(new com.google.maps.model.LatLng(nearestShareCycleLocation.latitude, nearestShareCycleLocation.longitude));

                // 次に selectedPinsList の要素を追加
                for (LatLng latLng : selectedPinsList) {
                    waypoints.add(new com.google.maps.model.LatLng(latLng.latitude, latLng.longitude));
                }


                // DirectionsResult から DirectionsRoute を取得
                result = DirectionsApi.newRequest(geoApiContext)
                        .origin(new com.google.maps.model.LatLng(start.latitude, start.longitude))
                        .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                        .mode(TravelMode.BICYCLING)
                        .waypoints(waypoints.toArray(new com.google.maps.model.LatLng[0]))
                        .optimizeWaypoints(true)  // 最適な順序で巡回するように設定
                        .await();

                // 合計所要時間を計算し、中継地点の順序と所要時間をログに表示
                for (int i = 0; i < result.routes[0].legs.length; i++) {
                    long segmentDurationSeconds = result.routes[0].legs[i].duration.inSeconds;
                    long segmentMinutes = segmentDurationSeconds / 60;
                    long segmentSeconds = segmentDurationSeconds % 60;
                    Log.d("Directions API", "中継地点 " + (i + 1) + " までの所要時間: " + segmentMinutes + "分 " + segmentSeconds + "秒");
                }

                route = result.routes[0];
                routePoints = new ArrayList<>();
                for (com.google.maps.model.LatLng step : route.overviewPolyline.decodePath()) {
                    routePoints.add(new LatLng(step.lat, step.lng));
                }

                // ルート表示の設定
                routePolyline = Map.addPolyline(new PolylineOptions()
                        .addAll(routePoints)
                        .width(15)
                        .color(Color.BLUE));

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Directions API", "ソート経路が取得できませんでした。");
            }
        }

    }

    //マップ表示
    @Override
    public void onMapReady(GoogleMap mMap) {
        //public googlemap Map と　onMapReadyで使っているmMapをおんなじものと認識させる
        Map = mMap;

        // getLastLocation() メソッドを呼び出す
        getLastLocation();
    }

    private void deleteRoutes() {
        // 既存のルートを削除
        try {
            if (routePolyline != null) {
                routePolyline.setVisible(false);
                routePolyline = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //内部ディレクトリの削除プログラム
    private void deleteAllCSVFiles() {
        File[] files = getFilesDir().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".csv")) {
                    file.delete();
                }
            }
            Log.d("CSV Delete", "内部ディレクトリのCSVファイルをすべて削除しました。");
        } else {
            Log.d("CSV Delete", "内部ディレクトリにCSVファイルは存在しません。");
        }
    }

    //Toastが上手く表示されないためAlerDialog.Builderに変更。
    private void showMarkerTapInstructions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Marker Tap Instructions")
                .setMessage("Tap a marker to decide where you want to go !")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private LatLng calculateNearestLocation(List<LatLng> locations, LatLng targetLocation) {
        LatLng nearestLocation = null;
        double shortestDistance = Double.MAX_VALUE;

        for (LatLng location : locations) {
            double distance = getDistance(location, targetLocation);
            if (distance < shortestDistance) {
                shortestDistance = distance;
                nearestLocation = location;
            }
        }
        return nearestLocation;
    }

    private double getDistance(LatLng location1, LatLng location2) {
        double lat1 = location1.latitude;
        double lon1 = location1.longitude;
        double lat2 = location2.latitude;
        double lon2 = location2.longitude;

        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515 * 1.609344;

        return dist;
    }

    private void showShareCycleLocationsOnMap(List<LatLng> shareCycleLocations, GoogleMap mMap) {

        // カスタムマーカーの画像を読み込む（ここでは仮の画像名 "custom_marker" とします）
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cycling3);
        // リサイズ後のサイズを指定
        int width = 50;  // ピクセル単位で幅を指定
        int height = 50; // ピクセル単位で高さを指定
        // リサイズした画像を作成
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);
        // カスタムマーカーを作成
        BitmapDescriptor customMarker = BitmapDescriptorFactory.fromBitmap(resizedBitmap);
        BitmapDescriptor shareCycleIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);

        for (LatLng location : shareCycleLocations) {
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .icon(customMarker));
        }
    }

    // ピンが指定されたCSVファイルに存在するかをチェックするメソッド
    private boolean isPinInCSV(Marker marker, String csvFileName) {
        LatLng markerLocation = marker.getPosition();
        List<LatLng> pinLocations = readLatLngFromCSV(csvFileName);
        if (pinLocations != null) {
            for (LatLng pinLocation : pinLocations) {
                if (pinLocation.latitude == markerLocation.latitude && pinLocation.longitude == markerLocation.longitude) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean isPinInWaypoints(Marker clickedMarker, List<LatLng> relayLocations) {
        if (clickedMarker != null && relayLocations != null) {
            LatLng markerPosition = clickedMarker.getPosition();
            for (LatLng waypoint : relayLocations) {
                if (waypoint != null && waypoint.equals(markerPosition)) {
                    return true; // ピンが relayLocations リスト内に存在する場合、true を返す
                }
            }
        }
        return false; // ピンが relayLocations リスト内に存在しない場合、false を返す
    }





    // もともとonMapReadyの中身
    private void getLastLocation() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 位置情報の取得をリクエスト
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        // 現在の位置情報GPSよりを取得し、start 変数に設定する
                        start = new LatLng(location.getLatitude(), location.getLongitude());
                        // ゴール地点（destination）、および経由地点の位置情報を設定
                        destination = new LatLng(location.getLatitude(), location.getLongitude());
                        // DirectionAPIのdestinationの緯度経度をログに出力
                        Log.d("Directions API", "①getLastLocatio()に入った直後:" + destination.latitude + "," + destination.longitude);

                        //ピンの色を設定
                        BitmapDescriptor defaultIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                        // タップ後のピンの色
                        BitmapDescriptor tappedIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);

                        // 現在地のピンをマップに追加
                        Marker start_marker = Map.addMarker(new MarkerOptions().position(start).title("current location").icon(defaultIcon));
                        markerTapCounts.put(start_marker, 0);

                        // 最初の注意画面ポップアップ表示の関数
                        showMarkerTapInstructions();


                        // ホテルの緯度経度リストを取得
                        List<LatLng> hotelLocation = readLatLngFromCSV("hotel_location.csv");
                        if (hotelLocation != null && !hotelLocation.isEmpty()) {
                            for (LatLng latLng : hotelLocation) {
                                Marker hotelMarker = Map.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title("Hotel & Inns")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                            }
                        }



                        //Toast.makeText(getApplicationContext(), "Tap a marker to decide where you want to go!", Toast.LENGTH_LONG).show();
                        // 指定の位置に移動
                        Map.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 13)); // ズームレベル 1 〜 20 の範囲で指定
                        markerTapCounts.put(start_marker, 0);

                        // CSVファイルから目的地の緯度経度とタイトルを取得
                        List<LatLng> relayLocations = readLatLngFromCSV(csvFilePath);
                        // シェアサイクル場所の緯度経度リストを取得
                        List<LatLng> shareCycleLocations = readLatLngFromCSV("cycling_location.csv");
                        // シェアサイクルの場所を青いピンでマップに表示
                        showShareCycleLocationsOnMap(shareCycleLocations, Map);


                        //ピンのソートプログラム
                        Map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            //////田中記入
                            @Override
                            public boolean onMarkerClick(Marker clickedMarker) {
                                MarkerInfo markerInfo = markerInfoMap.get(clickedMarker);

                                if (markerInfo != null) {
                                    String description = markerInfo.getDescription();
                                    int imageResId = markerInfo.getImageResId();

                                    // カスタムポップアップのレイアウトを取得
                                    View popupView = getLayoutInflater().inflate(R.layout.custom_popup, null);

                                    // カスタムポップアップの内容を設定
                                    TextView descriptionTextView = popupView.findViewById(R.id.descriptionTextView);
                                    ImageView imageView = popupView.findViewById(R.id.imageView);

                                    // マーカーに関連する情報をビューに設定
                                    String markerDescription = markerInfo.getDescription(); // マーカーの説明を取得

                                    descriptionTextView.setText(markerDescription); // 説明を TextView に設定
                                    imageView.setImageResource(imageResId); // 画像を ImageView に設定


                                    // AlertDialog.Builder を使用してダイアログを作成
                                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                    dialogBuilder.setView(popupView);

                                    dialogBuilder.setPositiveButton("close", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                                    AlertDialog dialog = dialogBuilder.create();
                                    dialog.show();

                                    // ポップアップの表示位置を調整
                                    LatLng markerLatLng = clickedMarker.getPosition();
                                    Point markerScreenPosition = Map.getProjection().toScreenLocation(markerLatLng);
                                    WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
                                    layoutParams.gravity = Gravity.TOP | Gravity.START;
                                    layoutParams.x = markerScreenPosition.x;
                                    layoutParams.y = 0;
                                    dialog.getWindow().setAttributes(layoutParams);


                                    try {
                                        // 中継地点となる最も近いシェアサイクル場所の緯度経度を計算
                                        LatLng nearestShareCycleLocation = calculateNearestLocation(shareCycleLocations, clickedMarker.getPosition());
                                        result = DirectionsApi.newRequest(geoApiContext)
                                                .origin(new com.google.maps.model.LatLng(start.latitude, start.longitude))
                                                .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                                                .waypoints(
                                                        new com.google.maps.model.LatLng(nearestShareCycleLocation.latitude, nearestShareCycleLocation.longitude),
                                                        new com.google.maps.model.LatLng(relayLocations.get(1).latitude, relayLocations.get(1).longitude)
                                                )
                                                .optimizeWaypoints(true)  // 最適な順序で巡回するように設定
                                                .await();

                                        // DirectionAPIのdestinationの緯度経度をログに出力
                                        Log.d("Directions API", "②もともとのホテルの位置:" + destination.latitude + "," + destination.longitude);

                                        // Directions API で得られたルート情報から、最後の中継地点の緯度経度を取得
                                        com.google.maps.model.LatLng lastWaypoint = result.routes[0].legs[result.routes[0].legs.length - 2].endLocation;
                                        // DirectionAPIのdestinationの緯度経度をログに出力
                                        Log.d("Directions API", "③最後の中継地点:" + lastWaypoint.lat + "," + lastWaypoint.lng);

                                        // 最も近いホテルの位置を計算
                                        LatLng nearestHotelLocation = calculateNearestLocation(hotelLocation, new LatLng(lastWaypoint.lat, lastWaypoint.lng));

                                        // ゴール地点を最も近いホテルの位置に設定

                                        destination = nearestHotelLocation;
                                        // DirectionAPIのdestinationの緯度経度をログに出力
                                        Log.d("Directions API", "④もっとも近いホテルの位置をゴールに:" + destination.latitude + "," + destination.longitude + "\n ");


                                    } catch (InterruptedException | IOException | com.google.maps.errors.ApiException e) {
                                        e.printStackTrace();
                                    }
                                }


                                // タップされたピンのカウントを増やす
                                Integer tapCount = markerTapCounts.get(clickedMarker);
                                if (tapCount != null) {
                                    tapCount = (tapCount + 1) % 2; // 1と0を繰り返す
                                    markerTapCounts.put(clickedMarker, tapCount);
                                } else {
                                    tapCount = 1;
                                    markerTapCounts.put(clickedMarker, tapCount);
                                }

                                // タップされたピンの色を変更
                                if (tapCount == 1) {
                                    boolean isAnimeLocationPin = isPinInCSV(clickedMarker, csvFilePath);
                                    boolean isWaypointPin = isPinInWaypoints(clickedMarker, relayLocations);

                                    if (isAnimeLocationPin || isWaypointPin) {
                                        clickedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)); // 緑色のアイコンに変更
                                    }
                                }


                                Log.d("Marker Tap Count", "Marker: " + clickedMarker.getTitle() + ", Count: " + tapCount);


                                // tapして緑色に変わったピンの緯度経度を内部ディレクトリにCSVで保存
                                if (tapCount == 1) {
                                    boolean isAnimeLocationPin = isPinInCSV(clickedMarker, "anime_location.csv");
                                    boolean isWaypointPin = isPinInWaypoints(clickedMarker, relayLocations);

                                    if (isAnimeLocationPin || isWaypointPin) {
                                        clickedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)); // 緑色のアイコンに変更
                                        tappedMarker = clickedMarker; // 緑色に変更されたピンを保存

                                        // タップされた緑色のピンの緯度経度を内部ディレクトリにCSVで保存
                                        LatLng latLng = clickedMarker.getPosition();
                                        String csvLine = latLng.latitude + "," + latLng.longitude + "\n";
                                        try {
                                            FileWriter fileWriter = new FileWriter(new File(getFilesDir(), "selected_pins.csv"), true);
                                            fileWriter.append(csvLine);
                                            fileWriter.close();
                                            Log.d("new_file", "緑色のピンを保存: " + csvLine);
                                        } catch (IOException e) {
                                            Log.d("new_file", "保存に失敗しました");
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                return false;
                            }
                        });


                        // CSVファイルから目的地の緯度経度とタイトルを取得しリスト化
                        List<LatLng> latLngList = readLatLngFromCSV(csvFilePath);
                        List<String> markerTitles = readMarkerTitlesFromCSV(); // 3列目のタイトルを取得

                        if (latLngList != null && !latLngList.isEmpty() && markerTitles != null && !markerTitles.isEmpty()) {
                            Log.d("CSV Read", "CSVファイルから緯度経度とタイトルを取得しました。");

                            // マーカーに関連する情報を作成し、markerInfoMap に追加
                            for (int i = 0; i < latLngList.size(); i++) {
                                LatLng latLng = latLngList.get(i);
                                String markerTitle = markerTitles.get(i);
                                location_marker = Map.addMarker(new MarkerOptions().position(latLng).title(markerTitle));
                                markerTapCounts.put(location_marker, 0);

                                // 各マーカーに異なる情報を設定
                                if (Anime_select_id == 1) {
                                    int imageResourceId = getResources().getIdentifier("w" + (i + 1), "drawable", getPackageName());
                                    String additionalInfo = "追加情報" + (i + 1);
                                    MarkerInfo markerInfo = new MarkerInfo(markerTitle, imageResourceId, additionalInfo);
                                    markerInfoMap.put(location_marker, markerInfo);
                                } else if(Anime_select_id == 2) {
                                    int imageResourceId = getResources().getIdentifier("museum" + (i + 1), "drawable", getPackageName());
                                    String additionalInfo = "追加情報" + (i + 1);
                                    MarkerInfo markerInfo = new MarkerInfo(markerTitle, imageResourceId, additionalInfo);
                                    markerInfoMap.put(location_marker, markerInfo);
                                } else if(Anime_select_id == 3) {
                                    int imageResourceId = getResources().getIdentifier("museum" + (i + 1), "drawable", getPackageName());
                                    String additionalInfo = "追加情報" + (i + 1);
                                    MarkerInfo markerInfo = new MarkerInfo(markerTitle, imageResourceId, additionalInfo);
                                    markerInfoMap.put(location_marker, markerInfo);
                                }
                            }

                        } else {
                            Log.d("CSV Read", "CSVファイルから緯度経度とタイトルを取得できませんでした。");
                        }

                    }
                }
            });
        } else {

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mView.onLowMemory();
    }

}
