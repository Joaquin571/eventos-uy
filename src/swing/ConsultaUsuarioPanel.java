package swing;

import datatypes.*;
import implementacion.Sistema;
import interfaces.ISistema;

import javax.swing.*;
import java.awt.*;
import java.util.List;


public class ConsultaUsuarioPanel {
    private JPanel mainPanel;
    private JComboBox<DtUsuario> cbUsuarios;
    private JLabel lblTipoUsuario;
    private JLabel lblNickname;
    private JLabel lblNombre;
    private JLabel lblCorreo;
    private JLabel lblEspecial1;
    private JLabel lblEspecial2;
    private JButton btnCerrar;

    private final ISistema sistema;
    private Runnable accionCerrar = () -> {};

    public ConsultaUsuarioPanel(){
        this.sistema = new Sistema();
        armarUI();
        cbUsuarios.addActionListener(e -> cargarDatosUsuario());
        btnCerrar.addActionListener(e -> accionCerrar.run());
    }

    public JPanel getMainPanel(){
        return mainPanel;
    }
    public void setAccionCerrar(Runnable accionCerrar){
        this.accionCerrar = accionCerrar;
    }
    public void recargarUsuarios(){
        cbUsuarios.removeAllItems();
        List<DtUsuario> usuarios = sistema.listarUsuarios();
        for (DtUsuario u : usuarios){
            cbUsuarios.addItem(u);
        }
        if(usuarios.isEmpty()){
            limpiarCampos();
        }
    }
    private void cargarDatosUsuario(){
        DtUsuario seleccionado = (DtUsuario) cbUsuarios.getSelectedItem();
        if(seleccionado == null){
            limpiarCampos();
            return;
        }
        DtUsuario completo = sistema.obtenerInformacionUsuario(seleccionado.getNickname());
        if(completo == null){
            return;
        }
        lblNickname.setText(completo.getNickname());
        lblNombre.setText(completo.getNickname());
        lblNombre.setText(completo.getNombre());
        lblCorreo.setText(completo.getCorreoElectronico());

        if(completo instanceof DtAsistente){
            DtAsistente a = (DtAsistente) completo;
            lblTipoUsuario.setText("Asistente");
            lblEspecial1.setText("Apellido: " + a.getApellido());
            lblEspecial2.setText("Fecha Nac.: " + (a.getFechaNacimiento() != null ? a.getFechaNacimiento().toString() : "-"));
        }else if (completo instanceof DtOrganizador){
            DtOrganizador o = (DtOrganizador) completo;
            lblTipoUsuario.setText("Organizado");
            lblEspecial1.setText("Sitio Web: " + o.getSitioWeb());
            lblEspecial2.setText("Descripcion: " + o.getDescripcion());
        }

    }
    private void limpiarCampos(){
        lblTipoUsuario.setText("-");
        lblNickname.setText("-");
        lblNombre.setText("-");
        lblCorreo.setText("-");
        lblEspecial1.setText("-");
        lblEspecial2.setText("-");
    }

    private void armarUI(){
        mainPanel = new JPanel(new BorderLayout(10,10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Seleccionar Usuario: "));
        cbUsuarios = new JComboBox<>();
        cbUsuarios.setPreferredSize(new Dimension(250,25));
        topPanel.add(cbUsuarios);

        JPanel detailPanel= new JPanel(new GridLayout(6,2,5,5));
        detailPanel.setBorder(BorderFactory.createTitledBorder("Informacion del Usuario"));

        detailPanel.add(new JLabel("Tipo:"));
        lblTipoUsuario = new JLabel("-");
        detailPanel.add(lblNombre);

        detailPanel.add(new JLabel("Nickname"));
        lblNickname = new JLabel("-");
        detailPanel.add(lblNickname);

        detailPanel.add(new JLabel("Nombre:"));
        lblNombre = new JLabel("-");
        detailPanel.add(lblNombre);

        detailPanel.add(new JLabel("Correo:"));
        lblCorreo = new JLabel("-");
        detailPanel.add(lblCorreo);

        detailPanel.add(new JLabel("Dato Adicional 1:"));
        lblEspecial1 = new JLabel("-");
        detailPanel.add(lblEspecial1);

        detailPanel.add(new JLabel("Dato Adicional 2:"));
        lblEspecial2 = new JLabel("-");
        detailPanel.add(lblEspecial2);

        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCerrar = new JButton("Cerrar");
        botPanel.add(btnCerrar);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(detailPanel, BorderLayout.CENTER);
        mainPanel.add(botPanel, BorderLayout.SOUTH);
    }


}
