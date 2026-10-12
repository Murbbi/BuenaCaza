package controlador;

import dao.InvitadoDAO;
import dao.ReservaDAO;
import dao.TemporadaDAO;
import modelo.Invitado;
import modelo.ReservaDetalle;
import modelo.Temporada;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class AdminController {
    private final InvitadoDAO invitadoDAO;
    private final TemporadaDAO temporadaDAO;
    private final ReservaDAO reservaDAO;

    public AdminController() {
        this(new InvitadoDAO(), new TemporadaDAO(), new ReservaDAO());
    }

    public AdminController(InvitadoDAO invitadoDAO, TemporadaDAO temporadaDAO, ReservaDAO reservaDAO) {
        this.invitadoDAO = invitadoDAO;
        this.temporadaDAO = temporadaDAO;
        this.reservaDAO = reservaDAO;
    }

    public List<Invitado> listarInvitados() throws SQLException {
        return invitadoDAO.listar();
    }

    public boolean tieneReservas(int idInvitado) throws SQLException {
        return invitadoDAO.tieneReservas(idInvitado);
    }

    public List<Temporada> listarTemporadas() throws SQLException {
        return temporadaDAO.listarConTipo();
    }

    public List<ReservaDetalle> listarReservas() throws SQLException {
        return reservaDAO.listarDetalles(null);
    }

    public void guardarInvitado(Invitado invitado) throws SQLException {
        if (invitado.getDni() < 1 || invitado.getNombre() == null || invitado.getNombre().isBlank()
                || invitado.getEmail() == null || invitado.getEmail().isBlank()
                || invitado.getDireccion() == null || invitado.getDireccion().isBlank()) {
            throw new IllegalArgumentException("Completá nombre, DNI, correo y dirección.");
        }
        if (invitado.getIdInvitado() == 0) {
            invitado.setEsAdmin(false);
            if (!invitadoDAO.insertar(invitado)) {
                throw new IllegalStateException("No se pudo guardar el invitado.");
            }
        } else if (!invitadoDAO.actualizar(invitado)) {
            throw new IllegalArgumentException("No se encontró el invitado seleccionado.");
        }
    }

    public void eliminarInvitado(int idInvitado) throws SQLException {
        if (!invitadoDAO.eliminar(idInvitado)) {
            throw new IllegalArgumentException("No se encontró el invitado seleccionado.");
        }
    }

    public void actualizarPrecio(int idTemporada, BigDecimal precio) throws SQLException {
        if (precio == null || precio.signum() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
        if (!temporadaDAO.actualizarPrecio(idTemporada, precio)) {
            throw new IllegalArgumentException("No se encontró la temporada seleccionada.");
        }
    }
}