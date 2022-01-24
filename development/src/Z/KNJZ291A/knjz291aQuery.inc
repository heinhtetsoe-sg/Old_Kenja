<?php

require_once('for_php7.php');

class knjz291aQuery extends Query {

    //出身学校年度一覧取得
    function selectSchoolCd($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //出身学校年度一覧取得
    function selectQuery($model)
    {
        $query  = " SELECT STAFFCD, STAFFNAME, ";
        $query .= " CASE WHEN EDBOARD_STAFFCD IS NOT NULL THEN '(' || EDBOARD_STAFFCD || ')' ";
        $query .= " ELSE NULL END AS EDBOARD_STAFFCD ";
        $query .= " FROM V_STAFF_MST ";
        $query .= " WHERE YEAR = '".$model->year."'"; 
        $query .= " ORDER by STAFFCD";
        return $query;
    }
    
    //出身学校年度一覧取得
    function selectYearQuery()
    {
        return "SELECT DISTINCT YEAR FROM STAFF_YDAT GROUP BY YEAR ORDER BY YEAR DESC";
    }
    
    //職員一覧取得
    function selectStaffQuery($model)
    {
        if ($model->Properties["useSchoolWare"] == "1") {
            $query .= " SELECT ";
            $query .= "     STF_WORK.STAFFCD, ";
            $query .= "     STF.STAFFNAME ";
            $query .= " FROM ";
            $query .= "     STAFF_WORK_HIST_DAT STF_WORK ";
            $query .= "     INNER JOIN STAFF_MST STF ON STF_WORK.STAFFCD = STF.STAFFCD ";
            $query .= " WHERE ";
            $query .= "     STF_WORK.FROM_SCHOOLCD = '{$model->schoolCd}' ";
            $query .= "     AND STF_WORK.USE_KNJ = '1' ";
            $query .= "     AND NOT EXISTS (SELECT 'X' ";
            $query .= "                  FROM STAFF_YDAT y ";
            $query .= "                  WHERE y.YEAR = '".$model->year."'";
            $query .= "                  AND y.STAFFCD = STF_WORK.STAFFCD) ";
            $query .= " GROUP BY ";
            $query .= "     STF_WORK.STAFFCD, ";
            $query .= "     STF.STAFFNAME ";
            $query .= " ORDER BY ";
            $query .= "     STF_WORK.STAFFCD ";
        } else {
            $query  = "SELECT STAFFCD,STAFFNAME,  ";
            $query .= "CASE WHEN EDBOARD_STAFFCD IS NOT NULL THEN '(' || EDBOARD_STAFFCD || ')' ";
            $query .= "ELSE NULL END AS EDBOARD_STAFFCD ";
            $query .= "FROM STAFF_MST m ";
            $query .= "WHERE NOT EXISTS (SELECT 'X' ";
            $query .= "                  FROM STAFF_YDAT y ";
            $query .= "                  WHERE y.YEAR = '".$model->year."'";
            $query .= "                  AND y.STAFFCD = m.STAFFCD) ";
            $query .= "ORDER BY STAFFCD";
        }

        return $query;
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery(&$model)
    {        
        //出身学校コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        
        $query = "DELETE FROM STAFF_YDAT WHERE YEAR = '" .$model->year ."'";
        $db->query($query);
        if ($array[0] != ""){
            //成績項目別得点データの登録
            $query = "INSERT INTO STAFF_YDAT(YEAR,STAFFCD,REGISTERCD,UPDATED)";
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .$array[$i] ."'";                                 //職員コード
                $query.= ",'".STAFFCD."'";
                $query.= ",'" .$updated ."')" .$sp;                             //更新日付
            }
            $db->query($query);
        } else {
            $model->year = "";
        }
        $db->commit(); // トランザクションをコミットする。

        Query::dbCheckIn($db);
        return;
    }
}
?>
