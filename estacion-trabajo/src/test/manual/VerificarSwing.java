import datatypes.*;
import implementacion.Fabrica;
import interfaces.ISistema;
import persistencia.BaseDeDatos;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.lang.reflect.*;
import java.nio.file.*;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.*;

/** Pruebas funcionales sobre paneles reales. Sólo crea datos únicos en eventosuy_test. */
public class VerificarSwing {
    static ISistema sistema;
    static int verificaciones;
    static String mensaje;
    static String sufijo = String.valueOf(System.nanoTime());
    static String org = "swing-org-" + sufijo, asistente = "swing-as-" + sufijo;
    static Path png;
    static String[] argsGlobales;
    public static void main(String[] args) throws Exception {
        try {
            confirmarBase();
            sistema = Fabrica.getInstance().getISistema();
            if (args.length > 0 && args[0].equals("recuperar")) {
                recuperar(args); System.exit(0);
            }
            png = Files.createTempFile(Path.of("estacion-trabajo/target"), "imagen-swing-", ".png");
            ImageIO.write(new BufferedImage(24, 12, BufferedImage.TYPE_INT_RGB), "png", png.toFile());
            var o = new DtOrganizador(org, "Organizador", org+"@test.uy", "Descripción", "");
            o.setContrasenia("anterior"); o.setConfirmacionContrasenia("anterior"); sistema.altaOrganizador(o);
            var a = new DtAsistente(asistente, "Asistente", asistente+"@test.uy", "Apellido", LocalDate.of(2000,1,1), null);
            a.setContrasenia("anterior"); a.setConfirmacionContrasenia("anterior"); sistema.altaAsistente(a);
            SwingUtilities.invokeAndWait(() -> {
                try { probarModificacion(); if (args.length > 0 && args[0].equals("completo")) probarAltasConsultas(); }
                catch (Exception ex) { throw new RuntimeException(ex); }
            });
            System.out.println("PRUEBAS SWING: " + verificaciones + " comprobaciones aprobadas.");
            System.exit(0);
        } catch (Throwable ex) { ex.printStackTrace(); System.exit(1); }
    }
    static void confirmarBase() throws Exception {
        if (!"eventosuy_test".equals(System.getenv("EVENTOS_DB_NAME"))) throw new IllegalStateException("Seleccione eventosuy_test.");
        var p = BaseDeDatos.propiedadesJpa(); Class.forName("org.postgresql.Driver");
        try (var c = DriverManager.getConnection(p.get("jakarta.persistence.jdbc.url"),p.get("jakarta.persistence.jdbc.user"),p.get("jakarta.persistence.jdbc.password"))) {
            c.setReadOnly(true);
            try (var s=c.createStatement();var r=s.executeQuery("select current_database()")) {
                if (!r.next() || !"eventosuy_test".equals(r.getString(1))) throw new IllegalStateException("Base real incorrecta.");
                System.out.println("BASE CONFIRMADA ANTES DE ESCRIBIR: eventosuy_test");
            }
        }
    }
    static void probarModificacion() throws Exception {
        var p = new swing.ModificarUsuarioPanel(); p.refrescarUsuarios();
        combo(p,"comboUsuarios").setSelectedItem(org);
        verificar(!texto(p,"txtNickname").isEditable() && !texto(p,"txtCorreo").isEditable(), "Nickname y correo no editables");
        texto(p,"txtNombre").setText("Organizador cambiado");
        texto(p,"txtContrasenia").setText("nueva"); texto(p,"txtConfirmacion").setText("distinta");
        accion(p,"guardarCambios");
        verificar(mensaje.contains("no coinciden"), "Error de confirmación visible");
        verificar(sistema.consultarUsuario(org).getNombre().equals("Organizador"), "Confirmación inválida no modifica datos");
        verificar(texto(p,"txtNombre").getText().equals("Organizador cambiado"), "Error conserva datos ingresados");
        texto(p,"txtConfirmacion").setText("nueva"); seleccionarImagen(p,png);
        accion(p,"guardarCambios");
        verificar(mensaje.contains("correctamente"), "Éxito de modificación");
        verificar(sistema.autenticar(org,"nueva") != null, "Autenticación con contraseña nueva");
        verificar(sistema.consultarUsuario(org).getNombre().equals("Organizador cambiado"), "Modificación persistida");
        verificar(sistema.consultarUsuario(org).getCorreoElectronico().equals(org+"@test.uy"), "Correo preservado");
        verificar(Arrays.equals(sistema.obtenerImagen(TipoEntidadImagen.USUARIO,org).getContenido(),Files.readAllBytes(png)), "Imagen de usuario persistida");
        verificar(texto(p,"txtContrasenia").getText().isEmpty(), "Contraseña vaciada después de guardar");
        texto(p,"txtNombre").setText("Sin cambiar contraseña"); accion(p,"guardarCambios");
        verificar(sistema.autenticar(org,"nueva") != null, "Campos vacíos conservan contraseña");
        combo(p,"comboUsuarios").setSelectedItem(asistente);
        verificar(texto(p,"txtContrasenia").getText().isEmpty(), "Cambiar usuario limpia contraseña");
        texto(p,"txtFechaNacimiento").setText(LocalDate.now().plusDays(1).toString()); accion(p,"guardarCambios");
        verificar(mensaje.contains("anterior"), "Excepción de negocio mostrada");
        texto(p,"txtFechaNacimiento").setText("2000-01-01"); texto(p,"txtContrasenia").setText("nueva-as"); texto(p,"txtConfirmacion").setText("nueva-as");
        seleccionarImagen(p,png); accion(p,"guardarCambios");
        verificar(sistema.autenticar(asistente,"nueva-as") != null, "Modificación de asistente con contraseña");
        Path falso=Files.createTempFile(Path.of("estacion-trabajo/target"),"falso-", ".png");Files.writeString(falso,"no es imagen");
        seleccionarImagen(p,falso);texto(p,"txtNombre").setText("No debe guardarse");accion(p,"guardarCambios");
        verificar(mensaje.contains("imagen válida"), "Firma incorrecta rechazada");
        verificar(!sistema.consultarUsuario(asistente).getNombre().equals("No debe guardarse"), "Imagen se valida antes de modificar");
        Path grande=Files.createTempFile(Path.of("estacion-trabajo/target"),"grande-", ".png"); Files.write(grande,new byte[2*1024*1024+1]);
        boolean rechazo=false;try {seleccionarImagen(p,grande);}catch(InvocationTargetException ex){rechazo=ex.getCause() instanceof IllegalArgumentException;}
        verificar(rechazo,"Imagen mayor a 2 MiB rechazada");
    }
    static Object campo(Object o,String n) throws Exception {Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
    static JTextField texto(Object o,String n) throws Exception {return (JTextField)campo(o,n);}
    static JComboBox combo(Object o,String n) throws Exception {return (JComboBox)campo(o,n);}
    static void seleccionarImagen(Object p,Path archivo) throws Exception {
        Object imagen=campo(p,"imagen");Method m=imagen.getClass().getDeclaredMethod("seleccionar",Path.class);m.setAccessible(true);m.invoke(imagen,archivo);
    }
    static void accion(Object p,String metodo) throws Exception {
        mensaje=null;
        javax.swing.Timer timer=new javax.swing.Timer(80,e -> {
            for(Window w:Window.getWindows())if(w instanceof JDialog d && d.isVisible()) {
                JOptionPane pane=buscar(d,JOptionPane.class);
                if(pane!=null){mensaje=String.valueOf(pane.getMessage());pane.setValue(JOptionPane.OK_OPTION);d.dispose();}
            }
        });
        timer.start();
        try {Method m=p.getClass().getDeclaredMethod(metodo);m.setAccessible(true);m.invoke(p);}finally{timer.stop();}
    }
    static <T> T buscar(Container c,Class<T> tipo) {
        for(Component hijo:c.getComponents()) {if(tipo.isInstance(hijo))return tipo.cast(hijo);if(hijo instanceof Container cont){T r=buscar(cont,tipo);if(r!=null)return r;}}
        return null;
    }
    static void verificar(boolean condicion,String descripcion) {
        if(!condicion)throw new AssertionError(descripcion+" / mensaje: "+mensaje);
        verificaciones++;System.out.println("OK: "+descripcion);
    }
    static String evento, edicion, institucion;
    static void probarAltasConsultas() throws Exception {
        String categoria = "Categoría Swing " + sufijo;
        sistema.altaCategoria(categoria, null);
        institucion = "Institución Swing " + sufijo;
        var inst = new swing.AltaInstitucionPanel();
        texto(inst,"txtNombre").setText(institucion); ((JTextArea)campo(inst,"txtDescripcion")).setText("Descripción");
        seleccionarImagen(inst,png); accion(inst,"guardarInstitucion");
        verificar(sistema.existeInstitucion(institucion),"Alta de institución desde formulario");
        comprobarImagen(TipoEntidadImagen.INSTITUCION,institucion);
        texto(inst,"txtNombre").setText(institucion+" sin imagen");((JTextArea)campo(inst,"txtDescripcion")).setText("Descripción");accion(inst,"guardarInstitucion");
        verificar(sistema.existeInstitucion(institucion+" sin imagen"),"Institución sin imagen");
        verificar(sistema.obtenerImagen(TipoEntidadImagen.INSTITUCION,institucion+" sin imagen")==null,"No arrastra imagen entre altas");
        evento="Evento Swing "+sufijo;
        var ev=new swing.AltaEventoPanel();
        prepararEvento(ev,evento,categoria);seleccionarImagen(ev,png);accion(ev,"agregarEvento");
        verificar(sistema.listarEventos().stream().anyMatch(e->e.getNombre().equals(evento)),"Alta de evento desde formulario");
        comprobarImagen(TipoEntidadImagen.EVENTO,evento);
        prepararEvento(ev,evento+" sin imagen",categoria);accion(ev,"agregarEvento");
        verificar(sistema.listarEventos().stream().anyMatch(e->e.getNombre().equals(evento+" sin imagen")),"Evento sin imagen");
        Path falso=Files.createTempFile(Path.of("estacion-trabajo/target"),"falso-evento-", ".png");Files.writeString(falso,"falso");
        prepararEvento(ev,evento+" inválido",categoria);seleccionarImagen(ev,falso);accion(ev,"agregarEvento");
        verificar(sistema.listarEventos().stream().noneMatch(e->e.getNombre().equals(evento+" inválido")),"Imagen inválida no crea evento");
        verificar(texto(ev,"txtNombre").getText().equals(evento+" inválido"),"Alta fallida conserva formulario");
        edicion="Edición Swing "+sufijo;
        var ed=new swing.AltaEdicionEventoPanel();prepararEdicion(ed,edicion);seleccionarImagen(ed,png);accion(ed,"guardarEdicion");
        verificar(sistema.obtenerEdicionesEvento(evento).stream().anyMatch(e->e.getIdNombre().equals(edicion)&&e.getEstado()==EstadoEdicion.INGRESADA),"Alta de edición nace Ingresada");
        comprobarImagen(TipoEntidadImagen.EDICION,edicion);
        prepararEdicion(ed,edicion+" sin imagen");accion(ed,"guardarEdicion");
        verificar(sistema.obtenerEdicionesEvento(evento).stream().anyMatch(e->e.getIdNombre().equals(edicion+" sin imagen")),"Edición sin imagen");
        Files.writeString(Path.of("estacion-trabajo/target/persistencia-swing.txt"),String.join("\n",org,evento,edicion,institucion,png.toAbsolutePath().toString()));
    }
    static void prepararEvento(Object p,String nombre,String categoria) throws Exception {
        texto(p,"txtNombre").setText(nombre);texto(p,"txtSigla").setText("SW");texto(p,"txtDescripcion").setText("Descripción");
        for(Object c:(java.util.List)campo(p,"checkCategorias")){JCheckBox check=(JCheckBox)c;check.setSelected(check.getText().contains(categoria));}
    }
    static void prepararEdicion(Object p,String nombre) throws Exception {
        combo(p,"comboEventos").setSelectedItem(evento);combo(p,"comboOrganizadores").setSelectedItem(org);
        texto(p,"txtNombreEdicion").setText(nombre);texto(p,"txtSigla").setText("SW");texto(p,"txtCiudad").setText("Montevideo");texto(p,"txtPais").setText("Uruguay");
    }
    static void comprobarImagen(TipoEntidadImagen tipo,String clave) throws Exception {
        DtImagen imagen=sistema.obtenerImagen(tipo,clave);
        verificar(imagen!=null && imagen.getTipoContenido().equals("image/png") && Arrays.equals(imagen.getContenido(),Files.readAllBytes(png)),"Persistencia de imagen "+tipo);
    }
    static void recuperar(String[] args) throws Exception {
        var datos=Files.readAllLines(Path.of("estacion-trabajo/target/persistencia-swing.txt"));
        png=Path.of(datos.get(4));
        comprobarImagen(TipoEntidadImagen.USUARIO,datos.get(0));comprobarImagen(TipoEntidadImagen.EVENTO,datos.get(1));
        comprobarImagen(TipoEntidadImagen.EDICION,datos.get(2));comprobarImagen(TipoEntidadImagen.INSTITUCION,datos.get(3));
        System.out.println("RECUPERACIÓN EN OTRO PROCESO: "+verificaciones+" imágenes verificadas.");
    }
}
