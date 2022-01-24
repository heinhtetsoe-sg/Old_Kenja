<?php

require_once('for_php7.php');

class knjx_syukketsukirokuQuery extends Query {

    //名称マスタ
    function getNameMst($field, $namecd1, $namecd2) {
        $query  = " SELECT ";
        $query .= "     ".$field." ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "      NAMECD1 = '".$namecd1."' ";
        $query .= "  AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //学校マスタの校種有無チェック
    function checkSchoolMst() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME  = 'SCHOOL_MST' AND ";
        $query .= "     NAME    = 'SCHOOL_KIND' ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.SEMESTER IN (SELECT ";
        $query .= "                         MAX(SEMESTER) ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT S1 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.YEAR     = S1.YEAR AND ";
        $query .= "                         T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                     ) ";

        return $query;
    }

    //出欠の記録参照
    function getAttendSemesDat($model, $knjSchoolMst) {
        $query  = " WITH ATTEND_SEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "         SUM(VALUE(LESSON,0) - VALUE(ABROAD,0))  AS LESSON, ";
            $query .= "         SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(OFFDAYS,0))  AS SICK, ";
        } else {
            $query .= "         SUM(VALUE(LESSON,0) - VALUE(ABROAD,0) - VALUE(OFFDAYS,0))  AS LESSON, ";
            $query .= "         SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0))  AS SICK, ";
        }
        $query .= "         SUM(VALUE(SUSPEND,0) ";
        if ($model->Properties["useSuspendBunsanHyoji"] != '1') {
            if ($model->Properties["useVirus"] == "true") {
                $query .= "             + VALUE(VIRUS,0) ";
            }
            if ($model->Properties["useKoudome"] == "true") {
                $query .= "             + VALUE(KOUDOME,0) ";
            }
        }
        $query .= "             )  AS SUSPEND, ";
        $query .= "         SUM(VALUE(MOURNING,0)) AS MOURNING, ";
        $query .= "         SUM(VALUE(LATE,0)) AS LATE, ";
        $query .= "         SUM(VALUE(EARLY,0)) AS EARLY ";
        if ($model->Properties["useSuspendBunsanHyoji"] == '1') {
            if ($model->Properties["useVirus"] == "true") {
                $query .= "         , SUM(VALUE(VIRUS,0)) AS VIRUS ";
            }
            if ($model->Properties["useKoudome"] == "true") {
                $query .= "         , SUM(VALUE(KOUDOME,0)) AS KOUDOME ";
            }
        }
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->exp_year."' AND ";
        $query .= "         SCHREGNO    = '".$model->schregno."' ";
        $query .= "     GROUP BY ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        $query .= "         LESSON, ";
        $query .= "         SUSPEND, ";
        $query .= "         MOURNING, ";
        $query .= "         VALUE(LESSON,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0) AS ATTEND1, ";
        $query .= "         SICK, ";
        $query .= "         VALUE(LESSON,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0) - VALUE(SICK,0) AS ATTEND2, ";
        $query .= "         LATE, ";
        $query .= "         EARLY ";
        if ($model->Properties["useSuspendBunsanHyoji"] == '1') {
            if ($model->Properties["useVirus"] == "true") {
                $query .= "         , VIRUS ";
            }
            if ($model->Properties["useKoudome"] == "true") {
                $query .= "         , KOUDOME ";
            }
        }
        $query .= "     FROM ";
        $query .= "         ATTEND_SEM ";
        $query .= " ) ";

        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SEMESTERNAME, ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T2.LESSON, ";
        $query .= "         T2.SUSPEND, ";
        $query .= "         T2.MOURNING, ";
        $query .= "         T2.ATTEND1, ";
        $query .= "         T2.SICK, ";
        $query .= "         T2.ATTEND2, ";
        $query .= "         T2.LATE, ";
        $query .= "         T2.EARLY ";
        if ($model->Properties["useSuspendBunsanHyoji"] == '1') {
            if ($model->Properties["useVirus"] == "true") {
                $query .= "         , T2.VIRUS ";
            }
            if ($model->Properties["useKoudome"] == "true") {
                $query .= "         , T2.KOUDOME ";
            }
        }
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST T1 ";
        $query .= "         LEFT JOIN MAIN T2 ON T1.SEMESTER = T2.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".$model->exp_year."' AND ";
        $query .= "         T1.SEMESTER <> '9' ";
        $query .= "     ORDER BY ";
        $query .= "         T1.SEMESTER ";

        return $query;
    }
}
?>
