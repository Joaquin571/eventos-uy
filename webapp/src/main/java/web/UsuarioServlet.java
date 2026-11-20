package web;

import contratos.dto.DtAsistente;
import contratos.dto.DtEdicion;
import contratos.dto.DtOrganizador;
import contratos.dto.DtUsuario;
import contratos.dto.EstadoEdicion;
import contratos.dto.DtRegistro;
import contratos.ServicioPortal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

@WebServlet("/usuario")
public class UsuarioServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!Csrf.validarPost(req, resp)) return;
        if (SesionWeb.credencial(req) == null) { resp.sendError(403); return; }
        String nick = req.getParameter("nick");
        String accion = req.getParameter("accion");
        if (nick == null || nick.isBlank()) { resp.sendError(400); return; }
        ServicioPortal sistema = ContextoListener.central(req);
        if ("seguir".equals(accion)) sistema.seguirUsuario(nick);
        else if ("dejar-de-seguir".equals(accion)) sistema.dejarDeSeguirUsuario(nick);
        else { resp.sendError(400); return; }
        resp.sendRedirect(req.getContextPath() + "/usuario?nick="
                + java.net.URLEncoder.encode(nick, java.nio.charset.StandardCharsets.UTF_8));
    }

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String nick = req.getParameter("nick");
        ServicioPortal sistema = ContextoListener.central(req);

        var consulta=sistema.consultarPerfil(nick);
        DtUsuario perfil=consulta.getUsuario();boolean esPropio=consulta.isPropio();
        req.setAttribute("fechaAlta", perfil.getFechaAlta() == null
                ? "No registrada" : perfil.getFechaAlta().format(FECHA));
        req.setAttribute("perfil", perfil);
        req.setAttribute("esPropio", esPropio);
        req.setAttribute("relaciones", sistema.consultarRelaciones(nick));
        req.setAttribute("puedeSeguir", !esPropio && SesionWeb.credencial(req) != null);
        if (!esPropio && SesionWeb.credencial(req) != null) Csrf.prepararFormulario(req);

        if (perfil instanceof DtAsistente asistente) {
            req.setAttribute("esAsistente", true);
            req.setAttribute("fechaNacimiento", asistente.getFechaNacimiento() == null
                    ? "" : asistente.getFechaNacimiento().format(FECHA));

            if (esPropio) {
                Collection<RegistroVista> registros = new ArrayList<>();

                for (DtRegistro registro : consulta.getRegistros()) {
                    float costo = registro.getCosto();

                    registros.add(new RegistroVista(
                            registro.getNombreEdicion(),
                            registro.getNombreTipoRegistro(),
                            costo == Math.rint(costo) ? String.valueOf((long) costo) : String.format("%.2f", costo),
                            registro.getFechaRegistro() == null ? "" : registro.getFechaRegistro().format(FECHA),
                            registro.isAsistenciaConfirmada()));
                }

                req.setAttribute("registros", registros);
            }
        } else if (perfil instanceof DtOrganizador) {
            req.setAttribute("esOrganizador", true);

            var ediciones=consulta.getEdiciones();
            req.setAttribute("ediciones", ediciones);
        }

        if (esPropio) MensajesFlash.mostrar(req, "/usuario");
        req.getRequestDispatcher("/WEB-INF/views/usuario.jsp").forward(req, resp);
    }
}
