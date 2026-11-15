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
                            registro.getFechaRegistro() == null ? "" : registro.getFechaRegistro().format(FECHA)));
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
