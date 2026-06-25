<?xml version="1.0" encoding="UTF-8" ?>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.Map,java.util.List"%>
<%
Map<String, Object> model = (Map<String, Object>)request.getAttribute("model");
@SuppressWarnings("unchecked")
List<String> migrated = (List<String>)model.get("migrated");
@SuppressWarnings("unchecked")
List<String> merged = (List<String>)model.get("merged");
@SuppressWarnings("unchecked")
List<String> orphansDeleted = (List<String>)model.get("orphansDeleted");
%>
<html>
<head>
<link type="text/css" rel="stylesheet" href="/ScrobbleFilter.css?v=5">
<title>ScrobbleFilter migration</title>
</head>
<body class="app-page">
<div class="app-header"><a class="brand" href="/hello/world">Scrobble<span class="filter">Filter</span></a></div>
<div id=main>
<h1>Migration summary</h1>
<P>Users migrated (fresh): <%= model.get("migratedCount") %> (<%= model.get("migratedArtistCount") %> artists)</P>
<P>Users merged into existing target: <%= model.get("mergedCount") %> (<%= model.get("mergedArtistCount") %> artists)</P>
<P>Orphans deleted (no lastfmName): <%= model.get("orphansDeletedCount") %> (<%= model.get("deletedArtistCount") %> artists)</P>
<P>Already in new format (untouched): <%= model.get("alreadyInNewFormatCount") %></P>

<% if (migrated != null && !migrated.isEmpty()) { %>
<h2>Migrated</h2>
<ul>
<% for (String entry : migrated) { %><li><%= entry %></li><% } %>
</ul>
<% } %>

<% if (merged != null && !merged.isEmpty()) { %>
<h2>Merged into existing user</h2>
<ul>
<% for (String entry : merged) { %><li><%= entry %></li><% } %>
</ul>
<% } %>

<% if (orphansDeleted != null && !orphansDeleted.isEmpty()) { %>
<h2>Orphans deleted</h2>
<ul>
<% for (String entry : orphansDeleted) { %><li><%= entry %></li><% } %>
</ul>
<% } %>
</div>
</body>
</html>
