package web;
import contratos.dto.DtUsuario;
import jakarta.servlet.http.HttpServletRequest;
/** Adaptación HTTP; la visibilidad y selección se resuelven en Sistema. */
final class AccesoEdiciones {
    private AccesoEdiciones() { }
    static DtUsuario usuario(HttpServletRequest req) { return SesionWeb.usuario(req); }
    static String texto(HttpServletRequest req,String campo) {
        String valor=req.getParameter(campo);return valor==null?"":valor.trim();
    }
}
