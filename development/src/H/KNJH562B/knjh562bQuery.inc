<?php

require_once('for_php7.php');

class knjh562bQuery extends Query
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

    //卒業学年の取得
    public function getGraduateGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " AND NAME1   = '".$model->schoolKind."' ";
        
        return $query;
    }

    //学年取得
    public function getGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別取得
    public function getDataDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'H508' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト名称取得
    public function getProName($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     T1.PROFICIENCYCD || ':' || T2.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST T1";
        $query .= " LEFT JOIN PROFICIENCY_MST T2 ON T1.PROFICIENCYDIV = T2.PROFICIENCYDIV";
        $query .= "                             AND T1.PROFICIENCYCD  = T2.PROFICIENCYCD";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' AND ";
        $query .= "     T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //実施科目取得
    public function getTestSubclass($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD AS VALUE, ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD || ':' || T2.SUBCLASS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_DAT T1 ";
        $query .= " LEFT JOIN PROFICIENCY_SUBCLASS_MST T2 ON T2.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ";
        $query .= " LEFT JOIN  ( ";
        $query .= "         SELECT DISTINCT ";
        $query .= "             W2.ATTEND_SUBCLASSCD ";
        $query .= "         FROM ";
        $query .= "             PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT W2 ";
        $query .= "         WHERE ";
        $query .= "                 W2.YEAR           = '".CTRL_YEAR."' ";
        $query .= "             AND W2.SEMESTER       = '".$model->field["SEMESTER"]."' ";
        $query .= "             AND W2.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "             AND W2.PROFICIENCYCD  = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= "             AND W2.GRADE          = '".$model->field["GRADE"]."' ";
        $query .= "         GROUP BY ";
        $query .= "             W2.ATTEND_SUBCLASSCD ";
        $query .= "     ) T3 ON T3.ATTEND_SUBCLASSCD = T1.PROFICIENCY_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR           = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER       = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' AND ";
        $query .= "     T1.PROFICIENCYCD  = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= "     AND T1.SCHREGNO IN ( ";
        $query .= "         SELECT ";
        $query .= "             W1.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT W1 ";
        $query .= "         WHERE ";
        $query .= "                 W1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "             AND W1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "             AND W1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
    
        return $query;
    }
}
