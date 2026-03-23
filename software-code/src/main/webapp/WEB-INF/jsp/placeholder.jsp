<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${empty pageTitle ? '页面' : pageTitle}" scope="request"/>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp"/>
<section class="page">
    <h1 class="page__title"><c:out value="${pageTitle}"/></h1>
    <p class="page__hint">占位页面：原型确定后可复制为独立 JSP 并接 Servlet。</p>
</section>
<jsp:include page="/WEB-INF/jsp/layout/footer.jsp"/>
