package jeancarlosdev.servitaxi_conductor.Service;

import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import jeancarlosdev.servitaxi_conductor.CustommerCall;

/***
 * Clase utilizada para la recepción de la Notificacion.
 */
public class MyFirebaseMessaging extends FirebaseMessagingService {

    /***
     * Sobreescribimos el método onMessageReceived, cuando se reciba  el RemoteMessage se iniciara una nueva actividad.
     * Esté método se ejecuta automáticamente cuando se reciba el RemoteMessage.
     * @param remoteMessage
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        LatLng customer_location = new Gson()
                .fromJson(remoteMessage
                        .getNotification()
                        .getBody(), LatLng.class);

        String [] arreglo = remoteMessage.getNotification().getTitle().split(";");

        Intent intent = new Intent(getBaseContext(), CustommerCall.class);

        intent.putExtra("lat", customer_location.latitude);

        intent.putExtra("lng", customer_location.longitude);
        intent.putExtra("customer", arreglo[0]);
        intent.putExtra("external", arreglo[1]);
        startActivity(intent);
    }
}
