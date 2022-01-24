<?php

require_once('for_php7.php');


class knjd321query extends Query {
    //1：法定時数・2：実時数
    function getJugyouJisuFlg() {
        $query  = " SELECT ";
        $query .= "     JUGYOU_JISU_FLG ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //上限値(分数)
    function getSyuutokuJougenti($model) {
        $query  = " SELECT ";
        $query .= "     SYUTOKU_BUNSI || '/' || SYUTOKU_BUNBO AS SYUTOKU_JOUGENTI, ";
        $query .= "     RISYU_BUNSI   || '/' || RISYU_BUNBO   AS RISYU_JOUGENTI, ";
        $query .= "     SYUTOKU_BUNSI_SPECIAL || '/' || SYUTOKU_BUNBO_SPECIAL AS SYUTOKU_JOUGENTI_SPECIAL, ";
        $query .= "     RISYU_BUNSI_SPECIAL   || '/' || RISYU_BUNBO_SPECIAL   AS RISYU_JOUGENTI_SPECIAL ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        if ($model->jugyou_jisu_flg == '1') { //1：法定時数 2：実時数
            $query .= "     JUGYOU_JISU_FLG = '1' ";
        } else {
            $query .= "     JUGYOU_JISU_FLG = '2' ";
        }
        return $query;
    }

    // 遅刻何回で欠課とするかの指数取得
    function getScAbsentCov() {
        $db = Query::dbCheckOut();
        $query = "SELECT * FROM v_school_mst WHERE year = '". CTRL_YEAR ."'";
        $absent = array();
        $absent = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $absent;
    }

    //出欠集計開始日付などを取得
    function getAttendDate($model) {
        $semester = ($model->field["GAKKI2"] == 9) ? CTRL_SEMESTER : $model->field["GAKKI2"];
        $query  = "SELECT SEMESTER ";
        $query .= "      ,MAX(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END) AS MAX_YEAR ";
        $query .= "      ,MONTH ";
        $query .= "      ,MAX(APPOINTED_DAY) AS MAX_APP ";
        $query .= "FROM   ATTEND_SEMES_DAT ";
        $query .= "WHERE  YEAR='".CTRL_YEAR."' AND SEMESTER <= '{$semester}' ";
        $query .= "GROUP BY SEMESTER,MONTH ";
        $query .= "ORDER BY 2,3,1 ";

        return $query;
    }

    //学期取得
    function getSelectSeme() {
        $query  = " SELECT DISTINCT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER";

        return $query;
    }

    //学年取得
    function getSelectGrade() {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //ABC評定科目
    function getABCHyoutei() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND NAMECD1 = 'D065' ";
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

/**************************************************************************************************/
/**************************************************************************************************/
/**************************************************************************************************/
    //成績不振者
    function selectListQuery($model) {
        $ctrl_year = CTRL_YEAR;
        $semester = ($model->field["GAKKI2"] == 9) ? CTRL_SEMESTER : $model->field["GAKKI2"];
        $date = str_replace("/","-",$model->field["DATE"]);

        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T3.HR_NAME, ";
        $query .= "         T2.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1";
        $query .= "     INNER JOIN ";
        $query .= "         SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN ";
        $query .= "         SCHREG_REGD_HDAT T3 ON T1.YEAR = T3.YEAR ";
        $query .= "                            AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "                            AND T1.GRADE = T3.GRADE ";
        $query .= "                            AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '{$ctrl_year}' ";
        $query .= "         AND T1.SEMESTER = '{$semester}' ";
        $query .= "         AND VALUE(T2.GRD_DATE, '9999-12-31') > '{$date}' ";
        //在校生検索を使用する場合、該当生徒のみ表示する。
        if (strlen($model->search_schregno)) {
            $query .= "     AND T1.SCHREGNO = '".$model->search_schregno."' ";
        } else if ($model->field["GRADE"] != '99') {
            $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        }

        //1:評定
        $query .= " ), RECORD_SCORE AS( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T3.SUBCLASSNAME, ";
        $query .= "         T1.SEM1_REC, ";
        $query .= "         T1.SEM2_REC, ";
        $query .= "         T1.SEM3_TERM_REC, ";
        $query .= "         T1.GRADE_RECORD, ";
        $query .= "         T1.A_PATTERN_ASSESS, ";
        $query .= "         T1.B_PATTERN_ASSESS, ";
        $query .= "         T1.C_PATTERN_ASSESS, ";
        $query .= "         T1.JUDGE_PATTERN, ";
        $query .= "         CASE WHEN T1.JUDGE_PATTERN = 'A' THEN DEC(T1.A_PATTERN_ASSESS) ";
        $query .= "              WHEN T1.JUDGE_PATTERN = 'B' THEN DEC(T1.B_PATTERN_ASSESS) ";
        $query .= "              WHEN T1.JUDGE_PATTERN = 'C' THEN DEC(T1.C_PATTERN_ASSESS) ";
        $query .= "              ELSE CAST(NULL AS SMALLINT) END AS PATTERN_ASSESS, ";
        $query .= "         T4.CREDITS, ";
        $query .= "         T4.CREDITS AS COMP_CREDIT, ";
        $query .= "         T4.CREDITS AS GET_CREDIT ";
        $query .= "     FROM ";
        $query .= "         KIN_RECORD_DAT T1 ";
        $query .= "         INNER JOIN SCHNO           T2 ON  T2.SCHREGNO   = T1.SCHREGNO ";
        $query .= "         LEFT  JOIN SUBCLASS_MST    T3 ON  T3.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                      AND T3.CLASSCD = T1.CLASSCD ";
            $query .= "                                      AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                      AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         LEFT  JOIN CREDIT_MST      T4 ON  T4.YEAR       = T1.YEAR ";
        $query .= "                                       AND T4.COURSECD   = T2.COURSECD ";
        $query .= "                                       AND T4.MAJORCD    = T2.MAJORCD ";
        $query .= "                                       AND T4.GRADE      = T2.GRADE ";
        $query .= "                                       AND T4.COURSECODE = T2.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                      AND T4.CLASSCD = T1.CLASSCD ";
            $query .= "                                      AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                      AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                       AND T4.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '{$ctrl_year}' ";

        //2:欠課時数
        $query .= " ),ATTEND_SUBCLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         SUM(VALUE(T1.LESSON,0) - VALUE(T1.SUSPEND,0) ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "                            - VALUE(T1.VIRUS,0) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "                            - VALUE(T1.KOUDOME,0) ";
        }
        $query .= "                                - VALUE(T1.MOURNING,0)) AS LESSON_JISUU, ";
        $query .= "        (SUM(VALUE(T1.LATE,0) + VALUE(T1.EARLY,0)) / 3) + ";
        $query .= "         SUM(VALUE(T1.SICK,0) + VALUE(T1.NOTICE,0) + VALUE(T1.NONOTICE,0) + VALUE(T1.NURSEOFF,0)) AS KEKKA_JISUU ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT T1 ";
        $query .= "         INNER JOIN SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '{$ctrl_year}' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";

        //3:出席日数
        $query .= " ),ATTEND_SEMES AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         SUM(VALUE(T1.LESSON,0) - VALUE(T1.SUSPEND,0) ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "                            - VALUE(T1.VIRUS,0) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "                            - VALUE(T1.KOUDOME,0) ";
        }
        $query .= "                                - VALUE(T1.MOURNING,0)) AS LESSON_NISUU, ";
        $query .= "         SUM(VALUE(T1.SICK,0) + VALUE(T1.NOTICE,0) + VALUE(T1.NONOTICE,0)) AS KESSEKI_NISUU ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT T1 ";
        $query .= "         INNER JOIN SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '{$ctrl_year}' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";

        /********/
        /* 評定 */
        /********/
        $query .= " ), SUB_MAIN_RECORD AS ( ";
        $query .= "     SELECT ";
        $query .= "         T3.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.CLASSCD, ";
            $query .= "         T3.SCHOOL_KIND, ";
            $query .= "         T3.CURRICULUM_CD, ";
        }
        $query .= "         T3.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE T3 ";
        $query .= "     WHERE ";
        if ($model->field["KYOUKA_SOUGOU1"] == '1' && $model->field["KYOUKA_SOUGOU2"] == '1') {
            $query .= "     ( ";
        }
        //教科・科目
        if ($model->field["KYOUKA_SOUGOU1"] == '1') {
            $query .= "     ( ";
            if (strlen($model->field["SEISEKI_HUSIN_HYOUTEI_FROM"]) && strlen($model->field["SEISEKI_HUSIN_HYOUTEI_TO"])) {
                $query .= "    (T3.PATTERN_ASSESS  >= {$model->field["SEISEKI_HUSIN_HYOUTEI_FROM"]} ";
                $query .= " AND T3.PATTERN_ASSESS  <= {$model->field["SEISEKI_HUSIN_HYOUTEI_TO"]}) ";
            } elseif (strlen($model->field["SEISEKI_HUSIN_HYOUTEI_TO"])) {
                $query .= "    (T3.PATTERN_ASSESS  <= {$model->field["SEISEKI_HUSIN_HYOUTEI_TO"]} ";
                $query .= " OR T3.PATTERN_ASSESS IS NULL) ";
            } else {
                $query .= " T3.PATTERN_ASSESS IS NULL ";
            }
            $query .= "     AND SUBSTR(T3.SUBCLASSCD,1,2) < '90' ";
            $query .= "     AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ";
            $query .= "         NOT IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '{$ctrl_year}' AND NAMECD1 = 'D065') ";
            $query .= "     ) ";
            $query .= "     OR ";
            $query .= "     ( ";
            $query .= "     T3.GRADE_RECORD IS NULL ";
            $query .= "     AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ";
            $query .= "         IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '{$ctrl_year}' AND NAMECD1 = 'D065') ";
            $query .= "     ) ";
        }
        if ($model->field["KYOUKA_SOUGOU1"] == '1' && $model->field["KYOUKA_SOUGOU2"] == '1') {
            $query .= "     ) OR ( ";
        }
        //総合的な時間
        if ($model->field["KYOUKA_SOUGOU2"] == '1') {
            $query .= "     T3.GRADE_RECORD IS NULL ";
            $query .= "     AND SUBSTR(T3.SUBCLASSCD,1,2) = '90' ";
        }
        if ($model->field["KYOUKA_SOUGOU1"] == '1' && $model->field["KYOUKA_SOUGOU2"] == '1') {
            $query .= "     ) ";
        }

        /************/
        /* 欠課時数 */
        /************/
        $query .= " ), SUB_MAIN_SUBCLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS T1 ";
        $query .= "         INNER JOIN SCHNO           T2 ON  T2.SCHREGNO   = T1.SCHREGNO ";
        $query .= "         LEFT  JOIN CREDIT_MST      T4 ON  T4.YEAR       = '{$ctrl_year}' ";
        $query .= "                                       AND T4.COURSECD   = T2.COURSECD ";
        $query .= "                                       AND T4.MAJORCD    = T2.MAJORCD ";
        $query .= "                                       AND T4.GRADE      = T2.GRADE ";
        $query .= "                                       AND T4.COURSECODE = T2.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                      AND T4.CLASSCD = T1.CLASSCD ";
            $query .= "                                      AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                      AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                       AND T4.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         T4.CREDITS IS NOT NULL AND ";
        $query .= "         ((T4.CREDITS * (CASE WHEN T2.GRADE = '03' THEN 8 ELSE 10 END)) + 1) < T1.KEKKA_JISUU ";

        /************/
        /* 出席日数 */
        /************/
        $query .= " ), SUB_MAIN_SEMES AS ( ";
        $query .= "     SELECT ";
        $query .= "         T3.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         '99' AS CLASSCD, ";
            $query .= "         '00' AS SCHOOL_KIND, ";
            $query .= "         '00' AS CURRICULUM_CD, ";
        }
        $query .= "         '999999' AS SUBCLASSCD "; //ダミー
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES T3 ";
        $query .= "     WHERE ";
        $query .= "         T3.LESSON_NISUU < (T3.KESSEKI_NISUU * 3) ";

        /**************************/
        /* 対象の生徒・科目を取得 */
        /**************************/
        $query .= " ), SUB_MAIN AS ( ";
        //在校生検索を使用する場合、該当生徒の全ての科目を表示する。
        if (strlen($model->search_schregno)) {
            $query .= "     SELECT ";
            $query .= "         T1.SCHREGNO, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         T1.CLASSCD, ";
                $query .= "         T1.SCHOOL_KIND, ";
                $query .= "         T1.CURRICULUM_CD, ";
            }
            $query .= "         T1.SUBCLASSCD ";
            $query .= "     FROM ";
            $query .= "         RECORD_SCORE T1 ";
        } else {
            if ($model->field["SEISEKI_HUSIN1"] == '1') {
                $query .= "     SELECT ";
                $query .= "         T1.SCHREGNO, ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         T1.CLASSCD, ";
                    $query .= "         T1.SCHOOL_KIND, ";
                    $query .= "         T1.CURRICULUM_CD, ";
                }
                $query .= "         T1.SUBCLASSCD ";
                $query .= "     FROM ";
                $query .= "         SUB_MAIN_RECORD T1 ";
            }
            if ($model->field["SEISEKI_HUSIN2"] == '1') {
                if ($model->field["SEISEKI_HUSIN1"] == '1') {
                    $query .= " UNION ";
                }
                $query .= "     SELECT ";
                $query .= "         T1.SCHREGNO, ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         T1.CLASSCD, ";
                    $query .= "         T1.SCHOOL_KIND, ";
                    $query .= "         T1.CURRICULUM_CD, ";
                }
                $query .= "         T1.SUBCLASSCD ";
                $query .= "     FROM ";
                $query .= "         RECORD_SCORE T1 ";
                $query .= "     INNER JOIN SUB_MAIN_SUBCLASS T2 ON  T2.SCHREGNO   = T1.SCHREGNO ";
                $query .= "                                     AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                                      AND T2.CLASSCD = T1.CLASSCD ";
                    $query .= "                                      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "                                      AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
            }
            if ($model->field["SEISEKI_HUSIN3"] == '1') {
                if ($model->field["SEISEKI_HUSIN1"] == '1' || $model->field["SEISEKI_HUSIN2"] == '1') {
                    $query .= " UNION ";
                }
                $query .= "     SELECT ";
                $query .= "         T1.SCHREGNO, ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         T1.CLASSCD, ";
                    $query .= "         T1.SCHOOL_KIND, ";
                    $query .= "         T1.CURRICULUM_CD, ";
                }
                $query .= "         T1.SUBCLASSCD ";
                $query .= "     FROM ";
                $query .= "         RECORD_SCORE T1 ";
                $query .= "     INNER JOIN SUB_MAIN_SEMES    T3 ON  T3.SCHREGNO   = T1.SCHREGNO ";
            }
        }


        //メイン
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.NAME, ";
        $query .= "     L2.SUBCLASSNAME, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L2.CLASSCD, ";
            $query .= "         L2.SCHOOL_KIND, ";
            $query .= "         L2.CURRICULUM_CD, ";
        }
        $query .= "     L2.SUBCLASSCD, ";
        $query .= "     T3.SEM1_REC, ";
        $query .= "     T3.SEM2_REC, ";
        $query .= "     T3.SEM3_TERM_REC, ";
        $query .= "     T3.GRADE_RECORD, ";
        $query .= "     T3.A_PATTERN_ASSESS, ";
        $query .= "     T3.B_PATTERN_ASSESS, ";
        $query .= "     T3.C_PATTERN_ASSESS, ";
        $query .= "     T3.JUDGE_PATTERN, ";
        $query .= "     T3.PATTERN_ASSESS, ";
        $query .= "     T3.CREDITS, ";
        $query .= "     CASE WHEN L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD ";
        $query .= "               NOT IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '{$ctrl_year}' AND NAMECD1 = 'D065') ";
        $query .= "               AND T3.PATTERN_ASSESS > 0 THEN T3.CREDITS ";
        $query .= "          WHEN L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD ";
        $query .= "               IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '{$ctrl_year}' AND NAMECD1 = 'D065') ";
        $query .= "               AND T3.GRADE_RECORD > 1 THEN T3.CREDITS ";
        $query .= "          WHEN L2.SUBCLASSCD = '900100' AND T3.GRADE_RECORD > 1 THEN T3.CREDITS ";
        $query .= "          ELSE CAST(NULL AS SMALLINT) END AS COMP_CREDIT, ";
        $query .= "     CASE WHEN L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD ";
        $query .= "               NOT IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '{$ctrl_year}' AND NAMECD1 = 'D065') ";
        $query .= "               AND T3.PATTERN_ASSESS > 1 THEN T3.CREDITS ";
        $query .= "          WHEN L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD ";
        $query .= "               IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '{$ctrl_year}' AND NAMECD1 = 'D065') ";
        $query .= "               AND T3.GRADE_RECORD > 1 THEN T3.CREDITS ";
        $query .= "          WHEN L2.SUBCLASSCD = '900100' AND T3.GRADE_RECORD > 1 THEN T3.CREDITS ";
        $query .= "          ELSE CAST(NULL AS SMALLINT) END AS GET_CREDIT, ";
        $query .= "     T4.KEKKA_JISUU, "; //欠課時数
        $query .= "     T4.LESSON_JISUU, "; //出席すべき時数
        $query .= "     T5.KESSEKI_NISUU, "; //欠席日数
        $query .= "     T5.LESSON_NISUU, "; //出席すべき日数
        $query .= "     L1.ABSENCE_HIGH     AS COMP_ABSENCE_HIGH, ";
        $query .= "     L1.GET_ABSENCE_HIGH AS GET_ABSENCE_HIGH ";
        $query .= " FROM ";
        $query .= "     SCHNO T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SUB_MAIN        M1 ON M1.SCHREGNO    = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     RECORD_SCORE    T3 ON  T3.SCHREGNO   = M1.SCHREGNO ";
        $query .= "                        AND T3.SUBCLASSCD = M1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T3.CLASSCD = M1.CLASSCD ";
            $query .= "                        AND T3.SCHOOL_KIND = M1.SCHOOL_KIND ";
            $query .= "                        AND T3.CURRICULUM_CD = M1.CURRICULUM_CD ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     ATTEND_SUBCLASS T4 ON  T4.SCHREGNO   = M1.SCHREGNO ";
        $query .= "                        AND T4.SUBCLASSCD = M1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T4.CLASSCD = M1.CLASSCD ";
            $query .= "                        AND T4.SCHOOL_KIND = M1.SCHOOL_KIND ";
            $query .= "                        AND T4.CURRICULUM_CD = M1.CURRICULUM_CD ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     ATTEND_SEMES    T5 ON  T5.SCHREGNO   = M1.SCHREGNO ";

        $query .= " LEFT  JOIN ";
        $query .= "     CREDIT_MST L1 ON  L1.YEAR       = '{$ctrl_year}' ";
        $query .= "                   AND L1.COURSECD   = T1.COURSECD ";
        $query .= "                   AND L1.MAJORCD    = T1.MAJORCD ";
        $query .= "                   AND L1.GRADE      = T1.GRADE ";
        $query .= "                   AND L1.COURSECODE = T1.COURSECODE ";
        $query .= "                   AND L1.SUBCLASSCD = M1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND L1.CLASSCD = M1.CLASSCD ";
            $query .= "                        AND L1.SCHOOL_KIND = M1.SCHOOL_KIND ";
            $query .= "                        AND L1.CURRICULUM_CD = M1.CURRICULUM_CD ";
        }
        $query .= " LEFT  JOIN ";
        $query .= "     SUBCLASS_MST L2 ON  M1.SUBCLASSCD = L2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND M1.CLASSCD = L2.CLASSCD ";
            $query .= "                        AND M1.SCHOOL_KIND = L2.SCHOOL_KIND ";
            $query .= "                        AND M1.CURRICULUM_CD = L2.CURRICULUM_CD ";
        }

        //在校生検索を使用する場合、該当生徒の全ての科目を表示する。
        if (!strlen($model->search_schregno)) {
            if ($model->field["KYOUKA_SOUGOU1"] == '1' || $model->field["KYOUKA_SOUGOU2"] == '1') {
                $query .= " WHERE ";
            }
            if ($model->field["KYOUKA_SOUGOU1"] == '1' && $model->field["KYOUKA_SOUGOU2"] == '1') {
                $query .= "     SUBSTR(M1.SUBCLASSCD,1,2) <= '90' ";
            } elseif ($model->field["KYOUKA_SOUGOU1"] == '1') {
                $query .= "     SUBSTR(M1.SUBCLASSCD,1,2) < '90' ";
            } elseif ($model->field["KYOUKA_SOUGOU2"] == '1') {
                $query .= "     SUBSTR(M1.SUBCLASSCD,1,2) = '90' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L2.CLASSCD, ";
            $query .= "         L2.SCHOOL_KIND, ";
            $query .= "         L2.CURRICULUM_CD, ";
        }
        $query .= "     L2.SUBCLASSCD ";

        return $query;
    }

    //文字評定の取得
    function getAssess($grade, $subclasscd, $score, $model)
    {

        $query .= " SELECT ";
        $query .= "     ASSESSMARK ";
        $query .= " FROM ";
        $query .= "     RELATIVEASSESS_MST ";
        $query .= " WHERE ";
        $query .= "     GRADE = '".$grade."' AND ";
        //教育課程対応
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' AND ";
        $query .= "     ASSESSCD = '3' AND ";
        $query .= "     ASSESSLOW <= DECIMAL(".$score.") AND ";
        $query .= "     ASSESSHIGH >= DECIMAL(".$score.") ";

        return $query;
    }

    //相対評定の取得
    function selectAssessLevel($grade, $subclasscd, $model)
    {

        $query .= " SELECT ";
        $query .= "     ASSESSMARK, ";
        $query .= "     ASSESSLOW, ";
        $query .= "     ASSESSHIGH ";
        $query .= " FROM ";
        $query .= "     RELATIVEASSESS_MST ";
        $query .= " WHERE ";
        $query .= "     GRADE = '".$grade."' AND ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' AND ";
        $query .= "     ASSESSCD = '3' ";
        $query .= " ORDER BY ";
        $query .= "     ASSESSLEVEL ";

        return $query;
    }

    /* データ更新処理 */
    function update($model)
    {
        $db = Query::dbCheckOut();
        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            //更新チェックボックス
            if ($model->fields["UPDATE_DATA"][$i] != '1') continue;

            $val = explode("-", $model->data["SCHREGNO"][$i]);
            $schno = $val[0];
            $classcd = $val[1];
            $kind = $val[2];
            $curri = $val[3];
            $subcd = $val[4];
            $year = CTRL_YEAR;
            //項目
            $data = array();
            if (strlen($model->fields["A_PATTERN_ASSESS"][$i])) {
                $data["A_PATTERN_ASSESS"][TEXT] = $model->fields["A_PATTERN_ASSESS"][$i];
            }
            if (strlen($model->fields["B_PATTERN_ASSESS"][$i])) {
                $data["B_PATTERN_ASSESS"][TEXT] = $model->fields["B_PATTERN_ASSESS"][$i];
            }
            if (strlen($model->fields["C_PATTERN_ASSESS"][$i])) {
                $data["C_PATTERN_ASSESS"][TEXT] = $model->fields["C_PATTERN_ASSESS"][$i];
            }
            if (strlen($model->fields["GRADE_RECORD"][$i])) {
                $data["GRADE_RECORD"][NUMBER]   = $model->fields["GRADE_RECORD"][$i];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //更新
            $where  = " WHERE YEAR          = '{$year}'";
            //教育課程対応
            $where .= "   AND CLASSCD       = '{$classcd}' ";
            $where .= "   AND SCHOOL_KIND   = '{$kind}' ";
            $where .= "   AND CURRICULUM_CD = '{$curri}' ";
            $where .= "   AND SUBCLASSCD    = '{$subcd}'";
            $where .= "   AND SCHREGNO      = '{$schno}'";
            $query = Query::updateSQL($data, "KIN_RECORD_DAT", $where);
            $db->query($query);
        }

        Query::dbCheckIn($db);
    }

}
?>
