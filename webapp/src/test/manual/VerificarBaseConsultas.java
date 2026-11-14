import persistencia.BaseDeDatos;
import java.sql.DriverManager;
public class VerificarBaseConsultas {
    public static void main(String[] args) throws Exception {
        if (!"eventosuy_test".equals(System.getenv("EVENTOS_DB_NAME"))) throw new IllegalStateException("Base no seleccionada.");
        var p=BaseDeDatos.propiedadesJpa();String url=p.get("jakarta.persistence.jdbc.url");
        if(!url.endsWith("/eventosuy_test"))throw new IllegalStateException("URL insegura.");
        Class.forName("org.postgresql.Driver");
        try(var c=DriverManager.getConnection(url,p.get("jakarta.persistence.jdbc.user"),p.get("jakarta.persistence.jdbc.password"))) {
            c.setReadOnly(true);
            try(var s=c.createStatement();var r=s.executeQuery("SELECT current_database()")) {
                if(!r.next()||!"eventosuy_test".equals(r.getString(1)))throw new IllegalStateException("Base real incorrecta.");
                System.out.println("BASE CONFIRMADA ANTES DE ESCRIBIR: "+r.getString(1)+" / "+url);
            }
        }
    }
}
