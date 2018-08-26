package jeancarlosdev.servitaxi_conductor.Modelos;

public class Conductor {

    private String email;

    private String nombre;

    private String password;

    private String apellido;

    public Conductor() {
    }

    public Conductor(String email,
                     String nombre,
                     String password,
                     String apellido) {

        this.email = email;
        this.nombre = nombre;
        this.password = password;
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
}
