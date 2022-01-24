<?php

require_once('for_php7.php');

class knjl084jQuery extends Query
{

    //追加合格者回数取得
    function getMaxSuccess($year)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SUCCESS_NOTICENO) AS MAX_SUCCESS_NOTICENO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";
        $query .= "     AND SUCCESS_NOTICENO IS NOT NULL ";

        return $query;
    }

    //履歴表示
    function GetListDt($year)
    {
        $query  = " SELECT ";
        $query .= "     MIN(SUCCESS_NOTICENO) AS S_NO, ";
        $query .= "     MAX(SUCCESS_NOTICENO) AS E_NO, ";
        $query .= "     COUNT(*) AS PERSONS, ";
        $query .= "     MAX(UPDATED) AS TRNCEDATE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";
        $query .= "     AND SUCCESS_NOTICENO IS NOT NULL ";

        return $query;
    }

    //開始番号チェック
    function checkSuccess($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND SUCCESS_NOTICENO IS NOT NULL ";
        $query .= "     AND SUCCESS_NOTICENO >= '".$model->startnumber."' ";

        return $query;
    }

    //発行番号付番
    function updateNumber($model)
    {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " UPDATE ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " SET ";
        $query .= "     T1.SUCCESS_NOTICENO =  ";
        $query .= "     (SELECT ";
        $query .= "         F1.SET_NUM ";
        $query .= "      FROM ";
        $query .= "         (SELECT ";
        $query .= "             S1.ENTEXAMYEAR, ";
        $query .= "             S1.EXAMNO, ";
        $query .= "             RIGHT(RTRIM('000000' || CAST(ROW_NUMBER() OVER() + ".$model->startnumber." - 1 AS CHAR(6))), 6) AS SET_NUM ";
        $query .= "          FROM ";
        $query .= "             ENTEXAM_APPLICANTBASE_DAT S1 ";
        $query .= "          WHERE ";
        $query .= "             S1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "             AND S1.SUCCESS_NOTICENO IS NULL ";
        $query .= "             AND VALUE(S1.JUDGEMENT, '0') = '1' ";
        $query .= "             AND VALUE(S1.PROCEDUREDIV, '0') = '1' ";
        $query .= "             AND VALUE(S1.ENTDIV, '0') != '2' ";
        $query .= "          ORDER BY ";
        $query .= "             S1.EXAMNO ";
        $query .= "         ) AS F1 ";
        $query .= "      WHERE ";
        $query .= "         T1.ENTEXAMYEAR = F1.ENTEXAMYEAR ";
        $query .= "         AND T1.EXAMNO = F1.EXAMNO ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND T1.SUCCESS_NOTICENO IS NULL ";
        $query .= "     AND VALUE(T1.JUDGEMENT, '0') = '1' ";
        $query .= "     AND VALUE(T1.PROCEDUREDIV, '0') = '1' ";
        $query .= "     AND VALUE(T1.ENTDIV, '0') != '2' ";

        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        //DB切断
        Query::dbCheckIn($db);
    }

    //発行番号クリア
    function ClearNumber($model)
    {
        //DB接続
        $db = Query::dbCheckOut();

        $query  = " UPDATE ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " SET ";
        $query .= "     SUCCESS_NOTICENO = CAST(NULL AS VARCHAR(6)) ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND SUCCESS_NOTICENO IS NOT NULL ";

        $db->query($query);

        //DB切断
        Query::dbCheckIn($db);
    }
}
?>