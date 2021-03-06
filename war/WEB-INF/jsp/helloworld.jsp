<?xml version="1.0" encoding="UTF-8" ?>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="scrobblefilter.model.User"%>
<%@page import="scrobblefilter.model.FilteredArtist"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<html>
<head>
<link type="text/css" rel="stylesheet" href="/ScrobbleFilter.css">
<title>Hello, World</title>
</head>

<%
User user = null;
Map model = (Map)request.getAttribute("model");
if (model != null) {
	user = (User)model.get("user");
} else {
	//try the session
	user = (User)request.getSession().getAttribute("user");
	if (user == null)
	{	
		response.sendRedirect("/hello/welcome");
	}
}
%>

<body>
<div id=main>
Hello<%= user==null?"":", "+user.getTwitterName()%>
<% if (user.getToken()==null) { %>
<P>You have not linked your twitter account.  <a href="twittersignin?name=<%=user.getName()%>">do it</a>
<% } else { %>
you have linked your twitter account
<% } %>
<table>
<% if (user.getLastfmName()==null) { %>
<form method=post action=updateLastfmName>
<input type=hidden name=twitterName value="<%=user.getTwitterName()%>"/>
<tr><td>last.fm user name</td><td><input type=text name=lastfmName></td></tr>
<% } else { %>
<P>Your lastfm name is <%=user.getLastfmName()%>
<form method=post action=addartist>
<input type=hidden name=twitterName value="<%=user.getTwitterName()%>"/>
<input type=hidden name=lastfmName value="<%=user.getLastfmName()%>"/>
<tr><td>add an artist to filter</td><td><input type=text name=artist></td></tr>
<% } %>
</table>
<input type=submit>
</form>
<form method=post action=updateCronSetting>
<P><input type="checkbox" <%= user.isCron()?"checked":"" %> disabled/> use cron job 
<input type=hidden name=twitterName value="<%= user.getTwitterName() %>"/>
<input type=submit name=cron value=<%= !user.isCron() %>>
</form>
<table>
<%
List<FilteredArtist> artists = user.listAllFilteredArtists() ;
if (!artists.isEmpty()) { %>
<tr><th colspan=2>Filtered Artists So Far</th></tr>
<% }
for (FilteredArtist artist : artists) {
%>
<tr><td><%=artist.getArtistName()%></td><td><a href="removeartist?id=<%=artist.getId()%>&twitterName=<%=user.getTwitterName()%>">remove</a></td></tr>
<% } %>
</table>
<% if (user.getLastfmName()!=null) { %>
<a href="filter?name=<%=user.getName()%>">see the filtered list</a>
<% } %>
</div> <!-- main -->
</body>
</html>