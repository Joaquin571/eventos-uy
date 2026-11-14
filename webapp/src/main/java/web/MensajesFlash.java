package web;

import jakarta.servlet.http.HttpServletRequest;

/** Mensajes de una sola lectura, separados por pantalla y sin parámetros en la URL. */
final class MensajesFlash {
    private record Mensaje(String exito, String advertencia) { }

    private MensajesFlash() { }

    static void guardar(HttpServletRequest req, String pantalla, String exito, String advertencia) {
        req.getSession().setAttribute(MensajesFlash.class.getName() + pantalla, new Mensaje(exito, advertencia));
    }

    static void mostrar(HttpServletRequest req, String pantalla) {
        if (!"GET".equals(req.getMethod())) return;
        var sesion = req.getSession(false);
        if (sesion == null) return;
        String clave = MensajesFlash.class.getName() + pantalla;
        Object valor;
        synchronized (sesion) {
            valor = sesion.getAttribute(clave);
            sesion.removeAttribute(clave);
        }
        if (valor instanceof Mensaje mensaje) {
            req.setAttribute("exito", mensaje.exito());
            req.setAttribute("advertencia", mensaje.advertencia());
        }
    }
}
