<?php

require_once('for_php7.php');

class knjf101aQuery extends Query
{
    //校種取得
    public function getSchkind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }
 
    //名称マスタ
    public function getNameMst($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        if ($model->Properties["useNurseoffRestrict"] == "1" && (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT)) {
            $query .= "     INNER JOIN STAFF_DETAIL_MST S1 ";
            $query .= "          ON T1.YEAR         = S1.YEAR ";
            $query .= "         AND S1.STAFFCD      = '".STAFFCD."' ";
            $query .= "         AND S1.STAFF_SEQ    = '008' ";
            $query .= "         AND T1.NAMECD2      = S1.FIELD1 ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND T1.NAMECD1 = 'Z043' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.NAMESPARE2 = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.NAMESPARE2 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校マスタ学校名
    public function getSchoolMst($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR='{$year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        return $query;
    }

    //学期取得
    public function getSelectSeme()
    {
        $query  = " SELECT DISTINCT SEMESTER,SEMESTERNAME FROM SEMESTER_MST ";
        $query .= " WHERE YEAR = '" .CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY SEMESTER";

        return $query;
    }
}
