<?php

require_once('for_php7.php');

class knjd626iQuery extends Query
{

    //年組取得（権限チェック）
    public function getGradeHrClass($model)
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "            ON T2.YEAR  = T1.YEAR";
        $query .= "           AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " AND T2.SCHOOL_KIND = 'H' "; //高校固定
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

    //学期取得
    public function getSemester()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科取得
    public function getClass($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     CHAIR.CLASSCD || CHAIR.SCHOOL_KIND AS VALUE, ";
        $query .= "     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || ' ' || MST.CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT STD ";
        $query .= " INNER JOIN ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= "      ON CHAIR.YEAR = STD.YEAR ";
        $query .= "     AND CHAIR.SEMESTER = STD.SEMESTER ";
        $query .= "     AND CHAIR.CHAIRCD = STD.CHAIRCD ";
        $query .= " INNER JOIN ";
        $query .= "     V_CLASS_MST MST ";
        $query .= "      ON MST.YEAR = CHAIR.YEAR ";
        $query .= "     AND MST.CLASSCD = CHAIR.CLASSCD ";
        $query .= "     AND MST.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     STD.SCHREGNO IN (SELECT  ";
        $query .= "                          SCHREGNO  ";
        $query .= "                      FROM ";
        $query .= "                          SCHREG_REGD_DAT  ";
        $query .= "                      WHERE ";
        $query .= "                          YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                          SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "                          GRADE = '".$model->field["GRADE"]."') ";
        $query .= "     AND STD.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }
    
    //科目取得
    public function getSubclass($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     MST.CLASSCD || MST.SCHOOL_KIND || MST.CURRICULUM_CD || MST.SUBCLASSCD AS VALUE, ";
        $query .= "     MST.CLASSCD || '-' || MST.SCHOOL_KIND || '-' || MST.CURRICULUM_CD || '-' || MST.SUBCLASSCD || ':' || MST.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT STD ";
        $query .= " INNER JOIN ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= "      ON CHAIR.YEAR = STD.YEAR ";
        $query .= "     AND CHAIR.SEMESTER = STD.SEMESTER ";
        $query .= "     AND CHAIR.CHAIRCD = STD.CHAIRCD ";
        if ($model->field["CLASSCD"]) {
            $query .= "     AND CHAIR.CLASSCD || CHAIR.SCHOOL_KIND = '".$model->field["CLASSCD"]."' ";
        }
        $query .= " INNER JOIN ";
        $query .= "     V_SUBCLASS_MST MST ";
        $query .= "      ON MST.YEAR = CHAIR.YEAR ";
        $query .= "     AND MST.CLASSCD = CHAIR.CLASSCD ";
        $query .= "     AND MST.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
        $query .= "     AND MST.CURRICULUM_CD = CHAIR.CURRICULUM_CD ";
        $query .= "     AND MST.SUBCLASSCD = CHAIR.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     STD.SCHREGNO IN (SELECT  ";
        $query .= "                          SCHREGNO  ";
        $query .= "                      FROM ";
        $query .= "                          SCHREG_REGD_DAT  ";
        $query .= "                      WHERE ";
        $query .= "                          YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                          SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "                          GRADE = '".$model->field["GRADE"]."') ";
        $query .= "     AND STD.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE";
        
        return $query;
    }
}
