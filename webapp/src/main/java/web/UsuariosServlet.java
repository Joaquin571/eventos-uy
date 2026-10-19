package web;

import datatypes.DtAsistente;
import datatypes.DtOrganizador;
import datatypes.DtUsuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/usuarios")
public class UsuariosServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<DtAsistente> asistentes = new ArrayList<>();
        List<DtOrganizador> organizadores = new ArrayList<>();

        for (DtUsuario usuario : ContextoListener.sistema(req).listarUsuarios()) {
            if (usuario instanceof DtAsistente asistente) {
                asistentes.add(asistente);
            } else if (usuario instanceof DtOrganizador organizador) {
                organizadores.add(organizador);
            }
        }

        req.setAttribute("asistentes", asistentes);
        req.setAttribute("organizadores", organizadores);
        req.getRequestDispatcher("/WEB-INF/views/usuarios.jsp").forward(req, resp);
    }
}