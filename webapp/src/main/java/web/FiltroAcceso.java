package web;

import datatypes.DtOrganizador;
import web.remoto.ClienteUsuarios;
import web.remoto.ErrorRemotoUsuarios;
import datatypes.DtUsuario;
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

    private static final Set<String> RUTAS_USUARIOS = Set.of("/login", "/logout", "/alta-usuario", "/usuarios", "/usuario",
            "/modificar-usuario", "/alta-institucion", "/consulta-institucion", "/alta-patrocinio", "/consulta-patrocinio", "/disponibilidad-usuario");

    private static final String[] ESTATICOS = {"/css/", "/js/", "/vendor/", "/img/"};

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        req.setCharacterEncoding("UTF-8");

        String ruta = req.getServletPath();

        if ("POST".equals(req.getMethod()) && !Csrf.validarPost(req, resp)) return;

        if (esEstatico(ruta)) {
            chain.doFilter(req, resp);
            return;
        }

        if (!"POST".equals(req.getMethod())) Csrf.prepararFormulario(req);
        resp.setHeader("Cache-Control", "private, no-store");

        try {
        if (RUTAS_USUARIOS.contains(ruta) && !ruta.equals("/logout")) {
            String token = ClienteUsuarios.token(req);
            if (token != null) req.getSession().setAttribute("usuario", ClienteUsuarios.get().usuarioActual(token));
            else if (req.getSession(false) != null) req.getSession(false).removeAttribute("usuario");
        }

        HttpSession sesion = req.getSession(false);
        DtUsuario usuario = sesion == null ? null : (DtUsuario) sesion.getAttribute("usuario");
        String rol = rolDe(usuario);

        Set<String> permitidos = PERMITIDOS.get(ruta);

        if (permitidos != null && !permitidos.contains(rol)) {
            if (rol.equals("visitante")) {
                resp.sendRedirect(req.getContextPath() + "/login");
            } else if (permitidos.equals(SOLO_VISITANTE)) {
                resp.sendRedirect(req.getContextPath() + (RUTAS_USUARIOS.contains(ruta)
                        ? "/usuario?nick=" + java.net.URLEncoder.encode(usuario.getNickname(), java.nio.charset.StandardCharsets.UTF_8) : "/"));
            } else {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            return;
        }

        // Datos que necesita el cabezal en todas las páginas
        req.setAttribute("rol", rol);
        req.setAttribute("categorias", ruta.equals("/logout") ? java.util.List.of() : RUTAS_USUARIOS.contains(ruta)
                ? ClienteUsuarios.get().listarCategorias() : ContextoListener.sistema(req).listarCategorias());

        chain.doFilter(req, resp);
        } catch (ErrorRemotoUsuarios e) {
            if (resp.isCommitted()) throw e;
            if ("SESION".equals(e.getCodigo())) {
                HttpSession vencida = req.getSession(false);
                if (vencida != null) vencida.invalidate();
                resp.sendRedirect(req.getContextPath() + "/login");
            } else if ("PERMISO".equals(e.getCodigo())) resp.sendError(403);
            else if ("NO_ENCONTRADO".equals(e.getCodigo())) resp.sendError(404);
            else {
                if ("/disponibilidad-usuario".equals(ruta)) {
                    resp.setStatus(503);
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().write("{\"error\":\"Servidor central no disponible\"}");
                } else {
                    resp.setStatus(503);
                    req.getRequestDispatcher("/WEB-INF/views/usuarios-no-disponible.jsp").forward(req, resp);
                }
            }
        }
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