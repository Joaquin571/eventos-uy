<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Alta de Evento - eventos.uy</title>
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

                    <h1 class="h3 fw-bold mb-4">Alta de Evento</h1>

                    <c:if test="${not empty param.creada}">
                        <c:url var="urlCreado" value="/evento">
                            <c:param name="nombre" value="${param.creada}" />
                        </c:url>
                        <div class="alert alert-success">
                            Evento «<c:out value="${param.creada}" />» creado correctamente.
                            <a href="${urlCreado}" class="alert-link">Ver evento</a>
                        </div>
                    </c:if>

                    <c:if test="${param.imagen eq 'error'}">
                        <div class="alert alert-warning">
                            El evento se creó, pero no se pudo guardar la imagen.
                        </div>
                    </c:if>

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger"><c:out value="${error}" /></div>
                    </c:if>

                    <div id="errorCliente" class="alert alert-danger d-none"></div>

                    <form id="formAltaEvento" method="post"
                          action="${raiz}/alta-evento" enctype="multipart/form-data">

                        <div class="mb-3">
                            <label for="nombre" class="form-label fw-bold">Nombre *</label>
                            <input type="text" id="nombre" name="nombre" class="form-control" required
                                   value="<c:out value='${form.nombre}' />">
                        </div>

                        <div class="mb-3">
                            <label for="sigla" class="form-label fw-bold">Sigla *</label>
                            <input type="text" id="sigla" name="sigla" class="form-control" required
                                   value="<c:out value='${form.sigla}' />">
                        </div>

                        <div class="mb-3">
                            <label for="descripcion" class="form-label fw-bold">Descripción *</label>
                            <textarea id="descripcion" name="descripcion" class="form-control"
                                      rows="3" required><c:out value="${form.descripcion}" /></textarea>
                        </div>

                        <div class="mb-3">
                            <label class="form-label fw-bold">Categorías * (al menos una)</label>
                            <c:forEach var="categoria" items="${categorias}" varStatus="estado">
                                <div class="form-check">
                                    <input type="checkbox" class="form-check-input categoria"
                                           name="categorias" id="cat${estado.index}"
                                           value="<c:out value='${categoria.idNombre}' />"
                                           <c:if test="${seleccionadas.contains(categoria.idNombre)}">checked</c:if>>
                                    <label class="form-check-label" for="cat${estado.index}">
                                        <c:out value="${categoria.idNombre}" />
                                    </label>
                                </div>
                            </c:forEach>
                        </div>

                        <div class="mb-4">
                            <label for="imagen" class="form-label fw-bold">Imagen</label>
                            <input type="file" id="imagen" name="imagen" class="form-control"
                                   accept="image/png,image/jpeg,image/gif,image/webp">
                            <div class="form-text">Opcional. PNG, JPG, GIF o WEBP, hasta 2 MB.</div>
                        </div>

                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary fw-bold">Crear evento</button>
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
    // Validación en el navegador: al menos una categoría y campos sin espacios vacíos
    document.getElementById('formAltaEvento').addEventListener('submit', function (e) {
        var aviso = document.getElementById('errorCliente');
        var campos = ['nombre', 'sigla', 'descripcion'];
        var mensaje = '';

        campos.forEach(function (id) {
            if (!mensaje && document.getElementById(id).value.trim() === '') {
                mensaje = 'Completá todos los campos obligatorios.';
            }
        });

        if (!mensaje && document.querySelectorAll('.categoria:checked').length === 0) {
            mensaje = 'Elegí al menos una categoría.';
        }

        if (mensaje) {
            e.preventDefault();
            aviso.textContent = mensaje;
            aviso.classList.remove('d-none');
            aviso.scrollIntoView({behavior: 'smooth', block: 'center'});
        }
    });
</script>
</body>
</html>
