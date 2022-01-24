<!--
  $Id: maintenance_kenjalog2.php,v 1.1 2014/02/03 02:31:38 m-yama Exp $
-->
$Revision: 1.1 $<br>
$Date: 2014/02/03 02:31:38 $<br>
$RCSfile: maintenance_kenjalog2.php,v $
<hr>
<PRE>
<?php

require_once('for_php7.php');

    system("tail -1000 /opt/IBM/WebSphere/AppServer/profiles/AppSrv01/logs/server1/SystemOut.log");
?>
</PRE>

