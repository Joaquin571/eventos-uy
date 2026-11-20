package contratos;

import java.lang.reflect.*;
import java.time.LocalDate;
import java.util.*;

/** Conversión estructural entre DTO locales y JAXB; no toma decisiones de dominio. */
public final class ConversorDatos {
    private ConversorDatos() { }
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T convertir(Object origen, Class<T> destino) {
        if (origen == null) return null;
        if (destino == String.class && origen instanceof LocalDate) return (T) origen.toString();
        if (destino == LocalDate.class && origen instanceof String texto) return (T) LocalDate.parse(texto);
        if (destino.isPrimitive() || destino == String.class || Number.class.isAssignableFrom(destino)
                || destino == Boolean.class || destino == LocalDate.class) return (T) origen;
        if (destino == byte[].class) return (T) ((byte[])origen).clone();
        if (destino.isEnum()) return (T) Enum.valueOf((Class)destino, ((Enum<?>)origen).name());
        try {
            if (destino.getSimpleName().equals("DtUsuario"))
                destino=(Class<T>)Class.forName(destino.getPackageName()+"."+origen.getClass().getSimpleName());
            T resultado=destino.getConstructor().newInstance();
            for (Class<?> tipo=destino;tipo!=Object.class;tipo=tipo.getSuperclass()) {
                for (Field campo:tipo.getDeclaredFields()) {
                    if (Modifier.isStatic(campo.getModifiers()) || campo.isSynthetic()) continue;
                    String nombre=campo.getName();
                    // Credenciales solo viajan en EntradaUsuario; nunca en una salida/perfil/sesión.
                    if (tipo.getSimpleName().equals("DtUsuario")
                            && (nombre.equals("contrasenia") || nombre.equals("confirmacionContrasenia"))) continue;
                    String mayuscula=Character.toUpperCase(nombre.charAt(0))+nombre.substring(1);
                    Method getter=null;
                    for(String candidato:List.of("get"+mayuscula,"is"+mayuscula,nombre)) {
                        try { getter=origen.getClass().getMethod(candidato);break; }
                        catch(NoSuchMethodException ignorada) { }
                    }
                    if(getter==null) throw new NoSuchMethodException(nombre);
                    Object valor=getter.invoke(origen);
                    if(valor==null) continue;
                    campo.setAccessible(true);
                    if(valor instanceof Collection<?> coleccion) {
                        Class<?> elemento=(Class<?>)((ParameterizedType)campo.getGenericType()).getActualTypeArguments()[0];
                        List<Object> lista=new ArrayList<>();
                        for(Object item:coleccion) lista.add(convertir(item,elemento));
                        campo.set(resultado,lista);
                    } else campo.set(resultado,convertir(valor,campo.getType()));
                }
            }
            return resultado;
        } catch(ReflectiveOperationException | ClassCastException e) {
            throw new IllegalStateException("Los DTO de transporte no coinciden con el contrato.",e);
        }
    }
}
