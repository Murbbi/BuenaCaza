package archivo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;

// Clase abstracta que sabe armar un archivo de texto con columnas de ancho fijo.
// No sabe qué datos va a exportar: eso lo decide cada clase hija con el método valores().
public abstract class ExportadorTexto<T> {
    // Arreglos de tamaño fijo: los títulos y los anchos de las columnas no cambian nunca
    private final String[] titulos;
    private final int[] anchos;

    protected ExportadorTexto(String[] titulos, int[] anchos) {
        if (titulos.length != anchos.length) {
            throw new IllegalArgumentException("Cada columna necesita un título y un ancho.");
        }
        this.titulos = titulos;
        this.anchos = anchos;
    }

    // Cada clase hija devuelve los datos de un elemento, en el mismo orden que los títulos
    protected abstract String[] valores(T elemento);

    // Escribe el archivo y devuelve cuántos registros se exportaron
    public int exportar(List<T> datos, File destino) throws IOException {
        int cantidad = 0;
        // try-with-resources: el archivo se cierra solo aunque haya un error
        try (BufferedWriter escritor = Files.newBufferedWriter(destino.toPath(), StandardCharsets.UTF_8)) {
            escritor.write(armarLinea(titulos));
            escritor.newLine();
            escritor.write("-".repeat(anchoTotal()));
            escritor.newLine();

            // Se recorre el ArrayList con un iterador (hasNext / next)
            Iterator<T> iterador = datos.iterator();
            while (iterador.hasNext()) {
                escritor.write(armarLinea(valores(iterador.next())));
                escritor.newLine();
                cantidad++;
            }
        }
        return cantidad;
    }

    // Junta todos los valores de una fila, cada uno ocupando el ancho de su columna
    private String armarLinea(String[] valores) {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < anchos.length; i++) {
            linea.append(ajustar(valores[i], anchos[i]));
        }
        return linea.toString().stripTrailing();
    }

    // Si el texto es más largo se corta, si es más corto se completa con espacios
    private String ajustar(String valor, int ancho) {
        String texto = valor == null ? "" : valor.replace('\n', ' ').trim();
        if (texto.length() >= ancho) {
            texto = texto.substring(0, ancho - 1);
        }
        return String.format("%-" + ancho + "s", texto);
    }

    private int anchoTotal() {
        int total = 0;
        for (int ancho : anchos) {
            total += ancho;
        }
        return total;
    }
}
