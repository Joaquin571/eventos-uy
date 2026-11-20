package web;
import contratos.dto.*;
import jakarta.servlet.http.*;
/** La credencial P03 permanece en HttpSession y nunca se expone como atributo de vista. */
public final class SesionWeb {
    static final String CREDENCIAL="_credencialCentral";
    private SesionWeb() { }

    /** Para el controlador movil: validar CSRF con el filtro antes de invocar. */
    public static void iniciarMovil(HttpServletRequest req, String identificador, String contrasenia) {
        var inicio = ContextoListener.sesiones(req).iniciarMovil(identificador, contrasenia);
        if (!"ASISTENTE".equals(inicio.rol())) {
            ContextoListener.sesiones(req).cerrar(inicio.credencial());
            throw new web.remoto.ErrorCentral("ACCESO_DENEGADO", "El acceso movil requiere un asistente.");
        }
        try { cerrar(req); }
        catch (RuntimeException error) {
            try { ContextoListener.sesiones(req).cerrar(inicio.credencial()); }
            catch (RuntimeException cierre) { error.addSuppressed(cierre); }
            throw error;
        }
        try {
            var sesion = req.getSession(true);
            var usuario = new DtAsistente();
            usuario.setNickname(inicio.nickname()); usuario.setNombre(inicio.nombre());
            guardar(sesion, usuario);
            sesion.setAttribute(CREDENCIAL, inicio.credencial());
        } catch (RuntimeException error) {
            try { ContextoListener.sesiones(req).cerrar(inicio.credencial()); }
            catch (RuntimeException cierre) { error.addSuppressed(cierre); }
            var sesion = req.getSession(false);
            if (sesion != null) sesion.invalidate();
            throw error;
        }
    }

    public static DtAsistente usuarioMovilActual(HttpServletRequest req) {
        return ContextoListener.central(req).usuarioMovilActual();
    }

    /** Revoca la credencial P03 y siempre invalida la sesion local. */
    public static void cerrar(HttpServletRequest req) {
        var sesion = req.getSession(false);
        String token = credencial(req);
        try { if (token != null) ContextoListener.sesiones(req).cerrar(token); }
        finally { if (sesion != null) sesion.invalidate(); }
    }
    static String credencial(HttpServletRequest req) {
        var sesion=req.getSession(false);
        return sesion==null?null:(String)sesion.getAttribute(CREDENCIAL);
    }
    static DtUsuario usuario(HttpServletRequest req) {
        var sesion=req.getSession(false);Object valor=sesion==null?null:sesion.getAttribute("usuario");
        return valor instanceof DtUsuario u?u:null;
    }
    static void guardar(HttpSession sesion, DtUsuario usuario) {
        DtUsuario minimo=usuario instanceof DtOrganizador?new DtOrganizador():new DtAsistente();
        minimo.setNickname(usuario.getNickname());minimo.setNombre(usuario.getNombre());
        sesion.setAttribute("usuario", minimo);
    }
}
