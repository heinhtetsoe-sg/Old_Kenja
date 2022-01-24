<?php

require_once('for_php7.php');

class knjp981Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
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

    //データ取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T3.LEVY_L_NAME ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= "     LEFT JOIN LEVY_L_MST T3 ON T1.SCHOOLCD = T3.SCHOOLCD  ";
        $query .= "                            AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "                            AND T1.YEAR = T3.YEAR ";
        $query .= "                            AND T1.LEVY_L_CD = T3.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "     AND T1.YEAR        = '".$model->year."'";
        if ($model->levy_in_out_div === '1' || $model->levy_in_out_div === '2') {
            $query .= " AND T1.LEVY_IN_OUT_DIV = '".$model->levy_in_out_div."'";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR,";
        $query .= "     T1.LEVY_L_CD, ";
        $query .= "     T1.LEVY_IN_OUT_DIV, ";
        $query .= "     T1.LEVY_M_CD ";

        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    function getLevyLCnt($model, $levy_l_cd, $levy_in_out_div="") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "     AND YEAR        = '". CTRL_YEAR ."' ";
        $query .= "     AND LEVY_L_CD   = '".$levy_l_cd."'";
        if ($levy_in_out_div) {
            $query .= " AND LEVY_IN_OUT_DIV = '".$levy_in_out_div."'";
        }
        return $query;
    }

    //１レコード取得
    function getRow($model, $flg)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND  = '".$model->schoolKind."'";
        $query .= "     AND T1.YEAR         = '".$model->year."' ";

        if ($flg == 1) {
            $query .= "AND  T1.LEVY_L_CD = '".$model->levy_lcd."'";
            $query .= "AND  T1.LEVY_M_CD = '".$model->levy_mcd."'";
        } elseif($flg == 2) {
            $query .= "AND  T1.LEVY_L_CD = '".$model->field["LEVY_L_CD"]."'";
            $query .= "AND  T1.LEVY_M_CD = '".sprintf("%02d",$model->field["LEVY_M_CD"])."'";
        }

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    //１レコード取得
    function countLevySMst($model)
    {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     LEVY_S_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND  = '".$model->schoolKind."'";
        $query .= "     AND YEAR         = '".$model->year."' ";
        $query .= "     AND LEVY_L_CD    = '".$model->levy_lcd."'";
        $query .= "     AND LEVY_M_CD    = '".$model->levy_mcd."'";
        return $query;
    }

    //収入・支出区分
    function getLevyInOutDiv() {

        $query  = " SELECT ";
        $query .= "     '1' AS VALUE, ";
        $query .= "     '1:' || '収入' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '2' AS VALUE, ";
        $query .= "     '2:' || '支出' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //会計科目コード取得
    function getLevyLcd($model)
    {
        $query  = "  SELECT ";
        $query .= "      LEVY_L_CD AS VALUE, ";
        $query .= "      LEVY_L_CD || ':' || LEVY_L_NAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      LEVY_L_MST ";
        $query .= "  WHERE ";
        $query .= "      SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "      AND SCHOOL_KIND  = '".$model->schoolKind."'";
        $query .= "      AND YEAR         = '".$model->year."' ";
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

    //INSERT
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHOOLCD"][TEXT]            = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]         = $model->schoolKind;
        $data["YEAR"][TEXT]                = $fields["YEAR"];
        $data["LEVY_M_CD"][TEXT]           = sprintf("%02d",$fields["LEVY_M_CD"]);
        $data["LEVY_L_CD"][TEXT]           = $fields["LEVY_L_CD"];
        $data["LEVY_IN_OUT_DIV"][TEXT]     = $fields["LEVY_IN_OUT_DIV"];
        $data["LEVY_M_NAME"][TEXT]         = $fields["LEVY_M_NAME"];
        $data["LEVY_M_ABBV"][TEXT]         = $fields["LEVY_M_ABBV"];
        $data["LEVY_S_EXIST_FLG"][TEXT]    = $fields["LEVY_S_EXIST_FLG"];
        $data["ZATU_FLG"][TEXT]            = $fields["ZATU_FLG"];
        $data["YOBI_FLG"][TEXT]            = $fields["YOBI_FLG"];
        $data["KURIKOSI_FLG"][TEXT]        = $fields["KURIKOSI_FLG"];
        $data["KURIKOSI_ALL"][TEXT]        = $fields["KURIKOSI_ALL"];
        $data["KURIKOSI_MONEY"][NUMBER]    = $fields["KURIKOSI_MONEY"];
        $data["REMARK"][TEXT]              = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $query = Query::insertSQL($data, "LEVY_M_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["LEVY_IN_OUT_DIV"][TEXT]     = $fields["LEVY_IN_OUT_DIV"];
        $data["LEVY_M_NAME"][TEXT]         = $fields["LEVY_M_NAME"];
        $data["LEVY_M_ABBV"][TEXT]         = $fields["LEVY_M_ABBV"];
        $data["LEVY_S_EXIST_FLG"][TEXT]    = $fields["LEVY_S_EXIST_FLG"];
        $data["ZATU_FLG"][TEXT]            = $fields["ZATU_FLG"];
        $data["YOBI_FLG"][TEXT]            = $fields["YOBI_FLG"];
        $data["KURIKOSI_FLG"][TEXT]        = $fields["KURIKOSI_FLG"];
        $data["KURIKOSI_ALL"][TEXT]        = $fields["KURIKOSI_ALL"];
        $data["KURIKOSI_MONEY"][NUMBER]    = $fields["KURIKOSI_MONEY"];
        $data["REMARK"][TEXT]              = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $where  = " WHERE ";
        $where .= "   SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
        $where .= "   AND YEAR          = '".$fields["YEAR"]."' ";
        $where .= "   AND LEVY_L_CD     = '".$fields["LEVY_L_CD"]."'";
        $where .= "   AND LEVY_M_CD     = '".sprintf("%02d",$fields["LEVY_M_CD"])."'";

        $query = Query::updateSQL($data, "LEVY_M_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM LEVY_M_MST ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
        $query .= "   AND YEAR          = '".$fields["YEAR"]."' ";
        $query .= "   AND LEVY_L_CD     = '".$fields["LEVY_L_CD"]."'";
        $query .= "   AND LEVY_M_CD     = '".sprintf("%02d",$fields["LEVY_M_CD"])."'";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

}
?>
