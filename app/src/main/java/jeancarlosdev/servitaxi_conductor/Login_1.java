package jeancarlosdev.servitaxi_conductor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import jeancarlosdev.servitaxi_conductor.Common.Common;
import jeancarlosdev.servitaxi_conductor.Modelos.ClienteBackJson;
import jeancarlosdev.servitaxi_conductor.Modelos.Conductor;
import jeancarlosdev.servitaxi_conductor.Modelos.MensajeBackJson;
import jeancarlosdev.servitaxi_conductor.Remote.Conexion;
import jeancarlosdev.servitaxi_conductor.Remote.VolleyPeticion;
import jeancarlosdev.servitaxi_conductor.Remote.VolleyProcesadorResultado;
import jeancarlosdev.servitaxi_conductor.Remote.VolleyTiposError;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Login_1 extends AppCompatActivity {

    Button btnSignIn, btnRegister;

    FirebaseDatabase db;

    DatabaseReference conductores;

    RelativeLayout layoutPrincipal;

    FirebaseAuth auth;

    private RequestQueue requestQueue;

    boolean guardo = false;

    boolean inicioSesion = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.
                Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf").
                        setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.frm_login_1);

        //Iniciar PaperDB
        Paper.init(this);

        auth = FirebaseAuth.getInstance();

        db = FirebaseDatabase.getInstance();

        conductores = db.getReference(Common.conductor_tb1);

        btnSignIn = (Button)findViewById(R.id.btnIniciar);

        btnRegister = (Button)findViewById(R.id.btnRegistrar);

        layoutPrincipal = (RelativeLayout)findViewById(R.id.layoutPrincipal);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarVentanaRegistro();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarVentanaLogin();
            }
        });


        String user = Paper.book().read(Common.conductor);
        String pass = Paper.book().read(Common.password);

        if(user != null && pass != null){
            if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)){
                autoLogin(user, pass);
            }
        }
    }

    private void autoLogin(String user, String pass) {

                        final android.app.AlertDialog dialogoEspera = new SpotsDialog(Login_1.this);

                        dialogoEspera.show();

                        //IniciarSesion.

                        auth.signInWithEmailAndPassword(user, pass)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        dialogoEspera.dismiss();

                                        FirebaseDatabase.getInstance().getReference(Common.conductor_tb1)
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        Common.currentUser = dataSnapshot.getValue(Conductor.class);
                                                        startActivity(new Intent(Login_1.this
                                                                , Bienvenido.class));

                                                        dialogoEspera.dismiss();

                                                        finish();
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialogoEspera.dismiss();
                                Snackbar.make(layoutPrincipal,
                                        "Error" +e.getMessage(),
                                        Snackbar.LENGTH_SHORT).show();

                                btnSignIn.setEnabled(true);

                            }
                        });



    }

    private void mostrarVentanaLogin() {
        final AlertDialog.Builder dialogInicio = new AlertDialog.Builder(this);

        dialogInicio.setTitle("Inicio de Sesion");

        dialogInicio.setMessage("Inicio de sesion conductor ServiTaxi");

        LayoutInflater inflater = LayoutInflater.from(this);

        View inicioSesion_layout = inflater.
                inflate(R.layout.layout_inicio_sesion, null);

        final MaterialEditText etxtEmailInicio = inicioSesion_layout.
                findViewById(R.id.etxtEmail);

        final MaterialEditText etxtContrasenaInicio = inicioSesion_layout.
                findViewById(R.id.etxtContrasena);


        dialogInicio.setView(inicioSesion_layout);

        dialogInicio.setPositiveButton("INICIAR SESION",
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                btnSignIn.setEnabled(false);

                /*Desactivo el boton de inicio de Sesion
                * para que el usuario no este manipulando*/

                if(TextUtils.isEmpty(etxtEmailInicio.getText().toString())){

                    Snackbar.make(layoutPrincipal,
                            "Por favor ingrese una direccion de correo " +
                                    "electronico",
                            Snackbar.LENGTH_SHORT).show();

                    return;
                }

                if(TextUtils.isEmpty(etxtContrasenaInicio.getText().toString())){

                    Snackbar.make(layoutPrincipal,
                            "Por favor ingrese contrasena"
                            , Snackbar.LENGTH_SHORT).show();

                    return;
                }

                if(etxtContrasenaInicio.getText().toString().length() < 6){

                    Snackbar.make(layoutPrincipal,
                            "Contrasena demasiado corta ",
                            Snackbar.LENGTH_SHORT).show();

                    return;
                }
                final android.app.AlertDialog dialogoEspera = new SpotsDialog(Login_1.this);

                dialogoEspera.show();

                HashMap<String, String> mapa = new HashMap<>();

                mapa.put("correo", etxtEmailInicio.getText().toString());

                mapa.put("clave", etxtContrasenaInicio.getText().toString());

                //IniciarSesion.

                auth.signInWithEmailAndPassword(etxtEmailInicio.getText().toString(),
                        etxtContrasenaInicio.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                dialogoEspera.dismiss();

                                FirebaseDatabase.getInstance().getReference(Common.conductor_tb1)
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    Common.currentUser = dataSnapshot.getValue(Conductor.class);
                                                    Paper.book().write(Common.conductor, etxtEmailInicio.getText().toString());
                                                    Paper.book().write(Common.password, etxtContrasenaInicio.getText().toString());

                                                    startActivity(new Intent(Login_1.this
                                                            , Bienvenido.class));

                                                    finish();

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialogoEspera.dismiss();
                        Snackbar.make(layoutPrincipal,
                                "Error" +e.getMessage(),
                                Snackbar.LENGTH_SHORT).show();

                        btnSignIn.setEnabled(true);

                        btnSignIn.setEnabled(true);
                    }
                });
            }
        });


        dialogInicio.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialogInicio.show();

    }

    private void mostrarVentanaRegistro() {
        AlertDialog.Builder dialog=  new AlertDialog.Builder(this);
        dialog.setTitle("Registrar Conductor");
        dialog.setMessage("Registro de Conductor ServiTaxi");

        LayoutInflater inflater = LayoutInflater.from(this);

        View register_Layout = inflater.
                inflate(R.layout.layout_registrar, null);

        final MaterialEditText etxtEmail = register_Layout.findViewById(
                R.id.etxtEmail);

        final MaterialEditText etxtContrasena = register_Layout.findViewById(
                R.id.etxtContrasena);

        final MaterialEditText etxtApellido = register_Layout.findViewById(
                R.id.etxtApellido);

        final MaterialEditText etxtNombre = register_Layout.findViewById(
                R.id.etxtNombre);

        dialog.setView(register_Layout);

        dialog.setPositiveButton("REGISTRAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                dialogInterface.dismiss();
                if(TextUtils.isEmpty(etxtEmail.getText().toString())){

                    Snackbar.make(layoutPrincipal,
                            "Por favor ingrese una direccion de correo " +
                                    "electronico",
                            Snackbar.LENGTH_SHORT).show();

                    return;
                }

                if(TextUtils.isEmpty(etxtContrasena.getText().toString())){

                    Snackbar.make(layoutPrincipal,
                            "Por favor ingrese contrasena"
                            , Snackbar.LENGTH_SHORT).show();

                    return;
                }


                if(etxtContrasena.getText().toString().length() < 6){

                    Snackbar.make(layoutPrincipal,
                            "Por favor ingrese una contrasena con mas de " +
                                    "6 caracteres ",
                            Snackbar.LENGTH_SHORT).show();

                    return;
                }

                if(TextUtils.isEmpty(etxtNombre.getText().toString())){

                    Snackbar.make(layoutPrincipal,
                            "Por favor ingrese su Nombre ",
                            Snackbar.LENGTH_SHORT).show();

                    return;
                }


                if(TextUtils.isEmpty(etxtApellido.getText().toString())){

                    Snackbar.make(layoutPrincipal,
                            "Por favor ingrese su telefono de celular ",
                            Snackbar.LENGTH_SHORT).show();

                    return;
                }

                Toast.makeText(getApplicationContext(),
                        "Se ha guardado correctamente",
                        Toast.LENGTH_SHORT).show();

                auth.createUserWithEmailAndPassword(
                        etxtEmail.getText().toString(),
                        etxtContrasena.getText().toString()
                )
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //Guardar Usuario en la base de datos.

                                Conductor conductor = new Conductor();

                                conductor.setEmail(etxtEmail.getText().toString());
                                conductor.setPassword(etxtContrasena.getText().toString());
                                conductor.setNombre(etxtNombre.getText().toString());
                                conductor.setApellido(etxtApellido.getText().toString());

                                //Usamos al email como llave primaria.
                                conductores.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(conductor)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Snackbar.make(layoutPrincipal,
                                                "Conductor Registrado correctamente",
                                                Snackbar.LENGTH_SHORT ).show();


                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Snackbar.make(layoutPrincipal,
                                                "Conductor no registrado " + e.getMessage(),
                                                Snackbar.LENGTH_SHORT ).show();

                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(layoutPrincipal,
                                "Conductor no registrado" + e.getMessage(),
                                Snackbar.LENGTH_SHORT ).show();
                    }
                });
            }
        });


        dialog.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    private boolean registrarCliente(
            @NonNull HashMap<String, String> map){

        VolleyPeticion<MensajeBackJson> registrar = Conexion.registrarCliente(
                getApplicationContext(),
                map,
                new Response.Listener<MensajeBackJson>() {
                    @Override
                    public void onResponse(MensajeBackJson response) {
                        if(response != null  && response.siglas.equalsIgnoreCase("FD")){

                            Snackbar.make(layoutPrincipal,
                                    "Error " + response.mensaje,
                                    Snackbar.LENGTH_SHORT
                            ).show();

                            btnSignIn.setEnabled(true);

                            guardo = true;

                            return;

                        }else{

                            Snackbar.make(layoutPrincipal,
                                    "Correcto, " +response.mensaje,
                                    Snackbar.LENGTH_SHORT).show();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        VolleyTiposError errors = VolleyProcesadorResultado.parseErrorResponse(error);

                        Log.e("Error", error.toString());

                        Snackbar.make(layoutPrincipal,
                                errors.errorMessage,
                                Snackbar.LENGTH_SHORT).show();

                        return;
                    }
                }
        );

        requestQueue.add(registrar);
        return guardo;
    }

    private boolean iniciarSesionCliente(
            @NonNull HashMap<String, String> map){

        VolleyPeticion<ClienteBackJson> inicio = Conexion.iniciarSesion(
                getApplicationContext(),
                map,
                new Response.Listener<ClienteBackJson>() {
                    @Override
                    public void onResponse(ClienteBackJson response) {

                        if(response != null
                                && response.siglas.equalsIgnoreCase("ND")){

                            Snackbar.make(layoutPrincipal,
                                    "Error " + response.mensaje,
                                    Snackbar.LENGTH_SHORT
                            ).show();

                            btnSignIn.setEnabled(true);

                            return;

                        }else {

                            Snackbar.make(layoutPrincipal,
                                    "Bienvenido, " + response.nombre,
                                    Snackbar.LENGTH_SHORT).show();

                            inicioSesion = true;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        VolleyTiposError errors = VolleyProcesadorResultado.parseErrorResponse(error);

                        Log.e("Error", error.toString());

                        Snackbar.make(layoutPrincipal,
                                errors.errorMessage,
                                Snackbar.LENGTH_SHORT).show();

                        return;
                    }
                }
        );

        requestQueue.add(inicio);

        return  inicioSesion;
    }
}
