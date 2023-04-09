package cl.vcs.application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.beardedhen.androidbootstrap.BootstrapButton;

public class Botones extends AppCompatActivity {

    BootstrapButton botonTomaEstado, botonEntregaBoleta, botonEntregaBoletaCodigo, botonCerrarSesion, botonEnviarDocumentos;
    String usuario = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_botones);

        usuario = getIntent().getStringExtra("usuario");

        botonTomaEstado = (BootstrapButton) findViewById(R.id.buttonTomaEstado);
        botonEntregaBoleta = (BootstrapButton) findViewById(R.id.buttonEntregaBoleta);
        botonEntregaBoletaCodigo = (BootstrapButton) findViewById(R.id.buttonEntregaBoletaCodigo);
        botonEnviarDocumentos = (BootstrapButton) findViewById(R.id.buttonEnviarDocumentos);
        botonCerrarSesion = (BootstrapButton) findViewById(R.id.buttonCerrarSesion);

        botonTomaEstado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Botones.this, Datos.class);
                intent.putExtra("usuario", usuario);
                startActivity(intent);
            }
        });

        botonEntregaBoleta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Botones.this, Boletas.class);
                intent.putExtra("usuario", usuario);
                startActivity(intent);
            }
        });

        botonEntregaBoletaCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Botones.this, BoletasCodigo.class);
                intent.putExtra("usuario", usuario);
                startActivity(intent);
            }
        });

        botonEnviarDocumentos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = MyApplication.getAppContext();

                String[] archivos = context.fileList();

                if(archivos.length == 0){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(Botones.this);
                    alertDialog.setMessage("No hay documentos para enviar. Este ya se envío correctamente.")
                            .setTitle("Envío de documento")
                            .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                    alertDialog.show();
                }else{
                    new EnviarArchivos().execute();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(Botones.this);
                    alertDialog.setMessage("El documento se ha enviado correctamente.")
                            .setTitle("Envío de documento")
                            .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                    alertDialog.show();
                }
            }
        });

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
            usuario = savedInstanceState.getString("comuna");
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

        outState.putString("usuario", usuario);
    }
}