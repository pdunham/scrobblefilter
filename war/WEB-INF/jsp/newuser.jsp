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
Enter your Last.fm username and a ScrobbleFilter password. New here? This sets
your password. Returning? Enter the one you chose. You can link Twitter or
Bluesky later.
<% if (error != null) { %><P style="color:red"><%= error %></P><% } %>
<form method=post action=/hello/register>
<table>
<tr><td>Last.fm username</td><td><input type=text name=lastfmName required></td></tr>
<tr><td>ScrobbleFilter password</td><td><input type=password name=password required></td></tr>
</table>
<input type=submit>
</form>
</div>
</body>
</html>
