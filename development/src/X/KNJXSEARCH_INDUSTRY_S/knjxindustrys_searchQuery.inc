<?php

require_once('for_php7.php');

class knjxindustrys_searchQuery extends Query {
    //大分類
    function getIndustryL($model) {
        $query  = " SELECT ";
        $query .= "     INDUSTRY_LCD AS VALUE, ";
        $query .= "     INDUSTRY_LCD || '：' || INDUSTRY_LNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_L_MST ";
        $query .= " ORDER BY ";
        $query .= "     INDUSTRY_LCD ";

        return $query;
    }

    //中分類
    function getIndustryM($model) {
        $query  = " SELECT ";
        $query .= "     INDUSTRY_MCD AS VALUE, ";
        $query .= "     INDUSTRY_MCD || '：' || INDUSTRY_MNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_M_MST ";
        $query .= " WHERE ";
        $query .= "     INDUSTRY_LCD = '".$model->field["INDUSTRY_LCD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     INDUSTRY_MCD ";

        return $query;
    }

    //検索結果
    function getIndustrysList($model){
        $query .= " SELECT ";
        $query .= "     INDUSTRY_SCD || ' | ' || ";
        $query .= "     INDUSTRY_SNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_S_MST T1 ";
        $query .= " WHERE ";
        $query .= "         INDUSTRY_SCD IS NOT NULL ";
        if ($model->cmd != "search") {
            $query .= "     AND INDUSTRY_LCD = '".$model->field["INDUSTRY_LCD"]."' ";
            $query .= "     AND INDUSTRY_MCD = '".$model->field["INDUSTRY_MCD"]."' ";
        } else {
            if ($model->field["INDUSTRY_LCD"] != '') {
                $query .= "     AND INDUSTRY_LCD = '".$model->field["INDUSTRY_LCD"]."' ";
            }
            if ($model->field["INDUSTRY_MCD"] != '') {
                $query .= "     AND INDUSTRY_MCD = '".$model->field["INDUSTRY_MCD"]."' ";
            }
            if ($model->field["INDUSTRY_SNAME"] != '') {
                $query .= "     AND INDUSTRY_SNAME LIKE '%{$model->field["INDUSTRY_SNAME"]}%' ";
            }
        }

        return $query;
    }
}
?>
