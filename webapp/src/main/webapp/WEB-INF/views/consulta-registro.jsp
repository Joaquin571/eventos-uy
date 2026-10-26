<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Consulta de Registro - eventos.uy</title>
    <link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="${raiz}/css/styles.css">
</head>
<body class="bg-light">

<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp" />

<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-12 col-md-10 col-lg-8">

            <h1 class="h3 fw-bold mb-4">Consulta de Registro</h1>

            <c:if test="${registrado and not esOrganizador}">
                <div class="alert alert-success">Te registraste correctamente a la edición.</div>
            </c:if>

            <c:if test="${not empty advertencia}">
                <div class="alert alert-warning"><c:out value="${advertencia}" /></div>
            </c:if>

            <c:choose>

                <%-- ORGANIZADOR: sus ediciones y los asistentes registrados --%>
                <c:when test="${esOrganizador}">
                    <div class="card border-0 shadow-sm rounded-3 mb-4">
                        <div class="card-body p-4">
                            <h2 class="h5 mb-3">Mis ediciones</h2>

                            <c:if test="${empty ediciones}">
                                <div class="alert alert-info mb-0">Todavía no tenés ediciones a tu cargo.</div>
                            </c:if>

                            <div class="list-group">
                                <c:forEach var="ed" items="${ediciones}">
                                    <c:url var="urlEdicion" value="/consulta-registro">
                                        <c:param name="edicion" value="${ed.idNombre}" />
                                    </c:url>
                                    <a href="${urlEdicion}"
                                       class="list-group-item list-group-item-action d-flex justify-content-between align-items-center ${ed.idNombre eq edicionElegida ? 'active' : ''}">
                                        <strong><c:out value="${ed.idNombre}" /></strong>
                                        <c:choose>
                                            <c:when test="${ed.estado.descripcion eq 'Aceptada'}">
                                                <span class="badge bg-success">Aceptada</span>
                                            </c:when>
                                            <c:when test="${ed.estado.descripcion eq 'Ingresada'}">
                                                <span class="badge bg-warning text-dark">Ingresada</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-danger">Rechazada</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </a>
                                </c:forEach>
                            </div>
                        </div>
                    </div>

                    <c:if test="${not empty edicionElegida}">
                        <div class="card border-0 shadow-sm rounded-3 mb-4">
                            <div class="card-body p-4">
                                <h2 class="h5 mb-3">
                                    Asistentes registrados en <c:out value="${edicionElegida}" />
                                </h2>

                                <c:if test="${empty registros}">
                                    <div class="alert alert-info mb-0">
                                        Esta edición todavía no tiene asistentes registrados.
                                    </div>
                                </c:if>

                                <div class="list-group">
                                    <c:forEach var="r" items="${registros}">
                                        <c:url var="urlAsistente" value="/consulta-registro">
                                            <c:param name="edicion" value="${edicionElegida}" />
                                            <c:param name="asistente" value="${r.nickname}" />
                                        </c:url>
                                        <a href="${urlAsistente}"
                                           class="list-group-item list-group-item-action ${r.nickname eq asistenteElegido ? 'active' : ''}">
                                            <strong><c:out value="${r.nickname}" /></strong>
                                            <small class="d-block">
                                                <c:out value="${r.nombreAsistente}" />
                                            </small>
                                        </a>
                                    </c:forEach>
                                </div>
                            </div>
                        </div>
                    </c:if>
                </c:when>

                <%-- ASISTENTE: las ediciones a las que se registró --%>
                <c:otherwise>
                    <div class="card border-0 shadow-sm rounded-3 mb-4">
                        <div class="card-body p-4">
                            <h2 class="h5 mb-3">Mis registros</h2>

                            <c:if test="${empty registros}">
                                <div class="alert alert-info mb-0">
                                    Todavía no te registraste a ninguna edición.
                                    <a href="${raiz}/registro-edicion" class="alert-link">Registrarme a una edición</a>
                                </div>
                            </c:if>

                            <div class="list-group">
                                <c:forEach var="r" items="${registros}">
                                    <c:url var="urlRegistro" value="/consulta-registro">
                                        <c:param name="edicion" value="${r.nombreEdicion}" />
                                    </c:url>
                                    <a href="${urlRegistro}"
                                       class="list-group-item list-group-item-action ${r.nombreEdicion eq edicionElegida ? 'active' : ''}">
                                        <strong><c:out value="${r.nombreEdicion}" /></strong>
                                        <small class="d-block">
                                            <c:out value="${r.nombreTipoRegistro}" /> ·
                                            <c:out value="${r.fecha}" />
                                        </small>
                                    </a>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>

            <%-- Detalle del registro elegido --%>
            <c:if test="${not empty detalle}">
                <div class="card border-0 shadow-sm rounded-3 mb-4">
                    <div class="card-body p-4">
                        <h2 class="h5 mb-3">Detalle del registro</h2>

                        <dl class="row mb-0">
                            <c:if test="${esOrganizador}">
                                <dt class="col-sm-5">Asistente</dt>
                                <dd class="col-sm-7">
                                    <c:url var="urlPerfil" value="/usuario">
                                        <c:param name="nick" value="${detalle.nickname}" />
                                    </c:url>
                                    <a href="${urlPerfil}"><c:out value="${detalle.nickname}" /></a>
                                    <c:if test="${not empty detalle.nombreAsistente}">
                                        (<c:out value="${detalle.nombreAsistente}" />)
                                    </c:if>
                                </dd>
                            </c:if>

                            <dt class="col-sm-5">Edición</dt>
                            <dd class="col-sm-7">
                                <c:url var="urlEdicionDetalle" value="/edicion">
                                    <c:param name="nombre" value="${detalle.nombreEdicion}" />
                                </c:url>
                                <a href="${urlEdicionDetalle}"><c:out value="${detalle.nombreEdicion}" /></a>
                            </dd>

                            <dt class="col-sm-5">Tipo de registro</dt>
                            <dd class="col-sm-7"><c:out value="${detalle.nombreTipoRegistro}" /></dd>

                            <dt class="col-sm-5">Fecha de registro</dt>
                            <dd class="col-sm-7"><c:out value="${detalle.fecha}" /></dd>

                            <dt class="col-sm-5">Costo</dt>
                            <dd class="col-sm-7">$<c:out value="${detalle.costo}" /></dd>

                            <dt class="col-sm-5">Utilizó código de patrocinio</dt>
                            <dd class="col-sm-7"><c:out value="${detalle.usoCodigo}" /></dd>
                        </dl>
                    </div>
                </div>
            </c:if>

            <a href="${raiz}/" class="btn btn-primary">Aceptar</a>
        </div>
    </div>
</main>

<script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>
