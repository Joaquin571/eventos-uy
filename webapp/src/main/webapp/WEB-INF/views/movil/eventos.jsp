<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<c:set var="seccionMovil" value="eventos" scope="request" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Eventos | eventos.uy</title>
    <link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="${raiz}/css/movil.css">
</head>
<body class="movil">
<jsp:include page="/WEB-INF/views/movil/fragmentos/cabecera.jsp" />
<main class="movil-contenedor">
    <h1 class="movil-etiqueta">Eventos</h1>
    <c:if test="${empty eventos}"><div class="alert alert-info">Todavía no hay eventos para mostrar.</div></c:if>
    <div class="row row-cols-2 row-cols-md-3 g-3 mb-4">
        <c:forEach var="e" items="${eventos}">
            <c:url var="urlEvento" value="/movil/eventos"><c:param name="evento" value="${e.nombre}" /></c:url>
            <div class="col">
                <a href="${urlEvento}" class="movil-evento ${e.nombre eq eventoElegido ? 'movil-evento-activo' : ''}">
                    <span class="movil-evento-inicial" aria-hidden="true"><c:out value="${e.sigla}" /></span>
                    <span class="movil-evento-nombre"><c:out value="${e.nombre}" /></span>
                </a>
            </div>
        </c:forEach>
    </div>

    <c:if test="${not empty eventoElegido}">
        <h2 class="movil-etiqueta">Ediciones · <c:out value="${eventoElegido}" /></h2>
        <c:if test="${empty ediciones}"><div class="alert alert-info">Este evento no tiene ediciones aceptadas.</div></c:if>
        <div class="list-group movil-lista mb-4">
            <c:forEach var="ed" items="${ediciones}">
                <c:url var="urlEdicion" value="/movil/edicion">
                    <c:param name="evento" value="${eventoElegido}" />
                    <c:param name="nombre" value="${ed.idNombre}" />
                </c:url>
                <a href="${urlEdicion}" class="list-group-item list-group-item-action movil-toque">
                    <strong class="movil-texto"><c:out value="${ed.idNombre}" /></strong>
                    <small class="d-block text-secondary"><c:out value="${ed.ciudad}" />, <c:out value="${ed.pais}" /></small>
                </a>
            </c:forEach>
        </div>
    </c:if>
</main>
<script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>
