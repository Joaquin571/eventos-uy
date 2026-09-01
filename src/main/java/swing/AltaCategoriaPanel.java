package swing;

import interfaces.ISistema;
import implementacion.Fabrica;
import manejadores.ManejadorEventos;
import clases.Categoria;

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
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("Categorías (Sin Padre)");
        ManejadorEventos me = ManejadorEventos.getInstance();
        Collection<Categoria> categorias = me.obtenerCategorias();

        if (categorias != null) {
            for (Categoria c : categorias) {
                // Solo agregamos en la raíz las que no tienen padre
                if (c.getPadre() == null) {
                    DefaultMutableTreeNode nodoPadre = new DefaultMutableTreeNode(c.getNombre());
                    armarNodosHijos(nodoPadre, c);
                    raiz.add(nodoPadre);
                }
            }
        }

        treeCategorias.setModel(new DefaultTreeModel(raiz));

        // Expandir todos los nodos para visualización completa
        for (int i = 0; i < treeCategorias.getRowCount(); i++) {
            treeCategorias.expandRow(i);
        }
    }

    private void armarNodosHijos(DefaultMutableTreeNode nodoPadre, Categoria categoriaPadre) {
        for (Categoria hija : categoriaPadre.getSubcategorias()) {
            DefaultMutableTreeNode nodoHijo = new DefaultMutableTreeNode(hija.getNombre());
            armarNodosHijos(nodoHijo, hija);
            nodoPadre.add(nodoHijo);
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