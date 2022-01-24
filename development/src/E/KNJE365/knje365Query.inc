<?php

require_once('for_php7.php');

class knje365Query extends Query {
    //学校判定
    function getSchoolHantei($schoolname) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     NAME1   = '".$schoolname."' ";

        return $query;
    }

    //学年一覧取得
    function getGradeList() {
        $query  = " SELECT ";
        $query .= "     GRADE_CD, ";
        $query .= "     GRADE, ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = 'H' AND ";
        $query .= "     GRADE_CD   <= '03' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE_CD DESC ";

        return $query;
    }

    //生徒一覧取得
    function getSchList() {
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_REGD_GDAT T2, ";
        $query .= "         AFT_GRAD_COURSE_DAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.YEAR         = T3.YEAR AND ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER     = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.GRADE        = T2.GRADE AND ";
        $query .= "         T2.SCHOOL_KIND  = 'H' AND ";
        $query .= "         T2.GRADE_CD     = '03' AND ";
        $query .= "         T1.SCHREGNO     = T3.SCHREGNO AND ";
        $query .= "         T3.SENKOU_KIND  = '0' AND ";
        $query .= "         T3.DECISION IN ('1', '2') ";

        return $query;
    }

    //科目一覧取得
    function getSubclassList($model, $flg="") {
        $query  = " WITH SCH_LIST AS ( ";
        $query .= knje365Query::getSchList();
        $query .= " ), SUBCLASS_WEIGHTING AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         T1.COMBINED_CLASSCD AS CLASSCD, ";
        $query .= "         T1.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ";
        $query .= "         T1.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ";
        $query .= "         T1.COMBINED_SUBCLASSCD AS SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_WEIGHTING_COURSE_DAT T1, ";
        $query .= "         SCH_LIST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.FLG          = '1' AND ";
        $query .= "         T1.GRADE        = T2.GRADE AND ";
        $query .= "         T1.COURSECD     = T1.COURSECD AND ";
        $query .= "         T1.MAJORCD      = T2.MAJORCD AND ";
        $query .= "         T1.COURSECODE   = T2.COURSECODE ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         T1.ATTEND_CLASSCD AS CLASSCD, ";
        $query .= "         T1.ATTEND_SCHOOL_KIND AS SCHOOL_KIND, ";
        $query .= "         T1.ATTEND_CURRICULUM_CD AS CURRICULUM_CD, ";
        $query .= "         T1.ATTEND_SUBCLASSCD AS SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_WEIGHTING_COURSE_DAT T1, ";
        $query .= "         SCH_LIST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.FLG          = '1' AND ";
        $query .= "         T1.GRADE        = T2.GRADE AND ";
        $query .= "         T1.COURSECD     = T1.COURSECD AND ";
        $query .= "         T1.MAJORCD      = T2.MAJORCD AND ";
        $query .= "         T1.COURSECODE   = T2.COURSECODE ";
        $query .= " ), SUBCLASS_LIST AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T3.GRADE, ";
        $query .= "         T3.COURSECD, ";
        $query .= "         T3.MAJORCD, ";
        $query .= "         T3.COURSECODE, ";
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_MST T1, ";
        //パーツタイプ対応
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "         RECORD_RANK_SDIV_DAT T2, ";
        } else {
            $query .= "         RECORD_RANK_DAT T2, ";
        }
        $query .= "         SCH_LIST T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.CLASSCD          = T2.CLASSCD AND ";
        $query .= "         T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
        $query .= "         T1.CURRICULUM_CD    = T2.CURRICULUM_CD AND ";
        $query .= "         T1.SUBCLASSCD       = T2.SUBCLASSCD AND ";
        $query .= "         T2.YEAR             = T3.YEAR AND ";
        $query .= "         T2.SCHREGNO         = T3.SCHREGNO AND ";
        $query .= "         T1.CLASSCD          < '90' AND ";
        $query .= "         T2.SEMESTER         = '9' AND ";
        //パーツタイプ対応
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "         T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '990008' AND ";
        } else {
            $query .= "         T2.TESTKINDCD || T2.TESTITEMCD = '9901' AND ";
        }
        $query .= "         NOT EXISTS (SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         SUBCLASS_WEIGHTING L1 ";
        $query .= "                     WHERE ";
        $query .= "                         T3.GRADE            = L1.GRADE AND ";
        $query .= "                         T3.COURSECD         = L1.COURSECD AND ";
        $query .= "                         T3.MAJORCD          = L1.MAJORCD AND ";
        $query .= "                         T3.COURSECODE       = L1.COURSECODE AND ";
        $query .= "                         T1.CLASSCD          = L1.CLASSCD AND ";
        $query .= "                         T1.SCHOOL_KIND      = L1.SCHOOL_KIND AND ";
        $query .= "                         T1.CURRICULUM_CD    = L1.CURRICULUM_CD AND ";
        $query .= "                         T1.SUBCLASSCD       = L1.SUBCLASSCD ";
        $query .= "                     ) ";
        $query .= " ), ATTEND_SUBCLASSCD AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         T1.ATTEND_CLASSCD AS CLASSCD, ";
        $query .= "         T1.ATTEND_SCHOOL_KIND AS SCHOOL_KIND, ";
        $query .= "         T1.ATTEND_CURRICULUM_CD AS CURRICULUM_CD, ";
        $query .= "         T1.ATTEND_SUBCLASSCD AS SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_WEIGHTING_COURSE_DAT T1, ";
        $query .= "         SCH_LIST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.FLG          = '1' AND ";
        $query .= "         T1.GRADE        = T2.GRADE AND ";
        $query .= "         T1.COURSECD     = T2.COURSECD AND ";
        $query .= "         T1.MAJORCD      = T2.MAJORCD AND ";
        $query .= "         T1.COURSECODE   = T2.COURSECODE AND ";
        $query .= "         T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD ";
        $query .= "                     NOT IN (SELECT ";
        $query .= "                                 COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
        $query .= "                             FROM ";
        $query .= "                                 SUBCLASS_WEIGHTING_COURSE_DAT L1 ";
        $query .= "                             WHERE ";
        $query .= "                                 L1.YEAR         = T1.YEAR AND ";
        $query .= "                                 L1.FLG          = T1.FLG AND ";
        $query .= "                                 L1.GRADE        = T1.GRADE AND ";
        $query .= "                                 L1.COURSECD     = T1.COURSECD AND ";
        $query .= "                                 L1.MAJORCD      = T1.MAJORCD AND ";
        $query .= "                                 L1.COURSECODE   = T1.COURSECODE ";
        $query .= "                             ) AND ";
        $query .= "         T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD ";
        $query .= "                     NOT IN (SELECT ";
        $query .= "                                 NAME1 ";
        $query .= "                             FROM ";
        $query .= "                                 V_NAME_MST L2 ";
        $query .= "                             WHERE ";
        $query .= "                                 L2.YEAR     = T1.YEAR AND ";
        $query .= "                                 L2.NAMECD1  = 'D052' ";
        $query .= "                             ) ";
        if ($flg) {
            $query .= " ) ";
        } else {
            $query .= " ), MAIN AS ( ";
        }
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_LIST ";
        $query .= "     UNION  ";
        $query .= "     SELECT ";
        $query .= "         T2.GRADE, ";
        $query .= "         T2.COURSECD, ";
        $query .= "         T2.MAJORCD, ";
        $query .= "         T2.COURSECODE, ";
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_MST T1, ";
        $query .= "         ATTEND_SUBCLASSCD T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.CLASSCD          = T2.CLASSCD AND ";
        $query .= "         T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
        $query .= "         T1.CURRICULUM_CD    = T2.CURRICULUM_CD AND ";
        $query .= "         T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        if ($flg == "") {
            $query .= " ) ";
            $query .= " SELECT DISTINCT ";
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
            $query .= "     SUBCLASSCD, ";
            $query .= "     SUBCLASSABBV ";
            $query .= " FROM ";
            $query .= "     MAIN ";
            $query .= " ORDER BY ";
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
            $query .= "     SUBCLASSCD ";
        }

        return $query;
    }

    //センター科目一覧取得
    function getMockCenterSubclassList($model) {
        $query  = " WITH SCH_LIST AS ( ";
        $query .= knje365Query::getSchList();
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     MCM.* ";
        $query .= " FROM ";
        $query .= "     MOCK_CENTER_SUBCLASS_MST MCM, ";
        $query .= "     MOCK_CENTER_DAT MCD, ";
        $query .= "     SCH_LIST SL ";
        $query .= " WHERE ";
        $query .= "         MCM.YEAR       = MCD.YEAR ";
        $query .= "     AND MCD.YEAR       = SL.YEAR ";
        $query .= "     AND MCD.CENTERCD   = '000000000' ";
        $query .= "     AND MCD.SCHREGNO   = SL.SCHREGNO ";
        $query .= "     AND MCD.RANK_RANGE = '1' ";
        $query .= "     AND MCD.RANK_DIV   = '02' ";
        $query .= "     AND MCD.CENTERDIV  = '1' ";
        $query .= " ORDER BY ";
        $query .= "     MCM.CENTER_SUBCLASS_CD ";

        return $query;
    }


    //CSV出力データ
    function getCsvQuery($model, $subclass_array, $grade_array, $mocksub_array) {
        $query  = " WITH SCH_LIST AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         L2.HR_NAME, ";
        $query .= "         L1.NAME, ";
        $query .= "         L1.SEX, ";
        $query .= "         L1.INOUTCD, ";
        $query .= "         L1.GRD_DIV, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST L1 ";
        $query .= "                 ON T1.SCHREGNO  = L1.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT L2 ";
        $query .= "                 ON T1.YEAR      = L2.YEAR ";
        $query .= "                AND T1.GRADE     = L2.GRADE ";
        $query .= "                AND T1.HR_CLASS  = L2.HR_CLASS, ";
        $query .= "         SCHREG_REGD_GDAT T2, ";
        $query .= "         AFT_GRAD_COURSE_DAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.YEAR         = T3.YEAR AND ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER     = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.GRADE        = T2.GRADE AND ";
        $query .= "         T2.SCHOOL_KIND  = 'H' AND ";
        $query .= "         T2.GRADE_CD     = '03' AND ";
        $query .= "         T1.SCHREGNO     = T3.SCHREGNO AND ";
        $query .= "         T3.SENKOU_KIND  = '0' AND ";
        $query .= "         T3.DECISION IN ('1', '2') ";
        $query .= " ), SUBCLASS AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_MST T1, ";
        $query .= "         SCH_LIST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN ('".implode($subclass_array,"','")."') ";
        $query .= " ), RECORD AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         L1.SCORE ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS T1 ";
        //パーツタイプ対応
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "         LEFT JOIN RECORD_RANK_SDIV_DAT L1 ";
        } else {
            $query .= "         LEFT JOIN RECORD_RANK_DAT L1 ";
        }
        $query .= "                  ON T1.CLASSCD          = L1.CLASSCD ";
        $query .= "                 AND T1.SCHOOL_KIND      = L1.SCHOOL_KIND ";
        $query .= "                 AND T1.CURRICULUM_CD    = L1.CURRICULUM_CD ";
        $query .= "                 AND T1.SUBCLASSCD       = L1.SUBCLASSCD ";
        $query .= "                 AND T1.SCHREGNO         = L1.SCHREGNO ";
        $query .= "                 AND L1.YEAR             = '".CTRL_YEAR."' ";
        $query .= "                 AND L1.SEMESTER         = '9' ";
        //パーツタイプ対応
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "                 AND L1.TESTKINDCD || L1.TESTITEMCD || L1.SCORE_DIV = '990008' ";
        } else {
            $query .= "                 AND L1.TESTKINDCD || L1.TESTITEMCD = '9901' ";
        }
        $query .= " ), SCH_INFO AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         MAX(T1.YEAR) AS YEAR ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         (SELECT ";
        $query .= "             YEAR, ";
        $query .= "             MAX(SEMESTER) AS SEMESTER ";
        $query .= "         FROM ";
        $query .= "             SEMESTER_MST ";
        $query .= "         WHERE ";
        $query .= "             SEMESTER != '9' ";
        $query .= "         GROUP BY ";
        $query .= "             YEAR ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER ";
        $query .= "     GROUP BY ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.SCHREGNO ";
        foreach ($grade_array as $key) {
            $query .= " ), PAST_RECORD".$key." AS ( ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.YEAR, ";
            $query .= "         T3.GRADE, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         DECIMAL(ROUND(DECIMAL(T1.AVG,5,1),0),5,0) AS AVERAGE, ";
            if ($model->Properties["usePerfectCourseGroup"] == '1') {
                $query .= "         T1.MAJOR_AVG_RANK AS RANK ";
            } else {
                $query .= "         T1.GRADE_AVG_RANK AS RANK ";
            }
            $query .= "     FROM ";
            //パーツタイプ対応
            if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                $query .= "         RECORD_RANK_SDIV_DAT T1, ";
            } else {
                $query .= "         RECORD_RANK_DAT T1, ";
            }
            $query .= "         SCH_LIST T2, ";
            $query .= "         SCH_INFO T3 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR         = T3.YEAR AND ";
            $query .= "         T1.SEMESTER     = '9' AND ";
            //パーツタイプ対応
            if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                $query .= "         T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '990008' AND ";
            } else {
                $query .= "         T1.TESTKINDCD || T1.TESTITEMCD = '9901' AND ";
            }
            $query .= "         T1.CLASSCD      = '99' AND ";
            $query .= "         T1.SUBCLASSCD   = '999999' AND ";
            $query .= "         T1.SCHREGNO     = T2.SCHREGNO AND ";
            $query .= "         T1.SCHREGNO     = T3.SCHREGNO AND ";
            $query .= "         T3.GRADE        = '".$key."' ";
        }
        $query .= " ), CENTER_SUBCLASS AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T1.CENTER_SUBCLASS_CD, ";
        $query .= "         T1.CENTER_CLASS_CD ";
        $query .= "     FROM ";
        $query .= "         MOCK_CENTER_SUBCLASS_MST T1, ";
        $query .= "         SCH_LIST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.CENTER_SUBCLASS_CD IN ('".implode($mocksub_array,"','")."') ";
        $query .= " ), CENTER_SCORE AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         '99' AS CLASSCD, ";
        $query .= "         'H' AS SCHOOL_KIND, ";
        $query .= "         '9' AS CURRICULUM_CD, ";
        $query .= "         T1.CENTER_SUBCLASS_CD AS SUBCLASSCD, ";
        $query .= "         L1.SCORE ";
        $query .= "     FROM ";
        $query .= "         CENTER_SUBCLASS T1 ";
        $query .= "         LEFT JOIN MOCK_CENTER_RANK_RANGE_DAT L1 ";
        $query .= "                  ON T1.CENTER_SUBCLASS_CD = L1.CENTER_SUBCLASS_CD ";
        $query .= "                 AND T1.CENTER_CLASS_CD    = L1.CENTER_CLASS_CD ";
        $query .= "                 AND T1.SCHREGNO           = L1.SCHREGNO ";
        $query .= "                 AND L1.YEAR               = '".CTRL_YEAR."' ";
        $query .= "                 AND L1.CENTERCD           = '000000000' ";
        $query .= "                 AND L1.RANK_RANGE         = '1' ";
        $query .= "                 AND L1.RANK_DIV           = '02' ";
        $query .= "                 AND L1.CENTERDIV          = '1' ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         L1.CLASSCD, ";
        $query .= "         L1.SCHOOL_KIND, ";
        $query .= "         L1.CURRICULUM_CD, ";
        $query .= "         L1.SUBCLASSCD, ";
        $query .= "         L1.SCORE ";
        $query .= "     FROM ";
        $query .= "         SCH_LIST T1 ";
        $query .= "         LEFT JOIN RECORD L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         L1.CLASSCD, ";
        $query .= "         L1.SCHOOL_KIND, ";
        $query .= "         L1.CURRICULUM_CD, ";
        $query .= "         L1.SUBCLASSCD, ";
        $query .= "         L1.SCORE ";
        $query .= "     FROM ";
        $query .= "         SCH_LIST T1 ";
        $query .= "         LEFT JOIN CENTER_SCORE L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.* ";
        foreach ($grade_array as $key) {
            $query .= "    ,P".$key.".AVERAGE AS GRADE_".$key."_AVE ";
            $query .= "    ,P".$key.".RANK AS GRADE_".$key."_RANK ";
        }
        $query .= "    ,RANK900BUN.SCORE AS CENTER_TOTAL1 ";
        $query .= "    ,RANK900RI.SCORE  AS CENTER_TOTAL2 ";
        $query .= "    ,RANK800.SCORE    AS CENTER_TOTAL3 ";
        $query .= "    ,RANK500BUN.SCORE AS CENTER_TOTAL4 ";
        $query .= "    ,RANK500RI.SCORE  AS CENTER_TOTAL5 ";
        $query .= "    ,MCD.BUNRIDIV ";
        $query .= "    ,VNM.NAME1 AS BUNRINAME1 ";
        $query .= "    ,T1.SEX || N1.NAME1 AS SEX_SHOW ";
        $query .= "    ,T1.INOUTCD || N2.NAME1 AS INOUTCD_SHOW ";
        $query .= "    ,T1.GRD_DIV || N3.NAME1 AS GRD_DIV_SHOW ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        foreach ($grade_array as $key) {
            $query .= "     LEFT JOIN PAST_RECORD".$key." P".$key;
            $query .= "              ON T1.SCHREGNO = P".$key.".SCHREGNO ";
        }
        $query .= "     LEFT JOIN MOCK_CENTER_KANSAN_RANK_DAT RANK900BUN ";
        $query .= "              ON RANK900BUN.YEAR       = '".CTRL_YEAR."' ";
        $query .= "             AND RANK900BUN.CENTERCD   = '000000000' ";
        $query .= "             AND T1.SCHREGNO           = RANK900BUN.SCHREGNO ";
        $query .= "             AND RANK900BUN.KANSAN_DIV = '03' ";
        $query .= "             AND RANK900BUN.BUNRIDIV   = '1' ";
        $query .= "             AND RANK900BUN.RANK_RANGE = '1' ";
        $query .= "             AND RANK900BUN.RANK_DIV   = '02' ";
        $query .= "             AND RANK900BUN.CENTERDIV  = '1' ";
        $query .= "     LEFT JOIN MOCK_CENTER_KANSAN_RANK_DAT RANK900RI ";
        $query .= "              ON RANK900RI.YEAR        = '".CTRL_YEAR."' ";
        $query .= "             AND RANK900RI.CENTERCD    = '000000000' ";
        $query .= "             AND T1.SCHREGNO           = RANK900RI.SCHREGNO ";
        $query .= "             AND RANK900RI.KANSAN_DIV  = '03' ";
        $query .= "             AND RANK900RI.BUNRIDIV    = '2' ";
        $query .= "             AND RANK900RI.RANK_RANGE  = '1' ";
        $query .= "             AND RANK900RI.RANK_DIV    = '02' ";
        $query .= "             AND RANK900RI.CENTERDIV   = '1' ";
        $query .= "     LEFT JOIN MOCK_CENTER_KANSAN_RANK_DAT RANK800 ";
        $query .= "              ON RANK800.YEAR          = '".CTRL_YEAR."' ";
        $query .= "             AND RANK800.CENTERCD      = '000000000' ";
        $query .= "             AND T1.SCHREGNO           = RANK800.SCHREGNO ";
        $query .= "             AND RANK800.KANSAN_DIV    = '01' ";
        $query .= "             AND RANK800.BUNRIDIV      = '0' ";
        $query .= "             AND RANK800.RANK_RANGE    = '1' ";
        $query .= "             AND RANK800.RANK_DIV      = '02' ";
        $query .= "             AND RANK800.CENTERDIV     = '1' ";
        $query .= "     LEFT JOIN MOCK_CENTER_KANSAN_RANK_DAT RANK500BUN ";
        $query .= "              ON RANK500BUN.YEAR       = '".CTRL_YEAR."' ";
        $query .= "             AND RANK500BUN.CENTERCD   = '000000000' ";
        $query .= "             AND T1.SCHREGNO           = RANK500BUN.SCHREGNO ";
        $query .= "             AND RANK500BUN.KANSAN_DIV = '02' ";
        $query .= "             AND RANK500BUN.BUNRIDIV   = '1' ";
        $query .= "             AND RANK500BUN.RANK_RANGE = '1' ";
        $query .= "             AND RANK500BUN.RANK_DIV   = '02' ";
        $query .= "             AND RANK500BUN.CENTERDIV  = '1' ";
        $query .= "     LEFT JOIN MOCK_CENTER_KANSAN_RANK_DAT RANK500RI ";
        $query .= "              ON RANK500RI.YEAR        = '".CTRL_YEAR."' ";
        $query .= "             AND RANK500RI.CENTERCD    = '000000000' ";
        $query .= "             AND T1.SCHREGNO           = RANK500RI.SCHREGNO ";
        $query .= "             AND RANK500RI.KANSAN_DIV  = '02' ";
        $query .= "             AND RANK500RI.BUNRIDIV    = '2' ";
        $query .= "             AND RANK500RI.RANK_RANGE  = '1' ";
        $query .= "             AND RANK500RI.RANK_DIV    = '02' ";
        $query .= "             AND RANK500RI.CENTERDIV   = '1' ";
        $query .= "     LEFT JOIN MOCK_CENTER_DAT MCD ";
        $query .= "              ON MCD.YEAR              = '".CTRL_YEAR."' ";
        $query .= "             AND MCD.CENTERCD          = '000000000' ";
        $query .= "             AND MCD.SCHREGNO          = T1.SCHREGNO ";
        $query .= "             AND MCD.RANK_RANGE        = '1' ";
        $query .= "             AND MCD.RANK_DIV          = '02' ";
        $query .= "             AND MCD.CENTERDIV         = '1' ";
        $query .= "     LEFT JOIN V_NAME_MST VNM ";
        $query .= "              ON MCD.YEAR              = '".CTRL_YEAR."' ";
        $query .= "             AND VNM.NAMECD1           = 'H316' ";
        $query .= "             AND VNM.NAMECD2           = MCD.BUNRIDIV ";
        $query .= "     LEFT JOIN NAME_MST N1 ";
        $query .= "              ON N1.NAMECD1            = 'Z002' ";
        $query .= "             AND N1.NAMECD2            = T1.SEX ";
        $query .= "     LEFT JOIN NAME_MST N2 ";
        $query .= "              ON N2.NAMECD1            = 'A001' ";
        $query .= "             AND N2.NAMECD2            = T1.INOUTCD ";
        $query .= "     LEFT JOIN NAME_MST N3 ";
        $query .= "              ON N3.NAMECD1            = 'A003' ";
        $query .= "             AND N3.NAMECD2            = T1.GRD_DIV ";
        if ($model->field["BRDIV"] != "3") {
            $query .= " WHERE ";
            $query .= "     MCD.BUNRIDIV = '{$model->field["BRDIV"]}' ";
        }
        $query .= " ORDER BY ";
        if ($model->field["SORT"] == "1") {
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     ATTENDNO, ";
        } else if ($model->field["SORT"] == "2") {
            $query .= "     SCHREGNO, ";
        } else if ($model->field["SORT"] == "3") {
            $query .= "     COURSECD, ";
            $query .= "     MAJORCD, ";
            $query .= "     COURSECODE, ";
            foreach ($grade_array as $key) {
                $query .= "     VALUE(P".$key.".AVERAGE, 0) DESC, ";
                break;
            }
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     ATTENDNO, ";
        }
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }

    //進路データ取得
    function getAftGradCourseDat() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     CASE WHEN T1.SCHOOL_GROUP IN ('01', '02', '04', '08') THEN '1' ";
        $query .= "          WHEN T1.SCHOOL_GROUP IN ('03', '05') THEN '2' END AS GROUP, "; 
        $query .= "     T1.DECISION, ";
        $query .= "     L1.SCHOOL_NAME_SHOW1 || ";
        $query .= "     CASE WHEN LENGTH(VALUE(L2.FACULTYNAME_SHOW1,'')) + LENGTH(VALUE(L3.DEPARTMENTNAME_SHOW1,'')) > 0 THEN '(' ELSE '' END || ";
        $query .= "     VALUE(L2.FACULTYNAME_SHOW1,'') || ";
        $query .= "     CASE WHEN (LENGTH(VALUE(L2.FACULTYNAME_SHOW1,'')) > 0 AND LENGTH(VALUE(L3.DEPARTMENTNAME_SHOW1,'')) > 0) THEN '・' ELSE '' END || ";
        $query .= "     VALUE(L3.DEPARTMENTNAME_SHOW1,'') || ";
        $query .= "     CASE WHEN LENGTH(VALUE(L2.FACULTYNAME_SHOW1,'')) + LENGTH(VALUE(L3.DEPARTMENTNAME_SHOW1,'')) > 0 THEN ')' ELSE '' END AS SCHOOL_GROUP1, ";
        $query .= "     T1.SCHOOL_GROUP, ";
        $query .= "     T1.STAT_CD, ";
        $query .= "     T1.FACULTYCD, ";
        $query .= "     T1.DEPARTMENTCD ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT T1 ";
        $query .= "     LEFT JOIN COLLEGE_MST L1 ";
        $query .= "             ON T1.STAT_CD       = L1.SCHOOL_CD ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST L2 ";
        $query .= "             ON T1.STAT_CD       = L2.SCHOOL_CD ";
        $query .= "            AND T1.FACULTYCD     = L2.FACULTYCD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST L3 ";
        $query .= "             ON T1.STAT_CD       = L3.SCHOOL_CD ";
        $query .= "            AND T1.FACULTYCD     = L3.FACULTYCD ";
        $query .= "            AND T1.DEPARTMENTCD  = L3.DEPARTMENTCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SENKOU_KIND  = '0' AND ";
        $query .= "     T1.SCHOOL_GROUP IN ('01', '02', '03', '04', '05', '08') AND ";
        $query .= "     T1.DECISION IN ('1', '2') ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     GROUP, ";
        $query .= "     T1.DECISION, ";
        $query .= "     T1.SCHOOL_GROUP, ";
        $query .= "     T1.STAT_CD, ";
        $query .= "     T1.FACULTYCD, ";
        $query .= "     T1.DEPARTMENTCD ";

        return $query;
    }
}
?>
