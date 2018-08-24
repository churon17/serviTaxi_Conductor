package jeancarlosdev.servitaxi_conductor.Utilidades;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utilidades extends StringUtils {


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
