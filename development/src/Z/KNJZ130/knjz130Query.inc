<?php

require_once('for_php7.php');

class knjz130Query extends Query {

    //名称年度一覧取得
    function selectQuery($model)
    {
        $query =  "select m.NAMECD1,m.NAMECD2,m.NAME1 from name_ydat d,name_mst m ";
        $query .= "where d.NAMECD1 = m.NAMECD1 ";
        $query .= "and d.NAMECD2 = m.NAMECD2 ";
        $query .= "and d.year = '".$model->year."'"; 
        $query .= "order by m.NAMECD1,m.NAMECD2";
        return $query;
    }
    
    //年度一覧取得
    function selectYearQuery()
    {
        $query  = "select smallint(year) as year from name_ydat ";
        $query .= "group by year ";
        $query .= "order by year desc "; 
        return $query;
    }
    //名称一覧取得
    function selectnameQuery($yearcd_array)
    {
        $yearcd = implode($yearcd_array,"','");
      
        $query  = "select NAMECD1,NAMECD2,NAME1 from name_mst "; 
        $query .= "where concat(NAMECD1,NAMECD2) not in ('".$yearcd."') ";
        $query .= "order by NAMECD1,NAMECD2";
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
        
        $query = "DELETE from name_ydat where year = '" .$model->year ."'";
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
