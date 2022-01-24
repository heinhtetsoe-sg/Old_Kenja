<?php

require_once('for_php7.php');

class knjd134bQuery extends Query
{
    public function getNameMstche($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS COUNT";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '$model->che_school_kind' ";

        return $query;
    }

    //科目一覧取得
    public function getSubclassList($model)
    {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || '　' || T3.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSCD || ' ' || T3.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     SUBCLASS_MST T3, ";
        $query .= "     CHAIR_STF_DAT T4 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = T2.YEAR AND ";
        $query .= "     T1.YEAR       = T4.YEAR AND ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER   = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER   = T4.SEMESTER AND ";
        $query .= "     T1.SEMESTER   = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.CHAIRCD    = T2.CHAIRCD AND ";
        $query .= "     T1.CHAIRCD    = T4.CHAIRCD AND ";
        if (0 < $model->count) {
            $query .= "    (SUBSTR(T3.SUBCLASSCD,1,2) IN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1 = '$model->che_school_kind') OR ";
        } else {
            $query .= "    (SUBSTR(T3.SUBCLASSCD,1,2) IN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1 = 'D008') OR ";
        }
        $query .= "     T3.SUBCLASSCD LIKE '90%') AND ";
        //制限付
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     T4.STAFFCD = '".STAFFCD."' AND ";
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD      = T1.CLASSCD AND ";
            $query .= "     T3.SCHOOL_KIND  = T1.SCHOOL_KIND AND ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') AND ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " T3.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            }
            $query .= "     T3.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "     T3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座一覧取得
    public function getChairList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRCD || '　' || T1.CHAIRNAME AS LABEL";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = T2.YEAR AND ";
        $query .= "     T1.YEAR       = T3.YEAR AND ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER   = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER   = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER   = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.CHAIRCD    = T2.CHAIRCD AND ";
        $query .= "     T1.CHAIRCD    = T3.CHAIRCD AND ";
        //制限付
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "       T3.STAFFCD = '".STAFFCD."' AND ";
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
