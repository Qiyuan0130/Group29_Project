<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle}" default="App"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css">
</head>
<body>
<header class="app-header">
    <div class="app-header__inner">
        <a class="app-logo" href="${pageContext.request.contextPath}/home">App</a>
        <nav class="app-nav">
            <!-- Add navigation links here -->
        </nav>
    </div>
</header>
<main class="app-main">
