package implementacion;

import org.junit.jupiter.api.Test;
import persistencia.BaseDeDatos;
import soporte.PruebaConBaseSegura;
import jakarta.persistence.EntityManager;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class PersistenciaConcurrenteTest extends PruebaConBaseSegura {
    @Test void operacionesConcurrentesUsanMismaFabricaYEntityManagersDistintos()throws Exception {
        var pool=Executors.newFixedThreadPool(8);var inicio=new CyclicBarrier(8);
        List<Future<EntityManager>> resultados=new ArrayList<>();
        List<EntityManager> abiertos=new ArrayList<>();
        try {
            for(int i=0;i<8;i++)resultados.add(pool.submit(()->{
                inicio.await(10,TimeUnit.SECONDS);return BaseDeDatos.getEntityManager();
            }));
            for(var futuro:resultados)abiertos.add(futuro.get(30,TimeUnit.SECONDS));
            var identidades=Collections.newSetFromMap(new IdentityHashMap<EntityManager,Boolean>());
            for(var em:abiertos) {
                assertTrue(em.isOpen());assertTrue(identidades.add(em));
                assertSame(abiertos.getFirst().getEntityManagerFactory(),em.getEntityManagerFactory());
            }
        } finally {
            pool.shutdownNow();assertTrue(pool.awaitTermination(10,TimeUnit.SECONDS));
            for(var em:abiertos)if(em.isOpen())em.close();
        }
    }
}
