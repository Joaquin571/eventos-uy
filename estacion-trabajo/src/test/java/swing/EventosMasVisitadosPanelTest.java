package swing;

import datatypes.*;
import interfaces.ISistema;
import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Prueba ejecutable con JDK, sin ventanas ni base de datos. */
public final class EventosMasVisitadosPanelTest {
    public static void main(String[] args) throws Exception {
        Actor actor = Actor.administradorLocal();
        AtomicInteger llamadas = new AtomicInteger();
        CountDownLatch entrada = new CountDownLatch(1), continuar = new CountDownLatch(1);
        ISistema sistema = (ISistema) Proxy.newProxyInstance(ISistema.class.getClassLoader(),
                new Class<?>[]{ISistema.class}, (proxy, metodo, argumentos) -> {
                    exigir(metodo.getName().equals("obtenerEventosMasVisitados"), "Solo debe consultar el ranking");
                    exigir(!SwingUtilities.isEventDispatchThread(), "La consulta bloquea Swing");
                    exigir(argumentos[0] == actor, "Debe conservar el actor");
                    return switch (llamadas.incrementAndGet()) {
                        case 1 -> {
                            entrada.countDown();
                            exigir(continuar.await(5, TimeUnit.SECONDS), "No se liberó la consulta");
                            yield List.of(new DtEventoVisitado("Alfa", 80, true), new DtEventoVisitado("Beta", 0, false));
                        }
                        case 2 -> List.of();
                        case 3 -> throw new IllegalStateException("Fallo simulado");
                        default -> List.of(new DtEventoVisitado("Nuevo", 90, false));
                    };
                });
        EventosMasVisitadosPanel[] panel = new EventosMasVisitadosPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            panel[0] = new EventosMasVisitadosPanel(actor, sistema);
            panel[0].actualizar();
        });
        exigir(entrada.await(5, TimeUnit.SECONDS), "No empezó la consulta");
        SwingUtilities.invokeAndWait(() -> {
            exigir(!boton(panel[0]).isEnabled(), "Debe deshabilitar Actualizar mientras carga");
            panel[0].actualizar(); // No dispara una segunda consulta concurrente.
        });
        continuar.countDown();
        esperar(panel[0]);
        SwingUtilities.invokeAndWait(() -> {
            JTable tabla = campo(panel[0], "tabla", JTable.class);
            exigir(tabla.getRowCount() == 2, "Faltan filas");
            exigir(tabla.getValueAt(0, 0).equals(1) && tabla.getValueAt(0, 1).equals("Alfa")
                    && tabla.getValueAt(0, 2).equals(80L) && tabla.getValueAt(0, 3).equals("Finalizado"), "Datos incorrectos");
            exigir(!tabla.isCellEditable(0, 1), "El ranking no es editable");
            boton(panel[0]).doClick();
        });
        esperar(panel[0]);
        SwingUtilities.invokeAndWait(() -> {
            exigir(campo(panel[0], "tabla", JTable.class).getRowCount() == 0, "Actualizar debe quitar filas anteriores");
            exigir(estado(panel[0]).getText().equals("Todavía no hay eventos."), "Falta estado vacío");
            boton(panel[0]).doClick();
        });
        esperar(panel[0]);
        SwingUtilities.invokeAndWait(() -> {
            exigir(estado(panel[0]).getText().contains("No se pudo"), "Falta manejo de error");
            boton(panel[0]).doClick();
        });
        esperar(panel[0]);
        SwingUtilities.invokeAndWait(() -> exigir(campo(panel[0], "tabla", JTable.class).getValueAt(0, 1).equals("Nuevo"), "No se recuperó del error"));
        exigir(llamadas.get() == 4, "Cantidad incorrecta de consultas");
        System.out.println("Ranking Swing aprobado: tabla, actor, EDT libre, actualización, vacío, error y reintento.");
    }
    private static void esperar(EventosMasVisitadosPanel panel) throws Exception {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
        while (true) {
            boolean[] listo = {false};
            SwingUtilities.invokeAndWait(() -> listo[0] = boton(panel).isEnabled());
            if (listo[0]) return;
            exigir(System.nanoTime() < limite, "La actualización no terminó");
            Thread.sleep(10);
        }
    }
    private static JButton boton(EventosMasVisitadosPanel p) { return campo(p, "actualizar", JButton.class); }
    private static JLabel estado(EventosMasVisitadosPanel p) { return campo(p, "estado", JLabel.class); }
    private static <T> T campo(Object objeto, String nombre, Class<T> tipo) {
        try { Field f = objeto.getClass().getDeclaredField(nombre); f.setAccessible(true); return tipo.cast(f.get(objeto)); }
        catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    private static void exigir(boolean condicion, String mensaje) { if (!condicion) throw new AssertionError(mensaje); }
}
