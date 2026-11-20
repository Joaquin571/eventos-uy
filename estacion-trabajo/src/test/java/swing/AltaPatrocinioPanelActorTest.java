package swing;

import datatypes.*;
import interfaces.ISistema;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.List;

/**
 * Verificador ejecutable con el JDK, sin JUnit, ventanas ni PostgreSQL.
 * Ejecutar después de test-compile:
 * java -Djava.awt.headless=true -cp "estacion-trabajo/target/test-classes;estacion-trabajo/target/classes;logica/target/classes" swing.AltaPatrocinioPanelActorTest
 */
public final class AltaPatrocinioPanelActorTest {
    private AltaPatrocinioPanelActorTest() { }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            comprobarGuardado(Actor.administradorLocal());
            comprobarGuardado(Actor.usuarioAutenticado(
                    new DtOrganizador("organizador", "Organizador", "org@test.uy", "Descripción", "")));
            comprobarGuardado(Actor.usuarioAutenticado(
                    new DtAsistente("asistente", "Asistente", "as@test.uy", "Apellido", LocalDate.of(2000, 1, 1), null)));
            comprobarActorObligatorio();
            comprobarEncapsulamiento();
        });
        System.out.println("5 comprobaciones aprobadas: administrador, organizador, asistente, actor obligatorio y campo final.");
    }

    private static void comprobarGuardado(Actor esperado) {
        Captura captura = new Captura();
        AltaPatrocinioPanel panel = new AltaPatrocinioPanel(esperado, captura.sistema());
        combo(panel, "comboEvento").setSelectedItem("Evento");
        combo(panel, "comboEdicion").setSelectedItem("Edición");
        combo(panel, "comboTipoRegistro").setSelectedItem("General");
        combo(panel, "comboInstitucion").setSelectedItem("Institución");
        combo(panel, "comboNivel").setSelectedItem("ORO");
        ((JTextField) campo(panel, "txtAporte")).setText("1000");
        ((JTextField) campo(panel, "txtCantidadRegistros")).setText("1");
        ((JTextField) campo(panel, "txtCodigo")).setText("CODIGO-SINTETICO");
        try {
            ((JButton) campo(panel, "btnAceptar")).doClick();
            throw new AssertionError("No se invocó la operación de alta.");
        } catch (OperacionCapturada fin) {
            // Se detiene tras invocar lógica, antes del diálogo modal: no sustituye la política de Sistema.
        }
        exigir(captura.invocaciones == 1, "Debe existir una sola llamada al alta.");
        exigir(captura.actor == esperado, "El panel debe transmitir la misma instancia de Actor recibida.");
        exigir(captura.actor.esAdministradorLocal() == esperado.esAdministradorLocal(),
                "El panel no puede elevar privilegios.");
        exigir(java.util.Objects.equals(captura.actor.nickname(), esperado.nickname()), "No debe cambiar la identidad.");
        exigir("Evento".equals(captura.evento) && "Edición".equals(captura.datos.getNombreEdicion()),
                "El guardado debe conservar la selección.");
    }

    private static void comprobarActorObligatorio() {
        try {
            new AltaPatrocinioPanel(null);
            throw new AssertionError("El constructor público debe rechazar un actor ausente.");
        } catch (NullPointerException esperada) { }
        try {
            new AltaPatrocinioPanel(null, new Captura().sistema());
            throw new AssertionError("El constructor interno debe rechazar un actor ausente.");
        } catch (NullPointerException esperada) { }
    }

    private static void comprobarEncapsulamiento() {
        try {
            Field actor = AltaPatrocinioPanel.class.getDeclaredField("actor");
            exigir(Modifier.isPrivate(actor.getModifiers()) && Modifier.isFinal(actor.getModifiers()),
                    "El campo Actor debe ser privado y final.");
            for (var constructor : AltaPatrocinioPanel.class.getDeclaredConstructors()) {
                exigir(constructor.getParameterCount() > 0 && constructor.getParameterTypes()[0] == Actor.class,
                        "Todos los constructores deben exigir Actor.");
            }
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    private static JComboBox<?> combo(AltaPatrocinioPanel panel, String nombre) {
        return (JComboBox<?>) campo(panel, nombre);
    }

    private static Object campo(AltaPatrocinioPanel panel, String nombre) {
        try {
            Field campo = AltaPatrocinioPanel.class.getDeclaredField(nombre);
            campo.setAccessible(true);
            return campo.get(panel);
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }

    /** Señal exclusiva de la prueba para evitar mostrar diálogos; no es una excepción de negocio. */
    private static final class OperacionCapturada extends Error { }

    private static final class Captura {
        private Actor actor;
        private DtPatrocinio datos;
        private String evento;
        private int invocaciones;

        ISistema sistema() {
            return (ISistema) Proxy.newProxyInstance(ISistema.class.getClassLoader(), new Class<?>[]{ISistema.class},
                    (proxy, metodo, argumentos) -> switch (metodo.getName()) {
                        case "listarEventos" -> List.of(new DtEvento("Evento", "EV", "Descripción", null, List.of()));
                        case "listarInstituciones" -> List.of(new DtInstitucion("Institución", "Descripción", ""));
                        case "obtenerEdicionesEvento" -> List.of(new DtEdicion("Edición", "ED", null, null, null,
                                "Ciudad", "País", "organizador"));
                        case "obtenerTiposRegistroEdicion" -> List.of(new DtTipoRegistro("General", "Descripción", 100, 10));
                        case "altaPatrocinio" -> {
                            datos = (DtPatrocinio) argumentos[0];
                            evento = (String) argumentos[1];
                            actor = (Actor) argumentos[2];
                            invocaciones++;
                            throw new OperacionCapturada();
                        }
                        default -> throw new AssertionError("Llamada inesperada: " + metodo.getName());
                    });
        }
    }
}
