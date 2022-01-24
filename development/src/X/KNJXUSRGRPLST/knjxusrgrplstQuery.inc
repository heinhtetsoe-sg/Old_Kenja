<?php
class knjxusrgrplstQuery extends Query {
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //MENUデータ取得
    function selectQuery($model)
    {
        $order = array("desc", "asc");

        $query  = "SELECT distinct";
        $query .= " T1.STAFFCD,";
        $query .= " T1.STAFFNAME_SHOW,";
        $query .= " T1.STAFFNAME_KANA,";
        $query .= " T2.GROUPCD,";
        $query .= " T2.GROUPNAME";
        $query .= " FROM";
        $query .= " STAFF_MST T1,USERGROUP_MST T2,USERGROUP_DAT T3,V_STAFF_MST T4 ";
        $query .= " WHERE";
        $query .= " T1.STAFFCD=T3.STAFFCD AND";
        $query .= " T1.STAFFCD=T4.STAFFCD AND";
        $query .= " T4.YEAR ='".CTRL_YEAR."' AND";
        $query .= " T3.YEAR =T4.YEAR AND";
        $query .= " T2.GROUPCD=T3.GROUPCD";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOLCD     = T3.SCHOOLCD";
            $query .= " AND T2.SCHOOL_KIND  = T3.SCHOOL_KIND";
            $query .= " AND T2.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }

        switch ($model->s_id)
        {
        case "1";
        $query .= " ORDER BY  T1.STAFFCD ".$order[$model->sort[$model->s_id]].", T2.GROUPCD asc"; break;
        case "2":
        $query .= " ORDER BY  T1.STAFFNAME_KANA ".$order[$model->sort[$model->s_id]].", T2.GROUPCD asc"; break;
        case "3":
        $query .= " ORDER BY  T2.GROUPCD ".$order[$model->sort[$model->s_id]].", T1.STAFFCD asc"; break;
        case "4":
        $query .= " ORDER BY  T2.GROUPCD ".$order[$model->sort[$model->s_id]].", T1.STAFFNAME_KANA asc"; break;
        default:
        $query .= " ORDER BY  T1.STAFFCD ".$order[$model->sort[$model->s_id]].", T2.GROUPCD asc"; break;
        }
        
       return $query;
    }
}
?>
