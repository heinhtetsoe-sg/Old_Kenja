<html>
 <head>
   <TITLE>Test page for DB2+PHP+APACHE</TITLE>
 <head>
<body>
<pre>
<?php

require_once('for_php7.php');

$dbname = "sample";
$username = "db2inst1"; //���ʤ��δĶ��ˤ��碌�Ƥ�������
$userpass = "db2inst1"; //���ʤ��δĶ��ˤ��碌�Ƥ�������

echo "DB2TEST: ";
$db = odbc_connect($dbname,$username,$userpass);


if($db){

	echo "CONNECT OK" . "<BR>";
	odbc_autocommit($db,1);

	$result=odbc_exec($db,"select * from employee");
	$rows=odbc_num_rows($result);
	$columns=odbc_num_fields($result);

	echo "rows=$rows,columns=$columns<br>\n";
	print "<table border>\n";
	$i=0;
	print "<tr>";
	for ($i=1;$i<=$columns;$i++) {
       	$field_name=odbc_field_name($result,$i);
       	print "<th>$field_name</th>";
	}
	print "</tr>";

	while (odbc_fetch_into($result,&$array)) {
       	print "<tr>";
       	for ($i=0 ; $i<$columns ; $i++) {
            	print "<td>$array[$i]</td>";
       	}
       	print "</tr>\n";
	}

	print "</table>\n";

	$ret=odbc_free_result($result);
	$ret=odbc_close($db);
} else {
	echo "Connect Failed<BR>";
};
?>
</pre>
</body>
</html>

