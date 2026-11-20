package web;

import contratos.dto.DtConstanciaAsistencia;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

final class GeneradorConstanciaPdf {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private GeneradorConstanciaPdf() {
    }

    static byte[] generar(DtConstanciaAsistencia datos) throws IOException {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            PDPage pagina = new PDPage(PDRectangle.A4);
            doc.addPage(pagina);

            PDType1Font negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream cs = new PDPageContentStream(doc, pagina)) {
                float margen = 60f;
                float y = pagina.getMediaBox().getHeight() - 70f;

                cs.beginText();
                cs.setFont(negrita, 20);
                cs.newLineAtOffset(margen, y);
                cs.showText("eventos.uy");
                cs.endText();

                y -= 35f;
                cs.beginText();
                cs.setFont(negrita, 16);
                cs.newLineAtOffset(margen, y);
                cs.showText("Constancia de Asistencia");
                cs.endText();

                y -= 15f;
                cs.moveTo(margen, y);
                cs.lineTo(pagina.getMediaBox().getWidth() - margen, y);
                cs.stroke();

                String asistente = textoSeguro(datos.getNombreAsistente() + " " + datos.getApellidoAsistente());
                y -= 35f;
                y = escribirParrafo(cs, normal, 12, margen, y,
                        "Por la presente se deja constancia de que " + asistente + " asistio a:");

                y -= 15f;
                y = escribirCampo(cs, negrita, normal, margen, y, "Evento:", textoSeguro(datos.getNombreEvento()));
                y = escribirCampo(cs, negrita, normal, margen, y, "Edicion:", textoSeguro(datos.getNombreEdicion()));
                y = escribirCampo(cs, negrita, normal, margen, y, "Ciudad:", textoSeguro(datos.getCiudad()));
                y = escribirCampo(cs, negrita, normal, margen, y, "Fecha de inicio:", fecha(datos.getFechaInicio()));
                y = escribirCampo(cs, negrita, normal, margen, y, "Fecha de fin:", fecha(datos.getFechaFin()));
                y = escribirCampo(cs, negrita, normal, margen, y, "Fecha de emision:", fecha(datos.getFechaEmision()));
            }

            doc.save(salida);
            return salida.toByteArray();
        }
    }

    private static float escribirParrafo(PDPageContentStream cs, PDType1Font fuente, float tam,
                                         float x, float y, String texto) throws IOException {
        cs.beginText();
        cs.setFont(fuente, tam);
        cs.newLineAtOffset(x, y);
        cs.showText(texto);
        cs.endText();
        return y - 22f;
    }

    private static float escribirCampo(PDPageContentStream cs, PDType1Font negrita, PDType1Font normal,
                                       float x, float y, String etiqueta, String valor) throws IOException {
        cs.beginText();
        cs.setFont(negrita, 12);
        cs.newLineAtOffset(x, y);
        cs.showText(etiqueta + " ");
        cs.setFont(normal, 12);
        cs.showText(valor);
        cs.endText();
        return y - 24f;
    }

    private static String fecha(LocalDate valor) {
        return valor == null ? "-" : valor.format(FECHA);
    }

    private static String textoSeguro(String texto) {
        if (texto == null) return "-";
        return texto.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ").trim();
    }
}