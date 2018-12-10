<%--
  Created by IntelliJ IDEA.
  User: Flying
  Date: 2016/5/28
  Time: 17:43
--%>
<%@page language = "java" import = "java.util.*" pageEncoding = "UTF-8" %>
<%
    String path = request.getContextPath();
    String basePath =
            request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>
<html>
<head>
    <base href = "<%=basePath%>">

    <title>Insert title here</title>
    <!--
    <link rel="stylesheet" type="text/css" href="styles.css">
    -->

</head>

<body>
    This is my JSP page. <br>
</body>
</html>
