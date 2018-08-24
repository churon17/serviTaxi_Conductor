package jeancarlosdev.servitaxi_conductor.Common;

import android.location.Location;

import jeancarlosdev.servitaxi_conductor.Remote.FCMClient;
import jeancarlosdev.servitaxi_conductor.Remote.IFCMService;
import jeancarlosdev.servitaxi_conductor.Remote.IGoogleAPI;
import jeancarlosdev.servitaxi_conductor.Remote.RetrofitClient;

public class Common {

    public static final String clientes_tb1 = "Clientes";
    public static final String conductor_tb1 = "Conductores";
    public static final String drivers_tb1 = "Drivers";
    public static final String solicitud_tb1 = "Solicitud";
    public static final String token_tb1 = "Tokens";

    public static Location mUltimaUbicacion = null;

    public static final String baseUrl = "https://maps.googleapis.com";
    public static final String fcmUrl = "https:/fcm.googleapis.com/";

    public static IGoogleAPI getIGoogleAPI(){

        return RetrofitClient.getClient(baseUrl).create(IGoogleAPI.class);
    }

    public static IFCMService getFCMService(){

        return FCMClient.getClient(fcmUrl).create(IFCMService.class);
    }


}
