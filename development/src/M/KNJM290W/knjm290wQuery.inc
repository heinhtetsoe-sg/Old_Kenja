<?php

require_once('for_php7.php');


class knjm290wQuery extends Query {

    //学期マスタ
    function getSemeMst()
    {
        $query .= "  SELECT ";
        $query .= "      SEMESTERNAME LABEL, ";
        $query .= "      SEMESTER AS VALUE    ";
        $query .= "  FROM ";
        $query .= "      SEMESTER_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '" .CTRL_YEAR ."'";
        $query .= "  ORDER BY ";
        $query .= "      SEMESTER ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model)
    {
        $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR='" .CTRL_YEAR."'";
        if ("9" == $model->field["GAKKI"]) {
            $query .= "       AND SEMESTER   = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "       AND SEMESTER   = '".$model->field["GAKKI"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //生徒取得
    function getSchno($model)
    {
        $query  = "SELECT T1.SCHREGNO, ";
        $query .= "       T1.SCHREGNO || '　' || T2.NAME AS NAME ";
        $query .= "FROM   SCHREG_REGD_DAT T1, ";
        $query .= "       SCHREG_BASE_MST T2 ";
        $query .= "WHERE  T1.YEAR       = '".CTRL_YEAR."' AND ";
        if ("9" == $model->field["GAKKI"]) {
            $query .= "       T1.SEMESTER   = '".CTRL_SEMESTER."' AND ";
        } else {
            $query .= "       T1.SEMESTER   = '".$model->field["GAKKI"]."' AND ";
        }
        $query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "       T2.SCHREGNO   = T1.SCHREGNO ";
        $query .= "ORDER BY T1.SCHREGNO ";

        return $query;
    }

    //教科取得
    function getClass($model)
    {
        $query  = "  SELECT DISTINCT ";
        $query .= "      T1.CLASSCD || '-' || T1.SCHOOL_KIND AS VALUE, ";
        $query .= "      T1.CLASSCD || '-' || T1.SCHOOL_KIND || ' ' || L1.CLASSNAME AS LABEL ";
        $query .= "  FROM ";
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "      REP_STANDARDDATE_COURSE_DAT T1 ";
        } else {
            $query .= "      REP_STANDARDDATE_DAT T1 ";
        }
        $query .= "  LEFT JOIN CLASS_MST L1 ON L1.CLASSCD = T1.CLASSCD ";
        $query .= "                        AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //科目取得
    function getSubclass($model, $cd)
    {
        $query  = "  SELECT DISTINCT ";
        $query .= "      T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "      T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '　' || L1.SUBCLASSNAME AS LABEL ";
        $query .= "  FROM ";
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "      REP_STANDARDDATE_COURSE_DAT T1 ";
        } else {
            $query .= "      REP_STANDARDDATE_DAT T1 ";
        }
        $query .= "  LEFT JOIN SUBCLASS_MST L1 ON L1.CLASSCD = T1.CLASSCD ";
        $query .= "                           AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                           AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                           AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND T1.CLASSCD || '-' || T1.SCHOOL_KIND = '{$cd}' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }
}
?>

