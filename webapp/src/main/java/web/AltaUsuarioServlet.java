package web;

import datatypes.DtAsistente;
import datatypes.DtImagen;
import datatypes.DtOrganizador;
import datatypes.DtUsuario;
import datatypes.TipoEntidadImagen;
import interfaces.ISistema;
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
@MultipartConfig
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

        ISistema sistema = ContextoListener.sistema(req);

        try {
            // La imagen se valida antes de crear el usuario, para no dejarlo a medias
            DtImagen imagen = Subidas.leerImagen(req, "imagen");

            if (imagen != null) {
                sistema.validarImagen(imagen);
            }

            boolean alta;

            if ("organizador".equals(form.get("tipo"))) {
                alta = sistema.altaOrganizador(armarOrganizador(req, form));
            } else {
                alta = sistema.altaAsistente(armarAsistente(req, form));
            }

            if (!alta) {
                throw new IllegalArgumentException(
                        "No se pudo registrar el usuario. Verificá los datos ingresados.");
            }

            String destino = "/login?registrado=ok";

            if (imagen != null) {
                try {
                    sistema.asociarImagen(TipoEntidadImagen.USUARIO, form.get("nickname"), imagen);
                } catch (IllegalArgumentException e) {
                    destino += "&imagen=error";
                }
            }

            resp.sendRedirect(req.getContextPath() + destino);

        } catch (IllegalArgumentException e) {
            mostrarFormulario(req, resp, form, e.getMessage());
        } catch (DateTimeParseException e) {
            mostrarFormulario(req, resp, form, "La fecha de nacimiento no es válida.");
        }
    }

    private DtAsistente armarAsistente(HttpServletRequest req, Map<String, String> form) {
        if (form.get("apellido").isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }

        LocalDate fechaNacimiento = LocalDate.parse(form.get("fechaNac"));

        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
        }

        String institucion = form.get("institucion").isEmpty() ? null : form.get("institucion");

        DtAsistente dt = new DtAsistente(
                form.get("nickname"), form.get("nombre"), form.get("correo"),
                form.get("apellido"), fechaNacimiento, institucion);

        agregarContrasenia(req, dt);
        return dt;
    }

    private DtOrganizador armarOrganizador(HttpServletRequest req, Map<String, String> form) {
        if (form.get("descripcion").isEmpty()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }

        DtOrganizador dt = new DtOrganizador(
                form.get("nickname"), form.get("nombre"), form.get("correo"),
                form.get("descripcion"), form.get("sitioWeb"));

        agregarContrasenia(req, dt);
        return dt;
    }

    private void agregarContrasenia(HttpServletRequest req, DtUsuario dt) {
        dt.setContrasenia(req.getParameter("contrasenia"));
        dt.setConfirmacionContrasenia(req.getParameter("confirmacion"));
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp,
                                   Map<String, String> form, String error)
            throws ServletException, IOException {
        req.setAttribute("form", form);
        req.setAttribute("error", error);
        req.setAttribute("instituciones", ContextoListener.sistema(req).listarInstituciones());
        req.getRequestDispatcher("/WEB-INF/views/alta-usuario.jsp").forward(req, resp);
    }
}