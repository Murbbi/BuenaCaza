package vista;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.net.URL;

public final class ComponentesUI {
    private ComponentesUI() {
    }

    public static void aplicarEstilos(Scene escena) {
        URL hoja = ComponentesUI.class.getResource("buenacaza.css");
        if (hoja == null) {
            throw new IllegalStateException("No se encontró la hoja de estilos vista/buenacaza.css.");
        }
        escena.getStylesheets().add(hoja.toExternalForm());
    }

    public static Label titulo(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("page-title");
        return etiqueta;
    }

    public static Label subtitulo(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("page-subtitle");
        etiqueta.setWrapText(true);
        return etiqueta;
    }

    public static VBox campo(String nombre, Control control) {
        Label etiqueta = new Label(nombre);
        etiqueta.getStyleClass().add("field-label");
        control.getStyleClass().add("form-control");
        control.setMaxWidth(Double.MAX_VALUE);
        VBox contenedor = new VBox(7, etiqueta, control);
        VBox.setVgrow(control, Priority.NEVER);
        return contenedor;
    }

    public static Button boton(String texto, String clase, Runnable accion) {
        Button boton = new Button(texto);
        boton.getStyleClass().add(clase);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setOnAction(evento -> accion.run());
        return boton;
    }

    public static VBox tarjeta(String etiqueta, String valor, String detalle) {
        Label nombre = new Label(etiqueta);
        nombre.getStyleClass().add("metric-label");
        Label numero = new Label(valor);
        numero.getStyleClass().add("metric-value");
        Label aclaracion = new Label(detalle);
        aclaracion.getStyleClass().add("metric-detail");
        aclaracion.setWrapText(true);
        VBox tarjeta = new VBox(8, nombre, numero, aclaracion);
        tarjeta.getStyleClass().add("metric-card");
        tarjeta.setPadding(new Insets(18));
        return tarjeta;
    }

    public static void mostrarError(Window propietario, String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.initOwner(propietario);
        alerta.showAndWait();
    }

    public static void mostrarInformacion(Window propietario, String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.initOwner(propietario);
        alerta.showAndWait();
    }

    public static void prepararCampo(TextField campo) {
        campo.getStyleClass().add("form-control");
        campo.setMaxWidth(Double.MAX_VALUE);
    }

    public static void prepararContrasena(PasswordField campo) {
        campo.getStyleClass().add("form-control");
        campo.setMaxWidth(Double.MAX_VALUE);
    }
}
