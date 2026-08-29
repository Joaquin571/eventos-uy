package manejadores;

import clases.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ManejadorEventos {
    private static ManejadorEventos instancia = null;
    private final Map<String, Evento> eventosNombre;
    private final Map<String, Categoria> categoriasNombre;

    private ManejadorEventos() {
        eventosNombre = new HashMap<>();
        categoriasNombre = new HashMap<>();
    }

    public static ManejadorEventos getInstance() {
        if (instancia == null) {
            instancia = new ManejadorEventos();
        }
        return instancia;
    }

    //SECCION EVENTOS
    public boolean addEvento(Evento evento) {

        String nombre = evento.getNombre();

        if (existeEvento(nombre)) {
            return false;
        }

        eventosNombre.put(nombre, evento);
        return true;
    }

    public Evento obtenerEvento(String nombre) {
        return eventosNombre.get(nombre);
    }

    public boolean existeEvento(String nombre) {
        return eventosNombre.containsKey(nombre);
    }

    public Collection<Evento> obtenerEventos() {
        return eventosNombre.values();
    }
    /*
    // EDICIONES
    public boolean addEdicion(Edicion edicion);
    public Edicion obtenerEdicion(String nombre);
    public boolean existeEdicion(String nombre);
    public Collection<Edicion> obtenerEdiciones();
    public Collection<Edicion> obtenerEdicionesEvento(String nombreEvento);

  */
    // CATEGORÍAS
    public boolean addCategoria(Categoria categoria) {
        if (existeCategoria(categoria.getNombre())) {
            return false;
        }
        categoriasNombre.put(categoria.getNombre(), categoria);
        return true;
    }
    public Categoria obtenerCategoria(String nombre) {
        return categoriasNombre.get(nombre);
    }
    public boolean existeCategoria(String nombre) {
        return categoriasNombre.containsKey(nombre);
    }
    public Collection<Categoria> obtenerCategorias() {
        return categoriasNombre.values();
    }

/*
    // TIPOS DE REGISTRO
    boolean addTipoRegistro(TipoRegistro tipoRegistro);
    TipoRegistro obtenerTipoRegistro(String nombre);
    boolean existeTipoRegistro(String nombre);
    Collection<TipoRegistro> obtenerTiposRegistro();
    Collection<TipoRegistro> obtenerTiposRegistroEdicion(String nombreEdicion);

    */
}

