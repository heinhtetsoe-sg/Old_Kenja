<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
 request.setCharacterEncoding("UTF-8");
 String pdfpath = request.getParameter("pdfpath");
 String message = request.getParameter("message");
 String timevalue = request.getParameter("timevalue");
 %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>終了</title>
<SCRIPT Language="JavaScript">
<!--
timerID=0;
timer_=new Number("<%=timevalue %>");
function autoClick(){ (window.open('', '_self').opener = window).close(); }
if (timer_ > 0) {
	setTimeout("autoClick()",timer_);
}
// -->
</SCRIPT>
</head>
<body>
<br><br>
<font size="5">&nbsp;<%=message %></font><br>
<TABLE border="0" width="100%" height="100%"><TR><TD align="center">
<font size="4">{<%=pdfpath %>}</font>
</TD>
</TR>
     <tr>
       <td>　</td><td><INPUT type="button" value=" 閉じる " onclick="autoClick();" ></td>
     </tr>
</TABLE>
</body>
</html>
