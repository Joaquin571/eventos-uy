<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${evento.nombre}" /> - eventos.uy</title>
    <link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="${raiz}/css/styles.css">
</head>
<body class="bg-light">

<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp" />

<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-12 col-lg-8">

            <div class="card border-0 shadow-sm mb-4">
                <div class="card-body p-4">
                    <div class="d-flex align-items-start">
                        <c:if test="${tieneImagen}">
                            <c:url var="urlImagen" value="/imagen">
                                <c:param name="tipo" value="evento" />
                                <c:param name="id" value="${evento.nombre}" />
                            </c:url>
                            <img src="${urlImagen}" alt="Imagen del evento"
                                 class="rounded me-3"
                                 style="width: 120px; height: 120px; object-fit: cover;">
                        </c:if>
                        <div>
                            <h1 class="h3 mb-2"><c:out value="${evento.nombre}" /></h1>
                            <p class="text-muted mb-2"><c:out value="${evento.descripcion}" /></p>
                            <p class="mb-1"><strong>Sigla:</strong> <c:out value="${evento.sigla}" /></p>
                            <p class="mb-1"><strong>Alta:</strong> <c:out value="${fechaAlta}" /></p>
                            <p class="mb-0">
                                <strong>Categorías:</strong>
                                <c:forEach var="categoria" items="${evento.categorias}" varStatus="estado">
                                    <span class="badge bg-secondary"><c:out value="${categoria}" /></span>
                                </c:forEach>
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="card border-0 shadow-sm mb-4">
                <div class="card-body p-4">
                    <h2 class="h5 mb-3">Ediciones</h2>
                    <c:choose>
                        <c:when test="${empty ediciones}">
                            <div class="alert alert-info mb-0">Este evento todavía no tiene ediciones.</div>
                        </c:when>
                        <c:otherwise>
                            <div class="list-group">
                                <c:forEach var="edicion" items="${ediciones}">
                                    <c:url var="urlEdicion" value="/edicion">
                                        <c:param name="nombre" value="${edicion.idNombre}" />
                                    </c:url>
                                    <a href="${urlEdicion}" class="list-group-item list-group-item-action">
                                        <strong><c:out value="${edicion.idNombre}" /></strong>
                                        <small class="text-muted d-block">
                                            <c:out value="${edicion.ciudad}" />, <c:out value="${edicion.pais}" />
                                        </small>
                                    </a>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <a href="${raiz}/" class="btn btn-secondary">Volver al listado</a>
        </div>
    </div>
</main>

<script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>
