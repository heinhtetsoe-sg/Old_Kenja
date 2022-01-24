<?php

require_once('for_php7.php');

class knjb3053query extends Query {

    //学年
    function getGrade($model, $grade = "")
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($grade) {
            $query .= " AND GRADE = '{$grade}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;  
    }

    //科目
    function getSubclass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SUBM.CLASSCD || '-' || SUBM.SCHOOL_KIND || '-' || SUBM.CURRICULUM_CD || '-' || SUBM.SUBCLASSCD AS VALUE, ";
        $query .= "     SUBM.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST CRE ";
        $query .= "     INNER JOIN SUBCLASS_MST SUBM ON CRE.CLASSCD = SUBM.CLASSCD ";
        $query .= "           AND CRE.SCHOOL_KIND = SUBM.SCHOOL_KIND ";
        $query .= "           AND CRE.CURRICULUM_CD = SUBM.CURRICULUM_CD ";
        $query .= "           AND CRE.SUBCLASSCD = SUBM.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     CRE.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CRE.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;  
    }

    //年組
    function getHrStaff($model)
    {
        $query  = " SELECT ";
        $query .= "     REGDH.GRADE, ";
        $query .= "     REGDH.HR_CLASS, ";
        $query .= "     REGDH.HR_NAME, ";
        $query .= "     MAX(REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE) AS COURSE, ";
        $query .= "     STF.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGDH ";
        $query .= "     LEFT JOIN STAFF_MST STF ON REGDH.TR_CD1 = STF.STAFFCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGDH.YEAR = REGD.YEAR ";
        $query .= "          AND REGDH.SEMESTER = REGD.SEMESTER ";
        $query .= "          AND REGDH.GRADE = REGD.GRADE ";
        $query .= "          AND REGDH.HR_CLASS = REGD.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     REGDH.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGDH.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGDH.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " GROUP BY ";
        $query .= "     REGDH.GRADE, ";
        $query .= "     REGDH.HR_CLASS, ";
        $query .= "     REGDH.HR_NAME, ";
        $query .= "     STF.STAFFNAME ";
        $query .= " ORDER BY ";
        $query .= "     REGDH.GRADE, ";
        $query .= "     REGDH.HR_CLASS ";

        return $query;  
    }

    //データ出力
    function getCredits($model)
    {
        $query  = " SELECT ";
        $query .= "     CRE.CLASSCD || '-' || CRE.SCHOOL_KIND || '-' || CRE.CURRICULUM_CD || '-' || CRE.SUBCLASSCD AS VALUE, ";
        $query .= "     CRE.COURSECD || CRE.MAJORCD || CRE.COURSECODE AS COURSE, ";
        $query .= "     CRE.CREDITS ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST CRE ";
        $query .= " WHERE ";
        $query .= "     CRE.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CRE.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;  
    }

}
?>
