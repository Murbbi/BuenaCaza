package controlador;

import dao.InvitadoDAO;
import modelo.Invitado;
import seguridad.PasswordHasher;

import java.sql.SQLException;

public class AutenticacionController {
    private InvitadoDAO invitadoDAO = new InvitadoDAO();

    // Devuelve el invitado si el correo y la contraseña son correctos, si no devuelve null
    public Invitado iniciarSesion(String email, char[] contrasena) throws SQLException {
        if (email.isBlank() || contrasena.length == 0) {
            return null;
        }
        return invitadoDAO.autenticar(email, contrasena);
    }

    public Invitado registrar(Invitado invitado, char[] contrasena) throws SQLException {
        PasswordHasher.validarContrasena(contrasena);
        if (invitadoDAO.existeEmail(invitado.getEmail())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo.");
        }
        return invitadoDAO.registrar(invitado, contrasena);
    }
}
