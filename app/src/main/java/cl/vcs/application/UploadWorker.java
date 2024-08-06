package cl.vcs.application;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import static java.nio.charset.StandardCharsets.*;

public class UploadWorker extends Worker {

    TextView textView;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }

    static void enviarDatos(Data datos, Context context){

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        WorkRequest uploadWorkRequest =
                new OneTimeWorkRequest.Builder(UploadWorker.class)
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                1000 * 60,
                                TimeUnit.MILLISECONDS)
                        .setConstraints(constraints)
                        .setInputData(datos)
                        .addTag("uploaderWorker")
                        .build();

        WorkManager
                .getInstance(context)
                .enqueue(uploadWorkRequest);
    }

    public static int[] contarWorkers(String tag, Context context) throws ExecutionException, InterruptedException {

        WorkManager workManager = WorkManager.getInstance(context);

        WorkQuery workQueryRunning = WorkQuery.Builder
                .fromTags(Arrays.asList("uploaderWorker"))
                .addStates(Arrays.asList(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING))
                .build();

        WorkQuery workQueryFallido = WorkQuery.Builder
                .fromTags(Arrays.asList("uploaderWorker"))
                .addStates(Arrays.asList(WorkInfo.State.CANCELLED, WorkInfo.State.FAILED))
                .build();

        ListenableFuture<List<WorkInfo>> workInfos1 = workManager.getWorkInfos(workQueryRunning);
        ListenableFuture<List<WorkInfo>> workInfos3 = workManager.getWorkInfos(workQueryFallido);

        int[] lista = {workInfos1.get().size(), workInfos3.get().size()};

        return lista;
    }

    @NonNull
    @Override
    public Result doWork() {

        int IDTablaCarga = getInputData().getInt("IDTablaCarga", 1);
        String usuario = getInputData().getString("Usuario");
        String consumo = getInputData().getString("Consumo");
        String consumoAnterior = getInputData().getString("ConsumoAnterior");
        String medidor = getInputData().getString("Medidor");
        String clave = getInputData().getString("Clave");
        String cliente = getInputData().getString("Cliente");
        String conjunto = getInputData().getString("Conjunto");
        String latitud = getInputData().getString("Latitud");
        String longitud = getInputData().getString("Longitud");
        String rutaImagen = getInputData().getString("RutaImagen");
        String nombreImagen = getInputData().getString("NombreImagen");
        Boolean checkMedidor = getInputData().getBoolean("checkMedidor", false);
        String currentPhotoPath = getInputData().getString("currentPhotoPath");

        String imagen = Imagen.getFotoFromPath(currentPhotoPath);

        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("IDTablaCarga", IDTablaCarga);
            jsonBody.put("Usuario", usuario);
            jsonBody.put("Consumo", consumo);
            jsonBody.put("ConsumoAnterior", consumoAnterior);
            jsonBody.put("Medidor", medidor);
            jsonBody.put("Clave", clave);
            jsonBody.put("Cliente", cliente);
            jsonBody.put("Conjunto", conjunto);
            jsonBody.put("Latitud", latitud);
            jsonBody.put("Longitud", longitud);
            jsonBody.put("RutaImagen", nombreImagen + ".jpg");
            jsonBody.put("NombreImagen", nombreImagen + ".jpg");
            jsonBody.put("ChekMedidor", checkMedidor);
            jsonBody.put("Imagenbase64", imagen);

            Log.e("JSON_TOMA", jsonBody.toString().replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("Á", "A"));

            String url = "https://movilrapp.amcospa.cl/api/TomaEstado";

            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    HostnameVerifier hv =
                            HttpsURLConnection.getDefaultHostnameVerifier();
                    return true;
                }
            };

            URL object = new URL(url);

            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) object.openConnection();
            httpsURLConnection.setSSLSocketFactory(Certificado.getSSLSocketFactory(MyApplication.getAppContext()));
            httpsURLConnection.setHostnameVerifier(hostnameVerifier);
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            httpsURLConnection.setRequestMethod("POST");

            DataOutputStream localDataOutputStream = new DataOutputStream(httpsURLConnection.getOutputStream());
            localDataOutputStream.write(jsonBody.toString().replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("Á", "A").getBytes("UTF-8"));
            localDataOutputStream.flush();
            localDataOutputStream.close();

            if(httpsURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                BufferedReader br = new BufferedReader(new InputStreamReader((httpsURLConnection.getInputStream())));
                StringBuilder sb = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
                Log.e("LOG_RESPONSE_OK", String.valueOf(httpsURLConnection.getResponseCode()));
                Log.e("LOG_RESPUESTA", sb.toString());
                if(sb.toString().equals("true")){
                    httpsURLConnection.disconnect();
                    File file = new File(currentPhotoPath);
                    file.delete();
                    return Result.success();
                }else{
                    httpsURLConnection.disconnect();
                    return Result.retry();
                }
            }

            httpsURLConnection.disconnect();
            Log.e("LOG_RESPONSE_FAILED", String.valueOf(httpsURLConnection.getResponseCode()));

        } catch (JSONException e) {
            Log.e("ErrorAPP0",e.toString());
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            Log.e("ErrorAPP1",e.toString());
            e.printStackTrace();
        } catch (ProtocolException e) {
            Log.e("ErrorAPP2",e.toString());
            e.printStackTrace();
        } catch (MalformedURLException e) {
            Log.e("ErrorAPP3",e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("ErrorAPP4",e.toString());
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return Result.retry();
    }
}
