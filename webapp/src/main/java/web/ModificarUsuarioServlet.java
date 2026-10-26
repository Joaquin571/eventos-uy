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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/modificar-usuario")
@MultipartConfig
public class ModificarUsuarioServlet extends HttpServlet {

    private static final String[] CAMPOS = {
            "nombre", "apellido", "fechaNac", "institucion", "descripcion", "sitioWeb"
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        DtUsuario perfil = perfilActual(req);
        mostrarFormulario(req, resp, perfil, formularioDesde(perfil), null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        ISistema sistema = ContextoListener.sistema(req);
        DtUsuario perfil = perfilActual(req);

        Map<String, String> form = new HashMap<>();
        for (String campo : CAMPOS) {
            String valor = req.getParameter(campo);
            form.put(campo, valor == null ? "" : valor.trim());
        }

        try {
            DtImagen imagen = Subidas.leerImagen(req, "imagen");

            if (imagen != null) {
                sistema.validarImagen(imagen);
            }

            if (perfil instanceof DtAsistente) {
                sistema.modificarAsistente(armarAsistente(req, form, perfil));
            } else {
                sistema.modificarOrganizador(armarOrganizador(req, form, perfil));
            }

            if (imagen != null) {
                sistema.asociarImagen(TipoEntidadImagen.USUARIO, perfil.getNickname(), imagen);
            }

            // El cabezal muestra el nombre: se actualiza el usuario guardado en la sesión
            req.getSession().setAttribute("usuario", sistema.consultarUsuario(perfil.getNickname()));

            resp.sendRedirect(req.getContextPath() + "/usuario?nick="
                    + URLEncoder.encode(perfil.getNickname(), StandardCharsets.UTF_8));

        } catch (IllegalArgumentException e) {
            mostrarFormulario(req, resp, perfil, form, e.getMessage());
        } catch (DateTimeParseException e) {
            mostrarFormulario(req, resp, perfil, form, "La fecha de nacimiento no es válida.");
        }
    }

    private DtUsuario perfilActual(HttpServletRequest req) {
        DtUsuario enSesion = (DtUsuario) req.getSession(false).getAttribute("usuario");
        return ContextoListener.sistema(req).consultarUsuario(enSesion.getNickname());
    }

    private DtAsistente armarAsistente(HttpServletRequest req, Map<String, String> form, DtUsuario perfil) {
        if (form.get("nombre").isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (form.get("apellido").isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }

        LocalDate fechaNacimiento = LocalDate.parse(form.get("fechaNac"));

        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
        }

        String institucion = form.get("institucion").isEmpty() ? null : form.get("institucion");

        DtAsistente dt = new DtAsistente(
                perfil.getNickname(), form.get("nombre"), perfil.getCorreoElectronico(),
                form.get("apellido"), fechaNacimiento, institucion);

        agregarContrasenia(req, dt);
        return dt;
    }

    private DtOrganizador armarOrganizador(HttpServletRequest req, Map<String, String> form, DtUsuario perfil) {
        if (form.get("nombre").isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (form.get("descripcion").isEmpty()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }

        DtOrganizador dt = new DtOrganizador(
                perfil.getNickname(), form.get("nombre"), perfil.getCorreoElectronico(),
                form.get("descripcion"), form.get("sitioWeb"));

        agregarContrasenia(req, dt);
        return dt;
    }

    // La contraseña solo se cambia si el usuario escribió una nueva
    private void agregarContrasenia(HttpServletRequest req, DtUsuario dt) {
        String nueva = req.getParameter("contrasenia");

        if (nueva != null && !nueva.isEmpty()) {
            dt.setContrasenia(nueva);
            dt.setConfirmacionContrasenia(req.getParameter("confirmacion"));
        }
    }

    private Map<String, String> formularioDesde(DtUsuario perfil) {
        Map<String, String> form = new HashMap<>();
        form.put("nombre", vacio(perfil.getNombre()));

        if (perfil instanceof DtAsistente asistente) {
            form.put("apellido", vacio(asistente.getApellido()));
            form.put("fechaNac", asistente.getFechaNacimiento() == null
                    ? "" : asistente.getFechaNacimiento().toString());
            form.put("institucion", vacio(asistente.getNombreInstitucion()));
        } else if (perfil instanceof DtOrganizador organizador) {
            form.put("descripcion", vacio(organizador.getDescripcion()));
            form.put("sitioWeb", vacio(organizador.getSitioWeb()));
        }
        return form;
    }

    private String vacio(String texto) {
        return texto == null ? "" : texto;
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp,
                                   DtUsuario perfil, Map<String, String> form, String error)
            throws ServletException, IOException {

        req.setAttribute("perfil", perfil);
        req.setAttribute("esAsistente", perfil instanceof DtAsistente);
        req.setAttribute("esOrganizador", perfil instanceof DtOrganizador);
        req.setAttribute("form", form);
        req.setAttribute("error", error);
        req.setAttribute("instituciones", ContextoListener.sistema(req).listarInstituciones());
        req.getRequestDispatcher("/WEB-INF/views/modificar-usuario.jsp").forward(req, resp);
    }
}