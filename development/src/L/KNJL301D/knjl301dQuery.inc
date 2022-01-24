<?php

require_once('for_php7.php');

class knjl301dQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getTtlNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     N_MST.NAME1 AS LABEL, ";
        $query .= "     N_MST.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST N_MST";
        $query .= " WHERE ";
        $query .= "     N_MST.YEAR    = '".$year."' AND ";
        $query .= "     N_MST.NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     N_MST.NAMECD2 ";
        
        return $query;
    }

    //名称マスタより名称のみ取得
    function getExamerList($model, $flg) {
        $query   = " SELECT ";
        if ($flg == "COUNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query  .= "     T1.EXAMNO, ";
            $query  .= "     T1.NAME, ";
            $query  .= "     T1.NAME_KANA, ";
            $query  .= "     NM_SEX.NAME1 AS SEX, ";
            $query  .= "     NM_ERACD.NAME1 AS ERACD, ";
            $query  .= "     T1.BIRTH_Y , ";
            $query  .= "     T1.BIRTH_M , ";
            $query  .= "     T1.BIRTH_D , ";
            $query  .= "     T1.DESIREDIV, ";
            $query  .= "     T1.FS_CD, ";
            $query  .= "     FSM.FINSCHOOL_NAME_ABBV, ";
            $query  .= "     T1.FS_GRDYEAR, ";
            $query  .= "     CASE WHEN (INTEGER('".CTRL_YEAR."') + 1 - INTEGER(T1.FS_GRDYEAR) > 0) THEN REPLACE(CHAR(INTEGER('".CTRL_YEAR."') + 1 - INTEGER(T1.FS_GRDYEAR)), ' ', '') || '浪' ELSE '' END AS PAST_STUDENT, ";
            $query  .= "     AD1.GNAME, ";
            $query  .= "     AD1.GKANA, ";
            $query  .= "     AD1.RELATIONSHIP, ";
            $query  .= "     NM_REL.NAME1 AS REL_NAME, ";
            $query  .= "     AD1.GZIPCD, ";
            $query  .= "     AD1.GADDRESS1, ";
            $query  .= "     AD1.GADDRESS2, ";
            $query  .= "     AD1.GTELNO ";
        }
        $query  .= " FROM ";
        $query  .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query  .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT AD1 ON T1.EXAMNO = AD1.EXAMNO and ";
        $query  .= "       T1.ENTEXAMYEAR = AD1.ENTEXAMYEAR and ";
        $query  .= "       T1.APPLICANTDIV = AD1.APPLICANTDIV ";
        $query  .= "    LEFT JOIN NAME_MST NM_ERACD ON NM_ERACD.NAMECD1 = 'L007' AND NM_ERACD.NAMECD2 = T1.ERACD ";
        $query  .= "    LEFT JOIN NAME_MST NM_SEX ON NM_SEX.NAMECD1 = 'Z002' AND NM_SEX.NAMECD2 = T1.SEX ";
        $query  .= "    LEFT JOIN FINSCHOOL_MST FSM ON T1.FS_CD = FSM.FINSCHOOLCD ";
        $query  .= "    LEFT JOIN NAME_MST NM_REL ON NM_REL.NAMECD1 = 'H201' AND NM_REL.NAMECD2 = AD1.RELATIONSHIP ";
        $query  .= " WHERE ";
        $query  .= "         T1.ENTEXAMYEAR  = '" . $model->examyear . "' ";
        $query  .= "     AND T1.APPLICANTDIV = '" . $model->applicantdiv . "' ";
        $query  .= "     AND T1.TESTDIV      = '" . $model->testdiv . "' ";
        if ($flg != "COUNT"){
            $query  .= " ORDER BY T1.EXAMNO ASC";
        }

        return $query;
    }
}
?>
