import clases.*;
import datatypes.*;
import implementacion.Fabrica;
import interfaces.ISistema;
import manejadores.*;
import persistencia.BaseDeDatos;
import soporte.ProteccionBasePruebas;
import java.time.LocalDate;
import java.util.List;

/** Caracterización de un cliente Java confiable; no es una petición HTTP. */
public class AuditarFronteraLogica {
    static ISistema s;
    static String sufijo=Long.toString(System.nanoTime());
    static int comprobaciones;
    public static void main(String[] args)throws Exception {
        ProteccionBasePruebas.verificar();
        try {
            s=Fabrica.getInstance().getISistema();String org="frOrg"+sufijo,asA="frAsA"+sufijo,asB="frAsB"+sufijo;
            var o=new DtOrganizador(org,"Frontera",org+"@test.uy","Auditoría","");o.setContrasenia("clave");o.setConfirmacionContrasenia("clave");
            comprobar(s.altaOrganizador(o),"Preparación de organizador");asistente(asA);asistente(asB);
            String cat="frCat"+sufijo,ev="frEv"+sufijo,ing="frIngresada"+sufijo,aceptada="frAceptada"+sufijo;
            s.altaCategoria(cat,null);comprobar(s.altaEvento(new DtEvento(ev,"EV","Auditoría",null,List.of(cat))),"Preparación de evento");
            for(String ed:List.of(ing,aceptada)) {
                comprobar(s.altaEdicion(new DtEdicion(ed,"ED",LocalDate.now(),LocalDate.now(),null,"Ciudad","País",org),ev),"Preparación de edición");
                comprobar(s.altaTipoRegistro(new DtTipoRegistro("General","Auditoría",100,1),ed),"Preparación de tipo");
            }
            boolean rechazo=false;
            try{s.registroAEdicion(asA,ing,"General",new DtRegistro(null,0));}
            catch(IllegalArgumentException e){rechazo=e.getMessage().contains("aceptada");}
            comprobar(rechazo,"ISistema rechaza registro en edición Ingresada");
            comprobar(ManejadorRegistros.getInstance().registrar(asA,ing,"General",LocalDate.of(2000,1,1),null),"H07: el manejador público acepta la edición Ingresada desde Java");
            comprobar(s.obtenerRegistrosEdicion(ing).size()==1,"H07: se demuestra persistencia, no solo ejecución de método");
            s.aceptarRechazarEdicion(aceptada,true);
            comprobar(s.registroAEdicion(asA,aceptada,"General",new DtRegistro(null,0)),"Registro aceptado por fachada");
            rechazo=false;try{s.registroAEdicion(asB,aceptada,"General",new DtRegistro(null,0));}
            catch(IllegalArgumentException e){rechazo=e.getMessage().contains("cupo");}
            comprobar(rechazo,"ISistema rechaza segundo registro por cupo");
            var me=ManejadorEventos.getInstance();
            var r=new Registro(LocalDate.now(),-999,me.obtenerTipoRegistro(aceptada,"General"),me.obtenerEdicion(aceptada));
            r.setAsistente((Asistente)ManejadorUsuarios.getInstance().obtenerUsuario(asB));
            comprobar(ManejadorRegistros.getInstance().addRegistro(r),"H07: addRegistro público omite cupo/costo");
            comprobar(s.obtenerRegistrosEdicion(aceptada).size()==2 && s.obtenerRegistrosAsistente(asB).stream().anyMatch(x->x.getCosto()==-999),
                    "H07: costo negativo y sobrecupo quedan persistidos por acceso Java interno");
            var identidad=s.autenticar(asA,"clave");
            s.cambiarContrasenia(asB,"cambiada","cambiada");
            comprobar(identidad.getNickname().equals(asA) && s.autenticar(asB,"cambiada").getNickname().equals(asB),
                    "H08: autenticar devuelve un DTO, no instala un actor autorizado dentro de ISistema");
            String inst="frInst"+sufijo;
            comprobar(s.altaInstitucion(new DtInstitucion(inst,"Auditoría",null)),"Diferencia: la lógica permite sitio web de institución ausente");
            System.out.println("FRONTERA: "+comprobaciones+" caracterizaciones verificadas. Requieren ejecutar Java con la lógica y credenciales de BD; no prueban acceso HTTP.");
        } finally {BaseDeDatos.cerrar();}
    }
    static void asistente(String nick) {
        var u=new DtAsistente(nick,"Frontera",nick+"@test.uy","Apellido",LocalDate.of(2000,1,1),null);
        u.setContrasenia("clave");u.setConfirmacionContrasenia("clave");comprobar(s.altaAsistente(u),"Preparación de asistente");
    }
    static void comprobar(boolean ok,String mensaje){if(!ok)throw new AssertionError(mensaje);comprobaciones++;System.out.println("OK FRONTERA: "+mensaje);}
}
