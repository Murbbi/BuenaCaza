package controlador;

import dao.InvitadoDAO;
import dao.ReservaDAO;
import dao.TemporadaDAO;
import modelo.Invitado;
import modelo.ReservaDetalle;
import modelo.Temporada;
import modelo.TemporadaTipo;

import java.sql.SQLException;
import java.util.List;

public class AdminController {
    private InvitadoDAO invitadoDAO = new InvitadoDAO();
    private TemporadaDAO temporadaDAO = new TemporadaDAO();
    private ReservaDAO reservaDAO = new ReservaDAO();

    public List<Invitado> listarInvitados() throws SQLException {
        return invitadoDAO.listar();
    }

    public List<Temporada> listarTemporadas() throws SQLException {
        return temporadaDAO.listar();
    }

    public List<TemporadaTipo> listarTiposTemporada() throws SQLException {
        return temporadaDAO.listarTipos();
    }

    public List<ReservaDetalle> listarReservas() throws SQLException {
        return reservaDAO.listarDetalles();
    }

    // Si el id es 0 es un invitado nuevo, si no se actualiza el que ya existe
    public void guardarInvitado(Invitado invitado) throws SQLException {
        if (invitado.getIdInvitado() == 0) {
            invitadoDAO.insertar(invitado);
        } else {
            invitadoDAO.actualizar(invitado);
        }
    }

    public void eliminarInvitado(Invitado invitado) throws SQLException {
        if (invitado.isComunidad()) {
            throw new IllegalArgumentException("No se puede eliminar un invitado que tiene reservas.");
        }
        invitadoDAO.eliminar(invitado.getIdInvitado());
    }

    public void guardarTemporada(Temporada temporada) throws SQLException {
        if (!temporada.getFechaFinal().isAfter(temporada.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha final debe ser posterior a la fecha de inicio.");
        }
        if (temporada.getPrecioPorNoche().signum() <= 0) {
            throw new IllegalArgumentException("El precio por noche debe ser mayor que cero.");
        }
        if (temporadaDAO.seSuperpone(temporada)) {
            throw new IllegalArgumentException("Las fechas se pisan con otra temporada ya cargada.");
        }

        if (temporada.getIdTemporada() == 0) {
            temporadaDAO.insertar(temporada);
        } else {
            temporadaDAO.actualizar(temporada);
        }
    }

    public void eliminarTemporada(int idTemporada) throws SQLException {
        if (temporadaDAO.tieneReservas(idTemporada)) {
            throw new IllegalArgumentException("No se puede eliminar una temporada que tiene reservas.");
        }
        temporadaDAO.eliminar(idTemporada);
    }
}
