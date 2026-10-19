package implementacion;

import datatypes.DtImagen;

import java.util.Set;

/**
 * Reglas de las imágenes que acepta el sistema: formato, tamaño y que el
 * contenido corresponda realmente al formato indicado.
 */
final class ValidadorImagen {

    static final int TAMANIO_MAXIMO = 2 * 1024 * 1024;

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/png", "image/jpeg", "image/gif", "image/webp");

    private ValidadorImagen() {
    }

    static void validar(DtImagen imagen) {
        if (imagen == null || imagen.getContenido() == null || imagen.getContenido().length == 0) {
            throw new IllegalArgumentException("La imagen está vacía.");
        }

        String tipo = imagen.getTipoContenido() == null
                ? "" : imagen.getTipoContenido().toLowerCase();

        if (!TIPOS_PERMITIDOS.contains(tipo)) {
            throw new IllegalArgumentException(
                    "Formato de imagen no soportado. Use PNG, JPG, GIF o WEBP.");
        }

        byte[] contenido = imagen.getContenido();

        if (contenido.length > TAMANIO_MAXIMO) {
            throw new IllegalArgumentException("La imagen supera el tamaño máximo de 2 MB.");
        }

        if (!coincideConFormato(tipo, contenido)) {
            throw new IllegalArgumentException(
                    "El archivo no es una imagen válida del formato indicado.");
        }
    }

    private static boolean coincideConFormato(String tipo, byte[] c) {
        return switch (tipo) {
            case "image/png" -> empieza(c, 0x89, 0x50, 0x4E, 0x47);
            case "image/jpeg" -> empieza(c, 0xFF, 0xD8, 0xFF);
            case "image/gif" -> empieza(c, 'G', 'I', 'F', '8');
            case "image/webp" -> c.length >= 12
                    && empieza(c, 'R', 'I', 'F', 'F')
                    && c[8] == 'W' && c[9] == 'E' && c[10] == 'B' && c[11] == 'P';
            default -> false;
        };
    }

    private static boolean empieza(byte[] contenido, int... firma) {
        if (contenido.length < firma.length) {
            return false;
        }

        for (int i = 0; i < firma.length; i++) {
            if ((contenido[i] & 0xFF) != firma[i]) {
                return false;
            }
        }
        return true;
    }
}