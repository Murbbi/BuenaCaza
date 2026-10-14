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
        mostrarLogin();
        escenario.show();
    }

    private void mostrarLogin() {
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
            mostrarAdmin(invitado);
            return;
        }
        VentanaReserva portal = new VentanaReserva(escenario, invitado, this::mostrarLogin);
        cambiarEscena(portal.crearEscena());
    }

    private void mostrarAdmin(Invitado invitado) {
        PantallaAdmin pantalla = new PantallaAdmin(escenario, invitado, this::mostrarLogin);
        cambiarEscena(pantalla.crearEscena());
    }

    private void cambiarEscena(Scene escena) {
        boolean maximizada = escenario.isMaximized();
        double ancho = escenario.getWidth();
        double alto = escenario.getHeight();
        double x = escenario.getX();
        double y = escenario.getY();
        escenario.setScene(escena);
        if (maximizada) {
            escenario.setMaximized(true);
        } else {
            escenario.setX(x);
            escenario.setY(y);
            escenario.setWidth(ancho);
            escenario.setHeight(alto);
        }
    }

    @Override
    public void stop() {
        escenario = null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}