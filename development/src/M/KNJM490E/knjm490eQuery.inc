<?php
class knjm490eQuery extends Query {
    //科目取得
    function GetSubclass($model) {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     SUB_M.CLASSCD || '-' || SUB_M.SCHOOL_KIND || '-' || SUB_M.CURRICULUM_CD || '-' || SUB_M.SUBCLASSCD as VALUE, ";
        } else {
            $query .= "     SUB_M.SUBCLASSCD as VALUE, ";
        }
        $query .= "     SUB_M.SUBCLASSNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST SUB_M ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIR ON CHAIR.SUBCLASSCD    = SUB_M.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                           AND CHAIR.CLASSCD       = SUB_M.CLASSCD ";
            $query .= "                           AND CHAIR.SCHOOL_KIND   = SUB_M.SCHOOL_KIND ";
            $query .= "                           AND CHAIR.CURRICULUM_CD = SUB_M.CURRICULUM_CD ";
        }
        $query .= "                               AND CHAIR.YEAR          = '".CTRL_YEAR."' ";
        $query .= "                               AND CHAIR.SEMESTER      = '".CTRL_SEMESTER."' ";
        $query .= " WHERE ";
        $query .= "         SUB_M.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SUB_M.SUBCLASSCD NOT LIKE '92%' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラスデータ取得
    function getClass($model) {
        $query  = " SELECT ";
        $query .= "     HR_NAME, ";
        $query .= "     GRADE || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        if ($model->chacd != 0){
            $query .= "     CHAIR_CLS_DAT t1 ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT t2 ON TRGTGRADE || TRGTCLASS = GRADE || HR_CLASS ";
            $query .= "     AND t2.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND t2.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= " WHERE ";
            $query .= "     t1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND t1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND t1.CHAIRCD = '".$model->chacd."' ";
        }else {
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE,HR_CLASS ";

        return $query;
    }

}
?>
