<?php

require_once('for_php7.php');


class knjd212dQuery extends Query {

    //処理学期
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";
        return $query;
    }

    //処理学年
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     AND SCHOOL_KIND = 'J' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年評定処理学年
    function getGradeSql($model) {
        $query  = " SELECT ";
        $query .= "     GRADE ";
        $query .= "   , SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    /* 実行 */
    function ExecuteQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->field["RECORD_DIV"] == "1") {
            // 学期評定更新
            $query = knjd212dQuery::getDeleteQuery($model, $model->grade);
            $db->query($query);

            $query = knjd212dQuery::getGakkiHyouteiQuery($model);
            knjd212dQuery::insertRows($model, $db, $query);

        } else if ($model->field["RECORD_DIV"] == "2") {

            // 学年評定更新
            $query = knjd212dQuery::getGradeSql($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $grade = $row["GRADE"];

                // 高校の場合、評定マスタ(絶対評定)の作成
                if ($row["SCHOOL_KIND"] == 'H') {
                    // 評定マスタ(絶対評定)の作成
                    knjd212dQuery::createAssessLevelSdivMst($model, $db, $grade);
                }
                // 成績データ削除
                $query = knjd212dQuery::getDeleteQuery($model, $grade);
                $db->query($query);
                // 成績データ取得
                $query = knjd212dQuery::getGakunenHyouteiQuery($model, $grade); 
                knjd212dQuery::insertRows($model, $db, $query);
            }
            $result->free();
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    // 挿入
    function insertRows($model, $db, $query) {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclasscd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
            $schregno = $row["SCHREGNO"];
            $assesslevel = $row["ASSESSLEVEL"];
            $query = knjd212dQuery::getInsertQuery($model, $subclasscd, $schregno, $assesslevel);
            $db->query($query);
        }
        $result->free();
    }

    //
    function getInsertQuery($model, $subclasscd, $schregno, $score) {
        list($classcd, $schoolKind, $curriculumCd, $subclasscd) = explode("-", $subclasscd);
        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $model->test["SEMESTER"];
        $data["TESTKINDCD"][TEXT]       = $model->test["TESTKINDCD"];
        $data["TESTITEMCD"][TEXT]       = $model->test["TESTITEMCD"];
        $data["SCORE_DIV"][TEXT]        = $model->test["SCORE_DIV"];
        $data["CLASSCD"][TEXT]          = $classcd;
        $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
        $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
        $data["SUBCLASSCD"][TEXT]       = $subclasscd;
        $data["SCHREGNO"][TEXT]         = $schregno;
        $data["SCORE"][NUMBER]          = $score;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "RECORD_SCORE_DAT");

        return $query;
    }

    //削除
    function getDeleteQuery($model, $grade) {
        $query  = " DELETE FROM ";
        $query .= "     RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->test["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD = '{$model->test["TESTKINDCD"]}' ";
        $query .= "     AND TESTITEMCD = '{$model->test["TESTITEMCD"]}' ";
        $query .= "     AND SCORE_DIV = '{$model->test["SCORE_DIV"]}' ";
        $query .= "     AND SCHREGNO IN (  ";
        $query .= "         SELECT SCHREGNO ";
        $query .= "         FROM SCHREG_REGD_DAT ";
        $query .= "         WHERE ";
        $query .= "              YEAR = '".CTRL_YEAR."' ";
        if ($model->field["RECORD_DIV"] == "1") {
            // 学期指定
            $query .= "          AND SEMESTER = '".$model->semester."' ";
        } else {
            $query .= "          AND SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query .= "          AND GRADE = '".$grade."' ";
        $query .= "          )  ";

        return $query;
    }

    // 中学は絶対評定マスタ、高校は相対評定マスタ（条件付きで絶対評定マスタ）で学年評定を更新する
    function getGakunenHyouteiQuery($model, $grade) {
        $query  = " WITH SUBC_SCORE_COUNT AS (";
        $query .= "   SELECT ";
        $query .= "         T1.CLASSCD ";
        $query .= "       , T1.SCHOOL_KIND ";
        $query .= "       , T1.CURRICULUM_CD ";
        $query .= "       , T1.SUBCLASSCD ";
        $query .= "       , COUNT(T1.SCORE) AS COUNT ";
        $query .= "   FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON";
        $query .= "             T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "         AND T1.YEAR = REGD.YEAR ";
        $query .= "         AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND REGD.GRADE = '".$grade."' ";
        $query .= "   WHERE ";
        $query .= "       T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND T1.SEMESTER = '{$model->test["SEMESTER"]}' ";
        $query .= "       AND T1.TESTKINDCD = '{$model->test["TESTKINDCD"]}' ";
        $query .= "       AND T1.TESTITEMCD = '{$model->test["TESTITEMCD"]}' ";
        $query .= "       AND T1.SCORE_DIV = '08' ";
        $query .= "   GROUP BY ";
        $query .= "         T1.CLASSCD ";
        $query .= "       , T1.SCHOOL_KIND ";
        $query .= "       , T1.CURRICULUM_CD ";
        $query .= "       , T1.SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "       T1.YEAR ";
        $query .= "     , T1.CLASSCD ";
        $query .= "     , T1.SCHOOL_KIND ";
        $query .= "     , T1.CURRICULUM_CD ";
        $query .= "     , T1.SUBCLASSCD ";
        $query .= "     , T1.SCHREGNO ";
        $query .= "     , T1.SCORE ";
        $query .= "     , GAVG.AVG AS GRADE_AVG ";
        $query .= "     , CASE ";
        $query .= "         WHEN GDAT.SCHOOL_KIND = 'J' THEN VALUE(ALSMA.ASSESSLEVEL, ALSM00.ASSESSLEVEL) "; // 絶対評価
        $query .= "         WHEN GDAT.SCHOOL_KIND = 'H' THEN ";
        $query .= "           CASE ";
        // 平均点が60点未満である場合、その科目の点数に2を掛けて平均点未満の場合は、評定が"1"となる。
        $query .= "             WHEN GAVG.AVG < 60 AND T1.SCORE * 2 < GAVG.AVG THEN 1 ";
        $query .= "             ELSE VALUE(ALSMA.ASSESSLEVEL, ALSM00.ASSESSLEVEL) "; // 絶対評価
        $query .= "           END ";
        $query .= "         END ";
        $query .= "       AS ASSESSLEVEL ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON";
        $query .= "             T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "         AND T1.YEAR = REGD.YEAR ";
        $query .= "         AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND REGD.GRADE = '".$grade."' ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON";
        $query .= "             T1.YEAR = GDAT.YEAR ";
        $query .= "         AND REGD.GRADE = GDAT.GRADE ";
        $query .= "     INNER JOIN SUBC_SCORE_COUNT SCC ON";
        $query .= "             T1.CLASSCD = SCC.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = SCC.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = SCC.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD = SCC.SUBCLASSCD ";
        $query .= "     LEFT JOIN RECORD_AVERAGE_SDIV_DAT GAVG ON";
        $query .= "             T1.YEAR = GAVG.YEAR ";
        $query .= "         AND T1.SEMESTER = GAVG.SEMESTER ";
        $query .= "         AND T1.TESTKINDCD = GAVG.TESTKINDCD ";
        $query .= "         AND T1.TESTITEMCD = GAVG.TESTITEMCD ";
        $query .= "         AND T1.SCORE_DIV = GAVG.SCORE_DIV ";
        $query .= "         AND T1.CLASSCD = GAVG.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = GAVG.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = GAVG.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD = GAVG.SUBCLASSCD ";
        $query .= "         AND GAVG.AVG_DIV = '1' ";
        $query .= "         AND GAVG.GRADE = REGD.GRADE ";
        $query .= "     LEFT JOIN ASSESS_LEVEL_SDIV_MST ALSMA ON";
        $query .= "             ALSMA.YEAR = T1.YEAR ";
        $query .= "         AND ALSMA.SEMESTER = '".$model->test["SEMESTER"]."'";
        $query .= "         AND ALSMA.TESTKINDCD = '".$model->test["TESTKINDCD"]."' ";
        $query .= "         AND ALSMA.TESTITEMCD = '".$model->test["TESTITEMCD"]."' ";
        $query .= "         AND ALSMA.SCORE_DIV = '".$model->test["SCORE_DIV"]."' ";
        $query .= "         AND T1.CLASSCD = ALSMA.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = ALSMA.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = ALSMA.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD = ALSMA.SUBCLASSCD ";
        $query .= "         AND ALSMA.RUISEKI_DIV = 'A' "; // 絶対評価
        $query .= "         AND ALSMA.DIV = '1' ";
        $query .= "         AND ALSMA.GRADE = REGD.GRADE ";
        $query .= "         AND ALSMA.HR_CLASS = '000' ";
        $query .= "         AND ALSMA.COURSECD = '0' ";
        $query .= "         AND ALSMA.MAJORCD= '000' ";
        $query .= "         AND ALSMA.COURSECODE = '0000' ";
        $query .= "         AND T1.SCORE BETWEEN ALSMA.ASSESSLOW AND ALSMA.ASSESSHIGH ";

        // 基本参照（評定マスタが登録されていない時に使用）
        $query .= "     LEFT JOIN ASSESS_LEVEL_SDIV_MST ALSM00 ON";
        $query .= "             ALSM00.YEAR = T1.YEAR ";
        $query .= "         AND ALSM00.SEMESTER = '".$model->test["SEMESTER"]."'";
        $query .= "         AND ALSM00.TESTKINDCD = '".$model->test["TESTKINDCD"]."' ";
        $query .= "         AND ALSM00.TESTITEMCD = '".$model->test["TESTITEMCD"]."' ";
        $query .= "         AND ALSM00.SCORE_DIV = '".$model->test["SCORE_DIV"]."' ";
        $query .= "         AND ALSM00.CLASSCD = '00' ";
        $query .= "         AND ALSM00.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "         AND ALSM00.CURRICULUM_CD = '00' ";
        $query .= "         AND ALSM00.SUBCLASSCD = '000000' ";
        $query .= "         AND ALSM00.RUISEKI_DIV = 'A' ";
        $query .= "         AND ALSM00.DIV = '1' ";
        $query .= "         AND ALSM00.GRADE = REGD.GRADE ";
        $query .= "         AND ALSM00.HR_CLASS = '000' ";
        $query .= "         AND ALSM00.COURSECD = '0' ";
        $query .= "         AND ALSM00.MAJORCD= '000' ";
        $query .= "         AND ALSM00.COURSECODE = '0000' ";
        $query .= "         AND T1.SCORE BETWEEN ALSM00.ASSESSLOW AND ALSM00.ASSESSHIGH ";

        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$model->test["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD = '{$model->test["TESTKINDCD"]}' ";
        $query .= "     AND T1.TESTITEMCD = '{$model->test["TESTITEMCD"]}' ";
        $query .= "     AND T1.SCORE_DIV = '08' ";
        return $query;
    }

    // （中学のみ）絶対評定マスタで学期評価を換算する
    function getGakkiHyouteiQuery($model) {

        $query  = " SELECT ";
        $query .= "       T1.YEAR ";
        $query .= "     , T1.CLASSCD ";
        $query .= "     , T1.SCHOOL_KIND ";
        $query .= "     , T1.CURRICULUM_CD ";
        $query .= "     , T1.SUBCLASSCD ";
        $query .= "     , T1.SCHREGNO ";
        $query .= "     , T1.SCORE ";
        $query .= "     , VALUE(ALSM.ASSESSLEVEL, ALSM00.ASSESSLEVEL) ASSESSLEVEL ";

        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON";
        $query .= "             T1.YEAR = REGD.YEAR ";
        $query .= "         AND T1.SEMESTER = REGD.SEMESTER ";
        $query .= "         AND T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "         AND REGD.GRADE = '".$model->grade."' ";

        //評定マスタ(絶対評価)から SCORE の ASSESSLEVEL を取得
        $query .= "     LEFT JOIN ASSESS_LEVEL_SDIV_MST ALSM ON";
        $query .= "             ALSM.YEAR = T1.YEAR ";
        $query .= "         AND ALSM.SEMESTER = '".$model->test["SEMESTER"]."'";
        $query .= "         AND ALSM.TESTKINDCD = '".$model->test["TESTKINDCD"]."' ";
        $query .= "         AND ALSM.TESTITEMCD = '".$model->test["TESTITEMCD"]."' ";
        $query .= "         AND ALSM.SCORE_DIV = '".$model->test["SCORE_DIV"]."' ";
        $query .= "         AND T1.CLASSCD = ALSM.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = ALSM.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = ALSM.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD = ALSM.SUBCLASSCD ";
        $query .= "         AND ALSM.RUISEKI_DIV = 'A' ";
        $query .= "         AND ALSM.DIV = '1' ";
        $query .= "         AND ALSM.GRADE = REGD.GRADE ";
        $query .= "         AND ALSM.HR_CLASS = '000' ";
        $query .= "         AND ALSM.COURSECD = '0' ";
        $query .= "         AND ALSM.MAJORCD= '000' ";
        $query .= "         AND ALSM.COURSECODE = '0000' ";
        $query .= "         AND T1.SCORE BETWEEN ALSM.ASSESSLOW AND ALSM.ASSESSHIGH ";

        // 基本参照（評定マスタが登録されていない時に使用）
        $query .= "     LEFT JOIN ASSESS_LEVEL_SDIV_MST ALSM00 ON";
        $query .= "             ALSM00.YEAR = T1.YEAR ";
        $query .= "         AND ALSM00.SEMESTER = '".$model->test["SEMESTER"]."'";
        $query .= "         AND ALSM00.TESTKINDCD = '".$model->test["TESTKINDCD"]."' ";
        $query .= "         AND ALSM00.TESTITEMCD = '".$model->test["TESTITEMCD"]."' ";
        $query .= "         AND ALSM00.SCORE_DIV = '".$model->test["SCORE_DIV"]."' ";
        $query .= "         AND ALSM00.CLASSCD = '00' ";
        $query .= "         AND ALSM00.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "         AND ALSM00.CURRICULUM_CD = '00' ";
        $query .= "         AND ALSM00.SUBCLASSCD = '000000' ";
        $query .= "         AND ALSM00.RUISEKI_DIV = 'A' ";
        $query .= "         AND ALSM00.DIV = '1' ";
        $query .= "         AND ALSM00.GRADE = REGD.GRADE ";
        $query .= "         AND ALSM00.HR_CLASS = '000' ";
        $query .= "         AND ALSM00.COURSECD = '0' ";
        $query .= "         AND ALSM00.MAJORCD= '000' ";
        $query .= "         AND ALSM00.COURSECODE = '0000' ";
        $query .= "         AND T1.SCORE BETWEEN ALSM00.ASSESSLOW AND ALSM00.ASSESSHIGH ";

        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$model->test["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD = '{$model->test["TESTKINDCD"]}' ";
        $query .= "     AND T1.TESTITEMCD = '{$model->test["TESTITEMCD"]}' ";
        $query .= "     AND T1.SCORE_DIV = '08' ";
        return $query;
    }


    //評定段階マスタ(絶対評定)を作成する
    function createAssessLevelSdivMst($model, $db, $grade) {

        // 評定段階マスタ(相対評定)を科目毎に取得
        $query = knjd212dQuery::getAssesLevelSubclass($model, $grade);
        $assResult = $db->query($query);
        while ($assRow = $assResult->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclass = $assRow["CLASSCD"]."-".$assRow["SCHOOL_KIND"]."-".$assRow["CURRICULUM_CD"]."-".$assRow["SUBCLASSCD"];

            // 評定段階マスタ(相対評定)がない場合は作成しない
            if (!strlen($assRow["ASS_SUB_FLG"])) {
                continue;
            }
            // 人数が10人以下の場合は作成しない
            if ($assRow["STD_CNT"] <= 10) {
                continue;
            }
            // 評定段階マスタ(絶対評定)を削除
            $query = knjd212dQuery::deleteAssessLevelSdivMstQuery($model, $grade, $subclass);
            $db->query($query);

            //最高点数
            $maxScore = 100;
            $rateCount = 0;
            // 指定した科目の評定段階マスタ(相対評定)を取得
            $query = knjd212dQuery::getSubclassTotalCnt($model, $grade, $subclass, $assRow["ASS_SUB_FLG"]);
            $assResult2 = $db->query($query);
            while ($assRow2 = $assResult2->fetchRow(DB_FETCHMODE_ASSOC)) {
                //評定段階毎の人数(累積)
                $rateCount += intval($assRow2["RATE_CNT"]);
                //平均点/2(四捨五入)を評定１の点数へ設定 ※平均点が60点以上の場合は29点を設定
                $level1Score = round($assRow2["AVG"] / 2);
                if ($assRow2["AVG"] >= 60) {
                    $level1Score = 29;
                }

                //評定段階毎の最低点数取得
                $query = knjd212dQuery::getRecordScoreDat($model, $grade, $subclass, $rateCount);
                $minRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

                $minScore = intval($minRow["SCORE_MIN"]);
                //評定段階が「２」の場合
                $assessLevel = $assRow2["ASSESSLEVEL"];
                if ($assessLevel == "2") {
                    //評定２の場合、平均点/2(四捨五入)を最低点数へ設定
                    $minScore = $level1Score + 1;
                }
                //最低点数が評定１の点数より低い場合は評定１点数＋１点を設定
                if ($minScore <= $level1Score) {
                    $minScore = $level1Score + 1;
                }

                // 評定段階マスタ(絶対評定)を追加
                $query = knjd212dQuery::insertAssessLevelSdivMstQuery($model, $grade, $subclass, $assessLevel, $minScore, $maxScore);
                $db->query($query);
                // 最高点数を変更
                $maxScore = intval($minScore) - 1;
            }
            $assResult2->free();
            // 評定段階マスタ(絶対評定：評定１)を追加
            $query = knjd212dQuery::insertAssessLevelSdivMstQuery($model, $grade, $subclass, "1", 0, $maxScore);
            $db->query($query);
        }
        $assResult->free();

    }

    //評定段階マスタ(絶対評定)を削除
    function deleteAssessLevelSdivMstQuery($model, $grade, $subclass) {
        list($classcd, $schoolKind, $curriculumCd, $subclasscd) = explode("-", $subclass);

        $query  = " DELETE FROM ";
        $query .= "     ASSESS_LEVEL_SDIV_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->test["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD = '{$model->test["TESTKINDCD"]}' ";
        $query .= "     AND TESTITEMCD = '{$model->test["TESTITEMCD"]}' ";
        $query .= "     AND SCORE_DIV = '{$model->test["SCORE_DIV"]}' ";
        $query .= "     AND CLASSCD = '{$classcd}' ";
        $query .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "     AND CURRICULUM_CD = '{$curriculumCd}' ";
        $query .= "     AND SUBCLASSCD = '{$subclasscd}' ";
        $query .= "     AND RUISEKI_DIV = 'A' ";
        $query .= "     AND DIV = '1' ";
        $query .= "     AND GRADE = '{$grade}' ";
        $query .= "     AND HR_CLASS = '000' ";
        $query .= "     AND COURSECD = '0' ";
        $query .= "     AND MAJORCD = '000' ";
        $query .= "     AND COURSECODE = '0000' ";

        return $query;
    }

    //評定段階マスタ(絶対評定)を追加
    function insertAssessLevelSdivMstQuery($model, $grade, $subclass, $assessLevel, $minScore, $maxScore) {

        list($classcd, $schoolKind, $curriculumCd, $subclasscd) = explode("-", $subclass);

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $model->test["SEMESTER"];
        $data["TESTKINDCD"][TEXT]       = $model->test["TESTKINDCD"];
        $data["TESTITEMCD"][TEXT]       = $model->test["TESTITEMCD"];
        $data["SCORE_DIV"][TEXT]        = $model->test["SCORE_DIV"];

        $data["CLASSCD"][TEXT]          = $classcd;
        $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
        $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
        $data["SUBCLASSCD"][TEXT]       = $subclasscd;

        $data["RUISEKI_DIV"][TEXT]      = "A";
        $data["DIV"][TEXT]              = "1";
        $data["GRADE"][TEXT]            = $grade;
        $data["HR_CLASS"][TEXT]         = "000";
        $data["COURSECD"][TEXT]         = "0";
        $data["MAJORCD"][TEXT]          = "000";
        $data["COURSECODE"][TEXT]       = "0000";

        $data["ASSESSLEVEL"][TEXT]      = $assessLevel;
        $data["ASSESSMARK"][TEXT]       = $assessLevel;

        $data["ASSESSLOW"][TEXT]        = $minScore;
        $data["ASSESSHIGH"][TEXT]       = $maxScore;

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "ASSESS_LEVEL_SDIV_MST");

        return $query;
    }


    //指定科目で指定した件数の最低点数を取得
    function getRecordScoreDat($model, $grade, $subclass, $rateCount) {
        list($classcd, $schoolKind, $curriculumCd, $subclasscd) = explode("-", $subclass);

        $query = "";
        $query .= " WITH REC_SCORE AS ( ";
        $query .= "   SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.CLASSCD ";
        $query .= "     , T1.SCHOOL_KIND ";
        $query .= "     , T1.CURRICULUM_CD ";
        $query .= "     , T1.SUBCLASSCD ";
        $query .= "     , REGD.GRADE ";
        $query .= "     , T1.SCORE ";
        $query .= "   FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "       ON T1.YEAR = REGD.YEAR ";
        $query .= "       AND T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "       AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "       AND REGD.GRADE = '{$grade}' ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$model->test["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD = '{$model->test["TESTKINDCD"]}' ";
        $query .= "     AND T1.TESTITEMCD = '{$model->test["TESTITEMCD"]}' ";
        $query .= "     AND T1.SCORE_DIV = '08' ";
        $query .= "     AND T1.CLASSCD = '{$classcd}' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$curriculumCd}' ";
        $query .= "     AND T1.SUBCLASSCD = '{$subclasscd}' ";
        $query .= "   ORDER BY ";
        $query .= "     T1.SCORE DESC FETCH FIRST {$rateCount} ROWS ONLY ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MIN(SCORE) SCORE_MIN ";
        $query .= "   , MAX(SCORE) SCORE_MAX ";
        $query .= " FROM ";
        $query .= "   REC_SCORE ";

        return $query;
    }

    // 評定段階マスタ(相対評定)を取得
    function getAssesLevelSubclass($model, $grade) {

        $query = "";
        $query .= " WITH STDCNT AS ( ";
        $query .= "   SELECT ";
        $query .= "       CHR.YEAR ";
        $query .= "     , CHR.CLASSCD ";
        $query .= "     , CHR.SCHOOL_KIND ";
        $query .= "     , CHR.CURRICULUM_CD ";
        $query .= "     , CHR.SUBCLASSCD ";
        $query .= "     , COUNT(STD.SCHREGNO) STD_CNT ";
        $query .= "   FROM ";
        $query .= "     CHAIR_DAT CHR ";
        $query .= "     INNER JOIN CHAIR_STD_DAT STD ";
        $query .= "       ON STD.YEAR = CHR.YEAR ";
        $query .= "       AND STD.SEMESTER = CHR.SEMESTER ";
        $query .= "       AND STD.CHAIRCD = CHR.CHAIRCD ";
        $query .= "       AND '".CTRL_DATE."' BETWEEN STD.APPDATE AND VALUE (STD.APPENDDATE, '9999-12-31') ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "          ON REGD.SCHREGNO = STD.SCHREGNO ";
        $query .= "         AND REGD.YEAR = STD.YEAR ";
        $query .= "         AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND REGD.GRADE = '".$grade."' ";
        $query .= "   WHERE ";
        $query .= "     CHR.YEAR = '".CTRL_YEAR."' ";
        $query .= "   GROUP BY ";
        $query .= "       CHR.YEAR ";
        $query .= "     , CHR.CLASSCD ";
        $query .= "     , CHR.SCHOOL_KIND ";
        $query .= "     , CHR.CURRICULUM_CD ";
        $query .= "     , CHR.SUBCLASSCD ";
        $query .= " ) ";
        $query .= " , REC_SCORE AS ( ";
        $query .= "   SELECT ";
        $query .= "       T1.YEAR ";
        $query .= "     , T1.CLASSCD ";
        $query .= "     , T1.SCHOOL_KIND ";
        $query .= "     , T1.CURRICULUM_CD ";
        $query .= "     , T1.SUBCLASSCD ";
        $query .= "   FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "       ON T1.YEAR = REGD.YEAR ";
        $query .= "       AND T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "       AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "       AND REGD.GRADE = '{$grade}' ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$model->test["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD = '{$model->test["TESTKINDCD"]}' ";
        $query .= "     AND T1.TESTITEMCD = '{$model->test["TESTITEMCD"]}' ";
        $query .= "     AND T1.SCORE_DIV = '08' ";
        $query .= "   GROUP BY ";
        $query .= "       T1.YEAR ";
        $query .= "     , T1.CLASSCD ";
        $query .= "     , T1.SCHOOL_KIND ";
        $query .= "     , T1.CURRICULUM_CD ";
        $query .= "     , T1.SUBCLASSCD ";
        $query .= " ) ";
        $query .= " , ASSESSCNT AS ( ";
        $query .= "   SELECT ";
        $query .= "       ALSMR.YEAR ";
        $query .= "     , ALSMR.CLASSCD ";
        $query .= "     , ALSMR.SCHOOL_KIND ";
        $query .= "     , ALSMR.CURRICULUM_CD ";
        $query .= "     , ALSMR.SUBCLASSCD ";
        $query .= "     , COUNT(ALSMR.ASSESSLEVEL) AS ASSESS_CNT ";
        $query .= "   FROM ";
        $query .= "     ASSESS_LEVEL_SDIV_MST ALSMR ";
        $query .= "   WHERE ";
        $query .= "     ALSMR.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND ALSMR.SEMESTER = '{$model->test["SEMESTER"]}' ";
        $query .= "     AND ALSMR.TESTKINDCD = '{$model->test["TESTKINDCD"]}' ";
        $query .= "     AND ALSMR.TESTITEMCD = '{$model->test["TESTITEMCD"]}' ";
        $query .= "     AND ALSMR.SCORE_DIV = '{$model->test["SCORE_DIV"]}' ";
        $query .= "     AND ALSMR.RUISEKI_DIV = 'R' ";
        $query .= "     AND ALSMR.DIV = '1' ";
        $query .= "     AND ALSMR.GRADE = '{$grade}' ";
        $query .= "     AND ALSMR.HR_CLASS = '000' ";
        $query .= "     AND ALSMR.COURSECD = '0' ";
        $query .= "     AND ALSMR.MAJORCD= '000' ";
        $query .= "     AND ALSMR.COURSECODE = '0000' ";
        $query .= "   GROUP BY ";
        $query .= "       ALSMR.YEAR ";
        $query .= "     , ALSMR.CLASSCD ";
        $query .= "     , ALSMR.SCHOOL_KIND ";
        $query .= "     , ALSMR.CURRICULUM_CD ";
        $query .= "     , ALSMR.SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     STD.CLASSCD ";
        $query .= "   , STD.SCHOOL_KIND ";
        $query .= "   , STD.CURRICULUM_CD ";
        $query .= "   , STD.SUBCLASSCD ";
        $query .= "   , STD.STD_CNT ";
        $query .= "   , CASE WHEN 0 < ALSMR.ASSESS_CNT THEN 'SUB' ";
        $query .= "          WHEN 0 < ALSM0.ASSESS_CNT THEN 'KIHON' ";
        $query .= "     END AS ASS_SUB_FLG ";
        $query .= " FROM ";
        $query .= "   STDCNT STD ";
        $query .= "   INNER JOIN REC_SCORE REC ";
        $query .= "     ON REC.YEAR = STD.YEAR ";
        $query .= "     AND REC.CLASSCD = STD.CLASSCD ";
        $query .= "     AND REC.SCHOOL_KIND = STD.SCHOOL_KIND ";
        $query .= "     AND REC.CURRICULUM_CD = STD.CURRICULUM_CD ";
        $query .= "     AND REC.SUBCLASSCD = STD.SUBCLASSCD ";
        $query .= "   LEFT JOIN ASSESSCNT ALSMR ";
        $query .= "     ON ALSMR.YEAR = STD.YEAR ";
        $query .= "     AND ALSMR.CLASSCD = STD.CLASSCD ";
        $query .= "     AND ALSMR.SCHOOL_KIND = STD.SCHOOL_KIND ";
        $query .= "     AND ALSMR.CURRICULUM_CD = STD.CURRICULUM_CD ";
        $query .= "     AND ALSMR.SUBCLASSCD = STD.SUBCLASSCD ";
        $query .= "   LEFT JOIN ASSESSCNT ALSM0 ";
        $query .= "     ON ALSM0.YEAR = STD.YEAR ";
        $query .= "     AND ALSM0.CLASSCD = '00' ";
        $query .= "     AND ALSM0.SCHOOL_KIND = STD.SCHOOL_KIND ";
        $query .= "     AND ALSM0.CURRICULUM_CD = '00' ";
        $query .= "     AND ALSM0.SUBCLASSCD = '000000' ";
        $query .= " WHERE ";
        $query .= "   0 < ALSMR.ASSESS_CNT OR 0 < ALSM0.ASSESS_CNT ";

        return $query;
    }

    // 指定した科目の評定段階マスタ(相対評定)を取得
    function getSubclassTotalCnt($model, $grade, $subclass, $assSubFlg) {
        list($classcd, $schoolKind, $curriculumCd, $subclasscd) = explode("-", $subclass);

        $query = "";
        $query .= " WITH STDCNT AS ( ";
        $query .= "   SELECT ";
        $query .= "     CHR.YEAR ";
        $query .= "     , CHR.CLASSCD ";
        $query .= "     , CHR.SCHOOL_KIND ";
        $query .= "     , CHR.CURRICULUM_CD ";
        $query .= "     , CHR.SUBCLASSCD ";
        $query .= "     , COUNT(STD.SCHREGNO) STD_CNT ";
        $query .= "   FROM ";
        $query .= "     CHAIR_DAT CHR ";
        $query .= "     INNER JOIN CHAIR_STD_DAT STD ";
        $query .= "       ON STD.YEAR = CHR.YEAR ";
        $query .= "       AND STD.SEMESTER = CHR.SEMESTER ";
        $query .= "       AND STD.CHAIRCD = CHR.CHAIRCD ";
        $query .= "       AND '".CTRL_DATE."' BETWEEN STD.APPDATE AND VALUE (STD.APPENDDATE, '9999-12-31') ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "          ON REGD.SCHREGNO = STD.SCHREGNO ";
        $query .= "         AND REGD.YEAR = STD.YEAR ";
        $query .= "         AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND REGD.GRADE = '".$grade."' ";
        $query .= "   WHERE ";
        $query .= "     CHR.YEAR = '".CTRL_YEAR."' ";
        $query .= "   GROUP BY ";
        $query .= "       CHR.YEAR ";
        $query .= "     , CHR.CLASSCD ";
        $query .= "     , CHR.SCHOOL_KIND ";
        $query .= "     , CHR.CURRICULUM_CD ";
        $query .= "     , CHR.SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     ALSMR.ASSESSLEVEL ";
        $query .= "   , RASD.AVG ";
        // 相対評定の割合人数は切り上げ
        $query .= "   , TRUNCATE((STD.STD_CNT * (ALSMR.PERCENT / 100) + 0.99), 0) RATE_CNT ";
        $query .= " FROM ";
        $query .= "   STDCNT STD ";
        $query .= "   INNER JOIN ASSESS_LEVEL_SDIV_MST ALSMR ";
        $query .= "     ON ALSMR.YEAR = STD.YEAR ";
        $query .= "     AND ALSMR.SEMESTER = '{$model->test["SEMESTER"]}' ";
        $query .= "     AND ALSMR.TESTKINDCD = '{$model->test["TESTKINDCD"]}' ";
        $query .= "     AND ALSMR.TESTITEMCD = '{$model->test["TESTITEMCD"]}' ";
        $query .= "     AND ALSMR.SCORE_DIV = '{$model->test["SCORE_DIV"]}' ";
        $query .= "     AND ALSMR.RUISEKI_DIV = 'R' ";
        $query .= "     AND ALSMR.DIV = '1' ";
        $query .= "     AND ALSMR.GRADE = '{$grade}' ";
        $query .= "     AND ALSMR.HR_CLASS = '000' ";
        $query .= "     AND ALSMR.COURSECD = '0' ";
        $query .= "     AND ALSMR.MAJORCD= '000' ";
        $query .= "     AND ALSMR.COURSECODE = '0000' ";
        if ($assSubFlg == 'SUB') {
            $query .= "     AND ALSMR.CLASSCD = STD.CLASSCD ";
            $query .= "     AND ALSMR.SCHOOL_KIND = STD.SCHOOL_KIND ";
            $query .= "     AND ALSMR.CURRICULUM_CD = STD.CURRICULUM_CD ";
            $query .= "     AND ALSMR.SUBCLASSCD = STD.SUBCLASSCD ";
        } else {
            $query .= "     AND ALSMR.CLASSCD = '00' ";
            $query .= "     AND ALSMR.SCHOOL_KIND = STD.SCHOOL_KIND ";
            $query .= "     AND ALSMR.CURRICULUM_CD = '00' ";
            $query .= "     AND ALSMR.SUBCLASSCD = '000000' ";
        }
        $query .= "   LEFT JOIN RECORD_AVERAGE_SDIV_DAT RASD ";
        $query .= "     ON RASD.YEAR = ALSMR.YEAR ";
        $query .= "     AND RASD.SEMESTER = ALSMR.SEMESTER ";
        $query .= "     AND RASD.TESTKINDCD = ALSMR.TESTKINDCD ";
        $query .= "     AND RASD.TESTITEMCD = ALSMR.TESTITEMCD ";
        $query .= "     AND RASD.SCORE_DIV = '08' ";
        $query .= "     AND RASD.CLASSCD = STD.CLASSCD ";
        $query .= "     AND RASD.SCHOOL_KIND = STD.SCHOOL_KIND ";
        $query .= "     AND RASD.CURRICULUM_CD = STD.CURRICULUM_CD ";
        $query .= "     AND RASD.SUBCLASSCD = STD.SUBCLASSCD ";
        $query .= "     AND RASD.AVG_DIV = '1' ";
        $query .= "     AND RASD.GRADE = ALSMR.GRADE ";
        $query .= "     AND RASD.HR_CLASS = ALSMR.HR_CLASS ";
        $query .= "     AND RASD.COURSECD = ALSMR.COURSECD ";
        $query .= "     AND RASD.MAJORCD = ALSMR.MAJORCD ";
        $query .= "     AND RASD.COURSECODE = ALSMR.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     STD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND STD.CLASSCD = '{$classcd}' ";
        $query .= "     AND STD.SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "     AND STD.CURRICULUM_CD = '{$curriculumCd}' ";
        $query .= "     AND STD.SUBCLASSCD = '{$subclasscd}' ";
        $query .= " ORDER BY ";
        $query .= "     ALSMR.ASSESSLEVEL DESC ";

        return $query;
    }

}
?>
