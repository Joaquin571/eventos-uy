package soporte;

import implementacion.Fabrica;
import persistencia.BaseDeDatos;

/** Proceso independiente para verificar que el contador no depende de locks de la JVM. */
public final class RegistrarVisitasProceso {
    public static void main(String[] args) throws Exception {
        ProteccionBasePruebas.verificar();
        try {
            BaseDeDatos.inicializar(BaseDeDatos.propiedadesJpa());
            java.nio.file.Files.writeString(java.nio.file.Path.of(args[3]), "listo");
            long limite = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
            while (!java.nio.file.Files.exists(java.nio.file.Path.of(args[2]))) {
                if (System.nanoTime() > limite) throw new IllegalStateException("No se abrió la barrera de concurrencia.");
                Thread.sleep(20);
            }
            var sistema = Fabrica.getInstance().getISistema();
            for (int i = 0; i < Integer.parseInt(args[1]); i++) sistema.registrarVisitaEvento(args[0], null);
        } finally { BaseDeDatos.cerrar(); }
    }
}
