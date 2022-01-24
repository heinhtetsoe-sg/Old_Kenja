<?php

require_once('for_php7.php');

class knjl382qQuery extends Query {
    
    //コピー年度(左)
    function getCopyYear()
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     SAT_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR != '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";
        
        return $query;
    }
    
    //県名コンボ(左)
    function getPrefcd()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t1.GROUPPREF, ";
        $query .= "    t2.PREF_NAME ";
        $query .= " FROM ";
        $query .= "    SAT_GROUP_DAT t1 ";
        $query .= "    left join PREF_MST t2 on t1.GROUPPREF = t2.PREF_CD ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    t1.GROUPPREF ";
        
        return $query;
    }

    //リスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "    * ";
        $query .= " FROM ";
        $query .= "    SAT_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        if($model->left_field["SEARCH_PREF"] != ""){
            $query .= " AND ";
            $query .= "    GROUPPREF = '".$model->left_field["SEARCH_PREF"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    GROUPCD ";

        return $query;
    }

    //１レコード取得
    function getRow($groupcd) {
        $query  = " SELECT ";
        $query .= "    * ";
        $query .= " FROM ";
        $query .= "    SAT_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "    GROUPCD = '".$groupcd."' ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";

        return $query;
    }
    
    //県名コンボ(右)
    function getPrefcdAll()
    {
        $query  = " SELECT ";
        $query .= "    * ";
        $query .= " FROM ";
        $query .= "    PREF_MST ";
        $query .= " ORDER BY ";
        $query .= "    PREF_CD ";
        
        return $query;
    }
    
    //会場コードの重複チェック
    function countCode($groupcd)
    {
        $query  = " SELECT ";
        $query .= "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "    SAT_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "    GROUPCD = '".$groupcd."' ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //DELETE
    function delete($groupcd)
    {
        $query  = " DELETE FROM ";
        $query .= "    SAT_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "    GROUPCD = '".$groupcd."' ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //コピーデータ作成前にログイン年度データ削除
    function delYear()
    {
        $query  = " DELETE FROM ";
        $query .= "     SAT_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //コピー時元データ取得(左)
    function getCopyBase($year)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SAT_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     GROUPCD ";
        
        return $query;
    }
}
?>
