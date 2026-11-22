package web;
import contratos.dto.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/consulta-registro")
public class ConsultaRegistroServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException {
        MensajesFlash.mostrar(req, "/consulta-registro");
        String edicion=AccesoEdiciones.texto(req,"edicion"),asistente=AccesoEdiciones.texto(req,"asistente");
        var central = ContextoListener.central(req);
        var consulta=central.consultarRegistros(edicion,asistente);
        Map<String,String> nombres=new HashMap<>();
        for(var usuario:consulta.getAsistentes())nombres.put(usuario.getNickname(),nombreCompleto(usuario));
        var propio=SesionWeb.usuario(req);if(propio!=null)nombres.put(propio.getNickname(),propio.getNombre());
        req.setAttribute("esOrganizador",consulta.isOrganizador());req.setAttribute("ediciones",consulta.getEdiciones());
        req.setAttribute("edicionElegida",edicion);req.setAttribute("asistenteElegido",asistente);
        req.setAttribute("registrado","1".equals(req.getParameter("registrado")));
        req.setAttribute("registros",consulta.getRegistros().stream().map(r->DetalleRegistroVista.de(r,nombres.getOrDefault(r.getNicknameAsistente(),r.getNicknameAsistente()))).toList());
        var detalle=consulta.getDetalle();
        if(detalle!=null){
            req.setAttribute("detalle",DetalleRegistroVista.de(detalle,nombres.getOrDefault(detalle.getNicknameAsistente(),detalle.getNicknameAsistente())));
            if(!consulta.isOrganizador() && !detalle.isAsistenciaConfirmada()){
                req.setAttribute("puedeConfirmarAsistencia", true);
            }
        }
        req.getRequestDispatcher("/WEB-INF/views/consulta-registro.jsp").forward(req,resp);
    }
    private String nombreCompleto(DtUsuario u){return u instanceof DtAsistente a?u.getNombre()+" "+a.getApellido():u.getNombre();}
}
