<?xml version="1.0" encoding="UTF-8" ?>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<html>
<head>
<link type="text/css" rel="stylesheet" href="/ScrobbleFilter.css">
<title>Welcome to ScrobbleFilter!</title>
</head>

<body>

<div id=main>
Hello
<P>
Sign in via Twitter and you can get started.
<form method=post action=/hello/twittersignin>
<table>
<tr><td>Twitter handle</td><td><input type=text name=name></td></tr>
</table>
<input type=submit>
</form>
</div>
</body>
</html>