<?php

require_once('for_php7.php');

class knjp916Query extends Query {
    /********/
    /* 画面 */
    /********/
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->year}' ";
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

    //振替端数項目指定
    function getHasuuLMcd($model) {
        $query  = " WITH HIST_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         TO_REQUEST_NO, ";
        $query .= "         TO_LINE_NO, ";
        $query .= "         TO_OUTGO_L_CD, ";
        $query .= "         TO_OUTGO_M_CD, ";
        $query .= "         TO_OUTGO_S_CD, ";
        $query .= "         TO_REQUEST_NO || rtrim(cast(TO_LINE_NO as char(2))) || TO_OUTGO_L_CD || TO_OUTGO_M_CD || TO_OUTGO_S_CD as REQ_LINE_NO ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_HASUU_MOVE_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     GROUP BY ";
        $query .= "         TO_REQUEST_NO, ";
        $query .= "         TO_LINE_NO, ";
        $query .= "         TO_OUTGO_L_CD, ";
        $query .= "         TO_OUTGO_M_CD, ";
        $query .= "         TO_OUTGO_S_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MEISAI.REQUEST_NO || '-' || rtrim(cast(MEISAI.LINE_NO as char(2))) || '-' || ";
        $query .= "     MEISAI.OUTGO_L_CD || '-' || MEISAI.OUTGO_M_CD || '-' || MEISAI.OUTGO_S_CD || ':' || rtrim(cast(MEISAI.HASUU as char(5))) AS VALUE, ";
        $query .= "     case ";
        $query .= "         when HIST.REQ_LINE_NO is not null then '●' ";
        $query .= "         else '' ";
        $query .= "     end || ";
        $query .= "     value(M_MST.LEVY_M_NAME, '') || '(' || MEISAI.OUTGO_L_CD || MEISAI.OUTGO_M_CD || '-' || MEISAI.REQUEST_NO || '-' || rtrim(cast(MEISAI.LINE_NO as char(2))) || ')' AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT MEISAI ";
        $query .= "     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTGO ON OUTGO.SCHOOLCD    = MEISAI.SCHOOLCD ";
        $query .= "                                           AND OUTGO.SCHOOL_KIND = MEISAI.SCHOOL_KIND ";
        $query .= "                                           AND OUTGO.YEAR        = MEISAI.YEAR ";
        $query .= "                                           AND OUTGO.OUTGO_L_CD  = MEISAI.OUTGO_L_CD ";
        $query .= "                                           AND OUTGO.OUTGO_M_CD  = MEISAI.OUTGO_M_CD ";
        $query .= "                                           AND OUTGO.REQUEST_NO  = MEISAI.REQUEST_NO ";
        $query .= "     LEFT JOIN LEVY_M_MST M_MST ON M_MST.SCHOOLCD    = MEISAI.SCHOOLCD ";
        $query .= "                               AND M_MST.SCHOOL_KIND = MEISAI.SCHOOL_KIND ";
        $query .= "                               AND M_MST.YEAR        = MEISAI.YEAR ";
        $query .= "                               AND M_MST.LEVY_L_CD   = MEISAI.OUTGO_L_CD ";
        $query .= "                               AND M_MST.LEVY_M_CD   = MEISAI.OUTGO_M_CD ";
        $query .= "     LEFT JOIN HIST_DAT HIST ON HIST.TO_REQUEST_NO = MEISAI.REQUEST_NO ";
        $query .= "                            AND HIST.TO_LINE_NO    = MEISAI.LINE_NO ";
        $query .= "                            AND HIST.TO_OUTGO_L_CD = MEISAI.OUTGO_L_CD ";
        $query .= "                            AND HIST.TO_OUTGO_M_CD = MEISAI.OUTGO_M_CD ";
        $query .= "                            AND HIST.TO_OUTGO_S_CD = MEISAI.OUTGO_S_CD ";
        $query .= " WHERE ";
        $query .= "         MEISAI.SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND MEISAI.SCHOOL_KIND   = '{$model->schoolKind}' ";
        $query .= "     AND MEISAI.YEAR          = '{$model->year}' ";
        $query .= "     AND OUTGO.OUTGO_APPROVAL = '1' ";
        $query .= "     AND OUTGO.OUTGO_CANCEL   is null ";
        $query .= "     AND MEISAI.HASUU         > 0 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //List to List 右リスト
    function getHasuuListRight($model) {
        list($toLMcd, $hasuu) = explode(':', $model->toLMcd);
        $toLMcd = str_replace('-', '', $toLMcd);

        $query  = " WITH HIST_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         TO_REQUEST_NO, ";
        $query .= "         TO_LINE_NO, ";
        $query .= "         TO_OUTGO_L_CD, ";
        $query .= "         TO_OUTGO_M_CD, ";
        $query .= "         TO_OUTGO_S_CD, ";
        $query .= "         TO_REQUEST_NO || rtrim(cast(TO_LINE_NO as char(2))) || TO_OUTGO_L_CD || TO_OUTGO_M_CD || TO_OUTGO_S_CD as REQ_LINE_NO ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_HASUU_MOVE_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     GROUP BY ";
        $query .= "         TO_REQUEST_NO, ";
        $query .= "         TO_LINE_NO, ";
        $query .= "         TO_OUTGO_L_CD, ";
        $query .= "         TO_OUTGO_M_CD, ";
        $query .= "         TO_OUTGO_S_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MEISAI.REQUEST_NO || '-' || rtrim(cast(MEISAI.LINE_NO as char(2))) || '-' || ";
        $query .= "     MEISAI.OUTGO_L_CD || '-' || MEISAI.OUTGO_M_CD || '-' || MEISAI.OUTGO_S_CD || ':' || rtrim(cast(MEISAI.HASUU as char(5))) AS VALUE, ";
        $query .= "     value(M_MST.LEVY_M_NAME, '') || '(' || MEISAI.OUTGO_L_CD || MEISAI.OUTGO_M_CD || '-' || MEISAI.REQUEST_NO || '-' || rtrim(cast(MEISAI.LINE_NO as char(2))) || ')' AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT MEISAI ";
        $query .= "     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTGO ON OUTGO.SCHOOLCD    = MEISAI.SCHOOLCD ";
        $query .= "                                           AND OUTGO.SCHOOL_KIND = MEISAI.SCHOOL_KIND ";
        $query .= "                                           AND OUTGO.YEAR        = MEISAI.YEAR ";
        $query .= "                                           AND OUTGO.OUTGO_L_CD  = MEISAI.OUTGO_L_CD ";
        $query .= "                                           AND OUTGO.OUTGO_M_CD  = MEISAI.OUTGO_M_CD ";
        $query .= "                                           AND OUTGO.REQUEST_NO  = MEISAI.REQUEST_NO ";
        $query .= "     LEFT JOIN LEVY_M_MST M_MST ON M_MST.SCHOOLCD    = MEISAI.SCHOOLCD ";
        $query .= "                               AND M_MST.SCHOOL_KIND = MEISAI.SCHOOL_KIND ";
        $query .= "                               AND M_MST.YEAR        = MEISAI.YEAR ";
        $query .= "                               AND M_MST.LEVY_L_CD   = MEISAI.OUTGO_L_CD ";
        $query .= "                               AND M_MST.LEVY_M_CD   = MEISAI.OUTGO_M_CD ";
        $query .= "     LEFT JOIN HIST_DAT HIST ON HIST.TO_REQUEST_NO = MEISAI.REQUEST_NO ";
        $query .= "                            AND HIST.TO_LINE_NO    = MEISAI.LINE_NO ";
        $query .= "                            AND HIST.TO_OUTGO_L_CD = MEISAI.OUTGO_L_CD ";
        $query .= "                            AND HIST.TO_OUTGO_M_CD = MEISAI.OUTGO_M_CD ";
        $query .= "                            AND HIST.TO_OUTGO_S_CD = MEISAI.OUTGO_S_CD ";
        $query .= " WHERE ";
        $query .= "         MEISAI.SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND MEISAI.SCHOOL_KIND   = '{$model->schoolKind}' ";
        $query .= "     AND MEISAI.YEAR          = '{$model->year}' ";
        $query .= "     AND OUTGO.OUTGO_APPROVAL = '1' ";
        $query .= "     AND OUTGO.OUTGO_CANCEL   is null ";
        $query .= "     AND MEISAI.HASUU         > 0 ";
        $query .= "     AND HIST.REQ_LINE_NO     is null ";
        $query .= "     AND MEISAI.REQUEST_NO || rtrim(cast(MEISAI.LINE_NO as char(2))) || ";
        $query .= "         MEISAI.OUTGO_L_CD || MEISAI.OUTGO_M_CD || MEISAI.OUTGO_S_CD <> '{$toLMcd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //List to List 左リスト
    function getHasuuListLeft($model) {
        list($toLMcd, $hasuu) = explode(':', $model->toLMcd);
        $toLMcd = str_replace('-', '', $toLMcd);

        $query  = " SELECT ";
        $query .= "     HIST.FROM_REQUEST_NO || '-' || rtrim(cast(HIST.FROM_LINE_NO as char(2))) || '-' || ";
        $query .= "     HIST.FROM_OUTGO_L_CD || '-' || HIST.FROM_OUTGO_M_CD || '-' || HIST.FROM_OUTGO_S_CD || ':' || rtrim(cast(HIST.MOVE_HASUU as char(5))) AS VALUE, ";
        $query .= "     value(M_MST.LEVY_M_NAME, '') || '(' || HIST.FROM_OUTGO_L_CD || HIST.FROM_OUTGO_M_CD || '-' || HIST.FROM_REQUEST_NO || '-' || rtrim(cast(HIST.FROM_LINE_NO as char(2))) || ')' AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_HASUU_MOVE_HIST_DAT HIST ";
        $query .= "     LEFT JOIN LEVY_M_MST M_MST ON M_MST.SCHOOLCD    = HIST.SCHOOLCD ";
        $query .= "                               AND M_MST.SCHOOL_KIND = HIST.SCHOOL_KIND ";
        $query .= "                               AND M_MST.YEAR        = HIST.YEAR ";
        $query .= "                               AND M_MST.LEVY_L_CD   = HIST.FROM_OUTGO_L_CD ";
        $query .= "                               AND M_MST.LEVY_M_CD   = HIST.FROM_OUTGO_M_CD ";
        $query .= " WHERE ";
        $query .= "         HIST.SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND HIST.SCHOOL_KIND   = '{$model->schoolKind}' ";
        $query .= "     AND HIST.YEAR          = '{$model->year}' ";
        $query .= "     AND HIST.TO_REQUEST_NO || rtrim(cast(HIST.TO_LINE_NO as char(2))) || ";
        $query .= "         HIST.TO_OUTGO_L_CD || HIST.TO_OUTGO_M_CD || HIST.TO_OUTGO_S_CD = '{$toLMcd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************/
    /* 処理実行 */
    /************/
    //更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // 履歴テーブル削除と、端数をリセットする
        knjp916Query::getDeleteHasuuMoveHistDat($db, $model);

        list($toTmpCd, $toHasuu) = explode(':', $model->toLMcd);
        list($toReqNo, $toLineNo, $toLcd, $toMcd, $toScd) = explode('-', $toTmpCd);

        // 対象項目更新
        $totalHasuu = 0;
        $hasuuArr = explode(',', $model->selectHasuu);
        if ($hasuuArr[0] != '') {
            foreach ($hasuuArr as $hasuuNo) {
                list($tmpCd, $hasuu) = explode(':', $hasuuNo);
                list($reqNo, $lineNo, $lcd, $mcd, $scd) = explode('-', $tmpCd);

                // 端数を0にする。（LEVY_REQUEST_INCOME_MEISAI_DAT）
                $data = array();
                $data["HASUU"][NUMBER]      = 0;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $where  = " WHERE ";
                $where .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
                $where .= " AND SCHOOL_KIND = '{$model->schoolKind}' ";
                $where .= " AND YEAR        = '{$model->year}' ";
                $where .= " AND REQUEST_NO  = '{$reqNo}' ";
                $where .= " AND LINE_NO     =  {$lineNo} ";
                $where .= " AND OUTGO_L_CD  = '{$lcd}'";
                $where .= " AND OUTGO_M_CD  = '{$mcd}'";
                $where .= " AND OUTGO_S_CD  = '{$scd}'";

                $query = Query::updateSQL($data, "LEVY_REQUEST_OUTGO_MEISAI_DAT", $where);
                $db->query($query);

                // 履歴テーブルを作成 --LEVY_REQUEST_HASUU_MOVE_HIST_DAT--
                $data2 = array();
                $data2["SCHOOLCD"][TEXT]         = (sprintf("%012d", SCHOOLCD));
                $data2["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
                $data2["YEAR"][TEXT]             = $model->year;
                $data2["FROM_REQUEST_NO"][TEXT]  = $reqNo;
                $data2["FROM_LINE_NO"][NUMBER]   = $lineNo;
                $data2["FROM_OUTGO_L_CD"][TEXT]  = $lcd;
                $data2["FROM_OUTGO_M_CD"][TEXT]  = $mcd;
                $data2["FROM_OUTGO_S_CD"][TEXT]  = $scd;
                $data2["TO_REQUEST_NO"][TEXT]    = $toReqNo;
                $data2["TO_LINE_NO"][NUMBER]     = $toLineNo;
                $data2["TO_OUTGO_L_CD"][TEXT]    = $toLcd;
                $data2["TO_OUTGO_M_CD"][TEXT]    = $toMcd;
                $data2["TO_OUTGO_S_CD"][TEXT]    = $toScd;
                $data2["MOVE_HASUU"][NUMBER]     = $hasuu;
                $data2["REGISTERCD"][TEXT]       = STAFFCD;
                $data2["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($data2, "LEVY_REQUEST_HASUU_MOVE_HIST_DAT");
                $db->query($query);

                $totalHasuu += $hasuu;
            }
        }

        // 指定先項目の端数取得
        $query = knjp916Query::getMeisaiHasuu($model, $toReqNo, $toLineNo, $toLcd, $toMcd, $toScd);
        $getHasuu = $db->getOne($query);

        if ($totalHasuu > 0) {

            // 指定先項目に端数を足しこむ
            $data3 = array();
            $data3["HASUU"][NUMBER]      = $getHasuu + $totalHasuu;
            $data3["REGISTERCD"][TEXT]   = STAFFCD;
            $data3["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= " AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $where .= " AND YEAR        = '{$model->year}' ";
            $where .= " AND REQUEST_NO  = '{$toReqNo}' ";
            $where .= " AND LINE_NO     =  {$toLineNo} ";
            $where .= " AND OUTGO_L_CD  = '{$toLcd}'";
            $where .= " AND OUTGO_M_CD  = '{$toMcd}'";
            $where .= " AND OUTGO_S_CD  = '{$toScd}'";

            $query = Query::updateSQL($data3, "LEVY_REQUEST_OUTGO_MEISAI_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $getHasuu + $totalHasuu;
    }

    // 履歴テーブル削除
    function getDeleteHasuuMoveHistDat($db, $model) {
        list($tmpCd, $hasuu) = explode(':', $model->toLMcd);
        list($reqNo, $lineNo, $lcd, $mcd, $scd) = explode('-', $tmpCd);

        // 履歴から端数を元の状態に戻す
        $totalHasuu = 0;
        $query = knjp916Query::getHasuuDat($model, $reqNo, $lineNo, $lcd, $mcd, $scd);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["HASUU"][NUMBER]      = $row["MOVE_HASUU"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= " AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $where .= " AND YEAR        = '{$model->year}' ";
            $where .= " AND REQUEST_NO  = '{$row["FROM_REQUEST_NO"]}' ";
            $where .= " AND LINE_NO     =  {$row["FROM_LINE_NO"]} ";
            $where .= " AND OUTGO_L_CD  = '{$row["FROM_OUTGO_L_CD"]}'";
            $where .= " AND OUTGO_M_CD  = '{$row["FROM_OUTGO_M_CD"]}'";
            $where .= " AND OUTGO_S_CD  = '{$row["FROM_OUTGO_S_CD"]}'";

            $query = Query::updateSQL($data, "LEVY_REQUEST_OUTGO_MEISAI_DAT", $where);
            $db->query($query);
            $totalHasuu += $row["MOVE_HASUU"];
        }

        // 指定先項目の端数取得
        $query = knjp916Query::getMeisaiHasuu($model, $reqNo, $lineNo, $lcd, $mcd, $scd);
        $getHasuu = $db->getOne($query);

        //指定先項目の端数をリセット
        $data2 = array();
        $data2["HASUU"][NUMBER]      = $getHasuu - $totalHasuu;
        $data2["REGISTERCD"][TEXT]   = STAFFCD;
        $data2["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE ";
        $where .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= " AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $where .= " AND YEAR        = '{$model->year}' ";
        $where .= " AND REQUEST_NO  = '{$reqNo}' ";
        $where .= " AND LINE_NO     =  {$lineNo} ";
        $where .= " AND OUTGO_L_CD  = '{$lcd}'";
        $where .= " AND OUTGO_M_CD  = '{$mcd}'";
        $where .= " AND OUTGO_S_CD  = '{$scd}'";

        $query = Query::updateSQL($data2, "LEVY_REQUEST_OUTGO_MEISAI_DAT", $where);
        $db->query($query);

        // 履歴テーブル削除
        $query  = " DELETE FROM LEVY_REQUEST_HASUU_MOVE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "       SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "   AND SCHOOL_KIND   = '{$model->schoolKind}' ";
        $query .= "   AND YEAR          = '{$model->year}' ";
        $query .= "   AND TO_REQUEST_NO = '{$reqNo}' ";
        $query .= "   AND TO_LINE_NO    =  {$lineNo} ";
        $query .= "   AND TO_OUTGO_L_CD = '{$lcd}' ";
        $query .= "   AND TO_OUTGO_M_CD = '{$mcd}' ";
        $query .= "   AND TO_OUTGO_S_CD = '{$scd}' ";

        $db->query($query);

        return;
    }

    // 履歴テーブル情報取得
    function getHasuuDat($model, $reqNo, $lineNo, $lcd, $mcd, $scd) {
        $query  = " SELECT ";
        $query .= "     FROM_REQUEST_NO, ";
        $query .= "     FROM_LINE_NO, ";
        $query .= "     FROM_OUTGO_L_CD, ";
        $query .= "     FROM_OUTGO_M_CD, ";
        $query .= "     FROM_OUTGO_S_CD, ";
        $query .= "     MOVE_HASUU ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_HASUU_MOVE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND   = '{$model->schoolKind}' ";
        $query .= "     AND YEAR          = '{$model->year}' ";
        $query .= "     AND TO_REQUEST_NO = '{$reqNo}' ";
        $query .= "     AND TO_LINE_NO    =  {$lineNo} ";
        $query .= "     AND TO_OUTGO_L_CD = '{$lcd}' ";
        $query .= "     AND TO_OUTGO_M_CD = '{$mcd}' ";
        $query .= "     AND TO_OUTGO_S_CD = '{$scd}' ";

        return $query;
    }

    // 端数取得
    function getMeisaiHasuu($model, $reqNo, $lineNo, $lcd, $mcd, $scd) {
        $query  = " SELECT ";
        $query .= "     HASUU ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     AND REQUEST_NO  = '{$reqNo}' ";
        $query .= "     AND LINE_NO     =  {$lineNo} ";
        $query .= "     AND OUTGO_L_CD  = '{$lcd}' ";
        $query .= "     AND OUTGO_M_CD  = '{$mcd}' ";
        $query .= "     AND OUTGO_S_CD  = '{$scd}' ";

        return $query;
    }
}
?>