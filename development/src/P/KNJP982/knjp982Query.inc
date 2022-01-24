<?php

require_once('for_php7.php');

class knjp982Query extends Query
{

    //校種取得
    public function getSchkind($model)
    {
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
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //データ取得
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD AS LEVY_L_M_CD ";
        if ($model->Properties["useSIGELsystem"] == "1") {
            $query .= "     ,SGLM.KIND_CD ";
            $query .= "     ,SGLM.KIND_NAME ";
            $query .= "     ,SGLM.DEPOSIT_CD ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_S_MST T1 ";
        $query .= "     LEFT JOIN LEVY_L_MST T2 ON T1.SCHOOLCD    = T2.SCHOOLCD  ";
        $query .= "                            AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "                            AND T1.YEAR        = T2.YEAR ";
        $query .= "                            AND T1.LEVY_L_CD   = T2.LEVY_L_CD ";
        if ($model->Properties["useSIGELsystem"] == "1") {
            $query .= "     LEFT JOIN LEVY_SGL_LMS_MAPPING_MST SGLM ON SGLM.SCHOOLCD    = T1.SCHOOLCD ";
            $query .= "                                            AND SGLM.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND SGLM.YEAR        = T1.YEAR ";
            $query .= "                                            AND SGLM.LEVY_L_CD   = T1.LEVY_L_CD ";
            $query .= "                                            AND SGLM.LEVY_M_CD   = T1.LEVY_M_CD ";
            $query .= "                                            AND SGLM.LEVY_S_CD   = T1.LEVY_S_CD ";
        }
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "     AND T1.YEAR        = '".$model->year."'";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.LEVY_L_CD, ";
        $query .= "     T1.LEVY_M_CD, ";
        $query .= "     T1.LEVY_S_CD ";

        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    public function getLevyLMCnt($cd, $model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     LEVY_S_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= " AND YEAR        = '". CTRL_YEAR ."' ";
        $query .= " AND LEVY_L_CD   = '".substr($cd, 0, 2)."'";
        $query .= " AND LEVY_M_CD   = '".substr($cd, 2, 2)."'";

        return $query;
    }


    //１レコード取得
    public function getRow($model, $flg)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD AS LEVY_L_M_CD ";
        if ($model->Properties["useSIGELsystem"] == "1") {
            $query .= "     ,SGLM.KIND_CD ";
            $query .= "     ,SGLM.KIND_NAME ";
            $query .= "     ,SGLM.DEPOSIT_CD ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_S_MST T1 ";
        if ($model->Properties["useSIGELsystem"] == "1") {
            $query .= "     LEFT JOIN LEVY_SGL_LMS_MAPPING_MST SGLM ON SGLM.SCHOOLCD    = T1.SCHOOLCD ";
            $query .= "                                            AND SGLM.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND SGLM.YEAR        = T1.YEAR ";
            $query .= "                                            AND SGLM.LEVY_L_CD   = T1.LEVY_L_CD ";
            $query .= "                                            AND SGLM.LEVY_M_CD   = T1.LEVY_M_CD ";
            $query .= "                                            AND SGLM.LEVY_S_CD   = T1.LEVY_S_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= " AND T1.YEAR        = '".$model->year."' ";

        if ($flg == 1) {
            $query .= " AND T1.LEVY_L_CD = '".substr($model->levy_lmcd, 0, 2)."'";
            $query .= " AND T1.LEVY_M_CD = '".substr($model->levy_lmcd, 2, 2)."'";
            $query .= " AND T1.LEVY_S_CD = '".$model->levy_scd."'";
        } elseif ($flg == 2) {
            $query .= " AND T1.LEVY_L_CD = '".substr($model->field["LEVY_L_M_CD"], 0, 2)."'";
            $query .= " AND T1.LEVY_M_CD = '".substr($model->field["LEVY_L_M_CD"], 2, 2)."'";
            $query .= " AND T1.LEVY_S_CD = '".sprintf("%03d", $model->field["LEVY_S_CD"])."'";
        }

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    //会計科目・項目コード取得
    public function getLevyCd($year, $model)
    {
        $query  = " SELECT ";
        $query .= "     T1.LEVY_L_CD || ':' || T2.LEVY_L_NAME || '+' || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD AS VALUE";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1";
        $query .= "     LEFT JOIN LEVY_L_MST T2 ON T1.SCHOOLCD    = T2.SCHOOLCD  ";
        $query .= "                            AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "                            AND T1.YEAR        = T2.YEAR ";
        $query .= "                            AND T1.LEVY_L_CD   = T2.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= " AND T1.YEAR        = '".$year."' ";
        $query .= " AND T1.LEVY_S_EXIST_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE     ";
        
        return $query;
    }

    //名称マスタ取得
    public function getName($year, $cd)
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
    public function &getInsertQuery($fields, $model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHOOLCD"][TEXT]          = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]       = $model->schoolKind;
        $data["YEAR"][TEXT]              = $fields["YEAR"];
        $data["LEVY_L_CD"][TEXT]         = substr($fields["LEVY_L_M_CD"], 0, 2);
        $data["LEVY_M_CD"][TEXT]         = substr($fields["LEVY_L_M_CD"], 2, 2);
        $data["LEVY_S_CD"][TEXT]         = sprintf("%03d", $fields["LEVY_S_CD"]);
        $data["LEVY_S_NAME"][TEXT]       = $fields["LEVY_S_NAME"];
        $data["LEVY_S_ABBV"][TEXT]       = $fields["LEVY_S_ABBV"];
        $data["REPAY_DIV"][TEXT]         = $fields["REPAY_DIV"];
        if ($model->Properties["useBenefit"] == "1") {
            $data["BENEFIT"][TEXT]           = $fields["BENEFIT"];
            $data["MAX_BENEFIT"][NUMBER]     = $fields["MAX_BENEFIT"];
        }
        $data["REMARK"][TEXT]            = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "LEVY_S_MST");
        $db->query($query);

        //SIGELシステム使用するか
        if ($model->Properties["useSIGELsystem"] == "1") {
            knjp982Query::getUpdateSIGELQuery($db, $fields, $model);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($fields, $model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["LEVY_S_NAME"][TEXT]       = $fields["LEVY_S_NAME"];
        $data["LEVY_S_ABBV"][TEXT]       = $fields["LEVY_S_ABBV"];
        $data["REPAY_DIV"][TEXT]         = $fields["REPAY_DIV"];
        if ($model->Properties["useBenefit"] == "1") {
            $data["BENEFIT"][TEXT]           = $fields["BENEFIT"];
            $data["MAX_BENEFIT"][NUMBER]     = $fields["MAX_BENEFIT"];
        }
        $data["REMARK"][TEXT]            = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";

        $where  = " WHERE ";
        $where .= "       SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "   AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $where .= "   AND YEAR        = '".$fields["YEAR"]."' ";
        $where .= "   AND LEVY_L_CD   = '".substr($fields["LEVY_L_M_CD"], 0, 2)."'";
        $where .= "   AND LEVY_M_CD   = '".substr($fields["LEVY_L_M_CD"], 2, 2)."'";
        $where .= "   AND LEVY_S_CD   = '".sprintf("%03d", $fields["LEVY_S_CD"])."'";
        
        $query = Query::updateSQL($data, "LEVY_S_MST", $where);
        $db->query($query);

        //SIGELシステム使用するか
        if ($model->Properties["useSIGELsystem"] == "1") {
            knjp982Query::getUpdateSIGELQuery($db, $fields, $model);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($fields, $model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM LEVY_S_MST ";
        $query .= " WHERE SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "   AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "   AND YEAR        = '".$fields["YEAR"]."' ";
        $query .= "   AND LEVY_L_CD   = '".substr($fields["LEVY_L_M_CD"], 0, 2)."'";
        $query .= "   AND LEVY_M_CD   = '".substr($fields["LEVY_L_M_CD"], 2, 2)."'";
        $query .= "   AND LEVY_S_CD   = '".sprintf("%03d", $fields["LEVY_S_CD"])."'";

        //SIGELシステム使用するか
        if ($model->Properties["useSIGELsystem"] == "1") {
            $query  = " DELETE FROM LEVY_SGL_LMS_MAPPING_MST ";
            $query .= " WHERE SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "   AND SCHOOL_KIND = '".$model->schoolKind."' ";
            $query .= "   AND YEAR        = '".$fields["YEAR"]."' ";
            $query .= "   AND LEVY_L_CD   = '".substr($fields["LEVY_L_M_CD"], 0, 2)."' ";
            $query .= "   AND LEVY_M_CD   = '".substr($fields["LEVY_L_M_CD"], 2, 2)."' ";
            $query .= "   AND LEVY_S_CD   = '".sprintf("%03d", $fields["LEVY_S_CD"])."' ";
        }

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    //SIGELシステムUPDATE
    public function getUpdateSIGELQuery($db, $fields, $model)
    {
        //カウント取得
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     LEVY_SGL_LMS_MAPPING_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '".$fields["YEAR"]."' ";
        $query .= "     AND LEVY_L_CD   = '".substr($fields["LEVY_L_M_CD"], 0, 2)."' ";
        $query .= "     AND LEVY_M_CD   = '".substr($fields["LEVY_L_M_CD"], 2, 2)."' ";
        $query .= "     AND LEVY_S_CD   = '".sprintf("%03d", $fields["LEVY_S_CD"])."' ";
        $cnt = $db->getOne($query);

        $data = array();
        $data["KIND_CD"][TEXT]              = $fields["KIND_CD"];
        $data["KIND_NAME"][TEXT]            = $fields["KIND_NAME"];
        $data["DEPOSIT_CD"][TEXT]           = $fields["DEPOSIT_CD"];
        $data["DEPOSIT_NAME"][TEXT]         = $fields["LEVY_S_NAME"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if ($cnt > 0) {
            $where  = " WHERE ";
            $where .= "       SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "   AND SCHOOL_KIND = '".$model->schoolKind."' ";
            $where .= "   AND YEAR        = '".$fields["YEAR"]."' ";
            $where .= "   AND LEVY_L_CD   = '".substr($fields["LEVY_L_M_CD"], 0, 2)."' ";
            $where .= "   AND LEVY_M_CD   = '".substr($fields["LEVY_L_M_CD"], 2, 2)."' ";
            $where .= "   AND LEVY_S_CD   = '".sprintf("%03d", $fields["LEVY_S_CD"])."' ";

            $query = Query::updateSQL($data, "LEVY_SGL_LMS_MAPPING_MST", $where);
        } else {
            $data["SCHOOLCD"][TEXT]          = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]       = $model->schoolKind;
            $data["YEAR"][TEXT]              = $fields["YEAR"];
            $data["LEVY_L_CD"][TEXT]         = substr($fields["LEVY_L_M_CD"], 0, 2);
            $data["LEVY_M_CD"][TEXT]         = substr($fields["LEVY_L_M_CD"], 2, 2);
            $data["LEVY_S_CD"][TEXT]         = sprintf("%03d", $fields["LEVY_S_CD"]);

            $query = Query::insertSQL($data, "LEVY_SGL_LMS_MAPPING_MST");
        }
        $db->query($query);

        return;
    }
}
