package modelo;

public class TemporadaTipo {
    private int idTemporadaTipo;
    private String tipo;

    public int getIdTemporadaTipo() {
        return idTemporadaTipo;
    }

    public void setIdTemporadaTipo(int idTemporadaTipo) {
        this.idTemporadaTipo = idTemporadaTipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    // Así el ComboBox muestra "Alta", "Media" o "Baja"
    @Override
    public String toString() {
        return tipo;
    }
}