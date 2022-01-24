<?php

require_once('for_php7.php');

class knjl386qQuery extends Query {


    //リスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "    t1.*, ";
        $query .= "    t2.NAME1 as INOUT, ";
        $query .= "    t3.PLACECD || '：' || t3.PLACENAME_SHORT as PLACE, ";
        $query .= "    t4.NAME1 as IND, ";
        $query .= "    t5.NAME1 as SEND ";
        $query .= " FROM ";
        $query .= "    SAT_EXAM_NO_DAT t1  ";
        $query .= "    left join NAME_MST t2 on t1.INOUT_KUBUN = t2.NAMECD2 and t2.NAMECD1 = 'L201' ";
        $query .= "    left join SAT_EXAM_PLACE_DAT t3 on t1.PLACECD = t3.PLACECD and t1.YEAR = t3.YEAR ";
        $query .= "    left join NAME_MST t4 on t1.IND_KUBUN = t4.NAMECD2 and t4.NAMECD1 = 'L202' ";
        $query .= "    left join NAME_MST t5 on t1.SEND_KUBUN = t5.NAMECD2 and t5.NAMECD1 = 'L203' ";
        $query .= " WHERE ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY  ";
        $query .= "    JUKEN_NO_FROM ";

        return $query;
    }

    //１レコード取得
    function getRow($recno) {
        $query  = " SELECT ";
        $query .= "    * ";
        $query .= " FROM ";
        $query .= "    SAT_EXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "    RECNO = '".$recno."' ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";

        return $query;
    }
    
    //国内外Combo
    function getInout()
    {
        $query  = " SELECT ";
        $query .= "    NAMECD1, ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1 ";
        $query .= " FROM ";
        $query .= "    NAME_MST ";
        $query .= " WHERE ";
        $query .= "    NAMECD1 = 'L201' ";
        $query .= " ORDER BY ";
        $query .= "    NAMECD2 ";
        
        return $query;
    }
    
    //会場Combo
    function getPlacecd()
    {
        $query  = " SELECT ";
        $query .= "    PLACECD as PLACE, ";
        $query .= "    PLACECD || '：' || PLACENAME_SHORT as PLACE_NAME ";
        $query .= " FROM ";
        $query .= "    SAT_EXAM_PLACE_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    PLACECD ";
        
        return $query;
    }
    
    //個人/団体
    function getIndkubun()
    {
        $query  = " SELECT ";
        $query .= "    NAMECD1, ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1 ";
        $query .= " FROM ";
        $query .= "    NAME_MST ";
        $query .= " WHERE ";
        $query .= "    NAMECD1 = 'L202' ";
        $query .= " ORDER BY ";
        $query .= "    NAMECD2 ";
        
        return $query;
    }

    //郵便/窓口
    function getSendkubun()
    {
        $query  = " SELECT ";
        $query .= "    NAMECD1, ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1 ";
        $query .= " FROM ";
        $query .= "    NAME_MST ";
        $query .= " WHERE ";
        $query .= "    NAMECD1 = 'L203' ";
        $query .= " ORDER BY ";
        $query .= "    NAMECD2 ";
        
        return $query;
    }
    
    //RECNOの最大値取得
    function getRecMax()
    {
        $query  = " SELECT ";
        $query .= "    MAX(RECNO) ";
        $query .= " FROM ";
        $query .= "    SAT_EXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //DELETE
    function delete($recno)
    {
        $query  = " DELETE FROM ";
        $query .= "    SAT_EXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "    RECNO = '".$recno."' ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //エラーチェック用
    function betweenCnt($checkno, $recno="")
    {
        $query  = " SELECT ";
        $query .= "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "    SAT_EXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "    '".$checkno."' BETWEEN JUKEN_NO_FROM AND JUKEN_NO_TO ";
        if($recno != ""){
            $query .= " AND ";
            $query .= "    RECNO != '".$recno."' ";
        }
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";

        return $query;
    }
    function inCheck($from, $to, $recno="")
    {
        $query  = " SELECT ";
        $query .= "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "    SAT_EXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "    (JUKEN_NO_FROM BETWEEN '".$from."' AND '".$to."' ";
        $query .= " OR ";
        $query .= "    JUKEN_NO_TO BETWEEN '".$from."' AND '".$to."') ";
        if($recno != ""){
            $query .= " AND ";
            $query .= "    RECNO != '".$recno."' ";
        }
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";

        return $query;
    }
    function sameChack($place, $indkubun, $sendkubun)
    {
        $query  = " SELECT ";
        $query .= "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "    SAT_EXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "    PLACECD = '".$place."' AND ";
        $query .= "    IND_KUBUN = '".$indkubun."' AND ";
        $query .= "    SEND_KUBUN = '".$sendkubun."' ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }

    //コピー年度(左)
    function getCopyYear()
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     SAT_EXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR != '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";
        
        return $query;
    }
    
    //コピーデータ作成前にログイン年度データ削除
    function delYear()
    {
        $query  = " DELETE FROM ";
        $query .= "     SAT_EXAM_NO_DAT ";
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
        $query .= "     SAT_EXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     RECNO ";
        
        return $query;
    }

}
?>
