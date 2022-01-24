<?php

require_once('for_php7.php');

class knjl346wQuery extends Query {
    //学校情報取得
    function getSchoolInfo($model) {
        $query  = " SELECT ";
        $query .= "     T1.KYOUIKU_IINKAI_SCHOOLCD, ";
        $query .= "     T2.FINSCHOOL_NAME, ";
        $query .= "     L1.NAME1 AS DIST_NAME ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST T1, ";
        $query .= "     FINSCHOOL_MST T2 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'Z015' AND T2.FINSCHOOL_DISTCD2 = L1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.KYOUIKU_IINKAI_SCHOOLCD = T2.FINSCHOOLCD ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //課程名取得
    function getCourseName($model) {
        $query  = " SELECT ";
        $query .= "     COURSENAME ";
        $query .= " FROM ";
        $query .= "     COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     COURSECD IN (SELECT ";
        $query .= "                     MIN(COURSECD) AS COURSECD ";
        $query .= "                 FROM ";
        $query .= "                     V_COURSE_MST ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' ";
        $query .= "                 ) ";

        return $query;
    }

    //CSVデータ
    function selectCsvQuery($model) {
/***
        $query  = " SELECT ";
        $query .= "     BOSHUU_KYOUGI, ";
        $query .= "     LAST_DAI1_COURSECD || LAST_DAI1_MAJORCD AS MAJOR, ";
        $query .= "     L1.SPORT_NAME AS BOSHUU_SPORT, ";
        $query .= "     L2.MAJORNAME AS BOSHUU_MAJOR, ";
        //合計
        $query .= "     COUNT(*) AS SIGAN_CNT, ";
        $query .= "     SUM(CASE WHEN T1.JUDGEMENT = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT, ";
        $query .= "     SUM(CASE WHEN T1.JUDGEMENT = '3' THEN 1 ELSE 0 END) AS ZENKI_GOUKAKU_CNT, ";
        $query .= "     SUM(CASE WHEN T1.JUDGEMENT = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT, ";
        //男子
        $query .= "     SUM(CASE WHEN T1.SEX = '1' THEN 1 ELSE 0 END) AS SIGAN_CNT1, ";
        $query .= "     SUM(CASE WHEN T1.SEX = '1' AND T1.JUDGEMENT = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT1, ";
        $query .= "     SUM(CASE WHEN T1.SEX = '1' AND T1.JUDGEMENT = '3' THEN 1 ELSE 0 END) AS ZENKI_GOUKAKU_CNT1, ";
        $query .= "     SUM(CASE WHEN T1.SEX = '1' AND T1.JUDGEMENT = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT1, ";
        //女子
        $query .= "     SUM(CASE WHEN T1.SEX = '2' THEN 1 ELSE 0 END) AS SIGAN_CNT2, ";
        $query .= "     SUM(CASE WHEN T1.SEX = '2' AND T1.JUDGEMENT = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT2, ";
        $query .= "     SUM(CASE WHEN T1.SEX = '2' AND T1.JUDGEMENT = '3' THEN 1 ELSE 0 END) AS ZENKI_GOUKAKU_CNT2, ";
        $query .= "     SUM(CASE WHEN T1.SEX = '2' AND T1.JUDGEMENT = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT2 ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_SPORT_MST L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND L1.SPORT_CD = T1.BOSHUU_KYOUGI ";
        $query .= "     LEFT JOIN MAJOR_MST L2 ON L2.COURSECD = T1.LAST_DAI1_COURSECD AND L2.MAJORCD = T1.LAST_DAI1_MAJORCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND T1.TESTDIV = '4' ";
        $query .= "     AND VALUE(T1.JUDGEMENT,'0') != '5' ";
        $query .= "     AND T1.BOSHUU_KYOUGI IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     BOSHUU_KYOUGI, ";
        $query .= "     LAST_DAI1_COURSECD, ";
        $query .= "     LAST_DAI1_MAJORCD, ";
        $query .= "     L1.SPORT_NAME, ";
        $query .= "     L2.MAJORNAME ";
        $query .= " ORDER BY ";
        $query .= "     BOSHUU_KYOUGI, ";
        $query .= "     LAST_DAI1_COURSECD, ";
        $query .= "     LAST_DAI1_MAJORCD ";
***/
        $query  = "";
        //募集競技、募集学科
        $query .= " WITH T_SPORT_MAJOR AS ( ";
        $query .= "     SELECT ";
        $query .= "         BOSHUU_KYOUGI, ";
        $query .= "         LAST_DAI1_COURSECD || LAST_DAI1_MAJORCD AS MAJORCD ";
        $query .= "     FROM ";
        $query .= "         V_ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND TESTDIV = '4' ";
        $query .= "         AND VALUE(JUDGEMENT,'0') != '5' ";
        $query .= "         AND BOSHUU_KYOUGI IS NOT NULL ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         BOSHUU_KYOUGI, ";
        $query .= "         SUC_COURSECD || SUC_MAJORCD AS MAJORCD ";
        $query .= "     FROM ";
        $query .= "         V_ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND TESTDIV = '4' ";
        $query .= "         AND VALUE(JUDGEMENT,'0') != '5' ";
        $query .= "         AND BOSHUU_KYOUGI IS NOT NULL ";
        $query .= "         AND SUC_MAJORCD IS NOT NULL ";
        $query .= " ) ";
        //志願者数、不合格者数
        $query .= " , T_SIGAN AS ( ";
        $query .= "     SELECT ";
        $query .= "         BOSHUU_KYOUGI, ";
        $query .= "         LAST_DAI1_COURSECD || LAST_DAI1_MAJORCD AS MAJORCD, ";
        $query .= "         COUNT(*) AS SIGAN_CNT, ";
        $query .= "         SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS SIGAN_CNT1, ";
        $query .= "         SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS SIGAN_CNT2, ";
        $query .= "         SUM(CASE WHEN JUDGEMENT = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT, ";
        $query .= "         SUM(CASE WHEN SEX = '1' AND JUDGEMENT = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT1, ";
        $query .= "         SUM(CASE WHEN SEX = '2' AND JUDGEMENT = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT2 ";
        $query .= "     FROM ";
        $query .= "         V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->ObjYear."' ";
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
        $query .= "         SUC_COURSECD || SUC_MAJORCD AS MAJORCD, ";
        $query .= "         SUM(CASE WHEN JUDGEMENT = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT, ";
        $query .= "         SUM(CASE WHEN JUDGEMENT = '3' THEN 1 ELSE 0 END) AS ZENKI_GOUKAKU_CNT, ";
        $query .= "         SUM(CASE WHEN SEX = '1' AND JUDGEMENT = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT1, ";
        $query .= "         SUM(CASE WHEN SEX = '1' AND JUDGEMENT = '3' THEN 1 ELSE 0 END) AS ZENKI_GOUKAKU_CNT1, ";
        $query .= "         SUM(CASE WHEN SEX = '2' AND JUDGEMENT = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT2, ";
        $query .= "         SUM(CASE WHEN SEX = '2' AND JUDGEMENT = '3' THEN 1 ELSE 0 END) AS ZENKI_GOUKAKU_CNT2 ";
        $query .= "     FROM ";
        $query .= "         V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->ObjYear."' ";
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
        $query .= "     T1.MAJORCD, ";
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
        $query .= "     LEFT JOIN T_SIGAN S1 ON S1.BOSHUU_KYOUGI = T1.BOSHUU_KYOUGI AND S1.MAJORCD = T1.MAJORCD ";
        $query .= "     LEFT JOIN T_GOUKAKU S2 ON S2.BOSHUU_KYOUGI = T1.BOSHUU_KYOUGI AND S2.MAJORCD = T1.MAJORCD ";
        $query .= "     LEFT JOIN ENTEXAM_SPORT_MST L1 ON L1.ENTEXAMYEAR = '".$model->ObjYear."' AND L1.SPORT_CD = T1.BOSHUU_KYOUGI ";
        $query .= "     LEFT JOIN MAJOR_MST L2 ON L2.COURSECD || L2.MAJORCD = T1.MAJORCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.BOSHUU_KYOUGI, ";
        $query .= "     T1.MAJORCD ";

        return $query;
    }

    //県への報告取得
    function getReport($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXECUTE_DATE, ";
        $query .= "     L1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     REPORT_ENTEXAM_L340W_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST L1 ON T1.REGISTERCD = L1.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' AND ";
        $query .= "     T1.ENTEXAMYEAR      = '".$model->ObjYear."' AND ";
        $query .= "     T1.CSV_PRG          = 6 AND ";
        $query .= "     T1.CSVDIV           = ".$model->field["CSVDIV"]." ";

        return $query;
    }

    //フィールド取得
    function getFieldName($table) {
        $query  = " SELECT ";
        $query .= "     COLUMN_NAME, ";
        $query .= "     DATA_TYPE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABLE_NAME = '".$table."' AND ";
        $query .= "     COLUMN_NAME NOT IN ('REGISTERCD', 'UPDATED') ";
        $query .= " ORDER BY ";
        $query .= "     ORDINAL_POSITION ";

        return $query;
    }

    //データ取得
    function getEntexamApplicantbaseDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->ObjYear."' ";

        return $query;
    }

    //学校側・データ取得
    function getDataQuery($model, $table, $where="") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .=       $table." T1 ";
        if ($where) {
            $query .= " WHERE ".$where;
        }

        return $query;
    }

    //県側・削除
    function getDeleteQuery($model, $table, $where="") {
        $query  = " DELETE FROM ";
        $query .= "     EDBOARD_".$table." T1 ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        if ($where) {
            $query .= " AND ".$where;
        }

        return $query;
    }

    //UPDATE
    function &getUpdateReport($model) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db2->autoCommit(false);

        /**********************************/
        /*  学校側から県側へデータコピー  */
        /**********************************/

        //対象テーブル一覧／条件
        $table_array = array();
        $table_array["COURSE_MST"]      = "";
        $table_array["COURSE_YDAT"]     = " YEAR IN ('".CTRL_YEAR."', '".$model->ObjYear."') ";
        $table_array["MAJOR_MST"]       = "";
        $table_array["ENTEXAM_APPLICANTBASE_DAT"]   = " ENTEXAMYEAR = '".$model->ObjYear."' ";
        $table_array["ENTEXAM_APPLICANTBASE_DETAIL_DAT"]  = "";
        $table_array["ENTEXAM_COURSE_MST"]  = " ENTEXAMYEAR = '".$model->ObjYear."' ";
        $table_array["ENTEXAM_SPORT_MST"]   = " ENTEXAMYEAR = '".$model->ObjYear."' ";

        foreach ($table_array as $table => $where) {
            //フィールド一覧取得
            $field_array = array();
            $query = knjl346wQuery::getFieldName($table);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["DATA_TYPE"] == "CHARACTER VARYING") {
                    $data_type = TEXT;
                } else if (in_array($row["DATA_TYPE"], array("INTEGER", "SMALLINT", "DECIMAL"))) {
                    $data_type = NUMBER;
                } else {
                    $data_type = $row["DATA_TYPE"];
                }
                $field_array[$row["COLUMN_NAME"]] = $data_type;
            }
            $result->free();

            //DELETE -- 県側
            $query = knjl346wQuery::getDeleteQuery($model, $table, $where);
            $db2->query($query);

            //INSERT -- 学校側から県側へコピー
            $query = knjl346wQuery::getDataQuery($model, $table, $where);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data = array(); //初期化
                $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
                foreach ($field_array as $field => $type) {
                    $data[$field][$type]            = $row[$field];
                }
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";
                $query  = Query::insertSQL($data, "EDBOARD_".$table);
                $db2->query($query);
            }
            $result->free();
        }

        /******************/
        /*  報告テーブル  */
        /******************/

        //DELETE -- 県側
        $query  = " DELETE FROM REPORT_ENTEXAM_L340W_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= "   AND ENTEXAMYEAR       = '".$model->ObjYear."' ";
        $query .= "   AND CSV_PRG           = 6 ";
        $query .= "   AND CSVDIV            = ".$model->field["CSVDIV"]." ";
        $db2->query($query);

        //INSERT -- 県側
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
        $data["CSV_PRG"][NUMBER]            = 6;
        $data["CSVDIV"][NUMBER]             = $model->field["CSVDIV"];
        $data["EXECUTE_DATE"][NUMBER]       = "sysdate()";
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        $query  = Query::insertSQL($data, "REPORT_ENTEXAM_L340W_DAT");
        $db2->query($query);

        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }
}
?>
