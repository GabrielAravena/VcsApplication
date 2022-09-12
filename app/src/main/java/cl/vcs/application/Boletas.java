package cl.vcs.application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapDropDown;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.beardedhen.androidbootstrap.BootstrapText;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class Boletas extends AppCompatActivity {

    CustomSearchableSpinner customSearchableSpinnerComunas;
    CustomSearchableSpinner customSearchableSpinnerConjuntos;
    RadioButton conConserje, sinConserje;
    CheckBox checkBoleta, checkFactura;
    BootstrapEditText cantidadBoletas, nombreRecibeBoletas, cantidadFacturas, rutFacturas, nombreRecibeFacturas;
    BootstrapButton salvarBoletas, botonCerrarSesion;
    ImageButton imageButton;
    TextView infoBoletasText, infoFacturasText, tipoDocumentoText;
    ProgressBar progressBar;
    TextView sincronizacionText1, sincronizacionText2;

    String comunaSelected = "", idComunaSelected = "", conjuntoSelected = "";

    String usuario;
    String[] conjuntos;
    String[] comunas;
    String[] idsComunas;
    String recibe = "";
    String documento = "";
    String latitud = "";
    String longitud = "";

    private static String urlComunas = "https://apimovil.vrrd.cl/api/Comuna?Usuario=";
    private static String urlConjuntos = "https://apimovil.vrrd.cl/api/ConjuntoBoletaV2?Usuario=";

    static final int REQUEST_LOCATION_CODE = 2;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;


    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boletas);

        imageButton = (ImageButton) findViewById(R.id.imageButton);
        customSearchableSpinnerComunas = (CustomSearchableSpinner) findViewById(R.id.customSearchableSpinnerComunas);
        customSearchableSpinnerConjuntos = (CustomSearchableSpinner) findViewById(R.id.customSearchableSpinnerConjuntos);
        tipoDocumentoText = (TextView) findViewById(R.id.tipoDocumentoText);
        conConserje = (RadioButton) findViewById(R.id.conConserje);
        sinConserje = (RadioButton) findViewById(R.id.sinConserje);
        checkBoleta = (CheckBox) findViewById(R.id.checkBoleta);
        checkFactura = (CheckBox) findViewById(R.id.checkFactura);
        infoBoletasText = (TextView) findViewById(R.id.infoBoletasText);
        cantidadBoletas = (BootstrapEditText) findViewById(R.id.cantidadBoletas);
        nombreRecibeBoletas = (BootstrapEditText) findViewById(R.id.nombreRecibeBoletas);
        infoFacturasText = (TextView) findViewById(R.id.infoFacturasText);
        cantidadFacturas = (BootstrapEditText) findViewById(R.id.cantidadFacturas);
        rutFacturas = (BootstrapEditText) findViewById(R.id.rutFacturas);
        nombreRecibeFacturas = (BootstrapEditText) findViewById(R.id.nombreRecibeFacturas);
        salvarBoletas = (BootstrapButton) findViewById(R.id.salvarBoletas);
        botonCerrarSesion = (BootstrapButton) findViewById(R.id.botonCerrarSesion);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        sincronizacionText1 = (TextView) findViewById(R.id.sincronizacionText1);
        sincronizacionText2 = (TextView) findViewById(R.id.sincronizacionText2);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                }
            }
        };

        usuario = getIntent().getStringExtra("usuario");
        progressBar.setVisibility(0);
        setDatosSincronizacion();

        new getComunas().execute(urlComunas + usuario);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Boletas.this, Botones.class);
                intent.putExtra("usuario", usuario);
                startActivity(intent);
            }
        });

        customSearchableSpinnerComunas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                customSearchableSpinnerComunas.isSpinnerDialogOpen = false;
                progressBar.setVisibility(0);
                comunaSelected = comunas[i];
                idComunaSelected = idsComunas[i];
                new getConjuntos().execute(urlConjuntos + usuario + "&IDUsuarioPerfil=" + idComunaSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                customSearchableSpinnerComunas.isSpinnerDialogOpen = false;
            }
        });

        customSearchableSpinnerConjuntos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                customSearchableSpinnerConjuntos.isSpinnerDialogOpen = false;

                if(!comunaSelected.trim().equalsIgnoreCase("") && !comunaSelected.trim().equalsIgnoreCase("Seleccione...")){
                    conjuntoSelected = conjuntos[i];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                customSearchableSpinnerConjuntos.isSpinnerDialogOpen = false;
            }
        });

        conConserje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tipoDocumentoText.setVisibility(View.VISIBLE);
                if(checkBoleta.isChecked()){
                    checkBoleta.toggle();
                }
                if(checkFactura.isChecked()){
                    checkFactura.toggle();
                }
                checkBoleta.setVisibility(View.VISIBLE);
                checkFactura.setVisibility(View.VISIBLE);
            }
        });

        sinConserje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tipoDocumentoText.setVisibility(View.GONE);
                checkBoleta.setVisibility(View.GONE);
                checkFactura.setVisibility(View.GONE);

                infoBoletasText.setVisibility(View.GONE);
                cantidadBoletas.setVisibility(View.GONE);
                nombreRecibeBoletas.setVisibility(View.GONE);

                infoFacturasText.setVisibility(View.GONE);
                cantidadFacturas.setVisibility(View.GONE);
                rutFacturas.setVisibility(View.GONE);
                nombreRecibeFacturas.setVisibility(View.GONE);
            }
        });

        checkBoleta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkBoleta.isChecked()){
                    infoBoletasText.setVisibility(View.VISIBLE);
                    cantidadBoletas.setVisibility(View.VISIBLE);
                    nombreRecibeBoletas.setVisibility(View.VISIBLE);
                }else{
                    infoBoletasText.setVisibility(View.GONE);
                    cantidadBoletas.setVisibility(View.GONE);
                    nombreRecibeBoletas.setVisibility(View.GONE);
                }
            }
        });

        checkFactura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkFactura.isChecked()){
                    infoFacturasText.setVisibility(View.VISIBLE);
                    cantidadFacturas.setVisibility(View.VISIBLE);
                    rutFacturas.setVisibility(View.VISIBLE);
                    nombreRecibeFacturas.setVisibility(View.VISIBLE);
                }else{
                    infoFacturasText.setVisibility(View.GONE);
                    cantidadFacturas.setVisibility(View.GONE);
                    rutFacturas.setVisibility(View.GONE);
                    nombreRecibeFacturas.setVisibility(View.GONE);
                }
            }
        });

        salvarBoletas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(
                        Boletas.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    createLocationRequest();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ActivityCompat.requestPermissions(Boletas.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
                    }
                }
            }
        });

        botonCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Boletas.this);
                alertDialog.setMessage("¿Está seguro que desea cerrar sesión?.")
                        .setTitle("Cerrar sesión")
                        .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Boletas.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                alertDialog.show();
            }
        });

        if(savedInstanceState != null){
            usuario = savedInstanceState.getString("usuario");
            comunas = savedInstanceState.getStringArray("comunas");
            conjuntos = savedInstanceState.getStringArray("conjuntos");
            cantidadBoletas.setText(savedInstanceState.getString("cantidadBoletas"));
            nombreRecibeBoletas.setText(savedInstanceState.getString("nombreRecibeBoletas"));
            cantidadFacturas.setText(savedInstanceState.getString("cantidadFacturas"));
            rutFacturas.setText(savedInstanceState.getString("rutFacturas"));
            nombreRecibeFacturas.setText(savedInstanceState.getString("nombreRecibeFacturas"));

            comunaSelected = savedInstanceState.getString("comunaSelected");
            if(conjuntos != null){
                customSearchableSpinnerComunas.setAdapter(new ArrayAdapter<>(Boletas.this, R.layout.spinner_item, comunas));
            }

            conjuntoSelected = savedInstanceState.getString("conjuntoSelected");
            if(conjuntos != null){
                customSearchableSpinnerConjuntos.setAdapter(new ArrayAdapter<>(Boletas.this, R.layout.spinner_item, conjuntos));
            }

            if(savedInstanceState.getBoolean("sinConserje")){
                tipoDocumentoText.setVisibility(View.GONE);
                checkBoleta.setVisibility(View.GONE);
                checkFactura.setVisibility(View.GONE);
            }

            if(savedInstanceState.getBoolean("conConserje") && savedInstanceState.getBoolean("checkBoleta")){
                infoBoletasText.setVisibility(View.VISIBLE);
                cantidadBoletas.setVisibility(View.VISIBLE);
                nombreRecibeBoletas.setVisibility(View.VISIBLE);
            }else{
                infoBoletasText.setVisibility(View.GONE);
                cantidadBoletas.setVisibility(View.GONE);
                nombreRecibeBoletas.setVisibility(View.GONE);
            }

            if(savedInstanceState.getBoolean("conConserje") && savedInstanceState.getBoolean("checkFactura")){
                infoFacturasText.setVisibility(View.VISIBLE);
                cantidadFacturas.setVisibility(View.VISIBLE);
                rutFacturas.setVisibility(View.VISIBLE);
                nombreRecibeFacturas.setVisibility(View.VISIBLE);
            }else{
                infoFacturasText.setVisibility(View.GONE);
                cantidadFacturas.setVisibility(View.GONE);
                rutFacturas.setVisibility(View.GONE);
                nombreRecibeFacturas.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Boletas.this, Botones.class);
        intent.putExtra("usuario", usuario);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("usuario", usuario);
        outState.putStringArray("conjuntos", conjuntos);
        outState.putString("conjuntoSelected", conjuntoSelected);
        outState.putBoolean("conConserje", conConserje.isChecked());
        outState.putBoolean("sinConserje", sinConserje.isChecked());
        outState.putBoolean("checkBoleta", checkBoleta.isChecked());
        outState.putBoolean("checkFactura", checkFactura.isChecked());
        outState.putString("cantidadBoletas", cantidadBoletas.getText().toString());
        outState.putString("nombreRecibeBoletas", nombreRecibeBoletas.getText().toString());
        outState.putString("cantidadFacturas", cantidadFacturas.getText().toString());
        outState.putString("rutFacturas", rutFacturas.getText().toString());
        outState.putString("nombreRecibeFacturas", nombreRecibeFacturas.getText().toString());
    }

    private void setDatosSincronizacion(){
        try {
            sincronizacionText1.setText("Ingresos por sincronizar: "+ BoletasWorker.contarWorkers("boletasWorker", this)[0]);
            sincronizacionText2.setText("Ingresos fallidos: "+ BoletasWorker.contarWorkers("boletasWorker", this)[1]);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void createLocationRequest() {
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000 * 10);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setNeedBle(true);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onComplete(Task<LocationSettingsResponse> result) {
                try {
                    LocationSettingsResponse response = result.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.

                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    }
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(Boletas.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        latitud = (""+location.getLatitude());
                                        longitud = (""+location.getLongitude());
                                        if(camposLlenos()){
                                            BoletasWorker.enviarDatos(datos(), Boletas.this);
                                            ArchivoTexto.guardar(Boletas.this, datosText(), "Boletas.txt");
                                            setDatosSincronizacion();
                                            vaciarConMensaje();
                                        }
                                    } else {
                                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                            return;
                                        }
                                        Toast.makeText(Boletas.this, "Espere un momento a que se cargue la geoposición", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        Boletas.this,
                                        REQUEST_LOCATION_CODE);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }

        });
    }

    private Data datos() {
        return new Data.Builder()
                .putString("Conjunto", conjuntoSelected)
                .putString("CantidadBoletas", cantidadBoletas.getText().toString())
                .putString("NombreBoletas", nombreRecibeBoletas.getText().toString())
                .putString("CantidadFacturas", cantidadFacturas.getText().toString())
                .putString("RutFacturas", rutFacturas.getText().toString())
                .putString("NombreFacturas", nombreRecibeFacturas.getText().toString())
                .putString("Cliente", usuario)
                .putString("Latitud", latitud)
                .putString("Longitud", longitud)
                .putString("recibe", recibe)
                .putString("documento", documento)
                .build();
    }

    private String datosText(){

        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("Conjunto", conjuntoSelected);
            jsonBody.put("CantidadBoleta", cantidadBoletas.getText().toString());
            jsonBody.put("NombreBoleta", nombreRecibeBoletas.getText().toString());
            jsonBody.put("CantidadFactura", cantidadFacturas.getText().toString());
            jsonBody.put("Rut", rutFacturas.getText().toString());
            jsonBody.put("NombreFactura", nombreRecibeFacturas.getText().toString());
            jsonBody.put("Usuario", usuario);
            jsonBody.put("Latitud", latitud);
            jsonBody.put("Longitud", longitud);
            jsonBody.put("recibe", recibe);
            jsonBody.put("documento", documento);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonBody.toString();
    }

    private void vaciarConMensaje() {
        progressBar.setVisibility(View.VISIBLE);

        eliminarConjunto(conjuntoSelected);

        conjuntoSelected = "";
        customSearchableSpinnerConjuntos.setAdapter(new ArrayAdapter<>(Boletas.this, R.layout.spinner_item, conjuntos));

        cantidadBoletas.setText("");
        nombreRecibeBoletas.setText("");
        cantidadFacturas.setText("");
        rutFacturas.setText("");
        nombreRecibeFacturas.setText("");
        recibe = "";
        documento = "";

        if(sinConserje.isChecked()){
            sinConserje.toggle();
        }
        if(checkBoleta.isChecked()){
            checkBoleta.toggle();
        }
        if(checkFactura.isChecked()){
            checkFactura.toggle();
        }

        if(cantidadBoletas.getVisibility() == View.VISIBLE){
            infoBoletasText.setVisibility(View.GONE);
            cantidadBoletas.setVisibility(View.GONE);
            nombreRecibeBoletas.setVisibility(View.GONE);
        }

        if(cantidadFacturas.getVisibility() == View.VISIBLE){
            infoFacturasText.setVisibility(View.GONE);
            cantidadFacturas.setVisibility(View.GONE);
            rutFacturas.setVisibility(View.GONE);
            nombreRecibeFacturas.setVisibility(View.GONE);
        }

        latitud = "";
        longitud = "";

        new getConjuntos().execute(urlConjuntos + usuario);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Boletas.this);
        alertDialog.setMessage("Los datos se han guardado correctamente.")
                .setTitle("Datos ingresados")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        alertDialog.show();
        progressBar.setVisibility(View.GONE);

    }

    private void eliminarConjunto(String conjuntoSelected) {
        if(conjuntos.length > 1){
            int newLength = conjuntos.length - 1;
            String[] auxConjuntos = new String[newLength];

            int j = 0;
            for(int i=0; i < conjuntos.length; i++){
                if(!conjuntos[i].equalsIgnoreCase(conjuntoSelected)){
                    auxConjuntos[j] = conjuntos[i];
                    j++;
                }
            }
            conjuntos = auxConjuntos;
            customSearchableSpinnerConjuntos.setAdapter(new ArrayAdapter<>(Boletas.this, R.layout.spinner_item, conjuntos));

        }else{
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Boletas.this);
            alertDialog.setMessage("Recuerda que debes tener conexión para sincronizar la aplicación. Al sincronizar se guardarán los datos y podrás recargar otro conjunto.")
                    .setTitle("No existen más conjuntos.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            alertDialog.show();

            String[] nulo = new String[1];
            nulo[0] = "No existen más conjuntos";
            customSearchableSpinnerConjuntos.setAdapter(new ArrayAdapter<>(Boletas.this, R.layout.spinner_item, nulo));
        }
    }

    private boolean camposLlenos() {
        if (comunaSelected.trim().equalsIgnoreCase("") || comunaSelected.trim().equalsIgnoreCase("Usuario sin comunas") || comunaSelected.trim().equalsIgnoreCase("Seleccione...")) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Boletas.this);
            alertDialog.setMessage("Debe seleccionar una comuna")
                    .setTitle("Alerta")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            alertDialog.show();
            return false;
        } else if (conjuntoSelected.trim().equalsIgnoreCase("") || conjuntoSelected.trim().equalsIgnoreCase("Usuario sin conjuntos") || conjuntoSelected.trim().equalsIgnoreCase("Seleccione...")) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Boletas.this);
            alertDialog.setMessage("Debe seleccionar un conjunto.")
                    .setTitle("Alerta")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            alertDialog.show();
            return false;
        } else if (!conConserje.isChecked() && !sinConserje.isChecked()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Boletas.this);
            alertDialog.setMessage("Debe seleccionar quien recibe.")
                    .setTitle("Alerta")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            alertDialog.show();
            return false;
        } else if (conConserje.isChecked() && checkBoleta.isChecked() && cantidadBoletas.getText().toString().trim().equalsIgnoreCase("")) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Boletas.this);
            alertDialog.setMessage("Debe ingresar cantidad de boletas.")
                    .setTitle("Alerta")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            alertDialog.show();
            return false;
        } else if (conConserje.isChecked() && checkBoleta.isChecked() && nombreRecibeBoletas.getText().toString().trim().equalsIgnoreCase("")) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Boletas.this);
            alertDialog.setMessage("Debe ingresar nombre de quien recibe boletas.")
                    .setTitle("Alerta")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            alertDialog.show();
            return false;
        } else if (conConserje.isChecked() && checkFactura.isChecked() && cantidadFacturas.getText().toString().trim().equalsIgnoreCase("")) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Boletas.this);
            alertDialog.setMessage("Debe ingresar cantidad de facturas.")
                    .setTitle("Alerta")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            alertDialog.show();
            return false;
        } else if (conConserje.isChecked() && checkFactura.isChecked() && rutFacturas.getText().toString().trim().equalsIgnoreCase("")) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Boletas.this);
            alertDialog.setMessage("Debe ingresar rut para las facturas.")
                    .setTitle("Alerta")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            alertDialog.show();
            return false;
        } else if (conConserje.isChecked() && checkFactura.isChecked() && nombreRecibeFacturas.getText().toString().trim().equalsIgnoreCase("")) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Boletas.this);
            alertDialog.setMessage("Debe ingresar nombre de quien recibe facturas.")
                    .setTitle("Alerta")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            alertDialog.show();
            return false;
        } else if (latitud.trim().equalsIgnoreCase("")) {
            Toast.makeText(this, "Espere un momento a que se cargue la geoposición", Toast.LENGTH_SHORT).show();
            return false;
        } else if (longitud.trim().equalsIgnoreCase("")) {
            Toast.makeText(this, "Espere un momento a que se cargue la geoposición", Toast.LENGTH_SHORT).show();
            return false;
        } else if(conConserje.isChecked() && !checkBoleta.isChecked() && !checkFactura.isChecked()){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Boletas.this);
            alertDialog.setMessage("Debe seleccionar tipo de documento.")
                    .setTitle("Alerta")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            alertDialog.show();
            return false;
        }

        if(!checkBoleta.isChecked()){
            cantidadBoletas.setText("");
            nombreRecibeBoletas.setText("");
        }

        if(!checkFactura.isChecked()){
            cantidadFacturas.setText("");
            rutFacturas.setText("");
            nombreRecibeFacturas.setText("");
        }

        if(checkBoleta.isChecked()){
            documento = "Boleta";
        }
        if(checkFactura.isChecked()){
            documento = "Factura";
        }
        if(checkBoleta.isChecked() && checkFactura.isChecked()){
            documento = "Ambos";
        }

        if(conConserje.isChecked()){
            recibe = "Con conserje";
        }else{
            recibe = "Sin conserje";
            cantidadBoletas.setText("");
            nombreRecibeBoletas.setText("");
            cantidadFacturas.setText("");
            rutFacturas.setText("");
            nombreRecibeFacturas.setText("");
            documento = "";
        }
        return true;
    }

    private class getComunas extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            Connection connection = new Connection();
            String jsonString = connection.getConnection(strings[0]);
            return jsonString;
        }

        @SuppressLint("WrongConstant")
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            if (response != null) {

                response = response.replaceFirst("[\\s\\S]{0,1}$", "").replaceAll("[\\\\][\\\\][\"]", "'").replaceFirst("\"", "").replaceAll("\\\\", "").replaceAll("\\[", "").replaceAll("\\]", "");
                response = "[{'IDUsuarioPerfil':'0', 'Com_Reg':'Seleccione...'},"+response+"]";
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    if (jsonArray.length() > 1) {
                        comunas = new String[jsonArray.length()];
                        idsComunas = new String[jsonArray.length()];

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            comunas[i] = jsonObject.getString("Com_Reg").replaceAll("'", "\"");
                            idsComunas[i] = jsonObject.getString("IDUsuarioPerfil").replaceAll("'", "\"");
                        }
                        customSearchableSpinnerComunas.setAdapter(new ArrayAdapter<>(Boletas.this, R.layout.spinner_item, comunas));
                    } else {
                        String[] nulo = new String[1];
                        nulo[0] = "No hay comunas";
                        customSearchableSpinnerComunas.setAdapter(new ArrayAdapter<>(Boletas.this, R.layout.spinner_item, nulo));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(Boletas.this, "Es posible que no tenga conexión a internet", Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(8);
        }
    }

    private class getConjuntos extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            Connection connection = new Connection();
            String jsonString = connection.getConnection(strings[0]);
            return jsonString;
        }

        @SuppressLint("WrongConstant")
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            if (response != null) {
                if(!comunaSelected.trim().equalsIgnoreCase("") && !comunaSelected.trim().equalsIgnoreCase("Seleccione...")){
                    response = response.replaceFirst("[\\s\\S]{0,1}$", "").replaceAll("[\\\\][\\\\][\"]", "'").replaceFirst("\"", "").replaceAll("\\\\", "").replaceAll("\\[", "").replaceAll("\\]", "");
                    response = "[{'Nombre':'Seleccione...'},"+response+"]";
                    try {

                        JSONArray jsonArray = new JSONArray(response);

                        if (jsonArray.length() > 0) {

                            conjuntos = new String[jsonArray.length()];

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                conjuntos[i] = jsonObject.getString("Nombre").replaceAll("'", "\"");
                            }
                            customSearchableSpinnerConjuntos.setAdapter(new ArrayAdapter<>(Boletas.this, R.layout.spinner_item, conjuntos));
                        } else {

                            String[] nulo = new String[1];
                            nulo[0] = "Usuario sin conjuntos";
                            customSearchableSpinnerConjuntos.setAdapter(new ArrayAdapter<>(Boletas.this, R.layout.spinner_item, nulo));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    String[] nulo = new String[1];
                    nulo[0] = "Seleccione...";
                    customSearchableSpinnerConjuntos.setAdapter(new ArrayAdapter<>(Boletas.this, R.layout.spinner_item, nulo));
                    conjuntoSelected = "";
                }
            } else {
                Toast.makeText(Boletas.this, "Es posible que no tenga conexión a internet", Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(8);
        }
    }
}
