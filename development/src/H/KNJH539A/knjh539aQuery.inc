<?php

require_once('for_php7.php');

class knjh539aquery extends Query {

    /* 学期取得 */
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND";
        $query .= "     SEMESTER < '9' ";

        return $query;
    }

    //学年取得
    function getGrade($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    T1.GRADE AS VALUE, ";
        $query .= "    L1.GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT T1 ";
        $query .= "    LEFT JOIN SCHREG_REGD_GDAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "         AND T1.GRADE = L1.GRADE ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "    AND L1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "    AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //Z010
    function getZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //区分
    function getProficiencyDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H508' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //実力テスト
    function getProficiencyMst($model)
    {
        $query  = "SELECT ";
        $query .= "    T1.PROFICIENCYCD AS VALUE, ";
        $query .= "    T1.PROFICIENCYCD || ':' || L1.PROFICIENCYNAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    PROFICIENCY_YMST T1 ";
        $query .= "    LEFT JOIN PROFICIENCY_MST L1 ON T1.PROFICIENCYDIV = L1.PROFICIENCYDIV ";
        $query .= "         AND T1.PROFICIENCYCD = L1.PROFICIENCYCD ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "    AND T1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "    AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //履歴一覧
    function getListRireki($model) {
        $query .= " SELECT ";
        $query .= "     T1.CALC_DATE, ";
        $query .= "     T1.CALC_TIME, ";
        $query .= "     L1.SEMESTERNAME AS SEMESTER_NAME, ";
        $query .= "     L2.GRADE_NAME1 AS GRADE_NAME, ";
        $query .= "     T1.PROFICIENCYDIV || ':' || L3.NAME1 AS PROFICIENCYDIV_NAME, ";
        $query .= "     T1.PROFICIENCYCD || ':' || L4.PROFICIENCYNAME1 AS PROFICIENCYCD_NAME ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_EXEC_DAT T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR AND L2.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'H508' AND L3.NAMECD2 = T1.PROFICIENCYDIV ";
        $query .= "     LEFT JOIN PROFICIENCY_MST L4 ON L4.PROFICIENCYDIV = T1.PROFICIENCYDIV AND L4.PROFICIENCYCD = T1.PROFICIENCYCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CALC_DATE DESC, ";
        $query .= "     T1.CALC_TIME DESC ";
        return $query;
    }
    //実行履歴
    function executeRireki($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //実行日付・時間を取得
        $calcRow = $db->getRow(knjh539aQuery::getCalcDateTime(), DB_FETCHMODE_ASSOC);
        $calcDate = $calcRow["CALC_DATE"];//実行日付
        $calcTime = $calcRow["CALC_TIME"];//実行時間
        //実行履歴データ・追加
        $query = knjh539aQuery::getInsertRireki($calcDate, $calcTime, $model);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
    //実行日付・時間を取得
    function getCalcDateTime() {
        $query  = " with t_date_time (CALC_DATE,CALC_TIME) as ( ";
        $query .= " values( ";
        $query .= "     date(sysdate()), ";
        $query .= "     time(sysdate()) ";
        $query .= " )) ";
        $query .= "  ";
        $query .= " select * from t_date_time ";
        return $query;
    }
    //実行履歴データ・追加
    function getInsertRireki($calcDate, $calcTime, $model) {
        $data = array();
        $data["CALC_DATE"][TEXT]            = $calcDate;
        $data["CALC_TIME"][TEXT]            = $calcTime;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
        $data["GRADE"][TEXT]                = $model->field["GRADE"];
        $data["PROFICIENCYDIV"][TEXT]       = $model->field["PROFICIENCYDIV"];
        $data["PROFICIENCYCD"][TEXT]        = $model->field["PROFICIENCYCD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $query = Query::insertSQL($data, "PROFICIENCY_EXEC_DAT");
        return $query;
    }

    //実力席次更新
    function UpdateProficiencyRankDat($model, $dataDiv)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($dataDiv == "1") {
            $delQuery  = " DELETE FROM ";
            $delQuery .= "      PROFICIENCY_RANK_DAT T1 ";
            $delQuery .= " WHERE ";
            $delQuery .= "      T1.YEAR = '".CTRL_YEAR."' ";
            $delQuery .= "      AND T1.SEMESTER = '".$model->field["SEMESTER"]."'";
            $delQuery .= "      AND T1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."'";
            $delQuery .= "      AND T1.PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."'";
            $delQuery .= "      AND EXISTS( ";
            $delQuery .= "              SELECT ";
            $delQuery .= "                  'x' ";
            $delQuery .= "              FROM ";
            $delQuery .= "                  SCHREG_REGD_DAT E1 ";
            $delQuery .= "              WHERE ";
            $delQuery .= "                  T1.SCHREGNO = E1.SCHREGNO ";
            $delQuery .= "                  AND T1.YEAR = E1.YEAR ";
            $delQuery .= "                  AND E1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $delQuery .= "                  AND E1.GRADE = '".$model->field["GRADE"]."' ";
            $delQuery .= "          ) ";

            $db->query($delQuery);
        }

        $query = knjh539aQuery::getProficiencyRankDat($model, "", "", "TUUJOU_KAMOKU", $dataDiv);
        if ($dataDiv == '1') {
            knjh539aQuery::insertProficiencyRankDat1($db, $query, $model, "TUUJOU_KAMOKU", $dataDiv);
        } else {
            knjh539aQuery::insertProficiencyRankDat2($db, $query, $model, "TUUJOU_KAMOKU", $dataDiv);
        }
        $db->commit();

        if ($model->Properties["deviationAvgRank"] == "1" && $dataDiv == "1") {
            foreach ($model->rankDivArray as $key => $val) {
                $query = knjh539aQuery::getProficiencyRankDatDeviation($model, "", "999999", "TUUJOU_KAMOKU", $key, $val);
                knjh539aQuery::insertProficiencyRankDatDeviation($db, $query, $model, "TUUJOU_KAMOKU", $key, $val);
            }
            $db->commit();
        }

        $groupMstQuery  = "SELECT DISTINCT ";
        $groupMstQuery .= "  GROUP_DIV ";
        $groupMstQuery .= "FROM ";
        $groupMstQuery .= "  PROFICIENCY_SUBCLASS_GROUP_MST ";
        $groupMstQuery .= "WHERE ";
        $groupMstQuery .= "  YEAR = '".CTRL_YEAR."' ";
        $groupMstQuery .= "  AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $groupMstQuery .= "  AND PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $groupMstQuery .= "  AND PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."' ";
        $groupMstQuery .= "  AND GRADE = '".$model->field["GRADE"]."' ";

        $result = $db->query($groupMstQuery);

        while ($groupMst = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $groupSubclass = "";
            for ($i = 0; $i < 6; $i++) {
                $groupSubclass .= $groupMst["GROUP_DIV"];
            }
            if ($groupMst["GROUP_DIV"] == '9') {
                if ($dataDiv == "1") {
                    $instate = " IN ('01', '02', '03', '04')";
                } else {
                    $instate = " IN ('11', '12', '13', '14')";
                }
                $delQuery  = " DELETE FROM ";
                $delQuery .= "     PROFICIENCY_RANK_DAT T1 ";
                $delQuery .= " WHERE ";
                $delQuery .= "     YEAR = '".CTRL_YEAR."' ";
                $delQuery .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
                $delQuery .= "     AND PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
                $delQuery .= "     AND PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."' ";
                $delQuery .= "     AND PROFICIENCY_SUBCLASS_CD = '".$groupSubclass."' ";
                $delQuery .= "     AND RANK_DATA_DIV ".$instate."";
                $delQuery .= "     AND EXISTS( ";
                $delQuery .= "              SELECT ";
                $delQuery .= "                  'x' ";
                $delQuery .= "              FROM ";
                $delQuery .= "                  SCHREG_REGD_DAT E1 ";
                $delQuery .= "              WHERE ";
                $delQuery .= "                  T1.SCHREGNO = E1.SCHREGNO ";
                $delQuery .= "                  AND T1.YEAR = E1.YEAR ";
                $delQuery .= "                  AND E1.SEMESTER = '".$model->field["SEMESTER"]."' ";
                $delQuery .= "                  AND E1.GRADE = '".$model->field["GRADE"]."' ";
                $delQuery .= "          ) ";
                $db->query($delQuery);
            }

            $query = knjh539aQuery::getProficiencyRankDat($model, $groupMst, $groupSubclass, "GASSAN_KAMOKU", $dataDiv);
            if ($dataDiv == '1') {
                knjh539aQuery::insertProficiencyRankDat1($db, $query, $model, "GASSAN_KAMOKU", $dataDiv);
            } else {
                knjh539aQuery::insertProficiencyRankDat2($db, $query, $model, "GASSAN_KAMOKU", $dataDiv);
            }
            $db->commit();

            if ($model->Properties["deviationAvgRank"] == "1" && $dataDiv == "1") {
                foreach ($model->rankDivArray as $key => $val) {
                    $query = knjh539aQuery::getProficiencyRankDatDeviation($model, $groupMst, $groupSubclass, "GASSAN_KAMOKU", $key, $val);

                    knjh539aQuery::insertProficiencyRankDatDeviation($db, $query, $model, "GASSAN_KAMOKU", $key, $val);
                    $db->commit();
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    function getProficiencyRankDat($model, $groupMst, $groupSubclass, $div, $dataDiv)
    {
        $query  = " WITH PROFICIENCY_DAT2 AS (";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     PROFICIENCYDIV, ";
        $query .= "     PROFICIENCYCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     CASE WHEN SCORE_DI = '*' ";
        $query .= "          THEN NULL ";
        $query .= "          ELSE SCORE ";
        $query .= "     END AS SCORE, ";
        $query .= "     SCORE_DI ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "     AND PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= " ), MOCT_T AS (";
        if ($div == "TUUJOU_KAMOKU") {
            $query .= " SELECT ";
            $query .= "     LT1.PROFICIENCYDIV, ";
            $query .= "     LT1.PROFICIENCYCD, ";
            $query .= "     '999999' AS PROFICIENCY_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL2.GROUP_CD, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE AS COURSE, ";
            $query .= "     SUM (CASE WHEN LT1.SCORE_DI = '*' ";
            $query .= "               THEN 1 ";
            $query .= "               ELSE 0 ";
            $query .= "          END) ";
            $query .= "     AS SCORE_DIS, ";
            if ($dataDiv == "2") {
                if ($model->z010 == 'meiji') {
                    $query .= "     SUM(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1)*100,-2)/100) AS SCORE, ";
                    $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.SCORE))*100000,0)/100000,9,5) AS AVG, ";
                    $query .= "     DECIMAL(ROUND(((FLOAT(SUM(LT1.SCORE))/FLOAT(SUM(VALUE(LL3.PERFECT, 100))))*100)*100000,0)/100000,9,5) AS AVG2 ";
                } else {
                    $query .= "     SUM(CEIL(FLOAT(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1)*100,-2)/100) )) AS SCORE, ";
                    $query .= "     DECIMAL(ROUND(FLOAT(SUM(CEIL(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1), 0))))/FLOAT(SUM(VALUE(LL3.PERFECT, 100) * VALUE(LL3.WEIGHTING, 1)))*100,6),9,5) AS AVG, "; // 合計 / 満点合計
                    $query .= "     DECIMAL(ROUND(FLOAT(SUM(CEIL(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1), 0))))/FLOAT(SUM(VALUE(LL3.PERFECT, 100) * VALUE(LL3.WEIGHTING, 1)))*100,6),9,5) AS AVG2 "; // 合計 / 満点合計
                }
            } else {
                $query .= "     SUM(LT1.SCORE) AS SCORE, ";
                $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.SCORE))*100000,0)/100000,9,5) AS AVG, ";
                $query .= "     DECIMAL(ROUND(((FLOAT(SUM(LT1.SCORE))/FLOAT(SUM(VALUE(LL3.PERFECT, 100))))*100)*100000,0)/100000,9,5) AS AVG2 ";
            }
            $query .= " FROM ";
            $query .= "     PROFICIENCY_DAT2 LT1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
            $query .= "         AND LL1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "         AND LL1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT LL2 ON LL2.YEAR = LT1.YEAR ";
            $query .= "          AND LL2.GRADE = LL1.GRADE ";
            $query .= "          AND LL2.COURSECD || LL2.MAJORCD || LL2.COURSECODE = LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE ";
            $query .= "     LEFT JOIN PROFICIENCY_PERFECT_COURSE_DAT LL3 ON LL3.YEAR = LT1.YEAR ";
            $query .= "          AND LL3.SEMESTER = LT1.SEMESTER ";
            $query .= "          AND LL3.PROFICIENCYDIV = LT1.PROFICIENCYDIV ";
            $query .= "          AND LL3.PROFICIENCYCD = LT1.PROFICIENCYCD ";
            $query .= "          AND LL3.PROFICIENCY_SUBCLASS_CD = LT1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "          AND LL3.GRADE = CASE WHEN LL3.DIV = '01' ";
            $query .= "                           THEN '00' ";
            $query .= "                           ELSE LL1.GRADE ";
            $query .= "                      END ";
            $query .= "          AND LL3.COURSECD || LL3.MAJORCD || LL3.COURSECODE = CASE WHEN LL3.DIV IN ('01','02') ";
            $query .= "                                                       THEN '00000000' ";
            $query .= "                                                       ELSE CASE WHEN DIV = '04' ";
            $query .= "                                                                 THEN '0' || LL2.GROUP_CD || '0000' ";
            $query .= "                                                                 ELSE LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE ";
            $query .= "                                                            END ";
            $query .= "                                                  END ";
            $query .= " WHERE ";
            $query .= "     (LT1.SCORE IS NOT NULL OR (LT1.SCORE IS NULL AND LT1.SCORE_DI = '*')) ";
            $query .= " GROUP BY ";
            $query .= "     LT1.PROFICIENCYDIV, ";
            $query .= "     LT1.PROFICIENCYCD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL2.GROUP_CD, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECODE ";
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     LT1.PROFICIENCYDIV, ";
            $query .= "     LT1.PROFICIENCYCD, ";
            $query .= "     LT1.PROFICIENCY_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL2.GROUP_CD, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE AS COURSE, ";
            $query .= "     SUM (CASE WHEN LT1.SCORE_DI = '*' ";
            $query .= "               THEN 1 ";
            $query .= "               ELSE 0 ";
            $query .= "          END) ";
            $query .= "     AS SCORE_DIS, ";
            if ($dataDiv == "2") {
                if ($model->z010 == 'meiji') {
                    $query .= "     SUM(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1)*100,-2)/100) AS SCORE, ";
                    $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.SCORE))*100000,0)/100000,9,5) AS AVG, ";
                    $query .= "     DECIMAL(ROUND(((FLOAT(SUM(LT1.SCORE))/FLOAT(SUM(VALUE(LL3.PERFECT, 100))))*100)*100000,0)/100000,9,5) AS AVG2 ";
                } else {
                    $query .= "     SUM(CEIL(FLOAT(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1)*100,-2)/100) )) AS SCORE, ";
                    $query .= "     DECIMAL(ROUND(FLOAT(SUM(CEIL(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1), 0))))/FLOAT(SUM(VALUE(LL3.PERFECT, 100) * VALUE(LL3.WEIGHTING, 1)))*100,6),9,5) AS AVG, "; // 合計 / 満点合計
                    $query .= "     DECIMAL(ROUND(FLOAT(SUM(CEIL(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1), 0))))/FLOAT(SUM(VALUE(LL3.PERFECT, 100) * VALUE(LL3.WEIGHTING, 1)))*100,6),9,5) AS AVG2 "; // 合計 / 満点合計
                }
            } else {
                $query .= "     SUM(LT1.SCORE) AS SCORE, ";
                $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.SCORE))*100000,0)/100000,9,5) AS AVG, ";
                $query .= "     DECIMAL(ROUND(((FLOAT(SUM(LT1.SCORE))/FLOAT(SUM(VALUE(LL3.PERFECT, 100))))*100)*100000,0)/100000,9,5) AS AVG2 ";
            }
            $query .= " FROM ";
            $query .= "     PROFICIENCY_DAT2 LT1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
            $query .= "         AND LL1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "         AND LL1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT LL2 ON LL2.YEAR = LT1.YEAR ";
            $query .= "          AND LL2.GRADE = LL1.GRADE ";
            $query .= "          AND LL2.COURSECD || LL2.MAJORCD || LL2.COURSECODE = LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE ";
            $query .= "     LEFT JOIN PROFICIENCY_PERFECT_COURSE_DAT LL3 ON LL3.YEAR = LT1.YEAR ";
            $query .= "          AND LL3.SEMESTER = LT1.SEMESTER ";
            $query .= "          AND LL3.PROFICIENCYDIV = LT1.PROFICIENCYDIV ";
            $query .= "          AND LL3.PROFICIENCYCD = LT1.PROFICIENCYCD ";
            $query .= "          AND LL3.PROFICIENCY_SUBCLASS_CD = LT1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "          AND LL3.GRADE = CASE WHEN LL3.DIV = '01' ";
            $query .= "                           THEN '00' ";
            $query .= "                           ELSE LL1.GRADE ";
            $query .= "                      END ";
            $query .= "          AND LL3.COURSECD || LL3.MAJORCD || LL3.COURSECODE = CASE WHEN LL3.DIV IN ('01','02') ";
            $query .= "                                                       THEN '00000000' ";
            $query .= "                                                       ELSE CASE WHEN DIV = '04' ";
            $query .= "                                                                 THEN '0' || LL2.GROUP_CD || '0000' ";
            $query .= "                                                                 ELSE LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE ";
            $query .= "                                                            END ";
            $query .= "                                                  END ";
            $query .= " WHERE ";
            $query .= "     LT1.SCORE IS NOT NULL ";
            $query .= " GROUP BY ";
            $query .= "     LT1.PROFICIENCYDIV, ";
            $query .= "     LT1.PROFICIENCYCD, ";
            $query .= "     LT1.PROFICIENCY_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL2.GROUP_CD, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECODE ";
        } else {
            $query .= " SELECT ";
            $query .= "     LT1.PROFICIENCYDIV, ";
            $query .= "     LT1.PROFICIENCYCD, ";
            $query .= "     '".$groupSubclass."' AS PROFICIENCY_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL2.GROUP_CD, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE AS COURSE, ";
            if ($dataDiv == "2") {
                if ($model->z010 == 'meiji') {
                    $query .= "     SUM(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1)*100,-2)/100) AS SCORE, ";
                    $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.SCORE))*100000,0)/100000,9,5) AS AVG, ";
                    $query .= "     DECIMAL(ROUND(((FLOAT(SUM(LT1.SCORE))/FLOAT(SUM(VALUE(LL3.PERFECT, 100))))*100)*100000,0)/100000,9,5) AS AVG2 ";
                } else {
                    $query .= "     SUM(CEIL(FLOAT(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1)*100,-2)/100) )) AS SCORE, ";
                    $query .= "     DECIMAL(ROUND(FLOAT(SUM(CEIL(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1), 0))))/FLOAT(SUM(VALUE(LL3.PERFECT, 100) * VALUE(LL3.WEIGHTING, 1)))*100,6),9,5) AS AVG, "; // 合計 / 満点合計
                    $query .= "     DECIMAL(ROUND(FLOAT(SUM(CEIL(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1), 0))))/FLOAT(SUM(VALUE(LL3.PERFECT, 100) * VALUE(LL3.WEIGHTING, 1)))*100,6),9,5) AS AVG2 "; // 合計 / 満点合計
                }
            } else {
                $query .= "     SUM(LT1.SCORE) AS SCORE, ";
                $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.SCORE))*100000,0)/100000,9,5) AS AVG, ";
                $query .= "     DECIMAL(ROUND(((FLOAT(SUM(LT1.SCORE))/FLOAT(SUM(VALUE(LL3.PERFECT, 100))))*100)*100000,0)/100000,9,5) AS AVG2 ";
            }
            $query .= " FROM ";
            $query .= "     PROFICIENCY_DAT2 LT1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
            $query .= "         AND LL1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "         AND LL1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT LL2 ON LL2.YEAR = LT1.YEAR ";
            $query .= "          AND LL2.GRADE = LL1.GRADE ";
            $query .= "          AND LL2.COURSECD || LL2.MAJORCD || LL2.COURSECODE = LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE ";
            $query .= "     LEFT JOIN PROFICIENCY_PERFECT_COURSE_DAT LL3 ON LL3.YEAR = LT1.YEAR ";
            $query .= "          AND LL3.SEMESTER = LT1.SEMESTER ";
            $query .= "          AND LL3.PROFICIENCYDIV = LT1.PROFICIENCYDIV ";
            $query .= "          AND LL3.PROFICIENCYCD = LT1.PROFICIENCYCD ";
            $query .= "          AND LL3.PROFICIENCY_SUBCLASS_CD = LT1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "          AND LL3.GRADE = CASE WHEN LL3.DIV = '01' ";
            $query .= "                           THEN '00' ";
            $query .= "                           ELSE LL1.GRADE ";
            $query .= "                      END ";
            $query .= "          AND LL3.COURSECD || LL3.MAJORCD || LL3.COURSECODE = CASE WHEN LL3.DIV IN ('01','02') ";
            $query .= "                                                       THEN '00000000' ";
            $query .= "                                                       ELSE CASE WHEN DIV = '04' ";
            $query .= "                                                                 THEN '0' || LL2.GROUP_CD || '0000' ";
            $query .= "                                                                 ELSE LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE ";
            $query .= "                                                            END ";
            $query .= "                                                  END, ";
            $query .= "     PROFICIENCY_SUBCLASS_GROUP_DAT L2 ";
            $query .= " WHERE ";
            $query .= "     LT1.SCORE IS NOT NULL ";
            $query .= "     AND L2.YEAR = LT1.YEAR ";
            $query .= "         AND L2.SEMESTER = LT1.SEMESTER ";
            $query .= "         AND L2.PROFICIENCYDIV = LT1.PROFICIENCYDIV ";
            $query .= "         AND L2.PROFICIENCYCD = LT1.PROFICIENCYCD ";
            $query .= "         AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "         AND L2.GRADE = LL1.GRADE ";
            $query .= "         AND L2.COURSECD = LL1.COURSECD ";
            $query .= "         AND L2.MAJORCD = LL1.MAJORCD ";
            $query .= "         AND L2.COURSECODE = LL1.COURSECODE ";
            $query .= "         AND L2.PROFICIENCY_SUBCLASS_CD = LT1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "     AND EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "            (SELECT ";
            $query .= "                 E1.SCHREGNO ";
            $query .= "             FROM ";
            $query .= "                (SELECT ";
            $query .= "                     ET1.PROFICIENCYDIV, ";
            $query .= "                     ET1.PROFICIENCYCD, ";
            $query .= "                     EL3.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                     EL3.SCHOOL_KIND, ";
                $query .= "                     EL3.CURRICULUM_CD, ";
                $query .= "                     EL3.SUBCLASSCD, ";
            }
            $query .= "                     SUM (CASE WHEN ET1.SCORE_DI = '*' ";
            $query .= "                               THEN 1 ";
            $query .= "                               ELSE 0 ";
            $query .= "                          END) ";
            $query .= "                     AS SCORE_DIS, ";
            $query .= "                     ET1.SCHREGNO ";
            $query .= "                 FROM ";
            $query .= "                     PROFICIENCY_DAT2 ET1 ";
            $query .= "                     INNER JOIN SCHREG_REGD_DAT EL1 ON EL1.YEAR = ET1.YEAR ";
            $query .= "                         AND EL1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "                         AND EL1.SCHREGNO = ET1.SCHREGNO ";
            $query .= "                         AND EL1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "                     LEFT JOIN PROFICIENCY_SUBCLASS_MST EL3 ON EL3.PROFICIENCY_SUBCLASS_CD = ET1.PROFICIENCY_SUBCLASS_CD, ";
            $query .= "                     PROFICIENCY_SUBCLASS_GROUP_DAT L2 ";
            $query .= "                 WHERE ";
            $query .= "                     (ET1.SCORE IS NOT NULL OR (ET1.SCORE IS NULL AND ET1.SCORE_DI = '*')) ";
            $query .= "                     AND L2.YEAR = ET1.YEAR ";
            $query .= "                         AND L2.SEMESTER = ET1.SEMESTER ";
            $query .= "                         AND L2.PROFICIENCYDIV = ET1.PROFICIENCYDIV ";
            $query .= "                         AND L2.PROFICIENCYCD = ET1.PROFICIENCYCD ";
            $query .= "                         AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "                         AND L2.GRADE = EL1.GRADE ";
            $query .= "                         AND L2.COURSECD = EL1.COURSECD ";
            $query .= "                         AND L2.MAJORCD = EL1.MAJORCD ";
            $query .= "                         AND L2.COURSECODE = EL1.COURSECODE ";
            $query .= "                         AND L2.PROFICIENCY_SUBCLASS_CD = ET1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "                 GROUP BY ";
            $query .= "                     ET1.PROFICIENCYDIV, ";
            $query .= "                     ET1.PROFICIENCYCD, ";
            $query .= "                     EL3.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                     EL3.SCHOOL_KIND, ";
                $query .= "                     EL3.CURRICULUM_CD, ";
                $query .= "                     EL3.SUBCLASSCD, ";
            }
            $query .= "                     ET1.SCHREGNO ";
            $query .= "                 ) E1 ";
            $query .= "             WHERE ";
            $query .= "                 E1.SCORE_DIS = 0 ";
            $query .= "             GROUP BY ";
            $query .= "                 E1.SCHREGNO ";
            if ($groupMst["GROUP_DIV"] != 9 && $model->Properties["notGroupCnt"] != "1") {
                $query .= "             HAVING ";
                $query .= "                 COUNT(*) >= ".$groupMst["GROUP_DIV"]." ";
            }
            $query .= "             ) EE1 ";
            $query .= "         WHERE ";
            $query .= "             EE1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "     ) ";
            $query .= " GROUP BY ";
            $query .= "     LT1.PROFICIENCYDIV, ";
            $query .= "     LT1.PROFICIENCYCD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL2.GROUP_CD, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECODE ";
        }
        if ($div == "GASSAN_KAMOKU") {
            $query .= " ), SCORE_DI_T AS  ( ";
            $query .= " SELECT ";
            $query .= "     E1.SCHREGNO, ";
            $query .= "     SUM(E1.SCORE_DIS) AS SCORE_DIS ";
            $query .= " FROM ";
            $query .= "    (SELECT ";
            $query .= "         ET1.PROFICIENCYDIV, ";
            $query .= "         ET1.PROFICIENCYCD, ";
            $query .= "                     EL3.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                     EL3.SCHOOL_KIND, ";
                $query .= "                     EL3.CURRICULUM_CD, ";
                $query .= "                     EL3.SUBCLASSCD, ";
            }
            $query .= "         SUM (CASE WHEN ET1.SCORE_DI = '*' ";
            $query .= "                   THEN 1 ";
            $query .= "                   ELSE 0 ";
            $query .= "              END) ";
            $query .= "         AS SCORE_DIS, ";
            $query .= "         ET1.SCHREGNO ";
            $query .= "     FROM ";
            $query .= "         PROFICIENCY_DAT2 ET1 ";
            $query .= "         INNER JOIN SCHREG_REGD_DAT EL1 ON EL1.YEAR = ET1.YEAR ";
            $query .= "             AND EL1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "             AND EL1.SCHREGNO = ET1.SCHREGNO ";
            $query .= "             AND EL1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "         LEFT JOIN PROFICIENCY_SUBCLASS_MST EL3 ON EL3.PROFICIENCY_SUBCLASS_CD = ET1.PROFICIENCY_SUBCLASS_CD, ";
            $query .= "         PROFICIENCY_SUBCLASS_GROUP_DAT L2 ";
            $query .= "     WHERE ";
            $query .= "         (ET1.SCORE IS NOT NULL OR (ET1.SCORE IS NULL AND ET1.SCORE_DI = '*')) ";
            $query .= "         AND L2.YEAR = ET1.YEAR ";
            $query .= "             AND L2.SEMESTER = ET1.SEMESTER ";
            $query .= "             AND L2.PROFICIENCYDIV = ET1.PROFICIENCYDIV ";
            $query .= "             AND L2.PROFICIENCYCD = ET1.PROFICIENCYCD ";
            $query .= "             AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "             AND L2.GRADE = EL1.GRADE ";
            $query .= "             AND L2.COURSECD = EL1.COURSECD ";
            $query .= "             AND L2.MAJORCD = EL1.MAJORCD ";
            $query .= "             AND L2.COURSECODE = EL1.COURSECODE ";
            $query .= "             AND L2.PROFICIENCY_SUBCLASS_CD = ET1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "     GROUP BY ";
            $query .= "         ET1.PROFICIENCYDIV, ";
            $query .= "         ET1.PROFICIENCYCD, ";
            $query .= "                     EL3.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                     EL3.SCHOOL_KIND, ";
                $query .= "                     EL3.CURRICULUM_CD, ";
                $query .= "                     EL3.SUBCLASSCD, ";
            }
            $query .= "         ET1.SCHREGNO ";
            $query .= "     ) E1 ";
            $query .= " GROUP BY ";
            $query .= "     E1.SCHREGNO ";
            if ($groupMst["GROUP_DIV"] != 9 && $model->Properties["notGroupCnt"] != "1") {
                $query .= " HAVING ";
                $query .= "     COUNT(*) >= ".$groupMst["GROUP_DIV"]." ";
            }
        }

        $query .= " ), MOCT_T_KANSAN AS  ( ";
        if ($div == "TUUJOU_KAMOKU") {
            $query .= " SELECT ";
            $query .= "     LT1.PROFICIENCYDIV, ";
            $query .= "     LT1.PROFICIENCYCD, ";
            $query .= "     '999999' AS PROFICIENCY_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL2.GROUP_CD, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE AS COURSE, ";
            $query .= "     SUM (CASE WHEN LT1.SCORE_DI = '*' ";
            $query .= "               THEN 1 ";
            $query .= "               ELSE 0 ";
            $query .= "          END) ";
            $query .= "     AS SCORE_DIS, ";
            if ($dataDiv == "2") {
                if ($model->z010 == 'meiji') {
                    $query .= "     SUM(CEIL(FLOAT(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1)*100,-2)/100) * 100 / VALUE(LL3.PERFECT, 100))) AS SCORE, ";
                    $query .= "     SUM(FLOAT(VALUE(LL3.PERFECT, 100))) AS PERFECT, ";
                    $query .= "     DECIMAL(ROUND(AVG(FLOAT(CEIL(FLOAT(LT1.SCORE) * 100 / VALUE(LL3.PERFECT, 100))))*100000,0)/100000,9,5) AS AVG "; // 換算点の平均
                } else {
                    $query .= "     SUM(CEIL(FLOAT(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1)*100,-2)/100) )) AS SCORE, ";
                    $query .= "     SUM(FLOAT(VALUE(LL3.PERFECT, 100) * VALUE(LL3.WEIGHTING, 1))) AS PERFECT, ";
                    $query .= "     DECIMAL(ROUND(FLOAT(SUM(CEIL(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1), 0))))/FLOAT(SUM(VALUE(LL3.PERFECT, 100) * VALUE(LL3.WEIGHTING, 1)))*100,6),9,5) AS AVG "; // 合計 / 満点合計
                }
            } else {
                $query .= "     SUM(CEIL(FLOAT(LT1.SCORE) * 100 / VALUE(LL3.PERFECT, 100))) AS SCORE, ";
                $query .= "     SUM(FLOAT(VALUE(LL3.PERFECT, 100))) AS PERFECT, ";
                $query .= "     DECIMAL(ROUND(AVG(FLOAT(CEIL(FLOAT(LT1.SCORE) * 100 / VALUE(LL3.PERFECT, 100))))*100000,0)/100000,9,5) AS AVG ";
            }
            $query .= " FROM ";
            $query .= "     PROFICIENCY_DAT2 LT1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
            $query .= "         AND LL1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "         AND LL1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT LL2 ON LL2.YEAR = LT1.YEAR ";
            $query .= "          AND LL2.GRADE = LL1.GRADE ";
            $query .= "          AND LL2.COURSECD || LL2.MAJORCD || LL2.COURSECODE = LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE ";
            $query .= "     LEFT JOIN PROFICIENCY_PERFECT_COURSE_DAT LL3 ON LL3.YEAR = LT1.YEAR ";
            $query .= "          AND LL3.SEMESTER = LT1.SEMESTER ";
            $query .= "          AND LL3.PROFICIENCYDIV = LT1.PROFICIENCYDIV ";
            $query .= "          AND LL3.PROFICIENCYCD = LT1.PROFICIENCYCD ";
            $query .= "          AND LL3.PROFICIENCY_SUBCLASS_CD = LT1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "          AND LL3.GRADE = CASE WHEN LL3.DIV = '01' ";
            $query .= "                           THEN '00' ";
            $query .= "                           ELSE LL1.GRADE ";
            $query .= "                      END ";
            $query .= "          AND LL3.COURSECD || LL3.MAJORCD || LL3.COURSECODE = CASE WHEN LL3.DIV IN ('01','02') ";
            $query .= "                                                       THEN '00000000' ";
            $query .= "                                                       ELSE CASE WHEN DIV = '04' ";
            $query .= "                                                                 THEN '0' || LL2.GROUP_CD || '0000' ";
            $query .= "                                                                 ELSE LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE ";
            $query .= "                                                            END ";
            $query .= "                                                  END ";
            $query .= " WHERE ";
            $query .= "     (LT1.SCORE IS NOT NULL OR (LT1.SCORE IS NULL AND LT1.SCORE_DI = '*')) ";
            $query .= " GROUP BY ";
            $query .= "     LT1.PROFICIENCYDIV, ";
            $query .= "     LT1.PROFICIENCYCD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL2.GROUP_CD, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECODE ";
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     LT1.PROFICIENCYDIV, ";
            $query .= "     LT1.PROFICIENCYCD, ";
            $query .= "     LT1.PROFICIENCY_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL2.GROUP_CD, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE AS COURSE, ";
            $query .= "     SUM (CASE WHEN LT1.SCORE_DI = '*' ";
            $query .= "               THEN 1 ";
            $query .= "               ELSE 0 ";
            $query .= "          END) ";
            $query .= "     AS SCORE_DIS, ";
            if ($dataDiv == "2") {
                if ($model->z010 == 'meiji') {
                    $query .= "     SUM(CEIL(FLOAT(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1)*100,-2)/100) * 100 / VALUE(LL3.PERFECT, 100))) AS SCORE, ";
                    $query .= "     SUM(FLOAT(VALUE(LL3.PERFECT, 100))) AS PERFECT, ";
                    $query .= "     DECIMAL(ROUND(AVG(FLOAT(CEIL(FLOAT(LT1.SCORE) * 100 / VALUE(LL3.PERFECT, 100))))*100000,0)/100000,9,5) AS AVG "; // 換算点の平均
                } else {
                    $query .= "     SUM(CEIL(FLOAT(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1)*100,-2)/100) )) AS SCORE, ";
                    $query .= "     SUM(FLOAT(VALUE(LL3.PERFECT, 100) * VALUE(LL3.WEIGHTING, 1))) AS PERFECT, ";
                    $query .= "     DECIMAL(ROUND(FLOAT(SUM(CEIL(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1), 0))))/FLOAT(SUM(VALUE(LL3.PERFECT, 100) * VALUE(LL3.WEIGHTING, 1)))*100,6),9,5) AS AVG "; // 合計 / 満点合計
                }
            } else {
                $query .= "     SUM(CEIL(FLOAT(LT1.SCORE) * 100 / VALUE(LL3.PERFECT, 100))) AS SCORE, ";
                $query .= "     SUM(FLOAT(VALUE(LL3.PERFECT, 100))) AS PERFECT, ";
                $query .= "     DECIMAL(ROUND(AVG(FLOAT(CEIL(FLOAT(LT1.SCORE) * 100 / VALUE(LL3.PERFECT, 100))))*100000,0)/100000,9,5) AS AVG ";
            }
            $query .= " FROM ";
            $query .= "     PROFICIENCY_DAT2 LT1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
            $query .= "         AND LL1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "         AND LL1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT LL2 ON LL2.YEAR = LT1.YEAR ";
            $query .= "          AND LL2.GRADE = LL1.GRADE ";
            $query .= "          AND LL2.COURSECD || LL2.MAJORCD || LL2.COURSECODE = LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE ";
            $query .= "     LEFT JOIN PROFICIENCY_PERFECT_COURSE_DAT LL3 ON LL3.YEAR = LT1.YEAR ";
            $query .= "          AND LL3.SEMESTER = LT1.SEMESTER ";
            $query .= "          AND LL3.PROFICIENCYDIV = LT1.PROFICIENCYDIV ";
            $query .= "          AND LL3.PROFICIENCYCD = LT1.PROFICIENCYCD ";
            $query .= "          AND LL3.PROFICIENCY_SUBCLASS_CD = LT1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "          AND LL3.GRADE = CASE WHEN LL3.DIV = '01' ";
            $query .= "                           THEN '00' ";
            $query .= "                           ELSE LL1.GRADE ";
            $query .= "                      END ";
            $query .= "          AND LL3.COURSECD || LL3.MAJORCD || LL3.COURSECODE = CASE WHEN LL3.DIV IN ('01','02') ";
            $query .= "                                                       THEN '00000000' ";
            $query .= "                                                       ELSE CASE WHEN DIV = '04' ";
            $query .= "                                                                 THEN '0' || LL2.GROUP_CD || '0000' ";
            $query .= "                                                                 ELSE LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE ";
            $query .= "                                                            END ";
            $query .= "                                                  END ";
            $query .= " WHERE ";
            $query .= "     LT1.SCORE IS NOT NULL ";
            $query .= " GROUP BY ";
            $query .= "     LT1.PROFICIENCYDIV, ";
            $query .= "     LT1.PROFICIENCYCD, ";
            $query .= "     LT1.PROFICIENCY_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL2.GROUP_CD, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECODE ";
        } else {
            $query .= " SELECT ";
            $query .= "     LT1.PROFICIENCYDIV, ";
            $query .= "     LT1.PROFICIENCYCD, ";
            $query .= "     '".$groupSubclass."' AS PROFICIENCY_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL2.GROUP_CD, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE AS COURSE, ";
            if ($dataDiv == "2") {
                if ($model->z010 == 'meiji') {
                    $query .= "     SUM(CEIL(FLOAT(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1)*100,-2)/100) * 100 / VALUE(LL3.PERFECT, 100))) AS SCORE, ";
                    $query .= "     SUM(FLOAT(VALUE(LL3.PERFECT, 100))) AS PERFECT, ";
                    $query .= "     DECIMAL(ROUND(AVG(FLOAT(CEIL(FLOAT(LT1.SCORE) * 100 / VALUE(LL3.PERFECT, 100))))*100000,0)/100000,9,5) AS AVG "; // 換算点の平均
                } else {
                    $query .= "     SUM(CEIL(FLOAT(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1)*100,-2)/100) )) AS SCORE, ";
                    $query .= "     SUM(FLOAT(VALUE(LL3.PERFECT, 100) * VALUE(LL3.WEIGHTING, 1))) AS PERFECT, ";
                    $query .= "     DECIMAL(ROUND(FLOAT(SUM(CEIL(ROUND(LT1.SCORE * VALUE(LL3.WEIGHTING, 1), 0))))/FLOAT(SUM(VALUE(LL3.PERFECT, 100) * VALUE(LL3.WEIGHTING, 1)))*100,6),9,5) AS AVG "; // 合計 / 満点合計
                }
            } else {
                $query .= "     SUM(CEIL(FLOAT(LT1.SCORE) * 100 / VALUE(LL3.PERFECT, 100))) AS SCORE, ";
                $query .= "     SUM(FLOAT(VALUE(LL3.PERFECT, 100))) AS PERFECT, ";
                $query .= "     DECIMAL(ROUND(AVG(FLOAT(CEIL(FLOAT(LT1.SCORE) * 100 / VALUE(LL3.PERFECT, 100))))*100000,0)/100000,9,5) AS AVG ";
            }
            $query .= " FROM ";
            $query .= "     PROFICIENCY_DAT2 LT1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
            $query .= "         AND LL1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "         AND LL1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT LL2 ON LL2.YEAR = LT1.YEAR ";
            $query .= "          AND LL2.GRADE = LL1.GRADE ";
            $query .= "          AND LL2.COURSECD || LL2.MAJORCD || LL2.COURSECODE = LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE ";
            $query .= "     LEFT JOIN PROFICIENCY_PERFECT_COURSE_DAT LL3 ON LL3.YEAR = LT1.YEAR ";
            $query .= "          AND LL3.SEMESTER = LT1.SEMESTER ";
            $query .= "          AND LL3.PROFICIENCYDIV = LT1.PROFICIENCYDIV ";
            $query .= "          AND LL3.PROFICIENCYCD = LT1.PROFICIENCYCD ";
            $query .= "          AND LL3.PROFICIENCY_SUBCLASS_CD = LT1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "          AND LL3.GRADE = CASE WHEN LL3.DIV = '01' ";
            $query .= "                           THEN '00' ";
            $query .= "                           ELSE LL1.GRADE ";
            $query .= "                      END ";
            $query .= "          AND LL3.COURSECD || LL3.MAJORCD || LL3.COURSECODE = CASE WHEN LL3.DIV IN ('01','02') ";
            $query .= "                                                       THEN '00000000' ";
            $query .= "                                                       ELSE CASE WHEN DIV = '04' ";
            $query .= "                                                                 THEN '0' || LL2.GROUP_CD || '0000' ";
            $query .= "                                                                 ELSE LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE ";
            $query .= "                                                            END ";
            $query .= "                                                  END, ";
            $query .= "     PROFICIENCY_SUBCLASS_GROUP_DAT L2 ";
            $query .= " WHERE ";
            $query .= "     LT1.SCORE IS NOT NULL ";
            $query .= "     AND L2.YEAR = LT1.YEAR ";
            $query .= "         AND L2.SEMESTER = LT1.SEMESTER ";
            $query .= "         AND L2.PROFICIENCYDIV = LT1.PROFICIENCYDIV ";
            $query .= "         AND L2.PROFICIENCYCD = LT1.PROFICIENCYCD ";
            $query .= "         AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "         AND L2.GRADE = LL1.GRADE ";
            $query .= "         AND L2.COURSECD = LL1.COURSECD ";
            $query .= "         AND L2.MAJORCD = LL1.MAJORCD ";
            $query .= "         AND L2.COURSECODE = LL1.COURSECODE ";
            $query .= "         AND L2.PROFICIENCY_SUBCLASS_CD = LT1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "     AND EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             SCORE_DI_T EE1 ";
            $query .= "         WHERE ";
            $query .= "             EE1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "     ) ";
            $query .= " GROUP BY ";
            $query .= "     LT1.PROFICIENCYDIV, ";
            $query .= "     LT1.PROFICIENCYCD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL2.GROUP_CD, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECODE ";
        }

        $query .= " ), T_AVERAGE AS  ( ";

        $query .= knjh539aQuery::getTavg("01", $div);

        $query .= " UNION ";
        $query .= knjh539aQuery::getTavg("02", $div);

        $query .= " UNION ";
        $query .= knjh539aQuery::getTavg("03", $div);

        $query .= " UNION ";
        $query .= knjh539aQuery::getTavg("04", $div);

        $query .= " UNION ";
        $query .= knjh539aQuery::getTavg("05", $div);


        $query .= " ), T_AVERAGE_KANSAN AS  ( ";

        $query .= knjh539aQuery::getTavgKansan("01");

        $query .= " UNION ";
        $query .= knjh539aQuery::getTavgKansan("02");

        $query .= " UNION ";
        $query .= knjh539aQuery::getTavgKansan("03");

        $query .= " UNION ";
        $query .= knjh539aQuery::getTavgKansan("04");

        $query .= " UNION ";
        $query .= knjh539aQuery::getTavgKansan("05");

        $query .= " ) ";

        if ($div == "TUUJOU_KAMOKU") {
            $query .= " SELECT ";
            $query .= "     W1.PROFICIENCYDIV, ";
            $query .= "     W1.PROFICIENCYCD, ";
            $query .= "     W1.SCHREGNO, ";
            $query .= "     W1.PROFICIENCY_SUBCLASS_CD, ";
            $query .= "     W1.GRADE, ";
            $query .= "     W1.SCORE_DIS, ";
            $query .= "     W1.SCORE, ";
            $query .= "     W1.AVG, ";
            $query .= "     W1.AVG2, ";
            $query .= "     L1.SCORE AS SCORE_KANSAN, ";
            $query .= "     L1.AVG AS AVG_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.SCORE, -1) END DESC) AS GRADE_SCORE_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.SCORE, -1) END DESC) AS CLASS_SCORE_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.SCORE, -1) END DESC) AS COURSE_SCORE_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.MAJORCD ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.SCORE, -1) END DESC) AS MAJORCD_SCORE_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.GROUP_CD ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.SCORE, -1) END DESC) AS GROUP_CD_SCORE_RANK, ";

            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.AVG, -1) END DESC) AS GRADE_AVG_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.AVG, -1) END DESC) AS CLASS_AVG_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.AVG, -1) END DESC) AS COURSE_AVG_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.MAJORCD ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.AVG, -1) END DESC) AS MAJORCD_AVG_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.GROUP_CD ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.AVG, -1) END DESC) AS GROUP_CD_AVG_RANK, ";

            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE ORDER BY case when 0 < W2.STDDEV then DECIMAL(ROUND((10*(W1.SCORE - ROUND(FLOAT(W2.AVG) * 10, 0) / 10)/W2.STDDEV+50)*10,0)/10,5,1) end DESC) AS GRADE_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY case when 0 < W3.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W3.AVG) * 10, 0) / 10)/W3.STDDEV+50)*10,0)/10,5,1) end DESC) AS CLASS_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY case when 0 < W4.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W4.AVG) * 10, 0) / 10)/W4.STDDEV+50)*10,0)/10,5,1) end DESC) AS COURSE_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.MAJORCD ORDER BY case when 0 < W5.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W5.AVG) * 10, 0) / 10)/W5.STDDEV+50)*10,0)/10,5,1) end DESC) AS MAJORCD_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.GROUP_CD ORDER BY case when 0 < W6.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W6.AVG) * 10, 0) / 10)/W6.STDDEV+50)*10,0)/10,5,1) end DESC) AS GROUP_CD_DEVIATION_RANK, ";

            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.AVG2, -1) END DESC) AS GRADE_AVG_RANK2, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.AVG2, -1) END DESC) AS CLASS_AVG_RANK2, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.AVG2, -1) END DESC) AS COURSE_AVG_RANK2, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.MAJORCD ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.AVG2, -1) END DESC) AS MAJORCD_AVG_RANK2, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.GROUP_CD ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.AVG2, -1) END DESC) AS GROUP_CD_AVG_RANK2, ";

            $query .= "     case when 0 < W2.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W2.AVG) * 10, 0) / 10)/W2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION, ";
            $query .= "     case when 0 < W3.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W3.AVG) * 10, 0) / 10)/W3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION, ";
            $query .= "     case when 0 < W4.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W4.AVG) * 10, 0) / 10)/W4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION, ";
            $query .= "     case when 0 < W5.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W5.AVG) * 10, 0) / 10)/W5.STDDEV+50)*10,0)/10,5,1) end AS MAJORCD_DEVIATION, ";
            $query .= "     case when 0 < W6.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W6.AVG) * 10, 0) / 10)/W6.STDDEV+50)*10,0)/10,5,1) end AS GROUP_CD_DEVIATION, ";

            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE ORDER BY case when 0 < K2.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K2.AVG) * 10, 0) / 10)/K2.STDDEV+50)*10,0)/10,5,1) end DESC) AS GRADE_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY case when 0 < K3.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K3.AVG) * 10, 0) / 10)/K3.STDDEV+50)*10,0)/10,5,1) end DESC) AS CLASS_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY case when 0 < K4.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K4.AVG) * 10, 0) / 10)/K4.STDDEV+50)*10,0)/10,5,1) end DESC) AS COURSE_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.MAJORCD ORDER BY case when 0 < K5.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K5.AVG) * 10, 0) / 10)/K5.STDDEV+50)*10,0)/10,5,1) end DESC) AS MAJORCD_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.GROUP_CD ORDER BY case when 0 < K6.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K6.AVG) * 10, 0) / 10)/K6.STDDEV+50)*10,0)/10,5,1) end DESC) AS GROUP_CD_DEVIATION_RANK_KANSAN, ";

            $query .= "     case when 0 < K2.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K2.AVG) * 10, 0) / 10)/K2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION_KANSAN, ";
            $query .= "     case when 0 < K3.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K3.AVG) * 10, 0) / 10)/K3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION_KANSAN, ";
            $query .= "     case when 0 < K4.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K4.AVG) * 10, 0) / 10)/K4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION_KANSAN, ";
            $query .= "     case when 0 < K5.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K5.AVG) * 10, 0) / 10)/K5.STDDEV+50)*10,0)/10,5,1) end AS MAJORCD_DEVIATION_KANSAN, ";
            $query .= "     case when 0 < K6.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K6.AVG) * 10, 0) / 10)/K6.STDDEV+50)*10,0)/10,5,1) end AS GROUP_CD_DEVIATION_KANSAN ";
            $query .= " FROM ";
            $query .= "     MOCT_T W1 ";
            $query .= "     LEFT JOIN MOCT_T_KANSAN L1 ON W1.PROFICIENCYDIV = L1.PROFICIENCYDIV ";
            $query .= "          AND W1.PROFICIENCYCD = L1.PROFICIENCYCD ";
            $query .= "          AND W1.PROFICIENCY_SUBCLASS_CD = L1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "          AND W1.SCHREGNO = L1.SCHREGNO ";
            $query .= "          AND W1.GRADE = L1.GRADE ";
            $query .= "          AND W1.HR_CLASS = L1.HR_CLASS ";
            $query .= "          AND W1.COURSE = L1.COURSE ";
            $query .= "     LEFT JOIN T_AVERAGE W2 ON W2.AVG_DIV = '01' AND W2.PROFICIENCYDIV = W1.PROFICIENCYDIV AND W2.PROFICIENCYCD = W1.PROFICIENCYCD AND W2.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND W2.GRADE = W1.GRADE ";
            $query .= "     LEFT JOIN T_AVERAGE W3 ON W3.AVG_DIV = '02' AND W3.PROFICIENCYDIV = W1.PROFICIENCYDIV AND W3.PROFICIENCYCD = W1.PROFICIENCYCD AND W3.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND W3.GRADE = W1.GRADE AND W3.HR_CLASS = W1.HR_CLASS ";
            $query .= "     LEFT JOIN T_AVERAGE W4 ON W4.AVG_DIV = '03' AND W4.PROFICIENCYDIV = W1.PROFICIENCYDIV AND W4.PROFICIENCYCD = W1.PROFICIENCYCD AND W4.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND W4.GRADE = W1.GRADE AND W4.COURSE = W1.COURSE ";
            $query .= "     LEFT JOIN T_AVERAGE W5 ON W5.AVG_DIV = '04' AND W5.PROFICIENCYDIV = W1.PROFICIENCYDIV AND W5.PROFICIENCYCD = W1.PROFICIENCYCD AND W5.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND W5.GRADE = W1.GRADE AND W5.COURSE = W1.COURSECD || W1.MAJORCD || '0000' ";
            $query .= "     LEFT JOIN T_AVERAGE W6 ON W6.AVG_DIV = '05' AND W6.PROFICIENCYDIV = W1.PROFICIENCYDIV AND W6.PROFICIENCYCD = W1.PROFICIENCYCD AND W6.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND W6.GRADE = W1.GRADE AND W6.COURSE = '0' || W1.GROUP_CD || '0000' ";
            $query .= "     LEFT JOIN T_AVERAGE_KANSAN K2 ON K2.AVG_DIV = '01' AND K2.PROFICIENCYDIV = W1.PROFICIENCYDIV AND K2.PROFICIENCYCD = W1.PROFICIENCYCD AND K2.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND K2.GRADE = W1.GRADE ";
            $query .= "     LEFT JOIN T_AVERAGE_KANSAN K3 ON K3.AVG_DIV = '02' AND K3.PROFICIENCYDIV = W1.PROFICIENCYDIV AND K3.PROFICIENCYCD = W1.PROFICIENCYCD AND K3.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND K3.GRADE = W1.GRADE AND K3.HR_CLASS = W1.HR_CLASS ";
            $query .= "     LEFT JOIN T_AVERAGE_KANSAN K4 ON K4.AVG_DIV = '03' AND K4.PROFICIENCYDIV = W1.PROFICIENCYDIV AND K4.PROFICIENCYCD = W1.PROFICIENCYCD AND K4.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND K4.GRADE = W1.GRADE AND K4.COURSE = W1.COURSE ";
            $query .= "     LEFT JOIN T_AVERAGE_KANSAN K5 ON K5.AVG_DIV = '04' AND K5.PROFICIENCYDIV = W1.PROFICIENCYDIV AND K5.PROFICIENCYCD = W1.PROFICIENCYCD AND K5.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND K5.GRADE = W1.GRADE AND K5.COURSE = W1.COURSECD || W1.MAJORCD || '0000' ";
            $query .= "     LEFT JOIN T_AVERAGE_KANSAN K6 ON K6.AVG_DIV = '05' AND K6.PROFICIENCYDIV = W1.PROFICIENCYDIV AND K6.PROFICIENCYCD = W1.PROFICIENCYCD AND K6.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND K6.GRADE = W1.GRADE AND K6.COURSE = '0' || W1.GROUP_CD || '0000' ";
        } else {
            $query .= " , RANK_T AS  ( ";
            $query .= " SELECT ";
            $query .= "     W1.SCHREGNO, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE ORDER BY VALUE(W1.SCORE, -1) DESC) AS GRADE_SCORE_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY VALUE(W1.SCORE, -1) DESC) AS CLASS_SCORE_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY VALUE(W1.SCORE, -1) DESC) AS COURSE_SCORE_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.MAJORCD ORDER BY VALUE(W1.SCORE, -1) DESC) AS MAJORCD_SCORE_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.GROUP_CD ORDER BY VALUE(W1.SCORE, -1) DESC) AS GROUP_CD_SCORE_RANK, ";

            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE ORDER BY VALUE(W1.AVG, -1) DESC) AS GRADE_AVG_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY VALUE(W1.AVG, -1) DESC) AS CLASS_AVG_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY VALUE(W1.AVG, -1) DESC) AS COURSE_AVG_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.MAJORCD ORDER BY VALUE(W1.AVG, -1) DESC) AS MAJORCD_AVG_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.GROUP_CD ORDER BY VALUE(W1.AVG, -1) DESC) AS GROUP_CD_AVG_RANK, ";

            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE ORDER BY case when 0 < W2.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W2.AVG) * 10, 0) / 10)/W2.STDDEV+50)*10,0)/10,5,1) end DESC) AS GRADE_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY case when 0 < W3.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W3.AVG) * 10, 0) / 10)/W3.STDDEV+50)*10,0)/10,5,1) end DESC) AS CLASS_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY case when 0 < W4.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W4.AVG) * 10, 0) / 10)/W4.STDDEV+50)*10,0)/10,5,1) end DESC) AS COURSE_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.MAJORCD ORDER BY case when 0 < W5.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W5.AVG) * 10, 0) / 10)/W5.STDDEV+50)*10,0)/10,5,1) end DESC) AS MAJORCD_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.GROUP_CD ORDER BY case when 0 < W6.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W6.AVG) * 10, 0) / 10)/W6.STDDEV+50)*10,0)/10,5,1) end DESC) AS GROUP_CD_DEVIATION_RANK, ";

            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE ORDER BY VALUE(W1.AVG2, -1) DESC) AS GRADE_AVG_RANK2, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY VALUE(W1.AVG2, -1) DESC) AS CLASS_AVG_RANK2, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY VALUE(W1.AVG2, -1) DESC) AS COURSE_AVG_RANK2, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.MAJORCD ORDER BY VALUE(W1.AVG2, -1) DESC) AS MAJORCD_AVG_RANK2, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.GROUP_CD ORDER BY VALUE(W1.AVG2, -1) DESC) AS GROUP_CD_AVG_RANK2, ";

            $query .= "     case when 0 < W2.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W2.AVG) * 10, 0) / 10)/W2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION, ";
            $query .= "     case when 0 < W3.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W3.AVG) * 10, 0) / 10)/W3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION, ";
            $query .= "     case when 0 < W4.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W4.AVG) * 10, 0) / 10)/W4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION, ";
            $query .= "     case when 0 < W5.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W5.AVG) * 10, 0) / 10)/W5.STDDEV+50)*10,0)/10,5,1) end AS MAJORCD_DEVIATION, ";
            $query .= "     case when 0 < W6.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W6.AVG) * 10, 0) / 10)/W6.STDDEV+50)*10,0)/10,5,1) end AS GROUP_CD_DEVIATION, ";

            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE ORDER BY case when 0 < K2.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K2.AVG) * 10, 0) / 10)/K2.STDDEV+50)*10,0)/10,5,1) end DESC) AS GRADE_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY case when 0 < K3.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K3.AVG) * 10, 0) / 10)/K3.STDDEV+50)*10,0)/10,5,1) end DESC) AS CLASS_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY case when 0 < K4.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K4.AVG) * 10, 0) / 10)/K4.STDDEV+50)*10,0)/10,5,1) end DESC) AS COURSE_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.MAJORCD ORDER BY case when 0 < K5.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K5.AVG) * 10, 0) / 10)/K5.STDDEV+50)*10,0)/10,5,1) end DESC) AS MAJORCD_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.GROUP_CD ORDER BY case when 0 < K3.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K6.AVG) * 10, 0) / 10)/K6.STDDEV+50)*10,0)/10,5,1) end DESC) AS GROUP_CD_DEVIATION_RANK_KANSAN, ";

            $query .= "     case when 0 < K2.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K2.AVG) * 10, 0) / 10)/K2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION_KANSAN, ";
            $query .= "     case when 0 < K3.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K3.AVG) * 10, 0) / 10)/K3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION_KANSAN, ";
            $query .= "     case when 0 < K4.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K4.AVG) * 10, 0) / 10)/K4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION_KANSAN, ";
            $query .= "     case when 0 < K5.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K5.AVG) * 10, 0) / 10)/K5.STDDEV+50)*10,0)/10,5,1) end AS MAJORCD_DEVIATION_KANSAN, ";
            $query .= "     case when 0 < K6.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K6.AVG) * 10, 0) / 10)/K6.STDDEV+50)*10,0)/10,5,1) end AS GROUP_CD_DEVIATION_KANSAN ";
            $query .= " FROM ";
            $query .= "     MOCT_T W1 ";
            $query .= "     LEFT JOIN MOCT_T_KANSAN L1 ON W1.PROFICIENCYDIV = L1.PROFICIENCYDIV ";
            $query .= "          AND W1.PROFICIENCYCD = L1.PROFICIENCYCD ";
            $query .= "          AND W1.PROFICIENCY_SUBCLASS_CD = L1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "          AND W1.SCHREGNO = L1.SCHREGNO ";
            $query .= "          AND W1.GRADE = L1.GRADE ";
            $query .= "          AND W1.HR_CLASS = L1.HR_CLASS ";
            $query .= "          AND W1.COURSE = L1.COURSE ";
            $query .= "     LEFT JOIN T_AVERAGE W2 ON W2.AVG_DIV = '01' AND W2.PROFICIENCYDIV = W1.PROFICIENCYDIV AND W2.PROFICIENCYCD = W1.PROFICIENCYCD AND W2.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND W2.GRADE = W1.GRADE ";
            $query .= "     LEFT JOIN T_AVERAGE W3 ON W3.AVG_DIV = '02' AND W3.PROFICIENCYDIV = W1.PROFICIENCYDIV AND W3.PROFICIENCYCD = W1.PROFICIENCYCD AND W3.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND W3.GRADE = W1.GRADE AND W3.HR_CLASS = W1.HR_CLASS ";
            $query .= "     LEFT JOIN T_AVERAGE W4 ON W4.AVG_DIV = '03' AND W4.PROFICIENCYDIV = W1.PROFICIENCYDIV AND W4.PROFICIENCYCD = W1.PROFICIENCYCD AND W4.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND W4.GRADE = W1.GRADE AND W4.COURSE = W1.COURSE ";
            $query .= "     LEFT JOIN T_AVERAGE W5 ON W5.AVG_DIV = '04' AND W5.PROFICIENCYDIV = W1.PROFICIENCYDIV AND W5.PROFICIENCYCD = W1.PROFICIENCYCD AND W5.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND W5.GRADE = W1.GRADE AND W5.COURSE = W1.COURSECD || W1.MAJORCD || '0000' ";
            $query .= "     LEFT JOIN T_AVERAGE W6 ON W6.AVG_DIV = '05' AND W6.PROFICIENCYDIV = W1.PROFICIENCYDIV AND W6.PROFICIENCYCD = W1.PROFICIENCYCD AND W6.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND W6.GRADE = W1.GRADE AND W6.COURSE = '0' || W1.GROUP_CD || '0000' ";
            $query .= "     LEFT JOIN T_AVERAGE_KANSAN K2 ON K2.AVG_DIV = '01' AND K2.PROFICIENCYDIV = W1.PROFICIENCYDIV AND K2.PROFICIENCYCD = W1.PROFICIENCYCD AND K2.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND K2.GRADE = W1.GRADE ";
            $query .= "     LEFT JOIN T_AVERAGE_KANSAN K3 ON K3.AVG_DIV = '02' AND K3.PROFICIENCYDIV = W1.PROFICIENCYDIV AND K3.PROFICIENCYCD = W1.PROFICIENCYCD AND K3.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND K3.GRADE = W1.GRADE AND K3.HR_CLASS = W1.HR_CLASS ";
            $query .= "     LEFT JOIN T_AVERAGE_KANSAN K4 ON K4.AVG_DIV = '03' AND K4.PROFICIENCYDIV = W1.PROFICIENCYDIV AND K4.PROFICIENCYCD = W1.PROFICIENCYCD AND K4.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND K4.GRADE = W1.GRADE AND K4.COURSE = W1.COURSE ";
            $query .= "     LEFT JOIN T_AVERAGE_KANSAN K5 ON K5.AVG_DIV = '04' AND K5.PROFICIENCYDIV = W1.PROFICIENCYDIV AND K5.PROFICIENCYCD = W1.PROFICIENCYCD AND K5.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND K5.GRADE = W1.GRADE AND K5.COURSE = W1.COURSECD || W1.MAJORCD || '0000' ";
            $query .= "     LEFT JOIN T_AVERAGE_KANSAN K6 ON K6.AVG_DIV = '05' AND K6.PROFICIENCYDIV = W1.PROFICIENCYDIV AND K6.PROFICIENCYCD = W1.PROFICIENCYCD AND K6.PROFICIENCY_SUBCLASS_CD = W1.PROFICIENCY_SUBCLASS_CD AND K6.GRADE = W1.GRADE AND K6.COURSE = '0' || W1.GROUP_CD || '0000' ";
            $query .= " WHERE ";
            $query .= "     EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             SCORE_DI_T EE1 ";
            $query .= "         WHERE ";
            $query .= "             EE1.SCHREGNO = W1.SCHREGNO ";
            $query .= "             AND EE1.SCORE_DIS = 0 ";
            $query .= "     ) ";

            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     W1.PROFICIENCYDIV, ";
            $query .= "     W1.PROFICIENCYCD, ";
            $query .= "     W1.SCHREGNO, ";
            $query .= "     W1.PROFICIENCY_SUBCLASS_CD, ";
            $query .= "     W1.GRADE, ";
            $query .= "     W1.SCORE, ";
            $query .= "     W1.AVG, ";
            $query .= "     W1.AVG2, ";
            $query .= "     L1.SCORE AS SCORE_KANSAN, ";
            $query .= "     L1.AVG AS AVG_KANSAN, ";

            $query .= "     RANK_T.GRADE_SCORE_RANK, ";
            $query .= "     RANK_T.CLASS_SCORE_RANK, ";
            $query .= "     RANK_T.COURSE_SCORE_RANK, ";
            $query .= "     RANK_T.MAJORCD_SCORE_RANK, ";
            $query .= "     RANK_T.GROUP_CD_SCORE_RANK, ";

            $query .= "     RANK_T.GRADE_AVG_RANK, ";
            $query .= "     RANK_T.CLASS_AVG_RANK, ";
            $query .= "     RANK_T.COURSE_AVG_RANK, ";
            $query .= "     RANK_T.MAJORCD_AVG_RANK, ";
            $query .= "     RANK_T.GROUP_CD_AVG_RANK, ";

            $query .= "     RANK_T.GRADE_DEVIATION_RANK, ";
            $query .= "     RANK_T.CLASS_DEVIATION_RANK, ";
            $query .= "     RANK_T.COURSE_DEVIATION_RANK, ";
            $query .= "     RANK_T.MAJORCD_DEVIATION_RANK, ";
            $query .= "     RANK_T.GROUP_CD_DEVIATION_RANK, ";

            $query .= "     RANK_T.GRADE_AVG_RANK2, ";
            $query .= "     RANK_T.CLASS_AVG_RANK2, ";
            $query .= "     RANK_T.COURSE_AVG_RANK2, ";
            $query .= "     RANK_T.MAJORCD_AVG_RANK2, ";
            $query .= "     RANK_T.GROUP_CD_AVG_RANK2, ";

            $query .= "     RANK_T.GRADE_DEVIATION, ";
            $query .= "     RANK_T.CLASS_DEVIATION, ";
            $query .= "     RANK_T.COURSE_DEVIATION, ";
            $query .= "     RANK_T.MAJORCD_DEVIATION, ";
            $query .= "     RANK_T.GROUP_CD_DEVIATION, ";

            $query .= "     RANK_T.GRADE_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK_T.CLASS_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK_T.COURSE_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK_T.MAJORCD_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK_T.GROUP_CD_DEVIATION_RANK_KANSAN, ";

            $query .= "     RANK_T.GRADE_DEVIATION_KANSAN, ";
            $query .= "     RANK_T.CLASS_DEVIATION_KANSAN, ";
            $query .= "     RANK_T.COURSE_DEVIATION_KANSAN, ";
            $query .= "     RANK_T.MAJORCD_DEVIATION_KANSAN, ";
            $query .= "     RANK_T.GROUP_CD_DEVIATION_KANSAN ";
            $query .= " FROM ";
            $query .= "     MOCT_T W1 ";
            $query .= "     LEFT JOIN RANK_T ON W1.SCHREGNO = RANK_T.SCHREGNO ";
            $query .= "     LEFT JOIN MOCT_T_KANSAN L1 ON W1.PROFICIENCYDIV = L1.PROFICIENCYDIV ";
            $query .= "          AND W1.PROFICIENCYCD = L1.PROFICIENCYCD ";
            $query .= "          AND W1.PROFICIENCY_SUBCLASS_CD = L1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "          AND W1.SCHREGNO = L1.SCHREGNO ";
            $query .= "          AND W1.GRADE = L1.GRADE ";
            $query .= "          AND W1.HR_CLASS = L1.HR_CLASS ";
            $query .= "          AND W1.COURSE = L1.COURSE ";

        }
        $query .= " ORDER BY ";
        $query .= "     W1.GRADE, ";
        $query .= "     W1.PROFICIENCYDIV, ";
        $query .= "     W1.PROFICIENCYCD, ";
        $query .= "     W1.PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     W1.SCORE DESC ";

        return $query;
    }

    function getProficiencyRankDatDeviation($model, $groupMst, $groupSubclass, $div, $rankDiv, $rankName) {

        $query  = " WITH PROFICIENCY_DAT2 AS (";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     PROFICIENCYDIV, ";
        $query .= "     PROFICIENCYCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     CASE WHEN SCORE_DI = '*' ";
        $query .= "          THEN NULL ";
        $query .= "          ELSE SCORE ";
        $query .= "     END AS SCORE, ";
        $query .= "     SCORE_DI ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "     AND PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= " ), MOCT_T AS (";
        if ($div == "TUUJOU_KAMOKU") {
            $query .= " SELECT ";
            $query .= "     LT1.PROFICIENCYDIV, ";
            $query .= "     LT1.PROFICIENCYCD, ";
            $query .= "     '999999' AS PROFICIENCY_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL3.GROUP_CD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE AS COURSE, ";
            $query .= "     SUM (CASE WHEN LL2.SCORE_DI = '*' ";
            $query .= "               THEN 1 ";
            $query .= "               ELSE 0 ";
            $query .= "          END) ";
            $query .= "     AS SCORE_DIS, ";
            $query .= "     SUM(LT1.SCORE) AS SCORE, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.AVG))*100000,0)/100000,9,5) AS AVG, ";
            $query .= "     DECIMAL(ROUND(((FLOAT(SUM(LT1.DEVIATION))/FLOAT(SUM(100)))*100)*10,0)/10,5,1) AS ".$rankName."_AVG ";
            $query .= " FROM ";
            $query .= "     PROFICIENCY_RANK_DAT LT1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
            $query .= "         AND LL1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "         AND LL1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT LL3 ON LL3.YEAR = LT1.YEAR ";
            $query .= "          AND LL3.GRADE = LL1.GRADE ";
            $query .= "          AND LL3.COURSECD || LL3.MAJORCD || LL3.COURSECODE = LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE ";
            $query .= "     INNER JOIN PROFICIENCY_DAT2 LL2 ON LL2.YEAR = LT1.YEAR ";
            $query .= "         AND LL2.SEMESTER = LT1.SEMESTER ";
            $query .= "         AND LL2.PROFICIENCYDIV = LT1.PROFICIENCYDIV ";
            $query .= "         AND LL2.PROFICIENCYCD = LT1.PROFICIENCYCD ";
            $query .= "         AND LL2.SCHREGNO = LT1.SCHREGNO ";
            $query .= "         AND LL2.PROFICIENCY_SUBCLASS_CD = LT1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "     LEFT JOIN PROFICIENCY_PERFECT_COURSE_DAT LL4 ON LL4.YEAR = LT1.YEAR ";
            $query .= "          AND LL4.SEMESTER = LT1.SEMESTER ";
            $query .= "          AND LL4.PROFICIENCYDIV = LT1.PROFICIENCYDIV ";
            $query .= "          AND LL4.PROFICIENCYCD = LT1.PROFICIENCYCD ";
            $query .= "          AND LL4.PROFICIENCY_SUBCLASS_CD = LT1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "          AND LL4.GRADE = CASE WHEN LL4.DIV = '01' ";
            $query .= "                           THEN '00' ";
            $query .= "                           ELSE LL1.GRADE ";
            $query .= "                      END ";
            $query .= "          AND LL4.COURSECD || LL4.MAJORCD || LL4.COURSECODE = CASE WHEN LL4.DIV IN ('01','02') ";
            $query .= "                                                       THEN '00000000' ";
            $query .= "                                                       ELSE CASE WHEN DIV = '04' ";
            $query .= "                                                                 THEN '0' || LL3.GROUP_CD || '0000' ";
            $query .= "                                                                 ELSE LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE ";
            $query .= "                                                            END ";
            $query .= "                                                  END ";
            $query .= " WHERE ";
            $query .= "     LT1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND LT1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "     AND LT1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
            $query .= "     AND LT1.PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."' ";
            $query .= "     AND LT1.RANK_DATA_DIV = '03' ";
            $query .= "     AND LT1.RANK_DIV = '".$rankDiv."' ";
            $query .= "     AND (LL2.SCORE IS NOT NULL OR (LL2.SCORE IS NULL AND LL2.SCORE_DI = '*')) ";
            $query .= " GROUP BY ";
            $query .= "     LT1.PROFICIENCYDIV, ";
            $query .= "     LT1.PROFICIENCYCD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL3.GROUP_CD, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECODE ";
        } else {
            $query .= " SELECT ";
            $query .= "     LT1.PROFICIENCYDIV, ";
            $query .= "     LT1.PROFICIENCYCD, ";
            $query .= "     '".$groupSubclass."' AS PROFICIENCY_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL3.GROUP_CD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE AS COURSE, ";
            $query .= "     SUM(LT1.SCORE) AS SCORE, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.AVG))*100000,0)/100000,9,5) AS AVG, ";
            $query .= "     DECIMAL(ROUND(((FLOAT(SUM(LT1.DEVIATION))/FLOAT(SUM(100)))*100)*10,0)/10,5,1) AS ".$rankName."_AVG ";
            $query .= " FROM ";
            $query .= "     PROFICIENCY_RANK_DAT LT1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
            $query .= "         AND LL1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "         AND LL1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT LL3 ON LL3.YEAR = LT1.YEAR ";
            $query .= "          AND LL3.GRADE = LL1.GRADE ";
            $query .= "          AND LL3.COURSECD || LL3.MAJORCD || LL3.COURSECODE = LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE ";
            $query .= "     INNER JOIN PROFICIENCY_DAT2 LL2 ON LL2.YEAR = LT1.YEAR ";
            $query .= "         AND LL2.SEMESTER = LT1.SEMESTER ";
            $query .= "         AND LL2.PROFICIENCYDIV = LT1.PROFICIENCYDIV ";
            $query .= "         AND LL2.PROFICIENCYCD = LT1.PROFICIENCYCD ";
            $query .= "         AND LL2.SCHREGNO = LT1.SCHREGNO ";
            $query .= "         AND LL2.PROFICIENCY_SUBCLASS_CD = LT1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "     LEFT JOIN PROFICIENCY_PERFECT_COURSE_DAT LL4 ON LL4.YEAR = LT1.YEAR ";
            $query .= "          AND LL4.SEMESTER = LT1.SEMESTER ";
            $query .= "          AND LL4.PROFICIENCYDIV = LT1.PROFICIENCYDIV ";
            $query .= "          AND LL4.PROFICIENCYCD = LT1.PROFICIENCYCD ";
            $query .= "          AND LL4.PROFICIENCY_SUBCLASS_CD = LT1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "          AND LL4.GRADE = CASE WHEN LL4.DIV = '01' ";
            $query .= "                           THEN '00' ";
            $query .= "                           ELSE LL1.GRADE ";
            $query .= "                      END ";
            $query .= "          AND LL4.COURSECD || LL4.MAJORCD || LL4.COURSECODE = CASE WHEN LL4.DIV IN ('01','02') ";
            $query .= "                                                       THEN '00000000' ";
            $query .= "                                                       ELSE CASE WHEN DIV = '04' ";
            $query .= "                                                                 THEN '0' || LL3.GROUP_CD || '0000' ";
            $query .= "                                                                 ELSE LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE ";
            $query .= "                                                            END ";
            $query .= "                                                  END ";
            $query .= "    ,PROFICIENCY_SUBCLASS_GROUP_DAT L2 ";
            $query .= " WHERE ";
            $query .= "     LT1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND LT1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "     AND LT1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
            $query .= "     AND LT1.PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."' ";
            $query .= "     AND LT1.RANK_DATA_DIV = '03' ";
            $query .= "     AND LT1.RANK_DIV = '".$rankDiv."' ";
            $query .= "     AND L2.YEAR = LT1.YEAR ";
            $query .= "         AND L2.SEMESTER = LT1.SEMESTER ";
            $query .= "         AND L2.PROFICIENCYDIV = LT1.PROFICIENCYDIV ";
            $query .= "         AND L2.PROFICIENCYCD = LT1.PROFICIENCYCD ";
            $query .= "         AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "         AND L2.GRADE = LL1.GRADE ";
            $query .= "         AND L2.COURSECD = LL1.COURSECD ";
            $query .= "         AND L2.MAJORCD = LL1.MAJORCD ";
            $query .= "         AND L2.COURSECODE = LL1.COURSECODE ";
            $query .= "         AND L2.PROFICIENCY_SUBCLASS_CD = LT1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "     AND EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "            (SELECT ";
            $query .= "                 E1.SCHREGNO ";
            $query .= "             FROM ";
            $query .= "                (SELECT ";
            $query .= "                     ET1.PROFICIENCYDIV, ";
            $query .= "                     ET1.PROFICIENCYCD, ";
            $query .= "                     EL3.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                     EL3.SCHOOL_KIND, ";
                $query .= "                     EL3.CURRICULUM_CD, ";
                $query .= "                     EL3.SUBCLASSCD, ";
            }
            $query .= "                     SUM (CASE WHEN ET1.SCORE_DI = '*' ";
            $query .= "                               THEN 1 ";
            $query .= "                               ELSE 0 ";
            $query .= "                          END) ";
            $query .= "                     AS SCORE_DIS, ";
            $query .= "                     ET1.SCHREGNO, ";
            $query .= "                     EL1.GRADE, ";
            $query .= "                     EL1.HR_CLASS, ";
            $query .= "                     EL1.COURSECD || EL1.MAJORCD || EL1.COURSECODE AS COURSE ";
            $query .= "                 FROM ";
            $query .= "                     PROFICIENCY_DAT2 ET1 ";
            $query .= "                     INNER JOIN SCHREG_REGD_DAT EL1 ON EL1.YEAR = ET1.YEAR ";
            $query .= "                         AND EL1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "                         AND EL1.SCHREGNO = ET1.SCHREGNO ";
            $query .= "                         AND EL1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "                     LEFT JOIN PROFICIENCY_SUBCLASS_MST EL3 ON EL3.PROFICIENCY_SUBCLASS_CD = ET1.PROFICIENCY_SUBCLASS_CD, ";
            $query .= "                     PROFICIENCY_SUBCLASS_GROUP_DAT L2 ";
            $query .= "                 WHERE ";
            $query .= "                     (ET1.SCORE IS NOT NULL OR (ET1.SCORE IS NULL AND ET1.SCORE_DI = '*')) ";
            $query .= "                     AND L2.YEAR = ET1.YEAR ";
            $query .= "                         AND L2.SEMESTER = ET1.SEMESTER ";
            $query .= "                         AND L2.PROFICIENCYDIV = ET1.PROFICIENCYDIV ";
            $query .= "                         AND L2.PROFICIENCYCD = ET1.PROFICIENCYCD ";
            $query .= "                         AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "                         AND L2.GRADE = EL1.GRADE ";
            $query .= "                         AND L2.COURSECD = EL1.COURSECD ";
            $query .= "                         AND L2.MAJORCD = EL1.MAJORCD ";
            $query .= "                         AND L2.COURSECODE = EL1.COURSECODE ";
            $query .= "                         AND L2.PROFICIENCY_SUBCLASS_CD = ET1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "                 GROUP BY ";
            $query .= "                     ET1.PROFICIENCYDIV, ";
            $query .= "                     ET1.PROFICIENCYCD, ";
            $query .= "                     EL3.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                     EL3.SCHOOL_KIND, ";
                $query .= "                     EL3.CURRICULUM_CD, ";
                $query .= "                     EL3.SUBCLASSCD, ";
            }
            $query .= "                     ET1.SCHREGNO, ";
            $query .= "                     EL1.GRADE, ";
            $query .= "                     EL1.HR_CLASS, ";
            $query .= "                     EL1.COURSECD, ";
            $query .= "                     EL1.MAJORCD, ";
            $query .= "                     EL1.COURSECODE ";
            $query .= "                 ) E1 ";
            $query .= "             WHERE ";
            $query .= "                 E1.SCORE_DIS = 0 ";
            $query .= "             GROUP BY ";
            $query .= "                 E1.SCHREGNO ";
            if ($groupMst["GROUP_DIV"] != 9 && $model->Properties["notGroupCnt"] != "1") {
                $query .= "             HAVING ";
                $query .= "                 COUNT(*) >= ".$groupMst["GROUP_DIV"]." ";
            }
            $query .= "             ) EE1 ";
            $query .= "         WHERE ";
            $query .= "             EE1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "     ) ";
            $query .= " GROUP BY ";
            $query .= "     LT1.PROFICIENCYDIV, ";
            $query .= "     LT1.PROFICIENCYCD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL3.GROUP_CD, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECODE ";
        }
        if ($div == "GASSAN_KAMOKU") {
            $query .= " ), SCORE_DI_T AS  ( ";
            $query .= " SELECT ";
            $query .= "     E1.SCHREGNO, ";
            $query .= "     SUM(E1.SCORE_DIS) AS SCORE_DIS ";
            $query .= " FROM ";
            $query .= "    (SELECT ";
            $query .= "         ET1.PROFICIENCYDIV, ";
            $query .= "         ET1.PROFICIENCYCD, ";
            $query .= "         EL3.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         EL3.SCHOOL_KIND, ";
                $query .= "         EL3.CURRICULUM_CD, ";
                $query .= "         EL3.SUBCLASSCD, ";
            }
            $query .= "         SUM (CASE WHEN ET1.SCORE_DI = '*' ";
            $query .= "                   THEN 1 ";
            $query .= "                   ELSE 0 ";
            $query .= "              END) ";
            $query .= "         AS SCORE_DIS, ";
            $query .= "         ET1.SCHREGNO, ";
            $query .= "         EL1.GRADE, ";
            $query .= "         EL1.HR_CLASS, ";
            $query .= "         EL1.COURSECD || EL1.MAJORCD || EL1.COURSECODE AS COURSE ";
            $query .= "     FROM ";
            $query .= "         PROFICIENCY_DAT2 ET1 ";
            $query .= "         INNER JOIN SCHREG_REGD_DAT EL1 ON EL1.YEAR = ET1.YEAR ";
            $query .= "             AND EL1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "             AND EL1.SCHREGNO = ET1.SCHREGNO ";
            $query .= "             AND EL1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "         LEFT JOIN PROFICIENCY_SUBCLASS_MST EL3 ON EL3.PROFICIENCY_SUBCLASS_CD = ET1.PROFICIENCY_SUBCLASS_CD, ";
            $query .= "         PROFICIENCY_SUBCLASS_GROUP_DAT L2 ";
            $query .= "     WHERE ";
            $query .= "         (ET1.SCORE IS NOT NULL OR (ET1.SCORE IS NULL AND ET1.SCORE_DI = '*')) ";
            $query .= "         AND L2.YEAR = ET1.YEAR ";
            $query .= "             AND L2.SEMESTER = ET1.SEMESTER ";
            $query .= "             AND L2.PROFICIENCYDIV = ET1.PROFICIENCYDIV ";
            $query .= "             AND L2.PROFICIENCYCD = ET1.PROFICIENCYCD ";
            $query .= "             AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "             AND L2.GRADE = EL1.GRADE ";
            $query .= "             AND L2.COURSECD = EL1.COURSECD ";
            $query .= "             AND L2.MAJORCD = EL1.MAJORCD ";
            $query .= "             AND L2.COURSECODE = EL1.COURSECODE ";
            $query .= "             AND L2.PROFICIENCY_SUBCLASS_CD = ET1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "     GROUP BY ";
            $query .= "         ET1.PROFICIENCYDIV, ";
            $query .= "         ET1.PROFICIENCYCD, ";
            $query .= "         EL3.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         EL3.SCHOOL_KIND, ";
                $query .= "         EL3.CURRICULUM_CD, ";
                $query .= "         EL3.SUBCLASSCD, ";
            }
            $query .= "         ET1.SCHREGNO, ";
            $query .= "         EL1.GRADE, ";
            $query .= "         EL1.HR_CLASS, ";
            $query .= "         EL1.COURSECD, ";
            $query .= "         EL1.MAJORCD, ";
            $query .= "         EL1.COURSECODE ";
            $query .= "     ) E1 ";
            $query .= " GROUP BY ";
            $query .= "     E1.SCHREGNO ";
            if ($groupMst["GROUP_DIV"] != 9 && $model->Properties["notGroupCnt"] != "1") {
                $query .= " HAVING ";
                $query .= "     COUNT(*) >= ".$groupMst["GROUP_DIV"]." ";
            }
        }
        $query .= " ), RANK_T AS  ( ";
        $query .= " SELECT ";
        $query .= "     W1.SCHREGNO, ";
        if ($div == "TUUJOU_KAMOKU") {
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.".$rankName."_AVG, -1) END DESC) AS GRADE_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.".$rankName."_AVG, -1) END DESC) AS CLASS_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.".$rankName."_AVG, -1) END DESC) AS COURSE_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.MAJORCD ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.".$rankName."_AVG, -1) END DESC) AS MAJORCD_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.GROUP_CD ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.".$rankName."_AVG, -1) END DESC) AS GROUP_CD_DEVIATION_RANK ";
            $query .= " FROM ";
            $query .= "     MOCT_T W1 ";
        } else {
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE ORDER BY VALUE(W1.".$rankName."_AVG, -1) DESC) AS GRADE_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY VALUE(W1.".$rankName."_AVG, -1) DESC) AS CLASS_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY VALUE(W1.".$rankName."_AVG, -1) DESC) AS COURSE_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.MAJORCD ORDER BY VALUE(W1.".$rankName."_AVG, -1) DESC) AS MAJORCD_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.PROFICIENCYDIV, W1.PROFICIENCYCD, W1.PROFICIENCY_SUBCLASS_CD, W1.GRADE, W1.GROUP_CD ORDER BY VALUE(W1.".$rankName."_AVG, -1) DESC) AS GROUP_CD_DEVIATION_RANK ";
            $query .= " FROM ";
            $query .= "     MOCT_T W1 ";
            $query .= " WHERE ";
            $query .= "     EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             SCORE_DI_T EE1 ";
            $query .= "         WHERE ";
            $query .= "             EE1.SCHREGNO = W1.SCHREGNO ";
            $query .= "             AND EE1.SCORE_DIS = 0 ";
            $query .= "     ) ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     W1.PROFICIENCYDIV, ";
        $query .= "     W1.PROFICIENCYCD, ";
        $query .= "     W1.SCHREGNO, ";
        $query .= "     W1.PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     W1.GRADE, ";
        $query .= "     W1.SCORE, ";
        $query .= "     W1.AVG, ";
        $query .= "     W1.".$rankName."_AVG, ";
        if ($div == "TUUJOU_KAMOKU") {
            $query .= "     W1.SCORE_DIS, ";
        }
        $query .= "     RANK_T.".$rankName."_DEVIATION_RANK ";
        $query .= " FROM ";
        $query .= "     MOCT_T W1 ";
        $query .= "     LEFT JOIN RANK_T ON W1.SCHREGNO = RANK_T.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     W1.GRADE, ";
        $query .= "     W1.PROFICIENCYDIV, ";
        $query .= "     W1.PROFICIENCYCD, ";
        $query .= "     W1.PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     W1.SCORE DESC ";

        return $query;
    }

    function getTavg($avgDiv, $div)
    {
        $groupBy = "";
        $selectHr = "'000' AS ";
        $selectCs = "'00000000' AS ";
        if ($avgDiv == "01") {
            $groupBy = " PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, GRADE ";
        } else if ($avgDiv == "02") {
            $groupBy = " PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, GRADE, HR_CLASS ";
            $selectHr = "";
        } else if ($avgDiv == "03") {
            $groupBy = " PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, GRADE, COURSE ";
            $selectCs = "";
        } else if ($avgDiv == "04") {
            $groupBy = " PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, GRADE, COURSECD, MAJORCD ";
            $selectCs = "COURSECD || MAJORCD || '0000' AS ";
        } else {
            $groupBy = " PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, GRADE, GROUP_CD ";
            $selectCs = "'0' || GROUP_CD || '0000' AS ";
        }

        $query  = " SELECT ";
        $query .= "    '".$avgDiv."' as AVG_DIV, ";
        $query .= "     PROFICIENCYDIV, ";
        $query .= "     PROFICIENCYCD, ";
        $query .= "     PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     GRADE, ";
        $query .= "     ".$selectHr."HR_CLASS, ";
        $query .= "     ".$selectCs."COURSE, ";
        $query .= "     SUM(SCORE) AS SCORE, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(SCORE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "     DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
        $query .= " FROM ";
        $query .= "     MOCT_T ";
        if ($div == "GASSAN_KAMOKU") {
            $query .= " WHERE ";
            $query .= "     EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             SCORE_DI_T EE1 ";
            $query .= "         WHERE ";
            $query .= "             EE1.SCHREGNO = MOCT_T.SCHREGNO ";
            $query .= "             AND EE1.SCORE_DIS = 0 ";
            $query .= "     ) ";
        }
        $query .= " GROUP BY ".$groupBy;

        return $query;
    }

    function getTavgKansan($avgDiv)
    {
        $groupBy = "";
        $selectHr = "'000' AS ";
        $selectCs = "'00000000' AS ";
        if ($avgDiv == "01") {
            $groupBy = " PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, GRADE ";
        } else if ($avgDiv == "02") {
            $groupBy = " PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, GRADE, HR_CLASS ";
            $selectHr = "";
        } else if ($avgDiv == "03") {
            $groupBy = " PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, GRADE, COURSE ";
            $selectCs = "";
        } else if ($avgDiv == "04") {
            $groupBy = " PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, GRADE, COURSECD, MAJORCD ";
            $selectCs = "COURSECD || MAJORCD || '0000' AS ";
        } else {
            $groupBy = " PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, GRADE, GROUP_CD ";
            $selectCs = "'0' || GROUP_CD || '0000' AS ";
        }

        $query  = " SELECT ";
        $query .= "    '".$avgDiv."' as AVG_DIV, ";
        $query .= "     PROFICIENCYDIV, ";
        $query .= "     PROFICIENCYCD, ";
        $query .= "     PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     GRADE, ";
        $query .= "     ".$selectHr."HR_CLASS, ";
        $query .= "     ".$selectCs."COURSE, ";
        $query .= "     SUM(SCORE) AS SCORE, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(SCORE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "     DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
        $query .= " FROM ";
        $query .= "     MOCT_T_KANSAN ";
        $query .= " GROUP BY ".$groupBy;

        return $query;
    }

    function insertProficiencyRankDat1($db, $query, $model, $div, $dataDiv)
    {
        $result = $db->query($query);

        // インサートの速度改善のためstatement使用
        $iquery = "INSERT INTO PROFICIENCY_RANK_DAT ";
        $iquery .= "( YEAR, SEMESTER, PROFICIENCYDIV, PROFICIENCYCD, ";
        $iquery .= "  SCHREGNO, PROFICIENCY_SUBCLASS_CD, RANK_DATA_DIV, RANK_DIV, SCORE, AVG, RANK, DEVIATION, REGISTERCD, UPDATED)";
        for ($i = 0; $i < 4 * get_count($model->rankDivArray); $i++) {
            if ($i != 0) {
                $iquery .= " union all ";
            }
            $iquery .= " values('".CTRL_YEAR."','".$model->field["SEMESTER"]."' ,'".$model->field["PROFICIENCYDIV"]."', '".$model->field["PROFICIENCYCD"]."', ";
            $iquery .= "        ?  ,?  ,?  ,?  ,?  ,?  ,?  ,? , '".STAFFCD."', SYSDATE()) ";
        }
        //$stmt1 = $db->prepare($iquery);

        $paramCount = 8;
        $rankDivCount = get_count($model->rankDivArray);
        $keys = array_keys($model->rankDivArray);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $param = array();
            for ($i = 0; $i < $rankDivCount; $i++) {
                $paramIdx = $paramCount * $i * 4;
                $key = $keys[$i];
                $val = $model->rankDivArray[$key];

                //総得点での順位
                $param[$paramIdx + 0] = $row["SCHREGNO"];
                $param[$paramIdx + 1] = $row["PROFICIENCY_SUBCLASS_CD"];
                $param[$paramIdx + 2] = "01";
                $param[$paramIdx + 3] = $key;
                $param[$paramIdx + 4] = isset($row["SCORE"]) ? (int)$row["SCORE"] : "CAST(NULL AS SMALLINT)";
                $param[$paramIdx + 5] = isset($row["AVG2"]) ? $row["AVG2"] : null;
                // $param[$paramIdx + 5] = isset($row["AVG2"]) ? $row["AVG2"] : "CAST(NULL AS DECIMAL(8, 5))";
                $param[$paramIdx + 6] = $row["SCORE_DIS"] > 0 ? "CAST(NULL AS SMALLINT)" : (isset($row[$val."_SCORE_RANK"]) ? (int)$row[$val."_SCORE_RANK"] : "CAST(NULL AS SMALLINT)");
                $param[$paramIdx + 7] = isset($row[$val."_DEVIATION"]) ? $row[$val."_DEVIATION"] : null;
                // $param[$paramIdx + 7] = isset($row[$val."_DEVIATION"]) ? $row[$val."_DEVIATION"] : "CAST(NULL AS DECIMAL(4, 1))";
                $paramIdx += $paramCount;

                //平均点での順位
                $param[$paramIdx + 0] = $row["SCHREGNO"];
                $param[$paramIdx + 1] = $row["PROFICIENCY_SUBCLASS_CD"];
                $param[$paramIdx + 2] = "02";
                $param[$paramIdx + 3] = $key;
                $param[$paramIdx + 4] = isset($row["SCORE"]) ? (int)$row["SCORE"] : "CAST(NULL AS SMALLINT)";
                $param[$paramIdx + 5] = isset($row["AVG2"]) ? $row["AVG2"] : null;
                // $param[$paramIdx + 5] = isset($row["AVG2"]) ? $row["AVG2"] : "CAST(NULL AS DECIMAL(8, 5))";
                $param[$paramIdx + 6] = $row["SCORE_DIS"] > 0 ? "CAST(NULL AS SMALLINT)" : (isset($row[$val."_AVG_RANK2"]) ? (int)$row[$val."_AVG_RANK2"] : "CAST(NULL AS SMALLINT)");
                $param[$paramIdx + 7] = isset($row[$val."_DEVIATION"]) ? $row[$val."_DEVIATION"] : null;
                // $param[$paramIdx + 7] = isset($row[$val."_DEVIATION"]) ? $row[$val."_DEVIATION"] : "CAST(NULL AS DECIMAL(4, 1))";
                $paramIdx += $paramCount;

                //偏差値での順位
                $param[$paramIdx + 0] = $row["SCHREGNO"];
                $param[$paramIdx + 1] = $row["PROFICIENCY_SUBCLASS_CD"];
                $param[$paramIdx + 2] = "03";
                $param[$paramIdx + 3] = $key;
                $param[$paramIdx + 4] = isset($row["SCORE_KANSAN"]) ? (int)$row["SCORE_KANSAN"] : "CAST(NULL AS SMALLINT)";
                $param[$paramIdx + 5] = isset($row["AVG_KANSAN"]) ? $row["AVG_KANSAN"] : null;
                // $param[$paramIdx + 5] = isset($row["AVG_KANSAN"]) ? $row["AVG_KANSAN"] : "CAST(NULL AS DECIMAL(8, 5))";
                $param[$paramIdx + 6] = $row["SCORE_DIS"] > 0 ? "CAST(NULL AS SMALLINT)" : (isset($row[$val."_DEVIATION_RANK_KANSAN"]) ? (int)$row[$val."_DEVIATION_RANK_KANSAN"] : "CAST(NULL AS SMALLINT)");
                $param[$paramIdx + 7] = isset($row[$val."_DEVIATION_KANSAN"]) ? $row[$val."_DEVIATION_KANSAN"] : null;
                // $param[$paramIdx + 7] = isset($row[$val."_DEVIATION_KANSAN"]) ? $row[$val."_DEVIATION_KANSAN"] : "CAST(NULL AS DECIMAL(4, 1))";
                $paramIdx += $paramCount;

                //平均点2での順位
                $param[$paramIdx + 0] = $row["SCHREGNO"];
                $param[$paramIdx + 1] = $row["PROFICIENCY_SUBCLASS_CD"];
                $param[$paramIdx + 2] = "04";
                $param[$paramIdx + 3] = $key;
                $param[$paramIdx + 4] = isset($row["SCORE"]) ? (int)$row["SCORE"] : "CAST(NULL AS SMALLINT)";
                $param[$paramIdx + 5] = isset($row["AVG2"]) ? $row["AVG2"] : null;
                // $param[$paramIdx + 5] = isset($row["AVG2"]) ? $row["AVG2"] : "CAST(NULL AS DECIMAL(8, 5))";
                $param[$paramIdx + 6] = $row["SCORE_DIS"] > 0 ? "CAST(NULL AS SMALLINT)" : (isset($row[$val."_AVG_RANK2"]) ? (int)$row[$val."_AVG_RANK2"] : "CAST(NULL AS SMALLINT)");
                $param[$paramIdx + 7] = isset($row[$val."_DEVIATION"]) ? $row[$val."_DEVIATION"] : null;
                // $param[$paramIdx + 7] = isset($row[$val."_DEVIATION"]) ? $row[$val."_DEVIATION"] : "CAST(NULL AS DECIMAL(4, 1))";
                $paramIdx += $paramCount;
            }
            $db->query($iquery, $param);
        }
        $result->free();
    }

    function insertProficiencyRankDat2($db, $query, $model, $div, $dataDiv)
    {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            foreach ($model->rankDivArray as $key => $val) {
                //総得点での順位
                $data = array();
                $data["YEAR"][TEXT]                     = CTRL_YEAR;
                $data["SEMESTER"][TEXT]                 = $model->field["SEMESTER"];
                $data["PROFICIENCYDIV"][TEXT]           = $row["PROFICIENCYDIV"];
                $data["PROFICIENCYCD"][TEXT]            = $row["PROFICIENCYCD"];
                $data["SCHREGNO"][TEXT]                 = $row["SCHREGNO"];
                $data["PROFICIENCY_SUBCLASS_CD"][TEXT]  = $row["PROFICIENCY_SUBCLASS_CD"];
                $data["RANK_DATA_DIV"][TEXT]            = "11";
                $data["RANK_DIV"][TEXT]                 = $key;
                $data["SCORE"][NUMBER]                  = $row["SCORE"];
                $data["AVG"][NUMBER]                    = $row["AVG2"];
                $data["RANK"][NUMBER]                   = $row["SCORE_DIS"] > 0 ? NULL : $row[$val."_SCORE_RANK"];
                $data["DEVIATION"][NUMBER]              = $row[$val."_DEVIATION"];
                $data["REGISTERCD"][TEXT]               = STAFFCD;
                $data["UPDATED"][FUNC]                  = "SYSDATE()";

                $query = Query::insertSQL($data, "PROFICIENCY_RANK_DAT");
                $db->query($query);

                //平均点での順位
                $data = array();
                $data["YEAR"][TEXT]                     = CTRL_YEAR;
                $data["SEMESTER"][TEXT]                 = $model->field["SEMESTER"];
                $data["PROFICIENCYDIV"][TEXT]           = $row["PROFICIENCYDIV"];
                $data["PROFICIENCYCD"][TEXT]            = $row["PROFICIENCYCD"];
                $data["SCHREGNO"][TEXT]                 = $row["SCHREGNO"];
                $data["PROFICIENCY_SUBCLASS_CD"][TEXT]  = $row["PROFICIENCY_SUBCLASS_CD"];
                $data["RANK_DATA_DIV"][TEXT]            = "12";
                $data["RANK_DIV"][TEXT]                 = $key;
                $data["SCORE"][NUMBER]                  = $row["SCORE"];
                $data["AVG"][NUMBER]                    = $row["AVG2"];
                $data["RANK"][NUMBER]                   = $row["SCORE_DIS"] > 0 ? NULL : $row[$val."_AVG_RANK2"];
                $data["DEVIATION"][NUMBER]              = $row[$val."_DEVIATION"];
                $data["REGISTERCD"][TEXT]               = STAFFCD;
                $data["UPDATED"][FUNC]                  = "SYSDATE()";

                $query = Query::insertSQL($data, "PROFICIENCY_RANK_DAT");
                $db->query($query);
            }
        }
        $result->free();
    }

    function insertProficiencyRankDatDeviation($db, $query, $model, $div, $rankDiv, $dataName)
    {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $delQuery  = " DELETE FROM ";
            $delQuery .= "     PROFICIENCY_RANK_DAT ";
            $delQuery .= " WHERE ";
            $delQuery .= "     YEAR = '".CTRL_YEAR."' ";
            $delQuery .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $delQuery .= "     AND PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
            $delQuery .= "     AND PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."' ";
            $delQuery .= "     AND PROFICIENCY_SUBCLASS_CD = '".$row["PROFICIENCY_SUBCLASS_CD"]."' ";
            $delQuery .= "     AND SCHREGNO = '".$row["SCHREGNO"]."' ";
            $delQuery .= "     AND RANK_DATA_DIV = '03'";
            $delQuery .= "     AND RANK_DIV = '".$rankDiv."'";
            $db->query($delQuery);

            //偏差値での順位
            $data = array();
            $data["YEAR"][TEXT]                     = CTRL_YEAR;
            $data["SEMESTER"][TEXT]                 = $model->field["SEMESTER"];
            $data["PROFICIENCYDIV"][TEXT]           = $row["PROFICIENCYDIV"];
            $data["PROFICIENCYCD"][TEXT]            = $row["PROFICIENCYCD"];
            $data["SCHREGNO"][TEXT]                 = $row["SCHREGNO"];
            $data["PROFICIENCY_SUBCLASS_CD"][TEXT]  = $row["PROFICIENCY_SUBCLASS_CD"];
            $data["RANK_DATA_DIV"][TEXT]            = "03";
            $data["RANK_DIV"][TEXT]                 = $rankDiv;
            $data["SCORE"][NUMBER]                  = $row["SCORE"];
            $data["AVG"][NUMBER]                    = $row["AVG"];
            $data["RANK"][NUMBER]                   = $row["SCORE_DIS"] > 0 ? NULL : $row[$dataName."_DEVIATION_RANK"];
            $data["DEVIATION"][NUMBER]              = $row[$dataName."_AVG"];
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][FUNC]                  = "SYSDATE()";
            $query = Query::insertSQL($data, "PROFICIENCY_RANK_DAT");
            $db->query($query);

        }
        $result->free();
    }

    //実力席次更新
    function UpdateProficiencyAverageDat($model, $dataDiv)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($dataDiv == "1") {
            $delQuery  = " DELETE FROM ";
            $delQuery .= "     PROFICIENCY_AVERAGE_DAT ";
            $delQuery .= " WHERE ";
            $delQuery .= "     YEAR = '".CTRL_YEAR."' ";
            $delQuery .= "     AND SEMESTER = '".$model->field["SEMESTER"]."'";
            $delQuery .= "     AND PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."'";
            $delQuery .= "     AND PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."'";
            $delQuery .= "     AND GRADE = '".$model->field["GRADE"]."' ";
            $db->query($delQuery);
        }

        $query = knjh539aQuery::getAverageDatSql($model, "", "", "TUUJOU_KAMOKU", $dataDiv);
        knjh539aQuery::InsertProficiencyAverageDat($db, $query, $model, $dataDiv);

        $groupMstQuery  = "SELECT DISTINCT ";
        $groupMstQuery .= "  GROUP_DIV ";
        $groupMstQuery .= "FROM ";
        $groupMstQuery .= "  PROFICIENCY_SUBCLASS_GROUP_MST ";
        $groupMstQuery .= "WHERE ";
        $groupMstQuery .= "  YEAR = '".CTRL_YEAR."' ";
        $groupMstQuery .= "  AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $groupMstQuery .= "  AND PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $groupMstQuery .= "  AND PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."' ";
        $groupMstQuery .= "  AND GRADE = '".$model->field["GRADE"]."' ";

        $result = $db->query($groupMstQuery);
        while ($groupMst = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $groupSubclass = "";
            for ($i = 0; $i < 6; $i++) {
                $groupSubclass .= $groupMst["GROUP_DIV"];
            }

            $query = knjh539aQuery::getAverageDatSql($model, $groupMst, $groupSubclass, "GASSAN_KAMOKU", $dataDiv);
            knjh539aQuery::InsertProficiencyAverageDat($db, $query, $model, $dataDiv);

        }
        $result = $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    function getAverageDatSql($model, $groupMst, $groupSubclass, $div, $dataDiv)
    {
        $query  = " WITH PROFICIENCY_DAT2 AS (";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     PROFICIENCYDIV, ";
        $query .= "     PROFICIENCYCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     CASE WHEN SCORE_DI = '*' ";
        $query .= "          THEN NULL ";
        $query .= "          ELSE SCORE ";
        $query .= "     END AS SCORE, ";
        $query .= "     SCORE_DI ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "     AND PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= " ), MOCT_T AS (";
        $query .= " SELECT ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     T1.PROFICIENCYCD, ";
        if ($div == "GASSAN_KAMOKU") {
            $query .= "     '".$groupSubclass."' AS PROFICIENCY_SUBCLASS_CD, ";
        } else {
            $query .= "     T1.PROFICIENCY_SUBCLASS_CD, ";
        }
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L4.GROUP_CD, ";
        $query .= "     L1.COURSECD, ";
        $query .= "     L1.MAJORCD, ";
        $query .= "     L1.COURSECD || L1.MAJORCD || L1.COURSECODE AS COURSE, ";
        if ($dataDiv == "2") {
            $query .= "     SUM(ROUND(T1.SCORE * VALUE(L3.WEIGHTING, 1)*100,-2)/100) AS SCORE, ";
            $query .= "     DECIMAL(ROUND(SUM(FLOAT(ROUND(VALUE(L3.PERFECT, 100) * VALUE(L3.WEIGHTING, 1)*100,-2)/100)) * 100000, 0) / 100000, 9, 5) AS PERFECT, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(ROUND(T1.SCORE * VALUE(L3.WEIGHTING, 1)*100,-2)/100)) * 100000, 0) / 100000, 9, 5) AS AVG ";
        } else {
            $query .= "     SUM(T1.SCORE) AS SCORE, ";
            $query .= "     SUM(VALUE(L3.PERFECT, 100)) AS PERFECT, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.SCORE)) * 100000, 0) / 100000, 9, 5) AS AVG ";
        }
        $query .= " FROM ";
        $query .= "     PROFICIENCY_DAT2 T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "           AND L1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "           AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "           AND L1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT L4 ON L4.YEAR = T1.YEAR ";
        $query .= "          AND L4.GRADE = L1.GRADE ";
        $query .= "          AND L4.COURSECD || L4.MAJORCD || L4.COURSECODE = L1.COURSECD || L1.MAJORCD || L1.COURSECODE ";
        $query .= "     LEFT JOIN PROFICIENCY_PERFECT_COURSE_DAT L3 ON L3.YEAR = T1.YEAR ";
        $query .= "          AND L3.SEMESTER = T1.SEMESTER ";
        $query .= "          AND L3.PROFICIENCYDIV = T1.PROFICIENCYDIV ";
        $query .= "          AND L3.PROFICIENCYCD = T1.PROFICIENCYCD ";
        $query .= "          AND L3.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ";
        $query .= "          AND L3.GRADE = CASE WHEN L3.DIV = '01' ";
        $query .= "                           THEN '00' ";
        $query .= "                           ELSE L1.GRADE ";
        $query .= "                      END ";
        $query .= "          AND L3.COURSECD || L3.MAJORCD || L3.COURSECODE = CASE WHEN L3.DIV IN ('01','02') ";
        $query .= "                                                       THEN '00000000' ";
        $query .= "                                                       ELSE CASE WHEN L3.DIV = '04' ";
        $query .= "                                                                 THEN '0' || L4.GROUP_CD || '0000' ";
        $query .= "                                                                 ELSE L1.COURSECD || L1.MAJORCD || L1.COURSECODE ";
        $query .= "                                                            END ";
        $query .= "                                                  END ";
        if ($div == "GASSAN_KAMOKU") {
            $query .= "     ,PROFICIENCY_SUBCLASS_GROUP_DAT L2 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCORE IS NOT NULL ";
        if ($div == "GASSAN_KAMOKU") {
            $query .= "     AND L2.YEAR = T1.YEAR ";
            $query .= "     AND L2.SEMESTER = T1.SEMESTER ";
            $query .= "     AND L2.PROFICIENCYDIV = T1.PROFICIENCYDIV ";
            $query .= "     AND L2.PROFICIENCYCD = T1.PROFICIENCYCD ";
            $query .= "     AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "     AND L2.GRADE = L1.GRADE ";
            $query .= "     AND L2.COURSECD = L1.COURSECD ";
            $query .= "     AND L2.MAJORCD = L1.MAJORCD ";
            $query .= "     AND L2.COURSECODE = L1.COURSECODE ";
            $query .= "     AND L2.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "     AND EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "            (SELECT ";
            $query .= "                 E1.SCHREGNO ";
            $query .= "             FROM ";
            $query .= "                (SELECT ";
            $query .= "                     ET1.PROFICIENCYDIV, ";
            $query .= "                     ET1.PROFICIENCYCD, ";
            $query .= "                     EL3.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                     EL3.SCHOOL_KIND, ";
                $query .= "                     EL3.CURRICULUM_CD, ";
                $query .= "                     EL3.SUBCLASSCD, ";
            }
            $query .= "                     SUM (CASE WHEN ET1.SCORE_DI = '*' ";
            $query .= "                               THEN 1 ";
            $query .= "                               ELSE 0 ";
            $query .= "                          END) ";
            $query .= "                     AS SCORE_DIS, ";
            $query .= "                     ET1.SCHREGNO, ";
            $query .= "                     EL1.GRADE, ";
            $query .= "                     EL1.HR_CLASS, ";
            $query .= "                     EL1.COURSECD || EL1.MAJORCD || EL1.COURSECODE AS COURSE ";
            $query .= "                 FROM ";
            $query .= "                     PROFICIENCY_DAT2 ET1 ";
            $query .= "                     INNER JOIN SCHREG_REGD_DAT EL1 ON EL1.YEAR = ET1.YEAR ";
            $query .= "                         AND EL1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "                         AND EL1.SCHREGNO = ET1.SCHREGNO ";
            $query .= "                         AND EL1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "                     LEFT JOIN PROFICIENCY_SUBCLASS_MST EL3 ON EL3.PROFICIENCY_SUBCLASS_CD = ET1.PROFICIENCY_SUBCLASS_CD, ";
            $query .= "                     PROFICIENCY_SUBCLASS_GROUP_DAT L2 ";
            $query .= "                 WHERE ";
            $query .= "                     (ET1.SCORE IS NOT NULL OR (ET1.SCORE IS NULL AND ET1.SCORE_DI = '*')) ";
            $query .= "                     AND L2.YEAR = ET1.YEAR ";
            $query .= "                         AND L2.SEMESTER = ET1.SEMESTER ";
            $query .= "                         AND L2.PROFICIENCYDIV = ET1.PROFICIENCYDIV ";
            $query .= "                         AND L2.PROFICIENCYCD = ET1.PROFICIENCYCD ";
            $query .= "                         AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "                         AND L2.GRADE = EL1.GRADE ";
            $query .= "                         AND L2.COURSECD = EL1.COURSECD ";
            $query .= "                         AND L2.MAJORCD = EL1.MAJORCD ";
            $query .= "                         AND L2.COURSECODE = EL1.COURSECODE ";
            $query .= "                         AND L2.PROFICIENCY_SUBCLASS_CD = ET1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "                 GROUP BY ";
            $query .= "                     ET1.PROFICIENCYDIV, ";
            $query .= "                     ET1.PROFICIENCYCD, ";
            $query .= "                     EL3.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                     EL3.SCHOOL_KIND, ";
                $query .= "                     EL3.CURRICULUM_CD, ";
                $query .= "                     EL3.SUBCLASSCD, ";
            }
            $query .= "                     ET1.SCHREGNO, ";
            $query .= "                     EL1.GRADE, ";
            $query .= "                     EL1.HR_CLASS, ";
            $query .= "                     EL1.COURSECD, ";
            $query .= "                     EL1.MAJORCD, ";
            $query .= "                     EL1.COURSECODE ";
            $query .= "                 ) E1 ";
            $query .= "             WHERE ";
            $query .= "                 E1.SCORE_DIS = 0 ";
            $query .= "             GROUP BY ";
            $query .= "                 E1.SCHREGNO ";
            if ($groupMst["GROUP_DIV"] != 9 && $model->Properties["notGroupCnt"] != "1") {
                $query .= "             HAVING ";
                $query .= "                 COUNT(*) >= ".$groupMst["GROUP_DIV"]." ";
            }
            $query .= "             ) EE1 ";
            $query .= "         WHERE ";
            $query .= "             EE1.SCHREGNO = T1.SCHREGNO ";
            $query .= "     ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     T1.PROFICIENCYCD, ";
        if ($div == "TUUJOU_KAMOKU") {
            $query .= "     T1.PROFICIENCY_SUBCLASS_CD, ";
        }
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L4.GROUP_CD, ";
        $query .= "     L1.COURSECD, ";
        $query .= "     L1.MAJORCD, ";
        $query .= "     L1.COURSECODE ";

        if ($div == "GASSAN_KAMOKU") {
            $query .= " ), SCORE_DI_T AS  ( ";
            $query .= " SELECT ";
            $query .= "     E1.SCHREGNO, ";
            $query .= "     SUM(E1.SCORE_DIS) AS SCORE_DIS ";
            $query .= " FROM ";
            $query .= "    (SELECT ";
            $query .= "         ET1.PROFICIENCYDIV, ";
            $query .= "         ET1.PROFICIENCYCD, ";
            $query .= "         EL3.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         EL3.SCHOOL_KIND, ";
                $query .= "         EL3.CURRICULUM_CD, ";
                $query .= "         EL3.SUBCLASSCD, ";
            }
            $query .= "         SUM (CASE WHEN ET1.SCORE_DI = '*' ";
            $query .= "                   THEN 1 ";
            $query .= "                   ELSE 0 ";
            $query .= "              END) ";
            $query .= "         AS SCORE_DIS, ";
            $query .= "         ET1.SCHREGNO, ";
            $query .= "         EL1.GRADE, ";
            $query .= "         EL1.HR_CLASS, ";
            $query .= "         EL1.COURSECD || EL1.MAJORCD || EL1.COURSECODE AS COURSE ";
            $query .= "     FROM ";
            $query .= "         PROFICIENCY_DAT2 ET1 ";
            $query .= "         INNER JOIN SCHREG_REGD_DAT EL1 ON EL1.YEAR = ET1.YEAR ";
            $query .= "             AND EL1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "             AND EL1.SCHREGNO = ET1.SCHREGNO ";
            $query .= "             AND EL1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "         LEFT JOIN PROFICIENCY_SUBCLASS_MST EL3 ON EL3.PROFICIENCY_SUBCLASS_CD = ET1.PROFICIENCY_SUBCLASS_CD, ";
            $query .= "         PROFICIENCY_SUBCLASS_GROUP_DAT L2 ";
            $query .= "     WHERE ";
            $query .= "         (ET1.SCORE IS NOT NULL OR (ET1.SCORE IS NULL AND ET1.SCORE_DI = '*')) ";
            $query .= "         AND L2.YEAR = ET1.YEAR ";
            $query .= "             AND L2.SEMESTER = ET1.SEMESTER ";
            $query .= "             AND L2.PROFICIENCYDIV = ET1.PROFICIENCYDIV ";
            $query .= "             AND L2.PROFICIENCYCD = ET1.PROFICIENCYCD ";
            $query .= "             AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "             AND L2.GRADE = EL1.GRADE ";
            $query .= "             AND L2.COURSECD = EL1.COURSECD ";
            $query .= "             AND L2.MAJORCD = EL1.MAJORCD ";
            $query .= "             AND L2.COURSECODE = EL1.COURSECODE ";
            $query .= "             AND L2.PROFICIENCY_SUBCLASS_CD = ET1.PROFICIENCY_SUBCLASS_CD ";
            $query .= "     GROUP BY ";
            $query .= "         ET1.PROFICIENCYDIV, ";
            $query .= "         ET1.PROFICIENCYCD, ";
            $query .= "         EL3.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         EL3.SCHOOL_KIND, ";
                $query .= "         EL3.CURRICULUM_CD, ";
                $query .= "         EL3.SUBCLASSCD, ";
            }
            $query .= "         ET1.SCHREGNO, ";
            $query .= "         EL1.GRADE, ";
            $query .= "         EL1.HR_CLASS, ";
            $query .= "         EL1.COURSECD, ";
            $query .= "         EL1.MAJORCD, ";
            $query .= "         EL1.COURSECODE ";
            $query .= "     ) E1 ";
            $query .= " GROUP BY ";
            $query .= "     E1.SCHREGNO ";
            if ($groupMst["GROUP_DIV"] != 9 && $model->Properties["notGroupCnt"] != "1") {
                $query .= " HAVING ";
                $query .= "     COUNT(*) >= ".$groupMst["GROUP_DIV"]." ";
            }
        }

        $query .= " ), T_AVERAGE AS  ( ";

        $query .= knjh539aQuery::getTavg2("01", $div);

        $query .= " UNION ";
        $query .= knjh539aQuery::getTavg2("02", $div);

        $query .= " UNION ";
        $query .= knjh539aQuery::getTavg2("03", $div);

        $query .= " UNION ";
        $query .= knjh539aQuery::getTavg2("04", $div);

        $query .= " UNION ";
        $query .= knjh539aQuery::getTavg2("05", $div);

        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     PROFICIENCYDIV, ";
        $query .= "     PROFICIENCYCD, ";
        $query .= "     PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     AVG_DIV, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     COURSE, ";
        $query .= "     SCORE, ";
        $query .= "     SCORE_KANSAN, ";
        $query .= "     HIGHSCORE, ";
        $query .= "     HIGHSCORE_KANSAN, ";
        $query .= "     LOWSCORE, ";
        $query .= "     LOWSCORE_KANSAN, ";
        $query .= "     CNT, ";
        $query .= "     AVG, ";
        $query .= "     AVG2, ";
        $query .= "     STDDEV ";
        $query .= " FROM ";
        $query .= "     T_AVERAGE ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     PROFICIENCYDIV, ";
        $query .= "     PROFICIENCYCD, ";
        $query .= "     AVG_DIV, ";
        $query .= "     PROFICIENCY_SUBCLASS_CD ";

        return $query;
    }

    function getTavg2($avgDiv, $div)
    {
        $groupBy = "";
        $selectHr = "'000' AS ";
        $selectCs = "'00000000' AS ";
        if ($avgDiv == "01") {
            $groupBy = " PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, GRADE ";
        } else if ($avgDiv == "02") {
            $groupBy = " PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, GRADE, HR_CLASS ";
            $selectHr = "";
        } else if ($avgDiv == "03") {
            $groupBy = " PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, GRADE, COURSE ";
            $selectCs = "";
        } else if ($avgDiv == "04") {
            $groupBy = " PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, GRADE, COURSECD, MAJORCD ";
            $selectCs = "COURSECD || MAJORCD || '0000' AS ";
        } else {
            $groupBy = " PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, GRADE, GROUP_CD ";
            $selectCs = "'0' || VALUE(GROUP_CD,'000') || '0000' AS ";
        }


        $query .= " SELECT ";
        $query .= "    '".$avgDiv."' as AVG_DIV, ";
        $query .= "     PROFICIENCYDIV, ";
        $query .= "     PROFICIENCYCD, ";
        $query .= "     PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     GRADE, ";
        $query .= "     ".$selectHr."HR_CLASS, ";
        $query .= "     ".$selectCs."COURSE, ";
        $query .= "     SUM(SCORE) AS SCORE, ";
        $query .= "     SUM(CEIL( FLOAT(SCORE) * 100 / PERFECT)) AS SCORE_KANSAN, ";
        $query .= "     MAX(SCORE) AS HIGHSCORE, ";
        $query .= "     MAX(CEIL( FLOAT(SCORE) * 100 / PERFECT)) AS HIGHSCORE_KANSAN, ";
        $query .= "     MIN(SCORE) AS LOWSCORE, ";
        $query .= "     MIN(CEIL( FLOAT(SCORE) * 100 / PERFECT)) AS LOWSCORE_KANSAN, ";
        $query .= "     COUNT(PROFICIENCYCD) AS CNT, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(SCORE))*100000,0) / 100000, 9, 5) AS AVG, ";
//        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CEIL( FLOAT(SCORE) * 100 / PERFECT)))*100000,0) / 100000, 9, 5) AS AVG2, ";
        $query .= "     DECIMAL(ROUND(((FLOAT(SUM(SCORE))/FLOAT(SUM(VALUE(PERFECT, 100))))*100)*100000,0)/100000,9,5) AS AVG2, ";
        $query .= "     DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0) / 10, 5, 1) AS STDDEV ";
        $query .= " FROM ";
        $query .= "     MOCT_T ";
        if ($div == "GASSAN_KAMOKU") {
            $query .= " WHERE ";
            $query .= "     EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             SCORE_DI_T EE1 ";
            $query .= "         WHERE ";
            $query .= "             EE1.SCHREGNO = MOCT_T.SCHREGNO ";
            $query .= "             AND EE1.SCORE_DIS = 0 ";
            $query .= "     ) ";
        }
        $query .= " GROUP BY ".$groupBy;

        return $query;
    }

    //実力平均データ登録
    function InsertProficiencyAverageDat($db, $query, $model, $dataDiv) {

        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]                     = CTRL_YEAR;
            $data["SEMESTER"][TEXT]                 = $model->field["SEMESTER"];
            $data["PROFICIENCYDIV"][TEXT]           = $row["PROFICIENCYDIV"];
            $data["PROFICIENCYCD"][TEXT]            = $row["PROFICIENCYCD"];
            $data["PROFICIENCY_SUBCLASS_CD"][TEXT]  = $row["PROFICIENCY_SUBCLASS_CD"];
            $data["DATA_DIV"][TEXT]                 = $dataDiv;
            $data["AVG_DIV"][TEXT]                  = $row["AVG_DIV"];
            $data["GRADE"][TEXT]                    = $row["GRADE"];
            $data["HR_CLASS"][TEXT]                 = $row["HR_CLASS"];
            $data["COURSECD"][TEXT]                 = substr($row["COURSE"], 0, 1);
            $data["MAJORCD"][TEXT]                  = substr($row["COURSE"], 1, 3);
            $data["COURSECODE"][TEXT]               = substr($row["COURSE"], 4);
            $data["SCORE"][NUMBER]                  = $row["SCORE"];
            $data["SCORE_KANSAN"][NUMBER]           = $row["SCORE_KANSAN"];
            $data["HIGHSCORE"][NUMBER]              = $row["HIGHSCORE"];
            $data["HIGHSCORE_KANSAN"][NUMBER]       = $row["HIGHSCORE_KANSAN"];
            $data["LOWSCORE"][NUMBER]               = $row["LOWSCORE"];
            $data["LOWSCORE_KANSAN"][NUMBER]        = $row["LOWSCORE_KANSAN"];
            $data["COUNT"][NUMBER]                  = $row["CNT"];
            $data["AVG"][NUMBER]                    = $row["AVG"];
            $data["AVG_KANSAN"][NUMBER]             = $row["AVG2"];
            $data["STDDEV"][NUMBER]                 = $row["STDDEV"];
            $data["STDDEV_KANSAN"][NUMBER]          = $row["STDDEV"];
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][FUNC]                  = "SYSDATE()";

            $query = Query::insertSQL($data, "PROFICIENCY_AVERAGE_DAT");
            $db->query($query);
        }
        $result->free();
    }
}
?>
