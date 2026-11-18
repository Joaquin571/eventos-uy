package implementacion;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/** Normaliza enlaces YouTube a una URL de reproducción controlada. */
public final class VideoYoutube {
    private static final Pattern ID = Pattern.compile("[A-Za-z0-9_-]{11}");
    private VideoYoutube() { }

    public static String normalizar(String enlace) {
        if (enlace == null || enlace.isBlank()) return null;
        if (enlace.length() > 2048) throw new IllegalArgumentException("La URL de YouTube es demasiado larga.");
        try {
            URI uri = new URI(enlace.strip());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getRawUserInfo() != null
                    || uri.getPort() != -1 || uri.getRawFragment() != null)
                throw new IllegalArgumentException("Ingresá una URL HTTPS válida de YouTube.");
            String host = uri.getHost();
            if (host == null) throw new IllegalArgumentException("Ingresá una URL válida de YouTube.");
            host = host.toLowerCase(java.util.Locale.ROOT);
            String ruta = uri.getPath();
            String id = null;
            if (host.equals("youtu.be") && ruta.matches("/[A-Za-z0-9_-]{11}")) id = ruta.substring(1);
            else if (host.equals("youtube.com") || host.equals("www.youtube.com") || host.equals("m.youtube.com")) {
                if (ruta.equals("/watch") && uri.getRawQuery() != null) {
                    for (String parametro : uri.getRawQuery().split("&")) {
                        if (parametro.startsWith("v=") && parametro.length() == 13) {
                            id = parametro.substring(2);
                            break;
                        }
                    }
                } else if (ruta.matches("/(embed|shorts|live)/[A-Za-z0-9_-]{11}"))
                    id = ruta.substring(ruta.lastIndexOf('/') + 1);
            }
            if (id == null || !ID.matcher(id).matches())
                throw new IllegalArgumentException("La URL no corresponde a un video de YouTube válido.");
            return "https://www.youtube-nocookie.com/embed/" + id;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("La URL de YouTube no es válida.", e);
        }
    }
}
