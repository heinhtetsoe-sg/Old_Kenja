<?php

require_once('for_php7.php');

class knjb0055query extends Query {
    //学期コンボ用
    function getSemester() {
        $query .= " SELECT ";
        $query .= "     YEAR || '年度  ' || SEMESTERNAME AS LABEL, ";
        $query .= "     YEAR || '-' || semester AS VALUE, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER ";      
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        //$query .= "     YEAR = '{$model->field["YEAR"]}' AND ";
        $query .= "     SEMESTER <> '9' ";

        return $query;
    }

    //科目ツリー用のSQL
    function getSubclassList($model) {
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query  = " SELECT DISTINCT ";
            $query .= "     L2.CLASSCD || '-' || L2.SCHOOL_KIND AS CLASSCD, ";
            $query .= "     L2.CLASSNAME, ";
            $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD AS SUBCLASSCD, ";
            $query .= "     L1.SUBCLASSNAME ";
            $query .= " FROM ";
            $query .= "     CHAIR_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     SUBCLASS_MST L1 ON L1.CLASSCD       = T1.CLASSCD ";
            $query .= "                    AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                    AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                    AND L1.SUBCLASSCD    = T1.SUBCLASSCD ";
            $query .= " LEFT JOIN ";
            $query .= "     CLASS_MST L2    ON L2.CLASSCD          = L1.CLASSCD ";
            $query .= "                    AND L2.SCHOOL_KIND      = L1.SCHOOL_KIND ";
            $query .= " WHERE ";
            $query .= "     YEAR || '-' || SEMESTER  = '{$model->field["SEMESTER"]}' AND ";
            $query .= "     INT(L2.CLASSCD) < 90 ";
            $query .= " ORDER BY ";
            $query .= "      L2.CLASSCD || '-' || L2.SCHOOL_KIND, ";
            $query .= "      L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD ";
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     L2.CLASSCD, ";
            $query .= "     L2.CLASSNAME, ";
            $query .= "     L1.SUBCLASSCD, ";
            $query .= "     L1.SUBCLASSNAME ";
            $query .= " FROM ";
            $query .= "     CHAIR_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= " LEFT JOIN ";
            $query .= "     CLASS_MST L2 ON L2.CLASSCD = SUBSTR(L1.SUBCLASSCD, 1, 2) ";
            $query .= " WHERE ";
            $query .= "     YEAR || '-' || SEMESTER  = '{$model->field["SEMESTER"]}' AND ";
            $query .= "     INT(L2.CLASSCD) < 90 ";
            $query .= " ORDER BY ";
            $query .= "     L2.CLASSCD, ";
            $query .= "     L1.SUBCLASSCD ";
        }

        return $query;
    }

    //右フレームのタイトル(教科名、科目名)取得
    function getSubclassName($model) {
        $query .= " SELECT ";
        $query .= "     T1.CLASSNAME, ";
        $query .= "     T2.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     CLASS_MST T1, ";
        $query .= "     SUBCLASS_MST T2 ";
        $query .= " WHERE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND = '{$model->field["CLASSCD"]}' AND ";
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD  = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     T1.CLASSCD = SUBSTR('{$model->field["SUBCLASSCD"]}', 1, 2) AND ";
            $query .= "     T2.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }

        return $query;
    }

    //右フレームのリストを表示するためのSQL
    function getChairList($model) {
        $query .= " SELECT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME, ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T5.APPDATE, ";
        $query .= "     T5.APPENDDATE, ";
        $query .= "     T6.STAFFNAME_SHOW, ";
        $query .= "     T6.STAFFCD, ";
        $query .= "     VALUE(T6.CHARGEDIV,0) AS CHARGEDIV, ";
        $query .= "     L2.HR_NAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     (SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         CHAIRCD, ";
        $query .= "         MAX(APPDATE) AS APPDATE, ";
        $query .= "         MAX(APPENDDATE) AS APPENDDATE ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         CHAIRCD ";
        $query .= "     ) T5 ON  T5.CHAIRCD = T1.CHAIRCD ";
        $query .= "          AND T5.YEAR = T1.YEAR ";
        $query .= "          AND T5.SEMESTER = T1.SEMESTER ";
        $query .= " LEFT JOIN ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         TT4.STAFFNAME_SHOW, ";
        $query .= "         TT3.YEAR, ";
        $query .= "         TT3.SEMESTER, ";
        $query .= "         TT3.STAFFCD, ";
        $query .= "         TT3.CHARGEDIV, ";
        $query .= "         TT3.CHAIRCD ";
        $query .= "      FROM ";
        $query .= "         CHAIR_STF_DAT TT3, ";
        $query .= "         STAFF_MST TT4 ";
        $query .= "     WHERE ";
        $query .= "         TT3.STAFFCD  = TT4.STAFFCD ";
        $query .= "     ) T6 ON  T6.YEAR = T1.YEAR ";
        $query .= "          AND T6.SEMESTER = T1.SEMESTER ";
        $query .= "          AND T6.CHAIRCD = T1.CHAIRCD ";
        $query .= " LEFT JOIN ";
        $query .= "     (SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         CHAIRCD, ";
        $query .= "         SUBSTR(MIN(TRGTGRADE || TRGTCLASS),1,2) AS TRGTGRADE, ";
        $query .= "         SUBSTR(MIN(TRGTGRADE || TRGTCLASS),3,3) AS TRGTCLASS ";
        $query .= "     FROM ";
        $query .= "         CHAIR_CLS_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         CHAIRCD ";
        $query .= "     ) L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "          AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "          AND L1.CHAIRCD = T1.CHAIRCD ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT L2 ON  L2.YEAR = T1.YEAR ";
        $query .= "                         AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "                         AND L2.GRADE = L1.TRGTGRADE ";
        $query .= "                         AND L2.HR_CLASS = L1.TRGTCLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || '-' ||T1.SEMESTER   = '{$model->field["SEMESTER"]}' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GROUPCD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     CHAIRCD, ";
        $query .= "     CHARGEDIV DESC, ";
        $query .= "     STAFFCD ASC ";

        return $query;
    }

    //講座を取得している生徒のカウント
    function countChairStdDat($model) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || '-' || SEMESTER   = '{$model->field["SEMESTER"]}' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {    
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }

        return $query;
    }

}
?>
