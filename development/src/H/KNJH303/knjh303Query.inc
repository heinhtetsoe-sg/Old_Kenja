<?php

require_once('for_php7.php');

class knjh303Query extends Query {


    //学籍基礎マスタより学籍番号と名前を取得
    function getSchregno_name($schregno)
    {
        $query  ="SELECT schregno,name_show FROM schreg_base_mst ";
        $query .=" WHERE schregno = '".$schregno."'";
        return $query;
    }

    //学籍賞罰データよりデータを取得
    function selectQuery($model)
    {
        $query = " SELECT ";
        $query .= "   T1.TRAINDATE, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.PATIENTCD, ";
        $query .= "   T1.PATIENTCD || ':' || MEISYOU_GET(T1.PATIENTCD,'H301',1) AS PATIENT, ";
        $query .= "   T1.STAFFCD, ";
        $query .= "   T1.STAFFCD || ':' || T2.STAFFNAME_SHOW AS STAFFNAME_SHOW, ";
        $query .= "   T1.HOWTOTRAINCD, ";
        $query .= "   T1.HOWTOTRAINCD || ':' || MEISYOU_GET(T1.HOWTOTRAINCD,'H302',1) AS HOWTOTRAIN, ";
        $query .= "   T1.CONTENT ";
        $query .= " FROM ";
        $query .= "   SCHREG_TRAINHIST_DAT T1 LEFT OUTER JOIN V_STAFF_MST T2  ";
        $query .= "   ON T1.YEAR = T2.YEAR AND T1.STAFFCD = T2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR ."' AND ";
        if ($model->cmd == 'edit' || $model->cmd == 'add'){
            $query .= "   T1.SCHREGNO = '". $model->schregno ."' AND ";
            $query .= "   T1.TRAINDATE = '". str_replace("/","-",$model->traindate) ."' ";
        }else{
            $query .= "   T1.SCHREGNO = '". $model->schregno ."' ";
            $query .= " ORDER BY ";
            $query .= "   T1.TRAINDATE ";
        }

        return $query;
    }


}
?>
