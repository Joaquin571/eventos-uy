package web;

import datatypes.DtImagen;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;

final class Subidas {

    private static final long TAMANIO_MAXIMO = 2L * 1024 * 1024;

    private Subidas() {
    }

    // Devuelve null si no se eligió ningún archivo.
    static DtImagen leerImagen(HttpServletRequest req, String campo)
            throws IOException, ServletException {

        Part parte = req.getPart(campo);

        if (parte == null || parte.getSize() == 0) {
            return null;
        }

        if (parte.getSize() > TAMANIO_MAXIMO) {
            throw new IllegalArgumentException("La imagen supera el tamaño máximo de 2 MB.");
        }

        try (InputStream entrada = parte.getInputStream()) {
            return new DtImagen(parte.getContentType(), entrada.readAllBytes());
        }
    }
}