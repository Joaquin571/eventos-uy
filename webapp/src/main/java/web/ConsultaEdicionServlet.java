package web;
import contratos.dto.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@WebServlet("/edicion")
public class ConsultaEdicionServlet extends HttpServlet {
    private static final DateTimeFormatter FECHA=DateTimeFormatter.ofPattern("dd/MM/yyyy");
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException {
        MensajesFlash.mostrar(req, "/edicion");
        var central=ContextoListener.central(req);
        var seleccion=central.consultarEdiciones(AccesoEdiciones.texto(req,"evento"),AccesoEdiciones.texto(req,"nombre"));
        req.setAttribute("eventos",seleccion.getEventos());req.setAttribute("eventoElegido",seleccion.getEvento());
        req.setAttribute("ediciones",seleccion.getEdiciones());
        if(seleccion.getEdicion()!=null){
            var detalle=central.consultarDetalleEdicion(seleccion.getEdicion().getIdNombre());
            var edicion=detalle.getEdicion();req.setAttribute("detalle",edicion);
            req.setAttribute("fechaInicio",fecha(edicion.getFechaInicio()));req.setAttribute("fechaFin",fecha(edicion.getFechaFin()));
            req.setAttribute("fechaAlta",fecha(edicion.getFechaAlta()));req.setAttribute("fechaArchivo",fecha(edicion.getFechaArchivo()));
            req.setAttribute("organizador",detalle.getOrganizador());
            req.setAttribute("tieneImagen",detalle.isTieneImagen());req.setAttribute("tipos",detalle.getTipos());
            req.setAttribute("patrocinadoras",detalle.getPatrocinadoras());
            req.setAttribute("esPropietario",detalle.isPropietario());req.setAttribute("puedeRegistrarse",detalle.isPuedeRegistrarse());

            LocalDate hoy = LocalDate.now();
            boolean puedeArchivar = detalle.isPropietario()
                    && edicion.getEstado() == EstadoEdicion.ACEPTADA
                    && edicion.getFechaFin() != null
                    && edicion.getFechaFin().isBefore(hoy);
            req.setAttribute("puedeArchivar", puedeArchivar);

            req.setAttribute("registros",detalle.getRegistros().stream().map(r->DetalleRegistroVista.de(r,r.getNicknameAsistente())).toList());
            if(detalle.getRegistroPropio()!=null){
                var regPropio = detalle.getRegistroPropio();
                req.setAttribute("registroPropio",DetalleRegistroVista.de(regPropio,SesionWeb.usuario(req).getNombre()));
                boolean puedeConfirmar = !regPropio.isAsistenciaConfirmada()
                        && edicion.getEstado() == EstadoEdicion.ACEPTADA
                        && edicion.getFechaInicio() != null
                        && !hoy.isBefore(edicion.getFechaInicio());
                req.setAttribute("puedeConfirmarAsistencia", puedeConfirmar);
            }
        }
        req.getRequestDispatcher("/WEB-INF/views/edicion.jsp").forward(req,resp);
    }
    private String fecha(LocalDate valor){return valor==null?"":valor.format(FECHA);}
}