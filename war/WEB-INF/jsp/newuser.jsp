<?xml version="1.0" encoding="UTF-8" ?>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.Map"%>
<%
Map<String, Object> model = (Map<String, Object>)request.getAttribute("model");
String error = (model != null) ? (String)model.get("error") : null;
%>
<html>
<head>
<link type="text/css" rel="stylesheet" href="/ScrobbleFilter.css">
<title>Welcome to ScrobbleFilter!</title>
</head>

<body>

<div id=main>
Hello
<P>
Tell me your Last.fm username to get started. You can link a Twitter account later.
<% if (error != null) { %><P style="color:red"><%= error %></P><% } %>
<form method=post action=/hello/register>
<table>
<tr><td>Last.fm username</td><td><input type=text name=lastfmName required></td></tr>
<tr><td>Twitter handle (optional)</td><td><input type=text name=name></td></tr>
</table>
<input type=submit>
</form>
</div>
</body>
</html>
