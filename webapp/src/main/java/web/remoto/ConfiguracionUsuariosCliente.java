package web.remoto;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

final class ConfiguracionUsuariosCliente {
    private ConfiguracionUsuariosCliente() { }
    static String direccion() {
        try {
            Path archivo = Path.of(System.getProperty("user.home"), ".eventosUy", "usuarios-web.properties");
            if (!Files.exists(archivo)) {
                Files.createDirectories(archivo.getParent());
                try (InputStream plantilla = ConfiguracionUsuariosCliente.class.getResourceAsStream("/usuarios-web.properties")) {
                    if (plantilla == null) throw new IOException("Falta la plantilla web.");
                    Files.copy(plantilla, archivo);
                }
            }
            Properties p = new Properties();
            try (InputStream entrada = Files.newInputStream(archivo)) { p.load(entrada); }
            String host = p.getProperty("host", "");
            int puerto = Integer.parseInt(p.getProperty("puerto", ""));
            String ruta = p.getProperty("ruta", "");
            if (host.isBlank() || puerto < 1 || puerto > 65535 || !ruta.startsWith("/")) throw new IOException("Dirección inválida.");
            return "http://" + host + ":" + puerto + ruta;
        } catch (IOException | NumberFormatException e) {
            throw new IllegalStateException("Revisá ~/.eventosUy/usuarios-web.properties.", e);
        }
    }
}