package web;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException {
        SesionWeb.cerrar(req);
        resp.sendRedirect(req.getContextPath()+"/");
    }
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException {
        resp.setHeader("Allow","POST");resp.sendError(405);
    }
}
