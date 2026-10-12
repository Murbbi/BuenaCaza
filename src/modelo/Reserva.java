package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Reserva {
    private int idReserva;
    private int idCabana;
    private int idInvitado;
    private int idTemporada;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private BigDecimal montoTotal;
    private String comentario;
    // private Invitado invitado; // Relación con la clase Invitado
    // private Cabana cabana; // Relación con la clase Cabana
    // private Temporada temporada; // Relación con la clase Temporada


    public Reserva() {
    }

    public Reserva(int idReserva, int idCabana, int idInvitado, int idTemporada,
            LocalDate fechaEntrada, LocalDate fechaSalida, BigDecimal montoTotal, String comentario) {
        this.idReserva = idReserva;
        this.idCabana = idCabana;
        this.idInvitado = idInvitado;
        this.idTemporada = idTemporada;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.montoTotal = montoTotal;
        this.comentario = comentario;
    }

    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public int getIdCabana() {
        return idCabana;
    }

    public void setIdCabana(int idCabana) {
        this.idCabana = idCabana;
    }

    public int getIdInvitado() {
        return idInvitado;
    }

    public void setIdInvitado(int idInvitado) {
        this.idInvitado = idInvitado;
    }

    public int getIdTemporada() {
        return idTemporada;
    }

    public void setIdTemporada(int idTemporada) {
        this.idTemporada = idTemporada;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(LocalDate fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    @Override
    public String toString() {
        return "Reserva " + idReserva + " - Cabaña " + idCabana + ", Invitado " + idInvitado
                + ", Temporada " + idTemporada + " (" + fechaEntrada + " a " + fechaSalida + ")";
    }
}