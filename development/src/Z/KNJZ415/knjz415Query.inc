<?php

require_once('for_php7.php');

class knjz415Query extends Query {

    //大分類グループリスト
    function GetLGroup($l_groupcd = "") {

        $query  = " SELECT ";
        $query .= "     T1.L_GROUPCD, ";
        $query .= "     T1.L_GROUPNAME, ";
        $query .= "     T2.INDUSTRY_LCD, ";
        $query .= "     T2.INDUSTRY_LNAME, ";
        $query .= "     T2.INDUSTRY_LCD, ";
        $query .= "     T2.INDUSTRY_LNAME ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_LGROUP_MST T1 ";
        $query .= " LEFT JOIN INDUSTRY_L_MST T2 ON T2.L_GROUPCD = T1.L_GROUPCD ";
        if($l_groupcd){
            $query .= " WHERE ";
            $query .= "     T1.L_GROUPCD = '".$l_groupcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.L_GROUPCD, ";
        $query .= "     T2.INDUSTRY_LCD ";

        return $query;
    }

    //大分類グループリスト
    function getLgroupMst($l_groupcd) {

        $query  = " SELECT ";
        $query .= "     L_GROUPCD, ";
        $query .= "     L_GROUPNAME ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_LGROUP_MST ";
        $query .= " WHERE ";
        $query .= "     L_GROUPCD = '".$l_groupcd."' ";
        $query .= " ORDER BY ";
        $query .= "     L_GROUPCD ";

        return $query;
    }

    //大分類一覧取得
    function IndustryLcdList($l_groupcd) {
        $query  = " SELECT ";
        $query .= "     INDUSTRY_LCD AS VALUE, ";
        $query .= "     INDUSTRY_LCD || ' ' || INDUSTRY_LNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_L_MST ";
        if($l_groupcd){
            $query .= " WHERE ";
            $query .= "     INDUSTRY_LCD NOT IN(SELECT ";
            $query .= "                             INDUSTRY_LCD ";
            $query .= "                         FROM ";
            $query .= "                             INDUSTRY_L_MST ";
            $query .= "                         WHERE ";
            $query .= "                             L_GROUPCD = '".$l_groupcd."') ";
        }
        return $query;
    }

    //大分類一覧取得
    function selectIndustryLcd($l_groupcd) {
        $query  = " SELECT ";
        $query .= "     INDUSTRY_LCD AS VALUE, ";
        $query .= "     INDUSTRY_LCD || ' ' || INDUSTRY_LNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_L_MST T1 ";
        $query .= " WHERE ";
        $query .= "     L_GROUPCD = '".$l_groupcd."' ";

        return $query;
    }

    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $data1 = $data2 = $data3 = array();

        $query  = " DELETE FROM ";
        $query .= "     INDUSTRY_LGROUP_MST ";
        $query .= " WHERE ";
        $query .= "     L_GROUPCD = '".$model->field["L_GROUPCD"]."' ";

        $db->query($query);

        $data1["L_GROUPCD"][TEXT]        = $model->field["L_GROUPCD"];
        $data1["L_GROUPNAME"][TEXT]      = $model->field["L_GROUPNAME"];
        $data1["REGISTERCD"][TEXT]       = STAFFCD;
        $data1["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data1, "INDUSTRY_LGROUP_MST");
        $db->query($query);

        $data2["L_GROUPCD"][TEXT]    = "";
        $data2["REGISTERCD"][TEXT]   = STAFFCD;
        $data2["UPDATED"][FUNC]      = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     L_GROUPCD = '".$model->field["L_GROUPCD"]."' ";

        $db->query(Query::updateSQL($data2, "INDUSTRY_L_MST", $where));

        $selectdata = explode(",", $model->selectdata);
        if($model->selectdata){
            for ($i = 0; $i < get_count($selectdata); $i++) {

                $data3["L_GROUPCD"][TEXT]    = $model->field["L_GROUPCD"];
                $data3["REGISTERCD"][TEXT]   = STAFFCD;
                $data3["UPDATED"][FUNC]      = "SYSDATE()";

                $where  = " WHERE ";
                $where .= "     INDUSTRY_LCD = '".$selectdata[$i]."' ";

                $db->query(Query::updateSQL($data3, "INDUSTRY_L_MST", $where));
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["L_GROUPCD"][TEXT]    = "";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     L_GROUPCD = '".$model->field["L_GROUPCD"]."' ";

        $db->query(Query::updateSQL($data, "INDUSTRY_L_MST", $where));

        $query  = " DELETE FROM INDUSTRY_LGROUP_MST ";
        $query .= " WHERE ";
        $query .= "     L_GROUPCD = '".$model->field["L_GROUPCD"]."' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
