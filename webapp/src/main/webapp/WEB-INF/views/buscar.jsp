<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!doctype html><html lang="es"><head><meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Búsqueda | eventos.uy</title>
<link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet" href="${raiz}/css/styles.css">
</head><body class="bg-light">
<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp"/>
<main class="container py-4"><h1>Resultados de búsqueda</h1>
<form action="${raiz}/buscar" method="get" class="row g-3 mb-4">
<div class="col-12 col-md-6">
<label for="textoBusqueda" class="form-label">Buscar eventos y ediciones</label>
<input id="textoBusqueda" type="search" name="texto" class="form-control" maxlength="255" value="<c:out value='${texto}'/>">
</div><div class="col-12 col-md-4">
<label for="ordenBusqueda" class="form-label">Ordenar resultados</label>
<select id="ordenBusqueda" name="orden" class="form-select">
<option value="FECHA_DESC" ${orden eq 'FECHA_DESC' ? 'selected' : ''}>Más recientes</option>
<option value="NOMBRE_ASC" ${orden eq 'NOMBRE_ASC' ? 'selected' : ''}>Nombre A-Z</option>
<option value="NOMBRE_DESC" ${orden eq 'NOMBRE_DESC' ? 'selected' : ''}>Nombre Z-A</option>
</select></div><div class="col-12 col-md-2 d-flex align-items-end">
<button type="submit" class="btn btn-primary w-100">Buscar</button></div></form>
<c:if test="${empty resultados}"><p>No se encontraron resultados.</p></c:if>
<ul class="list-group"><c:forEach var="r" items="${resultados}"><li class="list-group-item text-break">
<c:choose><c:when test="${r.tipo eq 'EVENTO'}"><c:url var="destino" value="/evento"><c:param name="nombre" value="${r.nombre}"/></c:url></c:when>
<c:otherwise><c:url var="destino" value="/edicion"><c:param name="evento" value="${r.evento}"/><c:param name="nombre" value="${r.nombre}"/></c:url></c:otherwise></c:choose>
<a href="${destino}"><c:out value="${r.nombre}"/></a> (<c:out value="${r.tipo}"/>) <c:out value="${r.descripcion}"/>
</li></c:forEach></ul></main>
<script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body></html>
