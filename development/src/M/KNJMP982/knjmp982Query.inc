<?php

require_once('for_php7.php');

class knjmp982Query extends Query {

    //データ取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD AS LEVY_L_M_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_S_MST T1 ";
        $query .= "     LEFT JOIN LEVY_L_MST T2 ON T1.YEAR = T2.YEAR  ";
        $query .= "                            AND T1.LEVY_L_CD = T2.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."'";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.LEVY_L_CD, ";
        $query .= "     T1.LEVY_M_CD, ";
        $query .= "     T1.LEVY_S_CD ";

        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    function getLevyLMCnt($cd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     LEVY_S_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR             = '". CTRL_YEAR ."' ";
        $query .= " AND     LEVY_L_CD = '".substr($cd, 0, 2)."'";
        $query .= " AND     LEVY_M_CD = '".substr($cd, 2, 2)."'";

        return $query;
    }


    //１レコード取得
    function getRow($model,$flg)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD AS LEVY_L_M_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_S_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";

        if($flg == 1){
            $query .= " AND T1.LEVY_L_CD = '".substr($model->levy_lmcd, 0, 2)."'";
            $query .= " AND T1.LEVY_M_CD = '".substr($model->levy_lmcd, 2, 2)."'";
            $query .= " AND T1.LEVY_S_CD = '".$model->levy_scd."'";
        }elseif($flg == 2){
            $query .= " AND T1.LEVY_L_CD = '".substr($model->field["LEVY_L_M_CD"], 0, 2)."'";
            $query .= " AND T1.LEVY_M_CD = '".substr($model->field["LEVY_L_M_CD"], 2, 2)."'";
            $query .= " AND T1.LEVY_S_CD = '".sprintf("%02d",$model->field["LEVY_S_CD"])."'";
        }

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    //会計科目・項目コード取得
    function getLevyCd($year)
    {
        $query  = " SELECT ";
        $query .= "     T1.LEVY_L_CD || ':' || T2.LEVY_L_NAME || '+' || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD AS VALUE";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1";
        $query .= "     LEFT JOIN LEVY_L_MST T2 ON T1.YEAR = T2.YEAR  ";
        $query .= "                            AND T1.LEVY_L_CD = T2.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= " AND T1.LEVY_S_EXIST_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE     ";
        
        return $query;
    }

    //名称マスタ取得
    function getName($year, $cd)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " AND NAMECD1 = '".$cd."' ";
        
        return $query;
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["YEAR"][TEXT]              = $fields["YEAR"];
        $data["LEVY_L_CD"][TEXT]         = substr($fields["LEVY_L_M_CD"], 0, 2);
        $data["LEVY_M_CD"][TEXT]         = substr($fields["LEVY_L_M_CD"], 2, 2);
        $data["LEVY_S_CD"][TEXT]         = sprintf("%02d",$fields["LEVY_S_CD"]);
        $data["LEVY_S_NAME"][TEXT]       = $fields["LEVY_S_NAME"];
        $data["LEVY_S_ABBV"][TEXT]       = $fields["LEVY_S_ABBV"];
        $data["REPAY_DIV"][TEXT]         = $fields["REPAY_DIV"];
        $data["REMARK"][TEXT]            = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "LEVY_S_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["LEVY_S_NAME"][TEXT]       = $fields["LEVY_S_NAME"];
        $data["LEVY_S_ABBV"][TEXT]       = $fields["LEVY_S_ABBV"];
        $data["REPAY_DIV"][TEXT]         = $fields["REPAY_DIV"];
        $data["REMARK"][TEXT]            = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";

        $where  = " WHERE year          = '".$fields["YEAR"]."'";
        $where .= "   AND LEVY_L_CD  = '".substr($fields["LEVY_L_M_CD"], 0, 2)."'";
        $where .= "   AND LEVY_M_CD  = '".substr($fields["LEVY_L_M_CD"], 2, 2)."'";
        $where .= "   AND LEVY_S_CD  = '".sprintf("%02d",$fields["LEVY_S_CD"])."'";
        
        $query = Query::updateSQL($data, "LEVY_S_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM LEVY_S_MST ";
        $query .= " WHERE YEAR          = '".$fields["YEAR"]."'";
        $query .= "   AND LEVY_L_CD  = '".substr($fields["LEVY_L_M_CD"], 0, 2)."'";
        $query .= "   AND LEVY_M_CD  = '".substr($fields["LEVY_L_M_CD"], 2, 2)."'";
        $query .= "   AND LEVY_S_CD  = '".sprintf("%02d",$fields["LEVY_S_CD"])."'";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    //今年度のデータが存在するかチェック
    function CheckData($year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM LEVY_S_MST WHERE YEAR = '".$year."'";
        $row = $db->getOne($query);
        
        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO LEVY_S_MST ";
        $query .= "           ( YEAR, ";
        $query .= "             LEVY_L_CD, ";
        $query .= "             LEVY_M_CD, ";
        $query .= "             LEVY_S_CD, ";
        $query .= "             LEVY_S_NAME, ";
        $query .= "             LEVY_S_ABBV, ";
        $query .= "             REPAY_DIV, ";
        $query .= "             REMARK, ";
        $query .= "             registercd, ";
        $query .= "             updated ) ";
        $query .= "   (SELECT ";
        $query .= "         '".$model->year."', ";
        $query .= "         T1.LEVY_L_CD, ";
        $query .= "         T1.LEVY_M_CD, ";
        $query .= "         T1.LEVY_S_CD, ";
        $query .= "         T1.LEVY_S_NAME, ";
        $query .= "         T1.LEVY_S_ABBV, ";
        $query .= "         T1.REPAY_DIV, ";
        $query .= "         T1.REMARK, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         LEVY_S_MST T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.year = '".($model->year - 1)."'";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
