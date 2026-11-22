package datos;

import datatypes.*;
import implementacion.Fabrica;
import interfaces.ISistema;
import persistencia.BaseDeDatos;

import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Datos adicionales de defensa: solo altas con un prefijo nuevo, sin borrar ni reemplazar datos. */
public final class CargarDatosLab3 {
    private CargarDatosLab3() { }

    public record Datos(String organizador, String asistente, String otroAsistente, String evento,
                        String eventoFinalizado, String conVideo, String sinVideo, String archivable,
                        String archivada, String ingresada, String rechazada) { }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !"--agregar-datos-demo".equals(args[0]))
            throw new IllegalArgumentException("Uso: CargarDatosLab3 --agregar-datos-demo (base terminada en _test o _demo).");
        var propiedades = BaseDeDatos.propiedadesJpa();
        String nombre = System.getenv("EVENTOS_DB_NAME");
        if (nombre == null || !nombre.matches("[A-Za-z0-9_]+_(test|demo)"))
            throw new IllegalStateException("Usar una base exclusiva terminada en _test o _demo.");
        String clave = System.getenv("EVENTOS_DEMO_PASSWORD");
        if (clave == null || clave.isBlank()) throw new IllegalStateException("Definir EVENTOS_DEMO_PASSWORD sin publicarla.");
        try (var c = DriverManager.getConnection(propiedades.get("jakarta.persistence.jdbc.url"),
                propiedades.get("jakarta.persistence.jdbc.user"), propiedades.get("jakarta.persistence.jdbc.password"));
             var q = c.createStatement(); var r = q.executeQuery("SELECT current_database()")) {
            if (!r.next() || !nombre.equals(r.getString(1))) throw new IllegalStateException("Base efectiva inesperada.");
        }
        try {
            BaseDeDatos.inicializar(propiedades); // validate: el cargador no migra el esquema.
            String prefijo = "demo3_" + UUID.randomUUID().toString().replace("-", "").substring(0,12);
            Datos datos = cargar(Fabrica.getInstance().getISistema(), prefijo, clave);
            System.out.println("Datos agregados con prefijo " + prefijo + ". No se modificaron conjuntos anteriores.");
            System.out.println("Organizador: " + datos.organizador());
            System.out.println("Asistentes: " + datos.asistente() + ", " + datos.otroAsistente());
            System.out.println("La contraseña es la proporcionada por EVENTOS_DEMO_PASSWORD; no se imprime.");
            System.out.println("Administrador: usar Swing, sin cuenta web.");
            System.out.println("Evento: " + datos.evento() + "; archivable: " + datos.archivable());
        } finally { BaseDeDatos.cerrar(); }
    }

    public static Datos cargar(ISistema s, String prefijo, String clave) throws Exception {
        if (prefijo == null || !prefijo.matches("[A-Za-z0-9_]{1,60}") || clave == null || clave.isBlank())
            throw new IllegalArgumentException("Prefijo y contraseña de demostración obligatorios.");
        String org = prefijo + "_org", as = prefijo + "_as", otro = prefijo + "_as2";
        // Rechazar una repetición antes de cualquier alta; nunca actualizar usuarios preexistentes.
        for (String nick : List.of(org,as,otro))
            if (s.existeUsuario(nick) || s.existeCorreoElectronico(nick + "@demo.example"))
                throw new IllegalArgumentException("El prefijo ya está en uso; elegir otro.");
        var organizador = new DtOrganizador(org,"Organizador Lab 3",org+"@demo.example","Demostración","");
        var asistente = new DtAsistente(as,"Ana",as+"@demo.example","Demo",LocalDate.of(2000,1,1),null);
        var segundo = new DtAsistente(otro,"Luis",otro+"@demo.example","Demo",LocalDate.of(1999,1,1),null);
        for (DtUsuario usuario : List.of(organizador,asistente,segundo)) {
            usuario.setContrasenia(clave); usuario.setConfirmacionContrasenia(clave);
        }
        exigir(s.altaOrganizador(organizador)); exigir(s.altaAsistente(asistente)); exigir(s.altaAsistente(segundo));
        Actor admin = Actor.administradorLocal(), actorOrg = Actor.usuarioAutenticado(organizador);
        Actor actorAs = Actor.usuarioAutenticado(asistente), actorOtro = Actor.usuarioAutenticado(segundo);
        s.seguirUsuario(org,actorAs); s.seguirUsuario(as,actorOrg);
        String cat = prefijo+"_categoria", evento = prefijo+"_evento", finalizado = prefijo+"_finalizado";
        s.altaCategoria(cat,null,admin);
        exigir(s.altaEvento(new DtEvento(evento,"DEMO","Demostración de búsqueda Lab 3",null,List.of(cat)),actorOrg));
        exigir(s.altaEvento(new DtEvento(finalizado,"FIN","Evento concluido",null,List.of(cat)),actorOrg));
        s.registrarVisitaEvento(evento,null); s.registrarVisitaEvento(evento,null);
        s.registrarVisitaEvento(finalizado,null); s.finalizarEvento(finalizado,actorOrg);
        LocalDate hoy = LocalDate.now();
        String video = prefijo+"_video", sinVideo = prefijo+"_sin_video", archivable = prefijo+"_archivable";
        String archivada = prefijo+"_archivada", ingresada = prefijo+"_ingresada", rechazada = prefijo+"_rechazada";
        for (String nombre : List.of(video,sinVideo,archivable,archivada,ingresada,rechazada)) {
            boolean terminada = nombre.equals(archivable) || nombre.equals(archivada);
            var edicion = new DtEdicion(nombre,"ED",terminada?hoy.minusDays(3):hoy,
                    terminada?hoy.minusDays(1):hoy.plusDays(2),null,"Montevideo","Uruguay",org);
            edicion.setDescripcion("Edición de demostración " + nombre);
            if (nombre.equals(video)) edicion.setVideoYoutube("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
            exigir(s.altaEdicion(edicion,evento,actorOrg));
            exigir(s.altaTipoRegistro(new DtTipoRegistro("General","Entrada de demostración",100,20),nombre,actorOrg));
            if (!nombre.equals(ingresada)) s.aceptarRechazarEdicion(nombre,!nombre.equals(rechazada),admin);
        }
        for (String nombre : List.of(video,archivada)) {
            exigir(s.registrarAutorizado(nombre,"General","general",null,actorAs));
            exigir(s.registrarAutorizado(nombre,"General","general",null,actorOtro));
            s.confirmarAsistencia(nombre,actorAs);
        }
        s.archivarEdicion(archivada,actorOrg);
        return new Datos(org,as,otro,evento,finalizado,video,sinVideo,archivable,archivada,ingresada,rechazada);
    }

    private static void exigir(boolean resultado) {
        if (!resultado) throw new IllegalStateException("No se completó el conjunto de demostración; revisar la última operación.");
    }
}
