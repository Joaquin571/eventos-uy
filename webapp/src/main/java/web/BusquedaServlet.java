package web;
import contratos.dto.OrdenBusqueda;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/buscar")
public class BusquedaServlet extends HttpServlet {
 @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException {
  String texto=req.getParameter("texto");
  OrdenBusqueda orden;
  try { orden=OrdenBusqueda.valueOf(req.getParameter("orden")); }
  catch (IllegalArgumentException | NullPointerException e) {orden=OrdenBusqueda.FECHA_DESC;}
  if (texto != null && texto.strip().length() > 255) {
   resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "La búsqueda es demasiado larga.");
   return;
  }
  req.setAttribute("resultados",ContextoListener.central(req).buscarEventosYEdiciones(texto,orden));
  req.setAttribute("texto",texto==null?"":texto);
  req.setAttribute("orden",orden.name());
  req.getRequestDispatcher("/WEB-INF/views/buscar.jsp").forward(req,resp);
 }
}
