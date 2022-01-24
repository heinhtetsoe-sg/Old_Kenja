<?php

require_once('for_php7.php');

class knjxjoboffer_searchQuery extends Query {

    //検索結果
    function getJobOfferList($model){
        $query .= " SELECT ";
        $query .= "     CHAR(T1.SENKOU_NO) || ' | ' || ";
        $query .= "     VALUE(T1.COMPANY_NAME, '　') || ' | ' || ";
        $query .= "     VALUE(T1.COMPANY_NAMEKANA, '　') || ' | ' || ";
        $query .= "     VALUE(T1.COMPANY_ZIPCD, '　') || ' | ' || ";
        $query .= "     VALUE(T1.COMPANY_ADDR1, '　') || ' | ' || ";
        $query .= "     VALUE(T1.COMPANY_ADDR2, '　') || ' | ' || ";
        $query .= "     VALUE(T1.COMPANY_TELNO1, '　') || ' | ' || ";
        $query .= "     VALUE(L1.COMPANY_CD, '　')  || ' | ' || ";
        $query .= "     VALUE(L1.COMPANY_NAME, '　')  || ' | ' || ";
        $query .= "     '〒 ' || VALUE(L1.ZIPCD, '　') || ' | ' || ";
        $query .= "     VALUE(L1.ADDR1, '　') || ' | ' || ";
        $query .= "     VALUE(L1.ADDR2, '　') || ' | ' || ";
        $query .= "     VALUE(L1.TELNO, '　') || ' | ' || ";
        $query .= "     VALUE(L2.INDUSTRY_MNAME, '　') AS LABEL ";
        $query .= " FROM ";
        $query .= "     JOB_OFFER_DAT T1 ";
        $query .= "     LEFT JOIN COMPANY_MST L1 ON T1.COMPANY_CD = L1.COMPANY_CD ";
        $query .= "     LEFT JOIN INDUSTRY_M_MST L2 ON L1.INDUSTRY_LCD = L2.INDUSTRY_LCD ";
        $query .= "                                AND L1.INDUSTRY_MCD = L2.INDUSTRY_MCD ";
        $query .= " WHERE ";
        $query .= "         SENKOU_NO IS NOT NULL ";
        if ($model->field["COMPANY_NAME"] != '') {
            $query .= "     AND T1.COMPANY_NAME LIKE '%{$model->field["COMPANY_NAME"]}%' ";
        }
        if ($model->field["COMPANY_NAMEKANA"] != '') {
            $query .= "     AND T1.COMPANY_NAMEKANA LIKE '%{$model->field["COMPANY_NAMEKANA"]}%' ";
        }
        if ($model->field["COMPANY_ADDR1"] != '') {
            $query .= "     AND T1.COMPANY_ADDR1 LIKE '%{$model->field["COMPANY_ADDR1"]}%' ";
        }
        
        return $query;
    }
}
?>
