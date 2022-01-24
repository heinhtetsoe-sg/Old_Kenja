<?php

require_once('for_php7.php');

class knjm710_detailQuery extends Query {


    //TOP項目を取得
    function getSetRow($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.COLLECT_L_NAME ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST T1 ";
        $query .= " LEFT OUTER JOIN COLLECT_L_MST L1 ";
        $query .= "    ON ";
        $query .= "       T1.COLLECT_L_CD = L1.COLLECT_L_CD ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR           = '" .$model->year ."' ";
        $query .= "   AND T1.COLLECT_L_CD   = '" .$model->exp_lcd ."' ";
        $query .= "   AND T1.COLLECT_M_CD   = '" .$model->exp_mcd ."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);
        
        return $row;
    }

    //１レコード取得
    function getRow($model)
    {
        $query  = "  WITH NAME AS ( ";
        $query .= "  SELECT ";
        $query .= "      T1.NAMECD2 || ':' || T1.NAME1 AS NAMELABEL, ";
        $query .= "      T1.NAMECD2 AS NAMEVALUE ";
        $query .= "  FROM ";
        $query .= "      NAME_MST T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.NAMECD1 = 'M012' ";
        $query .= "  ORDER BY ";
        $query .= "      T1.NAMECD2 ";
        $query .= "  ), DATA AS( ";
        $query .= "  SELECT ";
        $query .= "      T1.TOKUSYU_CD, ";
        $query .= "      T1.TOKUSYU_VAL ";
        $query .= "  FROM ";
        $query .= "      COLLECT_M_DETAIL_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '" .$model->year ."' ";
        $query .= "  AND T1.COLLECT_L_CD = '" .$model->exp_lcd ."' ";
        $query .= "  AND T1.COLLECT_M_CD = '" .$model->exp_mcd ."' ";
        $query .= "  ORDER BY ";
        $query .= "      T1.TOKUSYU_CD ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      NAMELABEL, ";
        $query .= "      NAMEVALUE, ";
        $query .= "      TOKUSYU_CD, ";
        $query .= "      TOKUSYU_VAL ";
        $query .= "  FROM ";
        $query .= "      NAME ";
        $query .= "  LEFT JOIN DATA ON TOKUSYU_CD = NAMEVALUE ";        

        return $query;
    }
    
    //名称マスタより値を取得
    function getNameMstQuery($model, $cd)
    {
        $query .= "  SELECT ";
        $query .= "      '1' || ':' || NAME2 AS NAME2LABEL, ";
        $query .= "      '2' || ':' || NAME3 AS NAME3LABEL ";
        $query .= "  FROM ";
        $query .= "      NAME_MST ";
        $query .= "  WHERE ";
        $query .= "      NAMECD1 = 'M012' ";
        $query .= "  AND NAMECD2 = '".$cd."' ";

        return $query;
    }
        
    //COLLECT_M_DETAIL_DAT存在チェック
    function checkCollectMDetailDat($namevalue, $model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$model->year."' ";
        $query .= " AND COLLECT_L_CD    = '".$model->exp_lcd."' ";
        $query .= " AND COLLECT_M_CD    = '".$model->exp_mcd."' ";
        $query .= " AND TOKUSYU_CD      = '".$namevalue."' ";
        
        return $query;
    }
    
    //UPDATE
    function &getUpdateQuery($db, $namevalue, $tokusyu_val, $model) {

        $db->autoCommit(false);

        $cnt = $db->getOne(knjm710_detailQuery::checkCollectMDetailDat($namevalue, $model));

        $data["TOKUSYU_VAL"][TEXT]     = $tokusyu_val;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($cnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR            = '".$model->year."' ";
            $where .= " AND COLLECT_L_CD    = '".$model->exp_lcd."' ";
            $where .= " AND COLLECT_M_CD    = '".$model->exp_mcd."' ";
            $where .= " AND TOKUSYU_CD      = '".$namevalue."' ";

            $query = Query::updateSQL($data, "COLLECT_M_DETAIL_DAT", $where);
            $db->query($query);
        } else if ($cnt == "0" && $tokusyu_val != "") {
            $data["YEAR"][TEXT]          = $model->year;
            $data["COLLECT_L_CD"][TEXT]  = $model->exp_lcd;
            $data["COLLECT_M_CD"][TEXT]  = $model->exp_mcd;
            $data["TOKUSYU_CD"][TEXT]    = $namevalue;
            $data["TOKUSYU_VAL"][TEXT]   = $tokusyu_val;
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "sysdate()";

            $query = Query::insertSQL($data, "COLLECT_M_DETAIL_DAT");
            $db->query($query);
        }

        $db->commit();
        return;
    }
    
    //DELETE
    function &getDeleteQuery($db, $namevalue, $model) {

        $db->autoCommit(false);
        
        $query  = " DELETE FROM";
        $query .= "     COLLECT_M_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$model->year."' ";
        $query .= " AND COLLECT_L_CD    = '".$model->exp_lcd."' ";
        $query .= " AND COLLECT_M_CD    = '".$model->exp_mcd."' ";
        $query .= " AND TOKUSYU_CD      = '".$namevalue."' ";

        $db->query($query);
        $db->commit();
        return;
    }
}
?>
