<?php

require_once('for_php7.php');
class knjp702Query extends Query {

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
        $query .= "     T1.SCHOOLCD = T3.SCHOOLCD ";
        $query .= "     AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "     AND T1.YEAR = T3.YEAR ";
        $query .= "     AND T1.COLLECT_L_CD = T3.COLLECT_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND   = '".$model->schoolKind."'";
        $query .= "     AND T1.YEAR          = '".$model->year."'";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR,";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD ";
        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    function getColectLCnt($model, $cd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND  = '".$model->schoolKind."'";
        $query .= "     AND YEAR         = '". CTRL_YEAR ."' ";
        $query .= "     AND COLLECT_L_CD = '".$cd."'";

        return $query;
    }

    //１レコード取得
    function getRow($model, $flg)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_NAME, ";
        $query .= "     T1.COLLECT_M_MONEY, ";
        $query .= "     M_DETAIL.REMARK1 AS SGL_ITEM, ";
        $query .= "     T1.KOUHI_SHIHI, ";
        $query .= "     T1.GAKUNOKIN_DIV, ";
        $query .= "     T1.REDUCTION_DIV, ";
        $query .= "     T1.IS_REDUCTION_SCHOOL, ";
        $query .= "     T1.IS_CREDITCNT, ";
        $query .= "     T1.IS_REPAY, ";
        $query .= "     T1.TEXTBOOKDIV, ";
        $query .= "     T1.SHOW_ORDER, ";
        for ($i = 1; $i <= 12; $i++) {
            $query .= "     GR_D.COLLECT_MONTH_{$i}, ";
        }
        $query .= "     T1.REMARK, ";
        $query .= "     T1.DIVIDE_PROCESS, ";
        $query .= "     T1.ROUND_DIGIT ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST T1 ";
        $query .= "     LEFT JOIN COLLECT_MONTH_GRP_DAT GR_D ON GR_D.SCHOOLCD       = T1.SCHOOLCD ";
        $query .= "                                         AND GR_D.SCHOOL_KIND    = T1.SCHOOL_KIND ";
        $query .= "                                         AND GR_D.YEAR           = T1.YEAR ";
        $query .= "                                         AND GR_D.COLLECT_GRP_CD = '0000' ";
        $query .= "                                         AND GR_D.COLLECT_L_CD   = T1.COLLECT_L_CD ";
        $query .= "                                         AND GR_D.COLLECT_M_CD   = T1.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_M_DETAIL_MST M_DETAIL ON T1.SCHOOLCD = M_DETAIL.SCHOOLCD ";
        $query .= "          AND T1.SCHOOL_KIND    = M_DETAIL.SCHOOL_KIND ";
        $query .= "          AND T1.YEAR           = M_DETAIL.YEAR ";
        $query .= "          AND T1.COLLECT_L_CD   = M_DETAIL.COLLECT_L_CD ";
        $query .= "          AND T1.COLLECT_M_CD   = M_DETAIL.COLLECT_M_CD ";
        $query .= "          AND M_DETAIL.SEQ      = '001' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND   = '".$model->schoolKind."'";
        $query .= "     AND T1.YEAR          = '".$model->year."'";

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

    //教科書区分
    function getTextBookDiv($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'M004' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学納金区分取得
    function getGakunokinDiv($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'P007' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //会計科目コード取得
    function getCollectLcd($model)
    {
        $query  = " SELECT ";
        $query .= "     COLLECT_L_CD AS VALUE, ";
        $query .= "     COLLECT_L_CD || ':' || COLLECT_L_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_L_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND  = '".$model->schoolKind."'";
        $query .= "     AND YEAR         = '".$model->year."'";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
        $query .= "     T1.SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND  = '".$model->schoolKind."'";
        $query .= "     AND T1.YEAR = '" .$model->year ."' ";
        $query .= "     AND T1.COLLECT_L_CD = '" .$model->exp_lcd ."' ";
        $query .= "     AND T1.COLLECT_M_CD = '" .$model->exp_mcd ."' ";
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

    //入金グループに登録済みのデータ取得
    function getCollectGrpDat($model, $collect_l_cd, $collect_m_cd, $collect_s_cd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND  = '".$model->schoolKind."' ";
        $query .= "     AND YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND COLLECT_L_CD = '".$collect_l_cd."' ";
        $query .= "     AND COLLECT_M_CD = '".$collect_m_cd."' ";
        $query .= "     AND COLLECT_S_CD = '".$collect_s_cd."' ";

        $countG = $db->getOne($query);
        Query::dbCheckIn($db);
        return $countG;
    }

    //INSERT
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]          = $model->schoolKind;
        $data["YEAR"][TEXT]                 = $fields["YEAR"];
        $data["COLLECT_L_CD"][TEXT]         = $fields["COLLECT_L_CD"];
        $data["COLLECT_M_CD"][TEXT]         = sprintf("%02d",$fields["COLLECT_M_CD"]);
        $data["COLLECT_M_NAME"][TEXT]       = $fields["COLLECT_M_NAME"];
        $data["COLLECT_M_MONEY"][NUMBER]    = $fields["COLLECT_M_MONEY"];
        $data["KOUHI_SHIHI"][TEXT]          = $fields["KOUHI_SHIHI"];
        $data["GAKUNOKIN_DIV"][TEXT]        = $fields["GAKUNOKIN_DIV"];
        $data["REDUCTION_DIV"][TEXT]        = $fields["REDUCTION_DIV"];
        $data["IS_REDUCTION_SCHOOL"][TEXT]  = $fields["IS_REDUCTION_SCHOOL"];
        $data["IS_CREDITCNT"][TEXT]         = $fields["IS_CREDITCNT"];
        $data["IS_REPAY"][TEXT]             = $fields["IS_REPAY"];
        $data["TEXTBOOKDIV"][TEXT]          = $fields["TEXTBOOKDIV"];
        $data["SHOW_ORDER"][TEXT]           = $fields["SHOW_ORDER"];
        $data["REMARK"][TEXT]               = $fields["REMARK"];
        $data["DIVIDE_PROCESS"][TEXT]       = $fields["DIVIDE_PROCESS"];
        $data["ROUND_DIGIT"][TEXT]          = $fields["ROUND_DIGIT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_M_MST");
        $db->query($query);

        if ($model->Properties["useSIGELsystem"] == "1") {
            //削除(COLLECT_M_DETAIL_MST)
            $query  = " DELETE FROM ";
            $query .= "     COLLECT_M_DETAIL_MST ";
            $query .= " WHERE ";
            $query .= "   SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
            $query .= "   AND YEAR          = '".$fields["YEAR"]."' ";
            $query .= "   AND COLLECT_L_CD  = '".$fields["COLLECT_L_CD"]."' ";
            $query .= "   AND COLLECT_M_CD  = '".sprintf("%02d",$fields["COLLECT_M_CD"])."' ";
            $query .= "   AND SEQ           = '001' ";
            $db->query($query);

            //追加(COLLECT_M_DETAIL_MST)
            $data = array();
            $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]  = $model->schoolKind;
            $data["YEAR"][TEXT]         = $fields["YEAR"];
            $data["COLLECT_L_CD"][TEXT] = $fields["COLLECT_L_CD"];
            $data["COLLECT_M_CD"][TEXT] = sprintf("%02d",$fields["COLLECT_M_CD"]);
            $data["SEQ"][TEXT]          = '001';
            $data["REMARK1"][TEXT]      = $fields["SGL_ITEM"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "COLLECT_M_DETAIL_MST");
            $db->query($query);
        }

        //削除(COLLECT_MONTH_GRP_DAT)
        $query  = " DELETE FROM ";
        $query .= "     COLLECT_MONTH_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD       = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHOOL_KIND    = '".$model->schoolKind."' ";
        $query .= "     AND YEAR           = '".$fields["YEAR"]."' ";
        $query .= "     AND COLLECT_GRP_CD = '0000' ";
        $query .= "     AND COLLECT_L_CD   = '".$fields["COLLECT_L_CD"]."' ";
        $query .= "     AND COLLECT_M_CD   = '".sprintf("%02d", $fields["COLLECT_M_CD"])."' ";
        $db->query($query);

        //追加(COLLECT_MONTH_GRP_DAT)
        $data = array();
        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
        $data["YEAR"][TEXT]             = $fields["YEAR"];
        $data["COLLECT_GRP_CD"][TEXT]   = "0000";
        $data["COLLECT_L_CD"][TEXT]     = $fields["COLLECT_L_CD"];
        $data["COLLECT_M_CD"][TEXT]     = sprintf("%02d", $fields["COLLECT_M_CD"]);
        $data["MONTH_CNT"][NUMBER]      = $model->monthCnt;
        for ($i = 1; $i <= 12; $i++) {
            $data["COLLECT_MONTH_".$i][TEXT]    = $model->field["COLLECT_MONTH_".$i];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $db->query(Query::insertSQL($data, "COLLECT_MONTH_GRP_DAT"));

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["COLLECT_M_NAME"][TEXT]       = $fields["COLLECT_M_NAME"];
        $data["COLLECT_M_MONEY"][NUMBER]    = $fields["COLLECT_M_MONEY"];
        $data["KOUHI_SHIHI"][TEXT]          = $fields["KOUHI_SHIHI"];
        $data["GAKUNOKIN_DIV"][TEXT]        = $fields["GAKUNOKIN_DIV"];
        $data["REDUCTION_DIV"][TEXT]        = $fields["REDUCTION_DIV"];
        $data["IS_REDUCTION_SCHOOL"][TEXT]  = $fields["IS_REDUCTION_SCHOOL"];
        $data["IS_CREDITCNT"][TEXT]         = $fields["IS_CREDITCNT"];
        $data["IS_REPAY"][TEXT]             = $fields["IS_REPAY"];
        $data["TEXTBOOKDIV"][TEXT]          = $fields["TEXTBOOKDIV"];
        $data["SHOW_ORDER"][TEXT]           = $fields["SHOW_ORDER"];
        $data["REMARK"][TEXT]               = $fields["REMARK"];
        $data["DIVIDE_PROCESS"][TEXT]       = $fields["DIVIDE_PROCESS"];
        $data["ROUND_DIGIT"][TEXT]          = $fields["ROUND_DIGIT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ";
        $where .= "   SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."'";
        $where .= "   AND SCHOOL_KIND   = '".$model->schoolKind."'";
        $where .= "   AND YEAR          = '".$fields["YEAR"]."'";
        $where .= "   AND COLLECT_L_CD  = '".$fields["COLLECT_L_CD"]."'";
        $where .= "   AND COLLECT_M_CD  = '".sprintf("%02d",$fields["COLLECT_M_CD"])."'";

        $query = Query::updateSQL($data, "COLLECT_M_MST", $where);
        $db->query($query);

        if ($model->Properties["useSIGELsystem"] == "1") {
            //削除(COLLECT_M_DETAIL_MST)
            $query  = " DELETE FROM ";
            $query .= "     COLLECT_M_DETAIL_MST ";
            $query .= " WHERE ";
            $query .= "   SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
            $query .= "   AND YEAR          = '".$fields["YEAR"]."' ";
            $query .= "   AND COLLECT_L_CD  = '".$fields["COLLECT_L_CD"]."' ";
            $query .= "   AND COLLECT_M_CD  = '".sprintf("%02d",$fields["COLLECT_M_CD"])."' ";
            $query .= "   AND SEQ           = '001' ";
            $db->query($query);

            //追加(COLLECT_M_DETAIL_MST)
            $data = array();
            $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]  = $model->schoolKind;
            $data["YEAR"][TEXT]         = $fields["YEAR"];
            $data["COLLECT_L_CD"][TEXT] = $fields["COLLECT_L_CD"];
            $data["COLLECT_M_CD"][TEXT] = sprintf("%02d",$fields["COLLECT_M_CD"]);
            $data["SEQ"][TEXT]          = '001';
            $data["REMARK1"][TEXT]      = $fields["SGL_ITEM"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "COLLECT_M_DETAIL_MST");
            $db->query($query);
        }

        //削除(COLLECT_MONTH_GRP_DAT)
        $query  = " DELETE FROM ";
        $query .= "     COLLECT_MONTH_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD       = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHOOL_KIND    = '".$model->schoolKind."' ";
        $query .= "     AND YEAR           = '".$fields["YEAR"]."' ";
        $query .= "     AND COLLECT_GRP_CD = '0000' ";
        $query .= "     AND COLLECT_L_CD   = '".$fields["COLLECT_L_CD"]."' ";
        $query .= "     AND COLLECT_M_CD   = '".sprintf("%02d", $fields["COLLECT_M_CD"])."' ";
        $db->query($query);

        //追加(COLLECT_MONTH_GRP_DAT)
        $data = array();
        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
        $data["YEAR"][TEXT]             = $fields["YEAR"];
        $data["COLLECT_GRP_CD"][TEXT]   = "0000";
        $data["COLLECT_L_CD"][TEXT]     = $fields["COLLECT_L_CD"];
        $data["COLLECT_M_CD"][TEXT]     = sprintf("%02d", $fields["COLLECT_M_CD"]);
        $data["MONTH_CNT"][NUMBER]      = $model->monthCnt;
        for ($i = 1; $i <= 12; $i++) {
            $data["COLLECT_MONTH_".$i][TEXT]    = $model->field["COLLECT_MONTH_".$i];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $db->query(Query::insertSQL($data, "COLLECT_MONTH_GRP_DAT"));

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM COLLECT_M_MST ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "   AND SCHOOL_KIND   = '".$model->schoolKind."'";
        $query .= "   AND YEAR          = '".$fields["YEAR"]."'";
        $query .= "   AND COLLECT_L_CD  = '".$fields["COLLECT_L_CD"]."'";
        $query .= "   AND COLLECT_M_CD  = '".sprintf("%02d",$fields["COLLECT_M_CD"])."'";
        $db->query($query);
        if ($model->Properties["useSIGELsystem"] == "1") {
            //削除(COLLECT_M_DETAIL_MST)
            $query  = " DELETE FROM ";
            $query .= "     COLLECT_M_DETAIL_MST ";
            $query .= " WHERE ";
            $query .= "   SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
            $query .= "   AND YEAR          = '".$fields["YEAR"]."' ";
            $query .= "   AND COLLECT_L_CD  = '".$fields["COLLECT_L_CD"]."' ";
            $query .= "   AND COLLECT_M_CD  = '".sprintf("%02d",$fields["COLLECT_M_CD"])."' ";
            $query .= "   AND SEQ           = '001' ";
            $db->query($query);
        }

        //削除(COLLECT_MONTH_GRP_DAT)
        $query  = " DELETE FROM ";
        $query .= "     COLLECT_MONTH_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD       = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHOOL_KIND    = '".$model->schoolKind."' ";
        $query .= "     AND YEAR           = '".$fields["YEAR"]."' ";
        $query .= "     AND COLLECT_GRP_CD = '0000' ";
        $query .= "     AND COLLECT_L_CD   = '".$fields["COLLECT_L_CD"]."' ";
        $query .= "     AND COLLECT_M_CD   = '".sprintf("%02d", $fields["COLLECT_M_CD"])."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    //今年度のデータが存在するかチェック(COLLECT_M_MST)
    function CheckData($model, $year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM COLLECT_M_MST ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "   AND SCHOOL_KIND   = '".$model->schoolKind."'";
        $query .= "   AND YEAR          = '".$year."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    //前年度の大分類コードが今年度に設定されているかチェック
    function CheckLcode($model, $year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' ";
        $query .= "   FROM COLLECT_L_MST T1 ";
        $query .= "  INNER JOIN COLLECT_M_MST T2 ";
        $query .= "          ON T1.SCHOOLCD = T2.SCHOOLCD ";
        $query .= "          AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "          AND T1.YEAR = T2.YEAR ";
        $query .= "          AND T1.COLLECT_L_CD = T2.COLLECT_L_CD ";
        $query .= "  WHERE ";
        $query .= "   T2.SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "   AND T2.SCHOOL_KIND   = '".$model->schoolKind."'";
        $query .= "   AND T2.YEAR          = '".$year."'";

        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

/** COLLECT_M_MST **/
        $query  = " INSERT INTO COLLECT_M_MST ";
        $query .= "           ( SCHOOLCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             YEAR, ";
        $query .= "             COLLECT_L_CD, ";
        $query .= "             COLLECT_M_CD, ";
        $query .= "             COLLECT_M_NAME, ";
        $query .= "             COLLECT_S_EXIST_FLG, ";
        $query .= "             COLLECT_M_MONEY, ";
        $query .= "             KOUHI_SHIHI, ";
        $query .= "             GAKUNOKIN_DIV, ";
        $query .= "             REDUCTION_DIV, ";
        $query .= "             IS_REDUCTION_SCHOOL, ";
        $query .= "             IS_CREDITCNT, ";
        $query .= "             IS_REPAY, ";
        $query .= "             TEXTBOOKDIV, ";
        $query .= "             SHOW_ORDER, ";
        $query .= "             REMARK, ";
        $query .= "             DIVIDE_PROCESS, ";
        $query .= "             ROUND_DIGIT, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         T1.SCHOOLCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         '".$model->year."', ";
        $query .= "         T1.COLLECT_L_CD, ";
        $query .= "         T1.COLLECT_M_CD, ";
        $query .= "         T1.COLLECT_M_NAME, ";
        $query .= "         T1.COLLECT_S_EXIST_FLG, ";
        $query .= "         T1.COLLECT_M_MONEY, ";
        $query .= "         T1.KOUHI_SHIHI, ";
        $query .= "         T1.GAKUNOKIN_DIV, ";
        $query .= "         T1.REDUCTION_DIV, ";
        $query .= "         T1.IS_REDUCTION_SCHOOL, ";
        $query .= "         T1.IS_CREDITCNT, ";
        $query .= "         T1.IS_REPAY, ";
        $query .= "         T1.TEXTBOOKDIV, ";
        $query .= "         T1.SHOW_ORDER, ";
        $query .= "         T1.REMARK, ";
        $query .= "         T1.DIVIDE_PROCESS, ";
        $query .= "         T1.ROUND_DIGIT, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         COLLECT_M_MST T1 ";
        $query .= "     INNER JOIN ";
        $query .= "         COLLECT_L_MST T2 ";
        $query .= "     ON ";
        $query .= "             T1.COLLECT_L_CD = T2.COLLECT_L_CD ";
        $query .= "         AND T1.SCHOOLCD     = T2.SCHOOLCD ";
        $query .= "         AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
        $query .= "         AND T1.YEAR         = T2.YEAR ";
        $query .= "     WHERE ";
        $query .= "             T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "         AND T1.SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "         AND T1.YEAR        = '".($model->year - 1)."'";
        $query .= "     ) ";
        $db->query($query);

/** COLLECT_M_DETAIL_MST **/
        $query  = " DELETE FROM ";
        $query .= "     COLLECT_M_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
        $query .= "   AND YEAR          = '".$fields["YEAR"]."' ";
        $db->query($query);

        $query  = " INSERT INTO COLLECT_M_DETAIL_MST ";
        $query .= "   (SELECT ";
        $query .= "         T1.SCHOOLCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         '".$model->year."', ";
        $query .= "         T1.COLLECT_L_CD, ";
        $query .= "         T1.COLLECT_M_CD, ";
        $query .= "         T1.SEQ, ";
        $query .= "         T1.INT_VAL1, ";
        $query .= "         T1.INT_VAL2, ";
        $query .= "         T1.INT_VAL3, ";
        $query .= "         T1.REMARK1, ";
        $query .= "         T1.REMARK2, ";
        $query .= "         T1.REMARK3, ";
        $query .= "         T1.REMARK4, ";
        $query .= "         T1.REMARK5, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         COLLECT_M_DETAIL_MST T1 ";
        $query .= "     WHERE ";
        $query .= "             T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "         AND T1.YEAR        = '".($model->year - 1)."' ";
        $query .= "     ) ";
        $db->query($query);

/** COLLECT_MONTH_GRP_DAT **/
        $query  = " DELETE FROM ";
        $query .= "     COLLECT_MONTH_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND    = '".$model->schoolKind."' ";
        $query .= "     AND YEAR           = '".$model->year."' ";
        $query .= "     AND COLLECT_GRP_CD = '0000' ";
        $db->query($query);

        $query  = " INSERT INTO COLLECT_MONTH_GRP_DAT ";
        $query .= "           ( SCHOOLCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             YEAR, ";
        $query .= "             COLLECT_GRP_CD, ";
        $query .= "             COLLECT_L_CD, ";
        $query .= "             COLLECT_M_CD, ";
        $query .= "             MONTH_CNT, ";
        foreach ($model->monthArray as $key => $val) {
            $query .= "             COLLECT_MONTH_{$val}, ";
        }
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         '".$model->year."', ";
        $query .= "         COLLECT_GRP_CD, ";
        $query .= "         COLLECT_L_CD, ";
        $query .= "         COLLECT_M_CD, ";
        $query .= "         MONTH_CNT, ";
        foreach ($model->monthArray as $key => $val) {
            $query .= "         COLLECT_MONTH_{$val}, ";
        }
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         COLLECT_MONTH_GRP_DAT ";
        $query .= "     WHERE ";
        $query .= "             SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND SCHOOL_KIND    = '".$model->schoolKind."' ";
        $query .= "         AND YEAR           = '".($model->year - 1)."' ";
        $query .= "         AND COLLECT_GRP_CD = '0000' ";
        $query .= "     ) ";
        $db->query($query);

/** COLLECT_M_DETAIL_DAT **/
        $query  = " DELETE FROM ";
        $query .= "     COLLECT_M_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND YEAR        = '".$model->year."' ";
        $db->query($query);

        $query  = " INSERT INTO COLLECT_M_DETAIL_DAT ";
        $query .= "           ( SCHOOLCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             YEAR, ";
        $query .= "             COLLECT_L_CD, ";
        $query .= "             COLLECT_M_CD, ";
        $query .= "             TOKUSYU_CD, ";
        $query .= "             TOKUSYU_VAL, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         '".$model->year."', ";
        $query .= "         COLLECT_L_CD, ";
        $query .= "         COLLECT_M_CD, ";
        $query .= "         TOKUSYU_CD, ";
        $query .= "         TOKUSYU_VAL, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         COLLECT_M_DETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "             SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "         AND YEAR        = '".($model->year - 1)."' ";
        $query .= "     ) ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
