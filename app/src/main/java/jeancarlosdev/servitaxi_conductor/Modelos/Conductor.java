package jeancarlosdev.servitaxi_conductor.Modelos;

public class Conductor {

    private String email;

    private String nombre;

    private String password;

    private String phone;

    public Conductor() {
    }

    public Conductor(String email,
                     String nombre,
                     String password,
                     String phone) {

        this.email = email;
        this.nombre = nombre;
        this.password = password;
        this.phone = phone;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
