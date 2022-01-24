<?php

require_once('for_php7.php');


class knjd666mQuery extends Query
{

    //学期取得
    public function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    public function getSchoolKind()
    {
        $query .= " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }

    //年組取得（権限チェック）
    public function getHrClass($year, $semester, $schoolKind)
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        $query .= "     AND T2.SCHOOL_KIND = '".$schoolKind."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象生徒
    public function getStudent($model, $seme)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.SCHREGNO AS SCHREGNO_SHOW, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$seme."' ";
        $query .= "     AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //考査取得
    public function getTestCd($year, $semester)
    {
        $query .= " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV || ' ' || TESTITEMNAME AS LABEL  ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     SCORE_DIV = '01' "; //固定
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //科目取得
    public function getSubclass($year, $semester, $grade_hr_class, $model)
    {
    
        $query .= " SELECT DISTINCT ";
        $query .= "     T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD AS VALUE, ";
        $query .= "     T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD || ' ' || T4.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     CHAIR_STD_DAT T2 ";
        $query .= "      ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " INNER JOIN ";
        $query .= "     CHAIR_DAT T3 ";
        $query .= "      ON T3.YEAR = T2.YEAR ";
        $query .= "     AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "     AND T3.CHAIRCD = T2.CHAIRCD ";
        $query .= "     AND T3.CLASSCD <= '89' "; //固定
        $query .= " INNER JOIN ";
        $query .= "     SUBCLASS_MST T4 ";
        $query .= "      ON T4.CLASSCD = T3.CLASSCD ";
        $query .= "     AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "     AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "     AND T4.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_REPLACE_COMBINED_DAT T5 ";
        $query .= "      ON T5.YEAR = T3.YEAR ";
        $query .= "     AND T5.COMBINED_CLASSCD = T3.CLASSCD ";
        $query .= "     AND T5.COMBINED_SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "     AND T5.COMBINED_CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "     AND T5.COMBINED_SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' AND ";
        $query .= "     T1.SEMESTER = '".$semester."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$grade_hr_class."' AND ";
        $query .= "     T5.REPLACECD IS NULL AND ";
        $query .= "     T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD NOT IN ";
        $query .= "     ('".$model->art1."','".$model->art2."','".$model->art3."','".$model->art4."')"; //美術は対象外
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }
}
