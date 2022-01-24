<?php

require_once('for_php7.php');

class knje991tQuery extends Query {

    //年組取得（権限チェック）
    function getGrdYear($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT DISTINCT ";
            $query .= "     REG_H.YEAR AS VALUE, ";
            $query .= "     REG_H.YEAR AS LABEL ";
            $query .= " FROM ";
            $query .= "     GRD_REGD_HDAT REG_H ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
                    $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
                    $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
                $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
                $query .= " AND ";
                $query .= "     (REG_H.TR_CD1     = '".STAFFCD."' OR ";
                $query .= "      REG_H.TR_CD2     = '".STAFFCD."' OR ";
                $query .= "      REG_H.TR_CD3     = '".STAFFCD."' OR "; 
                $query .= "      REG_H.SUBTR_CD1  = '".STAFFCD."' OR "; 
                $query .= "      REG_H.SUBTR_CD2  = '".STAFFCD."' OR "; 
                $query .= "      REG_H.SUBTR_CD3  = '".STAFFCD."') ";
            }
            $query .= " ORDER BY";
            $query .= "   REG_H.YEAR DESC ";
        }

        return $query;
    }

    //校種を取得　getSchoolDiv()で使用
    function getSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$grade."' ";

        return $query;
    }

    //卒業可能学年チェック
    function checkGrdGrade($grade) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' AND ";
        $query .= "     NAME1   = 'H' AND ";
        $query .= "     '".$grade."' BETWEEN NAMESPARE2 AND NAMESPARE3 ";

        return $query;
    }

    //生徒一覧
    //SCHOOLDIV を取得する(0:学年制、1:単位制)
    function getSchoolDiv($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOLDIV, ";
        $query .= "     VALUE(T2.NAMESPARE2,'0') AS NEN, ";
        $query .= "     VALUE(T3.NAMESPARE3,'0') AS IS_TUSIN ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR AND T2.NAMECD1 = 'Z001' AND ";
        $query .= "             T2.NAMECD2 = T1.SCHOOLDIV AND T2.NAMESPARE2 IN ('3','4','6') ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ON T3.YEAR = T1.YEAR AND T3.NAMECD1 = 'Z001' AND ";
        $query .= "             T3.NAMECD2 = T1.SCHOOLDIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '". CTRL_YEAR ."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        }

        return $query;
    }

}
?>
