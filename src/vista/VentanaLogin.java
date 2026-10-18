package vista;

import controlador.AutenticacionController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modelo.Invitado;

import java.sql.SQLException;
import java.util.function.Consumer;

public class VentanaLogin {
    private Stage stage;
    private AutenticacionController autenticacionController;
    private Consumer<Invitado> alIngresar;
    private Runnable alRegistrarse;

    private TextField email = new TextField();
    private PasswordField contrasena = new PasswordField();
    private Label estado = ComponentesUI.subtitulo("");

    public VentanaLogin(Stage stage, AutenticacionController autenticacionController,
            Consumer<Invitado> alIngresar, Runnable alRegistrarse) {
        this.stage = stage;
        this.autenticacionController = autenticacionController;
        this.alIngresar = alIngresar;
        this.alRegistrarse = alRegistrarse;
    }

    public Scene crearEscena() {
        // Parte izquierda con el nombre del complejo
        Label marca = new Label("Buena Caza");
        marca.getStyleClass().add("brand-title");
        Label descripcion = new Label("Un lugar para volver.\nUna estadía para recordar.");
        descripcion.getStyleClass().add("brand-caption");
        VBox arte = new VBox(18, marca, descripcion);
        arte.setAlignment(Pos.CENTER_LEFT);
        arte.setPadding(new Insets(70));
        arte.getStyleClass().add("login-art");
        arte.setPrefWidth(470);

        // Formulario de ingreso
        Label etiqueta = new Label("BIENVENIDO");
        etiqueta.getStyleClass().add("eyebrow");
        Label titulo = ComponentesUI.titulo("Iniciar sesión");
        Label subtitulo = ComponentesUI.subtitulo("Ingresá con tu correo y contraseña para continuar.");

        // Datos de prueba para no tener que escribirlos cada vez
        email.setText("matias@gmail.com");
        email.setPromptText("nombre@correo.com");
        contrasena.setText("1234567891011");
        contrasena.setPromptText("Tu contraseña");

        Button ingresar = ComponentesUI.boton("Ingresar", "button-primary", this::ingresar);
        Button registrarse = ComponentesUI.boton("Crear una cuenta", "button-link", alRegistrarse);

        VBox formulario = new VBox(18, etiqueta, titulo, subtitulo,
                ComponentesUI.campo("Correo electrónico", email),
                ComponentesUI.campo("Contraseña", contrasena),
                ingresar, estado, registrarse);
        formulario.getStyleClass().add("login-card");
        formulario.setPadding(new Insets(42));
        formulario.setMaxWidth(480);

        VBox panel = new VBox(formulario);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(36, 72, 36, 72));
        HBox raiz = new HBox(arte, panel);
        raiz.getStyleClass().add("page-background");
        HBox.setHgrow(panel, Priority.ALWAYS);

        Scene escena = new Scene(raiz, 1120, 720);
        ComponentesUI.aplicarEstilos(escena);
        return escena;
    }

    private void ingresar() {
        try {
            Invitado invitado = autenticacionController.iniciarSesion(email.getText(),
                    contrasena.getText().toCharArray());
            if (invitado == null) {
                estado.setText("El correo o la contraseña no son correctos.");
                return;
            }
            alIngresar.accept(invitado);
        } catch (SQLException e) {
            ComponentesUI.mostrarError(stage, "No se pudo iniciar sesión",
                    "No hay conexión con la base de datos.\n" + e.getMessage());
        }
    }
}
