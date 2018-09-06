package jeancarlosdev.servitaxi_conductor.Common;

import android.location.Location;

import jeancarlosdev.servitaxi_conductor.Modelos.Conductor;
import jeancarlosdev.servitaxi_conductor.Remote.FCMClient;
import jeancarlosdev.servitaxi_conductor.Remote.IFCMService;
import jeancarlosdev.servitaxi_conductor.Remote.IGoogleAPI;
import jeancarlosdev.servitaxi_conductor.Remote.RetrofitClient;

/***
 * Clase utilizada para reutilizar variables estáticas en distintas clases.
 */
public class Common {

    /***
     * Variable estática  utilizada para hacer referencia al nombre de la tabla Conductores en Firebase.
     */
    public static final String conductor_tb1 = "Conductores";

    /***
     * Variable estática  utilizada para hacer referencia al nombre de la tabla Drivers en Firebase.
     */
    public static final String drivers_tb1 = "Drivers";

    /***
     * Variable estática  utilizada para hacer referencia al nombre de la tabla Tokens en Firebase.
     */
    public static final String token_tb1 = "Tokens";


    /***
     * Variable estática  utilizada para hacer referencia al conductor que se realiza la petición.
     */
    public static Conductor currentUser;

    public static String external_unidad = "ext";

    /***
     * Variable estática de tipo LOCATION utilizada para mantener constantemente el valor de latitud y longitud del conductor.
     */
    public static Location mUltimaUbicacion = null;

    /***
     * Variable estática utilizada para acceder a la Api de GoogleMaps
     * Guarda la URL base para consultar el API de google MAPS.
     */
    public static final String baseUrl = "https://maps.googleapis.com";

    /***
     * Variable estática utilizada para notificaciones en FIREBASE CLOUD MESSAGING
     * Guarda la URL base para consultar FIREBASE CLOUD MESSAGING.
     */
    public static final String fcmUrl = "https:/fcm.googleapis.com/";

    /***
     * Variable estática utilizada para guardar en la BD del telefono el valor del correo electrónico del conductor, previamente Logueado.
     */
    public static String conductor = "usr";

    /***
     * Variable estática utilizada para guardar en la BD del telefono el valor de la contraseña del conductor, previamente Logueado.
     */
    public static String password = "pwd";

    /***
     * Método estático para realizar una instancia de IGoogleApi, sigue el modelo SINGLETON.
     */
    public static IGoogleAPI getIGoogleAPI(){

        return RetrofitClient.getClient(baseUrl).create(IGoogleAPI.class);
    }

    /***
     * Método estático para realizar una instancia de IFCMservice, sigue el modelo SINGLETON.
     */
    public static IFCMService getFCMService(){

        return FCMClient.getClient(fcmUrl).create(IFCMService.class);
    }


}
