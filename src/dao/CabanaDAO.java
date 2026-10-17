package dao;

import conexion.Conexion;
import modelo.Cabana;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CabanaDAO implements Dao<Cabana> {
    @Override
    public List<Cabana> listar() throws SQLException {
        String sql = "SELECT id_cabana, nombre, capacidad, estado FROM cabana ORDER BY id_cabana";
        List<Cabana> cabanas = new ArrayList<>();

        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                cabanas.add(new Cabana(
                        resultado.getInt("id_cabana"),
                        resultado.getString("nombre"),
                        resultado.getInt("capacidad"),
                        resultado.getString("estado")));
            }
        }

        return cabanas;
    }

    @Override
    public boolean insertar(Cabana cabana) throws SQLException {
        String sql = "INSERT INTO cabana (nombre, capacidad, estado) VALUES (?, ?, ?)";

        try (Connection conexion = Conexion.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, cabana.getNombre());
            sentencia.setInt(2, cabana.getCapacidad());
            sentencia.setString(3, cabana.getEstado());
            return sentencia.executeUpdate() == 1;
        }
    }
}