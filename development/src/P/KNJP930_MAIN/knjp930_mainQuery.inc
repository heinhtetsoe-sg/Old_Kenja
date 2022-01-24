<?php

require_once('for_php7.php');

class knjp930_mainQuery extends Query {

    //本締めデータチェック
    function getCloseFlgData($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_CLOSE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR = '{$model->year}' ";
        $query .= " AND CLOSE_FLG = '1' ";

        return $query;
    }

    //支出伺の決済の状況を取得(比較時に利用)
    function getOutgoData($model, $flg) {
        $query  = "";
        $query .= " SELECT ";
        if ($flg == "APPROVAL") {
            $query .= "     T1.OUTGO_APPROVAL ";
        } else if ($flg == "CANCEL") {
            $query .= "     T1.OUTGO_CANCEL ";
        } else {
            $query .= "     T1.REQUEST_NO ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEKOU_NO = '{$model->getRequestNo}' ";
        return $query;
    }

    //一覧取得
    function getRow($model) {
    
        $db = Query::dbCheckOut();
    
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        $query .= "     T1.SEKOU_L_CD || T1.SEKOU_M_CD AS SEKOU_L_M_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_SEKOU_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEKOU_L_CD = '{$model->getSekouLcd}' ";
        $query .= "     AND T1.SEKOU_L_CD || T1.SEKOU_M_CD = '{$model->getSekouLMcd}' ";
        $query .= "     AND T1.REQUEST_NO = '{$model->getRequestNo}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEKOU_L_CD, ";
        $query .= "     T1.SEKOU_M_CD, ";
        $query .= "     T1.REQUEST_NO ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //伺い人
    function getRequestStaff($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     STAFFCD AS VALUE, ";
        $query .= "     STAFFCD || ':' || STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
        
    //施行項目
    function getLevyMDiv($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD AS VALUE, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.LEVY_IN_OUT_DIV = '2' ";
        $query .= " AND T1.ZATU_FLG IS NULL  ";
        $query .= " AND T1.YOBI_FLG IS NULL  ";
        $query .= " AND T1.KURIKOSI_FLG IS NULL  ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //伝票番号を取得
    function getMaxRequestNo($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     MAX(REQUEST_NO) AS NO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_SEKOU_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR = '{$model->year}' ";

        return $query;
    }

    //削除
    function &getDeleteQuery($db, $model) {
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_SEKOU_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR = '{$model->year}' ";
        $query .= " AND SEKOU_L_CD = '{$model->getSekouLcd}' ";
        $query .= " AND SEKOU_M_CD = '{$model->getSekouMcd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $db->query($query);
        
        //支出伺伝票の紐付けをNULLにする
        if ($model->getOutgoRequestNo) {
            $data = array();
            $data["SEKOU_NO"][TEXT]   = "";
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][NUMBER]  = "sysdate()";
            $where  = " WHERE ";
            $where .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
            $where .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
            $where .= " AND YEAR = '{$model->year}' ";
            $where .= " AND OUTGO_L_CD = '{$model->getSekouLcd}' ";
            $where .= " AND OUTGO_M_CD = '{$model->getSekouMcd}' ";
            $where .= " AND REQUEST_NO = '{$model->getOutgoRequestNo}' ";
            
            $query = Query::updateSQL($data, "LEVY_REQUEST_OUTGO_DAT", $where);
            $db->query($query);
        }
        
        $db->commit();
        return;
    }
    
    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);
        
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_SEKOU_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR = '{$model->year}' ";
        $query .= " AND SEKOU_L_CD = '{$model->getSekouLcd}' ";
        $query .= " AND SEKOU_M_CD = '{$model->getSekouMcd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $db->query($query);

        $sekoulcd = substr($model->field["SEKOU_L_M_CD"], 0, 2);
        $sekoumcd = substr($model->field["SEKOU_L_M_CD"], 2, 2);
        
        $data = array();
        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->getSchoolKind;
        $data["YEAR"][TEXT]             = $model->year;
        $data["SEKOU_L_CD"][TEXT]       = $sekoulcd;
        $data["SEKOU_M_CD"][TEXT]       = $sekoumcd;
        $data["REQUEST_NO"][TEXT]       = $model->getRequestNo;
        $data["REQUEST_DATE"][DATE]     = str_replace("/","-",$model->field["REQUEST_DATE"]);
        $data["REQUEST_STAFF"][TEXT]    = $model->field["REQUEST_STAFF"];
        $data["REQUEST_GK"][NUMBER]     = $model->field["REQUEST_GK"];
        
        $data["SEKOU_JIGYOU_NAME"][TEXT]  = $model->field["SEKOU_JIGYOU_NAME"];
        $data["SEKOU_NAIYOU"][TEXT]       = $model->field["SEKOU_NAIYOU"];
        $data["SEKOU_DATE_FROM"][DATE]    = str_replace("/","-",$model->field["SEKOU_DATE_FROM"]);
        $data["SEKOU_DATE_TO"][DATE]      = str_replace("/","-",$model->field["SEKOU_DATE_TO"]);
        $data["SEKOU_PLACE"][TEXT]        = $model->field["SEKOU_PLACE"];
        $data["KEIYAKU_HOUHOU"][TEXT]     = $model->field["KEIYAKU_HOUHOU"];
        $data["REMARK"][TEXT]             = $model->field["REMARK"];

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $query = Query::insertSQL($data, "LEVY_REQUEST_SEKOU_DAT");
        $db->query($query);

        $db->commit();
        return;
    }
    
}
?>