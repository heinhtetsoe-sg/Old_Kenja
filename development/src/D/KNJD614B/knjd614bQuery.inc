<?php

require_once('for_php7.php');
class knjd614bQuery extends Query {

    //校種取得
    function getSchoolKind() {
        $query  = " SELECT DISTINCT ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= "     , NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER AS VALUE, ";
        $query .= "    SEMESTER || ':' || SEMESTERNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model)
    {
        $sem = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE || ':' || GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "ORDER BY ";
        $query .= "    GRADE";

        return $query;
    }

    //テスト種別取得
    function getTestcd($model)
    {
        $query  = "SELECT ";
        $query .= "    TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
        $query .= "    TESTKINDCD || TESTITEMCD || SCORE_DIV || ':' || TESTITEMNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= "WHERE ";
        $query .= "    YEAR='".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".$model->field["SEMESTER"] ."' ";
        $query .= "    AND SCORE_DIV = '01' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //クラス一覧取得
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "  HR_NAME AS LABEL, ";
        $query .= "  HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "   AND GRADE = '".$model->field["GRADE"] ."' ";
        $query .= " ORDER BY ";
        $query .= "   HR_CLASS ";
        return $query;
    }

    //科目一覧取得
    function getSubclassCd($model)
    {
        $sem = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query  = " WITH SCH_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER = '".$sem ."' ";
        $query .= "         AND GRADE = '".$model->field["GRADE"] ."' ";
        $query .= " ), RECDAT_BASE AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ";
        $query .= "     , L1.SUBCLASSNAME ";
        $query .= "     , T1.SCHOOL_KIND ";
        $query .= "     , T1.CLASSCD ";
        $query .= "     , T1.CURRICULUM_CD ";
        $query .= "     , T1.SUBCLASSCD AS ORDER_SUBCLASSCD ";
        $query .= "     , 0 AS R_C_FLG ";
        $query .= " FROM ";
        $query .= "     RECORD_RANK_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ";
        $query .= "         ON L1.CLASSCD = T1.CLASSCD ";
        $query .= "         AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "         AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "         AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTCD"] ."' ";
        $query .= "     AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";
        $query .= "     AND T1.SUBCLASSCD NOT IN ('333333', '555555', '999999') ";
        $query .= "     AND T1.CLASSCD < '90' ";
        $query .= " ), UN_I_BASE AS ( ";
        $query .= " SELECT * FROM RECDAT_BASE ";
        $query .= " UNION ALL ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD AS SUBCLASSCD ";
        $query .= "     , L1.SUBCLASSNAME ";
        $query .= "     , T1.COMBINED_SCHOOL_KIND AS SCHOOL_KIND ";
        $query .= "     , T1.COMBINED_CLASSCD AS CLASSCD ";
        $query .= "     , T1.COMBINED_CURRICULUM_CD AS CURRICULUM_CD ";
        $query .= "     , T1.COMBINED_SUBCLASSCD AS ORDER_SUBCLASSCD ";
        $query .= "     , 1 AS R_C_FLG ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_COMBINED_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ";
        $query .= "         ON L1.CLASSCD = T1.COMBINED_CLASSCD ";
        $query .= "         AND L1.SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ";
        $query .= "         AND L1.CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ";
        $query .= "         AND L1.SUBCLASSCD = T1.COMBINED_SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $qeury .= "     AND T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD EXISTS (SELECT SUBCLASSCD FROM RECDAT_BASE) '";
        $qeury .= "     AND T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD NOT EXISTS (SELECT SUBCLASSCD FROM RECDAT_BASE) '";
        $query .= " ) ";
        $query .= " SELECT SUBCLASSCD, SUBCLASSNAME, SCHOOL_KIND, CLASSCD, CURRICULUM_CD, ORDER_SUBCLASSCD, SUM(R_C_FLG) AS FLG FROM UN_I_BASE GROUP BY SUBCLASSCD, SUBCLASSNAME, SCHOOL_KIND, CLASSCD, CURRICULUM_CD, ORDER_SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     SCHOOL_KIND ";
        $query .= "     , CLASSCD ";
        $query .= "     , CURRICULUM_CD ";
        $query .= "     , SUBCLASSCD ";
        return $query;
    }

}
?>
