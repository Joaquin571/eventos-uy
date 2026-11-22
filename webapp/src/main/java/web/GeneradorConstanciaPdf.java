package web;

import contratos.dto.DtConstanciaAsistencia;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

final class GeneradorConstanciaPdf {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private GeneradorConstanciaPdf() {
    }

    static byte[] generar(DtConstanciaAsistencia datos) throws IOException {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            PDType1Font negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont normal;
            // La fuente se distribuye dentro de PDFBox: no depende de las fuentes instaladas en Tomcat.
            try (var fuente = GeneradorConstanciaPdf.class.getResourceAsStream(
                    "/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf")) {
                if (fuente == null) throw new IOException("No se encontró la fuente incluida para la constancia.");
                normal = PDType0Font.load(doc, fuente);
            }

            try (Escritor escritor = new Escritor(doc)) {
                escritor.linea(negrita, 20, "eventos.uy");
                escritor.espacio(7);
                escritor.linea(negrita, 16, "Constancia de Asistencia");
                escritor.separador();
                String asistente = textoSeguro(datos.getNombreAsistente() + " " + datos.getApellidoAsistente());
                escritor.parrafo(normal, "Por la presente se deja constancia de que " + asistente + " asistio a:");
                escritor.espacio(12);
                escritor.campo(negrita, normal, "Evento:", textoSeguro(datos.getNombreEvento()));
                escritor.campo(negrita, normal, "Edicion:", textoSeguro(datos.getNombreEdicion()));
                escritor.campo(negrita, normal, "Ciudad:", textoSeguro(datos.getCiudad()));
                escritor.campo(negrita, normal, "Fecha de inicio:", fecha(datos.getFechaInicio()));
                escritor.campo(negrita, normal, "Fecha de fin:", fecha(datos.getFechaFin()));
                escritor.campo(negrita, normal, "Fecha de emision:", fecha(datos.getFechaEmision()));
            }

            doc.save(salida);
            return salida.toByteArray();
        }
    }

    private static final class Escritor implements AutoCloseable {
        private static final float MARGEN = 60f;
        private static final float TAMANIO = 12f;
        private static final float INTERLINEADO = 16f;
        private final PDDocument documento;
        private PDPageContentStream contenido;
        private float y;

        Escritor(PDDocument documento) throws IOException {
            this.documento = documento;
            nuevaPagina();
        }

        private void nuevaPagina() throws IOException {
            if (contenido != null) contenido.close();
            contenido = null;
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);
            contenido = new PDPageContentStream(documento, pagina);
            y = PDRectangle.A4.getHeight() - 70f;
        }

        void linea(PDFont fuente, float tamanio, String texto) throws IOException {
            if (y < MARGEN) nuevaPagina();
            contenido.beginText();
            contenido.setFont(fuente, tamanio);
            contenido.newLineAtOffset(MARGEN, y);
            contenido.showText(texto);
            contenido.endText();
            y -= Math.max(INTERLINEADO, tamanio + 8f);
        }

        void espacio(float puntos) { y -= puntos; }

        void separador() throws IOException {
            if (y < MARGEN + 25f) nuevaPagina();
            contenido.moveTo(MARGEN, y);
            contenido.lineTo(PDRectangle.A4.getWidth() - MARGEN, y);
            contenido.stroke();
            espacio(28f);
        }

        void parrafo(PDFont fuente, String texto) throws IOException {
            for (String linea : dividir(fuente, texto)) linea(fuente, TAMANIO, linea);
        }

        void campo(PDFont negrita, PDFont normal, String etiqueta, String valor) throws IOException {
            List<String> lineas = dividir(normal, valor);
            float alto = 20f + lineas.size() * 20f;
            // Mantener la etiqueta y su valor juntos cuando el campo cabe en una página.
            if (alto <= PDRectangle.A4.getHeight() - 70f - MARGEN && y - alto < MARGEN) nuevaPagina();
            linea(negrita, TAMANIO, etiqueta);
            for (String linea : lineas) linea(normal, TAMANIO, linea);
            espacio(8f);
        }

        private List<String> dividir(PDFont fuente, String texto) throws IOException {
            String resto = texto.strip();
            List<String> lineas = new ArrayList<>();
            float anchoMaximo = PDRectangle.A4.getWidth() - 2f * MARGEN;
            while (!resto.isEmpty()) {
                int corte = 0, ultimoEspacio = -1;
                float ancho = 0;
                while (corte < resto.length()) {
                    int codigo = resto.codePointAt(corte);
                    int siguiente = corte + Character.charCount(codigo);
                    float anchoCaracter = fuente.getStringWidth(resto.substring(corte, siguiente)) * TAMANIO / 1000f;
                    if (ancho + anchoCaracter > anchoMaximo) break;
                    ancho += anchoCaracter;
                    if (Character.isWhitespace(codigo)) ultimoEspacio = corte;
                    corte = siguiente;
                }
                if (corte == resto.length()) { lineas.add(resto); break; }
                if (corte == 0) throw new IOException("Un carácter excede el ancho de la constancia.");
                int fin = ultimoEspacio > 0 ? ultimoEspacio : corte;
                lineas.add(resto.substring(0, fin).stripTrailing());
                resto = resto.substring(fin).stripLeading();
            }
            return lineas.isEmpty() ? List.of("-") : lineas;
        }

        @Override public void close() throws IOException {
            if (contenido != null) contenido.close();
        }
    }

    private static String fecha(LocalDate valor) {
        return valor == null ? "-" : valor.format(FECHA);
    }

    private static String textoSeguro(String texto) {
        if (texto == null) return "-";
        return texto.replaceAll("\\p{Cntrl}", " ").trim();
    }
}
