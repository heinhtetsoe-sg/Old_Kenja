<?php

require_once('for_php7.php');

class knjc043sQuery extends Query {

    //年組取得（権限チェック）
    function getAuth($model, $year,$semester)
    {
        if($model->field["HR_CLASS_TYPE"] == "2") {
            $query  = " SELECT DISTINCT ";
            $query .= "     GHR_CD AS VALUE, ";
            $query .= "     GHR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GHR_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR         = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (TR_CD1 = '".STAFFCD."' OR ";
                $query .= "          TR_CD2 = '".STAFFCD."' OR ";
                $query .= "          TR_CD3 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD1 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD2 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD3 = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T1.YEAR='" .$year ."'";
            $query .= "AND SEMESTER='".$semester ."'";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
                $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
                $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
                $query .= " OR TR_CD3 = '" .STAFFCD ."' ";
                $query .= " OR SUBTR_CD1 = '" .STAFFCD ."' ";
                $query .= " OR SUBTR_CD2 = '" .STAFFCD ."' ";
                $query .= " OR SUBTR_CD3 = '" .STAFFCD ."') ";
            }
        }
        return $query;
    }

    function getNameMst() {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    NAMECD1, ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1 "; // 学校区分
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1='Z010' AND ";
        $query .= "    NAMECD2='00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

}
?>
