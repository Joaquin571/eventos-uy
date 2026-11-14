package implementacion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class SingletonConcurrenteTest {
    @ParameterizedTest
    @ValueSource(strings={"implementacion.Fabrica","manejadores.ManejadorUsuarios","manejadores.ManejadorEventos",
            "manejadores.ManejadorInstituciones","manejadores.ManejadorPatrocinios","manejadores.ManejadorRegistros",
            "manejadores.ManejadorImagenes"})
    void primeraInicializacionPublicaUnaSolaInstancia(String nombre)throws Exception {
        // Aísla solo la clase singleton para comprobar su PRIMER acceso, sin resetear campos estáticos.
        ClassLoader cargador=new ClassLoader(getClass().getClassLoader()) {
            @Override protected Class<?> loadClass(String n,boolean resolver)throws ClassNotFoundException {
                if(!n.equals(nombre))return super.loadClass(n,resolver);
                synchronized(getClassLoadingLock(n)) {
                    Class<?> clase=findLoadedClass(n);
                    if(clase==null)try(var in=getParent().getResourceAsStream(n.replace('.','/')+".class")) {
                        if(in==null)throw new ClassNotFoundException(n);
                        byte[] bytes=in.readAllBytes();clase=defineClass(n,bytes,0,bytes.length);
                    } catch(java.io.IOException e){throw new ClassNotFoundException(n,e);}
                    if(resolver)resolveClass(clase);return clase;
                }
            }
        };
        var metodo=cargador.loadClass(nombre).getMethod("getInstance");
        var pool=Executors.newFixedThreadPool(16);var inicio=new CyclicBarrier(16);
        try {
            List<Future<Object>> resultados=new ArrayList<>();
            for(int i=0;i<16;i++)resultados.add(pool.submit(()->{inicio.await(10,TimeUnit.SECONDS);return metodo.invoke(null);}));
            Object unica=resultados.getFirst().get(10,TimeUnit.SECONDS);
            for(var futuro:resultados)assertSame(unica,futuro.get(10,TimeUnit.SECONDS));
        } finally {pool.shutdownNow();assertTrue(pool.awaitTermination(10,TimeUnit.SECONDS));}
    }
    @Test void fabricaConservaFachadasIndependientes() {
        assertNotSame(Fabrica.getInstance().getISistema(),Fabrica.getInstance().getISistema());
    }
}
