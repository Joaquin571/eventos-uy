package web.remoto;

import java.lang.reflect.*;
import java.time.LocalDate;
import java.util.*;

/** Traducción de transporte. No valida permisos ni toma decisiones de dominio. */
final class ConversorUsuarios {
    private ConversorUsuarios() { }
    static Object convertir(Object origen, Class<?> destino) {
        if (origen == null) return null;
        if (destino == String.class && origen instanceof LocalDate) return origen.toString();
        if (destino == LocalDate.class && origen instanceof String texto) return LocalDate.parse(texto);
        if (destino.isEnum()) {
            for (Object valor : destino.getEnumConstants()) {
                if (((Enum<?>) valor).name().equals(((Enum<?>) origen).name())) return valor;
            }
            throw new IllegalStateException("Enumerado SOAP no compatible.");
        }
        if (destino.isPrimitive() || destino == String.class || destino == byte[].class) return origen;
        try {
            // xsd:extension conserva los subtipos concretos del usuario.
            if (destino.getSimpleName().equals("DtUsuario")) {
                destino = Class.forName(destino.getPackageName() + "." + origen.getClass().getSimpleName());
            }
            Object resultado = destino.getConstructor().newInstance();
            for (Method getter : origen.getClass().getMethods()) {
                String nombre = getter.getName();
                if (getter.getParameterCount() != 0 || nombre.equals("getClass")) continue;
                String sufijo = nombre.startsWith("get") ? nombre.substring(3)
                        : nombre.startsWith("is") ? nombre.substring(2) : null;
                if (sufijo == null || sufijo.isEmpty()) continue;
                Object valor = getter.invoke(origen);
                if (valor == null) continue;
                if (valor instanceof Collection<?> coleccion) {
                    Method getterDestino = destino.getMethod("get" + sufijo);
                    Type tipo = ((ParameterizedType) getterDestino.getGenericReturnType()).getActualTypeArguments()[0];
                    Class<?> elemento = (Class<?>) tipo;
                    Collection<Object> lista = new ArrayList<>();
                    for (Object item : coleccion) lista.add(convertir(item, elemento));
                    try { destino.getMethod("set" + sufijo, getterDestino.getReturnType()).invoke(resultado, lista); }
                    catch (NoSuchMethodException e) {
                        @SuppressWarnings("unchecked")
                        Collection<Object> actual = (Collection<Object>) getterDestino.invoke(resultado);
                        actual.addAll(lista); // Los clientes JAXB tienen listas vivas, sin setter.
                    }
                } else {
                    Method setter = Arrays.stream(destino.getMethods())
                            .filter(m -> m.getName().equals("set" + sufijo) && m.getParameterCount() == 1)
                            .findFirst().orElseThrow(() -> new NoSuchMethodException("set" + sufijo));
                    setter.invoke(resultado, convertir(valor, setter.getParameterTypes()[0]));
                }
            }
            return resultado;
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new IllegalStateException("El contrato SOAP de usuarios no coincide con los DTO.", e);
        }
    }
}