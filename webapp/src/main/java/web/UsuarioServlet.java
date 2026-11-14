package web;

import datatypes.*;
import web.remoto.ClienteUsuarios;
import web.remoto.ErrorRemotoUsuarios;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet("/usuario")
public class UsuarioServlet extends HttpServlet {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DtPerfilUsuario detalle = ClienteUsuarios.get().consultarPerfil(ClienteUsuarios.token(req), req.getParameter("nick"));
        DtUsuario perfil = detalle.getUsuario();
        req.setAttribute("perfil", perfil);
        req.setAttribute("esPropio", detalle.isPropio());
        req.setAttribute("fechaAlta", perfil.getFechaAlta() == null ? "No registrada" : perfil.getFechaAlta().format(FECHA));
        req.setAttribute("esAsistente", perfil instanceof DtAsistente);
        req.setAttribute("esOrganizador", perfil instanceof DtOrganizador);
        if (perfil instanceof DtAsistente a) {
            req.setAttribute("fechaNacimiento", a.getFechaNacimiento() == null ? "" : a.getFechaNacimiento().format(FECHA));
            List<RegistroVista> registros = new ArrayList<>();
            for (DtRegistro registro : detalle.getRegistros()) {
                float costo = registro.getCosto();
                registros.add(new RegistroVista(registro.getNombreEdicion(), registro.getNombreTipoRegistro(),
                        costo == Math.rint(costo) ? String.valueOf((long) costo) : String.format("%.2f", costo),
                        registro.getFechaRegistro() == null ? "" : registro.getFechaRegistro().format(FECHA)));
            }
            req.setAttribute("registros", registros);
        }
        req.setAttribute("ediciones", detalle.getEdiciones());
        if (detalle.isPropio()) MensajesFlash.mostrar(req, "/usuario");
        req.getRequestDispatcher("/WEB-INF/views/usuario.jsp").forward(req, resp);
    }
}