<?php

require_once('for_php7.php');

class knje386query extends Query {

    //学年コンボ取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
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

    //更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knje386Query::getSchregno($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //DELETE
            $query  = " DELETE FROM AFT_TOTAL_STUDY_BATCH_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR = '".CTRL_YEAR."' ";
            $query .= "         AND SCHREGNO = '".$row["SCHREGNO"]."' ";
            $db->query($query);

            //教科点
            if (in_array("CLASS_SCORE", $model->selected)) {
                $classScore = $db->getOne(knje386Query::getClassScore($db, $model, $row));
            }
            //学力点
            if (in_array("ABILITY_SCORE", $model->selected)) {
                $abilityScore = $db->getOne(knje386Query::getAbilityScore($row));
            }
            //TOEFL
            if (in_array("TOEFL_SCORE", $model->selected)) {
                $toeflScore = $db->getOne(knje386Query::getToeflScore($model, $row));
            }
            //資格点
            if (in_array("QUALIFIED_SCORE", $model->selected)) {
                $query = knje386Query::getQualifiedScore($db, $model, $row);
                $qualifiedScore = ($query != "") ? $db->getOne($query) : "";
            }
            //加減点
            if (in_array("ADJUSTMENT_SCORE", $model->selected)) {
                $adjustmentScore = $db->getOne(knje386Query::getAdjustmentScore($row));
            }

            //INSERT
            $data = array();
            $data["YEAR"][TEXT]               = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]           = $row["SCHREGNO"];
            $data["CLASS_SCORE"][NUMBER]      = $classScore;
            $data["ABILITY_SCORE"][NUMBER]    = $abilityScore;
            $data["TOEFL_SCORE"][NUMBER]      = $toeflScore;
            $data["QUALIFIED_SCORE"][NUMBER]  = $qualifiedScore;
            $data["ADJUSTMENT_SCORE"][NUMBER] = $adjustmentScore;
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][NUMBER]          = "SYSDATE()";

            $query = Query::insertSQL($data, "AFT_TOTAL_STUDY_BATCH_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //生徒取得
    function getSchregno($model) {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";

        return $query;
    }

    //教科点取得
    function getClassScore($db, $model, $data) {

        //教科点の算出法を取得
        $query = knje386Query::getAftTotalStudyClassCalcMst();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $query  = " WITH SCHOOL_KIND_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR ";
        $query .= "         , SCHOOL_KIND ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_GDAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ) ";
        $query .= " , GRADE_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         GDAT.GRADE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_GDAT GDAT ";
        $query .= "         INNER JOIN SCHOOL_KIND_T SK ";
        $query .= "             ON GDAT.YEAR = SK.YEAR ";
        $query .= "             AND GDAT.SCHOOL_KIND = SK.SCHOOL_KIND ";
        $query .= "     WHERE ";
        $query .= "         GDAT.GRADE <= '".$model->field["GRADE"]."' ";
        $query .= " ) ";
        $query .= " , REGD_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         MAX(REGD.YEAR) AS YEAR ";
        $query .= "         , REGD.GRADE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "         INNER JOIN GRADE_T GT ";
        $query .= "             ON GT.GRADE = REGD.GRADE ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$data["SCHREGNO"]."' ";
        $query .= "     GROUP BY ";
        $query .= "         REGD.GRADE ";
        $query .= " ) ";
        if ($row["CALC_PATTERN"] == "01") {
            //CALC_PAT = "01"のとき、評定を使用
            $query .= " SELECT ";
            if ($row["ROUND_PATTERN"] == "1") {
                $query .= "     ROUND(AVG(CAST(STUDYREC.VALUATION AS FLOAT)), ".$row["ROUND_DEC_PLACES"]." - 1) * 100 "; //四捨五入
            } else if ($row["ROUND_PATTERN"] == "2") {
                $query .= "     TRUNC(AVG(CAST(STUDYREC.VALUATION AS FLOAT)) + 0.".sprintf("%0".$row["ROUND_DEC_PLACES"]."d", 9).", ".$row["ROUND_DEC_PLACES"]." - 1) * 100 "; //切り上げ
            } else {
                $query .= "     TRUNC(AVG(CAST(STUDYREC.VALUATION AS FLOAT)), ".$row["ROUND_DEC_PLACES"]." - 1) * 100 "; //切り捨て
            }
            $query .= " FROM ";
            $query .= "     SCHREG_STUDYREC_DAT STUDYREC ";
            $query .= "     INNER JOIN REGD_T ";
            $query .= "         ON REGD_T.YEAR = STUDYREC.YEAR ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$data["SCHREGNO"]."' ";
        } else {
            //CALC_PAT = "02"のとき、評価(100点法)を使用
            $query .= " SELECT ";
            $query .= "     ROUND(AVG(CAST(RRSD.SCORE AS FLOAT)), 0) "; //小数第1位で四捨五入
            $query .= " FROM ";
            $query .= "     RECORD_RANK_SDIV_DAT RRSD ";
            $query .= "     INNER JOIN REGD_T ";
            $query .= "         ON REGD_T.YEAR = RRSD.YEAR ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$data["SCHREGNO"]."' ";
            $query .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '9990008' ";
        }

        return $query;
    }

    //教科点の算出法を取得
    function getAftTotalStudyClassCalcMst() {
        $query  = " SELECT ";
        $query .= "     CALC_PATTERN ";
        $query .= "     , ROUND_DEC_PLACES ";
        $query .= "     , ROUND_PATTERN ";
        $query .= " FROM ";
        $query .= "     AFT_TOTAL_STUDY_CLASS_CALC_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学力点取得
    function getAbilityScore($data) {
        $query  = " SELECT ";
        $query .= "     SCORE ";
        $query .= " FROM ";
        $query .= "     AFT_TOTAL_STUDY_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '".$data["SCHREGNO"]."' ";

        return $query;
    }

    //TOEFL取得
    function getToeflScore($model, $data) {

        $testDate = str_replace("/", "-", $model->field["TEST_DATE"]);
        $testDate2YearsAgo = date("Y-m-d", strtotime($testDate . "-2 year"));

        //TOEFL基準日から過去2年間で最高点を取得する
        $query  = " SELECT ";
        $query .= "     MAX( ";
        $query .= "         CASE ";
        $query .= "             WHEN DAT.SCORE - MST.BASE_SCORE < MST.RANGE_F THEN MST.RANGE_F ";
        $query .= "             WHEN DAT.SCORE - MST.BASE_SCORE > MST.RANGE_T THEN MST.RANGE_T ";
        $query .= "             ELSE DAT.SCORE - MST.BASE_SCORE ";
        $query .= "             END ";
        $query .= "     ) ";
        $query .= " FROM ";
        $query .= "     AFT_TOTAL_STUDY_TOEFL_DAT DAT ";
        $query .= "     INNER JOIN TOEFL_MST MST ";
        $query .= "         ON MST.YEAR = '".CTRL_YEAR."' ";
        $query .= " WHERE ";
        $query .= "     DAT.SCHREGNO = '".$data["SCHREGNO"]."' ";
        $query .= "     AND DAT.TEST_DATE BETWEEN '".$testDate2YearsAgo."' AND '".$testDate."' ";

        return $query;
    }

    //資格点取得
    function getQualifiedScore($db, $model, $data) {
        $query = "";
        if ($model->Properties["useQualifiedManagementFlg"] == '1') {
            //QUALIFIED_RESULT_MSTにNOT_PRINTカラムがあるかチェック
            if (0 < $db->getOne(knje386Query::getSyscatColumns("QUALIFIED_RESULT_MST"))) {
                $query .= " WITH MAX_SCORE AS ( ";
                $query .= "     SELECT ";
                $query .= "         TEST.QUALIFIED_CD ";
                $query .= "         , MAX(RE.SCORE) AS SCORE ";
                $query .= "     FROM ";
                $query .= "         SCHREG_QUALIFIED_TEST_DAT TEST ";
                $query .= "         INNER JOIN QUALIFIED_RESULT_MST RE ";
                $query .= "             ON RE.QUALIFIED_CD = TEST.QUALIFIED_CD ";
                $query .= "             AND RE.RESULT_CD = TEST.RESULT_CD ";
                $query .= "     WHERE ";
                $query .= "         RE.YEAR = '".CTRL_YEAR."' ";
                $query .= "         AND TEST.SCHREGNO = '".$data["SCHREGNO"]."' ";
                $query .= "         AND RE.NOT_PRINT IS NULL ";
                $query .= "     GROUP BY ";
                $query .= "         TEST.QUALIFIED_CD ";
                $query .= " ) ";
                $query .= " SELECT ";
                $query .= "     SUM(SCORE) ";
                $query .= " FROM ";
                $query .= "     MAX_SCORE ";
            }
        } else {
            //QUALIFIED_RANK_DATにNOT_PRINTカラムがあるかチェック
            if (0 < $db->getOne(knje386Query::getSyscatColumns("QUALIFIED_RANK_DAT"))) {
                $query .= " WITH MAX_SCORE AS ( ";
                $query .= "     SELECT ";
                $query .= "         HOBBY.QUALIFIED_CD ";
                $query .= "         , MAX(RANK.SCORE) AS SCORE ";
                $query .= "     FROM ";
                $query .= "         SCHREG_QUALIFIED_HOBBY_DAT HOBBY ";
                $query .= "         INNER JOIN QUALIFIED_RANK_DAT RANK ";
                $query .= "             ON RANK.QUALIFIED_CD = HOBBY.QUALIFIED_CD ";
                $query .= "             AND RANK.RANK = HOBBY.RANK ";
                $query .= "     WHERE ";
                $query .= "         HOBBY.SCHREGNO = '".$data["SCHREGNO"]."' ";
                $query .= "         AND RANK.NOT_PRINT IS NULL ";
                $query .= "     GROUP BY ";
                $query .= "         HOBBY.QUALIFIED_CD ";
                $query .= " ) ";
                $query .= " SELECT ";
                $query .= "     SUM(SCORE) ";
                $query .= " FROM ";
                $query .= "     MAX_SCORE ";
            }
        }

        return $query;
    }

    //対象テーブルにNOT_PRINTがあるかどうか取得
    function getSyscatColumns($tableName) {
        $query  = " SELECT ";
        $query .= "     COUNT(COLNAME) ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME = '".$tableName."' ";
        $query .= "     AND COLNAME = 'NOT_PRINT' ";

        return $query;
    }

    //加減点取得
    function getAdjustmentScore($data) {
        $query  = " SELECT ";
        $query .= "     SCORE ";
        $query .= " FROM ";
        $query .= "     AFT_TOTAL_STUDY_ADJUSTMENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '".$data["SCHREGNO"]."' ";

        return $query;
    }
}
?>
