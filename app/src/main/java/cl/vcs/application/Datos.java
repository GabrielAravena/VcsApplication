package cl.vcs.application;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.work.Data;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapDropDown;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.beardedhen.androidbootstrap.BootstrapWell;
import com.beardedhen.androidbootstrap.TypefaceProvider;
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
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Datos extends AppCompatActivity {
    BootstrapDropDown dropDownClaves, dropDownConjuntos;
    CustomSearchableSpinner customSearchableSpinner;
    BootstrapWell bootstrapWell;
    BootstrapButton bootstrapButton3, botonFoto, bootstrapButton4, bootstrapButton5;
    BootstrapEditText consumoActual;
    ImageButton imageButton;
    ImageView imageView;
    ProgressBar progressBar;
    String latitud = "", longitud = "";
    String dropDown1Selected = "", dropDown2Selected = "", dropDown3Selected = "";
    String currentPhotoPath = "";
    TextView medidor;
    TextView consumoAnterior;
    TextView consumoActualText;
    TextView customSearchableSpinnerText;
    TextView sincronizacionText1, sincronizacionText2;
    CheckBox checkBox;
    Bitmap bitmapImage = null;
    String imageFileName = "";
    String comuna = "";
    String[] conjuntos;
    String[] direcciones;
    String[] claves;
    String[] idClaves;
    String clave = "";
    String[] consumoAnteriorList;
    String[] medidorList;
    int[] idTablaCargaList;
    int idTablaCarga = 0;
    boolean CheckMedidor = false;

    private static String urlConjuntos = "https://apimovil.vrrd.cl/api/Conjunto?Usuario=";
    private static String urlDirecciones = "https://apimovil.vrrd.cl/api/DireccionV2?Conjunto=";
    private static String urlClaves = "https://apimovil.vrrd.cl/api/Clave";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    static final int REQUEST_LOCATION_CODE = 2;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_CAMERA_CODE = 3;

    @SuppressLint({"WrongConstant", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceProvider.registerDefaultIconSets();
        setContentView(R.layout.activity_datos);

        imageButton = (ImageButton) findViewById(R.id.imageButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        dropDownClaves = (BootstrapDropDown) findViewById(R.id.bootstrapDropDown2);
        dropDownConjuntos = (BootstrapDropDown) findViewById(R.id.bootstrapDropDown3);
        bootstrapWell = (BootstrapWell) findViewById(R.id.bootstrapWell);
        bootstrapButton3 = (BootstrapButton) findViewById(R.id.bootstrapButton3);
        imageView = (ImageView) findViewById(R.id.imageView);
        botonFoto = (BootstrapButton) findViewById(R.id.botonFoto);
        bootstrapButton4 = (BootstrapButton) findViewById(R.id.bootstrapButton4);
        bootstrapButton5 = (BootstrapButton) findViewById(R.id.bootstrapButton5);
        medidor =  (TextView) findViewById(R.id.medidor);
        consumoAnterior = (TextView) findViewById(R.id.consumoAnterior);
        consumoActual = (BootstrapEditText) findViewById(R.id.consumoActual);
        consumoActualText = (TextView) findViewById(R.id.textView6);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        customSearchableSpinner = (CustomSearchableSpinner) findViewById(R.id.customSearchableSpinner);
        customSearchableSpinnerText = (TextView) findViewById(R.id.spinner_item);
        //botonEnviarArchivo = (BootstrapButton) findViewById(R.id.bootstrapButton6);
        sincronizacionText1 = (TextView) findViewById(R.id.sincronizacionText1);
        sincronizacionText2 = (TextView) findViewById(R.id.sincronizacionText2);

        comuna = getIntent().getStringExtra("comuna");
        progressBar.setVisibility(0);
        new getConjuntos().execute(urlConjuntos + comuna);
        new getClaves().execute(urlClaves);

        setDatosSincronizacion();

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

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Datos.this, Botones.class);
                intent.putExtra("comuna", comuna);
                startActivity(intent);
            }
        });

        dropDownConjuntos.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View v, int id) {
                progressBar.setVisibility(0);
                vaciar();
                dropDown3Selected = (String) dropDownConjuntos.getDropdownData()[id];
                dropDownConjuntos.setText(dropDown3Selected);
                new getDirecciones().execute(urlDirecciones + dropDown3Selected + "&Usuario=" + comuna);
            }
        });

        customSearchableSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                customSearchableSpinner.isSpinnerDialogOpen = false;

                if(consumoAnteriorList != null){
                    dropDown1Selected = direcciones[i];
                    consumoAnterior.setText(consumoAnteriorList[i]);
                    medidor.setText(medidorList[i]);
                    idTablaCarga = idTablaCargaList[i];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                customSearchableSpinner.isSpinnerDialogOpen = false;

            }
        });

        dropDownClaves.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View v, int id) {
                dropDown2Selected = (String) dropDownClaves.getDropdownData()[id];
                dropDownClaves.setText(dropDown2Selected);

                if (dropDown2Selected.trim().equalsIgnoreCase("") || dropDown2Selected.trim().equalsIgnoreCase("No se han cargado los datos") || dropDown2Selected.trim().equalsIgnoreCase("No hay direcciones para este conjunto") || dropDown2Selected.trim().equalsIgnoreCase("Seleccione...")) {
                    consumoActual.setVisibility(View.VISIBLE);
                    consumoActualText.setVisibility(View.VISIBLE);
                    clave = "";
                }else{
                    consumoActual.setVisibility(View.GONE);
                    consumoActualText.setVisibility(View.GONE);
                    clave = idClaves[id];
                }
            }
        });

        botonFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(
                        Datos.this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ActivityCompat.requestPermissions(Datos.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_CODE);
                    }
                }
            }
        });

        bootstrapButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(
                        Datos.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    createLocationRequest();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ActivityCompat.requestPermissions(Datos.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
                    }
                }
            }
        });

        bootstrapButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Datos.this);
                alertDialog.setMessage("¿Está seguro que desea cerrar sesión?.")
                        .setTitle("Cerrar sesión")
                        .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Datos.this, MainActivity.class);
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

        bootstrapButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(0);
                vaciar();
                new getConjuntos().execute(urlConjuntos + comuna);
                setDatosSincronizacion();
                //new getDirecciones().execute(urlDirecciones + dropDown3Selected + "&Usuario=" + comuna);
            }
        });

        if(savedInstanceState != null){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Datos.this);
            alertDialog.setMessage("Debe volver a tomar la foto.")
                    .setTitle("Error de memoria")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            alertDialog.show();

            comuna = savedInstanceState.getString("comuna");
            conjuntos = savedInstanceState.getStringArray("conjuntos");
            direcciones = savedInstanceState.getStringArray("direcciones");
            claves = savedInstanceState.getStringArray("claves");
            idClaves = savedInstanceState.getStringArray("idClaves");
            clave = savedInstanceState.getString("clave");
            consumoAnteriorList = savedInstanceState.getStringArray("consumoAnteriorList");
            medidorList = savedInstanceState.getStringArray("medidorList");
            idTablaCargaList = savedInstanceState.getIntArray("idTablaCargaList");
            idTablaCarga = savedInstanceState.getInt("idTablaCarga");
            CheckMedidor = savedInstanceState.getBoolean("CheckMedidor");

            dropDown3Selected = savedInstanceState.getString("dropDown3Selected");
            dropDownConjuntos.setDropdownData(conjuntos);
            if(!dropDown3Selected.equalsIgnoreCase("")){
                dropDownConjuntos.setText(dropDown3Selected);
            }else{
                dropDownConjuntos.setText("Seleccione...");
            }

            dropDown1Selected = savedInstanceState.getString("dropDown1Selected");
            if(direcciones != null){
                customSearchableSpinner.setAdapter(new ArrayAdapter<>(Datos.this, R.layout.spinner_item, direcciones));
            }

            dropDown2Selected = savedInstanceState.getString("dropDown2Selected");
            dropDownClaves.setDropdownData(claves);
            if(!dropDown2Selected.equalsIgnoreCase("")){
                dropDownClaves.setText(dropDown2Selected);
            }else{
                dropDownClaves.setText("Seleccione...");
            }

            medidor.setText(savedInstanceState.getString("medidor"));
            consumoAnterior.setText(savedInstanceState.getString("consumoAnterior"));
            consumoActual.setVisibility(savedInstanceState.getInt("consumoVisibility"));
            consumoActualText.setVisibility(savedInstanceState.getInt("consumoVisibility"));
        }
    }

    private String datosText(){

        String imagen = Imagen.getFotoFromPath(currentPhotoPath);

        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("IDTablaCarga", idTablaCarga);
            jsonBody.put("Usuario", comuna);
            jsonBody.put("Consumo", consumoActual.getText().toString());
            jsonBody.put("ConsumoAnterior", consumoAnterior.getText().toString());
            jsonBody.put("Medidor", medidor.getText().toString());
            jsonBody.put("Clave", clave);
            jsonBody.put("Cliente", comuna);
            jsonBody.put("Conjunto", dropDown3Selected);
            jsonBody.put("Latitud", latitud);
            jsonBody.put("Longitud", longitud);
            jsonBody.put("RutaImagen", imageFileName + ".jpg");
            jsonBody.put("NombreImagen", imageFileName + ".jpg");
            jsonBody.put("ChekMedidor", CheckMedidor);
            jsonBody.put("Imagenbase64", imagen);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonBody.toString();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("comuna", comuna);
        outState.putStringArray("conjuntos", conjuntos);
        outState.putStringArray("direcciones", direcciones);
        outState.putStringArray("claves", claves);
        outState.putStringArray("idClaves", idClaves);
        outState.putString("clave", clave);
        outState.putStringArray("consumoAnteriorList", consumoAnteriorList);
        outState.putStringArray("medidorList", medidorList);
        outState.putIntArray("idTablaCargaList", idTablaCargaList);
        outState.putInt("idTablaCarga", idTablaCarga);
        outState.putBoolean("CheckMedidor", CheckMedidor);

        outState.putString("dropDown3Selected", dropDown3Selected);
        outState.putString("dropDown1Selected", dropDown1Selected);
        outState.putString("dropDown2Selected", dropDown2Selected);

        outState.putString("consumoActual", consumoActual.getText().toString());
        outState.putString("consumoAnterior", consumoAnterior.getText().toString());
        outState.putString("medidor", medidor.getText().toString());

        outState.putInt("consumoVisibility", consumoActual.getVisibility());

    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Datos.this, Botones.class);
        intent.putExtra("comuna", comuna);
        startActivity(intent);
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
                            .addOnSuccessListener(Datos.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        latitud = (""+location.getLatitude());
                                        longitud = (""+location.getLongitude());
                                        if(camposLlenos()){
                                            UploadWorker.enviarDatos(datos(), Datos.this);
                                            ArchivoTexto.guardar(Datos.this, datosText(), "Toma_de_estado.txt");
                                            setDatosSincronizacion();
                                            vaciarConMensaje();
                                        }
                                    } else {
                                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                            return;
                                        }
                                        Toast.makeText(Datos.this, "Espere un momento a que se cargue la geoposición", Toast.LENGTH_SHORT).show();

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
                                        Datos.this,
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

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void setDatosSincronizacion(){
        try {
            sincronizacionText1.setText("Ingresos por sincronizar: "+ UploadWorker.contarWorkers("uploadWorker", this)[0]);
            sincronizacionText2.setText("Ingresos fallidos: "+ UploadWorker.contarWorkers("uploadWorker", this)[1]);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private File createImageFile () throws IOException {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            currentPhotoPath = image.getAbsolutePath();
            return image;
    }

    private void dispatchTakePictureIntent () {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "cl.vcs.application.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            bitmapImage = BitmapFactory.decodeFile(currentPhotoPath);
            imageView.setImageBitmap(bitmapImage);
        }
    }

        private Data datos () {

            return new Data.Builder()
                    .putInt("IDTablaCarga", idTablaCarga)
                    .putString("Usuario", comuna)
                    .putString("Consumo", consumoActual.getText().toString())
                    .putString("ConsumoAnterior", consumoAnterior.getText().toString())
                    .putString("Medidor", medidor.getText().toString())
                    .putString("Clave", clave)
                    .putString("Cliente", comuna)
                    .putString("Conjunto", dropDown3Selected)
                    .putString("Latitud", latitud)
                    .putString("Longitud", longitud)
                    .putString("RutaImagen", imageFileName)
                    .putString("NombreImagen", imageFileName)
                    .putBoolean("checkMedidor", CheckMedidor)
                    .putString("currentPhotoPath", currentPhotoPath)
                    .build();
        }

        private boolean camposLlenos () {

            if (dropDown3Selected.trim().equalsIgnoreCase("") || dropDown3Selected.trim().equalsIgnoreCase("Usuario sin conjuntos") || dropDown3Selected.trim().equalsIgnoreCase("No hay direcciones para este conjunto")) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Datos.this);
                alertDialog.setMessage("Debe seleccionar conjunto.")
                        .setTitle("Alerta")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                alertDialog.show();
                return false;
            } else if (dropDown1Selected.trim().equalsIgnoreCase("") || dropDown1Selected.trim().equalsIgnoreCase("No se han cargado los datos") || dropDown1Selected.trim().equalsIgnoreCase("No hay direcciones para este conjunto") || dropDown1Selected.trim().equalsIgnoreCase("Seleccione...")) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Datos.this);
                alertDialog.setMessage("Debe seleccionar dirección.")
                        .setTitle("Alerta")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                alertDialog.show();
                return false;
            } else if (medidor.getText().toString().trim().equalsIgnoreCase("")) {
                medidor.setError("No se ha cargado información");
                return false;
            } else if (consumoAnterior.getText().toString().trim().equalsIgnoreCase("")) {
                consumoAnterior.setError("No se ha cargado información");
                return false;
            } else if (consumoActual.getText().toString().trim().equalsIgnoreCase("") && consumoActual.getVisibility() == View.VISIBLE) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Datos.this);
                alertDialog.setMessage("Debe ingresar el consumo actual.")
                        .setTitle("Alerta")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                alertDialog.show();
                return false;
            } else if (bitmapImage == null) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Datos.this);
                alertDialog.setMessage("Debe tomar una foto.")
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
            }

            if(consumoActual.getVisibility() == View.GONE){
                consumoActual.setText("");
            }

            if(checkBox.isChecked()){
                CheckMedidor = true;
            }

            return true;
        }

        @SuppressLint("WrongConstant")
        private void vaciarConMensaje () {
            progressBar.setVisibility(0);

            eliminarDireccion(idTablaCarga);

            idTablaCarga = 0;

            dropDown1Selected = "";

            dropDown2Selected = "";
            clave = "";
            dropDownClaves.setText("Seleccione...");

            medidor.setText("");
            consumoAnterior.setText("");
            consumoActual.setText("");
            currentPhotoPath = "";
            imageFileName = "";
            latitud = "";
            longitud = "";
            CheckMedidor = false;
            if(checkBox.isChecked()){
                checkBox.toggle();
            }
            if(consumoActual.getVisibility() == View.GONE){
                consumoActual.setVisibility(View.VISIBLE);
                consumoActualText.setVisibility(View.VISIBLE);
            }
            bitmapImage = null;
            imageView.setImageBitmap(null);

            new getConjuntos().execute(urlConjuntos+comuna);
            //new getDirecciones().execute(urlDirecciones + dropDown3Selected + "&Usuario=" + comuna);

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Datos.this);
            alertDialog.setMessage("Los datos se han guardado correctamente.")
                    .setTitle("Datos ingresados")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            alertDialog.show();
        }

    private void eliminarDireccion(int idTablaCarga) {

        if(idTablaCargaList.length > 2){
            int newLength = idTablaCargaList.length - 1;
            String[] auxDirecciones = new String[newLength];
            String[] auxConsumoAnteriorList = new String[newLength];
            String[] auxMedidorList = new String[newLength];
            int[] auxIdTablaCargaList = new int[newLength];

            int j = 0;
            for(int i=0; i < direcciones.length; i++){
                if(idTablaCargaList[i] != idTablaCarga){
                    auxDirecciones[j] = direcciones[i];
                    auxConsumoAnteriorList[j] = consumoAnteriorList[i];
                    auxMedidorList[j] = medidorList[i];
                    auxIdTablaCargaList[j] = idTablaCargaList[i];
                    j++;
                }
            }
            direcciones = auxDirecciones;
            consumoAnteriorList = auxConsumoAnteriorList;
            medidorList = auxMedidorList;
            idTablaCargaList = auxIdTablaCargaList;
            customSearchableSpinner.setAdapter(new ArrayAdapter<>(Datos.this, R.layout.spinner_item, direcciones));

        }else{
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Datos.this);
            alertDialog.setMessage("Recuerda que debes tener conexión para sincronizar la aplicación. Al sincronizar se guardarán los datos y podrás recargar otro conjunto.")
                    .setTitle("No existen más direcciones asociadas a este conjunto.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            alertDialog.show();

            String[] nulo = new String[1];
            nulo[0] = "No hay direcciones para este conjunto";
            customSearchableSpinner.setAdapter(new ArrayAdapter<>(Datos.this, R.layout.spinner_item, nulo));

        }

    }

    private void vaciar() {

            idTablaCarga = 0;

            dropDown1Selected = "";

            if(direcciones != null){
                if(direcciones.length > 2){
                    customSearchableSpinner.setAdapter(new ArrayAdapter<>(Datos.this, R.layout.spinner_item, direcciones));
                }else{
                    String[] nulo = new String[1];
                    nulo[0] = "No hay direcciones para este conjunto";
                    customSearchableSpinner.setAdapter(new ArrayAdapter<>(Datos.this, R.layout.spinner_item, nulo));
                }
            }

            dropDown2Selected = "";
            clave = "";
            dropDownClaves.setText("Seleccione...");

            medidor.setText("");
            consumoAnterior.setText("");
            consumoActual.setText("");
            currentPhotoPath = "";
            imageFileName = "";
            latitud = "";
            longitud = "";
            CheckMedidor = false;
            if(checkBox.isChecked()){
                checkBox.toggle();
            }
            bitmapImage = null;
            imageView.setImageBitmap(null);

            if(checkBox.isChecked()){
                checkBox.toggle();
            }
            if(consumoActual.getVisibility() == View.GONE){
                consumoActual.setVisibility(View.VISIBLE);
                consumoActualText.setVisibility(View.VISIBLE);
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
                    response = response.replaceFirst("[\\s\\S]{0,1}$", "").replaceFirst("\"", "").replaceAll("\\\\", "");

                    try {

                        JSONArray jsonArray = new JSONArray(response);

                        if (jsonArray.length() > 0) {

                            conjuntos = new String[jsonArray.length()];

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                conjuntos[i] = jsonObject.getString("Nombre");

                            }
                            dropDownConjuntos.setDropdownData(conjuntos);
                        } else {

                            String[] nulo = new String[1];
                            nulo[0] = "Usuario sin conjuntos";
                            dropDownConjuntos.setDropdownData(nulo);

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(Datos.this, "Es posible que no tenga conexión a internet", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(8);
            }
        }

        private class getDirecciones extends AsyncTask<String, String, String> {

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

                    response = response.replaceFirst("[\\s\\S]{0,1}$", "").replaceFirst("\"", "").replaceAll("\\\\", "").replaceAll("\\[", "").replaceAll("\\]", "");
                    response = "[{'Calle':'Seleccione...'},"+response+"]";

                    try {
                        JSONArray jsonArray = new JSONArray(response);

                        if (jsonArray.length() > 1) {
                            direcciones = new String[jsonArray.length()];
                            consumoAnteriorList = new String[jsonArray.length()];
                            medidorList = new String[jsonArray.length()];
                            idTablaCargaList = new int[jsonArray.length()];

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                direcciones[i] = jsonObject.getString("Calle");
                                if(i > 0){
                                    consumoAnteriorList[i] = jsonObject.getString("LecturaAnt");
                                    medidorList[i] = jsonObject.getString("Serie");
                                    idTablaCargaList[i] = Integer.valueOf(jsonObject.getString("IDTablaCarga"));
                                }
                            }
                            customSearchableSpinner.setAdapter(new ArrayAdapter<>(Datos.this, R.layout.spinner_item, direcciones));
                        } else {

                            String[] nulo = new String[1];
                            nulo[0] = "No hay direcciones para este conjunto";
                            customSearchableSpinner.setAdapter(new ArrayAdapter<>(Datos.this, R.layout.spinner_item, nulo));

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(Datos.this, "Es posible que no tenga conexión a internet", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(8);
            }
        }

        private class getClaves extends AsyncTask<String, String, String> {

            @Override
            protected String doInBackground(String... strings) {
                Connection connection = new Connection();
                String jsonString = connection.getConnection(strings[0]);
                return jsonString;
            }

            protected void onPostExecute(String response) {
                super.onPostExecute(response);

                if (response != null) {

                    response = response.replaceFirst("[\\s\\S]{0,1}$", "").replaceAll("\\[", "").replaceAll("\\]", "").replaceFirst("\"", "").replaceAll("\\\\", "");
                    response = "[{'IDClave':'0', 'Descripcion':'Seleccione...'},"+response+"]";

                    try {
                        JSONArray jsonArray = new JSONArray(response);

                        if (jsonArray.length() != 1) {
                            claves = new String[jsonArray.length()];
                            idClaves = new String[jsonArray.length()];
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                claves[i] = jsonObject.getString("Descripcion").replaceAll("\\,", "");
                                idClaves[i] = jsonObject.getString("IDClave").replaceAll("\\,", "");
                            }
                            dropDownClaves.setDropdownData(claves);
                        } else {
                            String[] nulo = new String[1];
                            nulo[0] = "No hay direcciones para este conjunto";
                            dropDownClaves.setDropdownData(nulo);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(Datos.this, "Es posible que no tenga conexión a internet", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

