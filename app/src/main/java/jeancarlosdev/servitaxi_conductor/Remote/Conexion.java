package jeancarlosdev.servitaxi_conductor.Remote;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;

import java.util.HashMap;

import jeancarlosdev.servitaxi_conductor.Modelos.ClienteBackJson;
import jeancarlosdev.servitaxi_conductor.Modelos.Cooperativa;
import jeancarlosdev.servitaxi_conductor.Modelos.MensajeBackJson;
import jeancarlosdev.servitaxi_conductor.Modelos.MensajeCoopBackJson;

public class Conexion {

    private final static String API_URL = "https://servitaxi.000webhostapp.com/ServicioWEB/index.php/";

    public static VolleyPeticion<ClienteBackJson> iniciarSesion(@NonNull final Context contexto,
                                                                @NonNull final HashMap mapa,
                                                                @NonNull Response.Listener<ClienteBackJson> response_Listener,
                                                                @NonNull Response.ErrorListener errorListener){

        final String url = API_URL + "chofer/iniciarSesion";

        VolleyPeticion request = new VolleyPeticion(contexto,
                Request.Method.POST, //Tipo de metodo.
                url,
                mapa,
                HashMap.class,
                String.class,
                response_Listener,
                errorListener);

        request.setResponseClass(ClienteBackJson.class);

        return request;
    }


    public static VolleyPeticion<MensajeCoopBackJson> retornarCoopExternal(@NonNull final Context contexto,
                                                                @NonNull final HashMap mapa,
                                                                @NonNull Response.Listener<MensajeCoopBackJson> response_Listener,
                                                                @NonNull Response.ErrorListener errorListener){

        final String url = API_URL + "cooperativa/externalCooperativa";

        VolleyPeticion request = new VolleyPeticion(contexto,
                Request.Method.POST, //Tipo de metodo.
                url,
                mapa,
                HashMap.class,
                String.class,
                response_Listener,
                errorListener);

        request.setResponseClass(MensajeCoopBackJson.class);

        return request;
    }


    public static VolleyPeticion<MensajeBackJson> registrarChofer(@NonNull final Context contexto,
                                                                   @NonNull final HashMap mapa,
                                                                   @NonNull Response.Listener<MensajeBackJson> response_Listener,
                                                                   @NonNull Response.ErrorListener errorListener){

        final String url = API_URL + "chofer/guardar";

        VolleyPeticion request = new VolleyPeticion(contexto,
                Request.Method.POST,
                url,
                mapa,
                HashMap.class,
                String.class,
                response_Listener,
                errorListener);

        request.setResponseClass(MensajeBackJson.class);

        return request;
    }

    public static VolleyPeticion<Cooperativa[]> listaCooperativas(
            @NonNull final Context context,
            @NonNull Response.Listener<Cooperativa[]> responseListener,
            @NonNull Response.ErrorListener errorListener) {
        final String url = API_URL + "cooperativa/listarCooperativa" ;

        VolleyPeticion request = new VolleyPeticion(
                context,
                Request.Method.GET,
                url,
                responseListener,
                errorListener
        );
        request.setResponseClass(Cooperativa[].class);

        return request;
    }

    public static VolleyPeticion<MensajeBackJson> registrarUnidad(@NonNull final Context contexto,
                                                                   @NonNull final HashMap mapa,
                                                                   @NonNull Response.Listener<MensajeBackJson> response_Listener,
                                                                   @NonNull Response.ErrorListener errorListener){

        final String url = API_URL + "unidad/guardar";

        VolleyPeticion request = new VolleyPeticion(contexto,
                Request.Method.POST,
                url,
                mapa,
                HashMap.class,
                String.class,
                response_Listener,
                errorListener);

        request.setResponseClass(MensajeBackJson.class);

        return request;
    }
}

