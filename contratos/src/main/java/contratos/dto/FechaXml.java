package contratos.dto;
import java.time.LocalDate;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
public final class FechaXml extends XmlAdapter<String,LocalDate> {
 public LocalDate unmarshal(String valor) { return valor==null?null:LocalDate.parse(valor); }
 public String marshal(LocalDate valor) { return valor==null?null:valor.toString(); }
}
