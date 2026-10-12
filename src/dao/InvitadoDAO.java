package dao;

import conexion.Conexion;
import modelo.Invitado;
import seguridad.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InvitadoDAO {
    public boolean tieneReservas(int idInvitado) throws SQLException {
        if (idInvitado < 1) {
            return false;
        }
        String sql = "SELECT EXISTS(SELECT 1 FROM reserva WHERE id_invitado = ?)";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idInvitado);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getBoolean(1);
            }
        }
    }

    public Invitado autenticar(String email, char[] contrasena) throws SQLException {
        String sql = "SELECT id_invitado, dni, email, nombre, telefono, direccion, es_admin, "
                + "contrasena_hash FROM invitado WHERE LOWER(email) = LOWER(?)";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, email.trim());
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()
                        || !PasswordHasher.coincide(contrasena, resultado.getString("contrasena_hash"))) {
                    return null;
                }
                return new Invitado(
                        resultado.getInt("id_invitado"),
                        resultado.getInt("dni"),
                        resultado.getString("email"),
                        resultado.getString("nombre"),
                        resultado.getString("telefono"),
                        resultado.getString("direccion"),
                        resultado.getInt("es_admin") == 1);
            }
        }
    }

    public Invitado registrar(Invitado invitado, char[] contrasena) throws SQLException {
        String hash = PasswordHasher.hash(contrasena);
        String buscarInvitado = "SELECT id_invitado, dni, contrasena_hash, es_admin "
                + "FROM invitado WHERE LOWER(email) = LOWER(?) FOR UPDATE";
        String actualizarInvitado = "UPDATE invitado SET dni = ?, nombre = ?, telefono = ?, direccion = ?, "
                + "contrasena_hash = ? WHERE id_invitado = ?";
        String insertarInvitado = "INSERT INTO invitado "
                + "(dni, email, nombre, telefono, direccion, contrasena_hash) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConexion()) {
            conexion.setAutoCommit(false);
            try {
                int idInvitado = 0;
                boolean esAdmin = false;
                try (PreparedStatement sentencia = conexion.prepareStatement(buscarInvitado)) {
                    sentencia.setString(1, invitado.getEmail().trim());
                    try (ResultSet resultado = sentencia.executeQuery()) {
                        if (resultado.next()) {
                            idInvitado = resultado.getInt("id_invitado");
                            if (resultado.getInt("dni") != invitado.getDni()) {
                                throw new IllegalArgumentException(
                                        "Ese correo ya pertenece a otro invitado. Verificá el DNI.");
                            }
                            if (resultado.getString("contrasena_hash") != null) {
                                throw new IllegalArgumentException("Ya existe una cuenta con ese correo.");
                            }
                            esAdmin = resultado.getInt("es_admin") == 1;
                        }
                    }
                }

                if (idInvitado == 0) {
                    try (PreparedStatement sentencia = conexion.prepareStatement(
                            insertarInvitado, Statement.RETURN_GENERATED_KEYS)) {
                        sentencia.setInt(1, invitado.getDni());
                        sentencia.setString(2, invitado.getEmail().trim());
                        sentencia.setString(3, invitado.getNombre().trim());
                        sentencia.setString(4, invitado.getTelefono());
                        sentencia.setString(5, invitado.getDireccion().trim());
                        sentencia.setString(6, hash);
                        sentencia.executeUpdate();
                        try (ResultSet claves = sentencia.getGeneratedKeys()) {
                            if (!claves.next()) {
                                throw new SQLException("La base de datos no devolvió el ID del invitado.");
                            }
                            idInvitado = claves.getInt(1);
                        }
                    }
                } else {
                    try (PreparedStatement sentencia = conexion.prepareStatement(actualizarInvitado)) {
                        sentencia.setInt(1, invitado.getDni());
                        sentencia.setString(2, invitado.getNombre().trim());
                        sentencia.setString(3, invitado.getTelefono());
                        sentencia.setString(4, invitado.getDireccion().trim());
                        sentencia.setString(5, hash);
                        sentencia.setInt(6, idInvitado);
                        sentencia.executeUpdate();
                    }
                }

                conexion.commit();
                Invitado registrado = new Invitado(idInvitado, invitado.getDni(), invitado.getEmail().trim(),
                        invitado.getNombre().trim(), invitado.getTelefono(), invitado.getDireccion().trim(),
                        esAdmin);
                registrado.setContrasenaHash(hash);
                return registrado;
            } catch (SQLException | RuntimeException exception) {
                conexion.rollback();
                throw exception;
            }
        }
    }

    public List<Invitado> listar() throws SQLException {
        String sql = "SELECT id_invitado, dni, email, nombre, telefono, direccion, es_admin "
                + "FROM invitado ORDER BY id_invitado";
        List<Invitado> invitados = new ArrayList<>();

        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                invitados.add(new Invitado(
                        resultado.getInt("id_invitado"),
                        resultado.getInt("dni"),
                        resultado.getString("email"),
                        resultado.getString("nombre"),
                        resultado.getString("telefono"),
                        resultado.getString("direccion"),
                        resultado.getInt("es_admin") == 1
                ));
            }
        }

        return invitados;
    }

    public boolean insertar(Invitado invitado) throws SQLException {
        String sql = "INSERT INTO invitado "
                + "(dni, email, nombre, telefono, direccion, contrasena_hash, es_admin) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, invitado.getDni());
            sentencia.setString(2, invitado.getEmail());
            sentencia.setString(3, invitado.getNombre());
            sentencia.setString(4, invitado.getTelefono());
            sentencia.setString(5, invitado.getDireccion());
            sentencia.setString(6, invitado.getContrasenaHash());
            sentencia.setInt(7, invitado.isEsAdmin() ? 1 : 0);
            return sentencia.executeUpdate() == 1;
        }
    }

    public boolean actualizar(Invitado invitado) throws SQLException {
        String sql = "UPDATE invitado SET dni = ?, email = ?, nombre = ?, telefono = ?, direccion = ?, "
                + "contrasena_hash = COALESCE(?, contrasena_hash) WHERE id_invitado = ?";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, invitado.getDni());
            sentencia.setString(2, invitado.getEmail().trim());
            sentencia.setString(3, invitado.getNombre().trim());
            sentencia.setString(4, invitado.getTelefono());
            sentencia.setString(5, invitado.getDireccion().trim());
            sentencia.setString(6, invitado.getContrasenaHash());
            sentencia.setInt(7, invitado.getIdInvitado());
            if (sentencia.executeUpdate() == 1) {
                return true;
            }
        }
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(
                        "SELECT 1 FROM invitado WHERE id_invitado = ?")) {
            sentencia.setInt(1, invitado.getIdInvitado());
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next();
            }
        }
    }

    public boolean eliminar(int idInvitado) throws SQLException {
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(
                        "DELETE FROM invitado WHERE id_invitado = ?")) {
            sentencia.setInt(1, idInvitado);
            return sentencia.executeUpdate() == 1;
        }
    }
}
