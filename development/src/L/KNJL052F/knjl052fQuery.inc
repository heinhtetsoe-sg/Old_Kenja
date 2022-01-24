<?php

require_once('for_php7.php');

class knjl052fQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試回数取得
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

    //志望区分取得
    function getEntExamCourse($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     COURSECD || MAJORCD || EXAMCOURSECD AS VALUE, ";
        $query .= "     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV         = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分(中学用)
    function getTestdivL024($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$model->ObjYear."' ";
        $query .= "     AND NAMECD1 = 'L024' ";
        $query .= "     AND NAMECD2 IN ( ";
        $query .= "         SELECT ";
        $query .= "             TESTDIV ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= "         WHERE ";
        $query .= "             ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "             AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "             AND EXAM_TYPE IN ('6','7','8') "; //受験型　「6:グローバル」「7:スポーツ」「8:サイエンス」
        $query .= "         GROUP BY ";
        $query .= "             TESTDIV ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     smallint(NAMECD2) "; //入試区分2桁対応
        return $query;
    }

    //受験型(中学用)
    function getExamType($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$model->ObjYear."' ";
        $query .= "     AND NAMECD1 = 'L005' ";
        $query .= "     AND NAMECD2 IN ('6','7','8') "; //受験型　「6:グローバル」「7:スポーツ」「8:サイエンス」
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         RECE.RECEPTNO, ";
        $query .= "         RECE.EXAMNO, ";
        $query .= "         BASE.NAME, ";
        $query .= "         FINSCH.FINSCHOOL_NAME, ";
        $query .= "         INTER.INTERVIEW_VALUE, ";
        $query .= "         INTER.INTERVIEW_REMARK ";
        $query .= "     FROM ";
        $query .= "         V_ENTEXAM_RECEPT_DAT RECE ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "              ON RECE.ENTEXAMYEAR   = BASE.ENTEXAMYEAR ";
        $query .= "             AND RECE.EXAMNO        = BASE.EXAMNO ";
        if ($model->applicantdiv == "2") {
            $query .= "         INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RDET_003 ";
            $query .= "              ON RECE.ENTEXAMYEAR    = RDET_003.ENTEXAMYEAR ";
            $query .= "             AND RECE.APPLICANTDIV   = RDET_003.APPLICANTDIV ";
            $query .= "             AND RECE.TESTDIV        = RDET_003.TESTDIV ";
            $query .= "             AND RECE.RECEPTNO       = RDET_003.RECEPTNO ";
            $query .= "             AND RDET_003.SEQ        = '003' ";
            $query .= "             AND RDET_003.REMARK1    = '".$model->testdiv0."' ";
            $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT ADET_001 ";
            $query .= "              ON RECE.ENTEXAMYEAR    = ADET_001.ENTEXAMYEAR ";
            $query .= "             AND RECE.EXAMNO         = ADET_001.EXAMNO ";
            $query .= "             AND ADET_001.SEQ        = '001' ";
            $query .= "             AND ADET_001.REMARK8 || ADET_001.REMARK9 || ADET_001.REMARK10 = '".$model->totalcd."' ";
        } else {
            $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BDET_011 ";
            $query .= "              ON RECE.ENTEXAMYEAR    = BDET_011.ENTEXAMYEAR ";
            $query .= "             AND RECE.EXAMNO         = BDET_011.EXAMNO ";
            $query .= "             AND BDET_011.SEQ        = '011' ";
            $query .= "             AND BDET_011.REMARK{$model->testdiv} = '".$model->exam_type."' ";
        }
        $query .= "         LEFT JOIN ENTEXAM_INTERVIEW_DAT INTER ";
        $query .= "              ON RECE.ENTEXAMYEAR    = INTER.ENTEXAMYEAR ";
        $query .= "             AND RECE.APPLICANTDIV   = INTER.APPLICANTDIV ";
        $query .= "             AND RECE.TESTDIV        = INTER.TESTDIV ";
        $query .= "             AND RECE.EXAMNO         = INTER.EXAMNO ";
        $query .= "         LEFT JOIN FINSCHOOL_MST FINSCH ";
        $query .= "              ON BASE.FS_CD          = FINSCH.FINSCHOOLCD ";
        $query .= "     WHERE ";
        $query .= "         RECE.ENTEXAMYEAR    = '".$model->ObjYear."' AND ";
        $query .= "         RECE.APPLICANTDIV   = '".$model->applicantdiv."' AND ";
        $query .= "         RECE.TESTDIV        = '".$model->testdiv."' ";
        if ($model->applicantdiv == "2" && $model->testdiv == "3") {
            $query .= "     AND VALUE(BASE.GENERAL_FLG, '') != '1' ";
        }
        if (strlen($model->special_reason_div)) {
            $query .= "     AND BASE.SPECIAL_REASON_DIV IS NOT NULL ";
        } else {
            $query .= "     AND VALUE(RECE.JUDGEDIV,'') <> '4' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= "  FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     RECEPTNO ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        $tmp = explode(",", $model->hid_receptno);
        $receptno_array = array();
        foreach ($tmp as $key => $receptno_count) {
            $receptno_array = explode("-", $receptno_count);
            $receptno   = $receptno_array[0];
            $examno     = $receptno_array[2];

            $query  = " DELETE FROM ENTEXAM_INTERVIEW_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
            $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
            $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
            $query .= "     EXAMNO          = '".$examno."' ";
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
            $data["TESTDIV"][TEXT]              = $model->testdiv;
            $data["EXAMNO"][TEXT]               = $examno;
            $data["INTERVIEW_VALUE"][TEXT]      = $model->interView_Value[$receptno_array[1]];
            $data["INTERVIEW_REMARK"][TEXT]     = $model->interView_Remark[$receptno_array[1]];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>