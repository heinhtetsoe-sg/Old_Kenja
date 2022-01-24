<?php

require_once('for_php7.php');

class knjl384qQuery extends Query {

    //コピー年度(左)
    function getCopyYear()
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     SAT_EXAM_PLACE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR != '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";
        
        return $query;
    }

    //リスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "    * ";
        $query .= " FROM ";
        $query .= "    SAT_EXAM_PLACE_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    PLACECD ";

        return $query;
    }

    //１レコード取得
    function getRow($placecd) {
        $query  = " SELECT ";
        $query .= "    * ";
        $query .= " FROM ";
        $query .= "    SAT_EXAM_PLACE_DAT ";
        $query .= " WHERE ";
        $query .= "    PLACECD = '".$placecd."' ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";

        return $query;
    }
    
    //会場コードの重複チェック
    function countCode($placecd)
    {
        $query  = " SELECT ";
        $query .= "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "    SAT_EXAM_PLACE_DAT ";
        $query .= " WHERE ";
        $query .= "    PLACECD = '".$placecd."' ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //DELETE
    function delete($placecd)
    {
        $query  = " DELETE FROM ";
        $query .= "    SAT_EXAM_PLACE_DAT ";
        $query .= " WHERE ";
        $query .= "    PLACECD = '".$placecd."' ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
       
        return $query;
    }
    
    //コピーデータ作成前にログイン年度データ削除
    function delYear()
    {
        $query  = " DELETE FROM ";
        $query .= "     SAT_EXAM_PLACE_DAT ";
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
        $query .= "     SAT_EXAM_PLACE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     PLACECD ";
        
        return $query;
    }
}
?>
