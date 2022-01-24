<?php

require_once('for_php7.php');

class knjs330Query extends Query {

    //年組取得
    function getGradeHrClass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE, ";
        $query .= "     REGD_H.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGD_H ";
        $query .= " WHERE ";
        $query .= "     REGD_H.YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     REGD_H.SEMESTER    = '".CTRL_SEMESTER."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND REGD_H.GRADE IN (SELECT ";
            $query .= "                             REGD_G.GRADE ";
            $query .= "                          FROM ";
            $query .= "                             SCHREG_REGD_GDAT REGD_G ";
            $query .= "                          WHERE ";
            $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                             AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (REGD_H.TR_CD1    = '".STAFFCD."' OR ";
            $query .= "          REGD_H.TR_CD2    = '".STAFFCD."' OR ";
            $query .= "          REGD_H.TR_CD3    = '".STAFFCD."' OR ";
            $query .= "          REGD_H.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "          REGD_H.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "          REGD_H.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象月データ取得
    function getMonthAll($model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     CASE WHEN smallint(NAMECD2) < 4 ";
        $query .= "          THEN smallint(NAMECD2) + 12 ";
        $query .= "          ELSE smallint(NAMECD2) END AS ORDER ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '{$setNameCd}' ";
        $query .= " ORDER BY ";
        $query .= "     ORDER ";

        return $query;
    }

}
?>
