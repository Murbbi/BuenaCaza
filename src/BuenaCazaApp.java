import controlador.AutenticacionController;
import javafx.application.Application;
import javafx.stage.Stage;
import modelo.Invitado;
import vista.ComponentesUI;
import vista.PantallaAdmin;
import vista.VentanaLogin;
import vista.VentanaRegistro;
import vista.VentanaReserva;

// Clase principal: arranca la aplicación y cambia entre las pantallas
public class BuenaCazaApp extends Application {
    private AutenticacionController autenticacionController = new AutenticacionController();
    private Stage escenario;

    @Override
    public void start(Stage escenario) {
        this.escenario = escenario;
        escenario.setTitle("Buena Caza | Reservas");
        ComponentesUI.aplicarIcono(escenario);
        mostrarLogin();
        escenario.show();
    }

    private void mostrarLogin() {
        VentanaLogin login = new VentanaLogin(escenario, autenticacionController,
                this::abrirSesion, this::mostrarRegistro);
        escenario.setScene(login.crearEscena());
    }

    private void mostrarRegistro() {
        VentanaRegistro registro = new VentanaRegistro(escenario, autenticacionController,
                this::abrirSesion, this::mostrarLogin);
        escenario.setScene(registro.crearEscena());
    }

    // Después del login se abre el panel que corresponde según el tipo de usuario
    private void abrirSesion(Invitado invitado) {
        if (invitado.puedeAdministrar()) {
            mostrarAdmin(invitado);
            return;
        }
        VentanaReserva portal = new VentanaReserva(escenario, invitado, this::mostrarLogin);
        escenario.setScene(portal.crearEscena());
    }

    private void mostrarAdmin(Invitado invitado) {
        PantallaAdmin pantalla = new PantallaAdmin(escenario, invitado, this::mostrarLogin);
        escenario.setScene(pantalla.crearEscena());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
