package datatypes;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;

/** Fechas civiles ISO-8601, sin zona horaria. */
public class FechaXml extends XmlAdapter<String, LocalDate> {
    public LocalDate unmarshal(String valor) { return valor == null ? null : LocalDate.parse(valor); }
    public String marshal(LocalDate valor) { return valor == null ? null : valor.toString(); }
}