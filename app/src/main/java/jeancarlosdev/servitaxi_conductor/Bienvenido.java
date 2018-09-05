package jeancarlosdev.servitaxi_conductor;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.view.animation.LinearInterpolator;

import android.widget.ImageButton;
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
import java.util.Timer;
import java.util.TimerTask;

import io.paperdb.Paper;
import jeancarlosdev.servitaxi_conductor.Common.Common;
import jeancarlosdev.servitaxi_conductor.Modelos.Token;
import jeancarlosdev.servitaxi_conductor.Remote.IGoogleAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/***
 * Clase utilizada para implementar el Mapa de GoogleMaps.
 * En esta clase también se implementa Places, para buscar ubicaciones en la Api de Places de GoogleMaps.
 * Implementa tres interfaces necesarias para manipular en tiempo real la ubicación del usuario.
 * @see com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
 * @see com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
 * @see com.google.android.gms.location.LocationListener
 */
public class Bienvenido extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    //region Atributos

    /***
     * Componente ImageButton utilizado para cerrar sesión.
     */
    private ImageButton logout;

    /***
     * Para mostrar el mapa con el que vamos a trabajar.
     */
    private GoogleMap mMap;

    /***
     * Para ver si el usuario dio los permisos necesarios de Localización.
     */
    private static final int PERMISSION_REQUEST_CODE = 7777;

    /***
     * Para ver si el usuario dio los permisos necesarios de Localización.
     */

    private static final int PLAY_SERVICE_REQUEST_CODE = 7778;

    /***
     * LocationRequest se utiliza para solicitar una calidad de servicio para las actualizaciones de ubicación desde FusedLocationProviderApi.
     */

    private LocationRequest mLocationRequest;

    /***
     * Trabaja conjuntamente con LocationRequest, para las actualizaciones de ubicación.
     */
    private GoogleApiClient mGoogleApiClient;

    /***
     * Para cambiar el intervalo de actualización de LocationRequest para la ubicación en tiempo real.
     */
    private static int UPDATE_INTERVAL = 5000;

    /***
     * Para cambiar el intervalo de actualización de LocationRequest para la ubicación en tiempo real.
     */
    private static int FATEST_INTERVAL = 3000;

    /***
     * Para cambiar el intervalo de actualización de LocationRequest para la ubicación en tiempo real.
     */
    private static int DISPLACEMENT = 5000;


    /***
     * Referencia a la base de datos Conductores.
     */
    DatabaseReference conductores;

    /***
     * Para consultas de ubicacion en tiempo real con Firebase.
     */
    GeoFire geoFire;

    /***
     * Para agregar o quitar el marcador cuando deseemos dentro de nuestro mapa.
     */
    Marker mCurrent;

    /***
     * Switch utilizado para ver si el conductor esta en Linea o fuera de linea.
     */
    MaterialAnimatedSwitch location_switch;

    /***
     * El Fragment utilizado para nuestro Mapa.
     */
    SupportMapFragment mapFragment;

    /***
     * Instancia de la interfaz IGoogleApi.
     * @see IGoogleAPI
     */
    private IGoogleAPI mService;

    //region Animacion del Carro Atributos.

    /***
     * Lista de tipo LatLng para almancenar las rutas.
     */
    private List<LatLng> polyLineList;

    /***
     * Marcador del vehículo.
     */
    private Marker carMarker;

    private float v;

    private double lat, lng;

    private Handler handler;

    private LatLng startPosition, endPosition, currentPosition;

    private int index, next;

    /***
     * Componente utilizado para hacer consultas a la Api de Places de Google Maps.
     */
    private PlaceAutocompleteFragment places;

    private String destination;

    /**
     * Variables utilizadas para crear Polilíneas, para la simulación de rutas.
     */
    private PolylineOptions polylineOptions, blackPolylineOptions;

    private Polyline blackPolyline, greypoPolyline;

    DatabaseReference onlineRef, currentUserRef;

    //endregion

    //endregion

    //region Runnable

    /***
     * Hilo utilizado cuando el usuario utiliza el componente Places, para diagramar la ruta.
     * La ruta es Diagramada por medio de PolyLineOptions.
     */

    //region Runnable
    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {

            if (index < polyLineList.size() - 1) {

                index++;

                next = index + 1;
            }

            if (index < polyLineList.size() - 1) {

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

                            lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;

                            lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;

                            LatLng newPos = new LatLng(lat, lng);

                            carMarker.setPosition(newPos);

                            carMarker.setAnchor(0.5f, 0.5f);

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


    //region Metodos propios

    /***
     * Método que nos ayuda a cambiar la Localización.
     * Esté metodo checa si el usuario ha dado los permisos necesarios que necesita para obtener la ubicación del Conductor.
     */
    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager
                .PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                .PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, PERMISSION_REQUEST_CODE);
        } else {

            if (checkPlayServices()) {

                buildGoogleApiClient();
                createLocationRequest();
                if (location_switch.isChecked()) {
                    displayLocation();
                }
            }
        }
    }

    /**
     * Este método crea una nueva instancia de tipo LocationRequest.
     * Esta instancia nos permitirá manipular la ubicación actual del conductor, cada que momento necesitamos actualizar el intervalo.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    /**
     * Este método crea una nueva instancia de tipo GoogleApiClient.
     * Esta instancia trabajará conjuntamente con LocationRequest para la manipulación de la clase Actual.
     */
    private void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

    }

    /***
     * Método que nos ayuda a cerrar sesión, conjuntamente eliminar los datos guardados previamente Con Paper.
     * Estos datos serán eliminados para que se pueda iniciar una nueva sesión.
     * En este método también se cierra la instancia de FirebaseAuth, y se redirecciona a la ventana del Login.
     */
    private void singOut() {
        Paper.init(this);
        Paper.book().destroy();

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Bienvenido.this, Login_1.class);
        startActivity(intent);
        finish();
    }

    /***
     * Método utiliado para verificar si los servicios han sido concedidos por el usuario.
     * @return boolean.
     * True si todo esta correcto y los servicios han sido concedidos sin ningún problema.
     * False si los permisos no han sido concedidos.
     */
    private boolean checkPlayServices() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode,
                        this,
                        PLAY_SERVICE_REQUEST_CODE).show();
            } else {
                Toast.makeText(this, R.string.notSupported,
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            return false;
        }

        return true;
    }

    /***
     * Método que nos permite dejar de actualizar las localizaciones.
     * Esté método es necesario para liberar los recursos que no son necesarios.
     */
    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager
                .PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                .PERMISSION_GRANTED) {

            return;
        }

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                this);
    }

    /***
     * Método que muestra la ubicación actual y la muestra con un nuevo marker en el Mapa.
     * Esté  método a su vez manda a guardar constantemente la ubicación actual por Geofire a Firebase.
     */
    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager
                .PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                .PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);

        Common.mUltimaUbicacion = LocationServices.
                FusedLocationApi.
                getLastLocation(mGoogleApiClient);

        if (Common.mUltimaUbicacion != null) {

            if (location_switch.isChecked()) {

                final double latitude = Common.mUltimaUbicacion.getLatitude();

                final double longitud = Common.mUltimaUbicacion.getLongitude();

                geoFire.setLocation(
                        FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        new GeoLocation(latitude, longitud),
                        new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key,
                                                   DatabaseError error) {
                                if (mCurrent != null) {
                                    mCurrent.remove(); //Remove marker exist.
                                }

                                mCurrent = mMap.addMarker(new MarkerOptions().
                                        position(new LatLng(latitude, longitud))
                                        .title(getResources().getString(R.string.usted)));

                                //Mover la camara del cellPhone a esa posicion.

                                mMap.animateCamera(CameraUpdateFactory.
                                        newLatLngZoom(
                                                new LatLng(latitude,
                                                        longitud),
                                                17.0f));

                            }
                        }
                );
            }

        } else {

            Toast.makeText(this, R.string.noUbicacion, Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Esté método nos ayuda para inicializar la busqueda de Actualizaciones de las Localizaciones.
     * Checando previamente si los permisos estan correcctamente concedidos.
     */
    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager
                .PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                .PERMISSION_GRANTED) {

            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this
        );

    }


    /***
     * Método que nos obtiene la dirección actual y la de destino ubicada previamente en el componente de Places.
     * En este método nuevamente utilizamos PolyLineOptions para poder diagramar o figurar la ruta desde la posición actual, hasta el destino escogido.
     */
    private void getDirection() {

        currentPosition = new LatLng(Common.mUltimaUbicacion.getLatitude(),
                Common.mUltimaUbicacion.getLongitude()
        );

        String requestApi = null;

        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude +
                    "&destination=" + destination;

            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        JSONArray jsonArray = jsonObject.getJSONArray("routes");

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polyLineList = decodePoly(polyline);
                        }
                        //Adjusting bounds
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng latlng : polyLineList) {

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
                                        polyLineList.get(polyLineList.size() - 1)
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

                                        int percentValue = (int) valueAnimator.getAnimatedValue();

                                        int size = points.size();

                                        int newPoints = (int) (size * (percentValue / 100.0f));

                                        List<LatLng> p = points.subList(0, newPoints);

                                        blackPolyline.setPoints(p);

                                    }
                                });

                        polyLineAnimator.start();

                        carMarker = mMap.addMarker(new MarkerOptions()
                                .position(currentPosition)
                                .flat(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.rueda)));

                        handler = new Handler();
                        index = -1;
                        next = 1;
                        handler.postDelayed(drawPathRunnable, 3000);

                    } catch (Exception e) {

                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(Bienvenido.this,
                            "" + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {

            e.printStackTrace();
        }


    }
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.
                Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf").
                        setFontAttrId(R.attr.fontPath).build());

        setContentView(R.layout.frm_bienvenido);

        logout = (ImageButton) findViewById(R.id.btn_find_user);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                singOut();
            }
        });

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


        location_switch = (MaterialAnimatedSwitch) findViewById(R.id.switch_location);

        location_switch.setOnCheckedChangeListener(
                new MaterialAnimatedSwitch.OnCheckedChangeListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onCheckedChanged(boolean isOnline) {

                        if (isOnline) {


                            //Cambiamos a conectado cuando el Switch esta encendido.
                            FirebaseDatabase.getInstance().goOnline();

                            Snackbar.make(mapFragment.getView(), R.string.onLine, Snackbar.
                                    LENGTH_SHORT).show();

                            mMap.setMyLocationEnabled(true);


                            startLocationUpdates();

                            final Handler handler = new Handler();
                            Timer timer = new Timer();

                            TimerTask task = new TimerTask() {
                                @Override
                                public void run() {
                                    handler.post(new Runnable() {
                                        public void run() {
                                            try {

                                                displayLocation();

                                            } catch (Exception e) {
                                                Log.e("error", e.getMessage());
                                            }
                                        }
                                    });
                                }
                            };

                            timer.schedule(task, 0, 3000);  //ejecutar en intervalo de 3 segundos.



                        } else {

                            //Cambiamos a desconectado cuando el Switch esta apagado.
                            FirebaseDatabase.getInstance().goOffline();


                            Snackbar.make(mapFragment.getView(),
                                    R.string.offLine, Snackbar.
                                            LENGTH_SHORT).show();

                            stopLocationUpdates();

                            mCurrent.remove();

                            mMap.clear();

                            if (handler != null) {
                                handler.removeCallbacks(drawPathRunnable);

                            }
                        }
                    }
                });

        conductores = FirebaseDatabase.getInstance().getReference(Common.drivers_tb1);

        geoFire = new GeoFire(conductores);

        polyLineList = new ArrayList<>();

        //Places Api

        places = (PlaceAutocompleteFragment) getFragmentManager()
                .findFragmentById(R.id.place_autocomplete_fragment);

        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {
                        if (location_switch.isChecked()) {

                            destination = place.getAddress().toString();

                            destination = destination.replace(" ", "+");

                            getDirection();
                        } else {

                            Toast.makeText(Bienvenido.this,
                                    R.string.YesOnline,
                                    Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onError(Status status) {

                        Toast.makeText(Bienvenido.this,
                                "" + status.toString(),
                                Toast.LENGTH_SHORT).show();

                    }
                });
        setUpLocation();

        mService = Common.getIGoogleAPI();

        updateFirebaseToken();

    }

    /***
     * Método que nos sirve para actualizar el Token del Conductor en la Tabla de Firebase.
     * No recibe parámetros.
     */
    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tb1);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);

    }

    /***
     * Método para decodificar los Puntos PolyLine para graficar la ruta en GoogleMaps.
     * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * GitHub
     * */
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

        switch (requestCode) {

            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (checkPlayServices()) {

                        buildGoogleApiClient();
                        createLocationRequest();
                        if (location_switch.isChecked()) {
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