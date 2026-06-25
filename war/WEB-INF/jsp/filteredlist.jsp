<?xml version="1.0" encoding="UTF-8" ?>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.Map,scrobblefilter.model.Preferences"%>
<%@page import="java.util.List,scrobblefilter.model.ScrobbledArtist"%>
<%@page import="scrobblefilter.model.User"%>
<%
Map<String, Object> model = (Map<String, Object>)request.getAttribute("model");
Preferences prefs = model==null?null:(Preferences)model.get("prefs");
User user = (User)request.getSession().getAttribute("user");
String artist1 = prefs==null?"":prefs.getArtist();
List<ScrobbledArtist> topArtists = (List<ScrobbledArtist>)model.get("list");
String greetingName = prefs==null ? "" : (prefs.getTwitterName() != null && !prefs.getTwitterName().isEmpty() ? prefs.getTwitterName() : prefs.getLastfmName());
%>
<html>
<head>
<link type="text/css" rel="stylesheet" href="/ScrobbleFilter.css?v=4">
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
	<div class=error>The post failed: <%=model.get("error")%>
	<br><a href="world">back to your dashboard to re-link an account</a>
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
<p>Post this now:
<% if (user != null && user.getToken() != null) { %>
<a href="post?platform=twitter">post to twitter</a>
<% } %>
<% if (user != null && user.getBlueskyHandle() != null) { %>
<a href="post?platform=bluesky">post to bluesky</a>
<% } %>
<% if (user == null || (user.getToken() == null && user.getBlueskyHandle() == null)) { %>
<span>link a Twitter or Bluesky account on your <a href="world">dashboard</a> to post.</span>
<% } %>
</p>
</body>
</div> <!-- main -->
</html>
