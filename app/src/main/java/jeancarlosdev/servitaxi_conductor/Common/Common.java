package jeancarlosdev.servitaxi_conductor.Common;

import jeancarlosdev.servitaxi_conductor.Remote.IGoogleAPI;
import jeancarlosdev.servitaxi_conductor.Remote.RetrofitClient;

public class Common {
    public static final String baseUrl = "https://maps.googleapis.com";

    public static IGoogleAPI getIGoogleAPI(){

        return RetrofitClient.getClient(baseUrl).create(IGoogleAPI.class);
    }




}
