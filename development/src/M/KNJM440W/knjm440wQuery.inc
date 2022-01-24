<?php

require_once('for_php7.php');

class knjm440wQuery extends Query {

    //学期取得
    function getSemesterMst() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別
    function getTestcd($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS VALUE ";
        $query .= "   , T2.TESTITEMNAME AS LABEL ";
        $query .= " FROM NAME_MST T1 ";
        $query .= " INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T2.SEMESTER || '-' || T2.TESTKINDCD || '-' || T2.TESTITEMCD || '-' || T2.SCORE_DIV = T1.NAMESPARE1 ";
        if ($testcd != '') {
          $query .= "     AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'M002' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
 
    //存在チェック
    function checkSubclassStdPassDat($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_PASS_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->field["TESTCD"]."' ";

        return $query;
    }

    //更新対象
    function selectQuery($model) {
        $query  = " WITH CHAIR AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.CLASSCD, ";
        $query .= "         T2.SCHOOL_KIND, ";
        $query .= "         T2.CURRICULUM_CD, ";
        $query .= "         T2.SUBCLASSCD, ";
        $query .= "         T1.CHAIRCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1, ";
        $query .= "         CHAIR_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.CHAIRCD  = T2.CHAIRCD ";
        $query .= " ) ";
        //レポート回数（規定回数）
        $query .= " , SUBCLASS_STANDARD_SEQ AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         YEAR, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         STANDARD_SEQ, ";
        $query .= "         REPORTDIV ";
        $query .= "     FROM ";
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "         REP_STANDARDDATE_COURSE_DAT T1 ";
        } else {
            $query .= "         REP_STANDARDDATE_DAT T1 ";
        }
        $query .= " ) ";
        $query .= " , STANDARD_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         COUNT(STANDARD_SEQ) AS STANDARD_CNT ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_STANDARD_SEQ T1 ";
        $query .= "         INNER JOIN NAME_MST NMM002 ON NMM002.NAMECD1 = 'M002' ";
        $query .= "             AND NMM002.NAMECD2  = T1.REPORTDIV ";
        $query .= "    WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         REPORTDIV   = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         NMM002.NAMESPARE1 = '".$model->field["SEMESTER"]."-".substr($model->field["TESTCD"], 0, 2)."-".substr($model->field["TESTCD"], 2, 2)."-".substr($model->field["TESTCD"], 4, 2)."' AND ";
        $query .= "         EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     V_NAME_MST N1 ";
        $query .= "                 WHERE ";
        $query .= "                     N1.YEAR     = T1.YEAR AND ";
        $query .= "                     N1.NAMECD1  = 'M002' AND ";
        $query .= "                     N1.NAMECD2  = T1.REPORTDIV ";
        $query .= "                 ) ";
        $query .= "     GROUP BY ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD ";
        $query .= " ) ";
        //レポート回数（再提出回数MAX）
        $query .= " , MAX_SEQ AS ( ";
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
        $query .= "         (SELECT ";
        $query .= "             CLASSCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             CURRICULUM_CD, ";
        $query .= "             SUBCLASSCD, ";
        $query .= "             STANDARD_SEQ ";
        $query .= "         FROM ";
        $query .= "         SUBCLASS_STANDARD_SEQ T1 ";
        $query .= "             INNER JOIN NAME_MST NMM002 ON NMM002.NAMECD1 = 'M002' ";
        $query .= "                 AND NMM002.NAMECD2  = T1.REPORTDIV ";
        $query .= "         WHERE ";
        $query .= "             YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "             REPORTDIV   = '".$model->field["SEMESTER"]."' AND ";
        $query .= "             NMM002.NAMESPARE1 = '".$model->field["SEMESTER"]."-".substr($model->field["TESTCD"], 0, 2)."-".substr($model->field["TESTCD"], 2, 2)."-".substr($model->field["TESTCD"], 4, 2)."' AND ";
        $query .= "             EXISTS (SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         V_NAME_MST N1 ";
        $query .= "                     WHERE ";
        $query .= "                         N1.YEAR     = T1.YEAR AND ";
        $query .= "                         N1.NAMECD1  = 'M002' AND ";
        $query .= "                         N1.NAMECD2  = T1.REPORTDIV ";
        $query .= "                     ) ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR          = '".CTRL_YEAR."' AND ";
        $query .= "         T1.CLASSCD       = T2.CLASSCD AND ";
        $query .= "         T1.SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
        $query .= "         T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        $query .= "         T1.SUBCLASSCD    = T2.SUBCLASSCD AND ";
        $query .= "         T1.STANDARD_SEQ  = T2.STANDARD_SEQ ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.STANDARD_SEQ ";
        $query .= " ) ";
        //合格レポート数
        $query .= " , REP_PASS_CNT AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     SUM(CASE N1.NAMESPARE1 WHEN '1' THEN 1 ELSE 0 END) AS REP_PASS_CNT ";
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
        $query .= "     T1.STANDARD_SEQ  = T2.STANDARD_SEQ AND ";
        $query .= "     T1.REPRESENT_SEQ = T2.REPRESENT_SEQ ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";
        $query .= " ) ";
        //レポート判定
        $query .= " , REP_PASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         CASE WHEN T1.REP_PASS_CNT = T2.STANDARD_CNT THEN '1' ELSE NULL END AS REP_PASS_FLG ";
        $query .= "     FROM ";
        $query .= "         REP_PASS_CNT T1, ";
        $query .= "         STANDARD_CNT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.CLASSCD          = T2.CLASSCD AND ";
        $query .= "         T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
        $query .= "         T1.CURRICULUM_CD    = T2.CURRICULUM_CD AND ";
        $query .= "         T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        $query .= " ) ";
        //スクーリング規定回数
        $query .= " , SCH_SEQ AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUM(VALUE(SCH_SEQ_MIN, 0)) AS SCH_SEQ_MIN ";
        $query .= "     FROM ";
        $query .= "         CHAIR_CORRES_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     GROUP BY ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD ";
        $query .= " ) ";
        //スクーリング回数
        $query .= " , ATTEND_ALL AS ( ";
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
        $query .= " SELECT  ";
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
        $query .= " CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
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
        $query .= " ), ATTEND_KIND_ AS ( ";
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
        $query .= "     FROM ATTEND_KIND_ ";
        $query .= " ), SUBCLASS_ATTEND_SUM AS ( ";
        $query .= "     SELECT ";
        $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, SCHREGNO, SUM(T1.JISU2) AS SCHOOLING_TIME ";
        $query .= "     FROM SUBCLASS_ATTEND T1 ";
        $query .= "     GROUP BY ";
        $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, SCHREGNO ";
        $query .= " ) ";
        //スクーリング実績
        $query .= " , SCH_TIME AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     VALUE(T2.SCH_SEQ_MIN, 0) AS SCH_SEQ_MIN, ";
        $query .= "     SUM(VALUE(T3.SCHOOLING_TIME, 0)) AS SCHOOLING_TIME ";
        $query .= " FROM ";
        $query .= "     CHAIR T1 ";
        $query .= "     LEFT JOIN SCH_SEQ T2 ON T1.SUBCLASSCD    = T2.SUBCLASSCD ";
        $query .= "                         AND T1.CLASSCD       = T2.CLASSCD ";
        $query .= "                         AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
        $query .= "                         AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "     LEFT JOIN SUBCLASS_ATTEND_SUM T3 ON T1.SCHREGNO   = T3.SCHREGNO ";
        $query .= "                           AND T1.CLASSCD    = T3.CLASSCD ";
        $query .= "                           AND T1.SCHOOL_KIND    = T3.SCHOOL_KIND ";
        $query .= "                           AND T1.CURRICULUM_CD    = T3.CURRICULUM_CD ";
        $query .= "                           AND T1.SUBCLASSCD    = T3.SUBCLASSCD ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     SCH_SEQ_MIN ";
        $query .= " ) ";
        //スクーリング判定
        $query .= " , SCH_PASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         CASE WHEN SCH_SEQ_MIN <= SCHOOLING_TIME THEN '1' ELSE NULL END AS SCHOOLING_PASS_FLG ";
        $query .= "     FROM ";
        $query .= "         SCH_TIME ";
        $query .= " ) ";
        //成績
        $query .= " , RECORD AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T2.SCORE, ";
        $query .= "         T2.VALUE ";
        $query .= "     FROM ";
        $query .= "         RECORD_CHKFIN_DAT T1, ";
        $query .= "         V_RECORD_SCORE_HIST_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR          = T2.YEAR AND ";
        $query .= "         T1.YEAR          = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER      = T2.SEMESTER AND ";
        $query .= "         T1.TESTKINDCD    = T2.TESTKINDCD AND ";
        $query .= "         T2.SEMESTER || '-' || T2.TESTKINDCD || '-' || T2.TESTITEMCD || '-' || T2.SCORE_DIV < '".$model->field["SEMESTER"]."-".substr($model->field["TESTCD"], 0, 2)."-".substr($model->field["TESTCD"], 2, 4)."-".substr($model->field["TESTCD"], 4, 2)."' AND ";
        $query .= "         T1.TESTITEMCD    = T2.TESTITEMCD AND ";
        $query .= "         T1.RECORD_DIV    = '2' AND ";
        $query .= "         T1.CLASSCD       = T2.CLASSCD AND ";
        $query .= "         T1.SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
        $query .= "         T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        $query .= "         T1.SUBCLASSCD    = T2.SUBCLASSCD ";
        $query .= " ) ";
        //成績判定
        $query .= " , REC_PASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         MAX(CASE WHEN T2.VALUE IS NULL THEN '1' END) AS NOT_RECORD_PASS_FLG ";
        $query .= "     FROM ";
        $query .= "         CHAIR T1 ";
        $query .= "         LEFT JOIN RECORD T2 ON T1.SCHREGNO      = T2.SCHREGNO ";
        $query .= "                            AND T1.CLASSCD       = T2.CLASSCD ";
        $query .= "                            AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
        $query .= "                            AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "                            AND T1.SUBCLASSCD    = T2.SUBCLASSCD ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD ";
        $query .= " ) ";

        //メイン
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T2.REP_PASS_FLG, ";
        $query .= "     T3.SCHOOLING_PASS_FLG, ";
        $query .= "     T4.NOT_RECORD_PASS_FLG ";
        $query .= " FROM ";
        $query .= "     CHAIR T1 ";
        $query .= "     LEFT JOIN REP_PASS T2 ON T1.SCHREGNO      = T2.SCHREGNO ";
        $query .= "                          AND T1.CLASSCD       = T2.CLASSCD ";
        $query .= "                          AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
        $query .= "                          AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "                          AND T1.SUBCLASSCD    = T2.SUBCLASSCD ";
        $query .= "     LEFT JOIN SCH_PASS T3 ON T1.SCHREGNO      = T3.SCHREGNO ";
        $query .= "                          AND T1.CLASSCD       = T3.CLASSCD ";
        $query .= "                          AND T1.SCHOOL_KIND   = T3.SCHOOL_KIND ";
        $query .= "                          AND T1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "                          AND T1.SUBCLASSCD    = T3.SUBCLASSCD ";
        $query .= "     LEFT JOIN REC_PASS T4 ON T1.SCHREGNO      = T4.SCHREGNO ";
        $query .= "                          AND T1.CLASSCD       = T4.CLASSCD ";
        $query .= "                          AND T1.SCHOOL_KIND   = T4.SCHOOL_KIND ";
        $query .= "                          AND T1.CURRICULUM_CD = T4.CURRICULUM_CD ";
        $query .= "                          AND T1.SUBCLASSCD    = T4.SUBCLASSCD ";


        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_STD_PASS_SDIV_DAT ";
        $query .= " WHERE  ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' ";

        $db->query($query);

        //前期のみの開講科目の場合、レポートとスクーリングをチェックする
        $no_rec_pass_zenki = $db->getCol(knjm440wQuery::getNameMst($model, "1"));
        //後期のみの開講科目の場合、成績は判断に加えない
        $no_rec_pass = $db->getCol(knjm440wQuery::getNameMst($model, "2"));

        //追加
        $query = knjm440wQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $subclass = "";
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["TESTKINDCD"][TEXT]       = substr($model->field["TESTCD"], 0, 2);
            $data["TESTITEMCD"][TEXT]       = substr($model->field["TESTCD"], 2, 2);
            $data["SCORE_DIV"][TEXT]        = substr($model->field["TESTCD"], 4, 2);
            $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]    = $row["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]       = $row["SUBCLASSCD"];
            $subclass = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];

            $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
            $data["REP_PASS_FLG"][TEXT]     = $row["REP_PASS_FLG"];
            if ($model->field["SEMESTER"] == '1' && in_array($subclass, $no_rec_pass_zenki)) {
                $data["SCHOOLING_PASS_FLG"][TEXT] = $row["SCHOOLING_PASS_FLG"];
            } else if ($model->field["SEMESTER"] == '2') {
                $data["SCHOOLING_PASS_FLG"][TEXT] = $row["SCHOOLING_PASS_FLG"];
            }
            $data["SEM_PASS_FLG"][TEXT] = "";
            if ($row["REP_PASS_FLG"] == "1") {
                if ($model->field["SEMESTER"] == '1') {
                    if (!in_array($subclass, $no_rec_pass_zenki)) {
                        $data["SEM_PASS_FLG"][TEXT] = "1";
                    } else if (in_array($subclass, $no_rec_pass_zenki) && $row["SCHOOLING_PASS_FLG"] == "1") {
                        $data["SEM_PASS_FLG"][TEXT] = "1";
                    }
                } else if ($model->field["SEMESTER"] == '2') {
                    if ($row["SCHOOLING_PASS_FLG"] == "1" ) {
                        if (!in_array($subclass, $no_rec_pass) && $row["NOT_RECORD_PASS_FLG"] == "") {
                            $data["SEM_PASS_FLG"][TEXT] = "1";
                        } else if (in_array($subclass, $no_rec_pass)) {
                            $data["SEM_PASS_FLG"][TEXT] = "1";
                        }
                    }
                }
            }

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "SUBCLASS_STD_PASS_SDIV_DAT");
            $db->query($query);

        }
        $result->free();

        $db->commit();  // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    //前期/後期開講科目取得
    function getNameMst($model, $semester) {
        $query  = " SELECT ";
        $query .= "     NAME1 || '-' || ABBV1 || '-' || NAME2 || '-' || NAME3 AS SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        if ($semester === "1") {
            $query .= "     NAMECD1 = 'M015' ";
        } else {
            $query .= "     NAMECD1 = 'M016' ";
        }
        return $query;
    }

}
?>
