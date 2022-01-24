<?php

require_once('for_php7.php');

class knjd194Query extends Query {

    //学期を取得
    function getSemester($model) {
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["checkKettenDiv"]) {
            $query .= " AND SEMESTER <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //テスト種別
    function getTest($model) {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL, ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        if ($model->Properties["checkKettenDiv"]) {
            $query .= " AND TESTKINDCD || TESTITEMCD <> '9900' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期詳細マスタ(日付の取得)
    function getSemesterDetailMst($semesterDetail) {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTER_DETAIL, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     VALUE(SDATE, '9999-12-31') AS SDATE, ";
        $query .= "     VALUE(EDATE, '9999-12-31') AS EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER_DETAIL = '{$semesterDetail}' ";

        return $query;
    }

    function get_semester_detail($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER_DETAIL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '{$model->semester}' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '{$model->test_cd}' ";

        return $query;
    }

    //学年
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || ':' || GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

/******************************************************************************************/
/******************************************************************************************/
/******************************************************************************************/
    //学期名取得
    function getSemesterName($semester) {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '{$semester}' ";

        return $query;
    }
    //テスト名取得
    function getTestName($model) {
        $query  = " SELECT ";
        $query .= "     TESTITEMNAME ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '{$model->semester}' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '{$model->test_cd}' ";

        return $query;
    }
    //出欠集計開始日付などを取得
    function getAttendDate($model) {
        $semester = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query  = "SELECT SEMESTER ";
        $query .= "      ,MAX(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END) AS MAX_YEAR ";
        $query .= "      ,MONTH ";
        $query .= "      ,MAX(APPOINTED_DAY) AS MAX_APP ";
        $query .= "FROM   ATTEND_SEMES_DAT ";
        $query .= "WHERE  YEAR='".CTRL_YEAR."' AND SEMESTER <= '{$semester}' ";
        $query .= "GROUP BY SEMESTER,MONTH ";
        $query .= "ORDER BY 2,3 ";

        return $query;
    }
    //指定日付の直近の学期を取得
    function getMaxSemester($model, $date) {
        $query  = "SELECT MAX(SEMESTER) AS SEMESTER ";
        $query .= "FROM SEMESTER_MST ";
        $query .= "WHERE  YEAR='".CTRL_YEAR."' AND SEMESTER <> '9' AND SDATE <= '".$date."' ";

        return $query;
    }
    //学校マスタの取得
    function getSchoolMst() {
        //変数
        $year = CTRL_YEAR;
        //SQL
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        return $query;
    }
    //教科担任を取得
    function getSubclassStf($model, $year, $semester, $schregno, $subclasscd) {

        $query .= " WITH CHAIR_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     MIN(T1.CHAIRCD) AS CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$subclasscd."' ";
        } else {
            $query .= "         T1.SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             CHAIR_STD_DAT E1 ";
        $query .= "             INNER JOIN CHAIR_DAT E2 ON E2.YEAR = E1.YEAR ";
        $query .= "                 AND E2.SEMESTER = E1.SEMESTER ";
        $query .= "                 AND E2.CHAIRCD = E1.CHAIRCD ";
        $query .= "             INNER JOIN (SELECT EE1.YEAR, EE1.SEMESTER, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                EE2.CLASSCD || '-' || EE2.SCHOOL_KIND || '-' || EE2.CURRICULUM_CD || '-' || ";
        }
        $query .= "                                EE2.SUBCLASSCD AS SUBCLASSCD ";
        $query .= "                              , EE1.SCHREGNO, MAX(EE1.APPDATE) AS APPDATE ";
        $query .= "                         FROM CHAIR_STD_DAT EE1 ";
        $query .= "                         INNER JOIN CHAIR_DAT EE2 ON EE2.YEAR = EE1.YEAR ";
        $query .= "                             AND EE2.SEMESTER = EE1.SEMESTER ";
        $query .= "                             AND EE2.CHAIRCD = EE1.CHAIRCD ";
        $query .= "                         GROUP BY EE1.YEAR, EE1.SEMESTER, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                EE2.CLASSCD || '-' || EE2.SCHOOL_KIND || '-' || EE2.CURRICULUM_CD || '-' || ";
        }
        $query .= "                                  EE2.SUBCLASSCD ";
        $query .= "                                , EE1.SCHREGNO) L1 ON L1.YEAR = E1.YEAR ";
        $query .= "                 AND L1.SEMESTER = E1.SEMESTER ";
        $query .= "                 AND L1.SUBCLASSCD = ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                E2.CLASSCD || '-' || E2.SCHOOL_KIND || '-' || E2.CURRICULUM_CD || '-' || ";
        }
        $query .= "                     E2.SUBCLASSCD ";
        $query .= "                 AND L1.SCHREGNO = E1.SCHREGNO ";
        $query .= "                 AND L1.APPDATE = E1.APPDATE ";
        $query .= "         WHERE ";
        $query .= "             E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND E1.SEMESTER = '".$semester."' ";
        $query .= "             AND E1.CHAIRCD = T1.CHAIRCD ";
        $query .= "             AND E1.SCHREGNO = '".$schregno."' ";
        $query .= "     ) ";
        $query .= " ), STAFF_T AS ( ";
        $query .= " SELECT ";
        $query .= "     MIN(T1.STAFFCD) AS STAFFCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        $query .= "     AND T1.CHAIRCD IN (SELECT CHAIRCD FROM CHAIR_T) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD IN (SELECT STAFFCD FROM STAFF_T) ";

        return $query;
    }
/******************************************************************************************/
/******************************************************************************************/
/******************************************************************************************/
    //CSV作成(成績不振者)
    function selectCsvQuery2($model,$attend_seme,$month,$attend_sdate,$knjSchoolMst) {
        $semester = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $date = str_replace("/","-",$model->field["EDATE"]);
        $absent_cov      = $knjSchoolMst["ABSENT_COV"];
        $absent_cov_late = $knjSchoolMst["ABSENT_COV_LATE"];
        $amari_kuriage   = $knjSchoolMst["AMARI_KURIAGE"];
        $ctrl_year = CTRL_YEAR;
        $recordTableName = (($model->test_cd == "9900" && $model->field["SOTEN_HYOUKA"] == "1") || ($model->test_cd != "9900" && $model->field["SOTEN_HYOUKA"] == "2")) ? "RECORD_RANK_V_DAT" : "RECORD_RANK_DAT";

        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "             YEAR     = '{$ctrl_year}' ";
        $query .= "         AND SEMESTER = '{$semester}' ";
        if ($model->field["GRADE"] != '99') {
            $query .= "         AND GRADE    = '{$model->field["GRADE"]}' ";
        }
        $query .= " ) ";

        $query .= " ,ATTEND_SUBCLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR = '{$ctrl_year}' ";
        $query .= "         AND SEMESTER <= '{$attend_seme}' ";
        $query .= "         AND MONTH IN ('". implode($month, "','") ."') ";
        $query .= " ) ";

        $query .= " ,SCHEDULE AS ( ";
        $query .= "     SELECT ";
        $query .= "         EXECUTEDATE, ";
        $query .= "         PERIODCD, ";
        $query .= "         CHAIRCD, ";
        $query .= "         DATADIV ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_DAT ";
        $query .= "     WHERE ";
        $query .= "             EXECUTEDATE BETWEEN DATE('{$attend_sdate}') AND DATE('{$date}') ";
        $query .= "         AND PERIODCD != '0' ";
        $query .= " ) ";

        $query .= " ,T_attend_dat AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, ";
        $query .= "         W1.ATTENDDATE, ";
        $query .= "         W1.PERIODCD, ";
        $query .= "         W1.DI_CD, ";
        $query .= "         L1.ABBV2 ";
        $query .= "     FROM ";
        $query .= "         ATTEND_DAT W1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'C001' AND L1.NAMECD2 = W1.DI_CD ";
        $query .= "     WHERE ";
        $query .= "             W1.ATTENDDATE BETWEEN DATE('{$attend_sdate}') AND DATE('{$date}') ";
        $query .= "         AND NOT EXISTS( SELECT ";
        $query .= "                             'X' ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_TRANSFER_DAT T7 ";
        $query .= "                         WHERE ";
        $query .= "                                 T7.SCHREGNO = W1.SCHREGNO ";
        $query .= "                             AND T7.TRANSFERCD IN('1','2') ";
        $query .= "                             AND W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ";
        $query .= " ) ";

        //テスト項目マスタの集計フラグの表
        $query .= " , TEST_COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         '2' AS DATADIV ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST T1, ";
        $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
        $query .= "     WHERE ";
        $query .= "             T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND T2.COUNTFLG   = '0' "; //0：集計しない 0以外：集計する
        $query .= "     ) ";

        $query .= " ,T_attend AS ( ";
        $query .= "     SELECT ";
        $query .= "         S1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         S1.CLASSCD, ";
            $query .= "         S1.SCHOOL_KIND, ";
            $query .= "         S1.CURRICULUM_CD, ";
        }
        $query .= "         S1.SUBCLASSCD, ";
        $query .= "         S1.SEMESTER, ";
        $query .= "         COUNT(S3.SCHREGNO)  AS OFFDAYS, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8'  THEN 1 ELSE 0 END)  AS  ABSENT, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9'  THEN 1 ELSE 0 END)  AS  SUSPEND, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF, ";
        $query .= "         SUM(CASE WHEN S2.DI_CD IN('15','23','24') THEN SMALLINT(VALUE(S2.ABBV2, '1')) ELSE 0 END)  AS  LATE,  ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)  AS  EARLY, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '19' THEN 1 WHEN '20' THEN 1 ELSE 0 END)  AS  VIRUS, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '25' THEN 1 WHEN '26' THEN 1 ELSE 0 END)  AS  KOUDOME ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             T2.SCHREGNO, ";
        $query .= "             T1.EXECUTEDATE, ";
        $query .= "             T1.PERIODCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             T3.CLASSCD, ";
            $query .= "             T3.SCHOOL_KIND, ";
            $query .= "             T3.CURRICULUM_CD, ";
        }
        $query .= "             SUBCLASSCD, ";
        $query .= "             T2.SEMESTER ";
        $query .= "         FROM ";
        $query .= "             SCHEDULE T1, ";
        $query .= "             CHAIR_STD_DAT T2, ";
        $query .= "             CHAIR_DAT T3, ";
        $query .= "             SCHNO T4 ";
        $query .= "         WHERE ";
        $query .= "                 T1.CHAIRCD  = T3.CHAIRCD ";
        $query .= "             AND T3.YEAR     = '{$ctrl_year}' ";
        $query .= "             AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "             AND T2.YEAR     = '{$ctrl_year}' ";
        $query .= "             AND T2.SEMESTER = T3.SEMESTER ";
        $query .= "             AND T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "             AND T4.SCHREGNO = T2.SCHREGNO ";
        $query .= "             AND NOT EXISTS( SELECT ";
        $query .= "                                 'X' ";
        $query .= "                             FROM ";
        $query .= "                                 SCH_CHR_COUNTFLG T5 ";
        $query .= "                             WHERE ";
        $query .= "                                 T5.EXECUTEDATE  = T1.EXECUTEDATE AND ";
        $query .= "                                 T5.PERIODCD     = T1.PERIODCD AND ";
        $query .= "                                 T5.CHAIRCD      = T1.CHAIRCD AND ";
        $query .= "                                 T5.GRADE        = T4.GRADE AND ";
        $query .= "                                 T5.HR_CLASS     = T4.HR_CLASS AND ";
        $query .= "                                 T1.DATADIV      IN ('0','1') AND "; //テスト(DATADIV=2)以外
        $query .= "                                 T5.COUNTFLG     = '0' ";
        $query .= "                           ) ";
        $query .= "             AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ";
        $query .= "                             WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "                               AND TEST.PERIODCD    = T1.PERIODCD ";
        $query .= "                               AND TEST.CHAIRCD     = T1.CHAIRCD ";
        $query .= "                               AND TEST.DATADIV     = T1.DATADIV) "; //テスト(DATADIV=2)
        $query .= "             AND NOT EXISTS( SELECT ";
        $query .= "                                 'X' ";
        $query .= "                             FROM ";
        $query .= "                                 SCHREG_BASE_MST T6 ";
        $query .= "                             WHERE ";
        $query .= "                                 T6.SCHREGNO = T4.SCHREGNO AND ";
        $query .= "                                 ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                                  (T6.ENT_DIV IN('4','5')     AND T6.ENT_DATE > T1.EXECUTEDATE)) ";
        $query .= "                           ) ";
        $query .= "    AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND L4.DI_CD = '27' "; // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "                  ) ";
        $query .= "    AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND L4.DI_CD = '28' "; // 勤怠コード'28'は時間割にカウントしない
        $query .= "                  ) ";
        $query .= "         GROUP BY ";
        $query .= "             T2.SCHREGNO, ";
        $query .= "             T1.EXECUTEDATE, ";
        $query .= "             T1.PERIODCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             T3.CLASSCD, ";
            $query .= "             T3.SCHOOL_KIND, ";
            $query .= "             T3.CURRICULUM_CD, ";
        }
        $query .= "             SUBCLASSCD, ";
        $query .= "             T2.SEMESTER)S1 ";
        $query .= "     LEFT JOIN T_attend_dat        S2 ON  S2.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S2.ATTENDDATE = S1.EXECUTEDATE ";
        $query .= "                                      AND S2.PERIODCD   = S1.PERIODCD ";
        $query .= "     LEFT JOIN SCHREG_TRANSFER_DAT S3 ON  S3.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S3.TRANSFERCD = '2' ";
        $query .= "                                      AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE ";
        $query .= "     LEFT JOIN SCHREG_TRANSFER_DAT S4 ON  S4.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S4.TRANSFERCD = '1' ";
        $query .= "                                      AND S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE ";
        $query .= "     GROUP BY ";
        $query .= "         S1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         S1.CLASSCD, ";
            $query .= "         S1.SCHOOL_KIND, ";
            $query .= "         S1.CURRICULUM_CD, ";
        }
        $query .= "         S1.SUBCLASSCD, ";
        $query .= "         S1.SEMESTER ";
        $query .= " ) ";

        $query .= " ,ATTEND_SUM AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W1.CLASSCD, ";
            $query .= "         W1.SCHOOL_KIND, ";
            $query .= "         W1.CURRICULUM_CD, ";
        }
        $query .= "         W1.SUBCLASSCD, ";
        $query .= "         W1.SEMESTER, ";
        $query .= "         SUM(VALUE(W1.SICK,0) + VALUE(W1.NURSEOFF,0) ";
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .=           "+ VALUE(W1.ABSENT,0)";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .=           "+ VALUE(W1.SUSPEND,0)";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .=           "+ VALUE(W1.MOURNING,0)";
        }
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .=           "+ VALUE(W1.OFFDAYS,0)";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .=           "+ VALUE(W1.VIRUS,0)";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $query .=           "+ VALUE(W1.KOUDOME,0)";
        }
        $query .= "            ) AS SICK, ";
        $query .= "         SUM(VALUE(W1.NOTICE,0))     NOTICE, ";
        $query .= "         SUM(VALUE(W1.NONOTICE,0))   NONOTICE, ";
        $query .= "         SUM(VALUE(W1.ABSENT,0))     ABSENT, ";
        $query .= "         SUM(VALUE(W1.SUSPEND,0))    SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         SUM(VALUE(W1.VIRUS,0))      VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         SUM(VALUE(W1.KOUDOME,0))    KOUDOME, ";
        }
        $query .= "         SUM(VALUE(W1.MOURNING,0))   MOURNING, ";
        $query .= "         SUM(VALUE(W1.LATE,0))       LATE, ";
        $query .= "         SUM(VALUE(W1.EARLY,0))      EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS W1, ";
        $query .= "         SCHNO W0 ";
        $query .= "     WHERE ";
        $query .= "         W1.SCHREGNO = W0.SCHREGNO ";
        $query .= "     GROUP BY ";
        $query .= "         W1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W1.CLASSCD, ";
            $query .= "         W1.SCHOOL_KIND, ";
            $query .= "         W1.CURRICULUM_CD, ";
        }
        $query .= "         W1.SUBCLASSCD, ";
        $query .= "         W1.SEMESTER ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         W2.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W2.CLASSCD, ";
            $query .= "         W2.SCHOOL_KIND, ";
            $query .= "         W2.CURRICULUM_CD, ";
        }
        $query .= "         W2.SUBCLASSCD, ";
        $query .= "         W2.SEMESTER, ";
        $query .= "         VALUE(W2.SICK,0) + VALUE(W2.NURSEOFF,0) ";
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .= "          + VALUE(W2.ABSENT,0)";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .= "          + VALUE(W2.SUSPEND,0)";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .= "          + VALUE(W2.MOURNING,0)";
        }
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .= "          + VALUE(W2.OFFDAYS,0)";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .= "          + VALUE(W2.VIRUS,0)";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $query .= "          + VALUE(W2.KOUDOME,0)";
        }
        $query .= "                AS SICK, ";
        $query .= "         VALUE(W2.NOTICE,0)      NOTICE, ";
        $query .= "         VALUE(W2.NONOTICE,0)    NONOTICE, ";
        $query .= "         VALUE(W2.ABSENT,0)      ABSENT, ";
        $query .= "         VALUE(W2.SUSPEND,0)     SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         VALUE(W2.VIRUS,0)       VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         VALUE(W2.KOUDOME,0)     KOUDOME, ";
        }
        $query .= "         VALUE(W2.MOURNING,0)    MOURNING, ";
        $query .= "         VALUE(W2.LATE,0)        LATE, ";
        $query .= "         VALUE(W2.EARLY,0)       EARLY ";
        $query .= "     FROM ";
        $query .= "         T_attend W2 ";
        $query .= " ) ";

        //学期毎に清算
        $query .= " ,ATTEND_SUM2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SEMESTER, ";
        $query .= "         SUM(SICK)       SICK, ";
        $query .= "         SUM(NOTICE)     NOTICE, ";
        $query .= "         SUM(NONOTICE)   NONOTICE, ";
        $query .= "         SUM(ABSENT)     ABSENT, ";
        $query .= "         SUM(SUSPEND)    SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         SUM(VIRUS)      VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         SUM(KOUDOME)    KOUDOME, ";
        }
        $query .= "         SUM(MOURNING)   MOURNING, ";
        $query .= "         SUM(LATE)       LATE, ";
        $query .= "         SUM(EARLY)      EARLY, ";
        if ($model->Properties["chikokuHyoujiFlg"] == "1") {
            $query .= "     (SUM(LATE) + SUM(EARLY)) AS TIKOKU_SOUTAI, ";
        } else {
            if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     0 AS TIKOKU_SOUTAI, ";
            } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     MOD((sum(LATE) + sum(EARLY)), {$absent_cov_late}) AS TIKOKU_SOUTAI, ";
            } else {
                $query .= "     (SUM(LATE) + SUM(EARLY)) AS TIKOKU_SOUTAI, ";
            }
        }
        if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      decimal((float(sum(LATE) + sum(EARLY)) / {$absent_cov_late}) + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)),4,1) as NOTICE_LATE, ";
        } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ((sum(LATE) + sum(EARLY)) / {$absent_cov_late}) + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)) as NOTICE_LATE, ";
        } else {
            $query .= "      sum(NOTICE) + sum(NONOTICE) + sum(SICK) as NOTICE_LATE, ";
        }
        $query .= "         SUM(LATE) + SUM(EARLY) + SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE2, ";
        $query .= "         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE3 ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM ";
        $query .= "     WHERE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN ( ";
            $query .= "             SELECT DISTINCT ";
            $query .= "                 COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
        } else {
            $query .= "         SUBCLASSCD NOT IN ( ";
            $query .= "             SELECT DISTINCT ";
            $query .= "                 COMBINED_SUBCLASSCD ";
        }
        $query .= "             FROM ";
        $query .= "                 SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "             WHERE ";
        $query .= "                 YEAR = '{$ctrl_year}') ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SEMESTER ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T2.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.COMBINED_CLASSCD         AS CLASSCD, ";
            $query .= "         T1.COMBINED_SCHOOL_KIND     AS SCHOOL_KIND, ";
            $query .= "         T1.COMBINED_CURRICULUM_CD   AS CURRICULUM_CD, ";
        }
        $query .= "         T1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "         T2.SEMESTER, ";
        $query .= "         SUM(T2.SICK)        SICK, ";
        $query .= "         SUM(T2.NOTICE)      NOTICE, ";
        $query .= "         SUM(T2.NONOTICE)    NONOTICE, ";
        $query .= "         SUM(T2.ABSENT)      ABSENT, ";
        $query .= "         SUM(T2.SUSPEND)     SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         SUM(T2.VIRUS)       VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         SUM(T2.KOUDOME)     KOUDOME, ";
        }
        $query .= "         SUM(T2.MOURNING)    MOURNING, ";
        $query .= "         SUM(T2.LATE)        LATE, ";
        $query .= "         SUM(T2.EARLY)       EARLY, ";
        if ($model->Properties["chikokuHyoujiFlg"] == "1") {
            $query .= "     (SUM(T2.LATE) + SUM(T2.EARLY)) AS TIKOKU_SOUTAI, ";
        } else {
            if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     0 AS TIKOKU_SOUTAI, ";
            } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     MOD((sum(T2.LATE) + sum(T2.EARLY)), {$absent_cov_late}) AS TIKOKU_SOUTAI, ";
            } else {
                $query .= "     (SUM(T2.LATE) + SUM(T2.EARLY)) AS TIKOKU_SOUTAI, ";
            }
        }
        if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      decimal((float(sum(T2.LATE) + sum(T2.EARLY)) / {$absent_cov_late}) + (sum(T2.NOTICE) + sum(T2.NONOTICE) + sum(T2.SICK)),4,1) as NOTICE_LATE, ";
        } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ((sum(T2.LATE) + sum(T2.EARLY)) / {$absent_cov_late}) + (sum(T2.NOTICE) + sum(T2.NONOTICE) + sum(T2.SICK)) as NOTICE_LATE, ";
        } else {
            $query .= "      sum(T2.NOTICE) + sum(T2.NONOTICE) + sum(T2.SICK) as NOTICE_LATE, ";
        }
        $query .= "         SUM(LATE) + SUM(EARLY) + SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE2, ";
        $query .= "         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE3 ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT T1, ";
        $query .= "         ATTEND_SUM T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$ctrl_year}' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.ATTEND_CLASSCD = T2.CLASSCD AND ";
            $query .= "         T1.ATTEND_SCHOOL_KIND = T2.SCHOOL_KIND AND ";
            $query .= "         T1.ATTEND_CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "         T1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     GROUP BY ";
        $query .= "         T2.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.COMBINED_CLASSCD, ";
            $query .= "         T1.COMBINED_SCHOOL_KIND, ";
            $query .= "         T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "         T1.COMBINED_SUBCLASSCD, ";
        $query .= "         T2.SEMESTER ";
        $query .= " ) ";

        //年間で清算
        $query .= " ,ATTEND_SUM3 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUM(SICK)       SICK, ";
        $query .= "         SUM(NOTICE)     NOTICE, ";
        $query .= "         SUM(NONOTICE)   NONOTICE, ";
        $query .= "         SUM(ABSENT)     ABSENT, ";
        $query .= "         SUM(SUSPEND)    SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         SUM(VIRUS)      VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         SUM(KOUDOME)    KOUDOME, ";
        }
        $query .= "         SUM(MOURNING)   MOURNING, ";
        $query .= "         SUM(LATE)       LATE, ";
        $query .= "         SUM(EARLY)      EARLY, ";
        $query .= "         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS KETUJISU, ";
        if ($model->Properties["chikokuHyoujiFlg"] == "1") {
                $query .= "     SUM(LATE) + SUM(EARLY) AS TIKOKU_SOUTAI, ";
        } else {
            if (($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     SUM(TIKOKU_SOUTAI) TIKOKU_SOUTAI, ";
            } else {
                if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                    $query .= "      0 AS TIKOKU_SOUTAI, ";
                } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                    $query .= "      MOD((SUM(LATE) + SUM(EARLY)), {$absent_cov_late}) AS TIKOKU_SOUTAI, ";
                } elseif (($absent_cov == "5") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                    $query .= "      CASE WHEN MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) >= {$amari_kuriage} ";
                    $query .= "           THEN MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) -  {$amari_kuriage} ";
                    $query .= "           ELSE MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) ";
                    $query .= "      END AS TIKOKU_SOUTAI, ";
                } else {
                    $query .= "      SUM(LATE) + SUM(EARLY) AS TIKOKU_SOUTAI, ";
                }
            }
        }
        if (($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "     SUM(NOTICE_LATE) NOTICE_LATE, "; //欠課
        } else {
            if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "      DECIMAL((FLOAT(SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)),4,1) AS NOTICE_LATE, ";
            } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "      ((SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)) AS NOTICE_LATE, ";
            } elseif (($absent_cov == "5") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "      ((SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)) + (CASE WHEN MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) >= {$amari_kuriage} ";
                $query .= "                                                                                                          THEN 1 ";
                $query .= "                                                                                                          ELSE 0 ";
                $query .= "                                                                                                     END) AS NOTICE_LATE, ";
            } else {
                $query .= "      SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE, ";
            }
        }
        $query .= "         SUM(LATE) + SUM(EARLY) + SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE2, ";
        $query .= "         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE3 ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM2 ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";

        //評定とか拾ってくる
        if ($model->Properties["checkKettenDiv"] == "1") {
            $query .= " ), MAIN_T AS ( ";
            $query .= " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SEMESTER, ";
            $query .= "     T1.TESTKINDCD, ";
            $query .= "     T1.TESTITEMCD, ";
            $query .= "     T1.SCHREGNO, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T2.GRADE, ";
            $query .= "     T2.COURSECD, ";
            $query .= "     T2.MAJORCD, ";
            $query .= "     T2.COURSECODE ";
            $query .= " FROM ";
            $query .= "     RECORD_SLUMP_DAT T1";
            $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "         AND T2.SEMESTER = '{$semester}' ";
            $query .= "         AND T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "         AND T2.GRADE = '{$model->field["GRADE"]}' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '{$ctrl_year}' ";
            $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
            $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->test_cd}' ";
            $query .= "     AND T1.SLUMP = '1' ";
        } else if ($model->Properties["checkKettenDiv"] == "2") {
            $query .= " ), PERFECT_T AS ( ";
            $query .= " SELECT ";
            $query .= "     YEAR, ";
            $query .= "     SEMESTER, ";
            $query .= "     TESTKINDCD || TESTITEMCD AS TESTCD, ";
            $query .= "     CLASSCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     SCHOOL_KIND, ";
                $query .= "     CURRICULUM_CD, ";
            }
            $query .= "     SUBCLASSCD, ";
            $query .= "     MIN(DIV) AS DIV ";
            $query .= " FROM ";
            $query .= "     PERFECT_RECORD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$ctrl_year}' ";
            $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
            $query .= "     AND TESTKINDCD || TESTITEMCD = '{$model->test_cd}' ";
            $query .= " GROUP BY ";
            $query .= "     YEAR, ";
            $query .= "     SEMESTER, ";
            $query .= "     TESTKINDCD || TESTITEMCD, ";
            $query .= "     CLASSCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     SCHOOL_KIND, ";
                $query .= "     CURRICULUM_CD, ";
            }
            $query .= "     SUBCLASSCD ";
            $query .= " ), PERFECT_MAIN AS ( ";
            $query .= " SELECT ";
            $query .= "     T1.* ";
            $query .= " FROM ";
            $query .= "     PERFECT_RECORD_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             PERFECT_T E1 ";
            $query .= "         WHERE ";
            $query .= "             E1.YEAR = T1.YEAR ";
            $query .= "             AND E1.SEMESTER = T1.SEMESTER ";
            $query .= "             AND E1.TESTCD = T1.TESTKINDCD || T1.TESTITEMCD ";
            $query .= "             AND E1.CLASSCD = T1.CLASSCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "             AND E1.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "             AND E1.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND E1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "             AND E1.DIV = T1.DIV ";
            $query .= "     ) ";
            $query .= " ), SCH_PERFECT AS ( ";
            $query .= " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     L1.SEMESTER, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.COURSECODE, ";
            $query .= "     L1.TESTKINDCD, ";
            $query .= "     L1.TESTITEMCD, ";
            $query .= "     L1.CLASSCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     L1.SCHOOL_KIND, ";
                $query .= "     L1.CURRICULUM_CD, ";
            }
            $query .= "     L1.SUBCLASSCD, ";
            $query .= "     L1.PASS_SCORE ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     LEFT JOIN PERFECT_MAIN L1 ON T1.YEAR = L1.YEAR ";
            $query .= "          AND L1.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE T1.GRADE END ";
            $query .= "          AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = CASE WHEN DIV IN ('01','02') THEN '00000000' ELSE T1.COURSECD || T1.MAJORCD || T1.COURSECODE END ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '{$ctrl_year}' ";
            $query .= "     AND T1.SEMESTER = '{$semester}' ";
            $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";
            $query .= " ), MAIN_T AS ( ";
            $query .= " SELECT DISTINCT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SEMESTER, ";
            $query .= "     T1.TESTKINDCD, ";
            $query .= "     T1.TESTITEMCD, ";
            $query .= "     T1.SCHREGNO, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     L1.GRADE, ";
            $query .= "     L1.COURSECD, ";
            $query .= "     L1.MAJORCD, ";
            $query .= "     L1.COURSECODE ";
            $query .= " FROM ";
            $query .= "     {$recordTableName} T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "         AND T2.SEMESTER = '{$semester}' ";
            $query .= "         AND T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "         AND T2.GRADE = '{$model->field["GRADE"]}' ";
            $query .= "     LEFT JOIN SCH_PERFECT L1 ON T1.YEAR = L1.YEAR ";
            $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
            $query .= "          AND T1.TESTKINDCD || T1.TESTITEMCD = L1.TESTKINDCD || L1.TESTITEMCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "          AND T1.CLASSCD = L1.CLASSCD ";
                $query .= "          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
                $query .= "          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ";
            }
            $query .= "          AND T1.SUBCLASSCD = L1.SUBCLASSCD ";
            $query .= "          AND T1.SCHREGNO = L1.SCHREGNO ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '{$ctrl_year}' ";
            $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
            $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->test_cd}' ";
            $query .= "     AND T1.SUBCLASSCD NOT IN ('333333', '555555', '999999') ";
            $query .= "     AND T1.SCORE < VALUE(L1.PASS_SCORE, -1) ";
        } else {
            $query .= " ), MAIN_T AS ( ";
            $query .= " SELECT DISTINCT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SEMESTER, ";
            $query .= "     T1.TESTKINDCD, ";
            $query .= "     T1.TESTITEMCD, ";
            $query .= "     T1.SCHREGNO, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T2.GRADE, ";
            $query .= "     T2.COURSECD, ";
            $query .= "     T2.MAJORCD, ";
            $query .= "     T2.COURSECODE ";
            $query .= " FROM ";
            $query .= "     {$recordTableName} T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "         AND T2.SEMESTER = '{$semester}' ";
            $query .= "         AND T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "         AND T2.GRADE = '{$model->field["GRADE"]}' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '{$ctrl_year}' ";
            $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
            $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->test_cd}' ";
            $query .= "     AND T1.SUBCLASSCD NOT IN ('333333', '555555', '999999') ";
            $query .= "     AND T1.SCORE < {$model->field["KETTEN"]} ";
        }
        $query .= " ), RECORD_SCORE AS( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SCORE, ";
        $query .= "     SUB_M.SUBCLASSNAME, ";
        $query .= "     CRE.CREDITS, ";
        $query .= "     CASE WHEN MAIN_T.SCHREGNO IS NOT NULL ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '0' ";
        $query .= "     END AS SLUMP_FLG ";
        $query .= " FROM ";
        $query .= "     {$recordTableName} T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = '{$semester}' ";
        $query .= "         AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND T2.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "     LEFT JOIN MAIN_T ON MAIN_T.YEAR = T1.YEAR ";
        $query .= "          AND MAIN_T.SEMESTER = T1.SEMESTER ";
        $query .= "          AND MAIN_T.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "          AND MAIN_T.TESTITEMCD = T1.TESTITEMCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND MAIN_T.CLASSCD = T1.CLASSCD ";
            $query .= "          AND MAIN_T.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "          AND MAIN_T.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "          AND MAIN_T.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "          AND MAIN_T.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     LEFT JOIN SUBCLASS_MST SUB_M ON T1.CLASSCD = SUB_M.CLASSCD ";
            $query .= "                                 AND T1.SCHOOL_KIND = SUB_M.SCHOOL_KIND ";
            $query .= "                                 AND T1.CURRICULUM_CD = SUB_M.CURRICULUM_CD ";
            $query .= "                                 AND T1.SUBCLASSCD = SUB_M.SUBCLASSCD ";
        } else {
            $query .= "     LEFT JOIN SUBCLASS_MST SUB_M ON T1.SUBCLASSCD = SUB_M.SUBCLASSCD ";
        }
        $query .= "     LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = T1.YEAR ";
        $query .= "          AND CRE.COURSECD = T2.COURSECD ";
        $query .= "          AND CRE.MAJORCD = T2.MAJORCD ";
        $query .= "          AND CRE.GRADE = T2.GRADE ";
        $query .= "          AND CRE.COURSECODE = T2.COURSECODE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND CRE.CLASSCD = T1.CLASSCD ";
            $query .= "          AND CRE.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "          AND CRE.CURRICULUM_CD = T1.CURRICULUM_CD ";
        } else {
            $query .= "          AND CRE.CLASSCD = substr(T1.SUBCLASSCD, 1, 2) ";
        }
        $query .= "          AND CRE.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$ctrl_year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->test_cd}' ";
        $query .= "     AND T1.SUBCLASSCD NOT IN ('333333', '555555', '999999') ";
        if ($model->Properties["checkKettenDiv"] == "1") {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     MAIN_T.SCHREGNO, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     MAIN_T.CLASSCD, ";
                $query .= "     MAIN_T.SCHOOL_KIND, ";
                $query .= "     MAIN_T.CURRICULUM_CD, ";
            }
            $query .= "     MAIN_T.SUBCLASSCD, ";
            $query .= "     T1.SCORE, ";
            $query .= "     SUB_M.SUBCLASSNAME, ";
            $query .= "     CRE.CREDITS, ";
            $query .= "     '1' AS SLUMP_FLG ";
            $query .= " FROM ";
            $query .= "     MAIN_T ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = MAIN_T.YEAR ";
            $query .= "         AND T2.SEMESTER = '{$semester}' ";
            $query .= "         AND T2.SCHREGNO = MAIN_T.SCHREGNO ";
            $query .= "         AND T2.GRADE = '{$model->field["GRADE"]}' ";
            $query .= "     LEFT JOIN {$recordTableName} T1 ON MAIN_T.YEAR = T1.YEAR ";
            $query .= "          AND MAIN_T.SEMESTER = T1.SEMESTER ";
            $query .= "          AND MAIN_T.TESTKINDCD = T1.TESTKINDCD ";
            $query .= "          AND MAIN_T.TESTITEMCD = T1.TESTITEMCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "          AND MAIN_T.CLASSCD = T1.CLASSCD ";
                $query .= "          AND MAIN_T.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "          AND MAIN_T.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "          AND MAIN_T.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "          AND MAIN_T.SCHREGNO = T1.SCHREGNO ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     LEFT JOIN SUBCLASS_MST SUB_M ON MAIN_T.CLASSCD = SUB_M.CLASSCD ";
                $query .= "                                 AND MAIN_T.SCHOOL_KIND = SUB_M.SCHOOL_KIND ";
                $query .= "                                 AND MAIN_T.CURRICULUM_CD = SUB_M.CURRICULUM_CD ";
                $query .= "                                 AND MAIN_T.SUBCLASSCD = SUB_M.SUBCLASSCD ";
            } else {
                $query .= "     LEFT JOIN SUBCLASS_MST SUB_M ON MAIN_T.SUBCLASSCD = SUB_M.SUBCLASSCD ";
            }
            $query .= "     LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = MAIN_T.YEAR ";
            $query .= "          AND CRE.COURSECD = T2.COURSECD ";
            $query .= "          AND CRE.MAJORCD = T2.MAJORCD ";
            $query .= "          AND CRE.GRADE = T2.GRADE ";
            $query .= "          AND CRE.COURSECODE = T2.COURSECODE ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "          AND CRE.CLASSCD = MAIN_T.CLASSCD ";
                $query .= "          AND CRE.SCHOOL_KIND = MAIN_T.SCHOOL_KIND ";
                $query .= "          AND CRE.CURRICULUM_CD = MAIN_T.CURRICULUM_CD ";
            } else {
                $query .= "          AND CRE.CLASSCD = substr(MAIN_T.SUBCLASSCD, 1, 2) ";
            }
            $query .= "          AND CRE.SUBCLASSCD = MAIN_T.SUBCLASSCD ";
        }

        /******************************/
        /* 教科・科目 or 総合的な時間 */
        /******************************/
        //上限値を取得
        $query .= " ), T_ABSENCE AS ( ";
            if ($knjSchoolMst["JUGYOU_JISU_FLG"] == '1') { //1：法定時数 2：実時数
                $query .= " SELECT ";
                $query .= "     T2.*, ";
                $query .= "     L1.ABSENCE_HIGH     AS ABSENCE_HIGH, ";
                $query .= "     L1.GET_ABSENCE_HIGH AS GET_ABSENCE_HIGH, ";
                $query .= "     L1.ABSENCE_HIGH     - VALUE(L1.ABSENCE_WARN_RISHU_SEM{$semester}, 0)    AS RISHU_WARN, ";
                $query .= "     L1.GET_ABSENCE_HIGH - VALUE(L1.ABSENCE_WARN_SHUTOKU_SEM{$semester}, 0)  AS SHUTOKU_WARN ";
                $query .= " FROM ";
                $query .= "     SCHNO T1 ";
                $query .= " INNER JOIN ";
                $query .= "     ATTEND_SUM3 T2 ON T2.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT  JOIN ";
                $query .= "     V_CREDIT_MST L1 ON  L1.YEAR     = '{$ctrl_year}' ";
                $query .= "                   AND L1.COURSECD   = T1.COURSECD ";
                $query .= "                   AND L1.MAJORCD    = T1.MAJORCD ";
                $query .= "                   AND L1.GRADE      = T1.GRADE ";
                $query .= "                   AND L1.COURSECODE = T1.COURSECODE ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                   AND L1.CLASSCD = T2.CLASSCD ";
                    $query .= "                   AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ";
                    $query .= "                   AND L1.CURRICULUM_CD = T2.CURRICULUM_CD ";
                }
                $query .= "                   AND L1.SUBCLASSCD = T2.SUBCLASSCD ";
            } else {
                $query .= " SELECT ";
                $query .= "     T2.*, ";
                $query .= "     L1.COMP_ABSENCE_HIGH AS ABSENCE_HIGH, ";
                $query .= "     L1.GET_ABSENCE_HIGH  AS GET_ABSENCE_HIGH, ";
                $query .= "     L1.COMP_ABSENCE_HIGH - VALUE(L2.ABSENCE_WARN_RISHU_SEM{$semester}, 0)   AS RISHU_WARN, ";
                $query .= "     L1.GET_ABSENCE_HIGH  - VALUE(L2.ABSENCE_WARN_SHUTOKU_SEM{$semester}, 0) AS SHUTOKU_WARN ";
                $query .= " FROM ";
                $query .= "     SCHNO T1 ";
                $query .= " INNER JOIN ";
                $query .= "     ATTEND_SUM3 T2 ON T2.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT  JOIN ";
                $query .= "     SCHREG_ABSENCE_HIGH_DAT L1 ON  L1.YEAR       = '{$ctrl_year}' ";
                $query .= "                                AND L1.SCHREGNO   = T1.SCHREGNO ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                                AND L1.CLASSCD = T2.CLASSCD ";
                    $query .= "                                AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ";
                    $query .= "                                AND L1.CURRICULUM_CD = T2.CURRICULUM_CD ";
                }
                $query .= "                                AND L1.SUBCLASSCD = T2.SUBCLASSCD ";
                $query .= "                                AND L1.DIV        = '2' ";
                $query .= " LEFT  JOIN ";
                $query .= "     V_CREDIT_MST L2 ON  L2.YEAR     = '{$ctrl_year}' ";
                $query .= "                   AND L2.COURSECD   = T1.COURSECD ";
                $query .= "                   AND L2.MAJORCD    = T1.MAJORCD ";
                $query .= "                   AND L2.GRADE      = T1.GRADE ";
                $query .= "                   AND L2.COURSECODE = T1.COURSECODE ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                   AND L2.CLASSCD = T2.CLASSCD ";
                    $query .= "                   AND L2.SCHOOL_KIND = T2.SCHOOL_KIND ";
                    $query .= "                   AND L2.CURRICULUM_CD = T2.CURRICULUM_CD ";
                }
                $query .= "                   AND L2.SUBCLASSCD = T2.SUBCLASSCD ";
            }

        //出欠データ（フラグあり）
        $query .= " ), ATTEND_SUM_MAIN AS ( ";
            $query .= " SELECT ";
            $query .= "     T1.*, ";
            $query .= "     CASE WHEN ";
            if ($model->field["JOUGEN_DIV"] == '1') { //1：履修 2：修得
                if ($model->field["OVER_DIV"] == '1') { //1：注意 2：超過
                    $query .= "     T1.RISHU_WARN < T1.NOTICE_LATE AND ";
                    $query .= "     T1.RISHU_WARN > 0 ";
                } else {
                    $query .= "     T1.ABSENCE_HIGH < T1.NOTICE_LATE AND ";
                    $query .= "     T1.ABSENCE_HIGH > 0 ";
                }
            } else {
                if ($model->field["OVER_DIV"] == '1') {
                    $query .= "     T1.SHUTOKU_WARN < T1.NOTICE_LATE AND ";
                    $query .= "     T1.SHUTOKU_WARN > 0 ";
                } else {
                    $query .= "     T1.GET_ABSENCE_HIGH < T1.NOTICE_LATE AND ";
                    $query .= "     T1.GET_ABSENCE_HIGH > 0 ";
                }
            }
            $query .= "          THEN '1' ELSE '0' END AS OVER_FLG ";
            $query .= " FROM ";
            $query .= "     T_ABSENCE T1 ";

        //対象の生徒を取得
        $query .= " ), SUB_MAIN AS ( ";
            //出欠
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD ";
            $query .= " FROM ";
            $query .= "     ATTEND_SUM_MAIN T1 ";
            $query .= " WHERE ";
            $query .= "     T1.OVER_FLG = '1' ";
            //成績
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T3.CLASSCD, ";
                $query .= "     T3.SCHOOL_KIND, ";
                $query .= "     T3.CURRICULUM_CD, ";
            }
            $query .= "     T3.SUBCLASSCD ";
            $query .= " FROM ";
            $query .= "     SCHNO T1 ";
            $query .= " INNER JOIN ";
            $query .= "     RECORD_SCORE T3 ON T3.SCHREGNO = T1.SCHREGNO ";
            $query .= " WHERE ";
            $query .= "     T3.SLUMP_FLG = '1' ";

        $query .= " ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.NAME, ";
        $query .= "     L2.SUBCLASSNAME, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     L2.SUBCLASSCD, ";
        }
        $query .= "     T3.SCORE, ";
        $query .= "     L1.CREDITS, ";
        $query .= "     T2.KETUJISU, "; //欠時数
        $query .= "     T2.TIKOKU_SOUTAI, "; //遅刻・早退
        $query .= "     T2.NOTICE_LATE, "; //欠課
        $query .= "     T2.ABSENT, ";   //公欠数
        $query .= "     T2.MOURNING, "; //忌引数
        $query .= "     T2.SUSPEND ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     + T2.VIRUS ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     + T2.KOUDOME ";
        }
        $query .= "     AS SUSPEND, ";  //出停数
        $query .= "     CASE WHEN T3.SLUMP_FLG = '1' AND T2.OVER_FLG = '1' THEN '1' ";
        $query .= "          WHEN T3.SLUMP_FLG = '1' THEN '2' ";
        $query .= "          WHEN T2.OVER_FLG  = '1' THEN '3' ";
        $query .= "          ELSE '9' END AS FUSIN_FLG ";
        $query .= " FROM ";
        $query .= "     SCHNO T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SUB_MAIN        M1 ON M1.SCHREGNO    = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     ATTEND_SUM_MAIN T2 ON T2.SCHREGNO    = M1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                       AND T2.CLASSCD  = M1.CLASSCD ";
            $query .= "                       AND T2.SCHOOL_KIND  = M1.SCHOOL_KIND ";
            $query .= "                       AND T2.CURRICULUM_CD  = M1.CURRICULUM_CD ";
        }
        $query .= "                       AND T2.SUBCLASSCD  = M1.SUBCLASSCD ";
        $query .= " LEFT JOIN ";
        $query .= "     RECORD_SCORE    T3 ON  T3.SCHREGNO   = M1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T3.CLASSCD = M1.CLASSCD ";
            $query .= "                        AND T3.SCHOOL_KIND = M1.SCHOOL_KIND ";
            $query .= "                        AND T3.CURRICULUM_CD = M1.CURRICULUM_CD ";
        }
        $query .= "                        AND T3.SUBCLASSCD = M1.SUBCLASSCD ";
        $query .= " LEFT  JOIN ";
        $query .= "     CREDIT_MST L1 ON  L1.YEAR       = '{$ctrl_year}' ";
        $query .= "                   AND L1.COURSECD   = T1.COURSECD ";
        $query .= "                   AND L1.MAJORCD    = T1.MAJORCD ";
        $query .= "                   AND L1.GRADE      = T1.GRADE ";
        $query .= "                   AND L1.COURSECODE = T1.COURSECODE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                   AND L1.CLASSCD = M1.CLASSCD ";
            $query .= "                   AND L1.SCHOOL_KIND = M1.SCHOOL_KIND ";
            $query .= "                   AND L1.CURRICULUM_CD = M1.CURRICULUM_CD ";
        }
        $query .= "                   AND L1.SUBCLASSCD = M1.SUBCLASSCD ";
        $query .= " LEFT  JOIN ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SUBCLASS_MST L2 ON  M1.CLASSCD = L2.CLASSCD ";
            $query .= "                    AND  M1.SCHOOL_KIND = L2.SCHOOL_KIND ";
            $query .= "                    AND  M1.CURRICULUM_CD = L2.CURRICULUM_CD ";
            $query .= "                    AND  M1.SUBCLASSCD = L2.SUBCLASSCD ";
        } else {
            $query .= "     SUBCLASS_MST L2 ON  M1.SUBCLASSCD = L2.SUBCLASSCD ";
        }

        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     FUSIN_FLG, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L2.CLASSCD, ";
            $query .= "     L2.SCHOOL_KIND, ";
            $query .= "     L2.CURRICULUM_CD, ";
        }
        $query .= "     L2.SUBCLASSCD ";

        return $query;
    }
}
?>
