<?php

require_once('for_php7.php');

class knjl366wQuery extends Query {
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
        $query .= "         AND L1.CSV_PRG          = 6 ";
        $query .= "         AND L1.CSVDIV           = ".$model->field["CSVDIV"]." ";
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

    //CSVデータ
    function selectCsvQuery($model, $schoolCd) {
        $query  = "";
        //募集競技、募集学科
        $query .= " WITH T_SPORT_MAJOR AS ( ";
        $query .= "     SELECT ";
        $query .= "         BOSHUU_KYOUGI, ";
        $query .= "         LAST_DAI1_COURSECD || LAST_DAI1_MAJORCD AS MAJOR ";
        $query .= "     FROM ";
        $query .= "         V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "     WHERE ";
        $query .= "         EDBOARD_SCHOOLCD = '".$schoolCd."' ";
        $query .= "         AND ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND TESTDIV = '4' ";
        $query .= "         AND VALUE(JUDGEMENT,'0') != '5' ";
        $query .= "         AND BOSHUU_KYOUGI IS NOT NULL ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         BOSHUU_KYOUGI, ";
        $query .= "         SUC_COURSECD || SUC_MAJORCD AS MAJOR ";
        $query .= "     FROM ";
        $query .= "         V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "     WHERE ";
        $query .= "         EDBOARD_SCHOOLCD = '".$schoolCd."' ";
        $query .= "         AND ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND TESTDIV = '4' ";
        $query .= "         AND VALUE(JUDGEMENT,'0') != '5' ";
        $query .= "         AND BOSHUU_KYOUGI IS NOT NULL ";
        $query .= "         AND SUC_MAJORCD IS NOT NULL ";
        $query .= " ) ";
        //志願者数、不合格者数
        $query .= " , T_SIGAN AS ( ";
        $query .= "     SELECT ";
        $query .= "         BOSHUU_KYOUGI, ";
        $query .= "         LAST_DAI1_COURSECD || LAST_DAI1_MAJORCD AS MAJOR, ";
        $query .= "         COUNT(*) AS SIGAN_CNT, ";
        $query .= "         SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS SIGAN_CNT1, ";
        $query .= "         SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS SIGAN_CNT2, ";
        $query .= "         SUM(CASE WHEN JUDGEMENT = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT, ";
        $query .= "         SUM(CASE WHEN SEX = '1' AND JUDGEMENT = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT1, ";
        $query .= "         SUM(CASE WHEN SEX = '2' AND JUDGEMENT = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT2 ";
        $query .= "     FROM ";
        $query .= "         V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         EDBOARD_SCHOOLCD = '".$schoolCd."' ";
        $query .= "         AND ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND TESTDIV = '4' ";
        $query .= "         AND VALUE(JUDGEMENT,'0') != '5' ";
        $query .= "         AND BOSHUU_KYOUGI IS NOT NULL ";
        $query .= "     GROUP BY ";
        $query .= "         BOSHUU_KYOUGI, ";
        $query .= "         LAST_DAI1_COURSECD, ";
        $query .= "         LAST_DAI1_MAJORCD ";
        $query .= " ) ";
        //合格内定者数、前期合格内定者数
        $query .= " , T_GOUKAKU AS ( ";
        $query .= "     SELECT ";
        $query .= "         BOSHUU_KYOUGI, ";
        $query .= "         SUC_COURSECD || SUC_MAJORCD AS MAJOR, ";
        $query .= "         SUM(CASE WHEN JUDGEMENT = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT, ";
        $query .= "         SUM(CASE WHEN JUDGEMENT = '3' THEN 1 ELSE 0 END) AS ZENKI_GOUKAKU_CNT, ";
        $query .= "         SUM(CASE WHEN SEX = '1' AND JUDGEMENT = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT1, ";
        $query .= "         SUM(CASE WHEN SEX = '1' AND JUDGEMENT = '3' THEN 1 ELSE 0 END) AS ZENKI_GOUKAKU_CNT1, ";
        $query .= "         SUM(CASE WHEN SEX = '2' AND JUDGEMENT = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT2, ";
        $query .= "         SUM(CASE WHEN SEX = '2' AND JUDGEMENT = '3' THEN 1 ELSE 0 END) AS ZENKI_GOUKAKU_CNT2 ";
        $query .= "     FROM ";
        $query .= "         V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         EDBOARD_SCHOOLCD = '".$schoolCd."' ";
        $query .= "         AND ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND TESTDIV = '4' ";
        $query .= "         AND VALUE(JUDGEMENT,'0') != '5' ";
        $query .= "         AND BOSHUU_KYOUGI IS NOT NULL ";
        $query .= "         AND SUC_MAJORCD IS NOT NULL ";
        $query .= "     GROUP BY ";
        $query .= "         BOSHUU_KYOUGI, ";
        $query .= "         SUC_COURSECD, ";
        $query .= "         SUC_MAJORCD ";
        $query .= " ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.BOSHUU_KYOUGI, ";
        $query .= "     T1.MAJOR, ";
        $query .= "     L1.SPORT_NAME AS BOSHUU_SPORT, ";
        $query .= "     L2.MAJORNAME AS BOSHUU_MAJOR, ";
        //合計
        $query .= "     S1.SIGAN_CNT, ";
        $query .= "     S2.GOUKAKU_CNT, ";
        $query .= "     S2.ZENKI_GOUKAKU_CNT, ";
        $query .= "     S1.FUGOUKAKU_CNT, ";
        //男子
        $query .= "     S1.SIGAN_CNT1, ";
        $query .= "     S2.GOUKAKU_CNT1, ";
        $query .= "     S2.ZENKI_GOUKAKU_CNT1, ";
        $query .= "     S1.FUGOUKAKU_CNT1, ";
        //女子
        $query .= "     S1.SIGAN_CNT2, ";
        $query .= "     S2.GOUKAKU_CNT2, ";
        $query .= "     S2.ZENKI_GOUKAKU_CNT2, ";
        $query .= "     S1.FUGOUKAKU_CNT2 ";
        $query .= " FROM ";
        $query .= "     T_SPORT_MAJOR T1 ";
        $query .= "     LEFT JOIN T_SIGAN S1 ON S1.BOSHUU_KYOUGI = T1.BOSHUU_KYOUGI AND S1.MAJOR = T1.MAJOR ";
        $query .= "     LEFT JOIN T_GOUKAKU S2 ON S2.BOSHUU_KYOUGI = T1.BOSHUU_KYOUGI AND S2.MAJOR = T1.MAJOR ";
        $query .= "     LEFT JOIN EDBOARD_ENTEXAM_SPORT_MST L1 ON L1.EDBOARD_SCHOOLCD = '".$schoolCd."' AND L1.ENTEXAMYEAR = '".$model->ObjYear."' AND L1.SPORT_CD = T1.BOSHUU_KYOUGI ";
        $query .= "     LEFT JOIN EDBOARD_MAJOR_MST L2 ON L2.EDBOARD_SCHOOLCD = '".$schoolCd."' AND L2.COURSECD || L2.MAJORCD = T1.MAJOR ";
        $query .= " ORDER BY ";
        $query .= "     T1.BOSHUU_KYOUGI, ";
        $query .= "     T1.MAJOR ";

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
            $query .= "     CSV_PRG             = 6 AND ";
            $query .= "     CSVDIV              = ".$model->field["CSVDIV"]." ";

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
