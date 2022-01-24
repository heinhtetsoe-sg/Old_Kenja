<?php

require_once('for_php7.php');

class knjl075fQuery extends Query {

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
        $query .= "     VALUE ";
        return $query;
    }

    //入試回数(2:高校のみ)
    function getTestdiv0($year, $testdiv) {
        $query  = " WITH T_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '1' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '2' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '3' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '4' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '5' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L059' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 || '（' || L1.DATE || '）' AS LABEL, ";
        $query .= "     T1.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     LEFT JOIN T_DATE L1 ON L1.TESTDIV0 = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.NAMECD1 = 'L034' ";
        $query .= "     AND L1.DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース区分
    function getExamcourse1() {
        $query  = " WITH T_COMMON (VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1', '1:理数キャリア') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2', '2:国際教養') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('3', '3:スポーツ科学') ";
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
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
        $query .= "     YEAR = '{$year}' ";
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
        $query .= "       T1.TOTAL2, ";
        $query .= "       T1.JUDGEDIV, ";
        $query .= "       N1.NAME1 AS JUDGE_NAME, ";
        $query .= "       T2.SUC_COURSECD || '-' || T2.SUC_MAJORCD || '-' || T2.SUC_COURSECODE AS SUC_COURSE, ";
        $query .= "       C2.EXAMCOURSE_ABBV AS SUC_COURSE_NAME, ";
        $query .= "       T3.REMARK8 || '-' || T3.REMARK9 || '-' || T3.REMARK10 AS EXAMCOURSE, ";
        $query .= "       C3.EXAMCOURSE_ABBV AS EXAMCOURSE_NAME ";
        $query .= "  FROM ";
        $query .= "     V_ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3 ";
        $query .= "              ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T3.EXAMNO       = T1.EXAMNO ";
        $query .= "             AND T3.SEQ          = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C2 ";
        $query .= "              ON C2.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "             AND C2.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "             AND C2.TESTDIV      = '1' ";//固定
        $query .= "             AND C2.COURSECD     = T2.SUC_COURSECD ";
        $query .= "             AND C2.MAJORCD      = T2.SUC_MAJORCD ";
        $query .= "             AND C2.EXAMCOURSECD = T2.SUC_COURSECODE ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C3 ";
        $query .= "              ON C3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND C3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND C3.TESTDIV      = '1' ";//固定
        $query .= "             AND C3.COURSECD     = T3.REMARK8 ";
        $query .= "             AND C3.MAJORCD      = T3.REMARK9 ";
        $query .= "             AND C3.EXAMCOURSECD = T3.REMARK10 ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON N1.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "             AND N1.NAMECD1      = 'L013' ";
        $query .= "             AND N1.NAMECD2      = T1.JUDGEDIV ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T4 ";
        $query .= "              ON T4.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T4.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T4.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND T4.RECEPTNO     = T1.RECEPTNO ";
        $query .= "             AND T4.SEQ          = '003' ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->year."'";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND T4.REMARK1        = '".$model->testdiv0."' ";
        $query .= "   AND T3.REMARK10 LIKE '".$model->examcourse1."%' ";
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
        //抽出合計点
        if (strlen($model->avg_from)) {
            $query .= "   AND T1.TOTAL2 >= {$model->avg_from} ";
        }
        if (strlen($model->avg_to)) {
            $query .= "   AND T1.TOTAL2 <  {$model->avg_to} ";
        }
        $query .= " ORDER BY ";
        //表示順序ラジオボタン 1:成績順 2:受験番号順
        if ($model->sort == "1") $query .= "     value(T1.TOTAL2, -1) DESC, ";
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