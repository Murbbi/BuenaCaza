package vista;

import controlador.AutenticacionController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modelo.Invitado;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Consumer;

public class VentanaRegistro {
    private final Stage stage;
    private final AutenticacionController autenticacionController;
    private final Consumer<Invitado> alRegistrarse;
    private final Runnable alVolver;

    public VentanaRegistro(Stage stage, AutenticacionController autenticacionController,
            Consumer<Invitado> alRegistrarse, Runnable alVolver) {
        this.stage = stage;
        this.autenticacionController = autenticacionController;
        this.alRegistrarse = alRegistrarse;
        this.alVolver = alVolver;
    }

    public Scene crearEscena() {
        Label marca = new Label("Buena Caza");
        marca.getStyleClass().add("registration-brand");
        Label contexto = new Label("REGISTRO DE INVITADOS");
        contexto.getStyleClass().add("eyebrow");
        Label titulo = ComponentesUI.titulo("Creá tu cuenta");
        Label subtitulo = ComponentesUI.subtitulo(
                "Completá tus datos para consultar disponibilidad y reservar tu cabaña.");

        TextField nombre = campoTexto("Nombre y apellido");
        TextField dni = campoTexto("DNI");
        TextField email = campoTexto("nombre@correo.com");
        TextField telefono = campoTexto("Teléfono (opcional)");
        TextField direccion = campoTexto("Calle y número");
        PasswordField contrasena = new PasswordField();
        contrasena.setPromptText("Mínimo 12 caracteres");
        PasswordField confirmar = new PasswordField();
        confirmar.setPromptText("Repetí tu contraseña");
        Label estado = ComponentesUI.subtitulo("");

        GridPane campos = new GridPane();
        campos.setHgap(16);
        campos.setVgap(16);
        campos.add(ComponentesUI.campo("Nombre completo", nombre), 0, 0);
        campos.add(ComponentesUI.campo("DNI", dni), 1, 0);
        campos.add(ComponentesUI.campo("Correo electrónico", email), 0, 1);
        campos.add(ComponentesUI.campo("Teléfono", telefono), 1, 1);
        campos.add(ComponentesUI.campo("Dirección", direccion), 0, 2, 2, 1);
        campos.add(ComponentesUI.campo("Contraseña", contrasena), 0, 3);
        campos.add(ComponentesUI.campo("Confirmar contraseña", confirmar), 1, 3);
        for (int columna = 0; columna < 2; columna++) {
            ColumnConstraintsHelper.expandir(campos, columna);
        }

        var crearCuenta = ComponentesUI.boton("Crear cuenta", "button-primary",
                () -> registrar(nombre, dni, email, telefono, direccion, contrasena, confirmar, estado));
        var volver = ComponentesUI.boton("Ya tengo una cuenta", "button-link", alVolver);
        VBox tarjeta = new VBox(20, marca, contexto, titulo, subtitulo, campos, crearCuenta, estado, volver);
        tarjeta.setPadding(new Insets(34));
        tarjeta.setMaxWidth(700);
        tarjeta.getStyleClass().add("login-card");

        VBox centro = new VBox(tarjeta);
        centro.setAlignment(Pos.CENTER);
        centro.setPadding(new Insets(30));
        centro.getStyleClass().add("page-background");
        VBox.setVgrow(tarjeta, Priority.NEVER);
        Scene escena = new Scene(centro, 1470, 880);
        ComponentesUI.aplicarEstilos(escena);
        return escena;
    }

    private TextField campoTexto(String texto) {
        TextField campo = new TextField();
        campo.setPromptText(texto);
        return campo;
    }

    private void registrar(TextField nombre, TextField dni, TextField email, TextField telefono,
            TextField direccion, PasswordField contrasena, PasswordField confirmar, Label estado) {
        char[] clave = contrasena.getText().toCharArray();
        char[] confirmacion = confirmar.getText().toCharArray();
        try {
            if (nombre.getText().isBlank() || dni.getText().isBlank() || email.getText().isBlank()
                    || direccion.getText().isBlank()) {
                estado.setText("Completá nombre, DNI, correo y dirección.");
                return;
            }
            if (!email.getText().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                estado.setText("Ingresá un correo electrónico válido.");
                return;
            }
            int numeroDni;
            try {
                numeroDni = Integer.parseInt(dni.getText().trim());
            } catch (NumberFormatException exception) {
                estado.setText("El DNI debe contener solo números.");
                return;
            }
            if (numeroDni < 1) {
                estado.setText("Ingresá un DNI válido.");
                return;
            }
            if (!Arrays.equals(clave, confirmacion)) {
                estado.setText("Las contraseñas no coinciden.");
                return;
            }

            Invitado invitado = new Invitado();
            invitado.setDni(numeroDni);
            invitado.setNombre(nombre.getText().trim());
            invitado.setEmail(email.getText().trim());
            invitado.setTelefono(telefono.getText().isBlank() ? null : telefono.getText().trim());
            invitado.setDireccion(direccion.getText().trim());
            Invitado invitadoRegistrado = autenticacionController.registrar(invitado, clave);
            alRegistrarse.accept(invitadoRegistrado);
        } catch (IllegalArgumentException exception) {
            estado.setText(exception.getMessage());
        } catch (SQLException exception) {
            ComponentesUI.mostrarError(stage, "No se pudo crear la cuenta",
                    "Revisá que el correo y el DNI no estén registrados y que la base esté actualizada.\n\n"
                            + exception.getMessage());
        } finally {
            Arrays.fill(clave, '\0');
            Arrays.fill(confirmacion, '\0');
            contrasena.clear();
            confirmar.clear();
        }
    }

    private static final class ColumnConstraintsHelper {
        private static void expandir(GridPane grilla, int columna) {
            javafx.scene.layout.ColumnConstraints restricciones = new javafx.scene.layout.ColumnConstraints();
            restricciones.setHgrow(Priority.ALWAYS);
            restricciones.setPercentWidth(50);
            grilla.getColumnConstraints().add(restricciones);
        }
    }
}
