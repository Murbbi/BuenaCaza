package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Temporada {
    private int idTemporada;
    private int idTemporadaTipo;
    private LocalDate fechaInicio;
    private LocalDate fechaFinal;
    private BigDecimal precioPorNoche;
    private String tipo;

    public Temporada() {
    }

    public Temporada(int idTemporada, int idTemporadaTipo, LocalDate fechaInicio,
            LocalDate fechaFinal, BigDecimal precioPorNoche) {
        this.idTemporada = idTemporada;
        this.idTemporadaTipo = idTemporadaTipo;
        this.fechaInicio = fechaInicio;
        this.fechaFinal = fechaFinal;
        this.precioPorNoche = precioPorNoche;
    }

    public Temporada(int idTemporada, int idTemporadaTipo, LocalDate fechaInicio,
            LocalDate fechaFinal, BigDecimal precioPorNoche, String tipo) {
        this(idTemporada, idTemporadaTipo, fechaInicio, fechaFinal, precioPorNoche);
        this.tipo = tipo;
    }

    public int getIdTemporada() {
        return idTemporada;
    }

    public void setIdTemporada(int idTemporada) {
        this.idTemporada = idTemporada;
    }

    public int getIdTemporadaTipo() {
        return idTemporadaTipo;
    }

    public void setIdTemporadaTipo(int idTemporadaTipo) {
        this.idTemporadaTipo = idTemporadaTipo;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(LocalDate fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    public BigDecimal getPrecioPorNoche() {
        return precioPorNoche;
    }

    public void setPrecioPorNoche(BigDecimal precioPorNoche) {
        this.precioPorNoche = precioPorNoche;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return idTemporada + " - Temporada tipo " + idTemporadaTipo
                + " (" + fechaInicio + " hasta " + fechaFinal + ", precio por noche: " + precioPorNoche + ")";
    }
}