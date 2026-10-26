<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Usuarios - eventos.uy</title>
    <link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="${raiz}/css/styles.css">
</head>
<body class="bg-light">

<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp" />

<main class="container py-5">
    <h1 class="h3 mb-4">Usuarios</h1>

    <div class="row g-4">

        <div class="col-md-6">
            <h2 class="h5 mb-3">Organizadores</h2>
            <div class="list-group shadow-sm">
                <c:forEach var="usuario" items="${organizadores}">
                    <c:url var="urlPerfil" value="/usuario">
                        <c:param name="nick" value="${usuario.nickname}" />
                    </c:url>
                    <a href="${urlPerfil}" class="list-group-item list-group-item-action">
                        <strong><c:out value="${usuario.nombre}" /></strong>
                        <span class="text-muted">· <c:out value="${usuario.nickname}" /></span>
                    </a>
                </c:forEach>
                <c:if test="${empty organizadores}">
                    <div class="list-group-item text-muted">No hay organizadores.</div>
                </c:if>
            </div>
        </div>

        <div class="col-md-6">
            <h2 class="h5 mb-3">Asistentes</h2>
            <div class="list-group shadow-sm">
                <c:forEach var="usuario" items="${asistentes}">
                    <c:url var="urlPerfil" value="/usuario">
                        <c:param name="nick" value="${usuario.nickname}" />
                    </c:url>
                    <a href="${urlPerfil}" class="list-group-item list-group-item-action">
                        <strong><c:out value="${usuario.nombre}" /> <c:out value="${usuario.apellido}" /></strong>
                        <span class="text-muted">· <c:out value="${usuario.nickname}" /></span>
                    </a>
                </c:forEach>
                <c:if test="${empty asistentes}">
                    <div class="list-group-item text-muted">No hay asistentes.</div>
                </c:if>
            </div>
        </div>

    </div>
</main>

<script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>