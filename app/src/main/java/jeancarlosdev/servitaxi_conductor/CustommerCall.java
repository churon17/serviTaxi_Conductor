package jeancarlosdev.servitaxi_conductor;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import io.paperdb.Paper;
import jeancarlosdev.servitaxi_conductor.Common.Common;
import jeancarlosdev.servitaxi_conductor.Modelos.FCMResponse;
import jeancarlosdev.servitaxi_conductor.Modelos.MensajeBackJson;
import jeancarlosdev.servitaxi_conductor.Modelos.Notification;
import jeancarlosdev.servitaxi_conductor.Modelos.Sender;
import jeancarlosdev.servitaxi_conductor.Modelos.Token;
import jeancarlosdev.servitaxi_conductor.Remote.Conexion;
import jeancarlosdev.servitaxi_conductor.Remote.IFCMService;
import jeancarlosdev.servitaxi_conductor.Remote.IGoogleAPI;
import jeancarlosdev.servitaxi_conductor.Remote.VolleyPeticion;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


/***
 * Clase utilizada para visualizar CustomerCall Layout.
 * Esta clase respectivamente con el Layout será invocada solamemte cuando desde serviTaxi Cliente haya solictado en Llamar Taxi.
 */
public class CustommerCall extends AppCompatActivity {

    TextView txt_time, txt_address, txt_distance;

    Button btnAceptar;

    Button btnCancelar;

    MediaPlayer mediaPlayer;

    IGoogleAPI mService;

    IFCMService mFCMService;

    String customerId;

    String tiempo = "";

    String direccion = "";

    String external = "";

    String external_direccion = "";

    Double lat;

    Double lng;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.
                Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf").
                        setFontAttrId(R.attr.fontPath).build());

        setContentView(R.layout.activity_custommer_call);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

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

                    notificationBooking(customerId, "Lo sentimos", "El conductor no puede procesar tu solicitud en este momento");
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

                agregarDireccion(lat, lng);
                agregarCarrera();

                if(!TextUtils.isEmpty(customerId)) {
                    notificationBooking(customerId, "Aceptado", "El Taxi llegará en " + tiempo);
                    tiempo = "";
                }

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
            external = getIntent().getStringExtra("external");
            getDirection(lat, lng);
        }

    }

    private void agregarCarrera() {
        external_direccion = obtenerExternalDirección(direccion);

        HashMap<String, String> mapa = new HashMap<>();
        mapa.put("id_unidad", String.valueOf(Paper.book().read(Common.external_unidad)));
        mapa.put("id_direccion", external_direccion);
        mapa.put("id_cliente", external);

        VolleyPeticion<MensajeBackJson> agregarDir = Conexion.registrarDireccion(
                getApplicationContext(),
                mapa,
                new com.android.volley.Response.Listener<MensajeBackJson>() {
                    @Override
                    public void onResponse(MensajeBackJson response) {
                        if (response != null && ("FD".equalsIgnoreCase(response.siglas)
                                || "DNF".equalsIgnoreCase(response.siglas))) {
                            Log.e("DIRECCION", response.mensaje);
                        } else {
                            Log.e("DIRECCION", "Se agregó correctamente");
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", error.toString());

                        return;
                    }
                }
        );

        requestQueue.add(agregarDir);
    }

    private String obtenerExternalDirección(String nombreDir) {
        HashMap<String, String> mapa = new HashMap<>();
        mapa.put("id_unidad", nombreDir);

        VolleyPeticion<MensajeBackJson> agregarDir = Conexion.retornarExternalDirección(
                getApplicationContext(),
                mapa,
                new com.android.volley.Response.Listener<MensajeBackJson>() {
                    @Override
                    public void onResponse(MensajeBackJson response) {
                        if (response != null && ("FD".equalsIgnoreCase(response.siglas)
                                || "DNF".equalsIgnoreCase(response.siglas))) {
                            Log.e("DIRECCION", response.mensaje);
                        } else {
                            external_direccion = response.mensaje;
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", error.toString());

                        return;
                    }
                }
        );

        requestQueue.add(agregarDir);

        return external_direccion;
    }

    private void agregarDireccion(Double latitud, Double longitud) {
        HashMap<String, String> mapa = new HashMap<>();
        mapa.put("nombre",  direccion);
        mapa.put("latitud", String.valueOf(latitud));
        mapa.put("longitud", String.valueOf(longitud));

        VolleyPeticion<MensajeBackJson> agregarDir = Conexion.registrarDireccion(
                getApplicationContext(),
                mapa,
                new com.android.volley.Response.Listener<MensajeBackJson>() {
                    @Override
                    public void onResponse(MensajeBackJson response) {
                        if (response != null && ("FD".equalsIgnoreCase(response.siglas)
                                || "DNF".equalsIgnoreCase(response.siglas))) {
                            Log.e("DIRECCION", response.mensaje);
                        } else {
                            Log.e("DIRECCION", "Se agregó correctamente");
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", error.toString());

                        return;
                    }
                }
        );

        requestQueue.add(agregarDir);
    }

    /***
     * Este método nos sirve para enviar la Notificación al Cliente, ya sea la respuesta positiva o negativa.
     * @param customerId id del cliente.
     * @param title Titulo de la notificacion.
     * @param body El cuerpo de la notificacion.
     */
    private void notificationBooking(String customerId, String title, String body) {
        Token token = new Token(customerId);

        Notification  notification = new Notification(title, body);

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


    /***
     * Método que nos obtiene la dirección actual y la de destino ubicada previamente en el componente de Places.
     * En este método nuevamente utilizamos PolyLineOptions para poder diagramar o figurar la ruta desde la posición actual, hasta el destino escogido.
     * @param lat latitud del destino.
     * @param lng longitud del destino.
     */
    private void getDirection(double lat, double lng) {

        String requestApi = null;

        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ Common.mUltimaUbicacion.getLatitude()+"," +Common.mUltimaUbicacion.getLongitude()+
                    "&destination="+lat+","+lng ;

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
                        tiempo = time.getString("text");

                        String address = legsObject.getString("end_address");
                        txt_address.setText(address);
                        direccion = address;

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
