package swing;

import interfaces.ISistema;
import implementacion.Fabrica;
import datatypes.DtCategoria;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.Collection;

public class AltaCategoriaPanel extends JPanel {

    private JTextField txtNombre;
    private JTree treeCategorias;
    private ISistema sistema;
    private Runnable accionCerrar;

    public AltaCategoriaPanel() {
        sistema = Fabrica.getInstance().getISistema();
        setLayout(new BorderLayout(10, 10));

        // Panel de Árbol (Izquierda/Centro)
        JPanel panelArbol = new JPanel(new BorderLayout());
        panelArbol.setBorder(BorderFactory.createTitledBorder("Seleccionar Categoría Padre (Opcional)"));

        treeCategorias = new JTree();
        panelArbol.add(new JScrollPane(treeCategorias), BorderLayout.CENTER);
        add(panelArbol, BorderLayout.CENTER);

        // Formulario (Inferior)
        JPanel panelForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelForm.add(new JLabel("Nombre de la Categoría:"));

        txtNombre = new JTextField(15);
        panelForm.add(txtNombre);

        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        panelForm.add(btnGuardar);
        panelForm.add(btnCancelar);

        add(panelForm, BorderLayout.SOUTH);

        // Eventos
        btnGuardar.addActionListener(e -> guardarCategoria());
        btnCancelar.addActionListener(e -> {
            if (accionCerrar != null) accionCerrar.run();
        });

        cargarArbolCategorias();
    }


    public void cargarArbolCategorias() {
        DefaultMutableTreeNode raiz =
                new DefaultMutableTreeNode("Categorías (Sin Padre)");

        Collection<DtCategoria> categorias = sistema.listarCategorias();
        Map<String, DefaultMutableTreeNode> nodos = new HashMap<>();

        for (DtCategoria categoria : categorias) {
            nodos.put(
                    categoria.getIdNombre(),
                    new DefaultMutableTreeNode(categoria.getIdNombre())
            );
        }

        for (DtCategoria categoria : categorias) {
            DefaultMutableTreeNode nodo = nodos.get(categoria.getIdNombre());
            String nombrePadre = categoria.getNombrePadre();

            if (nombrePadre == null || nombrePadre.isBlank()) {
                raiz.add(nodo);
            } else {
                DefaultMutableTreeNode padre = nodos.get(nombrePadre);

                if (padre != null) {
                    padre.add(nodo);
                } else {
                    raiz.add(nodo);
                }
            }
        }

        treeCategorias.setModel(new DefaultTreeModel(raiz));

        for (int i = 0; i < treeCategorias.getRowCount(); i++) {
            treeCategorias.expandRow(i);
        }
    }


    private void guardarCategoria() {
        String nombre = txtNombre.getText().trim();
        String nombrePadre = null;

        DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) treeCategorias.getLastSelectedPathComponent();

        // Si seleccionó un nodo del árbol que no sea la raíz default
        if (nodoSeleccionado != null && !nodoSeleccionado.isRoot()) {
            nombrePadre = nodoSeleccionado.getUserObject().toString();
        }

        try {
            sistema.altaCategoria(nombre, nombrePadre);
            JOptionPane.showMessageDialog(this, "Categoría creada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            txtNombre.setText("");
            cargarArbolCategorias();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    public JPanel getMainPanel() {
        return this;
    }
}