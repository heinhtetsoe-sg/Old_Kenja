<?php

require_once('for_php7.php');

class knjm210mQuery extends Query {

    //検索結果抽出
    function getSearch($model)
    {
        /*$query  = "WITH MAX_ADDR AS ( ";
        $query .= "    SELECT SCHREGNO,MAX(ISSUEDATE) MAX_DATE ";
        $query .= "    FROM   SCHREG_ADDRESS_DAT ";
        $query .= "    GROUP BY SCHREGNO ) ";
        $query .= ",SCH_ADDR AS ( ";//2005.04.20
        $query .= "    SELECT W1.SCHREGNO,W1.ADDR1,W1.ADDR2 ";
        $query .= "    FROM   SCHREG_ADDRESS_DAT W1,MAX_ADDR W2 ";
        $query .= "    WHERE  W1.SCHREGNO=W2.SCHREGNO AND W1.ISSUEDATE=W2.MAX_DATE ) ";*/

        $query  = "SELECT T3.HR_NAMEABBV, ";
        $query .= "       T1.ATTENDNO, ";
        $query .= "       T2.SCHREGNO, ";
        $query .= "       T2.NAME, ";
        $query .= "       T2.SEX, ";
        $query .= "       (SELECT NAME2 FROM NAME_MST WHERE NAMECD1='Z002' AND NAMECD2=T2.SEX) AS SEX_NAME, ";
        $query .= "       VALUE(T4.SEND_ADDR1,'')||VALUE(T4.SEND_ADDR2,'') AS ADDR ";
        $query .= "FROM   SCHREG_REGD_DAT T1, ";
        $query .= "       SCHREG_REGD_HDAT T3, ";
        $query .= "       SCHREG_BASE_MST T2 ";
        $query .= "       LEFT JOIN SCHREG_SEND_ADDRESS_DAT T4 ON T4.SCHREGNO = T2.SCHREGNO ";
        $query .= "                                           AND T4.DIV = '1' ";
        $query .= "WHERE  T1.YEAR      ='".CTRL_YEAR."' AND ";
        $query .= "       T1.SEMESTER  ='".CTRL_SEMESTER."' AND ";
        //学籍番号のみで生徒を検索
        if ($model->field["SCHREGNO"]) {
            $query .= "   T2.SCHREGNO  ='".$model->field["SCHREGNO"]."' AND ";
        } else {
            if ($model->field["NAME"]) 
                $query .= "   T2.NAME LIKE '%".$model->field["NAME"]."%' AND ";
            if ($model->field["NAME_KANA"]) 
                $query .= "   T2.NAME_KANA LIKE '%".$model->field["NAME_KANA"]."%' AND ";
        }
        $query .= "       T3.YEAR      =T1.YEAR AND ";
        $query .= "       T3.SEMESTER  =T1.SEMESTER AND ";
        $query .= "       T3.GRADE     =T1.GRADE AND ";
        $query .= "       T3.HR_CLASS  =T1.HR_CLASS AND ";
        $query .= "       T2.SCHREGNO  =T1.SCHREGNO ";
        $query .= "ORDER BY T1.GRADE,T1.HR_CLASS,T1.ATTENDNO ";

        return $query;
    }

}
?>