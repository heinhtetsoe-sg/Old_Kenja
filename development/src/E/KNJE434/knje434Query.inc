<?php

require_once('for_php7.php');

class knje434Query extends Query {
    //教育委員会判定
    function z010Abbv1() {
        $query  = " SELECT ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //教育委員会用の学校コード取得
    function getSchoolMst($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //卒業可能学年範囲取得
    function getGradeRange() {
        $query  = " SELECT ";
        $query .= "     NAMESPARE2 AS GRADE_FROM, ";
        $query .= "     NAMESPARE3 AS GRADE_TO ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' AND ";
        $query .= "     NAME1   = 'H' ";

        return $query;
    }

    //確定日取得
    function getFixed($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     FIXED_DATE AS LABEL, ";
        $query .= "     FIXED_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION434_FIXED_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "     DATA_DIV            = '001' ";
        $query .= " ORDER BY ";
        $query .= "     FIXED_DATE DESC ";

        return $query;
    }

    //県への報告履歴取得
    function getReport($model) {
        $query  = " SELECT ";
        $query .= "     EXECUTE_DATE AS LABEL, ";
        $query .= "     EXECUTE_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     REPORT_AFT_DISEASE_ADDITION434_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "     DATA_DIV            = '001' ";
        $query .= " ORDER BY ";
        $query .= "     EXECUTE_DATE DESC ";

        return $query;
    }

    //異動対象日付取得
    function getIdouDate($model, $table) {
        $query  = " SELECT ";
        $query .= "     IDOU_DATE ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "     DATA_DIV            = '001' ";
        if ($model->field["FIXED_DATA"]) {
            $query .= " AND FIXED_DATE          = '".str_replace("/", "-", $model->field["FIXED_DATA"])."' ";
        }

        return $query;
    }

    //AFT_GRAD_COURSE_DAT集計データ取得
    function getAftGradCulc($model) {
        $idou_date = str_replace("/", "-", $model->field["IDOU_DATE"]);

        $query  = " WITH SCH_LIST AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.SEX ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.GRADE BETWEEN '".$model->grd_grade["GRADE_FROM"]."' AND '".$model->grd_grade["GRADE_TO"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         NOT((VALUE(GRD_DIV,'') IN ('2','3','6','7') AND GRD_DATE < '".$idou_date."') OR ENT_DATE > '".$idou_date."') ";
        $query .= " ), AFT_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEQ, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.SEX, ";
        $query .= "         T1.STAT_CD AS COMPANY_CD, ";
        $query .= "         CASE WHEN T1.PREF_CD = '".$model->schoolM["PREF_CD"]."' THEN '01' ELSE '02' END AS PREF_DIV, ";
        $query .= "         T1.PREF_CD, ";
        $query .= "         T1.PLANSTAT ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT T1 ";
        $query .= "         INNER JOIN SCH_LIST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SENKOU_KIND  = '1' AND ";
        $query .= "         T1.DECISION     = '1' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     SEX, ";
        $query .= "     PREF_DIV, ";
        $query .= "     PREF_CD, ";
        $query .= "     COMPANY_CD, ";
        $query .= "     SUM(1) AS INT_VAL1, ";
        $query .= "     SUM(CASE WHEN PLANSTAT = '1' THEN 1 ELSE 0 END) AS INT_VAL2 ";
        $query .= " FROM ";
        $query .= "     AFT_DATA ";
        $query .= " WHERE ";
        $query .= "     PREF_CD IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     SEX, ";
        $query .= "     PREF_DIV, ";
        $query .= "     PREF_CD, ";
        $query .= "     COMPANY_CD ";

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        /*********************************/
        /*  AFT_DISEASE_ADDITION434_DAT  */
        /*********************************/

        //DELETE
        $query  = " DELETE FROM ";
        $query .= "     AFT_DISEASE_ADDITION434_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "     DATA_DIV            = '001' ";

        $db->query($query);

        //INSERT
        $query = knje434Query::getAftGradCulc($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["DATA_DIV"][TEXT]             = '001';
            $data["COURSECD"][TEXT]             = '0';
            $data["MAJORCD"][TEXT]              = '000';
            $data["AGE"][NUMBER]                = 0;
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["SEQ"][TEXT]                  = $row["PREF_DIV"];
            $data["PREF_CD"][TEXT]              = $row["PREF_CD"];
            $data["COMPANY_CD"][TEXT]           = $row["COMPANY_CD"];
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";

            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION434_DAT");
            $db->query($query);
        }

        /**********************************/
        /*  AFT_DISEASE_ADDITION434_HDAT  */
        /**********************************/

        $idou_date = str_replace("/", "-", $model->field["IDOU_DATE"]);

        //DELETE
        $query  = " DELETE FROM ";
        $query .= "     AFT_DISEASE_ADDITION434_HDAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "     DATA_DIV            = '001' ";

        $db->query($query);

        //INSERT
        $data = array();
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["DATA_DIV"][TEXT]             = '001';
        $data["IDOU_DATE"][DATE]            = $idou_date;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";

        $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION434_HDAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //確定用データ取得
    function getAftDiseaseAddition434($model, $table) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "     DATA_DIV            = '001' ";

        return $query;
    }

    //確定UPDATE
    function &getFixedUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        /***************************************/
        /*  AFT_DISEASE_ADDITION434_FIXED_DAT  */
        /***************************************/

        $fixed_date = str_replace("/", "-", $model->field["FIXED_DATE"]);

        //DELETE
        $query  = " DELETE FROM ";
        $query .= "     AFT_DISEASE_ADDITION434_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "     DATA_DIV            = '001' AND ";
        $query .= "     FIXED_DATE          = '".$fixed_date."' ";

        $db->query($query);

        //INSERT
        $query = knje434Query::getAftDiseaseAddition434($model, "AFT_DISEASE_ADDITION434_DAT");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["DATA_DIV"][TEXT]             = $row["DATA_DIV"];
            $data["FIXED_DATE"][DATE]           = $fixed_date;
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["AGE"][NUMBER]                = $row["AGE"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["SEQ"][TEXT]                  = $row["SEQ"];
            $data["PREF_CD"][TEXT]              = $row["PREF_CD"];
            $data["COMPANY_CD"][TEXT]           = $row["COMPANY_CD"];
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";

            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION434_FIXED_DAT");
            $db->query($query);

        }
        $result->free();

        /****************************************/
        /*  AFT_DISEASE_ADDITION434_FIXED_HDAT  */
        /****************************************/

        //DELETE
        $query  = " DELETE FROM ";
        $query .= "     AFT_DISEASE_ADDITION434_FIXED_HDAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "     DATA_DIV            = '001' AND ";
        $query .= "     FIXED_DATE          = '".$fixed_date."' ";

        $db->query($query);

        //INSERT
        $query = knje434Query::getAftDiseaseAddition434($model, "AFT_DISEASE_ADDITION434_HDAT");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["DATA_DIV"][TEXT]             = $row["DATA_DIV"];
            $data["FIXED_DATE"][DATE]           = $fixed_date;
            $data["IDOU_DATE"][DATE]            = $row["IDOU_DATE"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";

            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION434_FIXED_HDAT");
            $db->query($query);

        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //データ取得
    function getAftDiseaseAddition434FixedDat($model) {
        $fixed_date = str_replace("/", "-", $model->field["FIXED_DATA"]);

        $query  = " SELECT DISTINCT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION434_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' AND ";
        $query .= "     YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "     DATA_DIV         = '001' AND ";
        $query .= "     FIXED_DATE      = '".$fixed_date."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateReport($model) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        $execute_date = str_replace("/", "-", $model->field["EXECUTE_DATE"]);
        $fixed_date = str_replace("/", "-", $model->field["FIXED_DATA"]);

        //県側、学校側・報告テーブル
        $query  = " DELETE FROM ";
        $query .= "     REPORT_AFT_DISEASE_ADDITION434_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "     DATA_DIV            = '001' AND ";
        $query .= "     EXECUTE_DATE        = '".$execute_date."' ";
        $db->query($query);
        $db2->query($query);

        //県側、学校側・報告テーブル
        //キー
        $data = array();
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["DATA_DIV"][TEXT]             = '001';
        $data["EXECUTE_DATE"][DATE]         = $execute_date;
        $data["FIXED_DATE"][DATE]           = $fixed_date;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        $query  = Query::insertSQL($data, "REPORT_AFT_DISEASE_ADDITION434_DAT");
        $db->query($query);
        $db2->query($query);

        //DELETE
        //県側・確定テーブル
        $query  = " DELETE FROM ";
        $query .= "     AFT_DISEASE_ADDITION434_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "     DATA_DIV            = '001' AND ";
        $query .= "     FIXED_DATE          = '".$fixed_date."' ";

        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得
        $query = knje434Query::getAftDiseaseAddition434FixedDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側・確定テーブル
            $data = array();
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["DATA_DIV"][TEXT]             = $row["DATA_DIV"];
            $data["FIXED_DATE"][DATE]           = $row["FIXED_DATE"];
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["AGE"][NUMBER]                = $row["AGE"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["SEQ"][TEXT]                  = $row["SEQ"];
            $data["PREF_CD"][TEXT]              = $row["PREF_CD"];
            $data["COMPANY_CD"][TEXT]           = $row["COMPANY_CD"];
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";

            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION434_FIXED_DAT");
            $db2->query($query);
        }
        $result->free();

        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }

    //CSV出力データ取得
    function getCsvQuery($model, $flg="") {

        $fixed_date = str_replace("/", "-", $model->field["FIXED_DATA"]);
        $table = ($model->field["FIXED_DATA"]) ? "AFT_DISEASE_ADDITION434_FIXED_DAT" : "AFT_DISEASE_ADDITION434_DAT";

        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEQ, ";
        $query .= "         PREF_CD, ";
        $query .= "         COMPANY_CD, ";
        $query .= "         SUM(CASE WHEN SEX = '1' THEN INT_VAL1 ELSE 0 END) AS GOUKAKU_M, ";
        $query .= "         SUM(CASE WHEN SEX = '2' THEN INT_VAL1 ELSE 0 END) AS GOUKAKU_F, ";
        $query .= "         SUM(INT_VAL1) AS GOUKAKU_T, ";
        $query .= "         SUM(CASE WHEN SEX = '1' THEN INT_VAL2 ELSE 0 END) AS NAITEI_M, ";
        $query .= "         SUM(CASE WHEN SEX = '2' THEN INT_VAL2 ELSE 0 END) AS NAITEI_F, ";
        $query .= "         SUM(INT_VAL2) AS NAITEI_T ";
        $query .= "     FROM ";
        $query .=           $table;
        $query .= "     WHERE ";
        $query .= "         EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "         YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "         DATA_DIV            = '001' ";
        if ($model->field["FIXED_DATA"]) {
            $query .= "     AND FIXED_DATE          = '".$fixed_date."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         SEQ, ";
        $query .= "         PREF_CD, ";
        $query .= "         COMPANY_CD ";
        $query .= " ) ";

        if ($flg == "cnt") {
            $query .= " SELECT ";
            $query .= "     COUNT(*) AS TOTAL, ";
            $query .= "     SUM(CASE WHEN SEQ = '01'THEN 1 ELSE 0 END) AS SEQ01, ";
            $query .= "     SUM(CASE WHEN SEQ = '02'THEN 1 ELSE 0 END) AS SEQ02 ";
            $query .= " FROM ";
            $query .= "     MAIN ";
        } else {
            $query .= " SELECT ";
            $query .= "     T1.*, ";
            $query .= "     CASE WHEN T1.SEQ = '01' THEN '県内' ELSE '県外' END AS SEQ_NAME, ";
            $query .= "     L1.PREF_NAME, ";
            $query .= "     L2.COMPANY_NAME ";
            $query .= " FROM ";
            $query .= "     MAIN T1 ";
            $query .= "     LEFT JOIN PREF_MST L1 ON T1.PREF_CD = L1.PREF_CD ";
            $query .= "     LEFT JOIN COMPANY_MST L2 ON T1.COMPANY_CD = L2.COMPANY_CD ";
            $query .= " ORDER BY ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.PREF_CD, ";
            $query .= "     T1.COMPANY_CD ";
        }

        return $query;
    }
}
?>
