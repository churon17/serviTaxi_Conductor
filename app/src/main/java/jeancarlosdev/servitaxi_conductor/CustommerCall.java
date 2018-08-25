package jeancarlosdev.servitaxi_conductor;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import jeancarlosdev.servitaxi_conductor.Common.Common;
import jeancarlosdev.servitaxi_conductor.Modelos.FCMResponse;
import jeancarlosdev.servitaxi_conductor.Modelos.Notification;
import jeancarlosdev.servitaxi_conductor.Modelos.Sender;
import jeancarlosdev.servitaxi_conductor.Modelos.Token;
import jeancarlosdev.servitaxi_conductor.Remote.IFCMService;
import jeancarlosdev.servitaxi_conductor.Remote.IGoogleAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustommerCall extends AppCompatActivity {

    TextView txt_time, txt_address, txt_distance;

    Button btnAceptar;

    Button btnCancelar;

    MediaPlayer mediaPlayer;

    IGoogleAPI mService;

    IFCMService mFCMService;

    String customerId;

    Double lat;

    Double lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custommer_call);

        mService = Common.getIGoogleAPI();

        mFCMService = Common.getFCMService();

        txt_time = (TextView) findViewById(R.id.txt_time);
        txt_address = (TextView) findViewById(R.id.txt_address);
        txt_distance = (TextView) findViewById(R.id.txt_distance);

        btnAceptar = findViewById(R.id.btnAceptar);
        btnCancelar = findViewById(R.id.btnRechazar);

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!TextUtils.isEmpty(customerId)){

                    cancelBooking(customerId);
                }
            }
        });

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intencion = new Intent(CustommerCall.this,
                        RastreoConductor.class);

                //Enviar localizacion del Cliente a una nueva actividad.
                intencion.putExtra("lat", lat);
                intencion.putExtra("lng", lng);
                intencion.putExtra("customerId", customerId);

                startActivity(intencion);
                finish();
            }
        });

        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();


        if (getIntent() != null){
            lat = getIntent().getDoubleExtra("lat", -1.0);
            lng = getIntent().getDoubleExtra("lng", -1.0);
            customerId = getIntent().getStringExtra("customer");

            getDirection(lat, lng);
        }

    }

    private void cancelBooking(String customerId) {
        Token token = new Token(customerId);

        Notification  notification = new Notification("Lo sentimos",
                "El conductor no puede procesar tu solicitud en este momento");

        Sender sender = new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call,
                                           Response<FCMResponse> response) {
                        if(response.body().success ==1){
                            Toast.makeText(CustommerCall.this,
                                    "Cancelado",
                                    Toast.LENGTH_SHORT);

                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call,
                                          Throwable t) {

                    }
                });

    }

    private void getDirection(double lat, double lng) {

        String requestApi = null;

        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ Common.mUltimaUbicacion.getLatitude()+"," +Common.mUltimaUbicacion.getLongitude()+
                    "&destination="+lat+","+lng ;

             /*+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
                */

            Log.e("RequestApi", requestApi);

            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    try{
                        JSONObject jsonObject=new JSONObject(response.body().toString());

                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);

                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);

                        JSONObject distance = legsObject.getJSONObject("distance");
                        txt_distance.setText(distance.getString("text"));

                        JSONObject time = legsObject.getJSONObject("duration");
                        txt_time.setText(time.getString("text"));

                        String address = legsObject.getString("end_address");
                        txt_address.setText(address);

                    }catch (Exception e){

                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(CustommerCall.this,
                            "" + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

        }catch (Exception e){

            e.printStackTrace();
        }


    }

    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mediaPlayer.start();
        super.onResume();
    }
}
