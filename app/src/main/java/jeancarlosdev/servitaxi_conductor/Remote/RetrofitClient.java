package jeancarlosdev.servitaxi_conductor.Remote;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    /**
     * Atributo de la clase Retrofit para almacenar o devolver posteriormente el objeto Retrofit creado.
     */
    private static Retrofit retrofit = null;

    /***
     * Método que nos servirá para obtener un Cliente de Tipo Retrofit.
     * En caso de que sea Null, esté método será el encargado de crearlo.
     * Sigue el patron SINGLETON-
     * @param baseUrl nos servirá para al cliente Retrofit brindarle un baseURL.
     * @return un objeto de tipo RETROFIT.
     */
    public static Retrofit getClient(String baseUrl){

        if(retrofit == null){

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ScalarsConverterFactory
                            .create())
                    .build();

        }

        return  retrofit;
    }

}
