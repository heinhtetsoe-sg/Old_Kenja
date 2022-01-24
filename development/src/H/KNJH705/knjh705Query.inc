<?php

require_once('for_php7.php');

class knjh705Query extends Query
{

    //教室情報取得
    public function getFacilityName()
    {
        $query  = "";
        $query  = "   SELECT FM.FACCD AS VALUE ";
        $query .= "        , FM.FACILITYNAME AS LABEL ";
        $query .= "     FROM FACILITY_MST FM ";
        $query .= " ORDER BY VALUE";

        return $query;
    }

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

    //表示用試験監督情報取得
    public function getSikenkantokuList($model)
    {
        $query  = "";
        $query  = "   SELECT ASTD.TESTID ";
        $query .= "        , AM.EXAM_DATE ";
        $query .= "        , NM.ABBV1 ";
        $query .= "        , ASTD. PERIODID";
        $query .= "        , FM.FACILITYNAME ";
        $query .= "        , ASTD.FACCD ";
        $query .= "        , ASTD.STAFFCD1 ";
        $query .= "        , ASTD.STAFFCD2 ";
        $query .= "        , ASTD.STAFFCD3 ";
        $query .= "        , SM1.STAFFNAME AS STAFFNAME1 ";
        $query .= "        , SM2.STAFFNAME AS STAFFNAME2 ";
        $query .= "        , SM3.STAFFNAME AS STAFFNAME3 ";
        $query .= "     FROM ACADEMICTEST_STF_DAT ASTD ";
        $query .= "LEFT JOIN ACADEMICTEST_MST AM ";
        $query .= "       ON AM.YEAR   = ASTD.YEAR ";
        $query .= "      AND AM.TESTID = ASTD.TESTID ";
        $query .= "LEFT JOIN NAME_MST NM ";
        $query .= "       ON NM.NAMECD1 = 'H321' ";
        $query .= "      AND NM.NAMECD2 = ASTD.PERIODID ";
        $query .= "LEFT JOIN FACILITY_MST FM ";
        $query .= "       ON FM.FACCD = ASTD.FACCD ";
        $query .= "LEFT JOIN STAFF_MST SM1 ";
        $query .= "       ON SM1.STAFFCD = ASTD.STAFFCD1 ";
        $query .= "LEFT JOIN STAFF_MST SM2 ";
        $query .= "       ON SM2.STAFFCD = ASTD.STAFFCD2 ";
        $query .= "LEFT JOIN STAFF_MST SM3 ";
        $query .= "       ON SM3.STAFFCD = ASTD.STAFFCD3 ";
        $query .= "    WHERE ASTD.YEAR   = '" . CTRL_YEAR . "'";
        $query .= "      AND ASTD.TESTID = '" . $model->field["TESTID"] . "'";
        $query .= " ORDER BY AM.EXAM_DATE ";
        $query .= "        , ASTD. PERIODID ";
        $query .= "        , ASTD.FACCD ";

        return $query;
    }

    //リンククリック時の試験監情報取得
    public function getSikenkantokuData($model)
    {
        $query  = "";
        $query .= "    SELECT ASTD.TESTID ";
        $query .= "         , ASTD.PERIODID ";
        $query .= "         , ASTD.FACCD ";
        $query .= "         , ASTD.STAFFCD1 ";
        $query .= "         , ASTD.STAFFCD2 ";
        $query .= "         , ASTD.STAFFCD3 ";
        $query .= "         , SM1.STAFFNAME AS STAFFNAME1 ";
        $query .= "         , SM2.STAFFNAME AS STAFFNAME2 ";
        $query .= "         , SM3.STAFFNAME AS STAFFNAME3 ";
        $query .= "      FROM ACADEMICTEST_STF_DAT ASTD ";
        $query .= " LEFT JOIN STAFF_MST SM1 ";
        $query .= "        ON SM1.STAFFCD = ASTD.STAFFCD1 ";
        $query .= " LEFT JOIN STAFF_MST SM2 ";
        $query .= "        ON SM2.STAFFCD = ASTD.STAFFCD2 ";
        $query .= " LEFT JOIN STAFF_MST SM3 ";
        $query .= "        ON SM3.STAFFCD = ASTD.STAFFCD3 ";
        $query .= "     WHERE ASTD.YEAR     = '" . CTRL_YEAR . "'";
        $query .= "       AND ASTD.TESTID   = '" .$model->testId. "'";
        $query .= "       AND ASTD.PERIODID = '" .$model->periodid. "'";
        $query .= "       AND ASTD.FACCD    = '" .$model->faccd. "'";

        return $query;
    }

    //テスト区分・時限・教室での重複チェック
    public function duplicateCheck($model)
    {
        $query  = "";
        $query  = " SELECT COUNT(*) AS CNT ";
        $query .= "   FROM ACADEMICTEST_STF_DAT ";
        $query .= "  WHERE YEAR     = '" . CTRL_YEAR . "'";
        $query .= "    AND TESTID   = '" .$model->field["TESTID"]. "'";
        $query .= "    AND PERIODID = '" .$model->field["PERIODID"]. "'";
        $query .= "    AND FACCD    = '" .$model->field["FACCD"]. "'";

        return $query;
    }

    //INSERT
    public function getInsertQuery($model)
    {
        //中抜けの場合
        if ($model->field["STAFFCD2"] == "" && $model->field["STAFFCD3"] != "") {
            $model->field["STAFFCD2"]   = $model->field["STAFFCD3"];
            $model->field["STAFFNAME2"] = $model->field["STAFFNAME3"];

            $model->field["STAFFCD3"]   = "";
            $model->field["STAFFNAME3"] = "";
        }
        $db = Query::dbCheckOut();
        $data["YEAR"][TEXT]       = CTRL_YEAR;
        $data["TESTID"][TEXT]     = $model->field["TESTID"];
        $data["PERIODID"][TEXT]   = $model->field["PERIODID"];
        $data["FACCD"][TEXT]      = $model->field["FACCD"];
        $data["STAFFCD1"][TEXT]   = $model->field["STAFFCD1"];
        $data["STAFFCD2"][TEXT]   = $model->field["STAFFCD2"];
        $data["STAFFCD3"][TEXT]   = $model->field["STAFFCD3"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "sysdate()";

        $query = Query::insertSQL($data, "ACADEMICTEST_STF_DAT");
        $db->query($query);
        Query::dbCheckIn($db);
    }

    //UPDATE
    public function getUpdateQuery($model)
    {
        //中抜けの場合
        if ($model->field["STAFFCD2"] == "" && $model->field["STAFFCD3"] != "") {
            $model->field["STAFFCD2"]   = $model->field["STAFFCD3"];
            $model->field["STAFFNAME2"] = $model->field["STAFFNAME3"];

            $model->field["STAFFCD3"]   = "";
            $model->field["STAFFNAME3"] = "";
        }
        $db = Query::dbCheckOut();
        $data["YEAR"][TEXT]       = CTRL_YEAR;
        $data["TESTID"][TEXT]     = $model->field["TESTID"];
        $data["PERIODID"][TEXT]   = $model->field["PERIODID"];
        $data["FACCD"][TEXT]      = $model->field["FACCD"];
        $data["STAFFCD1"][TEXT]   = $model->field["STAFFCD1"];
        $data["STAFFCD2"][TEXT]   = $model->field["STAFFCD2"];
        $data["STAFFCD3"][TEXT]   = $model->field["STAFFCD3"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "sysdate()";

        $where  = "  WHERE YEAR     = '" . CTRL_YEAR . "'";
        $where .= "    AND TESTID   = '" .$model->testId. "'";
        $where .= "    AND PERIODID = '" .$model->periodid. "'";
        $where .= "    AND FACCD    = '" .$model->faccd. "'";
        $where .= "    AND STAFFCD1 = '" .$model->staffcd1. "'";

        $query = Query::updateSQL($data, "ACADEMICTEST_STF_DAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
    }

    //DELETE
    public function getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ACADEMICTEST_STF_DAT ";
        $query .= "  WHERE YEAR     = '" . CTRL_YEAR . "'";
        $query .= "    AND TESTID   = '" .$model->testId. "'";
        $query .= "    AND PERIODID = '" .$model->periodid. "'";
        $query .= "    AND FACCD    = '" .$model->faccd. "'";
        $query .= "    AND STAFFCD1 = '" .$model->staffcd1. "'";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
