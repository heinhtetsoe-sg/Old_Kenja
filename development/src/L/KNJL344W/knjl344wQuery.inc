<?php

require_once('for_php7.php');

class knjl344wquery extends Query {
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

    //CSVデータ出力
    function selectCsvQuery($model) {
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
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON T1.FS_CD = FIN.FINSCHOOLCD ";
        $query .= "     LEFT JOIN COURSE_MST L1 ON T1.SUC_COURSECD = L1.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST L2 ON T1.SUC_COURSECD = L2.COURSECD AND T1.SUC_MAJORCD = L2.MAJORCD ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND T1.SEX = N1.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'L004' AND T1.TESTDIV = N2.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L013' AND T1.JUDGEMENT = N3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.JUDGEMENT   != '5' AND ";
        $query .= "     T1.OBOEGAKI_SIGANSYA = '1' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

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
        $query .= "     T1.CSV_PRG          = 4 AND ";
        $query .= "     T1.CSVDIV           = 0 ";

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
        $table_array["ENTEXAM_APPLICANTBASE_DAT"]           = " ENTEXAMYEAR = '".$model->ObjYear."' ";
        $table_array["ENTEXAM_APPLICANTBASE_DETAIL_DAT"]    = " ENTEXAMYEAR = '".$model->ObjYear."' ";

        foreach ($table_array as $table => $where) {
            //フィールド一覧取得
            $field_array = array();
            $query = knjl344wQuery::getFieldName($table);
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
            $query = knjl344wQuery::getDeleteQuery($model, $table, $where);
            $db2->query($query);

            //INSERT -- 学校側から県側へコピー
            $query = knjl344wQuery::getDataQuery($model, $table, $where);
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
        $query .= "   AND CSV_PRG           = 4 ";
        $query .= "   AND CSVDIV            = 0 ";
        $db2->query($query);

        //INSERT -- 県側
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
        $data["CSV_PRG"][NUMBER]            = 4;
        $data["CSVDIV"][NUMBER]             = 0;
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
