<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Iniciar sesión | eventos.uy</title>
    <link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="${raiz}/css/movil.css">
</head>
<body class="movil movil-login">
<main class="movil-contenedor movil-centrado">
    <div class="text-center mb-4">
        <div class="movil-logo" aria-hidden="true">e</div>
        <h1 class="h3 fw-bold mb-1">eventos<span class="marca-uy-oscura">.uy</span></h1>
        <p class="text-secondary mb-0">Acceso para asistentes</p>
    </div>

    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}" /></div></c:if>
    <c:if test="${not empty aviso}"><div class="alert alert-warning" role="alert"><c:out value="${aviso}" /></div></c:if>

    <form method="post" action="${raiz}/movil/login" class="movil-form">
        <jsp:include page="/WEB-INF/views/fragmentos/csrf.jsp"/>
        <div class="mb-3">
            <label for="identificador" class="form-label fw-semibold">Nickname o correo</label>
            <input type="text" id="identificador" name="identificador" class="form-control form-control-lg" required
                   autocomplete="username" autocapitalize="none" autocorrect="off" spellcheck="false"
                   value="<c:out value='${identificador}'/>">
        </div>
        <div class="mb-4">
            <label for="contrasenia" class="form-label fw-semibold">Contraseña</label>
            <input type="password" id="contrasenia" name="contrasenia" class="form-control form-control-lg" required
                   autocomplete="current-password">
        </div>
        <div class="d-grid">
            <button type="submit" class="btn btn-primary btn-lg fw-bold">Iniciar sesión</button>
        </div>
    </form>
    <p class="text-center text-secondary small mt-4">Solo los asistentes pueden iniciar sesión desde el celular.</p>
</main>
<script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>
