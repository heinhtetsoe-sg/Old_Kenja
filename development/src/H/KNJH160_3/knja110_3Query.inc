<?php

require_once('for_php7.php');

class knja110_3Query extends Query {

    //起動チェック
    function checktoStart()
    {
        return "SELECT COUNT(*) FROM v_name_mst WHERE namecd1 = 'A004' AND year = '".CTRL_YEAR."'";
    }

    //１レコード取得
    function getRow($model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= "  SELECT * FROM schreg_transfer_dat";
        $query .= "  WHERE";
        $query .= "      schregno       = '".$model->SCHREGNO."' AND";
        $query .= "      transfercd     = '".$model->TRANSFERCD."' AND";
        $query .= "      transfer_sdate = DATE('".$model->TRANSFER_SDATE."')";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    function getStudentName($schregno)
    {
        return "SELECT schregno,name_show AS name FROM schreg_base_mst WHERE schregno ='".$schregno."'";
    }

    //異動区分の取得
    function getTransfercd()
    {
        return "SELECT DISTINCT namecd2,name1 FROM v_name_mst WHERE namecd1='A004' AND YEAR ='".CTRL_YEAR."'";
    }
        
    //リストの取得
    function getList($model)
    {

        $query  = "";
        $query .= "  SELECT ";
        $query .= "      T1.schregno,";
        $query .= "      T1.transfer_sdate,";
        $query .= "      T1.transfer_edate,";
        $query .= "      T1.transfercd,";
        $query .= "      MEISYOU_GET(T1.transfercd,'A004',1) AS transfername,";
        $query .= "      T1.transferreason,";
        $query .= "      T1.transferplace,";
        $query .= "      T1.transferaddr,";
        $query .= "      T1.abroad_classdays,";
        $query .= "      T1.abroad_credits";
        $query .= "  FROM";
        $query .= "      schreg_transfer_dat T1";
        $query .= "  WHERE";
        $query .= "      T1.schregno = '".$model->SCHREGNO."'";
        $query .= "  ORDER BY";
        $query .= "      T1.transfer_sdate,";
        $query .= "      T1.transfercd";

        return $query;
    }

    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

#        $data["SCHREGNO"][TEXT]             = $model->SCHREGNO;
        $data["SCHREGNO"][TEXT]            = substr("00000000".$model->SCHREGNO,strlen($model->SCHREGNO),8);
        $data["TRANSFERCD"][TEXT]           = $model->field["TRANSFERCD"];
        $data["TRANSFER_SDATE"][TEXT]       = str_replace("/","-",$model->field["TRANSFER_SDATE"]);
        $data["TRANSFER_EDATE"][TEXT]       = str_replace("/","-",$model->field["TRANSFER_EDATE"]);
        $data["TRANSFERREASON"][TEXT]       = $model->field["TRANSFERREASON"];
        $data["TRANSFERPLACE"][TEXT]        = $model->field["TRANSFERPLACE"];
        $data["TRANSFERADDR"][TEXT]         = $model->field["TRANSFERADDR"];
        $data["ABROAD_CLASSDAYS"][NUMBER]   = $model->field["ABROAD_CLASSDAYS"];
        $data["ABROAD_CREDITS"][NUMBER]     = $model->field["ABROAD_CREDITS"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_TRANSFER_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["TRANSFER_SDATE"][TEXT]       = str_replace("/","-",$model->field["TRANSFER_SDATE"]);
        $data["TRANSFER_EDATE"][TEXT]       = str_replace("/","-",$model->field["TRANSFER_EDATE"]);
        $data["TRANSFERREASON"][TEXT]       = $model->field["TRANSFERREASON"];
        $data["TRANSFERPLACE"][TEXT]        = $model->field["TRANSFERPLACE"];
        $data["TRANSFERADDR"][TEXT]         = $model->field["TRANSFERADDR"];
        $data["ABROAD_CLASSDAYS"][NUMBER]   = $model->field["ABROAD_CLASSDAYS"];
        $data["ABROAD_CREDITS"][NUMBER]     = $model->field["ABROAD_CREDITS"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        $where = " WHERE SCHREGNO           = '" .$model->SCHREGNO."'";
        $where .= " AND TRANSFERCD          = '" .$model->field["TRANSFERCD"]."'";
        $where .= " AND TRANSFER_SDATE      = '" .str_replace("/","-",$model->TRANSFER_SDATE)."'";

        $query = Query::updateSQL($data, "SCHREG_TRANSFER_DAT" ,$where);

        $db->query($query);
        Query::dbCheckIn($db);

        return ;
   }

   function &getDeleteQuery($model)
   {
        $db = Query::dbCheckOut();

        $query = "  SELECT * FROM schreg_transfer_dat WHERE schregno = '".$model->SCHREGNO."'";
        $get    = $db->getRow($query);

        if (is_array($get)) {
            $query  = "";
            $query .= "  DELETE FROM schreg_transfer_dat";
            $query .= "  WHERE";
            $query .= "      schregno       = '".$model->SCHREGNO."' AND";
            $query .= "      transfercd     = '".$model->field["TRANSFERCD"]."' AND";
            $query .= "      transfer_sdate = '".str_replace("/","-",$model->field["TRANSFER_SDATE"])."'";
            $db->query($query);
            Query::dbCheckIn($db);
            return true;
        }else{
            Query::dbCheckIn($db);
            return false;
        }
    }
}
?>
