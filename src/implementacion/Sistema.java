package implementacion;

import clases.*;
import datatypes.*;
import interfaces.ISistema;
import manejadores.ManejadorUsuarios;
import manejadores.ManejadorInstituciones;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

public class Sistema implements ISistema {

    private final ManejadorUsuarios manejadorUsuarios;
    private final ManejadorInstituciones manejadorInstituciones;

    public Sistema() {
        manejadorUsuarios = ManejadorUsuarios.getInstance();
        manejadorInstituciones = ManejadorInstituciones.getInstance();

        // Temporal: datos para probar sin persistencia
        cargarDatosPrueba();
    }

    private void cargarDatosPrueba() {

        if (!manejadorUsuarios.existeUsuario("joaquin")) {

            Asistente asistente = new Asistente(
                    "joaquin",
                    "Joaquin",
                    "joaquin@gmail.com",
                    "Gonzalez",
                    LocalDate.of(2004, 8, 6),
                    null
            );

            manejadorUsuarios.addUsuario(asistente);
        }


        if (!manejadorUsuarios.existeUsuario("ignacio")) {

            Organizador organizador = new Organizador(
                    "ignacio",
                    "Ignacio",
                    "ignacio@gmail.com",
                    "Organizador de eventos",
                    "www.ignacio.com"
            );

            manejadorUsuarios.addUsuario(organizador);
        }
    }


    // =====================================================
    // VALIDACIONES
    // =====================================================

    @Override
    public boolean existeUsuario(String nickname) {
        return manejadorUsuarios.existeUsuario(nickname);
    }


    @Override
    public boolean existeCorreoElectronico(String correoElectronico) {

        return manejadorUsuarios.existeCorreo(correoElectronico);
    }


    // =====================================================
    // ALTA DE USUARIO
    // =====================================================

    @Override
    public boolean altaAsistente(DtAsistente dt) {

        Asistente asistente = new Asistente(
                dt.getNickname(),
                dt.getNombre(),
                dt.getCorreoElectronico(),
                dt.getApellido(),
                dt.getFechaNacimiento(),
                dt.getNombreInstitucion()
        );

        return manejadorUsuarios.addUsuario(asistente);
    }


    @Override
    public boolean altaOrganizador(DtOrganizador dt) {

        Organizador organizador = new Organizador(
                dt.getNickname(),
                dt.getNombre(),
                dt.getCorreoElectronico(),
                dt.getDescripcion(),
                dt.getSitioWeb()
        );

        return manejadorUsuarios.addUsuario(organizador);
    }


    // =====================================================
    // LISTADO DE USUARIOS
    // =====================================================

    @Override
    public Collection<DtUsuario> listarUsuarios() {

        Collection<DtUsuario> resultado = new ArrayList<>();

        for (Usuario usuario : manejadorUsuarios.listarUsuarios()) {

            if (usuario instanceof Asistente asistente) {

                resultado.add(
                        new DtAsistente(
                                asistente.getNickname(),
                                asistente.getNombre(),
                                asistente.getCorreoElectronico(),
                                asistente.getApellido(),
                                asistente.getFechaNacimiento(),
                                asistente.getNombreInstitucion()
                        )
                );

            } else if (usuario instanceof Organizador organizador) {

                resultado.add(
                        new DtOrganizador(
                                organizador.getNickname(),
                                organizador.getNombre(),
                                organizador.getCorreoElectronico(),
                                organizador.getDescripcion(),
                                organizador.getSitioWeb()
                        )
                );
            }
        }

        return resultado;
    }


    // =====================================================
    // CONSULTA DE USUARIO
    // =====================================================

    @Override
    public DtUsuario consultarUsuario(String nickname) {

        Usuario usuario =
                manejadorUsuarios.obtenerUsuario(nickname);

        if (usuario == null) {
            return null;
        }


        if (usuario instanceof Asistente asistente) {

            return new DtAsistente(
                    asistente.getNickname(),
                    asistente.getNombre(),
                    asistente.getCorreoElectronico(),
                    asistente.getApellido(),
                    asistente.getFechaNacimiento(),
                    asistente.getNombreInstitucion()
            );
        }


        if (usuario instanceof Organizador organizador) {

            return new DtOrganizador(
                    organizador.getNickname(),
                    organizador.getNombre(),
                    organizador.getCorreoElectronico(),
                    organizador.getDescripcion(),
                    organizador.getSitioWeb()
            );
        }


        return null;
    }


    // =====================================================
    // MODIFICAR ASISTENTE
    // =====================================================

    @Override
    public void modificarAsistente(
            DtAsistente dt) {

        manejadorUsuarios.modificarAsistente(
                dt.getNickname(),
                dt.getNombre(),
                dt.getCorreoElectronico(),
                dt.getApellido(),
                dt.getFechaNacimiento(),
                dt.getNombreInstitucion()
        );

        /*
         * Después conectamos institución con
         * ManejadorInstituciones.
         */
    }


    // =====================================================
    // MODIFICAR ORGANIZADOR
    // =====================================================

    @Override
    public void modificarOrganizador(DtOrganizador dt) {

        manejadorUsuarios.modificarOrganizador(
                dt.getNickname(),
                dt.getNombre(),
                dt.getCorreoElectronico(),
                dt.getDescripcion(),
                dt.getSitioWeb()
        );
    }

    // =====================================================
    // ALTA INSTITUCION
    // =====================================================
    @Override
    public boolean altaInstitucion(DtInstitucion dt) {

        Institucion institucion = new Institucion(
                dt.getNombre(),
                dt.getDescripcion(),
                dt.getSitioWeb()

        );

        return manejadorInstituciones.addInstitucion(institucion);
    }
    @Override
    public boolean existeInstitucion(String Nombre) {
        return manejadorInstituciones.existeInstitucion(Nombre);
    }
    @Override
    public Collection<DtInstitucion> listarInstituciones(){
        Collection<DtInstitucion> resultado = new ArrayList<>();

        for (Institucion institucion : manejadorInstituciones.listarInstituciones()) {
            resultado.add(new DtInstitucion(
                    institucion.getNombre(),
                    institucion.getDescripcion(),
                    institucion.getSitioWeb()
            ));
        }
        return resultado;
    }
}