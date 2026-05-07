<?xml version="1.0" encoding="UTF-8" ?>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.Map,scrobblefilter.model.Preferences"%>
<%@page import="java.util.List,scrobblefilter.model.ScrobbledArtist"%>
<%
Map<String, Object> model = (Map<String, Object>)request.getAttribute("model");
Preferences prefs = model==null?null:(Preferences)model.get("prefs");
String artist1 = prefs==null?"":prefs.getArtist();
List<ScrobbledArtist> topArtists = (List<ScrobbledArtist>)model.get("list");
String greetingName = prefs==null ? "" : (prefs.getTwitterName() != null && !prefs.getTwitterName().isEmpty() ? prefs.getTwitterName() : prefs.getLastfmName());
%>
<html>
<head>
<link type="text/css" rel="stylesheet" href="/ScrobbleFilter.css">
<title>Hello, <%=greetingName%></title>
<style>
.error {
color: #D8000C;
}
</style>
</head>

<body>
<div id=main>
Hello, <%=greetingName%>!
<br>
<a href="world">go back</a>
<br>
<% if (model!=null && model.get("error")!=null) { %>
	<div class=error>The tweet failed: <%=model.get("error")%>
	<br><a href="twittersignin?lastfmName=<%=prefs.getLastfmName()%>">Re-link your Twitter account</a>
	</div>
<% } %>
<table>
<tr><th>name</th><th>play count</th><th></th></tr>
<% for (ScrobbledArtist artist : topArtists) { %>
	<tr>
		<td>
			<%=artist.getName()%>
		</td>
		<td>
			<%=artist.getPlayCount()%>
		</td>
		<td>
			<form method=post action=addartist>
				<input type=hidden name=lastfmName value="<%=prefs.getLastfmName()%>"/>
				<input type=hidden name=artist value="<%=artist.getName() %>"/>
				<input type=submit value="filter this artist"/>
			</form>
		</td>
	</tr>
<% } %>
</table>
<a href="tweet?lastfmName=<%=prefs.getLastfmName()%>">tweet it</a>
</body>
</div> <!-- main -->
</html>
