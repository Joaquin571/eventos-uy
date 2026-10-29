<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Patrocinios | eventos.uy</title>
<link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet" href="${raiz}/css/styles.css"></head><body>
<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp" />
<main class="container py-4">
<h1>Alta de patrocinio</h1>
<c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}" /></div></c:if>
<form method="get" action="${raiz}/alta-patrocinio" class="card card-body mb-3" data-seleccion-patrocinio>
<label for="evento" class="form-label">Evento</label>
<select id="evento" name="evento" class="form-select" required><option value="">Seleccione un evento</option>
<c:forEach var="e" items="${eventos}"><option value="<c:out value='${e.nombre}'/>" ${e.nombre eq form.evento ? 'selected' : ''}><c:out value="${e.nombre}"/></option></c:forEach></select>
<button class="btn btn-outline-primary mt-2">Ver mis ediciones</button>
<c:if test="${empty eventos}"><p class="mt-2">No hay eventos disponibles.</p></c:if>
</form>
<c:if test="${not empty form.evento}">
<form method="get" action="${raiz}/alta-patrocinio" class="card card-body mb-3" data-seleccion-patrocinio>
<input type="hidden" name="evento" value="<c:out value='${form.evento}'/>">
<label for="edicion" class="form-label">Edición propia</label>
<select id="edicion" name="edicion" class="form-select" required><option value="">Seleccione una edición</option>
<c:forEach var="e" items="${ediciones}"><option value="<c:out value='${e.idNombre}'/>" ${e.idNombre eq form.edicion ? 'selected' : ''}><c:out value="${e.idNombre}"/> — <c:out value="${e.estado}"/></option></c:forEach></select>
<button class="btn btn-outline-primary mt-2">Continuar</button>
<c:if test="${empty ediciones}"><p class="mt-2">No organizás ediciones de este evento.</p></c:if>
</form></c:if>
<c:if test="${not empty form.edicion}">
<form id="formAltaPatrocinio" method="post" action="${raiz}/alta-patrocinio" class="card card-body">
<h2 class="h4"><c:out value="${form.edicion}"/></h2>
<input type="hidden" name="evento" value="<c:out value='${form.evento}'/>">
<input type="hidden" name="edicion" value="<c:out value='${form.edicion}'/>">
<label for="institucion" class="form-label mt-2">Institución</label>
<select id="institucion" name="institucion" class="form-select" required><option value="">Seleccione una institución</option>
<c:if test="${institucionNoDisponible}"><option selected value="<c:out value='${form.institucion}'/>"><c:out value="${form.institucion}"/> (no disponible)</option></c:if>
<c:forEach var="i" items="${instituciones}"><option value="<c:out value='${i.nombre}'/>" ${i.nombre eq form.institucion ? 'selected' : ''}><c:out value="${i.nombre}"/></option></c:forEach></select>
<label for="nivel" class="form-label mt-2">Nivel de patrocinio</label>
<select id="nivel" name="nivel" class="form-select" required><option value="">Seleccione un nivel</option>
<c:if test="${nivelNoDisponible}"><option selected value="<c:out value='${form.nivel}'/>"><c:out value="${form.nivel}"/> (no disponible)</option></c:if>
<c:forEach var="n" items="${niveles}"><option value="<c:out value='${n}'/>" ${n.toString() eq form.nivel ? 'selected' : ''}><c:out value="${n}"/></option></c:forEach></select>
<label for="tipoRegistro" class="form-label mt-2">Tipo de registro</label>
<select id="tipoRegistro" name="tipoRegistro" class="form-select" required><option value="">Seleccione un tipo de registro</option>
<c:if test="${tipoNoDisponible}"><option selected value="<c:out value='${form.tipoRegistro}'/>"><c:out value="${form.tipoRegistro}"/> (no disponible)</option></c:if>
<c:forEach var="t" items="${tipos}"><option value="<c:out value='${t.idNombre}'/>" data-costo="<c:out value='${t.costo}'/>" ${t.idNombre eq form.tipoRegistro ? 'selected' : ''}><c:out value="${t.idNombre}"/> — $<c:out value="${t.costo}"/></option></c:forEach></select>
<label for="aporte" class="form-label mt-2">Aporte económico (use punto decimal)</label>
<input id="aporte" name="aporte" type="text" inputmode="decimal" class="form-control" required value="<c:out value='${form.aporte}'/>">
<label for="registrograt" class="form-label mt-2">Cantidad de registros gratuitos</label>
<input id="registrograt" name="registrograt" type="text" inputmode="numeric" class="form-control" required value="<c:out value='${form.registrograt}'/>">
<label for="codigo" class="form-label mt-2">Código de patrocinio</label>
<input id="codigo" name="codigo" class="form-control" required value="<c:out value='${form.codigo}'/>">
<c:if test="${empty tipos}"><p class="alert alert-info mt-3">La edición no tiene tipos de registro. Creá uno antes de registrar un patrocinio.</p></c:if>
<c:if test="${empty instituciones}"><p class="alert alert-info mt-3">No hay instituciones disponibles. Creá una antes de registrar un patrocinio.</p></c:if>
<div class="d-flex gap-2 mt-3"><button class="btn btn-primary" ${empty tipos or empty instituciones ? 'disabled' : ''}>Dar de alta</button>
<a href="${raiz}/" class="btn btn-secondary">Cancelar</a></div>
</form></c:if>
<script src="${raiz}/js/alta-patrocinio.js"></script>
</main><script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body></html>
