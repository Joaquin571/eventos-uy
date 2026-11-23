import clases.NivelPatrocinio;
import datatypes.*;
import implementacion.Fabrica;
import interfaces.ISistema;
import persistencia.BaseDeDatos;

import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;

/** Fuente ejecutable con Java 25 y servidor.jar. Solo usa los casos de uso existentes. */
class CargarDatosDefensa {
    private static final LocalDate MINIMO = LocalDate.of(2026, 11, 23);
    private record Ficha(String evento, String sigla, String descripcion, String categoria,
                         String edicion, String ciudad, LocalDate inicio, int dias,
                         String organizador, int precio, EstadoEdicion estado) { }
    private record PatrocinioPreparado(String evento, String edicion, String institucion,
                                      String codigo, int monto, int gratuitos, String organizador) { }
    private static final List<Ficha> FICHAS = List.of(
        new Ficha("Festival de Música de Montevideo", "FMM", "Tres días de música uruguaya en escenarios de Montevideo.", "Música", "Festival de Música de Montevideo 2027", "Montevideo", LocalDate.of(2027,2,12),3,"cultura_sur",850,EstadoEdicion.ACEPTADA),
        new Ficha("Feria Gastronómica de San José", "FGSJ", "Productores y cocineros locales comparten sabores de la región.", "Gastronomía", "Feria Gastronómica de San José 2027", "San José de Mayo", LocalDate.of(2027,3,13),2,"encuentros_uy",300,EstadoEdicion.ACEPTADA),
        new Ficha("Congreso de Tecnología e Innovación", "CTI", "Charlas y experiencias sobre soluciones tecnológicas aplicadas en Uruguay.", "Tecnología", "Congreso de Tecnología e Innovación 2027", "Montevideo", LocalDate.of(2027,4,8),2,"innovacion_uy",1500,EstadoEdicion.ACEPTADA),
        new Ficha("Maratón de Punta del Este", "MPE", "Circuito costero para corredores de distintos niveles.", "Deporte", "Maratón de Punta del Este 2027", "Punta del Este", LocalDate.of(2027,4,18),1,"deporte_uy",700,EstadoEdicion.ACEPTADA),
        new Ficha("Expo Emprendedores Uruguay", "EEU", "Emprendimientos nacionales presentan productos y proyectos.", "Negocios", "Expo Emprendedores Uruguay 2027", "Montevideo", LocalDate.of(2027,5,7),3,"innovacion_uy",400,EstadoEdicion.ACEPTADA),
        new Ficha("Festival de Cine Nacional", "FCN", "Proyecciones y conversaciones con realizadores uruguayos.", "Cine", "Festival de Cine Nacional 2027", "Montevideo", LocalDate.of(2027,5,20),4,"cultura_sur",450,EstadoEdicion.ACEPTADA),
        new Ficha("Encuentro de Desarrollo de Software", "EDS", "Comunidad de desarrollo comparte prácticas y proyectos.", "Tecnología", "Encuentro de Desarrollo de Software 2027", "Montevideo", LocalDate.of(2027,6,11),2,"innovacion_uy",900,EstadoEdicion.ACEPTADA),
        new Ficha("Feria del Libro de Montevideo", "FLM", "Editoriales, autores y lectores se encuentran alrededor del libro.", "Literatura", "Feria del Libro de Montevideo 2027", "Montevideo", LocalDate.of(2027,6,24),5,"cultura_sur",0,EstadoEdicion.ACEPTADA),
        new Ficha("Jornada de Ciberseguridad", "JC", "Buenas prácticas para proteger sistemas y datos.", "Tecnología", "Jornada de Ciberseguridad 2027", "Montevideo", LocalDate.of(2027,7,15),1,"innovacion_uy",650,EstadoEdicion.ACEPTADA),
        new Ficha("Campeonato de Fútbol Amateur", "CFA", "Equipos barriales compiten en una jornada abierta.", "Deporte", "Campeonato de Fútbol Amateur 2027", "Canelones", LocalDate.of(2027,7,25),1,"deporte_uy",200,EstadoEdicion.ACEPTADA),
        new Ficha("Taller de Inteligencia Artificial", "TIA", "Introducción práctica a herramientas de inteligencia artificial.", "Tecnología", "Taller de Inteligencia Artificial 2027", "Montevideo", LocalDate.of(2027,8,14),1,"innovacion_uy",1200,EstadoEdicion.ACEPTADA),
        new Ficha("Expo Turismo Uruguay", "ETU", "Destinos y experiencias turísticas de todo el país.", "Turismo", "Expo Turismo Uruguay 2027", "Montevideo", LocalDate.of(2027,8,27),3,"encuentros_uy",350,EstadoEdicion.ACEPTADA),
        new Ficha("Festival de Jazz de Colonia", "FJC", "Músicos nacionales e invitados en escenarios de Colonia.", "Música", "Festival de Jazz de Colonia 2027", "Colonia del Sacramento", LocalDate.of(2027,9,17),2,"cultura_sur",750,EstadoEdicion.ACEPTADA),
        new Ficha("Encuentro de Videojuegos y Tecnología", "EVT", "Desarrolladores y jugadores comparten novedades y talleres.", "Tecnología", "Encuentro de Videojuegos y Tecnología 2027", "Montevideo", LocalDate.of(2027,10,9),2,"innovacion_uy",500,EstadoEdicion.INGRESADA),
        new Ficha("Feria de Artesanos de Canelones", "FAC", "Artesanías locales y oficios en un espacio para toda la familia.", "Artesanía", "Feria de Artesanos de Canelones 2027", "Canelones", LocalDate.of(2027,10,23),2,"encuentros_uy",0,EstadoEdicion.ACEPTADA),
        new Ficha("Congreso de Innovación Educativa", "CIE", "Docentes y especialistas intercambian experiencias de aula.", "Educación", "Congreso de Innovación Educativa 2027", "Montevideo", LocalDate.of(2027,11,12),2,"innovacion_uy",1000,EstadoEdicion.RECHAZADA)
    );
    private static final String[][] ORGANIZADORES = {
        {"cultura_sur","Colectivo Cultura Sur","Actividades culturales y artísticas en Uruguay."},
        {"encuentros_uy","Encuentros Uruguay","Ferias y encuentros para comunidades locales."},
        {"innovacion_uy","Red Innovación Uruguay","Espacios de intercambio sobre tecnología y emprendimiento."},
        {"deporte_uy","Movimiento Deportivo Uruguay","Actividades deportivas abiertas a la comunidad."}
    };
    private static final String[][] ASISTENTES = {
        {"ana_pereira","Ana","Pereira"},{"lucas_fernandez","Lucas","Fernández"},
        {"sofia_rodriguez","Sofía","Rodríguez"},{"martin_silva","Martín","Silva"},
        {"valeria_gomez","Valeria","Gómez"},{"diego_martinez","Diego","Martínez"}
    };
    private static final List<PatrocinioPreparado> PATROCINIOS = List.of(
        new PatrocinioPreparado("Congreso de Tecnología e Innovación","Congreso de Tecnología e Innovación 2027",
            "Cooperativa Horizonte","HORIZONTE27",25000,3,"innovacion_uy"),
        new PatrocinioPreparado("Festival de Música de Montevideo","Festival de Música de Montevideo 2027",
            "Fundación Río de la Plata","RIOPLATA27",20000,3,"cultura_sur")
    );
    private static void exigir(boolean valor, String operacion) {
        if (!valor) throw new IllegalStateException("No se pudo completar: " + operacion);
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !("--validar".equals(args[0]) || "--cargar".equals(args[0]) || "--verificar".equals(args[0])))
            throw new IllegalArgumentException("Uso: CargarDatosDefensa.java --validar|--cargar|--verificar");
        String nombre = System.getenv("EVENTOS_DB_NAME");
        if (!"eventosuy_demo".equals(nombre)) throw new IllegalStateException("La base prevista es eventosuy_demo.");
        var props = BaseDeDatos.propiedadesJpa();
        try (var c = DriverManager.getConnection(props.get("jakarta.persistence.jdbc.url"),
                props.get("jakarta.persistence.jdbc.user"), props.get("jakarta.persistence.jdbc.password"));
             var s = c.createStatement(); var r = s.executeQuery("select current_database()")) {
            if (!r.next() || !nombre.equals(r.getString(1))) throw new IllegalStateException("Base efectiva inesperada.");
        }
        BaseDeDatos.inicializar(props); // Hibernate validate; nunca crea ni cambia tablas.
        try {
            ISistema sistema = Fabrica.getInstance().getISistema();
            validarFichas();
            revisarExistentes(sistema);
            for (Ficha f : FICHAS) sistema.validarImagen(imagen(f));
            if ("--validar".equals(args[0])) { System.out.println("Modelo y 16 fichas válidos; no se cargaron datos."); return; }
            if ("--cargar".equals(args[0])) cargar(sistema);
            verificar(sistema);
        } finally { BaseDeDatos.cerrar(); }
    }
    private static void validarFichas() {
        Set<String> eventos = new HashSet<>(), ediciones = new HashSet<>();
        for (Ficha f : FICHAS) {
            if (!eventos.add(f.evento) || !ediciones.add(f.edicion) || f.inicio.isBefore(MINIMO) || f.dias < 1 || f.precio < 0)
                throw new IllegalStateException("Ficha duplicada o fecha/precio inválido: " + f.evento);
        }
        Set<String> codigos = new HashSet<>(), pares = new HashSet<>();
        for (PatrocinioPreparado p : PATROCINIOS) {
            Ficha f = FICHAS.stream().filter(x -> x.edicion.equals(p.edicion)).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Edición de patrocinio inexistente: " + p.edicion));
            if (!f.evento.equals(p.evento) || !f.organizador.equals(p.organizador)
                    || !codigos.add(p.codigo) || !pares.add(p.edicion + "\u0000" + p.institucion))
                throw new IllegalStateException("Relación o código de patrocinio inválido: " + p.codigo);
            validarReglaPatrocinio(f.precio, p.gratuitos, p.monto);
        }
    }
    /** Misma comparación con float que usa Sistema.altaPatrocinio. */
    static void validarReglaPatrocinio(float costoEntrada, int gratuitos, float aporte) {
        if (!Float.isFinite(costoEntrada) || costoEntrada < 0 || gratuitos < 0 || gratuitos > 150
                || !Float.isFinite(aporte) || aporte <= 0 || costoEntrada * gratuitos > aporte * 0.20f)
            throw new IllegalArgumentException("Patrocinio preparado fuera del límite del 20%." );
    }
    private static void revisarExistentes(ISistema s) {
        for (String[] o : ORGANIZADORES) {
            if (!s.existeUsuario(o[0])) continue;
            DtUsuario u = s.consultarUsuario(o[0]);
            if (!(u instanceof DtOrganizador) || !o[1].equals(u.getNombre())
                    || !(o[0]+"@example.com").equals(u.getCorreoElectronico()))
                throw new IllegalStateException("El nickname pertenece a otro organizador: " + o[0]);
        }
        for (String[] a : ASISTENTES) {
            if (!s.existeUsuario(a[0])) continue;
            DtUsuario u = s.consultarUsuario(a[0]);
            if (!(u instanceof DtAsistente) || !a[1].equals(u.getNombre())
                    || !(a[0]+"@example.com").equals(u.getCorreoElectronico()))
                throw new IllegalStateException("El nickname pertenece a otro asistente: " + a[0]);
        }
        Map<String,DtEvento> eventos = new HashMap<>();
        for (DtEvento e : s.listarEventos()) eventos.put(e.getNombre(),e);
        for (Ficha f : FICHAS) {
            DtEvento previo = eventos.get(f.evento);
            if (previo != null && (!f.descripcion.equals(previo.getDescripcion()) || !previo.getCategorias().contains(f.categoria)))
                throw new IllegalStateException("El evento existente tiene otros datos: " + f.evento);
            if (previo == null) continue;
            for (DtEdicion e : s.obtenerEdicionesEvento(f.evento)) {
                if (!f.edicion.equals(e.getIdNombre())) continue;
                if (!f.organizador.equals(e.getNombreOrganizador()) || !f.inicio.equals(e.getFechaInicio())
                        || !f.inicio.plusDays(f.dias-1).equals(e.getFechaFin()) || !f.ciudad.equals(e.getCiudad()))
                    throw new IllegalStateException("La edición existente tiene otros datos: " + f.edicion);
                if (e.getEstado() != f.estado && !(e.getEstado() == EstadoEdicion.INGRESADA
                        && f.estado != EstadoEdicion.INGRESADA))
                    throw new IllegalStateException("La edición existente tiene otro estado: " + f.edicion);
                DtTipoRegistro t = s.consultarTipoRegistro(f.edicion,"General");
                if (t != null && (Float.compare(t.getCosto(),f.precio) != 0 || t.getCupo() != 150))
                    throw new IllegalStateException("La entrada existente tiene otros datos: " + f.edicion);
            }
        }
        for (String nombre : List.of("Cooperativa Horizonte","Fundación Río de la Plata")) {
            if (!s.existeInstitucion(nombre)) continue;
            DtInstitucion i = s.consultarInstitucion(nombre);
            if (!"Institución ficticia para prácticas académicas.".equals(i.getDescripcion())
                    || !"https://example.com".equals(i.getSitioWeb()))
                throw new IllegalStateException("La institución existente tiene otros datos: " + nombre);
        }
        Actor admin = Actor.administradorLocal();
        for (PatrocinioPreparado p : PATROCINIOS) {
            DtPatrocinio porCodigo = s.consultarPatrocinio(p.codigo,admin);
            DtPatrocinio porRelacion = s.consultarPatrocinio(p.edicion,p.institucion,admin);
            if (porCodigo == null && porRelacion == null) continue;
            if (porCodigo == null || porRelacion == null
                    || !p.codigo.equals(porRelacion.getCodigoPatrocinio())
                    || !p.edicion.equals(porCodigo.getNombreEdicion())
                    || !p.institucion.equals(porCodigo.getNombreInstituto())
                    || !"General".equals(porCodigo.getNombreTipoRegistro())
                    || porCodigo.getNivel() != NivelPatrocinio.PLATA
                    || Float.compare(porCodigo.getMontoAporte(),p.monto) != 0
                    || porCodigo.getCantRegistrosGrat() != p.gratuitos)
                throw new IllegalStateException("Patrocinio existente incompatible: " + p.codigo);
        }
        for (int n=0; n<ASISTENTES.length; n++) {
            String nick = ASISTENTES[n][0];
            if (!s.existeUsuario(nick)) continue;
            for (int j=0; j<2; j++) {
                String edicion = FICHAS.get((n+j*6) % 13).edicion;
                for (DtRegistro r : s.obtenerRegistrosAsistente(nick))
                    if (edicion.equals(r.getNombreEdicion()) && !"General".equals(r.getNombreTipoRegistro()))
                        throw new IllegalStateException("El registro existente usa otra entrada: " + nick + "/" + edicion);
            }
        }
    }
    private static DtImagen imagen(Ficha f) throws Exception {
        int tono = Math.floorMod(f.categoria.hashCode(),360);
        Color fondo = Color.getHSBColor(tono/360f,0.55f,0.37f);
        BufferedImage lienzo = new BufferedImage(1200,360,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = lienzo.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0,0,fondo,1200,360,fondo.darker()));
            g.fillRect(0,0,1200,360);
            g.setColor(new Color(255,255,255,55));
            g.fillOval(925,-160,440,440);
            g.fillOval(-130,230,360,360);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif",Font.BOLD,25));
            g.drawString("EVENTOS UY  /  " + f.categoria.toUpperCase(Locale.ROOT),52,72);
            g.setFont(new Font("SansSerif",Font.BOLD,46));
            String[] palabras = f.evento.split(" ");
            StringBuilder linea = new StringBuilder(); int y = 165;
            for (String palabra : palabras) {
                String siguiente = linea.length()==0 ? palabra : linea + " " + palabra;
                if (g.getFontMetrics().stringWidth(siguiente)>1040 && linea.length()>0) {
                    g.drawString(linea.toString(),52,y); y+=59; linea.setLength(0);
                }
                if (linea.length()>0) linea.append(' ');
                linea.append(palabra);
            }
            g.drawString(linea.toString(),52,y);
            g.setFont(new Font("SansSerif",Font.PLAIN,26));
            g.drawString(f.ciudad + "  ·  " + f.inicio,52,310);
        } finally { g.dispose(); }
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        if (!ImageIO.write(lienzo,"png",salida)) throw new IllegalStateException("No se pudo generar PNG.");
        return new DtImagen("image/png",salida.toByteArray());
    }
    private static void cargar(ISistema s) throws Exception {
        String clave = System.getenv("EVENTOS_DEMO_PASSWORD");
        if (clave == null || clave.isBlank()) throw new IllegalStateException("Falta la clave de usuarios demo.");
        Actor admin = Actor.administradorLocal();
        for (String[] o : ORGANIZADORES) if (!s.existeUsuario(o[0])) {
            String correo = o[0] + "@example.com";
            if (s.existeCorreoElectronico(correo)) throw new IllegalStateException("Correo ocupado: " + correo);
            DtOrganizador d = new DtOrganizador(o[0],o[1],correo,o[2],"");
            d.setContrasenia(clave); d.setConfirmacionContrasenia(clave);
            exigir(s.altaOrganizador(d), "organizador " + o[0]);
        }
        for (int i=0; i<ASISTENTES.length; i++) {
            String[] a = ASISTENTES[i];
            if (!s.existeUsuario(a[0])) {
                String correo = a[0] + "@example.com";
                if (s.existeCorreoElectronico(correo)) throw new IllegalStateException("Correo ocupado: " + correo);
                DtAsistente d = new DtAsistente(a[0],a[1],correo,a[2],LocalDate.of(1992+i,3,12),null);
                d.setContrasenia(clave); d.setConfirmacionContrasenia(clave);
                exigir(s.altaAsistente(d), "asistente " + a[0]);
            }
        }
        Set<String> categorias = new LinkedHashSet<>();
        for (Ficha f : FICHAS) categorias.add(f.categoria);
        Set<String> presentes = new HashSet<>();
        for (DtCategoria c : s.listarCategorias()) presentes.add(c.getIdNombre());
        for (String c : categorias) if (!presentes.contains(c)) s.altaCategoria(c,null,admin);
        Set<String> eventos = new HashSet<>();
        for (DtEvento e : s.listarEventos()) eventos.add(e.getNombre());
        for (Ficha f : FICHAS) {
            Actor org = Actor.usuarioAutenticado((DtOrganizador)s.consultarUsuario(f.organizador));
            if (!eventos.contains(f.evento)) exigir(s.altaEvento(new DtEvento(f.evento,f.sigla,f.descripcion,null,List.of(f.categoria)),imagen(f),org),f.evento);
            Set<String> ediciones = new HashSet<>();
            for (DtEdicion e : s.obtenerEdicionesEvento(f.evento)) ediciones.add(e.getIdNombre());
            if (!ediciones.contains(f.edicion)) {
                DtEdicion e = new DtEdicion(f.edicion,f.sigla+"27",f.inicio,f.inicio.plusDays(f.dias-1),null,f.ciudad,"Uruguay",f.organizador);
                e.setDescripcion(f.descripcion);
                exigir(s.altaEdicion(e,f.evento,imagen(f),org),f.edicion);
            }
            if (s.consultarTipoRegistro(f.edicion,"General") == null)
                exigir(s.altaTipoRegistro(new DtTipoRegistro("General","Acceso general a la edición.",f.precio,150),f.edicion,org),"entrada " + f.edicion);
            EstadoEdicion actual = s.consultarDetalleEdicion(f.edicion,org).edicion().getEstado();
            if (actual == EstadoEdicion.INGRESADA && f.estado != EstadoEdicion.INGRESADA)
                s.aceptarRechazarEdicion(f.edicion,f.estado == EstadoEdicion.ACEPTADA,admin);
        }
        String[] instituciones = {"Cooperativa Horizonte", "Fundación Río de la Plata"};
        for (String nombre : instituciones) if (!s.existeInstitucion(nombre))
            exigir(s.altaInstitucion(new DtInstitucion(nombre,"Institución ficticia para prácticas académicas.","https://example.com"),admin),nombre);
        // Patrocinios y registros se añaden solo cuando su clave natural no existe.
        for (PatrocinioPreparado p : PATROCINIOS) patrocinio(s,p.evento,p.edicion,p.institucion,p.codigo,p.monto,p.gratuitos,p.organizador);
        for (int i=0; i<ASISTENTES.length; i++) {
            String nick = ASISTENTES[i][0];
            for (int j=0; j<2; j++) {
                String edicion = FICHAS.get((i+j*6) % 13).edicion;
                boolean existe = s.obtenerRegistrosAsistente(nick).stream().anyMatch(r -> edicion.equals(r.getNombreEdicion()));
                if (!existe) exigir(s.registrarAutorizado(edicion,"General","general",null,Actor.usuarioAutenticado((DtAsistente)s.consultarUsuario(nick))),"registro " + nick);
            }
        }
        System.out.println("Carga terminada. Las claves naturales existentes se conservaron.");
    }
    private static void patrocinio(ISistema s, String evento, String edicion, String institucion,
                                    String codigo, int monto, int gratis, String org) {
        if (s.consultarPatrocinio(codigo,Actor.administradorLocal()) != null) return;
        DtPatrocinio p = new DtPatrocinio(null,monto,gratis,codigo,NivelPatrocinio.PLATA,institucion,edicion,"General");
        exigir(s.altaPatrocinio(p,evento,Actor.usuarioAutenticado((DtOrganizador)s.consultarUsuario(org))),"patrocinio " + codigo);
    }
    private static void verificar(ISistema s) {
        long eventos = s.listarEventos().stream().filter(e -> FICHAS.stream().anyMatch(f -> f.evento.equals(e.getNombre()))).count();
        if (eventos != FICHAS.size()) throw new IllegalStateException("Faltan eventos de defensa: " + eventos + "/16");
        for (Ficha f : FICHAS) {
            DtEdicion e = s.obtenerEdicionesEvento(f.evento).stream().filter(x -> f.edicion.equals(x.getIdNombre())).findFirst().orElseThrow();
            if (!e.getFechaInicio().equals(f.inicio) || !e.getFechaFin().equals(f.inicio.plusDays(f.dias-1)))
                throw new IllegalStateException("Fechas inesperadas en " + f.edicion);
            if (e.getEstado() != f.estado) throw new IllegalStateException("Estado inesperado: " + f.edicion);
            DtTipoRegistro tipo = s.consultarTipoRegistro(f.edicion,"General");
            if (tipo == null || Float.compare(tipo.getCosto(),f.precio) != 0)
                throw new IllegalStateException("Falta entrada o cambió su precio: " + f.edicion);
            if (s.listarEventosPorCategoria(f.categoria).stream().noneMatch(x -> f.evento.equals(x.getNombre())))
                throw new IllegalStateException("El filtro web no devuelve " + f.evento);
        }
        for (String[] o : ORGANIZADORES) if (!s.existeUsuario(o[0])) throw new IllegalStateException("Falta organizador " + o[0]);
        for (String[] a : ASISTENTES) {
            if (!s.existeUsuario(a[0])) throw new IllegalStateException("Falta asistente " + a[0]);
            if (s.obtenerRegistrosAsistente(a[0]).size() < 2) throw new IllegalStateException("Faltan registros de " + a[0]);
        }
        if (!s.existeInstitucion("Cooperativa Horizonte") || !s.existeInstitucion("Fundación Río de la Plata")
                || s.consultarPatrocinio("HORIZONTE27",Actor.administradorLocal()) == null
                || s.consultarPatrocinio("RIOPLATA27",Actor.administradorLocal()) == null)
            throw new IllegalStateException("Faltan instituciones o patrocinios.");
        if (s.buscarEventosYEdiciones("Música",OrdenBusqueda.NOMBRE_ASC).isEmpty())
            throw new IllegalStateException("La búsqueda web no encuentra Música.");
        System.out.println("Verificados: 16 eventos y ediciones, 16 entradas, 10 usuarios, 2 patrocinios, 12 registros y consultas web.");
    }
}
