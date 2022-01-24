<?php

require_once('for_php7.php');

class knjz280Query extends Query {

    //所属年度一覧取得
    function selectQuery($model)
    {
        $query =  " SELECT JOBCD, JOBNAME FROM V_JOB_MST ";
        $query .= " WHERE YEAR = '".$model->year."' ";
        $query .= " ORDER BY JOBCD ";

        return $query;
    }

    //所属年度一覧取得
    function selectYearQuery()
    {
        $query =  " SELECT YEAR ";
        $query .= " FROM JOB_YDAT ";
        $query .= " GROUP BY YEAR ";
        $query .= " ORDER BY YEAR DESC ";

        return $query;
    }

    //所属一覧取得
    function selectJobQuery($jobcd_array,$model)
    {

        $query  = "SELECT JOBCD, JOBNAME ";
        $query .= " FROM JOB_MST M ";
        $query .= " WHERE NOT EXISTS ( SELECT 'X' ";
        $query .= "                    FROM JOB_YDAT Y ";
        $query .= "                    WHERE Y.YEAR = '".$model->year."'";
        $query .= "                    AND Y.JOBCD = M.JOBCD) ";
        $query .= " ORDER BY JOBCD ";

#        $jobcd = implode($jobcd_array,"','");
#        return "select JOBCD, JOBNAME from job_mst where JOBCD not in ('".$jobcd."') order by JOBCD ";

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
        
        $query = "DELETE from job_ydat where YEAR = '" .$model->year ."'";
        $db->query($query);
        
        if ($array[0] != "") {
            $query = "INSERT INTO job_ydat(YEAR,JOBCD,REGISTERCD,UPDATED)";
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
