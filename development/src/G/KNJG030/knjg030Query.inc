<?php

require_once('for_php7.php');


class knjg030Query extends Query {

    //年組取得（権限チェック）
    function getAuth($year, $semester, $model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT REG_H.GRADE || REG_H.HR_CLASS AS VALUE, REG_H.HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT REG_H ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
                    $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
                    $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
                $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE REG_H.YEAR = '" .$year ."'";
            $query .= "AND REG_H.SEMESTER = '".$semester ."'";
            $query .= " ORDER BY VALUE ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT REG_H.GRADE || REG_H.HR_CLASS AS VALUE, REG_H.HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT REG_H ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
                    $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
                    $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
                $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE REG_H.YEAR = '" .$year ."' ";
            $query .= "AND REG_H.SEMESTER = '".$semester ."' ";
            $query .= " AND (REG_H.TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR REG_H.TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR REG_H.TR_CD3 = '" .STAFFCD ."') ";
            $query .= " ORDER BY VALUE ";
        }

        return $query;
    }

    //学期取得
    function getSemester($year)
    {
        $query = "SELECT semester,semestername FROM semester_mst where year='" .$year. "' and not semester='9'";

        return $query;
    }

    //Z010
    function getZ010()
    {
        $query = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";

        return $query;
    }

    //職員取得
    function getStaffList() {
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD AS VALUE, ";
        $query .= "     T1.STAFFCD || '　' || STAFFNAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T1, ";
        $query .= "     STAFF_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD  = T2.STAFFCD  AND ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
