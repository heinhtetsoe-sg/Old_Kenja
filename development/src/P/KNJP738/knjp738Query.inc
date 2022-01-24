<?php

require_once('for_php7.php');

class knjp738Query extends Query {
    //年度取得
    function getYear($model) {
        $query  = " WITH T_ALL (LABEL, VALUE) AS ( ";
        $query .= "      VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
        $query .= "      ) ";
        $query .= " SELECT ";
        $query .= "      LABEL, ";
        $query .= "      VALUE ";
        $query .= " FROM ";
        $query .= "      T_ALL ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_CSV_GRP_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト取得
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRP_CD, ";
        $query .= "     M1.GRP_NAME, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     M2.COLLECT_M_NAME ";
        $query .= " FROM ";
        $query .= "     COLLECT_CSV_GRP_DAT T1 ";
        $query .= "     LEFT JOIN COLLECT_CSV_GRP_MST M1 ";
        $query .= "          ON T1.SCHOOLCD     = M1.SCHOOLCD ";
        $query .= "         AND T1.SCHOOL_KIND  = M1.SCHOOL_KIND ";
        $query .= "         AND T1.YEAR         = M1.YEAR ";
        $query .= "         AND T1.GRP_CD       = M1.GRP_CD ";
        $query .= "     LEFT JOIN COLLECT_M_MST M2 ";
        $query .= "          ON T1.SCHOOLCD     = M2.SCHOOLCD ";
        $query .= "         AND T1.SCHOOL_KIND  = M2.SCHOOL_KIND ";
        $query .= "         AND T1.YEAR         = M2.YEAR ";
        $query .= "         AND T1.COLLECT_L_CD = M2.COLLECT_L_CD ";
        $query .= "         AND T1.COLLECT_M_CD = M2.COLLECT_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     T1.SCHOOL_KIND  = '".$model->schoolkind."' AND ";
        $query .= "     T1.YEAR         = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRP_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $flg) {
        $query  = " SELECT ";
        $query .= "     GRP_CD, ";
        $query .= "     GRP_NAME ";
        $query .= " FROM ";
        $query .= "     COLLECT_CSV_GRP_MST ";
        $query .= " WHERE YEAR = '{$model->year}'";
        $query .= "   AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "   AND SCHOOL_KIND = '".$model->schoolkind."' ";
        if ($flg == 1) {
            $query .= "     AND GRP_CD = '{$model->grp_cd}'";
        } else {
            $query .= "   AND GRP_CD = '".sprintf("%03d", $model->field["GRP_CD"])."'";
        }

        return $query;
    }

    //入金項目一覧取得
    function getCollectM($model) {
        $query  = " SELECT ";
        $query .= "     T1.COLLECT_L_CD || ':' || T1.COLLECT_M_CD AS VALUE, ";
        $query .= "     T1.COLLECT_L_CD || T1.COLLECT_M_CD || '：' || T1.COLLECT_M_NAME AS LABEL, ";
        $query .= "     L1.GRP_CD ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST T1 ";
        $query .= "     LEFT JOIN COLLECT_CSV_GRP_DAT L1 ";
        $query .= "          ON T1.SCHOOLCD     = L1.SCHOOLCD ";
        $query .= "         AND T1.SCHOOL_KIND  = L1.SCHOOL_KIND ";
        $query .= "         AND T1.YEAR         = L1.YEAR ";
        $query .= "         AND T1.COLLECT_L_CD = L1.COLLECT_L_CD ";
        $query .= "         AND T1.COLLECT_M_CD = L1.COLLECT_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     T1.SCHOOL_KIND  = '".$model->schoolkind."' AND ";
        $query .= "     T1.YEAR         = '".$model->year."' AND ";
        $query .= "     (L1.GRP_CD = '".$model->grp_cd."' OR L1.GRP_CD IS NULL) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $selectdata = ($model->selectdata) ? explode(",", $model->selectdata) : array();

        //削除 -- COLLECT_CSV_GRP_MST
        $query  = " DELETE FROM COLLECT_CSV_GRP_MST ";
        $query .= "  WHERE YEAR        = '{$model->year}'";
        $query .= "    AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "    AND SCHOOL_KIND = '{$model->schoolkind}' ";
        $query .= "    AND GRP_CD      = '{$model->grp_cd}'";
        $db->query($query);

        //削除 -- COLLECT_CSV_GRP_DAT
        $query  = " DELETE FROM ";
        $query .= "     COLLECT_CSV_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolkind."' AND ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     GRP_CD      = '".$model->grp_cd."'";
        $db->query($query);

        if (get_count($selectdata) > 0) {
            foreach ($selectdata as $lmcd) {
                list ($lcd, $mcd) = explode(":", $lmcd);

                //更新 -- COLLECT_CSV_GRP_DAT
                $data = array();
                $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]          = $model->schoolkind;
                $data["YEAR"][TEXT]                 = $model->year;
                $data["GRP_CD"][TEXT]               = $model->field["GRP_CD"];
                $data["COLLECT_L_CD"][TEXT]         = $lcd;
                $data["COLLECT_M_CD"][TEXT]         = $mcd;
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "sysdate()";

                $query = Query::insertSQL($data, "COLLECT_CSV_GRP_DAT");
                $db->query($query);
            }

            //更新 -- COLLECT_CSV_GRP_MST
            $data = array();
            $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]          = $model->schoolkind;
            $data["YEAR"][TEXT]                 = $model->year;
            $data["GRP_CD"][TEXT]               = $model->field["GRP_CD"];
            $data["GRP_NAME"][TEXT]             = $model->field["GRP_NAME"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query = Query::insertSQL($data, "COLLECT_CSV_GRP_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //削除
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //COLLECT_CSV_GRP_MST
        $query  = " DELETE FROM COLLECT_CSV_GRP_MST ";
        $query .= "  WHERE YEAR        = '{$model->year}'";
        $query .= "    AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "    AND SCHOOL_KIND = '{$model->schoolkind}' ";
        $query .= "    AND GRP_CD      = '{$model->grp_cd}'";
        $db->query($query);

        //COLLECT_CSV_GRP_DAT
        $query  = " DELETE FROM COLLECT_CSV_GRP_DAT ";
        $query .= "  WHERE YEAR        = '{$model->year}'";
        $query .= "    AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "    AND SCHOOL_KIND = '{$model->schoolkind}' ";
        $query .= "    AND GRP_CD      = '{$model->grp_cd}'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

/**** 前年度コピー処理 ****/
    //今年度のデータが存在するかチェック
    function CheckData($model, $tableNm, $year) {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM {$tableNm} ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND YEAR        = '".$year."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO COLLECT_CSV_GRP_MST ";
        $query .= "   (SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         '".$model->year."', ";
        $query .= "         GRP_CD, ";
        $query .= "         GRP_NAME, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         COLLECT_CSV_GRP_MST ";
        $query .= "     WHERE ";
        $query .= "             SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND YEAR        = '".($model->year - 1)."' ";
        $query .= "     ) ";
        $db->query($query);

        $query  = " INSERT INTO COLLECT_CSV_GRP_DAT ";
        $query .= "   (SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         '".$model->year."', ";
        $query .= "         GRP_CD, ";
        $query .= "         COLLECT_L_CD, ";
        $query .= "         COLLECT_M_CD, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         COLLECT_CSV_GRP_DAT ";
        $query .= "     WHERE ";
        $query .= "             SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND YEAR        = '".($model->year - 1)."' ";
        $query .= "     ) ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
