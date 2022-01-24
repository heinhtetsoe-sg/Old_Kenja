<!--
  $Id: maintenance_kenjalog.php,v 1.3 2008/04/10 09:24:31 m-yama Exp $
-->
$Revision: 1.3 $<br>
$Date: 2008/04/10 09:24:31 $<br>
$RCSfile: maintenance_kenjalog.php,v $
<hr>
<PRE>
<?php
	system("tail -1000 /opt/WebSphere/AppServer/logs/kenja.log");
?>
</PRE>

