<?php

require_once('for_php7.php');

class knjl620iQuery extends Query
{

    //名称マスタよりデータを取得
    public function getName($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3, ";
        $query .= "     ABBV3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験コンボ
    public function getTestdivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     DEFAULT_FLG AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$model->examyear}' AND ";
        $query .= "     APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コースマスタ取得
    public function getCourseMst($model)
    {
        $query .= "   SELECT ";
        $query .= "       COURSE.GENERAL_CD AS COURSE, ";
        $query .= "       COURSE.REMARK2 AS LIMIT_CNT, ";
        $query .= "       COURSE_TYPE.REMARK1 AS MAJORCD ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_GENERAL_MST COURSE ";
        $query .= "       LEFT JOIN ENTEXAM_GENERAL_MST COURSE_TYPE ";
        $query .= "           ON COURSE_TYPE.ENTEXAMYEAR    = COURSE.ENTEXAMYEAR ";
        $query .= "           AND COURSE_TYPE.APPLICANTDIV  = COURSE.APPLICANTDIV ";
        $query .= "           AND COURSE_TYPE.TESTDIV       = COURSE.TESTDIV ";
        $query .= "           AND COURSE_TYPE.GENERAL_DIV   = '01' ";
        $query .= "           AND COURSE_TYPE.GENERAL_CD    = COURSE.REMARK1 ";
        $query .= "   WHERE ";
        $query .= "           COURSE.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "       AND COURSE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "       AND COURSE.TESTDIV      = '0' ";
        $query .= "       AND COURSE.GENERAL_DIV  = '02' ";
        if ($model->majorcd != "ALL") {
            $query .= "       AND COURSE_TYPE.REMARK1 = '{$model->majorcd}' ";
        }
        $query .= "   ORDER BY ";
        $query .= "       COURSE.GENERAL_CD ";

        return $query;
    }

    //判定マークマスタ取得
    public function getJudgeMarkMst($model)
    {
        $query  = "   SELECT ";
        $query .= "       REMARK1 AS MAPPING_CD, ";
        $query .= "       GENERAL_CD AS JUDGE_MARK_CD ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_GENERAL_MST MARK ";
        $query .= "   WHERE ";
        $query .= "       MARK.ENTEXAMYEAR      = '{$model->examyear}' ";
        $query .= "       AND MARK.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "       AND MARK.TESTDIV      = '0' ";
        $query .= "       AND MARK.GENERAL_DIV  = '03' ";

        return $query;
    }

    //仮判定・確定結果
    public function selectQuery($model)
    {
        $query  = "   SELECT ";
        $query .= "       COURSE.GENERAL_CD AS PASS_COURSE_CD, ";
        $query .= "       COURSE.GENERAL_NAME AS PASS_COURSE_NAME, ";
        $query .= "       COURSE.GENERAL_ABBV AS PASS_COURSE_ABBV, ";
        $query .= "       PASSMARK.BORDER_SCORE, ";
        $query .= "       PASSMARK.SUCCESS_CNT, ";
        $query .= "       PASSMARK.JUDGE_STATE ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_GENERAL_MST COURSE ";
        $query .= "       LEFT JOIN ENTEXAM_GENERAL_MST COURSE_TYPE ";
        $query .= "           ON COURSE_TYPE.ENTEXAMYEAR    = COURSE.ENTEXAMYEAR ";
        $query .= "           AND COURSE_TYPE.APPLICANTDIV  = COURSE.APPLICANTDIV ";
        $query .= "           AND COURSE_TYPE.TESTDIV       = COURSE.TESTDIV ";
        $query .= "           AND COURSE_TYPE.GENERAL_DIV   = '01' ";
        $query .= "           AND COURSE_TYPE.GENERAL_CD    = COURSE.REMARK1 ";
        $query .= "       LEFT JOIN ENTEXAM_PASSINGMARK_MST PASSMARK ";
        $query .= "           ON PASSMARK.ENTEXAMYEAR   = COURSE.ENTEXAMYEAR ";
        $query .= "           AND PASSMARK.APPLICANTDIV = COURSE.APPLICANTDIV ";
        $query .= "           AND PASSMARK.TESTDIV      = '{$model->testdiv}' ";
        $query .= "           AND PASSMARK.EXAM_TYPE    = '1' ";
        $query .= "           AND PASSMARK.SHDIV        = '1' ";
        $query .= "           AND PASSMARK.COURSECD     = '0' ";
        $query .= "           AND PASSMARK.MAJORCD      = COURSE_TYPE.REMARK1 ";
        $query .= "           AND PASSMARK.EXAMCOURSECD = COURSE.GENERAL_CD ";
        $query .= "   WHERE ";
        $query .= "           COURSE.ENTEXAMYEAR    = '{$model->examyear}' ";
        $query .= "       AND COURSE.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "       AND COURSE.TESTDIV        = '0' "; //固定 0
        $query .= "       AND COURSE.GENERAL_DIV    = '02' ";
        if ($model->majorcd != "ALL") {
            $query .= "       AND COURSE_TYPE.REMARK1   = '{$model->majorcd}' ";
        }
        $query .= "   ORDER BY ";
        $query .= "       COURSE.GENERAL_CD ";

        return $query;
    }

    //仮判定用SQL
    public function selectSimQuery($model)
    {
        $query .= "   WITH PIVOT(HOPE_SEQ) AS (VALUES (1), (2), (3), (4)), ";   //横持ちから縦持ちに変換するための表
        $query .= "   RECEPT_DATA AS ( ";
        $query .= "           SELECT ";
        $query .= "               RECEPT1.ENTEXAMYEAR, ";
        $query .= "               RECEPT1.APPLICANTDIV, ";
        $query .= "               RECEPT1.EXAM_TYPE, ";
        $query .= "               RECEPT1.RECEPTNO, ";
        $query .= "               RECEPT1.EXAMNO, ";
        $query .= "               RECEPT1.TOTAL4, ";
        $query .= "               RECEPT1.TOTAL_RANK4, ";
        $query .= "               HOPE_SEQ, ";
        $query .= "               CASE ";
        $query .= "                 WHEN P.HOPE_SEQ = '1' "; //第一志望
        $query .= "                     THEN RECEPT1.REMARK1 ";
        $query .= "                 WHEN P.HOPE_SEQ = '2' "; //第二志望
        $query .= "                     THEN RECEPT1.REMARK2 ";
        $query .= "                 WHEN P.HOPE_SEQ = '3' "; //第三志望
        $query .= "                     THEN RECEPT1.REMARK3 ";
        $query .= "                 WHEN P.HOPE_SEQ = '4' "; //第四志望
        $query .= "                     THEN RECEPT1.REMARK4 ";
        $query .= "                 END AS HOPE_COURSE ";
        $query .= "           FROM ";
        $query .= "               ( ";
        $query .= "                   SELECT ";
        $query .= "                       RECEPT0.ENTEXAMYEAR, ";
        $query .= "                       RECEPT0.APPLICANTDIV, ";
        $query .= "                       RECEPT0.EXAM_TYPE, ";
        $query .= "                       RECEPT0.RECEPTNO, ";
        $query .= "                       BASE.EXAMNO, ";
        $query .= "                       BD031.REMARK1, ";
        $query .= "                       BD031.REMARK2, ";
        $query .= "                       BD031.REMARK3, ";
        $query .= "                       BD031.REMARK4, ";
        $query .= "                       RECEPT0.TOTAL_RANK4, ";
        $query .= "                       RECEPT0.TOTAL4 ";
        $query .= "                   FROM ";
        $query .= "                       ENTEXAM_RECEPT_DAT RECEPT0 ";
        $query .= "                       INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "                           ON BASE.ENTEXAMYEAR   = RECEPT0.ENTEXAMYEAR ";
        $query .= "                           AND BASE.APPLICANTDIV = RECEPT0.APPLICANTDIV ";
        $query .= "                           AND BASE.EXAMNO       = RECEPT0.EXAMNO ";
        $query .= "                       INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ";
        $query .= "                           ON BD031.ENTEXAMYEAR   = RECEPT0.ENTEXAMYEAR ";
        $query .= "                           AND BD031.APPLICANTDIV = RECEPT0.APPLICANTDIV ";
        $query .= "                           AND BD031.EXAMNO       = RECEPT0.EXAMNO ";
        $query .= "                           AND BD031.SEQ          = '031' ";
        $query .= "                   WHERE ";
        $query .= "                       RECEPT0.ENTEXAMYEAR      = '{$model->examyear}' ";
        $query .= "                       AND RECEPT0.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "                       AND RECEPT0.TESTDIV      = '{$model->testdiv}' ";
        $query .= "                       AND RECEPT0.EXAM_TYPE    = '1' ";
        $query .= "                       AND RECEPT0.TOTAL4 IS NOT NULL ";
        if ($model->majorcd != "ALL") {
            $query .= "                       AND BASE.TESTDIV0        = '{$model->majorcd}' ";
        }
        $query .= "                       AND VALUE(BASE.JUDGEMENT, '') NOT IN ('2', '4') ";
        $query .= "                       AND BD031.REMARK1  IS NOT NULL "; //第一志望が入っていない受験者は除外
        $query .= "               ) RECEPT1 ";
        $query .= "               CROSS JOIN PIVOT AS P ";
        $query .= "       ) ";
        $query .= "   SELECT ";
        $query .= "       T1.EXAM_TYPE, ";
        $query .= "       T1.RECEPTNO, ";
        $query .= "       T1.EXAMNO, ";
        $query .= "       T1.TOTAL_RANK4, ";
        $query .= "       T1.HOPE_SEQ, ";
        $query .= "       T1.HOPE_COURSE, ";
        $query .= "       T1.TOTAL4 ";
        $query .= "   FROM ";
        $query .= "       RECEPT_DATA T1 ";
        $query .= "   WHERE ";
        $query .= "       T1.HOPE_COURSE IS NOT NULL ";
        $query .= "   ORDER BY ";
        $query .= "       T1.TOTAL_RANK4, ";
        $query .= "       T1.HOPE_SEQ ";

        return $query;
    }

    //仮判定用SQL(未受験・不合格用)
    public function selectSimUnPassQuery($model, $div = "")
    {
        $query  = "   SELECT ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.JUDGEMENT, ";
        $query .= "     RD015.REMARK1 AS HOPE_COURSE_MARK, ";
        $query .= "     RD015.REMARK2 AS HOPE_SEQ, ";
        $query .= "     RD015.REMARK6 AS HOPE_COURSE_NO1_MARK ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "       INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "           ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= "       LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD015 ";
        $query .= "           ON RD015.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND RD015.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND RD015.TESTDIV = RECEPT.TESTDIV ";
        $query .= "           AND RD015.EXAM_TYPE = RECEPT.EXAM_TYPE ";
        $query .= "           AND RD015.RECEPTNO = RECEPT.RECEPTNO ";
        $query .= "           AND RD015.SEQ = '015' ";
        $query .= "   WHERE ";
        $query .= "       RECEPT.ENTEXAMYEAR      = '{$model->examyear}' ";
        $query .= "       AND RECEPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "       AND RECEPT.TESTDIV      = '{$model->testdiv}' ";
        $query .= "       AND RECEPT.EXAM_TYPE    = '1' ";
        if ($model->majorcd != "ALL") {
            $query .= "       AND BASE.TESTDIV0       = '{$model->majorcd}' ";
        }
        if ($div == "UNPASS_ALL") {
            $query .= "       AND (RD015.REMARK1 IS NULL OR BASE.JUDGEMENT = '4') "; //未受験(未割当) or 欠席
        } elseif ($div == "UNPASS_HOPENO1") {
            $query .= "       AND (RD015.REMARK1 IS NOT NULL AND RD015.REMARK2 <> '1') "; //第一志望に割振り不可
        }
    
        return $query;
    }

    //-----------------------------------
    //仮判定 実行
    //-----------------------------------

    public function sim($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //判定マークマスタ取得
        $judgeMarkMst = array();
        $query = knjl620iQuery::getJudgeMarkMst($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $judgeMarkMst[$row["MAPPING_CD"]] = $row["JUDGE_MARK_CD"];
        }

        //コースごとの割振り可能人数
        $courseMst = array();
        $query = knjl620iQuery::getCourseMst($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $courseMst[$row["COURSE"]] = array("LIMIT_CNT" => ($row["LIMIT_CNT"] != "") ?  $row["LIMIT_CNT"] : "0",
                                               "SUCCESS_CNT"  => "0",
                                               "MAJORCD"      => $row["MAJORCD"]
                                        );
        }

        //総合成績順に仮合格を割り振る
        $allocatedRecept = array();
        $passRank = "-1";
        $query = knjl620iQuery::selectSimQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //割振り可能順位でないand募集人数超過は割振り不可
            if ($row["TOTAL_RANK4"] != $passRank && $courseMst[$row["HOPE_COURSE"]]["LIMIT_CNT"] <= 0) {
                continue;
            }
            //既に割振り済みの場合は割振り不可
            if (in_array($row["RECEPTNO"], $allocatedRecept)) {
                continue;
            }
            //合格点未満は割振り不可
            if (!array_key_exists($row["HOPE_COURSE"], $model->field["BORDER_SCORE"]) || $model->field["BORDER_SCORE"][$row["HOPE_COURSE"]] > $row["TOTAL4"]) {
                continue;
            }

            //割振り処理
            $row["HOPE_COURSE_MARK"] = $judgeMarkMst[$row["HOPE_COURSE"]];
            $row["HOPE_COURSE_NO1_MARK"] = ($row["HOPE_SEQ"] == "1") ? $row["HOPE_COURSE_MARK"] : "";
            $query = knjl620iQuery::updateReceptDetail($db, $model, $row);
            $db->query($query);

            //後処理
            $allocatedRecept[] = $row["RECEPTNO"];
            $passRank = $row["TOTAL_RANK4"];
            $courseMst[$row["HOPE_COURSE"]]["SUCCESS_CNT"]++;
            $courseMst[$row["HOPE_COURSE"]]["LIMIT_CNT"]--;
        }

        //第一志望が不合格
        $query = knjl620iQuery::selectSimUnPassQuery($model, "UNPASS_HOPENO1");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["HOPE_COURSE_NO1_MARK"] = "9"; //9:不合格
            $query = knjl620iQuery::updateReceptDetail($db, $model, $row);
            $db->query($query);
        }

        //未受験 or 不合格
        $query = knjl620iQuery::selectSimUnPassQuery($model, "UNPASS_ALL");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["JUDGEMENT"] == "4") {
                $row["HOPE_COURSE_MARK"] = "A"; //A:欠席マークコード
                $row["HOPE_COURSE_NO1_MARK"] = "A";
            } else {
                $row["HOPE_COURSE_MARK"] = "9"; //9:不合格マークコード
                $row["HOPE_COURSE_NO1_MARK"] = "9";
            }
            $query = knjl620iQuery::updateReceptDetail($db, $model, $row);
            $db->query($query);
        }

        //仮判定結果をテーブルに登録
        //削除
        $query = knjl620iQuery::deleteAllPassingMarkDat($model);
        $db->query($query);
        foreach ($courseMst as $course => $val) {
            $simData = array();
            $simData["SUCCESS_CNT"]  = $courseMst[$course]["SUCCESS_CNT"];
            $simData["BORDER_SCORE"] = $model->field["BORDER_SCORE"][$course];
            $simData["EXAMCOURSECD"] = $course;
            $simData["MAJORCD"]      = $courseMst[$course]["MAJORCD"];

            //追加
            $query = knjl620iQuery::insertPassingMarkDat($model, $simData);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    public function getExistsReceptDetail($model, $receptNo, $seq)
    {
        $query  = "   SELECT ";
        $query .= "     COUNT(*) ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= "   WHERE ";
        $query .= "           ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "       AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "       AND EXAM_TYPE    = '1' ";
        $query .= "       AND RECEPTNO     = '{$receptNo}' ";
        $query .= "       AND SEQ          = '{$seq}' ";

        return $query;
    }

    //更新 --ENTEXAM_RECEPT_DAT 確定時に合否区分を確定
    public function updateReceptJudgeDiv($db, $model, $row)
    {
        //合格割振り
        $data = array();
        $data["JUDGEDIV"][TEXT]     = $row["JUDGEDIV"]; //1:合格
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR        = '{$model->examyear}' ";
        $where .= "     AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $where .= "     AND TESTDIV        = '{$model->testdiv}' ";
        $where .= "     AND EXAM_TYPE      = '1' ";
        $where .= "     AND RECEPTNO       = '{$row["RECEPTNO"]}' ";
        $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);

        return $query;
    }

    //更新 --ENTEXAM_APPLICANTBASE_DAT 確定時に合否区分を確定
    public function updateApplicantJudgement($db, $model, $row)
    {
        //合格割振り
        $data = array();
        $data["JUDGEMENT"][TEXT]     = $row["JUDGEMENT"]; //1:合格
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR        = '{$model->examyear}' ";
        $where .= "     AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $where .= "     AND EXAMNO         = '{$row["EXAMNO"]}' ";
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);

        return $query;
    }

    public function updateReceptDetail($db, $model, $row)
    {
        $seq = "015";
        $data = array();
        if ($model->cmd == "sim") {
            $data["REMARK1"][TEXT]    = $row["HOPE_COURSE_MARK"]; //第一志望～第四志望を含めた仮判定マーク
            $data["REMARK2"][TEXT]    = $row["HOPE_SEQ"];    //
            $data["REMARK6"][TEXT]    = $row["HOPE_COURSE_NO1_MARK"]; //第一志望のみを考えた場合の仮判定マーク
        } else {
            $data["REMARK3"][TEXT]    = $row["JUDGE_MARK_CD"];
        }
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";
        $getCount = $db->getOne(knjl620iQuery::getExistsReceptDetail($model, $row["RECEPTNO"], $seq));
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]  = $model->examyear;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["TESTDIV"][TEXT]      = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]    = '1';
            $data["RECEPTNO"][TEXT]     = $row["RECEPTNO"];
            $data["SEQ"][TEXT]          = $seq;
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR        = '{$model->examyear}' ";
            $where .= "     AND APPLICANTDIV   = '{$model->applicantdiv}' ";
            $where .= "     AND TESTDIV        = '{$model->testdiv}' ";
            $where .= "     AND EXAM_TYPE      = '1' ";
            $where .= "     AND RECEPTNO       = '{$row["RECEPTNO"]}' ";
            $where .= "     AND SEQ            = '{$seq}' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
        }

        return $query;
    }

    //入試区分単位でDELETE --ENTEXAM_PASSINGMARK_MST
    public function deleteAllPassingMarkDat($model)
    {
        $query .= "   DELETE FROM ENTEXAM_PASSINGMARK_MST ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR      = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "       AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "       AND EXAM_TYPE    = '1' ";
        $query .= "       AND SHDIV        = '1' ";
        $query .= "       AND COURSECD     = '0' ";
        if ($model->majorcd != "ALL") {
            $query .= "       AND MAJORCD      = '{$model->majorcd}' ";
        }
        
        return $query;
    }

    //INSERT --ENTEXAM_PASSINGMARK_MST
    public function insertPassingMarkDat($model, $simData)
    {
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->examyear;
        $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
        $data["TESTDIV"][TEXT]      = $model->testdiv;
        $data["EXAM_TYPE"][TEXT]    = '1';
        $data["SHDIV"][TEXT]        = "1";
        $data["COURSECD"][TEXT]     = "0";
        $data["MAJORCD"][TEXT]      = $simData["MAJORCD"];
        $data["EXAMCOURSECD"][TEXT] = $simData["EXAMCOURSECD"];
        $data["BORDER_SCORE"][TEXT] = $simData["BORDER_SCORE"];
        $data["SUCCESS_CNT"][TEXT]  = $simData["SUCCESS_CNT"];
        $data["JUDGE_STATE"][TEXT]  = "1"; //1:仮判定

        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_PASSINGMARK_MST");
        
        return $query;
    }

    //UPDATE --ENTEXAM_PASSINGMARK_MST
    public function updatePassingMarkDat($model, $simData)
    {
        $data = array();
        $data["JUDGE_STATE"][TEXT]  = $simData["JUDGE_STATE"];

        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR        = '{$model->examyear}' ";
        $where .= "     AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $where .= "     AND TESTDIV        = '{$model->testdiv}' ";
        $where .= "     AND EXAM_TYPE      = '1' ";
        $where .= "     AND SHDIV          = '1' ";
        $where .= "     AND COURSECD       = '0' ";
        $where .= "     AND MAJORCD        = '{$model->majorcd}' ";
        $where .= "     AND EXAMCOURSECD   = '{$simData["COURSE"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_PASSINGMARK_MST", $where);
        
        return $query;
    }

    //-----------------------------------
    //確定処理実行
    //-----------------------------------
    public function selectDecisionQuery(&$model)
    {
        //仮判定済母集団
        $query  = "   SELECT ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     RECEPT.EXAMNO, ";
        $query .= "     RD015.REMARK1 AS SIM_COURSE ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "       INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RD015 ";
        $query .= "           ON RD015.ENTEXAMYEAR   = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND RD015.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND RD015.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND RD015.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
        $query .= "           AND RD015.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "           AND RD015.SEQ          = '015' ";
        $query .= "   WHERE ";
        $query .= "           RECEPT.ENTEXAMYEAR     = '{$model->examyear}' ";
        $query .= "       AND RECEPT.APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "       AND RECEPT.TESTDIV         = '{$model->testdiv}' ";
        $query .= "       AND RECEPT.EXAM_TYPE       = '1' ";

        return $query;
    }

    public function updateAllReceptJudgeDiv($model, $judgeDiv)
    {
        //--ENTEXAM_RECEPT_DAT
        $query  = "   UPDATE ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "   SET ";
        if ($judgeDiv == "NULL") {
            $query .= "       JUDGEDIV   = null, ";
        } else {
            $query .= "       JUDGEDIV   = '{$judgeDiv}', ";
        }
        $query .= "       REGISTERCD = '".STAFFCD."', ";
        $query .= "       UPDATED    = sysdate() ";
        $query .= "   WHERE ";
        $query .= "       RECEPT.ENTEXAMYEAR       = '{$model->examyear}'  ";
        $query .= "       AND RECEPT.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND RECEPT.TESTDIV       = '{$model->testdiv}' ";
        $query .= "       AND EXISTS ( ";
        $query .= "                 SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "                 WHERE ";
        $query .= "                     BASE.ENTEXAMYEAR        = RECEPT.ENTEXAMYEAR ";
        $query .= "                     AND BASE.APPLICANTDIV   = RECEPT.APPLICANTDIV ";
        $query .= "                     AND BASE.EXAMNO         = RECEPT.EXAMNO ";
        if ($model->majorcd != "ALL") {
            $query .= "                     AND BASE.TESTDIV0       = '{$model->majorcd}' ";
        }
        $query .= "                     AND VALUE(BASE.JUDGEMENT, '') <> '4' ";
        $query .= "           ) ";

        return $query;
    }

    public function updateAllApplicantJudgement($model, $judgement)
    {
        //--ENTEXAM_APPLICANTBASE_DAT
        $query  = "   UPDATE ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "   SET ";
        if ($judgement == "NULL") {
            $query .= "       JUDGEMENT  = null, ";
        } else {
            $query .= "       JUDGEMENT  = '{$judgement}', ";
        }
        $query .= "       REGISTERCD = '".STAFFCD."', ";
        $query .= "       UPDATED    = sysdate() ";
        $query .= "   WHERE ";
        $query .= "       BASE.ENTEXAMYEAR       = '{$model->examyear}'  ";
        $query .= "       AND BASE.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND BASE.TESTDIV       = '{$model->testdiv}' ";
        if ($model->majorcd != "ALL") {
            $query .= "       AND BASE.TESTDIV0      = '{$model->majorcd}' ";
        }
        $query .= "       AND VALUE(BASE.JUDGEMENT, '') <> '4' ";
        
        return $query;
    }

    public function decision(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);


        //判定マークマスタ取得
        $judgeMarkMst = array();
        $query = knjl620iQuery::getJudgeMarkMst($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $judgeMarkMst[$row["MAPPING_CD"]] = $row["JUDGE_MARK_CD"];
        }

        //合格区分の割り振り前に指定条件の志願者全体を一旦不合格にする
        $judgeDiv = "2"; //2:不合格
        $query = knjl620iQuery::updateAllReceptJudgeDiv($model, $judgeDiv); //--ENTEXAM_RECEPT_DAT
        $db->query($query);

        $judgement = "2"; //2:不合格
        $query = knjl620iQuery::updateAllApplicantJudgement($model, $judgement); //--ENTEXAM_APPLICANTBASE_DAT
        $db->query($query);

        //合格割振り
        $query = knjl620iQuery::selectDecisionQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SIM_COURSE"] == "A" || $row["SIM_COURSE"] == "9") { //A:未受験 or 9:不合格
                $row["JUDGE_MARK_CD"] = $row["SIM_COURSE"];
                $row["JUDGEDIV"] = "2"; //不合格
                $row["JUDGEMENT"] = ($row["SIM_COURSE"] == "A") ? "4" : "2"; //4:欠席 or 2:不合格
            } else {
                $row["JUDGE_MARK_CD"] = $judgeMarkMst[$row["SIM_COURSE"]];
                $row["JUDGEDIV"] = "1"; //合格
                $row["JUDGEMENT"] = "1"; //合格
            }

            //RECEPT_DETAIL更新
            $query = knjl620iQuery::updateReceptDetail($db, $model, $row);
            $db->query($query);

            if ($row["SIM_COURSE"] != "A") { //未受験者はRECEPTを作らない
                //RECEPT更新
                $query = knjl620iQuery::updateReceptJudgeDiv($db, $model, $row);
                $db->query($query);
            }
            
            //BASE更新
            $query = knjl620iQuery::updateApplicantJudgement($db, $model, $row);
            $db->query($query);
        }

        //判定状態を"確定"に更新
        $query  = "   UPDATE ENTEXAM_PASSINGMARK_MST ";
        $query .= "   SET ";
        $query .= "       JUDGE_STATE   = '2', "; //2:確定
        $query .= "       REGISTERCD    = '".STAFFCD."', ";
        $query .= "       UPDATED       = sysdate() ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR       = '{$model->examyear}'  ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND TESTDIV       = '{$model->testdiv}' ";
        $query .= "       AND EXAM_TYPE     = '1' ";
        $query .= "       AND SHDIV         = '1' ";
        $query .= "       AND COURSECD      = '0' ";
        if ($model->majorcd != "ALL") {
            $query .= "       AND MAJORCD       = '{$model->majorcd}' ";
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    //-----------------------------------
    //初期化処理実行
    //-----------------------------------

    public function clear(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除 -- ENTEXAM_PASSINGMARK_MST
        $query = knjl620iQuery::deleteAllPassingMarkDat($model);
        $db->query($query);

        //NULLセット更新 -- ENTEXAM_RECEPT_DAT
        $judgeDiv = "NULL";
        $query = knjl620iQuery::updateAllReceptJudgeDiv($model, $judgeDiv);
        $db->query($query);

        //ENTEXAM_RECEPT_DETAIl(SEQ015)を削除
        $query  = "   DELETE ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_RECEPT_DETAIL_DAT RD015 ";
        $query .= "   WHERE ";
        $query .= "       RD015.ENTEXAMYEAR       = '{$model->examyear}'  ";
        $query .= "       AND RD015.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND RD015.TESTDIV       = '{$model->testdiv}' ";
        $query .= "       AND EXISTS ( ";
        $query .= "                 SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "                 WHERE ";
        $query .= "                     BASE.ENTEXAMYEAR      = RD015.ENTEXAMYEAR ";
        $query .= "                     AND BASE.APPLICANTDIV = RD015.APPLICANTDIV ";
        $query .= "                     AND BASE.EXAMNO       = RD015.RECEPTNO "; //RECEPTNOとEXAMNOは同じ
        if ($model->majorcd != "ALL") {
            $query .= "                     AND BASE.TESTDIV0     = '{$model->majorcd}' ";
        }
        $query .= "       ) ";
        $db->query($query);

        //NULLセット更新 -- ENTEXAM_APPLICANTBASE_DAT
        $judgement = "NULL";
        $query = knjl620iQuery::updateAllApplicantJudgement($model, $judgement);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
}
