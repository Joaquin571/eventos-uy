package web.soap;

import jakarta.xml.ws.WebServiceException;

import javax.net.ssl.SSLHandshakeException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.util.List;

import static web.soap.PruebaHttpsServidor.variable;

/** Invoca el proxy generado con wsimport desde una JVM separada. */
public final class PruebaHttpsCliente {
    private PruebaHttpsCliente() { }

    public static void main(String[] args) {
        String esperado = args.length == 0 ? "exito" : args[0];
        if (args.length > 1 || !List.of("exito", "sin-confianza", "nombre-incorrecto").contains(esperado)) {
            throw new IllegalArgumentException("Usar: exito | sin-confianza | nombre-incorrecto");
        }

        URI destino = URI.create(variable("SOAP_HTTPS_URL"));
        if (!"https".equalsIgnoreCase(destino.getScheme()) || destino.getHost() == null
                || destino.getRawUserInfo() != null || destino.getRawQuery() != null
                || destino.getRawFragment() != null) {
            throw new IllegalArgumentException("SOAP_HTTPS_URL debe ser una URL HTTPS de invocación.");
        }

        Path confianza = Path.of(variable("SOAP_HTTPS_TRUSTSTORE")).toAbsolutePath();
        if (!Files.isRegularFile(confianza)) {
            throw new IllegalArgumentException("SOAP_HTTPS_TRUSTSTORE debe apuntar a un archivo.");
        }
        System.setProperty("javax.net.ssl.trustStore", confianza.toString());
        System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");
        System.setProperty("javax.net.ssl.trustStorePassword", variable("SOAP_HTTPS_TRUSTSTORE_PASSWORD"));

        try {
            var eventos = new ClienteEventosCentral(destino).listarEventos();
            if (!"exito".equals(esperado)) {
                throw new AssertionError("Se esperaba un rechazo TLS: " + esperado);
            }
            if (eventos.size() != 1) throw new AssertionError("Se esperaba un solo evento.");
            var evento = eventos.getFirst();
            if (!"Encuentro HTTPS ñ".equals(evento.getNombre()) || !"TLS".equals(evento.getSigla())
                    || !"Prueba SOAP sobre TLS".equals(evento.getDescripcion())
                    || !"2026-11-05".equals(evento.getFechaAlta())
                    || !List.of("Arte").equals(evento.getCategorias())) {
                throw new AssertionError("Los datos del evento recibido no coinciden.");
            }
            System.out.println("OK HTTPS: listarEventos devolvió el evento esperado.");
        } catch (WebServiceException error) {
            if (!rechazoEsperado(error, esperado)) throw error;
            System.out.println("OK rechazo TLS: " + esperado);
        }
    }

    private static boolean rechazoEsperado(Throwable error, String esperado) {
        if (!tieneCausa(error, SSLHandshakeException.class)) return false;
        if ("sin-confianza".equals(esperado)) {
            return tieneCausa(error, CertPathBuilderException.class)
                    || tieneCausa(error, CertPathValidatorException.class);
        }
        if ("nombre-incorrecto".equals(esperado)) {
            // Diagnósticos del proveedor JSSE de Java 25. No aceptar un fallo de confianza como
            // prueba de hostname: se requiere el error de certificado por identidad del servidor.
            for (Throwable causa = error; causa != null; causa = causa.getCause()) {
                if (causa instanceof CertificateException && causa.getMessage() != null
                        && (causa.getMessage().startsWith("No subject alternative")
                            || causa.getMessage().startsWith("No name matching "))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean tieneCausa(Throwable error, Class<? extends Throwable> tipo) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (tipo.isInstance(causa)) return true;
        }
        return false;
    }
}
