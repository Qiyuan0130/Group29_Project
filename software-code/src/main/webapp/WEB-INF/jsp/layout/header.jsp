<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle}" default="应用"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css">
</head>
<body>
<header class="app-header">
    <div class="app-header__inner">
        <a class="app-logo" href="${pageContext.request.contextPath}/home">应用</a>
        <nav class="app-nav">
            <!-- 原型确定后在此补充导航链接 -->
        </nav>
    </div>
</header>
<main class="app-main">
