package jeancarlosdev.servitaxi_conductor;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jeancarlosdev.servitaxi_conductor.Common.Common;
import jeancarlosdev.servitaxi_conductor.Modelos.Token;
import jeancarlosdev.servitaxi_conductor.Remote.IGoogleAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Bienvenido extends FragmentActivity implements OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener
{
    //region Atributos
    private GoogleMap mMap;

    private static final int PERMISSION_REQUEST_CODE = 7777;

    private static final int PLAY_SERVICE_REQUEST_CODE = 7778;

    private LocationRequest mLocationRequest;

    private GoogleApiClient mGoogleApiClient;

    private static int UPDATE_INTERVAL = 5000;

    private static int FATEST_INTERVAL = 3000;

    private static int DISPLACEMENT = 5000;

    DatabaseReference conductores;

    GeoFire geoFire;

    Marker mCurrent;

    MaterialAnimatedSwitch location_switch;

    SupportMapFragment mapFragment;

    private IGoogleAPI mService;

    boolean isDriverFound = false;

    String driverId = "";

    int radius = 1; // 1km

    //region Animacion del Carro Atributos.
    private List<LatLng> polyLineList;

    private Marker carMarker;

    private float v;

    private double lat, lng;

    private Handler handler;

    private LatLng startPosition, endPosition, currentPosition;

    private int index, next;


    private PlaceAutocompleteFragment places;

    private String destination;

    private PolylineOptions polylineOptions, blackPolylineOptions;

    private Polyline blackPolyline, greypoPolyline;


    DatabaseReference onlineRef, currentUserRef;

    //endregion

    //endregion

    //region Runnable

    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {

                if(index<polyLineList.size() - 1){

                    index ++;

                    next = index+1;
                }

                if(index <polyLineList.size() - 1){

                    startPosition = polyLineList.get(index);
                    endPosition = polyLineList.get(next);
                }

                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.setDuration(1000);
                valueAnimator.setInterpolator(new LinearInterpolator());
                valueAnimator.addUpdateListener(
                        new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {

                        v = valueAnimator.getAnimatedFraction();

                        lng = v*endPosition.longitude+(1-v)*startPosition.longitude;

                        lat = v*endPosition.latitude+(1-v)*startPosition.latitude;

                        LatLng  newPos=  new LatLng(lat, lng);

                        carMarker.setPosition(newPos);

                        carMarker.setAnchor(0.5f, 0.5f);

                        carMarker.setRotation(getBearing(startPosition, newPos));

                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                .target(newPos)
                                .zoom(15.5f)
                                .build()

                        ));
                    }
                });

                valueAnimator.start();

                handler.postDelayed(this, 3000);
        }
    };
    //endregion

    //Posicion Vehiculo en movimiento.
    private float getBearing(LatLng startPosition, LatLng endPosition) {

        double lat = Math.abs(startPosition.latitude - endPosition.latitude);

        double lon = Math.abs(startPosition.longitude - endPosition.longitude);

        if (startPosition.latitude< endPosition.latitude &&
                startPosition.longitude < endPosition.longitude){

            return (float)(Math.toDegrees(Math.atan(lon/lat)));

        }else if (startPosition.latitude>= endPosition.latitude &&
                startPosition.longitude < endPosition.longitude ){

            return (float)((90-Math.toDegrees(Math.atan(lon/lat))) + 90);

        }else if (startPosition.latitude>= endPosition.latitude &&
                startPosition.longitude >= endPosition.longitude ){

            return (float)(Math.toDegrees(Math.atan(lon/lat)) + 180);

        }else  if (startPosition.latitude < endPosition.latitude &&
                startPosition.longitude >= endPosition.longitude ){

            return (float)((90-Math.toDegrees(Math.atan(lon/lat))) + 270);
        }
        return -1;
    }


    //region Metodos propios
    private void setUpLocation() {
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager
                .PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                .PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, PERMISSION_REQUEST_CODE);
        }else {

            if(checkPlayServices()){

                buildGoogleApiClient();
                createLocationRequest();
                if(location_switch.isChecked()){
                    displayLocation();
                }
            }
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

    private void stopLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager
                .PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                .PERMISSION_GRANTED)
        {

            return;
        }

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                (com.google.android.gms.location.LocationListener) this);
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

      //  Location location = new Location("");

        //location.setLatitude(-4.02695490724033);

        //location.setLongitude(-79.20271825947566);

        //Common.mUltimaUbicacion = location;


        if(Common.mUltimaUbicacion != null){
            if(location_switch.isChecked()){

                final double latitude = Common.mUltimaUbicacion.getLatitude();

                final double longitud = Common.mUltimaUbicacion.getLongitude();

                geoFire.setLocation(
                        FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        new GeoLocation(latitude, longitud),
                        new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key,
                                                   DatabaseError error) {
                                if(mCurrent != null)
                                {
                                    mCurrent.remove(); //Remove marker exist.
                                }

                                mCurrent = mMap.addMarker(new MarkerOptions().
                                        position(new LatLng(latitude, longitud))
                                        .title("Usted"));

                                //Mover la camara del cellPhone a esa posicion.

                                mMap.animateCamera(CameraUpdateFactory.
                                        newLatLngZoom(
                                                new LatLng(latitude,
                                                        longitud),
                                                17.0f));

                                //Dibujar la animation de rotar el carrito.

                            }
                        }
                );



                 //rotateMarker(mCurrent, -360,  mMap);
            }


        }else{

            Log.d("ERROR", "No se puede obtener la Ubicacion");
        }

    }

    private void rotateMarker(final Marker mCurrent, final float i, GoogleMap mMap) {
        final Handler handler = new Handler();

        final long start = SystemClock.uptimeMillis();

        final float startRotation = mCurrent.getRotation();

        final long duration = 2000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;

                float t = interpolator.getInterpolation((float)elapsed/duration);

                float rot = t*i+(1-t)*startRotation;

                mCurrent.setRotation( -rot>180? rot/2 : rot);

                if(t<1.0){

                    handler.postDelayed(this,16 );

                }

            }
        });


    }

    private void startLocationUpdates() {

        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager
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

    private void getDirection() {

        currentPosition = new LatLng(Common.mUltimaUbicacion.getLatitude(),
                Common.mUltimaUbicacion.getLongitude()
        );

        String requestApi = null;

        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+currentPosition.latitude+"," +currentPosition.longitude+
                    "&destination="+destination ;

             /*+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
                */

            Log.e("RequestApi", requestApi);

            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    try{
                        JSONObject jsonObject=new JSONObject(response.body().toString());

                        JSONArray jsonArray = jsonObject.getJSONArray("routes");

                        for (int i= 0; i<jsonArray.length(); i++){

                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly =  route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polyLineList = decodePoly(polyline);
                        }
                        //Adjusting bounds
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng latlng:polyLineList ){

                            builder.include(latlng);

                        }

                        LatLngBounds bounds = builder.build();

                        CameraUpdate mCamareUpdate = CameraUpdateFactory
                                .newLatLngBounds(bounds, 2);

                        mMap.animateCamera(mCamareUpdate);

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.GRAY);
                        polylineOptions.width(5);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.endCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polyLineList);
                        greypoPolyline = mMap.addPolyline(polylineOptions);


                        blackPolylineOptions = new PolylineOptions();
                        blackPolylineOptions.color(Color.RED);
                        blackPolylineOptions.width(10);
                        blackPolylineOptions.startCap(new SquareCap());
                        blackPolylineOptions.endCap(new SquareCap());
                        blackPolylineOptions.jointType(JointType.ROUND);
                        blackPolyline = mMap.addPolyline(blackPolylineOptions);

                        mMap.addMarker(new MarkerOptions()
                                .position(
                                        polyLineList.get(polyLineList.size()-1)
                                ).title("Pasar a buscar"));

                        //Animation
                        ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0, 100);

                        polyLineAnimator.setDuration(2000);

                        polyLineAnimator.setInterpolator(new LinearInterpolator());

                        polyLineAnimator.addUpdateListener(
                                new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {

                                        List<LatLng> points = greypoPolyline.getPoints();

                                        int percentValue = (int)valueAnimator.getAnimatedValue();

                                        int size = points.size();

                                        int newPoints = (int)(size * (percentValue/100.0f));

                                        List<LatLng> p = points.subList(0, newPoints);

                                        blackPolyline.setPoints(p);

                                    }
                                });

                        polyLineAnimator.start();

                        carMarker = mMap.addMarker(new MarkerOptions()
                                .position(currentPosition)
                                .flat(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                        handler = new Handler();
                        index = -1;
                        next = 1;
                        handler.postDelayed(drawPathRunnable, 3000);

                    }catch (Exception e){

                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(Bienvenido.this,
                            "" + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

        }catch (Exception e){

            e.printStackTrace();
        }


    }





    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frm_bienvenido);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Presense System

        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");

        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.drivers_tb1).child(FirebaseAuth.getInstance().getCurrentUser().getUid());


        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Quitaremos el valor del conductor en la base de datos cuando el conductor esta desconectado.
                currentUserRef.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        location_switch = (MaterialAnimatedSwitch)findViewById(R.id.switch_location);

        location_switch.setOnCheckedChangeListener(
                new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {

                if(isOnline){


                    //Cambiamos a conectado cuando el Switch esta encendido.
                    FirebaseDatabase.getInstance().goOnline();

                    Snackbar.make(mapFragment.getView(), "En linea", Snackbar.
                            LENGTH_SHORT).show();

                    startLocationUpdates();
                    displayLocation();


                }else{

                    //Cambiamos a desconectado cuando el Switch esta apagado.
                    FirebaseDatabase.getInstance().goOffline();


                    Snackbar.make(mapFragment.getView(),
                            "Fuera de Linea", Snackbar.
                                    LENGTH_SHORT).show();

                    stopLocationUpdates();

                    mCurrent.remove();

                    mMap.clear();

                    if(handler!= null){
                        handler.removeCallbacks(drawPathRunnable);

                    }
                }

            }
        });

        conductores = FirebaseDatabase.getInstance().getReference(Common.drivers_tb1);

        geoFire = new GeoFire(conductores);

        polyLineList = new ArrayList<>();

        //Places Api

        places = (PlaceAutocompleteFragment)getFragmentManager()
                .findFragmentById(R.id.place_autocomplete_fragment);

        places
                .setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {
                        if(location_switch.isChecked()){

                            destination = place.getAddress().toString();

                            destination=destination.replace(" ", "+" );

                            Log.e("Destination", destination);

                            getDirection();
                        }else{

                            Toast.makeText(Bienvenido.this,
                                    "Tienes que estar en Linea",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                    @Override
                    public void onError(Status status) {

                        Toast.makeText(Bienvenido.this,
                                "" +status.toString(),
                                Toast.LENGTH_SHORT).show();

                    }
                });
        setUpLocation();

        mService = Common.getIGoogleAPI();

        updateFirebaseToken();

    }

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tb1 );

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);

    }

    //Download on GitHub.
    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){

            case PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    if(checkPlayServices()){

                        buildGoogleApiClient();
                        createLocationRequest();
                        if(location_switch.isChecked()){
                            displayLocation();
                        }
                    }
                }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.setTrafficEnabled(false);

        mMap.setBuildingsEnabled(false);

        mMap.setIndoorEnabled(false);

        mMap.getUiSettings().setZoomControlsEnabled(true);

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
}
