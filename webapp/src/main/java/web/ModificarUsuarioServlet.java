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

@WebServlet("/modificar-usuario")
@MultipartConfig(maxFileSize = 2L * 1024 * 1024, maxRequestSize = 3L * 1024 * 1024)
public class ModificarUsuarioServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DtUsuario perfil = ClienteUsuarios.get().usuarioActual(ClienteUsuarios.token(req));
        Map<String, String> form = new HashMap<>();
        form.put("nombre", perfil.getNombre());
        if (perfil instanceof DtAsistente a) {
            form.put("apellido", a.getApellido());
            form.put("fechaNac", Objects.toString(a.getFechaNacimiento(), ""));
            form.put("institucion", Objects.toString(a.getNombreInstitucion(), ""));
        } else if (perfil instanceof DtOrganizador o) {
            form.put("descripcion", o.getDescripcion());
            form.put("sitioWeb", Objects.toString(o.getSitioWeb(), ""));
        }
        mostrar(req, resp, perfil, form, null);
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DtUsuario perfil = ClienteUsuarios.get().usuarioActual(ClienteUsuarios.token(req));
        Map<String, String> form = FormularioUsuario.leer(req);
        try {
            DtUsuario actualizado = ClienteUsuarios.get().modificarUsuario(ClienteUsuarios.token(req),
                    FormularioUsuario.datos(req, form, perfil), Subidas.leerImagen(req, "imagen"));
            req.getSession().setAttribute("usuario", actualizado);
            MensajesFlash.guardar(req, "/usuario", "Los datos del perfil se guardaron.", null);
            resp.sendRedirect(req.getContextPath() + "/usuario?nick=" + URLEncoder.encode(actualizado.getNickname(), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            FormularioUsuario.verificarError(e);
            mostrar(req, resp, perfil, form, e.getMessage());
        }
    }
    private void mostrar(HttpServletRequest req, HttpServletResponse resp, DtUsuario perfil, Map<String, String> form, String error)
            throws ServletException, IOException {
        req.setAttribute("perfil", perfil);
        req.setAttribute("esAsistente", perfil instanceof DtAsistente);
        req.setAttribute("esOrganizador", perfil instanceof DtOrganizador);
        req.setAttribute("form", form);
        req.setAttribute("error", error);
        req.setAttribute("instituciones", ClienteUsuarios.get().listarInstituciones());
        req.getRequestDispatcher("/WEB-INF/views/modificar-usuario.jsp").forward(req, resp);
    }
}