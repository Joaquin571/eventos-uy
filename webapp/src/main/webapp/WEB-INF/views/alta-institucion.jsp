<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Alta de Institución - eventos.uy</title>
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

                    <h1 class="h3 fw-bold mb-4">Alta de Institución</h1>

                    <c:if test="${not empty param.creada}">
                        <div class="alert alert-success">
                            Institución «<c:out value="${param.creada}" />» creada correctamente.
                        </div>
                    </c:if>

                    <c:if test="${param.imagen eq 'error'}">
                        <div class="alert alert-warning">
                            La institución se creó, pero no se pudo guardar la imagen.
                        </div>
                    </c:if>

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger"><c:out value="${error}" /></div>
                    </c:if>

                    <form id="formAltaInstitucion" method="post"
                          action="${raiz}/alta-institucion" enctype="multipart/form-data">

                        <div class="mb-3">
                            <label for="nombre" class="form-label fw-bold">Nombre *</label>
                            <input type="text" id="nombre" name="nombre" class="form-control" required
                                   value="<c:out value='${form.nombre}' />">
                        </div>

                        <div class="mb-3">
                            <label for="descripcion" class="form-label fw-bold">Descripción *</label>
                            <textarea id="descripcion" name="descripcion" class="form-control"
                                      rows="3" required><c:out value="${form.descripcion}" /></textarea>
                        </div>

                        <div class="mb-3">
                            <label for="sitioWeb" class="form-label fw-bold">Sitio web *</label>
                            <input type="url" id="sitioWeb" name="sitioWeb" class="form-control" required
                                   placeholder="https://ejemplo.com"
                                   value="<c:out value='${form.sitioWeb}' />">
                        </div>

                        <div class="mb-4">
                            <label for="imagen" class="form-label fw-bold">Imagen</label>
                            <input type="file" id="imagen" name="imagen" class="form-control"
                                   accept="image/png,image/jpeg,image/gif,image/webp">
                            <div class="form-text">Opcional. PNG, JPG, GIF o WEBP, hasta 2 MB.</div>
                        </div>

                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary fw-bold">Crear institución</button>
                            <a href="${raiz}/" class="btn btn-outline-secondary">Cancelar</a>
                        </div>
                    </form>

                </div>
            </div>
        </div>
    </div>
</main>

<script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>