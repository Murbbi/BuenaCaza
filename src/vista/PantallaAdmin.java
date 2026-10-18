package vista;

import controlador.AdminController;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modelo.Invitado;
import modelo.ReservaDetalle;
import modelo.Temporada;
import modelo.TemporadaTipo;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PantallaAdmin {
    private Stage stage;
    private Invitado invitado;
    private Runnable alCerrarSesion;
    private AdminController adminController = new AdminController();

    private BorderPane raiz = new BorderPane();
    private VBox menu = new VBox(7);
    private Button menuSeleccionado;

    public PantallaAdmin(Stage stage, Invitado invitado, Runnable alCerrarSesion) {
        this.stage = stage;
        this.invitado = invitado;
        this.alCerrarSesion = alCerrarSesion;
    }

    public Scene crearEscena() {
        raiz.setLeft(crearBarraLateral());
        raiz.setTop(crearEncabezado());
        raiz.getStyleClass().add("page-background");
        Scene escena = new Scene(raiz, 1320, 840);
        ComponentesUI.aplicarEstilos(escena);
        mostrarInvitados();
        return escena;
    }

    // ---------------- Menú lateral ----------------

    private VBox crearBarraLateral() {
        Label marca = new Label("Buena Caza");
        marca.getStyleClass().add("sidebar-brand");
        Label panel = new Label("PANEL ADMINISTRADOR");
        panel.getStyleClass().add("sidebar-caption");
        VBox identidad = new VBox(5, marca, panel);
        identidad.setPadding(new Insets(10, 5, 28, 5));

        agregarOpcion("Gestionar invitados", this::mostrarInvitados);
        agregarOpcion("Temporadas y tarifas", this::mostrarTemporadas);
        agregarOpcion("Reporte de ocupación", this::mostrarOcupacion);
        agregarOpcion("Reporte de invitados", this::mostrarReporteInvitados);
        agregarOpcion("Panel de invitado", this::mostrarPanelInvitado);

        Button salir = ComponentesUI.boton("Cerrar sesión", "nav-button", alCerrarSesion);
        Region espacio = new Region();
        VBox.setVgrow(espacio, Priority.ALWAYS);

        VBox barra = new VBox(14, identidad, menu, espacio, salir);
        barra.setPadding(new Insets(28, 18, 22, 18));
        barra.setPrefWidth(250);
        barra.getStyleClass().add("sidebar");
        return barra;
    }

    private void agregarOpcion(String texto, Runnable accion) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("nav-button");
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setAlignment(Pos.CENTER_LEFT);
        boton.setOnAction(e -> {
            // Se marca como activa la opción elegida
            menuSeleccionado.getStyleClass().remove("nav-button-active");
            boton.getStyleClass().add("nav-button-active");
            menuSeleccionado = boton;
            accion.run();
        });
        // La primera opción arranca seleccionada
        if (menuSeleccionado == null) {
            menuSeleccionado = boton;
            boton.getStyleClass().add("nav-button-active");
        }
        menu.getChildren().add(boton);
    }

    private HBox crearEncabezado() {
        Label rol = new Label("Administración");
        rol.getStyleClass().add("eyebrow");
        Label cuenta = new Label(invitado.getEmail());
        cuenta.getStyleClass().add("page-subtitle");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox barra = new HBox(15, rol, espacio, cuenta);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(20, 34, 20, 34));
        barra.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #e2eae6 transparent;");
        return barra;
    }

    private void mostrarPanelInvitado() {
        VentanaReserva portal = new VentanaReserva(stage, invitado, alCerrarSesion);
        raiz.setCenter(portal.crearContenidoPanel());
    }

    // ---------------- Gestión de invitados (ABM) ----------------

    private void mostrarInvitados() {
        try {
            List<Invitado> invitados = adminController.listarInvitados();
            // Se consulta una sola vez quiénes tienen reservas, así la tabla no va a la base en cada fila
            List<Integer> conReservas = new ArrayList<>();
            for (Invitado inv : invitados) {
                if (inv.isComunidad()) {
                    conReservas.add(inv.getIdInvitado());
                }
            }
            TableView<Invitado> tabla = new TableView<>(FXCollections.observableArrayList(invitados));
            tabla.getColumns().add(ColumnaTabla.crear("Nombre", Invitado::getNombre));
            tabla.getColumns().add(ColumnaTabla.crear("DNI", inv -> String.valueOf(inv.getDni())));
            tabla.getColumns().add(ColumnaTabla.crear("Correo", Invitado::getEmail));
            tabla.getColumns().add(ColumnaTabla.crear("Teléfono", inv -> ComponentesUI.texto(inv.getTelefono())));
            tabla.getColumns().add(ColumnaTabla.crear("Dirección", Invitado::getDireccion));
            tabla.getColumns().add(ColumnaTabla.crear("Con reservas",
                    inv -> conReservas.contains(inv.getIdInvitado()) ? "Sí" : "No"));
            tabla.getColumns().add(ColumnaTabla.crear("Rol", Invitado::getRol));
            tabla.setPlaceholder(new Label("No hay invitados registrados."));

            Button nuevo = ComponentesUI.boton("Nuevo invitado", "button-primary", () -> editarInvitado(null));
            Button editar = ComponentesUI.boton("Editar seleccionado", "button-secondary",
                    () -> editarInvitado(tabla.getSelectionModel().getSelectedItem()));
            Button eliminar = ComponentesUI.boton("Eliminar seleccionado", "button-secondary",
                    () -> eliminarInvitado(tabla.getSelectionModel().getSelectedItem()));
            nuevo.setMaxWidth(180);
            editar.setMaxWidth(190);
            eliminar.setMaxWidth(190);

            mostrarPagina("Gestionar invitados",
                    "Consultá los datos de contacto, el rol y si cada huésped ya tiene reservas.",
                    panelTabla("Listado de invitados", tabla, nuevo, editar, eliminar));
        } catch (SQLException e) {
            mostrarErrorBase(e);
        }
    }

    // Si seleccionado es null se carga un invitado nuevo
    private void editarInvitado(Invitado seleccionado) {
        boolean nuevo = seleccionado == null;
        TextField nombre = new TextField(nuevo ? "" : seleccionado.getNombre());
        TextField dni = new TextField(nuevo ? "" : String.valueOf(seleccionado.getDni()));
        TextField email = new TextField(nuevo ? "" : seleccionado.getEmail());
        TextField telefono = new TextField(nuevo || seleccionado.getTelefono() == null ? "" : seleccionado.getTelefono());
        TextField direccion = new TextField(nuevo ? "" : seleccionado.getDireccion());

        GridPane formulario = new GridPane();
        formulario.setHgap(12);
        formulario.setVgap(12);
        formulario.setPadding(new Insets(12));
        formulario.addRow(0, new Label("Nombre completo"), nombre);
        formulario.addRow(1, new Label("DNI"), dni);
        formulario.addRow(2, new Label("Correo electrónico"), email);
        formulario.addRow(3, new Label("Teléfono"), telefono);
        formulario.addRow(4, new Label("Dirección"), direccion);

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle(nuevo ? "Nuevo invitado" : "Editar invitado");
        dialogo.setHeaderText(nuevo ? "Ingresá los datos del huésped" : seleccionado.getNombre());
        dialogo.getDialogPane().setContent(formulario);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogo.initOwner(stage);
        ComponentesUI.aplicarIcono(dialogo);

        Optional<ButtonType> respuesta = dialogo.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }
        if (nombre.getText().isBlank() || dni.getText().isBlank() || email.getText().isBlank()
                || direccion.getText().isBlank()) {
            ComponentesUI.mostrarError(stage, "Datos incompletos", "Completá nombre, DNI, correo y dirección.");
            return;
        }

        try {
            Invitado datos = new Invitado(nuevo ? 0 : seleccionado.getIdInvitado(),
                    Integer.parseInt(dni.getText().trim()), email.getText().trim(), nombre.getText().trim(),
                    telefono.getText().trim(), direccion.getText().trim());
            adminController.guardarInvitado(datos);
            mostrarInvitados();
        } catch (NumberFormatException e) {
            ComponentesUI.mostrarError(stage, "Datos no válidos", "El DNI debe tener solo números.");
        } catch (SQLException e) {
            ComponentesUI.mostrarError(stage, "No se pudo guardar",
                    "Revisá que el correo no sea de otra cuenta.\n" + e.getMessage());
        }
    }

    private void eliminarInvitado(Invitado seleccionado) {
        if (seleccionado == null) {
            ComponentesUI.mostrarInformacion(stage, "Elegí un invitado", "Seleccioná una fila antes de eliminar.");
            return;
        }
        if (seleccionado.puedeAdministrar()) {
            ComponentesUI.mostrarInformacion(stage, "Acción no permitida",
                    "No se puede eliminar una cuenta de administrador.");
            return;
        }
        if (!ComponentesUI.confirmar(stage, "Eliminar invitado", "¿Eliminar a " + seleccionado.getNombre() + "?")) {
            return;
        }

        try {
            adminController.eliminarInvitado(seleccionado);
            mostrarInvitados();
        } catch (IllegalArgumentException e) {
            ComponentesUI.mostrarError(stage, "No se pudo eliminar", e.getMessage());
        } catch (SQLException e) {
            mostrarErrorBase(e);
        }
    }

    // ---------------- Temporadas y tarifas ----------------

    private void mostrarTemporadas() {
        try {
            TableView<Temporada> tabla = new TableView<>(
                    FXCollections.observableArrayList(adminController.listarTemporadas()));
            tabla.getColumns().add(ColumnaTabla.crear("ID", t -> String.valueOf(t.getIdTemporada())));
            tabla.getColumns().add(ColumnaTabla.crear("Temporada", Temporada::getTipo));
            tabla.getColumns().add(ColumnaTabla.crear("Desde", t -> ComponentesUI.fecha(t.getFechaInicio())));
            tabla.getColumns().add(ColumnaTabla.crear("Hasta (exclusiva)", t -> ComponentesUI.fecha(t.getFechaFinal())));
            tabla.getColumns().add(ColumnaTabla.crear("Precio por noche", t -> ComponentesUI.moneda(t.getPrecioPorNoche())));
            tabla.setPlaceholder(new Label("No hay temporadas configuradas."));

            Button agregar = ComponentesUI.boton("Agregar temporada", "button-secondary", () -> editarTemporada(null));
            Button modificar = ComponentesUI.boton("Modificar", "button-primary", () -> {
                Temporada seleccionada = tabla.getSelectionModel().getSelectedItem();
                if (seleccionada == null) {
                    ComponentesUI.mostrarInformacion(stage, "Elegí una temporada", "Seleccioná una fila primero.");
                } else {
                    editarTemporada(seleccionada);
                }
            });
            Button eliminar = ComponentesUI.boton("Eliminar", "button-secondary",
                    () -> eliminarTemporada(tabla.getSelectionModel().getSelectedItem()));

            mostrarPagina("Temporadas y tarifas", "Revisá los períodos y ajustá el precio por noche.",
                    panelTabla("Tarifas por temporada", tabla, agregar, modificar, eliminar));
        } catch (SQLException e) {
            mostrarErrorBase(e);
        }
    }

    // Si existente es null se agrega una temporada nueva
    private void editarTemporada(Temporada existente) {
        try {
            List<TemporadaTipo> tipos = adminController.listarTiposTemporada();
            ComboBox<TemporadaTipo> tipo = new ComboBox<>(FXCollections.observableArrayList(tipos));
            DatePicker fechaInicio = new DatePicker(LocalDate.now());
            DatePicker fechaFinal = new DatePicker(LocalDate.now().plusMonths(1));
            TextField precio = new TextField();
            precio.setPromptText("Ej.: 85000");

            tipo.getSelectionModel().selectFirst();
            if (existente != null) {
                for (TemporadaTipo t : tipos) {
                    if (t.getIdTemporadaTipo() == existente.getIdTemporadaTipo()) {
                        tipo.getSelectionModel().select(t);
                    }
                }
                fechaInicio.setValue(existente.getFechaInicio());
                fechaFinal.setValue(existente.getFechaFinal());
                precio.setText(existente.getPrecioPorNoche().toPlainString());
            }

            GridPane campos = new GridPane();
            campos.setHgap(12);
            campos.setVgap(12);
            campos.addRow(0, new Label("Tipo:"), tipo);
            campos.addRow(1, new Label("Fecha de inicio:"), fechaInicio);
            campos.addRow(2, new Label("Fecha final (exclusiva):"), fechaFinal);
            campos.addRow(3, new Label("Precio por noche:"), precio);

            Dialog<ButtonType> dialogo = new Dialog<>();
            dialogo.setTitle(existente == null ? "Agregar temporada" : "Modificar temporada");
            dialogo.setHeaderText("Ingresá los datos del período.");
            dialogo.getDialogPane().setContent(campos);
            dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialogo.initOwner(stage);
            ComponentesUI.aplicarIcono(dialogo);

            Optional<ButtonType> respuesta = dialogo.showAndWait();
            if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
                return;
            }

            Temporada temporada = new Temporada(
                    existente == null ? 0 : existente.getIdTemporada(),
                    tipo.getValue().getIdTemporadaTipo(),
                    fechaInicio.getValue(),
                    fechaFinal.getValue(),
                    new BigDecimal(precio.getText().trim()));
            adminController.guardarTemporada(temporada);
            mostrarTemporadas();
        } catch (NumberFormatException e) {
            ComponentesUI.mostrarError(stage, "Precio no válido", "Ingresá un monto numérico válido.");
        } catch (IllegalArgumentException e) {
            ComponentesUI.mostrarError(stage, "No se pudo guardar la temporada", e.getMessage());
        } catch (SQLException e) {
            mostrarErrorBase(e);
        }
    }

    private void eliminarTemporada(Temporada seleccionada) {
        if (seleccionada == null) {
            ComponentesUI.mostrarInformacion(stage, "Elegí una temporada", "Seleccioná una fila primero.");
            return;
        }
        if (!ComponentesUI.confirmar(stage, "Eliminar temporada",
                "¿Eliminar la temporada " + seleccionada.getTipo() + "?")) {
            return;
        }

        try {
            adminController.eliminarTemporada(seleccionada.getIdTemporada());
            mostrarTemporadas();
        } catch (IllegalArgumentException e) {
            ComponentesUI.mostrarError(stage, "No se pudo eliminar", e.getMessage());
        } catch (SQLException e) {
            mostrarErrorBase(e);
        }
    }

    // ---------------- Reportes ----------------

    private void mostrarOcupacion() {
        try {
            List<ReservaDetalle> reservas = adminController.listarReservas();
            TableView<ReservaDetalle> tabla = new TableView<>(FXCollections.observableArrayList(reservas));
            tabla.getColumns().add(ColumnaTabla.crear("Invitado", ReservaDetalle::getInvitado));
            tabla.getColumns().add(ColumnaTabla.crear("Cabaña", ReservaDetalle::getCabana));
            tabla.getColumns().add(ColumnaTabla.crear("Entrada", r -> ComponentesUI.fecha(r.getFechaEntrada())));
            tabla.getColumns().add(ColumnaTabla.crear("Salida", r -> ComponentesUI.fecha(r.getFechaSalida())));
            tabla.getColumns().add(ColumnaTabla.crear("Temporada", ReservaDetalle::getTemporada));
            tabla.getColumns().add(ColumnaTabla.crear("Total", r -> ComponentesUI.moneda(r.getMontoTotal())));
            tabla.setPlaceholder(new Label("No hay reservas para mostrar."));

            mostrarPagina("Reporte de ocupación", "Reservas registradas por cabaña y rango de estadía.",
                    panelTabla("Detalle de estadías", tabla));
        } catch (SQLException e) {
            mostrarErrorBase(e);
        }
    }

    private void mostrarReporteInvitados() {
        try {
            FilteredList<Invitado> filtrados = new FilteredList<>(
                    FXCollections.observableArrayList(adminController.listarInvitados()));
            TableView<Invitado> tabla = new TableView<>(filtrados);
            tabla.getColumns().add(ColumnaTabla.crear("Nombre", Invitado::getNombre));
            tabla.getColumns().add(ColumnaTabla.crear("DNI", inv -> String.valueOf(inv.getDni())));
            tabla.getColumns().add(ColumnaTabla.crear("Correo electrónico", Invitado::getEmail));
            tabla.getColumns().add(ColumnaTabla.crear("Teléfono", inv -> ComponentesUI.texto(inv.getTelefono())));
            tabla.getColumns().add(ColumnaTabla.crear("Admin", inv -> inv.puedeAdministrar() ? "Sí" : "No"));
            tabla.getColumns().add(ColumnaTabla.crear("Dirección", inv -> ComponentesUI.texto(inv.getDireccion())));

            // Buscador: filtra la lista mientras se escribe
            TextField busqueda = new TextField();
            busqueda.setPromptText("Filtrar por nombre, DNI o correo");
            ComponentesUI.prepararCampo(busqueda);
            busqueda.textProperty().addListener((obs, anterior, texto) -> {
                String buscado = texto.trim().toLowerCase();
                filtrados.setPredicate(inv -> inv.getNombre().toLowerCase().contains(buscado)
                        || inv.getEmail().toLowerCase().contains(buscado)
                        || String.valueOf(inv.getDni()).contains(buscado));
            });

            VBox panel = panelTabla("Comunidad Buena Caza", tabla);
            panel.getChildren().add(0, busqueda);
            mostrarPagina("Reporte de invitados", "Buscá en la comunidad de huéspedes registrados.", panel);
        } catch (SQLException e) {
            mostrarErrorBase(e);
        }
    }

    // ---------------- Métodos de ayuda ----------------

    // Arma la tarjeta blanca con el título, la tabla y los botones (si hay)
    private VBox panelTabla(String titulo, TableView<?> tabla, Button... botones) {
        Label encabezado = new Label(titulo);
        encabezado.getStyleClass().add("section-title");
        VBox panel = new VBox(16, encabezado, tabla);
        if (botones.length > 0) {
            HBox acciones = new HBox(10, botones);
            acciones.setAlignment(Pos.CENTER_RIGHT);
            panel.getChildren().add(acciones);
        }
        panel.getStyleClass().add("content-card");
        panel.setPadding(new Insets(22));
        VBox.setVgrow(tabla, Priority.ALWAYS);
        return panel;
    }

    private void mostrarPagina(String titulo, String descripcion, VBox contenido) {
        VBox pagina = new VBox(20, ComponentesUI.titulo(titulo), ComponentesUI.subtitulo(descripcion), contenido);
        pagina.setPadding(new Insets(34));
        VBox.setVgrow(contenido, Priority.ALWAYS);
        ScrollPane scroll = new ScrollPane(pagina);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("page-background");
        raiz.setCenter(scroll);
    }

    private void mostrarErrorBase(SQLException e) {
        ComponentesUI.mostrarError(stage, "Error en la base de datos",
                "No se pudieron cargar los datos. Verificá que MySQL esté funcionando.\n" + e.getMessage());
    }
}
