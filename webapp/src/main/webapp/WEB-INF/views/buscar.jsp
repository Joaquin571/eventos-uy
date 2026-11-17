<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><title>Búsqueda | eventos.uy</title></head><body>
<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp"/>
<main class="container py-4"><h1>Resultados de búsqueda</h1>
<form action="${pageContext.request.contextPath}/buscar" method="get">
<input type="search" name="texto" value="<c:out value='${texto}'/>" aria-label="Buscar eventos y ediciones">
<select name="orden" aria-label="Ordenar resultados">
<option value="FECHA_DESC" ${orden eq 'FECHA_DESC' ? 'selected' : ''}>Más recientes</option>
<option value="NOMBRE_ASC" ${orden eq 'NOMBRE_ASC' ? 'selected' : ''}>Nombre A-Z</option>
<option value="NOMBRE_DESC" ${orden eq 'NOMBRE_DESC' ? 'selected' : ''}>Nombre Z-A</option>
</select><button type="submit">Buscar</button></form>
<c:if test="${empty resultados}"><p>No se encontraron resultados.</p></c:if>
<ul><c:forEach var="r" items="${resultados}"><li>
<c:choose><c:when test="${r.tipo eq 'EVENTO'}"><c:url var="destino" value="/evento"><c:param name="nombre" value="${r.nombre}"/></c:url></c:when>
<c:otherwise><c:url var="destino" value="/edicion"><c:param name="evento" value="${r.evento}"/><c:param name="nombre" value="${r.nombre}"/></c:url></c:otherwise></c:choose>
<a href="${destino}"><c:out value="${r.nombre}"/></a> (<c:out value="${r.tipo}"/>) <c:out value="${r.descripcion}"/>
</li></c:forEach></ul></main></body></html>
