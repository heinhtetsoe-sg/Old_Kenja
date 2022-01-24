<?php

require_once('for_php7.php');


class knjd212cQuery extends Query
{

    //処理学期
    public function getSemester($seme = "")
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if (strlen($seme)) {
            $query .= "     AND SEMESTER = '{$seme}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //処理学年
    public function getGrade($model, $grade = "")
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if (strlen($grade)) {
            $query .= "     AND GRADE = '{$grade}' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //処理科目
    public function getSubclasscd($model, $school_kind)
    {
        $year = CTRL_YEAR;
        $seme = ($model->semeester != "9") ? $model->semeester : CTRL_SEMESTER;
        $staffcd = STAFFCD;

        $query  = " SELECT ";
        $query .= "     V_SUB.CLASSCD || '-' || V_SUB.SCHOOL_KIND || '-' || V_SUB.CURRICULUM_CD || '-' || V_SUB.SUBCLASSCD || ':' || V_SUB.SUBCLASSNAME AS LABEL, ";
        $query .= "     V_SUB.CLASSCD || '-' || V_SUB.SCHOOL_KIND || '-' || V_SUB.CURRICULUM_CD || '-' || V_SUB.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST V_SUB ";
        $query .= " WHERE ";
        $query .= "     V_SUB.YEAR = '{$year}' ";
        $query .= "     AND V_SUB.SCHOOL_KIND = '{$school_kind}' ";
        $query .= "     AND V_SUB.CLASSCD <= '90' ";
        $query .= "     AND NOT EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             '' ";
        $query .= "         FROM ";
        $query .= "             SUBCLASS_RANK_REPLACE_DAT SUB_REP ";
        $query .= "         WHERE ";
        $query .= "             V_SUB.YEAR = SUB_REP.YEAR ";
        $query .= "             AND V_SUB.CLASSCD = SUB_REP.COMBINED_CLASSCD ";
        $query .= "             AND V_SUB.SCHOOL_KIND = SUB_REP.COMBINED_SCHOOL_KIND ";
        $query .= "             AND V_SUB.CURRICULUM_CD = SUB_REP.COMBINED_CURRICULUM_CD ";
        $query .= "             AND V_SUB.SUBCLASSCD = SUB_REP.COMBINED_SUBCLASSCD ";
        $query .= "     ) ";
        $query .= "     AND NOT EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             SUBCLASS_REPLACE_COMBINED_DAT SUB_REP_COMB ";
        $query .= "         WHERE ";
        $query .= "             V_SUB.YEAR = SUB_REP_COMB.YEAR ";
        $query .= "             AND V_SUB.CLASSCD = SUB_REP_COMB.ATTEND_CLASSCD ";
        $query .= "             AND V_SUB.SCHOOL_KIND = SUB_REP_COMB.ATTEND_SCHOOL_KIND ";
        $query .= "             AND V_SUB.CURRICULUM_CD = SUB_REP_COMB.ATTEND_CURRICULUM_CD ";
        $query .= "             AND V_SUB.SUBCLASSCD = SUB_REP_COMB.ATTEND_SUBCLASSCD ";
        $query .= "     ) ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN ( ";
            $query .= "         SELECT ";
            $query .= "             T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
            $query .= "         FROM ";
            $query .= "             CHAIR_DAT T1, ";
            $query .= "             CHAIR_STD_DAT T2, ";
            $query .= "             CHAIR_STF_DAT T4 ";
            $query .= "         WHERE ";
            $query .= "                 T1.YEAR       = '{$year}' ";
            $query .= "             AND T1.SEMESTER   = '{$seme}' ";
            $query .= "             AND SUBSTR(T1.SUBCLASSCD, 1 ,2) <= '90' ";
            $query .= "             AND T2.YEAR       = T1.YEAR ";
            $query .= "             AND T2.SEMESTER   = T1.SEMESTER ";
            $query .= "             AND T2.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             AND T4.YEAR       = T1.YEAR ";
            $query .= "             AND T4.SEMESTER   = T1.SEMESTER ";
            $query .= "             AND T4.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             AND T4.STAFFCD    = '{$staffcd}' "; //ログインした先生
            $query .= "         GROUP BY ";
            $query .= "             T1.CLASSCD, ";
            $query .= "             T1.SCHOOL_KIND, ";
            $query .= "             T1.CURRICULUM_CD, ";
            $query .= "             T1.SUBCLASSCD ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //履歴一覧
    public function getListRireki($model)
    {
        $year = CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     T1.CALC_DATE, ";
        $query .= "     T1.CALC_TIME, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     L2.GRADE_NAME1, ";
        $query .= "     L3.SEMESTERNAME ";
        $query .= " FROM ";
        $query .= " RECORD_SCORE_EXEC_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                              AND L1.CLASSCD = T1.CLASSCD ";
        $query .= "                              AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                              AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR AND L2.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN SEMESTER_MST L3 ON L3.YEAR = T1.YEAR AND L3.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semeester}' ";
        $query .= "     AND T1.GRADE = '{$model->grade}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CALC_DATE DESC, ";
        $query .= "     T1.CALC_TIME DESC ";
        return $query;
    }

    public function getAdminCtrlCalc($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD, ";
        $query .= "     SCORE_DIV, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     MOTO_SEMESTER, ";
        $query .= "     MOTO_TESTKINDCD, ";
        $query .= "     MOTO_TESTITEMCD, ";
        $query .= "     MOTO_SCORE_DIV ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_CALC_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->semeester}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->school_kind}' ";
        $query .= "     AND TESTKINDCD = '99' ";
        $query .= "     AND TESTITEMCD = '00' ";
        $query .= "     AND SCORE_DIV  = '08' ";

        return $query;
    }

    //処理実行時に算出設定済みかチェック
    public function getAdminCtrlCalcExists($model)
    {
        $query  = "   WITH BASE AS ( ";
        $query .= "     SELECT ";
        $query .= "       CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "       ADMIN_CONTROL_CALC_SDIV_DAT ";
        $query .= "     WHERE ";
        $query .= "       YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND SEMESTER = '{$model->semeester}' ";
        $query .= "       AND SCHOOL_KIND = '{$model->school_kind}' ";
        $query .= "       AND TESTKINDCD = '99' ";
        $query .= "       AND TESTITEMCD = '00' ";
        $query .= "       AND SCORE_DIV = '08' ";
        $query .= "   ) ";
        $query .= "   SELECT ";
        $query .= "     '1' AS TYPE, ";
        $query .= "     SUBCLASSCD ";
        $query .= "   FROM ";
        $query .= "     BASE ";
        $query .= "   WHERE ";
        if ($model->subclasscd != "000000") {
            $query .= "     SUBCLASSCD = '{$model->subclasscd}' ";
        } else {
            $query .= "     SUBCLASSCD IN ('".implode("','", array_keys($model->subclassArray))."') ";
        }
        $query .= "   UNION ";
        $query .= "   SELECT ";
        $query .= "     '2' AS TYPE, ";
        $query .= "     SUBCLASSCD ";
        $query .= "   FROM ";
        $query .= "     BASE ";
        $query .= "   WHERE ";
        $query .= "     SUBCLASSCD = '00-{$model->school_kind}-00-000000' ";
        $query .= "   ORDER BY ";
        $query .= "     TYPE, ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }

    public function getRateSemes($model)
    {
        $query  = " SELECT ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     RATE ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_RATE_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->school_kind}' ";
        $query .= "     AND SEMESTER = '{$model->semeester}' ";

        return $query;
    }

    public function updateScore($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //評価対象テスト
        $query = knjd212cQuery::getAdminCtrlCalc($model);
        $result = $db->query($query);
        $adminCtrlCalc = array();
        $sakitestArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclass = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
            $sakiTestCd = $row["SEMESTER"]."-".$row["TESTKINDCD"]."-".$row["TESTITEMCD"]."-".$row["SCORE_DIV"];
            $motoTestCd = $row["MOTO_SEMESTER"]."-".$row["MOTO_TESTKINDCD"]."-".$row["MOTO_TESTITEMCD"]."-".$row["MOTO_SCORE_DIV"];
            $adminCtrlCalc[$subclass][$sakiTestCd][] = $motoTestCd;
            $sakitestArray[$sakiTestCd] = $sakiTestCd;
        }
        $result->free();

        //更新科目と対象テストのマージ
        $updSubclassInfo = array();
        if ($model->subclasscd != '000000') {
            if (array_key_exists($model->subclasscd, $adminCtrlCalc)) {
                $updSubclassInfo[$model->subclasscd] = $adminCtrlCalc[$model->subclasscd];
            } else {
                $updSubclassInfo[$model->subclasscd] = $adminCtrlCalc["00-{$model->school_kind}-00-000000"];
            }
        } else {
            foreach ($model->subclassArray as $subclass => $sakiTest) {
                if (array_key_exists($subclass, $adminCtrlCalc)) {
                    $updSubclassInfo[$subclass] = $adminCtrlCalc[$subclass];
                } else {
                    $updSubclassInfo[$subclass] = $adminCtrlCalc["00-{$model->school_kind}-00-000000"];
                }
            }
        }
        foreach ($sakitestArray as $kye => $val) {
            knjd212cQuery::delReplaceSubclass($db, $model, $kye);
        }

        foreach ($updSubclassInfo as $subclassCd => $sakiArray) {
            knjd212cQuery::delInsScore($db, $model, $subclassCd, $sakiArray);
            knjd212cQuery::delInsScoreDetail($db, $model, $subclassCd, $sakiArray);
            knjd212cQuery::insReplaceSubclass($db, $model, $subclassCd, $sakiArray);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    public function delInsScore($db, $model, $subclassCd, $sakiArray)
    {
        //評価のテストコード
        reset($sakiArray);
        $sakiTestCd = key($sakiArray);
        list($sakiSeme, $sakiTest, $sakiItem, $sakiSdiv) = explode("-", $sakiTestCd);

        $query  = " DELETE FROM RECORD_SCORE_DAT SCORE_T ";
        $query .= " WHERE ";
        $query .= "     SCORE_T.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCORE_T.SEMESTER || '-' || SCORE_T.TESTKINDCD || '-' || SCORE_T.TESTITEMCD || '-' || SCORE_T.SCORE_DIV = '{$sakiTestCd}' ";
        $query .= "     AND SCORE_T.CLASSCD || '-' || SCORE_T.SCHOOL_KIND || '-' || SCORE_T.CURRICULUM_CD || '-' || SCORE_T.SUBCLASSCD = '{$subclassCd}' ";
        $query .= "     AND EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_DAT REGD ";
        $query .= "             WHERE ";
        $query .= "                 REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "                 AND REGD.SEMESTER = '{$model->seme_sch}' ";
        $query .= "                 AND REGD.GRADE = '{$model->grade}' ";
        $query .= "                 AND SCORE_T.SCHREGNO = REGD.SCHREGNO ";
        $query .= "         ) ";

        $db->query($query);

        //評価を作る為の元テストコードカウント(中間、期末)
        $cnt = get_count($sakiArray[$sakiTestCd]);

        //元テストコードの各種SQL
        $motoTestCdSql = "";
        foreach ($sakiArray[$sakiTestCd] as $key => $motoTestCd) {
            $motoTestCdSql .= $sep.$motoTestCd;
            $sep = "','";
        }
        $query  = " INSERT INTO RECORD_SCORE_DAT ";
        $query .= " ( ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD, ";
        $query .= "     SCORE_DIV, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     CHAIRCD, ";
        $query .= "     SCORE, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " WITH SCH_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->seme_sch}' ";
        $query .= "     AND GRADE = '{$model->grade}' ";
        $query .= " ) ";
        $query .= " , SCORE_T0 AS ( ";
        $query .= " SELECT ";
        $query .= "     SCORE_T.YEAR, ";
        $query .= "     SCORE_T.CLASSCD, ";
        $query .= "     SCORE_T.SCHOOL_KIND, ";
        $query .= "     SCORE_T.CURRICULUM_CD, ";
        $query .= "     SCORE_T.SUBCLASSCD, ";
        $query .= "     SCORE_T.SCHREGNO, ";
        $query .= "     MAX(SCORE_T.CHAIRCD) AS CHAIRCD, ";
        if ($model->semeester != '9') {
            $query .= "     ROUND(FLOAT(SUM(VALUE(SCORE_T.SCORE, SUP.SCORE))) / COUNT(VALUE(SCORE_T.SCORE, SUP.SCORE)) * {$model->rateSemes[$subclassCd]} / 100, 0) AS SCORE ";
        } else {
            $query .= "     ROUND(FLOAT(SUM(VALUE(SCORE_T.SCORE, SUP.SCORE))) / COUNT(VALUE(SCORE_T.SCORE, SUP.SCORE)), 0) AS SCORE ";
        }
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT SCORE_T ";
        $query .= "     LEFT JOIN SUPP_EXA_SDIV_DAT SUP ON SUP.YEAR = SCORE_T.YEAR ";
        $query .= "         AND SUP.SEMESTER = SCORE_T.SEMESTER ";
        $query .= "         AND SUP.TESTKINDCD = SCORE_T.TESTKINDCD ";
        $query .= "         AND SUP.TESTITEMCD = SCORE_T.TESTITEMCD ";
        $query .= "         AND SUP.SCORE_DIV = SCORE_T.SCORE_DIV ";
        $query .= "         AND SUP.CLASSCD = SCORE_T.CLASSCD ";
        $query .= "         AND SUP.SCHOOL_KIND = SCORE_T.SCHOOL_KIND ";
        $query .= "         AND SUP.CURRICULUM_CD = SCORE_T.CURRICULUM_CD ";
        $query .= "         AND SUP.SUBCLASSCD = SCORE_T.SUBCLASSCD ";
        $query .= "         AND SUP.SCHREGNO = SCORE_T.SCHREGNO ";
        $query .= "         AND SUP.SCORE_DI = '2' ";
        $query .= "         AND SCORE_T.VALUE_DI IN ('*', '**') ";
        $query .= "         AND SCORE_T.SCORE IS NULL ";
        $query .= "         AND SUP.SCORE IS NOT NULL ";
        $query .= " WHERE ";
        $query .= "     SCORE_T.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCORE_T.SEMESTER || '-' || SCORE_T.TESTKINDCD || '-' || SCORE_T.TESTITEMCD || '-' || SCORE_T.SCORE_DIV IN ('{$motoTestCdSql}') ";
        $query .= "     AND SCORE_T.CLASSCD || '-' || SCORE_T.SCHOOL_KIND || '-' || SCORE_T.CURRICULUM_CD || '-' || SCORE_T.SUBCLASSCD = '{$subclassCd}' ";
        $query .= "     AND EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 SCH_T ";
        $query .= "             WHERE ";
        $query .= "                 SCH_T.SCHREGNO = SCORE_T.SCHREGNO ";
        $query .= "         ) ";
        $query .= " GROUP BY ";
        $query .= "     SCORE_T.YEAR, ";
        $query .= "     SCORE_T.CLASSCD, ";
        $query .= "     SCORE_T.SCHOOL_KIND, ";
        $query .= "     SCORE_T.CURRICULUM_CD, ";
        $query .= "     SCORE_T.SUBCLASSCD, ";
        $query .= "     SCORE_T.SCHREGNO ";
        $query .= " ) ";
        $query .= " , SCORE_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCORE_T.YEAR, ";
        $query .= "     SCORE_T.CLASSCD, ";
        $query .= "     SCORE_T.SCHOOL_KIND, ";
        $query .= "     SCORE_T.CURRICULUM_CD, ";
        $query .= "     SCORE_T.SUBCLASSCD, ";
        $query .= "     SCORE_T.SCHREGNO, ";
        $query .= "     SCORE_T.CHAIRCD, ";
        $query .= "     SCORE_T.SCORE ";
        $query .= " FROM ";
        $query .= "     SCORE_T0 SCORE_T ";
        if ($model->semeester != '9') {
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     SCORE_T.YEAR, ";
            $query .= "     SCORE_T.CLASSCD, ";
            $query .= "     SCORE_T.SCHOOL_KIND, ";
            $query .= "     SCORE_T.CURRICULUM_CD, ";
            $query .= "     SCORE_T.SUBCLASSCD, ";
            $query .= "     SCORE_T.SCHREGNO, ";
            $query .= "     SCORE_T.CHAIRCD, ";
            $query .= "     SCORE_T.SCORE ";
            $query .= " FROM ";
            $query .= "     RECORD_SCORE_DAT SCORE_T ";
            $query .= " WHERE ";
            $query .= "     SCORE_T.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SCORE_T.SEMESTER || '-' || SCORE_T.TESTKINDCD || '-' || SCORE_T.TESTITEMCD || '-' || SCORE_T.SCORE_DIV = '{$sakiSeme}-99-00-02' ";
            $query .= "     AND SCORE_T.CLASSCD || '-' || SCORE_T.SCHOOL_KIND || '-' || SCORE_T.CURRICULUM_CD || '-' || SCORE_T.SUBCLASSCD = '{$subclassCd}' ";
            $query .= "     AND EXISTS ( ";
            $query .= "             SELECT ";
            $query .= "                 'x' ";
            $query .= "             FROM ";
            $query .= "                 SCH_T ";
            $query .= "             WHERE ";
            $query .= "                 SCH_T.SCHREGNO = SCORE_T.SCHREGNO ";
            $query .= "         ) ";
            $query .= "     AND (SCORE_T.CLASSCD, SCORE_T.SCHOOL_KIND, SCORE_T.CURRICULUM_CD, SCORE_T.SUBCLASSCD, SCORE_T.SCHREGNO) IN ( ";
            $query .= "             SELECT ";
            $query .= "                 T0.CLASSCD, T0.SCHOOL_KIND, T0.CURRICULUM_CD, T0.SUBCLASSCD, T0.SCHREGNO ";
            $query .= "             FROM ";
            $query .= "                 SCORE_T0 T0 ";
            $query .= "             WHERE ";
            $query .= "                 T0.SCORE IS NOT NULL ";
            $query .= "         ) ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SCORE_T.YEAR, ";
        $query .= "     '{$sakiSeme}' AS SEMESTER, ";
        $query .= "     '{$sakiTest}' AS TESTKINDCD, ";
        $query .= "     '{$sakiItem}' AS TESTITEMCD, ";
        $query .= "     '{$sakiSdiv}' AS SCORE_DIV, ";
        $query .= "     SCORE_T.CLASSCD, ";
        $query .= "     SCORE_T.SCHOOL_KIND, ";
        $query .= "     SCORE_T.CURRICULUM_CD, ";
        $query .= "     SCORE_T.SUBCLASSCD, ";
        $query .= "     SCORE_T.SCHREGNO, ";
        $query .= "     MAX(SCORE_T.CHAIRCD) AS CHAIRCD, ";
        $query .= "     SUM(SCORE_T.SCORE) AS SCORE, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     SCORE_T ";
        $query .= " GROUP BY ";
        $query .= "     SCORE_T.YEAR, ";
        $query .= "     SCORE_T.CLASSCD, ";
        $query .= "     SCORE_T.SCHOOL_KIND, ";
        $query .= "     SCORE_T.CURRICULUM_CD, ";
        $query .= "     SCORE_T.SUBCLASSCD, ";
        $query .= "     SCORE_T.SCHREGNO ";

        $db->query($query);
    }

    public function delInsScoreDetail($db, $model, $subclassCd, $sakiArray)
    {
        //評価のテストコード
        reset($sakiArray);
        $sakiTestCd = key($sakiArray);
        list($sakiSeme, $sakiTest, $sakiItem, $sakiSdiv) = explode("-", $sakiTestCd);

        $query  = " DELETE FROM RECORD_SCORE_DETAIL_DAT SCORE_T ";
        $query .= " WHERE ";
        $query .= "     SCORE_T.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCORE_T.SEMESTER || '-' || SCORE_T.TESTKINDCD || '-' || SCORE_T.TESTITEMCD || '-' || SCORE_T.SCORE_DIV = '{$sakiTestCd}' ";
        $query .= "     AND SCORE_T.CLASSCD || '-' || SCORE_T.SCHOOL_KIND || '-' || SCORE_T.CURRICULUM_CD || '-' || SCORE_T.SUBCLASSCD = '{$subclassCd}' ";
        $query .= "     AND EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_DAT REGD ";
        $query .= "             WHERE ";
        $query .= "                 REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "                 AND REGD.SEMESTER = '{$model->seme_sch}' ";
        $query .= "                 AND REGD.GRADE = '{$model->grade}' ";
        $query .= "                 AND SCORE_T.SCHREGNO = REGD.SCHREGNO ";
        $query .= "         ) ";
        $query .= "     AND DETAIL_SEQ = '001' ";

        $db->query($query);

        //評価を作る為の元テストコードカウント(中間、期末)
        $cnt = get_count($sakiArray[$sakiTestCd]);

        //元テストコードの各種SQL
        $motoTestCdSql = "";
        foreach ($sakiArray[$sakiTestCd] as $key => $motoTestCd) {
            $motoTestCdSql .= $sep.$motoTestCd;
            $sep = "','";
        }
        $query  = " INSERT INTO RECORD_SCORE_DETAIL_DAT ";
        $query .= " ( ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD, ";
        $query .= "     SCORE_DIV, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     DETAIL_SEQ, ";
        $query .= "     DEC_VAL1, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " WITH SCH_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->seme_sch}' ";
        $query .= "     AND GRADE = '{$model->grade}' ";
        $query .= " ) ";
        $query .= " , SCORE_T0 AS ( ";
        $query .= " SELECT ";
        $query .= "     SCORE_T.YEAR, ";
        $query .= "     SCORE_T.CLASSCD, ";
        $query .= "     SCORE_T.SCHOOL_KIND, ";
        $query .= "     SCORE_T.CURRICULUM_CD, ";
        $query .= "     SCORE_T.SUBCLASSCD, ";
        $query .= "     SCORE_T.SCHREGNO, ";
        if ($model->semeester != '9') {
            $query .= "     DECIMAL(ROUND(FLOAT(SUM(VALUE(SCORE_T.SCORE, SUP.SCORE))) / COUNT(VALUE(SCORE_T.SCORE, SUP.SCORE)) * {$model->rateSemes[$subclassCd]} / 100 * 100000, 0) / 100000, 8, 5) AS SCORE ";
        } else {
            $query .= "     ROUND(FLOAT(SUM(VALUE(SCORE_T.SCORE, SUP.SCORE))) / COUNT(VALUE(SCORE_T.SCORE, SUP.SCORE)) * 10, -1) / 10 AS SCORE ";
        }
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT SCORE_T ";
        $query .= "     LEFT JOIN SUPP_EXA_SDIV_DAT SUP ON SUP.YEAR = SCORE_T.YEAR ";
        $query .= "         AND SUP.SEMESTER = SCORE_T.SEMESTER ";
        $query .= "         AND SUP.TESTKINDCD = SCORE_T.TESTKINDCD ";
        $query .= "         AND SUP.TESTITEMCD = SCORE_T.TESTITEMCD ";
        $query .= "         AND SUP.SCORE_DIV = SCORE_T.SCORE_DIV ";
        $query .= "         AND SUP.CLASSCD = SCORE_T.CLASSCD ";
        $query .= "         AND SUP.SCHOOL_KIND = SCORE_T.SCHOOL_KIND ";
        $query .= "         AND SUP.CURRICULUM_CD = SCORE_T.CURRICULUM_CD ";
        $query .= "         AND SUP.SUBCLASSCD = SCORE_T.SUBCLASSCD ";
        $query .= "         AND SUP.SCHREGNO = SCORE_T.SCHREGNO ";
        $query .= "         AND SUP.SCORE_DI = '2' ";
        $query .= "         AND SCORE_T.VALUE_DI IN ('*', '**') ";
        $query .= "         AND SCORE_T.SCORE IS NULL ";
        $query .= "         AND SUP.SCORE IS NOT NULL ";
        $query .= " WHERE ";
        $query .= "     SCORE_T.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCORE_T.SEMESTER || '-' || SCORE_T.TESTKINDCD || '-' || SCORE_T.TESTITEMCD || '-' || SCORE_T.SCORE_DIV IN ('{$motoTestCdSql}') ";
        $query .= "     AND SCORE_T.CLASSCD || '-' || SCORE_T.SCHOOL_KIND || '-' || SCORE_T.CURRICULUM_CD || '-' || SCORE_T.SUBCLASSCD = '{$subclassCd}' ";
        $query .= "     AND EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 SCH_T ";
        $query .= "             WHERE ";
        $query .= "                 SCH_T.SCHREGNO = SCORE_T.SCHREGNO ";
        $query .= "         ) ";
        $query .= " GROUP BY ";
        $query .= "     SCORE_T.YEAR, ";
        $query .= "     SCORE_T.CLASSCD, ";
        $query .= "     SCORE_T.SCHOOL_KIND, ";
        $query .= "     SCORE_T.CURRICULUM_CD, ";
        $query .= "     SCORE_T.SUBCLASSCD, ";
        $query .= "     SCORE_T.SCHREGNO ";
        $query .= " ) ";
        $query .= " , SCORE_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCORE_T.YEAR, ";
        $query .= "     SCORE_T.CLASSCD, ";
        $query .= "     SCORE_T.SCHOOL_KIND, ";
        $query .= "     SCORE_T.CURRICULUM_CD, ";
        $query .= "     SCORE_T.SUBCLASSCD, ";
        $query .= "     SCORE_T.SCHREGNO, ";
        $query .= "     SCORE_T.SCORE ";
        $query .= " FROM ";
        $query .= "     SCORE_T0 SCORE_T ";
        if ($model->semeester != '9') {
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     SCORE_T.YEAR, ";
            $query .= "     SCORE_T.CLASSCD, ";
            $query .= "     SCORE_T.SCHOOL_KIND, ";
            $query .= "     SCORE_T.CURRICULUM_CD, ";
            $query .= "     SCORE_T.SUBCLASSCD, ";
            $query .= "     SCORE_T.SCHREGNO, ";
            $query .= "     SCORE_T.SCORE ";
            $query .= " FROM ";
            $query .= "     RECORD_SCORE_DAT SCORE_T ";
            $query .= " WHERE ";
            $query .= "     SCORE_T.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SCORE_T.SEMESTER || '-' || SCORE_T.TESTKINDCD || '-' || SCORE_T.TESTITEMCD || '-' || SCORE_T.SCORE_DIV = '{$sakiSeme}-99-00-02' ";
            $query .= "     AND SCORE_T.CLASSCD || '-' || SCORE_T.SCHOOL_KIND || '-' || SCORE_T.CURRICULUM_CD || '-' || SCORE_T.SUBCLASSCD = '{$subclassCd}' ";
            $query .= "     AND EXISTS ( ";
            $query .= "             SELECT ";
            $query .= "                 'x' ";
            $query .= "             FROM ";
            $query .= "                 SCH_T ";
            $query .= "             WHERE ";
            $query .= "                 SCH_T.SCHREGNO = SCORE_T.SCHREGNO ";
            $query .= "         ) ";
            $query .= "     AND (SCORE_T.CLASSCD, SCORE_T.SCHOOL_KIND, SCORE_T.CURRICULUM_CD, SCORE_T.SUBCLASSCD, SCORE_T.SCHREGNO) IN ( ";
            $query .= "             SELECT ";
            $query .= "                 T0.CLASSCD, T0.SCHOOL_KIND, T0.CURRICULUM_CD, T0.SUBCLASSCD, T0.SCHREGNO ";
            $query .= "             FROM ";
            $query .= "                 SCORE_T0 T0 ";
            $query .= "             WHERE ";
            $query .= "                 T0.SCORE IS NOT NULL ";
            $query .= "         ) ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SCORE_T.YEAR, ";
        $query .= "     '{$sakiSeme}' AS SEMESTER, ";
        $query .= "     '{$sakiTest}' AS TESTKINDCD, ";
        $query .= "     '{$sakiItem}' AS TESTITEMCD, ";
        $query .= "     '{$sakiSdiv}' AS SCORE_DIV, ";
        $query .= "     SCORE_T.CLASSCD, ";
        $query .= "     SCORE_T.SCHOOL_KIND, ";
        $query .= "     SCORE_T.CURRICULUM_CD, ";
        $query .= "     SCORE_T.SUBCLASSCD, ";
        $query .= "     SCORE_T.SCHREGNO, ";
        $query .= "     '001' AS DETAIL_SEQ, ";
        $query .= "     SUM(SCORE_T.SCORE) AS DEC_VAL1, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     SCORE_T ";
        $query .= " GROUP BY ";
        $query .= "     SCORE_T.YEAR, ";
        $query .= "     SCORE_T.CLASSCD, ";
        $query .= "     SCORE_T.SCHOOL_KIND, ";
        $query .= "     SCORE_T.CURRICULUM_CD, ";
        $query .= "     SCORE_T.SUBCLASSCD, ";
        $query .= "     SCORE_T.SCHREGNO ";

        $db->query($query);
    }

    public function delReplaceSubclass($db, $model, $sakiTestCd)
    {
        $query  = " DELETE FROM RECORD_SCORE_DAT SCORE_T ";
        $query .= " WHERE ";
        $query .= "     SCORE_T.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCORE_T.SEMESTER || '-' || SCORE_T.TESTKINDCD || '-' || SCORE_T.TESTITEMCD || '-' || SCORE_T.SCORE_DIV = '{$sakiTestCd}' ";
        $query .= "     AND EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_DAT REGD ";
        $query .= "             WHERE ";
        $query .= "                 REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "                 AND REGD.SEMESTER = '{$model->seme_sch}' ";
        $query .= "                 AND REGD.GRADE = '{$model->grade}' ";
        $query .= "                 AND SCORE_T.SCHREGNO = REGD.SCHREGNO ";
        $query .= "         ) ";
        $query .= "     AND EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 SUBCLASS_RANK_REPLACE_DAT SUB_REP ";
        $query .= "             WHERE ";
        $query .= "                 SUB_REP.YEAR = '".CTRL_YEAR."' ";
        $query .= "                 AND SCORE_T.CLASSCD || SCORE_T.SCHOOL_KIND || SCORE_T.CURRICULUM_CD || SCORE_T.SUBCLASSCD = ";
        $query .= "                     SUB_REP.COMBINED_CLASSCD || SUB_REP.COMBINED_SCHOOL_KIND || SUB_REP.COMBINED_CURRICULUM_CD || SUB_REP.COMBINED_SUBCLASSCD ";
        $query .= "         ) ";

        $db->query($query);

        $query  = " DELETE FROM RECORD_SCORE_DETAIL_DAT SCORE_T ";
        $query .= " WHERE ";
        $query .= "     SCORE_T.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCORE_T.SEMESTER || '-' || SCORE_T.TESTKINDCD || '-' || SCORE_T.TESTITEMCD || '-' || SCORE_T.SCORE_DIV = '{$sakiTestCd}' ";
        $query .= "     AND EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_DAT REGD ";
        $query .= "             WHERE ";
        $query .= "                 REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "                 AND REGD.SEMESTER = '{$model->seme_sch}' ";
        $query .= "                 AND REGD.GRADE = '{$model->grade}' ";
        $query .= "                 AND SCORE_T.SCHREGNO = REGD.SCHREGNO ";
        $query .= "         ) ";
        $query .= "     AND EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 SUBCLASS_RANK_REPLACE_DAT SUB_REP ";
        $query .= "             WHERE ";
        $query .= "                 SUB_REP.YEAR = '".CTRL_YEAR."' ";
        $query .= "                 AND SCORE_T.CLASSCD || SCORE_T.SCHOOL_KIND || SCORE_T.CURRICULUM_CD || SCORE_T.SUBCLASSCD = ";
        $query .= "                     SUB_REP.COMBINED_CLASSCD || SUB_REP.COMBINED_SCHOOL_KIND || SUB_REP.COMBINED_CURRICULUM_CD || SUB_REP.COMBINED_SUBCLASSCD ";
        $query .= "         ) ";
        $query .= "     AND DETAIL_SEQ = '001' ";

        $db->query($query);
    }

    public function insReplaceSubclass($db, $model, $subclassCd, $sakiArray)
    {
        //評価のテストコード
        reset($sakiArray);
        $sakiTestCd = key($sakiArray);
        list($sakiSeme, $sakiTest, $sakiItem, $sakiSdiv) = explode("-", $sakiTestCd);

        $query  = " INSERT INTO RECORD_SCORE_DAT ";
        $query .= " ( ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD, ";
        $query .= "     SCORE_DIV, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     CHAIRCD, ";
        $query .= "     SCORE, ";
        $query .= "     VALUE_DI, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " WITH SCH_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->seme_sch}' ";
        $query .= "     AND GRADE = '{$model->grade}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SUB_REP.YEAR, ";
        $query .= "     '{$sakiSeme}' AS SEMESTER, ";
        $query .= "     SCORE.TESTKINDCD, ";
        $query .= "     SCORE.TESTITEMCD, ";
        $query .= "     SCORE.SCORE_DIV, ";
        $query .= "     SUB_REP.COMBINED_CLASSCD, ";
        $query .= "     SUB_REP.COMBINED_SCHOOL_KIND, ";
        $query .= "     SUB_REP.COMBINED_CURRICULUM_CD, ";
        $query .= "     SUB_REP.COMBINED_SUBCLASSCD, ";
        $query .= "     SCORE.SCHREGNO, ";
        $query .= "     SCORE.CHAIRCD, ";
        $query .= "     SCORE.SCORE, ";
        $query .= "     SCORE.VALUE_DI, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_RANK_REPLACE_DAT SUB_REP ";
        $query .= "     INNER JOIN RECORD_SCORE_DAT SCORE ON SUB_REP.YEAR = SCORE.YEAR ";
        $query .= "           AND SCORE.SEMESTER = '{$sakiSeme}' ";
        $query .= "           AND SUB_REP.ATTEND_CLASSCD = SCORE.CLASSCD ";
        $query .= "           AND SUB_REP.ATTEND_SCHOOL_KIND = SCORE.SCHOOL_KIND ";
        $query .= "           AND SUB_REP.ATTEND_CURRICULUM_CD = SCORE.CURRICULUM_CD ";
        $query .= "           AND SUB_REP.ATTEND_SUBCLASSCD = SCORE.SUBCLASSCD ";
        $query .= "           AND SCORE.SEMESTER || '-' || SCORE.TESTKINDCD || '-' || SCORE.TESTITEMCD || '-' || SCORE.SCORE_DIV = '{$sakiTestCd}' ";
        $query .= " WHERE ";
        $query .= "     SUB_REP.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SUB_REP.ATTEND_CLASSCD || '-' || SUB_REP.ATTEND_SCHOOL_KIND || '-' || SUB_REP.ATTEND_CURRICULUM_CD || '-' || SUB_REP.ATTEND_SUBCLASSCD = '{$subclassCd}' ";
        $query .= "     AND EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 SCH_T ";
        $query .= "             WHERE ";
        $query .= "                 SCORE.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "         ) ";

        $db->query($query);

        $query  = " INSERT INTO RECORD_SCORE_DETAIL_DAT ";
        $query .= " ( ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD, ";
        $query .= "     SCORE_DIV, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     DETAIL_SEQ, ";
        $query .= "     DEC_VAL1, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " WITH SCH_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->seme_sch}' ";
        $query .= "     AND GRADE = '{$model->grade}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SUB_REP.YEAR, ";
        $query .= "     '{$sakiSeme}' AS SEMESTER, ";
        $query .= "     SCORE.TESTKINDCD, ";
        $query .= "     SCORE.TESTITEMCD, ";
        $query .= "     SCORE.SCORE_DIV, ";
        $query .= "     SUB_REP.COMBINED_CLASSCD, ";
        $query .= "     SUB_REP.COMBINED_SCHOOL_KIND, ";
        $query .= "     SUB_REP.COMBINED_CURRICULUM_CD, ";
        $query .= "     SUB_REP.COMBINED_SUBCLASSCD, ";
        $query .= "     SCORE.SCHREGNO, ";
        $query .= "     SCORE.DETAIL_SEQ, ";
        $query .= "     SCORE.DEC_VAL1, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_RANK_REPLACE_DAT SUB_REP ";
        $query .= "     INNER JOIN RECORD_SCORE_DETAIL_DAT SCORE ON SUB_REP.YEAR = SCORE.YEAR ";
        $query .= "           AND SCORE.SEMESTER = '{$sakiSeme}' ";
        $query .= "           AND SUB_REP.ATTEND_CLASSCD = SCORE.CLASSCD ";
        $query .= "           AND SUB_REP.ATTEND_SCHOOL_KIND = SCORE.SCHOOL_KIND ";
        $query .= "           AND SUB_REP.ATTEND_CURRICULUM_CD = SCORE.CURRICULUM_CD ";
        $query .= "           AND SUB_REP.ATTEND_SUBCLASSCD = SCORE.SUBCLASSCD ";
        $query .= "           AND SCORE.SEMESTER || '-' || SCORE.TESTKINDCD || '-' || SCORE.TESTITEMCD || '-' || SCORE.SCORE_DIV = '{$sakiTestCd}' ";
        $query .= " WHERE ";
        $query .= "     SUB_REP.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SUB_REP.ATTEND_CLASSCD || '-' || SUB_REP.ATTEND_SCHOOL_KIND || '-' || SUB_REP.ATTEND_CURRICULUM_CD || '-' || SUB_REP.ATTEND_SUBCLASSCD = '{$subclassCd}' ";
        $query .= "     AND EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 SCH_T ";
        $query .= "             WHERE ";
        $query .= "                 SCORE.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "         ) ";

        $db->query($query);
    }

    /* 実行履歴 */
    public function executeRireki($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //実行日付・時間を取得
        $calcRow = $db->getRow(knjd212cQuery::getCalcDateTime(), DB_FETCHMODE_ASSOC);
        $calcDate = $calcRow["CALC_DATE"];//実行日付
        $calcTime = $calcRow["CALC_TIME"];//実行時間
        //実行履歴データ・追加
        $query = knjd212cQuery::getInsertRireki($calcDate, $calcTime, $model);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //実行日付・時間を取得
    public function getCalcDateTime()
    {
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
    public function getInsertRireki($calcDate, $calcTime, $model)
    {
        $data = array();
        $data["CALC_DATE"][TEXT]            = $calcDate;
        $data["CALC_TIME"][TEXT]            = $calcTime;

        //処理科目(全て または 総合計のみ)
        if ($model->subclasscd == '000000') {
            //ダミーデータセット(00)
            $data["CLASSCD"][TEXT]          = "00";
            $data["SCHOOL_KIND"][TEXT]      = $model->school_kind;
            $data["CURRICULUM_CD"][TEXT]    = "00";
            $data["SUBCLASSCD"][TEXT]       = $model->subclasscd;
        //処理科目(各科目のみ)
        } elseif ($model->subclasscd != '') {
            $subArray = array();
            $subArray = explode("-", $model->subclasscd);
            $data["CLASSCD"][TEXT]          = $subArray[0];
            $data["SCHOOL_KIND"][TEXT]      = $subArray[1];
            $data["CURRICULUM_CD"][TEXT]    = $subArray[2];
            $data["SUBCLASSCD"][TEXT]       = $subArray[3];
        }

        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SEMESTER"][TEXT]             = $model->semeester;
        $data["GRADE"][TEXT]                = $model->grade;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $query = Query::insertSQL($data, "RECORD_SCORE_EXEC_DAT");

        return $query;
    }
}
?>
