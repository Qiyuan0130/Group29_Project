<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
</main>
<footer class="app-footer">
    <small>&copy; <span id="year"></span></small>
    <script>
        document.getElementById('year').textContent = new Date().getFullYear();
    </script>
</footer>
</body>
</html>
