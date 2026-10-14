package vista;

import controlador.ReservaController;
import controlador.ReservaController.Presupuesto;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modelo.Cabana;
import modelo.Invitado;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class VentanaReserva {
    private final Stage stage;
    private final Invitado invitado;
    private final ReservaController reservaController;
    private final Runnable alCerrarSesion;
    private final ComboBox<Cabana> cabanas = new ComboBox<>();
    private final DatePicker entrada = new DatePicker(LocalDate.now().plusDays(1));
    private final DatePicker salida = new DatePicker(LocalDate.now().plusDays(3));
    private final TextArea comentario = new TextArea();
    private final Label total = new Label("—");
    private final Label avisoCotizacion = new Label("Seleccioná una cabaña y fechas para calcular el total.");
    private final Label errorCotizacion = new Label();
    private final Label resumenNoches = new Label("Noches de estadía: —");
    private final Label resumenTarifa = new Label("Tarifa de temporada: —");
    private final Label resumenDescuento = new Label("Descuento de fidelidad: —");
    private final Label resumenTotal = new Label("COSTO TOTAL NETO: —");

    public VentanaReserva(Stage stage, Invitado invitado, Runnable alCerrarSesion) {
        this(stage, invitado, new ReservaController(), alCerrarSesion);
    }

    public VentanaReserva(Stage stage, Invitado invitado, ReservaController reservaController,
            Runnable alCerrarSesion) {
        this.stage = stage;
        this.invitado = invitado;
        this.reservaController = reservaController;
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

    public Node crearContenidoPanel() {
        Node panel = contenido();
        cargarDatos();
        return panel;
    }

    private HBox encabezado() {
        Label marca = new Label("Buena Caza");
        marca.getStyleClass().add("sidebar-brand");
        Label subtitulo = new Label("RESERVAS · PORTAL DEL INVITADO");
        subtitulo.getStyleClass().add("sidebar-caption");
        VBox identidad = new VBox(3, marca, subtitulo);
        Label bienvenida = new Label("Hola, " + invitado.getEmail());
        bienvenida.getStyleClass().add("page-subtitle");
        var salir = ComponentesUI.boton("Cerrar sesión", "button-secondary", alCerrarSesion);
        salir.setMaxWidth(150);
        HBox derecha = new HBox(20, bienvenida, salir);
        derecha.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(identidad, Priority.ALWAYS);
        HBox barra = new HBox(24, identidad, derecha);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(16, 34, 16, 34));
        barra.setStyle("-fx-background-color: #104638;");
        return barra;
    }

    private VBox contenido() {
        Label contexto = new Label("TU ESTADÍA");
        contexto.getStyleClass().add("eyebrow");
        Label titulo = ComponentesUI.titulo("Reservá tu próxima escapada");
        Label descripcion = ComponentesUI.subtitulo(
                "Elegí las fechas y la cabaña. El valor se calcula automáticamente según la temporada.");

        VBox panelReserva = formularioReserva();
        VBox panelPresupuesto = panelPresupuesto();

        VBox cuerpo = new VBox(22, contexto, titulo, descripcion, panelReserva, panelPresupuesto);
        cuerpo.setPadding(new Insets(32, 46, 40, 46));
        ScrollPane scroll = new ScrollPane(cuerpo);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("page-background");
        return new VBox(scroll);
    }

    private VBox formularioReserva() {
        Label encabezado = new Label("Datos de la reserva");
        encabezado.getStyleClass().add("section-title");
        cabanas.setPromptText("Seleccioná una cabaña");
        entrada.setMaxWidth(Double.MAX_VALUE);
        salida.setMaxWidth(Double.MAX_VALUE);
        comentario.setPromptText("Contanos si necesitás algo especial (opcional)");
        comentario.setPrefRowCount(2);

        GridPane campos = new GridPane();
        campos.setHgap(16);
        campos.setVgap(14);
        campos.add(ComponentesUI.campo("Cabaña", cabanas), 0, 0);
        campos.add(ComponentesUI.campo("Fecha de entrada", entrada), 1, 0);
        campos.add(ComponentesUI.campo("Fecha de salida", salida), 2, 0);
        campos.add(ComponentesUI.campo("Comentario", comentario), 0, 1, 3, 1);
        for (int i = 0; i < 3; i++) {
            javafx.scene.layout.ColumnConstraints ancho = new javafx.scene.layout.ColumnConstraints();
            ancho.setPercentWidth(i == 0 ? 40 : 30);
            ancho.setHgrow(Priority.ALWAYS);
            campos.getColumnConstraints().add(ancho);
        }

        Label etiquetaTotal = new Label("TOTAL ESTIMADO");
        etiquetaTotal.getStyleClass().add("metric-label");
        total.getStyleClass().add("price-total");
        avisoCotizacion.getStyleClass().add("status-note");
        avisoCotizacion.setWrapText(true);
        errorCotizacion.getStyleClass().add("quote-error-message");
        errorCotizacion.setWrapText(true);
        VBox panelError = new VBox(errorCotizacion);
        panelError.getStyleClass().add("quote-error-panel");
        panelError.setVisible(false);
        panelError.setManaged(false);
        VBox valor = new VBox(5, etiquetaTotal, total, avisoCotizacion, panelError);
        var confirmar = ComponentesUI.boton("Confirmar reserva", "button-primary", this::confirmarReserva);
        confirmar.setMaxWidth(210);
        HBox pie = new HBox(20, valor, confirmar);
        pie.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(valor, Priority.ALWAYS);

        cabanas.valueProperty().addListener((observable, anterior, nuevo) -> actualizarCotizacion());
        entrada.valueProperty().addListener((observable, anterior, nuevo) -> actualizarCotizacion());
        salida.valueProperty().addListener((observable, anterior, nuevo) -> actualizarCotizacion());
        VBox panel = new VBox(18, encabezado, campos, pie);
        panel.getStyleClass().add("form-card");
        panel.setPadding(new Insets(24));
        return panel;
    }

    private VBox panelPresupuesto() {
        Label encabezado = new Label("RESUMEN DEL PRESUPUESTO AUTOMÁTICO");
        encabezado.getStyleClass().add("section-title");
        resumenTotal.getStyleClass().add("price-total");
        VBox panel = new VBox(10, encabezado, resumenNoches, resumenTarifa,
                resumenDescuento, resumenTotal);
        panel.getStyleClass().add("content-card");
        panel.setPadding(new Insets(22));
        panel.setStyle("-fx-background-color: #e4f5fc; -fx-border-color: #8bd8f4; "
                + "-fx-border-radius: 18px; -fx-background-radius: 18px;");
        return panel;
    }

    private void cargarDatos() {
        try {
            cabanas.setItems(FXCollections.observableArrayList(reservaController.listarCabanas()));
            actualizarCotizacion();
        } catch (SQLException exception) {
            ComponentesUI.mostrarError(stage, "No se pudieron cargar las reservas",
                    "Verificá que MySQL esté disponible y que la base esté actualizada.\n\n"
                            + exception.getMessage());
        }
    }

    private void actualizarCotizacion() {
        limpiarErrorCotizacion();
        Cabana cabana = cabanas.getValue();
        if (cabana == null || entrada.getValue() == null || salida.getValue() == null) {
            total.setText("—");
            avisoCotizacion.setText("Seleccioná una cabaña y fechas para calcular el total.");
            limpiarResumenPresupuesto();
            return;
        }
        try {
            Presupuesto presupuesto = reservaController.cotizarPresupuesto(invitado,
                    cabana.getIdCabana(), entrada.getValue(), salida.getValue());
            total.setText(moneda(presupuesto.total()));
            resumenNoches.setText("* Noches de estadía: " + presupuesto.noches() + " noches");
            resumenTarifa.setText("* Tarifa Temporada " + textoTemporada(presupuesto.temporada())
                    + ": " + moneda(presupuesto.precioPorNoche()) + " / noche = "
                    + moneda(presupuesto.subtotal()));
            resumenDescuento.setText(presupuesto.aplicaDescuento()
                    ? "* Descuento Fidelidad (10% Comunidad): " + moneda(presupuesto.descuento())
                    : "* Descuento Fidelidad: " + moneda(presupuesto.descuento()));
            resumenTotal.setText("COSTO TOTAL NETO: " + moneda(presupuesto.total()));
            avisoCotizacion.setText("Presupuesto calculado para " + presupuesto.noches() + " noches.");
        } catch (IllegalArgumentException exception) {
            total.setText("—");
            mostrarErrorCotizacion(exception.getMessage());
            limpiarResumenPresupuesto();
        } catch (IllegalStateException exception) {
            total.setText("—");
            mostrarErrorCotizacion(exception.getMessage());
            limpiarResumenPresupuesto();
        } catch (SQLException exception) {
            total.setText("—");
            String error = "No se pudo calcular el precio: " + exception.getMessage();
            mostrarErrorCotizacion(error);
            limpiarResumenPresupuesto();
        }
    }

    private void limpiarResumenPresupuesto() {
        resumenNoches.setText("* Noches de estadía: —");
        resumenTarifa.setText("* Tarifa de temporada: —");
        resumenDescuento.setText("* Descuento de fidelidad: —");
        resumenTotal.setText("COSTO TOTAL NETO: —");
    }

    private void mostrarErrorCotizacion(String mensaje) {
        errorCotizacion.setText(mensaje);
        errorCotizacion.getParent().setVisible(true);
        errorCotizacion.getParent().setManaged(true);
        avisoCotizacion.setVisible(false);
        avisoCotizacion.setManaged(false);
    }

    private void limpiarErrorCotizacion() {
        errorCotizacion.setText("");
        errorCotizacion.getParent().setVisible(false);
        errorCotizacion.getParent().setManaged(false);
        avisoCotizacion.setVisible(true);
        avisoCotizacion.setManaged(true);
    }

    private String textoTemporada(String temporada) {
        return temporada == null || temporada.isBlank() ? "sin nombre" : temporada;
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
                    "Tu reserva en " + cabana.getNombre() + " quedó registrada.\nTotal: " + moneda(precio));
            comentario.clear();
            cargarDatos();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            mostrarErrorCotizacion(exception.getMessage());
        } catch (SQLException exception) {
            mostrarErrorCotizacion("No se pudo guardar la reserva: " + exception.getMessage());
        }
    }

    private String moneda(BigDecimal valor) {
        if (valor == null) {
            return "—";
        }
        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));
        formato.setMinimumFractionDigits(0);
        formato.setMaximumFractionDigits(0);
        formato.setRoundingMode(RoundingMode.HALF_UP);
        return formato.format(valor);
    }
}