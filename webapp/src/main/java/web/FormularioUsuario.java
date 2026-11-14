package web;

import datatypes.*;
import web.remoto.ClienteUsuarios;
import web.remoto.ErrorRemotoUsuarios;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Interpreta el formulario HTTP; las reglas se comprueban en el servidor central. */
final class FormularioUsuario {
    private FormularioUsuario() { }
    static Map<String, String> leer(HttpServletRequest req) {
        Map<String, String> form = new HashMap<>();
        for (String campo : new String[]{"tipo", "nickname", "nombre", "correo", "apellido", "fechaNac", "institucion", "descripcion", "sitioWeb"}) {
            form.put(campo, Objects.toString(req.getParameter(campo), "").trim());
        }
        return form;
    }
    static DtUsuario datos(HttpServletRequest req, Map<String, String> form, DtUsuario actual) {
        String tipo = actual == null ? form.get("tipo") : actual instanceof DtAsistente ? "asistente" : "organizador";
        String nick = actual == null ? form.get("nickname") : actual.getNickname();
        String correo = actual == null ? form.get("correo") : actual.getCorreoElectronico();
        DtUsuario dt;
        if ("asistente".equals(tipo)) {
            LocalDate fecha;
            try { fecha = form.get("fechaNac").isBlank() ? null : LocalDate.parse(form.get("fechaNac")); }
            catch (java.time.format.DateTimeParseException e) { throw new IllegalArgumentException("La fecha de nacimiento no es válida."); }
            dt = new DtAsistente(nick, form.get("nombre"), correo, form.get("apellido"), fecha,
                    form.get("institucion").isBlank() ? null : form.get("institucion"));
        } else if ("organizador".equals(tipo)) {
            dt = new DtOrganizador(nick, form.get("nombre"), correo, form.get("descripcion"), form.get("sitioWeb"));
        } else throw new IllegalArgumentException("Seleccioná un tipo de usuario válido.");
        dt.setContrasenia(req.getParameter("contrasenia"));
        dt.setConfirmacionContrasenia(req.getParameter("confirmacion"));
        return dt;
    }
    static void verificarError(IllegalArgumentException error) {
        if (error instanceof ErrorRemotoUsuarios remoto && !"DATOS".equals(remoto.getCodigo())) throw remoto;
    }
    static String parametro(HttpServletRequest req, String nombre) { return Objects.toString(req.getParameter(nombre), "").trim(); }
}