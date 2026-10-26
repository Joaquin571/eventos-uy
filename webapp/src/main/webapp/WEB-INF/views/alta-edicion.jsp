<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!DOCTYPE html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Alta de edición | eventos.uy</title>
<link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet" href="${raiz}/css/styles.css"></head><body>
<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp"/>
<main class="container py-4"><h1>Alta de edición</h1>
<c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty exito}"><div class="alert alert-success"><c:out value="${exito}"/></div></c:if>
<c:if test="${not empty advertencia}"><div class="alert alert-warning"><c:out value="${advertencia}"/></div></c:if>
<form method="post" action="${raiz}/alta-edicion" enctype="multipart/form-data" class="card card-body">
<label class="form-label">Evento<select name="evento" class="form-select" required><option value="">Seleccione un evento</option>
<c:forEach var="e" items="${eventos}"><option value="<c:out value='${e.nombre}'/>"><c:out value="${e.nombre}"/></option></c:forEach></select></label>
<label class="form-label">Nombre de edición<input name="nombre" class="form-control" required></label>
<label class="form-label">Sigla<input name="sigla" class="form-control" required></label>
<label class="form-label">Ciudad<input name="ciudad" class="form-control" required></label>
<label class="form-label">País<input name="pais" class="form-control" required></label>
<label class="form-label">Fecha de inicio<input type="date" name="fechaInicio" class="form-control" required></label>
<label class="form-label">Fecha de fin<input type="date" name="fechaFin" class="form-control" required></label>
<label class="form-label">Imagen opcional<input type="file" name="imagen" accept="image/png,image/jpeg,image/gif,image/webp" class="form-control"></label>
<div class="mt-3"><button class="btn btn-primary">Confirmar alta</button> <a class="btn btn-outline-secondary" href="${raiz}/">Cancelar</a></div></form>
</main><script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script></body></html>