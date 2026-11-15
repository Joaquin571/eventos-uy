package web;
import contratos.dto.*;
import jakarta.servlet.http.*;
/** La credencial P03 permanece en HttpSession y nunca se expone como atributo de vista. */
final class SesionWeb {
    static final String CREDENCIAL="_credencialCentral";
    private SesionWeb() { }
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
