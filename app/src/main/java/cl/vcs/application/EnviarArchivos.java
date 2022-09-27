package cl.vcs.application;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class EnviarArchivos extends AsyncTask<String, String, Void> {

    @Override
    protected Void doInBackground(String... strings) {

        Context context = MyApplication.getAppContext();
        String[] archivos = context.fileList();

        if(archivos.length == 0){
            Log.e("ELIMINAR_TXT", "No hay archivos");
        }else{
            getArchivos(context, archivos);
        }
        return null;
    }

    private void getArchivos(Context context, String[] archivos){

        int intentosToma = 0;
        int intentosBoletas = 0;

        for(String archivo : archivos){
            if(archivo.equalsIgnoreCase("Toma_de_estado.txt")) {
                while(intentosToma < 2){
                    File dir = context.getFilesDir();
                    File file = new File(dir, "Toma_de_estado.txt");

                    StringBuilder stringBuilder = ArchivoTexto.leerArchivo(context, "Toma_de_estado.txt");

                    try {
                        JSONArray jsonArray = new JSONArray(stringBuilder.toString());
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        String usuario = jsonObject.getString("Usuario");
                        if (enviar(fileToBase64(file), "Toma_de_estado.txt", usuario)) {
                            file.delete();
                            Log.e("ELIMINAR_TXT", "Archivo Toma de estado.txt eliminado");
                        }else{
                            charSet(context, "Toma_de_estado.txt");
                            intentosToma++;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }else if(archivo.equalsIgnoreCase("Boletas.txt")){
                while(intentosBoletas < 2){
                    File dir = context.getFilesDir();
                    File file = new File(dir, "Boletas.txt");

                    StringBuilder stringBuilder = ArchivoTexto.leerArchivo(context, "Boletas.txt");

                    try {
                        JSONArray jsonArray = new JSONArray(stringBuilder.toString());
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        String usuario = jsonObject.getString("Usuario");
                        if(enviar(fileToBase64(file), "Boletas.txt", usuario)){
                            file.delete();
                            Log.e("ELIMINAR_TXT", "Archivo Boletas.txt eliminado");
                        }else{
                            charSet(context, "Boletas.txt");
                            intentosBoletas++;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean enviar(String archivoString, String nombreArchivo, String usuario){

        String url = "https://apimovil.vrrd.cl/api/Archivo";

        try {

            JSONObject jsonBody = new JSONObject();

            jsonBody.put("Usuario", usuario);
            jsonBody.put("NombreDocumento", nombreArchivo);
            jsonBody.put("Documento", archivoString);

            Log.e("JSON_ARCHIVO", jsonBody.toString());

            URL object = new URL(url);

            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    HostnameVerifier hv =
                            HttpsURLConnection.getDefaultHostnameVerifier();
                    return true;
                }
            };

            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) object.openConnection();
            httpsURLConnection.setSSLSocketFactory(Certificado.getSSLSocketFactory(MyApplication.getAppContext()));
            httpsURLConnection.setHostnameVerifier(hostnameVerifier);
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            httpsURLConnection.setRequestMethod("POST");

            DataOutputStream localDataOutputStream = new DataOutputStream(httpsURLConnection.getOutputStream());
            localDataOutputStream.write(jsonBody.toString().getBytes("UTF-8"));
            localDataOutputStream.flush();
            localDataOutputStream.close();

            if (httpsURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader((httpsURLConnection.getInputStream())));
                StringBuilder sb = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
                Log.e("LOG_RESPONSE_OK", String.valueOf(httpsURLConnection.getResponseCode()));
                Log.e("LOG_RESPUESTA", sb.toString());
                if(sb.toString().equalsIgnoreCase("true")){
                    return true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void charSet(Context context, String nombreArchivo){

        StringBuilder stringBuilder = ArchivoTexto.leerArchivo(context, nombreArchivo);

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(nombreArchivo, Activity.MODE_PRIVATE));

            if(stringBuilder != null){
                outputStreamWriter.write(new String(stringBuilder.toString().getBytes("UTF-8")));
            }

            outputStreamWriter.flush();
            outputStreamWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String fileToBase64(File file) {

        byte[] fileArray = new byte[(int) file.length()];

        try {
            InputStream inputStream = new FileInputStream(file);
            inputStream.read(fileArray);

            String fileString = Base64.encodeToString(fileArray, Base64.DEFAULT);

            return fileString;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
