<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar Sesión - eventos.uy</title>
    <link rel="stylesheet" href="${raiz}/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="${raiz}/css/styles.css">
</head>
<body class="bg-light">

<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp" />

<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-12 col-md-6 col-lg-5">
            <div class="card border-0 shadow-sm rounded-3">
                <div class="card-body p-4 p-md-5">

                    <h2 class="h3 fw-bold mb-4 text-dark text-center">Iniciar Sesión</h2>

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger"><c:out value="${error}" /></div>
                    </c:if>
                    <c:if test="${param.registrado eq 'ok'}">
                        <div class="alert alert-success">Usuario registrado. Ya podés iniciar sesión.</div>
                    </c:if>

                    <c:if test="${param.imagen eq 'error'}">
                        <div class="alert alert-warning">
                            No se pudo guardar la imagen. Podés agregarla desde Editar perfil.
                        </div>
                    </c:if>

                    <form method="post" action="${raiz}/login">
                        <div class="mb-3">
                            <label for="identificador" class="form-label fw-bold">Nickname o Correo *</label>
                            <input type="text" id="identificador" name="identificador"
                                   class="form-control" required
                                   value="<c:out value='${identificador}' />">
                        </div>

                        <div class="mb-4">
                            <label for="contrasenia" class="form-label fw-bold">Contraseña *</label>
                            <input type="password" id="contrasenia" name="contrasenia"
                                   class="form-control" required>
                        </div>

                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary py-2 fw-bold">Ingresar</button>
                            <a href="${raiz}/" class="btn btn-outline-secondary">Cancelar</a>
                        </div>
                    </form>

                    <div class="mt-4 text-center">
                        <small class="text-muted">¿No tenés cuenta?
                            <a href="${raiz}/alta-usuario" class="fw-bold text-decoration-none">Registrate acá</a>
                        </small>
                    </div>

                </div>
            </div>
        </div>
    </div>
</main>

<script src="${raiz}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>