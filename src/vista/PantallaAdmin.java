package vista;

import controlador.AdminController;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modelo.Invitado;
import modelo.ReservaDetalle;
import modelo.Temporada;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class PantallaAdmin {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Stage stage;
    private final Invitado invitado;
    private final AdminController adminController;
    private final Runnable alCerrarSesion;
    private final BorderPane raiz = new BorderPane();
    private final VBox menu = new VBox(7);
    private String tituloPagina = "Resumen";
    private String descripcionPagina = "Estado general de la operación y accesos de administración.";
    private Button menuSeleccionado;

    public PantallaAdmin(Stage stage, Invitado invitado, Runnable alCerrarSesion) {
        this(stage, invitado, new AdminController(), alCerrarSesion);
    }

    public PantallaAdmin(Stage stage, Invitado invitado, AdminController adminController,
            Runnable alCerrarSesion) {
        this.stage = stage;
        this.invitado = invitado;
        this.adminController = adminController;
        this.alCerrarSesion = alCerrarSesion;
    }

    public Scene crearEscena() {
        raiz.setLeft(crearBarraLateral());
        raiz.setTop(crearEncabezado());
        raiz.setCenter(new VBox());
        raiz.getStyleClass().add("page-background");
        Scene escena = new Scene(raiz, 1320, 840);
        ComponentesUI.aplicarEstilos(escena);
        mostrarResumen();
        return escena;
    }

    private VBox crearBarraLateral() {
        Label marca = new Label("Buena Caza");
        marca.getStyleClass().add("sidebar-brand");
        Label panel = new Label("PANEL ADMINISTRADOR");
        panel.getStyleClass().add("sidebar-caption");
        VBox identidad = new VBox(5, marca, panel);
        identidad.setPadding(new Insets(10, 5, 28, 5));

        agregarOpcion("Resumen", this::mostrarResumen);
        agregarOpcion("Gestionar invitados", this::mostrarInvitados);
        agregarOpcion("Temporadas y tarifas", this::mostrarTemporadas);
        agregarOpcion("Reporte de ocupación", this::mostrarOcupacion);
        agregarOpcion("Reporte de invitados", this::mostrarReporteInvitados);

        var salir = ComponentesUI.boton("Cerrar sesión", "nav-button", alCerrarSesion);
        VBox panelLateral = new VBox(14, identidad, menu, new javafx.scene.layout.Region(), salir);
        panelLateral.setPadding(new Insets(28, 18, 22, 18));
        panelLateral.setPrefWidth(250);
        panelLateral.getStyleClass().add("sidebar");
        VBox.setVgrow(panel, Priority.ALWAYS);
        VBox.setVgrow(panelLateral.getChildren().get(2), Priority.ALWAYS);
        return panelLateral;
    }

    private void agregarOpcion(String texto, Runnable accion) {
        Button boton = ComponentesUI.boton(texto, "nav-button", () -> {
            if (menuSeleccionado != null) {
                menuSeleccionado.getStyleClass().remove("nav-button-active");
            }
            menuSeleccionado = (Button) menu.getChildren().stream()
                    .filter(nodo -> nodo instanceof Button actual && actual.getText().equals(texto))
                    .findFirst().orElse(null);
            if (menuSeleccionado != null) {
                menuSeleccionado.getStyleClass().add("nav-button-active");
            }
            accion.run();
        });
        boton.setAlignment(Pos.CENTER_LEFT);
        menu.getChildren().add(boton);
        if (menuSeleccionado == null) {
            menuSeleccionado = boton;
            boton.getStyleClass().add("nav-button-active");
        }
    }

    private HBox crearEncabezado() {
        Label rol = new Label("Administración");
        rol.getStyleClass().add("eyebrow");
        Label cuenta = new Label(invitado.getEmail());
        cuenta.getStyleClass().add("page-subtitle");
        HBox barra = new HBox(15, rol, new javafx.scene.layout.Region(), cuenta);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(20, 34, 20, 34));
        HBox.setHgrow(barra.getChildren().get(1), Priority.ALWAYS);
        barra.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #e2eae6 transparent;");
        return barra;
    }

    private void mostrarResumen() {
        actualizarTitulos("Resumen", "Vista general de reservas, huéspedes y tarifas.");
        try {
            List<Invitado> invitados = adminController.listarInvitados();
            List<Temporada> temporadas = adminController.listarTemporadas();
            List<ReservaDetalle> reservas = adminController.listarReservas();
            HBox indicadores = new HBox(16,
                    ComponentesUI.tarjeta("RESERVAS REGISTRADAS", String.valueOf(reservas.size()),
                            "Estadías guardadas en la base"),
                    ComponentesUI.tarjeta("INVITADOS", String.valueOf(invitados.size()),
                            "Personas registradas en la base"),
                    ComponentesUI.tarjeta("TEMPORADAS", String.valueOf(temporadas.size()),
                            "Tramos tarifarios configurados"));
            indicadores.setFillHeight(true);
            VBox resumen = new VBox(24,
                    ComponentesUI.titulo("Buen día"),
                    ComponentesUI.subtitulo("Desde este panel podés administrar el complejo Buena Caza."),
                    indicadores,
                    panelAccesosRapidos());
            setContenido(resumen);
        } catch (SQLException exception) {
            mostrarErrorBase(exception);
        }
    }

    private VBox panelAccesosRapidos() {
        VBox panel = new VBox(12,
                new Label("Accesos rápidos"),
                ComponentesUI.subtitulo("Configurá precios por temporada o consultá el estado de ocupación."));
        panel.getStyleClass().add("content-card");
        panel.setPadding(new Insets(22));
        HBox accesos = new HBox(12,
                ComponentesUI.boton("Editar tarifas", "button-secondary", this::mostrarTemporadas),
                ComponentesUI.boton("Ver ocupación", "button-secondary", this::mostrarOcupacion));
        panel.getChildren().add(accesos);
        return panel;
    }

    private void mostrarInvitados() {
        actualizarTitulos("Gestionar invitados",
                "Consultá los datos de contacto, el rol y si cada huésped ya tiene reservas.");
        try {
            List<Invitado> invitados = adminController.listarInvitados();
            Map<Integer, Boolean> conReservas = new HashMap<>();
            for (Invitado invitado : invitados) {
                conReservas.put(invitado.getIdInvitado(), invitado.isComunidad());
            }
            TableView<Invitado> tabla = new TableView<>(FXCollections.observableArrayList(invitados));
            tabla.getColumns().setAll(java.util.List.of(
                    ColumnaTabla.crear("Nombre", Invitado::getNombre),
                    ColumnaTabla.crear("DNI", invitado -> String.valueOf(invitado.getDni())),
                    ColumnaTabla.crear("Correo", Invitado::getEmail),
                    ColumnaTabla.crear("Teléfono", invitado -> texto(invitado.getTelefono())),
                    ColumnaTabla.crear("Dirección", Invitado::getDireccion),
                    ColumnaTabla.crear("Con reservas", invitado ->
                            conReservas.get(invitado.getIdInvitado()) ? "Sí" : "No"),
                    ColumnaTabla.crear("Rol", invitado -> invitado.isEsAdmin() ? "Administrador" : "Invitado")));
            tabla.setPlaceholder(new Label("No hay invitados registrados."));
            Button nuevo = ComponentesUI.boton("Nuevo invitado", "button-primary",
                    () -> editarInvitado(null, true));
            Button editar = ComponentesUI.boton("Editar seleccionado", "button-secondary",
                    () -> editarSeleccionado(tabla.getSelectionModel().getSelectedItem()));
            Button eliminar = ComponentesUI.boton("Eliminar seleccionado", "button-secondary",
                    () -> eliminarInvitado(tabla.getSelectionModel().getSelectedItem()));
            nuevo.setMaxWidth(180);
            editar.setMaxWidth(190);
            eliminar.setMaxWidth(190);
            HBox acciones = new HBox(10, nuevo, editar, eliminar);
            acciones.setAlignment(Pos.CENTER_RIGHT);
            setContenido(panelTabla(tabla, "Listado de invitados", acciones));
        } catch (SQLException exception) {
            mostrarErrorBase(exception);
        }
    }

    private void editarSeleccionado(Invitado seleccionado) {
        if (seleccionado == null) {
            ComponentesUI.mostrarInformacion(stage, "Elegí un invitado",
                    "Seleccioná una fila antes de editar.");
            return;
        }
        editarInvitado(seleccionado, false);
    }

    private void editarInvitado(Invitado original, boolean nuevo) {
        final Invitado seleccionado = original;
        boolean tieneReservas = false;
        if (!nuevo) {
            try {
                tieneReservas = seleccionado.isComunidad();
            } catch (SQLException exception) {
                mostrarErrorBase(exception);
                return;
            }
        }
        Dialog<Invitado> dialogo = new Dialog<>();
        dialogo.setTitle(nuevo ? "Nuevo invitado" : "Editar invitado");
        dialogo.setHeaderText(nuevo ? "Ingresá los datos del huésped" : seleccionado.getNombre());
        dialogo.initOwner(stage);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField nombre = entradaDialogo(nuevo ? "" : seleccionado.getNombre());
        TextField dni = entradaDialogo(nuevo ? "" : String.valueOf(seleccionado.getDni()));
        TextField email = entradaDialogo(nuevo ? "" : seleccionado.getEmail());
        TextField telefono = entradaDialogo(nuevo ? "" : texto(seleccionado.getTelefono()));
        TextField direccion = entradaDialogo(nuevo ? "" : seleccionado.getDireccion());
        Label reservas = new Label(tieneReservas
                ? "Tiene reservas registradas (descuento del 10%)."
                : "Todavía no tiene reservas registradas.");
        Label error = new Label();
        error.getStyleClass().add("status-note");
        GridPane formulario = new GridPane();
        formulario.setHgap(12);
        formulario.setVgap(12);
        formulario.setPadding(new Insets(12));
        formulario.addRow(0, new Label("Nombre completo"), nombre);
        formulario.addRow(1, new Label("DNI"), dni);
        formulario.addRow(2, new Label("Correo electrónico"), email);
        formulario.addRow(3, new Label("Teléfono"), telefono);
        formulario.addRow(4, new Label("Dirección"), direccion);
        formulario.add(reservas, 0, 5, 2, 1);
        formulario.add(error, 0, 6, 2, 1);
        dialogo.getDialogPane().setContent(formulario);
        dialogo.setResultConverter(boton -> {
            if (boton != ButtonType.OK) {
                return null;
            }
            if (nombre.getText().isBlank() || dni.getText().isBlank() || email.getText().isBlank()
                    || direccion.getText().isBlank()) {
                error.setText("Completá nombre, DNI, correo y dirección.");
                return null;
            }
            if (!email.getText().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                error.setText("Ingresá un correo electrónico válido.");
                return null;
            }
            try {
                int numeroDni = Integer.parseInt(dni.getText().trim());
                return new Invitado(nuevo ? 0 : seleccionado.getIdInvitado(), numeroDni,
                        email.getText().trim(), nombre.getText().trim(),
                        telefono.getText().isBlank() ? null : telefono.getText().trim(),
                        direccion.getText().trim(), nuevo ? false : seleccionado.isEsAdmin());
            } catch (NumberFormatException exception) {
                error.setText("El DNI debe contener solo números.");
                return null;
            }
        });
        try {
            Optional<Invitado> resultado = dialogo.showAndWait();
            if (resultado.isEmpty()) {
                if (!error.getText().isBlank()) {
                    ComponentesUI.mostrarError(stage, "Datos no válidos", error.getText());
                }
                return;
            }
            adminController.guardarInvitado(resultado.get());
            mostrarInvitados();
        } catch (IllegalArgumentException exception) {
            ComponentesUI.mostrarError(stage, "Datos no válidos", exception.getMessage());
        } catch (SQLException exception) {
            ComponentesUI.mostrarError(stage, "No se pudo guardar el invitado",
                    "Verificá que el correo no pertenezca a otra cuenta.\n\n" + exception.getMessage());
        }
    }

    private TextField entradaDialogo(String valor) {
        TextField campo = new TextField(valor);
        ComponentesUI.prepararCampo(campo);
        return campo;
    }

    private void eliminarInvitado(Invitado invitado) {
        if (invitado == null) {
            ComponentesUI.mostrarInformacion(stage, "Elegí un invitado",
                    "Seleccioná una fila antes de eliminar.");
            return;
        }
        if (invitado.isEsAdmin()) {
            ComponentesUI.mostrarInformacion(stage, "Acción no permitida",
                    "Las cuentas administradoras no se pueden eliminar desde la gestión de invitados.");
            return;
        }
        javafx.scene.control.Alert confirmacion = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar invitado");
        confirmacion.setHeaderText("¿Eliminar a " + invitado.getNombre() + "?");
        confirmacion.setContentText("La cuenta asociada también se eliminará. No se puede borrar un invitado "
                + "que tenga reservas registradas.");
        confirmacion.initOwner(stage);
        if (confirmacion.showAndWait().filter(respuesta -> respuesta == ButtonType.OK).isEmpty()) {
            return;
        }
        try {
            adminController.eliminarInvitado(invitado.getIdInvitado());
            mostrarInvitados();
        } catch (SQLException exception) {
            ComponentesUI.mostrarError(stage, "No se pudo eliminar el invitado",
                    "La base puede impedir la eliminación si conserva reservas asociadas.\n\n"
                            + exception.getMessage());
        }
    }

    private void mostrarTemporadas() {
        actualizarTitulos("Temporadas y tarifas", "Revisá los períodos y ajustá el precio por noche.");
        try {
            TableView<Temporada> tabla = new TableView<>(
                    FXCollections.observableArrayList(adminController.listarTemporadas()));
            tabla.getColumns().setAll(java.util.List.of(
                    ColumnaTabla.crear("Temporada", temporada -> texto(temporada.getTipo())),
                    ColumnaTabla.crear("Desde", temporada -> fecha(temporada.getFechaInicio())),
                    ColumnaTabla.crear("Hasta", temporada -> fecha(temporada.getFechaFinal())),
                    ColumnaTabla.crear("Precio por noche",
                            temporada -> moneda(temporada.getPrecioPorNoche()))));
            tabla.setPlaceholder(new Label("No hay temporadas configuradas."));
            var editar = ComponentesUI.boton("Editar precio", "button-primary", () -> editarPrecio(tabla));
            editar.setMaxWidth(170);
            HBox acciones = new HBox(editar);
            acciones.setAlignment(Pos.CENTER_RIGHT);
            setContenido(panelTabla(tabla, "Tarifas por temporada", acciones));
        } catch (SQLException exception) {
            mostrarErrorBase(exception);
        }
    }

    private void editarPrecio(TableView<Temporada> tabla) {
        Temporada seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            ComponentesUI.mostrarInformacion(stage, "Elegí una temporada",
                    "Seleccioná una fila antes de editar su precio.");
            return;
        }
        String precioActual = seleccionada.getPrecioPorNoche() == null
                ? "" : seleccionada.getPrecioPorNoche().toPlainString();
        TextInputDialog dialogo = new TextInputDialog(precioActual);
        dialogo.setTitle("Actualizar tarifa");
        dialogo.setHeaderText("Temporada " + seleccionada.getTipo());
        dialogo.setContentText("Nuevo precio por noche:");
        dialogo.initOwner(stage);
        Optional<String> respuesta = dialogo.showAndWait();
        if (respuesta.isEmpty()) {
            return;
        }
        try {
            BigDecimal precio = new BigDecimal(respuesta.get().trim().replace(',', '.'));
            adminController.actualizarPrecio(seleccionada.getIdTemporada(), precio);
            mostrarTemporadas();
        } catch (IllegalArgumentException exception) {
            ComponentesUI.mostrarError(stage, "Precio no válido",
                    "Ingresá un importe numérico mayor que cero.");
        } catch (SQLException exception) {
            mostrarErrorBase(exception);
        }
    }

    private void mostrarOcupacion() {
        actualizarTitulos("Reporte de ocupación", "Reservas registradas por cabaña y rango de estadía.");
        try {
            TableView<ReservaDetalle> tabla = crearTablaReservas(adminController.listarReservas(), true);
            setContenido(panelTabla(tabla, "Detalle de estadías"));
        } catch (SQLException exception) {
            mostrarErrorBase(exception);
        }
    }

    private void mostrarReporteInvitados() {
        actualizarTitulos("Reporte de invitados", "Buscá en la comunidad de huéspedes registrados.");
        try {
            List<Invitado> invitados = adminController.listarInvitados();
            FilteredList<Invitado> filtrados = new FilteredList<>(FXCollections.observableArrayList(invitados));
            TextField busqueda = new TextField();
            busqueda.setPromptText("Filtrar por nombre, DNI o correo");
            ComponentesUI.prepararCampo(busqueda);
            busqueda.textProperty().addListener((observable, anterior, nuevo) -> {
                String termino = nuevo == null ? "" : nuevo.trim().toLowerCase(java.util.Locale.ROOT);
                Predicate<Invitado> coincide = invitado -> invitado.getNombre().toLowerCase(java.util.Locale.ROOT)
                        .contains(termino)
                        || invitado.getEmail().toLowerCase(java.util.Locale.ROOT).contains(termino)
                        || String.valueOf(invitado.getDni()).contains(termino);
                filtrados.setPredicate(coincide);
            });
            TableView<Invitado> tabla = new TableView<>(filtrados);
            tabla.getColumns().setAll(java.util.List.of(
                    ColumnaTabla.crear("Nombre", Invitado::getNombre),
                    ColumnaTabla.crear("DNI", invitado -> String.valueOf(invitado.getDni())),
                    ColumnaTabla.crear("Correo electrónico", Invitado::getEmail),
                    ColumnaTabla.crear("Teléfono", invitado -> texto(invitado.getTelefono()))));
            VBox contenedor = panelTabla(tabla, "Comunidad Buena Caza");
            contenedor.getChildren().add(0, busqueda);
            setContenido(contenedor);
        } catch (SQLException exception) {
            mostrarErrorBase(exception);
        }
    }

    private TableView<ReservaDetalle> crearTablaReservas(List<ReservaDetalle> reservas, boolean conInvitado) {
        TableView<ReservaDetalle> tabla = new TableView<>(FXCollections.observableArrayList(reservas));
        if (conInvitado) {
            tabla.getColumns().add(ColumnaTabla.crear("Invitado", ReservaDetalle::getInvitado));
        }
        tabla.getColumns().addAll(java.util.List.of(
                ColumnaTabla.crear("Cabaña", ReservaDetalle::getCabana),
                ColumnaTabla.crear("Entrada", reserva -> fecha(reserva.getFechaEntrada())),
                ColumnaTabla.crear("Salida", reserva -> fecha(reserva.getFechaSalida())),
                ColumnaTabla.crear("Temporada", ReservaDetalle::getTemporada),
                ColumnaTabla.crear("Total", reserva -> moneda(reserva.getMontoTotal()))));
        tabla.setPlaceholder(new Label("No hay reservas para mostrar."));
        return tabla;
    }

    private VBox panelTabla(TableView<?> tabla, String titulo) {
        return panelTabla(tabla, titulo, null);
    }

    private VBox panelTabla(TableView<?> tabla, String titulo, HBox acciones) {
        Label encabezado = new Label(titulo);
        encabezado.getStyleClass().add("section-title");
        VBox panel = new VBox(16, encabezado, tabla);
        if (acciones != null) {
            panel.getChildren().add(acciones);
        }
        panel.getStyleClass().add("content-card");
        panel.setPadding(new Insets(22));
        VBox.setVgrow(tabla, Priority.ALWAYS);
        return panel;
    }

    private void actualizarTitulos(String titulo, String descripcion) {
        tituloPagina = titulo;
        descripcionPagina = descripcion;
    }

    private void setContenido(VBox contenido) {
        VBox pagina = new VBox(20, ComponentesUI.titulo(tituloPagina),
                ComponentesUI.subtitulo(descripcionPagina), contenido);
        pagina.setPadding(new Insets(34));
        ScrollPane scroll = new ScrollPane(pagina);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("page-background");
        raiz.setCenter(scroll);
        VBox.setVgrow(contenido, Priority.ALWAYS);
    }

    private void mostrarErrorBase(SQLException exception) {
        ComponentesUI.mostrarError(stage, "No se pudieron cargar los datos",
                "Verificá que MySQL esté disponible y que la base esté actualizada.\n\n"
                        + exception.getMessage());
    }

    private String fecha(java.time.LocalDate fecha) {
        return fecha.format(FORMATO_FECHA);
    }

    private String moneda(BigDecimal precio) {
        if (precio == null) {
            return "—";
        }
        return java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("es-AR"))
                .format(precio);
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? "—" : valor;
    }
}