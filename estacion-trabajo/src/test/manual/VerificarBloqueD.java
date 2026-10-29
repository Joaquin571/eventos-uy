import datatypes.*;
import clases.NivelPatrocinio;
import org.apache.catalina.startup.Tomcat;
import javax.swing.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/** Integración manual con paneles reales y WAR aislado. Sólo eventosuy_test. */
public class VerificarBloqueD extends VerificarSwing {
    static String pendiente, aceptada, rechazada, nuevoUsuario;
    static HttpClient visitante = HttpClient.newHttpClient();
    static String base;

    public static void main(String[] args) throws Exception {
        Tomcat tomcat = null;
        try {
            confirmarBase(); // JDBC de sólo lectura, antes de inicializar JPA o escribir.
            sistema = implementacion.Fabrica.getInstance().getISistema();
            var datos = Files.readAllLines(Path.of("estacion-trabajo/target/persistencia-swing.txt"));
            org = datos.get(0); evento = datos.get(1); edicion = datos.get(2); institucion = datos.get(3); png = Path.of(datos.get(4));
            recuperar(args); // Imágenes de los bloques anteriores, desde otro proceso.
            pendiente = "Pendiente D " + sufijo;
            aceptada = "Aceptada D " + sufijo;
            rechazada = "Rechazada D " + sufijo;
            for (String nombre : List.of(pendiente, aceptada, rechazada)) {
                sistema.altaEdicion(new DtEdicion(nombre, "D", LocalDate.now().plusDays(10),
                        LocalDate.now().plusDays(11), null, "Montevideo", "Uruguay", org), evento);
            }
            sistema.asociarImagen(TipoEntidadImagen.EDICION, aceptada, sistema.obtenerImagen(TipoEntidadImagen.EDICION, edicion));
            sistema.altaTipoRegistro(new DtTipoRegistro("General D", "Descripción", 10, 20), aceptada);
            String codigo = "codigo-d-" + sufijo;
            sistema.altaPatrocinio(new DtPatrocinio(null, 100, 1, codigo, NivelPatrocinio.ORO, institucion, aceptada, "General D"));

            tomcat = new Tomcat();
            tomcat.setBaseDir(Path.of("webapp/target/tomcat-swing-d-" + sufijo).toAbsolutePath().toString());
            tomcat.setPort(0);
            tomcat.getConnector();
            tomcat.addWebapp("/pruebas", Path.of("webapp/target/webapp-1.0-SNAPSHOT").toAbsolutePath().toString());
            tomcat.start();
            base = "http://localhost:" + tomcat.getConnector().getLocalPort() + "/pruebas";
            verificar(get(visitante, "/edicion?nombre=" + url(aceptada)).statusCode() == 404, "Web oculta edición Ingresada antes del cambio");

            SwingUtilities.invokeAndWait(() -> {
                try {
                    cambiarEstado(aceptada, true);
                    cambiarEstado(rechazada, false);
                    probarConsultas(codigo);
                    altaUsuario();
                } catch (Exception ex) { throw new RuntimeException(ex); }
            });

            var detalle = get(visitante, "/edicion?nombre=" + url(aceptada));
            verificar(detalle.statusCode() == 200 && detalle.body().contains(aceptada), "Web ve aceptación de Swing sin reinicio");
            verificar(!detalle.body().contains(codigo), "Detalle público no publica código de patrocinio");
            verificar(get(visitante, "/edicion?nombre=" + url(rechazada)).statusCode() == 404, "Web oculta edición Rechazada");
            verificar(get(visitante, "/edicion?nombre=" + url(pendiente)).statusCode() == 404, "Web mantiene oculta la edición Ingresada");
            var imagen = visitante.send(HttpRequest.newBuilder(URI.create(base + "/imagen?tipo=edicion&id=" + url(aceptada))).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
            verificar(imagen.statusCode() == 200 && Arrays.equals(imagen.body(), Files.readAllBytes(png)), "Web sirve la misma imagen persistida por Swing");
            var cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            var cliente = HttpClient.newBuilder().cookieHandler(cookies).build();
            var login = cliente.send(HttpRequest.newBuilder(URI.create(base + "/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("identificador=" + url(nuevoUsuario) + "&contrasenia=clave-d" )).build(), HttpResponse.BodyHandlers.ofString());
            verificar(login.statusCode() == 302 && !cookies.getCookieStore().getCookies().isEmpty(), "Usuario creado en Swing inicia sesión en web sin reinicio");
            var perfil = get(cliente, "/usuario?nick=" + url(nuevoUsuario));
            verificar(perfil.statusCode() == 200 && perfil.body().contains(nuevoUsuario), "Perfil web del usuario creado en Swing");
            System.out.println("BLOQUE D E INTEGRACIÓN: " + verificaciones + " comprobaciones aprobadas.");
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        } finally {
            if (tomcat != null) { tomcat.stop(); tomcat.destroy(); }
        }
        System.exit(0);
    }

    static void cambiarEstado(String nombre, boolean aceptar) throws Exception {
        var panel = new swing.AceptarRechazarEdicionPanel();
        combo(panel, "comboEvento").setSelectedItem(evento);
        ((JList)campo(panel, "listaEdiciones")).setSelectedValue(nombre, true);
        // La utilidad cierra confirmación con YES (0) y el mensaje posterior con OK (0).
        javax.swing.Timer timer = new javax.swing.Timer(80, e -> {
            for (java.awt.Window w : java.awt.Window.getWindows()) if (w instanceof JDialog d && d.isVisible()) {
                JOptionPane pane = buscar(d, JOptionPane.class);
                if (pane != null) { pane.setValue(JOptionPane.YES_OPTION); d.dispose(); }
            }
        });
        timer.start();
        try {
            var metodo = panel.getClass().getDeclaredMethod("cambiarEstado", boolean.class);
            metodo.setAccessible(true); metodo.invoke(panel, aceptar);
        } finally { timer.stop(); }
        verificar(sistema.obtenerEdicionesEvento(evento).stream().anyMatch(ed -> ed.getIdNombre().equals(nombre)
                && ed.getEstado() == (aceptar ? EstadoEdicion.ACEPTADA : EstadoEdicion.RECHAZADA)), "Cambio de estado desde panel: " + nombre);
    }

    static void probarConsultas(String codigo) throws Exception {
        var ed = new swing.ConsultaEdicionEventoPanel();
        for (var par : Map.of(pendiente, "Ingresada", aceptada, "Aceptada", rechazada, "Rechazada").entrySet()) {
            ed.seleccionarEventoYEdicion(evento, par.getKey());
            verificar(((JTextArea)campo(ed, "areaDetalles")).getText().contains("Estado: " + par.getValue()), "Consulta muestra estado " + par.getValue());
            comprobarVista(ed, par.getKey().equals(aceptada));
        }
        combo(ed, "comboEdiciones").setSelectedIndex(-1);
        comprobarVista(ed, false);
        var ev = new swing.ConsultaEventoPanel(null);
        var eventos = combo(ev, "comboEventos");
        for (int i = 0; i < eventos.getItemCount(); i++) if (((DtEvento)eventos.getItemAt(i)).getNombre().equals(evento)) eventos.setSelectedIndex(i);
        comprobarVista(ev, true);
        eventos.setSelectedIndex(-1); comprobarVista(ev, false);
        var usuario = new swing.ConsultaUsuarioPanel(null); usuario.recargarUsuarios();
        var usuarios = combo(usuario, "cbUsuarios");
        for (int i = 0; i < usuarios.getItemCount(); i++) if (((DtUsuario)usuarios.getItemAt(i)).getNickname().equals(org)) usuarios.setSelectedIndex(i);
        comprobarVista(usuario, true);
        usuarios.setSelectedIndex(-1); comprobarVista(usuario, false);
        var patrocinio = new swing.ConsultaPatrocinioPanel(); patrocinio.refrescarDatos();
        combo(patrocinio, "comboEvento").setSelectedItem(evento);
        combo(patrocinio, "comboEdicion").setSelectedItem(aceptada);
        combo(patrocinio, "comboPatrocinio").setSelectedItem(codigo);
        comprobarVista(patrocinio, true);
        combo(patrocinio, "comboPatrocinio").setSelectedIndex(-1); comprobarVista(patrocinio, false);
    }

    static void comprobarVista(Object panel, boolean tieneImagen) throws Exception {
        JLabel vista = (JLabel)campo(campo(panel, "imagen"), "vista");
        verificar(tieneImagen ? vista.getIcon() != null : vista.getIcon() == null && vista.getText().equals("Sin imagen"),
                panel.getClass().getSimpleName() + (tieneImagen ? " muestra imagen" : " limpia imagen"));
    }

    static void altaUsuario() throws Exception {
        nuevoUsuario = "usuario-d-" + sufijo;
        var panel = new swing.AltaUsuarioPanel();
        texto(panel, "txtNickname").setText(nuevoUsuario);
        texto(panel, "txtNombre").setText("Usuario D"); texto(panel, "txtCorreo").setText(nuevoUsuario + "@test.uy");
        texto(panel, "txtContrasenia").setText("clave-d"); texto(panel, "txtConfirmacion").setText("clave-d");
        texto(panel, "txtApellido").setText("Apellido"); texto(panel, "txtFechaNac").setText("2000-01-01");
        accion(panel, "registrarUsuario");
        verificar(sistema.autenticar(nuevoUsuario, "clave-d") != null, "Alta de usuario con contraseña desde Swing");
    }

    static String url(String texto) { return URLEncoder.encode(texto, java.nio.charset.StandardCharsets.UTF_8); }
    static HttpResponse<String> get(HttpClient cliente, String ruta) throws Exception {
        return cliente.send(HttpRequest.newBuilder(URI.create(base + ruta)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
}
