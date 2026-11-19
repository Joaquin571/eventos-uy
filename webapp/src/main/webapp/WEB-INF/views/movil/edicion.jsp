<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<c:set var="seccionMovil" value="eventos" scope="request" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${detalle.idNombre}" /> | eventos.uy</title>
    <link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="${raiz}/css/movil.css">
</head>
<body class="movil">
<jsp:include page="/WEB-INF/views/movil/fragmentos/cabecera.jsp" />
<main class="movil-contenedor">
    <c:url var="urlVolver" value="/movil/eventos"><c:param name="evento" value="${eventoElegido}" /></c:url>
    <a href="${urlVolver}" class="movil-volver movil-toque">‹ <c:out value="${eventoElegido}" /></a>
    <h1 class="h3 fw-bold movil-texto"><c:out value="${detalle.idNombre}" /></h1>

    <jsp:include page="/WEB-INF/views/movil/fragmentos/video.jsp" />

    <c:if test="${tieneImagen}">
        <c:url var="urlImagen" value="/imagen"><c:param name="tipo" value="edicion" /><c:param name="id" value="${detalle.idNombre}" /></c:url>
        <img src="${urlImagen}" alt="Imagen de la edición" class="img-fluid rounded mb-3 movil-imagen">
    </c:if>

    <dl class="movil-datos">
        <div><dt>Ciudad</dt><dd><c:out value="${detalle.ciudad}" />, <c:out value="${detalle.pais}" /></dd></div>
        <div><dt>Fechas</dt><dd><c:out value="${fechaInicio}" /> – <c:out value="${fechaFin}" /></dd></div>
        <div><dt>Sigla</dt><dd><c:out value="${detalle.sigla}" /></dd></div>
        <div><dt>Organizador</dt><dd><c:out value="${empty organizador ? detalle.nombreOrganizador : organizador.nombre}" /></dd></div>
    </dl>

    <h2 class="movil-etiqueta">Tipos de registro</h2>
    <c:if test="${empty tipos}"><p class="text-secondary">Esta edición no tiene tipos de registro.</p></c:if>
    <ul class="list-unstyled movil-precios">
        <c:forEach var="t" items="${tipos}">
            <li><span class="movil-texto"><c:out value="${t.idNombre}" /></span>
                <strong>$ <fmt:formatNumber value="${t.costo}" maxFractionDigits="2" groupingUsed="false" /></strong></li>
        </c:forEach>
    </ul>

    <h2 class="movil-etiqueta">Patrocinios</h2>
    <c:if test="${empty patrocinadoras}"><p class="text-secondary">Esta edición no tiene patrocinios.</p></c:if>
    <div class="d-flex flex-wrap gap-2 mb-3">
        <c:forEach var="i" items="${patrocinadoras}"><span class="badge text-bg-secondary movil-texto"><c:out value="${i}" /></span></c:forEach>
    </div>

    <c:if test="${not empty registroPropio}">
        <c:url var="urlMiRegistro" value="/movil/registros"><c:param name="edicion" value="${detalle.idNombre}" /></c:url>
        <a href="${urlMiRegistro}" class="movil-registro-propio movil-toque">
            <span>Mi registro: <strong class="movil-texto"><c:out value="${registroPropio.nombreTipoRegistro}" /></strong></span><span aria-hidden="true">›</span>
        </a>
    </c:if>
</main>
<script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>
