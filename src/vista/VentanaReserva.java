package vista;

import controlador.ReservaController;
import controlador.ReservaController.Presupuesto;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modelo.Cabana;
import modelo.Invitado;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

public class VentanaReserva {
    private Stage stage;
    private Invitado invitado;
    private Runnable alCerrarSesion;
    private ReservaController reservaController = new ReservaController();

    private ComboBox<Cabana> cabanas = new ComboBox<>();
    private DatePicker entrada = new DatePicker(LocalDate.now().plusDays(1));
    private DatePicker salida = new DatePicker(LocalDate.now().plusDays(3));
    private TextArea comentario = new TextArea();
    private Label total = new Label("—");
    private Label avisoCotizacion = new Label();
    private Label errorCotizacion = new Label();
    private VBox panelError = new VBox(errorCotizacion);

    // Labels del resumen del presupuesto
    private Label resumenNoches = new Label();
    private Label resumenTarifa = new Label();
    private Label resumenDescuento = new Label();
    private Label resumenTotal = new Label();

    public VentanaReserva(Stage stage, Invitado invitado, Runnable alCerrarSesion) {
        this.stage = stage;
        this.invitado = invitado;
        this.alCerrarSesion = alCerrarSesion;
    }

    public Scene crearEscena() {
        BorderPane raiz = new BorderPane();
        raiz.getStyleClass().add("page-background");
        raiz.setTop(encabezado());
        raiz.setCenter(crearContenidoPanel());
        Scene escena = new Scene(raiz, 1240, 820);
        ComponentesUI.aplicarEstilos(escena);
        return escena;
    }

    // Lo usa también el panel del administrador ("Panel de invitado")
    public Node crearContenidoPanel() {
        Label contexto = new Label("TU ESTADÍA");
        contexto.getStyleClass().add("eyebrow");
        Label titulo = ComponentesUI.titulo("Reservá tu próxima escapada");
        Label descripcion = ComponentesUI.subtitulo(
                "Elegí las fechas y la cabaña. El valor se calcula automáticamente según la temporada.");

        VBox cuerpo = new VBox(22, contexto, titulo, descripcion, formularioReserva(), panelPresupuesto());
        cuerpo.setPadding(new Insets(32, 46, 40, 46));
        ScrollPane scroll = new ScrollPane(cuerpo);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("page-background");

        cargarCabanas();
        return scroll;
    }

    private HBox encabezado() {
        Label marca = new Label("Buena Caza");
        marca.getStyleClass().add("sidebar-brand");
        Label subtitulo = new Label("RESERVAS · PORTAL DEL INVITADO");
        subtitulo.getStyleClass().add("sidebar-caption");
        VBox identidad = new VBox(3, marca, subtitulo);

        Label bienvenida = new Label("Hola, " + invitado.getEmail());
        bienvenida.getStyleClass().add("page-subtitle");
        Button salir = ComponentesUI.boton("Cerrar sesión", "button-secondary", alCerrarSesion);
        salir.setMaxWidth(150);

        HBox barra = new HBox(24, identidad, bienvenida, salir);
        HBox.setHgrow(identidad, Priority.ALWAYS);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(16, 34, 16, 34));
        barra.setStyle("-fx-background-color: #104638;");
        return barra;
    }

    private VBox formularioReserva() {
        Label encabezado = new Label("Datos de la reserva");
        encabezado.getStyleClass().add("section-title");
        cabanas.setPromptText("Seleccioná una cabaña");
        comentario.setPromptText("Contanos si necesitás algo especial (opcional)");
        comentario.setPrefRowCount(2);

        GridPane campos = new GridPane();
        campos.setHgap(16);
        campos.setVgap(14);
        campos.add(ComponentesUI.campo("Cabaña", cabanas), 0, 0);
        campos.add(ComponentesUI.campo("Fecha de entrada", entrada), 1, 0);
        campos.add(ComponentesUI.campo("Fecha de salida", salida), 2, 0);
        campos.add(ComponentesUI.campo("Comentario", comentario), 0, 1, 3, 1);
        int[] anchos = {40, 30, 30};
        for (int ancho : anchos) {
            ColumnConstraints columna = new ColumnConstraints();
            columna.setPercentWidth(ancho);
            campos.getColumnConstraints().add(columna);
        }

        Label etiquetaTotal = new Label("TOTAL ESTIMADO");
        etiquetaTotal.getStyleClass().add("metric-label");
        total.getStyleClass().add("price-total");
        avisoCotizacion.getStyleClass().add("status-note");
        errorCotizacion.getStyleClass().add("quote-error-message");
        errorCotizacion.setWrapText(true);
        panelError.getStyleClass().add("quote-error-panel");

        VBox valor = new VBox(5, etiquetaTotal, total, avisoCotizacion, panelError);
        Button confirmar = ComponentesUI.boton("Confirmar reserva", "button-primary", this::confirmarReserva);
        confirmar.setMaxWidth(210);
        HBox pie = new HBox(20, valor, confirmar);
        pie.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(valor, Priority.ALWAYS);

        // Cada vez que cambia la cabaña o alguna fecha se recalcula el presupuesto
        cabanas.setOnAction(e -> actualizarCotizacion());
        entrada.valueProperty().addListener((obs, anterior, nueva) -> actualizarCotizacion());
        salida.valueProperty().addListener((obs, anterior, nueva) -> actualizarCotizacion());

        VBox panel = new VBox(18, encabezado, campos, pie);
        panel.getStyleClass().add("form-card");
        panel.setPadding(new Insets(24));
        return panel;
    }

    private VBox panelPresupuesto() {
        Label encabezado = new Label("RESUMEN DEL PRESUPUESTO AUTOMÁTICO");
        encabezado.getStyleClass().add("section-title");
        resumenTotal.getStyleClass().add("price-total");
        VBox panel = new VBox(10, encabezado, resumenNoches, resumenTarifa, resumenDescuento, resumenTotal);
        panel.setPadding(new Insets(22));
        panel.setStyle("-fx-background-color: #e4f5fc; -fx-border-color: #8bd8f4; "
                + "-fx-border-radius: 18px; -fx-background-radius: 18px;");
        return panel;
    }

    private void cargarCabanas() {
        try {
            cabanas.setItems(FXCollections.observableArrayList(reservaController.listarCabanas()));
        } catch (SQLException e) {
            ComponentesUI.mostrarError(stage, "Error", "No se pudieron cargar las cabañas.\n" + e.getMessage());
        }
        actualizarCotizacion();
    }

    private void actualizarCotizacion() {
        limpiarPresupuesto();
        Cabana cabana = cabanas.getValue();
        if (cabana == null || entrada.getValue() == null || salida.getValue() == null) {
            avisoCotizacion.setText("Seleccioná una cabaña y fechas para calcular el total.");
            return;
        }

        try {
            Presupuesto presupuesto = reservaController.cotizarPresupuesto(invitado,
                    cabana.getIdCabana(), entrada.getValue(), salida.getValue());

            total.setText(ComponentesUI.moneda(presupuesto.total()));
            resumenNoches.setText("* Noches de estadía: " + presupuesto.noches() + " noches");
            resumenTarifa.setText("* Tarifa Temporada " + presupuesto.temporada() + ": "
                    + ComponentesUI.moneda(presupuesto.precioPorNoche()) + " / noche = "
                    + ComponentesUI.moneda(presupuesto.subtotal()));
            if (presupuesto.aplicaDescuento()) {
                resumenDescuento.setText("* Descuento Fidelidad (10% Comunidad): "
                        + ComponentesUI.moneda(presupuesto.descuento()));
            } else {
                resumenDescuento.setText("* Descuento de fidelidad: no aplica (se aplica desde la próxima visita)");
            }
            resumenTotal.setText("COSTO TOTAL NETO: " + ComponentesUI.moneda(presupuesto.total()));
            avisoCotizacion.setText("Presupuesto calculado para " + presupuesto.noches() + " noches.");
        } catch (IllegalArgumentException e) {
            mostrarErrorCotizacion(e.getMessage());
        } catch (SQLException e) {
            mostrarErrorCotizacion("No se pudo calcular el precio: " + e.getMessage());
        }
    }

    private void limpiarPresupuesto() {
        total.setText("—");
        resumenNoches.setText("* Noches de estadía: —");
        resumenTarifa.setText("* Tarifa de temporada: —");
        resumenDescuento.setText("* Descuento de fidelidad: —");
        resumenTotal.setText("COSTO TOTAL NETO: —");
        panelError.setVisible(false);
        panelError.setManaged(false);
        avisoCotizacion.setVisible(true);
    }

    private void mostrarErrorCotizacion(String mensaje) {
        errorCotizacion.setText(mensaje);
        panelError.setVisible(true);
        panelError.setManaged(true);
        avisoCotizacion.setVisible(false);
    }

    private void confirmarReserva() {
        Cabana cabana = cabanas.getValue();
        if (cabana == null) {
            mostrarErrorCotizacion("Seleccioná una cabaña disponible.");
            return;
        }
        try {
            BigDecimal precio = reservaController.crearReserva(invitado, cabana.getIdCabana(),
                    entrada.getValue(), salida.getValue(), comentario.getText());
            ComponentesUI.mostrarInformacion(stage, "Reserva confirmada",
                    "Tu reserva en " + cabana.getNombre() + " quedó registrada.\nTotal: " + ComponentesUI.moneda(precio));
            comentario.clear();
            actualizarCotizacion();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            mostrarErrorCotizacion(exception.getMessage());
        } catch (SQLException exception) {
            mostrarErrorCotizacion("No se pudo guardar la reserva: " + exception.getMessage());
        }
    }
}
