<?php

require_once('for_php7.php');

class knjm710Query extends Query {

    //データ取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T3.COLLECT_L_NAME ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     COLLECT_L_MST T3 ";
        $query .= " ON ";
        $query .= "     T1.COLLECT_L_CD = T3.COLLECT_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."'";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR,";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD ";
        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    function getColectLCnt($cd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '". CTRL_YEAR ."' ";
        $query .= " AND     COLLECT_L_CD = '".$cd."'";

        return $query;
    }

    //１レコード取得
    function getRow($model,$flg)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_NAME, ";
        $query .= "     T1.COLLECT_S_EXIST_FLG, ";
        $query .= "     T1.COLLECT_M_MONEY, ";
        $query .= "     T1.PAY_DIV, ";
        $query .= "     T1.PAY_DATE, ";
        $query .= "     T1.IS_JUGYOURYOU, ";
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";

        if ($flg == 1) {
            $query .= "AND  T1.COLLECT_L_CD = '".$model->exp_lcd."'";
            $query .= "AND  T1.COLLECT_M_CD = '".$model->exp_mcd."'";
        } elseif($flg == 2) {
            $query .= "AND  T1.COLLECT_L_CD = '".$model->field["COLLECT_L_CD"]."'";
            $query .= "AND  T1.COLLECT_M_CD = '".sprintf("%02d",$model->field["COLLECT_M_CD"])."'";
        }

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    //１レコード取得
    function countCollectSMst($model)
    {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_S_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= " AND COLLECT_L_CD = '".$model->exp_lcd."'";
        $query .= " AND COLLECT_M_CD = '".$model->exp_mcd."'";

        return $query;
    }

    //会計科目コード取得
    function getCollectLcd()
    {
        $query  = "  SELECT ";
        $query .= "      COLLECT_L_CD AS VALUE, ";
        $query .= "      COLLECT_L_CD || ':' || COLLECT_L_NAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      COLLECT_L_MST ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

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

    //詳細登録設定内容を取得
    function getRowShousai($model)
    {
        $query  = "  WITH NAME AS ( ";
        $query .= "  SELECT ";
        $query .= "      T1.NAMECD2 || ':' || T1.NAME1 AS NAMELABEL, ";
        $query .= "      T1.NAMECD2 AS NAMEVALUE, ";
        $query .= "      T1.NAME2, ";
        $query .= "      T1.NAME3 ";
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
        $query .= "      TOKUSYU_VAL, ";
        $query .= "      NAME2, ";
        $query .= "      NAME3 ";
        $query .= "  FROM ";
        $query .= "      NAME ";
        $query .= "  LEFT JOIN DATA ON TOKUSYU_CD = NAMEVALUE ";        

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["YEAR"][TEXT]                 = $fields["YEAR"];
        $data["COLLECT_M_CD"][TEXT]         = sprintf("%02d",$fields["COLLECT_M_CD"]);
        $data["COLLECT_L_CD"][TEXT]         = $fields["COLLECT_L_CD"];
        $data["COLLECT_M_NAME"][TEXT]       = $fields["COLLECT_M_NAME"];
        $data["COLLECT_S_EXIST_FLG"][TEXT]  = $fields["COLLECT_S_EXIST_FLG"];
        $data["COLLECT_M_MONEY"][NUMBER]    = $fields["COLLECT_M_MONEY"];
        $data["PAY_DIV"][TEXT]              = $fields["PAY_DIV"];
        $data["PAY_DATE"][TEXT]             = str_replace("/", "-", $fields["PAY_DATE"]);
        $data["REMARK"][TEXT]               = $fields["REMARK"];
        $data["IS_JUGYOURYOU"][TEXT]        = $fields["IS_JUGYOURYOU"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_M_MST");
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

        $data["COLLECT_M_NAME"][TEXT]       = $fields["COLLECT_M_NAME"];
        $data["COLLECT_S_EXIST_FLG"][TEXT]  = $fields["COLLECT_S_EXIST_FLG"];
        $data["COLLECT_M_MONEY"][NUMBER]    = $fields["COLLECT_M_MONEY"];
        $data["PAY_DIV"][TEXT]              = $fields["PAY_DIV"];
        $data["PAY_DATE"][TEXT]             = str_replace("/", "-", $fields["PAY_DATE"]);
        $data["REMARK"][TEXT]               = $fields["REMARK"];
        $data["IS_JUGYOURYOU"][TEXT]        = $fields["IS_JUGYOURYOU"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE YEAR          = '".$fields["YEAR"]."'";
        $where .= "   AND COLLECT_L_CD  = '".$fields["COLLECT_L_CD"]."'";
        $where .= "   AND COLLECT_M_CD  = '".sprintf("%02d",$fields["COLLECT_M_CD"])."'";

        $query = Query::updateSQL($data, "COLLECT_M_MST", $where);
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

        $query  = " DELETE FROM COLLECT_M_MST ";
        $query .= " WHERE YEAR          = '".$fields["YEAR"]."'";
        $query .= "   AND COLLECT_L_CD  = '".$fields["COLLECT_L_CD"]."'";
        $query .= "   AND COLLECT_M_CD  = '".sprintf("%02d",$fields["COLLECT_M_CD"])."'";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    //今年度のデータが存在するかチェック
    function CheckData($year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM COLLECT_M_MST WHERE YEAR = '".$year."'";
        $row = $db->getOne($query);
        
        Query::dbCheckIn($db);
        return $row;
    }

    //前年度の大分類コードが今年度に設定されているかチェック
    function CheckLcode($year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' ";
        $query .= "   FROM COLLECT_L_MST T1 ";
        $query .= "  INNER JOIN COLLECT_M_MST T2 ";
        $query .= "          ON T1.COLLECT_L_CD = T2.COLLECT_L_CD ";
        $query .= "  WHERE T2.YEAR = '".($year - 1)."'";
        
        $row = $db->getOne($query);
        
        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO COLLECT_M_MST ";
        $query .= "           ( YEAR, ";
        $query .= "             COLLECT_M_CD, ";
        $query .= "             COLLECT_L_CD, ";
        $query .= "             COLLECT_M_NAME, ";
        $query .= "             COLLECT_S_EXIST_FLG, ";
        $query .= "             COLLECT_M_MONEY, ";
        $query .= "             PAY_DIV, ";
        $query .= "             PAY_DATE, ";
        $query .= "             IS_JUGYOURYOU, ";
        $query .= "             REMARK, ";
        $query .= "             registercd, ";
        $query .= "             updated ) ";
        $query .= "   (SELECT ";
        $query .= "         '".$model->year."', ";
        $query .= "         T1.COLLECT_M_CD, ";
        $query .= "         T1.COLLECT_L_CD, ";
        $query .= "         T1.COLLECT_M_NAME, ";
        $query .= "         T1.COLLECT_S_EXIST_FLG, ";
        $query .= "         T1.COLLECT_M_MONEY, ";
        $query .= "         T1.PAY_DIV, ";
        $query .= "         T1.PAY_DATE, ";
        $query .= "         T1.IS_JUGYOURYOU, ";
        $query .= "         T1.REMARK, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         COLLECT_M_MST T1 ";
        $query .= "     INNER JOIN ";
        $query .= "         COLLECT_L_MST T2 ";
        $query .= "     ON ";
        $query .= "         T1.COLLECT_L_CD = T2.COLLECT_L_CD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".($model->year - 1)."'";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
