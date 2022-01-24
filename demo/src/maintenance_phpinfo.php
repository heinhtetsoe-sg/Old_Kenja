<!--
  $Id: maintenance_phpinfo.php,v 1.1 2015/11/01 23:32:31 m-yama Exp $
-->
<html>
 <head>
   <TITLE>Test page for DB2+PHP+APACHE</TITLE>
 <head>
<body>
$Revision: 1.1 $
<form action="./maintenance_phpinfo.php" method="POST">
<pre>
<?php

require_once('for_php7.php');

    echo phpinfo();
?>
</pre>
</form>
</body>
</html>

