package vista;

import controlador.AutenticacionController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modelo.Invitado;

import java.sql.SQLException;
import java.util.function.Consumer;

public class VentanaRegistro {
    private Stage stage;
    private AutenticacionController autenticacionController;
    private Consumer<Invitado> alRegistrarse;
    private Runnable alVolver;

    private TextField nombre = new TextField();
    private TextField dni = new TextField();
    private TextField email = new TextField();
    private TextField telefono = new TextField();
    private TextField direccion = new TextField();
    private PasswordField contrasena = new PasswordField();
    private PasswordField confirmar = new PasswordField();
    private Label estado = ComponentesUI.subtitulo("");

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

        nombre.setPromptText("Nombre y apellido");
        dni.setPromptText("DNI");
        email.setPromptText("nombre@correo.com");
        telefono.setPromptText("Teléfono (opcional)");
        direccion.setPromptText("Calle y número");
        contrasena.setPromptText("Mínimo 8 caracteres");
        confirmar.setPromptText("Repetí tu contraseña");

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
        // Las dos columnas ocupan la mitad cada una
        for (int i = 0; i < 2; i++) {
            ColumnConstraints columna = new ColumnConstraints();
            columna.setPercentWidth(50);
            campos.getColumnConstraints().add(columna);
        }

        Button crearCuenta = ComponentesUI.boton("Crear cuenta", "button-primary", this::registrar);
        Button volver = ComponentesUI.boton("Ya tengo una cuenta", "button-link", alVolver);
        VBox tarjeta = new VBox(20, marca, contexto, titulo, subtitulo, campos, crearCuenta, estado, volver);
        tarjeta.setPadding(new Insets(34));
        tarjeta.setMaxWidth(700);
        tarjeta.getStyleClass().add("login-card");

        VBox centro = new VBox(tarjeta);
        centro.setAlignment(Pos.CENTER);
        centro.setPadding(new Insets(30));
        centro.getStyleClass().add("page-background");

        Scene escena = new Scene(centro, 1060, 760);
        ComponentesUI.aplicarEstilos(escena);
        return escena;
    }

    private void registrar() {
        if (nombre.getText().isBlank() || dni.getText().isBlank() || email.getText().isBlank()
                || direccion.getText().isBlank()) {
            estado.setText("Completá nombre, DNI, correo y dirección.");
            return;
        }
        if (!email.getText().contains("@")) {
            estado.setText("Ingresá un correo electrónico válido.");
            return;
        }
        if (!contrasena.getText().equals(confirmar.getText())) {
            estado.setText("Las contraseñas no coinciden.");
            return;
        }

        try {
            Invitado invitado = new Invitado();
            invitado.setDni(Integer.parseInt(dni.getText().trim()));
            invitado.setNombre(nombre.getText().trim());
            invitado.setEmail(email.getText().trim());
            invitado.setTelefono(telefono.getText().trim());
            invitado.setDireccion(direccion.getText().trim());

            Invitado registrado = autenticacionController.registrar(invitado, contrasena.getText().toCharArray());
            alRegistrarse.accept(registrado);
        } catch (NumberFormatException e) {
            estado.setText("El DNI debe tener solo números.");
        } catch (IllegalArgumentException e) {
            estado.setText(e.getMessage());
        } catch (SQLException e) {
            ComponentesUI.mostrarError(stage, "No se pudo crear la cuenta",
                    "Revisá que el DNI no esté registrado.\n" + e.getMessage());
        }
    }
}
