package web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Token aleatorio por sesión. Nunca se registra ni se transporta en una URL. */
final class Csrf {
    static final String PARAMETRO = "_csrf";
    private static final String ATRIBUTO = Csrf.class.getName() + ".token";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Csrf() { }

    static boolean validarPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession sesion = req.getSession(false);
        Object esperado = sesion == null ? null : sesion.getAttribute(ATRIBUTO);
        if (!(esperado instanceof String token) || tokenEnUrl(req.getQueryString())) return rechazar(resp);
        // Tomcat parsea también multipart cuando el servlet destino tiene @MultipartConfig.
        // No se usa getInputStream/getReader y el servlet conserva acceso a sus Part.
        String[] recibidos;
        try {
            recibidos = req.getParameterValues(PARAMETRO);
        } catch (IllegalStateException e) {
            // Tomcat aplica los límites multipart al parsear los parámetros, antes del Servlet.
            resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                    "El formulario o la imagen supera el tamaño permitido.");
            return false;
        }
        if (recibidos == null || recibidos.length != 1 || recibidos[0] == null
                || recibidos[0].length() != token.length()
                || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8),
                        recibidos[0].getBytes(StandardCharsets.UTF_8))) return rechazar(resp);
        req.setAttribute("csrfToken", token);
        return true;
    }

    static void prepararFormulario(HttpServletRequest req) {
        HttpSession sesion = req.getSession(true);
        String token;
        synchronized (sesion) {
            Object existente = sesion.getAttribute(ATRIBUTO);
            if (existente instanceof String valor) {
                token = valor;
            } else {
                byte[] bytes = new byte[32]; ALEATORIO.nextBytes(bytes);
                token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                sesion.setAttribute(ATRIBUTO, token);
            }
        }
        req.setAttribute("csrfToken", token);
    }

    private static boolean tokenEnUrl(String query) {
        if (query == null) return false;
        try {
            for (String par : query.split("&")) {
                String nombre = URLDecoder.decode(par.split("=", 2)[0], StandardCharsets.UTF_8);
                if (PARAMETRO.equals(nombre)) return true;
            }
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static boolean rechazar(HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                "El formulario expiró o no es válido. Volvé a abrirlo e intentá nuevamente.");
        return false;
    }
}
