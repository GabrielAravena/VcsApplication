package cl.vcs.application;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class Connection{

    public String getConnection(String urlConnection) {


        try {

            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    HostnameVerifier hv =
                            HttpsURLConnection.getDefaultHostnameVerifier();
                    return true;
                }
            };

            URL url = new URL (urlConnection);
            HttpsURLConnection httspURLConnection = (HttpsURLConnection) url.openConnection();
            httspURLConnection.setRequestMethod("GET");
            httspURLConnection.setSSLSocketFactory(Certificado.getSSLSocketFactory(MyApplication.getAppContext()));
            httspURLConnection.setHostnameVerifier(hostnameVerifier);
            httspURLConnection.setDoInput(true);
            httspURLConnection.connect();
            int code = httspURLConnection.getResponseCode();

            if(code == HttpURLConnection.HTTP_ACCEPTED || code == HttpURLConnection.HTTP_OK){
                InputStream in = new BufferedInputStream(httspURLConnection.getInputStream());

                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                httspURLConnection.disconnect();
                return sb.toString();
            }

            httspURLConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }
}
