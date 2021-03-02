package cl.vcs.application;

import android.app.Activity;
import android.content.Context;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ArchivoTexto {

    public static void guardar(Context context, String dato, String nombreArchivo){

        StringBuilder stringBuilder = leerArchivo(context, nombreArchivo);

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(nombreArchivo, Activity.MODE_PRIVATE));

            if(stringBuilder != null){
                outputStreamWriter.write("[" + stringBuilder.toString().replaceAll("\\[", "").replaceAll("\\]", "") + "," + dato + "]");
            }else{
                outputStreamWriter.write("["+ dato + "]");
            }

            outputStreamWriter.flush();
            outputStreamWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static StringBuilder leerArchivo(Context context, String nombreArchivo) {

        try {
            InputStreamReader inputStreamReader = new InputStreamReader(context.openFileInput(nombreArchivo));

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String linea = "";
            StringBuilder stringBuilder = new StringBuilder();
            while ((linea = bufferedReader.readLine()) != null) {
            stringBuilder.append(linea);
            }

            return stringBuilder;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
