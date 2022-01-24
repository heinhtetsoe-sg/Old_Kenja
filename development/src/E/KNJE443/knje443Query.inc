<?php

require_once('for_php7.php');

class knje443Query extends Query {
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
        $query .= "     AFT_DISEASE_ADDITION443_FIXED_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     FIXED_DATE DESC ";

        return $query;
    }

    //文書番号
    function getTuutatu($model) {
        $query  = " SELECT ";
        $query .= "     T1.DOC_NUMBER AS VALUE, ";
        $query .= "     T1.DOC_NUMBER AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_SCHOOL_DAT T1 ";
        $query .= "     INNER JOIN AFT_SEARCH_REPORT_DAT L1 ";
        $query .= "          ON T1.YEAR         = L1.YEAR ";
        $query .= "         AND T1.DOC_NUMBER   = L1.DOC_NUMBER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                 = '".CTRL_YEAR."' AND ";
        $query .= "     T1.EDBOARD_SCHOOLCD     = '".$model->schoolcd."' AND ";
        $query .= "     L1.REQUEST_ANSWER_PRG   = '".PROGRAMID."' AND ";
        $query .= "     L1.SUBMISSION_DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.DOC_NUMBER ";

        return $query;
    }

    //県への報告履歴取得
    function getReport($model) {
        $query  = " SELECT ";
        $query .= "     EXECUTE_DATE AS LABEL, ";
        $query .= "     EXECUTE_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     REPORT_AFT_DISEASE_ADDITION443_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     EXECUTE_DATE DESC ";

        return $query;
    }

    //大学系列取得
    function getSchoolGroupType($model, $table) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_GROUP_TYPE ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' ";
        if ($model->field["FIXED_DATA"]) {
            $query .= " AND FIXED_DATE          = '".str_replace("/", "-", $model->field["FIXED_DATA"])."' ";
        }

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
        $query .= "     YEAR                = '".CTRL_YEAR."' ";
        if ($model->field["FIXED_DATA"]) {
            $query .= " AND FIXED_DATE          = '".str_replace("/", "-", $model->field["FIXED_DATA"])."' ";
        }

        return $query;
    }

    //AFT_GRAD_COURSE_DAT集計データ取得
    function getAftGradCulc($model) {
        $idou_date = str_replace("/", "-", $model->field["IDOU_DATE"]);
        $SG_type = ($model->field["SCHOOL_GROUP_TYPE"] == 0) ? '1,2' : $model->field["SCHOOL_GROUP_TYPE"];

        $query  = " WITH SCH_LIST AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.GRADE BETWEEN '".$model->grd_grade["GRADE_FROM"]."' AND '".$model->grd_grade["GRADE_TO"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         NOT((VALUE(GRD_DIV,'') IN ('2','3','6','7') AND GRD_DATE < '".$idou_date."') OR ENT_DATE > '".$idou_date."') ";
        $query .= " ), GRD_LIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.COURSECD, ";
        $query .= "         T2.MAJORCD ";
        $query .= "     FROM ";
        $query .= "         GRD_BASE_MST T1, ";
        $query .= "         GRD_REGD_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "         T1.GRD_SEMESTER = T2.SEMESTER AND ";
        $query .= "         T2.YEAR IN (SELECT ";
        $query .= "                         MAX(YEAR) ";
        $query .= "                     FROM ";
        $query .= "                         GRD_REGD_DAT S1 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.SCHREGNO     = S1.SCHREGNO AND ";
        $query .= "                         T1.GRD_SEMESTER = S1.SEMESTER ";
        $query .= "                     ) ";
        $query .= " ), AFT_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEQ, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.SCHOOL_GROUP, ";
        $query .= "         T1.STAT_CD AS SCHOOL_CD, ";
        $query .= "         T1.PLANSTAT ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT T1 ";
        $query .= "         INNER JOIN NAME_MST T2 ON T2.NAMECD1 = 'E012' AND T2.NAMECD2 = T1.SCHOOL_GROUP ";
        $query .= "                 AND T2.NAMESPARE2 IN ('".implode("','", explode(',', $SG_type))."') ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SENKOU_KIND  = '0' AND ";
        $query .= "         T1.DECISION     = '1' ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         '01' AS DIV, ";
        $query .= "         T1.*, ";
        $query .= "         T2.COURSECD, ";
        $query .= "         T2.MAJORCD ";
        $query .= "     FROM ";
        $query .= "         AFT_DATA T1 ";
        $query .= "         INNER JOIN SCH_LIST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         '02' AS DIV, ";
        $query .= "         T1.*, ";
        $query .= "         T2.COURSECD, ";
        $query .= "         T2.MAJORCD ";
        $query .= "     FROM ";
        $query .= "         AFT_DATA T1 ";
        $query .= "         INNER JOIN GRD_LIST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         NOT EXISTS (SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT E1 ";
        $query .= "                     WHERE ";
        $query .= "                         E1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                         E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                     ) ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     SCHOOL_GROUP, ";
        $query .= "     SCHOOL_CD, ";
        $query .= "     DIV, ";
        $query .= "     SUM(1) AS INT_VAL1, ";
        $query .= "     SUM(CASE WHEN PLANSTAT = '1' THEN 1 ELSE 0 END) AS INT_VAL2 ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " GROUP BY ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     SCHOOL_GROUP, ";
        $query .= "     SCHOOL_CD, ";
        $query .= "     DIV ";

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        /*********************************/
        /*  AFT_DISEASE_ADDITION443_DAT  */
        /*********************************/

        //DELETE
        $query  = " DELETE FROM ";
        $query .= "     AFT_DISEASE_ADDITION443_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' ";

        $db->query($query);

        //INSERT
        $query = knje443Query::getAftGradCulc($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["SCHOOL_GROUP"][TEXT]         = $row["SCHOOL_GROUP"];
            $data["SCHOOL_CD"][TEXT]            = $row["SCHOOL_CD"];
            $data["SEQ"][TEXT]                  = $row["DIV"];
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";

            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION443_DAT");
            $db->query($query);
        }

        /**********************************/
        /*  AFT_DISEASE_ADDITION443_HDAT  */
        /**********************************/

        $idou_date = str_replace("/", "-", $model->field["IDOU_DATE"]);

        //DELETE
        $query  = " DELETE FROM ";
        $query .= "     AFT_DISEASE_ADDITION443_HDAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' ";

        $db->query($query);

        //INSERT
        $data = array();
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SCHOOL_GROUP_TYPE"][TEXT]    = $model->field["SCHOOL_GROUP_TYPE"];
        $data["IDOU_DATE"][DATE]            = $idou_date;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";

        $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION443_HDAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //確定用データ取得
    function getAftDiseaseAddition443($model, $table) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' ";

        return $query;
    }

    //確定UPDATE
    function &getFixedUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        /***************************************/
        /*  AFT_DISEASE_ADDITION443_FIXED_DAT  */
        /***************************************/

        $fixed_date = str_replace("/", "-", $model->field["FIXED_DATE"]);

        //DELETE
        $query  = " DELETE FROM ";
        $query .= "     AFT_DISEASE_ADDITION443_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "     FIXED_DATE          = '".$fixed_date."' ";

        $db->query($query);

        //INSERT
        $query = knje443Query::getAftDiseaseAddition443($model, "AFT_DISEASE_ADDITION443_DAT");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $fixed_date;
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["SCHOOL_GROUP"][TEXT]         = $row["SCHOOL_GROUP"];
            $data["SCHOOL_CD"][TEXT]            = $row["SCHOOL_CD"];
            $data["SEQ"][TEXT]                  = $row["SEQ"];
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";

            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION443_FIXED_DAT");
            $db->query($query);

        }
        $result->free();

        /****************************************/
        /*  AFT_DISEASE_ADDITION443_FIXED_HDAT  */
        /****************************************/

        //DELETE
        $query  = " DELETE FROM ";
        $query .= "     AFT_DISEASE_ADDITION443_FIXED_HDAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "     FIXED_DATE          = '".$fixed_date."' ";

        $db->query($query);

        //INSERT
        $query = knje443Query::getAftDiseaseAddition443($model, "AFT_DISEASE_ADDITION443_HDAT");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $fixed_date;
            $data["SCHOOL_GROUP_TYPE"][TEXT]    = $row["SCHOOL_GROUP_TYPE"];
            $data["IDOU_DATE"][DATE]            = $row["IDOU_DATE"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";

            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION443_FIXED_HDAT");
            $db->query($query);

        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //データ取得
    function getAftDiseaseAddition443FixedDat($model) {
        $fixed_date = str_replace("/", "-", $model->field["FIXED_DATA"]);

        $query  = " SELECT DISTINCT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION443_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' AND ";
        $query .= "     YEAR             = '".CTRL_YEAR."' AND ";
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
        $query .= "     REPORT_AFT_DISEASE_ADDITION443_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "     EXECUTE_DATE        = '".$execute_date."' ";
        $db->query($query);
        $db2->query($query);

        //県側、学校側・報告テーブル
        //キー
        $data = array();
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["EXECUTE_DATE"][DATE]         = $execute_date;
        $data["FIXED_DATE"][DATE]           = $fixed_date;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        $query  = Query::insertSQL($data, "REPORT_AFT_DISEASE_ADDITION443_DAT");
        $db->query($query);
        $db2->query($query);

        //DELETE
        //県側・確定テーブル
        $query  = " DELETE FROM ";
        $query .= "     AFT_DISEASE_ADDITION443_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "     FIXED_DATE          = '".$fixed_date."' ";

        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得
        $query = knje443Query::getAftDiseaseAddition443FixedDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側・確定テーブル
            $data = array();
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $row["FIXED_DATE"];
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["SCHOOL_GROUP"][TEXT]         = $row["SCHOOL_GROUP"];
            $data["SCHOOL_CD"][TEXT]            = $row["SCHOOL_CD"];
            $data["SEQ"][TEXT]                  = $row["SEQ"];
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";

            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION443_FIXED_DAT");
            $db2->query($query);
        }
        $result->free();

        //報告処理
        $data = array();
        $data["ANSWER_FLG"][TEXT]      = "1";
        $data["ANSWER_DATE"][FUNC]     = "SYSDATE()";
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     YEAR = '".CTRL_YEAR."' AND ";
        $where .= "     DOC_NUMBER = {$model->field["DOC_NUMBER"]} AND ";
        $where .= "     EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";

        $query = Query::updateSQL($data, "AFT_SEARCH_REPORT_SCHOOL_DAT", $where);
        $db2->query($query);

        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }

    //CSV出力データ取得
    function getCsvQuery($model) {

        $fixed_date = str_replace("/", "-", $model->field["FIXED_DATA"]);
        $table = ($model->field["FIXED_DATA"]) ? "AFT_DISEASE_ADDITION443_FIXED_DAT" : "AFT_DISEASE_ADDITION443_DAT";

        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         SCHOOL_GROUP, ";
        $query .= "         SCHOOL_CD, ";
        $query .= "         SUM(CASE WHEN SEQ = '01' THEN INT_VAL1 ELSE 0 END) AS GOUKAKU_G, ";
        $query .= "         SUM(CASE WHEN SEQ = '02' THEN INT_VAL1 ELSE 0 END) AS GOUKAKU_K, ";
        $query .= "         SUM(INT_VAL1) AS GOUKAKU_T, ";
        $query .= "         SUM(CASE WHEN SEQ = '01' THEN INT_VAL2 ELSE 0 END) AS NAITEI_G, ";
        $query .= "         SUM(CASE WHEN SEQ = '02' THEN INT_VAL2 ELSE 0 END) AS NAITEI_K, ";
        $query .= "         SUM(INT_VAL2) AS NAITEI_T ";
        $query .= "     FROM ";
        $query .=           $table;
        $query .= "     WHERE ";
        $query .= "         EDBOARD_SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "         YEAR                = '".CTRL_YEAR."' ";
        if ($model->field["FIXED_DATA"]) {
            $query .= "     AND FIXED_DATE          = '".$fixed_date."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         SCHOOL_GROUP, ";
        $query .= "         SCHOOL_CD ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.COURSENAME, ";
        $query .= "     L2.MAJORNAME, ";
        $query .= "     L3.NAME1 AS SCHOOL_GROUP_NAME, ";
        $query .= "     L4.SCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     LEFT JOIN COURSE_MST L1 ON T1.COURSECD = L1.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST L2 ON T1.COURSECD = L2.COURSECD AND T1.MAJORCD = L2.MAJORCD ";
        $query .= "     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'E012' AND T1.SCHOOL_GROUP = L3.NAMECD2 ";
        $query .= "     LEFT JOIN COLLEGE_MST L4 ON T1.SCHOOL_CD = L4.SCHOOL_CD ";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.SCHOOL_GROUP, ";
        $query .= "     T1.SCHOOL_CD ";

        return $query;
    }
}
?>
