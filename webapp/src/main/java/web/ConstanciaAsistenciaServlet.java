package web;

import contratos.dto.DtConstanciaAsistencia;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/constancia-asistencia")
public class ConstanciaAsistenciaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String edicion = AccesoEdiciones.texto(req, "edicion");
        DtConstanciaAsistencia datos = ContextoListener.central(req).obtenerDatosConstancia(edicion);
        byte[] pdf = GeneradorConstanciaPdf.generar(datos);

        String nombreArchivo = "constancia-" + edicion.replaceAll("[^A-Za-z0-9_-]", "_") + ".pdf";
        resp.setContentType("application/pdf");
        resp.setHeader("Cache-Control", "private, no-store");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + nombreArchivo + "\"");
        resp.setContentLength(pdf.length);
        resp.getOutputStream().write(pdf);
    }
}