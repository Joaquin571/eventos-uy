package web;
import contratos.dto.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException {
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req,resp);
    }
    protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException {
        String identificador=req.getParameter("identificador");
        try {
            var inicio=ContextoListener.sesiones(req).iniciar(identificador,req.getParameter("contrasenia"));
            var anterior=req.getSession(false);if(anterior!=null)anterior.invalidate();
            var sesion=req.getSession(true);
            DtUsuario usuario="ORGANIZADOR".equals(inicio.rol())?new DtOrganizador():new DtAsistente();
            usuario.setNickname(inicio.nickname());usuario.setNombre(inicio.nombre());
            SesionWeb.guardar(sesion,usuario);sesion.setAttribute(SesionWeb.CREDENCIAL,inicio.credencial());
            resp.sendRedirect(req.getContextPath()+"/");
        }catch(IllegalArgumentException e){req.setAttribute("error",e.getMessage());req.setAttribute("identificador",identificador);doGet(req,resp);}
    }
}
