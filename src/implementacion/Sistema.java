package implementacion;

import clases.*;
import datatypes.*;
import interfaces.ISistema;
import manejadores.ManejadorUsuarios;
import manejadores.ManejadorInstituciones;
import manejadores.ManejadorPatrocinios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

public class Sistema implements ISistema {

    private final ManejadorUsuarios manejadorUsuarios;
    private final ManejadorInstituciones manejadorInstituciones;
    private final ManejadorPatrocinios manejadorPatrocinios;

    public Sistema() {

        manejadorUsuarios =
                ManejadorUsuarios.getInstance();

        manejadorInstituciones =
                ManejadorInstituciones.getInstance();

        manejadorPatrocinios =
                ManejadorPatrocinios.getInstance();

        cargarDatosPrueba();
    }


    private void cargarDatosPrueba() {

        if (!manejadorUsuarios.existeUsuario("joaquin")) {

            Asistente asistente =
                    new Asistente(
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

            Organizador organizador =
                    new Organizador(
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
    public boolean existeCorreoElectronico(
            String correoElectronico) {

        return manejadorUsuarios.existeCorreo(
                correoElectronico
        );
    }


    // =====================================================
    // ALTA USUARIO
    // =====================================================

    @Override
    public boolean altaAsistente(DtAsistente dt) {

        Asistente asistente =
                new Asistente(
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
    public boolean altaOrganizador(
            DtOrganizador dt) {

        Organizador organizador =
                new Organizador(
                        dt.getNickname(),
                        dt.getNombre(),
                        dt.getCorreoElectronico(),
                        dt.getDescripcion(),
                        dt.getSitioWeb()
                );

        return manejadorUsuarios.addUsuario(
                organizador
        );
    }


    // =====================================================
    // CONSULTA / LISTADO USUARIO
    // =====================================================

    @Override
    public Collection<DtUsuario> listarUsuarios() {

        Collection<DtUsuario> resultado =
                new ArrayList<>();

        for (Usuario usuario :
                manejadorUsuarios.listarUsuarios()) {

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

            } else if (
                    usuario instanceof Organizador organizador) {

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


    @Override
    public DtUsuario consultarUsuario(
            String nickname) {

        Usuario usuario =
                manejadorUsuarios.obtenerUsuario(
                        nickname
                );

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
    // MODIFICAR USUARIO
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
    }


    @Override
    public void modificarOrganizador(
            DtOrganizador dt) {

        manejadorUsuarios.modificarOrganizador(
                dt.getNickname(),
                dt.getNombre(),
                dt.getCorreoElectronico(),
                dt.getDescripcion(),
                dt.getSitioWeb()
        );
    }


    // =====================================================
    // INSTITUCIONES
    // =====================================================

    @Override
    public boolean altaInstitucion(
            DtInstitucion dt) {

        Institucion institucion =
                new Institucion(
                        dt.getNombre(),
                        dt.getDescripcion(),
                        dt.getSitioWeb()
                );

        return manejadorInstituciones.addInstitucion(
                institucion
        );
    }


    @Override
    public boolean existeInstitucion(
            String nombre) {

        return manejadorInstituciones.existeInstitucion(
                nombre
        );
    }


    @Override
    public Collection<DtInstitucion>
    listarInstituciones() {

        Collection<DtInstitucion> resultado =
                new ArrayList<>();

        for (Institucion institucion :
                manejadorInstituciones.listarInstituciones()) {

            resultado.add(
                    new DtInstitucion(
                            institucion.getNombre(),
                            institucion.getDescripcion(),
                            institucion.getSitioWeb()
                    )
            );
        }

        return resultado;
    }


    // =====================================================
    // ALTA PATROCINIO
    // =====================================================

    @Override
    public boolean altaPatrocinio(
            DtPatrocinio dt) {

        Patrocinio patrocinio =
                new Patrocinio(
                        dt.getFecha(),
                        dt.getMontoAporte(),
                        dt.getCantRegistrosGrat(),
                        dt.getCodigoPatrocinio(),
                        dt.getNivel()
                );

        return manejadorPatrocinios.addPatrocinio(
                patrocinio
        );
    }


    // =====================================================
    // CONSULTA PATROCINIO
    // =====================================================

    @Override
    public Collection<DtPatrocinio>
    listarPatrocinios() {

        Collection<DtPatrocinio> resultado =
                new ArrayList<>();

        for (Patrocinio patrocinio :
                manejadorPatrocinios.listarPatrocinios()) {

            resultado.add(
                    new DtPatrocinio(
                            patrocinio.getFecha(),
                            patrocinio.getMontoAporte(),
                            patrocinio.getCantRegistrosGrat(),
                            patrocinio.getCodigoPatrocinio(),
                            patrocinio.getNivel()
                    )
            );
        }

        return resultado;
    }


    @Override
    public DtPatrocinio consultarPatrocinio(
            String codigo) {

        Patrocinio patrocinio =
                manejadorPatrocinios.obtenerPatrocinio(
                        codigo
                );

        if (patrocinio == null) {
            return null;
        }

        return new DtPatrocinio(
                patrocinio.getFecha(),
                patrocinio.getMontoAporte(),
                patrocinio.getCantRegistrosGrat(),
                patrocinio.getCodigoPatrocinio(),
                patrocinio.getNivel()
        );
    }
}