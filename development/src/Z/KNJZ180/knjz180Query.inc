<?php

require_once('for_php7.php');

class knjz180Query extends Query
{

    //１レコード取得
    public function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM holiday_mst ".
                 " WHERE HOLIDAY = '" .strtr($cd, "/", "-") ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    /**
     * INSERT
     */
    public function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["HOLIDAY"][TEXT]       = strtr($fields["HOLIDAY"], "/", "-");
        $data["REMARK"][TEXT]        = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";

        $query = Query::insertSQL($data, "holiday_mst");

        $db->query($query);

        Query::dbCheckIn($db);

        return;
    }
    /**
     * UPDATE
     */
    public function &getUpdateQuery($fields, $cd)
    {
        $db = Query::dbCheckOut();

        $data["HOLIDAY"][TEXT]       = strtr($fields["HOLIDAY"], "/", "-");
        $data["REMARK"][TEXT]        = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";

        $where = " WHERE HOLIDAY     = '" .strtr($cd, "/", "-")."'";

        $query = Query::updateSQL($data, "holiday_mst", $where);

        $db->query($query);
        Query::dbCheckIn($db);

        return ;
    }
    /**
     * DELETE
     */
    public function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM holiday_mst "
                ." WHERE HOLIDAY  = '".strtr($fields["HOLIDAY"], "/", "-")."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
    /**
     * CHECKCOPY
     */
    public function &checkCopy($model)
    {
        $current_year = (int)$model->year_select - 1;
        $date_start = $current_year."-04-01";
        $date_end = ($current_year+1)."-03-31";

        $query  = "select count(*) from holiday_mst ";
        $query .= "where HOLIDAY between '".$date_start."' and '" .$date_end. "'";

        return $query;
    }
    /**
     * CHECKCOPY2
     */
    public function &checkCopy2($model)
    {
        $current_year = (int)$model->year_select;
        $date_start = $current_year."-04-01";
        $date_end = ($current_year+1)."-03-31";

        $query  = "select count(*) from holiday_mst ";
        $query .= "where HOLIDAY between '".$date_start."' and '" .$date_end. "'";

        return $query;
    }
    /**
     * COPYQUERY
     */
    public function &copyQuery($model, $db)
    {
        $current_year = (int)$model->year_select;
        $date_start = $current_year."-01-01";
        $date_end = $current_year."-03-31";
        $new_year = (int)$model->year_select + 1;

        $query  = "insert into holiday_mst (HOLIDAY,REMARK,REGISTERCD,UPDATED ) ";
        $query .= "select replace(HOLIDAY,'{$current_year}','{$new_year}'),REMARK,'".STAFFCD."',sysdate() from holiday_mst ";
        $query .= "where HOLIDAY between'".$date_start."' and '" .$date_end. "' order by HOLIDAY";

        $db->query($query);

        $current_year = (int)$model->year_select - 1;
        $date_start = $current_year."-04-01";
        $date_end = $current_year."-12-31";
        $new_year = (int)$model->year_select;

        $query  = "insert into holiday_mst (HOLIDAY,REMARK,REGISTERCD,UPDATED ) ";
        $query .= "select replace(HOLIDAY,'{$current_year}','{$new_year}'),REMARK,'".STAFFCD."',sysdate() from holiday_mst ";
        $query .= "where HOLIDAY between'".$date_start."' and '" .$date_end. "' order by HOLIDAY";

        $db->query($query);
    }
}
