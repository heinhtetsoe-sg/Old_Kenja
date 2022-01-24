<?php

require_once('for_php7.php');

class knjl015pQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     FIN_M.FINSCHOOL_NAME, ";
        $query .= "     DETAIL012.REMARK1 AS ZENKI_EXAMNO, ";
        $query .= "     RECEPT2.RECEPTNO AS RECEPTNO2, ";
        $query .= "     BASE2.EXAMNO AS EXAMNO2, ";
        $query .= "     BASE2.NAME AS NAME2, ";
        $query .= "     FIN_M2.FINSCHOOL_NAME AS FINSCHOOL_NAME2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN_M ";
        $query .= "              ON T2.FS_CD        = FIN_M.FINSCHOOLCD ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT RECEPT2 ";
        $query .= "              ON T1.ENTEXAMYEAR  = RECEPT2.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV = RECEPT2.APPLICANTDIV ";
        $query .= "             AND RECEPT2.TESTDIV = '2' ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE2 ";
        $query .= "              ON RECEPT2.ENTEXAMYEAR  = BASE2.ENTEXAMYEAR ";
        $query .= "             AND RECEPT2.APPLICANTDIV = BASE2.APPLICANTDIV ";
        $query .= "             AND RECEPT2.EXAMNO       = BASE2.EXAMNO ";
        $query .= "             AND T2.NAME              = BASE2.NAME ";
        if ($model->whereDiv == "1") {
            $query .= "             AND VALUE(T2.FS_CD, '')             = VALUE(BASE2.FS_CD, '') ";
        }
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN_M2 ";
        $query .= "              ON BASE2.FS_CD        = FIN_M2.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL012 ";
        $query .= "           ON T1.ENTEXAMYEAR  = DETAIL012.ENTEXAMYEAR ";
        $query .= "          AND T1.APPLICANTDIV = DETAIL012.APPLICANTDIV ";
        $query .= "          AND T1.EXAMNO       = DETAIL012.EXAMNO ";
        $query .= "          AND DETAIL012.SEQ   = '012' ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR    = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV        = '{$model->testdiv}' ";
        if ($model->kakuteiDisp != "1") {
            $query .= "     AND DETAIL012.REMARK1 IS NULL ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.RECEPTNO ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $keyList = array();
        foreach ($model->field["CHK_DATA"] as $key => $val) {
            list($receptno, $receptno2) = explode("-", $key);
            if ($val == "1") {
                $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
                $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' ";
                $query .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
                $query .= "   AND EXAMNO        = '".$receptno."' ";
                $query .= "   AND SEQ           = '012' ";
                $db->query($query);

                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $receptno;
                $data["SEQ"][TEXT]              = "012";
                $data["REMARK1"][TEXT]          = $receptno2;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>