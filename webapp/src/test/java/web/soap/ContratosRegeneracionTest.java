package web.soap;

import org.junit.jupiter.api.Test;
import org.w3c.dom.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Metro puede reordenar declaraciones globales entre JVM; el orden de campos
 * y los tipos, nombres, acciones y cardinalidades sí deben permanecer idénticos. */
class ContratosRegeneracionTest {
    @Test void fuentesWsdlXsdCoincidenConLosContratosRegenerados()throws Exception {
        for(String servicio:List.of("EventosService","SesionesService","PortalService")){
            for(String extension:List.of(".wsdl","_schema1.xsd")){
                String nombre=servicio+extension;
                Path fuente=Path.of("src/main/resources/wsdl",nombre);
                Path regenerado=Path.of("../servidor-central/target/wsdl",nombre);
                assertTrue(Files.isRegularFile(regenerado),"Ejecutar el reactor con -am para regenerar "+nombre);
                assertEquals(contrato(fuente),contrato(regenerado),"Contrato desactualizado: "+nombre);
            }
        }
    }
    private String contrato(Path archivo)throws Exception {
        var f=DocumentBuilderFactory.newInstance();f.setNamespaceAware(true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD,"");f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA,"");
        return nodo(f.newDocumentBuilder().parse(archivo.toFile()).getDocumentElement());
    }
    private String nodo(Element e){
        List<String> atributos=new ArrayList<>(),hijos=new ArrayList<>();
        for(int i=0;i<e.getAttributes().getLength();i++){
            var a=e.getAttributes().item(i);
            if(XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(a.getNamespaceURI()))continue;
            String valor=a.getNodeValue();
            if(valor.matches("[A-Za-z_][A-Za-z_0-9.-]*:[A-Za-z_][A-Za-z_0-9.-]*")){
                String prefijo=valor.substring(0,valor.indexOf(':'));String ns=e.lookupNamespaceURI(prefijo);
                if(ns!=null)valor="{"+ns+"}"+valor.substring(valor.indexOf(':')+1);
            }
            atributos.add("{"+a.getNamespaceURI()+"}"+a.getLocalName()+"="+valor);
        }
        Collections.sort(atributos);
        var nodos=e.getChildNodes();StringBuilder texto=new StringBuilder();
        for(int i=0;i<nodos.getLength();i++){
            Node n=nodos.item(i);if(n instanceof Element hijo)hijos.add(nodo(hijo));
            else if(n.getNodeType()==Node.TEXT_NODE)texto.append(n.getTextContent().trim());
        }
        boolean wsdl="http://schemas.xmlsoap.org/wsdl/".equals(e.getNamespaceURI())
                &&Set.of("definitions","portType","binding").contains(e.getLocalName());
        boolean esquema=XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(e.getNamespaceURI())&&e.getLocalName().equals("schema");
        if(wsdl||esquema)Collections.sort(hijos);
        return "{"+e.getNamespaceURI()+"}"+e.getLocalName()+atributos+texto+hijos;
    }
}
