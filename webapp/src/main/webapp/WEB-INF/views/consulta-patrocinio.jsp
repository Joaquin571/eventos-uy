<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="datatypes.DtPatrocinio"%>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Consulta Patrocinio - eventos.uy</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body class="bg-light">

<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp" />

<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-12 col-md-8 col-lg-6">
            <div class="card shadow-sm">
                <div class="card-body p-4">
                    <h1 class="h3 mb-4">Consulta de Patrocinio</h1>

                    <% String error = (String) request.getAttribute("error"); %>
                    <% if (error != null) { %>
                        <div class="alert alert-warning" role="alert">
                            <%= error %>
                        </div>
                    <% } %>

                    <% DtPatrocinio pat = (DtPatrocinio) request.getAttribute("patrocinio"); %>
                    <% Boolean esDuenio = (Boolean) request.getAttribute("esOrganizadorDuenio"); %>

                    <% if (pat != null) { %>
                        <h2 class="h5 mb-3">Detalle del patrocinio</h2>

                        <div class="mb-3">
                            <strong>Edición:</strong> <%= pat.getNombreEdicion() %>
                        </div>
                        <div class="mb-3">
                            <strong>Institución:</strong> <%= pat.getNombreInstituto() %>
                        </div>
                        <div class="mb-3">
                            <strong>Nivel:</strong> <%= pat.getNivel() %>
                        </div>
                        <div class="mb-3">
                            <strong>Aporte:</strong> $<%= pat.getMontoAporte() %>
                        </div>
                        <div class="mb-3">
                            <strong>Tipo de registro:</strong> <%= pat.getNombreTipoRegistro() %>
                        </div>
                        <div class="mb-3">
                            <strong>Cantidad de registros gratuitos:</strong> <%= pat.getCantRegistrosGrat() %>
                        </div>

                        <% if (Boolean.TRUE.equals(esDuenio)) { %>
                            <div class="mb-3 alert alert-info">
                                <strong>Código de patrocinio:</strong> <%= pat.getCodigoPatrocinio() %>
                            </div>
                        <% } %>

                    <% } else { %>
                        <p class="text-muted">Seleccione una edición e institución en los filtros para consultar su información.</p>
                    <% } %>

                    <div class="d-flex justify-content-end gap-2 mt-4">
                        <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Aceptar</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>

<script src="${pageContext.request.contextPath}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>