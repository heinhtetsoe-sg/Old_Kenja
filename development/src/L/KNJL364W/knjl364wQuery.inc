<?php

require_once('for_php7.php');

class knjl364wquery extends Query {
    //学校一覧取得
    function getSchoolData($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.EDBOARD_SCHOOLCD, ";
        $query .= "     T1.EDBOARD_SCHOOLNAME, ";
        $query .= "     L1.EXECUTE_DATE, ";
        $query .= "     CASE WHEN L1.EDBOARD_SCHOOLCD IS NOT NULL ";
        $query .= "          THEN '　　　' ";
        $query .= "          ELSE '【未】' ";
        $query .= "     END AS MITEISHUTSU ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN REPORT_ENTEXAM_L340W_DAT L1 ";
        $query .= "          ON T1.EDBOARD_SCHOOLCD = L1.EDBOARD_SCHOOLCD ";
        $query .= "         AND L1.ENTEXAMYEAR      = '".$model->ObjYear."' ";
        $query .= "         AND L1.CSV_PRG          = 4 ";
        $query .= "         AND L1.CSVDIV           = 0 ";
        $query .= " ORDER BY ";
        $query .= "     T1.EDBOARD_SCHOOLCD ";

        return $query;
    }

    //学校情報取得
    function getSchoolInfo($model, $schoolCd) {
        $query  = " SELECT ";
        $query .= "     T1.EDBOARD_SCHOOLNAME, ";
        $query .= "     L1.NAME1 AS DIST_NAME ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST T1, ";
        $query .= "     FINSCHOOL_MST T2 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'Z015' AND T2.FINSCHOOL_DISTCD2 = L1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD = T2.FINSCHOOLCD AND ";
        $query .= "     T1.EDBOARD_SCHOOLCD = '".$schoolCd."' ";

        return $query;
    }

    //課程名取得
    function getCourseName($model, $schoolCd) {
        $query  = " SELECT ";
        $query .= "     COURSENAME ";
        $query .= " FROM ";
        $query .= "     EDBOARD_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$schoolCd."' AND ";
        $query .= "     COURSECD IN (SELECT ";
        $query .= "                     MIN(COURSECD) AS COURSECD ";
        $query .= "                 FROM ";
        $query .= "                     V_EDBOARD_COURSE_MST ";
        $query .= "                 WHERE ";
        $query .= "                     EDBOARD_SCHOOLCD = '".$schoolCd."' AND ";
        $query .= "                     YEAR = '".CTRL_YEAR."' ";
        $query .= "                 ) ";

        return $query;
    }

    //CSVデータ出力
    function selectCsvQuery($model, $schoolCd) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SEX, ";
        $query .= "     N1.ABBV1 AS SEX_NAME, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME, ";
        $query .= "     T1.SUC_COURSECD || T1.SUC_MAJORCD AS SUC_MAJOR, ";
        $query .= "     L1.COURSENAME || L2.MAJORNAME AS SUC_MAJOR_NAME, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     N2.NAME1 AS TESTDIV_NAME, ";
        $query .= "     CASE WHEN T1.LAST_DAI2_COURSECD || T1.LAST_DAI2_MAJORCD || T1.LAST_DAI2_COURSECODE = T1.SUC_COURSECD || T1.SUC_MAJORCD || T1.SUC_COURSECODE THEN '○' ELSE '' END AS DAI2_SHIBOU, ";
        $query .= "     CASE WHEN N3.NAMESPARE1 = '1' THEN '○' WHEN T1.JUDGEMENT='2' THEN '×' WHEN T1.JUDGEMENT='4' THEN '－' ELSE '' END AS JUDGE, ";
        $query .= "     CASE WHEN N3.NAMESPARE1 = '1' AND T1.ENTDIV='1' THEN '○' ELSE '×' END AS ENTDIV ";
        $query .= " FROM ";
        $query .= "     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON T1.FS_CD = FIN.FINSCHOOLCD ";
        $query .= "     LEFT JOIN EDBOARD_COURSE_MST L1 ON T1.EDBOARD_SCHOOLCD = L1.EDBOARD_SCHOOLCD AND T1.SUC_COURSECD = L1.COURSECD ";
        $query .= "     LEFT JOIN EDBOARD_MAJOR_MST L2 ON T1.EDBOARD_SCHOOLCD = L2.EDBOARD_SCHOOLCD AND T1.SUC_COURSECD = L2.COURSECD AND T1.SUC_MAJORCD = L2.MAJORCD ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND T1.SEX = N1.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'L004' AND T1.TESTDIV = N2.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L013' AND T1.JUDGEMENT = N3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD = '".$schoolCd."' AND ";
        $query .= "     T1.ENTEXAMYEAR      = '".$model->ObjYear."' AND ";
        $query .= "     T1.JUDGEMENT       != '5' AND ";
        $query .= "     T1.OBOEGAKI_SIGANSYA = '1' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //DELETE
    function &getDeleteReportFixed($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
        foreach ($selectdata as $key => $schoolCd) {
            //県側報告テーブル
            $query  = " DELETE FROM ";
            $query .= "     REPORT_ENTEXAM_L340W_DAT ";
            $query .= " WHERE ";
            $query .= "     EDBOARD_SCHOOLCD    = '".$schoolCd."' AND ";
            $query .= "     ENTEXAMYEAR         = '".$model->ObjYear."' AND ";
            $query .= "     CSV_PRG             = 4 AND ";
            $query .= "     CSVDIV              = 0 ";

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
