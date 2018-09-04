package jeancarlosdev.servitaxi_conductor.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/***
 * Interfaz IGoogleApi utilizada para manipular el API de google.
 */
public interface IGoogleAPI {


    /***
     * Método sin implementación por ser una interfaz, que nos ayuda a obtener una llamada de tipo String
     * @param url recibe como parámetro la Url.
     * @return Call<String>.
     */
    @GET
    Call<String> getPath(@Url String url);

}
