<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><title>Finalizar evento</title></head><body>
<jsp:include page="/WEB-INF/views/fragmentos/cabezal.jsp" />
<main class="container py-4"><h1>Finalizar evento</h1>
<c:if test="${not empty error}"><p class="alert alert-danger"><c:out value="${error}"/></p></c:if>
<form method="post"><jsp:include page="/WEB-INF/views/fragmentos/csrf.jsp"/>
<label for="evento">Evento sin finalizar</label><select id="evento" name="evento" required>
<c:forEach items="${eventos}" var="e"><option value="${e.nombre}"><c:out value="${e.nombre}"/></option></c:forEach>
</select><button type="submit">Finalizar</button></form></main></body></html>
