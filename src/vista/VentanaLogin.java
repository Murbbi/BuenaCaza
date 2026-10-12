package vista;

import controlador.AutenticacionController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modelo.Invitado;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Consumer;

public class VentanaLogin {
    private final Stage stage;
    private final AutenticacionController autenticacionController;
    private final Consumer<Invitado> alIngresar;
    private final Runnable alRegistrarse;

    public VentanaLogin(Stage stage, AutenticacionController autenticacionController,
            Consumer<Invitado> alIngresar, Runnable alRegistrarse) {
        this.stage = stage;
        this.autenticacionController = autenticacionController;
        this.alIngresar = alIngresar;
        this.alRegistrarse = alRegistrarse;
    }

    public Scene crearEscena() {
        Label marca = new Label("Buena Caza");
        marca.getStyleClass().add("brand-title");
        Label descripcion = new Label("Un lugar para volver.\nUna estadía para recordar.");
        descripcion.getStyleClass().add("brand-caption");
        descripcion.setWrapText(true);
        VBox arte = new VBox(18, marca, descripcion);
        arte.setAlignment(Pos.CENTER_LEFT);
        arte.setPadding(new Insets(70));
        arte.getStyleClass().add("login-art");
        arte.setPrefWidth(470);

        Label etiqueta = new Label("BIENVENIDO");
        etiqueta.getStyleClass().add("eyebrow");
        Label titulo = ComponentesUI.titulo("Iniciar sesión");
        Label subtitulo = ComponentesUI.subtitulo("Ingresá con tu correo y contraseña para continuar.");
        TextField email = new TextField();
        email.setPromptText("nombre@correo.com");
        PasswordField contrasena = new PasswordField();
        contrasena.setPromptText("Tu contraseña");
        Label estado = ComponentesUI.subtitulo("");
        estado.setWrapText(true);
        var ingresar = ComponentesUI.boton("Ingresar", "button-primary", () -> ingresar(email, contrasena, estado));
        var registrarse = ComponentesUI.boton("Crear una cuenta", "button-link", alRegistrarse);

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

    private void ingresar(TextField email, PasswordField contrasena, Label estado) {
        char[] clave = contrasena.getText().toCharArray();
        try {
            Invitado invitado = autenticacionController.iniciarSesion(email.getText(), clave);
            if (invitado == null) {
                estado.setText("El correo o la contraseña no son correctos.");
                return;
            }
            estado.setText("");
            alIngresar.accept(invitado);
        } catch (SQLException exception) {
            ComponentesUI.mostrarError(stage, "No se pudo iniciar sesión",
                    "No se pudo consultar la cuenta. Verificá la conexión y el esquema de MySQL.\n\n"
                            + exception.getMessage());
        } finally {
            Arrays.fill(clave, '\0');
            contrasena.clear();
        }
    }
}