<?php

require_once('for_php7.php');


class knjmp912_mainQuery extends Query {

    //本締めデータチェック
    function getCloseFlgData($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_CLOSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND CLOSE_FLG = '1' ";

        return $query;
    }

    //一覧取得
    function getRow($model) {
    
        $db = Query::dbCheckOut();
    
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.HENKIN_L_CD || T1.HENKIN_M_CD || T1.HENKIN_S_CD AS HENKIN_L_M_S_CD, ";
        $query .= "     T1.COLLECT_L_CD || T1.COLLECT_M_CD || T1.COLLECT_S_CD AS COLLECT_L_M_S_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_HENKIN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.HENKIN_L_CD || T1.HENKIN_M_CD || T1.HENKIN_S_CD = '{$model->getHenkinLMScd}' ";
        $query .= "     AND T1.REQUEST_NO = '{$model->getRequestNo}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.HENKIN_L_CD, ";
        $query .= "     T1.HENKIN_M_CD, ";
        $query .= "     T1.HENKIN_S_CD, ";
        $query .= "     T1.REQUEST_NO ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //支出細目
    function getLevySDiv($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || T1.LEVY_S_CD || ':' || T1.LEVY_S_NAME AS LABEL, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || T1.LEVY_S_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     LEVY_S_MST T1 ";
        $query .= "     INNER JOIN LEVY_M_MST L1 ON L1.LEVY_L_CD = T1.LEVY_L_CD ";
        $query .= "                             AND L1.LEVY_M_CD = T1.LEVY_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.REPAY_DIV = '1' ";

        return $query;
    }

    //入金科目
    function getCollectSDiv($model) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     L2.COLLECT_L_CD || L2.COLLECT_M_CD || L2.COLLECT_M_CD AS VALUE, ";
        $query .= "     L2.COLLECT_L_CD || L2.COLLECT_M_CD || L2.COLLECT_M_CD || ':' || L2.COLLECT_S_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_L_MST T1 ";
        $query .= "     INNER JOIN COLLECT_M_MST L1 ON L1.COLLECT_L_CD = T1.COLLECT_L_CD ";
        $query .= "     INNER JOIN COLLECT_S_MST L2 ON L2.YEAR = L1.YEAR ";
        $query .= "                                AND L2.COLLECT_L_CD = T1.COLLECT_L_CD ";
        $query .= "                                AND L2.COLLECT_M_CD = L1.COLLECT_M_CD ";
        $query .= " WHERE ";
        $query .= "     L1.YEAR = '{$model->year}' ";
        $query .= " AND L1.COLLECT_S_EXIST_FLG = '1' ";
        $query .= " AND T1.LEVY_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //生徒取得
    function getSchno($model, $flg="") {
        //返金額 (生徒ごと)
        $query  = " WITH SCHREG_SELECT_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_HENKIN_SCHREG_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.HENKIN_L_CD || T1.HENKIN_M_CD || T1.HENKIN_S_CD = '{$model->getHenkinLMScd}' ";
        $query .= " AND T1.REQUEST_NO = '{$model->getRequestNo}' ";
        
        //対象データ取得
        //収入額がある生徒
        $query .= " ), INCOME_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     L1.SCHREGNO, ";
        $query .= "     SUM(INCOME_MONEY) AS INCOME_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                                AND L1.INCOME_L_CD = T1.INCOME_L_CD ";
        $query .= "                                                AND L1.INCOME_M_CD = T1.INCOME_M_CD ";
        $query .= "                                                AND L1.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_MEISAI_DAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                                AND L2.INCOME_L_CD = T1.INCOME_L_CD ";
        $query .= "                                                AND L2.INCOME_M_CD = T1.INCOME_M_CD ";
        $query .= "                                                AND L2.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "                                                AND L2.INCOME_S_CD = L1.INCOME_S_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.INCOME_APPROVAL = '1' ";
        $query .= " AND T1.INCOME_CANCEL IS NULL ";
        $query .= " GROUP BY ";
        $query .= "     L1.SCHREGNO ";
        //支出額がある生徒
        $query .= " ), OUTGO_DATA AS (     ";
        $query .= " SELECT ";
        $query .= "     L1.SCHREGNO, ";
        $query .= "     SUM(OUTGO_MONEY) AS OUTGO_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                                AND L1.OUTGO_L_CD = T1.OUTGO_L_CD ";
        $query .= "                                                AND L1.OUTGO_M_CD = T1.OUTGO_M_CD ";
        $query .= "                                                AND L1.REQUEST_NO = T1.REQUEST_NO ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_MEISAI_DAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                                AND L2.OUTGO_L_CD = T1.OUTGO_L_CD ";
        $query .= "                                                AND L2.OUTGO_M_CD = T1.OUTGO_M_CD ";
        $query .= "                                                AND L2.REQUEST_NO = T1.REQUEST_NO ";
        $query .= "                                                AND L2.OUTGO_S_CD = L1.OUTGO_S_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.OUTGO_APPROVAL = '1' ";
        $query .= " AND T1.OUTGO_CANCEL IS NULL ";
        $query .= " GROUP BY ";
        $query .= "     L1.SCHREGNO ";
        //収入額 - 支出額 >= 0が対象
        $query .= " ), TAISHOU_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(T1.INCOME_MONEY, 0) - VALUE(L1.OUTGO_MONEY, 0) AS ZAN_GK ";
        $query .= " FROM ";
        $query .= "     INCOME_DATA T1 ";
        $query .= "     LEFT JOIN OUTGO_DATA L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     VALUE(T1.INCOME_MONEY, 0) - VALUE(L1.OUTGO_MONEY, 0) >= 0 ";
        $query .= " )";
        
        $query .= " SELECT  ";
        $query .= "     T1.SCHREGNO AS VALUE,  ";
        //年組番表示
        if ($model->field["HR_CLASS_HYOUJI_FLG"] === '1') {
            $query .= "     SCH_RH.HR_NAME || T1.ATTENDNO || '番' || '　' || T2.NAME || '( 収入残額:' || CHAR(L1.ZAN_GK) || ')' AS LABEL  ";
        } else {
            $query .= "     T1.SCHREGNO || '　' || T2.NAME || '( 収入残額:' || CHAR(L1.ZAN_GK) || ')' AS LABEL  ";
        }
        $query .= " FROM  ";
        $query .= "     SCHREG_REGD_DAT T1  ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT SCH_RH ON T1.YEAR = SCH_RH.YEAR ";
        $query .= "                                      AND T1.SEMESTER = SCH_RH.SEMESTER ";
        $query .= "                                      AND T1.GRADE = SCH_RH.GRADE ";
        $query .= "                                      AND T1.HR_CLASS = SCH_RH.HR_CLASS ";
        $query .= "    ,SCHREG_BASE_MST T2 ";
        $query .= "     INNER JOIN TAISHOU_DATA L1 ON L1.SCHREGNO = T2.SCHREGNO ";
        //選択した伝票番号の生徒データのみを取得
        if ($flg) {
            $query .= "     INNER JOIN SCHREG_SELECT_DATA L2 ON L2.SCHREGNO = T2.SCHREGNO ";
        }
        $query .= " WHERE  ";
        $query .= "     T1.YEAR = '{$model->year}'  ";
        $query .= " AND T1.SEMESTER = '".CTRL_SEMESTER."'  ";
        $query .= " AND T2.SCHREGNO   = T1.SCHREGNO  ";
        
        //選択した伝票番号の生徒データを除く(右側)
        if ($flg == "") {
            $query .= " AND T2.SCHREGNO NOT IN (SELECT ";
            $query .= "                             SCHREGNO ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_SELECT_DATA ";
            $query .= "                         ) ";
        }
        $query .= " ORDER BY  ";
        //年組番表示
        if ($model->field["HR_CLASS_HYOUJI_FLG"] === '1') {
            $query .= "   T1.GRADE || T1.HR_CLASS || T1.ATTENDNO ";
        } else {
            $query .= "     SUBSTR(T1.SCHREGNO, 1, 4) DESC, ";
            $query .= "     SUBSTR(T1.SCHREGNO, 5)  ";
        }

        return $query;
    }

    //伝票番号を取得
    function getMaxRequestNo($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     MAX(REQUEST_NO) AS NO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_HENKIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);
        
        //対象生徒
        $schregSelectData = array();
        $schregSelectData = explode(",", $model->selectStudent);

        $henkinlcd = substr($model->field["HENKIN_L_M_S_CD"], 0, 2);
        $henkinmcd = substr($model->field["HENKIN_L_M_S_CD"], 2, 2);
        $henkinscd = substr($model->field["HENKIN_L_M_S_CD"], 4, 2);
        
        $collectlcd = substr($model->field["COLLECT_L_M_S_CD"], 0, 2);
        $collectmcd = substr($model->field["COLLECT_L_M_S_CD"], 2, 2);
        $collectscd = substr($model->field["COLLECT_L_M_S_CD"], 4, 2);
        
        //メインデータをセット(Delete Insert)
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_HENKIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND HENKIN_L_CD = '".$henkinlcd."' ";
        $query .= " AND HENKIN_M_CD = '".$henkinmcd."' ";
        $query .= " AND HENKIN_S_CD = '".$henkinscd."' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $db->query($query);
        
        $data = array();
        $data["YEAR"][TEXT]              = $model->year;
        $data["HENKIN_L_CD"][TEXT]       = $henkinlcd;
        $data["HENKIN_M_CD"][TEXT]       = $henkinmcd;
        $data["HENKIN_S_CD"][TEXT]       = $henkinscd;
        $data["REQUEST_NO"][TEXT]        = $model->getRequestNo;
        $data["HENKIN_GK"][NUMBER]       = $model->field["HENKIN_GK"];
        if ($model->selectStudent) {
            $data["HENKIN_CNT"][NUMBER]         = get_count($schregSelectData);
            $data["TOTAL_HENKIN_GK"][NUMBER]    = get_count($schregSelectData) * $model->field["HENKIN_GK"];
        } else {
            $data["HENKIN_CNT"][NUMBER]         = 0;
            $data["TOTAL_HENKIN_GK"][NUMBER]    = 0;
        }
        $data["COLLECT_L_CD"][TEXT]       = $collectlcd;
        $data["COLLECT_M_CD"][TEXT]       = $collectmcd;
        $data["COLLECT_S_CD"][TEXT]       = $collectscd;
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";
        $query = Query::insertSQL($data, "LEVY_REQUEST_HENKIN_DAT");
        $db->query($query);
        
        //返金生徒割り振りデータをセット(Delete Insert)
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_HENKIN_SCHREG_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.HENKIN_L_CD || T1.HENKIN_M_CD || T1.HENKIN_S_CD= '{$model->getHenkinLMScd}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        $db->query($query);
        //生徒データをInsert
        if ($model->selectStudent) {
            foreach ($schregSelectData as $schregNo) {
                //金額上限チェック
                $data = array();
                $data["YEAR"][TEXT]             = $model->year;
                $data["HENKIN_L_CD"][TEXT]      = $henkinlcd;
                $data["HENKIN_M_CD"][TEXT]      = $henkinmcd;
                $data["HENKIN_S_CD"][TEXT]      = $henkinscd;
                $data["REQUEST_NO"][TEXT]       = $model->getRequestNo;
                $data["SCHREGNO"][TEXT]         = $schregNo;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";
                $query = Query::insertSQL($data, "LEVY_REQUEST_HENKIN_SCHREG_DAT");
                $db->query($query);
            }
        }
        
        $db->commit();
        return;
    }
    
    //DELETE
    function &getDeleteQuery($db, $model) {
        $db->autoCommit(false);

        //LEVY_REQUEST_HENKIN_DAT
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_HENKIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND HENKIN_L_CD = '{$model->getHenkinLcd}' ";
        $query .= " AND HENKIN_M_CD = '{$model->getHenkinMcd}' ";
        $query .= " AND HENKIN_S_CD = '{$model->getHenkinScd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $db->query($query);

        //LEVY_REQUEST_HENKIN_SCHREG_DAT
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_HENKIN_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND HENKIN_L_CD = '{$model->getHenkinLcd}' ";
        $query .= " AND HENKIN_M_CD = '{$model->getHenkinMcd}' ";
        $query .= " AND HENKIN_S_CD = '{$model->getHenkinScd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $db->query($query);

        $db->commit();
        return;
    }
}
?>

