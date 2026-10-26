package datos;

import clases.NivelPatrocinio;
import datatypes.DtAsistente;
import datatypes.DtEdicion;
import datatypes.DtEvento;
import datatypes.DtInstitucion;
import datatypes.DtOrganizador;
import datatypes.DtPatrocinio;
import datatypes.DtRegistro;
import datatypes.DtTipoRegistro;
import datatypes.DtUsuario;
import implementacion.Fabrica;
import interfaces.ISistema;
import persistencia.BaseDeDatos;

import java.time.LocalDate;
import java.util.List;

/**
 * Carga los datos de prueba: organizadores y asistentes, ediciones en los
 * tres estados, tipos de registro, instituciones, un patrocinio, un registro
 * general y un registro con código de patrocinio.
 *
 * Se puede ejecutar varias veces: lo que ya existe se omite. Para partir de cero,
 * borrar y volver a crear la base. Todas las contraseñas son 1234.
 */
public class CargarDatosPrueba {

    private static final String CLAVE = "1234";

    @FunctionalInterface
    private interface Accion {
        boolean ejecutar() throws Exception;
    }

    private static int cargados = 0;
    private static int omitidos = 0;

    private static void intentar(String descripcion, Accion accion) {
        try {
            if (accion.ejecutar()) {
                cargados++;
                System.out.println("OK:       " + descripcion);
            } else {
                omitidos++;
                System.out.println("OMITIDO:  " + descripcion + " (ya existía)");
            }
        } catch (Exception e) {
            omitidos++;
            System.out.println("OMITIDO:  " + descripcion + " -> " + e.getMessage());
        }
    }

    private static <T extends DtUsuario> T conClave(T usuario) {
        usuario.setContrasenia(CLAVE);
        usuario.setConfirmacionContrasenia(CLAVE);
        return usuario;
    }

    private static DtEdicion edicion(String nombre, String sigla, LocalDate inicio, LocalDate fin,
                                     LocalDate alta, String organizador) {
        return new DtEdicion(nombre, sigla, inicio, fin, alta, "Montevideo", "Uruguay", organizador);
    }

    public static void main(String[] args) {
        ISistema sistema = Fabrica.getInstance().getISistema();
        LocalDate hoy = LocalDate.now();

        // ---- Categorías
        for (String categoria : List.of("Tecnología", "Cultura", "Deporte", "Música", "Negocios")) {
            intentar("categoría " + categoria, () -> {
                sistema.altaCategoria(categoria, null);
                return true;
            });
        }

        // ---- Instituciones
        intentar("institución UTEC", () -> sistema.altaInstitucion(new DtInstitucion(
                "UTEC", "Universidad Tecnológica", "https://www.utec.edu.uy")));
        intentar("institución ANTEL", () -> sistema.altaInstitucion(new DtInstitucion(
                "ANTEL", "Empresa de telecomunicaciones", "https://www.antel.com.uy")));

        // ---- Organizadores
        intentar("organizador imm", () -> sistema.altaOrganizador(conClave(new DtOrganizador(
                "imm", "Intendencia de Montevideo", "imm@montevideo.gub.uy",
                "Gobierno departamental de Montevideo", "https://montevideo.gub.uy"))));
        intentar("organizador mvdcomics", () -> sistema.altaOrganizador(conClave(new DtOrganizador(
                "mvdcomics", "Montevideo Comics SA", "info@mvdcomics.uy",
                "Productora de eventos de cultura geek", ""))));

        // ---- Asistentes (juan23 y ana_utec pertenecen a UTEC; maria88 no tiene institución)
        intentar("asistente juan23", () -> sistema.altaAsistente(conClave(new DtAsistente(
                "juan23", "Juan", "juan@mail.com", "Pérez", LocalDate.of(1998, 4, 12), "UTEC"))));
        intentar("asistente maria88", () -> sistema.altaAsistente(conClave(new DtAsistente(
                "maria88", "María", "maria@mail.com", "Gómez", LocalDate.of(1995, 9, 30), null))));
        intentar("asistente ana_utec", () -> sistema.altaAsistente(conClave(new DtAsistente(
                "ana_utec", "Ana", "ana@mail.com", "Silva", LocalDate.of(2000, 1, 18), "UTEC"))));

        // ---- Eventos
        intentar("evento Montevideo Comics", () -> sistema.altaEvento(new DtEvento(
                "Montevideo Comics", "MVDCOMICS", "Convención de historietas y cultura geek",
                LocalDate.of(2026, 3, 12), List.of("Cultura"))));
        intentar("evento Maratón de Montevideo", () -> sistema.altaEvento(new DtEvento(
                "Maratón de Montevideo", "MARATON", "Competencia deportiva anual en la capital",
                LocalDate.of(2026, 3, 12), List.of("Tecnología", "Deporte"))));

        // ---- Ediciones (todas nacen Ingresada)
        String[][] ediciones = {
                // evento, nombre, sigla, organizador
                {"Montevideo Comics", "Montevideo Comics 2026", "MC2026", "mvdcomics"},
                {"Montevideo Comics", "Montevideo Comics 2027", "MC2027", "mvdcomics"},
                {"Montevideo Comics", "Montevideo Comics 2025", "MC2025", "mvdcomics"},
                {"Maratón de Montevideo", "Maratón de Montevideo 2026", "MM2026", "imm"},
                {"Maratón de Montevideo", "Maratón de Montevideo 2027", "MM2027", "imm"},
                {"Maratón de Montevideo", "Maratón de Montevideo 2025", "MM2025", "imm"}
        };
        int[] anios = {2026, 2027, 2025, 2026, 2027, 2025};

        for (int i = 0; i < ediciones.length; i++) {
            String[] e = ediciones[i];
            int anio = anios[i];
            boolean comics = e[0].equals("Montevideo Comics");
            LocalDate inicio = comics ? LocalDate.of(anio, 11, 10) : LocalDate.of(anio, 11, 22);
            LocalDate fin = comics ? LocalDate.of(anio, 11, 12) : LocalDate.of(anio, 11, 22);

            intentar("edición " + e[1], () -> sistema.altaEdicion(
                    edicion(e[1], e[2], inicio, fin, LocalDate.of(anio - 1, 6, 1), e[3]), e[0]));
        }

        // ---- Tipos de registro
        intentar("tipo General (MC2026)", () -> sistema.altaTipoRegistro(new DtTipoRegistro(
                        "General", "Acceso general a los tres días de la convención", 1500, 100),
                "Montevideo Comics 2026"));
        intentar("tipo Estudiante (MC2026)", () -> sistema.altaTipoRegistro(new DtTipoRegistro(
                        "Estudiante", "Acceso con descuento para estudiantes", 800, 50),
                "Montevideo Comics 2026"));
        intentar("tipo Corredor 10K (Maratón 2026)", () -> sistema.altaTipoRegistro(new DtTipoRegistro(
                        "Corredor 10K", "Participación en la carrera de 10 kilómetros", 1000, 2),
                "Maratón de Montevideo 2026"));
        intentar("tipo Corredor 21K (Maratón 2026)", () -> sistema.altaTipoRegistro(new DtTipoRegistro(
                        "Corredor 21K", "Participación en la media maratón", 1800, 50),
                "Maratón de Montevideo 2026"));

        // ---- Estados de las ediciones: Aceptada, Rechazada y (el resto) Ingresada
        for (String nombre : List.of("Montevideo Comics 2026", "Maratón de Montevideo 2026")) {
            intentar("aceptar " + nombre, () -> {
                sistema.aceptarRechazarEdicion(nombre, true);
                return true;
            });
        }
        for (String nombre : List.of("Montevideo Comics 2025", "Maratón de Montevideo 2025")) {
            intentar("rechazar " + nombre, () -> {
                sistema.aceptarRechazarEdicion(nombre, false);
                return true;
            });
        }

        // ---- Patrocinio de UTEC (5 gratuitos de $1500 = $7500, menos del 20 % de $50000)
        intentar("patrocinio UTEC2026", () -> sistema.altaPatrocinio(new DtPatrocinio(
                hoy, 50000, 5, "UTEC2026", NivelPatrocinio.ORO,
                "UTEC", "Montevideo Comics 2026", "General")));

        // ---- Registros (las ediciones ya están aceptadas)
        intentar("registro general: maria88 en MC2026 (Estudiante)", () -> sistema.registroAEdicion(
                "maria88", "Montevideo Comics 2026", "Estudiante", new DtRegistro(hoy, 800)));
        intentar("registro con código: ana_utec en MC2026 (General, UTEC2026)", () -> sistema.registroAEdicion(
                "ana_utec", "Montevideo Comics 2026", "General", new DtRegistro(hoy, 0), "UTEC2026"));
        intentar("registro general: juan23 en Maratón 2026 (Corredor 10K)", () -> sistema.registroAEdicion(
                "juan23", "Maratón de Montevideo 2026", "Corredor 10K", new DtRegistro(hoy, 1000)));

        System.out.println();
        System.out.println("Datos de prueba: " + cargados + " cargados, " + omitidos + " omitidos.");
        System.out.println("Usuarios: imm, mvdcomics (organizadores) y juan23, maria88, ana_utec (asistentes).");
        System.out.println("Contraseña de todos: " + CLAVE);

        BaseDeDatos.cerrar();
    }
}