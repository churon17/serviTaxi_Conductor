package jeancarlosdev.servitaxi_conductor.Modelos;

/**
 * Clase utilizada para enviar y extraer datos en Firebase en la tabla Conductores.
 */
public class Conductor {

    /**
     * Atributo para obtener y enviar el email.
     */
    private String email;


    /**
     * Atributo para obtener y enviar el nombre.
     */

    private String nombre;

    /**
     * Atributo para obtener y enviar la contraseña.
     */

    private String password;

    /**
     * Atributo para obtener y enviar el apellido.
     */

    private String apellido;

    /**
     * Constructor de la clase, para inicializar sin necesidad de recibir atributos.
     * De esta manera poder manipular, en ejecución.
     */

    public Conductor() {
    }

    /**
     * Constructor de la clase, para inicializar obligado a recibir atributos.
     * De esta manera se inicializa inmediatamente la clase Conductor.
     */
    public Conductor(String email,
                     String nombre,
                     String password,
                     String apellido) {

        this.email = email;
        this.nombre = nombre;
        this.password = password;
        this.apellido = apellido;
    }

    /**
     *
     * @return Obtiene el atributo email, de la clase conductor.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Cambia el atributo email de la clase Conductor, recibe como parametro un String
     * @param email este parametro va a remplazar el atributo de la clase.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     * @return Obtiene el atributo nombre, de la clase conductor.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Cambia el atributo nombre de la clase Conductor, recibe como parametro un String
     * @param nombre este parametro va a remplazar el atributo de la clase.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     *
     * @return Obtiene el atributo password(Contraseña), de la clase conductor.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Cambia el atributo password(Contraseña) de la clase Conductor, recibe como parametro un String
     * @param password este parametro va a remplazar el atributo de la clase.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     *
     * @return Obtiene el atributo apellido, de la clase conductor.
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * Cambia el atributo  apellido de la clase Conductor, recibe como parametro un String
     * @param apellido este parametro va a remplazar el atributo de la clase.
     */
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
}
