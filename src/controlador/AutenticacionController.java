package controlador;

import dao.InvitadoDAO;
import modelo.Invitado;

import java.sql.SQLException;

public class AutenticacionController {
    private final InvitadoDAO invitadoDAO;

    public AutenticacionController() {
        this(new InvitadoDAO());
    }

    public AutenticacionController(InvitadoDAO invitadoDAO) {
        this.invitadoDAO = invitadoDAO;
    }

    public Invitado iniciarSesion(String email, char[] contrasena) throws SQLException {
        if (email == null || email.isBlank() || contrasena == null || contrasena.length == 0) {
            return null;
        }
        return invitadoDAO.autenticar(email, contrasena);
    }

    public Invitado registrar(Invitado invitado, char[] contrasena) throws SQLException {
        return invitadoDAO.registrar(invitado, contrasena);
    }
}