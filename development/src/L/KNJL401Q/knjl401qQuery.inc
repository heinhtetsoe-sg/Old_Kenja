<?php

require_once('for_php7.php');

class knjl401qQuery extends Query {

    //試験会場
    function getPlace()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t1.PLACECD, ";
        $query .= "    t1.PLACEAREA ";
        $query .= " FROM ";
        $query .= "    SAT_EXAM_PLACE_DAT t1 ";
        $query .= " WHERE ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    t1.PLACECD ";
        
        return $query;
    }
    
    //リスト(左)
    function getList($model) {
        $query  = " SELECT ";
        $query .= "    T1.SAT_NO ";
        $query .= "  , T1.NAME1 ";
        $query .= "  , T1.NAME2 ";
        $query .= "  , T1.LASTNAME ";
        $query .= "  , T1.FIRSTNAME ";
        $query .= "  , T1.IND_KUBUN ";
        $query .= "  , T1.SEND_KUBUN ";
        $query .= "  , T1.PLACECD ";
        $query .= " FROM ";
        $query .= "    SAT_APP_FORM_MST T1 ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        if ($model->cmd == 'search') {
            $query .= " AND ";
            $query .= "    SAT_NO = '".$model->field["EXAMNO"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    SAT_NO ";

        return $query;
    }

    //受験番号重複チェック
    function getCnt($examno)
    {
        $query  = " SELECT ";
        $query .= "    COUNT(SAT_NO) ";
        $query .= " FROM ";
        $query .= "    SAT_APP_FORM_MST ";
        $query .= " WHERE ";
        $query .= "    SAT_NO = '".$examno."' ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //受験番号帯に含まれているか確認
    function getNoGroup($model)
    {
        $query  = " SELECT ";
        $query .= "    T1.JUKEN_NO_FROM ";
        $query .= "  , CASE WHEN MAX(T2.SAT_NO) IS NULL THEN T1.JUKEN_NO_FROM ELSE RIGHT('00000' || TRIM(CAST(INT(MAX(T2.SAT_NO)) + 1 AS VARCHAR(5))), 5) END AS NEXT ";
        $query .= " FROM ";
        $query .= "    SAT_EXAM_NO_DAT T1 ";
        $query .= "    LEFT JOIN SAT_APP_FORM_MST T2 ON ";
        $query .= "        T2.YEAR = T1.YEAR ";
        $query .= "        AND T2.SAT_NO BETWEEN T1.JUKEN_NO_FROM AND T1.JUKEN_NO_TO ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.PLACECD = '".$model->field["PLACECD"]."' ";
        $query .= "    AND T1.IND_KUBUN = '".$model->field["APPLYDIV"]."' ";
        $query .= "    AND T1.SEND_KUBUN = '".$model->field["FEE"]."' ";
        $query .= " GROUP BY ";
        $query .= "    T1.JUKEN_NO_FROM ";
        
        return $query;
    }

    //受験番号帯に含まれているか確認
    function getNoGroup2($model, $mode)
    {
        $query  = " SELECT ";
        if ($mode == "COUNT") {
            $query .= "    COUNT(*) AS COUNT ";
        } else {
            $query .= "    T1.PLACECD ";
            $query .= "  , T1.IND_KUBUN ";
            $query .= "  , T1.SEND_KUBUN ";
        }
        $query .= " FROM ";
        $query .= "    SAT_EXAM_NO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.PLACECD = '".$model->field["PLACECD"]."' ";
        $query .= "    AND T1.IND_KUBUN = '".$model->field["APPLYDIV"]."' ";
        $query .= "    AND T1.SEND_KUBUN = '".$model->field["FEE"]."' ";
        $query .= "    AND '".$model->field["BAN_EXAMNO"]."' BETWEEN JUKEN_NO_FROM AND JUKEN_NO_TO ";
        
        return $query;
    }


    //検索結果
    function getSearchList($model){
        $search = $model->field["SEARCH_NAME"];
        $query .= " SELECT ";
        $query .= "     T1.SAT_NO AS VALUE ";
        $query .= "   , T1.SAT_NO || ' | ' ||  T1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SAT_APP_FORM_MST T1 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["SEARCH_NAME"] != '') {
            $query .= "     AND T1.NAME1 LIKE '%".preg_replace("/(　| )/", "%", $search)."%' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SAT_NO ";

        return $query;
    }

}
?>
