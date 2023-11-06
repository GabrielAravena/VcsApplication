package cl.vcs.application;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
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
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class BoletasWorker extends Worker {

    public BoletasWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    static void enviarDatos(Data datos, Context context) {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        WorkRequest boletasWorkerRequest =
                new OneTimeWorkRequest.Builder(BoletasWorker.class)
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS)
                        .setConstraints(constraints)
                        .setInputData(datos)
                        .addTag("boletasWorker")
                        .build();

        WorkManager
                .getInstance(context)
                .enqueue(boletasWorkerRequest);
    }

    public static int[] contarWorkers(String tag, Context context) throws ExecutionException, InterruptedException {

        WorkManager workManager = WorkManager.getInstance(context);

        WorkQuery workQueryRunning = WorkQuery.Builder
                .fromTags(Arrays.asList("boletasWorker"))
                .addStates(Arrays.asList(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING))
                .build();

        WorkQuery workQueryFallido = WorkQuery.Builder
                .fromTags(Arrays.asList("boletasWorker"))
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

        String conjunto = getInputData().getString("Conjunto");
        String cantidadBoletas = getInputData().getString("CantidadBoletas");
        String nombreRecibeBoletas = getInputData().getString("NombreBoletas");
        String cantidadFacturas = getInputData().getString("CantidadFacturas");
        String rutFacturas = getInputData().getString("RutFacturas");
        String nombreRecibeFacturas = getInputData().getString("NombreFacturas");
        String comuna = getInputData().getString("Cliente");
        String latitud = getInputData().getString("Latitud");
        String longitud = getInputData().getString("Longitud");
        String recibe = getInputData().getString("recibe");
        String documento = getInputData().getString("documento");

        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("Conjunto", conjunto);
            jsonBody.put("CantidadBoleta", cantidadBoletas);
            jsonBody.put("NombreBoleta", nombreRecibeBoletas);
            jsonBody.put("CantidadFactura", cantidadFacturas);
            jsonBody.put("Rut", rutFacturas);
            jsonBody.put("NombreFactura", nombreRecibeFacturas);
            jsonBody.put("Usuario", comuna);
            jsonBody.put("Latitud", latitud);
            jsonBody.put("Longitud", longitud);
            jsonBody.put("recibe", recibe);
            jsonBody.put("documento", documento);

            Log.e("JSON_BODY", jsonBody.toString().replaceAll("ñ", "n").replaceAll("Ñ", "N"));

            String url = "https://movilrapp.amcospa.cl/api/EntregaBoleta";

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
            localDataOutputStream.write(jsonBody.toString().replaceAll("ñ", "n").replaceAll("Ñ", "N").getBytes("UTF-8"));
            localDataOutputStream.flush();
            localDataOutputStream.close();

            if(httpsURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                BufferedReader br = new BufferedReader(new InputStreamReader((httpsURLConnection.getInputStream())));
                StringBuilder sb = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
                sb.toString();
                Log.e("LOG_RESPONSE_OK", String.valueOf(httpsURLConnection.getResponseCode()));
                Log.e("LOG_RESPUESTA", sb.toString());
                if(sb.toString().equals("true")){
                    httpsURLConnection.disconnect();
                    return Result.success();
                }else{
                    httpsURLConnection.disconnect();
                    return Result.retry();
                }
            }

            httpsURLConnection.disconnect();
            Log.e("LOG_RESPONSE_FAILED", String.valueOf(httpsURLConnection.getResponseCode()));

        } catch (JSONException | MalformedURLException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.retry();
    }
}
