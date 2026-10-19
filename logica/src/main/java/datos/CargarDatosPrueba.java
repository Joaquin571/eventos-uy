package datos;

import datatypes.DtAsistente;
import datatypes.DtEvento;
import datatypes.DtOrganizador;
import datatypes.DtUsuario;
import implementacion.Fabrica;
import interfaces.ISistema;
import persistencia.BaseDeDatos;

import java.time.LocalDate;
import java.util.List;

public class CargarDatosPrueba {

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    private static void intentar(String descripcion, Accion accion) {
        try {
            accion.ejecutar();
            System.out.println("OK: " + descripcion);
        } catch (Exception e) {
            System.out.println("OMITIDO: " + descripcion + " -> " + e.getMessage());
        }
    }

    private static <T extends DtUsuario> T conClave(T usuario, String clave) {
        usuario.setContrasenia(clave);
        usuario.setConfirmacionContrasenia(clave);
        return usuario;
    }

    public static void main(String[] args) {
        ISistema sistema = Fabrica.getInstance().getISistema();

        for (String categoria : List.of("Tecnología", "Cultura", "Deporte", "Música", "Negocios")) {
            intentar("categoría " + categoria, () -> sistema.altaCategoria(categoria, null));
        }

        DtOrganizador imm = conClave(new DtOrganizador(
                "imm", "Intendencia de Montevideo", "imm@montevideo.gub.uy",
                "Gobierno departamental de Montevideo", "https://montevideo.gub.uy"), "1234");
        DtOrganizador mvdcomics = conClave(new DtOrganizador(
                "mvdcomics", "Montevideo Comics SA", "info@mvdcomics.uy",
                "Productora de eventos de cultura geek", ""), "1234");

        intentar("organizador imm", () -> sistema.altaOrganizador(imm));
        intentar("organizador mvdcomics", () -> sistema.altaOrganizador(mvdcomics));

        DtAsistente juan = conClave(new DtAsistente(
                "juan23", "Juan", "juan@mail.com", "Pérez", LocalDate.of(1998, 4, 12), null), "1234");
        DtAsistente maria = conClave(new DtAsistente(
                "maria88", "María", "maria@mail.com", "Gómez", LocalDate.of(1995, 9, 30), null), "1234");
        DtAsistente ana = conClave(new DtAsistente(
                "ana_utec", "Ana", "ana@mail.com", "Silva", LocalDate.of(2000, 1, 18), null), "1234");

        intentar("asistente juan23", () -> sistema.altaAsistente(juan));
        intentar("asistente maria88", () -> sistema.altaAsistente(maria));
        intentar("asistente ana_utec", () -> sistema.altaAsistente(ana));

        intentar("evento Montevideo Comics", () -> sistema.altaEvento(new DtEvento(
                "Montevideo Comics", "MVDCOMICS", "Convención de historietas y cultura geek",
                LocalDate.of(2026, 3, 12), List.of("Cultura"))));
        intentar("evento Maratón de Montevideo", () -> sistema.altaEvento(new DtEvento(
                "Maratón de Montevideo", "MARATON", "Competencia deportiva anual en la capital",
                LocalDate.of(2026, 3, 12), List.of("Tecnología", "Deporte"))));

        BaseDeDatos.cerrar();
    }
}