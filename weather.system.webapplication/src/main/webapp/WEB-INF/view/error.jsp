<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<base href="<%=basePath%>">
		<title>中医体质评测统计系统</title>
		<%@ include file="/include/head.jsp"%>
	</head>
	<body class="easyui-layout" style="overflow-y: hidden" scroll="no">
		<noscript>
			<div class="cus-alert">
				<img src="resources/images/noscript.gif" alt='抱歉，请开启脚本支持！' />
			</div>
		</noscript>
		<div region="north" split="true" border="false" class="cus-title " >
			<span class="company-name">中医体质评测统计系统</span>
		</div>
		<div region="south" split="true" class="cus-foot">
			<div class="footer">
				Copyright&nbsp;©&nbsp;2016&nbsp;
			</div>
		</div>
		<div region="west" split="true" title="导航菜单" id="west">
			<div class="easyui-accordion" fit="true" border="false">
				<!--  导航内容 -->
			</div>
		</div>
		<div id="mainPanle" region="center">
			<div id="tabs" class="easyui-tabs" fit="true" border="false">
				<div title="欢迎使用">
					<div class="wellCome">
						<img src="resources/images/welcome_wz.png"/>
					<div>
				</div>
			</div>
		</div>
		<%@ include file="/include/close.jsp"%>
	</body>
</html>
