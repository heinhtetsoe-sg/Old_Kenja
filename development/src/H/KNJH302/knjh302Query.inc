<?php

require_once('for_php7.php');

class knjh302Query extends Query {

        //学籍基礎マスタより学籍番号と名前を取得
        function getSchregno_name($schregno)
        {
                $query  ="SELECT schregno,name_show FROM schreg_base_mst ";
                $query .=" WHERE schregno = '".$schregno."'";
                return $query;
        }

        //学籍賞罰データよりデータを取得
        function getAward($schregno)
        {

                $query  ="SELECT ";
                $query .="    T1.DETAIL_SDATE, ";
                $query .="    T1.DETAIL_DIV, ";
                $query .="    case when T1.DETAIL_DIV = '1' then T1.DETAIL_DIV || '：' || '賞データ' ";	
                $query .="    when T1.DETAIL_DIV = '2' then T1.DETAIL_DIV || '：' || '罰データ' ";
                $query .="    else T1.DETAIL_DIV || '：' || '自転車データ' end as DETAIL_DATA, ";
                $query .="    T1.DETAILCD, ";
                $query .="    T1.content, ";
                $query .="    T1.remark, ";
                $query .="    case when T1.DETAIL_DIV = '1' then T1.DETAILCD || '：' || meisyou_get(T1.DETAILCD,'H303',1) ";
                $query .="    when T1.DETAIL_DIV = '2' then T1.DETAILCD || '：' || meisyou_get(T1.DETAILCD,'H304',1) else '' end as DETAILCD ";
                $query .="FROM  SCHREG_DETAILHIST_DAT T1 ";
                $query .="WHERE ";
                $query .="    T1.DETAIL_DIV < '3' ";
                $query .="    AND T1.schregno = '".$schregno."' ";
                $query .="ORDER BY T1.year, T1.DETAIL_SDATE";

                return $query;
        }


}
?>
