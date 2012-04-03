<?xml version="1.0" encoding="UTF-8" ?>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.Map,scrobblefilter.model.Preferences"%>
<%@page import="java.util.List,scrobblefilter.model.ScrobbledArtist"%>
<%
Map<String, Object> model = (Map<String, Object>)request.getAttribute("model");
Preferences prefs = model==null?null:(Preferences)model.get("prefs");
String artist1 = prefs==null?"":prefs.getArtist();
List<ScrobbledArtist> topArtists = (List<ScrobbledArtist>)model.get("list");
%>
<html>
<head>
<title>Hello, <%=prefs.getTwitterName()%></title>
<style>
.error {
color: #D8000C;
}
</style>
</head>

<body>
Hello, <%=prefs.getTwitterName()%>!
<br>
<% if (model!=null && model.get("error")!=null) { %>
	<div class=error>The tweet failed: <%=model.get("error")%></div>
<% } %>
<table>
<tr><th>name</th><th>play count</th></tr>
<% for (ScrobbledArtist artist : topArtists) { %>
<tr><td><%=artist.getName()%></td><td><%=artist.getPlayCount()%></td></tr>
<% } %>
</table>
<a href="tweet?name=<%=prefs.getTwitterName()%>">tweet it</a>
</body>
</html>