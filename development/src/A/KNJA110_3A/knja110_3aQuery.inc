<?php

require_once('for_php7.php');

class knja110_3aQuery extends Query
{

    //起動チェック
    public function checktoStart()
    {
        return "SELECT COUNT(*) FROM v_name_mst WHERE namecd1 = 'A004' AND year = '".CTRL_YEAR."'";
    }

    //京都判定用
    public function getKindaiJudgment()
    {
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
    public function getRow($model)
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

    public function colCheck($tabname, $colname)
    {
        $query  = "";
        $query .= "  SELECT 1 ";
        $query .= "  FROM (SELECT 1 FROM SYSCAT.COLUMNS ";
        $query .= "        WHERE";
        $query .= "            TABNAME = '".$tabname."' AND";
        $query .= "            COLNAME= '".$colname."' ";
        $query .= "  ) T1 ";
        return $query;
    }

    public function getValidatePrintDropRegdSql($model)
    {
        $query  = "";
        $query .= "  SELECT DISTINCT ";
        $query .= "      T1.SCHREGNO ";
        $query .= "  FROM SCHREG_TRANSFER_DAT T1 ";
        $query .= "  INNER JOIN SCHREG_TRANSFER_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "      AND T2.TRANSFERCD = T1.TRANSFERCD ";
        $query .= "      AND MONTH(T2.TRANSFER_EDATE) = 3 ";
        $query .= "      AND DAY(T2.TRANSFER_EDATE) = 31 ";
        $query .= "      AND YEAR(T1.TRANSFER_SDATE) = YEAR(T2.TRANSFER_EDATE) ";
        $query .= "  WHERE";
        $query .= "      T1.SCHREGNO = '".$model->SCHREGNO."' AND";
        $query .= "      T1.TRANSFERCD = '1' AND ";
        $query .= "      MONTH(T1.TRANSFER_SDATE) = 4 AND ";
        $query .= "      DAY(T1.TRANSFER_SDATE) = 1 AND ";
        $query .= "      T1.TRANSFER_SDATE = DATE('".$model->TRANSFER_SDATE."') ";
        return $query;
    }

    public function getStudentName($schregno)
    {
        return "SELECT schregno,name_show AS name FROM schreg_base_mst WHERE schregno ='".$schregno."'";
    }

    //異動区分の取得
    public function getTransfercd()
    {
        return "SELECT DISTINCT namecd2,name1 FROM v_name_mst WHERE namecd1='A004' AND YEAR ='".CTRL_YEAR."'";
    }

    //取得した異動区分をログイン年度で名称マスタ登録有無を確認
    public function getTransfercdCheck($cd)
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
    public function getList($model)
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
    public function getEntDate($schregno)
    {
        $query  = " SELECT ";
        $query .= "     ENT_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

#        $data["SCHREGNO"][TEXT]             = $model->SCHREGNO;
        $data["SCHREGNO"][TEXT]             = substr("00000000".$model->SCHREGNO, strlen($model->SCHREGNO), 8);
        $data["TRANSFERCD"][TEXT]           = $model->field["TRANSFERCD"];
        $data["TRANSFER_SDATE"][TEXT]       = str_replace("/", "-", $model->field["TRANSFER_SDATE"]);
        $data["TRANSFER_EDATE"][TEXT]       = str_replace("/", "-", $model->field["TRANSFER_EDATE"]);
        $data["TRANSFERREASON"][TEXT]       = $model->field["TRANSFERREASON"];
        $data["TRANSFERPLACE"][TEXT]        = $model->field["TRANSFERPLACE"];
        $data["TRANSFERADDR"][TEXT]         = $model->field["TRANSFERADDR"];
        if ($model->kyoto > 0 && $model->field["TRANSFERCD"] == "1") {
            $data["REMARK1"][TEXT]          = $model->field["REMARK1"];
        }
        $data["ABROAD_CREDITS"][NUMBER]     = $model->field["ABROAD_CREDITS"];
        if ($model->hasABROAD_PRINT_DROP_REGD) {
            $data["ABROAD_PRINT_DROP_REGD"][TEXT] = $model->field["ABROAD_PRINT_DROP_REGD"];
        }
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_TRANSFER_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["TRANSFER_SDATE"][TEXT]       = str_replace("/", "-", $model->field["TRANSFER_SDATE"]);
        $data["TRANSFER_EDATE"][TEXT]       = str_replace("/", "-", $model->field["TRANSFER_EDATE"]);
        $data["TRANSFERREASON"][TEXT]       = $model->field["TRANSFERREASON"];
        $data["TRANSFERPLACE"][TEXT]        = $model->field["TRANSFERPLACE"];
        $data["TRANSFERADDR"][TEXT]         = $model->field["TRANSFERADDR"];
        if ($model->kyoto > 0 && $model->field["TRANSFERCD"] == "1") {
            $data["REMARK1"][TEXT]          = $model->field["REMARK1"];
        }
        $data["ABROAD_CREDITS"][NUMBER]     = $model->field["ABROAD_CREDITS"];
        if ($model->hasABROAD_PRINT_DROP_REGD) {
            $data["ABROAD_PRINT_DROP_REGD"][TEXT] = $model->field["ABROAD_PRINT_DROP_REGD"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        $where = " WHERE SCHREGNO           = '" .$model->SCHREGNO."'";
        $where .= " AND TRANSFERCD          = '" .$model->field["TRANSFERCD"]."'";
        $where .= " AND TRANSFER_SDATE      = '" .str_replace("/", "-", $model->TRANSFER_SDATE)."'";

        $query = Query::updateSQL($data, "SCHREG_TRANSFER_DAT", $where);

        $db->query($query);
        Query::dbCheckIn($db);

        return ;
    }

    public function &getDeleteQuery($model)
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
            $query .= "      transfer_sdate = '".str_replace("/", "-", $model->field["TRANSFER_SDATE"])."'";
            $db->query($query);
            Query::dbCheckIn($db);
            return true;
        } else {
            Query::dbCheckIn($db);
            return false;
        }
    }
}
