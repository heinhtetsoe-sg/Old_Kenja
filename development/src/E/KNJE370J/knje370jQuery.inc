<?php

require_once('for_php7.php');


class knje370jQuery extends Query {

    //年組取得（権限チェック）
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T1.HR_NAME AS LABEL, ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_GDAT T2 ON  T1.YEAR = T2.YEAR ";
        $query .= "                         AND T1.GRADE = T2.GRADE ";
        $query .= "                         AND T2.SCHOOL_KIND IN ('".SCHOOLKIND."') ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T1.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3 = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //駿台甲府判定
    function checkSundaiKoufu() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     NAME1   = 'sundaikoufu' ";

        return $query;
    }

    //学校名
    function getSchoolName($model)
    {
        $query  = " SELECT ";
        $query .= "     FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$model->notPrint}' ";

        return $query;
    }

    //進学
    function getPrintSingaku($model, $year, $semester, $senkouKind, $selectdata)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     AFT.SEQ, ";
        $query .= "     AFT.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD_H.HR_NAME, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     Z002.ABBV1 AS SEX, ";
        $query .= "     AFT.SENKOU_KIND, ";
        $query .= "     AFT.STAT_CD, ";
        $query .= "     FINSCHOOL.FINSCHOOL_NAME AS STAT_NAME, ";
        $query .= "     L015.NAME1 as GAKKOU_RITSU, ";
        $query .= "     AFT.BUNAME, ";
        $query .= "     AFT.PREF_CD, ";
        $query .= "     PREF.PREF_NAME, ";
        $query .= "     AFT.HOWTOEXAM, ";
        $query .= "     E002.NAME1 as HOWTOEXAM_NAME, ";
        $query .= "     AFT.DECISION, ";
        $query .= "     E005.NAME1 as DECISION_NAME, ";
        $query .= "     AFT.PLANSTAT, ";
        $query .= "     AFT_GRAD_D.REMARK9 AS EXAMNO, ";
        $query .= "     E006.NAME1 as PLANSTAT_NAME ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT AFT ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = AFT.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = AFT.SCHREGNO ";
        $query .= "                                  AND REGD.YEAR     = '{$year}' ";
        $query .= "                                  AND REGD.SEMESTER = '{$semester}' ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT REGD_H ON REGD_H.YEAR     = REGD.YEAR ";
        $query .= "                                   AND REGD_H.SEMESTER = REGD.SEMESTER ";
        $query .= "                                   AND REGD_H.GRADE    = REGD.GRADE ";
        $query .= "                                   AND REGD_H.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     INNER JOIN FINSCHOOL_MST FINSCHOOL ON AFT.STAT_CD = FINSCHOOL.FINSCHOOLCD ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT AFT_GRAD_D ON AFT.YEAR = AFT_GRAD_D.YEAR ";
        $query .= "                                        AND AFT.SEQ = AFT_GRAD_D.SEQ ";
        $query .= "                                        AND AFT_GRAD_D.DETAIL_SEQ = 1 ";
        $query .= "     LEFT JOIN PREF_MST PREF ON AFT.PREF_CD = PREF.PREF_CD ";
        $query .= "     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = AFT.HOWTOEXAM ";
        $query .= "     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = AFT.DECISION ";
        $query .= "     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = AFT.PLANSTAT ";
        $query .= "     LEFT JOIN NAME_MST L015 ON L015.NAMECD1 = 'L015' AND L015.NAMECD2 = FINSCHOOL.FINSCHOOL_DIV ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = BASE.SEX ";
        $query .= " WHERE ";
        $query .= "         AFT.YEAR         = '{$year}' ";
        $query .= "     AND AFT.SENKOU_KIND  = '{$senkouKind}' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS IN ('{$selectdata}') ";
        if ($model->field["NOT_PRINT"]) {
            $query .= "     AND AFT.STAT_CD != '{$model->field["NOT_PRINT"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     AFT.SEQ ";

        return $query;
    }
}
?>
