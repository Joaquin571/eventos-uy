<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<c:url var="urlImagen" value="/imagen">
    <c:param name="tipo" value="usuario" />
    <c:param name="id" value="${perfil.nickname}" />
</c:url>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Modificar mis datos - eventos.uy</title>
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

                    <h1 class="h3 fw-bold mb-4">Modificar mis datos</h1>

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger"><c:out value="${error}" /></div>
                    </c:if>

                    <form id="formModificarUsuario" method="post"
                          action="${raiz}/modificar-usuario" enctype="multipart/form-data">

                        <div class="mb-3">
                            <label class="form-label fw-bold">Nickname</label>
                            <input type="text" class="form-control" disabled
                                   value="<c:out value='${perfil.nickname}' />">
                        </div>

                        <div class="mb-3">
                            <label class="form-label fw-bold">Correo electrónico</label>
                            <input type="text" class="form-control" disabled
                                   value="<c:out value='${perfil.correoElectronico}' />">
                        </div>

                        <div class="mb-3">
                            <label for="nombre" class="form-label fw-bold">Nombre *</label>
                            <input type="text" id="nombre" name="nombre" class="form-control" required
                                   value="<c:out value='${form.nombre}' />">
                        </div>

                        <c:if test="${esAsistente}">
                            <div class="mb-3">
                                <label for="apellido" class="form-label fw-bold">Apellido *</label>
                                <input type="text" id="apellido" name="apellido" class="form-control" required
                                       value="<c:out value='${form.apellido}' />">
                            </div>
                            <div class="mb-3">
                                <label for="fechaNac" class="form-label fw-bold">Fecha de nacimiento *</label>
                                <input type="date" id="fechaNac" name="fechaNac" class="form-control" required
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
                        </c:if>

                        <c:if test="${esOrganizador}">
                            <div class="mb-3">
                                <label for="descripcion" class="form-label fw-bold">Descripción general *</label>
                                <textarea id="descripcion" name="descripcion" class="form-control"
                                          rows="3" required><c:out value="${form.descripcion}" /></textarea>
                            </div>
                            <div class="mb-3">
                                <label for="sitioWeb" class="form-label fw-bold">Sitio web</label>
                                <input type="url" id="sitioWeb" name="sitioWeb" class="form-control"
                                       placeholder="https://ejemplo.com"
                                       value="<c:out value='${form.sitioWeb}' />">
                            </div>
                        </c:if>

                        <div class="mb-3">
                            <label class="form-label fw-bold d-block">Imagen actual</label>
                            <img src="${urlImagen}" alt="Imagen de perfil" class="rounded-circle mb-2"
                                 style="width: 96px; height: 96px; object-fit: cover;">
                            <input type="file" id="imagen" name="imagen" class="form-control"
                                   accept="image/png,image/jpeg,image/gif,image/webp">
                            <div class="form-text">Opcional. Si elegís una, reemplaza la actual. Hasta 2 MB.</div>
                        </div>

                        <hr>

                        <p class="text-muted small mb-2">Dejá la contraseña vacía si no querés cambiarla.</p>

                        <div class="mb-3">
                            <label for="contrasenia" class="form-label fw-bold">Nueva contraseña</label>
                            <input type="password" id="contrasenia" name="contrasenia" class="form-control">
                        </div>

                        <div class="mb-4">
                            <label for="confirmacion" class="form-label fw-bold">Confirmar nueva contraseña</label>
                            <input type="password" id="confirmacion" name="confirmacion" class="form-control">
                        </div>

                        <c:url var="urlPerfil" value="/usuario">
                            <c:param name="nick" value="${perfil.nickname}" />
                        </c:url>

                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary fw-bold">Guardar cambios</button>
                            <a href="${urlPerfil}" class="btn btn-outline-secondary">Cancelar</a>
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
        const contrasenia = document.getElementById("contrasenia");
        const confirmacion = document.getElementById("confirmacion");
        const fechaNac = document.getElementById("fechaNac");

        if (fechaNac) {
            fechaNac.max = new Date().toISOString().split("T")[0];
        }

        confirmacion.addEventListener("input", function () {
            confirmacion.setCustomValidity("");
        });

        document.getElementById("formModificarUsuario").addEventListener("submit", function (e) {
            if (contrasenia.value !== confirmacion.value) {
                e.preventDefault();
                confirmacion.setCustomValidity("Las contraseñas no coinciden.");
                confirmacion.reportValidity();
            }
        });
    })();
</script>
</body>
</html>