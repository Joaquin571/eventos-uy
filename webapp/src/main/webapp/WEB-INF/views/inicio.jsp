<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Eventos - eventos.uy</title>
    <link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="${raiz}/css/styles.css">
</head>
<body class="bg-light">

<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp" />

<main class="container py-5">

    <h1 class="h3 mb-4">
        Eventos
        <c:if test="${not empty categoriaSeleccionada}">
            · <c:out value="${categoriaSeleccionada}" />
        </c:if>
    </h1>

    <c:choose>
        <c:when test="${empty eventos}">
            <div class="alert alert-info">No hay eventos para mostrar.</div>
        </c:when>
        <c:otherwise>
            <div class="row g-4">
                <c:forEach var="evento" items="${eventos}">
                    <c:url var="urlEvento" value="/evento">
                        <c:param name="nombre" value="${evento.nombre}" />
                    </c:url>
                    <div class="col-md-6 col-lg-4">
                        <div class="card h-100 shadow-sm">
                            <div class="card-body">
                                <h2 class="h5">
                                    <a href="${urlEvento}" class="text-decoration-none">
                                        <c:out value="${evento.nombre}" />
                                    </a>
                                </h2>
                                <p class="text-muted"><c:out value="${evento.descripcion}" /></p>
                                <small class="text-secondary">
                                    <c:forEach var="categoria" items="${evento.categorias}" varStatus="estado">
                                        <c:out value="${categoria}" /><c:if test="${not estado.last}"> · </c:if>
                                    </c:forEach>
                                </small>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>

</main>

<script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>