<?php

require_once('for_php7.php');

class knjm711Query extends Query {

    //データ取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.COLLECT_L_CD || COLLECT_M_CD AS COLLECT_L_M_CD ";
        $query .= " FROM ";
        $query .= "     COLLECT_S_MST T1 ";
        $query .= " LEFT OUTER JOIN COLLECT_L_MST T2 ON T1.COLLECT_L_CD = T2.COLLECT_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."'";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_S_CD ";

        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    function getColectLMCnt($cd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_S_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR             = '". CTRL_YEAR ."' ";
        $query .= " AND     COLLECT_L_CD = '".substr($cd, 0, 2)."'";
        $query .= " AND     COLLECT_M_CD = '".substr($cd, 2, 2)."'";

        return $query;
    }


    //１レコード取得
    function getRow($model,$flg)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COLLECT_L_CD || COLLECT_M_CD AS COLLECT_L_M_CD, ";
        $query .= "     COLLECT_S_CD, ";
        $query .= "     COLLECT_S_NAME, ";
        $query .= "     COLLECT_S_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_S_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";

        if($flg == 1){
            $query .= " AND COLLECT_L_CD = '".substr($model->exp_lmcd, 0, 2)."'";
            $query .= " AND COLLECT_M_CD = '".substr($model->exp_lmcd, 2, 2)."'";
            $query .= " AND COLLECT_S_CD = '".$model->exp_scd."'";
        }elseif($flg == 2){
            $query .= " AND COLLECT_L_CD = '".substr($model->field["COLLECT_L_M_CD"], 0, 2)."'";
            $query .= " AND COLLECT_M_CD = '".substr($model->field["COLLECT_L_M_CD"], 2, 2)."'";
            $query .= " AND COLLECT_S_CD = '".sprintf("%02d",$model->field["COLLECT_S_CD"])."'";
        }

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    //会計科目・項目コード取得
    function getColectCd($year)
    {
        $query  = " SELECT ";
        $query .= "     T1.COLLECT_L_CD || ':' || T2.COLLECT_L_NAME || '+' || T1.COLLECT_M_CD || ':' || T1.COLLECT_M_NAME AS LABEL, ";
        $query .= "     T1.COLLECT_L_CD || T1.COLLECT_M_CD AS VALUE";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST T1";
        $query .= " LEFT OUTER JOIN COLLECT_L_MST T2 ON T1.COLLECT_L_CD = T2.COLLECT_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= " AND T1.COLLECT_S_EXIST_FLG = '1' ";
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
        $query .= "      COLLECT_S_DETAIL_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '" .$model->year ."' ";
        $query .= "  AND T1.COLLECT_L_CD = '".substr($model->exp_lmcd, 0, 2)."' ";
        $query .= "  AND T1.COLLECT_M_CD = '".substr($model->exp_lmcd, 2, 2)."' ";
        $query .= "  AND T1.COLLECT_S_CD = '" .$model->exp_scd ."' ";
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
        $data["COLLECT_L_CD"][TEXT]         = substr($fields["COLLECT_L_M_CD"], 0, 2);
        $data["COLLECT_M_CD"][TEXT]         = substr($fields["COLLECT_L_M_CD"], 2, 2);
        $data["COLLECT_S_CD"][TEXT]         = sprintf("%02d",$fields["COLLECT_S_CD"]);
        $data["COLLECT_S_NAME"][TEXT]       = $fields["COLLECT_S_NAME"];
        $data["COLLECT_S_MONEY"][NUMBER]    = $fields["COLLECT_S_MONEY"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_S_MST");
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

        $data["COLLECT_S_NAME"][TEXT]       = $fields["COLLECT_S_NAME"];
        $data["COLLECT_S_MONEY"][NUMBER]    = $fields["COLLECT_S_MONEY"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE year          = '".$fields["YEAR"]."'";
        $where .= "   AND COLLECT_L_CD  = '".substr($fields["COLLECT_L_M_CD"], 0, 2)."'";
        $where .= "   AND COLLECT_M_CD  = '".substr($fields["COLLECT_L_M_CD"], 2, 2)."'";
        $where .= "   AND COLLECT_S_CD  = '".sprintf("%02d",$fields["COLLECT_S_CD"])."'";
        
        $query = Query::updateSQL($data, "COLLECT_S_MST", $where);
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

        $query  = " DELETE FROM COLLECT_S_MST ";
        $query .= " WHERE YEAR          = '".$fields["YEAR"]."'";
        $query .= "   AND COLLECT_L_CD  = '".substr($fields["COLLECT_L_M_CD"], 0, 2)."'";
        $query .= "   AND COLLECT_M_CD  = '".substr($fields["COLLECT_L_M_CD"], 2, 2)."'";
        $query .= "   AND COLLECT_S_CD  = '".sprintf("%02d",$fields["COLLECT_S_CD"])."'";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    //今年度のデータが存在するかチェック
    function CheckData($year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM COLLECT_S_MST WHERE YEAR = '".$year."'";
        $row = $db->getOne($query);
        
        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO COLLECT_S_MST ";
        $query .= "           ( YEAR, ";
        $query .= "             COLLECT_L_CD, ";
        $query .= "             COLLECT_M_CD, ";
        $query .= "             COLLECT_S_CD, ";
        $query .= "             COLLECT_S_NAME, ";
        $query .= "             COLLECT_S_MONEY, ";
        $query .= "             registercd, ";
        $query .= "             updated ) ";
        $query .= "   (SELECT ";
        $query .= "         '".$model->year."', ";
        $query .= "         T1.COLLECT_L_CD, ";
        $query .= "         T1.COLLECT_M_CD, ";
        $query .= "         T1.COLLECT_S_CD, ";
        $query .= "         T1.COLLECT_S_NAME, ";
        $query .= "         T1.COLLECT_S_MONEY, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         COLLECT_S_MST T1 ";
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
