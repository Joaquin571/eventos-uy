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
<form method="post" action="${raiz}/alta-tipo-registro" class="card card-body">
<label class="form-label">Edición<select name="edicion" class="form-select" required><option value="">Seleccione una edición propia</option>
<c:forEach var="e" items="${ediciones}"><option value="<c:out value='${e.idNombre}'/>"><c:out value="${e.idNombre}"/></option></c:forEach></select></label>
<label class="form-label">Nombre<input name="nombre" class="form-control" required></label>
<label class="form-label">Descripción<textarea name="descripcion" class="form-control" required></textarea></label>
<label class="form-label">Costo<input name="costo" type="number" min="0" step="0.01" class="form-control" required></label>
<label class="form-label">Cupo<input name="cupo" type="number" min="1" step="1" class="form-control" required></label>
<div class="mt-3"><button class="btn btn-primary">Confirmar alta</button> <a class="btn btn-outline-secondary" href="${raiz}/">Cancelar</a></div></form>
</main><script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script></body></html>