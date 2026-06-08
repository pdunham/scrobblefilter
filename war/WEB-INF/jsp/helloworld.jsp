<?xml version="1.0" encoding="UTF-8" ?>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="scrobblefilter.model.User"%>
<%@page import="scrobblefilter.model.FilteredArtist"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="java.net.URLEncoder"%>
<html>
<head>
<link type="text/css" rel="stylesheet" href="/ScrobbleFilter.css?v=2">
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
		return;
	}
}
String greetingName = user==null ? "" : (user.getTwitterName() != null ? user.getTwitterName() : user.getLastfmName());
%>

<body>
<div id=main>
<% String authError = (model != null) ? (String)model.get("authError") : null; %>
<% if (authError != null) { %><P style="color:red"><%= authError %></P><% } %>
Hello<%= user==null?"":", "+greetingName%>
<% if (user.getBlueskyHandle()==null) { %>
<P>You have not linked your Bluesky account.
<form method=get action=bluesky/signin>
<input type=text name=handle placeholder="you.bsky.social">
<input type=submit value="connect bluesky">
</form>
<% } else { %>
<P>You have linked your Bluesky account &mdash; @<%=user.getBlueskyHandle()%>
<form method=post action=updateBlueskyCronSetting class=toggle-row>
<label class="switch"><input type="checkbox" name="blueskyCron" value="true" <%= user.isBlueskyCron()?"checked":"" %> onchange="this.form.submit()"><span class="slider"></span></label>
post to bluesky weekly
<noscript><input type=submit value="save"></noscript>
</form>
<% } %>
<% if (user.getToken()==null) { %>
<P>You have not linked your twitter account.  <a href="twittersignin?lastfmName=<%=user.getLastfmName()%>">do it</a>
<% } else { %>
you have linked your twitter account &mdash; <a href="logout">log out</a>
<% } %>
<form method=post action=updateCronSetting class=toggle-row>
<label class="switch"><input type="checkbox" name="cron" value="true" <%= user.isCron()?"checked":"" %> onchange="this.form.submit()"><span class="slider"></span></label>
post to twitter weekly
<noscript><input type=submit value="save"></noscript>
</form>
<P>Your lastfm name is <%=user.getLastfmName()%>
<form method=post action=addartist>
<input type=hidden name=lastfmName value="<%=user.getLastfmName()%>"/>
<table>
<tr><td>add an artist to filter</td><td><input type=text name=artist></td></tr>
</table>
<input type=submit>
</form>
<table>
<%
@SuppressWarnings("unchecked")
List<FilteredArtist> artists = (model != null && model.containsKey("filteredArtists"))
    ? (List<FilteredArtist>)model.get("filteredArtists")
    : user.listAllFilteredArtists();
if (!artists.isEmpty()) { %>
<tr><th colspan=2>Filtered Artists So Far</th></tr>
<% }
for (FilteredArtist artist : artists) {
%>
<tr><td><%=artist.getArtistName()%></td><td><a href="removeartist?id=<%=URLEncoder.encode(artist.getId(), "UTF-8")%>">remove</a></td></tr>
<% } %>
</table>
<a href="filter?lastfmName=<%=user.getLastfmName()%>">see the filtered list</a>
</div> <!-- main -->
</body>
</html>
