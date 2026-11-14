<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Instituciones - eventos.uy</title>
<link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet" href="${raiz}/css/styles.css"></head>
<body class="bg-light"><jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp"/>
<main class="container py-4"><h1 class="h3">Instituciones</h1>
<div class="list-group my-3"><c:forEach var="item" items="${instituciones}">
<c:url var="enlace" value="/consulta-institucion"><c:param name="nombre" value="${item.nombre}"/></c:url>
<a class="list-group-item list-group-item-action" href="<c:out value='${enlace}'/>"><c:out value="${item.nombre}"/></a>
</c:forEach></div>
<c:if test="${not empty institucion}"><article class="card p-4">
<h2 class="h4"><c:out value="${institucion.nombre}"/></h2>
<p><c:out value="${institucion.descripcion}"/></p>
<p>Sitio web: <c:out value="${institucion.sitioWeb}"/></p>
<c:url var="foto" value="/imagen"><c:param name="tipo" value="institucion"/><c:param name="id" value="${institucion.nombre}"/></c:url>
<img class="img-fluid rounded" style="max-width:240px" src="<c:out value='${foto}'/>" alt="Imagen de la institución">
</article></c:if>
</main><script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script></body></html>