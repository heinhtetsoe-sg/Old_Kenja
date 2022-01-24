<?php

require_once('for_php7.php');

class knjx_rishu_kojinQuery extends Query {
    
    //SCHREG_SEND_ADDRESS_DAT　データ確認
    function countSchregSendAddress($schregno, $div)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_SEND_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " AND DIV      = '".$div."' ";

        return $query;
    }

    //１レコード取得
    function getRow($schregno, $div)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SEND_NAME     AS NAME, ";
        $query .= "     SEND_ZIPCD    AS ZIPCD, ";
        $query .= "     SEND_ADDR1    AS ADDR1, ";
        $query .= "     SEND_ADDR2    AS ADDR2, ";
        $query .= "     SEND_TELNO    AS TELNO, ";
        $query .= "     SEND_TELNO2   AS TELNO2, ";
        $query .= "     SEND_ADDR_FLG AS ADDR_FLG, ";
        $query .= "     SEND_AREACD   AS AREACD, ";
        $query .= "     SEND_JOBCD    AS JOBCD   ";        
        $query .= " FROM ";
        $query .= "     SCHREG_SEND_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " AND DIV      = '".$div."' ";

        return $query;
    }

    //１レコード取得(志願者)
    function getRowFresh($schregno)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     NAME, ";
        $query .= "     ZIPCD, ";
        $query .= "     ADDR1, ";
        $query .= "     ADDR2, ";
        $query .= "     TELNO, ";
        $query .= "     ADDR_FLG, ";
        $query .= "     AREACD ";
        $query .= " FROM ";
        $query .= "     FRESHMAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }
    
    //１レコード取得(在学者)
    function getRowAddress($schregno)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.NAME, ";
        $query .= "     T1.ZIPCD, ";
        $query .= "     T1.ADDR1, ";
        $query .= "     T1.ADDR2, ";
        $query .= "     T1.TELNO, ";
        $query .= "     T1.ADDR_FLG, ";
        $query .= "     T1.AREACD ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT T1";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregno."' ";
        $query .= " AND T1.ISSUEDATE IN (  SELECT ";
        $query .= "                         MAX(S1.ISSUEDATE) ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_ADDRESS_DAT S1 ";
        $query .= "                     WHERE ";
        $query .= "                         S1.SCHREGNO = T1.SCHREGNO "; 
        $query .= "                     ) ";

        return $query;
    }

    //SCHREG_BASE_DETAIL_MST取得
    function getDetailRow($schregno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     BASE_REMARK1, ";
        $query .= "     BASE_REMARK2, ";
        $query .= "     BASE_REMARK5 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " AND BASE_SEQ = '".$seq."' ";

        return $query;
    }

    //SCHREG_BASE_YEAR_DETAIL_MST取得
    function getYearDetailRow($model, $seq)
    {
        $query  = " SELECT ";
        $query .= "     BASE_REMARK1 AS SCHOOLING_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_YEAR_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     BASE_SEQ    = '".$seq."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($cd)
    {
        $query  = "  SELECT ";
        $query .= "      NAMECD2 AS VALUE, ";
        $query .= "      NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= "  FROM ";
        $query .= "      NAME_MST ";
        $query .= "  WHERE ";
        $query .= "      NAMECD1 ='".$cd."' ";
        
        return $query;
    }

    //UPDATE (SCHREG_SEND_ADDRESS_DAT)
    function &getUpdateQuery($fields, $model, $countdata)
    {
        $db = Query::dbCheckOut();
        
        if ($countdata == "0") {
            $data["SCHREGNO"][TEXT] = $model->schregno;
            $data["DIV"][TEXT]      = '1';
        }
        $data["SEND_NAME"][TEXT]         = $fields["NAME"];
        $data["SEND_ZIPCD"][TEXT]        = $fields["ZIPCD"];
        $data["SEND_ADDR1"][TEXT]        = $fields["ADDR1"];
        $data["SEND_ADDR2"][TEXT]        = $fields["ADDR2"];
        $data["SEND_TELNO"][TEXT]        = $fields["TELNO"];
        $data["SEND_TELNO2"][TEXT]       = $fields["TELNO2"];
        $data["SEND_ADDR_FLG"][TEXT]     = $fields["ADDR_FLG"];
        $data["SEND_AREACD"][TEXT]       = $fields["AREACD"];
        $data["SEND_JOBCD"][TEXT]        = $fields["JOBCD"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";
        
        $where  = " WHERE SCHREGNO   = '" .$model->schregno. "'";
        $where .= "   AND DIV        = '1' ";
        
        if ($countdata != "0") {
            $db->query(Query::updateSQL($data, "SCHREG_SEND_ADDRESS_DAT", $where));
        } else {
            $db->query(Query::insertSQL($data, "SCHREG_SEND_ADDRESS_DAT"));
        }
        Query::dbCheckIn($db);
        return ;
    }

    //UPDATE (SCHREG_BASE_DETAIL_MST)
    function &getUpdateBaseQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        $countBasedata = $db->getOne(knjx_rishu_kojinQuery::getSchregBaseDetailMst($model->schregno, '004'));
        if ($countBasedata == 0) {
            $data["SCHREGNO"][TEXT] = $model->schregno;
            $data["BASE_SEQ"][TEXT] = '004';
        }
        $data["BASE_REMARK1"][TEXT] = $fields["BASE_REMARK1"];
        $data["BASE_REMARK2"][TEXT] = $fields["BASE_REMARK2"];
        $data["BASE_REMARK5"][TEXT] = $fields["BASE_REMARK5"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        
        $where  = " WHERE SCHREGNO   = '" .$model->schregno. "'";
        $where .= "   AND BASE_SEQ   = '004' ";
        
        if ($countBasedata != 0) {
            $db->query(Query::updateSQL($data, "SCHREG_BASE_DETAIL_MST", $where));
        } else {
            $db->query(Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST"));
        }
        Query::dbCheckIn($db);
        return ;
    }

    //SCHREG_BASE_DETAIL_MSTデータ確認
    function getSchregBaseDetailMst($schregno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " AND BASE_SEQ = '".$seq."' ";

        return $query;
    }

    //UPDATE (SCHREG_BASE_YEAR_DETAIL_MST)
    function &getUpdateBaseYearQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        $countBaseYeardata = $db->getOne(knjx_rishu_kojinQuery::getSchregBaseYearDetailMst($model, '002'));

        $data = array();
        $data["BASE_REMARK1"][TEXT] = $fields["SCHOOLING_DATE"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        
        if ($countBaseYeardata != 0) {
            $where  = " WHERE SCHREGNO  = '" .$model->schregno. "' ";
            $where .= "   AND YEAR      = '" .$model->exp_year. "' ";
            $where .= "   AND BASE_SEQ  = '002' ";

            $db->query(Query::updateSQL($data, "SCHREG_BASE_YEAR_DETAIL_MST", $where));
        } else {
            $data["SCHREGNO"][TEXT] = $model->schregno;
            $data["YEAR"][TEXT]     = $model->exp_year;
            $data["BASE_SEQ"][TEXT] = '002';

            $db->query(Query::insertSQL($data, "SCHREG_BASE_YEAR_DETAIL_MST"));
        }

        Query::dbCheckIn($db);
        return ;
    }

    //SCHREG_BASE_YEAR_DETAIL_MSTデータ確認
    function getSchregBaseYearDetailMst($model, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_YEAR_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     BASE_SEQ    = '".$seq."' ";

        return $query;
    }
}
?>
