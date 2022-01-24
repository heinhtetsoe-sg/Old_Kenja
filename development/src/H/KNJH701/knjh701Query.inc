<?php

require_once('for_php7.php');

class knjh701Query extends Query
{
    //名称マスタ取得
    public function getNameMst($model, $namecd1)
    {
        $query  = "   SELECT NAMECD2 AS VALUE ";
        $query .= "        , NAME1 AS LABEL ";
        $query .= "     FROM V_NAME_MST ";
        $query .= "    WHERE YEAR    = '{$model->year}' ";
        $query .= "      AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //テスト区分・実施日付・テスト名称取得
    public function getTestKubunData()
    {
        $query  = "";
        $query  = "    SELECT NM.NAME1 AS TESTKUBUNNAME ";
        $query .= "         , AM.TESTDIV ";
        $query .= "         , AM.TESTID ";
        $query .= "         , AM.EXAM_DATE ";
        $query .= "         , AM.TESTNAME ";
        $query .= "      FROM ACADEMICTEST_MST AM ";
        $query .= " LEFT JOIN NAME_MST NM ";
        $query .= "        ON NM.NAMECD1 = 'H320' ";
        $query .= "       AND NM.NAMECD2 = AM.TESTDIV ";
        $query .= "     WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "  ORDER BY TESTDIV ";
        $query .= "         , TESTID ";

        return $query;
    }

    //ACADEMICTEST_MSTデータ取得
    public function getAcademicTestMst($year, $testDiv = "", $testId = "", $date = "")
    {
        $query  = "";
        $query  = " SELECT YEAR ";
        $query .= "      , TESTDIV ";
        $query .= "      , TESTID ";
        $query .= "      , EXAM_DATE ";
        $query .= "      , TESTNAME ";
        $query .= "      , TESTNAMEABBV ";
        $query .= "   FROM ACADEMICTEST_MST ";
        $query .= "  WHERE YEAR    = '" .$year. "' ";
        if ($testDiv !== "" &&
            $testId  !== "" &&
            $date    !== "" ) {
            //デフォルト引数が指定されているとき
            $query .= "    AND TESTDIV   = '" .$testDiv. "'";
            $query .= "    AND TESTID    = '" .$testDiv . $testId. "'";
            $query .= "    AND EXAM_DATE = '" .str_replace("/", "-", $date). "'";
        }

        return $query;
    }

    //重複チェック
    public function duplicateCheck($year, $mode, $model)
    {
        $query  = "";
        $query  = " SELECT COUNT(*) AS CNT ";
        $query .= "   FROM ACADEMICTEST_MST ";
        $query .= "  WHERE YEAR = '" .$year. "' ";

        switch ($mode) {
            case "1":
                //年度・テスト区分・テストIDの重複チェック
                $query .= "    AND TESTDIV   = '" .$model->field["TESTDIV"]. "'";
                $query .= "    AND TESTID    = '" .$model->field["TESTDIV"].sprintf("%02d", $model->field["TESTID"]). "'";
                break;
            case "2":
                //年度・実施日付の重複チェック
                $query .= "    AND EXAM_DATE = '" .str_replace("/", "-", $model->field["EXAM_DATE"]). "'";
                break;
        }
        return $query;
    }

    //INSERT
    public function getInsertQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["TESTDIV"][TEXT]      = $model->field["TESTDIV"];
        $data["TESTID"][TEXT]       = $model->field["TESTDIV"].sprintf("%02d", $model->field["TESTID"]);
        $data["EXAM_DATE"][TEXT]    = str_replace("/", "-", $model->field["EXAM_DATE"]);
        $data["TESTNAME"][TEXT]     = $model->field["TESTNAME"];
        $data["TESTNAMEABBV"][TEXT] = $model->field["TESTNAMEABBV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "ACADEMICTEST_MST");
        $db->query($query);
        Query::dbCheckIn($db);
    }

    //UPDATE
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["TESTDIV"][TEXT]      = $model->field["TESTDIV"];
        $data["TESTID"][TEXT]       = $model->field["TESTDIV"].sprintf("%02d", $model->field["TESTID"]);
        $data["EXAM_DATE"][TEXT]    = str_replace("/", "-", $model->field["EXAM_DATE"]);
        $data["TESTNAME"][TEXT]     = $model->field["TESTNAME"];
        $data["TESTNAMEABBV"][TEXT] = $model->field["TESTNAMEABBV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE YEAR      = '" .CTRL_YEAR. "' ";
        $where .= "   AND TESTDIV   = '" .$model->testdiv. "'";
        $where .= "   AND TESTID    = '" .$model->testdiv.sprintf("%02d", $model->testid). "'";
        $where .= "   AND EXAM_DATE = '" .$model->date. "'";

        $query = Query::updateSQL($data, "ACADEMICTEST_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //DELETE
    public function getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ACADEMICTEST_MST";
        $query .= "  WHERE YEAR      = '" .CTRL_YEAR. "' ";
        $query .= "    AND TESTDIV   = '" .$model->testdiv. "'";
        $query .= "    AND TESTID    = '" .$model->testdiv.sprintf("%02d", $model->testid). "'";
        $query .= "    AND EXAM_DATE = '" .$model->date. "'";

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //前年度からのデータを今年度にCOPY
    public function copyInsert($row)
    {
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["TESTDIV"][TEXT]      = $row["TESTDIV"];
        $data["TESTID"][TEXT]       = $row["TESTID"];
        $data["EXAM_DATE"][TEXT]    = str_replace("/", "-", $row["EXAM_DATE"]);
        $data["TESTNAME"][TEXT]     = $row["TESTNAME"];
        $data["TESTNAMEABBV"][TEXT] = $row["TESTNAMEABBV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "ACADEMICTEST_MST");
        return $query;
    }
}
