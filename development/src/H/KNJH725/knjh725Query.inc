<?php
require_once('for_php7.php');

class knjh725Query extends Query
{
    //テスト名称取得
    public function getNameMst($namecd1)
    {
        $query  = "";
        $query .= "   SELECT NAMECD2 AS VALUE ";
        $query .= "        , NAME1 AS LABEL ";
        $query .= "     FROM V_NAME_MST ";
        $query .= "    WHERE YEAR    = '" . CTRL_YEAR . "' ";
        $query .= "      AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //実行履歴取得
    public function getListRireki()
    {
        $query  = "";
        $query .= "    SELECT AED.CALC_DATE ";
        $query .= "         , AED.CALC_TIME ";
        $query .= "         , AED.REGISTERCD ";
        $query .= "         , CASE WHEN AED.TESTDIV = '9' THEN '学力テスト合計' ";
        $query .= "                ELSE VNM.NAME1 ";
        $query .= "           END AS LAVEL ";
        $query .= "         , SM.STAFFNAME ";
        $query .= "      FROM ACADEMICTEST_EXEC_DAT AED ";
        $query .= " LEFT JOIN V_NAME_MST VNM ";
        $query .= "        ON (AED.YEAR   = VNM.YEAR ";
        $query .= "       AND VNM.NAMECD1 = 'H320' ";
        $query .= "       AND AED.TESTDIV = VNM.NAMECD2) ";
        $query .= " LEFT JOIN STAFF_MST SM ";
        $query .= "        ON AED.REGISTERCD = SM.STAFFCD ";
        $query .= "     WHERE AED.YEAR = '" . CTRL_YEAR . "' ";
        $query .= "  ORDER BY AED.CALC_DATE DESC ";
        $query .= "         , AED.CALC_TIME DESC ";

        return $query;
    }

    //実行履歴追加
    public function executeRireki($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //実行日付・時間を取得
        $calcRow  = $db->getRow(knjh725Query::getCalcDateTime(), DB_FETCHMODE_ASSOC);
        $calcDate = $calcRow["CALC_DATE"];//実行日付
        $calcTime = $calcRow["CALC_TIME"];//実行時間

        //実行履歴データ・追加
        $query = knjh725Query::getInsertRireki($calcDate, $calcTime, $model);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //現在日付・時間を取得
    private function getCalcDateTime()
    {
        $query  = "";
        $query .= " WITH T_DATE_TIME (CALC_DATE,CALC_TIME) AS ( ";
        $query .= "     VALUES ( ";
        $query .= "         DATE(SYSDATE()) ";
        $query .= "       , TIME(SYSDATE()) ";
        $query .= "     ) ";
        $query .= " ) ";
        $query .= " SELECT * ";
        $query .= "   FROM T_DATE_TIME ";
        return $query;
    }

    //実行履歴登録
    private function getInsertRireki($calcDate, $calcTime, $model)
    {
        $data["YEAR"][TEXT]       = CTRL_YEAR;
        $data["TESTDIV"][TEXT]    = $model->field["TESTDIV"];
        $data["CALC_DATE"][TEXT]  = $calcDate;
        $data["CALC_TIME"][TEXT]  = $calcTime;
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "SYSDATE()";

        $query = Query::insertSQL($data, "ACADEMICTEST_EXEC_DAT");
        return $query;
    }

    //学力テスト序列確定実行
    public function executeQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->field["TESTDIV"] === "9") {
            knjh725Query::insertQuery($db, $model, "ACADEMICTEST_SUBCLASS_TOTALRANK_DAT");
            knjh725Query::insertQuery($db, $model, "ACADEMICTEST_ALL_TOTALSOCRE_DAT");
        } else {
            knjh725Query::insertQuery($db, $model, "ACADEMICTEST_SUBCLASS_RANK_DAT");
            knjh725Query::insertQuery($db, $model, "ACADEMICTEST_TOTALSCORE_DAT");
        }
        knjh725Query::insertQuery($db, $model, "ACADEMICTEST_SUBCLASS_STAT_DAT");
        knjh725Query::insertQuery($db, $model, "ACADEMICTEST_TOTAL_STAT_DAT");

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //テーブル別の処理
    private function insertQuery(&$db, $model, $tableName)
    {
        $query  = "";

        //削除クエリー
        $query = knjh725Query::getDeleteQuery($model, $tableName);
        $db->query($query);

        //追加クエリー
        $query = knjh725Query::getInsertQuery($model, $tableName);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //1行ずつデータを取得
            $data = knjh725Query::getFieldData($model, $tableName, $row);

            //追加
            $query = Query::insertSQL($data, $tableName);
            $db->query($query);
        }
    }

    //削除クエリー
    private function getDeleteQuery($model, $tableName)
    {
        $query  = "";
        $query .= " DELETE ";
        $query .= "   FROM {$tableName} ";
        $query .= "  WHERE YEAR = '" . CTRL_YEAR . "' ";
        if ($tableName === "ACADEMICTEST_SUBCLASS_RANK_DAT" ||
            $tableName === "ACADEMICTEST_TOTALSCORE_DAT"    ||
            $tableName === "ACADEMICTEST_SUBCLASS_STAT_DAT" ||
            $tableName === "ACADEMICTEST_TOTAL_STAT_DAT") {
            $query .= " AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        }

        return $query;
    }

    //テーブル別の処理の追加クエリー
    private function getInsertQuery($model, $tableName)
    {
        $query  = "";
        switch ($tableName) {
            case "ACADEMICTEST_SUBCLASS_RANK_DAT":
                $query = knjh725Query::getInsertAcademictestSubclassRankDatQuery($model);
                break;
            case "ACADEMICTEST_SUBCLASS_TOTALRANK_DAT":
                $query = knjh725Query::getInsertAcademictestSubclassTotalrankDatQuery();
                break;
            case "ACADEMICTEST_TOTALSCORE_DAT":
                $query = knjh725Query::getInsertAcademictestTotalscoreDatQuery($model);
                break;
            case "ACADEMICTEST_ALL_TOTALSOCRE_DAT":
                $query = knjh725Query::getInsertAcademictestAllTotalScoreDatQuery();
                break;
            case "ACADEMICTEST_SUBCLASS_STAT_DAT":
                $query = knjh725Query::getInsertAcademictestSubclassStatDatQuery($model);
                break;
            case "ACADEMICTEST_TOTAL_STAT_DAT":
                $query = knjh725Query::getInsertAcademictestTotalStatDatQuery($model);
                break;
        }

        return $query;
    }

    //fetchしたデータからinsert用の項目を取得する
    private function getFieldData($model, $tableName, $row)
    {
        //各テーブルで共通の項目
        $data["YEAR"][TEXT]       = CTRL_YEAR;
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "SYSDATE()";

        //テーブル別の項目
        switch ($tableName) {
            case "ACADEMICTEST_SUBCLASS_RANK_DAT":
                $data["TESTDIV"][TEXT]      = $row["TESTDIV"];
                $data["CLASSCD"][TEXT]      = $row["CLASSCD"];
                $data["SUBCLASSCD"][TEXT]   = $row["SUBCLASSCD"];
                $data["SCHREGNO"][TEXT]     = $row["SCHREGNO"];
                $data["RANK"][NUMBER]       = (int)$row["RANK"];
                $data["BUNRI_RANK"][NUMBER] = (int)$row["BUNRI_RANK"];
                break;
            case "ACADEMICTEST_SUBCLASS_TOTALRANK_DAT":
                $data["CLASSCD"][TEXT]       = $row["CLASSCD"];
                $data["SUBCLASSCD"][TEXT]    = $row["SUBCLASSCD"];
                $data["SCHREGNO"][TEXT]      = $row["SCHREGNO"];
                $data["TOTAL_SCORE"][NUMBER] = (int)$row["TOTAL_SCORE"];
                $data["RANK"][NUMBER]        = (int)$row["RANK"];
                $data["BUNRI_RANK"][NUMBER]  = (int)$row["BUNRI_RANK"];
                break;
            case "ACADEMICTEST_TOTALSCORE_DAT":
                $data["TESTDIV"][TEXT]         = $row["TESTDIV"];
                $data["SCHREGNO"][TEXT]        = $row["SCHREGNO"];
                $data["TOTAL_SCORE"][NUMBER]   = (int)$row["TOTAL_SCORE"];
                $data["TOTAL_AVERAGE"][NUMBER] = (float)$row["TOTAL_AVERAGE"];
                $data["RANK"][NUMBER]          = (int)$row["RANK"];
                $data["BUNRI_RANK"][NUMBER]    = (int)$row["BUNRI_RANK"];
                $data["POINT"][NUMBER]         = (float)$row["POINT"];
                break;
            case "ACADEMICTEST_ALL_TOTALSOCRE_DAT":
                $data["SCHREGNO"][TEXT]        = $row["SCHREGNO"];
                $data["TOTAL_SCORE"][NUMBER]   = (int)$row["TOTAL_SCORE"];
                $data["TOTAL_AVERAGE"][NUMBER] = (float)$row["TOTAL_AVERAGE"];
                $data["RANK"][NUMBER]          = (int)$row["RANK"];
                $data["BUNRI_RANK"][NUMBER]    = (int)$row["BUNRI_RANK"];
                $data["POINT"][NUMBER]         = (float)$row["POINT"];
                break;
            case "ACADEMICTEST_SUBCLASS_STAT_DAT":
                $data["TESTDIV"][TEXT]         = $model->field["TESTDIV"];
                $data["CLASSCD"][TEXT]         = $row["CLASSCD"];
                $data["SUBCLASSCD"][TEXT]      = $row["SUBCLASSCD"];
                $data["AVERAGE"][NUMBER]       = (float)$row["AVERAGE"];
                $data["BUN_AVERAGE"][NUMBER]   = (float)$row["BUN_AVERAGE"];
                $data["RI_AVERAGE"][NUMBER]    = (float)$row["RI_AVERAGE"];
                $data["MAX_SCORE"][NUMBER]     = (int)$row["MAX_SCORE"];
                $data["MAX_BUN_SCORE"][NUMBER] = (int)$row["MAX_BUN_SCORE"];
                $data["MAX_RI_SCORE"][NUMBER]  = (int)$row["MAX_RI_SCORE"];
                $data["MIN_SCORE"][NUMBER]     = (int)$row["MIN_SCORE"];
                $data["MIN_BUN_SCORE"][NUMBER] = (int)$row["MIN_BUN_SCORE"];
                $data["MIN_RI_SCORE"][NUMBER]  = (int)$row["MIN_RI_SCORE"];
                $data["STDDEV"][NUMBER]        = (float)$row["STDDEV"];
                $data["BUN_STDDEV"][NUMBER]    = (float)$row["BUN_STDDEV"];
                $data["RI_STDDEV"][NUMBER]     = (float)$row["RI_STDDEV"];
                break;
            case "ACADEMICTEST_TOTAL_STAT_DAT":
                $data["TESTDIV"][TEXT]         = $model->field["TESTDIV"];
                $data["AVERAGE"][NUMBER]       = (float)$row["AVERAGE"];
                $data["BUN_AVERAGE"][NUMBER]   = (float)$row["BUN_AVERAGE"];
                $data["RI_AVERAGE"][NUMBER]    = (float)$row["RI_AVERAGE"];
                $data["MAX_SCORE"][NUMBER]     = (int)$row["MAX_SCORE"];
                $data["MAX_BUN_SCORE"][NUMBER] = (int)$row["MAX_BUN_SCORE"];
                $data["MAX_RI_SCORE"][NUMBER]  = (int)$row["MAX_RI_SCORE"];
                $data["MIN_SCORE"][NUMBER]     = (int)$row["MIN_SCORE"];
                $data["MIN_BUN_SCORE"][NUMBER] = (int)$row["MIN_BUN_SCORE"];
                $data["MIN_RI_SCORE"][NUMBER]  = (int)$row["MIN_RI_SCORE"];
                $data["STDDEV"][NUMBER]        = (float)$row["STDDEV"];
                $data["BUN_STDDEV"][NUMBER]    = (float)$row["BUN_STDDEV"];
                $data["RI_STDDEV"][NUMBER]     = (float)$row["RI_STDDEV"];
                break;
        }

        return $data;
    }

    //学力テストポイント算出(文科型計算式)
    private function getGakuTePBun($gakuTeSum)
    {
        //算出方法：学力テスト合計点*0.2
        return  "ROUND((CAST({$gakuTeSum} AS DECIMAL(8, 5)) * 0.2), 1) ";
    }

    //学力テストポイント算出(理科型計算式)
    private function getGakuTePRi($gakuTeSum)
    {
        //算出方法：学力テスト合計点*4/3*0.2
        return " ROUND((CAST({$gakuTeSum} AS DECIMAL(8, 5)) * 4/3 * 0.2), 1) ";
    }

    //SQL集計関数(SUM)
    private function getSUM($colName)
    {
        return " SUM({$colName}) ";
    }

    //SQL集計関数(AVG)
    private function getAVG($colName)
    {
        return " ROUND(AVG(CAST({$colName} AS DECIMAL(8, 5))), 5) ";
    }

    //SQL集計関数(MAX)
    private function getMAX($colName)
    {
        return " MAX({$colName}) ";
    }

    //SQL集計関数(MIN)
    private function getMIN($colName)
    {
        return " MIN({$colName}) ";
    }

    //SQL集計関数(STDDEV)
    private function getSTDDEV($colName)
    {
        return " ROUND(STDDEV(CAST({$colName} AS DECIMAL(5, 1))), 1) ";
    }


    //1.各学力テスト回ごとの科目別の素点順位を取得SQL
    private function getInsertAcademictestSubclassRankDatQuery($model)
    {
        $query  = "";
        $query .= " WITH SCORE_BY_KAMOKU AS ( ";
        $query .= "    SELECT ASD.TESTDIV ";
        $query .= "         , ASD.CLASSCD ";
        $query .= "         , ASD.SUBCLASSCD ";
        $query .= "         , ASD.SCHREGNO ";
        $query .= "         , SAD.BUNRIDIV ";
        $query .= "         , ASD.SCORE_1 ";
        $query .= "      FROM ACADEMICTEST_SCORE_DAT ASD ";
        $query .= " LEFT JOIN SCHREG_ACADEMICTEST_DAT SAD ";
        $query .= "        ON (ASD.YEAR      = SAD.YEAR ";
        $query .= "       AND ASD.CLASSCD    = SAD.CLASSCD ";
        $query .= "       AND ASD.SUBCLASSCD = SAD.SUBCLASSCD ";
        $query .= "       AND ASD.SCHREGNO   = SAD.SCHREGNO) ";
        $query .= "     WHERE ASD.ABSENCE_FLG_1 IS NULL ";
        $query .= "       AND SAD.BUNRIDIV      IN ('1', '2') ";
        $query .= "       AND ASD.TESTDIV       = '{$model->field["TESTDIV"]}' ";
        $query .= "       AND ASD.YEAR          = '" . CTRL_YEAR . "' ";
        $query .= "       AND ASD.SCORE_1 IS NOT NULL ";
        $query .= " ) ";
        $query .= " SELECT TESTDIV ";
        $query .= "      , CLASSCD ";
        $query .= "      , SUBCLASSCD ";
        $query .= "      , SCHREGNO ";
        $query .= "      , SCORE_1 ";
        $query .= "      , BUNRIDIV ";
        $query .= "      , RANK() OVER (PARTITION BY TESTDIV ";
        $query .= "                                , CLASSCD ";
        $query .= "                                , SUBCLASSCD ";
        $query .= "                         ORDER BY SCORE_1 DESC) RANK ";
        $query .= "      , RANK() OVER (PARTITION BY TESTDIV ";
        $query .= "                                , BUNRIDIV ";
        $query .= "                                , CLASSCD ";
        $query .= "                                , SUBCLASSCD ";
        $query .= "                         ORDER BY SCORE_1 DESC) BUNRI_RANK ";
        $query .= "   FROM SCORE_BY_KAMOKU ";

        return $query;
    }

    //2.各学力テスト回の科目別の素点を合算した点数、その点数による全体順位・文理別順位取得SQL
    private function getInsertAcademictestSubclassTotalrankDatQuery()
    {
        $query  = "";
        $query .= " WITH TOTAL_SCORE_BY_KAMOKU AS ( ";
        $query .= "        SELECT ASD.CLASSCD ";
        $query .= "             , ASD.SUBCLASSCD ";
        $query .= "             , ASD.SCHREGNO ";
        $query .= "             , SAD.BUNRIDIV ";
        $query .= "             , " . knjh725Query::getSUM("ASD.SCORE_1") . " AS TOTAL_SCORE ";
        $query .= "          FROM ACADEMICTEST_SCORE_DAT ASD ";
        $query .= "     LEFT JOIN SCHREG_ACADEMICTEST_DAT SAD ";
        $query .= "            ON (ASD.YEAR      = SAD.YEAR ";
        $query .= "           AND ASD.CLASSCD    = SAD.CLASSCD ";
        $query .= "           AND ASD.SUBCLASSCD = SAD.SUBCLASSCD ";
        $query .= "           AND ASD.SCHREGNO   = SAD.SCHREGNO) ";
        $query .= "         WHERE ASD.ABSENCE_FLG_1 IS NULL ";
        $query .= "           AND SAD.BUNRIDIV      IN ('1', '2') ";
        $query .= "           AND ASD.YEAR          = '" . CTRL_YEAR . "' ";
        $query .= "           AND ASD.SCORE_1       IS NOT NULL ";
        $query .= "      GROUP BY ASD.CLASSCD ";
        $query .= "             , ASD.SUBCLASSCD ";
        $query .= "             , ASD.SCHREGNO ";
        $query .= "             , SAD.BUNRIDIV ";
        $query .= " ) ";
        $query .= " SELECT CLASSCD ";
        $query .= "      , SUBCLASSCD ";
        $query .= "      , SCHREGNO ";
        $query .= "      , TOTAL_SCORE ";
        $query .= "      , BUNRIDIV ";
        $query .= "      , RANK() OVER(PARTITION BY CLASSCD ";
        $query .= "                               , SUBCLASSCD ";
        $query .= "                        ORDER BY TOTAL_SCORE DESC) RANK ";
        $query .= "      , RANK() OVER(PARTITION BY BUNRIDIV ";
        $query .= "                               , CLASSCD ";
        $query .= "                               , SUBCLASSCD ";
        $query .= "                        ORDER BY TOTAL_SCORE DESC) BUNRI_RANK ";
        $query .= "   FROM TOTAL_SCORE_BY_KAMOKU ";

        return $query;
    }

    //3.各学力テスト回ごとの合計点、平均点、合計点による全体順位・文理別順位、学力テストポイント取得SQL
    private function getInsertAcademictestTotalscoreDatQuery($model)
    {
        $query  = "";
        $query .= " WITH TEST_TOTAL_SCORE_BY_TESTDIV AS ( ";
        $query .= "        SELECT ASD.TESTDIV ";
        $query .= "             , SAD.BUNRIDIV ";
        $query .= "             , ASD.SCHREGNO ";
        $query .= "             , " . knjh725Query::getSUM("ASD.SCORE_1") . " AS TOTAL_SCORE ";
        $query .= "             , " . knjh725Query::getAVG("ASD.SCORE_1") . " AS TOTAL_AVERAGE ";
        $query .= "          FROM ACADEMICTEST_SCORE_DAT ASD ";
        $query .= "     LEFT JOIN SCHREG_ACADEMICTEST_DAT SAD ";
        $query .= "            ON (ASD.YEAR      = SAD.YEAR ";
        $query .= "           AND ASD.CLASSCD    = SAD.CLASSCD ";
        $query .= "           AND ASD.SUBCLASSCD = SAD.SUBCLASSCD ";
        $query .= "           AND ASD.SCHREGNO   = SAD.SCHREGNO) ";
        $query .= "         WHERE ASD.ABSENCE_FLG_1 IS NULL ";
        $query .= "           AND SAD.BUNRIDIV      IN ('1', '2') ";
        $query .= "           AND ASD.TESTDIV       = '{$model->field["TESTDIV"]}' ";
        $query .= "           AND ASD.YEAR          = '" . CTRL_YEAR . "' ";
        $query .= "           AND ASD.SCORE_1       IS NOT NULL ";
        $query .= "      GROUP BY ASD.TESTDIV ";
        $query .= "             , ASD.SCHREGNO ";
        $query .= "             , SAD.BUNRIDIV ";
        $query .= " ) ";
        $query .= " SELECT TESTDIV ";
        $query .= "      , SCHREGNO ";
        $query .= "      , TOTAL_SCORE";
        $query .= "      , TOTAL_AVERAGE ";
        $query .= "      , BUNRIDIV ";
        $query .= "      , RANK() OVER(PARTITION BY TESTDIV ";
        $query .= "                        ORDER BY TOTAL_SCORE DESC) RANK ";
        $query .= "      , RANK() OVER(PARTITION BY TESTDIV ";
        $query .= "                               , BUNRIDIV ";
        $query .= "                        ORDER BY TOTAL_SCORE DESC) BUNRI_RANK ";
        $query .= "      , CASE WHEN BUNRIDIV = '1' THEN " . knjh725Query::getGakuTePBun("TOTAL_SCORE") . " ";
        $query .= "             WHEN BUNRIDIV = '2' THEN " . knjh725Query::getGakuTePRi("TOTAL_SCORE") . " ";
        $query .= "        END AS POINT ";
        $query .= "   FROM TEST_TOTAL_SCORE_BY_TESTDIV ";

        return $query;
    }

    //4.各学力テスト回の合計点を合算した点数、その点数の平均点、全体順位・文理別順位取得SQL
    private function getInsertAcademictestAllTotalScoreDatQuery()
    {
        $query  = "";
        $query .= " WITH TEST_TOTAL_SCORE AS ( ";
        $query .= "        SELECT SAD.BUNRIDIV ";
        $query .= "             , ASD.SCHREGNO ";
        $query .= "             , " . knjh725Query::getSUM("ASD.SCORE_1") . " AS TOTAL_SCORE ";
        $query .= "             , " . knjh725Query::getAVG("ASD.SCORE_1") . " AS TOTAL_AVERAGE ";
        $query .= "          FROM ACADEMICTEST_SCORE_DAT ASD ";
        $query .= "     LEFT JOIN SCHREG_ACADEMICTEST_DAT SAD ";
        $query .= "            ON (ASD.YEAR      = SAD.YEAR ";
        $query .= "           AND ASD.CLASSCD    = SAD.CLASSCD ";
        $query .= "           AND ASD.SUBCLASSCD = SAD.SUBCLASSCD ";
        $query .= "           AND ASD.SCHREGNO   = SAD.SCHREGNO) ";
        $query .= "         WHERE ASD.ABSENCE_FLG_1 IS NULL ";
        $query .= "           AND SAD.BUNRIDIV      IN ('1', '2') ";
        $query .= "           AND ASD.YEAR          = '" . CTRL_YEAR . "' ";
        $query .= "           AND ASD.SCORE_1       IS NOT NULL ";
        $query .= "      GROUP BY ASD.SCHREGNO ";
        $query .= "             , SAD.BUNRIDIV ";
        $query .= " ) ";
        $query .= " SELECT SCHREGNO ";
        $query .= "      , TOTAL_SCORE";
        $query .= "      , TOTAL_AVERAGE ";
        $query .= "      , BUNRIDIV ";
        $query .= "      , RANK() OVER(ORDER BY TOTAL_SCORE DESC) RANK ";
        $query .= "      , RANK() OVER(PARTITION BY BUNRIDIV ";
        $query .= "                        ORDER BY TOTAL_SCORE DESC) BUNRI_RANK ";
        $query .= "      , CASE WHEN BUNRIDIV = '1' THEN " . knjh725Query::getGakuTePBun("TOTAL_SCORE") . " ";
        $query .= "             WHEN BUNRIDIV = '2' THEN " . knjh725Query::getGakuTePRi("TOTAL_SCORE") . " ";
        $query .= "        END AS POINT ";
        $query .= "   FROM TEST_TOTAL_SCORE ";

        return $query;
    }

    //5-1.各学力テスト回ごとの科目別平均点（全体、文理別）、最高点（全体、文理別）、最低点（全体、文理別）、標準偏差（全体、文理別）
    //5-2.各学力テスト回の科目別合計点の平均点（全体、文理別）、最高点（全体、文理別）、最低点（全体、文理別）、標準偏差（全体、文理別）取得SQL
    private function getInsertAcademictestSubclassStatDatQuery($model)
    {
        $query  = "";
        //学力テスト区分で抽出したデータ
        $query .= " WITH SCORE_TABLE AS (";
        $query .= "        SELECT ASD.TESTDIV ";
        $query .= "             , SAD.BUNRIDIV ";
        $query .= "             , ASD.CLASSCD ";
        $query .= "             , ASD.SUBCLASSCD ";
        $query .= "             , ASD.SCORE_1 ";
        $query .= "          FROM ACADEMICTEST_SCORE_DAT ASD ";
        $query .= "     LEFT JOIN SCHREG_ACADEMICTEST_DAT SAD ";
        $query .= "            ON (ASD.YEAR      = SAD.YEAR ";
        $query .= "           AND ASD.CLASSCD    = SAD.CLASSCD ";
        $query .= "           AND ASD.SUBCLASSCD = SAD.SUBCLASSCD ";
        $query .= "           AND ASD.SCHREGNO   = SAD.SCHREGNO) ";
        $query .= "         WHERE ASD.ABSENCE_FLG_1 IS NULL ";
        $query .= "           AND SAD.BUNRIDIV      IN ('1', '2') ";
        $query .= "           AND ASD.YEAR          = '" . CTRL_YEAR . "' ";
        $query .= "           AND ASD.SCORE_1       IS NOT NULL ";
        //合計にチェックをしていない場合
        if ($model->field["TESTDIV"] !== "9") {
            $query .= "       AND ASD.TESTDIV       = '{$model->field["TESTDIV"]}' ";
        }
        $query .= " ), ";
        //教科CD・科目CDの一覧
        $query .= " CLASS_SUBCLASS AS ( ";
        $query .= "       SELECT ST.CLASSCD ";
        $query .= "            , ST.SUBCLASSCD ";
        $query .= "         FROM SCORE_TABLE ST ";
        $query .= "     GROUP BY ST.CLASSCD ";
        $query .= "            , ST.SUBCLASSCD ";
        $query .= " ), ";
        //生徒全体を対象に集計
        $query .= " ST_ALL AS ( ";
        $query .= "       SELECT ST.CLASSCD ";
        $query .= "            , ST.SUBCLASSCD ";
        $query .= "            , " . knjh725Query::getAVG("ST.SCORE_1") . " AS AVERAGE ";
        $query .= "            , " . knjh725Query::getMAX("ST.SCORE_1") . " AS MAX_SCORE ";
        $query .= "            , " . knjh725Query::getMIN("ST.SCORE_1") . " AS MIN_SCORE ";
        $query .= "            , " . knjh725Query::getSTDDEV("ST.SCORE_1") . " AS STDDEV ";
        $query .= "         FROM SCORE_TABLE ST ";
        $query .= "     GROUP BY ST.CLASSCD ";
        $query .= "            , ST.SUBCLASSCD ";
        $query .= " ), ";
        //文科型の生徒を対象に集計
        $query .= " ST_BUN AS ( ";
        $query .= "       SELECT ST.CLASSCD ";
        $query .= "            , ST.SUBCLASSCD ";
        $query .= "            , " . knjh725Query::getAVG("ST.SCORE_1") . " AS BUN_AVERAGE ";
        $query .= "            , " . knjh725Query::getMAX("ST.SCORE_1") . " AS MAX_BUN_SCORE ";
        $query .= "            , " . knjh725Query::getMIN("ST.SCORE_1") . " AS MIN_BUN_SCORE ";
        $query .= "            , " . knjh725Query::getSTDDEV("ST.SCORE_1") . " AS BUN_STDDEV ";
        $query .= "         FROM SCORE_TABLE ST ";
        $query .= "        WHERE ST.BUNRIDIV = '1' ";
        $query .= "     GROUP BY ST.CLASSCD ";
        $query .= "            , ST.SUBCLASSCD ";
        $query .= " ), ";
        //理科型の生徒を対象に集計
        $query .= " ST_RI AS ( ";
        $query .= "       SELECT ST.CLASSCD ";
        $query .= "            , ST.SUBCLASSCD ";
        $query .= "            , " . knjh725Query::getAVG("ST.SCORE_1") . " AS RI_AVERAGE ";
        $query .= "            , " . knjh725Query::getMAX("ST.SCORE_1") . " AS MAX_RI_SCORE ";
        $query .= "            , " . knjh725Query::getMIN("ST.SCORE_1") . " AS MIN_RI_SCORE ";
        $query .= "            , " . knjh725Query::getSTDDEV("ST.SCORE_1") . " AS RI_STDDEV ";
        $query .= "         FROM SCORE_TABLE ST ";
        $query .= "        WHERE ST.BUNRIDIV = '2' ";
        $query .= "     GROUP BY ST.CLASSCD ";
        $query .= "            , ST.SUBCLASSCD ";
        $query .= " ) ";
        //メインのSQL
        $query .= "    SELECT CS.CLASSCD ";
        $query .= "         , CS.SUBCLASSCD ";
        $query .= "         , SA.AVERAGE ";
        $query .= "         , SB.BUN_AVERAGE ";
        $query .= "         , SR.RI_AVERAGE ";
        $query .= "         , SA.MAX_SCORE ";
        $query .= "         , SB.MAX_BUN_SCORE ";
        $query .= "         , SR.MAX_RI_SCORE ";
        $query .= "         , SA.MIN_SCORE ";
        $query .= "         , SB.MIN_BUN_SCORE ";
        $query .= "         , SR.MIN_RI_SCORE ";
        $query .= "         , SA.STDDEV ";
        $query .= "         , SB.BUN_STDDEV ";
        $query .= "         , SR.RI_STDDEV ";
        $query .= "      FROM CLASS_SUBCLASS CS ";
        $query .= " LEFT JOIN ST_ALL SA ";
        $query .= "        ON (CS.CLASSCD   = SA.CLASSCD ";
        $query .= "       AND CS.SUBCLASSCD = SA.SUBCLASSCD) ";
        $query .= " LEFT JOIN ST_BUN SB ";
        $query .= "        ON (CS.CLASSCD   = SB.CLASSCD ";
        $query .= "       AND CS.SUBCLASSCD = SB.SUBCLASSCD) ";
        $query .= " LEFT JOIN ST_RI SR ";
        $query .= "        ON (CS.CLASSCD   = SR.CLASSCD ";
        $query .= "       AND CS.SUBCLASSCD = SR.SUBCLASSCD) ";

        return $query;
    }

    //6-1.各学力テスト回ごとの合計点の平均点（全体、文理別）、最高点（全体、文理別）、最低点（全体、文理別）、標準偏差（全体、文理別）
    //6-2.各学力テスト回の合計点を合算した点数の平均点（全体、文理別）、最高点（全体、文理別）、最低点（全体、文理別）、標準偏差（全体、文理別）取得SQL
    private function getInsertAcademictestTotalStatDatQuery($model)
    {
        $query  = "";
        //学力テスト区分で抽出したデータ
        $query .= " WITH SCORE_TABLE AS( ";
        if ($model->field["TESTDIV"] !== "9") {
            $query .= "      SELECT ASD.TESTDIV ";
        } else {
            $query .= "      SELECT '9' AS TESTDIV ";
        }
        $query .= "               , SAD.BUNRIDIV ";
        $query .= "               , ASD.SCORE_1 ";
        $query .= "            FROM ACADEMICTEST_SCORE_DAT ASD ";
        $query .= "       LEFT JOIN SCHREG_ACADEMICTEST_DAT SAD ";
        $query .= "              ON (ASD.YEAR      = SAD.YEAR AND ASD.CLASSCD = SAD.CLASSCD ";
        $query .= "             AND ASD.SUBCLASSCD = SAD.SUBCLASSCD ";
        $query .= "             AND ASD.SCHREGNO   = SAD.SCHREGNO) ";
        $query .= "           WHERE ASD.ABSENCE_FLG_1 IS NULL ";
        $query .= "             AND SAD.BUNRIDIV      IN ('1', '2') ";
        $query .= "             AND ASD.YEAR          = '" . CTRL_YEAR . "' ";
        $query .= "             AND ASD.SCORE_1       IS NOT NULL ";
        //合計にチェックをしていない場合
        if ($model->field["TESTDIV"] !== "9") {
            $query .= "         AND ASD.TESTDIV       = '{$model->field["TESTDIV"]}' ";
        }
        $query .= " ), ";
        //学力テスト区分の一覧
        $query .= " TEST_DIV AS ( ";
        $query .= "       SELECT ST.TESTDIV ";
        $query .= "         FROM SCORE_TABLE ST ";
        $query .= "     GROUP BY ST.TESTDIV ";
        $query .= " ), ";
        //生徒全体を対象に集計
        $query .= " ST_ALL AS ( ";
        $query .= "       SELECT ST.TESTDIV ";
        $query .= "            , " . knjh725Query::getAVG("ST.SCORE_1") . " AS AVERAGE ";
        $query .= "            , " . knjh725Query::getMAX("ST.SCORE_1") . " AS MAX_SCORE ";
        $query .= "            , " . knjh725Query::getMIN("ST.SCORE_1") . " AS MIN_SCORE ";
        $query .= "            , " . knjh725Query::getSTDDEV("ST.SCORE_1") . " AS STDDEV ";
        $query .= "         FROM SCORE_TABLE ST ";
        $query .= "     GROUP BY ST.TESTDIV ";
        $query .= " ), ";
        //文科型の生徒を対象に集計
        $query .= " ST_BUN AS ( ";
        $query .= "       SELECT ST.TESTDIV ";
        $query .= "            , " . knjh725Query::getAVG("ST.SCORE_1") . " AS BUN_AVERAGE ";
        $query .= "            , " . knjh725Query::getMAX("ST.SCORE_1") . " AS MAX_BUN_SCORE ";
        $query .= "            , " . knjh725Query::getMIN("ST.SCORE_1") . " AS MIN_BUN_SCORE ";
        $query .= "            , " . knjh725Query::getSTDDEV("ST.SCORE_1") . " AS BUN_STDDEV ";
        $query .= "         FROM SCORE_TABLE ST ";
        $query .= "        WHERE ST.BUNRIDIV = '1' ";
        $query .= "     GROUP BY ST.TESTDIV ";
        $query .= " ), ";
        //理科型の生徒を対象に集計
        $query .= " ST_RI AS ( ";
        $query .= "       SELECT ST.TESTDIV ";
        $query .= "            , " . knjh725Query::getAVG("ST.SCORE_1") . " AS RI_AVERAGE ";
        $query .= "            , " . knjh725Query::getMAX("ST.SCORE_1") . " AS MAX_RI_SCORE ";
        $query .= "            , " . knjh725Query::getMIN("ST.SCORE_1") . " AS MIN_RI_SCORE ";
        $query .= "            , " . knjh725Query::getSTDDEV("ST.SCORE_1") . " AS RI_STDDEV ";
        $query .= "         FROM SCORE_TABLE ST ";
        $query .= "        WHERE ST.BUNRIDIV = '2' ";
        $query .= "     GROUP BY ST.TESTDIV ";
        $query .= " ) ";
        //メインのSQL
        $query .= "    SELECT TD.TESTDIV ";
        $query .= "         , SA.AVERAGE ";
        $query .= "         , SB.BUN_AVERAGE ";
        $query .= "         , SR.RI_AVERAGE ";
        $query .= "         , SA.MAX_SCORE ";
        $query .= "         , SB.MAX_BUN_SCORE ";
        $query .= "         , SR.MAX_RI_SCORE ";
        $query .= "         , SA.MIN_SCORE ";
        $query .= "         , SB.MIN_BUN_SCORE ";
        $query .= "         , SR.MIN_RI_SCORE ";
        $query .= "         , SA.STDDEV ";
        $query .= "         , SB.BUN_STDDEV ";
        $query .= "         , SR.RI_STDDEV ";
        $query .= "      FROM TEST_DIV TD ";
        $query .= " LEFT JOIN ST_ALL SA ";
        $query .= "        ON TD.TESTDIV = SA.TESTDIV ";
        $query .= " LEFT JOIN ST_BUN SB ";
        $query .= "        ON TD.TESTDIV = SB.TESTDIV ";
        $query .= " LEFT JOIN ST_RI SR ";
        $query .= "        ON TD.TESTDIV = SR.TESTDIV ";

        return $query;
    }
}
