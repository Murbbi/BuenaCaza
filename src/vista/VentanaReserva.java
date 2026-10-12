package vista;

import controlador.ReservaController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modelo.Cabana;
import modelo.Invitado;
import modelo.ReservaDetalle;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class VentanaReserva {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
    private final TableView<ReservaDetalle> tablaReservas = new TableView<>();

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
        raiz.setCenter(contenido());
        Scene escena = new Scene(raiz, 1240, 820);
        ComponentesUI.aplicarEstilos(escena);
        cargarDatos();
        return escena;
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
        VBox panelHistorial = new VBox(16,
                ComponentesUI.titulo("Mis reservas"),
                ComponentesUI.subtitulo("Consultá las fechas y el detalle de tus estadías."));
        panelHistorial.getStyleClass().add("content-card");
        panelHistorial.setPadding(new Insets(24));
        configurarTablaReservas();
        panelHistorial.getChildren().add(tablaReservas);
        VBox.setVgrow(tablaReservas, Priority.ALWAYS);

        VBox cuerpo = new VBox(22, contexto, titulo, descripcion, panelReserva, panelHistorial);
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
        VBox valor = new VBox(5, etiquetaTotal, total, avisoCotizacion);
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

    private void configurarTablaReservas() {
        tablaReservas.getColumns().setAll(java.util.List.of(
                ColumnaTabla.crear("Cabaña", ReservaDetalle::getCabana),
                ColumnaTabla.crear("Entrada", reserva -> fecha(reserva.getFechaEntrada())),
                ColumnaTabla.crear("Salida", reserva -> fecha(reserva.getFechaSalida())),
                ColumnaTabla.crear("Temporada", ReservaDetalle::getTemporada),
                ColumnaTabla.crear("Total", reserva -> moneda(reserva.getMontoTotal()))));
        tablaReservas.setPlaceholder(new Label("Todavía no tenés reservas."));
        tablaReservas.setPrefHeight(220);
    }

    private void cargarDatos() {
        try {
            cabanas.setItems(FXCollections.observableArrayList(reservaController.listarCabanas()));
            tablaReservas.setItems(FXCollections.observableArrayList(
                    reservaController.listarReservasDelInvitado(invitado.getIdInvitado())));
            actualizarCotizacion();
        } catch (SQLException exception) {
            ComponentesUI.mostrarError(stage, "No se pudieron cargar las reservas",
                    "Verificá que MySQL esté disponible y que la base esté actualizada.\n\n"
                            + exception.getMessage());
        }
    }

    private void actualizarCotizacion() {
        Cabana cabana = cabanas.getValue();
        if (cabana == null || entrada.getValue() == null || salida.getValue() == null) {
            total.setText("—");
            avisoCotizacion.setText("Seleccioná una cabaña y fechas para calcular el total.");
            return;
        }
        try {
            BigDecimal precio = reservaController.cotizar(invitado,
                    cabana.getIdCabana(), entrada.getValue(), salida.getValue());
            total.setText(moneda(precio));
            String noches = java.time.temporal.ChronoUnit.DAYS.between(
                    entrada.getValue(), salida.getValue()) + " noches.";
            avisoCotizacion.setText(invitado.isComunidad()
                    ? "Incluye el 10% de descuento por fidelidad · " + noches
                    : "Importe estimado para " + noches);
        } catch (IllegalArgumentException exception) {
            total.setText("—");
            avisoCotizacion.setText(exception.getMessage());
        } catch (SQLException exception) {
            total.setText("—");
            avisoCotizacion.setText("No se pudo calcular el precio: " + exception.getMessage());
        }
    }

    private void confirmarReserva() {
        Cabana cabana = cabanas.getValue();
        if (cabana == null) {
            avisoCotizacion.setText("Seleccioná una cabaña disponible.");
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
            avisoCotizacion.setText(exception.getMessage());
        } catch (SQLException exception) {
            ComponentesUI.mostrarError(stage, "No se pudo guardar la reserva",
                    "La operación no se completó en la base de datos.\n\n" + exception.getMessage());
        }
    }

    private String fecha(LocalDate valor) {
        return valor.format(FORMATO_FECHA);
    }

    private String moneda(BigDecimal valor) {
        if (valor == null) {
            return "—";
        }
        return java.text.NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR")).format(valor);
    }
}