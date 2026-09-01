package implementacion;

import clases.*;
import datatypes.*;
import interfaces.ISistema;
import manejadores.ManejadorEventos;
import manejadores.ManejadorInstituciones;
import manejadores.ManejadorPatrocinios;
import manejadores.ManejadorUsuarios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Sistema implements ISistema {

    private final ManejadorUsuarios manejadorUsuarios;
    private final ManejadorInstituciones manejadorInstituciones;
    private final ManejadorEventos manejadorEventos;
    private final ManejadorPatrocinios manejadorPatrocinios;

    public Sistema() {
        manejadorUsuarios = ManejadorUsuarios.getInstance();
        manejadorInstituciones = ManejadorInstituciones.getInstance();
        manejadorEventos = ManejadorEventos.getInstance();
        manejadorPatrocinios = ManejadorPatrocinios.getInstance();

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
    // VALIDACIONES USUARIO
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
    // ALTA USUARIO
    // =====================================================

    @Override
    public boolean altaAsistente(DtAsistente dt) {

        Institucion institucion = null;

        if (dt.getNombreInstitucion() != null &&
                !dt.getNombreInstitucion().isBlank()) {

            institucion = manejadorInstituciones.obtenerInstitucion(
                    dt.getNombreInstitucion()
            );
        }

        Asistente asistente = new Asistente(
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
    // CONSULTA USUARIO
    // =====================================================

    @Override
    public Collection<DtUsuario> listarUsuarios() {

        Collection<DtUsuario> resultado = new ArrayList<>();

        for (Usuario usuario : manejadorUsuarios.listarUsuarios()) {

            if (usuario instanceof Asistente asistente) {

                resultado.add(new DtAsistente(
                        asistente.getNickname(),
                        asistente.getNombre(),
                        asistente.getCorreoElectronico(),
                        asistente.getApellido(),
                        asistente.getFechaNacimiento(),
                        asistente.getNombreInstitucion()
                ));

            } else if (usuario instanceof Organizador organizador) {

                resultado.add(new DtOrganizador(
                        organizador.getNickname(),
                        organizador.getNombre(),
                        organizador.getCorreoElectronico(),
                        organizador.getDescripcion(),
                        organizador.getSitioWeb()
                ));
            }
        }

        return resultado;
    }

    @Override
    public DtUsuario consultarUsuario(String nickname) {

        Usuario usuario = manejadorUsuarios.obtenerUsuario(nickname);

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
    public void modificarAsistente(DtAsistente dt) {

        Institucion institucion = null;

        if (dt.getNombreInstitucion() != null &&
                !dt.getNombreInstitucion().isBlank()) {

            institucion =
                    manejadorInstituciones.obtenerInstitucion(
                            dt.getNombreInstitucion()
                    );
        }

        manejadorUsuarios.modificarAsistente(
                dt.getNickname(),
                dt.getNombre(),
                dt.getCorreoElectronico(),
                dt.getApellido(),
                dt.getFechaNacimiento(),
                institucion
        );
    }

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
    // INSTITUCIONES
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
    public boolean existeInstitucion(String nombre) {
        return manejadorInstituciones.existeInstitucion(nombre);
    }

    @Override
    public Collection<DtInstitucion> listarInstituciones() {

        Collection<DtInstitucion> resultado = new ArrayList<>();

        for (Institucion institucion :
                manejadorInstituciones.listarInstituciones()) {

            resultado.add(new DtInstitucion(
                    institucion.getNombre(),
                    institucion.getDescripcion(),
                    institucion.getSitioWeb()
            ));
        }

        return resultado;
    }

    // =====================================================
    // CATEGORÍAS
    // =====================================================

    @Override
    public Collection<String> listarCategorias() {

        Collection<String> resultado = new ArrayList<>();

        for (Categoria categoria : manejadorEventos.obtenerCategorias()) {
            resultado.add(categoria.getNombre());
        }

        return resultado;
    }

    @Override
    public void altaCategoria(
            String nombre,
            String nombrePadre
    ) throws Exception {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new Exception(
                    "El nombre de la categoría no puede estar vacío."
            );
        }

        if (manejadorEventos.obtenerCategoria(nombre) != null) {
            throw new Exception(
                    "Ya existe una categoría con el nombre '" +
                            nombre + "'."
            );
        }

        Categoria categoriaPadre = null;

        if (nombrePadre != null && !nombrePadre.trim().isEmpty()) {

            categoriaPadre =
                    manejadorEventos.obtenerCategoria(nombrePadre);

            if (categoriaPadre == null) {
                throw new Exception(
                        "La categoría padre especificada no existe."
                );
            }
        }

        Categoria nuevaCategoria =
                new Categoria(nombre, categoriaPadre);

        if (categoriaPadre != null) {
            categoriaPadre.agregarSubcategoria(nuevaCategoria);
        }

        manejadorEventos.addCategoria(nuevaCategoria);
    }

    @Override
    public Collection<String> listarCategoriasFormateadas() {

        List<String> resultado = new ArrayList<>();

        for (Categoria categoria :
                manejadorEventos.obtenerCategorias()) {

            if (categoria.getPadre() == null) {
                agregarConIndentacion(
                        categoria,
                        "",
                        resultado
                );
            }
        }

        return resultado;
    }

    private void agregarConIndentacion(
            Categoria categoria,
            String prefijo,
            List<String> resultado
    ) {

        resultado.add(prefijo + categoria.getNombre());

        for (Categoria hija : categoria.getSubcategorias()) {
            agregarConIndentacion(
                    hija,
                    prefijo + "   - ",
                    resultado
            );
        }
    }

    // =====================================================
    // EVENTOS
    // =====================================================

    @Override
    public boolean existeEvento(String nombre) {
        return manejadorEventos.existeEvento(nombre);
    }

    @Override
    public boolean altaEvento(DtEvento dt) throws Exception {

        if (manejadorEventos.existeEvento(dt.getNombre())) {
            throw new Exception(
                    "Ya existe un evento con el nombre: " +
                            dt.getNombre()
            );
        }

        if (dt.getCategorias() == null ||
                dt.getCategorias().isEmpty()) {

            throw new Exception(
                    "Debe seleccionar al menos una categoría."
            );
        }

        Evento evento = new Evento(
                dt.getNombre(),
                dt.getSigla(),
                dt.getDescripcion(),
                dt.getFechaAlta()
        );

        for (String nombreCategoria : dt.getCategorias()) {

            Categoria categoria =
                    manejadorEventos.obtenerCategoria(nombreCategoria);

            if (categoria == null) {
                throw new Exception(
                        "La categoría '" +
                                nombreCategoria +
                                "' no existe."
                );
            }

            evento.agregarCategoria(categoria);
        }

        return manejadorEventos.addEvento(evento);
    }

    @Override
    public Collection<DtEvento> listarEventos() {

        Collection<DtEvento> resultado =
                new ArrayList<>();

        for (Evento evento :
                manejadorEventos.obtenerEventos()) {

            Set<String> categorias =
                    new HashSet<>();

            for (Categoria categoria :
                    evento.getCategorias()) {

                categorias.add(categoria.getNombre());
            }

            resultado.add(new DtEvento(
                    evento.getNombre(),
                    evento.getSigla(),
                    evento.getDescripcion(),
                    evento.getFechaAlta(),
                    categorias
            ));
        }

        return resultado;
    }

    @Override
    public DtEvento obtenerInformacionEvento(
            String nombreEvento
    ) {

        Evento evento =
                manejadorEventos.obtenerEvento(nombreEvento);

        if (evento == null) {
            return null;
        }

        Collection<String> categorias =
                new ArrayList<>();

        for (Categoria categoria :
                evento.getCategorias()) {

            categorias.add(categoria.getNombre());
        }

        return new DtEvento(
                evento.getNombre(),
                evento.getSigla(),
                evento.getDescripcion(),
                evento.getFechaAlta(),
                categorias
        );
    }

    // =====================================================
    // EDICIONES
    // =====================================================

    @Override
    public boolean altaEdicion(
            DtEdicion dt,
            String nombreEvento
    ) {

        Evento evento =
                manejadorEventos.obtenerEvento(nombreEvento);

        if (evento == null ||
                manejadorEventos.existeEdicion(
                        dt.getIdNombre())) {

            return false;
        }

        /*
         * Todavía el CU no está enviando el organizador.
         * Por eso temporalmente usamos null.
         */
        Organizador organizador = null;

        Edicion edicion = new Edicion(
                dt.getIdNombre(),
                dt.getSigla(),
                dt.getFechaInicio(),
                dt.getFechaFin(),
                dt.getFechaAlta(),
                dt.getCiudad(),
                dt.getPais(),
                organizador
        );

        manejadorEventos.addEdicion(edicion);
        evento.agregarEdicion(edicion);

        return true;
    }

    @Override
    public Collection<DtEdicion> obtenerEdicionesEvento(
            String nombreEvento
    ) {

        Collection<DtEdicion> resultado =
                new ArrayList<>();

        for (Edicion edicion :
                manejadorEventos.obtenerEdicionesEvento(
                        nombreEvento)) {

            resultado.add(new DtEdicion(
                    edicion.getIdNombre(),
                    edicion.getSigla(),
                    edicion.getFechaInicio(),
                    edicion.getFechaFin(),
                    edicion.getFechaAlta(),
                    edicion.getCiudad(),
                    edicion.getPais()
            ));
        }

        return resultado;
    }

    // =====================================================
    // TIPO REGISTRO
    // =====================================================

    @Override
    public boolean altaTipoRegistro(
            DtTipoRegistro dt,
            String nombreEdicion
    ) {

        Edicion edicion =
                manejadorEventos.obtenerEdicion(nombreEdicion);

        if (edicion == null ||
                manejadorEventos.existeTipoRegistro(
                        dt.getIdNombre())) {

            return false;
        }

        TipoRegistro tipoRegistro = new TipoRegistro(
                dt.getIdNombre(),
                dt.getDescripcion(),
                dt.getCosto(),
                dt.getCupo()
        );

        manejadorEventos.addTipoRegistro(tipoRegistro);
        edicion.agregarTipoRegistro(tipoRegistro);

        return true;
    }

    @Override
    public Collection<DtTipoRegistro>
    obtenerTiposRegistroEdicion(
            String nombreEdicion
    ) {

        Collection<DtTipoRegistro> resultado =
                new ArrayList<>();

        for (TipoRegistro tipo :
                manejadorEventos.obtenerTiposRegistroEdicion(
                        nombreEdicion)) {

            resultado.add(new DtTipoRegistro(
                    tipo.getIdNombre(),
                    tipo.getDescripcion(),
                    tipo.getCosto(),
                    tipo.getCupo()
            ));
        }

        return resultado;
    }

    @Override
    public DtTipoRegistro consultarTipoRegistro(
            String nombreTipoRegistro
    ) {

        TipoRegistro tipoRegistro =
                manejadorEventos.obtenerTipoRegistro(
                        nombreTipoRegistro);

        if (tipoRegistro == null) {
            return null;
        }

        return new DtTipoRegistro(
                tipoRegistro.getIdNombre(),
                tipoRegistro.getDescripcion(),
                tipoRegistro.getCosto(),
                tipoRegistro.getCupo()
        );
    }

    // =====================================================
    // REGISTRO A EDICIÓN
    // =====================================================

    @Override
    public boolean estaRegistradoAEdicion(
            String nicknameAsistente,
            String nombreEdicion
    ) {

        return manejadorUsuarios.estaRegistradoAEdicion(
                nicknameAsistente,
                nombreEdicion
        );
    }

    @Override
    public boolean registroAEdicion(
            String nicknameAsistente,
            String nombreEdicion,
            String nombreTipoRegistro,
            DtRegistro dt
    ) {

        Usuario usuario =
                manejadorUsuarios.obtenerUsuario(
                        nicknameAsistente);

        if (!(usuario instanceof Asistente)) {
            return false;
        }

        Asistente asistente =
                (Asistente) usuario;

        Edicion edicion =
                manejadorEventos.obtenerEdicion(
                        nombreEdicion);

        if (edicion == null) {
            return false;
        }

        TipoRegistro tipoRegistro =
                edicion.obtenerTipoRegistro(
                        nombreTipoRegistro);

        if (tipoRegistro == null) {
            return false;
        }

        Registro registro = new Registro(
                dt.getFechaRegistro(),
                tipoRegistro.getCosto(),
                tipoRegistro,
                edicion
        );

        asistente.agregarRegistro(registro);

        return true;
    }

    // =====================================================
    // PATROCINIOS
    // =====================================================

    @Override
    public boolean altaPatrocinio(DtPatrocinio dt) {

        /*
         * ATENCIÓN:
         * Tu clase Patrocinio ahora pide 6 parámetros,
         * pero el DTO que veníamos usando solamente aporta 5.
         *
         * Hasta saber cuál es exactamente ese sexto parámetro
         * NO conviene inventarlo acá.
         */

        return false;
    }

    @Override
    public Collection<DtPatrocinio> listarPatrocinios() {

        Collection<DtPatrocinio> resultado =
                new ArrayList<>();

        for (Patrocinio patrocinio :
                manejadorPatrocinios.listarPatrocinios()) {

            resultado.add(new DtPatrocinio(
                    patrocinio.getFecha(),
                    patrocinio.getMontoAporte(),
                    patrocinio.getCantRegistrosGrat(),
                    patrocinio.getCodigoPatrocinio(),
                    patrocinio.getNivel()
            ));
        }

        return resultado;
    }

    @Override
    public DtPatrocinio consultarPatrocinio(
            String codigo
    ) {

        Patrocinio patrocinio =
                manejadorPatrocinios.obtenerPatrocinio(codigo);

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