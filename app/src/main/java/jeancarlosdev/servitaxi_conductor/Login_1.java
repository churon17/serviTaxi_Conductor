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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
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
import jeancarlosdev.servitaxi_conductor.Modelos.Cooperativa;
import jeancarlosdev.servitaxi_conductor.Modelos.MensajeBackJson;
import jeancarlosdev.servitaxi_conductor.Modelos.MensajeCoopBackJson;
import jeancarlosdev.servitaxi_conductor.Remote.Conexion;
import jeancarlosdev.servitaxi_conductor.Remote.VolleyPeticion;
import jeancarlosdev.servitaxi_conductor.Remote.VolleyProcesadorResultado;
import jeancarlosdev.servitaxi_conductor.Remote.VolleyTiposError;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Clase que nos ayudará para el inicio de sesión y el registrar por parte del Conductor.
 */

public class Login_1 extends AppCompatActivity {

    /***
     * Botón que nos ayudará para abrir la ventana de Login.
     */

    Button btnSignIn;

    /**
     * Botón que nos ayudará a abrir la ventana de Registro.
     */

    Button btnRegister;
    /**
     * Atributo para poder utilizar la Base de Datos de Firebase
     */
    FirebaseDatabase db;

    /**
     * Atributo para hacer referencia a la tabla conductores.
     */
    DatabaseReference conductores;

    /***
     * Variable de tipo RelativeLayout para posteriormente hacer uso del mismo en distintas métodos.
     */
    RelativeLayout layoutPrincipal;

    /**
     * Para la Autentificación con Firebase.
     */
    FirebaseAuth auth;

    /***
     * Para poder realizar las peticiones con Volley
     */
    private RequestQueue requestQueue;


    /***
     * Variable de tipo arreglo para  almacenar las Cooperativas que hay en el servidor.
     */
    private String[] listaCooperativas;

    /***
     * HashMap utilizado para constantemente enviar y recibir información del servidor.
     */
    HashMap<String, String> mapa;

    String NombreCoop = "";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    /***
     * Método invocado cuando se crea la Actividad.
     * En esté método inicializamos todas las variables a utilizar y hacemos llamados a algunos métodos como setOnClickListener para los botones.
     * @param savedInstanceState
     */
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

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        consultaCooperativas();

        db = FirebaseDatabase.getInstance();

        conductores = db.getReference(Common.conductor_tb1);

        btnSignIn = (Button) findViewById(R.id.btnIniciar);

        btnRegister = (Button) findViewById(R.id.btnRegistrar);

        layoutPrincipal = (RelativeLayout) findViewById(R.id.layoutPrincipal);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    mostrarVentanaRegistro();

                }catch (Exception e ){

                    Toast.makeText(Login_1.this, R.string.ocurrioUnError, Toast.LENGTH_SHORT).show();
                }


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

        if (user != null && pass != null) {
            if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)) {
                autoLogin(user, pass);
            }
        }
    }

    /**
     * Método que nos ayudará para verificar si previamente ha existido una sesión y no se ha cerrado.
     * Si es que no se ha cerrado la sesión automaticamente lo Redirigira a la Actividad Bienvenido.
     * @param user para verificar con Firebase y el servicio.
     * @param pass para verificar con Firebase y el servicio.
     * Estos dos parametros se obtendran previamente de los Paper.
     */
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
                        "Error" + e.getMessage(),
                        Snackbar.LENGTH_SHORT).show();

                btnSignIn.setEnabled(true);

            }
        });


    }

    /***
     * Muestra la ventana para Loguear al Conductor, este autentificación se hace tanto para Firebase como para el Servicio.
     */
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

                        if (TextUtils.isEmpty(etxtEmailInicio.getText().toString())) {

                            Snackbar.make(layoutPrincipal,
                                    "Por favor ingrese una direccion de correo " +
                                            "electronico",
                                    Snackbar.LENGTH_SHORT).show();

                            return;
                        }

                        if (TextUtils.isEmpty(etxtContrasenaInicio.getText().toString())) {

                            Snackbar.make(layoutPrincipal,
                                    "Por favor ingrese contrasena"
                                    , Snackbar.LENGTH_SHORT).show();

                            return;
                        }

                        if (etxtContrasenaInicio.getText().toString().length() < 6) {

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
                                        "Error" + e.getMessage(),
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

    /**
     * Método que actúa como Setter para la variable Cooperativa.
     * @param nombre el nombre que va a ser asignado a la variable Cooperativa.
     * @return el nombre de la Cooperativa.
     */
    private String retornarNombreCoop(String nombre){

        NombreCoop = nombre;

        return NombreCoop;
    }

    /***
     * Muestra la ventana para registrar al Conductor, este registro se hace tanto para Firebase como para el Servicio.
     */
    private void mostrarVentanaRegistro() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
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

        final MaterialEditText etxtPlaca = register_Layout.findViewById(
                R.id.etxtPlaca);

        Spinner spinnerCooperativa = register_Layout.findViewById(R.id.spinnerCooperativa);

        if (listaCooperativas.length > 0) {

            ArrayAdapter<String> adapter = new ArrayAdapter<String>
                    (register_Layout.getContext(), android.R.layout.simple_spinner_item, listaCooperativas);


            adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);

            spinnerCooperativa.setAdapter(adapter);

        }

        spinnerCooperativa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                final String  cooperativaEscogida =  parent.getItemAtPosition(pos).toString() ;

                retornarNombreCoop(cooperativaEscogida);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



        dialog.setView(register_Layout);

        dialog.setPositiveButton("REGISTRAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
                if (TextUtils.isEmpty(etxtEmail.getText().toString())) {

                    Snackbar.make(layoutPrincipal,
                            "Por favor ingrese una direccion de correo " +
                                    "electronico",
                            Snackbar.LENGTH_SHORT).show();

                    return;
                }

                if (TextUtils.isEmpty(etxtContrasena.getText().toString())) {

                    Snackbar.make(layoutPrincipal,
                            "Por favor ingrese contrasena"
                            , Snackbar.LENGTH_SHORT).show();

                    return;
                }


                if (etxtContrasena.getText().toString().length() < 6) {

                    Snackbar.make(layoutPrincipal,
                            "Por favor ingrese una contrasena con mas de " +
                                    "6 caracteres ",
                            Snackbar.LENGTH_SHORT).show();

                    return;
                }

                if (TextUtils.isEmpty(etxtNombre.getText().toString())) {

                    Snackbar.make(layoutPrincipal,
                            "Por favor ingrese su Nombre ",
                            Snackbar.LENGTH_SHORT).show();

                    return;
                }


                if (TextUtils.isEmpty(etxtApellido.getText().toString())) {

                    Snackbar.make(layoutPrincipal,
                            "Por favor ingrese su Apellido",
                            Snackbar.LENGTH_SHORT).show();

                    return;
                }

                if (TextUtils.isEmpty(etxtPlaca.getText().toString())) {

                    Snackbar.make(layoutPrincipal,
                            "Por favor ingrese su Placa del Vehiculo",
                            Snackbar.LENGTH_SHORT).show();

                    return;
                }

                mapa = new HashMap<>();

                mapa.put("correo", etxtEmail.getText().toString());

                mapa.put("clave", etxtContrasena.getText().toString());

                mapa.put("nombre", etxtNombre.getText().toString());

                mapa.put("apellido", etxtApellido.getText().toString());

                //region RegistrarCliente y Unidad.

                VolleyPeticion<MensajeBackJson> registrar = Conexion.registrarChofer(
                        getApplicationContext(),
                        mapa,
                        new Response.Listener<MensajeBackJson>() {
                            @Override
                            public void onResponse(MensajeBackJson response) {

                                if (response != null && ("FD".equalsIgnoreCase(response.siglas) || "DNF".equalsIgnoreCase(response.siglas))) {

                                    Toast.makeText(layoutPrincipal.getContext(), response.mensaje, Toast.LENGTH_SHORT).show();

                                    return;

                                } else {

                                    mapa = new HashMap<>();

                                    mapa.put("correo", etxtEmail.getText().toString());

                                    mapa.put("clave", etxtContrasena.getText().toString());


                                    VolleyPeticion<ClienteBackJson> inicio = Conexion.iniciarSesion(
                                            getApplicationContext(),
                                            mapa,
                                            new Response.Listener<ClienteBackJson>() {
                                                @Override
                                                public void onResponse(final ClienteBackJson responseCliente) {

                                                    if (responseCliente != null
                                                            && responseCliente.siglas.equalsIgnoreCase("ND")) {

                                                        Toast.makeText(Login_1.this, responseCliente.mensaje, Toast.LENGTH_SHORT).show();

                                                        return;

                                                    } else {

                                                        mapa = new HashMap<>();

                                                        mapa.put("nombre", NombreCoop);

                                                        VolleyPeticion<MensajeCoopBackJson> inicio = Conexion.retornarCoopExternal(
                                                                getApplicationContext(),
                                                                mapa,
                                                                new Response.Listener<MensajeCoopBackJson>() {
                                                                    @Override
                                                                    public void onResponse(MensajeCoopBackJson responseUnidad) {

                                                                        if (responseUnidad != null
                                                                                && responseUnidad.siglas.equalsIgnoreCase("ND")) {

                                                                            Toast.makeText(Login_1.this, responseUnidad.mensaje, Toast.LENGTH_SHORT).show();

                                                                            return;

                                                                        } else {


                                                                            mapa = new HashMap<>();

                                                                            mapa.put("llaveChofer", responseCliente.external);

                                                                            mapa.put("llaveCoop", responseUnidad.external);

                                                                            mapa.put("placa", etxtPlaca.getText().toString());


                                                                            VolleyPeticion<MensajeBackJson> registrar = Conexion.registrarUnidad(
                                                                                    getApplicationContext(),
                                                                                    mapa,
                                                                                    new Response.Listener<MensajeBackJson>() {
                                                                                        @Override
                                                                                        public void onResponse(MensajeBackJson response) {
                                                                                            if (response != null && response.siglas.equalsIgnoreCase("FD")) {

                                                                                                Toast.makeText(Login_1.this, response.mensaje, Toast.LENGTH_SHORT).show();

                                                                                                return;

                                                                                            } else {

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
                                                                                                                                        Snackbar.LENGTH_SHORT).show();


                                                                                                                            }
                                                                                                                        })
                                                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                                                            @Override
                                                                                                                            public void onFailure(@NonNull Exception e) {

                                                                                                                                Snackbar.make(layoutPrincipal,
                                                                                                                                        "Conductor no registrado " + e.getMessage(),
                                                                                                                                        Snackbar.LENGTH_SHORT).show();

                                                                                                                            }
                                                                                                                        });
                                                                                                            }
                                                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                                                    @Override
                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                        Snackbar.make(layoutPrincipal,
                                                                                                                "Conductor no registrado" + e.getMessage(),
                                                                                                                Snackbar.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                });

                                                                                            }
                                                                                        }
                                                                                    },
                                                                                    new Response.ErrorListener() {
                                                                                        @Override
                                                                                        public void onErrorResponse(VolleyError error) {

                                                                                            VolleyTiposError errors = VolleyProcesadorResultado.parseErrorResponse(error);

                                                                                            Log.e("Error", error.toString());

                                                                                            return;
                                                                                        }
                                                                                    }
                                                                            );

                                                                            requestQueue.add(registrar);


                                                                        }
                                                                    }
                                                                },
                                                                new Response.ErrorListener() {
                                                                    @Override
                                                                    public void onErrorResponse(VolleyError error) {

                                                                        VolleyTiposError errors = VolleyProcesadorResultado.parseErrorResponse(error);

                                                                        Toast.makeText(Login_1.this, error.toString(), Toast.LENGTH_SHORT).show();

                                                                        return;
                                                                    }
                                                                }
                                                        );

                                                        requestQueue.add(inicio);




                                                    }
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {

                                                    VolleyTiposError errors = VolleyProcesadorResultado.parseErrorResponse(error);

                                                    Toast.makeText(Login_1.this, error.toString(), Toast.LENGTH_SHORT).show();

                                                    return;
                                                }
                                            }
                                    );

                                    requestQueue.add(inicio);
                                }
                            }
                        },
                        new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {

                                VolleyTiposError errors = VolleyProcesadorResultado.parseErrorResponse(error);

                                Log.e("Error", error.toString());

                                return;
                            }
                        }
                );

                requestQueue.add(registrar);
                //endregion
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


    /**
     * Este método nos permite listar Todas las cooperativas, para el registro del Conductor.
     * No recibe parámetros.
     */
    private void consultaCooperativas() {

        VolleyPeticion<Cooperativa[]> listaNoticias = Conexion.listaCooperativas(
                getApplicationContext(),
                new Response.Listener<Cooperativa[]>() {
                    @Override
                    public void onResponse(Cooperativa[] response) {

                        listaCooperativas = new String[response.length];

                        for (int i = 0; i < response.length; i++) {

                            listaCooperativas[i] = response[i].nombre;

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Snackbar.make(layoutPrincipal,
                                error.getMessage(),
                                Snackbar.LENGTH_SHORT
                        ).show();

                    }
                }
        );
        requestQueue.add(listaNoticias);
    }


}
