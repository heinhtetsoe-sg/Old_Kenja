<!--
  $Id: maintenance_php_list.php,v 1.2 2008/04/11 08:27:24 takaesu Exp $
-->
$Revision: 1.2 $<br>
$Date: 2008/04/11 08:27:24 $<br>
$RCSfile: maintenance_php_list.php,v $
<hr>
<PRE>
<?php

require_once('for_php7.php');

	#system("tail -1000 /opt/WebSphere/AppServer/logs/kenja.log");
	#system("/usr/local/bin/alp_pg_list.rb");
	system("md5sum /usr/local/development/src/[A-N]/KNJ[A-N]???/*");
	system("md5sum /usr/local/development/src/[M-Z]/KNJ[M-Z]???/*");
?>
</PRE>

