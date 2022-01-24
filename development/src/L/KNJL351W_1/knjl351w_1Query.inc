<?php

require_once('for_php7.php');

class knjl351w_1query extends Query {


    //名称マスタよりデータを取得
    function get_name_cd($year, $cd, $sitei = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$cd}' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '{$sitei}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //TITLEデータ
    function get_name_cdAft($model, $namecd1, $aftField, $applicantDiv, $testDiv = "", $testDiv2 = "") {

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.{$aftField} AS VALUE, ";
        $query .= "     L1.NAME1, ";
        $query .= "     L1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_SINRO_ADDITION_L350W_FIELD_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = '{$namecd1}' ";
        $query .= "          AND T1.{$aftField} = L1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->entexamYear}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        if ($aftField == "TESTDIV") {
            $query .= "     AND T1.{$aftField} NOT IN ('4') ";
        }
        if ($aftField == "RUIKEI_DIV") {
            $query .= "     AND TESTDIV = '{$testDiv}' ";
            $query .= "     AND TESTDIV2 = '{$testDiv2}' ";
            $query .= "     AND T1.TESTDIV || T1.RUIKEI_DIV || T1.TESTDIV2 || T1.LARGE_DIV NOT IN ('204098', '204099') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校のコース
    function getSchoolCourse($model, $schoolCd) {
        $query  = " SELECT DISTINCT ";
        $query .= "     L350W.COURSECD, ";
        $query .= "     COURSE.COURSENAME ";
        $query .= " FROM ";
        $query .= "     V_AFT_SINRO_ADDITION_L350W_DAT L350W ";
        $query .= "     LEFT JOIN COURSE_MST COURSE ON L350W.COURSECD = COURSE.COURSECD ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '{$schoolCd}' ";
        $query .= "     AND YEAR = '{$model->entexamYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND L350W.COURSECD != '9' ";
        $query .= " ORDER BY ";
        $query .= "     COURSECD ";
        return $query;
    }

    //TITLEデータ
    function getTitleQuery($model, $applicantDiv, $testDiv, $testDiv2, $ruikeiDiv) {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AFT_SINRO_ADDITION_L350W_FIELD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->entexamYear}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "     AND TESTDIV = '{$testDiv}' ";
        $query .= "     AND RUIKEI_DIV = '{$ruikeiDiv}' ";
        $query .= "     AND TESTDIV2 = '{$testDiv2}' ";
        $query .= "     AND TESTDIV || RUIKEI_DIV || TESTDIV2 || LARGE_DIV NOT IN ('204098', '204099') ";
        $query .= "     AND LARGE_DIV NOT IN ('98') ";
        $query .= " ORDER BY ";
        $query .= "     CASE WHEN LARGE_DIV IN ('98', '99') THEN 1 ELSE 2 END, ";
        $query .= "     LARGE_DIV ";

        return $query;
    }

    //追検査TITLEデータ
    function getTitleTsuiKensaQuery($model, $applicantDiv, $testDiv2) {

        $query  = " SELECT ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     RUIKEI_DIV, ";
        $query .= "     TESTDIV2, ";
        $query .= "     LARGE_DIV, ";
        $query .= "     MAX(VALUE(LARGE_NAME, '')) AS LARGE_NAME ";
        $query .= " FROM ";
        $query .= "     AFT_SINRO_ADDITION_L350W_FIELD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->entexamYear}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "     AND TESTDIV2 = '{$testDiv2}' ";
        $query .= "     AND LARGE_DIV NOT IN ('98', '99') ";
        $query .= " GROUP BY ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     RUIKEI_DIV, ";
        $query .= "     TESTDIV2, ";
        $query .= "     LARGE_DIV ";
        $query .= " ORDER BY ";
        $query .= "     RUIKEI_DIV, ";
        $query .= "     LARGE_DIV ";

        return $query;
    }

    //入試区分名称
    function getTestDivName($model) {

        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L004' ";
        $query .= "     AND NAMECD2 = '{$model->entexamYear}' ";

        return $query;
    }

    //定員
    function getCapacity($model) {

        $query  = " SELECT ";
        $query .= "     EDBOARD_SCHOOLCD, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD, ";
        $query .= "     MAX(VALUE(CAPACITY, 0)) AS CAPACITY, ";
        $query .= "     MAX(VALUE(CAPACITY2, 0)) AS CAPACITY2 ";
        $query .= " FROM ";
        $query .= "     EDBOARD_ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->entexamYear}' ";
        $query .= " GROUP BY ";
        $query .= "     EDBOARD_SCHOOLCD, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD ";

        return $query;
    }

    //CSV1
    function getCsvQuery1($model, $selArry, $addidx)
    {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     MAJOR.COURSECD, ";
        $query .= "     MAJOR.COURSENAME, ";
        $query .= "     MAJOR.MAJORCD, ";
        $query .= "     MAJOR.MAJORNAME, ";
        $query .= "     COURSECODE.COURSECODE, ";
        $query .= "     COURSECODE.COURSECODENAME, ";
        $query .= "     ED_SCHOOL.EDBOARD_SCHOOLCD, ";
        $query .= "     ED_SCHOOL.EDBOARD_SCHOOLNAME ";
        foreach ($selArry as $key => $val) {
            $n = $key + $addidx;
            $query .= "     , L350W{$key}.COUNT AS COUNT{$n} ";
        }
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST ED_SCHOOL ";
        $query .= "     LEFT JOIN V_EDBOARD_COURSE_MAJOR_MST MAJOR ON MAJOR.EDBOARD_SCHOOLCD = ED_SCHOOL.EDBOARD_SCHOOLCD ";
        $query .= "          AND MAJOR.YEAR = '{$model->entexamYear}' ";
        $query .= "     LEFT JOIN V_EDBOARD_COURSECODE_MST COURSECODE ON COURSECODE.EDBOARD_SCHOOLCD = ED_SCHOOL.EDBOARD_SCHOOLCD ";
        $query .= "          AND COURSECODE.YEAR = '{$model->entexamYear}' ";
        foreach ($selArry as $key => $val) {
            $query .= "     LEFT JOIN AFT_SINRO_ADDITION_L350W_DAT L350W{$key} ON L350W{$key}.EDBOARD_SCHOOLCD = ED_SCHOOL.EDBOARD_SCHOOLCD ";
            $query .= "          AND L350W{$key}.YEAR = '{$model->entexamYear}' ";
            $query .= "          AND L350W{$key}.APPLICANTDIV || L350W{$key}.TESTDIV || L350W{$key}.RUIKEI_DIV || L350W{$key}.TESTDIV2 || L350W{$key}.LARGE_DIV = '{$val}' ";
            $query .= "          AND L350W{$key}.SEX = '9' ";
            $query .= "          AND L350W{$key}.COURSECD = MAJOR.COURSECD ";
            $query .= "          AND L350W{$key}.MAJORCD = MAJOR.MAJORCD ";
            $query .= "          AND L350W{$key}.COURSECODE = COURSECODE.COURSECODE ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.COURSECD, ";
        $query .= "     MAIN_T.COURSENAME, ";
        $query .= "     MAIN_T.MAJORCD, ";
        $query .= "     MAIN_T.MAJORNAME, ";
        $query .= "     MAIN_T.COURSECODE, ";
        $query .= "     MAIN_T.COURSECODENAME, ";
        $query .= "     MAIN_T.EDBOARD_SCHOOLCD, ";
        $query .= "     MAIN_T.EDBOARD_SCHOOLNAME ";
        foreach ($selArry as $key => $val) {
            $n = $key + $addidx;
            $query .= "     , MAIN_T.COUNT{$n} ";
        }
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     MAIN_T.COURSECD, ";
        $query .= "     MAIN_T.EDBOARD_SCHOOLCD, ";
        $query .= "     MAIN_T.MAJORCD, ";
        $query .= "     MAIN_T.COURSECODE ";

        return $query;
    }

    //CSV1追検査
    function getCsvQuery1TsuiKensa($model, $selArry, $addidx)
    {
        $sep = "";
        $query  = " WITH ";
        foreach ($selArry as $key => $val) {
            $query .= $sep;
            $query .= " MAIN_T{$key} AS ( ";
            $query .= " SELECT ";
            $query .= "     MAJOR.COURSECD, ";
            $query .= "     MAJOR.MAJORCD, ";
            $query .= "     COURSECODE.COURSECODE, ";
            $query .= "     ED_SCHOOL.EDBOARD_SCHOOLCD, ";
            $query .= "     SUM(VALUE(L350W.COUNT, 0)) AS COUNT ";
            $query .= " FROM ";
            $query .= "     EDBOARD_SCHOOL_MST ED_SCHOOL ";
            $query .= "     LEFT JOIN V_EDBOARD_COURSE_MAJOR_MST MAJOR ON MAJOR.EDBOARD_SCHOOLCD = ED_SCHOOL.EDBOARD_SCHOOLCD ";
            $query .= "          AND MAJOR.YEAR = '{$model->entexamYear}' ";
            $query .= "     LEFT JOIN V_EDBOARD_COURSECODE_MST COURSECODE ON COURSECODE.EDBOARD_SCHOOLCD = ED_SCHOOL.EDBOARD_SCHOOLCD ";
            $query .= "          AND COURSECODE.YEAR = '{$model->entexamYear}' ";
            $query .= "     LEFT JOIN AFT_SINRO_ADDITION_L350W_DAT L350W ON L350W.EDBOARD_SCHOOLCD = ED_SCHOOL.EDBOARD_SCHOOLCD ";
            $query .= "          AND L350W.YEAR = '{$model->entexamYear}' ";
            $query .= "          AND L350W.APPLICANTDIV || L350W.RUIKEI_DIV || L350W.TESTDIV2 || L350W.LARGE_DIV = '{$val}' ";
            $query .= "          AND L350W.SEX = '9' ";
            $query .= "          AND L350W.COURSECD = MAJOR.COURSECD ";
            $query .= "          AND L350W.MAJORCD = MAJOR.MAJORCD ";
            $query .= "          AND L350W.COURSECODE = COURSECODE.COURSECODE ";
            $query .= " GROUP BY ";
            $query .= "     MAJOR.COURSECD, ";
            $query .= "     MAJOR.MAJORCD, ";
            $query .= "     COURSECODE.COURSECODE, ";
            $query .= "     ED_SCHOOL.EDBOARD_SCHOOLCD ";
            $query .= " ) ";
            $sep = ",";
        }
        $query .= " , MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     MAJOR.COURSECD, ";
        $query .= "     MAJOR.MAJORCD, ";
        $query .= "     COURSECODE.COURSECODE, ";
        $query .= "     ED_SCHOOL.EDBOARD_SCHOOLCD ";
        foreach ($selArry as $key => $val) {
            $n = $key + $addidx;
            $query .= "     , MAIN_T{$key}.COUNT AS COUNT{$n} ";
        }
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST ED_SCHOOL ";
        $query .= "     LEFT JOIN V_EDBOARD_COURSE_MAJOR_MST MAJOR ON MAJOR.EDBOARD_SCHOOLCD = ED_SCHOOL.EDBOARD_SCHOOLCD ";
        $query .= "          AND MAJOR.YEAR = '{$model->entexamYear}' ";
        $query .= "     LEFT JOIN V_EDBOARD_COURSECODE_MST COURSECODE ON COURSECODE.EDBOARD_SCHOOLCD = ED_SCHOOL.EDBOARD_SCHOOLCD ";
        $query .= "          AND COURSECODE.YEAR = '{$model->entexamYear}' ";
        foreach ($selArry as $key => $val) {
            $query .= "     LEFT JOIN MAIN_T{$key} ON MAIN_T{$key}.EDBOARD_SCHOOLCD = ED_SCHOOL.EDBOARD_SCHOOLCD ";
            $query .= "          AND MAIN_T{$key}.COURSECD = MAJOR.COURSECD ";
            $query .= "          AND MAIN_T{$key}.MAJORCD = MAJOR.MAJORCD ";
            $query .= "          AND MAIN_T{$key}.COURSECODE = COURSECODE.COURSECODE ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.COURSECD, ";
        $query .= "     MAIN_T.MAJORCD, ";
        $query .= "     MAIN_T.COURSECODE, ";
        $query .= "     MAIN_T.EDBOARD_SCHOOLCD ";
        foreach ($selArry as $key => $val) {
            $n = $key + $addidx;
            $query .= "     , MAIN_T.COUNT{$n} ";
        }
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     MAIN_T.COURSECD, ";
        $query .= "     MAIN_T.EDBOARD_SCHOOLCD, ";
        $query .= "     MAIN_T.MAJORCD, ";
        $query .= "     MAIN_T.COURSECODE ";

        return $query;
    }

}
?>
