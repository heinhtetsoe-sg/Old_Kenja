<?php

require_once('for_php7.php');

class knjp711query extends Query {
    //学校種別の取得
    function getNameMstA023($model) {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //収納計画グループ取得
    function getCollectGrp($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COLLECT_GRP_CD AS VALUE, ";
        $query .= "     T1.COLLECT_GRP_CD || ':' || T2.COLLECT_GRP_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_DAT T1 ";
        $query .= "     INNER JOIN COLLECT_GRP_MST T2 ";
        $query .= "          ON T2.SCHOOLCD     = T1.SCHOOLCD ";
        $query .= "         AND T2.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        $query .= "         AND T2.YEAR         = T1.YEAR ";
        $query .= "         AND T2.COLLECT_GRP_CD = T1.COLLECT_GRP_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     T1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' AND ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
        if ($model->Properties["selectCollectDiv"] == "1") {
            $query .= " AND T1.COLLECT_GRP_CD <> '0000' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧取得
    function selectQuery($model) {
        if ($model->field["COLLECT_GRP_CD"] == '0000') {
            $query  = " WITH MAIN AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.*, ";
            $query .= "         '".$model->field["COLLECT_GRP_CD"]."' AS COLLECT_GRP_CD ";
            $query .= "     FROM ";
            $query .= "         COLLECT_M_MST T1 ";
            $query .= "     WHERE ";
            $query .= "         SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' AND ";
            $query .= "         SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' AND ";
            $query .= "         YEAR         = '".CTRL_YEAR."' ";
            $query .= " ) ";
        } else {
            $query  = " WITH MAIN AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.*, ";
            $query .= "         T2.COLLECT_M_NAME, ";
            $query .= "         T2.COLLECT_M_MONEY ";
            $query .= "     FROM ";
            $query .= "         COLLECT_GRP_DAT T1 ";
            $query .= "         INNER JOIN COLLECT_M_MST T2 ";
            $query .= "              ON T2.SCHOOLCD         = T1.SCHOOLCD ";
            $query .= "             AND T2.SCHOOL_KIND      = T1.SCHOOL_KIND ";
            $query .= "             AND T2.YEAR             = T1.YEAR ";
            $query .= "             AND T2.COLLECT_L_CD     = T1.COLLECT_L_CD ";
            $query .= "             AND T2.COLLECT_M_CD     = T1.COLLECT_M_CD ";
            $query .= "     WHERE ";
            $query .= "         T1.SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."' AND ";
            $query .= "         T1.SCHOOL_KIND      = '".$model->field["SCHOOL_KIND"]."' AND ";
            $query .= "         T1.YEAR             = '".CTRL_YEAR."' AND ";
            $query .= "         T1.COLLECT_GRP_CD   = '".$model->field["COLLECT_GRP_CD"]."' ";
            $query .= " ) ";
        }

        $query .= " SELECT ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_M_NAME, ";
        $query .= "     T1.COLLECT_M_MONEY, ";
        $query .= "     L1.MONTH_CNT ";
        for ($i = 1; $i <= 12; $i++) {
            $query .= "    ,L1.COLLECT_MONTH_".$i." ";
        }
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     LEFT JOIN COLLECT_MONTH_GRP_DAT L1 ";
        $query .= "          ON L1.SCHOOLCD         = T1.SCHOOLCD ";
        $query .= "         AND L1.SCHOOL_KIND      = T1.SCHOOL_KIND ";
        $query .= "         AND L1.YEAR             = T1.YEAR ";
        $query .= "         AND L1.COLLECT_GRP_CD   = T1.COLLECT_GRP_CD ";
        $query .= "         AND L1.COLLECT_L_CD     = T1.COLLECT_L_CD ";
        $query .= "         AND L1.COLLECT_M_CD     = T1.COLLECT_M_CD ";
        $query .= " ORDER BY ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD ";

        return $query;
    }

    //更新処理
    function updateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     COLLECT_MONTH_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND     = '".$model->field["SCHOOL_KIND"]."' AND ";
        $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
        $query .= "     COLLECT_GRP_CD  = '".$model->field["COLLECT_GRP_CD"]."' ";
        $db->query($query);

        //追加
        $array = explode(",", $model->colcd);
        foreach ($array as $key => $val) {
            if (strlen($model->data["MONTH_CNT"][$val])) {
                $data = array();
                $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]      = $model->field["SCHOOL_KIND"];
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["COLLECT_GRP_CD"][TEXT]   = $model->field["COLLECT_GRP_CD"];
                $data["COLLECT_L_CD"][TEXT]     = substr($val,0,2);
                $data["COLLECT_M_CD"][TEXT]     = substr($val,2);
                $data["MONTH_CNT"][NUMBER]      = $model->data["MONTH_CNT"][$val];
                for ($i = 1; $i <= 12; $i++) {
                    $data["COLLECT_MONTH_".$i][TEXT]    = $model->data["COLLECT_MONTH_".$i][$val];
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $db->query(Query::insertSQL($data, "COLLECT_MONTH_GRP_DAT"));
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
