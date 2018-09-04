package jeancarlosdev.servitaxi_conductor.Utilidades;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/***
 * Clase Utilidades que hereda de StringUtils, utilizada para manipular o verificar Strings.
 */
public class Utilidades extends StringUtils {


    /***
     * Método que nos ayuda a darle un formato a la Fecha
     * @param date esta fecha será la que se aplicará un formato.
     * @return un String con la fecha aplicada el formato.
     */
    public static String formatoFecha(Date date){
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strFecha = "";

        try {
            strFecha = formato.format(date);
            return strFecha;
        }
        catch (Exception ex){
            ex.printStackTrace();
            return "";
        }
    }

}
