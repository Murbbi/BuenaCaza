package dao;

import conexion.Conexion;
import modelo.Reserva;
import modelo.ReservaDetalle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {
    public List<Reserva> listar() throws SQLException {
        String sql = "SELECT id_reserva, id_cabana, id_invitado, id_temporada, fecha_entrada, fecha_salida, monto_total, comentario FROM reserva ORDER BY id_reserva";
        List<Reserva> reservas = new ArrayList<>();

        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                reservas.add(new Reserva(
                        resultado.getInt("id_reserva"),
                        resultado.getInt("id_cabana"),
                        resultado.getInt("id_invitado"),
                        resultado.getInt("id_temporada"),
                        LocalDate.parse(resultado.getString("fecha_entrada")),
                        LocalDate.parse(resultado.getString("fecha_salida")),
                        resultado.getBigDecimal("monto_total"),
                        resultado.getString("comentario")
                ));
            }
        }

        return reservas;
    }

    public boolean insertar(Reserva reserva) throws SQLException {
        String sql = "INSERT INTO reserva (id_cabana, id_invitado, id_temporada, fecha_entrada, fecha_salida, monto_total, comentario) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, reserva.getIdCabana());
            sentencia.setInt(2, reserva.getIdInvitado());
            sentencia.setInt(3, reserva.getIdTemporada());
            sentencia.setDate(4, java.sql.Date.valueOf(reserva.getFechaEntrada()));
            sentencia.setDate(5, java.sql.Date.valueOf(reserva.getFechaSalida()));
            sentencia.setBigDecimal(6, reserva.getMontoTotal());
            sentencia.setString(7, reserva.getComentario());
            return sentencia.executeUpdate() == 1;
        }
    }

    public boolean estaDisponible(int idCabana, LocalDate entrada, LocalDate salida) throws SQLException {
        String sql = "SELECT c.estado, r.id_reserva FROM cabana c "
                + "LEFT JOIN reserva r ON r.id_cabana = c.id_cabana "
                + "AND r.fecha_entrada < ? AND r.fecha_salida > ? "
                + "WHERE c.id_cabana = ? LIMIT 1";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setDate(1, java.sql.Date.valueOf(salida));
            sentencia.setDate(2, java.sql.Date.valueOf(entrada));
            sentencia.setInt(3, idCabana);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next() || !"libre".equalsIgnoreCase(resultado.getString("estado"))) {
                    return false;
                }
                resultado.getInt("id_reserva");
                return resultado.wasNull();
            }
        }
    }

    public boolean insertarSiDisponible(Reserva reserva) throws SQLException {
        String bloquearCabana = "SELECT estado FROM cabana WHERE id_cabana = ? FOR UPDATE";
        String verificarCruce = "SELECT 1 FROM reserva WHERE id_cabana = ? "
                + "AND fecha_entrada < ? AND fecha_salida > ? LIMIT 1 FOR UPDATE";
        String insertar = "INSERT INTO reserva "
                + "(id_cabana, id_invitado, id_temporada, fecha_entrada, fecha_salida, monto_total, comentario) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConexion()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement sentencia = conexion.prepareStatement(bloquearCabana)) {
                    sentencia.setInt(1, reserva.getIdCabana());
                    try (ResultSet resultado = sentencia.executeQuery()) {
                        if (!resultado.next() || !"libre".equalsIgnoreCase(resultado.getString("estado"))) {
                            throw new IllegalStateException("La cabaña seleccionada no está disponible.");
                        }
                    }
                }

                try (PreparedStatement sentencia = conexion.prepareStatement(verificarCruce)) {
                    sentencia.setInt(1, reserva.getIdCabana());
                    sentencia.setDate(2, java.sql.Date.valueOf(reserva.getFechaSalida()));
                    sentencia.setDate(3, java.sql.Date.valueOf(reserva.getFechaEntrada()));
                    try (ResultSet resultado = sentencia.executeQuery()) {
                        if (resultado.next()) {
                            throw new IllegalStateException(
                                    "La cabaña ya tiene una reserva que se superpone con esas fechas.");
                        }
                    }
                }

                try (PreparedStatement sentencia = conexion.prepareStatement(insertar)) {
                    sentencia.setInt(1, reserva.getIdCabana());
                    sentencia.setInt(2, reserva.getIdInvitado());
                    sentencia.setInt(3, reserva.getIdTemporada());
                    sentencia.setDate(4, java.sql.Date.valueOf(reserva.getFechaEntrada()));
                    sentencia.setDate(5, java.sql.Date.valueOf(reserva.getFechaSalida()));
                    sentencia.setBigDecimal(6, reserva.getMontoTotal());
                    sentencia.setString(7, reserva.getComentario());
                    boolean insertada = sentencia.executeUpdate() == 1;
                    conexion.commit();
                    return insertada;
                }
            } catch (SQLException | RuntimeException exception) {
                conexion.rollback();
                throw exception;
            }
        }
    }

    public List<ReservaDetalle> listarDetalles(Integer idInvitado) throws SQLException {
        String sql = "SELECT r.id_reserva, c.nombre AS cabana, i.nombre AS invitado, "
                + "r.fecha_entrada, r.fecha_salida, tt.tipo AS temporada, r.monto_total "
                + "FROM reserva r JOIN cabana c ON c.id_cabana = r.id_cabana "
                + "JOIN invitado i ON i.id_invitado = r.id_invitado "
                + "JOIN temporada t ON t.id_temporada = r.id_temporada "
                + "JOIN temporada_tipo tt ON tt.id_temporada_tipo = t.id_temporada_tipo "
                + (idInvitado == null ? "" : "WHERE r.id_invitado = ? ")
                + "ORDER BY r.id_reserva";
        List<ReservaDetalle> reservas = new ArrayList<>();
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            if (idInvitado != null) {
                sentencia.setInt(1, idInvitado);
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    java.sql.Date entrada = resultado.getDate("fecha_entrada");
                    java.sql.Date salida = resultado.getDate("fecha_salida");
                    reservas.add(new ReservaDetalle(resultado.getInt("id_reserva"),
                            resultado.getString("cabana"), resultado.getString("invitado"),
                            entrada.toLocalDate(), salida.toLocalDate(), resultado.getString("temporada"),
                            resultado.getBigDecimal("monto_total")));
                }
            }
        }
        return reservas;
    }
}
