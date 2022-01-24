<?php

require_once('for_php7.php');

class knjl327wQuery extends Query {

    //権限チェック（入試管理者）
    function getAdminFlg() {
        $query  = " SELECT ";
        $query .= "     FIELD1 ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";
        $query .= "     AND STAFF_SEQ = '009' ";

        $db = Query::dbCheckOut();
        $rtnFlg = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtnFlg;
    }
    
    //名称マスタより取得
    function getNameMst($year, $namecd1, $style = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L004") {
            if ($style == "1") {
                $query .= "     AND NAMECD2 IN ('1','2','4') ";
            } else if ($style == "2") {
                $query .= "     AND NAMECD2 IN ('1','2','3','4') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //内定者一覧取得
    function getRightList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.EXAMNO AS VALUE, ";
        $query .= "     T1.EXAMNO || '　' || T1.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = T1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        if ($model->style == "2") {
            if ($model->testdiv == "1") {
                $query .= "     AND (T1.TESTDIV = '".$model->testdiv."' ";
                $query .= "          OR ";
                $query .= "          (T1.TESTDIV = '4' AND VALUE(T1.JUDGEMENT, '') = '3') ";
                $query .= "         ) ";
            } else if ($model->testdiv == "4") {
                $query .= "     AND T1.TESTDIV      = '".$model->testdiv."' ";
                $query .= "     AND VALUE(T1.JUDGEMENT, '') != '3' ";
            } else {
                $query .= "     AND T1.TESTDIV      = '".$model->testdiv."' ";
            }
            $query .= "     AND N1.NAMESPARE1   = '1' ";
        } else {
            $query .= "     AND T1.TESTDIV      = '".$model->testdiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //合格発表日(午前、午後)
    function getSucAmPm() {
        $query  = " WITH T_COMMON(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1', '午前') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2', '午後') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //合格発表日(時)
    function getSucHour() {
        $query  = " WITH T_COMMON(SORT, VALUE, LABEL) AS ( ";
        for ($i = 0; $i <= 12; $i++) {
            if ($i > 0) $query .= "     UNION ALL ";
            $hour = $i;
            $sort = sprintf("%02d", $i);
            $query .= "     VALUES('{$sort}', '{$hour}', '{$hour}時') ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "     SORT ";
        return $query;
    }

    //合格発表日(分)
    function getSucMinute() {
        $query  = " WITH T_COMMON(VALUE, LABEL) AS ( ";
        for ($i = 0; $i <= 59; $i++) {
            if ($i > 0) $query .= "     UNION ALL ";
            $minute = sprintf("%02d", $i);
            $query .= "     VALUES('{$minute}', '{$minute}分') ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
}
?>
