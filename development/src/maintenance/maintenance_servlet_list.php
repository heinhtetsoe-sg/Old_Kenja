<!--
  $Id: maintenance_servlet_list.php,v 1.3 2008/05/07 06:07:03 takaesu Exp $
-->
$Revision: 1.3 $<br>
$Date: 2008/05/07 06:07:03 $<br>
$RCSfile: maintenance_servlet_list.php,v $
<hr>
<PRE>
<?php
	#system("tail -1000 /opt/WebSphere/AppServer/logs/kenja.log");
	#system("/usr/local/bin/alp_pg_list.rb aaa");
	system("md5sum /classes/servletpack/KNJ[A-Z]/KNJ[A-Z]*.class");
	system("md5sum /classes/servletpack/KNJ[A-Z]/detail/*.class");
?>
</PRE>

