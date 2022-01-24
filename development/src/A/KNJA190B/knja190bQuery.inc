<?php

require_once('for_php7.php');

class knja190bQuery extends Query {

    //年組取得（権限チェック） 
    function getAuth($model, $year, $semester) {
        //実クラス
        if ($model->field["HUKUSIKI_KIRIKAE"] == "2" && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $query  = " SELECT GHR_CD AS VALUE, GHR_NAME AS LABEL ";
            $query .= " FROM SCHREG_REGD_GHR_HDAT T1 ";
        //FI複式クラス
        } else if ($model->field["HUKUSIKI_KIRIKAE"] == "2" && $model->Properties["useFi_Hrclass"] == '1') {
            $query  = " SELECT T1.GRADE || T1.HR_CLASS AS VALUE, T1.HR_NAME AS LABEL ";
            $query .= " FROM SCHREG_REGD_FI_HDAT T1 ";
        //通常
        } else {
            $query  = " SELECT T1.GRADE || T1.HR_CLASS AS VALUE, T1.HR_NAME AS LABEL ";
            $query .= " FROM SCHREG_REGD_HDAT T1 ";
        }
        //実クラス以外
        if (!($model->field["HUKUSIKI_KIRIKAE"] == "2" && $model->Properties["useSpecial_Support_Hrclass"] == '1')) {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
                    $query .= "              ON T1.YEAR         = T2.YEAR ";
                    $query .= "             AND T1.GRADE        = T2.GRADE ";
                    $query .= "             AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
                $query .= "              ON T1.YEAR         = T2.YEAR ";
                $query .= "             AND T1.GRADE        = T2.GRADE ";
                $query .= "             AND T2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            }
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$year."' ";
        $query .= " AND T1.SEMESTER = '".$semester."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "      OR TR_CD2 = '".STAFFCD."' ";
            $query .= "      OR TR_CD3 = '".STAFFCD."') ";
        }

        return $query;
    }


    //生徒取得
    function getSchno($model,$year,$semester) {
        //実クラス
        if ($model->field["HUKUSIKI_KIRIKAE"] == "2" && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $tableRegdDat  = "SCHREG_REGD_GHR_DAT";
            $tableRegdHDat = "SCHREG_REGD_GHR_HDAT";
        //FI複式クラス
        } else if ($model->field["HUKUSIKI_KIRIKAE"] == "2" && $model->Properties["useFi_Hrclass"] == '1') {
            $tableRegdDat  = "SCHREG_REGD_FI_DAT";
            $tableRegdHDat = "SCHREG_REGD_FI_HDAT";
        //通常
        } else {
            $tableRegdDat  = "SCHREG_REGD_DAT";
            $tableRegdHDat = "SCHREG_REGD_HDAT";
        }

        $query  = "SELECT T1.SCHREGNO,T2.NAME_SHOW, ";
        //実クラス
        if ($model->field["HUKUSIKI_KIRIKAE"] == "2" && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $query .= " T1.GHR_CD AS GRADE_HR_CLASS, ";
            $query .= " T1.GHR_ATTENDNO AS ATTENDNO, ";
            $query .= " T3.GHR_NAME AS HR_NAME ";
        //FI複式クラス
        //通常
        } else {
            $query .= " T1.GRADE||T1.HR_CLASS AS GRADE_HR_CLASS, ";
            $query .= " T1.ATTENDNO AS ATTENDNO, ";
            $query .= " T3.HR_NAME AS HR_NAME ";
        }
        $query .= "FROM   SCHREG_BASE_MST T2,{$tableRegdDat} T1 ";
        $query .= "       LEFT JOIN {$tableRegdHDat} T3 ON T3.YEAR=T1.YEAR AND T3.SEMESTER=T1.SEMESTER ";
        //実クラス
        if ($model->field["HUKUSIKI_KIRIKAE"] == "2" && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $query .= "                                 AND T3.GHR_CD=T1.GHR_CD ";
        //FI複式クラス
        //通常
        } else {
            $query .= "                                 AND T3.GRADE=T1.GRADE AND T3.HR_CLASS=T1.HR_CLASS ";
        }
        $query .= "WHERE  T1.YEAR       = '".$year."' AND ";
        $query .= "       T1.SEMESTER   = '".$semester."' AND ";
        //実クラス
        if ($model->field["HUKUSIKI_KIRIKAE"] == "2" && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $query .= "       T1.GHR_CD = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        } else {
            $query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        }
        $query .= "       T2.SCHREGNO   = T1.SCHREGNO ";
        $query .= "ORDER BY ATTENDNO ";

        return $query;
    }

    //テーブル存在チェック
    function checkTableExist() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    function getSchName($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND IN (SELECT ";
                $query .= "                         SCHOOL_KIND ";
                $query .= "                     FROM ";
                $query .= "                         SCHREG_REGD_GDAT ";
                $query .= "                     WHERE ";
                $query .= "                         YEAR = '".CTRL_YEAR."' AND ";
                $query .= "                         GRADE = SUBSTR('".$model->field["GRADE_HR_CLASS"]."',1,2) ";
                $query .= "                     ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR = '".CTRL_YEAR."' AND ";
            $query .= "                         GRADE = SUBSTR('".$model->field["GRADE_HR_CLASS"]."',1,2) ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }
}
?>
