<?php

require_once('for_php7.php');

class knjh704Query extends Query
{
    //時限取得
    public function getPeriod($model, $namecd1)
    {
        $query  = "";
        $query  = "   SELECT NAMECD2 AS VALUE ";
        $query .= "        , ABBV1 AS LABEL ";
        $query .= "     FROM NAME_MST ";
        $query .= "    WHERE NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY VALUE";

        return $query;
    }

    //テスト名称取得
    public function getTestName()
    {
        $query  = "";
        $query  = "   SELECT TESTID AS VALUE ";
        $query .= "        , TESTNAME AS LABEL ";
        $query .= "     FROM ACADEMICTEST_MST AM ";
        $query .= "    WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY TESTDIV ";
        $query .= "        , TESTID ";

        return $query;
    }

    //試験日程取得
    public function getTestNittei($model)
    {
        $query  = "";
        $query  = "    SELECT AM.EXAM_DATE ";
        $query .= "         , ASD.TESTID ";
        $query .= "         , NM.ABBV1 AS PERIODNAME";
        $query .= "         , ASUB.SUBCLASSNAME ";
        $query .= "         , ASD.BUNRIDIV || ASD.CLASSCD || ASD.SUBCLASSCD AS SUBCLASSCD";
        $query .= "         , ASD.START_HOUR || ':' || ASD.START_MINUTE AS STARTTIME ";
        $query .= "         , ASD.END_HOUR || ':' || ASD.END_MINUTE AS ENDTIME ";
        $query .= "         , ASD.START_HOUR ";
        $query .= "         , ASD.START_MINUTE ";
        $query .= "         , ASD.END_HOUR ";
        $query .= "         , ASD.END_MINUTE ";
        $query .= "         , ASD.PERIODID ";
        $query .= "      FROM ACADEMICTEST_SCH_DAT ASD ";
        $query .= " LEFT JOIN ACADEMICTEST_MST AM ";
        $query .= "        ON AM.YEAR = ASD.YEAR ";
        $query .= "       AND AM.TESTID = ASD.TESTID ";
        $query .= " LEFT JOIN ACADEMICTEST_SUBCLASS_DAT ASUB ";
        $query .= "        ON ASUB.YEAR = ASD.YEAR ";
        $query .= "       AND ASUB.BUNRIDIV   = ASD.BUNRIDIV ";
        $query .= "       AND ASUB.CLASSCD    = ASD.CLASSCD ";
        $query .= "       AND ASUB.SUBCLASSCD = ASD.SUBCLASSCD ";
        $query .= " LEFT JOIN NAME_MST NM ";
        $query .= "        ON NM.NAMECD1 = 'H321' ";
        $query .= "       AND NM.NAMECD2 = ASD.PERIODID ";
        $query .= "     WHERE ASD.YEAR   = '" . CTRL_YEAR . "'";
        $query .= "       AND ASD.TESTID = '" . $model->field["TESTID"] . "'";
        $query .= "       AND ASUB.TESTDIV = '" . substr($model->field["TESTID"], 0, 1) . "'";
        $query .= "  ORDER BY AM.EXAM_DATE ";
        $query .= "         , NM.ABBV1 ";
        $query .= "         , ASD.START_HOUR ";
        $query .= "         , ASD.END_HOUR ";
        $query .= "         , ASUB.BUNRIDIV ";
        $query .= "         , ASUB.SUBCLASSCD ";

        return $query;
    }

    //科目名取得
    public function getSubClassName($model)
    {
        $query  = "";
        $query  = "   SELECT ASD.BUNRIDIV || ASD.CLASSCD || ASD.SUBCLASSCD  AS SUBCLASSCD";
        $query .= "        , ASD.SUBCLASSNAME ";
        $query .= "     FROM ACADEMICTEST_SUBCLASS_DAT ASD ";
        $query .= "    WHERE YEAR = '" . CTRL_YEAR . "'";
        $query .= "      AND TESTDIV = '" . substr($model->field["TESTID"], 0, 1) . "'";
        $query .= " ORDER BY BUNRIDIV";
        $query .= "        , SUBCLASSCD";

        return $query;
    }

    //リンククリック時の試験日程取得
    public function getTestNitteiData($model)
    {
        $query  = "";
        $query .= " SELECT ASD.TESTID ";
        $query .= "      , ASD.PERIODID ";
        $query .= "      , ASD.START_HOUR ";
        $query .= "      , ASD.START_MINUTE ";
        $query .= "      , ASD.END_HOUR ";
        $query .= "      , ASD.END_MINUTE ";
        $query .= "      , ASD.BUNRIDIV || ASD.CLASSCD || ASD.SUBCLASSCD AS SUBCLASSCD";
        $query .= "   FROM ACADEMICTEST_SCH_DAT ASD ";
        $query .= "  WHERE ASD.YEAR         = '" . CTRL_YEAR . "'";
        $query .= "    AND ASD.TESTID       = '" .$model->testId. "'";
        $query .= "    AND ASD.PERIODID     = '" .$model->periodid. "'";
        $query .= "    AND ASD.START_HOUR   = '" .$model->startHour. "'";
        $query .= "    AND ASD.START_MINUTE = '" .$model->startMinute. "'";
        $query .= "    AND ASD.END_HOUR     = '" .$model->endHour. "'";
        $query .= "    AND ASD.END_MINUTE   = '" .$model->endMinute. "'";
        $query .= "    AND ASD.BUNRIDIV || ASD.CLASSCD || ASD.SUBCLASSCD = '" .$model->subclasscd. "'";

        return $query;
    }

    //学力テストID・科目での重複チェック
    public function duplicateCheck($model)
    {
        $query  = "";
        $query  = " SELECT COUNT(*) AS CNT ";
        $query .= "   FROM ACADEMICTEST_SCH_DAT ";
        $query .= "  WHERE YEAR       = '" . CTRL_YEAR . "'";
        $query .= "    AND TESTID     = '" .$model->field["TESTID"]. "'";
        $query .= "    AND BUNRIDIV || CLASSCD || SUBCLASSCD = '" .$model->field["SUBCLASSCD"]. "'";

        return $query;
    }

    //INSERT
    public function getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["TESTID"][TEXT]       = $model->field["TESTID"];
        $data["PERIODID"][TEXT]     = $model->field["PERIODID"];
        $data["START_HOUR"][TEXT]   = $model->field["START_HOUR"];
        $data["START_MINUTE"][TEXT] = $model->field["START_MINUTE"];
        $data["END_HOUR"][TEXT]     = $model->field["END_HOUR"];
        $data["END_MINUTE"][TEXT]   = $model->field["END_MINUTE"];
        $data["BUNRIDIV"][TEXT]     = substr($model->field["SUBCLASSCD"], 0, 1);
        $data["CLASSCD"][TEXT]      = substr($model->field["SUBCLASSCD"], 1, 2);
        $data["SUBCLASSCD"][TEXT]   = substr($model->field["SUBCLASSCD"], 3);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "ACADEMICTEST_SCH_DAT");

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //UPDATE
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["TESTID"][TEXT]       = $model->field["TESTID"];
        $data["PERIODID"][TEXT]     = $model->field["PERIODID"];
        $data["START_HOUR"][TEXT]   = $model->field["START_HOUR"];
        $data["START_MINUTE"][TEXT] = $model->field["START_MINUTE"];
        $data["END_HOUR"][TEXT]     = $model->field["END_HOUR"];
        $data["END_MINUTE"][TEXT]   = $model->field["END_MINUTE"];
        $data["BUNRIDIV"][TEXT]     = substr($model->field["SUBCLASSCD"], 0, 1);
        $data["CLASSCD"][TEXT]      = substr($model->field["SUBCLASSCD"], 1, 2);
        $data["SUBCLASSCD"][TEXT]   = substr($model->field["SUBCLASSCD"], 3);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = "  WHERE YEAR         = '" .CTRL_YEAR. "'";
        $where .= "    AND TESTID       = '" .$model->testId. "'";
        $where .= "    AND PERIODID     = '" .$model->periodid. "'";
        $where .= "    AND START_HOUR   = '" .$model->startHour. "'";
        $where .= "    AND START_MINUTE = '" .$model->startMinute. "'";
        $where .= "    AND END_HOUR     = '" .$model->endHour. "'";
        $where .= "    AND END_MINUTE   = '" .$model->endMinute. "'";
        $where .= "    AND BUNRIDIV     = '" .substr($model->subclasscd, 0, 1). "'";
        $where .= "    AND CLASSCD      = '" .substr($model->subclasscd, 1, 2). "'";
        $where .= "    AND SUBCLASSCD   = '" .substr($model->subclasscd, 3). "'";

        $query = Query::updateSQL($data, "ACADEMICTEST_SCH_DAT", $where);

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //DELETE
    public function getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ACADEMICTEST_SCH_DAT ";
        $query .= "  WHERE YEAR         = '" .CTRL_YEAR. "'";
        $query .= "    AND TESTID       = '" .$model->testId. "'";
        $query .= "    AND PERIODID     = '" .$model->periodid. "'";
        $query .= "    AND START_HOUR   = '" .$model->startHour. "'";
        $query .= "    AND START_MINUTE = '" .$model->startMinute. "'";
        $query .= "    AND END_HOUR     = '" .$model->endHour. "'";
        $query .= "    AND END_MINUTE   = '" .$model->endMinute. "'";
        $query .= "    AND BUNRIDIV     = '" .substr($model->subclasscd, 0, 1). "'";
        $query .= "    AND CLASSCD      = '" .substr($model->subclasscd, 1, 2). "'";
        $query .= "    AND SUBCLASSCD   = '" .substr($model->subclasscd, 3). "'";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
