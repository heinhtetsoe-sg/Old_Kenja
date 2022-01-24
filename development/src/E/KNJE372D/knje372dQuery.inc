<?php

require_once('for_php7.php');

class knje372dQuery extends Query
{

    /* 学期取得 */
    public function getSemester($year, $semester)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        if ($semester != "") {
            $query .= " AND SEMESTER = '".$semester."' ";
        } else {
            $query .= " AND SEMESTER < '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    public function getGrade($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    T1.GRADE AS VALUE, ";
        $query .= "    T1.GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SCHOOL_KIND = 'H' ";
        $query .= " AND T1.GRADE NOT IN ( SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'A023' AND NAME1 = T1.SCHOOL_KIND ) ";
        //校内学力試験に登録されている学年
        $query .= " AND T1.GRADE IN ( SELECT NAME3 FROM V_NAME_MST WHERE YEAR = T1.YEAR AND NAMECD1 = 'E072' ) ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //テスト考査取得
    public function getTestItem($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEMESTER || '-' ||T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T2.SEMESTERNAME || ' ' ||T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= " INNER JOIN SEMESTER_MST T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCORE_DIV  = '08' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 名称マスタ取得
    public function getNameMst($nameCd1, $nameCd2)
    {
        $query  = " SELECT * ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE NAMECD1 = '{$nameCd1}' ";
        if ($nameCd2) {
            $query .= "   AND NAMECD2 = '{$nameCd2}' ";
        }
        return $query;
    }

    //換算値データの存在チェック
    public function selectAftSchregConvertScoreSql($year, $schregNo)
    {
        $query  = " SELECT COUNT(*) CNT ";
        $query .= " FROM AFT_SCHREG_CONVERT_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " AND SCHREGNO = '{$schregNo}' ";

        return $query;
    }

    //校内学力試験データ更新処理
    public function executeProficiency($db, $model, $proficiencyMst)
    {

        //校内学力試験の点数取得
        $schregScoreList = array();
        // 名称マスタから「校内学力試験」の情報を取得
        $query = knje372dQuery::executeProficiencySql($model, $proficiencyMst);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            $score = array();
            if (isset($schregScoreList[$schregNo])) {
                $score = $schregScoreList[$schregNo];
            }

            //テストでのSCORE設定カラム位置判定
            $scoreName = "PROFICIENCY1";
            if ($row["PROFICIENCYCD"] == "311") {
                $scoreName = "PROFICIENCY2";
            }
            //スコア設定
            $key = $row["GRADE"];
            if ($proficiencyMst[$key]["SUBCLASS_CD1"] == $row["PROFICIENCY_SUBCLASS_CD"]) {
                $score[$scoreName."_SCORE1"] = $row["SCORE"];
            } elseif ($proficiencyMst[$key]["SUBCLASS_CD2"] == $row["PROFICIENCY_SUBCLASS_CD"]) {
                $score[$scoreName."_SCORE2"] = $row["SCORE"];
            } else {
                if (!$score[$scoreName."_SCORE3"]) {
                    $score[$scoreName."_SCORE3"] = 0;
                    $score[$scoreName."_SCORE3_CNT"] = 0;
                }
                $score[$scoreName."_SCORE3"] += $row["SCORE"];
                $score[$scoreName."_SCORE3_CNT"]++;
            }
            $schregScoreList[$schregNo] = $score;
        }
        $result->free();

        //平均点算出
        foreach ($schregScoreList as $schregNo => $score) {
            if ($score["PROFICIENCY1_SCORE1"] && $score["PROFICIENCY1_SCORE2"] && $score["PROFICIENCY1_SCORE3"]) {
                $total = 0;
                $total += $score["PROFICIENCY1_SCORE1"];
                $total += $score["PROFICIENCY1_SCORE2"];
                if ($score["PROFICIENCY1_SCORE3_CNT"] > 1) {
                    $score["PROFICIENCY1_SCORE3"] = round($score["PROFICIENCY1_SCORE3"] / $score["PROFICIENCY1_SCORE3_CNT"]);
                }
                $total += $score["PROFICIENCY1_SCORE3"];
                $score["PROFICIENCY1_AVG"] = $total / 3;
            }
            if ($score["PROFICIENCY2_SCORE1"] && $score["PROFICIENCY2_SCORE2"] && $score["PROFICIENCY2_SCORE3"]) {
                $total = 0;
                $total += $score["PROFICIENCY2_SCORE1"];
                $total += $score["PROFICIENCY2_SCORE2"];
                if ($score["PROFICIENCY2_SCORE3_CNT"] > 1) {
                    $score["PROFICIENCY2_SCORE3"] = round($score["PROFICIENCY2_SCORE3"] / $score["PROFICIENCY2_SCORE3_CNT"]);
                }
                $total += $score["PROFICIENCY2_SCORE3"];
                $score["PROFICIENCY2_AVG"] = $total / 3;
            }
            $schregScoreList[$schregNo] = $score;
        }

        //DB更新
        foreach ($schregScoreList as $schregNo => $score) {
            // データ存在チェック
            $query = knje372dQuery::selectAftSchregConvertScoreSql(CTRL_YEAR, $schregNo);
            $count = $db->getOne($query);
            if ($count > 0) {
                $data = array();
                $data["PROFICIENCY1_SCORE1"][NUMBER] = $score["PROFICIENCY1_SCORE1"];
                $data["PROFICIENCY1_SCORE2"][NUMBER] = $score["PROFICIENCY1_SCORE2"];
                $data["PROFICIENCY1_SCORE3"][NUMBER] = $score["PROFICIENCY1_SCORE3"];
                $data["PROFICIENCY1_AVG"][NUMBER]    = $score["PROFICIENCY1_AVG"];
                $data["PROFICIENCY2_SCORE1"][NUMBER] = $score["PROFICIENCY2_SCORE1"];
                $data["PROFICIENCY2_SCORE2"][NUMBER] = $score["PROFICIENCY2_SCORE2"];
                $data["PROFICIENCY2_SCORE3"][NUMBER] = $score["PROFICIENCY2_SCORE3"];
                $data["PROFICIENCY2_AVG"][NUMBER]    = $score["PROFICIENCY2_AVG"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "sysdate()";
                $where  = " WHERE YEAR = '".CTRL_YEAR."'";
                $where .= "   AND SCHREGNO = '{$schregNo}'";
                $query = Query::updateSQL($data, "AFT_SCHREG_CONVERT_SCORE_DAT", $where);
                $db->query($query);
            } else {
                $data = array();
                $data["YEAR"][TEXT]                  = CTRL_YEAR;
                $data["SCHREGNO"][TEXT]              = $schregNo;
                $data["PROFICIENCY1_SCORE1"][NUMBER] = $score["PROFICIENCY1_SCORE1"];
                $data["PROFICIENCY1_SCORE2"][NUMBER] = $score["PROFICIENCY1_SCORE2"];
                $data["PROFICIENCY1_SCORE3"][NUMBER] = $score["PROFICIENCY1_SCORE3"];
                $data["PROFICIENCY1_AVG"][NUMBER]    = $score["PROFICIENCY1_AVG"];
                $data["PROFICIENCY2_SCORE1"][NUMBER] = $score["PROFICIENCY2_SCORE1"];
                $data["PROFICIENCY2_SCORE2"][NUMBER] = $score["PROFICIENCY2_SCORE2"];
                $data["PROFICIENCY2_SCORE3"][NUMBER] = $score["PROFICIENCY2_SCORE3"];
                $data["PROFICIENCY2_AVG"][NUMBER]    = $score["PROFICIENCY2_AVG"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "sysdate()";
                $query = Query::insertSQL($data, "AFT_SCHREG_CONVERT_SCORE_DAT");
                $db->query($query);
            }
        }

        //3学年選択時は2学年生分のデータも作成
        if ($model->field["GRADE"] == $model->field["GRADE_MAX"]) {
            $year = CTRL_YEAR - 1;
            foreach ($schregScoreList as $schregNo => $score) {
                // データ存在チェック
                $query = knje372dQuery::selectAftSchregConvertScoreSql($year, $schregNo);
                $count = $db->getOne($query);
                if ($count > 0) {
                    $data = array();
                    $data["PROFICIENCY1_SCORE1"][NUMBER] = $score["PROFICIENCY1_SCORE1"];
                    $data["PROFICIENCY1_SCORE2"][NUMBER] = $score["PROFICIENCY1_SCORE2"];
                    $data["PROFICIENCY1_SCORE3"][NUMBER] = $score["PROFICIENCY1_SCORE3"];
                    $data["PROFICIENCY1_AVG"][NUMBER]    = $score["PROFICIENCY1_AVG"];
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][FUNC]              = "sysdate()";
                    $where  = " WHERE YEAR = '".$year."'";
                    $where .= "   AND SCHREGNO = '{$schregNo}'";
                    $query = Query::updateSQL($data, "AFT_SCHREG_CONVERT_SCORE_DAT", $where);
                    $db->query($query);
                } else {
                    $data = array();
                    $data["YEAR"][TEXT]                  = $year;
                    $data["SCHREGNO"][TEXT]              = $schregNo;
                    $data["PROFICIENCY1_SCORE1"][NUMBER] = $score["PROFICIENCY1_SCORE1"];
                    $data["PROFICIENCY1_SCORE2"][NUMBER] = $score["PROFICIENCY1_SCORE2"];
                    $data["PROFICIENCY1_SCORE3"][NUMBER] = $score["PROFICIENCY1_SCORE3"];
                    $data["PROFICIENCY1_AVG"][NUMBER]    = $score["PROFICIENCY1_AVG"];
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][FUNC]              = "sysdate()";
                    $query = Query::insertSQL($data, "AFT_SCHREG_CONVERT_SCORE_DAT");
                    $db->query($query);
                }
            }
        }
    }

    //校内学力試験データ取得SQL
    public function executeProficiencySql($model, $proficiencyMst)
    {
        $query  = "";
        $query .= " WITH SCHREG AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "       SCHREGNO ";
        $query .= "     , YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , GRADE ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T2.YEAR ";
        $query .= "   , T2.SEMESTER ";
        $query .= "   , T2.PROFICIENCYDIV ";
        $query .= "   , T2.PROFICIENCYCD ";
        $query .= "   , T2.SCHREGNO ";
        $query .= "   , T1.GRADE ";
        $query .= "   , T2.PROFICIENCY_SUBCLASS_CD ";
        $query .= "   , T2.SCORE ";
        $query .= " FROM ";
        $query .= "   PROFICIENCY_YMST T1 ";
        $query .= "   INNER JOIN PROFICIENCY_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.PROFICIENCYDIV = T1.PROFICIENCYDIV ";
        $query .= "     AND T2.PROFICIENCYCD = T1.PROFICIENCYCD ";
        $query .= "   INNER JOIN SCHREG ";
        $query .= "     ON SCHREG.YEAR = T1.YEAR ";
        $query .= "     AND SCHREG.SEMESTER = T1.SEMESTER ";
        $query .= "     AND SCHREG.GRADE = T1.GRADE ";
        $query .= "     AND SCHREG.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   (T1.YEAR, T1.PROFICIENCYDIV, T1.PROFICIENCYCD) IN ( ";

        $whereArray = array();
        //取得条件作成
        foreach ($proficiencyMst as $key => $value) {
            if ($value["GRADE"] <= $model->field["GRADE"]) {
                $year = CTRL_YEAR - ($model->field["GRADE"] - $value["GRADE"]);
                $whereArray[] = " VALUES ('{$year}', '{$value["PROFICIENCYDIV"]}', '{$value["PROFICIENCYCD"]}') ";
            }
        }
        $query .= implode(" UNION ", $whereArray);
        $query .= "   ) ";

        return $query;
    }

    //学年取得
    public function getGradeList($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    T1.GRADE, ";
        $query .= "    T1.GRADE_NAME1 AS NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SCHOOL_KIND = 'H' ";
        $query .= " AND T1.GRADE <= '{$model->field["GRADE"]}' ";
        $query .= "ORDER BY ";
        $query .= "    T1.GRADE ";

        return $query;
    }

    //総合成績データ更新処理
    public function executeRecordRankScore($db, $model, $gradeList)
    {

        //校内学力試験の点数取得
        $schregScoreList = array();
        // 名称マスタから「校内学力試験」の情報を取得
        $query = knje372dQuery::executeRecordRankScoreSql($model, $gradeList);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            $score = array();
            if (isset($schregScoreList[$schregNo])) {
                $score = $schregScoreList[$schregNo];
            }

            //選択している学年から総合成績の設定先を取得
            $gradeCalc = ($model->field["GRADE_MAX"] - $row["GRADE"]);
            $grade = "GRADE1";
            if ($gradeCalc == 0) {
                $grade = "GRADE3";
            } elseif ($gradeCalc == 1) {
                $grade = "GRADE2";
            }
            //スコア設定
            $score[$grade."_TOTAL_SCORE"] = $row["SCORE"];
            $score[$grade."_AVG"]         = $row["AVG"];

            $schregScoreList[$schregNo] = $score;
        }
        $result->free();

        //総合成績算出
        foreach ($schregScoreList as $schregNo => $score) {
            $avg = 0;
            if ($score["GRADE1_AVG"]) {
                $avg += $score["GRADE1_AVG"];
            }
            if ($score["GRADE2_AVG"]) {
                $avg += $score["GRADE2_AVG"];
            }
            if ($score["GRADE3_AVG"]) {
                $avg += $score["GRADE3_AVG"];
            }
            $score["TOTAL_AVG"] = $avg / 3;
            $schregScoreList[$schregNo] = $score;
        }

        //DB更新(※更新のみ 校内学力試験データ作成で作成済み)
        foreach ($schregScoreList as $schregNo => $score) {
            $data = array();
            $data["GRADE1_TOTAL_SCORE"][NUMBER] = $score["GRADE1_TOTAL_SCORE"];
            $data["GRADE2_TOTAL_SCORE"][NUMBER] = $score["GRADE2_TOTAL_SCORE"];
            $data["GRADE3_TOTAL_SCORE"][NUMBER] = $score["GRADE3_TOTAL_SCORE"];
            $data["TOTAL_AVG"][NUMBER]          = $score["TOTAL_AVG"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";
            $where  = " WHERE YEAR = '".CTRL_YEAR."'";
            $where .= "   AND SCHREGNO = '{$schregNo}'";
            $query = Query::updateSQL($data, "AFT_SCHREG_CONVERT_SCORE_DAT", $where);
            $db->query($query);
        }
    }

    //総合成績データ取得SQL
    public function executeRecordRankScoreSql($model, $gradeList)
    {
        //学期は考査で選択した学期
        $semester = substr($model->field["TESTITEM"], 0, 1);
        $semester = ($semester == "9") ? $model->maxSemester : $semester;


        $query  = "";
        $query .= " WITH SCHREG AS ( ";
        $query .= "   SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "   FROM ";
        $query .= "     AFT_SCHREG_CONVERT_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "       ON REGD.YEAR = T1.YEAR ";
        $query .= "       AND REGD.SEMESTER = '{$semester}' ";
        $query .= "       AND REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ) ";
        $query .= " , SCHREG_GRADE AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "       YEAR ";
        $query .= "     , SCHREGNO ";
        $query .= "     , GRADE ";
        $query .= "   FROM SCHREG_REGD_DAT ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , T2.GRADE ";
        $query .= "   , T1.SCORE ";
        $query .= "   , T1.AVG ";
        $query .= " FROM RECORD_RANK_SDIV_DAT T1 ";
        $query .= " INNER JOIN SCHREG_GRADE T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " INNER JOIN SCHREG ";
        $query .= "     ON SCHREG.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        //学年毎総合成績
        $query .= " AND T1.SEMESTER || '-' || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$model->field["TESTITEM"]}' ";
        $query .= " AND T1.CLASSCD = '99' ";
        $query .= " AND T1.SCHOOL_KIND = 'H' ";
        $query .= " AND T1.CURRICULUM_CD = '99' ";
        $query .= " AND T1.SUBCLASSCD = '999999' ";

        $query .= " UNION ALL ";

        $query .= " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , T2.GRADE ";
        $query .= "   , T1.SCORE ";
        $query .= "   , T1.AVG ";
        $query .= " FROM RECORD_RANK_SDIV_DAT T1 ";
        $query .= " INNER JOIN SCHREG_GRADE T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " INNER JOIN SCHREG ";
        $query .= "     ON SCHREG.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   (T1.YEAR, T1.SEMESTER) IN ( ";

        $whereArray = array();
        //前年度取得条件作成
        foreach ($gradeList as $value) {
            if ($value < $model->field["GRADE"]) {
                $year = CTRL_YEAR - ($model->field["GRADE"] - $value);
                $whereArray[] = " VALUES ('{$year}', '9') ";
            }
        }
        $query .= implode(" UNION ", $whereArray);
        $query .= "   ) ";
        //学年毎総合成績
        $query .= " AND T1.TESTKINDCD = '99' ";
        $query .= " AND T1.TESTITEMCD = '00' ";
        $query .= " AND T1.SCORE_DIV = '08' ";
        $query .= " AND T1.CLASSCD = '99' ";
        $query .= " AND T1.SCHOOL_KIND = 'H' ";
        $query .= " AND T1.CURRICULUM_CD = '99' ";
        $query .= " AND T1.SUBCLASSCD = '999999' ";

        return $query;
    }

    //換算値データ更新処理（換算値算出）
    public function executeConvertScore($db, $model)
    {

        //校内学力試験の点数取得
        $schregScoreList = array();
        // 名称マスタから「校内学力試験」の情報を取得
        $query = knje372dQuery::executeConvertScoreSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            $score = array();

            //点数登録が無い場合は換算値をクリア
            $score["CONVERT_SCORE"] = "";
            if ($model->field["GRADE"] == $model->field["GRADE_MAX"]) {
                //3年生
                if ($row["PROFICIENCY1_SCORE1"] && $row["PROFICIENCY1_SCORE2"] && $row["PROFICIENCY1_SCORE3"] &&
                    $row["PROFICIENCY2_SCORE1"] && $row["PROFICIENCY2_SCORE2"] && $row["PROFICIENCY2_SCORE3"] &&
                    !$row["DECLINE_FLG"]) {
                    $convertScore  = $row["TOTAL_AVG"] * 2;
                    $convertScore += $row["PROFICIENCY1_AVG"];
                    $convertScore += $row["PROFICIENCY2_AVG"];
                    $convertScore = $convertScore / 4;
                    $score["CONVERT_SCORE"] = $convertScore;
                }
            } else {
                //2年生は1回目の実力テストで換算値を算出
                if ($row["PROFICIENCY1_SCORE1"] && $row["PROFICIENCY1_SCORE2"] && $row["PROFICIENCY1_SCORE3"]) {
                    $convertScore  = $row["TOTAL_AVG"] * 2;
                    $convertScore += $row["PROFICIENCY1_AVG"];
                    $convertScore += $row["PROFICIENCY2_AVG"];
                    $convertScore = $convertScore / 4;
                    $score["CONVERT_SCORE"] = $convertScore;
                }
            }
            //スコア設定
            $schregScoreList[$schregNo] = $score;
        }
        $result->free();

        //DB更新(※更新のみ 校内学力試験データ作成で作成済み)
        foreach ($schregScoreList as $schregNo => $score) {
            $data = array();
            $data["CONVERT_SCORE"][NUMBER] = $score["CONVERT_SCORE"];
            // $data["CONVERT_RANK"][NUMBER]  = $score["CONVERT_RANK"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";
            $where  = " WHERE YEAR = '".CTRL_YEAR."'";
            $where .= "   AND SCHREGNO = '{$schregNo}'";
            $query = Query::updateSQL($data, "AFT_SCHREG_CONVERT_SCORE_DAT", $where);
            $db->query($query);
        }
    }

    //総合成績データ取得SQL
    public function executeConvertScoreSql($model)
    {

        //学期は考査で選択した学期
        $semester = substr($model->field["TESTITEM"], 0, 1);
        $semester = ($semester == "9") ? $model->maxSemester : $semester;

        $query  = "";
        $query .= " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.PROFICIENCY1_SCORE1, ";
        $query .= "   T1.PROFICIENCY1_SCORE2, ";
        $query .= "   T1.PROFICIENCY1_SCORE3, ";
        $query .= "   T1.PROFICIENCY1_AVG, ";
        $query .= "   T1.PROFICIENCY2_SCORE1, ";
        $query .= "   T1.PROFICIENCY2_SCORE2, ";
        $query .= "   T1.PROFICIENCY2_SCORE3, ";
        $query .= "   T1.PROFICIENCY2_AVG, ";
        $query .= "   T1.TOTAL_AVG, ";
        $query .= "   REC_INFO.DECLINE_FLG ";
        $query .= " FROM ";
        $query .= "   AFT_SCHREG_CONVERT_SCORE_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "     ON REGD.YEAR = T1.YEAR ";
        $query .= "     AND REGD.SEMESTER = '{$semester}' ";
        $query .= "     AND REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN AFT_SCHREG_RECOMMENDATION_INFO_DAT REC_INFO ";
        $query .= "     ON REC_INFO.YEAR = T1.YEAR ";
        $query .= "     AND REC_INFO.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND REGD.GRADE = '{$model->field["GRADE"]}' ";

        return $query;
    }

    //出欠情報更新処理
    public function executeAttendAdjust($db, $model, $gradeList)
    {

        //校内学力試験の点数取得
        $schregAttendList = array();
        $schregGradeAttendList = array();
        // 名称マスタから「校内学力試験」の情報を取得
        $query = knje372dQuery::executeAttendAdjustSql($model, $gradeList);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            $attend = array();

            $attend[$row["GRADE"]]  = $row["SICK"] + $row["NOTICE"] + $row["NONOTICE"];
            $attend[$row["GRADE"]] += $row["LATE"] + $row["EARLY"];
            //出欠情報設定
            $schregAttendList[$schregNo] = $attend;
            $schregGradeAttendList[$schregNo][$row["GRADE"]] = $attend;
        }
        $result->free();

        //出欠調整点
        foreach ($schregAttendList as $schregNo => $attend) {
            $attendTotal = 0;
            $prevAttend = 0;
            $attend["ATTEND_ADJUSTMENT_SCORE"] = 0;
            foreach ($schregGradeAttendList[$schregNo] as $grade => $attend2) {
                if ($grade < $model->field["GRADE_MAX"]) {
                    //1学年・2学年のみ
                    if ($attend2[$grade] >= 56) {
                        $attend["DECLINE_FLG"] = "1";
                    } elseif ($attend2[$grade] >= 46) {
                        $attend["ATTEND_ADJUSTMENT_SCORE"] -= 5;
                    } elseif ($attend2[$grade] >= 36) {
                        $attend["ATTEND_ADJUSTMENT_SCORE"] -= 4;
                    } elseif ($attend2[$grade] >= 26) {
                        $attend["ATTEND_ADJUSTMENT_SCORE"] -= 3;
                    } elseif ($attend2[$grade] >= 16) {
                        $attend["ATTEND_ADJUSTMENT_SCORE"] -= 2;
                    }
                }
                if ($prevAttend >= 16 && $attend2[$grade] <= 3) {
                    $attend["ATTEND_ADJUSTMENT_SCORE"] += 2;
                }
                $prevAttend = $attend2[$grade];
                $attendTotal += $attend2[$grade];
            }
            //皆勤・精勤
            if ($attendTotal <= 3) {
                $attend["ATTEND_ADJUSTMENT_SCORE"] += 1;
            }
            $schregAttendList[$schregNo] = $attend;
        }

        //DB更新(※更新のみ 校内学力試験データ作成で作成済み)
        foreach ($schregAttendList as $schregNo => $attend) {
            $data = array();
            $data["ATTEND_ADJUSTMENT_SCORE"][NUMBER] = $attend["ATTEND_ADJUSTMENT_SCORE"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";
            $where  = " WHERE YEAR = '".CTRL_YEAR."'";
            $where .= "   AND SCHREGNO = '{$schregNo}'";
            $query = Query::updateSQL($data, "AFT_SCHREG_CONVERT_SCORE_DAT", $where);
            $db->query($query);

            if ($attend["DECLINE_FLG"] == "1") {
                $data = array();
                $data["DECLINE_FLG"][TEXT] = $attend["DECLINE_FLG"];

                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "sysdate()";
                $where  = " WHERE YEAR = '".CTRL_YEAR."'";
                $where .= "   AND SCHREGNO = '{$schregNo}'";
                $query = Query::updateSQL($data, "AFT_SCHREG_RECOMMENDATION_INFO_DAT", $where);
                $db->query($query);
            }
        }
    }

    //出欠情報データ取得SQL
    public function executeAttendAdjustSql($model, $gradeList)
    {

        //学期は考査で選択した学期
        $semester = substr($model->field["TESTITEM"], 0, 1);
        $semester = ($semester == "9") ? $model->maxSemester : $semester;

        $query  = "";
        $query .= " WITH SCHREG AS ( ";
        $query .= "   SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "   FROM ";
        $query .= "     AFT_SCHREG_CONVERT_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "       ON REGD.YEAR = T1.YEAR ";
        $query .= "       AND REGD.SEMESTER = '{$semester}' ";
        $query .= "       AND REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ) ";
        $query .= " , SCHREG_GRADE AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "       YEAR ";
        $query .= "     , SCHREGNO ";
        $query .= "     , GRADE ";
        $query .= "   FROM SCHREG_REGD_DAT ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , T2.GRADE ";
        $query .= "   , SUM(T1.SICK) SICK ";
        $query .= "   , SUM(T1.NOTICE) NOTICE ";
        $query .= "   , SUM(T1.NONOTICE) NONOTICE ";
        $query .= "   , SUM(T1.LATE) LATE ";
        $query .= "   , SUM(T1.EARLY) EARLY ";

        $query .= " FROM ATTEND_SEMES_DAT T1 ";
        $query .= " INNER JOIN SCHREG_GRADE T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " INNER JOIN SCHREG ";
        $query .= "     ON SCHREG.SCHREGNO = T1.SCHREGNO ";

        $query .= " WHERE ";
        $query .= "   T1.YEAR IN ( ";

        $whereArray = array();
        foreach ($gradeList as $value) {
            if ($value <= $model->field["GRADE"]) {
                $year = CTRL_YEAR - ($model->field["GRADE"] - $value);
                $whereArray[] = " '{$year}' ";
            }
        }
        $query .= implode(",", $whereArray);
        $query .= "   ) ";

        $query .= " GROUP BY ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , T2.GRADE ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , T2.GRADE ";

        return $query;
    }

    //換算値データ更新処理（順位算出）
    public function executeConvertScoreRank($db, $model)
    {
        $schregScoreList = array();
        // 換算値データ（順位算出）を取得
        $query = knje372dQuery::executeConvertScoreRankSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            $score = array();
            if ($row["CONVERT_SCORE"] > 0) {
                $score["CONVERT_RANK"] = $row["CONVERT_RANK"];
            } else {
                $score["CONVERT_RANK"] = "";
            }

            //スコア設定
            $schregScoreList[$schregNo] = $score;
        }
        $result->free();

        //DB更新(※更新のみ 校内学力試験データ作成で作成済み)
        foreach ($schregScoreList as $schregNo => $score) {
            $data = array();
            $data["CONVERT_RANK"][NUMBER]  = $score["CONVERT_RANK"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";
            $where  = " WHERE YEAR = '".CTRL_YEAR."'";
            $where .= "   AND SCHREGNO = '{$schregNo}'";
            $query = Query::updateSQL($data, "AFT_SCHREG_CONVERT_SCORE_DAT", $where);
            $db->query($query);
        }
    }

    //換算値データ（順位算出）取得SQL
    public function executeConvertScoreRankSql($model)
    {

        //学期は考査で選択した学期
        $semester = substr($model->field["TESTITEM"], 0, 1);
        $semester = ($semester == "9") ? $model->maxSemester : $semester;

        $query  = "";
        $query .= " SELECT ";
        $query .= "   RANK() OVER(ORDER BY (VALUE (T1.CONVERT_SCORE, 0) + VALUE (T1.ATTEND_ADJUSTMENT_SCORE, 0) + VALUE (T1.ADJUSTMENT_SCORE, 0)) DESC) AS CONVERT_RANK, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.CONVERT_SCORE ";
        $query .= " FROM ";
        $query .= "   AFT_SCHREG_CONVERT_SCORE_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "     ON REGD.YEAR = T1.YEAR ";
        $query .= "     AND REGD.SEMESTER = '{$semester}' ";
        $query .= "     AND REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND REGD.GRADE = '{$model->field["GRADE"]}' ";

        $query .= " ORDER BY ";
        $query .= "   (VALUE(T1.CONVERT_SCORE, 0) + VALUE(T1.ATTEND_ADJUSTMENT_SCORE, 0) + VALUE(T1.ADJUSTMENT_SCORE, 0)) DESC ";

        return $query;
    }


    //推薦基準区分更新処理
    public function executeRecommendationBaseDiv($db, $model, $gradeList)
    {

        //校内学力試験の点数取得
        $schregScoreList = array();
        // 推薦基準区分の情報を取得
        $query = knje372dQuery::executeRecommendationBaseDivSql($model, $gradeList);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            $score = array();

            $score["AGV"] = 0;
            $score["GMS_SCORE"] = 0;
            $score["GMS_CNT"] = 0;

            if (isset($schregScoreList[$schregNo])) {
                $score = $schregScoreList[$schregNo];
            }
            //評定平均値が4.3以上の教科があるかチェック(推薦基準区分算出時に使用)
            if ($row["CLASSCD"] != "99") {
                $score["ASSESS_AVG_CHECK"][$row["CLASSCD"]][] = $row["SCORE"];
            }

            //全体評定
            if ($row["CLASSCD"] == "99" && $row["CURRICULUM_CD"] == "99" && $row["SUBCLASSCD"] == "999999") {
                $score["AGV"] += $row["AVG"];
            }
            //GMS志望用(教科:英語)
            if ($row["CLASSCD"] == "18") {
                $score["GMS_SCORE"] += $row["SCORE"];
                $score["GMS_CNT"]++;
            }

            $schregScoreList[$schregNo] = $score;
        }
        $result->free();

        //推薦基準区分算出
        foreach ($schregScoreList as $schregNo => $score) {
            $avg = 0;
            $totalAvg = $score["AGV"] / 3;

            $subclassAvgCondition = false;
            foreach ($score["ASSESS_AVG_CHECK"] as $classCd => $subclassArray) {
                $subclassScoreTotal = 0;
                $subclassCnt = 0;
                foreach ($subclassArray as $subclassScore) {
                    $classScoreTotal += $subclassScore;
                    $subclassCnt++;
                }
                $classAssessAvg = $subclassScoreTotal * 1.0 / $subclassCnt ;
                if ($classAssessAvg > 4.3) {
                    $subclassAvgCondition = true;
                    break;
                }
            }


            if (3.5 <= $totalAvg) {
                $score["RECOMMENDATION_BASE_DIV"] = "ア";
            } elseif (3.2 <= $totalAvg && $subclassAvgCondition) {
                $score["RECOMMENDATION_BASE_DIV"] = "イ";
            } elseif (3.0 <= $totalAvg && $totalAvg <= 3.4) {
                $score["RECOMMENDATION_BASE_DIV"] = "ウ";
            } else {
                $score["RECOMMENDATION_BASE_DIV"] = "9";
            }
            //GMS志望の推薦基準区分
            if ($score["GMS_CNT"] > 0) {
                $gmsAvg = $score["GMS_SCORE"] / $score["GMS_CNT"];
                if ($gmsAvg < 3.0) {
                    $score["GMS_RECOMMENDATION_BASE_DIV"] = "9";
                }
            }

            $schregScoreList[$schregNo] = $score;
        }

        //DB更新(※更新のみ 校内学力試験データ作成で作成済み)
        foreach ($schregScoreList as $schregNo => $score) {
            $data = array();
            $data["RECOMMENDATION_BASE_DIV"][TEXT] = $score["RECOMMENDATION_BASE_DIV"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";
            $where  = " WHERE YEAR = '".CTRL_YEAR."'";
            $where .= "   AND SCHREGNO = '{$schregNo}'";
            $query = Query::updateSQL($data, "AFT_SCHREG_HOPE_DEPARTMENT", $where);
            $db->query($query);

            //GMS志望の更新(GMS志望のみ更新)
            $data = array();
            $data["RECOMMENDATION_BASE_DIV"][TEXT] = $score["GMS_RECOMMENDATION_BASE_DIV"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";
            $where  = " WHERE YEAR = '".CTRL_YEAR."'";
            $where .= "   AND SCHREGNO = '{$schregNo}'";
            $where .= "   AND DEPARTMENT_CD IN ('22', '72') ";
            $query = Query::updateSQL($data, "AFT_SCHREG_HOPE_DEPARTMENT", $where);
            $db->query($query);
        }
    }

    //総合成績データ取得SQL
    public function executeRecommendationBaseDivSql($model, $gradeList)
    {

        //学期は考査で選択した学期
        $semester = substr($model->field["TESTITEM"], 0, 1);
        $semester = ($semester == "9") ? $model->maxSemester : $semester;
        $setTestCd = substr($model->field["TESTITEM"], 0, 6)."09";

        $query  = "";
        $query .= " WITH SCHREG AS ( ";
        $query .= "   SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "   FROM ";
        $query .= "     AFT_SCHREG_CONVERT_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "       ON REGD.YEAR = T1.YEAR ";
        $query .= "       AND REGD.SEMESTER = '{$semester}' ";
        $query .= "       AND REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ) ";
        $query .= " , SCHREG_GRADE AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "       YEAR ";
        $query .= "     , SCHREGNO ";
        $query .= "     , GRADE ";
        $query .= "   FROM SCHREG_REGD_DAT ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.TESTKINDCD ";
        $query .= "   , T1.TESTITEMCD ";
        $query .= "   , T1.SCORE_DIV ";
        $query .= "   , T1.CLASSCD ";
        $query .= "   , T1.SCHOOL_KIND ";
        $query .= "   , T1.CURRICULUM_CD ";
        $query .= "   , T1.SUBCLASSCD ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , T2.GRADE ";
        $query .= "   , T1.SCORE ";
        $query .= "   , T1.AVG ";
        $query .= " FROM RECORD_RANK_SDIV_DAT T1 ";
        $query .= " INNER JOIN SCHREG_GRADE T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " INNER JOIN SCHREG ";
        $query .= "     ON SCHREG.SCHREGNO = T1.SCHREGNO ";

        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER || '-' || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$setTestCd}' ";

        $query .= " UNION ALL ";

        $query .= " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.TESTKINDCD ";
        $query .= "   , T1.TESTITEMCD ";
        $query .= "   , T1.SCORE_DIV ";
        $query .= "   , T1.CLASSCD ";
        $query .= "   , T1.SCHOOL_KIND ";
        $query .= "   , T1.CURRICULUM_CD ";
        $query .= "   , T1.SUBCLASSCD ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , T2.GRADE ";
        $query .= "   , T1.SCORE ";
        $query .= "   , T1.AVG ";
        $query .= " FROM RECORD_RANK_SDIV_DAT T1 ";
        $query .= " INNER JOIN SCHREG_GRADE T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " INNER JOIN SCHREG ";
        $query .= "     ON SCHREG.SCHREGNO = T1.SCHREGNO ";

        $query .= " WHERE ";
        $query .= "   (T1.YEAR, T1.SEMESTER) IN ( ";

        $whereArray = array();
        //前年度取得条件作成
        foreach ($gradeList as $value) {
            if ($value < $model->field["GRADE"]) {
                $year = CTRL_YEAR - ($model->field["GRADE"] - $value);
                $whereArray[] = " VALUES ('{$year}', '9') ";
            }
        }
        $query .= implode(" UNION ", $whereArray);
        $query .= "   ) ";
        //学年毎総合成績
        $query .= " AND T1.TESTKINDCD = '99' ";
        $query .= " AND T1.TESTITEMCD = '00' ";
        $query .= " AND T1.SCORE_DIV = '09' ";

        return $query;
    }



    //学科区分更新処理
    public function executeDepartmentBaseDiv($db, $model, $gradeList)
    {

        //生徒の情報設定リスト
        $schregList = array();

        ///////////////////////////////////////////////////
        // 生徒の情報取得
        ///////////////////////////////////////////////////
        // 学科区分の情報を取得(科目毎の評定平均値)
        $query = knje372dQuery::executeDepartmentBaseDivSql($model, $gradeList);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["CLASSCD"] == "99") {
                continue;
            }

            $schregNo = $row["SCHREGNO"];

            $subclassCd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
            $schregList[$schregNo]["ASSESS"][$subclassCd] = $row["SCORE"];
        }
        $result->free();

        // 生徒の履修コース情報を取得
        $query = knje372dQuery::selectStdCourseCdSql($model, $gradeList);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            if (!isset($schregList[$schregNo]["COURSE"])) {
                $schregList[$schregNo]["COURSE"] = array();
            }
            $courseCd = $row["COURSECD"]."-".$row["MAJORCD"]."-".$row["COURSECODE"];
            $schregList[$schregNo]["COURSE"][] = $courseCd;
        }
        $result->free();

        // 生徒の履修科目情報を取得
        $query = knje372dQuery::selectStdSubclassCdSql($model, $gradeList);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            if (!isset($schregList[$schregNo]["SUBCLASS"])) {
                $schregList[$schregNo]["SUBCLASS"] = array();
            }
            $subclassCd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
            $schregList[$schregNo]["SUBCLASS"][] = $subclassCd;
        }
        $result->free();

        ///////////////////////////////////////////////////
        // 志望学科の条件取得
        ///////////////////////////////////////////////////
        //評定平均 条件取得
        $assessAvgSubclassList = array();
        $query = knje372dQuery::selectRecommendationAssessAvgSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $assessAvg = array();
            if (isset($assessAvgSubclassList[$row["RECOMMENDATION_CD"]])) {
                $assessAvg = $assessAvgSubclassList[$row["RECOMMENDATION_CD"]];
            }
            if (!isset($assessAvg[$row["SEQ"]])) {
                $assessAvg[$row["SEQ"]]["SUBCLASS"] = array();
                $assessAvg[$row["SEQ"]]["DEPARTMENT_S"] = $row["DEPARTMENT_S"];
                $assessAvg[$row["SEQ"]]["DEPARTMENT_H"] = $row["DEPARTMENT_H"];
                $assessAvg[$row["SEQ"]]["ASSESS_AVG"] = $row["ASSESS_AVG"];
            }
            $subclassCd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
            $assessAvg[$row["SEQ"]]["SUBCLASS"][] = $subclassCd;

            $assessAvgSubclassList[$row["RECOMMENDATION_CD"]] = $assessAvg;
        }

        //必須履修科目 条件取得
        $reqdSubclassList = array();
        $query = knje372dQuery::selectRecommendationReqdSubclassSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $courseCd = $row["COURSECD"]."-".$row["MAJORCD"]."-".$row["COURSECODE"];
            $subclassCd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];

            $reqdSubclass = array();

            $reqdSubclass["DEPARTMENT_S"] = $row["DEPARTMENT_S"];
            $reqdSubclass["DEPARTMENT_H"] = $row["DEPARTMENT_H"];
            $reqdSubclass["COURSECD"] = $courseCd;
            $reqdSubclass["SUBCLASSCD"] = $subclassCd;
            $reqdSubclassList[$row["RECOMMENDATION_CD"]][] = $reqdSubclass;
        }

        //志望学科区分の条件判定
        foreach ($schregList as $schregNo => $schregInfo) {
            if (!isset($schregList[$schregNo]["CONDITION"])) {
                $schregList[$schregNo]["CONDITION"] = array();
            }

            ///////////////////////////////////////////////
            //評定平均値の判定
            foreach ($assessAvgSubclassList as $recommentdationCd => $assessAvg) {
                //志望学科条件
                $isCondition = true;
                foreach ($assessAvg as $seq => $assessInfo) {
                    $subclassTotal = 0;
                    $subclassAvg = 0;
                    $subclassCnt = 0;
                    foreach ($assessInfo["SUBCLASS"] as $subclass) {
                        if (isset($schregInfo["ASSESS"][$subclass])) {
                            $subclassTotal += $schregInfo["ASSESS"][$subclass];
                            $subclassCnt++;
                        }
                    }
                    if ($subclassCnt > 0) {
                        $subclassAvg = $subclassTotal * 1.0 / $subclassCnt;
                    }
                    //評定平均値が低い場合「9」を設定
                    if ($subclassAvg < $assessInfo["ASSESS_AVG"]) {
                        $isCondition = false;
                    }
                }
                if (!$isCondition) {
                    $condition = array();
                    $condition["DEPARTMENT_S"] = $assessInfo["DEPARTMENT_S"];
                    $condition["DEPARTMENT_H"] = $assessInfo["DEPARTMENT_H"];
                    $condition["DEPARTMENT_BASE_DIV"] = "9";
                    $schregList[$schregNo]["CONDITION"][$recommentdationCd] = $condition;
                }
            }

            ///////////////////////////////////////////////
            //必須履修科目の判定
            foreach ($reqdSubclassList as $recommentdationCd => $reqdSubclass) {
                foreach ($reqdSubclass as $reqd) {
                    $isCondition = true;
                    //コースが同じ場合は履修必須科目の判定
                    if (isset($schregInfo["COURSE"]) && in_array($reqd["COURSECD"], $schregInfo["COURSE"])) {
                        //履修必須科目がない場合「9」を設定
                        if (!isset($schregInfo["SUBCLASS"]) || !in_array($reqd["SUBCLASSCD"], $schregInfo["SUBCLASS"])) {
                            $isCondition = false;
                        }
                    }
                    if (!$isCondition) {
                        $condition = array();
                        $condition["DEPARTMENT_S"] = $reqd["DEPARTMENT_S"];
                        $condition["DEPARTMENT_H"] = $reqd["DEPARTMENT_H"];
                        $condition["DEPARTMENT_BASE_DIV"] = "9";
                        $schregList[$schregNo]["CONDITION"][$recommentdationCd] = $condition;
                    }
                }
            }
        }

        //DB更新(※更新のみ 校内学力試験データ作成で作成済み)
        foreach ($schregList as $schregNo => $schregInfo) {
            foreach ($schregInfo["CONDITION"] as $recommentdationCd => $condition) {
                //学科区分更新
                if ($condition["DEPARTMENT_BASE_DIV"] == "9") {
                    $data = array();
                    $data["DEPARTMENT_BASE_DIV"][TEXT] = $condition["DEPARTMENT_BASE_DIV"];
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][FUNC]              = "sysdate()";
                    $where  = " WHERE YEAR = '".CTRL_YEAR."' ";
                    $where .= "   AND SCHREGNO = '{$schregNo}' ";
                    $where .= "   AND DEPARTMENT_CD IN ('{$condition["DEPARTMENT_S"]}', '{$condition["DEPARTMENT_H"]}') ";
                    $query = Query::updateSQL($data, "AFT_SCHREG_HOPE_DEPARTMENT", $where);
                    $db->query($query);
                }
            }
        }
    }

    //総合成績データ取得SQL
    public function executeDepartmentBaseDivSql($model, $gradeList)
    {

        //学期は考査で選択した学期
        $semester = substr($model->field["TESTITEM"], 0, 1);
        $semester = ($semester == "9") ? $model->maxSemester : $semester;
        $setTestCd = substr($model->field["TESTITEM"], 0, 6)."09";

        $query  = "";
        $query .= " WITH SCHREG AS ( ";
        $query .= "   SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "   FROM ";
        $query .= "     AFT_SCHREG_CONVERT_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "       ON REGD.YEAR = T1.YEAR ";
        $query .= "       AND REGD.SEMESTER = '{$semester}' ";
        $query .= "       AND REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ) ";
        $query .= " , SCHREG_GRADE AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "       YEAR ";
        $query .= "     , SCHREGNO ";
        $query .= "     , GRADE ";
        $query .= "   FROM SCHREG_REGD_DAT ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.TESTKINDCD ";
        $query .= "   , T1.TESTITEMCD ";
        $query .= "   , T1.SCORE_DIV ";
        $query .= "   , T1.CLASSCD ";
        $query .= "   , T1.SCHOOL_KIND ";
        $query .= "   , T1.CURRICULUM_CD ";
        $query .= "   , T1.SUBCLASSCD ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , T2.GRADE ";
        $query .= "   , T1.SCORE ";
        $query .= "   , T1.AVG ";
        $query .= " FROM RECORD_RANK_SDIV_DAT T1 ";
        $query .= " INNER JOIN SCHREG_GRADE T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " INNER JOIN SCHREG ";
        $query .= "     ON SCHREG.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER || '-' || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$setTestCd}' ";

        $query .= " UNION ALL ";

        $query .= " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.TESTKINDCD ";
        $query .= "   , T1.TESTITEMCD ";
        $query .= "   , T1.SCORE_DIV ";
        $query .= "   , T1.CLASSCD ";
        $query .= "   , T1.SCHOOL_KIND ";
        $query .= "   , T1.CURRICULUM_CD ";
        $query .= "   , T1.SUBCLASSCD ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , T2.GRADE ";
        $query .= "   , T1.SCORE ";
        $query .= "   , T1.AVG ";
        $query .= " FROM RECORD_RANK_SDIV_DAT T1 ";
        $query .= " INNER JOIN SCHREG_GRADE T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " INNER JOIN SCHREG ";
        $query .= "     ON SCHREG.SCHREGNO = T1.SCHREGNO ";

        $query .= " WHERE ";
        $query .= "   (T1.YEAR, T1.SEMESTER) IN ( ";

        $whereArray = array();
        //前年度取得条件作成
        foreach ($gradeList as $value) {
            if ($value < $model->field["GRADE"]) {
                $year = CTRL_YEAR - ($model->field["GRADE"] - $value);
                $whereArray[] = " VALUES ('{$year}', '9') ";
            }
        }
        $query .= implode(" UNION ", $whereArray);
        $query .= "   ) ";
        //学年毎総合成績
        $query .= " AND T1.TESTKINDCD = '99' ";
        $query .= " AND T1.TESTITEMCD = '00' ";
        $query .= " AND T1.SCORE_DIV = '09' ";

        return $query;
    }

    //生徒の履修コースデータ取得SQL
    public function selectStdCourseCdSql($model, $gradeList)
    {

        //学期は考査で選択した学期
        $semester = substr($model->field["TESTITEM"], 0, 1);
        $semester = ($semester == "9") ? $model->maxSemester : $semester;

        $query  = "";
        $query .= " WITH SCHREG AS ( ";
        $query .= "   SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "   FROM ";
        $query .= "     AFT_SCHREG_CONVERT_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "       ON REGD.YEAR = T1.YEAR ";
        $query .= "       AND REGD.SEMESTER = '{$semester}' ";
        $query .= "       AND REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     SCHREGNO ";
        $query .= "   , COURSECD ";
        $query .= "   , MAJORCD ";
        $query .= "   , COURSECODE ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "   SCHREGNO IN ( SELECT SCHREGNO FROM SCHREG ) ";

        return $query;
    }

    //生徒の履修科目データ取得SQL
    public function selectStdSubclassCdSql($model, $gradeList)
    {

        //学期は考査で選択した学期
        $semester = substr($model->field["TESTITEM"], 0, 1);
        $semester = ($semester == "9") ? $model->maxSemester : $semester;

        $query  = "";
        $query .= " WITH SCHREG AS ( ";
        $query .= "   SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "   FROM ";
        $query .= "     AFT_SCHREG_CONVERT_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "       ON REGD.YEAR = T1.YEAR ";
        $query .= "       AND REGD.SEMESTER = '{$semester}' ";
        $query .= "       AND REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "   T2.SCHREGNO ";
        $query .= "   , T1.CLASSCD ";
        $query .= "   , T1.SCHOOL_KIND ";
        $query .= "   , T1.CURRICULUM_CD ";
        $query .= "   , T1.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "   CHAIR_DAT T1 ";
        $query .= "   INNER JOIN CHAIR_STD_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "   T2.SCHREGNO IN ( SELECT SCHREGNO FROM SCHREG ) ";
        
        return $query;
    }

    //大学 学科毎 評定平均値データ取得SQL
    public function selectRecommendationAssessAvgSql($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.RECOMMENDATION_CD ";
        $query .= "   , T1.DEPARTMENT_S ";
        $query .= "   , T1.DEPARTMENT_H ";
        $query .= "   , T2.SEQ ";
        $query .= "   , T2.ASSESS_AVG ";
        $query .= "   , T3.CLASSCD ";
        $query .= "   , T3.SCHOOL_KIND ";
        $query .= "   , T3.CURRICULUM_CD ";
        $query .= "   , T3.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "   AFT_RECOMMENDATION_LIMIT_MST T1 ";
        $query .= "   INNER JOIN AFT_RECOMMENDATION_ASSESS_AVG_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.RECOMMENDATION_CD = T1.RECOMMENDATION_CD ";
        $query .= "   INNER JOIN AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT T3 ";
        $query .= "     ON T3.YEAR = T2.YEAR ";
        $query .= "     AND T3.RECOMMENDATION_CD = T2.RECOMMENDATION_CD ";
        $query .= "     AND T3.SEQ = T2.SEQ ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RECOMMENDATION_CD ";
        $query .= "   , T2.SEQ ";
        $query .= "   , T3.CLASSCD ";
        $query .= "   , T3.SCHOOL_KIND ";
        $query .= "   , T3.CURRICULUM_CD ";
        $query .= "   , T3.SUBCLASSCD ";
        
        return $query;
    }

    //大学 学科毎 必須履修科目データ取得SQL
    public function selectRecommendationReqdSubclassSql($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.RECOMMENDATION_CD ";
        $query .= "   , T1.DEPARTMENT_S ";
        $query .= "   , T1.DEPARTMENT_H ";
        $query .= "   , T2.COURSECD ";
        $query .= "   , T2.MAJORCD ";
        $query .= "   , T2.COURSECODE ";
        $query .= "   , T2.CLASSCD ";
        $query .= "   , T2.SCHOOL_KIND ";
        $query .= "   , T2.CURRICULUM_CD ";
        $query .= "   , T2.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "   AFT_RECOMMENDATION_LIMIT_MST T1 ";
        $query .= "   INNER JOIN AFT_RECOMMENDATION_REQD_SUBCLASS_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.RECOMMENDATION_CD = T1.RECOMMENDATION_CD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "   T1.RECOMMENDATION_CD ";
        $query .= "   , T2.COURSECD ";
        $query .= "   , T2.MAJORCD ";
        $query .= "   , T2.COURSECODE ";
        $query .= "   , T2.CLASSCD ";
        $query .= "   , T2.SCHOOL_KIND ";
        $query .= "   , T2.CURRICULUM_CD ";
        $query .= "   , T2.SUBCLASSCD ";
        
        return $query;
    }
}
