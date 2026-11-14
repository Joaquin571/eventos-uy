<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Consultas | eventos.uy</title>
<link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet" href="${raiz}/css/styles.css"></head><body>
<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp" />
<main class="container py-4">
<h1>Consulta de tipo de registro</h1>
<c:url var="urlConsultaTipo" value="/tipo-registro"/>
<form method="get" action="${urlConsultaTipo}" class="card card-body mb-3" data-selector-consulta>
<label for="evento" class="form-label">Evento</label>
<select id="evento" name="evento" class="form-select" required><option value="">Seleccione un evento</option>
<c:forEach var="e" items="${eventos}"><option value="<c:out value='${e.nombre}'/>" ${e.nombre eq eventoElegido ? 'selected' : ''}><c:out value="${e.nombre}"/></option></c:forEach></select>
<button class="btn btn-outline-primary mt-2">Ver ediciones</button></form>
<c:if test="${not empty eventoElegido}">
<form method="get" action="${urlConsultaTipo}" class="card card-body mb-3" data-selector-consulta>
<input type="hidden" name="evento" value="<c:out value='${eventoElegido}'/>">
<label for="edicion" class="form-label">Edición aceptada</label>
<select id="edicion" name="edicion" class="form-select" required><option value="">Seleccione una edición</option>
<c:forEach var="e" items="${ediciones}"><option value="<c:out value='${e.idNombre}'/>" ${e.idNombre eq edicionElegida.idNombre ? 'selected' : ''}><c:out value="${e.idNombre}"/></option></c:forEach></select>
<button class="btn btn-outline-primary mt-2">Ver tipos</button>
<c:if test="${empty ediciones}"><p>No hay ediciones aceptadas para este evento.</p></c:if>
</form>
<c:url var="urlEvento" value="/evento"><c:param name="nombre" value="${eventoElegido}"/></c:url><a href="${urlEvento}" class="btn btn-outline-secondary mb-3">Volver al evento</a>
</c:if>
<c:if test="${not empty edicionElegida}">
<h2 class="h4">Tipos de <c:out value="${edicionElegida.idNombre}"/></h2>
<form method="get" action="${urlConsultaTipo}" class="card card-body mb-3" data-selector-consulta>
<input type="hidden" name="evento" value="<c:out value='${eventoElegido}'/>">
<input type="hidden" name="edicion" value="<c:out value='${edicionElegida.idNombre}'/>">
<label for="tipo" class="form-label">Tipo de registro</label>
<select id="tipo" name="nombre" class="form-select" required><option value="">Seleccione un tipo</option>
<c:forEach var="t" items="${tipos}"><option value="<c:out value='${t.idNombre}'/>" ${t.idNombre eq detalle.idNombre ? 'selected' : ''}><c:out value="${t.idNombre}"/></option></c:forEach></select>
<button class="btn btn-outline-primary mt-2">Consultar</button>
<c:if test="${empty tipos}"><p>Esta edición no tiene tipos de registro.</p></c:if>
</form>
<c:url var="urlEdicion" value="/edicion"><c:param name="nombre" value="${edicionElegida.idNombre}"/></c:url><a href="${urlEdicion}" class="btn btn-outline-secondary mb-3">Volver a la edición</a>
</c:if>
<c:if test="${not empty detalle}"><article class="card card-body"><h2><c:out value="${detalle.idNombre}"/></h2>
<p>Descripción: <c:out value="${detalle.descripcion}"/></p><p>Costo: $<c:out value="${costo}"/></p><p>Cupo: <c:out value="${detalle.cupo}"/></p>
</article></c:if>
</main><script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
<script src="${raiz}/js/consultas-selectores.js"></script></body></html>
