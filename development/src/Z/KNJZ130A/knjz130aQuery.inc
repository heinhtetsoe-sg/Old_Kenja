<?php

require_once('for_php7.php');

class knjz130aQuery extends Query {

    //名称年度一覧取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     MST.NAMECD1 ";
        $query .= "     , MST.NAMECD2 ";
        $query .= "     , MST.NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_YDAT DAT ";
        $query .= "     INNER JOIN NAME_MST MST ";
        $query .= "         ON MST.NAMECD1 = DAT.NAMECD1 ";
        $query .= "         AND MST.NAMECD2 = DAT.NAMECD2 ";
        $query .= "     INNER JOIN NAMECDDESC_MST NCD_M ";
        $query .= "         ON NCD_M.NAMECD = DAT.NAMECD1 ";
        $query .= " WHERE ";
        $query .= "     DAT.YEAR = '".$model->year."' ";
        $query .= "     AND NCD_M.MODIFY_FLG = '2' ";
        $query .= " ORDER BY ";
        $query .= "     MST.NAMECD1 ";
        $query .= "     , MST.NAMECD2 ";

        return $query;
    }
    
    //年度一覧取得
    function selectYearQuery()
    {
        $query  = " SELECT ";
        $query .= "     SMALLINT (YEAR) AS YEAR ";
        $query .= " FROM ";
        $query .= "     NAME_YDAT YDAT ";
        $query .= "     INNER JOIN NAMECDDESC_MST NCD_M ";
        $query .= "         ON NCD_M.NAMECD = YDAT.NAMECD1 ";
        $query .= " WHERE ";
        $query .= "     NCD_M.MODIFY_FLG = '2' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }
    //名称一覧取得
    function selectnameQuery($yearcd_array)
    {
        $yearcd = implode($yearcd_array,"','");

        $query  = " SELECT ";
        $query .= "     MST.NAMECD1 ";
        $query .= "     , MST.NAMECD2 ";
        $query .= "     , MST.NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST MST ";
        $query .= "     INNER JOIN NAMECDDESC_MST NCD_M ";
        $query .= "         ON NCD_M.NAMECD = MST.NAMECD1 ";
        $query .= " WHERE ";
        $query .= "     CONCAT(MST.NAMECD1, MST.NAMECD2) NOT IN ('".$yearcd."') ";
        $query .= "     AND NCD_M.MODIFY_FLG = '2' ";
        $query .= " ORDER BY ";
        $query .= "     MST.NAMECD1 ";
        $query .= "     , MST.NAMECD2 ";

        return $query;
        
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery($model)
    {        
        //名称コード配列に格納
        $array = explode (",", $model->selectdata);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     NAME_YDAT DAT ";
        $query .= " WHERE ";
        $query .= "     DAT.YEAR = '".$model->year."' ";
        $query .= "     AND EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "             * ";
        $query .= "         FROM ";
        $query .= "             NAMECDDESC_MST NCD_M ";
        $query .= "         WHERE ";
        $query .= "             NCD_M.NAMECD = DAT.NAMECD1 ";
        $query .= "             AND NCD_M.MODIFY_FLG = '2' ";
        $query .= "     ) ";
        $db->query($query);

        //名称年度データの登録
        if ($array[0] !="") {
            $query = "INSERT INTO name_ydat(year,NAMECD1,NAMECD2,REGISTERCD,UPDATED)";
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $array_sep = explode(":",$array[$i]);

                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .$array_sep[0] ."'";                              //名称区分
                $query.= ",'" .$array_sep[1] ."'";                              //名称コード
                $query.= ",'" .STAFFCD ."'";                                    //職員コード
                $query.= ",'" .$updated ."')" .$sp;                             //更新日付
    
            }
            $db->query($query);
        } else {
            $all_light = true;
        }
        $db->commit(); // ﾄﾗﾝｻﾞｸｼｮﾝをコミットする。

        Query::dbCheckIn($db);
        return $all_light;
    }
}
?>
