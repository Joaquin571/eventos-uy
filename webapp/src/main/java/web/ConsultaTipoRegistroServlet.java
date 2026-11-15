package web;
import contratos.dto.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet("/tipo-registro")
public class ConsultaTipoRegistroServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException {
        var central=ContextoListener.central(req);String nombre=AccesoEdiciones.texto(req,"nombre");
        String edicion=AccesoEdiciones.texto(req,"edicion");
        // Central valida también el tipo cuando falta o no coincide la edición.
        DtTipoRegistro tipo=nombre.isEmpty()?null:central.consultarTipoRegistro(edicion,nombre);
        var seleccion=central.consultarEdiciones(AccesoEdiciones.texto(req,"evento"),edicion);
        req.setAttribute("eventos",seleccion.getEventos());req.setAttribute("eventoElegido",seleccion.getEvento());
        req.setAttribute("ediciones",seleccion.getEdiciones());
        if(seleccion.getEdicion()!=null){
            req.setAttribute("edicionElegida",seleccion.getEdicion());
            req.setAttribute("tipos",central.consultarDetalleEdicion(edicion).getTipos());
        }
        if(tipo!=null){req.setAttribute("detalle",tipo);req.setAttribute("costo",DetalleRegistroVista.formatearCosto(tipo.getCosto()));}
        req.getRequestDispatcher("/WEB-INF/views/tipo-registro.jsp").forward(req,resp);
    }
}
