package modelo;

import dao.InvitadoDAO;

import java.sql.SQLException;

public class Invitado {
    private int idInvitado;
    private int dni;
    private String email;
    private String nombre;
    private String telefono;
    private String direccion;
    private String contrasenaHash;
    private boolean esAdmin;

    public Invitado() {
    }

    public Invitado(int idInvitado, int dni, String email, String nombre,
            String telefono, String direccion, boolean esAdmin) {
        this.idInvitado = idInvitado;
        this.dni = dni;
        this.email = email;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.esAdmin = esAdmin;
    }

    public Invitado(int idInvitado, int dni, String email, String nombre,
            String telefono, String direccion) {
        this.idInvitado = idInvitado;
        this.dni = dni;
        this.email = email;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.esAdmin = false;
    }

    public int getIdInvitado() {
        return idInvitado;
    }

    public void setIdInvitado(int idInvitado) {
        this.idInvitado = idInvitado;
    }

    public int getDni() {
        return dni;
    }

    public void setDni(int dni) {
        this.dni = dni;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public boolean isEsAdmin() {
        return esAdmin;
    }

    public void setEsAdmin(boolean esAdmin) {
        this.esAdmin = esAdmin;
    }

    public boolean isComunidad() throws SQLException {
        return new InvitadoDAO().tieneReservas(idInvitado);
    }

    @Override
    public String toString() {
        return idInvitado + " - " + nombre + " (DNI: " + dni + ", email: " + email
                + ", admin: " + esAdmin + ")";
    }
}