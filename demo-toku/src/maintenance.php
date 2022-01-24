<!--
  $Id: maintenance.php,v 1.8 2015/11/01 23:32:59 m-yama Exp $
-->
<html>
 <head>
   <TITLE>メンテナンス メインメニュー</TITLE>
 <head>
<body>
<pre>
<?PHP
    print "<tr>";

    print "<td>";
    print "・<a href=\"".REQUESTROOT."/maintenance_kenjamenu.php\" target=\"under\">メニュー一覧</a>";
    print "</td>";

    print "<td>";
    print "・<a href=\"".REQUESTROOT."/maintenance_prgInfo.php\" target=\"under\">prgInfoProperties一覧</a>";
    print "</td>";

    print "<td>";
    print " ・<a href=\"".REQUESTROOT."/maintenance_kenjalog.php\" target=\"under\">帳票ログ(Base2)</a>";
    print "</td>";

    print "<td>";
    print " ・<a href=\"".REQUESTROOT."/maintenance_kenjalog2.php\" target=\"under\">帳票ログ(Base3)</a>";
    print "</td>";

    print "<td>";
    print " ・<a href=\"".REQUESTROOT."/maintenance_php_list.php\" target=\"under\">PHPプログラム一覧</a>";
    print "</td>";

    print "<td>";
    print " ・<a href=\"".REQUESTROOT."/maintenance_servlet_list.php\" target=\"under\">帳票プログラム一覧</a>";
    print "</td>";

    print "<td>";
    print "・<a href=\"".REQUESTROOT."/maintenance_phpinfo.php\" target=\"under\">PHP_INFO</a>";
    print "</td>";

    print "</tr>";

?>
</pre>
</body>
</html>

