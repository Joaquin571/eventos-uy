package web;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException {
        var sesion=req.getSession(false);String credencial=SesionWeb.credencial(req);
        try {if(credencial!=null)ContextoListener.sesiones(req).cerrar(credencial);}
        finally {if(sesion!=null)sesion.invalidate();}
        resp.sendRedirect(req.getContextPath()+"/");
    }
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException {
        resp.setHeader("Allow","POST");resp.sendError(405);
    }
}
