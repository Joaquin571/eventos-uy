<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<c:set var="seccionMovil" value="${modoAsistencia ? 'asistencia' : 'registros'}" scope="request" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${modoAsistencia ? 'Registrar asistencia' : 'Mis registros'} | eventos.uy</title>
    <link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="${raiz}/css/movil.css">
</head>
<body class="movil">
<jsp:include page="/WEB-INF/views/movil/fragmentos/cabecera.jsp" />
<main class="movil-contenedor">
    <h1 class="movil-etiqueta">${modoAsistencia ? 'Registrar asistencia' : 'Mis registros'}</h1>
    <c:if test="${not empty exito}"><div class="alert alert-success" role="alert"><c:out value="${exito}" /></div></c:if>
    <c:if test="${not empty advertencia}"><div class="alert alert-warning" role="alert"><c:out value="${advertencia}" /></div></c:if>
    <c:if test="${empty registros}"><div class="alert alert-info">Todavía no te registraste a ninguna edición.</div></c:if>
    <div class="list-group movil-lista mb-4">
        <c:forEach var="r" items="${registros}">
            <c:url var="urlRegistro" value="/movil/registros">
                <c:param name="edicion" value="${r.nombreEdicion}" />
                <c:param name="modo" value="${modoAsistencia ? 'asistencia' : ''}" />
            </c:url>
            <a href="${urlRegistro}" class="list-group-item list-group-item-action movil-toque ${r.nombreEdicion eq edicionElegida ? 'active' : ''}">
                <span class="d-flex justify-content-between align-items-start gap-2 w-100">
                    <span>
                        <strong class="movil-texto"><c:out value="${r.nombreEdicion}" /></strong>
                        <small class="d-block"><c:out value="${r.nombreTipoRegistro}" /> · <c:out value="${r.fecha}" /></small>
                    </span>
                    <c:if test="${r.asistenciaConfirmada}"><span class="badge text-bg-success">Asistió</span></c:if>
                </span>
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
                    <div><dt>Asistencia</dt><dd>
                        <c:choose>
                            <c:when test="${detalle.asistenciaConfirmada}"><span class="badge text-bg-success">Asistió</span></c:when>
                            <c:otherwise><span class="badge text-bg-secondary">Sin registrar</span></c:otherwise>
                        </c:choose>
                    </dd></div>
                </dl>
                <c:if test="${puedeConfirmarAsistencia}">
                    <form method="post" action="${raiz}/movil/asistencia" class="mt-3">
                        <jsp:include page="/WEB-INF/views/fragmentos/csrf.jsp"/>
                        <input type="hidden" name="edicion" value="<c:out value='${detalle.nombreEdicion}'/>">
                        <c:if test="${modoAsistencia}"><input type="hidden" name="modo" value="asistencia"></c:if>
                        <button type="submit" class="btn btn-primary btn-lg w-100 fw-bold">Registrar asistencia</button>
                    </form>
                </c:if>
                <c:if test="${not detalle.asistenciaConfirmada and not puedeConfirmarAsistencia}">
                    <p class="small text-secondary mt-3 mb-0">La asistencia se puede registrar cuando la edición ya comenzó.</p>
                </c:if>
            </div>
        </section>
    </c:if>
</main>
<script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>
