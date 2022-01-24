<?php

require_once('for_php7.php');

class knjh210_2Query extends Query {

    //１レコード取得
    function getRow($model, $cd)
    {
        $query  = " SELECT ";
        $query .= "     DOMI.DOMI_CD, ";
        $query .= "     DOMI.DOMI_NAME, ";
        $query .= "     DOMI.DOMI_ZIPCD, ";
        $query .= "     DOMI.DOMI_ADDR1, ";
        $query .= "     DOMI.DOMI_ADDR2, ";
        $query .= "     DOMI.DOMI_TELNO, ";
        $query .= "     DOMI.DOMI_TELNO2, ";
        $query .= "     DOMI.DOMI_FAXNO, ";
        $query .= "     DOMI.DOMI_LEADER, ";
        $query .= "     DOMI.REGISTERCD, ";
        $query .= "     DOMI.UPDATED ";
        if ($model->Properties["useCollectMoneySchool"] == "1") {
            $query .= "     ,M_DOMI.SCHOOL_KIND ";
            $query .= "     ,M_DOMI.COLLECT_L_CD || '-' || M_DOMI.COLLECT_M_CD AS COLLECT_LM_CD ";
            $query .= "     ,M_DOMI.MONTH_MONEY ";
            $query .= "     ,M_DOMI.DAY_MONEY ";
        }
        $query .= " FROM ";
        $query .= "     DOMITORY_MST DOMI ";
        if ($model->Properties["useCollectMoneySchool"] == "1") {
            $query .= "     LEFT JOIN COLLECT_M_DOMITORY_MST M_DOMI ON DOMI.DOMI_CD = M_DOMI.DOMI_CD ";
        }
        $query .= " WHERE ";
        $query .= "     DOMI.DOMI_CD = '{$cd}' ";

        return $query;
    }

    //登録種目データ取得
    function getDomitoryMst($model, $domicd)
    {
        $query  = " SELECT ";
        $query .= "     T1.DOMI_CD || ' ' || T1.DOMI_NAME AS LABEL, ";
        $query .= "     T1.DOMI_CD AS VALUE, ";
        $query .= " FROM ";
        $query .= "     DOMITORY_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.DOMI_CD = '".$domicd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //入金項目取得
    function getLMmst($schoolKind)
    {
        $query  = " SELECT ";
        $query .= "     MMST.COLLECT_L_CD || '-' || MMST.COLLECT_M_CD AS VALUE, ";
        $query .= "     MMST.COLLECT_L_CD || MMST.COLLECT_M_CD || ':' || MMST.COLLECT_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST MMST ";
        $query .= " WHERE ";
        $query .= "     MMST.SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND MMST.SCHOOL_KIND   = '{$schoolKind}' ";
        $query .= "     AND MMST.YEAR          = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //種目マスタ取得
    function filterDomitoryMst($model, $domicd)
    {
        $query  = " SELECT ";
        $query .= "     DOMI_CD || ' ' || DOMI_NAME AS LABEL, ";
        $query .= "     DOMI_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     DOMITORY_MST ";
        $query .= " WHERE ";
        $query .= "     DOMI_CD NOT IN ( SELECT ";
        $query .= "                             DOMI_CD ";
        $query .= "                         FROM ";
        $query .= "                             DOMITORY_MST  ";
        $query .= "                         WHERE ";
        $query .= "                             DOMI_CD = '".$domicd."' ";
        $query .= "                             ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度データの確認
    function IsExisting($model, $domicd)
    {
        $db    = Query::dbCheckOut();
        $query = "select * from DOMITORY_YDAT where DOMI_CD = '".$domicd."'"; 
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);
        if (is_array($row)) {
            return true;
        } else {
            return false;
        }       
    }
    
    //--- INSERT
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $data["DOMI_CD"][TEXT]      = $fields["DOMI_CD"];
        $data["DOMI_NAME"][TEXT]    = $fields["DOMI_NAME"];
        $data["DOMI_ZIPCD"][TEXT]   = $fields["DOMI_ZIPCD"];
        $data["DOMI_ADDR1"][TEXT]   = $fields["DOMI_ADDR1"];
        $data["DOMI_ADDR2"][TEXT]   = $fields["DOMI_ADDR2"];
        $data["DOMI_TELNO"][TEXT]   = $fields["DOMI_TELNO"];
        $data["DOMI_TELNO2"][TEXT]  = $fields["DOMI_TELNO2"];
        $data["DOMI_FAXNO"][TEXT]   = $fields["DOMI_FAXNO"];
        $data["DOMI_LEADER"][TEXT]  = $fields["DOMI_LEADER"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "DOMITORY_MST");
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }
    
    //--- UPDATE 
    function &getUpdateQuery($model, $fields)
    {        
        $db = Query::dbCheckOut();
        
        $data["DOMI_CD"][TEXT]      = $fields["DOMI_CD"];
        $data["DOMI_NAME"][TEXT]    = $fields["DOMI_NAME"];
        $data["DOMI_ZIPCD"][TEXT]   = $fields["DOMI_ZIPCD"];
        $data["DOMI_ADDR1"][TEXT]   = $fields["DOMI_ADDR1"];
        $data["DOMI_ADDR2"][TEXT]   = $fields["DOMI_ADDR2"];
        $data["DOMI_TELNO"][TEXT]   = $fields["DOMI_TELNO"];
        $data["DOMI_TELNO2"][TEXT]  = $fields["DOMI_TELNO2"];
        $data["DOMI_FAXNO"][TEXT]   = $fields["DOMI_FAXNO"];
        $data["DOMI_LEADER"][TEXT]  = $fields["DOMI_LEADER"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where = " WHERE DOMI_CD = '" .$fields["DOMI_CD"]."'";
        $query = Query::updateSQL($data, "DOMITORY_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    /**
     * DELETE
     */
    function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM DOMITORY_MST "
                ." WHERE DOMI_CD  = '" .$fields["DOMI_CD"]."'";
        $db->query($query);

        if ($model->Properties["useCollectMoneySchool"] == "1") {
            $query  = " DELETE FROM COLLECT_M_DOMITORY_MST ";
            $query .= " WHERE DOMI_CD  = '{$fields["DOMI_CD"]}' ";
            $db->query($query);
        }

        Query::dbCheckIn($db);

        return $result;
    }

    function &getDelInsMdomitoryQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM COLLECT_M_DOMITORY_MST ";
        $query .= " WHERE DOMI_CD  = '{$fields["DOMI_CD"]}' ";

        $db->query($query);

        if ($this->field["SCHOOL_KIND"] != "" && $this->field["COLLECT_LM_CD"] != "") {
            list($lcd, $mcd) = explode("-", $fields["COLLECT_LM_CD"]);
            $data["DOMI_CD"][TEXT]       = $fields["DOMI_CD"];
            $data["SCHOOLCD"][TEXT]      = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]   = $fields["SCHOOL_KIND"];
            $data["YEAR"][TEXT]          = CTRL_YEAR;
            $data["COLLECT_L_CD"][TEXT]  = $lcd;
            $data["COLLECT_M_CD"][TEXT]  = $mcd;
            $data["MONTH_MONEY"][NUMBER] = $fields["MONTH_MONEY"];
            $data["DAY_MONEY"][NUMBER]   = $fields["DAY_MONEY"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "sysdate()";

            $query = Query::insertSQL($data, "COLLECT_M_DOMITORY_MST");
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }
}
?>
