package modelo;

public class Cabana {
    private int idCabana;
    private String nombre;
    private int capacidad;
    private String estado;

    public Cabana() {
    }

    public Cabana(int idCabana, String nombre, int capacidad, String estado) {
        this.idCabana = idCabana;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    public int getIdCabana() {
        return idCabana;
    }

    public void setIdCabana(int idCabana) {
        this.idCabana = idCabana;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return idCabana + " - " + nombre + " (capacidad: " + capacidad + ", " + estado + ")";
    }
}