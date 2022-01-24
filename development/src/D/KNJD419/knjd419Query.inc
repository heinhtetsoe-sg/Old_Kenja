<?php

require_once('for_php7.php');

class knjd419Query extends Query {
    //年度取得
    function getYear() {
        $query  = "  SELECT ";
        $query .= "      '".CTRL_YEAR."' AS VALUE, ";
        $query .= "      '".CTRL_YEAR."' AS LABEL ";
        $query .= "  FROM ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      YEAR AS VALUE, ";
        $query .= "      YEAR AS LABEL ";
        $query .= "  FROM ";
        $query .= "      GRADE_KIND_COMP_GROUP_YMST ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //学期取得
    function getSemester($model) {
        $query  = "  SELECT ";
        $query .= "      SEMESTER AS VALUE, ";
        $query .= "      SEMESTERNAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      SEMESTER_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".$model->year."' AND ";
        $query .= "      SEMESTER <> '9' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //学部取得
    function getSchoolKind() {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //年組取得
    function getHrClass($model) {
        if ($model->hukusiki_radio == "2") {
            $query  = " SELECT DISTINCT ";
            $query .= "     L0.GHR_CD AS VALUE, ";
            $query .= "     L0.GHR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GHR_DAT T0 ";
            $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT L0 ON L0.YEAR       = T0.YEAR  ";
            $query .= "                                       AND L0.SEMESTER   = T0.SEMESTER ";
            $query .= "                                       AND L0.GHR_CD     = T0.GHR_CD, ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR       = T1.YEAR  ";
            $query .= "                                   AND T2.SEMESTER   = T1.SEMESTER  ";
            $query .= "                                   AND T2.GRADE      = T1.GRADE  ";
            $query .= "                                   AND T2.HR_CLASS   = T1.HR_CLASS, ";
            $query .= "     SCHREG_REGD_GDAT T3 ";
            $query .= " WHERE ";
            $query .= "     T0.YEAR         = '{$model->year}' ";
            $query .= " AND T0.SEMESTER     = '{$model->semester}' ";
            $query .= " AND T0.YEAR         = T1.YEAR ";
            $query .= " AND T0.SEMESTER     = T1.SEMESTER ";
            $query .= " AND T0.SCHREGNO     = T1.SCHREGNO ";
            $query .= " AND T1.YEAR         = T3.YEAR ";
            $query .= " AND T1.GRADE        = T3.GRADE ";
            $query .= " AND T3.SCHOOL_KIND  = '{$model->gakubu_school_kind}' ";
            $query .= " AND T2.GRADE || T2.HR_CLASS NOT IN (SELECT ";
            $query .= "                                         NAMECD2 || NAME1 AS HR_CLASS ";
            $query .= "                                     FROM ";
            $query .= "                                         V_NAME_MST ";
            $query .= "                                     WHERE ";
            $query .= "                                         NAMECD1 = 'A032' ";
            $query .= "                                     AND YEAR = '{$model->year}') ";
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= " AND (   L0.TR_CD1       = '".STAFFCD."' ";
                $query .= "      OR L0.TR_CD2       = '".STAFFCD."' ";
                $query .= "      OR L0.TR_CD3       = '".STAFFCD."' ";
                $query .= "      OR L0.SUBTR_CD1    = '".STAFFCD."' ";
                $query .= "      OR L0.SUBTR_CD2    = '".STAFFCD."' ";
                $query .= "      OR L0.SUBTR_CD3    = '".STAFFCD."') ";
            }
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1, ";
            $query .= "     SCHREG_REGD_GDAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR         = '{$model->year}' ";
            $query .= " AND T1.SEMESTER     = '{$model->semester}' ";
            $query .= " AND T1.YEAR         = T2.YEAR ";
            $query .= " AND T1.GRADE        = T2.GRADE ";
            $query .= " AND T2.SCHOOL_KIND  = '{$model->gakubu_school_kind}' ";
            $query .= " AND T1.GRADE || T1.HR_CLASS NOT IN (SELECT ";
            $query .= "                                         NAMECD2 || NAME1 AS HR_CLASS ";
            $query .= "                                     FROM ";
            $query .= "                                         V_NAME_MST ";
            $query .= "                                     WHERE ";
            $query .= "                                         NAMECD1 = 'A032' ";
            $query .= "                                     AND YEAR = '{$model->year}') ";
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= " AND (   T1.TR_CD1       = '".STAFFCD."' ";
                $query .= "      OR T1.TR_CD2       = '".STAFFCD."' ";
                $query .= "      OR T1.TR_CD3       = '".STAFFCD."' ";
                $query .= "      OR T1.SUBTR_CD1    = '".STAFFCD."' ";
                $query .= "      OR T1.SUBTR_CD2    = '".STAFFCD."' ";
                $query .= "      OR T1.SUBTR_CD3    = '".STAFFCD."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（特別クラス選択時）
    function getHrClass2($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     L1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_DAT T0 ";
        $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT L0 ON L0.YEAR       = T0.YEAR  ";
        $query .= "                                       AND L0.SEMESTER   = T0.SEMESTER ";
        $query .= "                                       AND L0.GHR_CD     = T0.GHR_CD, ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR       = T1.YEAR  ";
        $query .= "                                   AND L1.SEMESTER   = T1.SEMESTER  ";
        $query .= "                                   AND L1.GRADE      = T1.GRADE  ";
        $query .= "                                   AND L1.HR_CLASS   = T1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR       = T1.YEAR  ";
        $query .= "                                   AND L2.GRADE      = T1.GRADE  ";
        $query .= "                                   AND L2.SCHOOL_KIND  = '{$model->gakubu_school_kind}' ";
        $query .= " WHERE ";
        $query .= "     T0.YEAR         = T1.YEAR ";
        $query .= " AND T0.YEAR         = '{$model->year}' ";
        $query .= " AND T0.SEMESTER     = T1.SEMESTER ";
        $query .= " AND T0.SEMESTER     = '{$model->semester}' ";
        $query .= " AND T0.GHR_CD       = '{$model->grade_hr_class}' ";
        $query .= " AND T0.SCHREGNO     = T1.SCHREGNO ";
        $query .= " AND T1.GRADE || T1.HR_CLASS NOT IN (SELECT ";
        $query .= "                                         NAMECD2 || NAME1 AS HR_CLASS ";
        $query .= "                                     FROM ";
        $query .= "                                         V_NAME_MST ";
        $query .= "                                     WHERE ";
        $query .= "                                         NAMECD1 = 'A032' ";
        $query .= "                                     AND YEAR    = '{$model->year}') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //状態区分取得
    function getCondition() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A033' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //グループコード取得
    function getGroupcd($model) {
        $query  = " SELECT ";
        $query .= "     GROUPCD AS VALUE, ";
        $query .= "     GROUPCD || ':' || GROUPNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "      GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     SEMESTER    = '".$model->semester."' AND ";
        $query .= "     GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' AND ";
        if (strlen($model->grade_hr_class) == "2") {
            $query .= "     GHR_CD = '".$model->grade_hr_class."' AND ";
            $query .= "     GRADE || '-' || HR_CLASS = '".$model->grade_hr_class2."' AND ";
        } else {
            $query .= "     GHR_CD = '00' AND ";
            $query .= "     GRADE || '-' || HR_CLASS = '".$model->grade_hr_class."' AND ";
        }
        $query .= "     CONDITION   = '".$model->condition."' ";
        $query .= " ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //指導計画帳票パターン取得
    function getGuidancePattern($model) {
        $query  = " SELECT ";
        $query .= "     T1.GUIDANCE_PATTERN AS VALUE, ";
        $query .= "     T1.GUIDANCE_PATTERN || ':' || T2.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST T1, ";
        $query .= "     NAME_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$model->year."' AND ";
        $query .= "     T1.SEMESTER     = '".$model->semester."' AND ";
        $query .= "     T1.GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' AND ";
        if (strlen($model->grade_hr_class) == "2") {
            $query .= "     T1.GHR_CD = '".$model->grade_hr_class."' AND ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->grade_hr_class2."' AND ";
        } else {
            $query .= "     T1.GHR_CD = '00' AND ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->grade_hr_class."' AND ";
        }
        $query .= "     T1.CONDITION    = '".$model->condition."' AND ";
        $query .= "     T1.GROUPCD      = '".$model->groupcd."' AND ";
        $query .= "     T2.NAMECD1      = 'A035' AND ";
        $query .= "     T2.NAMECD2      = T1.GUIDANCE_PATTERN ";

        return $query;
    }

    //単元項目名取得
    function getGuidanceItemName($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_ITEM_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     SEMESTER    = '".$model->semester."' AND ";
        $query .= "     GAKUBU_SCHOOL_KIND  = '".$model->gakubu_school_kind."' AND ";
        $query .= "     CONDITION   = '".$model->condition."' AND ";
        $query .= "     GUIDANCE_PATTERN    = '".$model->guidance_pattern."' ";

        return $query;
    }

    //リスト取得（左フレーム）
    function getList($model, $row, $flg) {
        $query  = " WITH GROUP_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         GAKUBU_SCHOOL_KIND, ";
        $query .= "         GHR_CD, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         CONDITION, ";
        $query .= "         GROUPCD, ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         HREPORT_GUIDANCE_GROUP_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         GAKUBU_SCHOOL_KIND, ";
        $query .= "         GHR_CD, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         CONDITION, ";
        $query .= "         GROUPCD, ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD ";
        $query .= " ), GROUP_UNIT_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         GAKUBU_SCHOOL_KIND, ";
        $query .= "         GHR_CD, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         CONDITION, ";
        $query .= "         GROUPCD, ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         UNITCD, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         HREPORT_GUIDANCE_GROUP_UNIT_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         GAKUBU_SCHOOL_KIND, ";
        $query .= "         GHR_CD, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         CONDITION, ";
        $query .= "         GROUPCD, ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         UNITCD ";
        $query .= " ) ";

        $query .= " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.*, ";
            $query .= "     L2.SUBCLASSNAME, ";
            $query .= "     L1.UNITCD, ";
            $query .= "     L1.UNITNAME, ";
            $query .= "     CASE WHEN (T1.UNIT_AIM_DIV  = '1' AND L4.CNT > 0) THEN '済み' ";
            $query .= "          WHEN (VALUE(T1.UNIT_AIM_DIV, '') != '1' AND L3.CNT > 0) THEN '済み' ";
            $query .= "          ELSE '' END AS ZUMI ";
        }
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_DAT T1 ";
        $query .= "     LEFT JOIN GRADE_KIND_UNIT_GROUP_YMST L1 ";
        $query .= "             ON T1.YEAR          = L1.YEAR ";
        $query .= "            AND T1.SEMESTER      = L1.SEMESTER ";
        $query .= "            AND T1.GAKUBU_SCHOOL_KIND = L1.GAKUBU_SCHOOL_KIND ";
        $query .= "            AND T1.GHR_CD        = L1.GHR_CD ";
        $query .= "            AND T1.GRADE         = L1.GRADE ";
        $query .= "            AND T1.HR_CLASS      = L1.HR_CLASS ";
        $query .= "            AND T1.CONDITION     = L1.CONDITION ";
        $query .= "            AND T1.GROUPCD       = L1.GROUPCD ";
        $query .= "            AND T1.CLASSCD       = L1.CLASSCD ";
        $query .= "            AND T1.SCHOOL_KIND   = L1.SCHOOL_KIND ";
        $query .= "            AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "            AND T1.SUBCLASSCD    = L1.SUBCLASSCD ";
        $query .= "     LEFT JOIN SUBCLASS_MST L2 ";
        $query .= "             ON T1.CLASSCD       = L2.CLASSCD ";
        $query .= "            AND T1.SCHOOL_KIND   = L2.SCHOOL_KIND ";
        $query .= "            AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ";
        $query .= "            AND T1.SUBCLASSCD    = L2.SUBCLASSCD ";
        $query .= "     LEFT JOIN GROUP_DAT L3 ";
        $query .= "             ON T1.YEAR          = L3.YEAR ";
        $query .= "            AND T1.SEMESTER      = L3.SEMESTER ";
        $query .= "            AND T1.GAKUBU_SCHOOL_KIND = L3.GAKUBU_SCHOOL_KIND ";
        $query .= "            AND T1.GHR_CD        = L3.GHR_CD ";
        $query .= "            AND T1.GRADE         = L3.GRADE ";
        $query .= "            AND T1.HR_CLASS      = L3.HR_CLASS ";
        $query .= "            AND T1.CONDITION     = L3.CONDITION ";
        $query .= "            AND T1.GROUPCD       = L3.GROUPCD ";
        $query .= "            AND T1.CLASSCD       = L3.CLASSCD ";
        $query .= "            AND T1.SCHOOL_KIND   = L3.SCHOOL_KIND ";
        $query .= "            AND T1.CURRICULUM_CD = L3.CURRICULUM_CD ";
        $query .= "            AND T1.SUBCLASSCD    = L3.SUBCLASSCD ";
        $query .= "     LEFT JOIN GROUP_UNIT_DAT L4 ";
        $query .= "             ON T1.YEAR          = L4.YEAR ";
        $query .= "            AND T1.SEMESTER      = L4.SEMESTER ";
        $query .= "            AND T1.GAKUBU_SCHOOL_KIND = L4.GAKUBU_SCHOOL_KIND ";
        $query .= "            AND T1.GHR_CD        = L4.GHR_CD ";
        $query .= "            AND T1.GRADE         = L4.GRADE ";
        $query .= "            AND T1.HR_CLASS      = L4.HR_CLASS ";
        $query .= "            AND T1.CONDITION     = L4.CONDITION ";
        $query .= "            AND T1.GROUPCD       = L4.GROUPCD ";
        $query .= "            AND T1.CLASSCD       = L4.CLASSCD ";
        $query .= "            AND T1.SCHOOL_KIND   = L4.SCHOOL_KIND ";
        $query .= "            AND T1.CURRICULUM_CD = L4.CURRICULUM_CD ";
        $query .= "            AND T1.SUBCLASSCD    = L4.SUBCLASSCD ";
        $query .= "            AND L1.UNITCD        = L4.UNITCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$model->year."' AND ";
        $query .= "     T1.SEMESTER     = '".$model->semester."' AND ";
        $query .= "     T1.GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' AND ";
        if (strlen($model->grade_hr_class) == "2") {
            $query .= "     T1.GHR_CD = '".$model->grade_hr_class."' AND ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->grade_hr_class2."' AND ";
        } else {
            $query .= "     T1.GHR_CD = '00' AND ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->grade_hr_class."' AND ";
        }
        $query .= "     T1.CONDITION    = '".$model->condition."' AND ";
        $query .= "     T1.GROUPCD      = '".$model->groupcd."' ";
        if ($flg == "cnt") {
            $query .= "     AND ";
            $query .= "     T1.CLASSCD          = '".$row["CLASSCD"]."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$row["SCHOOL_KIND"]."' AND ";
            $query .= "     T1.CURRICULUM_CD    = '".$row["CURRICULUM_CD"]."' AND ";
            $query .= "     T1.SUBCLASSCD       = '".$row["SUBCLASSCD"]."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     L1.UNITCD ";
        }

        return $query;
    }

    //科目名取得
    function getSubclassName($model) {
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ' ' || SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     CLASSCD         = '".$model->classcd."' AND ";
        $query .= "     SCHOOL_KIND     = '".$model->school_kind."' AND ";
        $query .= "     CURRICULUM_CD   = '".$model->curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD      = '".$model->subclasscd."' ";

        return $query;
    }

    //単元一覧取得
    function getUnitcdList($model) {
        $query  = " SELECT ";
        $query .= "     UNITNAME AS LABEL, ";
        $query .= "     UNITCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_UNIT_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     SEMESTER        = '".$model->semester."' AND ";
        $query .= "     GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' AND ";
        if (strlen($model->grade_hr_class) == "2") {
            $query .= "     GHR_CD = '".$model->grade_hr_class."' AND ";
            $query .= "     GRADE || '-' || HR_CLASS = '".$model->grade_hr_class2."' AND ";
        } else {
            $query .= "     GHR_CD = '00' AND ";
            $query .= "     GRADE || '-' || HR_CLASS = '".$model->grade_hr_class."' AND ";
        }
        $query .= "     CONDITION       = '".$model->condition."' AND ";
        $query .= "     GROUPCD         = '".$model->groupcd."' AND ";
        $query .= "     CLASSCD         = '".$model->classcd."' AND ";
        $query .= "     SCHOOL_KIND     = '".$model->school_kind."' AND ";
        $query .= "     CURRICULUM_CD   = '".$model->curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD      = '".$model->subclasscd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //項目数取得 -- HREPORT_GUIDANCE_GROUP_HDAT
    function getHreportGuidanceGroupHdat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_GROUP_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     SEMESTER        = '".$model->semester."' AND ";
        $query .= "     GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' AND ";
        if (strlen($model->grade_hr_class) == "2") {
            $query .= "     GHR_CD = '".$model->grade_hr_class."' AND ";
            $query .= "     GRADE || '-' || HR_CLASS = '".$model->grade_hr_class2."' AND ";
        } else {
            $query .= "     GHR_CD = '00' AND ";
            $query .= "     GRADE || '-' || HR_CLASS = '".$model->grade_hr_class."' AND ";
        }
        $query .= "     CONDITION       = '".$model->condition."' AND ";
        $query .= "     GROUPCD         = '".$model->groupcd."' AND ";
        $query .= "     CLASSCD         = '".$model->classcd."' AND ";
        $query .= "     SCHOOL_KIND     = '".$model->school_kind."' AND ";
        $query .= "     CURRICULUM_CD   = '".$model->curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD      = '".$model->subclasscd."' ";

        return $query;
    }

    //所見取得 -- HREPORT_GUIDANCE_GROUP_DAT
    function getHreportGuidanceGroupDat($model, $seq="") {
        $query  = " SELECT ";
            $query .= "     T1.* ";
        if ($seq) {
            $query .= "    ,GROUP_REMARK AS GROUP_PROCEDURE ";
        }
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_GROUP_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     SEMESTER        = '".$model->semester."' AND ";
        $query .= "     GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' AND ";
        if (strlen($model->grade_hr_class) == "2") {
            $query .= "     GHR_CD = '".$model->grade_hr_class."' AND ";
            $query .= "     GRADE || '-' || HR_CLASS = '".$model->grade_hr_class2."' AND ";
        } else {
            $query .= "     GHR_CD = '00' AND ";
            $query .= "     GRADE || '-' || HR_CLASS = '".$model->grade_hr_class."' AND ";
        }
        $query .= "     CONDITION       = '".$model->condition."' AND ";
        $query .= "     GROUPCD         = '".$model->groupcd."' AND ";
        $query .= "     CLASSCD         = '".$model->classcd."' AND ";
        $query .= "     SCHOOL_KIND     = '".$model->school_kind."' AND ";
        $query .= "     CURRICULUM_CD   = '".$model->curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD      = '".$model->subclasscd."' ";
        if ($seq) {
            $query .= " AND SEQ             = ".$seq." ";
        }

        return $query;
    }

    //所見取得 -- HREPORT_GUIDANCE_GROUP_UNIT_DAT
    function getHreportGuidanceGroupUnitDat($model, $unitcd) {
        $query  = " SELECT ";
        $query .= "     GROUP_REMARK1, ";
        $query .= "     GROUP_REMARK2 ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_GROUP_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     SEMESTER        = '".$model->semester."' AND ";
        $query .= "     GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' AND ";
        if (strlen($model->grade_hr_class) == "2") {
            $query .= "     GHR_CD = '".$model->grade_hr_class."' AND ";
            $query .= "     GRADE || '-' || HR_CLASS = '".$model->grade_hr_class2."' AND ";
        } else {
            $query .= "     GHR_CD = '00' AND ";
            $query .= "     GRADE || '-' || HR_CLASS = '".$model->grade_hr_class."' AND ";
        }
        $query .= "     CONDITION       = '".$model->condition."' AND ";
        $query .= "     GROUPCD         = '".$model->groupcd."' AND ";
        $query .= "     CLASSCD         = '".$model->classcd."' AND ";
        $query .= "     SCHOOL_KIND     = '".$model->school_kind."' AND ";
        $query .= "     CURRICULUM_CD   = '".$model->curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD      = '".$model->subclasscd."' AND ";
        $query .= "     UNITCD          = '".$unitcd."' ";

        return $query;
    }

    //所見取得 -- HREPORT_GUIDANCE_GROUP_YDAT
    function getHreportGuidanceGroupYdat($model) {
        $query  = " SELECT ";
        $query .= "     GROUP_YEAR_TARGET ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_GROUP_YDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' AND ";
        if (strlen($model->grade_hr_class) == "2") {
            $query .= "     GHR_CD = '".$model->grade_hr_class."' AND ";
            $query .= "     GRADE || '-' || HR_CLASS = '".$model->grade_hr_class2."' AND ";
        } else {
            $query .= "     GHR_CD = '00' AND ";
            $query .= "     GRADE || '-' || HR_CLASS = '".$model->grade_hr_class."' AND ";
        }
        $query .= "     CONDITION       = '".$model->condition."' AND ";
        $query .= "     GROUPCD         = '".$model->groupcd."' AND ";
        $query .= "     CLASSCD         = '".$model->classcd."' AND ";
        $query .= "     SCHOOL_KIND     = '".$model->school_kind."' AND ";
        $query .= "     CURRICULUM_CD   = '".$model->curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD      = '".$model->subclasscd."' ";

        return $query;
    }

    //更新 -- HREPORT_GUIDANCE_GROUP_DAT
    function &getUpdateQuery1($model, $ghr_cd, $grade, $hr_class, $seq, $remark) {
        $data = array();
        $data["YEAR"][TEXT]                 = $model->year;
        $data["SEMESTER"][TEXT]             = $model->semester;
        $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $model->gakubu_school_kind;
        $data["GHR_CD"][TEXT]               = $ghr_cd;
        $data["GRADE"][TEXT]                = $grade;
        $data["HR_CLASS"][TEXT]             = $hr_class;
        $data["CONDITION"][TEXT]            = $model->condition;
        $data["GROUPCD"][TEXT]              = $model->groupcd;
        $data["CLASSCD"][TEXT]              = $model->classcd;
        $data["SCHOOL_KIND"][TEXT]          = $model->school_kind;
        $data["CURRICULUM_CD"][TEXT]        = $model->curriculum_cd;
        $data["SUBCLASSCD"][TEXT]           = $model->subclasscd;
        $data["SEQ"][NUMBER]                = $seq;
        $data["GROUP_REMARK"][TEXT]         = $remark;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_GROUP_DAT");
        return $query;
    }

    //更新 -- HREPORT_GUIDANCE_GROUP_UNIT_DAT
    function &getUpdateQuery2($model, $field2, $ghr_cd, $grade, $hr_class) {
        $data = array();
        $data["YEAR"][TEXT]                 = $model->year;
        $data["SEMESTER"][TEXT]             = $model->semester;
        $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $model->gakubu_school_kind;
        $data["GHR_CD"][TEXT]               = $ghr_cd;
        $data["GRADE"][TEXT]                = $grade;
        $data["HR_CLASS"][TEXT]             = $hr_class;
        $data["CONDITION"][TEXT]            = $model->condition;
        $data["GROUPCD"][TEXT]              = $model->groupcd;
        $data["CLASSCD"][TEXT]              = $model->classcd;
        $data["SCHOOL_KIND"][TEXT]          = $model->school_kind;
        $data["CURRICULUM_CD"][TEXT]        = $model->curriculum_cd;
        $data["SUBCLASSCD"][TEXT]           = $model->subclasscd;
        $data["UNITCD"][TEXT]               = $model->field2["UNITCD"];
        foreach (explode(',', $field2) as $key) {
            $data[$key][TEXT]                   = $model->field2[$key];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_GROUP_UNIT_DAT");
        return $query;
    }

    //更新 -- HREPORT_GUIDANCE_GROUP_YDAT
    function &getUpdateQuery3($model, $field3, $ghr_cd, $grade, $hr_class) {
        $data = array();
        $data["YEAR"][TEXT]                 = $model->year;
        $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $model->gakubu_school_kind;
        $data["GHR_CD"][TEXT]               = $ghr_cd;
        $data["GRADE"][TEXT]                = $grade;
        $data["HR_CLASS"][TEXT]             = $hr_class;
        $data["CONDITION"][TEXT]            = $model->condition;
        $data["GROUPCD"][TEXT]              = $model->groupcd;
        $data["CLASSCD"][TEXT]              = $model->classcd;
        $data["SCHOOL_KIND"][TEXT]          = $model->school_kind;
        $data["CURRICULUM_CD"][TEXT]        = $model->curriculum_cd;
        $data["SUBCLASSCD"][TEXT]           = $model->subclasscd;
        foreach (explode(',', $field3) as $key) {
            $data[$key][TEXT]                   = $model->field3[$key];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_GROUP_YDAT");
        return $query;
    }

    //更新 -- HREPORT_GUIDANCE_GROUP_HDAT
    function &getUpdateQuery4($model, $ghr_cd, $grade, $hr_class) {
        $data = array();
        $data["YEAR"][TEXT]                 = $model->year;
        $data["SEMESTER"][TEXT]             = $model->semester;
        $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $model->gakubu_school_kind;
        $data["GHR_CD"][TEXT]               = $ghr_cd;
        $data["GRADE"][TEXT]                = $grade;
        $data["HR_CLASS"][TEXT]             = $hr_class;
        $data["CONDITION"][TEXT]            = $model->condition;
        $data["GROUPCD"][TEXT]              = $model->groupcd;
        $data["CLASSCD"][TEXT]              = $model->classcd;
        $data["SCHOOL_KIND"][TEXT]          = $model->school_kind;
        $data["CURRICULUM_CD"][TEXT]        = $model->curriculum_cd;
        $data["SUBCLASSCD"][TEXT]           = $model->subclasscd;
        $data["GROUP_REMARK_CNT"][TEXT]     = $model->fieldH["GROUP_REMARK_CNT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_GROUP_HDAT");
        return $query;
    }

    //削除 -- HREPORT_GUIDANCE_GROUP_DAT
    function &getDeleteQuery1($model, $ghr_cd, $grade, $hr_class, $seq="") {
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."' AND ";
        $query .= "     SEMESTER            = '".$model->semester."' AND ";
        $query .= "     GAKUBU_SCHOOL_KIND  = '".$model->gakubu_school_kind."' AND ";
        $query .= "     GHR_CD              = '".$ghr_cd."' AND ";
        $query .= "     GRADE               = '".$grade."' AND ";
        $query .= "     HR_CLASS            = '".$hr_class."' AND ";
        $query .= "     CONDITION           = '".$model->condition."' AND ";
        $query .= "     GROUPCD             = '".$model->groupcd."' AND ";
        $query .= "     CLASSCD             = '".$model->classcd."' AND ";
        $query .= "     SCHOOL_KIND         = '".$model->school_kind."' AND ";
        $query .= "     CURRICULUM_CD       = '".$model->curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD          = '".$model->subclasscd."' AND ";
        if ($seq) {
            $query .= "     SEQ                 = $seq ";
        } else {
            $query .= "     (SEQ                 < 51 ";
            $query .= "      OR SEQ BETWEEN  61 AND 70) ";
        }

        return $query;
    }

    //削除 -- HREPORT_GUIDANCE_GROUP_UNIT_DAT
    function &getDeleteQuery2($model, $ghr_cd, $grade, $hr_class) {
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_GROUP_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."' AND ";
        $query .= "     SEMESTER            = '".$model->semester."' AND ";
        $query .= "     GAKUBU_SCHOOL_KIND  = '".$model->gakubu_school_kind."' AND ";
        $query .= "     GHR_CD              = '".$ghr_cd."' AND ";
        $query .= "     GRADE               = '".$grade."' AND ";
        $query .= "     HR_CLASS            = '".$hr_class."' AND ";
        $query .= "     CONDITION           = '".$model->condition."' AND ";
        $query .= "     GROUPCD             = '".$model->groupcd."' AND ";
        $query .= "     CLASSCD             = '".$model->classcd."' AND ";
        $query .= "     SCHOOL_KIND         = '".$model->school_kind."' AND ";
        $query .= "     CURRICULUM_CD       = '".$model->curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD          = '".$model->subclasscd."' AND ";
        $query .= "     UNITCD              = '".$model->field2["UNITCD"]."'  ";

        return $query;
    }

    //削除 -- HREPORT_GUIDANCE_GROUP_YDAT
    function &getDeleteQuery3($model, $ghr_cd, $grade, $hr_class) {
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_GROUP_YDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."' AND ";
        $query .= "     GAKUBU_SCHOOL_KIND  = '".$model->gakubu_school_kind."' AND ";
        $query .= "     GHR_CD              = '".$ghr_cd."' AND ";
        $query .= "     GRADE               = '".$grade."' AND ";
        $query .= "     HR_CLASS            = '".$hr_class."' AND ";
        $query .= "     CONDITION           = '".$model->condition."' AND ";
        $query .= "     GROUPCD             = '".$model->groupcd."' AND ";
        $query .= "     CLASSCD             = '".$model->classcd."' AND ";
        $query .= "     SCHOOL_KIND         = '".$model->school_kind."' AND ";
        $query .= "     CURRICULUM_CD       = '".$model->curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD          = '".$model->subclasscd."' ";

        return $query;
    }

    //削除 -- HREPORT_GUIDANCE_GROUP_HDAT
    function &getDeleteQuery4($model, $ghr_cd, $grade, $hr_class) {
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_GROUP_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."' AND ";
        $query .= "     SEMESTER            = '".$model->semester."' AND ";
        $query .= "     GAKUBU_SCHOOL_KIND  = '".$model->gakubu_school_kind."' AND ";
        $query .= "     GHR_CD              = '".$ghr_cd."' AND ";
        $query .= "     GRADE               = '".$grade."' AND ";
        $query .= "     HR_CLASS            = '".$hr_class."' AND ";
        $query .= "     CONDITION           = '".$model->condition."' AND ";
        $query .= "     GROUPCD             = '".$model->groupcd."' AND ";
        $query .= "     CLASSCD             = '".$model->classcd."' AND ";
        $query .= "     SCHOOL_KIND         = '".$model->school_kind."' AND ";
        $query .= "     CURRICULUM_CD       = '".$model->curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD          = '".$model->subclasscd."' ";

        return $query;
    }

    //MAX学期取得
    function getMaxSemester() {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     MAX(SEMESTER) AS MAX_SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " GROUP BY ";
        $query .= "      YEAR ";

        return $query;
    }

    //存在チェック（コピー） -- GRADE_KIND_COMP_GROUP_DAT
    function checkGradeKindCompGroupDat($year, $semester) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' ";
        $query .= " AND SEMESTER = '".$semester."' ";

        return $query;
    }

    //存在チェック（コピー） -- HREPORT_GUIDANCE_GROUP_DAT
    function checkHreportGuidanceGroupDat($year, $semester) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";

        return $query;
    }

    //存在チェック（コピー） -- HREPORT_GUIDANCE_GROUP_UNIT_DAT
    function checkHreportGuidanceGroupUnitDat($year, $semester) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_GROUP_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";

        return $query;
    }

    //存在チェック（コピー） -- HREPORT_GUIDANCE_GROUP_YDAT
    function checkHreportGuidanceGroupYdat($year, $semester) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_GROUP_YDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' ";

        return $query;
    }

    //コピー用データ取得 -- HREPORT_GUIDANCE_GROUP_DAT
    function copyPreData1($model, $pre_year, $pre_semester) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_GROUP_DAT T1, ";
        $query .= "     GRADE_KIND_COMP_GROUP_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.GAKUBU_SCHOOL_KIND   = T2.GAKUBU_SCHOOL_KIND AND ";
        $query .= "     T1.GHR_CD               = T2.GHR_CD AND ";
        $query .= "     T1.GRADE                = T2.GRADE AND ";
        $query .= "     T1.HR_CLASS             = T2.HR_CLASS AND ";
        $query .= "     T1.CONDITION            = T2.CONDITION AND ";
        $query .= "     T1.GROUPCD              = T2.GROUPCD AND ";
        $query .= "     T1.CLASSCD              = T2.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND          = T2.SCHOOL_KIND AND ";
        $query .= "     T1.CURRICULUM_CD        = T2.CURRICULUM_CD AND ";
        $query .= "     T1.SUBCLASSCD           = T2.SUBCLASSCD AND ";
        $query .= "     T1.YEAR                 = '".$pre_year."' AND ";
        $query .= "     T1.SEMESTER             = '".$pre_semester."' AND ";
        $query .= "     T2.YEAR                 = '".$model->year."' AND ";
        $query .= "     T2.SEMESTER             = '".$model->semester."' ";

        return $query;
    }

    //コピー -- HREPORT_GUIDANCE_GROUP_DAT
    function &copyInsertQuery1($model, $Row) {
        $data = array();
        $data["YEAR"][TEXT]                 = $model->year;
        $data["SEMESTER"][TEXT]             = $model->semester;
        $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $Row["GAKUBU_SCHOOL_KIND"];
        $data["GHR_CD"][TEXT]               = $Row["GHR_CD"];
        $data["GRADE"][TEXT]                = $Row["GRADE"];
        $data["HR_CLASS"][TEXT]             = $Row["HR_CLASS"];
        $data["CONDITION"][TEXT]            = $Row["CONDITION"];
        $data["GROUPCD"][TEXT]              = $Row["GROUPCD"];
        $data["CLASSCD"][TEXT]              = $Row["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]          = $Row["SCHOOL_KIND"];
        $data["CURRICULUM_CD"][TEXT]        = $Row["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]           = $Row["SUBCLASSCD"];
        $data["SEQ"][NUMBER]                = $Row["SEQ"];
        $data["GROUP_REMARK"][TEXT]         = $Row["GROUP_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_GROUP_DAT");
        return $query;
    }

    //コピー用データ取得 -- HREPORT_GUIDANCE_GROUP_UNIT_DAT
    function copyPreData2($model, $pre_year, $pre_semester) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_GROUP_UNIT_DAT T1, ";
        $query .= "     GRADE_KIND_COMP_GROUP_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.GAKUBU_SCHOOL_KIND   = T2.GAKUBU_SCHOOL_KIND AND ";
        $query .= "     T1.GHR_CD               = T2.GHR_CD AND ";
        $query .= "     T1.GRADE                = T2.GRADE AND ";
        $query .= "     T1.HR_CLASS             = T2.HR_CLASS AND ";
        $query .= "     T1.CONDITION            = T2.CONDITION AND ";
        $query .= "     T1.GROUPCD              = T2.GROUPCD AND ";
        $query .= "     T1.CLASSCD              = T2.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND          = T2.SCHOOL_KIND AND ";
        $query .= "     T1.CURRICULUM_CD        = T2.CURRICULUM_CD AND ";
        $query .= "     T1.SUBCLASSCD           = T2.SUBCLASSCD AND ";
        $query .= "     T1.YEAR                 = '".$pre_year."' AND ";
        $query .= "     T1.SEMESTER             = '".$pre_semester."' AND ";
        $query .= "     T2.YEAR                 = '".$model->year."' AND ";
        $query .= "     T2.SEMESTER             = '".$model->semester."' ";

        return $query;
    }

    //コピー -- HREPORT_GUIDANCE_GROUP_UNIT_DAT
    function &copyInsertQuery2($model, $Row) {
        $data = array();
        $data["YEAR"][TEXT]                 = $model->year;
        $data["SEMESTER"][TEXT]             = $model->semester;
        $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $Row["GAKUBU_SCHOOL_KIND"];
        $data["GHR_CD"][TEXT]               = $Row["GHR_CD"];
        $data["GRADE"][TEXT]                = $Row["GRADE"];
        $data["HR_CLASS"][TEXT]             = $Row["HR_CLASS"];
        $data["CONDITION"][TEXT]            = $Row["CONDITION"];
        $data["GROUPCD"][TEXT]              = $Row["GROUPCD"];
        $data["CLASSCD"][TEXT]              = $Row["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]          = $Row["SCHOOL_KIND"];
        $data["CURRICULUM_CD"][TEXT]        = $Row["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]           = $Row["SUBCLASSCD"];
        $data["UNITCD"][TEXT]               = $Row["UNITCD"];
        $data["GROUP_REMARK1"][TEXT]        = $Row["GROUP_REMARK1"];
        $data["GROUP_REMARK2"][TEXT]        = $Row["GROUP_REMARK2"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_GROUP_UNIT_DAT");
        return $query;
    }

    //コピー用データ取得 -- HREPORT_GUIDANCE_GROUP_YDAT
    function copyPreData3($model, $pre_year, $pre_semester) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_GROUP_YDAT T1, ";
        $query .= "     GRADE_KIND_COMP_GROUP_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.GAKUBU_SCHOOL_KIND   = T2.GAKUBU_SCHOOL_KIND AND ";
        $query .= "     T1.GHR_CD               = T2.GHR_CD AND ";
        $query .= "     T1.GRADE                = T2.GRADE AND ";
        $query .= "     T1.HR_CLASS             = T2.HR_CLASS AND ";
        $query .= "     T1.CONDITION            = T2.CONDITION AND ";
        $query .= "     T1.GROUPCD              = T2.GROUPCD AND ";
        $query .= "     T1.CLASSCD              = T2.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND          = T2.SCHOOL_KIND AND ";
        $query .= "     T1.CURRICULUM_CD        = T2.CURRICULUM_CD AND ";
        $query .= "     T1.SUBCLASSCD           = T2.SUBCLASSCD AND ";
        $query .= "     T1.YEAR                 = '".$pre_year."' AND ";
        $query .= "     T2.YEAR                 = '".$model->year."' AND ";
        $query .= "     T2.SEMESTER             = '".$model->semester."' ";

        return $query;
    }

    //コピー -- HREPORT_GUIDANCE_GROUP_YDAT
    function &copyInsertQuery3($model, $Row) {
        $data = array();
        $data["YEAR"][TEXT]                 = $model->year;
        $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $Row["GAKUBU_SCHOOL_KIND"];
        $data["GHR_CD"][TEXT]               = $Row["GHR_CD"];
        $data["GRADE"][TEXT]                = $Row["GRADE"];
        $data["HR_CLASS"][TEXT]             = $Row["HR_CLASS"];
        $data["CONDITION"][TEXT]            = $Row["CONDITION"];
        $data["GROUPCD"][TEXT]              = $Row["GROUPCD"];
        $data["CLASSCD"][TEXT]              = $Row["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]          = $Row["SCHOOL_KIND"];
        $data["CURRICULUM_CD"][TEXT]        = $Row["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]           = $Row["SUBCLASSCD"];
        $data["GROUP_YEAR_TARGET"][TEXT]    = $Row["GROUP_YEAR_TARGET"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_GROUP_YDAT");
        return $query;
    }

    //コピー用データ取得 -- HREPORT_GUIDANCE_GROUP_HDAT
    function copyPreData4($model, $pre_year, $pre_semester) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_GROUP_HDAT T1, ";
        $query .= "     GRADE_KIND_COMP_GROUP_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.GAKUBU_SCHOOL_KIND   = T2.GAKUBU_SCHOOL_KIND AND ";
        $query .= "     T1.GHR_CD               = T2.GHR_CD AND ";
        $query .= "     T1.GRADE                = T2.GRADE AND ";
        $query .= "     T1.HR_CLASS             = T2.HR_CLASS AND ";
        $query .= "     T1.CONDITION            = T2.CONDITION AND ";
        $query .= "     T1.GROUPCD              = T2.GROUPCD AND ";
        $query .= "     T1.CLASSCD              = T2.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND          = T2.SCHOOL_KIND AND ";
        $query .= "     T1.CURRICULUM_CD        = T2.CURRICULUM_CD AND ";
        $query .= "     T1.SUBCLASSCD           = T2.SUBCLASSCD AND ";
        $query .= "     T1.YEAR                 = '".$pre_year."' AND ";
        $query .= "     T1.SEMESTER             = '".$pre_semester."' AND ";
        $query .= "     T2.YEAR                 = '".$model->year."' AND ";
        $query .= "     T2.SEMESTER             = '".$model->semester."' ";

        return $query;
    }

    //コピー -- HREPORT_GUIDANCE_GROUP_HDAT
    function &copyInsertQuery4($model, $Row) {
        $data = array();
        $data["YEAR"][TEXT]                 = $model->year;
        $data["SEMESTER"][TEXT]             = $model->semester;
        $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $Row["GAKUBU_SCHOOL_KIND"];
        $data["GHR_CD"][TEXT]               = $Row["GHR_CD"];
        $data["GRADE"][TEXT]                = $Row["GRADE"];
        $data["HR_CLASS"][TEXT]             = $Row["HR_CLASS"];
        $data["CONDITION"][TEXT]            = $Row["CONDITION"];
        $data["GROUPCD"][TEXT]              = $Row["GROUPCD"];
        $data["CLASSCD"][TEXT]              = $Row["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]          = $Row["SCHOOL_KIND"];
        $data["CURRICULUM_CD"][TEXT]        = $Row["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]           = $Row["SUBCLASSCD"];
        $data["GROUP_REMARK_CNT"][TEXT]     = $Row["GROUP_REMARK_CNT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_GROUP_HDAT");
        return $query;
    }
}
?>
