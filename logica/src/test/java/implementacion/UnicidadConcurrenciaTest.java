package implementacion;

import clases.*;
import datatypes.*;
import interfaces.ISistema;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import persistencia.BaseDeDatos;
import soporte.PruebaConBaseSegura;

import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/** Casos de uso reales, conexiones independientes y comprobación del estado persistido. */
class UnicidadConcurrenciaTest extends PruebaConBaseSegura {
    private final ISistema sistema=Fabrica.getInstance().getISistema();
    private final String sufijo=Long.toString(System.nanoTime());
    private final String org="orgU"+sufijo, evento="eventoU"+sufijo, ed="edU"+sufijo;
    private int secuencia;

    private DtOrganizador organizador(String nick,String correo) {
        var dt=new DtOrganizador(nick,"Nombre",correo,"Descripción","");
        dt.setContrasenia("clave");dt.setConfirmacionContrasenia("clave");return dt;
    }
    private void preparar(int cupo)throws Exception {
        assertTrue(sistema.altaOrganizador(organizador(org,org+"@test.uy")));
        String cat="catU"+sufijo;sistema.altaCategoria(cat,null);
        assertTrue(sistema.altaEvento(new DtEvento(evento,"EV","Descripción",null,List.of(cat))));
        assertTrue(sistema.altaEdicion(edicion(ed),evento));
        sistema.aceptarRechazarEdicion(ed,true);
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General","Descripción",10,cupo),ed));
    }
    private DtEdicion edicion(String nombre) {
        return new DtEdicion(nombre,"ED",LocalDate.now(),LocalDate.now(),null,"Ciudad","País",org);
    }
    private String asistente(String institucion) {
        String nick="asU"+(++secuencia)+sufijo;
        var dt=new DtAsistente(nick,"Nombre",nick+"@test.uy","Apellido",LocalDate.of(2000,1,1),institucion);
        dt.setContrasenia("clave");dt.setConfirmacionContrasenia("clave");
        assertTrue(sistema.altaAsistente(dt));return nick;
    }
    private String institucion() {
        String nombre="instU"+(++secuencia)+sufijo;
        assertTrue(sistema.altaInstitucion(new DtInstitucion(nombre,"Descripción","")));return nombre;
    }
    private DtPatrocinio patrocinio(String codigo,String institucion) {
        return new DtPatrocinio(null,1000,1,codigo,NivelPatrocinio.ORO,institucion,ed,"General");
    }
    private long contar(String jpql,String clave) {
        try(var em=BaseDeDatos.getEntityManager()) {
            return em.createQuery(jpql,Long.class).setParameter("clave",clave).getSingleResult();
        }
    }

    @Test void nicknameIgualNoSeDuplicaConDosAltas()throws Exception {
        String nick="nickU"+sufijo;
        unaSola(()->sistema.altaOrganizador(organizador(nick,"a"+sufijo+"@test.uy")),
                ()->sistema.altaOrganizador(organizador(nick,"b"+sufijo+"@test.uy")));
        assertEquals(1,contar("SELECT COUNT(u) FROM Usuario u WHERE u.nickname=:clave",nick));
    }
    @Test void correoExactoNoSeDuplicaConNicknamesDistintos()throws Exception {
        String correo="correoU"+sufijo+"@test.uy";
        unaSola(()->sistema.altaOrganizador(organizador("a"+sufijo,correo)),
                ()->sistema.altaOrganizador(organizador("b"+sufijo,correo)));
        assertEquals(1,contar("SELECT COUNT(u) FROM Usuario u WHERE u.correoElectronico=:clave",correo));
    }
    @Test void correosEquivalentesNoSeDuplicanYAutenticanConCualquierMayuscula()throws Exception {
        exigirIndicesH13();
        String correo="correoCi"+sufijo+"@test.uy",a="ciA"+sufijo,b="ciB"+sufijo;
        String otra=correo.toUpperCase(java.util.Locale.ROOT);
        unaSola(()->sistema.altaOrganizador(organizador(a,correo)),
                ()->sistema.altaOrganizador(organizador(b,otra)));
        assertEquals(1,contar("SELECT COUNT(u) FROM Usuario u WHERE LOWER(u.correoElectronico)=LOWER(:clave)",correo));
        var autenticado=sistema.autenticar(otra,"clave");
        assertTrue(List.of(a,b).contains(autenticado.getNickname()));
        assertTrue(List.of(correo,otra).contains(autenticado.getCorreoElectronico()),"No se normalizan los datos guardados.");
    }
    @Test void indiceCorreoResuelveCarreraAunqueAmbasTransaccionesHayanVistoCero()throws Exception {
        exigirIndicesH13();
        String correo="sqlCi"+sufijo+"@test.uy";var leyeron=new CyclicBarrier(2);
        unaSola(()->insertarCorreoDirecto("sqlA"+sufijo,correo,leyeron),
                ()->insertarCorreoDirecto("sqlB"+sufijo,correo.toUpperCase(java.util.Locale.ROOT),leyeron));
        assertEquals(1,contar("SELECT COUNT(u) FROM Usuario u WHERE LOWER(u.correoElectronico)=LOWER(:clave)",correo));
        assertEquals(1,contar("SELECT COUNT(u) FROM Organizador u WHERE LOWER(u.correoElectronico)=LOWER(:clave)",correo));
    }
    @Test void indiceTipoResuelveCarreraSinDependerDelBloqueoDelManejador()throws Exception {
        exigirIndicesH13();preparar(5);var leyeron=new CyclicBarrier(2);
        unaSola(()->insertarTipoDirecto("Directo",leyeron),()->insertarTipoDirecto("DIRECTO",leyeron));
        assertEquals(1,contar("SELECT COUNT(t) FROM TipoRegistro t WHERE t.edicion.idNombre=:clave AND LOWER(t.idNombre)='directo'",ed));
    }
    @Test void tipoEquivalenteSiguePermitidoEnEdicionesDiferentes()throws Exception {
        exigirIndicesH13();preparar(5);
        String otra="otraCi"+ed;assertTrue(sistema.altaEdicion(edicion(otra),evento));
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("GENERAL","Otra edición",10,5),otra));
        assertEquals("General",sistema.consultarTipoRegistro(ed,"GENERAL").getIdNombre());
        assertEquals("GENERAL",sistema.consultarTipoRegistro(otra,"general").getIdNombre());
    }
    @Test void institucionesYEdicionesConservanSusClavesExactas()throws Exception {
        exigirIndicesH13();preparar(5);
        String otra=ed.toUpperCase(java.util.Locale.ROOT);
        assertNotEquals(ed,otra);assertTrue(sistema.altaEdicion(edicion(otra),evento));
        assertEquals(2,sistema.obtenerEdicionesEvento(evento).size());
        String inst="InstCi"+sufijo,mayuscula=inst.toUpperCase(java.util.Locale.ROOT);
        assertTrue(sistema.altaInstitucion(new DtInstitucion(inst,"Primera","https://test.uy")));
        assertFalse(sistema.existeInstitucion(mayuscula));
        assertTrue(sistema.altaInstitucion(new DtInstitucion(mayuscula,"Segunda","https://test.uy")));
        assertTrue(sistema.existeInstitucion(inst));assertTrue(sistema.existeInstitucion(mayuscula));
    }
    @Test void consultaPatrocinioPorClaveExactaConservaSusReferencias()throws Exception {
        exigirIndicesH13();preparar(5);String inst=institucion(),codigo="consultaCi"+sufijo;
        assertTrue(sistema.altaPatrocinio(patrocinio(codigo,inst)));
        var dt=sistema.consultarPatrocinio(ed,inst);
        assertNotNull(dt);assertEquals(ed,dt.getNombreEdicion());
        assertEquals(inst,dt.getNombreInstituto());assertEquals(codigo,dt.getCodigoPatrocinio());
    }
    @Test void patrociniosDeEdicionesDistintasPorMayusculasNoSeConfundenNiBloquean()throws Exception {
        exigirIndicesH13();preparar(5);String otra=ed.toUpperCase(java.util.Locale.ROOT),inst=institucion();
        assertTrue(sistema.altaEdicion(edicion(otra),evento));
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General","Descripción",10,5),otra));
        String a="patA"+sufijo,b="patB"+sufijo;
        assertTrue(sistema.altaPatrocinio(patrocinio(a,inst)));
        assertNull(sistema.consultarPatrocinio(otra,inst),"No debe devolver el patrocinio de otra clave.");
        assertFalse(manejadores.ManejadorPatrocinios.getInstance().existePatrocinioInstitucionEdicion(inst,otra));
        var nuevo=new DtPatrocinio(null,1000,1,b,NivelPatrocinio.ORO,inst,otra,"General");
        assertTrue(sistema.altaPatrocinio(nuevo));
        assertEquals(a,sistema.consultarPatrocinio(ed,inst).getCodigoPatrocinio());
        assertEquals(b,sistema.consultarPatrocinio(otra,inst).getCodigoPatrocinio());
        assertEquals(otra,sistema.consultarPatrocinio(otra,inst).getNombreEdicion());
    }
    @Test void patrociniosDeInstitucionesDistintasPorMayusculasNoSeConfundenNiBloquean()throws Exception {
        exigirIndicesH13();preparar(5);String inst="InstPat"+sufijo,otra=inst.toUpperCase(java.util.Locale.ROOT);
        assertTrue(sistema.altaInstitucion(new DtInstitucion(inst,"Primera","https://test.uy")));
        assertTrue(sistema.altaInstitucion(new DtInstitucion(otra,"Segunda","https://test.uy")));
        String a="instPatA"+sufijo,b="instPatB"+sufijo;
        assertTrue(sistema.altaPatrocinio(patrocinio(a,inst)));
        assertNull(sistema.consultarPatrocinio(ed,otra));
        assertFalse(manejadores.ManejadorPatrocinios.getInstance().existePatrocinioInstitucionEdicion(otra,ed));
        assertTrue(sistema.altaPatrocinio(patrocinio(b,otra)));
        assertEquals(a,sistema.consultarPatrocinio(ed,inst).getCodigoPatrocinio());
        assertEquals(b,sistema.consultarPatrocinio(ed,otra).getCodigoPatrocinio());
        assertEquals(otra,sistema.consultarPatrocinio(ed,otra).getNombreInstituto());
    }
    @Test void patrocinioNoSeEncuentraMedianteUnaClaveInexistenteConOtraMayuscula()throws Exception {
        exigirIndicesH13();preparar(5);String inst=institucion();
        assertTrue(sistema.altaPatrocinio(patrocinio("sinAlias"+sufijo,inst)));
        assertNull(sistema.consultarPatrocinio(ed.toUpperCase(java.util.Locale.ROOT),inst));
        assertNull(sistema.consultarPatrocinio(ed,inst.toUpperCase(java.util.Locale.ROOT)));
        assertNotNull(sistema.consultarPatrocinio(ed,inst));
    }
    @Test void patrociniosConcurrentesDeParesDistintosPorMayusculasConfirmanAmbos()throws Exception {
        exigirIndicesH13();preparar(5);String inst="InstSim"+sufijo,otra=inst.toUpperCase(java.util.Locale.ROOT);
        assertTrue(sistema.altaInstitucion(new DtInstitucion(inst,"Primera","https://test.uy")));
        assertTrue(sistema.altaInstitucion(new DtInstitucion(otra,"Segunda","https://test.uy")));
        var pool=Executors.newFixedThreadPool(2);var inicio=new CyclicBarrier(2);
        String a="simA"+sufijo,b="simB"+sufijo;
        try {
            var x=pool.submit(()->{inicio.await(10,TimeUnit.SECONDS);return sistema.altaPatrocinio(patrocinio(a,inst));});
            var y=pool.submit(()->{inicio.await(10,TimeUnit.SECONDS);return sistema.altaPatrocinio(patrocinio(b,otra));});
            assertTrue(x.get(15,TimeUnit.SECONDS));assertTrue(y.get(15,TimeUnit.SECONDS));
            assertEquals(2,contar("SELECT COUNT(p) FROM Patrocinio p WHERE p.edicion.idNombre=:clave",ed));
            assertEquals(a,sistema.consultarPatrocinio(ed,inst).getCodigoPatrocinio());
            assertEquals(b,sistema.consultarPatrocinio(ed,otra).getCodigoPatrocinio());
        } finally {pool.shutdownNow();assertTrue(pool.awaitTermination(15,TimeUnit.SECONDS));}
    }
    private void exigirIndicesH13() {
        try(var em=BaseDeDatos.getEntityManager()) {
            Number cantidad=(Number)em.createNativeQuery("""
                    SELECT count(*) FROM pg_index x JOIN pg_class i ON i.oid=x.indexrelid
                    JOIN pg_namespace n ON n.oid=i.relnamespace
                    WHERE n.nspname='public' AND i.relname IN ('ux_usuario_correo_ci','ux_tipo_registro_edicion_nombre_ci')
                    AND x.indisunique AND x.indisvalid AND x.indisready
                    """).getSingleResult();
            assertEquals(2,cantidad.intValue(),"Falta aplicar H13 en eventosuy_test antes de estas pruebas; JUnit no ejecuta migraciones.");
        }
    }
    private boolean insertarCorreoDirecto(String nick,String correo,CyclicBarrier leyeron)throws Exception {
        var usuario=new Organizador(nick,"Prueba de índice",correo,"Descripción","");usuario.setContrasenia("clave");
        try(var em=BaseDeDatos.getEntityManager()) {
            var tx=em.getTransaction();tx.begin();
            try {
                assertEquals(0L,em.createQuery("SELECT COUNT(u) FROM Usuario u WHERE LOWER(u.correoElectronico)=LOWER(:correo)",Long.class)
                        .setParameter("correo",correo).getSingleResult());
                leyeron.await(10,TimeUnit.SECONDS);
                em.persist(usuario);tx.commit();return true;
            } catch(RuntimeException e) {
                if(tx.isActive())tx.rollback();
                assertEquals("23505",estadoSql(e),"Debe rechazar la unicidad, no un error técnico distinto.");return false;
            } finally {if(tx.isActive())tx.rollback();}
        }
    }
    private boolean insertarTipoDirecto(String nombre,CyclicBarrier leyeron)throws Exception {
        try(var em=BaseDeDatos.getEntityManager()) {
            var tx=em.getTransaction();tx.begin();
            try {
                assertEquals(0L,em.createQuery("SELECT COUNT(t) FROM TipoRegistro t WHERE t.edicion.idNombre=:ed AND LOWER(t.idNombre)='directo'",Long.class)
                        .setParameter("ed",ed).getSingleResult());
                var tipo=new TipoRegistro(nombre,"Prueba de índice",10,5);tipo.setEdicion(em.find(Edicion.class,ed));
                leyeron.await(10,TimeUnit.SECONDS);
                em.persist(tipo);tx.commit();return true;
            } catch(RuntimeException e) {
                if(tx.isActive())tx.rollback();assertEquals("23505",estadoSql(e));return false;
            } finally {if(tx.isActive())tx.rollback();}
        }
    }
    private String estadoSql(Throwable error) {
        for(Throwable causa=error;causa!=null;causa=causa.getCause())
            if(causa instanceof java.sql.SQLException sql)return sql.getSQLState();
        return null;
    }
    @Test void eventoIgualNoSeDuplicaConDosAltas()throws Exception {
        String cat="catU"+sufijo;sistema.altaCategoria(cat,null);
        DtEvento dt=new DtEvento(evento,"EV","Descripción",null,List.of(cat));
        unaSola(()->sistema.altaEvento(dt),()->sistema.altaEvento(dt));
        assertEquals(1,contar("SELECT COUNT(e) FROM Evento e WHERE e.nombre=:clave",evento));
    }
    @Test void edicionIgualNoSeDuplicaConDosAltas()throws Exception {
        preparar(5);String nueva="otra"+ed;
        unaSola(()->sistema.altaEdicion(edicion(nueva),evento),()->sistema.altaEdicion(edicion(nueva),evento));
        assertEquals(1,contar("SELECT COUNT(e) FROM Edicion e WHERE e.idNombre=:clave",nueva));
    }
    @Test void institucionIgualNoSeDuplicaConDosAltas()throws Exception {
        String nombre="instU"+sufijo;var dt=new DtInstitucion(nombre,"Descripción","");
        unaSola(()->sistema.altaInstitucion(dt),()->sistema.altaInstitucion(dt));
        assertEquals(1,contar("SELECT COUNT(i) FROM Institucion i WHERE i.nombre=:clave",nombre));
    }
    @Test void tiposQueSoloDifierenEnMayusculasEsperanElMismoPadre()throws Exception {
        preparar(5);
        conBloqueo(Edicion.class,ed,
                ()->sistema.altaTipoRegistro(new DtTipoRegistro("Preferente","Descripción",10,5),ed),
                ()->sistema.altaTipoRegistro(new DtTipoRegistro("PREFERENTE","Descripción",10,5),ed));
        assertEquals(1,contar("SELECT COUNT(t) FROM TipoRegistro t WHERE t.edicion.idNombre=:clave AND LOWER(t.idNombre)='preferente'",ed));
    }
    @Test void mismaInstitucionYEdicionNoAdmitenDosPatrocinios()throws Exception {
        preparar(5);String inst=institucion();
        unaSola(()->sistema.altaPatrocinio(patrocinio("a"+sufijo,inst)),
                ()->sistema.altaPatrocinio(patrocinio("b"+sufijo,inst)));
        assertEquals(1,contar("SELECT COUNT(p) FROM Patrocinio p WHERE p.edicion.idNombre=:clave",ed));
    }
    @Test void codigoIgualNoSeDuplicaEnInstitucionesDistintas()throws Exception {
        preparar(5);String a=institucion(),b=institucion(),codigo="codU"+sufijo;
        unaSola(()->sistema.altaPatrocinio(patrocinio(codigo,a)),()->sistema.altaPatrocinio(patrocinio(codigo,b)));
        assertEquals(1,contar("SELECT COUNT(p) FROM Patrocinio p WHERE p.codigoPatrocinio=:clave",codigo));
    }
    @Test void mismoAsistenteEnTiposDistintosEsperaYRecibeErrorDeNegocio()throws Exception {
        preparar(5);String nick=asistente(null);
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("Especial","Descripción",10,5),ed));
        conBloqueo(Asistente.class,nick,()->registrar(nick,"General",null),()->registrar(nick,"Especial",null));
        assertEquals(1,contar("SELECT COUNT(r) FROM Registro r WHERE r.asistente.nickname=:clave",nick));
    }
    @Test void ultimoCupoSoloSeOtorgaUnaVez()throws Exception {
        preparar(1);String a=asistente(null),b=asistente(null);
        Long tipo;
        try(var em=BaseDeDatos.getEntityManager()) {
            tipo=em.createQuery("SELECT t.id FROM TipoRegistro t WHERE t.edicion.idNombre=:e",Long.class).setParameter("e",ed).getSingleResult();
        }
        conBloqueo(TipoRegistro.class,tipo,()->registrar(a,"General",null),()->registrar(b,"General",null));
        assertEquals(1,contar("SELECT COUNT(r) FROM Registro r WHERE r.edicion.idNombre=:clave",ed));
    }
    @Test void ultimoUsoPatrocinadoNoSeConsumeDosVeces()throws Exception {
        preparar(5);String inst=institucion(),a=asistente(inst),b=asistente(inst),codigo="codU"+sufijo;
        assertTrue(sistema.altaPatrocinio(patrocinio(codigo,inst)));
        unaSola(()->registrar(a,"General",codigo),()->registrar(b,"General",codigo));
        assertEquals(1,contar("SELECT COUNT(r) FROM Registro r WHERE r.patrocinio.codigoPatrocinio=:clave",codigo));
        var registro=sistema.obtenerRegistrosAsistente(a);
        var otro=sistema.obtenerRegistrosAsistente(b);
        assertEquals(1,registro.size()+otro.size());
        assertEquals(0f,java.util.stream.Stream.concat(registro.stream(),otro.stream()).findFirst().orElseThrow().getCosto());
    }
    private boolean registrar(String nick,String tipo,String codigo) {
        return sistema.registroAEdicion(nick,ed,tipo,new DtRegistro(null,999),codigo);
    }
    private void unaSola(Callable<Boolean> a,Callable<Boolean> b)throws Exception { competir(a,b,null); }
    private void conBloqueo(Class<?> tipo,Object clave,Callable<Boolean> a,Callable<Boolean> b)throws Exception {
        try(var em=BaseDeDatos.getEntityManager()) {
            var tx=em.getTransaction();tx.begin();
            try {
                assertNotNull(em.find(tipo,clave,LockModeType.PESSIMISTIC_WRITE));
                int pid=((Number)em.createNativeQuery("SELECT pg_backend_pid()").getSingleResult()).intValue();
                competir(a,b,()->{
                    try {esperarDosBloqueadas(pid);}
                    finally {if(tx.isActive())tx.commit();}
                });
            } finally {if(tx.isActive())tx.rollback();}
        }
    }
    private void competir(Callable<Boolean> a,Callable<Boolean> b,Accion liberar)throws Exception {
        var pool=Executors.newFixedThreadPool(2);var inicio=new CyclicBarrier(2);
        try {
            Future<Boolean> x=pool.submit(()->intentar(inicio,a)),y=pool.submit(()->intentar(inicio,b));
            if(liberar!=null)liberar.ejecutar();
            boolean primero=x.get(15,TimeUnit.SECONDS),segundo=y.get(15,TimeUnit.SECONDS);
            assertNotEquals(primero,segundo,"Exactamente una operación debe confirmar.");
        } finally {pool.shutdownNow();assertTrue(pool.awaitTermination(20,TimeUnit.SECONDS));}
    }
    private boolean intentar(CyclicBarrier inicio,Callable<Boolean> accion)throws Exception {
        inicio.await(10,TimeUnit.SECONDS);
        try {return accion.call();}
        catch(IllegalArgumentException error) {assertFalse(error.getMessage().isBlank());return false;}
        catch(Exception error) {
            // altaEvento conserva Exception comprobada en su contrato anterior.
            if(error.getClass()==Exception.class && error.getMessage().startsWith("Ya existe"))return false;
            throw error;
        }
    }
    private void esperarDosBloqueadas(int pid)throws Exception {
        var p=BaseDeDatos.propiedadesJpa();
        try(var c=DriverManager.getConnection(p.get("jakarta.persistence.jdbc.url"),p.get("jakarta.persistence.jdbc.user"),p.get("jakarta.persistence.jdbc.password"))) {
            c.setReadOnly(true);
            // El segundo concurrente puede esperar al primero en la cola de la tupla,
            // en vez de figurar como bloqueado directamente por la transacción inicial.
            try(var s=c.prepareStatement("""
                    WITH RECURSIVE bloqueadas(pid) AS (
                        SELECT a.pid FROM pg_stat_activity a
                        WHERE a.datname=current_database() AND ? = ANY(pg_blocking_pids(a.pid))
                        UNION
                        SELECT a.pid FROM pg_stat_activity a JOIN bloqueadas b
                            ON b.pid = ANY(pg_blocking_pids(a.pid))
                        WHERE a.datname=current_database()
                    ) SELECT COUNT(*) FROM bloqueadas
                    """)) {
                s.setInt(1,pid);s.setQueryTimeout(3);long limite=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
                while(System.nanoTime()<limite) {
                    try(var r=s.executeQuery()){assertTrue(r.next());if(r.getInt(1)>=2)return;}
                    Thread.sleep(25);
                }
                fail("No se observaron las dos transacciones esperando al bloqueo real.");
            }
        }
    }
    @FunctionalInterface private interface Accion {void ejecutar()throws Exception;}
}
