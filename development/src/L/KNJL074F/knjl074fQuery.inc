<?php

require_once('for_php7.php');

class knjl074fQuery extends Query {

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
        if ($namecd1 == "L013") {
            $query .= "     AND NAMECD2 <> '4' ";
        }
        $query .= " ORDER BY ";
        if ($namecd1 == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }
        return $query;
    }

    //受験型
    function getExamType($year, $testdiv) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = 'L005' ";
        $query .= "     AND VALUE(ABBV2, '') IN ( ";
        $query .= "         SELECT ";
        $query .= "             VALUE(ABBV2, '') ";
        $query .= "         FROM ";
        $query .= "             V_NAME_MST ";
        $query .= "         WHERE ";
        $query .= "                 YEAR = '".$year."' ";
        $query .= "             AND NAMECD1 = 'L024' ";
        $query .= "             AND NAMECD2 = '".$testdiv."' ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //特待生情報
    function getJudgeKind($year, $applicantdiv) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME{$applicantdiv} AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = 'L025' ";
        $query .= "     AND NAME{$applicantdiv} IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE{$applicantdiv}, ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = "";
        $query .= "SELECT T1.RECEPTNO, ";
        $query .= "       T1.EXAMNO, ";
        $query .= "       T2.NAME, ";
        $query .= "       T2.JUDGE_KIND, ";
        $query .= "       T1.PROCEDUREDIV1, ";
        $query .= "       T1.TOTAL4, ";
        $query .= "       T1.JUDGEDIV, ";
        $query .= "       N1.NAME1 AS JUDGE_NAME, ";
        $query .= "       N2.NAME1 AS EXAM_TYPE_NAME, ";
        $query .= "       N3.NAME1 AS EIKEN_SHUTOKU_KYUU ";
        $query .= "  FROM ";
        $query .= "     V_ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT T4 ";
        $query .= "              ON T4.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T4.EXAMNO       = T1.EXAMNO ";
        $query .= "             AND T4.SEQ          = '011' ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON N1.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "             AND N1.NAMECD1      = 'L013' ";
        $query .= "             AND N1.NAMECD2      = T1.JUDGEDIV ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ";
        $query .= "              ON N2.YEAR         = T4.ENTEXAMYEAR ";
        $query .= "             AND N2.NAMECD1      = 'L005' ";
        $query .= "             AND N2.NAMECD2      = T4.REMARK{$model->testdiv} ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_014 ";
        $query .= "              ON BASE_D_014.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND BASE_D_014.EXAMNO      = T1.EXAMNO ";
        $query .= "             AND BASE_D_014.SEQ         = '014' ";
        $query .= "     LEFT JOIN V_NAME_MST N3 ";
        $query .= "              ON N3.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "             AND N3.NAMECD1      = 'L055' ";
        $query .= "             AND N3.NAMECD2      = BASE_D_014.REMARK9 ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->year."'";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND N1.NAMESPARE1     = '1' ";//合格者
        //特別措置者のみ
        if (strlen($model->special_reason_div)) {
            $query .= "     AND T2.SPECIAL_REASON_DIV IS NOT NULL ";
        } else {
            $query .= "     AND VALUE(T1.JUDGEDIV, '0') <> '4' "; //4:欠席は対象外
        }
        if ($model->target == "2") {
            $query .= "     AND VALUE(T2.GENERAL_FLG, '') != '1' ";
        }
        if ($model->target == "3") {
            $query .= "     AND VALUE(T2.GENERAL_FLG, '')  = '1' ";
        }
        //受験型(EXAM_TYPE)
        if (strlen($model->exam_type)) {
            $query .= "   AND T4.REMARK{$model->testdiv} = '".$model->exam_type."' ";
        }
        //抽出合計点
        if (strlen($model->avg_from)) {
            $query .= "   AND T1.TOTAL4 >= {$model->avg_from} ";
        }
        if (strlen($model->avg_to)) {
            $query .= "   AND T1.TOTAL4 <  {$model->avg_to} ";
        }
        $query .= " ORDER BY ";
        //表示順序ラジオボタン 1:成績順 2:受験番号順
        if ($model->sort == "1") $query .= "     value(T1.TOTAL4, -1) DESC, ";
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

            //受付データ
            $data = array();
            $data["HONORDIV"][TEXT]         = $model->field["JUDGE_KIND"][$receptno];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND TESTDIV       = '".$model->testdiv."' ";
            $where .= "   AND RECEPTNO      = '".$receptno."' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //基礎データ
            $data = array();
            $data["JUDGE_KIND"][TEXT]       = $model->field["JUDGE_KIND"][$receptno];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO        = '".$examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>