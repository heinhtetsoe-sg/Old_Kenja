<!--
  $Id: maintenance_prgInfo.php,v 1.2 2011/08/11 06:22:42 m-yama Exp $
-->
<html>
 <head>
   <TITLE>Test page for DB2+PHP+APACHE</TITLE>
 <head>
<body>
$Revision: 1.2 $
<form action="./maintenance_prgInfo.php" method="POST">
<pre>
<?php

require_once('for_php7.php');

echo "<a name=\"top\"></a><BR>";
$dbname = "sample";
$username = "db2inst1"; //あなたの環境にあわせてください
$userpass = "db2inst1"; //あなたの環境にあわせてください

$fp = @fopen('../gk.conf','r');
//fgetsがFALSEを返す（通常はファイル終端に達した時）までループ
while( $line = fgets($fp,1024) ){ // バイト数の1024に深い意味はありません
    $pos = strpos($line, "Database = ");
    // === を使用していることに注目しましょう。単純に == を使ったのでは
    // 期待通りに動作しません。なぜなら 'a' が 0 番目 (最初) の文字だからです。
    if ($pos === false) {
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
echo "<a href=\"#DB\"><font color=\"red\" size=5>DB&プロパティー情報へ</font></a><BR>";


$propateArray = array();
$fp = @fopen('prgInfo.properties','r');
print "<table border>\n";
print "<tr>";
print "<th>プロパティー名</th>";
print "<th>値</th>";
print "</tr>";
//fgetsがFALSEを返す（通常はファイル終端に達した時）までループ
while( $line = fgets($fp,1024) ){ // バイト数の1024に深い意味はありません
    $setLineData = $line;
    $pos = strpos($line, " = ");
    $pos2 = strpos($line, "#");
    $pos3 = strpos($line, "//");
    $pos4 = strpos($line, "# \$Id");
    if ($pos4 !== false) {
        continue;
    }
    // === を使用していることに注目しましょう。単純に == を使ったのでは
    // 期待通りに動作しません。なぜなら 'a' が 0 番目 (最初) の文字だからです。
    if ($pos !== false) {
        if ($pos2 !== false || $pos3 !== false) {
            continue;
        }
        $lineArray = preg_split("/ = /", $line);
        $propateArray[$lineArray[0]] = $lineArray[1];
        print "<tr align=left>";
        print "<th>$lineArray[0]</th>";
        print "<th>$lineArray[1]</th>";
        print "</tr>";
    } else {
        print "<tr align=left bgcolor=\"#f3aaff\">";
        print "<th colspan=2>$line</th>";
        print "</tr>";
    }
}
print "</table>\n";
fclose($fp);

echo "<BR>";
echo "<BR>";
echo "<BR>";
echo "<BR>";
echo "<a name=\"DB\"></a>";
echo "------------------以下DB情報---------------------<BR>";
echo "<a href=\"#top\"><font color=\"red\" size=5>プロパティー情報へ</font></a><BR>";

$sortDiv = $_POST['sort'];
$checked1 = $sortDiv != "2" ? "checked" : "";
$checked2 = $sortDiv == "2" ? "checked" : "";
echo "<input type=\"radio\" name=\"sort\" value=\"1\" id=\"sort1\" {$checked1}><label for=\"sort1\">パラメータ名順</label>　<input type=\"radio\" name=\"sort\" value=\"2\" id=\"sort2\" {$checked2}><label for=\"sort2\">PRGID順</label>";

echo "<input type=\"submit\" value=\"読み込み\">";

$db = odbc_connect($dbname, $username, $userpass);

$sort = $sortDiv != "2" ? "NAME" : "PROGRAMID";

if($db){

    odbc_autocommit($db,1);

    $result=odbc_exec($db,"SELECT NAME, PROGRAMID, VALUE FROM PRGINFO_PROPERTIES ORDER BY UPPER(".$sort.") ");
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

    $befName = "";
    while (odbc_fetch_into($result,$array)) {
        print "<tr>";
        for ($i=0 ; $i<$columns ; $i++) {
            if ($i == 0 && $befName != $array[$i]) {
                if (array_key_exists($array[$i], $propateArray)) {
                    print "<td bgcolor=\"ff00ff\">".$array[$i]."</td>";
                    print "<td bgcolor=\"ff00ff\"></td>";
                    print "<td bgcolor=\"ff00ff\">".$propateArray[$array[$i]]."</td>";
                } else {
                    print "<td colspan=3 bgcolor=\"ff00ff\">プロパティーファイルなし</td>";
                }
                $befName = $array[$i];
                print "</tr>";
                print "<tr>";
            }
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
</form>
</body>
</html>

