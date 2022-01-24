<?php

require_once('for_php7.php');

class knjd210hQuery extends Query {

    //起動チェック
    function ChecktoStart($db)
    {
        return $db->getOne("SELECT COUNT(*) FROM RATEASSESS_MST WHERE YEAR = '".CTRL_YEAR."'");
    }

    //学期マスタ情報抽出
    function getSemester()
    {
        $query  = "SELECT SEMESTER,SEMESTERNAME ";
        $query .= "FROM   SEMESTER_MST ";
        $query .= "WHERE  YEAR='".CTRL_YEAR."' AND SEMESTER<>'3' ";
        $query .= "ORDER BY SEMESTER ";

        return $query;
    }

    //学年情報抽出
    function getGrade()
    {
        $query  = "SELECT GRADE, GRADE_NAME1 ";
        $query .= "FROM   SCHREG_REGD_GDAT ";
        $query .= "WHERE  YEAR='".CTRL_YEAR."' ";
        $query .= "  AND  SCHOOL_KIND='H' ";
        $query .= "ORDER BY GRADE ";

        return $query;
    }

    //生成済み一覧情報抽出
    function selectQuery()
    {
        $query  = "SELECT T1.YEAR, T1.SEMESTER, SEMESTERNAME, T1.GRADE, T3.GRADE_NAME1, T1.UPDATED ";
        $query .= "FROM   W_RATEASSESSHIST_DAT T1 ";
        $query .= "       LEFT JOIN SEMESTER_MST T2 ON T2.YEAR=T1.YEAR AND T2.SEMESTER=T1.SEMESTER ";
        $query .= "       LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
        $query .= "WHERE  T1.YEAR='".CTRL_YEAR."' ";
        $query .= "ORDER BY T1.YEAR, T1.SEMESTER, T1.GRADE ";

        return $query;
    }

    //算出対象者の情報抽出
    function getRecordQuery($model, $flg)
    {
        $query  = "";

        //成績
        $query .= "WITH T_RECORD AS ( ";
        $query .= "    SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "           SUBCLASSCD, SCHREGNO, ";
        $query .= "           SEM1_INTR_SCORE AS SCORE1_I, SEM1_TERM_SCORE AS SCORE1_T, ";
        $query .= "           SEM2_INTR_SCORE AS SCORE2_I, SEM2_TERM_SCORE AS SCORE2_T, ";
        $query .= "           SEM3_TERM_SCORE AS SCORE3_T, ";
        $query .= "           GRAD_VALUE2, GRAD_VALUE ";
        $query .= "    FROM   RECORD_DAT ";
        $query .= "    WHERE  YEAR = '".CTRL_YEAR."' ";
        $query .= "    ) ";

        //在籍
        $query .= ",T_SCHREG AS ( ";
        $query .= "    SELECT SCHREGNO, COURSECD||MAJORCD||COURSECODE AS COURSE ";
        $query .= "    FROM   SCHREG_REGD_DAT ";
        $query .= "    WHERE  YEAR = '".CTRL_YEAR."' AND ";
        if ($model->seme == "9") {
            $query .= "       SEMESTER = '".CTRL_SEMESTER."' AND ";
        } else {
            $query .= "       SEMESTER = '".$model->seme."' AND ";
        }
        $query .= "           GRADE = '".$model->grad."' ";
        $query .= "    ) ";

        //成績&在籍
        $query .= ",T_REC_SCH AS ( ";
        $query .= "    SELECT T2.COURSE, T1.* ";
        $query .= "    FROM   T_RECORD T1 ";
        $query .= "           INNER JOIN T_SCHREG T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "    WHERE  SCORE1_I IS NOT NULL OR SCORE2_I IS NOT NULL OR SCORE1_T IS NOT NULL OR SCORE2_T IS NOT NULL OR SCORE3_T IS NOT NULL ";
        $query .= "    ) ";

        //成績(コース毎科目別の各学期中間素点をカウント)
        $query .= ",T_CHK AS ( ";
        $query .= "    SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "           SUBCLASSCD,COURSE,COUNT(*) AS CNT,'1' AS SEM ";
        $query .= "    FROM   T_REC_SCH ";
        $query .= "    WHERE  SCORE1_I IS NOT NULL ";
        $query .= "    GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "           SUBCLASSCD,COURSE ";
        $query .= "    UNION ";
        $query .= "    SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "           SUBCLASSCD,COURSE,COUNT(*) AS CNT,'2' AS SEM ";
        $query .= "    FROM   T_REC_SCH ";
        $query .= "    WHERE  SCORE2_I IS NOT NULL ";
        $query .= "    GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "           SUBCLASSCD,COURSE ";
        $query .= "    ) ";

        //時間割講座テスト
        $query .= ",T_TEST AS ( ";
        $query .= "    SELECT CHAIRCD, SEMESTER, TESTKINDCD, COUNT(*) AS CNT ";
        $query .= "    FROM   SCH_CHR_TEST ";
        $query .= "    WHERE  YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         ((TESTKINDCD = '02' AND TESTITEMCD = '01') OR ";
        $query .= "          (TESTKINDCD = '01' AND TESTITEMCD = '01')) ";
        $query .= "    GROUP BY CHAIRCD, SEMESTER, TESTKINDCD ";
        $query .= "    ) ";

        //時間割講座テスト(各学期の期末のみ実施する科目を抽出)
        $query .= ",T_TESTSUM AS ( ";
        $query .= "    SELECT CHAIRCD, SEMESTER ";
        $query .= "    FROM   T_TEST ";
        $query .= "    GROUP BY CHAIRCD, SEMESTER ";
        $query .= "    HAVING SUM(SMALLINT(TESTKINDCD)) = 2 ";
        $query .= "    ) ";
        $query .= ",T_TEST_SUB AS ( ";
        $query .= "    SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ";
        }
        $query .= "           W2.SUBCLASSCD, W2.SEMESTER ";
        $query .= "    FROM   T_TESTSUM W1, CHAIR_DAT W2 ";
        $query .= "    WHERE  W2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "           W2.SEMESTER = W1.SEMESTER AND ";
        $query .= "           W2.CHAIRCD = W1.CHAIRCD ";
        $query .= "    GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ";
        }
        $query .= "           W2.SUBCLASSCD, W2.SEMESTER ";
        $query .= "    ) ";

        //コース毎に科目別個人総合点を計算
        $query .= ",T_MIX_DATA AS ( ";
        $query .= "    SELECT T1.COURSE, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
        }
        $query .= "           T1.SUBCLASSCD, T1.SCHREGNO ";
        $query .= "          ,CASE WHEN T3.SEMESTER = '1' AND T5.CNT IS NULL ";
        $query .= "                THEN SCORE1_T ELSE SCORE1_I + SCORE1_T END AS SCORE1 ";//１学期評定用
        $query .= "          ,CASE WHEN T4.SEMESTER = '2' AND T6.CNT IS NULL ";
        $query .= "                THEN SCORE2_T ELSE SCORE2_I + SCORE2_T END AS SCORE2 ";//２学期評定用
        $query .= "          ,CASE WHEN T3.SEMESTER = '1' AND T5.CNT IS NULL ";
        $query .= "                THEN ";
        $query .= "                CASE WHEN T4.SEMESTER = '2' AND T6.CNT IS NULL ";
        $query .= "                     THEN SCORE1_T + SCORE2_T ";
        $query .= "                     ELSE SCORE1_T + SCORE2_I + SCORE2_T END ";
        $query .= "                ELSE ";
        $query .= "                CASE WHEN T4.SEMESTER = '2' AND T6.CNT IS NULL ";
        $query .= "                     THEN SCORE1_I + SCORE1_T + SCORE2_T ";
        $query .= "                     ELSE SCORE1_I + SCORE1_T + SCORE2_I + SCORE2_T END ";
        $query .= "                END AS SCORE12 ";//学年評定(仮評定)用---３年生のみ使用
        if (CTRL_YEAR == '2019') {
            $query .= "          ,CASE WHEN T3.SEMESTER = '1' AND T5.CNT IS NULL ";
            $query .= "                THEN ";
            $query .= "                CASE WHEN T4.SEMESTER = '2' AND T6.CNT IS NULL ";
            $query .= "                     THEN SCORE1_T + SCORE2_T ";
            $query .= "                     ELSE SCORE1_T + SCORE2_I + SCORE2_T END ";
            $query .= "                ELSE ";
            $query .= "                CASE WHEN T4.SEMESTER = '2' AND T6.CNT IS NULL ";
            $query .= "                     THEN SCORE1_I + SCORE1_T + SCORE2_T ";
            $query .= "                     ELSE SCORE1_I + SCORE1_T + SCORE2_I + SCORE2_T END ";
            $query .= "                END AS SCORE123 ";//学年評定用
        } else {
            $query .= "          ,CASE WHEN T3.SEMESTER = '1' AND T5.CNT IS NULL ";
            $query .= "                THEN ";
            $query .= "                CASE WHEN T4.SEMESTER = '2' AND T6.CNT IS NULL ";
            $query .= "                     THEN SCORE1_T + SCORE2_T + SCORE3_T ";
            $query .= "                     ELSE SCORE1_T + SCORE2_I + SCORE2_T + SCORE3_T END ";
            $query .= "                ELSE ";
            $query .= "                CASE WHEN T4.SEMESTER = '2' AND T6.CNT IS NULL ";
            $query .= "                     THEN SCORE1_I + SCORE1_T + SCORE2_T + SCORE3_T ";
            $query .= "                     ELSE SCORE1_I + SCORE1_T + SCORE2_I + SCORE2_T + SCORE3_T END ";
            $query .= "                END AS SCORE123 ";//学年評定用
        }
        $query .= "    FROM   T_REC_SCH T1 ";
        $query .= "           LEFT JOIN T_TEST_SUB T3 ON T3.SUBCLASSCD=T1.SUBCLASSCD AND T3.SEMESTER='1' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T3.CLASSCD=T1.CLASSCD AND T3.SCHOOL_KIND=T1.SCHOOL_KIND AND T3.CURRICULUM_CD=T1.CURRICULUM_CD ";
        }
        $query .= "           LEFT JOIN T_TEST_SUB T4 ON T4.SUBCLASSCD=T1.SUBCLASSCD AND T4.SEMESTER='2' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T4.CLASSCD=T1.CLASSCD AND T4.SCHOOL_KIND=T1.SCHOOL_KIND AND T4.CURRICULUM_CD=T1.CURRICULUM_CD ";
        }
        $query .= "           LEFT JOIN T_CHK T5 ON T5.COURSE=T1.COURSE AND T5.SUBCLASSCD=T1.SUBCLASSCD AND T5.SEM='1' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T5.CLASSCD=T1.CLASSCD AND T5.SCHOOL_KIND=T1.SCHOOL_KIND AND T5.CURRICULUM_CD=T1.CURRICULUM_CD ";
        }
        $query .= "           LEFT JOIN T_CHK T6 ON T6.COURSE=T1.COURSE AND T6.SUBCLASSCD=T1.SUBCLASSCD AND T6.SEM='2' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T6.CLASSCD=T1.CLASSCD AND T6.SCHOOL_KIND=T1.SCHOOL_KIND AND T6.CURRICULUM_CD=T1.CURRICULUM_CD ";
        }
        $query .= "    ) ";

        //算出対象者を抽出
        $query .= ",T_MAIN AS ( ";
        if ($flg == "2") {
            $query .= "    SELECT COURSE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "       CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
            }
            $query .= "           SUBCLASSCD, SCHREGNO, SCORE12 AS SCORE ";
            $query .= "    FROM   T_MIX_DATA ";
            $query .= "    WHERE  SCORE12 IS NOT NULL ";
        } else {
            if ($model->seme == "1") {
                $query .= "    SELECT COURSE, ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "       CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
                }
                $query .= "           SUBCLASSCD, SCHREGNO, SCORE1 AS SCORE ";
                $query .= "    FROM   T_MIX_DATA ";
                $query .= "    WHERE  SCORE1 IS NOT NULL ";
            }
            if ($model->seme == "2") {
                $query .= "    SELECT COURSE, ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "       CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
                }
                $query .= "           SUBCLASSCD, SCHREGNO, SCORE2 AS SCORE ";
                $query .= "    FROM   T_MIX_DATA ";
                $query .= "    WHERE  SCORE2 IS NOT NULL ";
            }
            if ($model->seme == "9") {
                $query .= "    SELECT COURSE, ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "       CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
                }
                $query .= "           SUBCLASSCD, SCHREGNO, SCORE123 AS SCORE ";
                $query .= "    FROM   T_MIX_DATA ";
                $query .= "    WHERE  SCORE123 IS NOT NULL ";
            }
        }
        $query .= "    ) ";

        //算出対象者の人数
        $query .= ",T_CNT AS ( ";
        $query .= "    SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "           SUBCLASSCD,COURSE,COUNT(*) AS CNT ";
        $query .= "    FROM   T_MAIN ";
        $query .= "    GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "           SUBCLASSCD,COURSE ";
        $query .= "    ) ";

        //算出対象者の順位
        $query .= ",T_RNK AS ( ";
        $query .= "    SELECT COURSE, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "           SUBCLASSCD, SCHREGNO, SCORE, ";
        $query .= "           RANK() OVER (PARTITION BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "                                     SUBCLASSCD,COURSE ORDER BY SCORE DESC) AS RNK_SCORE ";
        $query .= "    FROM   T_MAIN ";
        $query .= "    ) ";
        $query .= ",T_PARSENT AS ( ";
        $query .= "    SELECT T1.*, ";
        $query .= "           DECIMAL(FLOAT(T1.RNK_SCORE)/T2.CNT*100,4,1) AS PARSENT ";
        $query .= "    FROM   T_RNK T1 ";
        $query .= "           LEFT JOIN T_CNT T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD AND T2.COURSE = T1.COURSE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T2.CLASSCD=T1.CLASSCD AND T2.SCHOOL_KIND=T1.SCHOOL_KIND AND T2.CURRICULUM_CD=T1.CURRICULUM_CD ";
        }
        $query .= "    ) ";

        //算出換算表
        $query .= ",T_ASSESS AS ( ";
        $query .= "    SELECT ASSESSLEVEL AS ASSESS, ASSESSLEVEL5 AS ASSESS5, RATE ";
        $query .= "    FROM   RATEASSESS_MST ";
        $query .= "    WHERE  YEAR = '".CTRL_YEAR."' ";
        $query .= "    ) ";
        $query .= ",T_ASSESS_SUM AS ( ";
        $query .= "    SELECT 10 AS ASSESS, ";
        $query .= "           SUM(RATE) AS RATE ";
        $query .= "    FROM   T_ASSESS ";
        $query .= "    WHERE  ASSESS >= 10 ";
        $query .= "    UNION ";
        $query .= "    SELECT 9 AS ASSESS, ";
        $query .= "           SUM(RATE) AS RATE ";
        $query .= "    FROM   T_ASSESS ";
        $query .= "    WHERE  ASSESS >= 9 ";
        $query .= "    UNION ";
        $query .= "    SELECT 8 AS ASSESS, ";
        $query .= "           SUM(RATE) AS RATE ";
        $query .= "    FROM   T_ASSESS ";
        $query .= "    WHERE  ASSESS >= 8 ";
        $query .= "    UNION ";
        $query .= "    SELECT 7 AS ASSESS, ";
        $query .= "           SUM(RATE) AS RATE ";
        $query .= "    FROM   T_ASSESS ";
        $query .= "    WHERE  ASSESS >= 7 ";
        $query .= "    UNION ";
        $query .= "    SELECT 6 AS ASSESS, ";
        $query .= "           SUM(RATE) AS RATE ";
        $query .= "    FROM   T_ASSESS ";
        $query .= "    WHERE  ASSESS >= 6 ";
        $query .= "    UNION ";
        $query .= "    SELECT 5 AS ASSESS, ";
        $query .= "           SUM(RATE) AS RATE ";
        $query .= "    FROM   T_ASSESS ";
        $query .= "    WHERE  ASSESS >= 5 ";
        $query .= "    UNION ";
        $query .= "    SELECT 4 AS ASSESS, ";
        $query .= "           SUM(RATE) AS RATE ";
        $query .= "    FROM   T_ASSESS ";
        $query .= "    WHERE  ASSESS >= 4 ";
        $query .= "    UNION ";
        $query .= "    SELECT 3 AS ASSESS, ";
        $query .= "           SUM(RATE) AS RATE ";
        $query .= "    FROM   T_ASSESS ";
        $query .= "    WHERE  ASSESS >= 3 ";
        $query .= "    UNION ";
        $query .= "    SELECT 2 AS ASSESS, ";
        $query .= "           SUM(RATE) AS RATE ";
        $query .= "    FROM   T_ASSESS ";
        $query .= "    WHERE  ASSESS >= 2 ";
        $query .= "    UNION ";
        $query .= "    SELECT 1 AS ASSESS, ";
        $query .= "           SUM(RATE) AS RATE ";
        $query .= "    FROM   T_ASSESS ";
        $query .= "    WHERE  ASSESS >= 1 ";
        $query .= "    ) ";
        $query .= ",T_RATE AS ( ";
        $query .= "    SELECT T1.ASSESS, T1.ASSESS5, T1.RATE, T2.RATE - T1.RATE AS RATE_LOW, T2.RATE AS RATE_HIGH ";
        $query .= "    FROM   T_ASSESS T1 ";
        $query .= "           LEFT JOIN T_ASSESS_SUM T2 ON T2.ASSESS=T1.ASSESS ";
        $query .= "    ) ";

        //メイン
        //算出対象者の評定を抽出
        $query .= "SELECT T1.COURSE, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
        }
        $query .= "       T1.SUBCLASSCD, T1.SCHREGNO, T2.ASSESS, T2.ASSESS5, ";
        $query .= "       T3.GRAD_VALUE2, T3.GRAD_VALUE ";
        $query .= "FROM   T_PARSENT T1 ";
        $query .= "       LEFT JOIN T_RATE T2 ON T2.RATE_LOW < T1.PARSENT AND T1.PARSENT <= T2.RATE_HIGH ";
        $query .= "       LEFT JOIN T_RECORD T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T3.CLASSCD=T1.CLASSCD AND T3.SCHOOL_KIND=T1.SCHOOL_KIND AND T3.CURRICULUM_CD=T1.CURRICULUM_CD ";
        }
        $query .= "ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
        }
        $query .= "       T1.SUBCLASSCD,T1.COURSE,T1.SCORE DESC ";
//echo $query;

        return $query;
    }

    //Insert作成
    function &getInsertQuery($model, $flg)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //算出対象者を配列にセット
        $opt_rec = array();
        $result = $db->query(knjd210hQuery::getRecordQuery($model, $flg));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_rec[] = $row;
        }

        //成績データを更新
        for ($i = 0; $i < get_count($opt_rec); $i++){

            $data = array();

            if ($model->seme == "1") {
                $data["SEM1_VALUE"][NUMBER]     = $opt_rec[$i]["ASSESS"];
                //高校３年生は学年評定がＮＵＬＬの場合のみ更新する
                if ($model->grad == "06" && !strlen($opt_rec[$i]["GRAD_VALUE2"]) && !strlen($opt_rec[$i]["GRAD_VALUE"])) {
                    $data["GRAD_VALUE2"][NUMBER]   = $opt_rec[$i]["ASSESS"];
                    $data["GRAD_VALUE"][NUMBER]    = $opt_rec[$i]["ASSESS5"];
                }
            }
            if ($model->seme == "2") {
                if ($flg == "2") {
                    //高校３年生は学年評定がＮＵＬＬの場合のみ更新する
                    if ($model->grad == "06" && !strlen($opt_rec[$i]["GRAD_VALUE2"]) && !strlen($opt_rec[$i]["GRAD_VALUE"])) {
                        $data["GRAD_VALUE2"][NUMBER]   = $opt_rec[$i]["ASSESS"];
                        $data["GRAD_VALUE"][NUMBER]    = $opt_rec[$i]["ASSESS5"];
                    }
                } else {
                    $data["SEM2_VALUE"][NUMBER]     = $opt_rec[$i]["ASSESS"];
                }
            }
            if ($model->seme == "9") {
                //高校３年生は学年評定がＮＵＬＬの場合のみ更新する
                if ($model->grad == "06" && !strlen($opt_rec[$i]["GRAD_VALUE2"]) && !strlen($opt_rec[$i]["GRAD_VALUE"])) {
                    $data["GRAD_VALUE2"][NUMBER]   = $opt_rec[$i]["ASSESS"];
                    $data["GRAD_VALUE"][NUMBER]    = $opt_rec[$i]["ASSESS5"];
                } else if ($model->grad == "04" || $model->grad == "05") {
                    $data["GRAD_VALUE2"][NUMBER]   = $opt_rec[$i]["ASSESS"];
                    $data["GRAD_VALUE"][NUMBER]    = $opt_rec[$i]["ASSESS5"];
                }
            }

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE YEAR           = '".CTRL_YEAR."' AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= "   CLASSCD        = '".$opt_rec[$i]["CLASSCD"]."' AND ";
                $where .= "   SCHOOL_KIND    = '".$opt_rec[$i]["SCHOOL_KIND"]."' AND ";
                $where .= "   CURRICULUM_CD  = '".$opt_rec[$i]["CURRICULUM_CD"]."' AND ";
            }
            $where .= "       SUBCLASSCD     = '".$opt_rec[$i]["SUBCLASSCD"]."' AND ";
            $where .= "       SCHREGNO       = '".$opt_rec[$i]["SCHREGNO"]."' ";

            $query = Query::updateSQL($data, "RECORD_DAT", $where);
            $db->query($query);

        }

        //履歴の削除
        $query  =  " DELETE FROM W_RATEASSESSHIST_DAT ";
        $query .=  " WHERE  YEAR = '".CTRL_YEAR."' AND ";
        $query .=  "        SEMESTER = '".$model->seme."' AND ";
        $query .=  "        GRADE = '".$model->grad."' ";
        $db->query($query);

        //履歴の追加
        $data = array();
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["SEMESTER"][TEXT]     = $model->seme;
        $data["GRADE"][TEXT]        = $model->grad;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "W_RATEASSESSHIST_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

//echo get_count($opt_rec)."<br>";

        return true;
    }

}
?>
