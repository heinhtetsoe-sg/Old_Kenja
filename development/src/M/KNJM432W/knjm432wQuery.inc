<?php

class knjm432wQuery extends Query
{

    //学期コンボ
    public function getSemecmb()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目（講座）リスト
    public function getChrSubCd($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRNAME AS LABEL, ";
        $query .= "     T1.CHAIRCD || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "     LEFT JOIN REP_STANDARDDATE_COURSE_DAT R1 ON R1.YEAR = T1.YEAR ";
        } else {
            $query .= "     LEFT JOIN REP_STANDARDDATE_DAT R1 ON R1.YEAR = T1.YEAR ";
        }
        $query .= "           AND R1.CLASSCD = T1.CLASSCD ";
        $query .= "           AND R1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "           AND R1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "           AND R1.SUBCLASSCD = T1.SUBCLASSCD ";
        //参照・更新可（制限付き）
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     INNER JOIN CHAIR_STF_DAT S1 ON S1.YEAR = T1.YEAR ";
            $query .= "           AND S1.SEMESTER = T1.SEMESTER ";
            $query .= "           AND S1.CHAIRCD = T1.CHAIRCD ";
            $query .= "           AND S1.STAFFCD = '" .STAFFCD ."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useTsushinSemesKonboHyoji"] == "1") {
            $query .= "     AND T1.SEMESTER = '".$model->semecmb."' ";
        }
        $query .= "     AND (R1.CLASSCD IS NOT NULL OR T1.CLASSCD = '90') ";
        if ($model->Properties["knjm432wSelectChaircd"] == "1") {
            $arr = explode('-', $model->subclasscd);
            $query .= "     AND T1.CLASSCD = '".$arr[0]."' ";
            $query .= "     AND T1.SCHOOL_KIND = '".$arr[1]."' ";
            $query .= "     AND T1.CURRICULUM_CD = '".$arr[2]."' ";
            $query .= "     AND T1.SUBCLASSCD = '".$arr[3]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目リスト
    public function getSubclassCd($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SUBM.SUBCLASSNAME AS LABEL, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "     INNER JOIN REP_STANDARDDATE_COURSE_DAT R1 ON R1.YEAR = T1.YEAR ";
        } else {
            $query .= "     INNER JOIN REP_STANDARDDATE_DAT R1 ON R1.YEAR = T1.YEAR ";
        }
        $query .= "           AND R1.CLASSCD = T1.CLASSCD ";
        $query .= "           AND R1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "           AND R1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "           AND R1.SUBCLASSCD = T1.SUBCLASSCD ";
        //参照・更新可（制限付き）
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     INNER JOIN CHAIR_STF_DAT S1 ON S1.YEAR = T1.YEAR ";
            $query .= "           AND S1.SEMESTER = T1.SEMESTER ";
            $query .= "           AND S1.CHAIRCD = T1.CHAIRCD ";
            $query .= "           AND S1.STAFFCD = '" .STAFFCD ."' ";
        }
        $query .= "     INNER JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = T1.CLASSCD ";
        $query .= "           AND SUBM.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "           AND SUBM.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "           AND SUBM.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //レポート取得
    public function getReport($model)
    {
        $chrsub_array = array();
        $chrsub_array = explode("-", $model->sub);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHAIR_CORRES_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.CHAIRCD      = '".$chrsub_array[0]."' ";
        $query .= "     AND T1.CLASSCD      = '".$chrsub_array[1]."' ";
        $query .= "     AND T1.SCHOOL_KIND  = '".$chrsub_array[2]."' ";
        $query .= "     AND T1.CURRICULUM_CD= '".$chrsub_array[3]."' ";
        $query .= "     AND T1.SUBCLASSCD   = '".$chrsub_array[4]."' ";

        return $query;
    }

    //レポート回数取得
    public function getReportCnt($model)
    {
        $chrsub_array = array();
        $chrsub_array = explode("-", $model->sub);

        $query  = " SELECT DISTINCT ";
        $query .= "     STANDARD_SEQ ";
        $query .= " FROM ";
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "     REP_STANDARDDATE_COURSE_DAT T1 ";
        } else {
            $query .= "     REP_STANDARDDATE_DAT T1 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.CLASSCD      = '".$chrsub_array[1]."' ";
        $query .= "     AND T1.SCHOOL_KIND  = '".$chrsub_array[2]."' ";
        $query .= "     AND T1.CURRICULUM_CD= '".$chrsub_array[3]."' ";
        $query .= "     AND T1.SUBCLASSCD   = '".$chrsub_array[4]."' ";
        if ($model->Properties["useTsushinSemesKonboHyoji"] == "1") {
            $query .= "     AND T1.REPORTDIV    = '".$model->semecmb."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     STANDARD_SEQ ";

        return $query;
    }

    //テスト取得
    public function getTest($model)
    {
        if ($model->Properties["knjm432wSelectChaircd"] == "1") {
            list($cls, $sck, $cur, $subcd) = explode("-", $model->subclasscd);
        } else {
            list($chrcd, $cls, $sck, $cur, $subcd) = explode("-", $model->sub);
        }

        $query  = " SELECT DISTINCT ";
        $query .= "     N1.NAMESPARE1 AS TESTCD, ";
        $query .= "     I1.TESTITEMABBV1, ";
        $query .= "     I2.SEMESTER, ";
        $query .= "     I2.SEMESTERNAME ";
        $query .= " FROM ";
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "     REP_STANDARDDATE_COURSE_DAT T1 ";
        } else {
            $query .= "     REP_STANDARDDATE_DAT T1 ";
        }
        $query .= "     INNER JOIN V_NAME_MST N1 ON N1.YEAR = T1.YEAR ";
        $query .= "           AND N1.NAMECD1 = 'M002' ";
        $query .= "           AND N1.NAMECD2 = T1.REPORTDIV ";
        $query .= "           AND N1.NAMESPARE1 IS NOT NULL ";
        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV I1 ON T1.YEAR = I1.YEAR ";
        $query .= "           AND I1.SEMESTER || '-' || I1.TESTKINDCD || '-' || I1.TESTITEMCD || '-' || I1.SCORE_DIV = N1.NAMESPARE1 ";
        $query .= "     INNER JOIN SEMESTER_MST I2 ON I2.YEAR = I1.YEAR ";
        $query .= "           AND I2.SEMESTER = I1.SEMESTER ";
        if ($model->Properties["useTsushinSemesKonboHyoji"] == "1") {
            $query .= "       AND I2.SEMESTER = '".$model->semecmb."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.CLASSCD      = '".$cls."' ";
        $query .= "     AND T1.SCHOOL_KIND  = '".$sck."' ";
        $query .= "     AND T1.CURRICULUM_CD= '".$cur."' ";
        $query .= "     AND T1.SUBCLASSCD   = '".$subcd."' ";
        $query .= " ORDER BY ";
        $query .= "     TESTCD ";

        return $query;
    }

    //成績データ取得
    public function GetRecordDatdata($model, $hyoukaSeme)
    {
        $chrsub_array = array();
        $chrsub_array = explode("-", $model->sub);

        $query  = " WITH CHAIR_STD AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T2.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "         INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "               AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "               AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useTsushinSemesKonboHyoji"] == "1") {
            $query .= "     AND T1.SEMESTER = '".$model->semecmb."' ";
        }
        $query .= "         AND T1.CHAIRCD = '".$chrsub_array[0]."' ";
        $query .= "         AND T1.CLASSCD      = '".$chrsub_array[1]."' ";
        $query .= "         AND T1.SCHOOL_KIND  = '".$chrsub_array[2]."' ";
        $query .= "         AND T1.CURRICULUM_CD= '".$chrsub_array[3]."' ";
        $query .= "         AND T1.SUBCLASSCD   = '".$chrsub_array[4]."' ";
        //成績
        $query .= " ), SCORE_HIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV AS TESTCD, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GET_CREDIT, ";
        $query .= "         T1.COMP_CREDIT, ";
        $query .= "         T1.COMP_CONTINUE, ";
        $query .= "         T1.SCORE, ";
        $query .= "         T1.VALUE ";
        $query .= "     FROM ";
        $query .= "         V_RECORD_SCORE_HIST_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useTsushinSemesKonboHyoji"] == "1") {
            $query .= "     AND T1.SEMESTER = '".$model->semecmb."' ";
        }
        $query .= "         AND T1.CLASSCD      = '".$chrsub_array[1]."' ";
        $query .= "         AND T1.SCHOOL_KIND  = '".$chrsub_array[2]."' ";
        $query .= "         AND T1.CURRICULUM_CD= '".$chrsub_array[3]."' ";
        $query .= "         AND T1.SUBCLASSCD   = '".$chrsub_array[4]."' ";
        //追試チェック用
        $query .= " ), SCORE_HIST_CHECK AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV AS TESTCD, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         MAX(CASE WHEN T1.SEQ > 1 THEN '1' END) AS TSUISHI_FLG ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_HIST_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useTsushinSemesKonboHyoji"] == "1") {
            $query .= "     AND T1.SEMESTER = '".$model->semecmb."' ";
        }
        $query .= "         AND T1.CLASSCD      = '".$chrsub_array[1]."' ";
        $query .= "         AND T1.SCHOOL_KIND  = '".$chrsub_array[2]."' ";
        $query .= "         AND T1.CURRICULUM_CD= '".$chrsub_array[3]."' ";
        $query .= "         AND T1.SUBCLASSCD   = '".$chrsub_array[4]."' ";
        $query .= "         AND T1.SEQ > 1 "; //追試
        $query .= "     GROUP BY ";
        $query .= "         T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV, ";
        $query .= "         T1.SCHREGNO ";
        //受験許可フラグ
        $query .= " ), T_PASS_FLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV AS TESTCD, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.SEM_PASS_FLG ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_STD_PASS_SDIV_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useTsushinSemesKonboHyoji"] == "1") {
            $query .= "     AND T1.SEMESTER = '".$model->semecmb."' ";
        }
        $query .= "         AND T1.CLASSCD      = '".$chrsub_array[1]."' ";
        $query .= "         AND T1.SCHOOL_KIND  = '".$chrsub_array[2]."' ";
        $query .= "         AND T1.CURRICULUM_CD= '".$chrsub_array[3]."' ";
        $query .= "         AND T1.SUBCLASSCD   = '".$chrsub_array[4]."' ";
        $query .= " ), MAX_SEQ AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.STANDARD_SEQ, ";
        $query .= "         MAX(T1.REPRESENT_SEQ) AS REPRESENT_SEQ ";
        $query .= "     FROM ";
        $query .= "         REP_PRESENT_DAT T1, ";
        $query .= "         CHAIR_STD T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR          = '".CTRL_YEAR."' AND ";
        $query .= "         T1.CLASSCD       = T2.CLASSCD AND ";
        $query .= "         T1.SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
        $query .= "         T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        $query .= "         T1.SUBCLASSCD    = T2.SUBCLASSCD AND ";
        $query .= "         T1.SCHREGNO      = T2.SCHREGNO ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.STANDARD_SEQ ";
        //スクーリング規定回数
        $query .= " ), SCH_SEQ AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         VALUE(T1.SCH_SEQ_MIN, 0) AS SCH_SEQ_MIN ";
        $query .= "     FROM ";
        $query .= "         CHAIR_CORRES_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.CHAIRCD      = '".$chrsub_array[0]."' ";
        $query .= "         AND T1.CLASSCD      = '".$chrsub_array[1]."' ";
        $query .= "         AND T1.SCHOOL_KIND  = '".$chrsub_array[2]."' ";
        $query .= "         AND T1.CURRICULUM_CD= '".$chrsub_array[3]."' ";
        $query .= "         AND T1.SUBCLASSCD   = '".$chrsub_array[4]."' ";
        //スクーリング回数
        $query .= " ), ATTEND_ALL AS ( ";
        $query .= "   SELECT  ";
        $query .= "       T1.YEAR,  ";
        $query .= "       T2.SEMESTER,  ";
        $query .= "       T3.CLASSCD,  ";
        $query .= "       T3.SCHOOL_KIND,  ";
        $query .= "       T3.CURRICULUM_CD,  ";
        $query .= "       T3.SUBCLASSCD,  ";
        $query .= "       T1.SCHREGNO,  ";
        $query .= "       T1.SCHOOLINGKINDCD,  ";
        $query .= "       T4.NAMESPARE1,  ";
        $query .= "       T1.EXECUTEDATE,  ";
        $query .= "       T1.PERIODCD,  ";
        $query .= "       T1.CREDIT_TIME, ";
        $query .= "       T5.SCH_SEQ_MIN ";
        $query .= "   FROM  ";
        $query .= "       SCH_ATTEND_DAT T1  ";
        $query .= "       INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR  ";
        $query .= "           AND T2.SEMESTER <> '9'  ";
        if ($model->Properties["useTsushinSemesKonboHyoji"] == "1") {
            $query .= "       AND T2.SEMESTER = '".$model->semecmb."' ";
        }
        $query .= "           AND T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE  ";
        $query .= "       INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR  ";
        $query .= "           AND T3.SEMESTER = T2.SEMESTER  ";
        $query .= "           AND T3.CHAIRCD = T1.CHAIRCD  ";
        $query .= "       LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'M001'  ";
        $query .= "           AND T4.NAMECD2 = T1.SCHOOLINGKINDCD  ";
        $query .= "       LEFT JOIN CHAIR_CORRES_DAT T5 ON T5.YEAR = T1.YEAR ";
        $query .= "           AND T5.CHAIRCD = T3.CHAIRCD ";
        $query .= "   WHERE  ";
        $query .= "       T1.YEAR = '".CTRL_YEAR."'  ";
        $query .= " ), ATTEND_KIND1 AS ( ";
        $query .= "     SELECT  ";
        $query .= "         1 AS KIND, ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, SCHREGNO, ";
        $query .= "         COUNT(DISTINCT EXECUTEDATE) AS JISU1, ";
        $query .= "         COUNT(DISTINCT EXECUTEDATE) AS JISU2 ";
        $query .= "     FROM ATTEND_ALL ";
        $query .= "     WHERE NAMESPARE1 = '1' AND SCHOOLINGKINDCD = '1' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= " CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, SCHREGNO ";
        $query .= " ), ATTEND_KIND2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         2 AS KIND, ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, SCHREGNO, ";
        $query .= "         SCH_SEQ_MIN, ";
        $query .= "         SCH_SEQ_MIN * INT(VALUE(L1.NAME1, '6')) / 10 AS LIMIT, ";
        $query .= "         SUM(CREDIT_TIME) AS JISU1, ";
        $query .= "         VALUE(INT(MIN(SCH_SEQ_MIN * INT(VALUE(L1.NAME1, '6')) / 10, SUM(CREDIT_TIME))), 0) AS JISU2 ";
        $query .= "     FROM ATTEND_ALL ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'M020' AND L1.NAMECD2 = '01'";
        $query .= "     WHERE SCHOOLINGKINDCD = '2' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= " CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, SCHREGNO, ";
        $query .= "         SCH_SEQ_MIN, ";
        $query .= "         L1.NAME1 ";
        $query .= " ), ATTEND_KIND3 AS ( ";
        $query .= "     SELECT  ";
        $query .= "         3 AS KIND, ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, SCHREGNO, ";
        $query .= "         SUM(CREDIT_TIME) AS JISU1, ";
        $query .= "         SUM(CREDIT_TIME) AS JISU2 ";
        $query .= "     FROM ATTEND_ALL ";
        $query .= "     WHERE NAMESPARE1 = '1' AND SCHOOLINGKINDCD <> '1' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, SCHREGNO ";
        $query .= " ), SUBCLASS_ATTEND AS ( ";
        $query .= "     SELECT KIND, YEAR, ";
        $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, SCHREGNO, JISU1, JISU2 ";
        $query .= "     FROM ATTEND_KIND1 ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT KIND, YEAR, ";
        $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, SCHREGNO, JISU1, JISU2 ";
        $query .= "     FROM ATTEND_KIND2 ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT KIND, YEAR, ";
        $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, SCHREGNO, JISU1, JISU2 ";
        $query .= "     FROM ATTEND_KIND3 ";
        $query .= " ), SUBCLASS_ATTEND_SUM AS ( ";
        $query .= "     SELECT ";
        $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, SCHREGNO, SUM(T1.JISU2) AS SCHOOLING_TIME ";
        $query .= "     FROM SUBCLASS_ATTEND T1 ";
        $query .= "     GROUP BY ";
        $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, SCHREGNO ";
        for ($repCnt = 1; $repCnt <= $model->reportInfo["REP_SEQ_ALL"]; $repCnt++) {
            $no = $model->reportInfo["REP_START_SEQ"] - 1 + $repCnt;
            if ($model->Properties["useTsushinSemesKonboHyoji"] == "1" && !in_array($no, $model->repCntArray)) {
                continue;
            }
            $query .= " ), REPORT_{$no} AS ( ";
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     N1.ABBV1 AS REPORT_ABBV ";
            $query .= " FROM ";
            $query .= "     REP_PRESENT_DAT T1 ";
            $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'M003' AND N1.NAMECD2 = T1.GRAD_VALUE, ";
            $query .= "     MAX_SEQ T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR          = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SCHREGNO      = T2.SCHREGNO AND ";
            $query .= "     T1.CLASSCD       = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
            $query .= "     T1.SUBCLASSCD    = T2.SUBCLASSCD AND ";
            $query .= "     T1.STANDARD_SEQ  = {$no} AND ";
            $query .= "     T1.STANDARD_SEQ  = T2.STANDARD_SEQ AND ";
            $query .= "     T1.REPRESENT_SEQ = T2.REPRESENT_SEQ ";
        }
        $query .= " ), PRINT_STATUS AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         CASE WHEN COUNT(*) > 0 THEN 'レ' ELSE '' END AS PRINT_STATUS ";
        $query .= "     FROM ";
        $query .= "         CERTIF_ISSUE_SUBCLASS_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.CLASSCD       = '".$chrsub_array[1]."' ";
        $query .= "         AND T1.SCHOOL_KIND   = '".$chrsub_array[2]."' ";
        $query .= "         AND T1.CURRICULUM_CD = '".$chrsub_array[3]."' ";
        $query .= "         AND T1.SUBCLASSCD    = '".$chrsub_array[4]."' ";
        $query .= "         AND T1.CERTIF_KINDCD = '134' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS, ";
        $query .= "     T3.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T4.HR_NAME, ";
        $query .= "     ATTEND.SCHOOLING_TIME, ";
        //テスト
        foreach ($model->testcdArray as $key => $codeArray) {
            //成績
            $query .= " R{$key}.SCORE AS SCORE{$key}, ";
            //成績（VALUE）
            $query .= " R{$key}.VALUE AS SCORE_VALUE{$key}, ";
            //追試チェック用
            $query .= " V{$key}.TSUISHI_FLG AS TSUISHI_FLG{$key}, ";
            //受験許可フラグ
            $query .= " F{$key}.SEM_PASS_FLG AS SEM_PASS_FLG{$key}, ";
            //考査満点マスタ(合格点)
            $query .= " P{$key}.PASS_SCORE AS PASS_SCORE{$key}, ";
        }
        $query .= "     HIST08.VALUE AS HYOUKA, ";
        $query .= "     HIST09.VALUE AS HYOUTEI, ";
        if ($model->Properties["useHyoutei"] == "1" && $model->inputCreditOnly != "1") {
            $query .= "     HIST08.COMP_CONTINUE AS COMP_CONTINUE, ";
        } else {
            $query .= "     HIST09.COMP_CONTINUE AS COMP_CONTINUE, ";
        }
        $query .= "     HIST09.GET_CREDIT, ";
        $query .= "     HIST09.COMP_CREDIT, ";
        for ($repCnt = 1; $repCnt <= $model->reportInfo["REP_SEQ_ALL"]; $repCnt++) {
            $no = $model->reportInfo["REP_START_SEQ"] - 1 + $repCnt;
            if ($model->Properties["useTsushinSemesKonboHyoji"] == "1" && !in_array($no, $model->repCntArray)) {
                continue;
            }
            $query .= "     REPORT_{$no}.REPORT_ABBV AS REPORT_ABBV{$no}, ";
        }
        $query .= "     CRE_M.CREDITS, ";
        $query .= "     PRINT_ST.PRINT_STATUS, ";
        $query .= "     L1.REMARK ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "           AND T3.YEAR = T1.YEAR ";
        if ($model->Properties["useTsushinSemesKonboHyoji"] == "1") {
            $query .= "       AND T3.SEMESTER = '".$model->semecmb."' ";
        } else {
            $query .= "       AND T3.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query .= "     LEFT JOIN CREDIT_MST CRE_M ON T3.YEAR = CRE_M.YEAR ";
        $query .= "          AND T3.COURSECD = CRE_M.COURSECD ";
        $query .= "          AND T3.MAJORCD = CRE_M.MAJORCD ";
        $query .= "          AND T3.GRADE = CRE_M.GRADE ";
        $query .= "          AND T3.COURSECODE = CRE_M.COURSECODE ";
        $query .= "          AND T1.CLASSCD = CRE_M.CLASSCD ";
        $query .= "          AND T1.SCHOOL_KIND = CRE_M.SCHOOL_KIND ";
        $query .= "          AND T1.CURRICULUM_CD = CRE_M.CURRICULUM_CD ";
        $query .= "          AND T1.SUBCLASSCD = CRE_M.SUBCLASSCD ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T3.YEAR ";
        $query .= "           AND T4.SEMESTER = T3.SEMESTER ";
        $query .= "           AND T4.GRADE = T3.GRADE ";
        $query .= "           AND T4.HR_CLASS = T3.HR_CLASS ";
        $query .= "     LEFT JOIN SUBCLASS_ATTEND_SUM ATTEND ON T1.SCHREGNO = ATTEND.SCHREGNO ";
        $query .= "          AND T1.CLASSCD = ATTEND.CLASSCD ";
        $query .= "          AND T1.SCHOOL_KIND = ATTEND.SCHOOL_KIND ";
        $query .= "          AND T1.CURRICULUM_CD = ATTEND.CURRICULUM_CD ";
        $query .= "          AND T1.SUBCLASSCD = ATTEND.SUBCLASSCD ";
        //テスト
        foreach ($model->testcdArray as $key => $codeArray) {
            //成績
            $query .= "     LEFT JOIN SCORE_HIST R{$key} ON R{$key}.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND R{$key}.TESTCD = '".$codeArray["TESTCD"]."' ";
            //追試チェック用
            $query .= "     LEFT JOIN SCORE_HIST_CHECK V{$key} ON V{$key}.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND V{$key}.TESTCD = '".$codeArray["TESTCD"]."' ";
            //受験許可フラグ
            $query .= "     LEFT JOIN T_PASS_FLG F{$key} ON F{$key}.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND F{$key}.TESTCD = '".$codeArray["TESTCD"]."' ";
            //考査満点マスタ(合格点)
            $query .= "     LEFT JOIN PERFECT_RECORD_SDIV_DAT P{$key} ON P{$key}.YEAR = T1.YEAR ";
            $query .= "          AND P{$key}.SEMESTER || '-' || P{$key}.TESTKINDCD || '-' || P{$key}.TESTITEMCD || '-' || P{$key}.SCORE_DIV = '".$codeArray["TESTCD"]."' ";
            $query .= "          AND P{$key}.CLASSCD = T1.CLASSCD ";
            $query .= "          AND P{$key}.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "          AND P{$key}.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "          AND P{$key}.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "          AND P{$key}.GRADE = CASE WHEN P{$key}.DIV = '01' THEN '00' ELSE T3.GRADE END ";
            $query .= "          AND P{$key}.COURSECD || P{$key}.MAJORCD || P{$key}.COURSECODE = CASE WHEN P{$key}.DIV IN ('01','02') THEN '00000000' ELSE T3.COURSECD || T3.MAJORCD || T3.COURSECODE END ";
        }
        if ($model->Properties["useTsushinSemesKonboHyoji"] == "1") {
            $query .= "     LEFT JOIN SCORE_HIST HIST08 ON HIST08.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND HIST08.TESTCD = '{$model->semecmb}-99-00-08' ";
            $query .= "     LEFT JOIN SCORE_HIST HIST09 ON HIST09.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND HIST09.TESTCD = '{$model->semecmb}-99-00-09' ";
        } else {
            $query .= "     LEFT JOIN SCORE_HIST HIST08 ON HIST08.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND HIST08.TESTCD = '{$hyoukaSeme}-99-00-08' ";
            $query .= "     LEFT JOIN SCORE_HIST HIST09 ON HIST09.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND HIST09.TESTCD = '9-99-00-09' ";
        }
        for ($repCnt = 1; $repCnt <= $model->reportInfo["REP_SEQ_ALL"]; $repCnt++) {
            $no = $model->reportInfo["REP_START_SEQ"] - 1 + $repCnt;
            if ($model->Properties["useTsushinSemesKonboHyoji"] == "1" && !in_array($no, $model->repCntArray)) {
                continue;
            }
            $query .= "     LEFT JOIN REPORT_{$no} ON REPORT_{$no}.CLASSCD = T1.CLASSCD ";
            $query .= "          AND REPORT_{$no}.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "          AND REPORT_{$no}.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "          AND REPORT_{$no}.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "          AND REPORT_{$no}.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND REPORT_{$no}.SCHREGNO = T1.SCHREGNO ";
        }
        //備考
        $query .= "     LEFT JOIN RECORD_REMARK_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "          AND L1.CLASSCD = T1.CLASSCD ";
        $query .= "          AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "          AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "          AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "          AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN PRINT_STATUS PRINT_ST ON PRINT_ST.SCHREGNO = T1.SCHREGNO ";
        //クラス番号順の場合はクラスまで指定する
        if ($model->order == 2) {
            $query .= " ORDER BY ";
            $query .= "     T3.GRADE, ";
            $query .= "     T3.HR_CLASS, ";
            $query .= "     T3.ATTENDNO ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     SUBSTR(T1.SCHREGNO, 1, 4) DESC, ";
            $query .= "     SUBSTR(T1.SCHREGNO, 5, 4) ";
        }

        return $query;
    }

    /* データ更新処理 */
    public function updateQuery($model)
    {
        $db = Query::dbCheckOut();

        $chrsub_array = array();
        $chrsub_array = explode("-", $model->sub);

        $testSeme = ($model->Properties["useTsushinSemesKonboHyoji"] == "1") ? $model->semecmb : "9";

        $gakkiHyoukaZenki = false;
        if ($model->Properties["knjm432wZenkiKamokuUpdateSeme1"] == "1") {
            $result = $db->query(knjm432wQuery::getTest($model));
            $semes = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //学期名称
                if ($row["SEMESTER"] && !in_array($row["SEMESTER"], $semes)) {
                    $semes[] = $row["SEMESTER"];
                }
            }
            $result->free();

            if ($semes == array("1")) {
                $gakkiHyoukaZenki = true;
            }
        }

        for ($i = 1; $i <= count($model->setdata["SCHREGNO"]); $i++) {
            //skip
            if ($model->setdata["NINTEI"][$i] != "1") {
                continue;
            }
            $year = CTRL_YEAR;
            $schno = $model->setdata["SCHREGNO"][$i];
            if ($model->inputCreditOnly == "1") {
                $testCd = "{$testSeme}-99-00-09";
                $setValue = "";
                $setGetCredit = $model->setdata["GET_CREDIT"][$i];
                $setCompCredit = $model->setdata["COMP_CREDIT"][$i];

                $query = knjm432wQuery::getUpdateQuery($db, $model, $chrsub_array, $schno, $testCd, $setValue, $model->setdata["COMP_CONTINUE"][$i], $setGetCredit, $setCompCredit);
                $db->query($query);
            } elseif ($model->Properties["useHyoutei"] == "1") {
                if ($model->Properties["knjm432wZenkiKamokuUpdateSeme1"] == "1" && $gakkiHyoukaZenki) {
                    $testCd = "1-99-00-08";
                } else {
                    $testCd = "{$testSeme}-99-00-08";
                }

                $setValue = $model->setdata["HYOUKA"][$i];
                $setGetCredit = "";
                $setCompCredit = "";

                $query = knjm432wQuery::getUpdateQuery($db, $model, $chrsub_array, $schno, $testCd, $setValue, $model->setdata["COMP_CONTINUE"][$i], $setGetCredit, $setCompCredit);
                $db->query($query);
            } elseif ($model->Properties["useHyoukaHyoutei"] == "1") {
                $testCd = "{$testSeme}-99-00-08";
                $setValue = $model->setdata["HYOUKA"][$i];
                $setGetCredit = "";
                $setCompCredit = "";
                $query = knjm432wQuery::getUpdateQuery($db, $model, $chrsub_array, $schno, $testCd, $setValue, $model->setdata["COMP_CONTINUE"][$i], $setGetCredit, $setCompCredit);
                $db->query($query);

                $testCd = "{$testSeme}-99-00-09";
                $setValue = $model->setdata["HYOUTEI"][$i];
                $setGetCredit = "";
                $setCompCredit = "";
                $query = knjm432wQuery::getUpdateQuery($db, $model, $chrsub_array, $schno, $testCd, $setValue, $model->setdata["COMP_CONTINUE"][$i], $setGetCredit, $setCompCredit);
                $db->query($query);
            } else {
                $testCd = "{$testSeme}-99-00-09";
                $setValue = $model->setdata["HYOUTEI"][$i];
                $setGetCredit = "";
                $setCompCredit = "";

                $query = knjm432wQuery::getUpdateQuery($db, $model, $chrsub_array, $schno, $testCd, $setValue, $model->setdata["COMP_CONTINUE"][$i], $setGetCredit, $setCompCredit);
                $db->query($query);
            }

            if ($model->Properties["useHyoutei"] == "1" && $model->inputCreditOnly != "1") {
                $level = "";
                if (strlen($setValue) > 0 && $setValue > "0") {
                    $query  = " SELECT ";
                    $query .= "     ASSESSLEVEL ";
                    $query .= " FROM ";
                    $query .= "     ASSESS_MST ";
                    $query .= " WHERE ";
                    $query .= "     ASSESSCD = '3' ";
                    $query .= "     AND $setValue BETWEEN ASSESSLOW AND ASSESSHIGH ";
                    $level = $db->getOne($query);
                } elseif ($setValue === "0") {
                    $level = "0";
                }
                if ($model->Properties["knjm432wZenkiKamokuUpdateSeme1"] == "1") {
                    $testCd = "9-99-00-09";
                } else {
                    $testCd = "{$testSeme}-99-00-09";
                }
                $query = knjm432wQuery::getUpdateQuery($db, $model, $chrsub_array, $schno, $testCd, $level, $model->setdata["COMP_CONTINUE"][$i], $setGetCredit, $setCompCredit);
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
    }

    /* データ更新処理 */
    public function getUpdateQuery($db, $model, $chrsub_array, $schno, $testCd, $setValue, $compContinue, $setGetCredit, $setCompCredit)
    {

        $year = CTRL_YEAR;
        $test_array = explode("-", $testCd);

        $data = array();
        $data["YEAR"][TEXT]             = $year;
        $data["SEMESTER"][TEXT]         = $test_array[0];
        $data["TESTKINDCD"][TEXT]       = $test_array[1];
        $data["TESTITEMCD"][TEXT]       = $test_array[2];
        $data["SCORE_DIV"][TEXT]        = $test_array[3];
        $data["CLASSCD"][TEXT]          = $chrsub_array[1];
        $data["SCHOOL_KIND"][TEXT]      = $chrsub_array[2];
        $data["CURRICULUM_CD"][TEXT]    = $chrsub_array[3];
        $data["SUBCLASSCD"][TEXT]       = $chrsub_array[4];
        $data["SCHREGNO"][TEXT]         = $schno;
        $data["SEQ"][NUMBER]            = 1; //1回目のみ
        $data["TEST_DATE"][TEXT]        = CTRL_DATE ;
        $data["CHAIRCD"][TEXT]          = $chrsub_array[0];
        $data["VALUE"][NUMBER]          = $setValue;
        if ($test_array[1] == "99" && $test_array[2] == "00" && $test_array[3] == "09") {
            if ($model->inputCreditOnly == "1") {
                $data["GET_CREDIT"][NUMBER]  = $setGetCredit;
                $data["COMP_CREDIT"][NUMBER] = $setCompCredit;
            } elseif ($setValue === "0") {
                $data["GET_CREDIT"][NUMBER] = 0;
                $data["COMP_CREDIT"][NUMBER] = 0;
            } elseif ($setValue > 1) {
                $data["GET_CREDIT"][NUMBER] = $model->schCredit[$schno];
                $data["COMP_CREDIT"][NUMBER] = $model->schCredit[$schno];
            } elseif (strlen($setValue) == 0) {
                $data["GET_CREDIT"][NUMBER] = null;
                $data["COMP_CREDIT"][NUMBER] = null;
            } else {
                $data["GET_CREDIT"][NUMBER] = null;
                $data["COMP_CREDIT"][NUMBER] = $model->schCredit[$schno];
            }
        }
        if ($compContinue == "1") {
            $data["VALUE"][NUMBER]       = null;
            $data["GET_CREDIT"][NUMBER]  = null;
            $data["ADD_CREDIT"][NUMBER]  = null;
        }
        $data["COMP_CONTINUE"][TEXT]    = $compContinue;
        $data["REGISTERCD"][TEXT]       = STAFFCD ;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //条件
        $where  = " WHERE YEAR = '{$year}' ";
        $where .= "     AND SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV = '{$testCd}' ";
        $where .= "     AND CLASSCD      = '".$chrsub_array[1]."' ";
        $where .= "     AND SCHOOL_KIND  = '".$chrsub_array[2]."' ";
        $where .= "     AND CURRICULUM_CD= '".$chrsub_array[3]."' ";
        $where .= "     AND SUBCLASSCD   = '".$chrsub_array[4]."' ";
        $where .= "     AND SCHREGNO = '{$schno}'";
        $where .= "     AND SEQ = 1 ";

        $query = "SELECT COUNT(*) AS CNT FROM RECORD_SCORE_HIST_DAT ".$where;
        $dataCnt = $db->getOne($query);

        //更新
        if (0 < $dataCnt) {
            $query = Query::updateSQL($data, "RECORD_SCORE_HIST_DAT", $where);
        //追加
        } else {
            $query = Query::insertSQL($data, "RECORD_SCORE_HIST_DAT");
        }
        return $query;
    }

    //発行番号印刷不可をチェック
    public function getCertifSchool()
    {
        $query  = " SELECT ";
        $query .= "     CERTIF_NO "; //0:印刷あり 1:印刷なし
        $query .= " FROM ";
        $query .= "     CERTIF_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     CERTIF_KINDCD = '134' ";

        return $query;
    }

    //証明書最終番号情報を取得
    public function get_certif_maxnumber($db, $model, $schno)
    {
        if ($model->Properties["certif_no_8keta"] == "1") {
            $keta2 = substr(CTRL_YEAR, 2, 2);

            $query = knjm432wQuery::getBumon($model, $schno);
            $keta3 = $db->getOne($query);

            $query = knjm432wQuery::getCeritfDiv();
            $keta4 = $db->getOne($query);

            $query  = " WITH MAX_T AS ( ";
            $query .= " SELECT ";
            $query .= "     COALESCE(MAX(INTEGER(REMARK1)+1),1) as NUMBER ";
            $query .= " FROM ";
            $query .= "     CERTIF_DETAIL_EACHTYPE_SUBCLASS_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '" .CTRL_YEAR ."'";
            $query .= "     AND REMARK1 like '{$keta2}{$keta3}{$keta4}%' ";
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     COALESCE(MAX(INTEGER(REMARK1)+1),1) as NUMBER ";
            $query .= " FROM ";
            $query .= "     CERTIF_DETAIL_EACHTYPE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '" .CTRL_YEAR ."'";
            $query .= "     AND REMARK1 like '{$keta2}{$keta3}{$keta4}%' ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     MAX(NUMBER) as NUMBER ";
            $query .= " FROM ";
            $query .= "     MAX_T ";

            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $row["NUMBER"] = $row["NUMBER"] == 1 ? $keta2.$keta3.$keta4.sprintf("%04d", $row["NUMBER"]) : $row["NUMBER"];
            $row["NUMBER"] = sprintf("%08d", $row["NUMBER"]);
        } else {
            $query  = " WITH MAX_T AS ( ";
            $query .= " SELECT ";
            $query .= "     COALESCE(MAX(INTEGER(CERTIF_NO)+1),1) as NUMBER ";
            $query .= " FROM ";
            $query .= "     CERTIF_ISSUE_SUBCLASS_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '" .CTRL_YEAR ."'";
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     COALESCE(MAX(INTEGER(CERTIF_NO)+1),1) as NUMBER ";
            $query .= " FROM ";
            $query .= "     CERTIF_ISSUE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '" .CTRL_YEAR ."'";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     MAX(NUMBER) as NUMBER ";
            $query .= " FROM ";
            $query .= "     MAX_T ";

            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        return $row["NUMBER"];
    }

    //証明書最終番号情報を取得
    public function getBumon($model, $schno)
    {
        $query .= " SELECT ";
        $query .= "     G005_R.NAME2 AS BUMON ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN NAME_MST G005_R ON G005_R.NAMECD1 = 'G005' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "          AND G005_R.NAMESPARE1 = '".SCHOOLKIND."' ";
        }
        $query .= "          AND G005_R.NAMESPARE2 = REGD.COURSECD ";
        $query .= "          AND G005_R.NAMESPARE3 = REGD.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.SCHREGNO = '{$schno}' ";
        return $query;
    }

    //証明書最終番号情報を取得
    public function getCeritfDiv()
    {
        $query .= " SELECT ";
        $query .= "     CKIND.CERTIF_DIV ";
        $query .= " FROM ";
        $query .= "     CERTIF_KIND_MST CKIND ";
        $query .= " WHERE ";
        $query .= "     CKIND.CERTIF_KINDCD = '134' ";
        return $query;
    }

    //証明書発行者情報を取得
    public function getissuername($db)
    {
        $query  = " SELECT ";
        $query .= "  STAFFNAME_SHOW as NAME ";
        $query .= " FROM ";
        $query .= "  STAFF_MST ";
        $query .= " WHERE ";
        $query .= "  STAFFCD = '".STAFFCD."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row["NAME"];
    }

    //証明書番号最終INDEX情報を取得
    public function get_certif_index_max()
    {
        $query  = " WITH CERTIF_INDEX_MAX AS ( ";
        $query .= " SELECT ";
        $query .= "  COALESCE(MAX(INTEGER(CERTIF_INDEX)+1),1) as INDEX ";
        $query .= " FROM ";
        $query .= "  CERTIF_ISSUE_DAT ";
        $query .= " WHERE ";
        $query .= "  YEAR = '".CTRL_YEAR."'";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "  COALESCE(MAX(INTEGER(CERTIF_INDEX)+1),1) as INDEX ";
        $query .= " FROM ";
        $query .= "  CERTIF_ISSUE_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "  YEAR = '".CTRL_YEAR."'";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "  MAX(INDEX) AS INDEX ";
        $query .= " FROM ";
        $query .= "  CERTIF_INDEX_MAX ";

        return $query;
    }

    //--- 証明書発行
    public function &getUpdateIssueQuery($db, $schno, $name, $no, $date, $certif_no_div, $model)
    {
        $chrsub_array = array();
        $chrsub_array = explode("-", $model->sub);

        $query = knjm432wQuery::get_certif_index_max();
        $index = $db->getOne($query);

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["CERTIF_INDEX"][TEXT]     = $index;
        $data["CLASSCD"][TEXT]          = $chrsub_array[1];
        $data["SCHOOL_KIND"][TEXT]      = $chrsub_array[2];
        $data["CURRICULUM_CD"][TEXT]    = $chrsub_array[3];
        $data["SUBCLASSCD"][TEXT]       = $chrsub_array[4];
        $data["SCHREGNO"][TEXT]         = $schno;
        $data["CERTIF_KINDCD"][TEXT]    = "134";
        $data["GRADUATE_FLG"][TEXT]     = "0";
        $data["APPLYDATE"][DATE]        = CTRL_DATE;
        $data["ISSUERNAME"][TEXT]       = $name;
        $data["ISSUECD"][TEXT]          = "1";
        $data["ISSUEDATE"][DATE]        = CTRL_DATE;
        if ($model->Properties["certifNoSyudou"] != "1") {
            $data["CERTIF_NO"][NUMBER]      = $no;
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "CERTIF_ISSUE_SUBCLASS_DAT");
        $db->query($query);

        if ($model->Properties["certif_no_8keta"] == "1" || $model->Properties["certifNoSyudou"] == "1") {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["CERTIF_INDEX"][TEXT]     = $index;
            $data["CLASSCD"][TEXT]          = $chrsub_array[1];
            $data["SCHOOL_KIND"][TEXT]      = $chrsub_array[2];
            $data["CURRICULUM_CD"][TEXT]    = $chrsub_array[3];
            $data["SUBCLASSCD"][TEXT]       = $chrsub_array[4];
            $data["SCHREGNO"][TEXT]         = $schno;
            $data["TYPE"][TEXT]             = "1";
            $data["REMARK1"][TEXT]          = $no;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "CERTIF_DETAIL_EACHTYPE_SUBCLASS_DAT");
            $db->query($query);
        }
        return $schno."_".$index;
    }
}
