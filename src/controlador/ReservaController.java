package controlador;

import dao.CabanaDAO;
import dao.ReservaDAO;
import dao.TemporadaDAO;
import modelo.Cabana;
import modelo.Invitado;
import modelo.Reserva;
import modelo.Temporada;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReservaController {
    // Datos que se muestran en el resumen del presupuesto
    public record Presupuesto(long noches, String temporada, BigDecimal precioPorNoche,
            BigDecimal subtotal, BigDecimal descuento, BigDecimal total, boolean aplicaDescuento) {
    }

    private CabanaDAO cabanaDAO = new CabanaDAO();
    private TemporadaDAO temporadaDAO = new TemporadaDAO();
    private ReservaDAO reservaDAO = new ReservaDAO();

    public List<Cabana> listarCabanas() throws SQLException {
        return cabanaDAO.listar().stream()
                .filter(cabana -> "libre".equalsIgnoreCase(cabana.getEstado()))
                .toList();
    }

    public Presupuesto cotizarPresupuesto(Invitado invitado, int idCabana,
            LocalDate entrada, LocalDate salida) throws SQLException {
        validarFechas(entrada, salida);
        if (!reservaDAO.estaDisponible(idCabana, entrada, salida)) {
            throw new IllegalArgumentException("La cabaña no está libre para esas fechas.");
        }
        Temporada temporada = buscarTemporada(entrada, salida);
        boolean aplicaDescuento = invitado.isComunidad();

        long noches = ChronoUnit.DAYS.between(entrada, salida);
        BigDecimal subtotal = temporada.getPrecioPorNoche().multiply(BigDecimal.valueOf(noches));
        BigDecimal total = calcularTotal(temporada, entrada, salida, aplicaDescuento);
        BigDecimal descuento = subtotal.subtract(total);
        return new Presupuesto(noches, temporada.getTipo(), temporada.getPrecioPorNoche(),
                subtotal, descuento, total, aplicaDescuento);
    }

    public BigDecimal crearReserva(Invitado invitado, int idCabana, LocalDate entrada,
            LocalDate salida, String comentario) throws SQLException {
        validarFechas(entrada, salida);
        if (!reservaDAO.estaDisponible(idCabana, entrada, salida)) {
            throw new IllegalArgumentException("La cabaña no está libre para esas fechas.");
        }
        Temporada temporada = buscarTemporada(entrada, salida);
        BigDecimal total = calcularTotal(temporada, entrada, salida, invitado.isComunidad());

        Reserva reserva = new Reserva(0, idCabana, invitado.getIdInvitado(),
                temporada.getIdTemporada(), entrada, salida, total, comentario);
        if (!reservaDAO.insertar(reserva)) {
            throw new IllegalStateException("No se pudo guardar la reserva.");
        }
        return total;
    }

    // La estadía tiene que entrar completa dentro de una temporada
    private Temporada buscarTemporada(LocalDate entrada, LocalDate salida) throws SQLException {
        Temporada temporada = temporadaDAO.buscarPorFecha(entrada);
        if (temporada == null || salida.isAfter(temporada.getFechaFinal())) {
            throw new IllegalArgumentException("Las fechas deben quedar dentro de una temporada configurada.");
        }
        return temporada;
    }

    private BigDecimal calcularTotal(Temporada temporada, LocalDate entrada, LocalDate salida,
            boolean aplicaDescuento) {
        long noches = ChronoUnit.DAYS.between(entrada, salida);
        BigDecimal total = temporada.getPrecioPorNoche().multiply(BigDecimal.valueOf(noches));
        if (aplicaDescuento) {
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
