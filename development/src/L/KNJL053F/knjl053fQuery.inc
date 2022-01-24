<?php

require_once('for_php7.php');

class knjl053fQuery extends Query {

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
        if ($namecd1 == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //名称マスタより取得
    function getNameMstL035($namecd1, $year, $namecd2 = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 || '(' || ABBV2 || ')' AS LABEL ";
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
        $query .= "SELECT T1.RECEPTNO, ";
        $query .= "       T1.EXAMNO, ";
        $query .= "       T2.GENERAL_FLG, ";
        $query .= "       Q1.SH_FLG, ";
        for ($i = 1; $i <= 6; $i++) {
            $query .= "       Q1.SH_SCHOOLNAME{$i}, ";
            $query .= "       Q1.SH_JUDGEMENT{$i}, ";
        }
        $query .= "       T2.NAME ";
        $query .= "  FROM ";
        $query .= "     V_ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_QUESTIONNAIRE_DAT Q1 ";
        $query .= "              ON Q1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND Q1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND Q1.EXAMNO       = T1.EXAMNO ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->year."'";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.TESTDIV        = '".$model->testdiv."' ";
        //特別入試対象者は除く
        if ($model->chkUnShow == "on") {
            $query .= "   AND VALUE(T2.GENERAL_FLG, '0') <> '1' ";
        }
        if (strlen($model->special_reason_div)) {
            $query .= "   AND T2.SPECIAL_REASON_DIV IS NOT NULL ";
        } else {
            $query .= "   AND VALUE(T1.JUDGEDIV,'') <> '4' ";
        }
        if (strlen($model->s_receptno)) {
            $query .= "   AND T1.RECEPTNO >= '{$model->s_receptno}' ";
        }
        if (strlen($model->e_receptno)) {
            $query .= "   AND T1.RECEPTNO <= '{$model->e_receptno}' ";
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
        $keyList = explode(",", $model->hid_examno);
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];

            $data = array();
            $data["SH_FLG"][TEXT]           = $model->field["SH_FLG"][$receptno];
            for ($i = 1; $i <= 6; $i++) {
                $data["SH_SCHOOLNAME{$i}"][TEXT]    = $model->field["SH_SCHOOLNAME{$i}"][$receptno];
                $data["SH_JUDGEMENT{$i}"][TEXT]     = $model->field["SH_JUDGEMENT{$i}"][$receptno];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND TESTDIV       = '1' "; //固定
            $where .= "   AND EXAMNO        = '".$examno."'";
            $query  = " SELECT COUNT(*) FROM ENTEXAM_QUESTIONNAIRE_DAT " .$where;
            if (0 < $db->getOne($query)) {
                $query = Query::updateSQL($data, "ENTEXAM_QUESTIONNAIRE_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["TESTDIV"][TEXT]      = "1"; //固定
                $data["EXAMNO"][TEXT]       = $examno;
                $query = Query::insertSQL($data, "ENTEXAM_QUESTIONNAIRE_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>