<?php

require_once('for_php7.php');

class knjl030vQuery extends Query
{
    //校種コンボ
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->examyear."' ";
        $query .= "     AND NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験コンボ
    public function getExamId($model, $examId = "")
    {
        $query  = " SELECT ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY || ':' || EXAM_NAME AS LABEL, ";
        $query .= "     EXAM_SCHOOL_KIND || '-' || APPLICANT_DIV || '-' || COURSE_DIV || '-' || FREQUENCY AS VALUE, ";
        $query .= "     EXAM_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->examyear}' ";
        $query .= "     AND EXAM_SCHOOL_KIND = '{$model->schoolKind}' ";
        if ($examId) {
            $query .= "     AND EXAM_SCHOOL_KIND || '-' || APPLICANT_DIV || '-' || COURSE_DIV || '-' || FREQUENCY = '{$examId}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //履歴一覧
    public function getListRireki($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.EXEC_DATE, ";
        $query .= "     T1.EXEC_TIME, ";
        $query .= "     T1.EXAM_SCHOOL_KIND || T1.APPLICANT_DIV || T1.COURSE_DIV || T1.FREQUENCY || ':' || L1.EXAM_NAME AS EXAM_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_EXEC_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_STD_MST L1 ON L1.YEAR = T1.YEAR ";
        $query .= "         AND L1.EXAM_SCHOOL_KIND = T1.EXAM_SCHOOL_KIND ";
        $query .= "         AND L1.APPLICANT_DIV = T1.APPLICANT_DIV ";
        $query .= "         AND L1.COURSE_DIV = T1.COURSE_DIV ";
        $query .= "         AND L1.FREQUENCY = T1.FREQUENCY ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->examyear}' ";
        $query .= "     AND T1.EXAM_SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXEC_DATE DESC, ";
        $query .= "     T1.EXEC_TIME DESC ";
        return $query;
    }

    //成績集計処理
    public function getExecQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //実行履歴
        knjl030vQuery::insRireki($db, $model);

        //ENTEXAM_STD_RANK_DAT(削除)
        knjl030vQuery::delRankDat($db, $model);

        //ENTEXAM_STD_AVG_DAT(削除)
        knjl030vQuery::delAvgDat($db, $model);

        //ENTEXAM_STD_RANK_DAT(追加)

        //集計種別(S:試験得点)
        $query = knjl030vQuery::getSummaryDivScore($model);
        knjl030vQuery::insRankDat($db, $query, $model, "S");

        //(合計)
        //集計種別(2S:2科合計)(4S:4科合計)(TS:全科合計)
        $query = knjl030vQuery::getSummaryDivTotal($model, "2S");
        knjl030vQuery::insRankDat($db, $query, $model, "2S");
        $query = knjl030vQuery::getSummaryDivTotal($model, "4S");
        knjl030vQuery::insRankDat($db, $query, $model, "4S");
        $query = knjl030vQuery::getSummaryDivTotal($model, "TS");
        knjl030vQuery::insRankDat($db, $query, $model, "TS");

        //(順位)
        //集計種別(R:試験得点順位)(2R:2科順位)(4R:4科順位)(TR:全科順位)
        $query = knjl030vQuery::getSummaryDivRank($model, "S");
        knjl030vQuery::insRankDat($db, $query, $model, "R");
        $query = knjl030vQuery::getSummaryDivRank($model, "2S");
        knjl030vQuery::insRankDat($db, $query, $model, "2R");
        $query = knjl030vQuery::getSummaryDivRank($model, "4S");
        knjl030vQuery::insRankDat($db, $query, $model, "4R");
        $query = knjl030vQuery::getSummaryDivRank($model, "TS");
        knjl030vQuery::insRankDat($db, $query, $model, "TR");

        //ENTEXAM_STD_AVG_DAT(追加)

        //(標準偏差)
        //集計種別(V:試験得点標準偏差)(2V:2科標準偏差)(4V:4科標準偏差)(TV:全科標準偏差)
        $query = knjl030vQuery::getSummaryDivStddev($model, "S");
        knjl030vQuery::insAvgDat($db, $query, $model, "V");
        $query = knjl030vQuery::getSummaryDivStddev($model, "2S");
        knjl030vQuery::insAvgDat($db, $query, $model, "2V");
        $query = knjl030vQuery::getSummaryDivStddev($model, "4S");
        knjl030vQuery::insAvgDat($db, $query, $model, "4V");
        $query = knjl030vQuery::getSummaryDivStddev($model, "TS");
        knjl030vQuery::insAvgDat($db, $query, $model, "TV");

        //(平均)
        //集計種別(A:試験得点平均)(2A:2科平均)(4A:4科平均)(TA:全科平均)
        $query = knjl030vQuery::getSummaryDivAvg($model, "S");
        knjl030vQuery::insAvgDat($db, $query, $model, "A");
        $query = knjl030vQuery::getSummaryDivAvg($model, "2S");
        knjl030vQuery::insAvgDat($db, $query, $model, "2A");
        $query = knjl030vQuery::getSummaryDivAvg($model, "4S");
        knjl030vQuery::insAvgDat($db, $query, $model, "4A");
        $query = knjl030vQuery::getSummaryDivAvg($model, "TS");
        knjl030vQuery::insAvgDat($db, $query, $model, "TA");

        //ENTEXAM_STD_RANK_DAT(追加)

        //(偏差値)
        //集計種別(D:試験得点偏差値)(2D:2科偏差値)(4D:4科偏差値)(TD:全科偏差値)
        $query = knjl030vQuery::getSummaryDivDeviation($model, "S", "V", "A");
        knjl030vQuery::insRankDat($db, $query, $model, "D");
        $query = knjl030vQuery::getSummaryDivDeviation($model, "2S", "2V", "2A");
        knjl030vQuery::insRankDat($db, $query, $model, "2D");
        $query = knjl030vQuery::getSummaryDivDeviation($model, "4S", "4V", "4A");
        knjl030vQuery::insRankDat($db, $query, $model, "4D");
        $query = knjl030vQuery::getSummaryDivDeviation($model, "TS", "TV", "TA");
        knjl030vQuery::insRankDat($db, $query, $model, "TD");

        $db->commit();
        Query::dbCheckIn($db);
    }

    //実行履歴データ(追加)
    public function insRireki($db, $model)
    {
        $idArray = array();
        $idArray = explode("-", $model->examId);

        $data = array();
        $data["EXEC_DATE"][FUNC]        = "date(sysdate())";
        $data["EXEC_TIME"][FUNC]        = "time(sysdate())";
        $data["YEAR"][TEXT]             = $model->examyear;
        $data["EXAM_SCHOOL_KIND"][TEXT] = $idArray[0];
        $data["APPLICANT_DIV"][TEXT]    = $idArray[1];
        $data["COURSE_DIV"][TEXT]       = $idArray[2];
        $data["FREQUENCY"][TEXT]        = $idArray[3];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_STD_EXEC_DAT");
        $db->query($query);
    }

    //集計種別(S:試験得点)
    public function getSummaryDivScore($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T3.RECEPTNO, ";
        $query .= "     T3.EXAM_SUBCLASS, ";
        $query .= "     T3.SCORE AS EXAM_SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT T2 ON T2.YEAR = T1.YEAR AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_STD_SCORE_DAT T3 ON T3.YEAR = T1.YEAR AND T3.RECEPTNO = T1.RECEPTNO ";
        $query .= "     INNER JOIN ENTEXAM_STD_PERFECT_MST T4 ON T4.YEAR = T1.YEAR ";
        $query .= "         AND T4.EXAM_SCHOOL_KIND = T1.EXAM_SCHOOL_KIND ";
        $query .= "         AND T4.APPLICANT_DIV = T1.APPLICANT_DIV ";
        $query .= "         AND T4.COURSE_DIV = T1.COURSE_DIV ";
        $query .= "         AND T4.FREQUENCY = T1.FREQUENCY ";
        $query .= "         AND T4.EXAM_SUBCLASS = T3.EXAM_SUBCLASS ";
        $query .= "         AND T4.STEPS = 'A' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->examyear}' ";
        $query .= "     AND T1.EXAM_SCHOOL_KIND || '-' || T1.APPLICANT_DIV || '-' || T1.COURSE_DIV || '-' || T1.FREQUENCY = '{$model->examId}' ";
        $query .= " ORDER BY ";
        $query .= "     T3.RECEPTNO, ";
        $query .= "     T3.EXAM_SUBCLASS ";

        return $query;
    }

    //集計種別(2S:2科合計)(4S:4科合計)(TS:全科合計)
    public function getSummaryDivTotal($model, $summaryDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T3.RECEPTNO, ";
        $query .= "     '99' AS EXAM_SUBCLASS, ";
        $query .= "     SUM(T3.EXAM_SCORE) AS EXAM_SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT T2 ON T2.YEAR = T1.YEAR AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_STD_RANK_DAT T3 ON T3.YEAR = T1.YEAR AND T3.RECEPTNO = T1.RECEPTNO ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST N1 ON N1.ENTEXAMYEAR = T1.YEAR ";
        $query .= "         AND N1.APPLICANTDIV = T1.EXAM_SCHOOL_KIND ";
        $query .= "         AND N1.SETTING_CD = 'L009' ";
        $query .= "         AND N1.SEQ = T3.EXAM_SUBCLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->examyear}' ";
        $query .= "     AND T1.EXAM_SCHOOL_KIND || '-' || T1.APPLICANT_DIV || '-' || T1.COURSE_DIV || '-' || T1.FREQUENCY = '{$model->examId}' ";
        $query .= "     AND T3.EXAM_SCORE IS NOT NULL ";
        $query .= "     AND T3.SUMMARY_DIV = 'S' "; //試験得点
        if ($summaryDiv == "2S") {
            $query .= "     AND N1.NAMESPARE1 = '1' ";
        } elseif ($summaryDiv == "4S") {
            $query .= "     AND N1.NAMESPARE2 = '1' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T3.RECEPTNO ";
        $query .= " ORDER BY ";
        $query .= "     T3.RECEPTNO ";

        return $query;
    }

    //集計種別(R:試験得点順位)(2R:2科順位)(4R:4科順位)(TR:全科順位)
    public function getSummaryDivRank($model, $summaryDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T3.RECEPTNO, ";
        $query .= "     T3.EXAM_SUBCLASS, ";
        $query .= "     RANK() OVER(PARTITION BY T3.SUMMARY_DIV, T3.EXAM_SUBCLASS ORDER BY T3.EXAM_SCORE DESC) AS EXAM_SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT T2 ON T2.YEAR = T1.YEAR AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_STD_RANK_DAT T3 ON T3.YEAR = T1.YEAR AND T3.RECEPTNO = T1.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->examyear}' ";
        $query .= "     AND T1.EXAM_SCHOOL_KIND || '-' || T1.APPLICANT_DIV || '-' || T1.COURSE_DIV || '-' || T1.FREQUENCY = '{$model->examId}' ";
        $query .= "     AND T3.SUMMARY_DIV = '{$summaryDiv}' ";
        $query .= "     AND T3.EXAM_SCORE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T3.RECEPTNO, ";
        $query .= "     T3.EXAM_SUBCLASS, ";
        $query .= "     T3.SUMMARY_DIV ";
        
        return $query;
    }

    //集計種別(V:試験得点標準偏差)(2V:2科標準偏差)(4V:4科標準偏差)(TV:全科標準偏差)
    public function getSummaryDivStddev($model, $summaryDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T3.EXAM_SUBCLASS, ";
        $query .= "     STDDEV(T3.EXAM_SCORE) AS EXAM_SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT T2 ON T2.YEAR = T1.YEAR AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_STD_RANK_DAT T3 ON T3.YEAR = T1.YEAR AND T3.RECEPTNO = T1.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->examyear}' ";
        $query .= "     AND T1.EXAM_SCHOOL_KIND || '-' || T1.APPLICANT_DIV || '-' || T1.COURSE_DIV || '-' || T1.FREQUENCY = '{$model->examId}' ";
        $query .= "     AND T3.SUMMARY_DIV = '{$summaryDiv}' ";
        $query .= "     AND T3.EXAM_SCORE IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     T3.EXAM_SUBCLASS ";
        $query .= " ORDER BY ";
        $query .= "     T3.EXAM_SUBCLASS ";
        
        return $query;
    }

    //集計種別(A:試験得点平均)(2A:2科平均)(4A:4科平均)(TA:全科平均)
    public function getSummaryDivAvg($model, $summaryDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T3.EXAM_SUBCLASS, ";
        $query .= "     AVG(T3.EXAM_SCORE) AS EXAM_SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT T2 ON T2.YEAR = T1.YEAR AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_STD_RANK_DAT T3 ON T3.YEAR = T1.YEAR AND T3.RECEPTNO = T1.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->examyear}' ";
        $query .= "     AND T1.EXAM_SCHOOL_KIND || '-' || T1.APPLICANT_DIV || '-' || T1.COURSE_DIV || '-' || T1.FREQUENCY = '{$model->examId}' ";
        $query .= "     AND T3.SUMMARY_DIV = '{$summaryDiv}' ";
        $query .= "     AND T3.EXAM_SCORE IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     T3.EXAM_SUBCLASS ";
        $query .= " ORDER BY ";
        $query .= "     T3.EXAM_SUBCLASS ";
        
        return $query;
    }

    //集計種別(D:試験得点偏差値)(2D:2科偏差値)(4D:4科偏差値)(TD:全科偏差値)
    public function getSummaryDivDeviation($model, $summaryDiv, $summaryDivStddev, $summaryDivAvg)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T3.RECEPTNO, ";
        $query .= "     T3.EXAM_SUBCLASS, ";
        $query .= "     CASE WHEN 0 < T4.EXAM_SCORE THEN 10 * (T3.EXAM_SCORE - T5.EXAM_SCORE) / T4.EXAM_SCORE + 50 END AS EXAM_SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT T2 ON T2.YEAR = T1.YEAR AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_STD_RANK_DAT T3 ON T3.YEAR = T1.YEAR AND T3.RECEPTNO = T1.RECEPTNO ";
        $query .= "     LEFT JOIN ENTEXAM_STD_AVG_DAT T4 ON T4.YEAR = T1.YEAR ";
        $query .= "         AND T4.EXAM_SCHOOL_KIND = T1.EXAM_SCHOOL_KIND ";
        $query .= "         AND T4.APPLICANT_DIV = T1.APPLICANT_DIV ";
        $query .= "         AND T4.COURSE_DIV = T1.COURSE_DIV ";
        $query .= "         AND T4.FREQUENCY = T1.FREQUENCY ";
        $query .= "         AND T4.EXAM_SUBCLASS = T3.EXAM_SUBCLASS ";
        $query .= "         AND T4.SUMMARY_DIV = '{$summaryDivStddev}' ";
        $query .= "     LEFT JOIN ENTEXAM_STD_AVG_DAT T5 ON T5.YEAR = T1.YEAR ";
        $query .= "         AND T5.EXAM_SCHOOL_KIND = T1.EXAM_SCHOOL_KIND ";
        $query .= "         AND T5.APPLICANT_DIV = T1.APPLICANT_DIV ";
        $query .= "         AND T5.COURSE_DIV = T1.COURSE_DIV ";
        $query .= "         AND T5.FREQUENCY = T1.FREQUENCY ";
        $query .= "         AND T5.EXAM_SUBCLASS = T3.EXAM_SUBCLASS ";
        $query .= "         AND T5.SUMMARY_DIV = '{$summaryDivAvg}' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->examyear}' ";
        $query .= "     AND T1.EXAM_SCHOOL_KIND || '-' || T1.APPLICANT_DIV || '-' || T1.COURSE_DIV || '-' || T1.FREQUENCY = '{$model->examId}' ";
        $query .= "     AND T3.SUMMARY_DIV = '{$summaryDiv}' ";
        $query .= "     AND T3.EXAM_SCORE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T3.RECEPTNO, ";
        $query .= "     T3.EXAM_SUBCLASS, ";
        $query .= "     T3.SUMMARY_DIV ";
        
        return $query;
    }

    //ENTEXAM_STD_RANK_DAT(削除)
    public function delRankDat($db, $model)
    {
        $query  = "";
        $query .= " DELETE FROM ";
        $query .= "     ENTEXAM_STD_RANK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->examyear}' ";
        $query .= "     AND RECEPTNO IN ( ";
        $query .= "         SELECT ";
        $query .= "             T1.RECEPTNO ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_STD_RECEPT_DAT T1 ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = '{$model->examyear}' ";
        $query .= "             AND T1.EXAM_SCHOOL_KIND || '-' || T1.APPLICANT_DIV || '-' || T1.COURSE_DIV || '-' || T1.FREQUENCY = '{$model->examId}' ";
        $query .= "     ) ";

        $db->query($query);
    }

    //ENTEXAM_STD_RANK_DAT(追加)
    public function insRankDat($db, $query, $model, $summaryDiv)
    {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = $model->examyear;
            $data["RECEPTNO"][TEXT]         = $row["RECEPTNO"];
            $data["EXAM_SUBCLASS"][TEXT]    = $row["EXAM_SUBCLASS"];
            $data["SUMMARY_DIV"][TEXT]      = $summaryDiv;
            $data["EXAM_SCORE"][NUMBER]     = $row["EXAM_SCORE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            $query = Query::insertSQL($data, "ENTEXAM_STD_RANK_DAT");
            $db->query($query);
        }
        $result->free();
    }

    //ENTEXAM_STD_AVG_DAT(削除)
    public function delAvgDat($db, $model)
    {
        $query  = "";
        $query .= " DELETE FROM ";
        $query .= "     ENTEXAM_STD_AVG_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->examyear}' ";
        $query .= "     AND EXAM_SCHOOL_KIND || '-' || APPLICANT_DIV || '-' || COURSE_DIV || '-' || FREQUENCY = '{$model->examId}' ";

        $db->query($query);
    }

    //ENTEXAM_STD_AVG_DAT(追加)
    public function insAvgDat($db, $query, $model, $summaryDiv)
    {
        $idArray = array();
        $idArray = explode("-", $model->examId);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = $model->examyear;
            $data["EXAM_SCHOOL_KIND"][TEXT] = $idArray[0];
            $data["APPLICANT_DIV"][TEXT]    = $idArray[1];
            $data["COURSE_DIV"][TEXT]       = $idArray[2];
            $data["FREQUENCY"][TEXT]        = $idArray[3];
            $data["EXAM_SUBCLASS"][TEXT]    = $row["EXAM_SUBCLASS"];
            $data["SUMMARY_DIV"][TEXT]      = $summaryDiv;
            $data["EXAM_SCORE"][NUMBER]     = $row["EXAM_SCORE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            $query = Query::insertSQL($data, "ENTEXAM_STD_AVG_DAT");
            $db->query($query);
        }
        $result->free();
    }

    //CSV出力
    public function getCsvList($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T3.RECEPTNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.EXAM_SUBCLASS, ";
        $query .= "     T3.SUMMARY_DIV, ";
        $query .= "     T3.EXAM_SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT T2 ON T2.YEAR = T1.YEAR AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_STD_RANK_DAT T3 ON T3.YEAR = T1.YEAR AND T3.RECEPTNO = T1.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->examyear}' ";
        $query .= "     AND T1.EXAM_SCHOOL_KIND || '-' || T1.APPLICANT_DIV || '-' || T1.COURSE_DIV || '-' || T1.FREQUENCY = '{$model->examId}' ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     NULL AS RECEPTNO, ";
        $query .= "     NULL AS NAME, ";
        $query .= "     T3.EXAM_SUBCLASS, ";
        $query .= "     T3.SUMMARY_DIV, ";
        $query .= "     T3.EXAM_SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_AVG_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T3.YEAR = '{$model->examyear}' ";
        $query .= "     AND T3.EXAM_SCHOOL_KIND || '-' || T3.APPLICANT_DIV || '-' || T3.COURSE_DIV || '-' || T3.FREQUENCY = '{$model->examId}' ";
        $query .= " ORDER BY ";
        $query .= "     RECEPTNO, ";
        $query .= "     EXAM_SUBCLASS, ";
        $query .= "     SUMMARY_DIV ";
        
        return $query;
    }
}
