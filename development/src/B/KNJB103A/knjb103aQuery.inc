<?php
class knjb103aQuery extends Query
{
    public function getGrade()
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getTest($semester, $grade)
    {
        $query  = " SELECT distinct ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     INNER JOIN ADMIN_CONTROL_SDIV_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND T2.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "         AND T2.CLASSCD      = '00' ";
        $query .= "         AND T2.CURRICULUM_CD  = '00' ";
        $query .= "         AND T2.SUBCLASSCD  = '000000' ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "         AND T3.GRADE = '".$grade."' ";
        $query .= "         AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        $query .= "     AND T1.SCORE_DIV = '01' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //時限
    public function getSettingJigen($model)
    {
        $query = " SELECT ";
        $query .= "     name1  AS LABEL, ";
        $query .= "     namecd2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'B001' AND ";
        $query .= "     NAMESPARE1 IS NULL AND ";
        $query .= "     year = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ取得
    public function selectQuery($model, $periodCd = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.PERIODCD AS PERIODCD, ";
        $query .= "     CASE WHEN T1.PERIODCD = 'Z' THEN '礼拝' ELSE T2.NAME1 END AS PERIODNAME, ";
        if ($periodCd == "") {
            $query .= "     T1.STARTTIME_HOUR || ':' || T1.STARTTIME_MINUTE AS STARTTIME, ";
            $query .= "     T1.ENDTIME_HOUR || ':' || T1.ENDTIME_MINUTE AS ENDTIME ";
        } else {
            $query .= "     T1.STARTTIME_HOUR, T1.STARTTIME_MINUTE, ";
            $query .= "     T1.ENDTIME_HOUR, T1.ENDTIME_MINUTE ";
        }
        $query .= " FROM ";
        $query .= "     SCH_TESTTIME_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ";
        $query .= "       ON T2.YEAR = T1.YEAR ";
        $query .= "      AND T2.NAMECD1 = 'B001' ";
        $query .= "      AND T2.NAMECD2 = T1.PERIODCD ";
        $query .= " WHERE";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.TESTKINDCD   = '".substr($model->field["SUB_TESTCD"], 0, 2)."' ";
        $query .= "     AND T1.TESTITEMCD   = '".substr($model->field["SUB_TESTCD"], 2, 2)."' ";
        if ($periodCd != "") {
            $query .= "  AND T1.PERIODCD = '".$periodCd."' ";
        }
        if ($periodCd == "") {
            $query .= " ORDER BY ";
            $query .= "     STARTTIME ";
        }

        return $query;
    }

    //１レコード取得
    public function getRow($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCH_TESTTIME_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$model->year."' ";
        $query .= "   AND T1.GRADE        = '".$model->field["GRADE"]."' ";
        $query .= "   AND T1.SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "   AND T1.TESTKINDCD   = '".substr($model->field["SUB_TESTCD"], 0, 2)."' ";
        $query .= "   AND T1.TESTITEMCD   = '".substr($model->field["SUB_TESTCD"], 2, 2)."' ";
        $query .= "   AND T1.PERIODCD     = '".$model->field["PERIODNAME2"]."' ";
        return $query;
    }

    //INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["YEAR"][TEXT]                = $model->year;
        $data["GRADE"][TEXT]               = $model->field["GRADE"];
        $data["SEMESTER"][TEXT]            = $model->field["SEMESTER"];
        $data["TESTKINDCD"][TEXT]          = substr($model->field["SUB_TESTCD"], 0, 2);
        $data["TESTITEMCD"][TEXT]          = substr($model->field["SUB_TESTCD"], 2, 2);
        $data["PERIODCD"][TEXT]            = $model->field["PERIODNAME2"];
        if ($model->field["PERIODNAME2"] == "Z") {
            $data["WORSHIP_FLG"][TEXT]     = "1";
        } else {
            $data["WORSHIP_FLG"][TEXT]     = "";
        }
        $data["STARTTIME_HOUR"][TEXT]      = sprintf("%02d", $model->field["STARTTIME_HOUR"]);
        $data["STARTTIME_MINUTE"][TEXT]    = sprintf("%02d", $model->field["STARTTIME_MINUTE"]);
        $data["ENDTIME_HOUR"][TEXT]        = sprintf("%02d", $model->field["ENDTIME_HOUR"]);
        $data["ENDTIME_MINUTE"][TEXT]      = sprintf("%02d", $model->field["ENDTIME_MINUTE"]);
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $query = Query::insertSQL($data, "SCH_TESTTIME_DAT");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data = array();

        $data["YEAR"][TEXT]                = $model->year;
        $data["GRADE"][TEXT]               = $model->field["GRADE"];
        $data["SEMESTER"][TEXT]            = $model->field["SEMESTER"];
        $data["TESTKINDCD"][TEXT]          = substr($model->field["SUB_TESTCD"], 0, 2);
        $data["TESTITEMCD"][TEXT]          = substr($model->field["SUB_TESTCD"], 2, 2);
        $data["PERIODCD"][TEXT]            = $model->field["PERIODNAME2"];
        if ($model->field["PERIODNAME2"] == "Z") {
            $data["WORSHIP_FLG"][TEXT]     = "1";
        } else {
            $data["WORSHIP_FLG"][TEXT]     = "";
        }
        $data["STARTTIME_HOUR"][TEXT]      = sprintf("%02d", $model->field["STARTTIME_HOUR"]);
        $data["STARTTIME_MINUTE"][TEXT]    = sprintf("%02d", $model->field["STARTTIME_MINUTE"]);
        $data["ENDTIME_HOUR"][TEXT]        = sprintf("%02d", $model->field["ENDTIME_HOUR"]);
        $data["ENDTIME_MINUTE"][TEXT]      = sprintf("%02d", $model->field["ENDTIME_MINUTE"]);

        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $where  = " WHERE YEAR         = '".$model->year."' ";
        $where .= "   AND GRADE        = '".$model->field["GRADE"]."' ";
        $where .= "   AND SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $where .= "   AND TESTKINDCD   = '".substr($model->field["SUB_TESTCD"], 0, 2)."' ";
        $where .= "   AND TESTITEMCD   = '".substr($model->field["SUB_TESTCD"], 2, 2)."' ";
        $where .= "   AND PERIODCD     = '".$model->field["PERIODNAME2"]."' ";

        $query = Query::updateSQL($data, "SCH_TESTTIME_DAT", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM SCH_TESTTIME_DAT ";
        $query .= "       WHERE YEAR   = '".$model->year."' ";
        $query .= "   AND GRADE        = '".$model->field["GRADE"]."' ";
        $query .= "   AND SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "   AND TESTKINDCD   = '".substr($model->field["SUB_TESTCD"], 0, 2)."' ";
        $query .= "   AND TESTITEMCD   = '".substr($model->field["SUB_TESTCD"], 2, 2)."' ";
        $query .= "   AND PERIODCD     = '".$model->field["PERIODNAME2"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //ある年度にデータが存在するのかチェック
    public function getCheckSchTesttimeDatSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCH_TESTTIME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //COPY
    public function getCopyQuery($beforeYear, $nextYear)
    {
        $query  = "INSERT INTO ";
        $query  .= "     SCH_TESTTIME_DAT ";
        $query  .= "SELECT ";
        $query  .= "     '".$nextYear."' AS YEAR, ";
        $query  .= "     GRADE, ";
        $query  .= "     SEMESTER, ";
        $query  .= "     TESTKINDCD, ";
        $query  .= "     TESTITEMCD, ";
        $query  .= "     PERIODCD, ";
        $query  .= "     STARTTIME_HOUR, ";
        $query  .= "     STARTTIME_MINUTE, ";
        $query  .= "     ENDTIME_HOUR, ";
        $query  .= "     ENDTIME_MINUTE, ";
        $query  .= "     WORSHIP_FLG, ";
        $query  .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query  .= "     SYSDATE() ";
        $query  .= " FROM";
        $query  .= "     SCH_TESTTIME_DAT ";
        $query  .= " WHERE ";
        $query  .= "     YEAR = '".$beforeYear."' ";

        return $query;
    }
}
