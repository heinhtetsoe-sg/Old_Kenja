<?php

require_once('for_php7.php');

class knjh702Query extends Query
{
    //名称マスタテスト区分取得
    public function getTestDivNameMst($namecd1)
    {
        $query  = "   SELECT NAMECD2 AS VALUE ";
        $query .= "        , NAME1 AS LABEL ";
        $query .= "     FROM V_NAME_MST ";
        $query .= "    WHERE YEAR    = '" . CTRL_YEAR . "' ";
        $query .= "      AND NAMECD1 = '" . $namecd1 . "' ";
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //名称マスタ文理区分取得
    public function getBunriDivNameMst($namecd1)
    {
        $query  = "    SELECT MST.ABBV1   AS LABEL ";
        $query .= "         , YDT.NAMECD2 AS VALUE ";
        $query .= "      FROM NAME_YDAT YDT ";
        $query .= " LEFT JOIN NAME_MST  MST ";
        $query .= "        ON MST.NAMECD1 = YDT.NAMECD1 ";
        $query .= "       AND MST.NAMECD2 = YDT.NAMECD2 ";
        $query .= "     WHERE YDT.YEAR    = '" . CTRL_YEAR . "' ";
        $query .= "       AND YDT.NAMECD1 = '" . $namecd1 . "' ";
        $query .= "  ORDER BY VALUE ";

        return $query;
    }

    //学力テスト科目リスト取得
    public function getAcademicTestSubclassList()
    {
        //学力テスト科目リスト
        $query  = " WITH SUBCLASS_TABLE AS( ";
        $query .= "    SELECT YEAR ";
        $query .= "         , TESTDIV ";
        $query .= "         , BUNRIDIV ";
        $query .= "         , CLASSCD ";
        $query .= "         , SUBCLASSCD ";
        $query .= "         , SUBCLASSNAME ";
        $query .= "         , SUBCLASSABBV ";
        $query .= "         , ELECTDIV ";
        $query .= "      FROM ACADEMICTEST_SUBCLASS_DAT ";
        $query .= "     WHERE YEAR = '" . CTRL_YEAR . "' ";
        $query .= " ), ";
        //テスト区分取得
        $query .= " TESTDIV AS ( ";
        $query .= "      SELECT ST.TESTDIV ";
        $query .= "           , NM.NAME1 AS TESTDIVNAME ";
        $query .= "        FROM SUBCLASS_TABLE ST ";
        $query .= "   LEFT JOIN NAME_MST NM ";
        $query .= "          ON NM.NAMECD1 = 'H320' ";
        $query .= "         AND NM.NAMECD2 = ST.TESTDIV ";
        $query .= "    GROUP BY ST.TESTDIV ";
        $query .= "           , NM.NAME1 ";
        $query .= " ), ";
        //文理区分取得
        $query .= " BUNRIDIV AS ( ";
        $query .= "      SELECT ST.BUNRIDIV ";
        $query .= "           , NM.ABBV1 AS BUNRI ";
        $query .= "        FROM SUBCLASS_TABLE ST ";
        $query .= "   LEFT JOIN NAME_MST NM ";
        $query .= "          ON NM.NAMECD1 = 'H319' ";
        $query .= "         AND NM.NAMECD2 = ST.BUNRIDIV ";
        $query .= "    GROUP BY ST.BUNRIDIV ";
        $query .= "           , NM.ABBV1 ";
        $query .= " ) ";
        //メインSQL
        $query .= "    SELECT ST.YEAR ";
        $query .= "         , ST.TESTDIV ";
        $query .= "         , ST.BUNRIDIV ";
        $query .= "         , ST.CLASSCD ";
        $query .= "         , ST.SUBCLASSCD ";
        $query .= "         , ST.SUBCLASSNAME ";
        $query .= "         , ST.SUBCLASSABBV ";
        $query .= "         , ST.ELECTDIV ";
        $query .= "         , TSD.TESTDIVNAME ";
        $query .= "         , BUD.BUNRI ";
        $query .= "      FROM SUBCLASS_TABLE ST ";
        $query .= " LEFT JOIN TESTDIV TSD ";
        $query .= "        ON ST.TESTDIV = TSD.TESTDIV ";
        $query .= " LEFT JOIN BUNRIDIV BUD ";
        $query .= "        ON ST.BUNRIDIV = BUD.BUNRIDIV ";
        $query .= "  ORDER BY ST.TESTDIV ";
        $query .= "         , ST.BUNRIDIV ";
        $query .= "         , ST.SUBCLASSCD ";

        return $query;
    }

    //学力テスト科目リスト取得
    public function getAcademicTestSubclassData($year, $testdiv = "", $bunridiv = "", $classcd = "", $subclasscd = "")
    {
        $query  = "   SELECT TESTDIV ";
        $query .= "        , BUNRIDIV ";
        $query .= "        , CLASSCD ";
        $query .= "        , SUBCLASSCD ";
        $query .= "        , SUBCLASSNAME ";
        $query .= "        , SUBCLASSABBV ";
        $query .= "        , ELECTDIV ";
        $query .= "     FROM ACADEMICTEST_SUBCLASS_DAT ";
        $query .= "    WHERE YEAR       = '" . $year . "' ";
        if ($testdiv    !== "" &&
            $bunridiv   !== "" &&
            $classcd    !== "" &&
            $subclasscd !== "") {
            //デフォルト引数が指定されているとき
            $query .= "  AND TESTDIV    = '" . $testdiv . "' ";
            $query .= "  AND BUNRIDIV   = '" . $bunridiv . "' ";
            $query .= "  AND CLASSCD    = '" . $classcd . "' ";
            $query .= "  AND SUBCLASSCD = '" . $subclasscd . "' ";
        }

        return $query;
    }

    //教科マスタ取得
    public function getClasscd()
    {
        $query  = " SELECT CLASSCD || '-' || SCHOOL_KIND || '：' || CLASSNAME AS LABEL ";
        $query .= "      , CLASSCD AS VALUE ";
        $query .= "   FROM CLASS_MST ";
        $query .= "  WHERE SCHOOL_KIND = '" . SCHOOLKIND . "' ";

        return $query;
    }

    //重複チェック
    public function duplicateCheck($year, $model)
    {
        $query  = "   SELECT COUNT(*) AS CNT ";
        $query .= "     FROM ACADEMICTEST_SUBCLASS_DAT ";
        $query .= "    WHERE YEAR       = '" . $year . "' ";
        $query .= "      AND TESTDIV    = '" . $model->field["TESTDIV"] . "' ";
        $query .= "      AND BUNRIDIV   = '" . $model->field["BUNRIDIV"] . "' ";
        $query .= "      AND CLASSCD    = '" . $model->field["CLASSCD"] . "' ";
        $query .= "      AND SUBCLASSCD = '" . $model->field["SUBCLASSCD"] . "' ";

        return $query;
    }

    //INSERT
    public function getInsertQuery($model)
    {
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["TESTDIV"][TEXT]      = $model->field["TESTDIV"];
        $data["BUNRIDIV"][TEXT]     = $model->field["BUNRIDIV"];
        $data["CLASSCD"][TEXT]      = $model->field["CLASSCD"];
        $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
        $data["SUBCLASSNAME"][TEXT] = $model->field["SUBCLASSNAME"];
        $data["SUBCLASSABBV"][TEXT] = $model->field["SUBCLASSABBV"];
        $data["ELECTDIV"][TEXT]     = $model->field["ELECTDIV"] == "2" ? "2" : "1";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "ACADEMICTEST_SUBCLASS_DAT");
        return $query;
    }

    //UPDATE
    public function getUpdateQuery($model)
    {
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["BUNRIDIV"][TEXT]      = $model->field["BUNRIDIV"];
        $data["CLASSCD"][TEXT]       = $model->field["CLASSCD"];
        $data["SUBCLASSCD"][TEXT]    = $model->field["SUBCLASSCD"];
        $data["SUBCLASSNAME"][TEXT]  = $model->field["SUBCLASSNAME"];
        $data["SUBCLASSABBV"][TEXT]  = $model->field["SUBCLASSABBV"];
        $data["ELECTDIV"][TEXT]      = $model->field["ELECTDIV"] == "2" ? "2" : "1";
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";

        $where  = " WHERE YEAR       = '".CTRL_YEAR."' ";
        $where .= "   AND TESTDIV    = '".$model->testdiv."' ";
        $where .= "   AND BUNRIDIV   = '".$model->bunridiv."' ";
        $where .= "   AND CLASSCD    = '".$model->classcd."' ";
        $where .= "   AND SUBCLASSCD = '".$model->subclasscd."' ";

        $query = Query::updateSQL($data, "ACADEMICTEST_SUBCLASS_DAT", $where);
        return $query;
    }

    //DELETE
    public function getDeleteQuery($model)
    {
        $query  = " DELETE FROM ACADEMICTEST_SUBCLASS_DAT ";
        $query .= "       WHERE YEAR       = '".CTRL_YEAR."' ";
        $query .= "         AND TESTDIV    = '".$model->testdiv."' ";
        $query .= "         AND BUNRIDIV   = '".$model->bunridiv."' ";
        $query .= "         AND CLASSCD    = '".$model->classcd."' ";
        $query .= "         AND SUBCLASSCD = '".$model->subclasscd."' ";

        return $query;
    }

    //前年度からのデータを今年度にCOPY
    public function copyInsert($row)
    {
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["TESTDIV"][TEXT]      = $row["TESTDIV"];
        $data["BUNRIDIV"][TEXT]     = $row["BUNRIDIV"];
        $data["CLASSCD"][TEXT]      = $row["CLASSCD"];
        $data["SUBCLASSCD"][TEXT]   = $row["SUBCLASSCD"];
        $data["SUBCLASSNAME"][TEXT] = $row["SUBCLASSNAME"];
        $data["SUBCLASSABBV"][TEXT] = $row["SUBCLASSABBV"];
        $data["ELECTDIV"][TEXT]     = $row["ELECTDIV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ACADEMICTEST_SUBCLASS_DAT");
        return $query;
    }
}
