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
/***
 * Clase utilizada para realizar las peticiones al servidor.
 */
public class Conexion {

    /***
     * Atributo utilizado para guardar la URL base donde se encuentra el Backend de nuestra Aplicación.
     */
    private final static String API_URL = "https://servitaxi.000webhostapp.com/ServicioWEB/index.php/";

    /***
     * Método que nos permitirá iniciar sesión de parte del cliente de la Aplicación ServiTaxi.
     * @param contexto recibe el contexto actual donde va a ser llamado este método.
     * @param mapa recibe un dato de Tipo HashMap<String, String> que contendrá los datos necesarios para enviar al servidor.
     * @param response_Listener recibe un dato Response.Listener<ClienteBackJson> para recibir la respuesta del servidor.
     * @param errorListener recibe un dato Response.ErrorListener, para detectar un error en caso que la petición falle.
     * @return un tipo de dato VolleyPeticion<ClienteBackJson> donde se almacenará la respuesta del servidor.
     * @see ClienteBackJson
     * @see HashMap
     */
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


    /***
     * Método que nos permitirá retornar el externalID de determinada Cooperativa para registrar un nuevo Conductor, con su respectiva unidad.
     * El external nos permitira enlazar la tabla unidad con la de Cooperativa
     * @param contexto recibe el contexto actual donde va a ser llamado este método.
     * @param mapa recibe un dato de Tipo HashMap<String, String> que contendrá los datos necesarios para enviar al servidor.
     * @param response_Listener recibe un dato Response.Listener<MensajeCoopBackJson> para recibir la respuesta del servidor.
     * @param errorListener recibe un dato Response.ErrorListener, para detectar un error en caso que la petición falle.
     * @return un tipo de dato VolleyPeticion<MensajeCoopBackJson> donde se almacenará la respuesta del servidor.
     * @see MensajeCoopBackJson
     * @see HashMap
     */
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

    /***
     * Método que nos permitirá guardar un Chofer en nuestro servicio.
     * @param contexto recibe el contexto actual donde va a ser llamado este método.
     * @param mapa recibe un dato de Tipo HashMap<String, String> que contendrá los datos necesarios para enviar al servidor.
     * @param response_Listener recibe un dato Response.Listener<MensajeBackJson> para recibir la respuesta del servidor.
     * @param errorListener recibe un dato Response.ErrorListener, para detectar un error en caso que la petición falle.
     * @return un tipo de dato VolleyPeticion<MensajeBackJson> donde se almacenará la respuesta del servidor.
     * @see MensajeBackJson
     * @see HashMap
     */
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

    /***
     * Método que nos devolvera un arreglo de Cooperativas, este arreglo de Cooperativas nos servirá al momento de registrar un nuevo Chofer.
     * Es necesario saber a que Cooperativa pertenece este chofer para poder ser Registrado.
     * @param context  recibe el contexto actual donde va a ser llamado este método.
     * @param responseListener recibe un dato Response.Listener<Cooperativa[]> para recibir la respuesta del servidor.
     * @param errorListener recibe un dato Response.ErrorListener, para detectar un error en caso que la petición falle.
     * @return Cooperativa[], un arreglo de Cooperativas para ser Listadas.
     * @see Cooperativa
     */

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

    /***
     * Método que nos permitirá registrar una Unidad en nuestro servicio.
     * Esta unidad será creada conjuntamente con el registro del Chofer.
     * @param contexto  recibe el contexto actual donde va a ser llamado este método.
     * @param mapa recibe un dato de Tipo HashMap<String, String> que contendrá los datos necesarios para enviar al servidor.
     * @param response_Listener recibe un dato Response.Listener<MensajeBackJson> para recibir la respuesta del servidor.
     * @param errorListener recibe un dato Response.ErrorListener, para detectar un error en caso que la petición falle.
     * @return un tipo de dato VolleyPeticion<MensajeBackJson> donde se almacenará la respuesta del servidor.
     * @see MensajeBackJson
     * @see HashMap
     */
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

    public static VolleyPeticion<MensajeBackJson> registrarDireccion(@NonNull final Context contexto,
                                                                    @NonNull final HashMap mapa,
                                                                    @NonNull Response.Listener<MensajeBackJson> response_Listener,
                                                                    @NonNull Response.ErrorListener errorListener){

        final String url = API_URL + "direccion/guardar";

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

    public static VolleyPeticion<MensajeBackJson> retornarExternalUnidad(@NonNull final Context contexto,
                                                                     @NonNull final HashMap mapa,
                                                                     @NonNull Response.Listener<MensajeBackJson> response_Listener,
                                                                     @NonNull Response.ErrorListener errorListener){

        final String url = API_URL + "unidad/inicioSesion";

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

    public static VolleyPeticion<MensajeBackJson> retornarExternalDirección(@NonNull final Context contexto,
                                                                         @NonNull final HashMap mapa,
                                                                         @NonNull Response.Listener<MensajeBackJson> response_Listener,
                                                                         @NonNull Response.ErrorListener errorListener){

        final String url = API_URL + "direccion/external";

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

