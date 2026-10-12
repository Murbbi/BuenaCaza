package controlador;

import dao.CabanaDAO;
import dao.ReservaDAO;
import dao.TemporadaDAO;
import modelo.Cabana;
import modelo.Reserva;
import modelo.ReservaDetalle;
import modelo.Temporada;
import modelo.Invitado;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReservaController {
    private final CabanaDAO cabanaDAO;
    private final TemporadaDAO temporadaDAO;
    private final ReservaDAO reservaDAO;

    public ReservaController() {
        this(new CabanaDAO(), new TemporadaDAO(), new ReservaDAO());
    }

    public ReservaController(CabanaDAO cabanaDAO, TemporadaDAO temporadaDAO, ReservaDAO reservaDAO) {
        this.cabanaDAO = cabanaDAO;
        this.temporadaDAO = temporadaDAO;
        this.reservaDAO = reservaDAO;
    }

    public List<Cabana> listarCabanas() throws SQLException {
        return cabanaDAO.listar().stream()
                .filter(cabana -> "libre".equalsIgnoreCase(cabana.getEstado()))
                .toList();
    }

    public List<ReservaDetalle> listarReservasDelInvitado(int idInvitado) throws SQLException {
        return reservaDAO.listarDetalles(idInvitado);
    }

    public BigDecimal cotizar(Invitado invitado, int idCabana, LocalDate entrada, LocalDate salida)
            throws SQLException {
        validarFechas(entrada, salida);
        validarCabana(idCabana);
        if (!reservaDAO.estaDisponible(idCabana, entrada, salida)) {
            throw new IllegalArgumentException("La cabaña no está libre para esas fechas.");
        }
        return calcularTotal(invitado, temporadaParaFechas(entrada, salida), entrada, salida);
    }

    public BigDecimal crearReserva(Invitado invitado, int idCabana, LocalDate entrada,
            LocalDate salida, String comentario) throws SQLException {
        validarFechas(entrada, salida);
        validarCabana(idCabana);
        Temporada temporada = temporadaParaFechas(entrada, salida);
        BigDecimal total = calcularTotal(invitado, temporada, entrada, salida);
        Reserva reserva = new Reserva(0, idCabana, invitado.getIdInvitado(), temporada.getIdTemporada(),
                entrada, salida, total, comentario == null || comentario.isBlank() ? null : comentario.trim());
        if (!reservaDAO.insertarSiDisponible(reserva)) {
            throw new IllegalStateException("No se pudo guardar la reserva.");
        }
        return total;
    }

    private void validarCabana(int idCabana) {
        if (idCabana < 1) {
            throw new IllegalArgumentException("Seleccioná una cabaña disponible.");
        }
    }

    private Temporada temporadaParaFechas(LocalDate entrada, LocalDate salida) throws SQLException {
        Temporada temporada = temporadaDAO.buscarPorFecha(entrada);
        if (temporada == null || salida.isAfter(temporada.getFechaFinal())) {
            throw new IllegalArgumentException(
                    "Las fechas deben quedar dentro de una temporada configurada.");
        }
        return temporada;
    }

    private BigDecimal calcularTotal(Invitado invitado, Temporada temporada,
            LocalDate entrada, LocalDate salida) throws SQLException {
        if (temporada.getPrecioPorNoche() == null) {
            throw new IllegalStateException("La temporada no tiene un precio configurado.");
        }
        long noches = ChronoUnit.DAYS.between(entrada, salida);
        BigDecimal total = temporada.getPrecioPorNoche().multiply(BigDecimal.valueOf(noches));
        if (invitado.isComunidad()) {
            total = total.multiply(new BigDecimal("0.90"));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private void validarFechas(LocalDate entrada, LocalDate salida) {
        if (entrada == null || salida == null || !salida.isAfter(entrada)) {
            throw new IllegalArgumentException("La salida debe ser posterior a la fecha de entrada.");
        }
        if (entrada.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de entrada no puede estar en el pasado.");
        }
    }
}