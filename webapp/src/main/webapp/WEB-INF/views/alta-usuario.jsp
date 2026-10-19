<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registrarse - eventos.uy</title>
    <link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="${raiz}/css/styles.css">
</head>
<body class="bg-light">

<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp" />

<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-12 col-md-8 col-lg-6">
            <div class="card border-0 shadow-sm rounded-3">
                <div class="card-body p-4">

                    <h1 class="h3 fw-bold mb-4">Registrarse</h1>

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger"><c:out value="${error}" /></div>
                    </c:if>

                    <form id="formAltaUsuario" method="post" action="${raiz}/alta-usuario">

                        <div class="mb-3">
                            <label for="tipo" class="form-label fw-bold">Registrarme como *</label>
                            <select id="tipo" name="tipo" class="form-select">
                                <option value="asistente" ${form.tipo ne 'organizador' ? 'selected' : ''}>Asistente</option>
                                <option value="organizador" ${form.tipo eq 'organizador' ? 'selected' : ''}>Organizador</option>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label for="nickname" class="form-label fw-bold">Nickname *</label>
                            <input type="text" id="nickname" name="nickname" class="form-control" required
                                   value="<c:out value='${form.nickname}' />">
                        </div>

                        <div class="mb-3">
                            <label for="nombre" class="form-label fw-bold">Nombre *</label>
                            <input type="text" id="nombre" name="nombre" class="form-control" required
                                   value="<c:out value='${form.nombre}' />">
                        </div>

                        <div class="mb-3">
                            <label for="correo" class="form-label fw-bold">Correo electrónico *</label>
                            <input type="email" id="correo" name="correo" class="form-control" required
                                   value="<c:out value='${form.correo}' />">
                        </div>

                        <div id="camposAsistente">
                            <div class="mb-3">
                                <label for="apellido" class="form-label fw-bold">Apellido *</label>
                                <input type="text" id="apellido" name="apellido" class="form-control"
                                       value="<c:out value='${form.apellido}' />">
                            </div>
                            <div class="mb-3">
                                <label for="fechaNac" class="form-label fw-bold">Fecha de nacimiento *</label>
                                <input type="date" id="fechaNac" name="fechaNac" class="form-control"
                                       value="<c:out value='${form.fechaNac}' />">
                            </div>
                            <div class="mb-3">
                                <label for="institucion" class="form-label fw-bold">Institución</label>
                                <select id="institucion" name="institucion" class="form-select">
                                    <option value="">Ninguna</option>
                                    <c:forEach var="institucion" items="${instituciones}">
                                        <option value="<c:out value='${institucion.nombre}' />"
                                                ${form.institucion eq institucion.nombre ? 'selected' : ''}>
                                            <c:out value="${institucion.nombre}" />
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>

                        <div id="camposOrganizador" class="d-none">
                            <div class="mb-3">
                                <label for="descripcion" class="form-label fw-bold">Descripción general *</label>
                                <textarea id="descripcion" name="descripcion" class="form-control"
                                          rows="3"><c:out value="${form.descripcion}" /></textarea>
                            </div>
                            <div class="mb-3">
                                <label for="sitioWeb" class="form-label fw-bold">Sitio web</label>
                                <input type="url" id="sitioWeb" name="sitioWeb" class="form-control"
                                       placeholder="https://ejemplo.com"
                                       value="<c:out value='${form.sitioWeb}' />">
                            </div>
                        </div>

                        <div class="mb-3">
                            <label for="contrasenia" class="form-label fw-bold">Contraseña *</label>
                            <input type="password" id="contrasenia" name="contrasenia"
                                   class="form-control" required>
                        </div>

                        <div class="mb-4">
                            <label for="confirmacion" class="form-label fw-bold">Confirmar contraseña *</label>
                            <input type="password" id="confirmacion" name="confirmacion"
                                   class="form-control" required>
                        </div>

                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary fw-bold">Registrarme</button>
                            <a href="${raiz}/" class="btn btn-outline-secondary">Cancelar</a>
                        </div>
                    </form>

                </div>
            </div>
        </div>
    </div>
</main>

<script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
<script>
    (function () {
        const tipo = document.getElementById("tipo");
        const camposAsistente = document.getElementById("camposAsistente");
        const camposOrganizador = document.getElementById("camposOrganizador");
        const confirmacion = document.getElementById("confirmacion");
        const fechaNac = document.getElementById("fechaNac");

        fechaNac.max = new Date().toISOString().split("T")[0];

        function alternarCampos() {
            const esOrganizador = tipo.value === "organizador";

            camposAsistente.classList.toggle("d-none", esOrganizador);
            camposOrganizador.classList.toggle("d-none", !esOrganizador);

            document.getElementById("apellido").required = !esOrganizador;
            fechaNac.required = !esOrganizador;
            document.getElementById("descripcion").required = esOrganizador;
        }

        tipo.addEventListener("change", alternarCampos);
        alternarCampos();

        confirmacion.addEventListener("input", function () {
            confirmacion.setCustomValidity("");
        });

        document.getElementById("formAltaUsuario").addEventListener("submit", function (e) {
            if (document.getElementById("contrasenia").value !== confirmacion.value) {
                e.preventDefault();
                confirmacion.setCustomValidity("Las contraseñas no coinciden.");
                confirmacion.reportValidity();
            }
        });
    })();
</script>
</body>
</html>