package swing;

import datatypes.*;
import interfaces.ISistema;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/** Verificador headless: una operación compuesta por guardado; ninguna escritura de imagen posterior. */
public final class AltaEventoEdicionAtomicaTest {
    private static final class Fin extends Error { }
    public static void main(String[] args)throws Exception {
        Files.createDirectories(Path.of("estacion-trabajo/target"));
        Path foto=Files.createTempFile(Path.of("estacion-trabajo/target"),"atomica-",".png");
        ImageIO.write(new BufferedImage(2,2,BufferedImage.TYPE_INT_RGB),"png",foto.toFile());
        SwingUtilities.invokeAndWait(()->{
            for(var actor:List.of(Actor.administradorLocal(),Actor.usuarioAutenticado(new DtOrganizador("org","Org","o@test.uy","Desc","")),Actor.usuarioAutenticado(new DtAsistente("as","As","a@test.uy","Ap",null,null)))){
                verificar(actor,foto,false);verificar(actor,foto,true);
            }
            for(var tipo:List.of(AltaEventoPanel.class,AltaEdicionEventoPanel.class,AltaCategoriaPanel.class,AceptarRechazarEdicionPanel.class)){
                try{tipo.getConstructor(Actor.class).newInstance(new Object[]{null});throw new AssertionError("Se permitió actor nulo.");}
                catch(InvocationTargetException e){if(!(e.getCause() instanceof NullPointerException))throw new AssertionError(e);}
                catch(ReflectiveOperationException e){throw new AssertionError(e);}
                for(var c:tipo.getDeclaredConstructors())if(c.getParameterTypes()[0]!=Actor.class)throw new AssertionError("Constructor sin Actor.");
            }
        });
        System.out.println("10 comprobaciones Swing aprobadas: 6 guardados compuestos y 4 barreras de Actor.");
    }
    private static void verificar(Actor actor,Path foto,boolean edicion){
        int[] llamadas={0};
        ISistema s=(ISistema)Proxy.newProxyInstance(ISistema.class.getClassLoader(),new Class<?>[]{ISistema.class},(p,m,a)->switch(m.getName()){
            case "listarCategoriasFormateadas"->List.of("Categoría");
            case "listarEventos"->List.of(new DtEvento("Evento","EV","Desc",null,List.of()));
            case "listarUsuarios"->List.of(new DtOrganizador("org","Org","o@test.uy","Desc",""));
            case "validarImagen"->null;
            case "altaEvento","altaEdicion"->{
                if(a.length!=(edicion?4:3)||a[a.length-1]!=actor||!(a[a.length-2] instanceof DtImagen img)||img.getContenido().length==0)throw new AssertionError("Debe transmitir datos, imagen y Actor en una operación.");
                llamadas[0]++;throw new Fin();
            }
            default->throw new AssertionError("Operación inesperada o escritura separada: "+m.getName());
        });
        Object panel=edicion?new AltaEdicionEventoPanel(actor,s):new AltaEventoPanel(actor,s);
        if(edicion){
            ((JComboBox<?>)campo(panel,"comboEventos")).setSelectedItem("Evento");((JComboBox<?>)campo(panel,"comboOrganizadores")).setSelectedItem("org");
            for(String n:List.of("txtNombreEdicion","txtSigla","txtCiudad","txtPais"))((JTextField)campo(panel,n)).setText("Texto");
        }else{
            for(String n:List.of("txtNombre","txtSigla","txtDescripcion"))((JTextField)campo(panel,n)).setText("Texto");
            ((List<JCheckBox>)campo(panel,"checkCategorias")).getFirst().setSelected(true);
        }
        Object imagen=campo(panel,"imagen");poner(imagen,"seleccion",foto);
        try{var metodo=panel.getClass().getDeclaredMethod(edicion?"guardarEdicion":"agregarEvento");metodo.setAccessible(true);metodo.invoke(panel);throw new AssertionError("No se guardó.");}
        catch(InvocationTargetException e){if(!(e.getCause() instanceof Fin))throw new AssertionError(e.getCause());}
        catch(ReflectiveOperationException e){throw new AssertionError(e);}
        if(llamadas[0]!=1)throw new AssertionError("Debe existir una única escritura.");
    }
    private static Object campo(Object o,String n){try{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}catch(Exception e){throw new AssertionError(e);}}
    private static void poner(Object o,String n,Object v){try{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);}catch(Exception e){throw new AssertionError(e);}}
}
