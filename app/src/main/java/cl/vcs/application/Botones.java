package cl.vcs.application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.beardedhen.androidbootstrap.BootstrapButton;

public class Botones extends AppCompatActivity {

    BootstrapButton botonTomaEstado, botonEntregaBoleta, botonCerrarSesion, botonEnviarDocumentos;
    String comuna = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_botones);

        comuna = getIntent().getStringExtra("comuna");

        botonTomaEstado = (BootstrapButton) findViewById(R.id.buttonTomaEstado);
        botonEntregaBoleta = (BootstrapButton) findViewById(R.id.buttonEntregaBoleta);
        //botonEnviarDocumentos = (BootstrapButton) findViewById(R.id.buttonEnviarDocumentos);
        botonCerrarSesion = (BootstrapButton) findViewById(R.id.buttonCerrarSesion);

        botonTomaEstado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Botones.this, Datos.class);
                intent.putExtra("comuna", comuna);
                startActivity(intent);
            }
        });

        botonEntregaBoleta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Botones.this, Boletas.class);
                intent.putExtra("comuna", comuna);
                startActivity(intent);
            }
        });

        /*botonEnviarDocumentos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new EnviarArchivos().execute();
            }
        });*/

        botonCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Botones.this);
                alertDialog.setMessage("¿Está seguro que desea cerrar sesión?.")
                        .setTitle("Cerrar sesión")
                        .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Botones.this, MainActivity.class);
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
            comuna = savedInstanceState.getString("comuna");
        }
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Botones.this);
        alertDialog.setMessage("¿Está seguro que desea cerrar sesión?.")
                .setTitle("Cerrar sesión")
                .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Botones.this, MainActivity.class);
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("comuna", comuna);
    }
}