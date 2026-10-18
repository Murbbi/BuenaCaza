package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    // Si no se definen las variables de entorno se usan estos valores
    private static final String URL = System.getenv().getOrDefault(
            "BUENACAZA_DB_URL", "jdbc:mysql://192.168.1.41:3306/buena_caza");
    private static final String USUARIO = System.getenv().getOrDefault("BUENACAZA_DB_USER", "buena_caza_app");
    private static final String CONTRASENA = System.getenv().getOrDefault("BUENACAZA_DB_PASSWORD", "oso123456");

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}
