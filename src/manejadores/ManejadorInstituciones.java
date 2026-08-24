package manejadores;

import clases.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ManejadorInstituciones {
    private static ManejadorInstituciones instancia = null;

    private final Map<String, Institucion> institucionesIdNombre;
    private ManejadorInstituciones() {institucionesIdNombre = new HashMap<>();}

    public static ManejadorInstituciones getInstance() {
        if (instancia == null) {
            instancia = new ManejadorInstituciones();
        }
        return instancia;
    }
    public boolean addInstitucion(Institucion institucion) {
        String nombre = institucion.getIdNombre();

        if (existeInstitucion(nombre)) {
            return false;
        }

        institucionesIdNombre.put(nombre, institucion);
        return true;
    }

    public Institucion obtenerInstitucion(String nombre) {
        return institucionesIdNombre.get(nombre);
    }

    public boolean existeInstitucion(String nombre) {
        return institucionesIdNombre.containsKey(nombre);
    }

    public Collection<Institucion> obtenerInstituciones() {
        return institucionesIdNombre.values();
    }


}
