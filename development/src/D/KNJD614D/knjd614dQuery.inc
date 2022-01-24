<?php

require_once('for_php7.php');

class knjd614dQuery extends Query
{
    //学期取得
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

    //年組取得（権限チェック）
    public function getGradeHrClass($model, $semester, $div = "")
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        if ($div == "GRADE") {
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T2.GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T1.YEAR = T2.YEAR ";
        $query .= "           AND T1.GRADE = T2.GRADE ";
        $query .= "           AND T2.SCHOOL_KIND = 'H' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        if ($div != "GRADE") {
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   T1.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //
    public function getTestType($model, $ghosyaCd, $searchCdStrt, $searchCdEnd)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "   substr(T1.MOCKCD,6,1) AS GHOSYACD, ";
        $query .= "   substr(T1.MOCKCD,8,2) AS MCD, ";
        $query .= "   T1.MOCKCD AS VALUE, ";
        $query .= "   T1.MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "   MOCK_MST T1 ";
        $query .= " WHERE ";
        $query .= "   substr(T1.MOCKCD,6,1) = '{$ghosyaCd}' ";
        $query .= "   AND substr(T1.MOCKCD,8,2) BETWEEN '{$searchCdStrt}' AND '{$searchCdEnd}' ";
        $query .= "   AND VALUE(substr(T1.MOCKCD,7,1), 0) <= INT('{$model->field["GRADE"]}') ";
        $query .= "   AND '0' || substr(T1.MOCKCD,7,1) IN (SELECT GRADE FROM SCHREG_REGD_GDAT WHERE SCHOOL_KIND = 'H')";
        //当年度の生徒に紐づく過年度の模試データがあるか、チェック
        $query .= "   AND EXISTS ( SELECT 'X' ";
        $query .= "                FROM MOCK_RANK_RANGE_DAT T2 ";
        $query .= "                     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = '".CTRL_YEAR."' AND T3.SCHREGNO = T2.SCHREGNO AND T3.GRADE = '{$model->field["GRADE"]}'";
        $query .= "                WHERE T2.YEAR <= '".CTRL_YEAR."' AND T2.MOCKCD = T1.MOCKCD ) ";
        $query .= " ORDER BY ";
        $query .= "   GHOSYACD, ";
        $query .= "   MCD ";

        return $query;
    }

    //テスト取得
    public function getTest($semester)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV || ':' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";
        $query .= "     AND SCORE_DIV = '01' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期日付取得
    public function getSemesterDate($field, $semester)
    {
        $query  = " SELECT ";
        $query .= "     ".$field." ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";

        return $query;
    }

    public function getRecommendationSeqNo($model)
    {
        $query  = " SELECT ";
        $query .= "   SETTING_SEQ AS VALUE, ";
        $query .= "   SETTING_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "   AFT_RECOMMENDATION_PRINT_SETTING_HDAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->field["YEAR"]}' ";

        return $query;
    }

    public function getRecommendationInfo($model)
    {
        $query  = " SELECT ";
        $query .= "   SEMESTER, ";
        $query .= "   TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTCD, ";
        $query .= "   GRADE, ";
        $query .= "   ATTEND_SDATE, ";
        $query .= "   ATTEND_EDATE, ";
        $query .= "   SCHOOL_TEST_FLG1, ";
        $query .= "   G1_FLG, ";
        $query .= "   G1_SCORE_FLG, ";
        $query .= "   G1_DEV_FLG, ";
        $query .= "   G1_VALUATION_FLG, ";
        $query .= "   G2_FLG, ";
        $query .= "   G2_SCORE_FLG, ";
        $query .= "   G2_DEV_FLG, ";
        $query .= "   G2_VALUATION_FLG, ";
        $query .= "   G3_FLG, ";
        $query .= "   G3_SCORE_FLG, ";
        $query .= "   G3_DEV_FLG, ";
        $query .= "   G3_VALUATION_FLG, ";
        $query .= "   VALUATION_TOTAL_FLG, ";
        $query .= "   G1_G3_FLG, ";
        $query .= "   ALL_FLG, ";
        $query .= "   QUALIFIED_FLG, ";
        $query .= "   SCHOOL_TEST_FLG2 ";
        $query .= " FROM ";
        $query .= "   AFT_RECOMMENDATION_PRINT_SETTING_HDAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->field["YEAR"]}' ";
        $query .= "   AND SETTING_SEQ = '{$model->field["SETTING_SEQ"]}' ";

        return $query;
    }

    public function chkRecommendationName($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.SETTING_SEQ ";
        $query .= " FROM ";
        $query .= "     AFT_RECOMMENDATION_PRINT_SETTING_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "   AND T1.SETTING_NAME = '{$model->field["SETTING_NAME"]}' ";
        //$query .= "   AND T1.SETTING_SEQ = '{$model->field["SETTING_SEQ"]}' ";

        return $query;
    }

    public function getNextRecommendationSeq($model)
    {
        $query .= " SELECT ";
        $query .= "     VALUE(MAX(SETTING_SEQ), 0) + 1 AS MXSEQ ";
        $query .= " FROM ";
        $query .= "     AFT_RECOMMENDATION_PRINT_SETTING_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";

        return $query;
    }

    public function getPrevSelectedHR($model)
    {
        $query  = " SELECT ";
        $query .= "   GRADE || '-' || HR_CLASS AS GR_HR ";
        $query .= " FROM ";
        $query .= "   AFT_RECOMMENDATION_PRINT_SETTING_HR_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->field["YEAR"]}' ";
        $query .= "   AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "   AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$model->field["TESTCD"]}' ";
        $query .= "   AND GRADE = '{$model->field["GRADE"]}' ";
        $query .= "   AND SETTING_SEQ = {$model->field["SETTING_SEQ"]} ";

        return $query;
    }

    public function getPrevCheckedInfo($model)
    {
        $query .= " SELECT ";
        $query .= "   SEQ, ";
        $query .= "   CHECK_FLG ";
        $query .= " FROM ";
        $query .= "   AFT_RECOMMENDATION_PRINT_SETTING_MOCK_HDAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->field["YEAR"]}' ";
        $query .= "   AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "   AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$model->field["TESTCD"]}' ";
        $query .= "   AND GRADE = '{$model->field["GRADE"]}' ";
        $query .= "   AND SETTING_SEQ = {$model->field["SETTING_SEQ"]} ";

        return $query;
    }

    public function getPrevSelectMockInfo($model)
    {
        $query .= " SELECT ";
        $query .= "   SEQ, ";
        $query .= "   MOCKCD ";
        $query .= " FROM ";
        $query .= "   AFT_RECOMMENDATION_PRINT_SETTING_MOCK_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->field["YEAR"]}' ";
        $query .= "   AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "   AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$model->field["TESTCD"]}' ";
        $query .= "   AND GRADE = '{$model->field["GRADE"]}' ";
        $query .= "   AND SETTING_SEQ = {$model->field["SETTING_SEQ"]} ";

        return $query;
    }

    public function updateRecommendation($model, $useSeq)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();

        $data["YEAR"][TEXT] = $model->field["YEAR"];
        $data["SETTING_SEQ"][NUMBER] = $useSeq;
        $data["SEMESTER"][TEXT] = $model->field["SEMESTER"];
        $data["TESTKINDCD"][TEXT] = strlen($model->field["TESTCD"]) < 6 ? "" : substr($model->field["TESTCD"], 0, 2);
        $data["TESTITEMCD"][TEXT] = strlen($model->field["TESTCD"]) < 6 ? "" : substr($model->field["TESTCD"], 2, 2);
        $data["SCORE_DIV"][TEXT] = strlen($model->field["TESTCD"]) < 6 ? "" : substr($model->field["TESTCD"], 4, 2);
        $data["GRADE"][TEXT] = $model->field["GRADE"];
        $data["SETTING_NAME"][TEXT] = $model->field["SETTING_NAME"];
        $data["ATTEND_SDATE"][TEXT] = $model->field["DATE_FROM"];
        $data["ATTEND_EDATE"][TEXT] = $model->field["DATE_TO"];
        $data["SCHOOL_TEST_FLG1"][TEXT] = $model->field["CATEGORY_IS_INNER"];
        $data["G1_FLG"][TEXT] = $model->field["CATEGORY_IS_INNER_G1"];
        $data["G1_SCORE_FLG"][TEXT] = $model->field["CATEGORY_IS_INNER_G1S1"];
        $data["G1_DEV_FLG"][TEXT] = $model->field["CATEGORY_IS_INNER_G1SH"];
        $data["G1_VALUATION_FLG"][TEXT] = $model->field["CATEGORY_IS_INNER_G1S9"];
        $data["G2_FLG"][TEXT] = $model->field["CATEGORY_IS_INNER_G2"];
        $data["G2_SCORE_FLG"][TEXT] = $model->field["CATEGORY_IS_INNER_G2S1"];
        $data["G2_DEV_FLG"][TEXT] = $model->field["CATEGORY_IS_INNER_G2SH"];
        $data["G2_VALUATION_FLG"][TEXT] = $model->field["CATEGORY_IS_INNER_G2S9"];
        $data["G3_FLG"][TEXT] = $model->field["CATEGORY_IS_INNER_G3"];
        $data["G3_SCORE_FLG"][TEXT] = $model->field["CATEGORY_IS_INNER_G3S1"];
        $data["G3_DEV_FLG"][TEXT] = $model->field["CATEGORY_IS_INNER_G3SH"];
        $data["G3_VALUATION_FLG"][TEXT] = $model->field["CATEGORY_IS_INNER_G3S9"];
        $data["VALUATION_TOTAL_FLG"][TEXT] = $model->field["CATEGORY_IS_INNER_9_ALL"];
        $data["G1_G3_FLG"][TEXT] = $model->field["CATEGORY_IS_INNER_9_ALL12_123"];
        $data["ALL_FLG"][TEXT] = $model->field["CATEGORY_IS_PROF_TEST"];
        $data["QUALIFIED_FLG"][TEXT] = $model->field["CATEGORY_IS_QUALIFY"];
        $data["SCHOOL_TEST_FLG2"][TEXT] = $model->field["CATEGORY_IS_INNER_8020"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC] = "SYSDATE()";

        $query = Query::insertSQL($data, "AFT_RECOMMENDATION_PRINT_SETTING_HDAT");
        $db->query($query);

        foreach ($model->selHR as $vv => $grHr) {
            if (is_null($grHr) || $grHr == "") {
                continue;
            }
            $data = array();
            $data["YEAR"][TEXT] = $model->field["YEAR"];
            $data["SETTING_SEQ"][NUMBER] = $useSeq;
            $data["SEMESTER"][TEXT] = $model->field["SEMESTER"];
            $data["TESTKINDCD"][TEXT] = strlen($model->field["TESTCD"]) < 6 ? "" : substr($model->field["TESTCD"], 0, 2);
            $data["TESTITEMCD"][TEXT] = strlen($model->field["TESTCD"]) < 6 ? "" : substr($model->field["TESTCD"], 2, 2);
            $data["SCORE_DIV"][TEXT] = strlen($model->field["TESTCD"]) < 6 ? "" : substr($model->field["TESTCD"], 4, 2);
            $data["GRADE"][TEXT] = $model->field["GRADE"];
            $data["HR_CLASS"][TEXT] = strlen($grHr) > 3 ? substr($grHr, -3, 3) : $grHr;
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC] = "SYSDATE()";

            $query = Query::insertSQL($data, "AFT_RECOMMENDATION_PRINT_SETTING_HR_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    public function updateMockSelectInfo($model, $useSeq, $updNo)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $setChk = "";
        $getArry = array();
        $saveTblName = "";
        switch ($updNo) {
            case 2:
                $setChk = $model->field["CATEGORY_IS_OUTER_COLLEGE"];
                $getArry = $model->selOutCol;
                $saveTblName = "AFT_RECOMMENDATION_PRINT_SETTING_MOCK_DAT";
                break;
            case 3:
                $setChk = $model->field["CATEGORY_IS_BENESSE_TEST"];
                $getArry = $model->selBene;
                $saveTblName = "AFT_RECOMMENDATION_PRINT_SETTING_MOCK_DAT";
                break;
            case 4:
                $setChk = $model->field["CATEGORY_IS_STUDY_SUP"];
                $getArry = $model->selSSup;
                $saveTblName = "AFT_RECOMMENDATION_PRINT_SETTING_MOCK_DAT";
                break;
            case 5:
                $setChk = $model->field["CATEGORY_IS_SUNDAI"];
                $getArry = $model->selSund;
                $saveTblName = "AFT_RECOMMENDATION_PRINT_SETTING_MOCK_DAT";
                break;
            case 6:
                $setChk = $model->field["CATEGORY_IS_KAWAI"];
                $getArry = $model->selKawi;
                $saveTblName = "AFT_RECOMMENDATION_PRINT_SETTING_MOCK_DAT";
                break;
            default:
                break;
        }
        $data = array();
        $data["YEAR"][TEXT] = $model->field["YEAR"];
        $data["SETTING_SEQ"][NUMBER] = $useSeq;
        $data["SEMESTER"][TEXT] = $model->field["SEMESTER"];
        $data["TESTKINDCD"][TEXT] = strlen($model->field["TESTCD"]) < 6 ? "" : substr($model->field["TESTCD"], 0, 2);
        $data["TESTITEMCD"][TEXT] = strlen($model->field["TESTCD"]) < 6 ? "" : substr($model->field["TESTCD"], 2, 2);
        $data["SCORE_DIV"][TEXT] = strlen($model->field["TESTCD"]) < 6 ? "" : substr($model->field["TESTCD"], 4, 2);
        $data["GRADE"][TEXT] = $model->field["GRADE"];
        $data["SEQ"][NUMBER] = $updNo;
        $data["CHECK_FLG"][TEXT] = $setChk;
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC] = "SYSDATE()";

        $query = Query::insertSQL($data, "AFT_RECOMMENDATION_PRINT_SETTING_MOCK_HDAT");
        $db->query($query);

        foreach ($getArry as $vv => $saveVal) {
            if (is_null($saveVal) || $saveVal == "") {
                continue;
            }
            $data = array();
            $data["YEAR"][TEXT] = $model->field["YEAR"];
            $data["SETTING_SEQ"][TEXT] = $useSeq;
            $data["GRADE"][TEXT] = $model->field["GRADE"];
            $data["SEMESTER"][TEXT] = $model->field["SEMESTER"];
            $data["TESTKINDCD"][TEXT] = strlen($model->field["TESTCD"]) < 6 ? "" : substr($model->field["TESTCD"], 0, 2);
            $data["TESTITEMCD"][TEXT] = strlen($model->field["TESTCD"]) < 6 ? "" : substr($model->field["TESTCD"], 2, 2);
            $data["SCORE_DIV"][TEXT] = strlen($model->field["TESTCD"]) < 6 ? "" : substr($model->field["TESTCD"], 4, 2);
            $data["SEQ"][TEXT] = $updNo;
            $data["MOCKCD"][TEXT] = $saveVal;
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC] = "SYSDATE()";

            $query = Query::insertSQL($data, $saveTblName);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    public function delSavedInfo($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     AFT_RECOMMENDATION_PRINT_SETTING_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "   AND T1.SETTING_SEQ = '{$model->field["SETTING_SEQ"]}' ";
        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "   AFT_RECOMMENDATION_PRINT_SETTING_HR_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "   AND T1.SETTING_SEQ = '{$model->field["SETTING_SEQ"]}' ";
        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "   AFT_RECOMMENDATION_PRINT_SETTING_MOCK_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "   AND T1.SETTING_SEQ = '{$model->field["SETTING_SEQ"]}' ";
        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "   AFT_RECOMMENDATION_PRINT_SETTING_MOCK_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "   AND T1.SETTING_SEQ = '{$model->field["SETTING_SEQ"]}' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    public function getOutColHeadInfoSql()
    {
        $query  = " WITH NICHIDAI_BASE_SUMFIX(PUTTYPE, MOCK_SUBCLASS_CD, TSCORE, TDEVIATION, TNOT2DISPFLG) AS ( ";
        $query .= " VALUES('1', '041001', '3科合計素点', '3科合計化点', '0') ";
        $query .= " UNION ";
        $query .= " VALUES('1', '041002', '3科日大順位', '', '1') ";
        $query .= " UNION ";
        $query .= " VALUES('1', '041003', '5/4科合計素点', '5/4科合計化点', '0') ";
        $query .= " UNION ";
        $query .= " VALUES('1', '041004', '5/4科日大順位', '', '1') ";
        $query .= " ), DATA_SUM_BASE AS ( ";
        $query .= " SELECT ";
        $query .= "   ROW_NUMBER() OVER(PARTITION BY T1.MOCK_SUBCLASS_CD) AS RNUM, ";
        $query .= "   CASE WHEN LENGTH(T1.MOCK_SUBCLASS_CD) > 3 THEN SUBSTR(T1.MOCK_SUBCLASS_CD, 3, 1) ELSE NULL END AS PUTTYPE, ";
        $query .= "   T1.MOCK_SUBCLASS_CD, ";
        $query .= "   T1.TSCORE, ";
        $query .= "   T1.TDEVIATION, ";
        $query .= "   '0' AS TNOT2DISPFLG ";
        $query .= " FROM ";
        $query .= "   mock_csv_zkai_score_head_dat T1 ";
        $query .= " WHERE ";
        $query .= "   YEAR BETWEEN '".(intval(CTRL_YEAR) - 2)."' AND '".CTRL_YEAR."' ";  //過去3年分
        $query .= " GROUP BY ";
        $query .= "   T1.MOSI_CD, ";
        $query .= "   T1.MOCK_SUBCLASS_CD, ";
        $query .= "   T1.TSCORE, ";
        $query .= "   T1.TDEVIATION ";
        $query .= " ORDER BY ";
        $query .= "   T1.MOSI_CD, ";
        $query .= "   T1.MOCK_SUBCLASS_CD ";
        $query .= " ), DATA_SUM_TBL AS ( ";
        $query .= " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   NICHIDAI_BASE_SUMFIX ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "   T1.PUTTYPE, ";
        $query .= "   T1.MOCK_SUBCLASS_CD, ";
        $query .= "   T1.TSCORE, ";
        $query .= "   T1.TDEVIATION, ";
        $query .= "   T1.TNOT2DISPFLG ";
        $query .= " FROM ";
        $query .= "   DATA_SUM_BASE T1 ";
        $query .= " WHERE ";
        $query .= "   T1.MOCK_SUBCLASS_CD IS NOT NULL ";
        $query .= "   AND T1.RNUM = 1 ";  //MOCK_SUBCLASS_CDで見つかったものの中で、MOSI_CDの1番若い名称を取得
        $query .= "   AND SUBSTR(T1.MOCK_SUBCLASS_CD, 1, 2) = '04' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   DATA_SUM_TBL T1 ";
        $query .= " ORDER BY ";
        $query .= "   CASE WHEN LENGTH(T1.MOCK_SUBCLASS_CD) > 3 THEN SUBSTR(T1.MOCK_SUBCLASS_CD, 3, 1) ELSE NULL END, ";
        $query .= "   T1.MOCK_SUBCLASS_CD ";
        return $query;
    }

    public function getBeneHeadInfoSql()
    {
        $query  = " WITH DATA_SUM_BASE AS ( ";
        $query .= " SELECT ";
        $query .= "   ROW_NUMBER() OVER(PARTITION BY T1.MOCK_SUBCLASS_CD) AS RNUM, ";
        $query .= "   CASE WHEN LENGTH(T1.MOCK_SUBCLASS_CD) > 3 THEN SUBSTR(T1.MOCK_SUBCLASS_CD, 3, 1) ELSE NULL END AS PUTTYPE, ";
        $query .= "   T1.MOCK_SUBCLASS_CD, ";
        $query .= "   T1.TSCORE, ";
        $query .= "   T1.TALL_DEV, ";
        $query .= "   T1.TGTZ, ";
        $query .= "   T1.TSCHOOL_RANK, ";
        $query .= "   T1.TALL_RANK ";
        $query .= " FROM ";
        $query .= "   mock_csv_bene_score_head_dat  T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR BETWEEN '".(intval(CTRL_YEAR) - 2)."' AND '".CTRL_YEAR."' ";  //過去3年分
        $query .= "   AND T1.MOCK_SUBCLASS_CD IS NOT NULL ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   PUTTYPE, ";
        $query .= "   MOCK_SUBCLASS_CD, ";
        $query .= "   TSCORE, ";
        $query .= "   TALL_DEV, ";
        $query .= "   TGTZ, ";
        $query .= "   TSCHOOL_RANK, ";
        $query .= "   TALL_RANK ";
        $query .= " FROM ";
        $query .= "   DATA_SUM_BASE ";
        $query .= " WHERE ";
        $query .= "   RNUM = 1 ";
        $query .= "   AND SUBSTR(MOCK_SUBCLASS_CD, 1, 2) = '03' ";
        $query .= " ORDER BY ";
        $query .= "   PUTTYPE, ";
        $query .= "   MOCK_SUBCLASS_CD ";
        return $query;
    }

    public function getSSupHeadInfoSql()
    {
        $query  = " WITH DATA_SUM_BASE AS ( ";
        $query .= " SELECT ";
        $query .= "   ROW_NUMBER() OVER(PARTITION BY T1.MOCK_SUBCLASS_CD) AS RNUM, ";
        $query .= "   CASE WHEN LENGTH(T1.MOCK_SUBCLASS_CD) > 3 THEN SUBSTR(T1.MOCK_SUBCLASS_CD, 3, 1) ELSE NULL END AS PUTTYPE, ";
        $query .= "   T1.MOCK_SUBCLASS_CD, ";
        $query .= "   T1.TSCORE, ";
        $query .= "   T1.TALL_DEV, ";
        $query .= "   T1.TGTZ, ";
        $query .= "   T1.TSCHOOL_RANK, ";
        $query .= "   T1.TALL_RANK ";
        $query .= " FROM ";
        $query .= "   mock_csv_bene_score_head_dat  T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR BETWEEN '".(intval(CTRL_YEAR) - 2)."' AND '".CTRL_YEAR."' ";  //過去3年分
        $query .= "   AND T1.MOCK_SUBCLASS_CD IS NOT NULL ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   PUTTYPE, ";
        $query .= "   MOCK_SUBCLASS_CD, ";
        $query .= "   TSCORE, ";
        $query .= "   TALL_DEV, ";
        $query .= "   TGTZ, ";
        $query .= "   TSCHOOL_RANK, ";
        $query .= "   TALL_RANK ";
        $query .= " FROM ";
        $query .= "   DATA_SUM_BASE ";
        $query .= " WHERE ";
        $query .= "   RNUM = 1 ";
        $query .= "   AND SUBSTR(MOCK_SUBCLASS_CD, 1, 2) = '02' ";
        $query .= " ORDER BY ";
        $query .= "   PUTTYPE, ";
        $query .= "   MOCK_SUBCLASS_CD ";
        return $query;
    }

    public function getSundHeadInfoSql()
    {
        $query  = " WITH DATA_SUM_BASE AS ( ";
        $query .= " SELECT ";
        $query .= "   ROW_NUMBER() OVER(PARTITION BY T1.MOCK_SUBCLASS_CD) AS RNUM, ";
        $query .= "   CASE WHEN LENGTH(T1.MOCK_SUBCLASS_CD) > 3 THEN SUBSTR(T1.MOCK_SUBCLASS_CD, 3, 1) ELSE NULL END AS PUTTYPE, ";
        $query .= "   T1.MOCK_SUBCLASS_CD, ";
        $query .= "   T1.TSCORE, ";
        $query .= "   T1.TDEVIATION, ";
        $query .= "   T1.TRANK ";
        $query .= " FROM ";
        $query .= "   mock_csv_sundai_score_head_dat  T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR BETWEEN '".(intval(CTRL_YEAR) - 2)."' AND '".CTRL_YEAR."' ";  //過去3年分
        $query .= "   AND T1.MOCK_SUBCLASS_CD IS NOT NULL ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   PUTTYPE, ";
        $query .= "   MOCK_SUBCLASS_CD, ";
        $query .= "   TSCORE, ";
        $query .= "   TDEVIATION, ";
        $query .= "   TRANK ";
        $query .= " FROM ";
        $query .= "   DATA_SUM_BASE ";
        $query .= " WHERE ";
        $query .= "   RNUM = 1 ";
        $query .= " ORDER BY ";
        $query .= "   PUTTYPE, ";
        $query .= "   MOCK_SUBCLASS_CD ";
        return $query;
    }

    public function getKawiHeadInfoSql()
    {
        $query  = " WITH NICHIDAI_BASE_SUMFIX(PUTTYPE, MOCK_SUBCLASS_CD, TSCORE, TDEVIATION, TNOT2DISPFLG) AS ( ";
        $query .= " VALUES('1', '051001', '3科合計素点', '3科合計化点', '0') ";
        $query .= " UNION ";
        $query .= " VALUES('1', '051002', '3科日大順位', '', '1') ";
        $query .= " UNION ";
        $query .= " VALUES('1', '051003', '5/4科合計素点', '5/4科合計化点', '0') ";
        $query .= " UNION ";
        $query .= " VALUES('1', '051004', '5/4科日大順位', '', '1') ";
        $query .= " ), DATA_SUM_BASE AS ( ";
        $query .= " SELECT ";
        $query .= "   ROW_NUMBER() OVER(PARTITION BY T1.MOCK_SUBCLASS_CD) AS RNUM, ";
        $query .= "   CASE WHEN LENGTH(T1.MOCK_SUBCLASS_CD) > 3 THEN SUBSTR(T1.MOCK_SUBCLASS_CD, 3, 1) ELSE NULL END AS PUTTYPE, ";
        $query .= "   T1.MOCK_SUBCLASS_CD, ";
        $query .= "   T1.TSCORE, ";
        $query .= "   T1.TDEVIATION, ";
        $query .= "   '0' AS TNOT2DISPFLG";
        $query .= " FROM ";
        $query .= "   mock_csv_zkai_score_head_dat T1 ";
        $query .= " WHERE ";
        $query .= "   YEAR BETWEEN '".(intval(CTRL_YEAR) - 2)."' AND '".CTRL_YEAR."' ";  //過去3年分
        $query .= " GROUP BY ";
        $query .= "   T1.MOSI_CD, ";
        $query .= "   T1.MOCK_SUBCLASS_CD, ";
        $query .= "   T1.TSCORE, ";
        $query .= "   T1.TDEVIATION ";
        $query .= " ORDER BY ";
        $query .= "   T1.MOSI_CD, ";
        $query .= "   T1.MOCK_SUBCLASS_CD ";
        $query .= " ), DATA_SUM_TBL AS ( ";
        $query .= " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   NICHIDAI_BASE_SUMFIX ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "   T1.PUTTYPE, ";
        $query .= "   T1.MOCK_SUBCLASS_CD, ";
        $query .= "   T1.TSCORE, ";
        $query .= "   T1.TDEVIATION, ";
        $query .= "   T1.TNOT2DISPFLG ";
        $query .= " FROM ";
        $query .= "   DATA_SUM_BASE T1 ";
        $query .= " WHERE ";
        $query .= "   T1.MOCK_SUBCLASS_CD IS NOT NULL ";
        $query .= "   AND T1.RNUM = 1 ";  //MOCK_SUBCLASS_CDで見つかったものの中で、MOSI_CDの1番若い名称を取得
        $query .= "   AND SUBSTR(T1.MOCK_SUBCLASS_CD, 1, 2) = '05' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   DATA_SUM_TBL T1 ";
        $query .= " ORDER BY ";
        $query .= "   CASE WHEN LENGTH(T1.MOCK_SUBCLASS_CD) > 3 THEN SUBSTR(T1.MOCK_SUBCLASS_CD, 3, 1) ELSE NULL END, ";
        $query .= "   T1.MOCK_SUBCLASS_CD ";
        return $query;
    }
    public function getGradeName($model)
    {
        return " SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '".CTRL_YEAR."' AND GRADE = '".$model->field["GRADE"]."' ";
    }

    public function getClsScoreSql($model)
    {
        $SEMEALL = "9";
        $getTestCdVal = $SEMEALL."-990009";
        $query .= " WITH DELSKIPSCHREG_T AS ( ";
        $query .= " SELECT ";
        $query .= "   MAX(T1.YEAR) AS MYEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   (T1.YEAR < '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER = (SELECT MAX(TW.SEMESTER) FROM SEMESTER_MST TW WHERE TW.YEAR = T1.YEAR AND TW.SEMESTER <> '".$SEMEALL."') ";
        $query .= "   ) OR (T1.YEAR = '" .CTRL_YEAR. "' AND T1.SEMESTER = '" .$model->field["SEMESTER"]. "') ";
        $query .= "   AND T1.SCHREGNO IN (SELECT TX.SCHREGNO FROM SCHREG_REGD_DAT TX WHERE TX.YEAR = '" .CTRL_YEAR. "' AND TX.SEMESTER = '" .$model->field["SEMESTER"]. "' AND TX.GRADE || '-' || TX.HR_CLASS IN ('".implode("','", $model->field["CATEGORY_SELECTED1"])."')) ";
        $query .= " GROUP BY ";
        $query .= "   GRADE, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   SCHREGNO ";
        $query .= " ), USESCHREG_T AS ( ";
        $query .= " SELECT ";
        $query .= "   TN.* ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT TN ";
        $query .= "   INNER JOIN DELSKIPSCHREG_T TZ ";
        $query .= "     ON TZ.MYEAR = TN.YEAR ";
        $query .= "    AND TZ.SEMESTER = TN.SEMESTER ";
        $query .= "    AND TZ.SCHREGNO = TN.SCHREGNO ";
        $query .= " ), RECSREC_T AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.SCHREGNO ";
        $query .= "   , T1.CLASSCD ";
        $query .= "   , T1.SCHOOL_KIND ";
        $query .= "   , T1.CURRICULUM_CD ";
        $query .= "   , T1.SUBCLASSCD ";
        $query .= "   , SUM(T1.VALUATION) AS VALUATION ";
        $query .= " FROM ";
        $query .= "   SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   value(T1.PRINT_FLG, '0') <> '1' ";
        $query .= "   AND T1.YEAR < '".CTRL_YEAR."' ";
        $query .= "   AND T1.VALUATION IS NOT NULL ";
        $query .= "   AND T1.SCHREGNO IN (SELECT TU.SCHREGNO FROM USESCHREG_T TU WHERE TU.YEAR = T1.YEAR) ";
        $query .= " GROUP BY ";
        $query .= "   T1.SCHREGNO ";
        $query .= "   , T1.CLASSCD ";
        $query .= "   , T1.SCHOOL_KIND ";
        $query .= "   , T1.CURRICULUM_CD ";
        $query .= "   , T1.SUBCLASSCD ";
        if ($model->field["CATEGORY_IS_INNER_9_ALL12_123"] == "2") {
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "   T1.SCHREGNO ";
            $query .= "   , T1.CLASSCD ";
            $query .= "   , T1.SCHOOL_KIND ";
            $query .= "   , T1.CURRICULUM_CD ";
            $query .= "   , T1.SUBCLASSCD ";
            $query .= "   , SUM(T1.SCORE) AS VALUATION ";
            $query .= " FROM ";
            $query .= "   RECORD_SCORE_DAT T1 ";
            $query .= " WHERE ";
            $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "   AND T1.SEMESTER || '-' || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$getTestCdVal."' ";
            $query .= "   AND T1.SCORE IS NOT NULL ";
            $query .= "   AND T1.SCHREGNO IN (SELECT TU.SCHREGNO FROM USESCHREG_T TU WHERE TU.YEAR = T1.YEAR) ";
            $query .= " GROUP BY ";
            $query .= "   T1.SCHREGNO ";
            $query .= "   , T1.CLASSCD ";
            $query .= "   , T1.SCHOOL_KIND ";
            $query .= "   , T1.CURRICULUM_CD ";
            $query .= "   , T1.SUBCLASSCD ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.SCHREGNO ";
        $query .= "   , T1.CLASSCD ";
        $query .= "   , T1.SCHOOL_KIND ";
        $query .= "   , T2.CLASSNAME ";
        $query .= "   , COUNT(SUBCLASSCD) AS CNT ";
        $query .= "   , SUM(T1.VALUATION) AS VALUATION ";
        $query .= " FROM ";
        $query .= "   RECSREC_T T1 ";
        $query .= "   LEFT JOIN CLASS_MST T2 ";
        $query .= "     ON T2.CLASSCD = T1.CLASSCD ";
        $query .= "    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= " GROUP BY ";
        $query .= "   T1.SCHREGNO ";
        $query .= "   , T1.CLASSCD ";
        $query .= "   , T1.SCHOOL_KIND ";
        $query .= "   , T2.CLASSNAME ";
        $query .= " ORDER BY ";
        $query .= "   T1.SCHREGNO ";
        $query .= "   , T1.CLASSCD ";
        return $query;
    }

    public function getQualifiedInfoSql()
    {
        $query  = " WITH SCHREG_YEAR AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "   T2.SCHOOL_KIND = 'H' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.SEQ, ";
        $query .= "   T1.REGDDATE, ";
        $query .= "   T1.QUALIFIED_CD, ";
        $query .= "   T2.QUALIFIED_NAME, ";
        $query .= "   T2.PROMOTER, ";
        $query .= "   T1.RANK, ";
        $query .= "   H312.NAME1 AS RANKNAME, ";
        $query .= "   T1.SCORE, ";
        $query .= "   T1.REMARK ";
        $query .= " FROM ";
        $query .= "   schreg_qualified_hobby_dat T1 ";
        $query .= "   LEFT JOIN qualified_mst T2 ";
        $query .= "     ON T2.qualified_cd = T1.qualified_cd ";
        $query .= "   LEFT JOIN V_NAME_MST H312 ";
        $query .= "     ON H312.YEAR = T1.YEAR ";
        $query .= "    AND H312.NAMECD1 = 'H312' ";
        $query .= "    AND H312.NAMECD2 = T1.rank ";
        $query .= "   INNER JOIN SCHREG_YEAR T3 ";
        $query .= "     ON T3.YEAR = T1.YEAR ";
        $query .= "    AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = '?' ";
        $query .= " ORDER BY ";
        $query .= "   YEAR, ";
        $query .= "   regddate ";
        return $query;
    }

    public function getAttendInfo($model, $year, $schregno)
    {
        if ($model->semesMap[$year] == null) {
            return null;
        }
        $semInfo = knjd614dQuery::getSpecifyYearSemes($model, $year);
        if ($semInfo == null) {
            return null;
        }

        $yStr = $semInfo["YEAR"];
        $semes = $semInfo["SEMESTER"];
        $sDate = $year == CTRL_YEAR ? $model->field["DATE_FROM"] : $semInfo["SDATE"];  //当年度は指示画面の日付
        $eDate = $year == CTRL_YEAR ? $model->field["DATE_TO"] : $semInfo["EDATE"];    //当年度は指示画面の日付

        if ($semInfo == null || $sDate == null || $eDate == null || strcmp($sDate, $eDate) > 0) {
            return null;
        }

        if (preg_match('/http/', SERVLET_URL)) {
            $url = SERVLET_URL ."/KNJZ"; // 本番用
        } else {
            $url = (empty($_SERVER["HTTPS"]) ? "http://" : "https://") . $_SERVER["HTTP_HOST"] .SERVLET_URL ."/KNJZ"; // 沖縄確認用
        }
        $SEMEALL = "9";
        $data = array();
        // 帳票のAttendAccumulateで欠課時数取得
        $data = array('CALL_DEST_JAVA_PRGID' => 'servletpack.KNJZ.detail.dao.AttendAccumulateIO',
                      'METHOD_NAME'     => 'json',
                      'DBNAME'          => DB_DATABASE,
                      'SDATE'           => str_replace('/', '-', $sDate),
                      'DATE'            => str_replace('/', '-', $eDate),
                      'ATTEND_DIV'      => 'ATTEND_SEMES_DAT',
                      'CTRL_YEAR'       => $year,
                      'CTRL_SEMESTER'   => $semes,
                      'SCHREGNO'        => $schregno,
                      "useTestCountflg" => "TESTITEM_MST_COUNTFLG_NEW_SDIV",
                      "useCurriculumcd" => $model->Properties["useCurriculumcd"],
                      'DOCUMENTROOT'    => DOCUMENTROOT
        );

        // use key 'http' even if you send the request to https://...
        $options = array(
            'http' => array(
                'header'  => "Content-type: application/x-www-form-urlencoded\r\n",
                'method'  => 'POST',
                'timeout' => 300,
                'content' => http_build_query($data)
            )
        );
        $target = array();
        $debug = array();
        $context  = stream_context_create($options);
        $result = file_get_contents($url, false, $context);
        if ($result != false) {
            $arr = json_decode($result, true);
            foreach ($arr as $schregno => $records) {  //データは生徒毎にarray格納。
                foreach ($records as $record) {  //1データづつ処理
                    if ($record["SEMESTER"] != $SEMEALL) {
                        continue;
                    }
                    $keyStr = $record["SEMESTER"]."-".$record["SCHREGNO"];
                    $target[$keyStr] = $record;
                }
            }
        }
        //echo "[[debug]] <br>".implode("<br>", $debug);
        return $target;
    }

    //指定年度の通年学期情報(!!当年度はCTRL_SEMESTERまで!!)を取得する。※当年度は、SDATEが
    public function getSpecifyYearSemes($model, $year)
    {
        $retMap = null;
        $fstSDate = "";
        foreach ($model->semesMap as $kStr => $noUseObj) {
            if (strlen($kStr) >= 4) {
                $yStr = substr($kStr, 0, 4);
                if ($yStr == $year) {
                    $retMap = $model->semesMap[$yStr];
                    if ($year != CTRL_YEAR) {
                        break;
                    } else {
                        if ($fstSDate == "") {
                            $fstSDate = $retMap["SDATE"];
                        }
                    }
                }
            }
        }
        //_semesMapはソートされているので、当年度であれば、ループの最後がCTRL_SEMESTERのデータ。ただし、開始日は最初のSDATEに変える
        if ($year == CTRL_YEAR && $retMap != null) {
            $retMap["SDATE"] =  $fstSDate;
        }
        return $retMap;
    }

    public function getSubClsSql()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T2.CLASSCD, ";
        $query .= "   T2.SCHOOL_KIND, ";
        $query .= "   T2.CURRICULUM_CD, ";
        $query .= "   T2.SUBCLASSCD, ";
        $query .= "   T3.SUBCLASSABBV, ";
        $query .= "   T4.CLASSNAME";
        $query .= " FROM ";
        $query .= "   CHAIR_STD_DAT T1 ";
        $query .= "   LEFT JOIN CHAIR_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "   LEFT JOIN SUBCLASS_MST T3 ";
        $query .= "     ON T3.CLASSCD = T2.CLASSCD ";
        $query .= "    AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "    AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "    AND T3.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "   LEFT JOIN CLASS_MST T4 ";
        $query .= "     ON T4.CLASSCD = T2.CLASSCD ";
        $query .= "    AND T4.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '??' ";
        $query .= "   AND T1.SCHREGNO = '?' ";
        $query .= "   AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD NOT IN ";
        $query .= "   ( ";
        $query .= "    SELECT ";
        $query .= "      TW.ATTEND_CLASSCD || '-' || TW.ATTEND_SCHOOL_KIND || '-' || TW.ATTEND_CURRICULUM_CD || '-' || TW.ATTEND_SUBCLASSCD ";
        $query .= "    FROM ";
        $query .= "      SUBCLASS_REPLACE_COMBINED_DAT TW ";
        $query .= "    WHERE ";
        $query .= "      TW.YEAR = T2.YEAR ";
        $query .= "      AND TW.COMBINED_CLASSCD || '-' || TW.COMBINED_SCHOOL_KIND || '-' || TW.COMBINED_CURRICULUM_CD || '-' || TW.COMBINED_SUBCLASSCD ";
        $query .= "          <> T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ";
        $query .= "   ) ";
        $query .= "   AND T2.CLASSCD < '90' ";
        $query .= " ORDER BY ";
        $query .= "   T1.YEAR, ";
        $query .= "   T2.CLASSCD, ";
        $query .= "   T2.SCHOOL_KIND, ";
        $query .= "   T2.CURRICULUM_CD, ";
        $query .= "   T2.SUBCLASSCD ";
        return $query;
    }

    public function getCalcResult82Sql($model)
    {
        $query  = " SELECT ";
        $query .= "  T1.YEAR, ";
        $query .= "  T1.SCHREGNO, ";
        $query .= "  T1.TEST_VALUATION_AVG, ";
        $query .= "  T1.TEST_VALUATION_PERCENT_SCORE, ";
        $query .= "  T1.MOCK_TOTAL_PERCENT_SCORE, ";
        $query .= "  T2.PERCENTAGE ";
        $query .= " FROM ";
        $query .= "  AFT_SCHREG_RECOMMENDATION_RANK_DAT T1 ";
        $query .= "  LEFT JOIN AFT_RECOMMENDATION_RANK_HEAD_DAT T2 ";
        $query .= "    ON T2.YEAR = T1.YEAR ";
        $query .= "   AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "  T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "  AND T1.SCHREGNO = '?' ";
        return $query;
    }

    public function getMockScoreSql($years)
    {
        $query  = " WITH OTHER_RANKRANGE_AT AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   substr(T1.MOCKCD, 6,1) AS GYOSHATYPE, ";
        $query .= "   T1.MOCKCD, ";
        $query .= "   T3.MOCKNAME1, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   substr(T1.MOCK_SUBCLASS_CD, 1,2) AS MOSI_NUMBER, ";
        $query .= "   substr(T1.MOCK_SUBCLASS_CD, 3,1) AS PUTTYPE, ";
        $query .= "   T1.MOCK_SUBCLASS_CD, ";
        $query .= "   T1.SCORE, ";
        $query .= "   T1.DEVIATION, ";
        $query .= "   T1.GTZ, ";
        $query .= "   T1.RANK AS INNER_RANK ";
        $query .= " FROM ";
        $query .= "   MOCK_RANK_RANGE_DAT T1 ";
        $query .= "   LEFT JOIN MOCK_MST T3 ";
        $query .= "    ON T3.MOCKCD = T1.MOCKCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR BETWEEN '???' AND '??' ";
        $query .= "   AND T1.RANK_RANGE = '2' ";
        $query .= "   AND T1.RANK_DIV = '02' ";
        $query .= "   AND T1.MOCKDIV = '1' ";
        $query .= "   AND T1.SCHREGNO = '?' ";
        $query .= " ORDER BY ";
        $query .= "   T1.YEAR, ";
        $query .= "   MOCKCD, ";
        $query .= "   MOCK_SUBCLASS_CD ";
        $query .= " ), OTHER_RANKRANGE_BT AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   substr(T1.MOCKCD, 6,1) AS GYOSHATYPE, ";
        $query .= "   T1.MOCKCD, ";
        $query .= "   T3.MOCKNAME1, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   substr(T1.MOCK_SUBCLASS_CD, 1,2) AS MOSI_NUMBER, ";
        $query .= "   substr(T1.MOCK_SUBCLASS_CD, 3,1) AS PUTTYPE, ";
        $query .= "   T1.MOCK_SUBCLASS_CD, ";
        $query .= "   T1.SCORE, ";
        $query .= "   T1.DEVIATION, ";
        $query .= "   T1.GTZ, ";
        $query .= "   T1.RANK ";
        $query .= " FROM ";
        $query .= "   MOCK_RANK_RANGE_DAT T1 ";
        $query .= "   LEFT JOIN MOCK_MST T3 ";
        $query .= "    ON T3.MOCKCD = T1.MOCKCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR BETWEEN '???' AND '??' ";
        $query .= "   AND T1.RANK_RANGE = '1' ";
        $query .= "   AND T1.RANK_DIV = '02' ";
        $query .= "   AND T1.MOCKDIV = '1' ";
        $query .= "   AND T1.SCHREGNO = '?' ";
        $query .= " ORDER BY ";
        $query .= "   T1.YEAR, ";
        $query .= "   MOCKCD, ";
        $query .= "   MOCK_SUBCLASS_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   CASE WHEN T1.YEAR IS NULL THEN T2.YEAR ELSE T1.YEAR END AS YEAR, ";
        $query .= "   CASE WHEN T1.GYOSHATYPE IS NULL THEN T2.GYOSHATYPE ELSE T1.GYOSHATYPE END AS GYOSHATYPE, ";
        $query .= "   CASE WHEN T1.MOCKCD IS NULL THEN T2.MOCKCD ELSE T1.MOCKCD END AS MOCKCD, ";
        $query .= "   CASE WHEN T1.MOCKNAME1 IS NULL THEN T2.MOCKNAME1 ELSE T1.MOCKNAME1 END AS MOCKNAME1, ";
        $query .= "   CASE WHEN T1.SCHREGNO IS NULL THEN T2.SCHREGNO ELSE T1.SCHREGNO END AS SCHREGNO, ";
        $query .= "   CASE WHEN T1.MOSI_NUMBER IS NULL THEN T2.MOSI_NUMBER ELSE T1.MOSI_NUMBER END AS MOSI_NUMBER, ";
        $query .= "   CASE WHEN T1.PUTTYPE IS NULL THEN T2.PUTTYPE ELSE T1.PUTTYPE END AS PUTTYPE, ";
        $query .= "   CASE WHEN T1.MOCK_SUBCLASS_CD IS NULL THEN T2.MOCK_SUBCLASS_CD ELSE T1.MOCK_SUBCLASS_CD END AS MOCK_SUBCLASS_CD, ";
        $query .= "   CASE WHEN T1.SCORE IS NULL THEN T2.SCORE ELSE T1.SCORE END AS SCORE, ";
        $query .= "   CASE WHEN T1.DEVIATION IS NULL THEN T2.DEVIATION ELSE T1.DEVIATION END AS DEVIATION, ";
        $query .= "   CASE WHEN T1.GTZ IS NULL THEN T2.GTZ ELSE T1.GTZ END AS GTZ, ";
        $query .= "   T1.INNER_RANK,";
        $query .= "   T2.RANK ";
        $query .= " FROM ";
        $query .= "  OTHER_RANKRANGE_AT T1 ";
        $query .= "  FULL OUTER JOIN OTHER_RANKRANGE_BT T2 ";
        $query .= "    ON T2.YEAR = T1.YEAR ";
        $query .= "   AND T2.GYOSHATYPE = T1.GYOSHATYPE ";
        $query .= "   AND T2.MOCKCD = T1.MOCKCD ";
        $query .= "   AND T2.MOCKNAME1 = T1.MOCKNAME1 ";
        $query .= "   AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   AND T2.MOSI_NUMBER = T1.MOSI_NUMBER ";
        $query .= "   AND T2.PUTTYPE = T1.PUTTYPE ";
        $query .= "   AND T2.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ";
        return $query;
    }

    public function getScoreSql()
    {
        $query  = " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SEMESTER, ";
        $query .= "   TESTKINDCD, ";
        $query .= "   TESTITEMCD, ";
        $query .= "   SCORE_DIV, ";
        $query .= "   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLSCD, ";
        $query .= "   SCORE, ";
        $query .= "   GRADE_DEVIATION, ";
        $query .= "   AVG, ";
        $query .= "   CLASS_RANK, ";
        $query .= "   COURSE_RANK, ";
        $query .= "   GRADE_RANK ";
        $query .= " FROM ";
        $query .= "   RECORD_RANK_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR IN '??' ";  //過年度分も一気に取得
        $query .= "   AND SCHOOL_KIND = 'H' "; //高校のみ
        $query .= "   AND SCORE_DIV IN ('01', '08', '09') ";
        $query .= "   AND SCHREGNO = '?' ";
        $query .= " ORDER BY ";
        $query .= "   SCHREGNO, ";
        $query .= "   YEAR, ";
        $query .= "   SCORE_DIV, ";
        $query .= "   SEMESTER, ";
        $query .= "   TESTKINDCD, ";
        $query .= "   TESTITEMCD, ";
        $query .= "   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
        return $query;
    }

    public function getStudentSql($grHrCls)
    {
        $query  = " WITH MAX_GRADESEMES AS ( ";
        $query .= " SELECT ";
        $query .= "   GRADE, ";
        $query .= "   SCHREGNO, ";
        $query .= "   MAX(YEAR) AS YEAR, ";
        $query .= "   MAX(SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT ";
        $query .= " GROUP BY ";
        $query .= "   SCHREGNO, ";
        $query .= "   GRADE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T2.NAME, ";
        $query .= "   T2.NAME_KANA, ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T4.GRADE_CD, ";
        $query .= "   T4.GRADE_NAME2, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T5.HR_NAME, ";
        $query .= "   T5.HR_NAMEABBV, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T6.STAFFNAME, ";
        $query .= "   T7.ABBV1 AS SEX, ";
        $query .= "   T2.INOUTCD, ";
        $query .= "   T2.ENT_DIV, ";
        $query .= "   T8.ATTENDREC_REMARK ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "     ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   INNER JOIN MAX_GRADESEMES T3 ";
        $query .= "     ON T3.YEAR = T1.YEAR ";
        $query .= "    AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT T4 ";
        $query .= "     ON T4.YEAR = T1.YEAR ";
        $query .= "    AND T4.GRADE = T1.GRADE ";
        $query .= "   LEFT JOIN SCHREG_REGD_HDAT T5 ";
        $query .= "     ON T5.YEAR = T1.YEAR ";
        $query .= "    AND T5.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T5.GRADE = T1.GRADE ";
        $query .= "    AND T5.HR_CLASS = T1.HR_CLASS ";
        $query .= "   LEFT JOIN V_STAFF_MST T6 ";
        $query .= "     ON T6.YEAR = T5.YEAR ";
        $query .= "    AND T6.STAFFCD = T5.TR_CD1 ";
        $query .= "   LEFT JOIN V_NAME_MST T7 ";
        $query .= "     ON T7.YEAR = T1.YEAR ";
        $query .= "    AND T7.NAMECD1 = 'Z002' ";
        $query .= "    AND T7.NAMECD2 = T2.SEX ";
        $query .= "   LEFT JOIN HEXAM_ENTREMARK_DAT T8 ";
        $query .= "     ON T8.YEAR = T1.YEAR ";
        $query .= "    AND T8.SCHREGNO = T1.SCHREGNO ";
        $query .= "    AND T8.ANNUAL = T1.ANNUAL ";
        $query .= " WHERE ";
        $query .= "   T4.SCHOOL_KIND = 'H' ";
        $query .= "   AND T1.SCHREGNO IN ( ";
        $query .= "                SELECT ";
        $query .= "                  SCHREGNO ";
        $query .= "                FROM ";
        $query .= "                  SCHREG_REGD_DAT ";
        $query .= "                WHERE ";
        $query .= "                  YEAR = '".CTRL_YEAR."' ";
        $query .= "                  AND GRADE || '-' || HR_CLASS = '".$grHrCls."' ";
        $query .= " ) ";
        $query .= " ORDER BY ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T1.SCHREGNO ";
        return $query;
    }

    public function getSemesterMapSql($model)
    {
        $SEMEALL = "9";
        $query  = " SELECT YEAR, SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE YEAR < '".CTRL_YEAR."' AND SEMESTER = '".$SEMEALL."' ";
        $query .= " UNION ALL ";
        $query .= " SELECT YEAR, SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER <= '".$model->field["SEMESTER"]."' ORDER BY YEAR, SEMESTER ";
        return $query;
    }
}
