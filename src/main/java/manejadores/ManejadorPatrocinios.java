package manejadores;

import clases.Patrocinio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ManejadorPatrocinios {

    private static ManejadorPatrocinios instancia = null;

    private final Map<String, Patrocinio> patrociniosCodigo;

    private ManejadorPatrocinios() {patrociniosCodigo = new HashMap<>();}

    public static ManejadorPatrocinios getInstance() {
        if (instancia == null) {
            instancia = new ManejadorPatrocinios();
        }
        return instancia;
    }

    public boolean addPatrocinio(Patrocinio patrocinio) {

        String codigo = patrocinio.getCodigoPatrocinio();

        if (existePatrocinio(codigo)) {
            return false;
        }

        patrociniosCodigo.put(codigo, patrocinio);
        return true;
    }

    public boolean existePatrocinio(String codigo) {
        return patrociniosCodigo.containsKey(codigo);
    }

    public Patrocinio obtenerPatrocinio(String codigo) {
        return patrociniosCodigo.get(codigo);
    }

    public Collection<Patrocinio> listarPatrocinios() {
        return patrociniosCodigo.values();
    }
}