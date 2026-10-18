package dao;

import conexion.Conexion;
import modelo.Temporada;
import modelo.TemporadaTipo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TemporadaDAO implements Dao<Temporada> {
    public List<TemporadaTipo> listarTipos() throws SQLException {
        String sql = "SELECT id_temporada_tipo, tipo FROM temporada_tipo ORDER BY id_temporada_tipo";
        List<TemporadaTipo> tipos = new ArrayList<>();

        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                TemporadaTipo tipo = new TemporadaTipo();
                tipo.setIdTemporadaTipo(resultado.getInt("id_temporada_tipo"));
                tipo.setTipo(resultado.getString("tipo"));
                tipos.add(tipo);
            }
        }

        return tipos;
    }

    // Trae las temporadas junto con el nombre del tipo (Alta, Media, Baja)
    @Override
    public List<Temporada> listar() throws SQLException {
        String sql = "SELECT t.id_temporada, t.id_temporada_tipo, t.fecha_inicio, t.fecha_final, "
                + "t.precio_por_noche, tt.tipo FROM temporada t "
                + "JOIN temporada_tipo tt ON tt.id_temporada_tipo = t.id_temporada_tipo "
                + "ORDER BY t.fecha_inicio";
        List<Temporada> temporadas = new ArrayList<>();

        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                temporadas.add(leerTemporada(resultado));
            }
        }

        return temporadas;
    }

    @Override
    public boolean insertar(Temporada temporada) throws SQLException {
        String sql = "INSERT INTO temporada (id_temporada_tipo, fecha_inicio, fecha_final, precio_por_noche) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, temporada.getIdTemporadaTipo());
            sentencia.setDate(2, Date.valueOf(temporada.getFechaInicio()));
            sentencia.setDate(3, Date.valueOf(temporada.getFechaFinal()));
            sentencia.setBigDecimal(4, temporada.getPrecioPorNoche());
            return sentencia.executeUpdate() == 1;
        }
    }

    public boolean actualizar(Temporada temporada) throws SQLException {
        String sql = "UPDATE temporada SET id_temporada_tipo = ?, fecha_inicio = ?, fecha_final = ?, "
                + "precio_por_noche = ? WHERE id_temporada = ?";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, temporada.getIdTemporadaTipo());
            sentencia.setDate(2, Date.valueOf(temporada.getFechaInicio()));
            sentencia.setDate(3, Date.valueOf(temporada.getFechaFinal()));
            sentencia.setBigDecimal(4, temporada.getPrecioPorNoche());
            sentencia.setInt(5, temporada.getIdTemporada());
            return sentencia.executeUpdate() == 1;
        }
    }

    public boolean eliminar(int idTemporada) throws SQLException {
        String sql = "DELETE FROM temporada WHERE id_temporada = ?";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idTemporada);
            return sentencia.executeUpdate() == 1;
        }
    }

    public boolean tieneReservas(int idTemporada) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reserva WHERE id_temporada = ?";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idTemporada);
            ResultSet resultado = sentencia.executeQuery();
            resultado.next();
            return resultado.getInt(1) > 0;
        }
    }

    // Revisa que el período nuevo no se pise con otra temporada ya cargada
    public boolean seSuperpone(Temporada temporada) throws SQLException {
        String sql = "SELECT COUNT(*) FROM temporada WHERE fecha_inicio < ? AND fecha_final > ? AND id_temporada <> ?";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setDate(1, Date.valueOf(temporada.getFechaFinal()));
            sentencia.setDate(2, Date.valueOf(temporada.getFechaInicio()));
            sentencia.setInt(3, temporada.getIdTemporada());
            ResultSet resultado = sentencia.executeQuery();
            resultado.next();
            return resultado.getInt(1) > 0;
        }
    }

    // Devuelve la temporada en la que cae la fecha, o null si no hay ninguna
    public Temporada buscarPorFecha(LocalDate fecha) throws SQLException {
        String sql = "SELECT t.id_temporada, t.id_temporada_tipo, t.fecha_inicio, t.fecha_final, "
                + "t.precio_por_noche, tt.tipo FROM temporada t "
                + "JOIN temporada_tipo tt ON tt.id_temporada_tipo = t.id_temporada_tipo "
                + "WHERE t.fecha_inicio <= ? AND t.fecha_final > ?";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setDate(1, Date.valueOf(fecha));
            sentencia.setDate(2, Date.valueOf(fecha));
            ResultSet resultado = sentencia.executeQuery();
            if (resultado.next()) {
                return leerTemporada(resultado);
            }
            return null;
        }
    }

    private Temporada leerTemporada(ResultSet resultado) throws SQLException {
        return new Temporada(
                resultado.getInt("id_temporada"),
                resultado.getInt("id_temporada_tipo"),
                resultado.getDate("fecha_inicio").toLocalDate(),
                resultado.getDate("fecha_final").toLocalDate(),
                resultado.getBigDecimal("precio_por_noche"),
                resultado.getString("tipo"));
    }
}
