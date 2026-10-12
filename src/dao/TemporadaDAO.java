package dao;

import conexion.Conexion;
import modelo.Temporada;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TemporadaDAO {
    public List<Temporada> listar() throws SQLException {
        String sql = "SELECT id_temporada, id_temporada_tipo, fecha_inicio, fecha_final, precio_por_noche FROM temporada ORDER BY id_temporada";
        List<Temporada> temporadas = new ArrayList<>();

        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                temporadas.add(new Temporada(
                        resultado.getInt("id_temporada"),
                        resultado.getInt("id_temporada_tipo"),
                        LocalDate.parse(resultado.getString("fecha_inicio")),
                        LocalDate.parse(resultado.getString("fecha_final")),
                        resultado.getBigDecimal("precio_por_noche")
                ));
            }
        }

        return temporadas;
    }

    public boolean insertar(Temporada temporada) throws SQLException {
        String sql = "INSERT INTO temporada (id_temporada_tipo, fecha_inicio, fecha_final, precio_por_noche) VALUES (?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, temporada.getIdTemporadaTipo());
            sentencia.setDate(2, java.sql.Date.valueOf(temporada.getFechaInicio()));
            sentencia.setDate(3, java.sql.Date.valueOf(temporada.getFechaFinal()));
            sentencia.setBigDecimal(4, temporada.getPrecioPorNoche());
            return sentencia.executeUpdate() == 1;
        }
    }

    public Temporada buscarPorFecha(LocalDate fecha) throws SQLException {
        String sql = "SELECT id_temporada, id_temporada_tipo, fecha_inicio, fecha_final, precio_por_noche "
                + "FROM temporada WHERE fecha_inicio <= ? AND fecha_final > ? "
                + "ORDER BY fecha_inicio LIMIT 1";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setDate(1, java.sql.Date.valueOf(fecha));
            sentencia.setDate(2, java.sql.Date.valueOf(fecha));
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return null;
                }
                return new Temporada(resultado.getInt("id_temporada"),
                        resultado.getInt("id_temporada_tipo"),
                        LocalDate.parse(resultado.getString("fecha_inicio")),
                        LocalDate.parse(resultado.getString("fecha_final")),
                        resultado.getBigDecimal("precio_por_noche"));
            }
        }
    }

    public boolean actualizarPrecio(int idTemporada, BigDecimal precio) throws SQLException {
        String sql = "UPDATE temporada SET precio_por_noche = ? WHERE id_temporada = ?";
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setBigDecimal(1, precio);
            sentencia.setInt(2, idTemporada);
            if (sentencia.executeUpdate() == 1) {
                return true;
            }
        }
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(
                        "SELECT 1 FROM temporada WHERE id_temporada = ?")) {
            sentencia.setInt(1, idTemporada);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next();
            }
        }
    }

    public List<Temporada> listarConTipo() throws SQLException {
        String sql = "SELECT t.id_temporada, t.id_temporada_tipo, t.fecha_inicio, t.fecha_final, "
                + "t.precio_por_noche, tt.tipo FROM temporada t "
                + "JOIN temporada_tipo tt ON tt.id_temporada_tipo = t.id_temporada_tipo "
                + "ORDER BY t.fecha_inicio";
        List<Temporada> temporadas = new ArrayList<>();
        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                temporadas.add(new Temporada(resultado.getInt("id_temporada"),
                        resultado.getInt("id_temporada_tipo"),
                        LocalDate.parse(resultado.getString("fecha_inicio")),
                        LocalDate.parse(resultado.getString("fecha_final")),
                        resultado.getBigDecimal("precio_por_noche"), resultado.getString("tipo")));
            }
        }
        return temporadas;
    }
}
