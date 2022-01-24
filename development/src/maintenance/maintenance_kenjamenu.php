<!--
  $Id: maintenance_kenjamenu.php,v 1.3 2014/02/03 02:34:23 m-yama Exp $
-->
<html>
 <head>
   <TITLE>Test page for DB2+PHP+APACHE</TITLE>
 <head>
<body>
$Revision: 1.3 $
<pre>
<?php

require_once('for_php7.php');

$dbname = "sample";
$username = "db2inst1"; //あなたの環境にあわせてください
$userpass = "db2inst1"; //あなたの環境にあわせてください

$fp = @fopen(CONFDIR.'/gk.conf','r');
//fgetsがFALSEを返す（通常はファイル終端に達した時）までループ
while( $line = fgets($fp,1024) ){ // バイト数の1024に深い意味はありません
    $pos = strpos($line, "Database = ");
    // === を使用していることに注目しましょう。単純に == を使ったのでは
    // 期待通りに動作しません。なぜなら 'a' が 0 番目 (最初) の文字だからです。
    if ($pos === false) {
        $posPass = strpos($line, "Password = ");
        // === を使用していることに注目しましょう。単純に == を使ったのでは
        // 期待通りに動作しません。なぜなら 'a' が 0 番目 (最初) の文字だからです。
        if ($posPass === false) {
            continue;
        } else {
            if ($posPass == 0) {
                $userpass = str_replace("Password = ", "", $line);
                $userpass = str_replace("\t", "", $userpass);
                $userpass = str_replace(" ", "", $userpass);
            }
        }
        continue;
    } else {
        if ($pos == 0) {
            $retVal = str_replace("Database = ", "", $line);
            $retVal = str_replace("\t", "", $retVal);
            $retVal = str_replace(" ", "", $retVal);
            $pos2 = strpos($retVal, "#");
            if ($pos2 === false) {
                $retVal = str_replace("\r\n", "", $retVal);
                $dbname = str_replace("\n", "", $retVal);
            } else {
                $retVal = substr($retVal, 0, $pos2);
                $retVal = str_replace("\r\n", "", $retVal);
                $dbname = str_replace("\n", "", $retVal);
            }
        }
    }
}
fclose($fp);
echo "DBNAME:".$dbname."<BR>";

$db = odbc_connect($dbname,$username,$userpass);


if($db){

	odbc_autocommit($db,1);

	$result=odbc_exec($db,"SELECT MENUID, SUBMENUID, PARENTMENUID, MENUNAME, PROGRAMID, PROGRAMPATH FROM MENU_MST WHERE PROGRAMID IS NOT NULL ORDER BY MENUID");
	$rows=odbc_num_rows($result);
	$columns=odbc_num_fields($result);

	echo "件数=$rows<br>\n";
	print "<table border>\n";
	$i=0;
	print "<tr>";
	for ($i=1;$i<=$columns;$i++) {
       	$field_name=odbc_field_name($result,$i);
       	print "<th>$field_name</th>";
	}
	print "</tr>";

	while (odbc_fetch_into($result,$array)) {
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

