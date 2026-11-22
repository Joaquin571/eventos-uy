package web;

import contratos.ServicioPortal;
import contratos.dto.DtAsistente;
import contratos.dto.DtConstanciaAsistencia;
import contratos.dto.DtUsuario;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Test;
import web.remoto.ErrorCentral;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConstanciaPdfYServletsTest {

    @Test
    void camposConLongitudMaximaNoQuedanFueraDeLasPaginas() throws Exception {
        String nombre = "AS" + "W".repeat(253), apellido = "AP" + "W".repeat(253);
        String evento = "EV" + "W".repeat(253), edicion = "ED" + "W".repeat(253), ciudad = "CI" + "W".repeat(253);
        var datos = new DtConstanciaAsistencia(nombre, apellido, evento, edicion, ciudad,
                LocalDate.now(), LocalDate.now(), LocalDate.now());
        try (PDDocument doc = Loader.loadPDF(GeneradorConstanciaPdf.generar(datos))) {
            var lector = new PDFTextStripper() {
                @Override protected void processTextPosition(TextPosition posicion) {
                    var pagina = doc.getPage(getCurrentPageNo() - 1).getMediaBox();
                    assertTrue(posicion.getXDirAdj() >= 59f, "Texto fuera del margen izquierdo");
                    assertTrue(posicion.getXDirAdj() + posicion.getWidthDirAdj() <= pagina.getWidth() - 59f,
                            "Texto recortado por el margen derecho");
                    assertTrue(posicion.getYDirAdj() <= pagina.getHeight() - 45f,
                            "Texto recortado por el borde inferior");
                    super.processTextPosition(posicion);
                }
            };
            String texto = lector.getText(doc).replaceAll("\\s+", "");
            for (String campo : new String[]{nombre, apellido, evento, edicion, ciudad})
                assertTrue(texto.contains(campo), "Se perdió parte de un campo largo");
        }
    }

    @Test
    void conservaNombresConCaracteresFueraDeWinAnsi() throws Exception {
        var datos = new DtConstanciaAsistencia("Łukasz", "Žižek", "Encuentro Ω", "Edición 2026",
                "Łódź", LocalDate.now(), LocalDate.now(), LocalDate.now());
        try (PDDocument doc = Loader.loadPDF(GeneradorConstanciaPdf.generar(datos))) {
            String texto = new PDFTextStripper().getText(doc);
            assertTrue(texto.contains("Łukasz Žižek"));
            assertTrue(texto.contains("Encuentro Ω"));
            assertTrue(texto.contains("Łódź"));
        }
    }

    @Test
    void saltosDeLineaYTabulacionesEnDatosNoRompenElPdf() throws Exception {
        var datos = new DtConstanciaAsistencia("Ana\nMaría", "Pérez", "Evento\tUY", "Edición",
                "Montevideo", LocalDate.now(), LocalDate.now(), LocalDate.now());
        try (PDDocument doc = Loader.loadPDF(GeneradorConstanciaPdf.generar(datos))) {
            String texto = new PDFTextStripper().getText(doc);
            assertTrue(texto.contains("Ana María Pérez"));
            assertTrue(texto.contains("Evento UY"));
        }
    }

    @Test
    void generaPdfValidoConTodosLosCamposExigidos() throws Exception {
        DtConstanciaAsistencia datos = new DtConstanciaAsistencia(
                "Lucia",
                "Gomez",
                "Montevideo Tech",
                "MT 2026",
                "Montevideo",
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                LocalDate.of(2026, 11, 20)
        );

        byte[] pdf = GeneradorConstanciaPdf.generar(datos);
        assertTrue(pdf.length > 200);
        assertEquals("%PDF-", new String(pdf, 0, 5));

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertEquals(1, doc.getNumberOfPages());
            String texto = new PDFTextStripper().getText(doc);
            assertTrue(texto.contains("Lucia Gomez"));
            assertTrue(texto.contains("Montevideo Tech"));
            assertTrue(texto.contains("MT 2026"));
            assertTrue(texto.contains("Montevideo"));
            assertTrue(texto.contains("10/11/2026"));
            assertTrue(texto.contains("12/11/2026"));
            assertTrue(texto.contains("20/11/2026"));
        }
    }

    @Test
    void servletDescargaPdfConEncabezadosSegurosYRechazaNoAutorizados() throws Exception {
        DtAsistente ana = new DtAsistente("ana", "Ana", "ana@test.uy", "Perez", LocalDate.of(2000, 1, 1), null);
        EscenarioHttp ok = new EscenarioHttp(ana, true);
        ok.params.put("edicion", "MT 2026");
        ok.params.put("nickname", "otro_usuario_manipulado");

        PortalDoble.http(() -> new ConstanciaAsistenciaServlet().doGet(ok.req, ok.resp), ok.req, ok.resp);

        assertEquals(200, ok.status);
        assertEquals("application/pdf", ok.contentType);
        assertEquals("private, no-store", ok.headers.get("Cache-Control"));
        assertTrue(ok.headers.get("Content-Disposition").contains("attachment; filename=\"constancia-MT_2026.pdf\""));
        assertEquals("MT 2026", ok.edicionConsultada);

        try (PDDocument doc = Loader.loadPDF(ok.salidaBytes.toByteArray())) {
            String texto = new PDFTextStripper().getText(doc);
            assertTrue(texto.contains("Ana Perez"));
        }

        // Usuario sin confirmación / acceso denegado no descarga PDF
        EscenarioHttp denegado = new EscenarioHttp(ana, false);
        denegado.params.put("edicion", "MT 2026");
        PortalDoble.http(() -> new ConstanciaAsistenciaServlet().doGet(denegado.req, denegado.resp), denegado.req, denegado.resp);
        assertEquals(403, denegado.status);
        assertEquals(0, denegado.salidaBytes.size());
    }

    @Test
    void servletsConfirmarYArchivarRedirigenConMensajeFlash() throws Exception {
        DtAsistente ana = new DtAsistente("ana", "Ana", "ana@test.uy", "Perez", LocalDate.of(2000, 1, 1), null);
        EscenarioHttp escConfirmar = new EscenarioHttp(ana, true);
        escConfirmar.params.put("edicion", "MT 2026");
        escConfirmar.params.put("origen", "edicion");
        new ConfirmarAsistenciaServlet().doPost(escConfirmar.req, escConfirmar.resp);
        assertEquals(302, escConfirmar.status);
        assertEquals("/pruebas/edicion?nombre=MT+2026", escConfirmar.redirect);

        EscenarioHttp escArchivar = new EscenarioHttp(ana, true);
        escArchivar.params.put("edicion", "MT 2026");
        new ArchivarEdicionServlet().doPost(escArchivar.req, escArchivar.resp);
        assertEquals(302, escArchivar.status);
        assertEquals("/pruebas/edicion?nombre=MT+2026", escArchivar.redirect);
    }

    private static final class EscenarioHttp {
        final Map<String, String> params = new HashMap<>();
        final Map<String, Object> sesion = new HashMap<>();
        final Map<String, String> headers = new HashMap<>();
        final ByteArrayOutputStream salidaBytes = new ByteArrayOutputStream();
        int status = 200;
        String contentType;
        String redirect;
        String edicionConsultada;

        EscenarioHttp(DtUsuario usuario, boolean autorizado) {
            if (usuario != null) {
                sesion.put("usuario", usuario);
            }
            ServicioPortal portal = (ServicioPortal) Proxy.newProxyInstance(
                    ServicioPortal.class.getClassLoader(),
                    new Class<?>[]{ServicioPortal.class},
                    (p, m, a) -> switch (m.getName()) {
                        case "obtenerDatosConstancia" -> {
                            edicionConsultada = (String) a[0];
                            if (!autorizado) {
                                throw new ErrorCentral("ACCESO_DENEGADO", "Sin permiso");
                            }
                            yield new DtConstanciaAsistencia(
                                    "Ana", "Perez", "Evento", edicionConsultada,
                                    "Montevideo", LocalDate.of(2026, 11, 10),
                                    LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 20)
                            );
                        }
                        case "confirmarAsistencia" -> true;
                        case "archivarEdicion" -> null;
                        default -> throw new AssertionError("Método inesperado: " + m.getName());
                    }
            );
            ServletContext ctx = doble(ServletContext.class, (p, m, a) ->
                    "getAttribute".equals(m.getName()) && "central".equals(a[0]) ? portal : null);
            HttpSession httpSession = doble(HttpSession.class, (p, m, a) -> switch (m.getName()) {
                case "getAttribute" -> sesion.get(a[0]);
                case "setAttribute" -> { sesion.put((String) a[0], a[1]); yield null; }
                case "removeAttribute" -> { sesion.remove(a[0]); yield null; }
                default -> null;
            });
            this.req = doble(HttpServletRequest.class, (p, m, a) -> switch (m.getName()) {
                case "getSession" -> sesion.isEmpty() && a != null && a.length == 1 && Boolean.FALSE.equals(a[0]) ? null : httpSession;
                case "getParameter" -> params.get(a[0]);
                case "getServletContext" -> ctx;
                case "getContextPath" -> "/pruebas";
                default -> null;
            });
            ServletOutputStream out = new ServletOutputStream() {
                @Override public boolean isReady() { return true; }
                @Override public void setWriteListener(WriteListener l) {}
                @Override public void write(int b) { salidaBytes.write(b); }
            };
            this.resp = doble(HttpServletResponse.class, (p, m, a) -> switch (m.getName()) {
                case "sendError" -> { status = (Integer) a[0]; yield null; }
                case "sendRedirect" -> { status = 302; redirect = (String) a[0]; yield null; }
                case "setHeader" -> { headers.put((String) a[0], (String) a[1]); yield null; }
                case "setContentType" -> { contentType = (String) a[0]; yield null; }
                case "setContentLength" -> null;
                case "getOutputStream" -> out;
                default -> null;
            });
        }

        final HttpServletRequest req;
        final HttpServletResponse resp;

        static <T> T doble(Class<T> tipo, java.lang.reflect.InvocationHandler h) {
            return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, h));
        }
    }
}
