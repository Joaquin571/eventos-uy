<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<header>
    <nav class="navbar navbar-expand-md navbar-dark navbar-movil sticky-top">
        <div class="container-fluid px-3">
            <a class="navbar-brand fw-bold" href="${raiz}/movil/eventos">eventos<span class="marca-uy">.uy</span></a>
            <button class="navbar-toggler movil-toque" type="button" data-bs-toggle="collapse"
                    data-bs-target="#menuMovil" aria-controls="menuMovil"
                    aria-expanded="false" aria-label="Abrir menú">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="menuMovil">
                <div class="movil-usuario">
                    <strong><c:out value="${sessionScope.usuario.nombre}" /></strong>
                    <small><c:out value="${sessionScope.usuario.nickname}" /> · Asistente</small>
                </div>
                <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                        <a class="nav-link movil-toque ${seccionMovil eq 'eventos' ? 'active' : ''}"
                           href="${raiz}/movil/eventos">Consulta de edición</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link movil-toque ${seccionMovil eq 'registros' ? 'active' : ''}"
                           href="${raiz}/movil/registros">Mis registros</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link movil-toque ${seccionMovil eq 'asistencia' ? 'active' : ''}"
                           href="${raiz}/movil/registros?modo=asistencia">Registrar asistencia</a>
                    </li>
                    <li class="nav-item">
                        <form method="post" action="${raiz}/movil/logout" class="m-0">
                            <jsp:include page="/WEB-INF/views/fragmentos/csrf.jsp"/>
                            <button type="submit" class="nav-link movil-toque movil-salir w-100 text-start">Cerrar sesión</button>
                        </form>
                    </li>
                </ul>
            </div>
        </div>
    </nav>
</header>
