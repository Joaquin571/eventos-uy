package web;


import contratos.dto.DtAsistente;
import contratos.dto.DtImagen;
import contratos.dto.DtOrganizador;
import contratos.dto.DtUsuario;
import contratos.ServicioPortal;
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
@MultipartConfig(maxFileSize = 2L * 1024 * 1024, maxRequestSize = 3L * 1024 * 1024)
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

        ServicioPortal sistema = ContextoListener.central(req);
        DtUsuario perfil = perfilActual(req);

        Map<String, String> form = new HashMap<>();
        for (String campo : CAMPOS) {
            String valor = req.getParameter(campo);
            form.put(campo, valor == null ? "" : valor.trim());
        }

        try {
            DtImagen imagen = Subidas.leerImagen(req, "imagen");

            contratos.dto.EntradaUsuario entrada = new contratos.dto.EntradaUsuario();
            entrada.setUsuario(perfil instanceof DtAsistente ? armarAsistente(req, form, perfil) : armarOrganizador(req, form, perfil));
            entrada.setImagen(imagen);entrada.setContrasenia(req.getParameter("contrasenia"));
            entrada.setConfirmacion(req.getParameter("confirmacion"));
            DtUsuario actualizado = sistema.modificarUsuario(entrada);
            SesionWeb.guardar(req.getSession(), actualizado);
            MensajesFlash.guardar(req, "/usuario", "Los datos del perfil se guardaron.", null);
            resp.sendRedirect(req.getContextPath() + "/usuario?nick="
                    + URLEncoder.encode(perfil.getNickname(), StandardCharsets.UTF_8));

        } catch (IllegalArgumentException e) {
            mostrarFormulario(req, resp, perfil, form, e.getMessage());
        } catch (DateTimeParseException e) {
            mostrarFormulario(req, resp, perfil, form, "La fecha de nacimiento no es válida.");
        }
    }

    private DtUsuario perfilActual(HttpServletRequest req) {
        return ContextoListener.central(req).usuarioActual();
    }

    private DtAsistente armarAsistente(HttpServletRequest req, Map<String, String> form, DtUsuario perfil) {

        LocalDate fechaNacimiento = LocalDate.parse(form.get("fechaNac"));


        String institucion = form.get("institucion").isEmpty() ? null : form.get("institucion");

        DtAsistente dt = new DtAsistente(
                perfil.getNickname(), form.get("nombre"), perfil.getCorreoElectronico(),
                form.get("apellido"), fechaNacimiento, institucion);


        return dt;
    }

    private DtOrganizador armarOrganizador(HttpServletRequest req, Map<String, String> form, DtUsuario perfil) {

        DtOrganizador dt = new DtOrganizador(
                perfil.getNickname(), form.get("nombre"), perfil.getCorreoElectronico(),
                form.get("descripcion"), form.get("sitioWeb"));


        return dt;
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
        req.setAttribute("instituciones", ContextoListener.central(req).listarInstituciones());
        req.getRequestDispatcher("/WEB-INF/views/modificar-usuario.jsp").forward(req, resp);
    }
}
