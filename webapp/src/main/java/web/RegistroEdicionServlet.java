package web;
import contratos.dto.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
@WebServlet("/registro-edicion")
public class RegistroEdicionServlet extends HttpServlet {
    private static final DateTimeFormatter FECHA=DateTimeFormatter.ofPattern("dd/MM/yyyy");
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException {
        mostrar(req,resp,AccesoEdiciones.texto(req,"evento"),AccesoEdiciones.texto(req,"edicion"),AccesoEdiciones.texto(req,"tipo"),"general","",null);
    }
    protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException {
        String evento=AccesoEdiciones.texto(req,"evento"),edicion=AccesoEdiciones.texto(req,"edicion"),tipo=AccesoEdiciones.texto(req,"tipo");
        String modalidad=AccesoEdiciones.texto(req,"modalidad"),codigo=AccesoEdiciones.texto(req,"codigo");
        try {
            ContextoListener.central(req).registrar(edicion,tipo,modalidad,codigo);
            resp.sendRedirect(req.getContextPath()+"/consulta-registro?registrado=1&edicion="+URLEncoder.encode(edicion,StandardCharsets.UTF_8));
        }catch(IllegalArgumentException e){mostrar(req,resp,evento,edicion,tipo,modalidad,codigo,e.getMessage());}
    }
    private void mostrar(HttpServletRequest req,HttpServletResponse resp,String evento,String edicion,String tipo,String modalidad,String codigo,String error)throws ServletException,IOException {
        var consulta=ContextoListener.central(req).formularioRegistro(evento,edicion,tipo);
        req.setAttribute("eventos",consulta.getEventos());req.setAttribute("eventoElegido",consulta.getEvento());
        req.setAttribute("ediciones",consulta.getEdiciones());req.setAttribute("tipos",consulta.getTipos());
        var elegida=consulta.getEdicion();req.setAttribute("detalle",elegida);
        req.setAttribute("edicionElegida",elegida==null?"":elegida.getIdNombre());
        req.setAttribute("tipoElegido",consulta.getTipo()==null?"":consulta.getTipo().getIdNombre());
        Map<String,String> costos=new LinkedHashMap<>();for(var t:consulta.getTipos())costos.put(t.getIdNombre(),DetalleRegistroVista.formatearCosto(t.getCosto()));
        req.setAttribute("costos",costos);req.setAttribute("modalidad",modalidad);req.setAttribute("codigo",codigo);req.setAttribute("error",error);
        if(elegida!=null){req.setAttribute("fechaInicio",fecha(elegida.getFechaInicio()));req.setAttribute("fechaFin",fecha(elegida.getFechaFin()));}
        req.getRequestDispatcher("/WEB-INF/views/registro-edicion.jsp").forward(req,resp);
    }
    private String fecha(LocalDate valor){return valor==null?"":valor.format(FECHA);}
}
