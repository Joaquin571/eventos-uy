package implementacion;

import clases.*;
import datatypes.*;
import interfaces.ISistema;
import manejadores.ManejadorUsuarios;
import manejadores.ManejadorInstituciones;
import manejadores.ManejadorEventos;
import manejadores.ManejadorPatrocinios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Sistema implements ISistema {

    private final ManejadorUsuarios manejadorUsuarios;
    private final ManejadorInstituciones manejadorInstituciones;
    private final ManejadorEventos manejadorEventos;
    private final ManejadorPatrocinios manejadorPatrocinios;

    public Sistema() {

        manejadorUsuarios = ManejadorUsuarios.getInstance();
        manejadorInstituciones = ManejadorInstituciones.getInstance();
        manejadorPatrocinios = ManejadorPatrocinios.getInstance();
        manejadorEventos = ManejadorEventos.getInstance();

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
        Institucion institucion=manejadorInstituciones.obtenerInstitucion( dt.getNombreInstitucion());
        Asistente asistente =
                new Asistente(
                        dt.getNickname(),
                        dt.getNombre(),
                        dt.getCorreoElectronico(),
                        dt.getApellido(),
                        dt.getFechaNacimiento(),
                        institucion

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
                                asistente.getInstitucion().getNombre()
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
                    asistente.getInstitucion().getNombre()
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
    // CATEGORÍAS
    // =====================================================

    @Override
    public Collection<String> listarCategorias() {
        Collection<String> resultado = new ArrayList<>();
        for (Categoria c : manejadorEventos.obtenerCategorias()) {
            resultado.add(c.getNombre());
        }
        return resultado;
    }
    // =====================================================
    // ALTA PATROCINIO
    // =====================================================

    @Override
    public boolean altaPatrocinio(
            DtPatrocinio dt) {
        Institucion institucion=manejadorInstituciones.obtenerInstitucion(dt.getNombreInstituto());
        Patrocinio patrocinio =
                new Patrocinio(
                        dt.getFecha(),
                        dt.getMontoAporte(),
                        dt.getCantRegistrosGrat(),
                        dt.getCodigoPatrocinio(),
                        dt.getNivel(),
                        institucion

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
                            patrocinio.getNivel(),
                            patrocinio.getInstitucion().getNombre()
                    )
            );
        }
        
        return resultado;
    }
        
    // =====================================================
    // ALTA DE EVENTO
    // =====================================================

    @Override
    public boolean existeEvento(String nombre) {
        return manejadorEventos.existeEvento(nombre);
    }

    @Override
    public boolean altaEvento(DtEvento dt) throws Exception {

        // 1. Validar si el evento ya existe por nombre
        if (manejadorEventos.existeEvento(dt.getNombre())) {
            throw new Exception("Ya existe un evento con el nombre: " + dt.getNombre());
        }

        // 2. Validar que se haya seleccionado al menos una categoría
        if (dt.getCategorias() == null || dt.getCategorias().isEmpty()) {
            throw new Exception("Debe seleccionar al menos una categoría para el evento.");
        }


        // 3. Instanciar la clase de dominio Evento
        Evento evento = new Evento(
                dt.getNombre(),
                dt.getSigla(),
                dt.getDescripcion(),
                dt.getFechaAlta()

        );

        // 4. Obtener cada Categoria desde manejadorEventos y asociarla al Evento
        for (String nombreCat : dt.getCategorias()) {
            Categoria cat = manejadorEventos.obtenerCategoria(nombreCat);
            if (cat != null) {
                evento.agregarCategoria(cat);
            } else {
                throw new Exception("La categoría '" + nombreCat + "' no existe en el sistema.");
            }
        }

        // 5. Guardar el evento en la colección en memoria
        return manejadorEventos.addEvento(evento);
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
                patrocinio.getNivel(),
                patrocinio.getInstitucion().getNombre()
        );
    }

    // =====================================================
    // REGISTRO
    // =====================================================

    @Override
    public boolean estaRegistradoAEdicion(String nicknameAsistente, String nombreEdicion) {
        return manejadorUsuarios.estaRegistradoAEdicion(nicknameAsistente, nombreEdicion);
    }

    @Override
    public boolean registroAEdicion(String nicknameAsistente, String nombreEdicion, String nombreTipoRegistro, DtRegistro dtRegistro)throws Exception {
        Usuario usr = manejadorUsuarios.obtenerUsuario(nicknameAsistente);
        if (!(usr instanceof Asistente)) {
            return false;
        }
        Asistente asistente = (Asistente) usr;
        Edicion edicion = manejadorEventos.obtenerEdicion(nombreEdicion);
        if (edicion == null) {
            return false;
        }
        TipoRegistro tipoRegistro = edicion.obtenerTipoRegistro(nombreTipoRegistro);
        if (tipoRegistro == null) {
            return false;
        }


        Registro registro = new Registro(dtRegistro.getFechaRegistro(), tipoRegistro.getCosto(),asistente, edicion,tipoRegistro);
        tipoRegistro.agregarRegistro(registro);

        return true;
    }


    // =====================================================
    // Evento y Ediciones
    // =====================================================
    @Override
    public Collection<DtEvento> listarEventos() {
        Collection<Evento> eventos = manejadorEventos.obtenerEventos();
        Collection<DtEvento> dtEventos = new ArrayList<>();

        for (Evento e : eventos) {
            Set<String> nombresCategorias = new HashSet<>();
            for (Categoria c : e.getCategorias()) {
                nombresCategorias.add(c.getNombre());
            }

            dtEventos.add(new DtEvento(
                    e.getNombre(),
                    e.getSigla(),
                    e.getDescripcion(),
                    e.getFechaAlta(),
                    nombresCategorias
            ));
        }

        return dtEventos;
    }

    @Override
    public Collection<DtEdicion> obtenerEdicionesEvento(String nombreEvento) {
        Collection<Edicion> ediciones = manejadorEventos.obtenerEdicionesEvento(nombreEvento);
        Collection<DtEdicion> dtEdiciones = new ArrayList<>();
        for (Edicion ed : ediciones) {
            dtEdiciones.add(new DtEdicion(ed.getIdNombre(), ed.getSigla(), ed.getFechaInicio(), ed.getFechaFin(), ed.getFechaAlta(), ed.getCiudad(), ed.getPais(),ed.getOrganizador().getNickname()));
        }
        return dtEdiciones;
    }

    // =====================================================
    // Evento y Ediciones
    // =====================================================
    @Override
    public boolean altaTipoRegistro(DtTipoRegistro dtTipoRegistro, String nombreEdicion) {
        Edicion edicion = manejadorEventos.obtenerEdicion(nombreEdicion);
        if (edicion == null || manejadorEventos.existeTipoRegistro(dtTipoRegistro.getIdNombre())) {
            return false;
        }
        TipoRegistro tr = new TipoRegistro(dtTipoRegistro.getIdNombre(), dtTipoRegistro.getDescripcion(), dtTipoRegistro.getCosto(), dtTipoRegistro.getCupo());
        manejadorEventos.addTipoRegistro(tr);
        edicion.agregarTipoRegistro(tr);
        return true;
    }

    @Override
    public Collection<DtTipoRegistro> obtenerTiposRegistroEdicion(String nombreEdicion) {
        Collection<TipoRegistro> tipos = manejadorEventos.obtenerTiposRegistroEdicion(nombreEdicion);
        Collection<DtTipoRegistro> dtTipos = new ArrayList<>();
        for (TipoRegistro tr : tipos) {
            dtTipos.add(new DtTipoRegistro(tr.getIdNombre(), tr.getDescripcion(), tr.getCosto(), tr.getCupo()));
        }
        return dtTipos;
    }

    @Override
    public DtTipoRegistro consultarTipoRegistro(String nombreTipoRegistro) {
        TipoRegistro tr = manejadorEventos.obtenerTipoRegistro(nombreTipoRegistro);
        if (tr != null) {
            return new DtTipoRegistro(tr.getIdNombre(), tr.getDescripcion(), tr.getCosto(), tr.getCupo());
        }
        return null;
    }

    @Override
    public boolean altaEdicion(DtEdicion dtEdicion, String nombreEvento) {
        Evento evento = manejadorEventos.obtenerEvento(nombreEvento);
        if (evento == null || manejadorEventos.existeEdicion(dtEdicion.getIdNombre())) {
            return false;
        }
         Usuario usuario=manejadorUsuarios.obtenerOrganizador(dtEdicion.getNombreOrganizador());
        if(usuario==null || !(usuario instanceof Organizador))
        {
            return false;
        }
        Organizador organizador=(Organizador)usuario;
        Edicion edicion = new Edicion(
                dtEdicion.getIdNombre(),
                dtEdicion.getSigla(),
                dtEdicion.getFechaInicio(),
                dtEdicion.getFechaFin(),
                dtEdicion.getFechaAlta(),
                dtEdicion.getCiudad(),
                dtEdicion.getPais(),
                organizador
        );

        manejadorEventos.addEdicion(edicion);
        evento.agregarEdicion(edicion);
        return true;
    }

}

