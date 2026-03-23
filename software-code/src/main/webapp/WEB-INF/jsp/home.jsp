<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="首页" scope="request"/>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp"/>
<section class="page">
    <h1 class="page__title">首页</h1>
    <p class="page__hint">上传原型图后，将在此替换为与原型一致的布局与组件。</p>
</section>
<jsp:include page="/WEB-INF/jsp/layout/footer.jsp"/>
