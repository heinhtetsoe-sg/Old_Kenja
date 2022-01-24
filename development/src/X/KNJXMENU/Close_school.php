<?php

require_once('for_php7.php');

print <<<EOD
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>閉じる</title>
<SCRIPT Language="JavaScript">
<!--
timerID=0;
function autoClick(){
	window.opener = window;
	var win = window.open(location.href,"_self");
	win.close();
}
// -->
</SCRIPT>
</head>
<body onLoad="document.myFORM.myBTN.click()">
<FORM NAME="myFORM">
<INPUT TYPE="button" VALUE="CLOSE" NAME="myBTN" onClick="return autoClick();"><BR>
</FORM>
</body>
</html>
EOD;
?>
