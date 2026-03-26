<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${empty pageTitle ? 'Page' : pageTitle}" scope="request"/>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp"/>
<section class="page">
    <h1 class="page__title"><c:out value="${pageTitle}"/></h1>
    <p class="page__hint">Placeholder page: copy into a dedicated JSP and wire to a Servlet when ready.</p>
</section>
<jsp:include page="/WEB-INF/jsp/layout/footer.jsp"/>
