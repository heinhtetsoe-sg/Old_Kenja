<?php

require_once('for_php7.php');

class knjz350kQuery extends Query {

    //(左)入力可テキストボックス表示データ取得
    function selectLeftQuery($year)
    {
        $query  = "SELECT ";
        $query .= "    dat.control_flg AS control_flg, ";
        $query .= "    dat.control_code AS control_code, ";
        $query .= "    name.name1 AS name1, ";
        $query .= "    name.namecd2 AS namecd2, ";
        $query .= "    name.namespare1 ";
        $query .= "FROM ";
#        $query .= "    admin_control_dat dat LEFT OUTER JOIN name_mst name ON namecd1='Z004' AND control_code=namecd2 ";   2004/09/03 arakaki 近大-作業依頼書20040903-03.doc
        $query .= "    admin_control_dat dat LEFT OUTER JOIN V_NAME_MST name ON dat.YEAR=name.YEAR AND name.namecd1='Z004' AND dat.control_code=name.namecd2 ";
        $query .= "WHERE ";
        $query .= "    dat.year = '$year'  AND ";
        $query .= "    dat.control_flg='1' ";
        $query .= "UNION ";
        $query .= "SELECT ";
        $query .= "    dat.control_flg AS control_flg, ";
        $query .= "    dat.control_code AS control_code, ";
        $query .= "    name.name1 AS name1, ";
        $query .= "    name.namecd2 AS namecd2, ";
        $query .= "    name.namespare1 ";
        $query .= "FROM ";
#        $query .= "    admin_control_dat dat LEFT OUTER JOIN name_mst name ON namecd1='Z005' AND control_code=namecd2 ";   2004/09/03 arakaki 近大-作業依頼書20040903-03.doc
        $query .= "    admin_control_dat dat LEFT OUTER JOIN V_NAME_MST name ON dat.YEAR=name.YEAR AND name.namecd1='Z005' AND dat.control_code=name.namecd2 ";
        $query .= "WHERE ";
        $query .= "    dat.year = '$year'  AND ";
        $query .= "    dat.control_flg='2' ";
        $query .= "ORDER BY ";
        $query .= "    control_flg, namespare1, control_code ";

        //echo $query;
        return $query;
    }

    //(右)入力不可テキストボックス表示データ取得
    function selectRightQuery($year)
    {
        $query  = "SELECT DISTINCT name.namecd1, name.namecd2, name.name1 ";
#        $query .= "  FROM name_mst name ";     2004/09/03 arakaki 近大-作業依頼書20040903-03.doc
        $query .= "  FROM V_NAME_MST name ";
        $query .= " WHERE YEAR = '$year' ";
        $query .= "   AND namecd1='Z004' ";
        $query .= "   AND NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM ";
        $query .= "                         admin_control_dat dat ";
        $query .= "                    WHERE ";
        $query .= "                         dat.year = '$year'  AND ";
        $query .= "                         dat.control_flg='1' AND ";
        $query .= "                         dat.control_code=name.namecd2 ) ";
        $query .= "UNION ";
        $query .= "SELECT DISTINCT name.namecd1, name.namecd2, name.name1 ";
#        $query .= "  FROM name_mst name ";     2004/09/03 arakaki 近大-作業依頼書20040903-03.doc
        $query .= "  FROM V_NAME_MST name ";
        $query .= " WHERE YEAR = '$year' ";
        $query .= "   AND namecd1='Z005' ";
        $query .= "   AND NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM ";
        $query .= "                         admin_control_dat dat ";
        $query .= "                    WHERE ";
        $query .= "                         dat.year = '$year'  AND ";
        $query .= "                         dat.control_flg='2' AND ";
        $query .= "                         dat.control_code=name.namecd2 ) ";
        return $query;


    }

    //UPDATE
    function &getUpdateQuery(&$model)
    {

        //所属コード配列に格納
        $grade  = explode (",", $model->selectdata);
        $attend = explode (",", $model->selectdata2);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        
        $query = "DELETE FROM admin_control_dat WHERE year = '" .$model->year ."'";
        $db->query($query);

        if ($grade[0] != "") {
            for ($i = 0; $i < get_count($grade); $i++){
                $query  = "INSERT INTO admin_control_dat(year, control_flg, control_code, registercd, updated) ";
                $query .= "VALUES('$model->year', '1', '$grade[$i]', '" . STAFFCD . "', '$updated')";
                $db->query($query);
            }
        }
        if ($attend[0] != "") {
            for ($i = 0; $i < get_count($attend); $i++){
                $query  = "INSERT INTO admin_control_dat(year, control_flg, control_code, registercd, updated) ";
                $query .= "VALUES('$model->year', '2', '$attend[$i]', '" . STAFFCD . "', '$updated')";
                $db->query($query);
            }
        }
        $db->commit(); // トランザクションをコミットする。
        $db->autoCommit(true);
        Query::dbCheckIn($db);
        return;
    }
}
?>
