package implementacion;

import clases.*;
import datatypes.*;
import interfaces.ISistema;
import manejadores.ManejadorEventos;
import manejadores.ManejadorInstituciones;
import manejadores.ManejadorPatrocinios;
import manejadores.ManejadorRegistros;
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
    private final ManejadorRegistros manejadorRegistros;

    public Sistema() {
        manejadorUsuarios = ManejadorUsuarios.getInstance();
        manejadorInstituciones = ManejadorInstituciones.getInstance();
        manejadorEventos = ManejadorEventos.getInstance();
        manejadorPatrocinios = ManejadorPatrocinios.getInstance();
        manejadorRegistros = ManejadorRegistros.getInstance();

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

        if (dt.getNombreInstitucion() != null
                && !dt.getNombreInstitucion().isBlank()) {

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
    @Override
    public Collection<DtRegistro> obtenerRegistrosAsistente(String nickname) {

        Collection<DtRegistro> resultado = new ArrayList<>();

        for (Registro registro :
                manejadorRegistros.obtenerRegistrosAsistente(nickname)) {

            String nombreTipoRegistro = null;
            String nombreEdicion = null;

            if (registro.getTipoRegistro() != null) {
                nombreTipoRegistro =
                        registro.getTipoRegistro().getIdNombre();
            }

            if (registro.getEdicion() != null) {
                nombreEdicion =
                        registro.getEdicion().getIdNombre();
            }

            resultado.add(
                    new DtRegistro(
                            registro.getFechaRegistro(),
                            registro.getCosto(),
                            nombreTipoRegistro,
                            nombreEdicion
                    )
            );
        }

        return resultado;
    }
    @Override
    public Collection<DtEdicion> obtenerEdicionesOrganizador(String nickname) {

        Collection<DtEdicion> resultado = new ArrayList<>();

        for (Edicion edicion :
                manejadorEventos.obtenerEdicionesOrganizador(nickname)) {

            String nombreOrganizador = null;

            if (edicion.getOrganizador() != null) {
                nombreOrganizador =
                        edicion.getOrganizador().getNickname();
            }

            resultado.add(
                    new DtEdicion(
                            edicion.getIdNombre(),
                            edicion.getSigla(),
                            edicion.getFechaInicio(),
                            edicion.getFechaFin(),
                            edicion.getFechaAlta(),
                            edicion.getCiudad(),
                            edicion.getPais(),
                            nombreOrganizador
                    )
            );
        }

        return resultado;
    }

    // =====================================================
    // MODIFICAR USUARIO
    // =====================================================

    @Override
    public void modificarAsistente(DtAsistente dt) {

        Institucion institucion = null;

        if (dt.getNombreInstitucion() != null
                && !dt.getNombreInstitucion().isBlank()) {

            institucion = manejadorInstituciones.obtenerInstitucion(
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

        if (dt.getCategorias() == null
                || dt.getCategorias().isEmpty()) {

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

        Collection<DtEvento> resultado = new ArrayList<>();

        for (Evento evento :
                manejadorEventos.obtenerEventos()) {

            Set<String> categorias = new HashSet<>();

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
                manejadorEventos.existeEdicion(dt.getIdNombre())) {

            return false;
        }

        Organizador organizador =
                manejadorUsuarios.obtenerOrganizador(
                        dt.getNombreOrganizador()
                );

        if (organizador == null) {
            return false;
        }

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

        edicion.setEvento(evento);

        manejadorEventos.addEdicion(edicion);
        return true;
    }



    @Override
    public Collection<DtEdicion> obtenerEdicionesEvento(
            String nombreEvento
    ) {

        Collection<DtEdicion> resultado = new ArrayList<>();

        for (Edicion edicion :
                manejadorEventos.obtenerEdicionesEvento(nombreEvento)) {

            String nombreOrganizador = null;

            if (edicion.getOrganizador() != null) {
                nombreOrganizador =
                        edicion.getOrganizador().getNickname();
            }

            resultado.add(new DtEdicion(
                    edicion.getIdNombre(),
                    edicion.getSigla(),
                    edicion.getFechaInicio(),
                    edicion.getFechaFin(),
                    edicion.getFechaAlta(),
                    edicion.getCiudad(),
                    edicion.getPais(),
                    nombreOrganizador
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
                manejadorEventos.obtenerEdicion(
                        nombreEdicion
                );

        if (edicion == null) {
            return false;
        }

        // El nombre solo debe ser único DENTRO de esta edición.
        if (manejadorEventos.existeTipoRegistro(
                nombreEdicion,
                dt.getIdNombre()
        )) {
            return false;
        }

        TipoRegistro tipoRegistro =
                new TipoRegistro(
                        dt.getIdNombre(),
                        dt.getDescripcion(),
                        dt.getCosto(),
                        dt.getCupo()
                );

        tipoRegistro.setEdicion(edicion);

        return manejadorEventos.addTipoRegistro(
                tipoRegistro
        );
    }

    @Override
    public Collection<DtTipoRegistro> obtenerTiposRegistroEdicion(
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
            String nombreEdicion,
            String nombreTipoRegistro
    ) {

        TipoRegistro tipoRegistro =
                manejadorEventos.obtenerTipoRegistro(
                        nombreEdicion,
                        nombreTipoRegistro
                );

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
        return manejadorRegistros.existeRegistroAsistenteEdicion(
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

        // =====================================================
        // VALIDACIONES BÁSICAS
        // =====================================================

        if (nicknameAsistente == null
                || nicknameAsistente.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un asistente."
            );
        }

        if (nombreEdicion == null
                || nombreEdicion.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una edición."
            );
        }

        if (nombreTipoRegistro == null
                || nombreTipoRegistro.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un tipo de registro."
            );
        }

        if (dt == null
                || dt.getFechaRegistro() == null) {

            throw new IllegalArgumentException(
                    "Los datos del registro no son válidos."
            );
        }

        // =====================================================
        // ASISTENTE
        // =====================================================

        Usuario usuario =
                manejadorUsuarios.obtenerUsuario(
                        nicknameAsistente
                );

        if (!(usuario instanceof Asistente asistente)) {

            throw new IllegalArgumentException(
                    "El usuario seleccionado no es un asistente válido."
            );
        }

        // =====================================================
        // EDICIÓN
        // =====================================================

        Edicion edicion =
                manejadorEventos.obtenerEdicion(
                        nombreEdicion
                );

        if (edicion == null) {

            throw new IllegalArgumentException(
                    "La edición seleccionada no existe."
            );
        }

        // =====================================================
        // TIPO DE REGISTRO
        // Se identifica por EDICIÓN + NOMBRE
        // =====================================================

        TipoRegistro tipoRegistro =
                manejadorEventos.obtenerTipoRegistro(
                        nombreEdicion,
                        nombreTipoRegistro
                );

        if (tipoRegistro == null) {

            throw new IllegalArgumentException(
                    "El tipo de registro seleccionado no existe para esta edición."
            );
        }

        // =====================================================
        // EVITAR DOBLE REGISTRO A LA MISMA EDICIÓN
        // =====================================================

        if (manejadorRegistros
                .existeRegistroAsistenteEdicion(
                        nicknameAsistente,
                        nombreEdicion
                )) {

            throw new IllegalArgumentException(
                    "El asistente '"
                            + nicknameAsistente
                            + "' ya está registrado a la edición '"
                            + nombreEdicion
                            + "'."
            );
        }

        // =====================================================
        // VALIDAR CUPO
        // Ahora se cuenta por EDICIÓN + TIPO
        // =====================================================

        long cantidadActual =
                manejadorRegistros.contarRegistrosTipo(
                        nombreEdicion,
                        nombreTipoRegistro
                );

        if (cantidadActual >= tipoRegistro.getCupo()) {

            throw new IllegalArgumentException(
                    "Se alcanzó el cupo máximo del tipo de registro '"
                            + nombreTipoRegistro
                            + "'."
            );
        }

        // =====================================================
        // CREAR REGISTRO
        // =====================================================

        Registro registro =
                new Registro(
                        dt.getFechaRegistro(),
                        tipoRegistro.getCosto(),
                        tipoRegistro,
                        edicion
                );

        registro.setAsistente(asistente);

        boolean agregado =
                manejadorRegistros.addRegistro(
                        registro
                );

        if (!agregado) {

            throw new IllegalArgumentException(
                    "No fue posible completar el registro."
            );
        }

        return true;
    }
    // =====================================================
    // CONSULTA DE REGISTRO
    // =====================================================

    @Override
    public DtRegistro obtenerDetalleRegistro(String nicknameAsistente, String nombreEdicion) {
        if (nicknameAsistente == null || nicknameAsistente.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un asistente.");
        }

        if (nombreEdicion == null || nombreEdicion.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una edición.");
        }

        Registro r = manejadorRegistros.obtenerRegistro(nicknameAsistente, nombreEdicion);
        if (r == null) {
            return null;
        }

        String edicion = r.getEdicion() != null ? r.getEdicion().getIdNombre() : "";
        String tipo = r.getTipoRegistro() != null ? r.getTipoRegistro().getIdNombre() : "";

        return new DtRegistro(r.getFechaRegistro(), r.getCosto(), tipo, edicion);
    }


    // =====================================================
    // PATROCINIOS
    // =====================================================

    @Override
    public boolean altaPatrocinio(DtPatrocinio dt) {

        // =====================================================
        // VALIDACIONES BÁSICAS
        // =====================================================

        if (dt == null) {
            throw new IllegalArgumentException(
                    "Los datos del patrocinio no pueden ser nulos."
            );
        }

        if (dt.getCodigoPatrocinio() == null
                || dt.getCodigoPatrocinio().isBlank()) {

            throw new IllegalArgumentException(
                    "El código del patrocinio es obligatorio."
            );
        }

        if (dt.getNombreInstituto() == null
                || dt.getNombreInstituto().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una institución."
            );
        }

        if (dt.getNombreEdicion() == null
                || dt.getNombreEdicion().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una edición."
            );
        }

        if (dt.getNombreTipoRegistro() == null
                || dt.getNombreTipoRegistro().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un tipo de registro."
            );
        }

        if (dt.getNivel() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un nivel de patrocinio."
            );
        }

        if (dt.getMontoAporte() <= 0) {

            throw new IllegalArgumentException(
                    "El aporte económico debe ser mayor que 0."
            );
        }

        if (dt.getCantRegistrosGrat() < 0) {

            throw new IllegalArgumentException(
                    "La cantidad de registros gratuitos no puede ser negativa."
            );
        }

        // =====================================================
        // CÓDIGO DE PATROCINIO ÚNICO
        // =====================================================

        if (manejadorPatrocinios.existePatrocinio(
                dt.getCodigoPatrocinio()
        )) {

            throw new IllegalArgumentException(
                    "Ya existe un patrocinio con el código '"
                            + dt.getCodigoPatrocinio()
                            + "'."
            );
        }

        // =====================================================
        // RECUPERAR INSTITUCIÓN
        // =====================================================

        Institucion institucion =
                manejadorInstituciones.obtenerInstitucion(
                        dt.getNombreInstituto()
                );

        if (institucion == null) {

            throw new IllegalArgumentException(
                    "La institución seleccionada no existe."
            );
        }

        // =====================================================
        // RECUPERAR EDICIÓN
        // =====================================================

        Edicion edicion =
                manejadorEventos.obtenerEdicion(
                        dt.getNombreEdicion()
                );

        if (edicion == null) {

            throw new IllegalArgumentException(
                    "La edición seleccionada no existe."
            );
        }

        // =====================================================
        // RECUPERAR TIPO DE REGISTRO
        // =====================================================

        TipoRegistro tipoRegistro = manejadorEventos.obtenerTipoRegistro(dt.getNombreEdicion(), dt.getNombreTipoRegistro());
        if (tipoRegistro == null) {
            throw new IllegalArgumentException(
                    "El tipo de registro seleccionado no existe."
            );
        }
        // =====================================================
        // RESTRICCIÓN DE LA LETRA:
        // UNA INSTITUCIÓN NO PUEDE TENER DOS PATROCINIOS
        // EN LA MISMA EDICIÓN
        // =====================================================

        if (manejadorPatrocinios
                .existePatrocinioInstitucionEdicion(
                        institucion.getNombre(),
                        edicion.getIdNombre()
                )) {

            throw new IllegalArgumentException(
                    "La institución '"
                            + institucion.getNombre()
                            + "' ya posee un patrocinio para la edición '"
                            + edicion.getIdNombre()
                            + "'."
            );
        }

        // =====================================================
        // RESTRICCIÓN DEL 20 %
        // =====================================================

        float costoRegistros =
                tipoRegistro.getCosto()
                        * dt.getCantRegistrosGrat();

        float maximoPermitido =
                dt.getMontoAporte() * 0.20f;

        if (costoRegistros > maximoPermitido) {

            throw new IllegalArgumentException(
                    "El costo de los registros gratuitos ($"
                            + costoRegistros
                            + ") supera el 20% del aporte económico ($"
                            + maximoPermitido
                            + ")."
            );
        }

        // =====================================================
        // CREAR PATROCINIO
        // =====================================================

        Patrocinio patrocinio =
                new Patrocinio(
                        dt.getFecha(),
                        dt.getMontoAporte(),
                        dt.getCantRegistrosGrat(),
                        dt.getCodigoPatrocinio(),
                        dt.getNivel(),
                        institucion
                );

        patrocinio.setEdicion(edicion);
        patrocinio.setTipoRegistro(tipoRegistro);

        boolean agregado =
                manejadorPatrocinios.addPatrocinio(
                        patrocinio
                );

        if (!agregado) {

            throw new IllegalArgumentException(
                    "No fue posible registrar el patrocinio."
            );
        }

        return true;
    }

    @Override
    public Collection<DtPatrocinio> listarPatrocinios() {

        Collection<DtPatrocinio> resultado =
                new ArrayList<>();

        for (Patrocinio patrocinio :
                manejadorPatrocinios.listarPatrocinios()) {

            String nombreInstitucion = null;

            if (patrocinio.getInstitucion() != null) {
                nombreInstitucion =
                        patrocinio.getInstitucion().getNombre();
            }

            resultado.add(new DtPatrocinio(
                    patrocinio.getFecha(),
                    patrocinio.getMontoAporte(),
                    patrocinio.getCantRegistrosGrat(),
                    patrocinio.getCodigoPatrocinio(),
                    patrocinio.getNivel(),
                    nombreInstitucion,
                    patrocinio.getEdicion() != null
                            ? patrocinio.getEdicion().getIdNombre()
                            : null,
                    patrocinio.getTipoRegistro() != null
                            ? patrocinio.getTipoRegistro().getIdNombre()
                            : null
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

        String nombreInstitucion = null;

        if (patrocinio.getInstitucion() != null) {
            nombreInstitucion =
                    patrocinio.getInstitucion().getNombre();
        }

        return new DtPatrocinio(
                patrocinio.getFecha(),
                patrocinio.getMontoAporte(),
                patrocinio.getCantRegistrosGrat(),
                patrocinio.getCodigoPatrocinio(),
                patrocinio.getNivel(),
                nombreInstitucion,
                patrocinio.getEdicion() != null
                        ? patrocinio.getEdicion().getIdNombre()
                        : null,
                patrocinio.getTipoRegistro() != null
                        ? patrocinio.getTipoRegistro().getIdNombre()
                        : null
        );
    }
    @Override
    public Collection<DtPatrocinio> obtenerPatrociniosEdicion(
            String nombreEdicion
    ) {

        Collection<DtPatrocinio> resultado =
                new ArrayList<>();

        for (Patrocinio patrocinio :
                manejadorPatrocinios
                        .obtenerPatrociniosEdicion(
                                nombreEdicion
                        )) {

            String nombreInstitucion = null;

            if (patrocinio.getInstitucion() != null) {

                nombreInstitucion =
                        patrocinio
                                .getInstitucion()
                                .getNombre();
            }

            resultado.add(
                    new DtPatrocinio(
                            patrocinio.getFecha(),
                            patrocinio.getMontoAporte(),
                            patrocinio.getCantRegistrosGrat(),
                            patrocinio.getCodigoPatrocinio(),
                            patrocinio.getNivel(),
                            nombreInstitucion,
                            patrocinio.getEdicion() != null
                                    ? patrocinio
                                    .getEdicion()
                                    .getIdNombre()
                                    : null,
                            patrocinio.getTipoRegistro() != null
                                    ? patrocinio
                                    .getTipoRegistro()
                                    .getIdNombre()
                                    : null
                    )
            );
        }

        return resultado;
    }
}