<?php

require_once('for_php7.php');

class knjl351wquery extends Query {

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

        $db = Query::dbCheckOut2();
        $rtnFlg = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtnFlg;
    }
    
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
    function get_name_cdAft($model, $namecd1, $aftField) {

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.{$aftField} AS VALUE, ";
        $query .= "     L1.NAME1, ";
        $query .= "     T1.{$aftField} || ':' || L1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_SINRO_ADDITION_L350W_FIELD_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = '{$namecd1}' ";
        $query .= "          AND T1.{$aftField} = L1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->entexamYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        if ($aftField == "TESTDIV") {
            $query .= "     AND T1.{$aftField} NOT IN ('4') ";
        }
        if ($aftField == "RUIKEI_DIV") {
            $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
            $query .= "     AND TESTDIV2 = '{$model->field["TESTDIV2"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象学校
    function getSchoolData($model)
    {
        $query .= " WITH REPORT_T AS ( ";
        $query .= " SELECT ";
        $query .= "     EDBOARD_SCHOOLCD, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        if ($model->field["RUIKEI_DIV"] != "ALL") {
            $query .= "     RUIKEI_DIV, ";
        }
        $query .= "     TESTDIV2, ";
        $query .= "     MAX(EXECUTE_DATE) AS EXECUTE_DATE, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     REPORT_AFT_SINRO_ADDITION_L350W_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->entexamYear}' ";
        $query .= " GROUP BY ";
        $query .= "     EDBOARD_SCHOOLCD, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        if ($model->field["RUIKEI_DIV"] != "ALL") {
            $query .= "     RUIKEI_DIV, ";
        }
        $query .= "     TESTDIV2 ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     ED_SCHOOL.EDBOARD_SCHOOLCD, ";
        $query .= "     ED_SCHOOL.EDBOARD_SCHOOLNAME, ";
        $query .= "     REPORT_T.EXECUTE_DATE, ";
        $query .= "     CASE WHEN VALUE(REPORT_T.CNT, 0) = 0 ";
        $query .= "          THEN '【未】' ";
        $query .= "          ELSE '　　　' ";
        $query .= "     END AS MITEISHUTSU ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST ED_SCHOOL ";
        $query .= "     LEFT JOIN REPORT_T ON ED_SCHOOL.EDBOARD_SCHOOLCD = REPORT_T.EDBOARD_SCHOOLCD ";
        $query .= "          AND REPORT_T.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "          AND REPORT_T.TESTDIV = '{$model->field["TESTDIV"]}' ";
        if ($model->field["RUIKEI_DIV"] != "ALL") {
            $query .= "          AND REPORT_T.RUIKEI_DIV = '{$model->field["RUIKEI_DIV"]}' ";
        }
        $query .= "          AND REPORT_T.TESTDIV2 = '{$model->field["TESTDIV2"]}' ";
        $query .= " ORDER BY ";
        $query .= "     ED_SCHOOL.EDBOARD_SCHOOLCD ";

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
    function getTitleQuery($model, $ruikeiDiv) {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AFT_SINRO_ADDITION_L350W_FIELD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->entexamYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND RUIKEI_DIV = '{$ruikeiDiv}' ";
        $query .= "     AND TESTDIV2 = '{$model->field["TESTDIV2"]}' ";
        $query .= " ORDER BY ";
        $query .= "     CASE WHEN LARGE_DIV IN ('98', '99') THEN 1 ELSE 2 END, ";
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
        $query .= "     AND NAMECD2 = '{$model->field["TESTDIV"]}' ";

        return $query;
    }

    //CSVデータ出力
    function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     L350W.*, ";
        $query .= "     CASE WHEN L350W.COURSECD = '9' ";
        $query .= "          THEN '合計' ";
        $query .= "          ELSE VALUE(MAJOR.MAJORNAME, '') || VALUE(COURSE.COURSECODENAME, '') ";
        $query .= "     END AS COURSENAME ";
        $query .= " FROM ";
        $query .= "     V_AFT_SINRO_ADDITION_L350W_DAT L350W ";
        $query .= "     LEFT JOIN EDBOARD_MAJOR_MST MAJOR ON L350W.EDBOARD_SCHOOLCD = MAJOR.EDBOARD_SCHOOLCD AND L350W.COURSECD || L350W.MAJORCD = MAJOR.COURSECD || MAJOR.MAJORCD ";
        $query .= "     LEFT JOIN EDBOARD_COURSECODE_MST COURSE ON L350W.EDBOARD_SCHOOLCD = COURSE.EDBOARD_SCHOOLCD AND L350W.COURSECODE = COURSE.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->entexamYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND TESTDIV2 = '{$model->field["TESTDIV2"]}' ";
        $query .= " ORDER BY ";
        $query .= "     L350W.EDBOARD_SCHOOLCD, ";
        $query .= "     L350W.RUIKEI_DIV, ";
        $query .= "     L350W.COURSECD, ";
        $query .= "     L350W.MAJORCD, ";
        $query .= "     L350W.COURSECODE ";

        return $query;
    }
}
?>
