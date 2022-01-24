<?php

require_once('for_php7.php');

class knja110_3bQuery extends Query {

    //起動チェック
    function checktoStart()
    {
        return "SELECT COUNT(*) FROM v_name_mst WHERE namecd1 = 'A004' AND year = '".CTRL_YEAR."'";
    }

    //京都判定用
    function getKindaiJudgment() {
        $query  = "  SELECT  ";
        $query .= "      COUNT(*) ";
        $query .= "  FROM ";
        $query .= "      NAME_MST ";
        $query .= "  WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     NAME1   = 'kyoto' ";

        return $query;
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

    //取得した異動区分をログイン年度で名称マスタ登録有無を確認
    function getTransfercdCheck($cd)
    {
        $query  = "  SELECT  ";
        $query .= "      COUNT(*) AS CNT ";
        $query .= "  FROM ";
        $query .= "      V_NAME_MST ";
        $query .= "  WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND NAMECD1 = 'A004' ";
        $query .= "  AND NAMECD2 = '".$cd."' ";
        
        return $query;
    }

    //リストの取得
    function getList($model)
    {

        $query  = "";
        $query .= "  SELECT ";
        $query .= "      T1.*,";
        $query .= "      MEISYOU_GET(T1.transfercd,'A004',1) AS transfername ";
        $query .= "  FROM";
        $query .= "      schreg_transfer_dat T1";
        $query .= "  WHERE";
        $query .= "      T1.schregno = '".$model->SCHREGNO."'";
        $query .= "  ORDER BY";
        $query .= "      T1.transfer_sdate,";
        $query .= "      T1.transfercd";

        return $query;
    }

    //入学日付の取得
    function getEntDate($schregno)
    {
        $query  = " SELECT ";
        $query .= "     ENT_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

#        $data["SCHREGNO"][TEXT]             = $model->SCHREGNO;
        $data["SCHREGNO"][TEXT]             = substr("00000000".$model->SCHREGNO,strlen($model->SCHREGNO),8);
        $data["TRANSFERCD"][TEXT]           = $model->field["TRANSFERCD"];
        $data["TRANSFER_SDATE"][TEXT]       = str_replace("/","-",$model->field["TRANSFER_SDATE"]);
        $data["TRANSFER_EDATE"][TEXT]       = str_replace("/","-",$model->field["TRANSFER_EDATE"]);
        $data["TRANSFERREASON"][TEXT]       = $model->field["TRANSFERREASON"];
        $data["TRANSFERPLACE"][TEXT]        = $model->field["TRANSFERPLACE"];
        $data["TRANSFERADDR"][TEXT]         = $model->field["TRANSFERADDR"];
        if ($model->kyoto > 0 && $model->field["TRANSFERCD"] == "1") {
            $data["REMARK1"][TEXT]          = $model->field["REMARK1"];
        }
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
        if ($model->kyoto > 0 && $model->field["TRANSFERCD"] == "1") {
            $data["REMARK1"][TEXT]          = $model->field["REMARK1"];
        }
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
