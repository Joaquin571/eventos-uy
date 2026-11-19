<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<c:set var="seccionMovil" value="registros" scope="request" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Mis registros | eventos.uy</title>
    <link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="${raiz}/css/movil.css">
</head>
<body class="movil">
<jsp:include page="/WEB-INF/views/movil/fragmentos/cabecera.jsp" />
<main class="movil-contenedor">
    <h1 class="movil-etiqueta">Mis registros</h1>
    <c:if test="${empty registros}"><div class="alert alert-info">Todavía no te registraste a ninguna edición.</div></c:if>
    <div class="list-group movil-lista mb-4">
        <c:forEach var="r" items="${registros}">
            <c:url var="urlRegistro" value="/movil/registros"><c:param name="edicion" value="${r.nombreEdicion}" /></c:url>
            <a href="${urlRegistro}" class="list-group-item list-group-item-action movil-toque ${r.nombreEdicion eq edicionElegida ? 'active' : ''}">
                <strong class="movil-texto"><c:out value="${r.nombreEdicion}" /></strong>
                <small class="d-block"><c:out value="${r.nombreTipoRegistro}" /> · <c:out value="${r.fecha}" /></small>
            </a>
        </c:forEach>
    </div>

    <c:if test="${not empty detalle}">
        <section class="card movil-tarjeta mb-4">
            <div class="card-body">
                <h2 class="h5 movil-texto"><c:out value="${detalle.nombreEdicion}" /></h2>
                <dl class="movil-filas mb-0">
                    <div><dt>Tipo</dt><dd><c:out value="${detalle.nombreTipoRegistro}" /></dd></div>
                    <div><dt>Fecha de registro</dt><dd><c:out value="${detalle.fecha}" /></dd></div>
                    <div><dt>Costo</dt><dd>$ <c:out value="${detalle.costo}" /></dd></div>
                    <div><dt>Código de patrocinio</dt><dd><c:out value="${detalle.usoCodigo}" /></dd></div>
                    <%-- PENDIENTE (Adrián): fila "Asistencia" y botón "Registrar asistencia". --%>
                </dl>
            </div>
        </section>
    </c:if>
</main>
<script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>
