<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!DOCTYPE html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Alta de tipo de registro | eventos.uy</title>
<link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet" href="${raiz}/css/styles.css"></head><body>
<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp"/>
<main class="container py-4"><h1>Alta de tipo de registro</h1>
<c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty exito}"><div class="alert alert-success"><c:out value="${exito}"/></div></c:if>
<c:if test="${not empty advertencia}"><div class="alert alert-warning"><c:out value="${advertencia}"/></div></c:if>
<form id="formAltaTipoRegistro" method="post" action="${raiz}/alta-tipo-registro" class="card card-body">
<jsp:include page="/WEB-INF/views/fragmentos/csrf.jsp"/>
<label class="form-label">Edición<select id="tipoEdicion" name="edicion" class="form-select" required aria-describedby="tipoEdicionError"><option value="">Seleccione una edición propia</option>
<c:forEach var="e" items="${ediciones}"><option value="<c:out value='${e.idNombre}'/>" <c:if test="${form.edicion eq e.idNombre}">selected</c:if>><c:out value="${e.idNombre}"/></option></c:forEach></select></label>
<div id="tipoEdicionError" class="invalid-feedback d-block" aria-live="polite"></div>
<label class="form-label">Nombre<input id="tipoNombre" name="nombre" value="<c:out value='${form.nombre}'/>" class="form-control" required aria-describedby="tipoNombreError"></label>
<div id="tipoNombreError" class="invalid-feedback d-block" aria-live="polite"></div>
<label class="form-label">Descripción<textarea id="tipoDescripcion" name="descripcion" class="form-control" required aria-describedby="tipoDescripcionError"><c:out value="${form.descripcion}"/></textarea></label>
<div id="tipoDescripcionError" class="invalid-feedback d-block" aria-live="polite"></div>
<label class="form-label">Costo<input id="tipoCosto" name="costo" value="<c:out value='${form.costo}'/>" type="number" min="0" step="0.01" class="form-control" required aria-describedby="tipoCostoError"></label>
<div id="tipoCostoError" class="invalid-feedback d-block" aria-live="polite"></div>
<label class="form-label">Cupo<input id="tipoCupo" name="cupo" value="<c:out value='${form.cupo}'/>" type="number" min="1" step="1" class="form-control" required aria-describedby="tipoCupoError"></label>
<div id="tipoCupoError" class="invalid-feedback d-block" aria-live="polite"></div>
<div class="mt-3"><button class="btn btn-primary">Confirmar alta</button> <a class="btn btn-outline-secondary" href="${raiz}/">Cancelar</a></div></form>
</main><script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
<script src="${raiz}/js/validacion-altas.js"></script></body></html>
