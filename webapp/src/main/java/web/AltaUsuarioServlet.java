package web;

import contratos.dto.DtAsistente;
import contratos.dto.DtImagen;
import contratos.dto.DtOrganizador;
import contratos.dto.DtUsuario;
import contratos.dto.TipoEntidadImagen;
import contratos.ServicioPortal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/alta-usuario")
@MultipartConfig(maxFileSize = 2L * 1024 * 1024, maxRequestSize = 3L * 1024 * 1024)
public class AltaUsuarioServlet extends HttpServlet {

    private static final String[] CAMPOS = {
            "tipo", "nickname", "nombre", "correo", "apellido",
            "fechaNac", "institucion", "descripcion", "sitioWeb"
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        mostrarFormulario(req, resp, new HashMap<>(), null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Map<String, String> form = new HashMap<>();
        for (String campo : CAMPOS) {
            String valor = req.getParameter(campo);
            form.put(campo, valor == null ? "" : valor.trim());
        }

        ServicioPortal sistema = ContextoListener.central(req);

        try {
            DtImagen imagen = Subidas.leerImagen(req, "imagen");
            DtUsuario usuario = "organizador".equals(form.get("tipo"))
                    ? armarOrganizador(req, form) : armarAsistente(req, form);
            contratos.dto.EntradaUsuario entrada = new contratos.dto.EntradaUsuario();
            entrada.setUsuario(usuario); entrada.setImagen(imagen);
            entrada.setContrasenia(req.getParameter("contrasenia"));
            entrada.setConfirmacion(req.getParameter("confirmacion"));
            sistema.altaUsuario(entrada);
            String destino = "/login?registrado=ok";
            resp.sendRedirect(req.getContextPath() + destino);

        } catch (IllegalArgumentException e) {
            mostrarFormulario(req, resp, form, e.getMessage());
        } catch (DateTimeParseException e) {
            mostrarFormulario(req, resp, form, "La fecha de nacimiento no es válida.");
        }
    }

    private DtAsistente armarAsistente(HttpServletRequest req, Map<String, String> form) {

        LocalDate fechaNacimiento = LocalDate.parse(form.get("fechaNac"));


        String institucion = form.get("institucion").isEmpty() ? null : form.get("institucion");

        DtAsistente dt = new DtAsistente(
                form.get("nickname"), form.get("nombre"), form.get("correo"),
                form.get("apellido"), fechaNacimiento, institucion);


        return dt;
    }

    private DtOrganizador armarOrganizador(HttpServletRequest req, Map<String, String> form) {

        DtOrganizador dt = new DtOrganizador(
                form.get("nickname"), form.get("nombre"), form.get("correo"),
                form.get("descripcion"), form.get("sitioWeb"));


        return dt;
    }


    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp,
                                   Map<String, String> form, String error)
            throws ServletException, IOException {
        req.setAttribute("form", form);
        req.setAttribute("error", error);
        req.setAttribute("instituciones", ContextoListener.central(req).listarInstituciones());
        req.getRequestDispatcher("/WEB-INF/views/alta-usuario.jsp").forward(req, resp);
    }
}
