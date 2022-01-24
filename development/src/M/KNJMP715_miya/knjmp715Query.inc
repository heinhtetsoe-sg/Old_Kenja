<?php

require_once('for_php7.php');

class knjmp715Query extends Query {

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
        $query .= "     YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
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
        $query .= "     YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
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
        $query .= "     ENTERYEAR = '{$model->mst_field["SELECT_YEAR"]}') ";

        return $query;
    }

    //生徒の名前の取得
    function getStudentName($model)
    {
        $tableName = " V_SCHREG_BASE_MST ";
        if ($model->search_div == "1") {
            $tableName = knjmp715Query::getFresh($model);
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

    //単位数
    function getCredits($model)
    {
        $query  = " SELECT ";
        $query .= "     SUM(T1.CREDITS) AS CREDITS ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         SUB_STD.SCHREGNO, ";
        $query .= "         SUB_STD.CLASSCD, ";
        $query .= "         SUB_STD.SCHOOL_KIND, ";
        $query .= "         SUB_STD.CURRICULUM_CD, ";
        $query .= "         SUB_STD.SUBCLASSCD, ";
        $query .= "         SUB_STD.CLASSCD, ";
        $query .= "         SUM(VALUE(CRE.CREDITS, 0)) AS CREDITS ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "         INNER JOIN SUBCLASS_STD_SELECT_DAT SUB_STD ON SUB_STD.YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
        $query .= "              AND REGD.SCHREGNO = SUB_STD.SCHREGNO ";
        $query .= "         LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
        $query .= "              AND REGD.COURSECD = CRE.COURSECD ";
        $query .= "              AND REGD.MAJORCD = CRE.MAJORCD ";
        $query .= "              AND REGD.GRADE = CRE.GRADE ";
        $query .= "              AND REGD.COURSECODE = CRE.COURSECODE ";
        $query .= "              AND SUB_STD.CLASSCD = CRE.CLASSCD ";
        $query .= "              AND SUB_STD.SCHOOL_KIND = CRE.SCHOOL_KIND ";
        $query .= "              AND SUB_STD.CURRICULUM_CD = CRE.CURRICULUM_CD ";
        $query .= "              AND SUB_STD.SUBCLASSCD = CRE.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         REGD.SCHREGNO = '{$model->schregno}' ";
        $query .= "         AND REGD.YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
        $query .= "         AND REGD.SEMESTER = (SELECT MAX(E1.SEMESTER) FROM SCHREG_REGD_DAT E1 WHERE E1.YEAR = '{$model->mst_field["SELECT_YEAR"]}' AND E1.SCHREGNO = '{$model->schregno}') ";
        $query .= "     GROUP BY ";
        $query .= "         SUB_STD.SCHREGNO, ";
        $query .= "         SUB_STD.CLASSCD, ";
        $query .= "         SUB_STD.SCHOOL_KIND, ";
        $query .= "         SUB_STD.CURRICULUM_CD, ";
        $query .= "         SUB_STD.SUBCLASSCD, ";
        $query .= "         SUB_STD.CLASSCD ";
        $query .= "     ) T1 ";

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
        $query .= "     SLIP.YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
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
        $query .= "     T1.YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
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
        $query .= "     SLIP.YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
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

    //グループコンボ
    function getGroupList($model) {
        $query .= " SELECT ";
        $query .= "     GD.COLLECT_GRP_CD AS VALUE, ";
        $query .= "     GD.COLLECT_GRP_CD || ':' || GM.COLLECT_GRP_NAME AS LABEL, ";
        $query .= "     SUM(VALUE(CM.COLLECT_M_MONEY, 0)) + SUM(VALUE(CS.COLLECT_S_MONEY, 0)) AS TMONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_DAT GD ";
        $query .= "     LEFT JOIN COLLECT_GRP_MST GM ON GD.YEAR = GM.YEAR ";
        $query .= "          AND GD.COLLECT_GRP_CD = GM.COLLECT_GRP_CD ";
        $query .= "     LEFT JOIN COLLECT_M_MST CM ON GD.YEAR = CM.YEAR ";
        $query .= "          AND GD.COLLECT_L_CD = CM.COLLECT_L_CD ";
        $query .= "          AND GD.COLLECT_M_CD = CM.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_S_MST CS ON GD.YEAR = CS.YEAR ";
        $query .= "          AND GD.COLLECT_L_CD = CS.COLLECT_L_CD ";
        $query .= "          AND GD.COLLECT_M_CD = CS.COLLECT_M_CD ";
        $query .= "          AND GD.COLLECT_S_CD = CS.COLLECT_S_CD ";
        $query .= " WHERE ";
        $query .= "     GD.YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
        $query .= " GROUP BY ";
        $query .= "     GD.COLLECT_GRP_CD, ";
        $query .= "     GM.COLLECT_GRP_NAME ";
        $query .= " ORDER BY ";
        $query .= "     GD.COLLECT_GRP_CD ";

        return $query;
    }

    //授業料
    function getJugyouryou($model) {
        $query  = " SELECT ";
        $query .= "     COLLECT_L_CD || ':' || COLLECT_M_CD AS VALUE, ";
        $query .= "     COLLECT_L_CD || COLLECT_M_CD || ':' || COLLECT_M_NAME AS LABEL, ";
        $query .= "     COLLECT_M_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
        $query .= "     AND IS_JUGYOURYOU = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //グループ選択
    function groupMstData($model)
    {
        $query  = " SELECT ";
        $query .= "     '1' AS ORDERCD, ";
        $query .= "     GD.COLLECT_L_CD, ";
        $query .= "     GD.COLLECT_M_CD, ";
        $query .= "     CM.COLLECT_M_MONEY AS MONEY_DUE, ";
        $query .= "     CS.COLLECT_S_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_DAT GD ";
        $query .= "     LEFT JOIN COLLECT_GRP_MST GM ON GD.YEAR = GM.YEAR ";
        $query .= "          AND GD.COLLECT_GRP_CD = GM.COLLECT_GRP_CD ";
        $query .= "     LEFT JOIN COLLECT_M_MST CM ON GD.YEAR = CM.YEAR ";
        $query .= "          AND GD.COLLECT_L_CD = CM.COLLECT_L_CD ";
        $query .= "          AND GD.COLLECT_M_CD = CM.COLLECT_M_CD ";
        $query .= "     LEFT JOIN ( ";
        $query .= "             SELECT ";
        $query .= "                 CSM.YEAR, ";
        $query .= "                 CSM.COLLECT_L_CD, ";
        $query .= "                 CSM.COLLECT_M_CD, ";
        $query .= "                 SUM(CSM.COLLECT_S_MONEY) AS COLLECT_S_MONEY ";
        $query .= "             FROM ";
        $query .= "                 COLLECT_S_MST CSM ";
        $query .= "             GROUP BY ";
        $query .= "                 CSM.YEAR, ";
        $query .= "                 CSM.COLLECT_L_CD, ";
        $query .= "                 CSM.COLLECT_M_CD ";
        $query .= "     ) CS ON GD.YEAR = CS.YEAR ";
        $query .= "          AND GD.COLLECT_L_CD = CS.COLLECT_L_CD ";
        $query .= "          AND GD.COLLECT_M_CD = CS.COLLECT_M_CD ";
        $query .= " WHERE ";
        $query .= "     GD.YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
        $query .= "     AND GD.COLLECT_GRP_CD = '{$model->mst_field["GROPCD"]}' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '2' AS ORDERCD, ";
        $query .= "     COLLECT_L_CD, ";
        $query .= "     COLLECT_M_CD, ";
        $query .= "     COLLECT_M_MONEY AS MONEY_DUE, ";
        $query .= "     0 AS COLLECT_S_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
        $query .= "     AND COLLECT_L_CD || ':' || COLLECT_M_CD = '{$model->mst_field["JUGYOU"]}' ";
        $query .= " ORDER BY ";
        $query .= "     ORDERCD, ";
        $query .= "     COLLECT_L_CD, ";
        $query .= "     COLLECT_M_CD ";

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
        $query .= "     YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
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
        $query .= "         END) AS MONEY_DUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT SLIP ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_DUE_M_DAT SLIP_MD ON SLIP.YEAR = SLIP_MD.YEAR ";
        $query .= "          AND SLIP.SLIP_NO = SLIP_MD.SLIP_NO ";
        $query .= "     LEFT JOIN COLLECT_M_MST CM ON SLIP_MD.YEAR = CM.YEAR ";
        $query .= "          AND SLIP_MD.COLLECT_L_CD = CM.COLLECT_L_CD ";
        $query .= "          AND SLIP_MD.COLLECT_M_CD = CM.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_DUE_S_DAT SLIP_SD ON SLIP_MD.YEAR = SLIP_SD.YEAR ";
        $query .= "          AND SLIP_MD.SLIP_NO = SLIP_SD.SLIP_NO ";
        $query .= "          AND SLIP_MD.SEQ = SLIP_SD.MSEQ ";
        $query .= " WHERE ";
        $query .= "     SLIP.YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
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
        $query .= "     MAIN_T.MONEY_DUE ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     SLIP_NO, ";
        $query .= "     SEQ ";

        return $query;
    }

    //予定削除
    function &deleteDue($db, $model)
    {
        $query  = " DELETE FROM  COLLECT_SLIP_MONEY_DUE_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
        $query .= "     AND SLIP_NO = '{$model->mst_field["SLIP_NO"]}' ";
        $db->query($query);
    }

    //予定追加
    function &insertDue($db, &$model)
    {

        //新規作成FLG
        $isNewData = true;

        //自身の未請求MAX伝票番号なし
        if ($model->mst_field["SLIP_NO"] == "") {
            //全体での未請求MAX伝票番号
            $query = knjmp715Query::getMaxSlipNo($model);
            $model->mst_field["SLIP_NO"] = $db->getOne($query);
        } else {
            $isNewData = false;
        }
        //自身/全体での未請求MAX伝票番号なし
        if ($model->mst_field["SLIP_NO"] == "") {
            //年度＋6桁
            $model->mst_field["SLIP_NO"] = $model->mst_field["SELECT_YEAR"]."000001";
        } else if ($isNewData) {
            $model->mst_field["SLIP_NO"] = $model->mst_field["SLIP_NO"] + 1;
        }

        //伝票データ作成
        $data = array();
        $data["YEAR"][TEXT]            = $model->mst_field["SELECT_YEAR"];
        $data["SLIP_NO"][TEXT]         = $model->mst_field["SLIP_NO"];
        $data["SCHREGNO"][TEXT]        = $model->schregno;
        $data["PAY_DIV"][TEXT]         = $model->mst_field["PAY_DIV"];
        $data["SLIP_DATE"][FUNC]       = "date(sysdate())";
        $data["SLIP_STAFFCD"][TEXT]    = STAFFCD;
        $data["CANCEL_DATE"][DATE]     = $model->mst_field["CANCEL_DATE"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        if ($isNewData) {
            $data["COLLECT_GRP_CD"][TEXT]    = $model->mst_field["GROPCD"];
            $query = Query::insertSQL($data, "COLLECT_SLIP_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
            $where .= "     AND SLIP_NO = '{$model->mst_field["SLIP_NO"]}' ";
            $query = Query::updateSQL($data, "COLLECT_SLIP_DAT", $where);
            $db->query($query);
        }

        foreach ($model->updField as $seq => $setData) {
            $data = array();
            list($lcd, $mcd) = explode(":", $setData["COLLECT_LM_CD"]);
            $data["YEAR"][TEXT]             = $model->mst_field["SELECT_YEAR"];
            $data["SLIP_NO"][TEXT]          = $model->mst_field["SLIP_NO"];
            $data["SEQ"][TEXT]              = sprintf("%02d", $seq);
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["COLLECT_L_CD"][TEXT]     = $lcd;
            $data["COLLECT_M_CD"][TEXT]     = $mcd;
            $data["MONEY_DUE"][NUMBER]      = $setData["MONEY_DUE"];
            $data["COLLECT_CNT"][NUMBER]    = $setData["COLLECT_CNT"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "COLLECT_SLIP_MONEY_DUE_M_DAT");
            $db->query($query);
        }
    }

    //削除後のインサート
    function &insertSeq($db, $model) {
        $furinaoshiSeq = 1;
        foreach ($model->updField as $seq => $setData) {
            if ($setData["DELCHK"] == "1") {
                continue;
            }
            $data = array();
            list($lcd, $mcd) = explode(":", $setData["COLLECT_LM_CD"]);
            $data["YEAR"][TEXT]             = $model->mst_field["SELECT_YEAR"];
            $data["SLIP_NO"][TEXT]          = $model->mst_field["SLIP_NO"];
            $data["SEQ"][TEXT]              = sprintf("%02d", $furinaoshiSeq);
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["COLLECT_L_CD"][TEXT]     = $lcd;
            $data["COLLECT_M_CD"][TEXT]     = $mcd;
            $data["MONEY_DUE"][NUMBER]      = $setData["MONEY_DUE"];
            $data["COLLECT_CNT"][NUMBER]    = $setData["COLLECT_CNT"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "COLLECT_SLIP_MONEY_DUE_M_DAT");
            $db->query($query);

            $furinaoshiSeq++;
        }
    }

    //MAX伝票番号
    function getMaxSlipNo($model) {
        $query  = " SELECT ";
        $query .= "     MAX(SLIP.SLIP_NO) AS SLIP_NO ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT SLIP ";
        $query .= " WHERE ";
        $query .= "     SLIP.YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";

        return $query;
    }

    //印刷履歴更新
    function insertPrintHist($db, $model)
    {
        $query = knjmp715Query::getMaxClaimNo($model, "SLIP");
        $claimNo = $db->getOne($query);
        $setClaimNo = $model->mst_field["SLIP_NO"];
        $setClaimEda = "00";
        if ($claimNo) {
            list($setClaimNo, $setClaimEda) = explode(":", $claimNo);
            $setClaimEda++;
        } else {
            $query = knjmp715Query::getMaxClaimNo($model, "MAX");
            $claimNo = $db->getOne($query);
            if ($claimNo) {
                $setClaimNo = $claimNo + 1;
            }
        }

        $query = knjmp715Query::getTotalMoneyDue($model);
        $setClaimMoney = $db->getOne($query);

        $data = array();
        $data["YEAR"][TEXT]             = $model->mst_field["SELECT_YEAR"];
        $data["CLAIM_NO"][TEXT]         = $setClaimNo;
        $data["CLAIM_EDA"][TEXT]        = sprintf("%02d", $setClaimEda);
        $data["SLIP_NO"][TEXT]          = $model->mst_field["SLIP_NO"];
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["CLAIM_DIV"][TEXT]        = $setClaimEda == "00" ? "1" : "2";
        $data["CLAIM_DATE"][FUNC]       = "date(sysdate())";
        $data["CLAIM_STAFFCD"][TEXT]    = STAFFCD;
        $data["CLAIM_MONEY"][NUMBER]    = $setClaimMoney;
        $data["PAY_DIV"][TEXT]          = $model->mst_field["PAY_DIV"];
        $data["LIMIT_DATE"][DATE]       = $model->mst_field["LIMIT_DATE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_SLIP_PRINT_HIST_DAT");
        $db->query($query);

    }

    //MAX請求書番号
    function getMaxClaimNo($model, $div = "") {
        $query  = " SELECT ";
        if ($div == "SLIP") {
            $query .= "     MAX(PRINT_HIST.CLAIM_NO || ':' || PRINT_HIST.CLAIM_EDA) AS CLAIM_NO_EDA ";
        } else {
            $query .= "     MAX(PRINT_HIST.CLAIM_NO) AS CLAIM_NO ";
        }
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PRINT_HIST_DAT PRINT_HIST ";
        $query .= " WHERE ";
        $query .= "     PRINT_HIST.YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
        if ($div == "SLIP") {
            $query .= "     AND PRINT_HIST.SLIP_NO = '{$model->mst_field["SLIP_NO"]}' ";
        }

        return $query;
    }

    //請求額
    function getTotalMoneyDue($model)
    {
        $query  = " SELECT ";
        $query .= "     SUM(MONEY_DUE * COLLECT_CNT) AS CLAIM_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_MONEY_DUE_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
        $query .= "     AND SLIP_NO = '{$model->mst_field["SLIP_NO"]}' ";

        return $query;
    }

}
?>
