package persistencia;

import org.junit.jupiter.api.Test;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import static org.junit.jupiter.api.Assertions.*;

class ConfiguracionBaseTest {
    private Map<String,String> entorno(){return new HashMap<>(Map.of("EVENTOS_DB_HOST","127.0.0.1","EVENTOS_DB_PORT","5432","EVENTOS_DB_NAME","eventosuy_test","EVENTOS_DB_USER","usuario_sintetico","EVENTOS_DB_PASSWORD","clave-sintetica"));}
    @Test void conexionSeConstruyeExclusivamenteConConfiguracionExplicita(){
        var p=BaseDeDatos.propiedadesJpa(entorno());assertEquals("jdbc:postgresql://127.0.0.1:5432/eventosuy_test",p.get("jakarta.persistence.jdbc.url"));
        assertEquals("usuario_sintetico",p.get("jakarta.persistence.jdbc.user"));assertEquals("clave-sintetica",p.get("jakarta.persistence.jdbc.password"));
    }
    @Test void cadaDatoAusenteFallaSinUsarUnaBaseOCredencialDeRespaldo(){
        for(var k:entorno().keySet()){
            var e=entorno();e.remove(k);var error=assertThrows(IllegalStateException.class,()->BaseDeDatos.propiedadesJpa(e));
            assertTrue(error.getMessage().contains(k));assertFalse(error.getMessage().contains("clave-sintetica"));
        }
    }
    @Test void configuracionInvalidaNoExponeSusValores(){
        for(String puerto:List.of("0","65536","texto")){
            var e=entorno();e.put("EVENTOS_DB_PORT",puerto);assertThrows(IllegalStateException.class,()->BaseDeDatos.propiedadesJpa(e));
        }
        var e=entorno();e.put("EVENTOS_DB_HOST","usuario:clave-privada@host");
        var error=assertThrows(IllegalStateException.class,()->BaseDeDatos.propiedadesJpa(e));assertFalse(error.getMessage().contains("clave-privada"));
    }
    @Test void descriptorJpaNoIncluyeConexionNiCredenciales()throws Exception {
        var f=DocumentBuilderFactory.newInstance();f.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        try(var in=ConfiguracionBaseTest.class.getResourceAsStream("/META-INF/persistence.xml")){
            assertNotNull(in);var p=f.newDocumentBuilder().parse(in).getElementsByTagName("property");
            for(int i=0;i<p.getLength();i++)assertFalse(Set.of("jakarta.persistence.jdbc.url","jakarta.persistence.jdbc.user","jakarta.persistence.jdbc.password").contains(p.item(i).getAttributes().getNamedItem("name").getNodeValue()));
        }
    }
}
