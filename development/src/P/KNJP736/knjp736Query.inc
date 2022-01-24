<?php

require_once('for_php7.php');

class knjp736Query extends Query {

    function getYear($model) {
        $query  = " WITH THIS_YEAR(LABEL, VALUE) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."', '".CTRL_YEAR."'), ";
        $query .= "     ('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     THIS_YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    function getSlipNo($model) {
        $query  = " SELECT ";
        $query .= "     SLIP_NO AS LABEL, ";
        $query .= "     SLIP_NO AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->mst_field["YEAR"]}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     SLIP_NO DESC ";

        return $query;
    }

    function getSlipInfo($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->mst_field["YEAR"]}' ";
        $query .= "     AND SLIP_NO = '{$model->mst_field["SLIP_NO"]}' ";

        return $query;
    }

    function getFresh($model) {
        $query  = "(SELECT ";
        $query .= "     M1.*, ";
        $query .= "     ML1.BASE_REMARK4 AS MUSYOU_KAISU ";
        $query .= " FROM ";
        $query .= "     FRESHMAN_DAT M1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST ML1 ON M1.SCHREGNO = ML1.SCHREGNO ";
        $query .= "          AND ML1.BASE_SEQ = '004' ";
        $query .= " WHERE ";
        $query .= "     ENTERYEAR = '{$model->mst_field["YEAR"]}') ";

        return $query;
    }

    //生徒の名前の取得
    function getStudentName($model)
    {
        $tableName = " V_SCHREG_BASE_MST ";
        if ($model->search_div == "1") {
            $tableName = knjp736Query::getFresh($model);
        }
        
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     MUSYOU_KAISU ";
        $query .= " FROM ";
        $query .= "     {$tableName} T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //生徒伝票
    function getSlipData($model) {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SLIP.SLIP_NO, ";
        $query .= "     MAX(SLIP.PAY_DIV) AS PAY_DIV, ";
        $query .= "     MAX(SLIP.SLIP_DATE) AS SLIP_DATE, ";
        $query .= "     MAX(SLIP.CANCEL_DATE) AS CANCEL_DATE, ";
        $query .= "     MAX(SLIP_MP.PAID_MONEY_DATE) AS PAID_MONEY_DATE, ";
        $query .= "     SUM(CASE WHEN SLIP_SD.COLLECT_S_CD IS NULL ";
        $query .= "              THEN VALUE(SLIP_MD.MONEY_DUE, 0) * VALUE(SLIP_MD.COLLECT_CNT, 0) ";
        $query .= "              ELSE VALUE(SLIP_SD.MONEY_DUE, 0) * VALUE(SLIP_SD.COLLECT_CNT, 0) ";
        $query .= "         END) AS TMONEY, ";
        $query .= "     SUM(CASE WHEN SLIP_SP.COLLECT_S_CD IS NULL ";
        $query .= "              THEN VALUE(SLIP_MP.PAID_MONEY, 0) ";
        $query .= "              ELSE VALUE(SLIP_SP.PAID_MONEY, 0) ";
        $query .= "         END) AS TPAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT SLIP ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_DUE_M_DAT SLIP_MD ON SLIP.YEAR = SLIP_MD.YEAR ";
        $query .= "          AND SLIP.SLIP_NO = SLIP_MD.SLIP_NO ";
        $query .= "     LEFT JOIN COLLECT_M_MST CM ON SLIP_MD.YEAR = CM.YEAR ";
        $query .= "          AND SLIP_MD.COLLECT_L_CD = CM.COLLECT_L_CD ";
        $query .= "          AND SLIP_MD.COLLECT_M_CD = CM.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_PAID_M_DAT SLIP_MP ON SLIP_MD.YEAR = SLIP_MP.YEAR ";
        $query .= "          AND SLIP_MD.SLIP_NO = SLIP_MP.SLIP_NO ";
        $query .= "          AND SLIP_MD.SEQ = SLIP_MP.SEQ ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_DUE_S_DAT SLIP_SD ON SLIP_MD.YEAR = SLIP_SD.YEAR ";
        $query .= "          AND SLIP_MD.SLIP_NO = SLIP_SD.SLIP_NO ";
        $query .= "          AND SLIP_MD.SEQ = SLIP_SD.MSEQ ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_PAID_S_DAT SLIP_SP ON SLIP_SD.YEAR = SLIP_SP.YEAR ";
        $query .= "          AND SLIP_SD.SLIP_NO = SLIP_SP.SLIP_NO ";
        $query .= "          AND SLIP_SD.MSEQ = SLIP_SP.MSEQ ";
        $query .= "          AND SLIP_SD.SSEQ = SLIP_SP.SSEQ ";
        $query .= " WHERE ";
        $query .= "     SLIP.YEAR = '{$model->mst_field["YEAR"]}' ";
        $query .= "     AND SLIP.SLIP_NO = '{$model->mst_field["SLIP_NO"]}' ";
        $query .= "     AND SLIP.SCHREGNO = '{$model->schregno}' ";
        $query .= " GROUP BY ";
        $query .= "     SLIP.SLIP_NO ";
        $query .= " ), PRINT_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SLIP_NO, ";
        $query .= "     MAX(T1.CLAIM_NO || '-' || T1.CLAIM_EDA) AS CLAIM_NO ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PRINT_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->mst_field["YEAR"]}' ";
        $query .= "     AND T1.SLIP_NO = '{$model->mst_field["SLIP_NO"]}' ";
        $query .= "     AND VALUE(T1.CLAIM_NONE_FLG, '0') = '0' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SLIP_NO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.SLIP_NO, ";
        $query .= "     MAIN_T.PAY_DIV, ";
        $query .= "     MAIN_T.SLIP_DATE, ";
        $query .= "     MAIN_T.CANCEL_DATE, ";
        $query .= "     MAIN_T.TMONEY, ";
        $query .= "     MAIN_T.TPAID_MONEY, ";
        $query .= "     PRINT_T.CLAIM_NO ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= "     LEFT JOIN PRINT_T ON MAIN_T.SLIP_NO = PRINT_T.SLIP_NO ";

        return $query;
    }

    //生徒伝票ALL
    function getSlipDataALL($model) {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SUM(CASE WHEN SLIP_SD.COLLECT_S_CD IS NULL ";
        $query .= "              THEN VALUE(SLIP_MD.MONEY_DUE, 0) * VALUE(SLIP_MD.COLLECT_CNT, 0) ";
        $query .= "              ELSE VALUE(SLIP_SD.MONEY_DUE, 0) * VALUE(SLIP_SD.COLLECT_CNT, 0) ";
        $query .= "         END) AS ALL_MONEY, ";
        $query .= "     SUM(CASE WHEN SLIP_SP.COLLECT_S_CD IS NULL ";
        $query .= "              THEN VALUE(SLIP_MP.PAID_MONEY, 0) ";
        $query .= "              ELSE VALUE(SLIP_SP.PAID_MONEY, 0) ";
        $query .= "         END) AS ALL_PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT SLIP ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_DUE_M_DAT SLIP_MD ON SLIP.YEAR = SLIP_MD.YEAR ";
        $query .= "          AND SLIP.SLIP_NO = SLIP_MD.SLIP_NO ";
        $query .= "     LEFT JOIN COLLECT_M_MST CM ON SLIP_MD.YEAR = CM.YEAR ";
        $query .= "          AND SLIP_MD.COLLECT_L_CD = CM.COLLECT_L_CD ";
        $query .= "          AND SLIP_MD.COLLECT_M_CD = CM.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_PAID_M_DAT SLIP_MP ON SLIP_MD.YEAR = SLIP_MP.YEAR ";
        $query .= "          AND SLIP_MD.SLIP_NO = SLIP_MP.SLIP_NO ";
        $query .= "          AND SLIP_MD.SEQ = SLIP_MP.SEQ ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_DUE_S_DAT SLIP_SD ON SLIP_MD.YEAR = SLIP_SD.YEAR ";
        $query .= "          AND SLIP_MD.SLIP_NO = SLIP_SD.SLIP_NO ";
        $query .= "          AND SLIP_MD.SEQ = SLIP_SD.MSEQ ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_PAID_S_DAT SLIP_SP ON SLIP_SD.YEAR = SLIP_SP.YEAR ";
        $query .= "          AND SLIP_SD.SLIP_NO = SLIP_SP.SLIP_NO ";
        $query .= "          AND SLIP_SD.MSEQ = SLIP_SP.MSEQ ";
        $query .= "          AND SLIP_SD.SSEQ = SLIP_SP.SSEQ ";
        $query .= " WHERE ";
        $query .= "     SLIP.YEAR = '{$model->mst_field["YEAR"]}' ";
        $query .= "     AND SLIP.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND SLIP.CANCEL_DATE IS NULL ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.ALL_MONEY, ";
        $query .= "     MAIN_T.ALL_PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";

        return $query;
    }

    //商品
    function getCollectM($model) {
        $query  = " SELECT ";
        $query .= "     COLLECT_L_CD || ':' || COLLECT_M_CD AS VALUE, ";
        $query .= "     COLLECT_L_CD || COLLECT_M_CD || ':' || COLLECT_M_NAME AS LABEL, ";
        $query .= "     COLLECT_M_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->mst_field["YEAR"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒伝票明細
    function getMeisaiData($model) {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SLIP.SLIP_NO, ";
        $query .= "     SLIP_MD.SEQ, ";
        $query .= "     MAX(CM.COLLECT_L_CD) AS COLLECT_L_CD, ";
        $query .= "     MAX(CM.COLLECT_M_CD) AS COLLECT_M_CD, ";
        $query .= "     MAX(SLIP_MD.COLLECT_CNT) AS COLLECT_CNT, ";
        $query .= "     SUM(CASE WHEN SLIP_SD.COLLECT_S_CD IS NULL ";
        $query .= "              THEN VALUE(SLIP_MD.MONEY_DUE, 0) ";
        $query .= "              ELSE VALUE(SLIP_SD.MONEY_DUE, 0) ";
        $query .= "         END) AS MONEY_DUE, ";
        $query .= "     SUM(CASE WHEN SLIP_SD.COLLECT_S_CD IS NULL ";
        $query .= "              THEN VALUE(SLIP_MP.PAID_MONEY, 0) ";
        $query .= "              ELSE VALUE(SLIP_SP.PAID_MONEY, 0) ";
        $query .= "         END) AS PAID_MONEY, ";
        $query .= "     MAX(CASE WHEN SLIP_SD.COLLECT_S_CD IS NULL ";
        $query .= "              THEN SLIP_MP.PAID_MONEY_DATE ";
        $query .= "              ELSE SLIP_SP.PAID_MONEY_DATE ";
        $query .= "         END) AS PAID_MONEY_DATE ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT SLIP ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_DUE_M_DAT SLIP_MD ON SLIP.YEAR = SLIP_MD.YEAR ";
        $query .= "          AND SLIP.SLIP_NO = SLIP_MD.SLIP_NO ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_PAID_M_DAT SLIP_MP ON SLIP_MD.YEAR = SLIP_MP.YEAR ";
        $query .= "          AND SLIP_MD.SLIP_NO = SLIP_MP.SLIP_NO ";
        $query .= "          AND SLIP_MD.SEQ = SLIP_MP.SEQ ";
        $query .= "     LEFT JOIN COLLECT_M_MST CM ON SLIP_MD.YEAR = CM.YEAR ";
        $query .= "          AND SLIP_MD.COLLECT_L_CD = CM.COLLECT_L_CD ";
        $query .= "          AND SLIP_MD.COLLECT_M_CD = CM.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_DUE_S_DAT SLIP_SD ON SLIP_MD.YEAR = SLIP_SD.YEAR ";
        $query .= "          AND SLIP_MD.SLIP_NO = SLIP_SD.SLIP_NO ";
        $query .= "          AND SLIP_MD.SEQ = SLIP_SD.MSEQ ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_PAID_S_DAT SLIP_SP ON SLIP_SD.YEAR = SLIP_SP.YEAR ";
        $query .= "          AND SLIP_SD.SLIP_NO = SLIP_SP.SLIP_NO ";
        $query .= "          AND SLIP_SD.MSEQ = SLIP_SP.MSEQ ";
        $query .= "          AND SLIP_SD.SSEQ = SLIP_SP.SSEQ ";
        $query .= " WHERE ";
        $query .= "     SLIP.YEAR = '{$model->mst_field["YEAR"]}' ";
        $query .= "     AND SLIP.SLIP_NO = '{$model->mst_field["SLIP_NO"]}' ";
        $query .= "     AND SLIP.SCHREGNO = '{$model->schregno}' ";
        $query .= " GROUP BY ";
        $query .= "     SLIP.SLIP_NO, ";
        $query .= "     SLIP_MD.SEQ ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.SLIP_NO, ";
        $query .= "     MAIN_T.SEQ, ";
        $query .= "     MAIN_T.COLLECT_L_CD, ";
        $query .= "     MAIN_T.COLLECT_M_CD, ";
        $query .= "     MAIN_T.COLLECT_CNT, ";
        $query .= "     MAIN_T.MONEY_DUE, ";
        $query .= "     MAIN_T.PAID_MONEY, ";
        $query .= "     MAIN_T.PAID_MONEY_DATE ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     SLIP_NO, ";
        $query .= "     SEQ ";

        return $query;
    }

    //削除
    function &deletePaid($db, $model)
    {
        $query  = " DELETE FROM  COLLECT_SLIP_MONEY_PAID_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->mst_field["YEAR"]}' ";
        $query .= "     AND SLIP_NO = '{$model->mst_field["SLIP_NO"]}' ";
        $db->query($query);
    }

    //追加
    function &insertPaid($db, $model)
    {

        foreach ($model->updField as $key => $setData) {
            $query = knjp736Query::getDueMDat($model, $setData);
            $dueMdat = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $data = array();
            list($lcd, $mcd) = explode(":", $setData["COLLECT_LM_CD"]);
            $data["YEAR"][TEXT]             = $model->mst_field["YEAR"];
            $data["SLIP_NO"][TEXT]          = $model->mst_field["SLIP_NO"];
            $data["SEQ"][TEXT]              = sprintf("%02d", $setData["SEQ"]);
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["COLLECT_L_CD"][TEXT]     = $dueMdat["COLLECT_L_CD"];
            $data["COLLECT_M_CD"][TEXT]     = $dueMdat["COLLECT_M_CD"];
            $data["PAID_MONEY_DATE"][DATE]  = $setData["PAID_MONEY_DATE"];
            $data["PAID_MONEY"][NUMBER]     = $setData["PAID_MONEY"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "COLLECT_SLIP_MONEY_PAID_M_DAT");
            $db->query($query);
        }
    }

    //MAX伝票番号
    function getDueMDat($model, $setData) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_MONEY_DUE_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->mst_field["YEAR"]}' ";
        $query .= "     AND SLIP_NO = '{$model->mst_field["SLIP_NO"]}' ";
        $query .= "     AND SEQ = '{$setData["SEQ"]}' ";

        return $query;
    }

}
?>
