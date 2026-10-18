package dao;

import conexion.Conexion;
import modelo.Reserva;
import modelo.ReservaDetalle;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO implements Dao<Reserva> {
    @Override
    public List<Reserva> listar() throws SQLException {
        String sql = "SELECT id_reserva, id_cabana, id_invitado, id_temporada, fecha_entrada, fecha_salida, "
                + "monto_total, comentario FROM reserva ORDER BY id_reserva";
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
                        resultado.getDate("fecha_entrada").toLocalDate(),
                        resultado.getDate("fecha_salida").toLocalDate(),
                        resultado.getBigDecimal("monto_total"),
                        resultado.getString("comentario")));
            }
        }

        return reservas;
    }

    @Override
    public boolean insertar(Reserva reserva) throws SQLException {
        String sql = "INSERT INTO reserva (id_cabana, id_invitado, id_temporada, fecha_entrada, fecha_salida, "
                + "monto_total, comentario) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, reserva.getIdCabana());
            sentencia.setInt(2, reserva.getIdInvitado());
            sentencia.setInt(3, reserva.getIdTemporada());
            sentencia.setDate(4, Date.valueOf(reserva.getFechaEntrada()));
            sentencia.setDate(5, Date.valueOf(reserva.getFechaSalida()));
            sentencia.setBigDecimal(6, reserva.getMontoTotal());
            sentencia.setString(7, reserva.getComentario());
            return sentencia.executeUpdate() == 1;
        }
    }

    // Busca si hay otra reserva de la misma cabaña que se pise con las fechas elegidas
    public boolean estaDisponible(int idCabana, LocalDate entrada, LocalDate salida) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reserva WHERE id_cabana = ? AND fecha_entrada < ? AND fecha_salida > ?";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idCabana);
            sentencia.setDate(2, Date.valueOf(salida));
            sentencia.setDate(3, Date.valueOf(entrada));
            ResultSet resultado = sentencia.executeQuery();
            resultado.next();
            return resultado.getInt(1) == 0;
        }
    }

    // Para el reporte de ocupación: une la reserva con la cabaña, el invitado y el tipo de temporada
    public List<ReservaDetalle> listarDetalles() throws SQLException {
        String sql = "SELECT r.id_reserva, c.nombre AS cabana_nombre, i.nombre AS invitado_nombre, "
                + "r.fecha_entrada, r.fecha_salida, tt.tipo AS temporada_tipo, r.monto_total "
                + "FROM reserva r "
                + "JOIN cabana c ON c.id_cabana = r.id_cabana "
                + "JOIN invitado i ON i.id_invitado = r.id_invitado "
                + "JOIN temporada t ON t.id_temporada = r.id_temporada "
                + "JOIN temporada_tipo tt ON tt.id_temporada_tipo = t.id_temporada_tipo "
                + "ORDER BY r.id_reserva";
        List<ReservaDetalle> reservas = new ArrayList<>();

        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                reservas.add(new ReservaDetalle(
                        resultado.getInt("id_reserva"),
                        resultado.getString("cabana_nombre"),
                        resultado.getString("invitado_nombre"),
                        resultado.getDate("fecha_entrada").toLocalDate(),
                        resultado.getDate("fecha_salida").toLocalDate(),
                        resultado.getString("temporada_tipo"),
                        resultado.getBigDecimal("monto_total")));
            }
        }

        return reservas;
    }
}
