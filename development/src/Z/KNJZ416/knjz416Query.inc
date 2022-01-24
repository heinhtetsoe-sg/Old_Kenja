<?php

require_once('for_php7.php');

class knjz416Query extends Query {

    //中分類グループリスト
    function getMGroup($m_groupcd = "") {

        $query  = " SELECT ";
        $query .= "     T1.M_GROUPCD, ";
        $query .= "     T1.M_GROUPNAME, ";
        $query .= "     T2.INDUSTRY_MCD, ";
        $query .= "     T2.INDUSTRY_MNAME, ";
        $query .= "     T2.INDUSTRY_LCD, ";
        $query .= "     T3.INDUSTRY_LNAME ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_MGROUP_MST T1 ";
        $query .= " LEFT JOIN INDUSTRY_M_MST T2 ON T2.M_GROUPCD = T1.M_GROUPCD ";
        $query .= " LEFT JOIN INDUSTRY_L_MST T3 ON T3.INDUSTRY_LCD = T2.INDUSTRY_LCD ";
        if($m_groupcd){
            $query .= " WHERE ";
            $query .= "     T1.M_GROUPCD = '".$m_groupcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.M_GROUPCD, ";
        $query .= "     T2.INDUSTRY_LCD, ";
        $query .= "     T2.INDUSTRY_MCD ";

        return $query;
    }

    //中分類グループリスト
    function getMgroupMst($m_groupcd) {

        $query  = " SELECT ";
        $query .= "     M_GROUPCD, ";
        $query .= "     M_GROUPNAME ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_MGROUP_MST ";
        $query .= " WHERE ";
        $query .= "     M_GROUPCD = '".$m_groupcd."' ";
        $query .= " ORDER BY ";
        $query .= "     M_GROUPCD ";

        return $query;
    }

    //大分類コンボ
    function IndustryLcd() {

        $query  = " SELECT ";
        $query .= "     INDUSTRY_LCD AS VALUE, ";
        $query .= "     INDUSTRY_LCD || ':' || INDUSTRY_LNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_L_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //中分類一覧取得
    function IndustryMcdList($l_groupcd, $m_groupcd) {
        $query  = " SELECT ";
        $query .= "     INDUSTRY_MCD AS VALUE, ";
        $query .= "     INDUSTRY_MCD || ' ' || INDUSTRY_MNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_M_MST ";
        $query .= " WHERE ";
        $query .= "     INDUSTRY_LCD = '".$l_groupcd."' ";
        if($m_groupcd){
            $query .= "     AND INDUSTRY_MCD NOT IN(SELECT ";
            $query .= "                             INDUSTRY_MCD ";
            $query .= "                         FROM ";
            $query .= "                             INDUSTRY_M_MST ";
            $query .= "                         WHERE ";
            $query .= "                             M_GROUPCD = '".$m_groupcd."') ";
        }
        return $query;
    }

    //中分類一覧取得
    function selectIndustryMcd($m_groupcd, $industry_lcd) {
        $query  = " SELECT ";
        $query .= "     INDUSTRY_MCD AS VALUE, ";
        $query .= "     INDUSTRY_MCD || ' ' || INDUSTRY_MNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_M_MST T1 ";
        $query .= " WHERE ";
        $query .= "     M_GROUPCD = '".$m_groupcd."' AND ";
        $query .= "     INDUSTRY_LCD = '".$industry_lcd."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $data1 = $data2 = $data3 = array();

        $query  = " DELETE FROM ";
        $query .= "     INDUSTRY_MGROUP_MST ";
        $query .= " WHERE ";
        $query .= "     M_GROUPCD = '".$model->field["M_GROUPCD"]."' ";

        $db->query($query);

        $data1["M_GROUPCD"][TEXT]        = $model->field["M_GROUPCD"];
        $data1["M_GROUPNAME"][TEXT]      = $model->field["M_GROUPNAME"];
        $data1["REGISTERCD"][TEXT]       = STAFFCD;
        $data1["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data1, "INDUSTRY_MGROUP_MST");
        $db->query($query);

        $data2["M_GROUPCD"][TEXT]    = "";
        $data2["REGISTERCD"][TEXT]   = STAFFCD;
        $data2["UPDATED"][FUNC]      = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     M_GROUPCD    = '".$model->field["M_GROUPCD"]."' AND ";
        $where .= "     INDUSTRY_LCD = '".$model->field["INDUSTRY_LCD"]."' ";

        $db->query(Query::updateSQL($data2, "INDUSTRY_M_MST", $where));

        $selectdata = explode(",", $model->selectdata);
        if($model->selectdata){
            for ($i = 0; $i < get_count($selectdata); $i++) {

                $data3["M_GROUPCD"][TEXT]    = $model->field["M_GROUPCD"];
                $data3["REGISTERCD"][TEXT]   = STAFFCD;
                $data3["UPDATED"][FUNC]      = "SYSDATE()";

                $where  = " WHERE ";
                $where .= "     INDUSTRY_MCD = '".$selectdata[$i]."' AND ";
                $where .= "     INDUSTRY_LCD = '".$model->field["INDUSTRY_LCD"]."' ";

                $db->query(Query::updateSQL($data3, "INDUSTRY_M_MST", $where));
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

        $data["M_GROUPCD"][TEXT]    = "";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     M_GROUPCD = '".$model->field["M_GROUPCD"]."' ";

        $db->query(Query::updateSQL($data, "INDUSTRY_M_MST", $where));

        $query  = " DELETE FROM INDUSTRY_MGROUP_MST ";
        $query .= " WHERE ";
        $query .= "     M_GROUPCD = '".$model->field["M_GROUPCD"]."' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
