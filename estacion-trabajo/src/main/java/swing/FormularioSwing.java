package swing;

import javax.swing.*;
import java.awt.*;

/** Construcción de formularios sin instrumentación del diseñador de IntelliJ. */
final class FormularioSwing {
    private FormularioSwing() {}

    static JPanel campos(Object... pares) {
        JPanel panel = new JPanel(new GridBagLayout());
        for (int i = 0; i < pares.length; i += 2) {
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(5, 8, 5, 8);
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridy = i / 2;
            c.gridx = 0;
            JLabel etiqueta = new JLabel((String) pares[i]);
            JComponent campo = (JComponent) pares[i + 1];
            etiqueta.setLabelFor(campo);
            panel.add(etiqueta, c);
            c.gridx = 1;
            c.weightx = 1;
            panel.add(campo instanceof JTextArea ? new JScrollPane(campo) : campo, c);
        }
        return panel;
    }

    static JPanel botones(JButton... botones) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        for (JButton boton : botones) panel.add(boton);
        return panel;
    }

    static JPanel componer(JPanel botones, JComponent... secciones) {
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        for (JComponent seccion : secciones) contenido.add(seccion);
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(new JScrollPane(contenido), BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(560, 440));
        return panel;
    }
}
