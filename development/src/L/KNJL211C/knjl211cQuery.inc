<?php

require_once('for_php7.php');

class knjl211cQuery extends Query {

    //入試制度取得
    function getApctDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = 'L103' AND ";
        $query .= "     NAMECD2 = '1' ";

        return $query;
    }

    //プレテスト区分を取得
    function getPreTestdiv($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAME1 AS CSV_LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = 'L104' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //ＣＳＶ作成
    function selectCsvQuery($model)
    {
        $sort = explode(',', $model->selectdata);

        $query  = " SELECT ";
        $query .= "     T1.PRE_RECEPTNO, ";
        $query .= "     N2.NAME1 AS PRE_EXAM_TYPE, ";
        $query .= "     T1.PRE_RECEPTDATE, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     N1.ABBV1 AS SEX, ";
        $query .= "     T1.GNAME, ";
        $query .= "     T1.GKANA, ";
        $query .= "     T1.ZIPCD, ";
        $query .= "     T1.ADDRESS1, ";
        $query .= "     T1.ADDRESS2, ";
        $query .= "     T1.TELNO, ";
        $query .= "     F1.FINSCHOOL_NAME AS FINSCHOOLNAME, ";
        $query .= "     P1.PRISCHOOL_NAME AS PS_CD, ";
        $query .= "     CASE T1.PS_CONTACT WHEN '1' THEN '○' ELSE '' END PS_CONTACT, ";
        $query .= "     T1.BUS_USE, ";
        $query .= "     T1.STATIONDIV, ";
        $query .= "     CASE WHEN '3' = T1.STATIONDIV THEN 0 ELSE 1 END AS STATIONDIV3, ";
        $query .= "     CASE WHEN '1' = T1.STATIONDIV THEN 0 ELSE 1 END AS STATIONDIV1, ";
        $query .= "     CASE WHEN '2' = T1.STATIONDIV THEN 0 ELSE 1 END AS STATIONDIV2, ";
        $query .= "     T1.BUS_USER_COUNT, ";
        $query .= "     T1.RECOM_EXAMNO, ";
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_PRE_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1='Z002' AND T1.SEX = N1.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L105' AND N2.NAMECD2 = T1.PRE_EXAM_TYPE ";
        $query .= "     LEFT JOIN PRISCHOOL_MST P1 ON T1.PS_CD = P1.PRISCHOOLCD ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ON T1.FS_CD = F1.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '1' AND ";
        $query .= "     T1.PRE_TESTDIV  = '".$model->pre_testdiv."' ";
        $query .= " ORDER BY ";
        for($i = 0; $i < get_count($sort); $i++){
            if($i != "0")                       $query .= " , ";
            if($sort[$i] == 'PRE_RECEPTNO')     $query .= " T1.PRE_RECEPTNO ";
            if($sort[$i] == 'PRE_RECEPTDATE')   $query .= " T1.PRE_RECEPTDATE ";
            if($sort[$i] == 'PRE_EXAM_TYPE')    $query .= " T1.PRE_EXAM_TYPE ";
            if($sort[$i] == 'PS_CD')            $query .= " T1.PS_CD ";
            if($sort[$i] == 'STATIONDIV1')      $query .= " STATIONDIV1 ";
            if($sort[$i] == 'STATIONDIV2')      $query .= " STATIONDIV2 ";
            if($sort[$i] == 'STATIONDIV3')      $query .= " STATIONDIV3 ";
        }
        $query .= " , T1.PRE_RECEPTNO ";

        return $query;
    }
}
?>
