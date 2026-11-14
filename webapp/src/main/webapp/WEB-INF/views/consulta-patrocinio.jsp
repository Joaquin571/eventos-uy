<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Patrocinios | eventos.uy</title>
<link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet" href="${raiz}/css/styles.css"></head><body>
<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp" />
<main class="container py-4">
<h1>Consulta de patrocinio</h1>
<c:if test="${param.exito eq 'true' and not empty patrocinio}"><div class="alert alert-success">Patrocinio registrado correctamente.</div></c:if>
<form method="get" action="${raiz}/consulta-patrocinio" class="card card-body mb-3">
<label for="evento" class="form-label">Evento</label>
<select id="evento" name="evento" class="form-select" required><option value="">Seleccione un evento</option>
<c:forEach var="e" items="${eventos}"><option value="<c:out value='${e.nombre}'/>" ${e.nombre eq eventoElegido ? 'selected' : ''}><c:out value="${e.nombre}"/></option></c:forEach></select>
<button class="btn btn-outline-primary mt-2">Ver ediciones</button></form>
<c:if test="${not empty eventoElegido}">
<form method="get" action="${raiz}/consulta-patrocinio" class="card card-body mb-3">
<input type="hidden" name="evento" value="<c:out value='${eventoElegido}'/>">
<label for="edicion" class="form-label">Edición</label>
<select id="edicion" name="edicion" class="form-select" required><option value="">Seleccione una edición</option>
<c:forEach var="e" items="${ediciones}"><option value="<c:out value='${e.idNombre}'/>" ${e.idNombre eq edicionElegida ? 'selected' : ''}><c:out value="${e.idNombre}"/> — <c:out value="${e.estado}"/></option></c:forEach></select>
<button class="btn btn-outline-primary mt-2">Ver patrocinios</button>
<c:if test="${empty ediciones}"><p class="mt-2">No hay ediciones aceptadas para este evento.</p></c:if>
</form></c:if>
<c:if test="${not empty edicionElegida}">
<form method="get" action="${raiz}/consulta-patrocinio" class="card card-body mb-3">
<input type="hidden" name="evento" value="<c:out value='${eventoElegido}'/>">
<input type="hidden" name="edicion" value="<c:out value='${edicionElegida}'/>">
<label for="institucion" class="form-label">Patrocinio de institución</label>
<select id="institucion" name="institucion" class="form-select" required><option value="">Seleccione un patrocinio</option>
<c:forEach var="i" items="${patrocinadoras}"><option value="<c:out value='${i}'/>" ${i eq institucionElegida ? 'selected' : ''}><c:out value="${i}"/></option></c:forEach></select>
<button class="btn btn-outline-primary mt-2">Consultar</button>
<c:if test="${empty patrocinadoras}"><p class="mt-2">Esta edición no tiene patrocinios.</p></c:if>
</form>
<c:url var="urlEdicion" value="/edicion"><c:param name="nombre" value="${edicionElegida}"/></c:url>
<a href="${urlEdicion}" class="btn btn-outline-secondary mb-3">Volver a la edición</a>
</c:if>
<c:if test="${not empty patrocinio}"><article class="card card-body mb-3"><h2 class="h4">Detalle del patrocinio</h2>
<dl class="row">
<dt class="col-sm-4">Edición</dt><dd class="col-sm-8"><c:out value="${patrocinio.nombreEdicion}"/></dd>
<dt class="col-sm-4">Institución</dt><dd class="col-sm-8"><c:out value="${patrocinio.nombreInstituto}"/></dd>
<dt class="col-sm-4">Nivel</dt><dd class="col-sm-8"><c:out value="${patrocinio.nivel}"/></dd>
<dt class="col-sm-4">Aporte</dt><dd class="col-sm-8">$<c:out value="${aporte}"/></dd>
<dt class="col-sm-4">Tipo de registro</dt><dd class="col-sm-8"><c:out value="${patrocinio.nombreTipoRegistro}"/></dd>
<dt class="col-sm-4">Registros gratuitos</dt><dd class="col-sm-8"><c:out value="${patrocinio.cantRegistrosGrat}"/></dd>
<dt class="col-sm-4">Fecha de alta</dt><dd class="col-sm-8"><c:out value="${fechaAlta}"/></dd>
<c:if test="${esOrganizadorDuenio}"><dt class="col-sm-4">Código de patrocinio</dt><dd class="col-sm-8"><c:out value="${patrocinio.codigoPatrocinio}"/></dd></c:if>
</dl></article></c:if>
<c:if test="${not empty eventoElegido}"><c:url var="urlEvento" value="/evento"><c:param name="nombre" value="${eventoElegido}"/></c:url><a href="${urlEvento}" class="btn btn-outline-secondary">Volver al evento</a></c:if>
<a href="${raiz}/" class="btn btn-secondary">Volver al inicio</a>
</main><script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body></html>
