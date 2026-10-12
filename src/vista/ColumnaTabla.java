package vista;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;

import java.util.function.Function;

public final class ColumnaTabla {
    private ColumnaTabla() {
    }

    public static <T> TableColumn<T, String> crear(String titulo, Function<T, String> valor) {
        TableColumn<T, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(celda ->
                new SimpleStringProperty(valor.apply(celda.getValue())));
        columna.setPrefWidth(150);
        return columna;
    }
}
