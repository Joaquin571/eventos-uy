package manejadores;

import clases.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ManejadorEventos {
    private static ManejadorEventos instancia = null;
    private final Map<String, Evento> eventosNombre;
    private final Map<String, Categoria> categoriasNombre;
    private final Map<String, Edicion> edicionesNombre = new HashMap<>();
    private final Map<String, TipoRegistro> tiposRegistroNombre = new HashMap<>();

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

    // EDICIONES
    public boolean addEdicion(Edicion edicion){
        if(existeEdicion(edicion.getIdNombre())){
            return false;
        }
        edicionesNombre.put(edicion.getIdNombre(), edicion);
        return true;
    }
    public Edicion obtenerEdicion(String nombre){
        return edicionesNombre.get(nombre);
    }
    public boolean existeEdicion(String nombre){
        return edicionesNombre.containsKey(nombre);
    }
    public Collection<Edicion> obtenerEdiciones(){
        return edicionesNombre.values();
    }
    public Collection<Edicion> obtenerEdicionesEvento(String nombreEvento){
        Evento evento = obtenerEvento(nombreEvento);
        if(evento != null ){
            return evento.getEdiciones();
        }
        return new ArrayList<>();
    }


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


    // TIPOS DE REGISTRO
    public boolean addTipoRegistro(TipoRegistro tipoRegistro){
        if(existeTipoRegistro(tipoRegistro.getIdNombre())){
            return false;
        }
        tiposRegistroNombre.put(tipoRegistro.getIdNombre(), tipoRegistro);
        return true;
    }

    public TipoRegistro obtenerTipoRegistro(String nombre){
        return tiposRegistroNombre.get(nombre);
    }
    public boolean existeTipoRegistro(String nombre){
        return tiposRegistroNombre.containsKey(nombre);
    }
    public Collection<TipoRegistro> obtenerTiposRegistro(){
        return tiposRegistroNombre.values();
    }
    public Collection<TipoRegistro> obtenerTiposRegistroEdicion(String nombreEdicion){
        Edicion edicion = obtenerEdicion(nombreEdicion);
        if (edicion != null) {
            return edicion.getTiposRegistro();
        }
        return new java.util.ArrayList<>();
    }


}

