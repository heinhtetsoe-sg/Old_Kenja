<?php

require_once('for_php7.php');

class knjmp911Query extends Query {

    //本締めデータチェック
    function getCloseFlgData() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_CLOSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND CLOSE_FLG = '1' ";

        return $query;
    }

    //科目ごとの収入額、支出額、残金を取得
    function getCloseData() {
        $query  = " WITH INCOME_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.INCOME_L_CD, ";
        $query .= "     SUM(VALUE(T1.REQUEST_GK, 0)) AS INCOME_TOTAL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_M_MST L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                            AND L1.LEVY_L_CD = T1.INCOME_L_CD ";
        $query .= "                            AND L1.LEVY_M_CD = T1.INCOME_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.INCOME_APPROVAL = '1' ";
        $query .= " AND T1.INCOME_CANCEL IS NULL ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.INCOME_L_CD ";
        $query .= " ), OUTGO_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     OUTGO_L_CD, ";
        $query .= "     SUM(VALUE(REQUEST_GK, 0)) AS OUTGO_TOTAL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND OUTGO_APPROVAL = '1' ";
        $query .= " AND OUTGO_CANCEL IS NULL ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     OUTGO_L_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.INCOME_L_CD AS CLOSE_L_CD, ";
        $query .= "     VALUE(T1.INCOME_TOTAL, 0) AS INCOME_TOTAL, ";
        $query .= "     VALUE(T2.OUTGO_TOTAL, 0) AS OUTGO_TOTAL,     ";
        $query .= "     VALUE(T1.INCOME_TOTAL, 0) - VALUE(T2.OUTGO_TOTAL, 0) AS ZANKIN_TOTAL ";
        $query .= " FROM ";
        $query .= "     INCOME_DATA T1 ";
        $query .= "     LEFT JOIN OUTGO_DATA T2 ON T1.YEAR = T2.YEAR ";
        $query .= "                            AND T1.INCOME_L_CD = T2.OUTGO_L_CD ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.INCOME_L_CD ";

        return $query;
    }

    //科目ごとに生徒の収入額、支出額、残金を取得
    function getCloseSchregData() {
        $query  = " WITH INCOME_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     L1.YEAR, ";
        $query .= "     L1.INCOME_L_CD, ";
        $query .= "     L1.SCHREGNO, ";
        $query .= "     SUM(VALUE(L1.INCOME_MONEY, 0)) AS INCOME_TOTAL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                                 AND L1.INCOME_L_CD = T1.INCOME_L_CD ";
        $query .= "                                                 AND L1.INCOME_M_CD = T1.INCOME_M_CD ";
        $query .= "                                                 AND L1.REQUEST_NO = T1.REQUEST_NO ";
        $query .= "     LEFT JOIN LEVY_M_MST L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                            AND L2.LEVY_L_CD = T1.INCOME_L_CD ";
        $query .= "                            AND L2.LEVY_M_CD = T1.INCOME_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.INCOME_APPROVAL = '1' ";
        $query .= " AND T1.INCOME_CANCEL IS NULL ";
        $query .= " GROUP BY ";
        $query .= "     L1.YEAR, ";
        $query .= "     L1.INCOME_L_CD, ";
        $query .= "     L1.SCHREGNO ";
        $query .= " ), OUTGO_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     L1.YEAR, ";
        $query .= "     L1.OUTGO_L_CD, ";
        $query .= "     L1.SCHREGNO, ";
        $query .= "     SUM(VALUE(L1.OUTGO_MONEY, 0)) AS OUTGO_TOTAL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT L1  ON L1.YEAR = T1.YEAR ";
        $query .= "                                                 AND L1.OUTGO_L_CD = T1.OUTGO_L_CD ";
        $query .= "                                                 AND L1.OUTGO_M_CD = T1.OUTGO_M_CD ";
        $query .= "                                                 AND L1.REQUEST_NO = T1.REQUEST_NO ";
        $query .= "  ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.OUTGO_APPROVAL = '1' ";
        $query .= " AND T1.OUTGO_CANCEL IS NULL ";
        $query .= " GROUP BY ";
        $query .= "     L1.YEAR, ";
        $query .= "     L1.OUTGO_L_CD, ";
        $query .= "     L1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.INCOME_L_CD AS CLOSE_L_CD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(T1.INCOME_TOTAL, 0) AS INCOME_TOTAL, ";
        $query .= "     VALUE(T2.OUTGO_TOTAL, 0) AS OUTGO_TOTAL,     ";
        $query .= "     VALUE(T1.INCOME_TOTAL, 0) - VALUE(T2.OUTGO_TOTAL, 0) AS ZANKIN_TOTAL ";
        $query .= " FROM ";
        $query .= "     INCOME_DATA T1 ";
        $query .= "     LEFT JOIN OUTGO_DATA T2 ON T1.YEAR = T2.YEAR ";
        $query .= "                            AND T1.INCOME_L_CD = T2.OUTGO_L_CD ";
        $query .= "                            AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T2.YEAR, ";
        $query .= "     T2.OUTGO_L_CD AS CLOSE_L_CD, ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     VALUE(T1.INCOME_TOTAL, 0) AS INCOME_TOTAL, ";
        $query .= "     VALUE(T2.OUTGO_TOTAL, 0) AS OUTGO_TOTAL,     ";
        $query .= "     VALUE(T1.INCOME_TOTAL, 0) - VALUE(T2.OUTGO_TOTAL, 0) AS ZANKIN_TOTAL ";
        $query .= " FROM ";
        $query .= "     OUTGO_DATA T2  ";
        $query .= "     LEFT JOIN INCOME_DATA T1 ON T1.YEAR = T2.YEAR ";
        $query .= "                             AND T1.INCOME_L_CD = T2.OUTGO_L_CD ";
        $query .= "                             AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO IS NULL ";
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     CLOSE_L_CD, ";
        $query .= "     SCHREGNO ";

        return $query;
    }
    
    //対象の締めデータ削除処理
    function deleteCloseData($db) {

        $query  = " DELETE FROM ";
        $query .= "     LEVY_CLOSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $db->query($query);

        return;
    }

    //対象の締めデータ更新処理
    function updateCloseData($db, $row, $model) {
        
        $data = array();
        $data["YEAR"][TEXT] = CTRL_YEAR;
        $data["CLOSE_L_CD"][TEXT] = $row["CLOSE_L_CD"];
        $data["INCOME_TOTAL"][NUMBER] = $row["INCOME_TOTAL"];
        $data["OUTGO_TOTAL"][NUMBER]  = $row["OUTGO_TOTAL"];
        $data["ZANKIN_TOTAL"][NUMBER] = $row["ZANKIN_TOTAL"];
        $data["CLOSE_FLG"][TEXT]      = $model->field["CLOSE_FLG"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "LEVY_CLOSE_DAT");
        $db->query($query);

        return;
    }
    
    //対象の生徒ごとの締めデータ削除処理
    function deleteCloseSchregData($db) {

        $query  = " DELETE FROM ";
        $query .= "     LEVY_CLOSE_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $db->query($query);

        return;
    }

    //対象の生徒ごとの締めデータ更新処理
    function updateCloseSchregData($db, $row, $model) {
        
        $data = array();
        $data["YEAR"][TEXT] = CTRL_YEAR;
        $data["CLOSE_L_CD"][TEXT] = $row["CLOSE_L_CD"];
        $data["SCHREGNO"][TEXT] = $row["SCHREGNO"];
        $data["INCOME_TOTAL"][NUMBER] = $row["INCOME_TOTAL"];
        $data["OUTGO_TOTAL"][NUMBER]  = $row["OUTGO_TOTAL"];
        $data["ZANKIN_TOTAL"][NUMBER] = $row["ZANKIN_TOTAL"];
        $data["CLOSE_FLG"][TEXT]      = $model->field["CLOSE_FLG"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "LEVY_CLOSE_SCHREG_DAT");
        $db->query($query);
        return;
    }
    
    //繰越金の項目取得
    function getKurikosicd($flg="", $lcd="") {
        $nextYear = CTRL_YEAR + 1;
        $query  = " SELECT ";
        if ($flg) {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     LEVY_L_CD, ";
            $query .= "     LEVY_M_CD ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_M_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$nextYear}' ";
        $query .= " AND KURIKOSI_FLG = '1' ";
        if ($lcd) {
            $query .= " AND LEVY_L_CD = '{$lcd}' ";
        }
        return $query;
    }

    //対象の締めデータ削除処理
    function deleteKurikosiData($db) {
        $nextYear = CTRL_YEAR + 1;
        $setRequestNo = $nextYear.'A00000';
        
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$nextYear}' ";
        $query .= " AND REQUEST_NO = '{$setRequestNo}' ";
        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_INCOME_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$nextYear}' ";
        $query .= " AND REQUEST_NO = '{$setRequestNo}' ";
        $db->query($query);

        return;
    }

    //繰越データ更新処理
    function updateKurikosiData($db, $row, $model) {
        $nextYear = CTRL_YEAR + 1;
        
        //繰越金の項目取得
        $kurikosiRow = array();
        $kurikosiRow = $db->getRow(knjmp911Query::getKurikosicd("", $row["CLOSE_L_CD"]), DB_FETCHMODE_ASSOC);
        
        $data = array();
        $data["YEAR"][TEXT] = $nextYear;
        $data["INCOME_L_CD"][TEXT]  = $kurikosiRow["LEVY_L_CD"];
        $data["INCOME_M_CD"][TEXT]  = $kurikosiRow["LEVY_M_CD"];
        $data["REQUEST_NO"][TEXT]   = $nextYear.'A00000';
        $data["REQUEST_DATE"][DATE] = $nextYear.'-04-01';
        $data["REQUEST_GK"][NUMBER] = $row["ZANKIN_TOTAL"];
        $data["INCOME_APPROVAL"][TEXT] = "1";
        $data["INCOME_DATE"][DATE]  = $nextYear.'-04-01';
        $data["INCOME_NO"][TEXT]    = $nextYear.'A00000';
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        $query = Query::insertSQL($data, "LEVY_REQUEST_INCOME_DAT");
        $db->query($query);
        
        $data = array();
        $data["YEAR"][TEXT] = $nextYear;
        $data["INCOME_L_CD"][TEXT] = $kurikosiRow["LEVY_L_CD"];
        $data["INCOME_M_CD"][TEXT] = $kurikosiRow["LEVY_M_CD"];
        $data["REQUEST_NO"][TEXT]  = $nextYear.'A00000';
        $data["INCOME_S_CD"][TEXT] = '01';
        $data["LINE_NO"][NUMBER]   = "1";
        $data["COMMODITY_PRICE"][NUMBER] = $row["ZANKIN_TOTAL"];
        $data["COMMODITY_CNT"][NUMBER] = "1";
        $data["TOTAL_PRICE"][NUMBER] = $row["ZANKIN_TOTAL"];
        $data["WARIHURI_DIV"][TEXT] = '2';
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "LEVY_REQUEST_INCOME_MEISAI_DAT");
        $db->query($query);
        return;
    }

}
?>