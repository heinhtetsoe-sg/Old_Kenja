<?php

require_once('for_php7.php');

class knjj201Query extends Query {

    //年度
    function selectYearQuery() {
        return "SELECT DISTINCT year FROM GUARDIAN_COMMITTEE_YDAT ORDER BY year DESC";
    }

    //年度一覧取得
    function selectQuery($model) {
        $query  = "SELECT t1.DIV, t1.EXECUTIVECD, N1.NAME1 AS DIV_NAME, t1.NAME AS EXECUTIVECD_NAME ";
        $query .= " FROM GUARDIAN_COMMITTEE_MST t1,GUARDIAN_COMMITTEE_YDAT t2 ";
        $query .= "      LEFT JOIN NAME_MST N1 ON N1.NAMECD1='J006' AND N1.NAMECD2=t2.DIV ";
        $query .= " WHERE t2.year = '".$model->year."' "; 
        $query .= "       AND t1.DIV = t2.DIV "; 
        $query .= "       AND t1.EXECUTIVECD = t2.EXECUTIVECD "; 
        $query .= " ORDER BY t1.DIV, t1.EXECUTIVECD";
        return $query;
    }

    //一覧取得
    function selectFareQuery($fare_cd_array,$model) {
        $query  = "SELECT DIV, EXECUTIVECD, N1.NAME1 AS DIV_NAME, NAME AS EXECUTIVECD_NAME ";
        $query .= " FROM GUARDIAN_COMMITTEE_MST m ";
        $query .= "      LEFT JOIN NAME_MST N1 ON N1.NAMECD1='J006' AND N1.NAMECD2=m.DIV ";
        $query .= " WHERE NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM GUARDIAN_COMMITTEE_YDAT y ";
        $query .= "                    WHERE y.year = '".$model->year."'";
        $query .= "                      AND y.DIV = m.DIV "; 
        $query .= "                      AND y.EXECUTIVECD = m.EXECUTIVECD) ";
        $query .= " ORDER BY DIV, EXECUTIVECD ";
        return $query;
    }
    //UPDATE
    function &getUpdateQuery(&$model) {
        //コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query = "DELETE FROM GUARDIAN_COMMITTEE_YDAT WHERE year = '" .$model->year ."'";
        $db->query($query);

        $query = "INSERT INTO GUARDIAN_COMMITTEE_YDAT(year,DIV,EXECUTIVECD,registercd,updated)";
        $query.= "VALUES";
        if ($array[0] != ""){
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .substr($array[$i],0,1)."'";                      //区分
                $query.= ",'" .substr($array[$i],1)."'";                        //コード
                $query.= ",'" .STAFFCD ."'";                                    //STAFFCD
                $query.= ",'" .$updated ."')" .$sp;                             //更新日付
            }
            $db->query($query);
        } else {
            $model->year = "";
        }
        $db->commit(); // ﾄﾗﾝｻﾞｸｼｮﾝをコミットする。

        Query::dbCheckIn($db);
        return;
    }
}
?>
