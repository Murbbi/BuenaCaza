import controlador.AutenticacionController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import modelo.Invitado;
import vista.PantallaAdmin;
import vista.VentanaLogin;
import vista.VentanaRegistro;
import vista.VentanaReserva;

public class BuenaCazaApp extends Application {
    private final AutenticacionController autenticacionController = new AutenticacionController();
    private Stage escenario;

    @Override
    public void start(Stage escenario) {
        this.escenario = escenario;
        escenario.setTitle("Buena Caza | Reservas");
        escenario.setMinWidth(900);
        escenario.setMinHeight(650);
        mostrarLogin();
        escenario.show();
    }

    private void mostrarLogin() {
        escenario.setMaximized(false);
        VentanaLogin login = new VentanaLogin(escenario, autenticacionController,
                this::abrirSesion, this::mostrarRegistro);
        cambiarEscena(login.crearEscena());
    }

    private void mostrarRegistro() {
        VentanaRegistro registro = new VentanaRegistro(escenario, autenticacionController,
                this::abrirSesion, this::mostrarLogin);
        cambiarEscena(registro.crearEscena());
    }

    private void abrirSesion(Invitado invitado) {
        if (invitado.isEsAdmin()) {
            PantallaAdmin pantalla = new PantallaAdmin(escenario, invitado, this::mostrarLogin);
            cambiarEscena(pantalla.crearEscena());
            escenario.setMaximized(true);
            return;
        }
        VentanaReserva portal = new VentanaReserva(escenario, invitado, this::mostrarLogin);
        cambiarEscena(portal.crearEscena());
        escenario.setMaximized(true);
    }

    private void cambiarEscena(Scene escena) {
        escenario.setScene(escena);
        escenario.sizeToScene();
    }

    @Override
    public void stop() {
        escenario = null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}