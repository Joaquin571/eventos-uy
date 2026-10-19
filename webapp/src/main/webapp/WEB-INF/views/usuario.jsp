<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${perfil.nombre}" /> - eventos.uy</title>
    <link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="${raiz}/css/styles.css">
</head>
<body class="bg-light">

<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp" />

<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-12 col-lg-8">

            <div class="card border-0 shadow-sm mb-4">
                <div class="card-body p-4">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <h1 class="h3 mb-1">
                                <c:out value="${perfil.nombre}" />
                                <c:if test="${esAsistente}"> <c:out value="${perfil.apellido}" /></c:if>
                            </h1>
                            <span class="badge bg-secondary">
                                <c:choose>
                                    <c:when test="${esOrganizador}">Organizador</c:when>
                                    <c:otherwise>Asistente</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <c:if test="${esPropio}">
                            <a href="${raiz}/modificar-usuario" class="btn btn-outline-primary">Editar perfil</a>
                        </c:if>
                    </div>

                    <hr>

                    <dl class="row mb-0">
                        <dt class="col-sm-4">Nickname</dt>
                        <dd class="col-sm-8"><c:out value="${perfil.nickname}" /></dd>

                        <dt class="col-sm-4">Correo electrónico</dt>
                        <dd class="col-sm-8"><c:out value="${perfil.correoElectronico}" /></dd>

                        <c:if test="${esAsistente}">
                            <dt class="col-sm-4">Fecha de nacimiento</dt>
                            <dd class="col-sm-8"><c:out value="${fechaNacimiento}" /></dd>

                            <dt class="col-sm-4">Institución</dt>
                            <dd class="col-sm-8">
                                <c:out value="${empty perfil.nombreInstitucion ? 'Ninguna' : perfil.nombreInstitucion}" />
                            </dd>
                        </c:if>

                        <c:if test="${esOrganizador}">
                            <dt class="col-sm-4">Descripción</dt>
                            <dd class="col-sm-8"><c:out value="${perfil.descripcion}" /></dd>

                            <c:if test="${not empty perfil.sitioWeb}">
                                <dt class="col-sm-4">Sitio web</dt>
                                <dd class="col-sm-8"><c:out value="${perfil.sitioWeb}" /></dd>
                            </c:if>
                        </c:if>
                    </dl>
                </div>
            </div>

            <c:if test="${esOrganizador}">
                <div class="card border-0 shadow-sm">
                    <div class="card-body p-4">
                        <h2 class="h5 mb-3">Ediciones</h2>

                        <c:if test="${empty ediciones}">
                            <p class="text-muted mb-0">No hay ediciones para mostrar.</p>
                        </c:if>

                        <div class="list-group">
                            <c:forEach var="edicion" items="${ediciones}">
                                <c:url var="urlEdicion" value="/edicion">
                                    <c:param name="nombre" value="${edicion.idNombre}" />
                                </c:url>
                                <a href="${urlEdicion}"
                                   class="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
                                    <span>
                                        <strong><c:out value="${edicion.idNombre}" /></strong>
                                        <c:if test="${edicion.estado.descripcion ne 'Aceptada'}">
                                            <small class="text-muted d-block">Visible solo para el organizador</small>
                                        </c:if>
                                    </span>
                                    <c:choose>
                                        <c:when test="${edicion.estado.descripcion eq 'Aceptada'}">
                                            <span class="badge bg-success">Aceptada</span>
                                        </c:when>
                                        <c:when test="${edicion.estado.descripcion eq 'Ingresada'}">
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
            </c:if>

            <c:if test="${esAsistente and esPropio}">
                <div class="card border-0 shadow-sm">
                    <div class="card-body p-4">
                        <h2 class="h5 mb-3">Mis registros a ediciones</h2>

                        <c:if test="${empty registros}">
                            <p class="text-muted mb-0">Todavía no te registraste a ninguna edición.</p>
                        </c:if>

                        <div class="list-group">
                            <c:forEach var="registro" items="${registros}">
                                <c:url var="urlRegistro" value="/consulta-registro">
                                    <c:param name="edicion" value="${registro.nombreEdicion}" />
                                </c:url>
                                <a href="${urlRegistro}" class="list-group-item list-group-item-action">
                                    <strong><c:out value="${registro.nombreEdicion}" /></strong>
                                    <small class="text-muted d-block">
                                        <c:out value="${registro.nombreTipoRegistro}" /> ·
                                        $<c:out value="${registro.costo}" /> ·
                                        <c:out value="${registro.fechaRegistro}" />
                                    </small>
                                </a>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </c:if>

        </div>
    </div>
</main>

<script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>