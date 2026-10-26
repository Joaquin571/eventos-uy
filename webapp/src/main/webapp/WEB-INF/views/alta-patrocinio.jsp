<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.Collection, datatypes.DtEvento, datatypes.DtInstitucion"%>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Alta Patrocinio - eventos.uy</title>
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
                    <h1 class="h3 mb-4">Alta de Patrocinio</h1>

                    <% String error = (String) request.getAttribute("error"); %>
                    <% if (error != null) { %>
                        <div class="alert alert-danger" role="alert">
                            <%= error %>
                        </div>
                    <% } %>

                    <form action="${pageContext.request.contextPath}/alta-patrocinio" method="post">
                        <div class="mb-3">
                            <label for="evento" class="form-label">Evento</label>
                            <select class="form-select" id="evento" name="evento" required>
                                <option value="">Seleccione un evento</option>
                                <%
                                    Collection<DtEvento> eventos = (Collection<DtEvento>) request.getAttribute("eventos");
                                    if (eventos != null) {
                                        for (DtEvento ev : eventos) {
                                %>
                                    <option value="<%= ev.getNombre() %>"><%= ev.getNombre() %></option>
                                <%      }
                                    }
                                %>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label for="edicion" class="form-label">Edición</label>
                            <select class="form-select" id="edicion" name="edicion" required>
                                <option value="">Seleccione una edición</option>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label for="institucion" class="form-label">Institución</label>
                            <select class="form-select" id="institucion" name="institucion" required>
                                <option value="">Seleccione una institución</option>
                                <%
                                    Collection<DtInstitucion> insts = (Collection<DtInstitucion>) request.getAttribute("instituciones");
                                    if (insts != null) {
                                        for (DtInstitucion ins : insts) {
                                %>
                                    <option value="<%= ins.getNombre() %>"><%= ins.getNombre() %></option>
                                <%      }
                                    }
                                %>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label for="nivel" class="form-label">Nivel de patrocinio</label>
                            <select class="form-select" id="nivel" name="nivel" required>
                                <option value="">Seleccione un nivel</option>
                                <option value="PLATINO">Platino</option>
                                <option value="ORO">Oro</option>
                                <option value="PLATA">Plata</option>
                                <option value="BRONCE">Bronce</option>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label for="tipoRegistro" class="form-label">Tipo de Registro</label>
                            <select class="form-select" id="tipoRegistro" name="tipoRegistro" required>
                                <option value="">Seleccione un tipo de registro</option>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label for="aporte" class="form-label">Aporte económico</label>
                            <input type="number" class="form-control" id="aporte" name="aporte" min="0" step="0.01" required>
                        </div>

                        <div class="mb-3">
                            <label for="registrograt" class="form-label">Cantidad de registros gratuitos</label>
                            <input type="number" class="form-control" id="registrograt" name="registrograt" min="0" step="1" required>
                        </div>

                        <div class="mb-4">
                            <label for="codigo" class="form-label">Código de patrocinio</label>
                            <input type="text" class="form-control" id="codigo" name="codigo" required>
                        </div>

                        <div class="d-flex justify-content-end gap-2">
                            <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">Cancelar</a>
                            <button type="submit" class="btn btn-primary">Dar de alta</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</main>

<script src="${pageContext.request.contextPath}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/js/selects.js"></script>
</body>
</html>