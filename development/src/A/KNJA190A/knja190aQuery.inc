<?php

require_once('for_php7.php');

class knja190aQuery extends Query {

    //年組取得（権限チェック）
    function getAuth($model,$year,$semester)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            //複式クラス
            if ($model->field["HUKUSIKI_KIRIKAE"] == "2" && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query  = "SELECT GHR_CD AS VALUE,GHR_NAME AS LABEL ";
                $query .= "FROM SCHREG_REGD_GHR_HDAT ";
            //FI複式クラス
            } else if ($model->field["HUKUSIKI_KIRIKAE"] == "2" && $model->Properties["useFi_Hrclass"] == '1') {
                $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
                $query .= "FROM SCHREG_REGD_FI_HDAT ";
            //通常
            } else {
                $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
                $query .= "FROM SCHREG_REGD_HDAT ";
            }
            $query .= "WHERE YEAR='" .$year ."'";
            $query .= "AND SEMESTER='".$semester ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            //複式クラス
            if ($model->field["HUKUSIKI_KIRIKAE"] == "2" && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query  = "SELECT GHR_CD AS VALUE,GHR_NAME AS LABEL ";
                $query .= "FROM SCHREG_REGD_GHR_HDAT ";
            //FI複式クラス
            } else if ($model->field["HUKUSIKI_KIRIKAE"] == "2" && $model->Properties["useFi_Hrclass"] == '1') {
                $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
                $query .= "FROM SCHREG_REGD_FI_HDAT ";
            //通常
            } else {
                $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
                $query .= "FROM SCHREG_REGD_HDAT ";
            }
            $query .= "WHERE YEAR='" .$year ."' ";
            $query .= "AND SEMESTER='".$semester ."' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."') ";
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

}
?>
