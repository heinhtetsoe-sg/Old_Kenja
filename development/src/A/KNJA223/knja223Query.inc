<?php

require_once('for_php7.php');


class knja223Query extends Query {

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA223' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if ($model->auth == DEF_REFERABLE || $model->auth == DEF_UPDATABLE) {
            $query  = " SELECT ";
            $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE, ";
            $query .= "     REGD_H.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGD_H ";
            $query .= " WHERE ";
            $query .= "     REGD_H.YEAR='" .CTRL_YEAR ."'";
            $query .= "     AND REGD_H.SEMESTER='".CTRL_SEMESTER ."'";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND REGD_H.GRADE IN (SELECT ";
                    $query .= "                             REGD_G.GRADE ";
                    $query .= "                          FROM ";
                    $query .= "                             SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                          WHERE ";
                    $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                             AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND REGD_H.GRADE IN (SELECT ";
                $query .= "                             REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                             SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                             AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT ";
            $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE, ";
            $query .= "     REGD_H.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGD_H ";
            $query .= " WHERE ";
            $query .= "     REGD_H.YEAR='" .CTRL_YEAR ."' ";
            $query .= "     AND REGD_H.SEMESTER='".CTRL_SEMESTER ."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND REGD_H.GRADE IN (SELECT ";
                    $query .= "                             REGD_G.GRADE ";
                    $query .= "                          FROM ";
                    $query .= "                             SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                          WHERE ";
                    $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                             AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND REGD_H.GRADE IN (SELECT ";
                $query .= "                             REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                             SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                             AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
            $query .= "     AND (REGD_H.TR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR REGD_H.TR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR REGD_H.TR_CD3 = '" .STAFFCD ."' ";
            $query .= "          OR REGD_H.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR REGD_H.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR REGD_H.SUBTR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //CSV出力
    function selectCsvQuery($model)
    {
        $selectdata = array();
        $selectdata = explode(",", $model->selectdata);

        $query  = "SELECT ";
        $query .= "    w3.grade || w3.hr_class as grade_hr_class,";
        $query .= "    smallint(w3.attendno) as attendno,";
        $query .= "    CASE WHEN w4.SEX = '2' THEN '*' ELSE '' END AS SEX, ";
        $query .= "    w4.name,";
        $query .= "    w4.name_kana,";
        $query .= "    w1.hr_nameabbv,";
        $query .= "    w2.staffname ";
        $query .= "FROM ";
        $query .= "    schreg_base_mst w4,";
        $query .= "    schreg_regd_dat w3,";
        $query .= "    schreg_regd_hdat w1 ";
        $query .= "    left join staff_mst w2 on w1.tr_cd1 = w2.staffcd ";
        $query .= "WHERE ";
        $query .= "    w1.year = '" .CTRL_YEAR ."' AND ";
        $query .= "    w1.semester = '".CTRL_SEMESTER ."' AND ";
        $query .= "    w1.grade || w1.hr_class in ('" .implode($selectdata, "','") ."') AND ";
        $query .= "    w1.year = w3.year AND ";
        $query .= "    w1.semester = w3.semester AND ";
        $query .= "    w1.grade = w3.grade AND ";
        $query .= "    w1.hr_class = w3.hr_class AND ";
        $query .= "    w3.schregno = w4.schregno ";
        $query .= "order by w3.grade, w3.hr_class, w3.attendno";

        return $query;
    }

}
?>
