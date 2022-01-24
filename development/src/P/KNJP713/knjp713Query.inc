<?php

require_once('for_php7.php');

class knjp713Query extends Query {

    //年度
    function getYear() {
        $query  = " WITH CTRL_T (LABEL, VALUE) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."', '".CTRL_YEAR."'), ";
        $query .= "     ('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_MST ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CTRL_T ";
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

    //前年度からコピー時のデータ存在チェック
    function selectYearQuery($model, $year, $table)
    {
        $query  = "SELECT COUNT(*) FROM $table WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND YEAR        = '{$year}' ";

        return $query;
    }

    //コピー時のマスタチェック(前年度に登録されている項目が今年度に存在するか)
    function mstMcdCheck($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.COLLECT_LM_CD, ";
        $query .= "     T2.COLLECT_LM_CD AS MST_EXP_MCD ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT COLLECT_L_CD || COLLECT_M_CD AS COLLECT_LM_CD ";
        $query .= "        FROM V_COLLECT_GRP_DAT ";
        $query .= "       WHERE YEAR = '".($model->taisyouYear - 1)."' ";
        $query .= "             AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "             AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "             AND COLLECT_KOJIN_FLG != '1') T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT COLLECT_L_CD || COLLECT_M_CD AS COLLECT_LM_CD ";
        $query .= "        FROM COLLECT_M_MST ";
        $query .= "       WHERE YEAR = '{$model->taisyouYear}' ";
        $query .= "             AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "             AND SCHOOL_KIND = '".$model->schoolKind."') T2 ";
        $query .= " ON ";
        $query .= "         T1.COLLECT_LM_CD = T2.COLLECT_LM_CD ";

        return $query;
    }

    //入金グループリスト
    function SelectQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     GRP_M.COLLECT_GRP_CD, ";
        $query .= "     GRP_M.COLLECT_GRP_NAME, ";
        $query .= "     GRP_D.COLLECT_L_CD, ";
        $query .= "     GRP_D.COLLECT_M_CD, ";
        $query .= "     COL_M.COLLECT_M_NAME ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_MST GRP_M ";
        $query .= "     LEFT JOIN V_COLLECT_GRP_DAT GRP_D ON GRP_D.YEAR = GRP_M.YEAR ";
        $query .= "          AND GRP_D.SCHOOLCD     = GRP_M.SCHOOLCD ";
        $query .= "          AND GRP_D.SCHOOL_KIND  = GRP_M.SCHOOL_KIND ";
        $query .= "          AND GRP_M.COLLECT_GRP_CD = GRP_D.COLLECT_GRP_CD ";
        $query .= "     LEFT JOIN COLLECT_M_MST COL_M ON GRP_M.YEAR = COL_M.YEAR ";
        $query .= "          AND GRP_D.SCHOOLCD     = COL_M.SCHOOLCD ";
        $query .= "          AND GRP_D.SCHOOL_KIND  = COL_M.SCHOOL_KIND ";
        $query .= "          AND GRP_D.COLLECT_L_CD = COL_M.COLLECT_L_CD ";
        $query .= "          AND GRP_D.COLLECT_M_CD = COL_M.COLLECT_M_CD ";
        $query .= " WHERE ";
        $query .= "     GRP_M.YEAR = '{$model->taisyouYear}' ";
        $query .= "     AND GRP_M.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND GRP_M.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND VALUE(GRP_D.COLLECT_KOJIN_FLG, '0') != '1' ";
        $query .= " ORDER BY ";
        $query .= "     COLLECT_GRP_CD, ";
        $query .= "     COLLECT_L_CD, ";
        $query .= "     COLLECT_M_CD ";
        return $query;
    }

    //１レコード取得
    function getRow($model, $flg)
    {
        $query  = " SELECT ";
        $query .= "     COLLECT_GRP_CD, ";
        $query .= "     COLLECT_GRP_NAME, ";
        $query .= "     COLLECT_KOJIN_FLG ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_MST ";
        $query .= " WHERE YEAR = '{$model->taisyouYear}'";
        $query .= "   AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "   AND SCHOOL_KIND = '".$model->schoolKind."' ";
        if ($flg == 1) {
            $query .= "     AND COLLECT_GRP_CD = '{$model->exp_grpcd}'";
        } else {
            $query .= "   AND COLLECT_GRP_CD = '".sprintf("%04d", $model->field["COLLECT_GRP_CD"])."'";
        }

        return $query;
    }

    //グループデータの存在チェック
    function getGrpData($year, $model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND COLLECT_GRP_CD = '{$model->exp_grpcd}' ";
        return $query;
    }

    //入金項目割当一覧(左リスト:リロード時)
    function ReloadSelectMcd($model)
    {
        $query  = " SELECT ";
        $query .= "   COLLECT_L_CD, ";
        $query .= "   COLLECT_M_CD, ";
        $query .= "   COLLECT_M_NAME ";
        $query .= " FROM ";
        $query .= "   COLLECT_M_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->taisyouYear}' ";
        $query .= "     AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND COLLECT_L_CD || COLLECT_M_CD IN ('".str_replace(",","','",$model->field["COLLECT_M_CD"])."') ";
        $query .= " ORDER BY ";
        $query .= "   COLLECT_L_CD, ";
        $query .= "   COLLECT_M_CD ";
        return $query;
    }

    //入金項目割当一覧(左リスト)
    function GetSelectMcd($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "   T2.COLLECT_L_CD, ";
        $query .= "   T2.COLLECT_M_CD, ";
        $query .= "   T2.COLLECT_M_NAME ";
        $query .= " FROM ";
        $query .= "   V_COLLECT_GRP_DAT T1, ";
        $query .= "   COLLECT_M_MST T2 ";
        $query .= " WHERE ";
        $query .= "   T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "   T1.SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "   T1.YEAR = '{$model->taisyouYear}' AND ";
        $query .= "   T1.COLLECT_GRP_CD = '{$model->exp_grpcd}' AND ";
        $query .= "   T1.COLLECT_KOJIN_FLG != '1' AND ";
        $query .= "   T1.SCHOOLCD    = T2.SCHOOLCD AND ";
        $query .= "   T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
        $query .= "   T1.YEAR = T2.YEAR AND ";
        $query .= "   T1.COLLECT_L_CD = T2.COLLECT_L_CD AND ";
        $query .= "   T1.COLLECT_M_CD = T2.COLLECT_M_CD ";
        $query .= " ORDER BY ";
        $query .= "   COLLECT_L_CD, ";
        $query .= "   T2.COLLECT_M_CD ";
        return $query;
    }

    //入金項目一覧(右リスト)
    function GetMcd($model, $exp_mcd)
    {
        $query  = " SELECT ";
        $query .= "   COLLECT_L_CD, ";
        $query .= "   COLLECT_M_CD, ";
        $query .= "   COLLECT_M_NAME ";
        $query .= " FROM ";
        $query .= "   COLLECT_M_MST ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "   SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "   YEAR = '{$model->taisyouYear}' AND ";
        $query .= "   COLLECT_L_CD || COLLECT_M_CD NOT IN ('".implode("','",$exp_mcd)."') ";
        $query .= " ORDER BY ";
        $query .= "   COLLECT_L_CD, ";
        $query .= "   COLLECT_M_CD ";
        return $query;
    }

    //入金項目別入金計画データ（基本設定取得）
    function getMonth0000dat($model, $l_mcd) {
        $query  = " SELECT ";
        $query .= "     MONTH_CNT ";
        for ($month=1; $month <= 12; $month++) {
            $query .= "     ,COLLECT_MONTH_{$month} ";
        }
        $query .= " FROM ";
        $query .= "   COLLECT_MONTH_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND    = '".$model->schoolKind."' ";
        $query .= "     AND YEAR           = '{$model->taisyouYear}' ";
        $query .= "     AND COLLECT_GRP_CD = '0000' ";
        $query .= "     AND COLLECT_L_CD || COLLECT_M_CD = '{$l_mcd}' ";

        return $query;
    }

    //削除
    function &getDeleteQuery($model,&$db)
    {
        //入金グループマスタ
        $query  = " DELETE FROM COLLECT_GRP_MST ";
        $query .= "  WHERE YEAR = '{$model->taisyouYear}'";
        $query .= "    AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "    AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "    AND COLLECT_GRP_CD  = '".sprintf("%04d", $model->field["COLLECT_GRP_CD"])."'";
        $db->query($query);

        //入金グループ入金項目データ
        $query  = " DELETE FROM COLLECT_GRP_DAT ";
        $query .= "  WHERE YEAR = '{$model->taisyouYear}'";
        $query .= "    AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "    AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "    AND COLLECT_GRP_CD  = '".sprintf("%04d",$model->field["COLLECT_GRP_CD"])."'";
        $db->query($query);

        //入金項目別入金計画データ
        $query  = " DELETE FROM COLLECT_MONTH_GRP_DAT ";
        $query .= "  WHERE YEAR           = '{$model->taisyouYear}'";
        $query .= "    AND SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "    AND SCHOOL_KIND    = '".$model->schoolKind."' ";
        $query .= "    AND COLLECT_GRP_CD = '".sprintf("%04d",$model->field["COLLECT_GRP_CD"])."'";
        $db->query($query);

        return;
    }

    //追加・更新
    function &getUpdateQuery($model,&$db)
    {
        //入金グループマスタ
        $data = array();
        $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]          = $model->schoolKind;
        $data["YEAR"][TEXT]                 = $model->taisyouYear;
        $data["COLLECT_GRP_CD"][TEXT]       = sprintf("%04d", $model->field["COLLECT_GRP_CD"]);
        $data["COLLECT_GRP_NAME"][TEXT]     = $model->field["COLLECT_GRP_NAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_GRP_MST");
        $db->query($query);

        //入金項目
        if ($model->field["COLLECT_M_CD"]) {
            $expmcd = explode(",", $model->field["COLLECT_M_CD"]);
            foreach ($expmcd as $val) {
                //入金グループ入金項目データ
                $data = array();
                $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
                $data["YEAR"][TEXT]             = $model->taisyouYear;
                $data["COLLECT_GRP_CD"][TEXT]   = sprintf("%04d", $model->field["COLLECT_GRP_CD"]);
                $data["COLLECT_L_CD"][TEXT]     = substr($val, 0, 2);
                $data["COLLECT_M_CD"][TEXT]     = substr($val, 2, 2);
                $data["COLLECT_S_CD"][TEXT]     = "00";
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "COLLECT_GRP_DAT");
                $db->query($query);

                //入金項目別入金計画データ
                $monthArr = array();
                $monthArr = $db->getRow(knjp713Query::getMonth0000dat($model, $val), DB_FETCHMODE_ASSOC);
                $data = array();
                $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
                $data["YEAR"][TEXT]             = $model->taisyouYear;
                $data["COLLECT_GRP_CD"][TEXT]   = sprintf("%04d", $model->field["COLLECT_GRP_CD"]);
                $data["COLLECT_L_CD"][TEXT]     = substr($val, 0, 2);
                $data["COLLECT_M_CD"][TEXT]     = substr($val, 2, 2);
                $data["MONTH_CNT"][NUMBER]      = $monthArr["MONTH_CNT"];
                for ($month=1; $month <= 12; $month++) {
                    $data["COLLECT_MONTH_".$month][TEXT]      = $monthArr["COLLECT_MONTH_".$month];
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "COLLECT_MONTH_GRP_DAT");
                $db->query($query);
            }
        }

        return;
    }

    //前年度からデータをコピー
    function &getCopyYearQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $setYear = $model->taisyouYear;

        //入金グループマスタ
        $query  = " DELETE FROM COLLECT_GRP_MST ";
        $query .= "  WHERE YEAR = '{$setYear}'";
        $query .= "        AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "        AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "        AND VALUE(COLLECT_KOJIN_FLG, '0') != '1' ";
        $db->query($query);

        $query  = "INSERT INTO COLLECT_GRP_MST(";
        $query .= " SCHOOLCD,";
        $query .= " SCHOOL_KIND,";
        $query .= " YEAR,";
        $query .= " COLLECT_GRP_CD,";
        $query .= " COLLECT_GRP_NAME,";
        $query .= " REGISTERCD,";
        $query .= " UPDATED)";
        $query .= " SELECT";
        $query .= " SCHOOLCD,";
        $query .= " SCHOOL_KIND,";
        $query .= " '{$setYear}',";
        $query .= " COLLECT_GRP_CD,";
        $query .= " COLLECT_GRP_NAME,";
        $query .= " '" .STAFFCD. "', sysdate()";
        $query .= " FROM COLLECT_GRP_MST";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . ($setYear - 1) . "' ";
        $query .= "     AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND VALUE(COLLECT_KOJIN_FLG, '0') != '1' ";
        $db->query($query);

        //入金グループデータ
        $query  = " DELETE FROM COLLECT_GRP_DAT T1 ";
        $query .= "  WHERE T1.YEAR = '{$setYear}'";
        $query .= "        AND T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "        AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "        AND EXISTS(SELECT ";
        $query .= "                        'x' ";
        $query .= "                   FROM ";
        $query .= "                        COLLECT_GRP_MST T2 ";
        $query .= "                   WHERE ";
        $query .= "                        T2.YEAR = T1.YEAR ";
        $query .= "                        AND T2.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                        AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                        AND T2.COLLECT_GRP_CD = T1.COLLECT_GRP_CD ";
        $query .= "                        AND VALUE(T2.COLLECT_KOJIN_FLG, '0') = '0' ";
        $query .= "                  ) ";
        $db->query($query);

        $query  = "INSERT INTO COLLECT_GRP_DAT(";
        $query .= " SCHOOLCD,";
        $query .= " SCHOOL_KIND,";
        $query .= " YEAR,";
        $query .= " COLLECT_GRP_CD,";
        $query .= " COLLECT_L_CD,";
        $query .= " COLLECT_M_CD,";
        $query .= " COLLECT_S_CD,";
        $query .= " REGISTERCD,";
        $query .= " UPDATED)";
        $query .= " SELECT";
        $query .= "     SCHOOLCD,";
        $query .= "     SCHOOL_KIND,";
        $query .= "     '{$setYear}',";
        $query .= "     COLLECT_GRP_CD,";
        $query .= "     COLLECT_L_CD,";
        $query .= "     COLLECT_M_CD,";
        $query .= "     COLLECT_S_CD,";
        $query .= "     '" .STAFFCD. "', SYSDATE()";
        $query .= " FROM";
        $query .= "     V_COLLECT_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($setYear - 1)."' ";
        $query .= "     AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND COLLECT_KOJIN_FLG != '1' ";
        $db->query($query);

        //入金項目別入金計画データ
        $query  = " DELETE FROM COLLECT_MONTH_GRP_DAT ";
        $query .= "  WHERE YEAR            = '{$setYear}'";
        $query .= "    AND SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "    AND SCHOOL_KIND     = '".$model->schoolKind."' ";
        $query .= "    AND COLLECT_GRP_CD != '0000' ";
        $db->query($query);

        $query  = " INSERT INTO COLLECT_MONTH_GRP_DAT ( ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         YEAR, ";
        $query .= "         COLLECT_GRP_CD, ";
        $query .= "         COLLECT_L_CD, ";
        $query .= "         COLLECT_M_CD, ";
        $query .= "         MONTH_CNT, ";
        for ($month=1; $month <= 12; $month++) {
            $query .= "         COLLECT_MONTH_{$month}, ";
        }
        $query .= "         REGISTERCD, ";
        $query .= "         UPDATED) ";
        $query .= " SELECT ";
        $query .= "     SCHOOLCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     '{$setYear}', ";
        $query .= "     COLLECT_GRP_CD, ";
        $query .= "     COLLECT_L_CD, ";
        $query .= "     COLLECT_M_CD, ";
        $query .= "     MONTH_CNT, ";
        for ($month=1; $month <= 12; $month++) {
            $query .= "     COLLECT_MONTH_{$month}, ";
        }
        $query .= "     '" .STAFFCD. "', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM";
        $query .= "     COLLECT_MONTH_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR            = '".($setYear - 1)."' ";
        $query .= "     AND SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND     = '".$model->schoolKind."' ";
        $query .= "     AND COLLECT_GRP_CD != '0000' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

}
?>
