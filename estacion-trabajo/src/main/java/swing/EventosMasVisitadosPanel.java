package swing;

import datatypes.Actor;
import datatypes.DtEventoVisitado;
import implementacion.Fabrica;
import interfaces.ISistema;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public final class EventosMasVisitadosPanel extends JPanel {
    private final ISistema sistema;
    private final Actor actor;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Posición", "Nombre del evento", "Visitas", "Estado"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
        @Override public Class<?> getColumnClass(int columna) {
            return columna == 0 ? Integer.class : columna == 2 ? Long.class : String.class;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JButton actualizar = new JButton("Actualizar");
    private final JLabel estado = new JLabel("Pulse Actualizar para consultar el ranking.");

    public EventosMasVisitadosPanel(Actor actor) {
        this(actor, Fabrica.getInstance().getISistema());
    }

    EventosMasVisitadosPanel(Actor actor, ISistema sistema) {
        super(new BorderLayout(12, 12));
        this.actor = Objects.requireNonNull(actor);
        this.sistema = Objects.requireNonNull(sistema);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setPreferredSize(new Dimension(650, 340));
        JLabel titulo = new JLabel("Eventos más visitados");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        add(titulo, BorderLayout.NORTH);
        tabla.setFillsViewportHeight(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout(8, 8));
        pie.add(estado, BorderLayout.CENTER);
        pie.add(actualizar, BorderLayout.EAST);
        add(pie, BorderLayout.SOUTH);
        actualizar.addActionListener(e -> actualizar());
    }

    public void actualizar() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::actualizar);
            return;
        }
        if (!actualizar.isEnabled()) return;
        actualizar.setEnabled(false);
        estado.setToolTipText(null);
        estado.setText("Consultando…");
        new SwingWorker<List<DtEventoVisitado>, Void>() {
            @Override protected List<DtEventoVisitado> doInBackground() {
                return sistema.obtenerEventosMasVisitados(actor);
            }
            @Override protected void done() {
                try {
                    List<DtEventoVisitado> eventos = get();
                    modelo.setRowCount(0);
                    for (DtEventoVisitado evento : eventos) {
                        modelo.addRow(new Object[]{modelo.getRowCount() + 1, evento.getNombre(),
                                evento.getVisitas(), evento.isFinalizado() ? "Finalizado" : "Activo"});
                    }
                    estado.setText(eventos.isEmpty() ? "Todavía no hay eventos." : "Ranking actualizado.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    estado.setText("Consulta interrumpida. Puede volver a actualizar.");
                } catch (ExecutionException e) {
                    estado.setText("No se pudo actualizar el ranking. Puede reintentar.");
                    estado.setToolTipText(e.getCause() instanceof excepciones.AccesoDenegadoException
                            ? e.getCause().getMessage() : "Compruebe la conexión e intente nuevamente.");
                } finally {
                    actualizar.setEnabled(true);
                }
            }
        }.execute();
    }
}
