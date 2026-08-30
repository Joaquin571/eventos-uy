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
import java.util.List;

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

    // =====================================================
    // CONSULTA DE EVENTO
    // =====================================================

    @Override
    public Collection<String> listarEventos() {
        // Obtenemos la instancia del Singleton con getInstance()
        ManejadorEventos me = ManejadorEventos.getInstance();

        // obtenerEventos() te devuelve Collection<Evento>
        Collection<Evento> eventos = me.obtenerEventos();
        Collection<String> nombres = new ArrayList<>();

        for (Evento e : eventos) {
            nombres.add(e.getNombre());
        }
        return nombres;
    }

    @Override
    public DtEvento obtenerInformacionEvento(String nombreEvento) {
        ManejadorEventos me = ManejadorEventos.getInstance();
        Evento e = me.obtenerEvento(nombreEvento);

        if (e != null) {
            // Usamos Collection y ArrayList igual que en listarUsuarios
            Collection<String> nombresCategorias = new ArrayList<>();
            for (Categoria cat : e.getCategorias()) {
                nombresCategorias.add(cat.getNombre());
            }

            return new DtEvento(
                    e.getNombre(),
                    e.getSigla(),
                    e.getDescripcion(),
                    e.getFechaAlta(),
                    nombresCategorias
            );
        }
        return null;
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

    // =====================================================
    // ALTA CATEGORIA
    // =====================================================
    @Override
    public void altaCategoria(String nombre, String nombrePadre) throws Exception {
        ManejadorEventos me = ManejadorEventos.getInstance();

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new Exception("El nombre de la categoría no puede estar vacío.");
        }

        if (me.obtenerCategoria(nombre) != null) {
            throw new Exception("Ya existe una categoría con el nombre '" + nombre + "'.");
        }

        Categoria catPadre = null;
        if (nombrePadre != null && !nombrePadre.trim().isEmpty()) {
            catPadre = me.obtenerCategoria(nombrePadre);
            if (catPadre == null) {
                throw new Exception("La categoría padre especificada no existe.");
            }
        }

        Categoria nuevaCategoria = new Categoria(nombre, catPadre);

        if (catPadre != null) {
            catPadre.agregarSubcategoria(nuevaCategoria);
        }

        me.addCategoria(nuevaCategoria);
    }

    @Override
    public Collection<String> listarCategoriasFormateadas() {
        ManejadorEventos me = ManejadorEventos.getInstance();
        List<String> resultado = new ArrayList<>();

        Collection<Categoria> categorias = me.obtenerCategorias();
        if (categorias != null) {
            for (Categoria c : categorias) {
                // Empezamos solo por las raíces (las que no tienen padre)
                if (c.getPadre() == null) {
                    agregarConIndentacion(c, "", resultado);
                }
            }
        }
        return resultado;
    }

    private void agregarConIndentacion(Categoria cat, String prefijo, List<String> resultado) {
        // Agrega el nombre con espacio/prefijo para dar efecto visual de árbol
        resultado.add(prefijo + cat.getNombre());

        for (Categoria hija : cat.getSubcategorias()) {
            agregarConIndentacion(hija, prefijo + "   - ", resultado);
        }
    }
}

