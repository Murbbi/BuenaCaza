package modelo;

// Un administrador es un invitado con permisos para gestionar el complejo.
public class Administrador extends Invitado {

    public Administrador(int idInvitado, int dni, String email, String nombre,
            String telefono, String direccion) {
        super(idInvitado, dni, email, nombre, telefono, direccion, true);
    }

    @Override
    public String getRol() {
        return "Administrador";
    }

    @Override
    public boolean puedeAdministrar() {
        return true;
    }
}
