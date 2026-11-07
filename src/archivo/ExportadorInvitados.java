package archivo;

import modelo.Invitado;

import java.util.List;

// Exporta los clientes (invitados) del complejo a un archivo de texto con columnas fijas
public class ExportadorInvitados extends ExportadorTexto<Invitado> {
    private static final String[] TITULOS = {
            "ID", "DNI", "NOMBRE", "CORREO", "TELEFONO", "DIRECCION", "ROL", "CON RESERVAS" };
    private static final int[] ANCHOS = { 6, 11, 30, 35, 16, 40, 15, 14 };

    // Ids de los invitados que ya tienen reservas (para la columna "CON RESERVAS")
    private final List<Integer> conReservas;

    public ExportadorInvitados(List<Integer> conReservas) {
        super(TITULOS, ANCHOS);
        this.conReservas = conReservas;
    }

    @Override
    protected String[] valores(Invitado invitado) {
        return new String[] {
                String.valueOf(invitado.getIdInvitado()),
                String.valueOf(invitado.getDni()),
                invitado.getNombre(),
                invitado.getEmail(),
                invitado.getTelefono(),
                invitado.getDireccion(),
                invitado.getRol(),
                conReservas.contains(invitado.getIdInvitado()) ? "Si" : "No" };
    }
}
