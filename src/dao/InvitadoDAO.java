package dao;

import conexion.Conexion;
import modelo.Administrador;
import modelo.Invitado;
import seguridad.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InvitadoDAO implements Dao<Invitado> {

    // Si el invitado tiene al menos una reserva es parte de la Comunidad Buena Caza
    public boolean tieneReservas(int idInvitado) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reserva WHERE id_invitado = ?";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idInvitado);
            ResultSet resultado = sentencia.executeQuery();
            resultado.next();
            return resultado.getInt(1) > 0;
        }
    }

    public Invitado autenticar(String email, char[] contrasena) throws SQLException {
        String sql = "SELECT id_invitado, dni, email, nombre, telefono, direccion, es_admin, contrasena_hash "
                + "FROM invitado WHERE email = ?";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, email.trim());
            ResultSet resultado = sentencia.executeQuery();
            if (resultado.next() && PasswordHasher.coincide(contrasena, resultado.getString("contrasena_hash"))) {
                return crearDesdeFila(resultado);
            }
            return null;
        }
    }

    public boolean existeEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM invitado WHERE email = ?";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, email.trim());
            ResultSet resultado = sentencia.executeQuery();
            resultado.next();
            return resultado.getInt(1) > 0;
        }
    }

    // Registro desde la pantalla "Crear cuenta". Devuelve el invitado ya con su id.
    public Invitado registrar(Invitado invitado, char[] contrasena) throws SQLException {
        String sql = "INSERT INTO invitado (dni, email, nombre, telefono, direccion, contrasena_hash) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setInt(1, invitado.getDni());
            sentencia.setString(2, invitado.getEmail());
            sentencia.setString(3, invitado.getNombre());
            sentencia.setString(4, invitado.getTelefono());
            sentencia.setString(5, invitado.getDireccion());
            sentencia.setString(6, PasswordHasher.hash(contrasena));
            sentencia.executeUpdate();

            ResultSet claves = sentencia.getGeneratedKeys();
            if (claves.next()) {
                invitado.setIdInvitado(claves.getInt(1));
            }
            return invitado;
        }
    }

    @Override
    public List<Invitado> listar() throws SQLException {
        String sql = "SELECT id_invitado, dni, email, nombre, telefono, direccion, es_admin "
                + "FROM invitado ORDER BY id_invitado";
        List<Invitado> invitados = new ArrayList<>();

        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                invitados.add(crearDesdeFila(resultado));
            }
        }

        return invitados;
    }

    @Override
    public boolean insertar(Invitado invitado) throws SQLException {
        String sql = "INSERT INTO invitado (dni, email, nombre, telefono, direccion, es_admin) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, invitado.getDni());
            sentencia.setString(2, invitado.getEmail());
            sentencia.setString(3, invitado.getNombre());
            sentencia.setString(4, invitado.getTelefono());
            sentencia.setString(5, invitado.getDireccion());
            sentencia.setInt(6, invitado.isEsAdmin() ? 1 : 0);
            return sentencia.executeUpdate() == 1;
        }
    }

    public boolean actualizar(Invitado invitado) throws SQLException {
        String sql = "UPDATE invitado SET dni = ?, email = ?, nombre = ?, telefono = ?, direccion = ? "
                + "WHERE id_invitado = ?";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, invitado.getDni());
            sentencia.setString(2, invitado.getEmail());
            sentencia.setString(3, invitado.getNombre());
            sentencia.setString(4, invitado.getTelefono());
            sentencia.setString(5, invitado.getDireccion());
            sentencia.setInt(6, invitado.getIdInvitado());
            return sentencia.executeUpdate() == 1;
        }
    }

    public boolean eliminar(int idInvitado) throws SQLException {
        String sql = "DELETE FROM invitado WHERE id_invitado = ?";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idInvitado);
            return sentencia.executeUpdate() == 1;
        }
    }

    private Invitado crearDesdeFila(ResultSet resultado) throws SQLException {
        return crear(
                resultado.getInt("id_invitado"),
                resultado.getInt("dni"),
                resultado.getString("email"),
                resultado.getString("nombre"),
                resultado.getString("telefono"),
                resultado.getString("direccion"),
                resultado.getInt("es_admin") == 1);
    }

    // Según la columna es_admin se crea un Administrador o un Invitado.
    private Invitado crear(int idInvitado, int dni, String email, String nombre,
            String telefono, String direccion, boolean esAdmin) {
        if (esAdmin) {
            return new Administrador(idInvitado, dni, email, nombre, telefono, direccion);
        }
        return new Invitado(idInvitado, dni, email, nombre, telefono, direccion);
    }
}
