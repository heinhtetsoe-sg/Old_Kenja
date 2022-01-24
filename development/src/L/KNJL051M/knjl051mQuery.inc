<?php

require_once('for_php7.php');

class knjl051mQuery extends Query 
{

    //会場データ
    function getHallDat($model)
    {

        $query  = " WITH HALL AS ( ";
        $query .= " SELECT ";
        $query .= "     EXAMHALL_NAME, ";
        $query .= "     EXAMHALLCD, ";
        $query .= "     CAST(RIGHT(EXAMHALLCD, 1) AS CHAR(1)) AS CDNAME, ";
        $query .= "     S_RECEPTNO, ";
        $query .= "     E_RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= "     TESTDIV = '1' ";
        $query .= "     AND EXAM_TYPE = '1' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.EXAMHALL_NAME, ";
        $query .= "     T1.EXAMHALLCD, ";
        $query .= "     T1.CDNAME, ";
        $query .= "     T1.S_RECEPTNO, ";
        $query .= "     T1.E_RECEPTNO, ";
        $query .= "     COUNT(*) AS RECEPT_CNT ";
        $query .= " FROM ";
        $query .= "     HALL T1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT L1 ON L1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "          AND APPLICANTDIV = '1' ";
        $query .= "          AND TESTDIV = '1' ";
        $query .= "          AND EXAM_TYPE = '1' ";
        $query .= "          AND RECEPTNO BETWEEN T1.S_RECEPTNO AND T1.E_RECEPTNO ";
        $query .= " GROUP BY ";
        $query .= "     T1.EXAMHALL_NAME, ";
        $query .= "     T1.EXAMHALLCD, ";
        $query .= "     T1.CDNAME, ";
        $query .= "     T1.S_RECEPTNO, ";
        $query .= "     T1.E_RECEPTNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMHALLCD ";

        return $query;
    }

    //科目詳細
    function getSubclassDetail($model)
    {

        $query  = " SELECT ";
        $query .= "     T1.NAME1 || '-' || L1.TESTPAPERCD AS LABEL, ";
        $query .= "     T1.NAMECD2 || '-' ||L1.TESTPAPERCD || '-' || CASE WHEN L1.PERFECT < 10 THEN CAST(L1.PERFECT AS CHAR(1)) ";
        $query .= "                                                       WHEN L1.PERFECT < 100 THEN CAST(L1.PERFECT AS CHAR(2)) ";
        $query .= "                                                       WHEN L1.PERFECT < 1000 THEN CAST(L1.PERFECT AS CHAR(3)) ";
        $query .= "                                                       ELSE '' ";
        $query .= "                                                  END ";
        $query .= "     AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     LEFT JOIN ENTEXAM_PERFECT_DETAIL_MST L1 ON  T1.NAMECD2 = L1.TESTSUBCLASSCD ";
        $query .= "                                             AND T1.YEAR    = L1.ENTEXAMYEAR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.NAMECD1 = 'L009' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //得点データ
    function getInputScoreCnt($model, $subPaperCd, $sReceptno, $eReceptno)
    {
        list($subclassCd, $paperCd, $perfect) = preg_split("/-/", $subPaperCd);

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS INPUT_CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '1' ";
        $query .= "     AND TESTDIV = '1' ";
        $query .= "     AND EXAM_TYPE = '1' ";
        $query .= "     AND RECEPTNO BETWEEN '".$sReceptno."' AND '".$eReceptno."' ";
        $query .= "     AND TESTSUBCLASSCD = '".$subclassCd."' ";
        $query .= "     AND TESTPAPERCD = '".$paperCd."' ";

        return $query;
    }

}
?>