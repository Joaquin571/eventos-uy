package web;

import datatypes.DtAsistente;
import datatypes.DtEdicion;
import datatypes.DtOrganizador;
import datatypes.DtUsuario;
import datatypes.EstadoEdicion;
import datatypes.DtRegistro;
import interfaces.ISistema;
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

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String nick = req.getParameter("nick");
        ISistema sistema = ContextoListener.sistema(req);

        DtUsuario perfil = (nick == null || nick.isBlank()) ? null : sistema.consultarUsuario(nick);

        if (perfil == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "El usuario no existe.");
            return;
        }

        HttpSession sesion = req.getSession(false);
        DtUsuario actual = sesion == null ? null : (DtUsuario) sesion.getAttribute("usuario");
        boolean esPropio = actual != null && actual.getNickname().equals(perfil.getNickname());

        req.setAttribute("perfil", perfil);
        req.setAttribute("esPropio", esPropio);

        if (perfil instanceof DtAsistente asistente) {
            req.setAttribute("esAsistente", true);
            req.setAttribute("fechaNacimiento", asistente.getFechaNacimiento() == null
                    ? "" : asistente.getFechaNacimiento().format(FECHA));

            if (esPropio) {
                Collection<RegistroVista> registros = new ArrayList<>();

                for (DtRegistro registro : sistema.obtenerRegistrosAsistente(perfil.getNickname())) {
                    float costo = registro.getCosto();

                    registros.add(new RegistroVista(
                            registro.getNombreEdicion(),
                            registro.getNombreTipoRegistro(),
                            costo == Math.rint(costo) ? String.valueOf((long) costo) : String.format("%.2f", costo),
                            registro.getFechaRegistro() == null ? "" : registro.getFechaRegistro().format(FECHA)));
                }

                req.setAttribute("registros", registros);
            }
        } else if (perfil instanceof DtOrganizador) {
            req.setAttribute("esOrganizador", true);

            Collection<DtEdicion> ediciones = new ArrayList<>();
            for (DtEdicion edicion : sistema.obtenerEdicionesOrganizador(perfil.getNickname())) {
                if (esPropio || edicion.getEstado() == EstadoEdicion.ACEPTADA) {
                    ediciones.add(edicion);
                }
            }
            req.setAttribute("ediciones", ediciones);
        }

        req.getRequestDispatcher("/WEB-INF/views/usuario.jsp").forward(req, resp);
    }
}