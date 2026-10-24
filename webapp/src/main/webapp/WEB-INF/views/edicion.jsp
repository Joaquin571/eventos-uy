<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!DOCTYPE html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Consulta de edición | eventos.uy</title>
<link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet" href="${raiz}/css/styles.css"></head><body>
<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp"/>
<main class="container py-4"><h1>Consulta de edición</h1>
<c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty exito}"><div class="alert alert-success"><c:out value="${exito}"/></div></c:if>
<c:if test="${not empty advertencia}"><div class="alert alert-warning"><c:out value="${advertencia}"/></div></c:if>
<form method="get" action="${raiz}/edicion" class="card card-body mb-3">
<label class="form-label">Evento<select name="evento" class="form-select" required><option value="">Seleccione un evento</option>
<c:forEach var="e" items="${eventos}"><option value="<c:out value='${e.nombre}'/>" ${e.nombre eq eventoElegido ? 'selected' : ''}><c:out value="${e.nombre}"/></option></c:forEach></select></label>
<button class="btn btn-primary mt-2">Buscar ediciones</button></form>
<c:if test="${not empty eventoElegido}"><div class="list-group mb-3">
<c:forEach var="e" items="${ediciones}"><c:url var="urlEd" value="/edicion"><c:param name="evento" value="${eventoElegido}"/><c:param name="nombre" value="${e.idNombre}"/></c:url>
<a class="list-group-item list-group-item-action" href="${urlEd}"><c:out value="${e.idNombre}"/> — <c:out value="${e.estado}"/></a></c:forEach>
<c:if test="${empty ediciones}"><p>No hay ediciones visibles para este evento.</p></c:if></div></c:if>
<c:if test="${not empty detalle}"><article class="card card-body"><h2><c:out value="${detalle.idNombre}"/></h2>
<p>Estado: <c:out value="${detalle.estado}"/></p>
<p>Sigla: <c:out value="${detalle.sigla}"/></p>
<p>Ciudad: <c:out value="${detalle.ciudad}"/> — País: <c:out value="${detalle.pais}"/></p>
<p>Organizador: <c:out value="${detalle.nombreOrganizador}"/></p>
<p>Inicio: <c:out value="${detalle.fechaInicio}"/> — Fin: <c:out value="${detalle.fechaFin}"/></p>
<p>Fecha de alta: <c:out value="${detalle.fechaAlta}"/></p>
<h3>Tipos de registro</h3><ul><c:forEach var="t" items="${tipos}"><li><c:out value="${t.idNombre}"/> — $<c:out value="${t.costo}"/> — Cupo: <c:out value="${t.cupo}"/></li></c:forEach></ul>
</article></c:if>
</main><script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script></body></html>