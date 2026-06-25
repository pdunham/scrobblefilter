<?xml version="1.0" encoding="UTF-8" ?>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="scrobblefilter.model.User"%>
<%@page import="scrobblefilter.model.FilteredArtist"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="java.net.URLEncoder"%>
<html>
<head>
<link type="text/css" rel="stylesheet" href="/ScrobbleFilter.css?v=6">
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
String greetingName = user==null ? "" : user.getLastfmName();
%>

<body class="app-page">
<div class="app-header"><a class="brand" href="world">Scrobble<span class="filter">Filter</span></a></div>
<div id=main>
<% String authError = (model != null) ? (String)model.get("authError") : null; %>
<% if (authError != null) { %><div class="error"><%= authError %></div><% } %>
<h1>Hello<%= user==null?"":", "+greetingName%></h1>
<% if (user!=null) { %><p><a href="logout">Log out</a></p><% } %>

<h2>Bluesky</h2>
<% boolean blueskyHasAccount = user.getBlueskyHandle()!=null; %>
<% if (!blueskyHasAccount) { %>
<p>You have not linked your Bluesky account.</p>
<form method=get action=bluesky/signin>
<div class="field-row">
<input type=text name=handle placeholder="you.bsky.social">
<input type=submit value="connect bluesky">
</div>
</form>
<% } else if (user.isBlueskyReconnectNeeded()) { %>
<p class="needs-reconnect">Your Bluesky session for @<%=user.getBlueskyHandle()%> has expired.
&nbsp;<a href="bluesky/signin?handle=<%=URLEncoder.encode(user.getBlueskyHandle(), "UTF-8")%>">reconnect</a></p>
<% } else { %>
<p>You have linked your Bluesky account &mdash; @<%=user.getBlueskyHandle()%></p>
<% } %>
<form method=post action=updateBlueskyCronSetting class="toggle-row<%= blueskyHasAccount?"":" disabled" %>">
<label class="switch"><input type="checkbox" name="blueskyCron" value="true" <%= user.isBlueskyCron()?"checked":"" %> <%= blueskyHasAccount?"":"disabled" %> onchange="this.form.submit()"><span class="slider"></span></label>
post to bluesky weekly
<noscript><input type=submit value="save"></noscript>
</form>

<h2>Twitter</h2>
<% boolean twitterLinked = user.getToken()!=null; %>
<% if (!twitterLinked) { %>
<p>You have not linked your twitter account. <a href="twittersignin?lastfmName=<%=user.getLastfmName()%>">do it</a></p>
<% } else { %>
<p>You have linked your twitter account<% if (user.getTwitterName()!=null) { %> &mdash; @<%=user.getTwitterName()%><% } %></p>
<% } %>
<form method=post action=updateCronSetting class="toggle-row<%= twitterLinked?"":" disabled" %>">
<label class="switch"><input type="checkbox" name="cron" value="true" <%= user.isCron()?"checked":"" %> <%= twitterLinked?"":"disabled" %> onchange="this.form.submit()"><span class="slider"></span></label>
post to twitter weekly
<noscript><input type=submit value="save"></noscript>
</form>

<h2>Filtered artists</h2>
<p>Your Last.fm name is <%=user.getLastfmName()%>.</p>
<form method=post action=addartist>
<input type=hidden name=lastfmName value="<%=user.getLastfmName()%>"/>
<div class="field-row">
<label>Add an artist to filter</label>
<input type=text name=artist placeholder="Artist name">
<input type=submit value="Add">
</div>
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
<p><a href="filter?lastfmName=<%=user.getLastfmName()%>">see the filtered list</a></p>
</div> <!-- main -->
</body>
</html>
