package cl.vcs.application;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.beardedhen.androidbootstrap.TypefaceProvider;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    BootstrapButton button;
    BootstrapEditText usuario;
    BootstrapEditText password;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TypefaceProvider.registerDefaultIconSets();
        setContentView(R.layout.activity_main);

        button = (BootstrapButton) findViewById(R.id.button);
        usuario = (BootstrapEditText) findViewById(R.id.usuario);
        password = (BootstrapEditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //setAlarm(getApplicationContext());
        ingresar();
    }

    private void setAlarm(Context context) {
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        Log.e("ALARMA", "Alarma creada!!!");
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, BootReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 37);

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    public void ingresar(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(camposLlenos()){
                    progressBar.setVisibility(view.VISIBLE);
                    new getUsuario().execute();
                }
            }
        });
    }

    private class getUsuario extends AsyncTask<String, String, String> {

        String stringUsuario = usuario.getText().toString();
        String stringPassword = password.getText().toString();

        @Override
        protected String doInBackground(String... strings) {
            Connection connection = new Connection();
            String response = connection.getConnection("https://example/api/Usuario?Usuario="+stringUsuario+"&Contrasena="+stringPassword);
            return response;
        }
        @SuppressLint("WrongConstant")
        protected void onPostExecute(String response){
            super.onPostExecute(response);

            if(response != null){
                response = response.replaceAll("\"", "").replaceAll("\\\\", "");

                if(!response.trim().equals("null")){
                    progressBar.setVisibility(8);
                    enviar(response);
                } else{
                    progressBar.setVisibility(8);
                    vaciarCampos();
                }
            }else{
                progressBar.setVisibility(8);
                Toast.makeText(MainActivity.this, "Es posible que no tenga conexión a internet", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void enviar(String response) {
        usuario.setText("");
        password.setText("");
        Intent intent = new Intent(this, Botones.class);
        intent.putExtra("comuna", response);
        startActivity(intent);
    }

    private boolean camposLlenos() {
        if (usuario.getText().toString().trim().equalsIgnoreCase("")) {
            usuario.setError("Debe ingresar usuario");
            return false;
        } else if (password.getText().toString().trim().equalsIgnoreCase("")) {
            password.setError("Debe ingresar contraseña");
            return false;
        }else{
            return true;
        }
    }

    private void vaciarCampos() {
        usuario.setText("");
        password.setText("");
        Toast.makeText(this, "Usuario o Contraseña incorrectos", Toast.LENGTH_SHORT).show();
    }
}
