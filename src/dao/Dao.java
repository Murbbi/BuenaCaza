package dao;

import java.sql.SQLException;
import java.util.List;

// Define las operaciones básicas que ofrece cualquier acceso a datos,
// sin indicar cómo se resuelven. Cada DAO las implementa con su propio SQL.
public interface Dao<T> {
    List<T> listar() throws SQLException;

    boolean insertar(T entidad) throws SQLException;
}
