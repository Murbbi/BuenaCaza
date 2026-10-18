package vista;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Métodos que se repiten en varias pantallas (estilos, botones, mensajes y formatos)
public class ComponentesUI {

    public static void aplicarEstilos(Scene escena) {
        escena.getStylesheets().add(ComponentesUI.class.getResource("buenacaza.css").toExternalForm());
    }

    public static void aplicarIcono(Stage ventana) {
        ventana.getIcons().add(new Image(ComponentesUI.class.getResource("icono-32.png").toExternalForm()));
        ventana.getIcons().add(new Image(ComponentesUI.class.getResource("icono.png").toExternalForm()));
    }

    public static void aplicarIcono(Dialog<?> dialogo) {
        aplicarIcono((Stage) dialogo.getDialogPane().getScene().getWindow());
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

    // Arma una etiqueta con el control abajo (por ejemplo "Correo electrónico" + el TextField)
    public static VBox campo(String nombre, Control control) {
        Label etiqueta = new Label(nombre);
        etiqueta.getStyleClass().add("field-label");
        control.getStyleClass().add("form-control");
        control.setMaxWidth(Double.MAX_VALUE);
        return new VBox(7, etiqueta, control);
    }

    public static Button boton(String texto, String clase, Runnable accion) {
        Button boton = new Button(texto);
        boton.getStyleClass().add(clase);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setOnAction(evento -> accion.run());
        return boton;
    }

    public static void prepararCampo(TextField campo) {
        campo.getStyleClass().add("form-control");
        campo.setMaxWidth(Double.MAX_VALUE);
    }

    public static void mostrarError(Window ventana, String titulo, String mensaje) {
        mostrarAlerta(Alert.AlertType.ERROR, ventana, titulo, mensaje);
    }

    public static void mostrarInformacion(Window ventana, String titulo, String mensaje) {
        mostrarAlerta(Alert.AlertType.INFORMATION, ventana, titulo, mensaje);
    }

    public static boolean confirmar(Window ventana, String titulo, String pregunta) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(pregunta);
        alerta.initOwner(ventana);
        aplicarIcono(alerta);
        return alerta.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK;
    }

    private static void mostrarAlerta(Alert.AlertType tipo, Window ventana, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.initOwner(ventana);
        aplicarIcono(alerta);
        alerta.showAndWait();
    }

    // Muestra los montos como "$ 85.000"
    public static String moneda(BigDecimal valor) {
        if (valor == null) {
            return "—";
        }
        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        formato.setMaximumFractionDigits(0);
        return formato.format(valor);
    }

    public static String fecha(LocalDate fecha) {
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public static String texto(String valor) {
        if (valor == null || valor.isBlank()) {
            return "—";
        }
        return valor;
    }
}
