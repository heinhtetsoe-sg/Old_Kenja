<?php

require_once('for_php7.php');

class knje153Query extends Query {

    //学年取得
    function getGrade($model, $grade="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE_NAME1 AS LABEL, ";
        $query .= "     T1.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = T3.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.GRADE    = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS ";
        if ($grade) {
            $query .= " AND T1.GRADE    = '".$grade."' ";
        }
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= " AND (   T3.TR_CD1       = '".STAFFCD."' ";
            $query .= "      OR T3.TR_CD2       = '".STAFFCD."' ";
            $query .= "      OR T3.TR_CD3       = '".STAFFCD."' ";
            $query .= "      OR T3.SUBTR_CD1    = '".STAFFCD."' ";
            $query .= "      OR T3.SUBTR_CD2    = '".STAFFCD."' ";
            $query .= "      OR T3.SUBTR_CD3    = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校コード取得
    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //高セキュリティー
    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJE153' AND ";
        $query .= "     INVALID_FLG = '0' ";

        return $query;
    }

    //ＣＳＶ出力
    function getAttendDataCsv($model) {

        $query  = " WITH SCH_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME_SHOW ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.GRADE    = '".$model->field["GRADE"]."' ";
        //出欠データ取得（ATTEND_SEMES_DAT）
        $query .= " ), ATTEND_SEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        if ($model->field["OUTPUT"] == "2") {
            $query .= "         T2.YEAR, ";
            $query .= "         '0' AS SCHOOLCD, ";
        }
        $query .= "         SUM(T2.LESSON) AS LESSON, ";
        $query .= "         SUM(T2.OFFDAYS) AS OFFDAYS, ";
        $query .= "         SUM(T2.ABSENT) AS ABSENT, ";
        $query .= "         SUM(T2.SUSPEND) AS SUSPEND, ";
        $query .= "         SUM(T2.MOURNING) AS MOURNING, ";
        $query .= "         SUM(T2.ABROAD) AS ABROAD, ";
        $query .= "         SUM(T2.SICK+NOTICE+NONOTICE) AS SICK, ";
        $query .= "         SUM(T2.LATE) AS LATE, ";
        $query .= "         SUM(T2.EARLY) AS EARLY ";
        $query .= "     FROM ";
        $query .= "         SCH_INFO T1, ";
        $query .= "         ATTEND_SEMES_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW ";
        if ($model->field["OUTPUT"] == "2") {
            $query .= "        ,T2.YEAR ";
        }
        //前籍校出欠データ取得（SCHREG_ATTENDREC_DAT）
        $query .= " ), ATTENDREC AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        if ($model->field["OUTPUT"] == "2") {
            $query .= "         T2.YEAR, ";
            $query .= "         T2.SCHOOLCD, ";
        }
        $query .= "         SUM(T2.CLASSDAYS) AS LESSON, ";
        $query .= "         SUM(T2.OFFDAYS) AS OFFDAYS, ";
        $query .= "         SUM(T2.ABSENT) AS ABSENT, ";
        $query .= "         SUM(T2.SUSPEND) AS SUSPEND, ";
        $query .= "         SUM(T2.MOURNING) AS MOURNING, ";
        $query .= "         SUM(T2.ABROAD) AS ABROAD, ";
        $query .= "         SUM(T2.SICK+ACCIDENTNOTICE+NOACCIDENTNOTICE) AS SICK, ";
        $query .= "         0 AS LATE, ";
        $query .= "         0 AS EARLY ";
        $query .= "     FROM ";
        $query .= "         SCH_INFO T1, ";
        $query .= "         SCHREG_ATTENDREC_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T2.SCHOOLCD = '1' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW ";
        if ($model->field["OUTPUT"] == "2") {
            $query .= "         ,T2.YEAR ";
            $query .= "         ,T2.SCHOOLCD ";
        }
        $query .= " ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     NAME_SHOW, ";
        $query .= "     VALUE(SUM(LESSON),0) AS LESSON, ";
        $query .= "     VALUE(SUM(OFFDAYS),0) AS OFFDAYS, ";
        $query .= "     VALUE(SUM(ABSENT),0) AS ABSENT, ";
        $query .= "     VALUE(SUM(SUSPEND),0) AS SUSPEND, ";
        $query .= "     VALUE(SUM(MOURNING),0) AS MOURNING, ";
        $query .= "     VALUE(SUM(ABROAD),0) AS ABROAD, ";
        $query .= "     VALUE(SUM(SICK),0) AS SICK, ";
        $query .= "     VALUE(SUM(LATE),0) AS LATE, ";
        $query .= "     VALUE(SUM(EARLY),0) AS EARLY ";
        if ($model->field["OUTPUT"] == "2") {
            $query .= "     ,YEAR ";
            $query .= "     ,SCHOOLCD ";
        }
        $query .= " FROM ";
        $query .= "     (SELECT * FROM ATTEND_SEM UNION SELECT * FROM ATTENDREC) T1 ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     NAME_SHOW ";
        if ($model->field["OUTPUT"] == "2") {
            $query .= "     ,YEAR ";
            $query .= "     ,SCHOOLCD ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     SCHREGNO ";
        if ($model->field["OUTPUT"] == "2") {
            $query .= "     ,YEAR ";
            $query .= "     ,SCHOOLCD DESC ";
        }

        return $query;
    }
}
?>
