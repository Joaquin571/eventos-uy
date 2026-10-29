import persistencia.BaseDeDatos;
import java.sql.DriverManager;
import javax.swing.*;
import java.awt.*;

/** Prueba real de arranque; se ejecuta sólo contra eventosuy_test. */
public class VerificarArranque {
    public static void main(String[] args) throws Exception {
        if (!"eventosuy_test".equals(System.getenv("EVENTOS_DB_NAME"))) throw new IllegalStateException("Seleccione eventosuy_test.");
        var p = BaseDeDatos.propiedadesJpa();
        Class.forName("org.postgresql.Driver");
        try (var c = DriverManager.getConnection(p.get("jakarta.persistence.jdbc.url"), p.get("jakarta.persistence.jdbc.user"), p.get("jakarta.persistence.jdbc.password"))) {
            c.setReadOnly(true);
            try (var s = c.createStatement(); var r = s.executeQuery("select current_database()")) {
                if (!r.next() || !"eventosuy_test".equals(r.getString(1))) throw new IllegalStateException("Base real incorrecta.");
                System.out.println("BASE CONFIRMADA: eventosuy_test");
            }
        }
        SwingUtilities.invokeAndWait(() -> {
            swing.principal.main(new String[0]);
        });
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = (JFrame) java.util.Arrays.stream(Frame.getFrames()).filter(Frame::isVisible).findFirst().orElseThrow();
            int cantidad = revisar(frame);
            if (cantidad != 16) throw new AssertionError("Se esperaban 16 ventanas, hay " + cantidad);
            System.out.println("ARRANQUE CORRECTO: ventana visible y " + cantidad + " casos de uso construidos.");
            if (args.length == 0 || !args[0].equals("visual")) frame.dispose();
        });
        if (args.length == 0 || !args[0].equals("visual")) System.exit(0);
    }
    static int revisar(Container c) {
        int cantidad = 0;
        for (Component hijo : c.getComponents()) {
            if (hijo instanceof JInternalFrame f) {
                if (f.getContentPane().getComponentCount() == 0) throw new AssertionError("Panel vacío: " + f.getTitle());
                f.setVisible(true);
                System.out.println("PANEL: " + f.getTitle());
                f.setVisible(false);
                cantidad++;
            } else if (hijo instanceof Container cont) cantidad += revisar(cont);
        }
        return cantidad;
    }
}
