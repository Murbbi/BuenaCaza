package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReservaDetalle {
    private final int idReserva;
    private final String cabana;
    private final String invitado;
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;
    private final String temporada;
    private final BigDecimal montoTotal;

    public ReservaDetalle(int idReserva, String cabana, String invitado,
            LocalDate fechaEntrada, LocalDate fechaSalida, String temporada, BigDecimal montoTotal) {
        this.idReserva = idReserva;
        this.cabana = cabana;
        this.invitado = invitado;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.temporada = temporada;
        this.montoTotal = montoTotal;
    }

    public int getIdReserva() {
        return idReserva;
    }

    public String getCabana() {
        return cabana;
    }

    public String getInvitado() {
        return invitado;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public String getTemporada() {
        return temporada;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }
}
