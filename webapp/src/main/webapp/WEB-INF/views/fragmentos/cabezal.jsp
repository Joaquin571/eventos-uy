<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<c:if test="${not empty sessionScope.usuario}">
    <c:url var="urlPerfil" value="/usuario">
        <c:param name="nick" value="${sessionScope.usuario.nickname}" />
    </c:url>
</c:if>

<header>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm">
        <div class="container">
            <a class="navbar-brand fw-bold" href="${raiz}/">eventos.uy</a>

            <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                    data-bs-target="#navbarPrincipal" aria-controls="navbarPrincipal"
                    aria-expanded="false" aria-label="Abrir menú">
                <span class="navbar-toggler-icon"></span>
            </button>

            <div class="collapse navbar-collapse" id="navbarPrincipal">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item"><a class="nav-link" href="${raiz}/">Eventos</a></li>
                    <li class="nav-item"><a class="nav-link" href="${raiz}/usuarios">Usuarios</a></li>

                    <c:if test="${rol eq 'organizador'}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" role="button"
                               data-bs-toggle="dropdown">Gestión</a>
                            <ul class="dropdown-menu">
                                <li><a class="dropdown-item" href="${raiz}/alta-evento">Alta Evento</a></li>
                                <li><a class="dropdown-item" href="${raiz}/alta-edicion">Alta Edición</a></li>
                                <li><a class="dropdown-item" href="${raiz}/alta-tipo-registro">Alta Tipo Registro</a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li><a class="dropdown-item" href="${raiz}/alta-institucion">Alta Institución</a></li>
                                <li><a class="dropdown-item" href="${raiz}/alta-patrocinio">Alta Patrocinio</a></li>
                            </ul>
                        </li>
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" role="button"
                               data-bs-toggle="dropdown">Consultas</a>
                            <ul class="dropdown-menu">
                                <li><a class="dropdown-item" href="${raiz}/consulta-registro">Consulta Registro</a></li>
                                <li><a class="dropdown-item" href="${raiz}/consulta-patrocinio">Consulta Patrocinio</a></li>
                            </ul>
                        </li>
                    </c:if>

                    <c:if test="${rol eq 'asistente'}">
                        <li class="nav-item"><a class="nav-link" href="${raiz}/registro-edicion">Registrarme a edición</a></li>
                        <li class="nav-item"><a class="nav-link" href="${raiz}/consulta-registro">Mis registros</a></li>
                    </c:if>
                </ul>

                <c:choose>
                    <c:when test="${not empty sessionScope.usuario}">
                        <div class="dropdown">
                            <button class="btn btn-light dropdown-toggle" type="button"
                                    data-bs-toggle="dropdown">
                                <c:out value="${sessionScope.usuario.nombre}" />
                            </button>
                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><a class="dropdown-item" href="${urlPerfil}">Mi perfil</a></li>
                                <li><a class="dropdown-item" href="${raiz}/modificar-usuario">Modificar mis datos</a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li>
                                    <form method="post" action="${raiz}/logout" class="m-0">
                                        <button type="submit" class="dropdown-item text-danger">Cerrar sesión</button>
                                    </form>
                                </li>
                            </ul>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="d-flex gap-2">
                            <a class="btn btn-outline-light" href="${raiz}/login">Iniciar sesión</a>
                            <a class="btn btn-light text-primary" href="${raiz}/alta-usuario">Registrarse</a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </nav>

    <c:if test="${not empty categorias}">
        <section class="bg-white border-bottom">
            <div class="container py-2 d-flex flex-wrap gap-2 align-items-center">
                <span class="text-muted small me-2">Categorías</span>
                <c:forEach var="categoria" items="${categorias}">
                    <c:url var="urlCategoria" value="/inicio">
                        <c:param name="categoria" value="${categoria.idNombre}" />
                    </c:url>
                    <a href="${urlCategoria}" class="btn btn-sm btn-outline-secondary">
                        <c:out value="${categoria.idNombre}" />
                    </a>
                </c:forEach>
            </div>
        </section>
    </c:if>
</header>