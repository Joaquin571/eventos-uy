<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registro a Edición de Evento - eventos.uy</title>
    <link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="${raiz}/css/styles.css">
</head>
<body class="bg-light">

<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp" />

<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-12 col-md-10 col-lg-8">

            <h1 class="h3 fw-bold mb-4">Registro a Edición de Evento</h1>

            <c:if test="${not empty error}">
                <div class="alert alert-danger"><c:out value="${error}" /></div>
            </c:if>

            <c:if test="${not empty advertencia}">
                <div class="alert alert-warning"><c:out value="${advertencia}" /></div>
            </c:if>

            <%-- Paso 1: evento --%>
            <div class="card border-0 shadow-sm rounded-3 mb-4">
                <div class="card-body p-4">
                    <h2 class="h5 mb-3">1. Evento</h2>

                    <form method="get" action="${raiz}/registro-edicion" class="d-flex gap-2">
                        <select name="evento" id="evento" class="form-select" required
                                onchange="this.form.submit()">
                            <option value="">Seleccione un evento</option>
                            <c:forEach var="e" items="${eventos}">
                                <option value="<c:out value='${e.nombre}' />"
                                        ${e.nombre eq eventoElegido ? 'selected' : ''}>
                                    <c:out value="${e.nombre}" />
                                </option>
                            </c:forEach>
                        </select>
                        <button type="submit" class="btn btn-outline-primary">Ver ediciones</button>
                    </form>
                </div>
            </div>

            <%-- Paso 2: edición (solo las Aceptada) --%>
            <c:if test="${not empty eventoElegido}">
                <div class="card border-0 shadow-sm rounded-3 mb-4">
                    <div class="card-body p-4">
                        <h2 class="h5 mb-3">2. Edición</h2>

                        <c:choose>
                            <c:when test="${empty ediciones}">
                                <div class="alert alert-info mb-0">
                                    Este evento no tiene ediciones abiertas al registro.
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="list-group">
                                    <c:forEach var="ed" items="${ediciones}">
                                        <c:url var="urlEdicion" value="/registro-edicion">
                                            <c:param name="evento" value="${eventoElegido}" />
                                            <c:param name="edicion" value="${ed.idNombre}" />
                                        </c:url>
                                        <a href="${urlEdicion}"
                                           class="list-group-item list-group-item-action ${ed.idNombre eq edicionElegida ? 'active' : ''}">
                                            <strong><c:out value="${ed.idNombre}" /></strong>
                                            <small class="d-block">
                                                <c:out value="${ed.ciudad}" />, <c:out value="${ed.pais}" />
                                            </small>
                                        </a>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:if>

            <%-- Paso 3: datos de la edición, tipo de registro y modalidad --%>
            <c:if test="${not empty detalle}">
                <div class="card border-0 shadow-sm rounded-3 mb-4">
                    <div class="card-body p-4">
                        <h2 class="h5 mb-3">3. Datos de la edición</h2>

                        <dl class="row mb-0">
                            <dt class="col-sm-4">Edición</dt>
                            <dd class="col-sm-8">
                                <c:url var="urlDetalleEdicion" value="/edicion">
                                    <c:param name="nombre" value="${detalle.idNombre}" />
                                </c:url>
                                <a href="${urlDetalleEdicion}"><c:out value="${detalle.idNombre}" /></a>
                            </dd>

                            <dt class="col-sm-4">Sigla</dt>
                            <dd class="col-sm-8"><c:out value="${detalle.sigla}" /></dd>

                            <dt class="col-sm-4">Ciudad</dt>
                            <dd class="col-sm-8"><c:out value="${detalle.ciudad}" /></dd>

                            <dt class="col-sm-4">País</dt>
                            <dd class="col-sm-8"><c:out value="${detalle.pais}" /></dd>

                            <dt class="col-sm-4">Inicio</dt>
                            <dd class="col-sm-8"><c:out value="${fechaInicio}" /></dd>

                            <dt class="col-sm-4">Fin</dt>
                            <dd class="col-sm-8"><c:out value="${fechaFin}" /></dd>
                        </dl>
                    </div>
                </div>

                <div class="card border-0 shadow-sm rounded-3">
                    <div class="card-body p-4">
                        <h2 class="h5 mb-3">4. Tipo de registro</h2>

                        <c:choose>
                            <c:when test="${empty tipos}">
                                <div class="alert alert-info mb-0">
                                    Esta edición todavía no tiene tipos de registro.
                                </div>
                            </c:when>
                            <c:otherwise>
                                <form id="formRegistro" method="post" action="${raiz}/registro-edicion">
                                    <input type="hidden" name="evento" value="<c:out value='${eventoElegido}' />">
                                    <input type="hidden" name="edicion" value="<c:out value='${edicionElegida}' />">

                                    <div class="list-group mb-4">
                                        <c:forEach var="t" items="${tipos}">
                                            <label class="list-group-item d-flex gap-3">
                                                <input class="form-check-input flex-shrink-0 mt-1" type="radio"
                                                       name="tipo" required
                                                       value="<c:out value='${t.idNombre}' />"
                                                       ${t.idNombre eq tipoElegido ? 'checked' : ''}>
                                                <span class="flex-grow-1">
                                                    <strong><c:out value="${t.idNombre}" /></strong>
                                                    <small class="text-muted d-block">
                                                        <c:out value="${t.descripcion}" />
                                                    </small>
                                                    <small class="text-muted d-block">
                                                        Costo: $<c:out value="${costos[t.idNombre]}" /> ·
                                                        Cupo: <c:out value="${t.cupo}" />
                                                    </small>
                                                </span>
                                            </label>
                                        </c:forEach>
                                    </div>

                                    <h2 class="h5 mb-3">5. Modalidad de registro</h2>

                                    <div class="mb-3">
                                        <div class="form-check form-check-inline">
                                            <input class="form-check-input" type="radio" name="modalidad"
                                                   id="modalidadGeneral" value="general"
                                                   ${modalidad ne 'codigo' ? 'checked' : ''}>
                                            <label class="form-check-label" for="modalidadGeneral">General</label>
                                        </div>
                                        <div class="form-check form-check-inline">
                                            <input class="form-check-input" type="radio" name="modalidad"
                                                   id="modalidadCodigo" value="codigo"
                                                   ${modalidad eq 'codigo' ? 'checked' : ''}>
                                            <label class="form-check-label" for="modalidadCodigo">
                                                Con código de patrocinio
                                            </label>
                                        </div>
                                    </div>

                                    <div class="mb-4" id="contenedorCodigo">
                                        <label for="codigo" class="form-label fw-bold">Código de patrocinio</label>
                                        <input type="text" id="codigo" name="codigo" class="form-control"
                                               autocomplete="off" placeholder="Ingresá el código"
                                               value="<c:out value='${codigo}' />">
                                        <div class="form-text">
                                            El código tiene que corresponder a esta edición, al tipo de registro
                                            elegido y a tu institución. Con un código válido el costo es cero.
                                        </div>
                                    </div>

                                    <div class="d-flex justify-content-end gap-2">
                                        <a href="${raiz}/" class="btn btn-outline-secondary">Cancelar</a>
                                        <button type="submit" class="btn btn-primary fw-bold">Registrarme</button>
                                    </div>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:if>

        </div>
    </div>
</main>

<script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
<script>
    // El campo del código solo se muestra (y se exige) cuando el registro es con código.
    (function () {
        var general = document.getElementById("modalidadGeneral");
        var conCodigo = document.getElementById("modalidadCodigo");
        var contenedor = document.getElementById("contenedorCodigo");
        var codigo = document.getElementById("codigo");

        if (!general || !conCodigo || !contenedor || !codigo) {
            return;
        }

        function actualizar() {
            var usaCodigo = conCodigo.checked;
            contenedor.classList.toggle("d-none", !usaCodigo);
            codigo.required = usaCodigo;
        }

        general.addEventListener("change", actualizar);
        conCodigo.addEventListener("change", actualizar);
        actualizar();
    })();
</script>
</body>
</html>
