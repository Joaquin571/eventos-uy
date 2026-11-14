package swing;

import datatypes.DtImagen;
import soporte.ImagenesPrueba;
import soporte.ProteccionBasePruebas;
import javax.swing.*;
import java.nio.file.*;

/** Verificacion automatica sin ventanas; no sustituye la inspeccion visual humana. */
public class VerificarImagenesSegurasSwing {
    public static void main(String[] args) throws Exception {
        ProteccionBasePruebas.verificar();
        SwingUtilities.invokeAndWait(() -> {
            var panel=new ImagenSwing(false);
            byte[][] imagenes={ImagenesPrueba.imagen("png"),ImagenesPrueba.imagen("jpeg"),ImagenesPrueba.gifAnimado(2),
                    ImagenesPrueba.webp(),ImagenesPrueba.webpSinPerdida(),ImagenesPrueba.webpAnimado(2)};
            String[] mime={"image/png","image/jpeg","image/gif","image/webp","image/webp","image/webp"};
            JLabel vista=(JLabel)panel.getComponent(0);
            for(int i=0;i<imagenes.length;i++) {
                panel.mostrar(new DtImagen(mime[i],imagenes[i]));
                if(vista.getIcon()==null)throw new AssertionError("Vista previa no disponible: "+mime[i]+" caso "+i);
            }
            panel.mostrar(new DtImagen("image/png",new byte[]{(byte)137,80,78,71}));
            if(vista.getIcon()!=null || !vista.getText().equals("Vista previa no disponible"))
                throw new AssertionError("Imagen corrupta no rechazada en vista previa");
        });
        Path excesiva=Files.createTempFile("eventosuy-imagen-excesiva-",".png");
        try {
            Files.write(excesiva,new byte[2*1024*1024+1]);
            try { ImagenSwing.leer(excesiva); throw new AssertionError("Archivo excesivo aceptado"); }
            catch(IllegalArgumentException esperada) { }
        } finally { Files.deleteIfExists(excesiva); }
        System.out.println("RESULTADO H11 SWING: 8 verificaciones aprobadas (6 vistas previas, corrupta y lectura limitada).");
    }
}
