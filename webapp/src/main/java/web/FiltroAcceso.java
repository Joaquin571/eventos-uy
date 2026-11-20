package web;

import contratos.dto.DtOrganizador;
import contratos.dto.DtUsuario;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@WebFilter("/*")
public class FiltroAcceso implements Filter {

    private static final Set<String> SOLO_VISITANTE = Set.of("visitante");
    private static final Set<String> SOLO_ASISTENTE = Set.of("asistente");
    private static final Set<String> SOLO_ORGANIZADOR = Set.of("organizador");
    private static final Set<String> CON_SESION = Set.of("asistente", "organizador");

    // Rutas restringidas por rol. Las que no figuran son públicas.
    private static final Map<String, Set<String>> PERMITIDOS = Map.ofEntries(
            Map.entry("/login", SOLO_VISITANTE),
            Map.entry("/alta-usuario", SOLO_VISITANTE),
            Map.entry("/logout", CON_SESION),
            Map.entry("/modificar-usuario", CON_SESION),
            Map.entry("/consulta-registro", CON_SESION),
            Map.entry("/registro-edicion", SOLO_ASISTENTE),
            Map.entry("/alta-evento", SOLO_ORGANIZADOR),
            Map.entry("/alta-edicion", SOLO_ORGANIZADOR),
            Map.entry("/alta-tipo-registro", SOLO_ORGANIZADOR),
            Map.entry("/alta-institucion", SOLO_ORGANIZADOR),
            Map.entry("/alta-patrocinio", SOLO_ORGANIZADOR)
    );

    private static final String[] ESTATICOS = {"/css/", "/js/", "/vendor/", "/img/"};

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        try {
            filtrar(req, resp, chain);
        } catch (web.remoto.ErrorCentral error) {
            responderError(req, resp, error);
        } catch (IllegalArgumentException error) {
            resp.sendError(400, "La solicitud no es válida.");
        }
    }

    static void responderError(HttpServletRequest req, HttpServletResponse resp, web.remoto.ErrorCentral error)
            throws IOException, ServletException {
            switch (error.getCodigo()) {
                case "SESION_INVALIDA" -> {
                    var sesion = req.getSession(false);
                    if (sesion != null) sesion.invalidate();
                    resp.sendRedirect(req.getContextPath() + "/login");
                }
                case "ACCESO_DENEGADO" -> resp.sendError(403);
                case "NO_ENCONTRADO" -> resp.sendError(404);
                default -> {
                    resp.setStatus(503);
                    req.getRequestDispatcher("/WEB-INF/views/servicio-no-disponible.jsp").forward(req, resp);
                }
            }
    }

    private void filtrar(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        req.setCharacterEncoding("UTF-8");

        String ruta = req.getServletPath();

        if ("POST".equals(req.getMethod()) && !Csrf.validarPost(req, resp)) return;

        // La consulta AJAX pública no necesita cabezal, categorías ni una sesión HTTP.
        // Su Servlet conserva la respuesta JSON incluso si Central no está disponible.
        if (esEstatico(ruta) || "/disponibilidad-usuario".equals(ruta)) {
            chain.doFilter(req, resp);
            return;
        }

        if (!"POST".equals(req.getMethod())) Csrf.prepararFormulario(req);
        resp.setHeader("Cache-Control", "private, no-store");

        HttpSession sesion = req.getSession(false);
        DtUsuario usuario = SesionWeb.usuario(req);
        if (!"/logout".equals(ruta) && SesionWeb.credencial(req) != null) {
            usuario = ContextoListener.central(req).usuarioActual();
            SesionWeb.guardar(sesion, usuario);
        } else if (usuario != null && SesionWeb.credencial(req) == null) {
            sesion.removeAttribute("usuario");
            usuario = null;
        }
        String rol = rolDe(usuario);

        Set<String> permitidos = PERMITIDOS.get(ruta);

        if (permitidos != null && !permitidos.contains(rol)) {
            if (rol.equals("visitante")) {
                resp.sendRedirect(req.getContextPath() + "/login");
            } else if (permitidos.equals(SOLO_VISITANTE)) {
                resp.sendRedirect(req.getContextPath() + "/");
            } else {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            return;
        }

        // Datos que necesita el cabezal en todas las páginas
        req.setAttribute("rol", rol);
        req.setAttribute("categorias", ContextoListener.central(req).listarCategorias());

        chain.doFilter(req, resp);
    }

    private boolean esEstatico(String ruta) {
        if (ruta.equals("/imagen")) {
            return true;
        }
        for (String prefijo : ESTATICOS) {
            if (ruta.startsWith(prefijo)) {
                return true;
            }
        }
        return false;
    }

    private String rolDe(DtUsuario usuario) {
        if (usuario == null) {
            return "visitante";
        }
        return usuario instanceof DtOrganizador ? "organizador" : "asistente";
    }
}
