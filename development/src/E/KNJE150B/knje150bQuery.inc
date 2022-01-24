<?php

require_once('for_php7.php');

class knje150bQuery extends Query {
    //学年取得
    function getGrade($model, $grade = "") {
        $query  = " SELECT DISTINCT ";
        if (!$grade) {
            $query .= "     GDAT.GRADE AS VALUE, ";
            $query .= "     GDAT.GRADE_NAME1 AS LABEL, ";
        }
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGDH ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGDH.YEAR = GDAT.YEAR ";
        $query .= "           AND REGDH.GRADE = GDAT.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     REGDH.YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     REGDH.SEMESTER    = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (REGDH.TR_CD1, REGDH.TR_CD2, REGDH.TR_CD3, REGDH.SUBTR_CD1, REGDH.SUBTR_CD2, REGDH.SUBTR_CD3) ";
        }
        if ($grade) {
            $query .= "     AND REGDH.GRADE = '{$grade}' ";
        }
        if (!$grade) {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($model, $gradeHr = "") {
        $query  = " SELECT ";
        if (!$gradeHr || ($model->field["DISP"] == "1" && $gradeHr)) {
            $query .= "     REGDH.GRADE || REGDH.HR_CLASS AS VALUE, ";
            $query .= "     REGDH.HR_NAME AS LABEL, ";
        }
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGDH ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGDH.YEAR = GDAT.YEAR ";
        $query .= "           AND REGDH.GRADE = GDAT.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     REGDH.YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     REGDH.SEMESTER    = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (REGDH.TR_CD1, REGDH.TR_CD2, REGDH.TR_CD3, REGDH.SUBTR_CD1, REGDH.SUBTR_CD2, REGDH.SUBTR_CD3) ";
        }
        if ($gradeHr) {
            if ($model->field["DISP"] == "1") {
                $query .= "     AND REGDH.GRADE = '{$gradeHr}' ";
            } else {
                $query .= "     AND REGDH.GRADE || REGDH.HR_CLASS = '{$gradeHr}' ";
            }
        }
        if (!$gradeHr || ($model->field["DISP"] == "1" && $gradeHr)) {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ( ";
                        //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
                        //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "     EXISTS( SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "             WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                 ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE <  ";
        $query .= "                         CASE WHEN T2.EDATE < '".CTRL_DATE."' ";
        $query .= "                              THEN T2.EDATE ELSE '".CTRL_DATE."' END) OR ";
        $query .= "                  (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE >  ";
        $query .= "                         CASE WHEN T2.EDATE < '".CTRL_DATE."' ";
        $query .= "                              THEN T2.EDATE ELSE '".CTRL_DATE."' END)) ) OR ";
                        //留学(1)・休学(2)者
        $query .= "     EXISTS( SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "             WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                   S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                   CASE WHEN T2.EDATE < '".CTRL_DATE."' ";
        $query .= "                        THEN T2.EDATE ELSE '".CTRL_DATE."' END ";
        $query .= "                        BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)) ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //対象生徒
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.SCHREGNO ";

        return $query;
    }

    //設定の初期値取得
    function getHreportConditionDat($model) {
        $query  = " SELECT ";
        $query .= "     *";
        $query .= " FROM ";
        $query .= "     HREPORT_CONDITION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     GRADE       = '".sprintf("%02d", "")."' AND ";
        $query .= "     COURSECD    = '".sprintf("%01d", "")."' AND ";
        $query .= "     MAJORCD     = '".sprintf("%03d", "")."' AND ";
        $query .= "     COURSECODE  = '".sprintf("%04d", "")."' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //CSV出力データ
    function getCsvDat($model) {

        $query  = " WITH GET_ADDR AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "         , T1.ADDR1 ";
        $query .= "         , T1.ADDR2 ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ADDRESS_DAT T1 ";
        $query .= "         INNER JOIN ( ";
        $query .= "             SELECT ";
        $query .= "                 MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= "                 , SCHREGNO ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_ADDRESS_DAT ";
        $query .= "             GROUP BY ";
        $query .= "                 SCHREGNO ";
        $query .= "         ) T2 ";
        $query .= "             ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND T2.ISSUEDATE = T1.ISSUEDATE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "     , T2.NAME ";
        $query .= "     , T2.NAME_KANA ";
        $query .= "     , T2.BIRTHDAY ";
        $query .= "     , T10.NAME2 AS SEX ";
        $query .= "     , T3.HR_NAME ";
        $query .= "     , T1.ATTENDNO ";
        $query .= "     , T4.ADDR1 ";
        $query .= "     , T4.ADDR2 ";
        $query .= "     , T1.YEAR ";
        $query .= "     , T8.CLASSCD || '-' || T8.SCHOOL_KIND  || '-' || T8.CURRICULUM_CD || '-' || T8.SUBCLASSCD AS SUBCLASSCD ";
        $query .= "     , T7.CLASSNAME ";
        $query .= "     , T8.SUBCLASSNAME ";
        $query .= "     , T6.VALUATION ";
        $query .= "     , T5.VIEWCD ";
        $query .= "     , T9.NAME1 AS VIEW ";
        $query .= "     , T8.ELECTDIV ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "         ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "         ON T3.YEAR = T1.YEAR ";
        $query .= "         AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T3.GRADE = T1.GRADE ";
        $query .= "         AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN GET_ADDR T4 ";
        $query .= "         ON T4.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN JVIEWSTAT_SUB_DAT T5 ";
        $query .= "         ON T5.YEAR = T1.YEAR ";
        $query .= "         AND T5.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND T5.SEMESTER = '9' ";
        $query .= "     LEFT JOIN SCHREG_STUDYREC_DAT T6 ";
        $query .= "         ON T6.YEAR = T1.YEAR ";
        $query .= "         AND T6.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND T6.ANNUAL = T1.ANNUAL ";
        $query .= "         AND T6.SCHOOLCD = '0' ";
        $query .= "     LEFT JOIN CLASS_MST T7 ";
        $query .= "         ON (T7.CLASSCD = T5.CLASSCD OR T7.CLASSCD = T6.CLASSCD) ";
        $query .= "         AND (T7.SCHOOL_KIND = T5.SCHOOL_KIND OR T7.SCHOOL_KIND = T6.SCHOOL_KIND) ";
        $query .= "     LEFT JOIN SUBCLASS_MST T8 ";
        $query .= "         ON (T8.CLASSCD = T5.CLASSCD OR T8.CLASSCD = T6.CLASSCD) ";
        $query .= "         AND (T8.SCHOOL_KIND = T5.SCHOOL_KIND OR T8.SCHOOL_KIND = T6.SCHOOL_KIND) ";
        $query .= "         AND (T8.CURRICULUM_CD = T5.CURRICULUM_CD OR T8.CURRICULUM_CD = T6.CURRICULUM_CD) ";
        $query .= "         AND (T8.SUBCLASSCD = T5.SUBCLASSCD OR T8.SUBCLASSCD = T6.SUBCLASSCD) ";
        $query .= "     LEFT JOIN V_NAME_MST T9 ";
        $query .= "         ON T9.YEAR = T1.YEAR ";
        $query .= "         AND T9.ABBV1 = T5.STATUS ";
        $query .= "         AND T9.NAMECD1 = 'D028' ";
        $query .= "     LEFT JOIN V_NAME_MST T10 ";
        $query .= "         ON T10.YEAR = T1.YEAR ";
        $query .= "         AND T10.NAMECD1 = 'Z002' ";
        $query .= "         AND T10.NAMECD2 = T2.SEX ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";

         if ($model->field["DISP"] == "1") {
             $query .= " AND T3.GRADE || T3.HR_CLASS IN ('".str_replace(",", "','", $model->selectdata) ."') ";
         } else {
             $query .= " AND T1.SCHREGNO IN ('".str_replace(",", "','", $model->selectdata) ."') ";
         }

        $query .= "     AND ( ";
        $query .= "         (T5.SCHREGNO IS NULL AND T6.SCHREGNO IS NOT NULL) ";
        $query .= "         OR (T5.SCHREGNO IS NOT NULL AND T6.SCHREGNO IS NULL) ";
        $query .= "         OR ( ";
        $query .= "             T5.CLASSCD || T5.SCHOOL_KIND || T5.CURRICULUM_CD || T5.SUBCLASSCD  ";
        $query .= "                 = T6.CLASSCD || T6.SCHOOL_KIND || T6.CURRICULUM_CD || T6.SUBCLASSCD ";
        $query .= "         ) ";
        $query .= "     ) ";
        $query .= " ORDER BY T3.GRADE, T3.HR_CLASS, T1.ATTENDNO, T8.SUBCLASSCD, T7.SHOWORDER2, T5.VIEWCD, T7.SHOWORDER ";

        return $query;
    }

}
?>
