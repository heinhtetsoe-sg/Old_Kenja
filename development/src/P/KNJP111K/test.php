<?php

require_once('for_php7.php');

/* ��{�t�@�C���̓ǂݍ��� */
ini_set("include_path", "/usr/local/lib/php:/usr/local/kinu/lib");
include_once("prepend.inc");
set_time_limit(0);
var_dump(DSN);
$db = Query::dbCheckOut();

$query = " SELECT ";
$query .= "     T1.FILE_LINE_NUMBER ";
$query .= " FROM ";
$query .= "     APPL_RESULT_TMP_DATA T1, ";
$query .= "     (SELECT ";
$query .= "         W2.SCHREGNO, ";
$query .= "         W4.APPLI_MONEY_DUE, ";
$query .= "         replace(replace(W3.NAME_KANA,' ',''),'�@','') AS NAME_KANA, ";
$query .= "         digits(INTEGER(W1.BANK_MAJORCD || W1.GRADE || W1.BANK_HR_CLASS || SUBSTR(W2.ATTENDNO,2,2))) AS REFERENCE_NUMBER ";
$query .= "     FROM ";
$query .= "         BANK_CLASS_MST W1, ";
$query .= "         SCHREG_REGD_DAT W2, ";
$query .= "         SCHREG_BASE_MST W3, ";
$query .= "         APPLICATION_DAT W4 ";
$query .= "     WHERE ";
$query .= "         W1.YEAR = '2005' AND ";
$query .= "         W1.YEAR = W2.YEAR AND ";
$query .= "         W1.YEAR = W4.YEAR AND ";
$query .= "         W2.SEMESTER = '1' AND ";
$query .= "         W1.GRADE = W2.GRADE AND ";
$query .= "         W1.HR_CLASS = W2.HR_CLASS AND ";
$query .= "         W2.SCHREGNO = W3.SCHREGNO AND ";
$query .= "         W2.SCHREGNO = W4.SCHREGNO AND ";
$query .= "         W4.APPLICATIONCD    = '0002' ";
$query .= "     ) T2 ";
$query .= " WHERE ";
$query .= "     T2.REFERENCE_NUMBER = digits(INTEGER(T1.REFERENCE_NUMBER))  ";
$query .= "  ";
$result = $db->query($query);
while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
{
    var_dump($row);
}
$db->commit();
Query::dbCheckIn($db);


?>
