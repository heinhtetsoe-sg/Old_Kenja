<?php

require_once('for_php7.php');

class knjz055Query extends Query {

    //所属年度一覧取得
    function selectQuery($model)
    {
        $query =  " SELECT COURSECODE, COURSECODENAME FROM V_COURSECODE_MST ";
        $query .= " WHERE YEAR = '".$model->year."' ";
        $query .= " ORDER BY COURSECODE ";

        return $query;
    }

    //所属年度一覧取得
    function selectYearQuery()
    {
        $query =  " SELECT YEAR ";
        $query .= " FROM COURSECODE_YDAT ";
        $query .= " GROUP BY YEAR ";
        $query .= " ORDER BY YEAR DESC ";

        return $query;
    }

    //所属一覧取得
    function selectJobQuery($COURSECODE_array,$model)
    {

        $query  = "SELECT COURSECODE, COURSECODENAME ";
        $query .= " FROM COURSECODE_MST M ";
        $query .= " WHERE NOT EXISTS ( SELECT 'X' ";
        $query .= "                    FROM COURSECODE_YDAT Y ";
        $query .= "                    WHERE Y.YEAR = '".$model->year."'";
        $query .= "                    AND Y.COURSECODE = M.COURSECODE) ";
        $query .= " ORDER BY COURSECODE ";

#        $COURSECODE = implode($COURSECODE_array,"','");
#        return "select COURSECODE, COURSECODENAME from COURSECODE_MST where COURSECODE not in ('".$COURSECODE."') order by COURSECODE ";

        return $query;
    }

    /**
     * UPDATE
     */
    function &getUpdateQuery(&$model)
    {        
        //所属コード配列に格納
        $array = explode (",", $model->selectdata);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        
        $query = "DELETE from COURSECODE_YDAT where YEAR = '" .$model->year ."'";
        $db->query($query);
        
        if ($array[0] != "") {
            $query = "INSERT INTO COURSECODE_YDAT(YEAR,COURSECODE,REGISTERCD,UPDATED)";
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";           //年度
                $query.= ",'" .$array[$i] ."'";             //所属コード
                $query.= ",'" .STAFFCD ."'";                //登録者コード
                $query.= ",'" .$updated ."')" .$sp;         //更新日付
            }
            $db->query($query);
        } else {
            $model->year = "";
        }

        //transaction committed
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
