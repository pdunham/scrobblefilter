<?xml version="1.0" encoding="UTF-8" ?>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="scrobblefilter.model.User"%>
<%@page import="scrobblefilter.model.FilteredArtist"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List,java.util.ArrayList"%>
<html>
<head>
<title>Tweets sent</title>
</head>


<body>
<!-- TODO: check whethertweets actually sent -->
All tweets sent.
<%
Map<String, Object> model = (Map<String, Object>)request.getAttribute("model");
List<User> users = model==null?new ArrayList<User>():(List<User>)model.get("users");
%><H3><%= users.size() %> users</H3><%
for (User user : users) {
	
	%><p><%=user.getName() %></p><% 
	
}
%>
</body>
</html>