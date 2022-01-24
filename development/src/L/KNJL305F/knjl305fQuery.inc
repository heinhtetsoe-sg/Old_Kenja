<?php

require_once('for_php7.php');

class knjl305fQuery extends Query {

    //中学は固定
    function getTestDivKotei() {
        $query  = " WITH VAL_T(LABEL, VALUE) AS ( ";
        $query .= " VALUES('1:帰国生入試', '1') ";
        $query .= " UNION ";
        $query .= " VALUES('2:一般入試', '2') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     VAL_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //中学は固定
    function getTestDivKoteiVal($testdiv) {
        $query  = " WITH VAL_T(LABEL, VALUE) AS ( ";
        $query .= " VALUES('帰国生', '1') ";
        $query .= " UNION ";
        $query .= " VALUES('一般', '2') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     LABEL ";
        $query .= " FROM ";
        $query .= "     VAL_T ";
        $query .= " WHERE ";
        $query .= "     VALUE = '".$testdiv."' ";

        return $query;
    }

    //名称マスタより取得
    function getNameMst($year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得
    function getNameMstVal($year, $field, $namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     ".$field." ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

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

    function getCsvQuery($year, $model, $i)
    {
        $query .= " WITH BASE AS ( ";
        $query .= "     SELECT ";
        $query .= "         BASE.EXAMNO, ";
        $query .= "         BASE.NAME_KANA, ";
        $query .= "         BASE.FS_CD, ";
        $query .= "         T6.FINSCHOOL_NAME, ";
        $query .= "         ENT_COURSE.EXAMCOURSE_ABBV AS EXAM_COURSEMARK1 ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL001 ON BASE.ENTEXAMYEAR = DETAIL001.ENTEXAMYEAR ";
        $query .= "              AND BASE.EXAMNO = DETAIL001.EXAMNO ";
        $query .= "              AND DETAIL001.SEQ = '001' ";
        $query .= "         LEFT JOIN ENTEXAM_COURSE_MST ENT_COURSE ON BASE.ENTEXAMYEAR = ENT_COURSE.ENTEXAMYEAR ";
        $query .= "             AND BASE.APPLICANTDIV = ENT_COURSE.APPLICANTDIV ";
        $query .= "             AND ENT_COURSE.TESTDIV = '1' ";
        $query .= "             AND DETAIL001.REMARK8 = ENT_COURSE.COURSECD ";
        $query .= "             AND DETAIL001.REMARK9 = ENT_COURSE.MAJORCD ";
        $query .= "             AND DETAIL001.REMARK10 = ENT_COURSE.EXAMCOURSECD ";
        $query .= "         LEFT JOIN FINSCHOOL_MST T6 ON T6.FINSCHOOLCD = BASE.FS_CD ";
        $query .= "     WHERE ";
        $query .= "         BASE.ENTEXAMYEAR = '".$year."' ";
        $query .= "         AND BASE.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "         AND BASE.TESTDIV = '".$model->field["TESTDIV"]."' ";
        if ($model->field["APPLICANTDIV"] == "2") {
            $query .= "         AND BASE.TESTDIV0 = '".$model->field["TESTDIV0"]."' ";
        }
        $query .= " ), MAX_RECRUIT_VISIT AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.RECRUIT_NO, ";
        $query .= "     T1.JUDGE_KIND, ";
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "     L1.NAME1 AS JUDGENAME ";
        } else {
            $query .= "     L1.NAME2 AS JUDGENAME ";
        }
        $query .= " FROM ";
        $query .= "     RECRUIT_VISIT_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L025' ";
        $query .= "          AND T1.JUDGE_KIND = L1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= " ), APPLICANT_BEFORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         BEF.RECRUIT_NO, ";
        $query .= "         BEF.NAME_KANA, ";
        $query .= "         BEF.FINSCHOOLCD AS FS_CD, ";
        $query .= "         T6.FINSCHOOL_NAME, ";
        $query .= "         RVISIT.JUDGENAME AS JUDGENAME ";
        $query .= "     FROM ";
        $query .= "         RECRUIT_DAT BEF ";
        $query .= "         LEFT JOIN V_FINSCHOOL_MST T6 ON T6.YEAR = BEF.YEAR ";
        $query .= "            AND T6.FINSCHOOLCD = BEF.FINSCHOOLCD ";
        $query .= "         LEFT JOIN MAX_RECRUIT_VISIT RVISIT ON BEF.RECRUIT_NO = RVISIT.RECRUIT_NO ";
        $query .= "     WHERE ";
        $query .= "         BEF.YEAR = '".$year."' ";
        $query .= " ), COMMON AS ( ";
        $query .= "     SELECT DISTINCT BASE.EXAMNO, BEF.RECRUIT_NO AS REMARK1 ";
        $query .= "     FROM ( ";
        $query .= "       SELECT NAME_KANA, FS_CD FROM BASE ";
        $query .= "       INTERSECT ";
        $query .= "       SELECT NAME_KANA, FS_CD FROM APPLICANT_BEFORE ";
        $query .= "     ) T1 ";
        $query .= "     INNER JOIN BASE ON BASE.NAME_KANA = T1.NAME_KANA ";
        $query .= "         AND BASE.FS_CD = T1.FS_CD ";
        $query .= "     INNER JOIN APPLICANT_BEFORE BEF ON BEF.NAME_KANA = T1.NAME_KANA ";
        $query .= "         AND BEF.FS_CD = T1.FS_CD ";
        $query .= " ) ";
        if ($i == 0) {
            $query .= " SELECT ";
            $query .= "     T1.EXAMNO AS BASE_EXAMNO, ";
            $query .= "     T1.NAME_KANA AS BASE_NAME_KANA, ";
            $query .= "     T1.FS_CD AS BASE_FS_CD, ";
            $query .= "     T1.FINSCHOOL_NAME AS BASE_FINSCHOOL_NAME, ";
            $query .= "     T1.EXAM_COURSEMARK1 AS BASE_EXAM_COURSEMARK1, ";
            $query .= "     T2.RECRUIT_NO, ";
            $query .= "     T2.NAME_KANA AS BEF_NAME_KANA, ";
            $query .= "     T2.FS_CD AS BEF_FS_CD, ";
            $query .= "     T2.FINSCHOOL_NAME AS BEF_FINSCHOOL_NAME, ";
            $query .= "     T2.JUDGENAME AS BEF_JUDGENAME ";
            $query .= " FROM COMMON T0 ";
            $query .= " INNER JOIN BASE T1 ON T1.EXAMNO = T0.EXAMNO ";
            $query .= " INNER JOIN APPLICANT_BEFORE T2 ON T2.RECRUIT_NO = T0.REMARK1 ";
        } else if ($i == 1) {
            $query .= " SELECT ";
            $query .= "     T1.EXAMNO AS BASE_EXAMNO, ";
            $query .= "     T1.NAME_KANA AS BASE_NAME_KANA, ";
            $query .= "     T1.FS_CD AS BASE_FS_CD, ";
            $query .= "     T1.FINSCHOOL_NAME AS BASE_FINSCHOOL_NAME, ";
            $query .= "     T1.EXAM_COURSEMARK1 AS BASE_EXAM_COURSEMARK1, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS RECRUIT_NO, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_NAME_KANA, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_FS_CD, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_FINSCHOOL_NAME, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_JUDGENAME ";
            $query .= " FROM BASE T1 ";
            $query .= " WHERE NOT EXISTS (SELECT 'X' FROM COMMON T0 WHERE T1.EXAMNO = T0.EXAMNO) ";
        } else if ($i == 2) {
            $query .= " SELECT ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BASE_EXAMNO, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BASE_NAME_KANA, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BASE_FS_CD, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BASE_FINSCHOOL_NAME, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BASE_EXAM_COURSEMARK1, ";
            $query .= "     T2.RECRUIT_NO, ";
            $query .= "     T2.NAME_KANA AS BEF_NAME_KANA, ";
            $query .= "     T2.FS_CD AS BEF_FS_CD, ";
            $query .= "     T2.FINSCHOOL_NAME AS BEF_FINSCHOOL_NAME, ";
            $query .= "     T2.JUDGENAME AS BEF_JUDGENAME ";
            $query .= " FROM APPLICANT_BEFORE T2 ";
            $query .= " WHERE NOT EXISTS (SELECT 'X' FROM COMMON T0 WHERE T2.RECRUIT_NO = T0.REMARK1 ) ";
        }
        $query .= " ORDER BY ";
        if ("1" == $model->field["OUTPUT"]) {
            $query .= "     BASE_EXAMNO ";
        } else {
            $query .= "     RECRUIT_NO, ";
            $query .= "     BASE_EXAMNO ";
        }

        return $query;
    }

}
?>
