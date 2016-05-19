package com.example.user.simpleui;

import android.Manifest;
import android.app.Fragment;
import android.app.admin.SystemUpdatePolicy;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class OrderDetailActivity extends AppCompatActivity implements GeocodingTaskResponse
        , RoutingListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {

    TextView note;
    TextView storeInfo;
    TextView menuResults;
    ImageView photo;
    ImageView mapImageView;
    String storeName;
    String address;

    MapFragment mapFragment;
    private GoogleMap googleMap;
    private ArrayList<Polyline> polylines;
    private LatLng storeLocation;
    private GoogleApiClient mGoogleApiClient;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        note = (TextView) findViewById(R.id.note);
        storeInfo = (TextView) findViewById(R.id.storeInfo);
        menuResults = (TextView) findViewById(R.id.menuResults);
        photo = (ImageView) findViewById(R.id.photoImageView);
        mapImageView = (ImageView) findViewById(R.id.mapImageView);

        Intent intent = getIntent();
        note.setText(intent.getStringExtra("note"));
        storeInfo.setText(intent.getStringExtra("storeInfo"));

        String[] info = intent.getStringExtra("storeInfo").split(",");
        storeName = info[0];
        address = info[1];

        String results = intent.getStringExtra("menuResults");
        String text = "";
        try {
            JSONArray jsonArray = new JSONArray(results);
            for (int i = 0; i <= jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                text += object.getString("name") + " : 大杯" + object.getString("l") + "杯   中杯" + object.getString("m") + "杯" + "\n";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        menuResults.setText(text);

        String url = intent.getStringExtra("photoURL");

        if (!url.equals("")) {
            //使用Picasso套件
//            Picasso.with(this).load(url).into(photo);
            //顯示圖片
            new ImageLoadingTask(photo).execute(url);
            //顯示google取得的地圖
//            (new GeoCodingTask(photo)).execute("台北市羅斯福力四段一號");

            //memirt leak
//            for(int i = 0 ; i<=10 ; i++){
//                Thread t = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        while(true){
//                            SystemClock.sleep(10000);
//                        }
//                    }
//                });
//            }
        }
//        (new GeoCodingTask(mapImageView)).execute(address);

        //取得 Fragment，要用FragmentManager -> 子 activity
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMapFragment);
        //取得 google map
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                (new GeoCodingTask(OrderDetailActivity.this)).execute(address);
                googleMap = map;
            }
        });


        scrollView = (ScrollView)findViewById(R.id.scrollView);

        ImageView transparentImageView = (ImageView) findViewById(R.id.imageView2);
        transparentImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("Debug", "Touch it");
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });

    }

    @Override
    public void responseWithGeocodingResults(LatLng location) {
        //判斷使用者是否同意拿取位置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17));
                //加上標籤
                googleMap.addMarker(new MarkerOptions().position(location));
                //提供路線
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                return;
            }
        }

        storeLocation = location;
        googleMap.setMyLocationEnabled(true);   //回到自己的位置

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }


    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int index) {
        if (polylines != null) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < routes.size(); i++) {

            //In case of more than 5 alternative routes

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(Color.BLUE);
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(routes.get(i).getPoints());
            Polyline polyline = googleMap.addPolyline(polyOptions);

            polylines.add(polyline);

//            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ routes.get(i).getDistanceValue()+": duration - "+ routes.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    @Override
    public void onConnected(Bundle bundle) {
        //得到位置
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        //若改變，需要重新設定Camera，所以需要重新作一個CallBack
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, LocationRequest.create(),this);

        LatLng start = new LatLng(25.018390, 121.533229);

        if (location != null) {
            start = new LatLng(location.getLatitude(), location.getLongitude());
            //設定相機
            CameraUpdate center = CameraUpdateFactory.newLatLng(start);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(17);
            googleMap.moveCamera(center);
            googleMap.animateCamera(zoom);

        }

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.WALKING)
                .waypoints(start, storeLocation)
                .withListener(this)
                .build();

        routing.execute();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng start = new LatLng(location.getLatitude(), location.getLongitude());
        //設定相機
        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(17);
        googleMap.moveCamera(center);
        googleMap.animateCamera(zoom);

    }

    private static class GeoCodingTask extends AsyncTask<String, Void, double[]> {
        //ImageView imageView;
        GoogleMap googleMap;
        private final WeakReference<GeocodingTaskResponse> geocodingTaskResponseWeakReference;


        @Override
        protected double[] doInBackground(String... params) {
            String address = params[0];
            double[] latlng = Utils.addressToLatLng(address);
//            Bitmap bitmap = Utils.getStaticMap(latlng);
//            return bitmap;
            return latlng;
        }

        @Override
        protected void onPostExecute(double[] latIng) {
            if (latIng != null && geocodingTaskResponseWeakReference.get() != null) {
                LatLng storeLocation = new LatLng(latIng[0], latIng[1]);
                GeocodingTaskResponse response = geocodingTaskResponseWeakReference.get();
                response.responseWithGeocodingResults(storeLocation);
            }

//            super.onPostExecute(bitmap);
//            if (bitmap != null) {
//                imageView.setImageBitmap(bitmap);
//            }
        }

//        public GeoCodingTask(ImageView imageView) {
//            this.imageView = imageView;
//        }

        public GeoCodingTask(GeocodingTaskResponse response) {
//            this.imageView = imageView;
            this.geocodingTaskResponseWeakReference = new WeakReference<GeocodingTaskResponse>(response);
        }
    }

    private static class ImageLoadingTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            byte[] bytes = Utils.urlToBytes(url);
            if (bytes != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                return bitmap;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }


        public ImageLoadingTask(ImageView imageView) {
            this.imageView = imageView;
        }
    }

}

interface GeocodingTaskResponse {
    void responseWithGeocodingResults(LatLng location);
}