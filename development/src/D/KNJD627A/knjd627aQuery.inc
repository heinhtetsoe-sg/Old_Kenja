<?php

require_once('for_php7.php');

class knjd627aquery extends Query
{

    //存在チェック（在籍データ）
    public function checkToSchregno($model)
    {
        $query  = " SELECT COUNT(*) ";
        $query .= " FROM   SCHREG_REGD_DAT ";
        $query .= " WHERE  YEAR='".CTRL_YEAR."' AND GRADE='".$model->grade."' ";
        $query .= "    AND HR_CLASS IS NOT NULL ";
        $query .= "    AND COURSECD IS NOT NULL ";
        $query .= "    AND MAJORCD IS NOT NULL ";
        $query .= "    AND COURSECODE IS NOT NULL ";
        return $query;
    }

    //存在チェック（成績データ）
    public function checkToRecordScore($model)
    {
        $query  = " SELECT COUNT(*) ";
        $query .= " FROM   RECORD_RANK_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "        YEAR = '".CTRL_YEAR."' ";
        return $query;
    }

    //処理学年
    public function getGrade()
    {
        $query  = " SELECT DISTINCT GRADE AS VALUE, GRADE_NAME1 AS LABEL ";
        $query .= " FROM   SCHREG_REGD_GDAT ";
        $query .= " WHERE  YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND SCHOOL_KIND = 'H' ";
        $query .= " ORDER BY VALUE ";
        return $query;
    }

    public function getSoutenData($model)
    {
        //処理時間短縮のため、対象の校種、考査種別のみにする(過年度も対象のため、YEARは非条件)。
        $query  = " WITH FILT_TESTTYPE AS ( ";
        $query .= " SELECT ";
        $query .= "   T3.* ";
        $query .= " FROM ";
        $query .= "   RECORD_RANK_SDIV_DAT T3 ";
        $query .= " WHERE ";
        $query .= "   T3.SCHOOL_KIND = 'H' ";
        $query .= "   AND T3.SEMESTER || '-' || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV IN ('1-010101', '1-990008', '2-010101', '2-990008', '3-990008', '9-990008') ";
        // 当年度生徒の過去の"年度と学年"のセットを取得(当年度居ない生徒を拾うために"年度と学年"のセットを取ってきて、それで検索をかけるため)
        $query .= " ), GET_YEARGRADE AS ( ";
        $query .= " SELECT ";
        $query .= "   MAX(T1.YEAR) AS YEAR, ";
        $query .= "   T2.GRADE ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR <= '".CTRL_YEAR."' ";
        $query .= "   AND T2.SCHOOL_KIND = 'H' ";
        $query .= "   AND T1.SCHREGNO IN(SELECT DISTINCT ";
        $query .= "                        SCHREGNO ";
        $query .= "                      FROM ";
        $query .= "                        SCHREG_REGD_DAT ";
        $query .= "                      WHERE ";
        $query .= "                        YEAR = '".CTRL_YEAR."' ";
        $query .= "                        AND GRADE = '".$model->grade."' ";
        $query .= "                     ) ";
        $query .= "   AND T2.GRADE IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "   T2.GRADE ";
        // 当年度生徒の過去の"年度と学年"のセットからREGDDATのキーを取得
        $query .= " ), SEL_SCHREGKEY AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "   T2.SCHOOL_KIND = 'H' ";
        $query .= "   AND T1.YEAR || T1.GRADE IN(SELECT ";
        $query .= "                                TY.YEAR || TY.GRADE ";
        $query .= "                              FROM ";
        $query .= "                                GET_YEARGRADE TY ";
        $query .= "                             ) ";
        // '9'学期の試験結果を拾うために、'9'のデータを(1レコードでもREGDにデータがあれば)生成する。
        $query .= " ), ADD_SEMEALL AS ( ";
        $query .= " SELECT ";
        $query .= "   TRA.SCHREGNO, ";
        $query .= "   TRA.YEAR, ";
        $query .= "   TRA.SEMESTER AS SEMESTER, ";
        $query .= "   TRA.GRADE, ";
        $query .= "   TRA.COURSECD, ";
        $query .= "   TRA.MAJORCD, ";
        $query .= "   TRA.COURSECODE ";
        $query .= " FROM ";
        $query .= "   SEL_SCHREGKEY TA ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT TRA ";
        $query .= "     ON TRA.YEAR = TA.YEAR ";
        $query .= "    AND TRA.SEMESTER = TA.SEMESTER ";
        $query .= "    AND TRA.SCHREGNO = TA.SCHREGNO ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "   TRB.SCHREGNO, ";
        $query .= "   TRB.YEAR, ";
        $query .= "   '9' AS SEMESTER, ";
        $query .= "   TRB.GRADE, ";
        $query .= "   TRB.COURSECD, ";
        $query .= "   TRB.MAJORCD, ";
        $query .= "   TRB.COURSECODE ";
        $query .= " FROM ";
        $query .= "   SEL_SCHREGKEY TB ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT TRB ";
        $query .= "     ON TRB.YEAR = TB.YEAR ";
        $query .= "    AND TRB.SEMESTER = TB.SEMESTER ";
        $query .= "    AND TRB.SCHREGNO = TB.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   TB.SEMESTER = (SELECT MAX(TTC.SEMESTER) FROM SEL_SCHREGKEY TTC WHERE TTC.YEAR = TB.YEAR AND TTC.SCHREGNO = TB.SCHREGNO) ";
        $query .= " GROUP BY ";
        $query .= "   TRB.YEAR, ";
        $query .= "   TRB.SCHREGNO, ";
        $query .= "   TRB.GRADE, ";
        $query .= "   TRB.COURSECD, ";
        $query .= "   TRB.MAJORCD, ";
        $query .= "   TRB.COURSECODE ";
        $query .= " ORDER BY ";
        $query .= "   SCHREGNO, ";
        $query .= "   YEAR, ";
        $query .= "   SEMESTER ";
        // 総点を算出。
        $query .= " ), CALC_BASEDATA AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T2.TESTKINDCD, ";
        $query .= "   T2.TESTITEMCD, ";
        $query .= "   T2.SCORE_DIV, ";
        $query .= "   T1.COURSECODE, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   SUM(CASE WHEN T4.PASS_SCORE <= T2.SCORE THEN VALUE(T3.CREDITS, 0) ELSE 0 END) AS TOTAL_CREDIT, ";
        $query .= "   SUM(VALUE(T2.SCORE, 0) * VALUE(T3.CREDITS, 0)) AS TOTAL_POINT, ";
        $query .= "   SUM(VALUE(CASE WHEN T2.SUBCLASSCD = '900500' THEN 0 ELSE T2.SCORE END, 0) * VALUE(T3.CREDITS, 0)) AS TOTAL_POINT_IG_GS, ";
        $query .= "   SUM(VALUE(CASE WHEN T5.ELECTDIV = '1' THEN 0 ELSE T2.SCORE END, 0) * VALUE(T3.CREDITS, 0)) AS TOTAL_POINT_HISSU ";
        $query .= " FROM ";
        $query .= "   ADD_SEMEALL T1 ";
        $query .= "   LEFT JOIN FILT_TESTTYPE T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN CREDIT_MST T3 ";
        $query .= "     ON T3.YEAR = T1.YEAR ";
        $query .= "    AND T3.COURSECD = T1.COURSECD ";
        $query .= "    AND T3.MAJORCD = T1.MAJORCD ";
        $query .= "    AND T3.GRADE = T1.GRADE ";
        $query .= "    AND T3.COURSECODE = T1.COURSECODE ";
        $query .= "    AND T3.CLASSCD = T2.CLASSCD ";
        $query .= "    AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "    AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "    AND T3.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "   LEFT JOIN PERFECT_RECORD_DAT T4 ";
        $query .= "     ON T4.YEAR = T2.YEAR ";
        $query .= "    AND T4.SEMESTER = T2.SEMESTER ";
        $query .= "    AND T4.TESTKINDCD = T2.TESTKINDCD ";
        $query .= "    AND T4.TESTITEMCD = T2.TESTITEMCD ";
        $query .= "    AND T4.CLASSCD = T2.CLASSCD ";
        $query .= "    AND T4.SCHOOL_KIND =T2.SCHOOL_KIND ";
        $query .= "    AND T4.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "    AND T4.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "    AND DIV = '01' "; //DIVの値に合わせて結合は変化するので注意。
        $query .= "   LEFT JOIN V_SUBCLASS_MST T5 ";
        $query .= "     ON T5.YEAR = T2.YEAR";
        $query .= "    AND T5.CLASSCD = T2.CLASSCD";
        $query .= "    AND T5.SCHOOL_KIND = T2.SCHOOL_KIND";
        $query .= "    AND T5.CURRICULUM_CD = T2.CURRICULUM_CD";
        $query .= "    AND T5.SUBCLASSCD = T2.SUBCLASSCD";
        $query .= " WHERE ";
        $query .= "   T2.TESTKINDCD IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T2.TESTKINDCD, ";
        $query .= "   T2.TESTITEMCD, ";
        $query .= "   T2.SCORE_DIV, ";
        $query .= "   T1.COURSECODE, ";
        $query .= "   T1.SCHREGNO ";
        // 過年度分を加味した集計値を算出
        $query .= " ), CALC_SAMMARY1 AS ( ";
        $query .= " SELECT ";
        $query .= "   MAX(T1.YEAR) AS YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.TESTKINDCD, ";
        $query .= "   T1.TESTITEMCD, ";
        $query .= "   T1.SCORE_DIV, ";
        $query .= "   T1.COURSECODE, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   SUM(VALUE(T1.TOTAL_CREDIT, 0)) AS TOTAL_CREDIT, ";
        $query .= "   SUM(VALUE(T1.TOTAL_POINT, 0)) AS TOTAL_POINT ";
        $query .= " FROM ";
        $query .= "   CALC_BASEDATA T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR < '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER || '-' || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '9-990008' ";
        $query .= " GROUP BY ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.TESTKINDCD, ";
        $query .= "   T1.TESTITEMCD, ";
        $query .= "   T1.SCORE_DIV, ";
        $query .= "   T1.COURSECODE, ";
        $query .= "   T1.SCHREGNO ";
        $query .= " ), CALC_SAMMARY2 AS ( ";
        $query .= " SELECT ";
        $query .= "   MAX(T1.YEAR) AS YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.TESTKINDCD, ";
        $query .= "   T1.TESTITEMCD, ";
        $query .= "   T1.SCORE_DIV, ";
        $query .= "   T1.COURSECODE, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   SUM(VALUE(T1.TOTAL_CREDIT, 0)) AS TOTAL_CREDIT, ";
        $query .= "   SUM(VALUE(T1.TOTAL_POINT, 0)) AS TOTAL_POINT ";
        $query .= " FROM ";
        $query .= "   CALC_BASEDATA T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR <= '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER || '-' || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '9-990008' ";
        $query .= " GROUP BY ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.TESTKINDCD, ";
        $query .= "   T1.TESTITEMCD, ";
        $query .= "   T1.SCORE_DIV, ";
        $query .= "   T1.COURSECODE, ";
        $query .= "   T1.SCHREGNO ";
        $query .= " ) ";
        // 順位を確定
        $query .= " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.TESTKINDCD, ";
        $query .= "   T1.TESTITEMCD, ";
        $query .= "   T1.SCORE_DIV, ";
        $query .= "   '99' AS CLASSCD, ";
        $query .= "   'H' AS SCHOOL_KIND, ";
        $query .= "   '99' AS CURRICULUM_CD, ";
        $query .= "   '999999' AS SUBCLASSCD, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.TOTAL_CREDIT, ";
        $query .= "   T1.TOTAL_POINT, ";
        $query .= "   T1.TOTAL_POINT_IG_GS, ";
        $query .= "   T1.TOTAL_POINT_HISSU, ";
        $query .= "   RANK() OVER(PARTITION BY T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ORDER BY T1.TOTAL_POINT DESC) AS GRADE_RANK, ";
        $query .= "   RANK() OVER(PARTITION BY T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.COURSECODE ORDER BY T1.TOTAL_POINT DESC) AS COURSE_RANK, ";
        $query .= "   CASE WHEN T3.YEAR IS NOT NULL THEN T3.TOTAL_CREDIT ";
        $query .= "        WHEN T2.YEAR IS NOT NULL THEN T2.TOTAL_CREDIT ";
        $query .= "        ELSE 0 END AS SUMMARY_CREDIT, ";
        $query .= "   CASE WHEN T3.YEAR IS NOT NULL THEN T3.TOTAL_POINT ";
        $query .= "        WHEN T2.YEAR IS NOT NULL THEN T2.TOTAL_POINT ";
        $query .= "        ELSE 0 END AS SUMMARY_POINT, ";
        $query .= "   CASE WHEN T3.YEAR IS NOT NULL THEN RANK() OVER(PARTITION BY T3.YEAR, T3.SEMESTER, T3.TESTKINDCD, T3.TESTITEMCD, T3.SCORE_DIV ORDER BY T3.TOTAL_POINT DESC) ";
        $query .= "        WHEN T2.YEAR IS NOT NULL THEN RANK() OVER(PARTITION BY T2.YEAR, T2.SEMESTER, T2.TESTKINDCD, T2.TESTITEMCD, T2.SCORE_DIV ORDER BY T2.TOTAL_POINT DESC) ";
        $query .= "        ELSE 0 END AS SUMMARY_GRADE_RANK, ";
        $query .= "   CASE WHEN T3.YEAR IS NOT NULL THEN RANK() OVER(PARTITION BY T3.YEAR, T3.SEMESTER, T3.TESTKINDCD, T3.TESTITEMCD, T3.SCORE_DIV, T3.COURSECODE ORDER BY T3.TOTAL_POINT DESC) ";
        $query .= "        WHEN T2.YEAR IS NOT NULL THEN RANK() OVER(PARTITION BY T2.YEAR, T2.SEMESTER, T2.TESTKINDCD, T2.TESTITEMCD, T2.SCORE_DIV, T2.COURSECODE ORDER BY T2.TOTAL_POINT DESC) ";
        $query .= "        ELSE 0 END AS SUMMARY_COURSE_RANK ";
        $query .= " FROM ";
        $query .= "   CALC_BASEDATA T1 ";
        $query .= "   LEFT JOIN CALC_SAMMARY1 T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "    AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "    AND T2.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "    AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN CALC_SAMMARY2 T3 ";
        $query .= "     ON T3.YEAR = T1.YEAR ";
        $query .= "    AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T3.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "    AND T3.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "    AND T3.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "    AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.TESTKINDCD, ";
        $query .= "   T1.TESTITEMCD, ";
        $query .= "   T1.SCORE_DIV, ";
        $query .= "   T1.SCHREGNO ";
        return $query;
    }

    public function getUpdateQuery($model, $upddata)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($upddata as $row) {
            $where = " WHERE ";
            $where .= "   YEAR = '".$row[YEAR]."' ";
            $where .= "   AND SEMESTER = '".$row[SEMESTER]."' ";
            $where .= "   AND TESTKINDCD = '".$row[TESTKINDCD]."' ";
            $where .= "   AND TESTITEMCD = '".$row[TESTITEMCD]."' ";
            $where .= "   AND SCORE_DIV = '".$row[SCORE_DIV]."' ";
            $where .= "   AND CLASSCD = '".$row[CLASSCD]."' ";
            $where .= "   AND SCHOOL_KIND = '".$row[SCHOOL_KIND]."' ";
            $where .= "   AND CURRICULUM_CD = '".$row[CURRICULUM_CD]."' ";
            $where .= "   AND SUBCLASSCD = '".$row[SUBCLASSCD]."' ";
            $where .= "   AND SCHREGNO = '".$row[SCHREGNO]."' ";

            //存在チェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     RECORD_RANK_SDIV_SOUTEN_DAT ";
            $query .= $where;

            $dataCnt = $db->getOne($query);

            //データセット
            $data = array();

            $data["TOTAL_CREDIT"][NUMBER] = $row["TOTAL_CREDIT"];
            $data["TOTAL_POINT"][NUMBER] = $row["TOTAL_POINT"];
            $data["TOTAL_POINT_IG_GS"][NUMBER] = $row["TOTAL_POINT_IG_GS"];
            $data["TOTAL_POINT_HISSU"][NUMBER] = $row["TOTAL_POINT_HISSU"];
            $data["GRADE_RANK"][NUMBER] = $row["GRADE_RANK"];
            $data["COURSE_RANK"][NUMBER] = $row["COURSE_RANK"];
            $data["SUMMARY_CREDIT"][NUMBER] = $row["SUMMARY_CREDIT"];
            $data["SUMMARY_POINT"][NUMBER] = $row["SUMMARY_POINT"];
            $data["SUMMARY_GRADE_RANK"][NUMBER] = $row["SUMMARY_GRADE_RANK"];
            $data["SUMMARY_COURSE_RANK"][NUMBER] = $row["SUMMARY_COURSE_RANK"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC] = "SYSDATE()";
            if ($dataCnt > 0) {
                //更新
                $query = Query::updateSQL($data, "RECORD_RANK_SDIV_SOUTEN_DAT", $where);
            } else {
                $data["YEAR"][TEXT] = $row["YEAR"];
                $data["SEMESTER"][TEXT] = $row["SEMESTER"];
                $data["TESTKINDCD"][TEXT] = $row["TESTKINDCD"];
                $data["TESTITEMCD"][TEXT] = $row["TESTITEMCD"];
                $data["SCORE_DIV"][TEXT] = $row["SCORE_DIV"];
                $data["CLASSCD"][TEXT] = $row["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT] = $row["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT] = $row["CURRICULUM_CD"];
                $data["SUBCLASSCD"][TEXT] = $row["SUBCLASSCD"];
                $data["SCHREGNO"][TEXT] = $row["SCHREGNO"];
                //追加
                $query = Query::insertSQL($data, "RECORD_RANK_SDIV_SOUTEN_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
