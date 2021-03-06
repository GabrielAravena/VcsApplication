package cl.vcs.application;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
            for(String archivo : archivos){
                if(archivo.equalsIgnoreCase("Toma_de_estado.txt")){
                    File dir = context.getFilesDir();
                    File file = new File(dir, "Toma_de_estado.txt");
                    if(enviar(fileToBase64(file))){
                        file.delete();
                        Log.e("ELIMINAR_TXT", "Archivo Toma de estado.txt eliminado");
                    }

                }else if(archivo.equalsIgnoreCase("Boletas.txt")){
                    File dir = context.getFilesDir();
                    File file = new File(dir, "Boletas.txt");
                    if(enviar(fileToBase64(file))){
                        file.delete();
                        Log.e("ELIMINAR_TXT", "Archivo Boletas.txt eliminado");
                    }
                }
            }
        }
        return null;
    }

    private boolean enviar(String archivoString){
        Log.e("ARCHIVO", archivoString);
        String url = "https://apimovil.daim.cl/api/EntregaBoleta";

        try {

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
            localDataOutputStream.writeBytes(archivoString);
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
        }
        return false;
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
