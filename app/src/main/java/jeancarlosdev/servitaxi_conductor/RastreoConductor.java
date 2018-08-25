package jeancarlosdev.servitaxi_conductor;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jeancarlosdev.servitaxi_conductor.Common.Common;
import jeancarlosdev.servitaxi_conductor.Helper.DirectionJSONParser;
import jeancarlosdev.servitaxi_conductor.Remote.IGoogleAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RastreoConductor extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener
    {

    private GoogleMap mMap;

    double riderLat;

    double riderLng;

    private static final int PLAY_SERVICE_REQUEST_CODE = 7778;

    private LocationRequest mLocationRequest;

    private GoogleApiClient mGoogleApiClient;

    private static int UPDATE_INTERVAL = 5000;

    private static int FATEST_INTERVAL = 3000;

    private static int DISPLACEMENT = 5000;

    private Circle riderMarker;

    private Marker driverMarker;

    private Polyline direction;

    IGoogleAPI mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rastreo_conductor);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(getIntent() != null){

            riderLat = getIntent().getDoubleExtra("lat", -1.0);
            riderLng = getIntent().getDoubleExtra("lng", -1.0);
        }

        mService = Common.getIGoogleAPI();
        setUpLocation();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        riderMarker = mMap.addCircle(new CircleOptions()
        .center(new LatLng(riderLat, riderLng))
        .radius(10)
        .strokeColor(Color.BLUE)
        .fillColor(0x220000FF)
        .strokeWidth(5.0f));
    }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            displayLocation();
            startLocationUpdates();
        }

        @Override
        public void onConnectionSuspended(int i) {
            mGoogleApiClient.connect();
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }

        @Override
        public void onLocationChanged(Location location) {
            Common.mUltimaUbicacion = location;
            displayLocation();
        }

        private void startLocationUpdates() {

            if(ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager
                    .PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                    .PERMISSION_GRANTED)
            {

                return;
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest,this
            );

        }

        private void displayLocation() {

            if(ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager
                    .PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                    .PERMISSION_GRANTED)
            {

                return;
            }

            Common.mUltimaUbicacion = LocationServices.
                    FusedLocationApi.
                    getLastLocation(mGoogleApiClient);

            if(Common.mUltimaUbicacion != null){

                    final double latitude = Common.mUltimaUbicacion.getLatitude();

                    final double longitud = Common.mUltimaUbicacion.getLongitude();

                    if(driverMarker!= null){
                        driverMarker.remove();
                    }

                    driverMarker = mMap.addMarker(
                            new MarkerOptions().position(new LatLng(
                                    latitude, longitud
                            ))
                            .title("Usted")
                            .icon(BitmapDescriptorFactory.defaultMarker())
                    );

                    mMap.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(latitude,
                                    longitud),
                                    17.0f
                    ));

                    if(direction!= null){

                        direction.remove();
                    }
                    getDirection();



            }else{

                Log.d("ERROR", "No se puede obtener la Ubicacion");
            }

        }

    private void getDirection() {
       LatLng currentPosition = new LatLng(Common.mUltimaUbicacion.getLatitude(),
                Common.mUltimaUbicacion.getLongitude()
        );

        String requestApi = null;

        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+currentPosition.latitude+"," +currentPosition.longitude+
                    "&destination="+riderLat +","+riderLng;

             /*+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
                */

            Log.e("RequestApi", requestApi);

            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    try{

                    new ParseTask().execute(response.body().toString());

                    }catch (Exception e){

                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(RastreoConductor.this,
                            "" + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

        }catch (Exception e){

            e.printStackTrace();
        }

    }

        private void setUpLocation() {
                if(checkPlayServices()){

                    buildGoogleApiClient();
                    createLocationRequest();
                    displayLocation();

                }
        }

        private void createLocationRequest() {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FATEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

        }

        private void buildGoogleApiClient() {

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mGoogleApiClient.connect();

        }

        private boolean checkPlayServices() {

            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

            if(resultCode!= ConnectionResult.SUCCESS){

                if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                    GooglePlayServicesUtil.getErrorDialog(resultCode,
                            this,
                            PLAY_SERVICE_REQUEST_CODE).show();
                }else{
                    Toast.makeText(this, "this device is not supported",
                            Toast.LENGTH_SHORT).show();

                    finish();

                }

                return false;

            }

            return  true;
        }

        private class ParseTask
                extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

            ProgressDialog mDialog = new ProgressDialog(RastreoConductor.this
            );

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mDialog.setMessage("Por favor, espere.....");
                mDialog.show();
            }

            @Override
            protected List<List<HashMap<String, String>>>
            doInBackground(String... strings) {
                JSONObject jsonObject;

                List<List<HashMap<String, String>>> routes = null;

                try{

                    jsonObject = new JSONObject(strings[0]);

                    //De internet.
                    DirectionJSONParser parser = new DirectionJSONParser();
                    routes = parser.parse(jsonObject);


                }catch (Exception e){

                }
                return routes;
            }

            @Override
            protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
               mDialog.dismiss();

                ArrayList points = null;

                PolylineOptions polylineOptions = null;

                for (int i=0; i<lists.size(); i++){

                    points = new ArrayList();

                    polylineOptions = new PolylineOptions();

                    List<HashMap<String, String>> path = lists.get(i);

                    for (int j=0; j<path.size(); j++){

                        HashMap<String, String> point =  path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));

                        double lng = Double.parseDouble(point.get("lng"));

                        LatLng position = new LatLng(lat,lng);

                        points.add(position);
                    }

                    polylineOptions.addAll(points);

                    polylineOptions.width(10);

                    polylineOptions.color(Color.RED);

                    polylineOptions.geodesic(true);
                }

                direction = mMap.addPolyline(polylineOptions);
            }
        }


    }
