<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Video embebido en 16:9 a todo el ancho. Solo se dibuja si el servlet publica "urlVideoEmbed". --%>
<c:if test="${not empty urlVideoEmbed}">
    <div class="ratio ratio-16x9 movil-video mb-3">
        <iframe src="<c:out value='${urlVideoEmbed}'/>" title="Video de la edición" loading="lazy" allowfullscreen
                referrerpolicy="strict-origin-when-cross-origin"
                allow="accelerometer; encrypted-media; gyroscope; picture-in-picture"></iframe>
    </div>
</c:if>
