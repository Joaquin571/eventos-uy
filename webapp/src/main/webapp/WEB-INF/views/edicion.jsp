<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Consultas | eventos.uy</title>
<link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet" href="${raiz}/css/styles.css"></head><body>
<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp" />
<main class="container py-4">
<h1>Consulta de edición</h1>
<c:url var="urlConsulta" value="/edicion"/>
<form method="get" action="${urlConsulta}" class="card card-body mb-3" data-selector-consulta>
<label for="evento" class="form-label">Evento</label>
<select id="evento" name="evento" class="form-select" required><option value="">Seleccione un evento</option>
<c:forEach var="e" items="${eventos}"><option value="<c:out value='${e.nombre}'/>" ${e.nombre eq eventoElegido ? 'selected' : ''}><c:out value="${e.nombre}"/></option></c:forEach></select>
<button class="btn btn-primary mt-2">Buscar ediciones</button></form>
<c:if test="${not empty eventoElegido}">
<div class="list-group mb-3">
<c:forEach var="e" items="${ediciones}"><c:url var="urlEd" value="/edicion"><c:param name="evento" value="${eventoElegido}"/><c:param name="nombre" value="${e.idNombre}"/></c:url>
<a class="list-group-item list-group-item-action" href="${urlEd}"><c:out value="${e.idNombre}"/></a></c:forEach>
<c:if test="${empty ediciones}"><p>No hay ediciones aceptadas para este evento.</p></c:if>
</div>
<c:url var="urlEvento" value="/evento"><c:param name="nombre" value="${eventoElegido}"/></c:url>
<a href="${urlEvento}" class="btn btn-outline-secondary mb-3">Volver al evento</a>
</c:if>
<c:if test="${not empty detalle}">
<article class="card card-body mb-3"><h2><c:out value="${detalle.idNombre}"/></h2>
<c:if test="${tieneImagen}"><c:url var="urlImagen" value="/imagen"><c:param name="tipo" value="edicion"/><c:param name="id" value="${detalle.idNombre}"/></c:url>
<img src="${urlImagen}" alt="Imagen de la edición" class="rounded mb-3" style="max-width: 320px; max-height: 240px; object-fit: contain;"></c:if>
<p>Estado: <c:out value="${detalle.estado}"/></p>
<p>Sigla: <c:out value="${detalle.sigla}"/></p>
<p>Ciudad: <c:out value="${detalle.ciudad}"/> — País: <c:out value="${detalle.pais}"/></p>
<c:url var="urlOrganizador" value="/usuario"><c:param name="nick" value="${detalle.nombreOrganizador}"/></c:url>
<p>Organizador: <a href="${urlOrganizador}"><c:out value="${empty organizador ? detalle.nombreOrganizador : organizador.nombre}"/></a></p>
<p>Inicio: <c:out value="${fechaInicio}"/> — Fin: <c:out value="${fechaFin}"/></p>
<p>Fecha de alta: <c:out value="${fechaAlta}"/></p>
<h3>Tipos de registro</h3><ul>
<c:forEach var="t" items="${tipos}"><c:url var="urlTipo" value="/tipo-registro"><c:param name="edicion" value="${detalle.idNombre}"/><c:param name="nombre" value="${t.idNombre}"/></c:url>
<li><a href="${urlTipo}"><c:out value="${t.idNombre}"/></a> — $<c:out value="${t.costo}"/> — Cupo: <c:out value="${t.cupo}"/></li></c:forEach>
</ul><c:if test="${empty tipos}"><p>Esta edición no tiene tipos de registro.</p></c:if>
<h3>Patrocinios</h3><ul>
<c:forEach var="i" items="${patrocinadoras}"><c:url var="urlPatrocinio" value="/consulta-patrocinio"><c:param name="edicion" value="${detalle.idNombre}"/><c:param name="institucion" value="${i}"/></c:url>
<li><a href="${urlPatrocinio}"><c:out value="${i}"/></a></li></c:forEach>
</ul><c:if test="${empty patrocinadoras}"><p>Esta edición no tiene patrocinios.</p></c:if>
<c:if test="${puedeRegistrarse}"><c:url var="urlRegistro" value="/registro-edicion"><c:param name="edicion" value="${detalle.idNombre}"/></c:url>
<a href="${urlRegistro}" class="btn btn-primary align-self-start">Registrarme</a></c:if>
</article>
<c:if test="${esPropietario}">
<section class="card card-body mb-3"><h3>Asistentes registrados</h3>
<c:if test="${empty registros}"><p>Esta edición no tiene asistentes registrados.</p></c:if>
<c:if test="${not empty registros}"><div class="table-responsive"><table class="table"><thead><tr><th>Asistente</th><th>Tipo</th><th>Fecha</th><th>Costo</th><th>Usó código</th></tr></thead><tbody>
<c:forEach var="r" items="${registros}"><c:url var="urlConsultaRegistro" value="/consulta-registro"><c:param name="edicion" value="${detalle.idNombre}"/><c:param name="asistente" value="${r.nickname}"/></c:url>
<c:url var="urlTipoRegistro" value="/tipo-registro"><c:param name="edicion" value="${detalle.idNombre}"/><c:param name="nombre" value="${r.nombreTipoRegistro}"/></c:url>
<tr><td><a href="${urlConsultaRegistro}"><c:out value="${r.nickname}"/></a></td><td><a href="${urlTipoRegistro}"><c:out value="${r.nombreTipoRegistro}"/></a></td><td><c:out value="${r.fecha}"/></td><td>$<c:out value="${r.costo}"/></td><td><c:out value="${r.usoCodigo}"/></td></tr>
</c:forEach></tbody></table></div></c:if>
<c:url var="urlRegistros" value="/consulta-registro"><c:param name="edicion" value="${detalle.idNombre}"/></c:url>
<a href="${urlRegistros}">Consultar registros de esta edición</a></section>
</c:if>
<c:if test="${not empty registroPropio}"><section class="card card-body mb-3"><h3>Mi registro</h3>
<c:url var="urlTipoPropio" value="/tipo-registro"><c:param name="edicion" value="${detalle.idNombre}"/><c:param name="nombre" value="${registroPropio.nombreTipoRegistro}"/></c:url>
<p>Tipo: <a href="${urlTipoPropio}"><c:out value="${registroPropio.nombreTipoRegistro}"/></a></p>
<p>Fecha de registro: <c:out value="${registroPropio.fecha}"/></p><p>Costo: $<c:out value="${registroPropio.costo}"/></p><p>Utilizó código de patrocinio: <c:out value="${registroPropio.usoCodigo}"/></p>
<c:url var="urlRegistroPropio" value="/consulta-registro"><c:param name="edicion" value="${detalle.idNombre}"/></c:url>
<a href="${urlRegistroPropio}">Consultar mi registro</a></section></c:if>
</c:if>
</main><script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
<script src="${raiz}/js/consultas-selectores.js"></script></body></html>
