<?php

require_once('for_php7.php');

class knjl280yQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称
    function GetName($namecd1,$ObjYear) {
        $query = "SELECT namecd2, name1, name2, namespare1, namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2";
        return $query;
    }

    //対象者
    function GetLeftList($model) {
        /* ------------- 手続者 ------------- */
        $query  = "  SELECT";
        $query .= "      T1.EXAMNO, ";
        $query .= "      T1.NAME, ";
        $query .= "      T1.PAY_MONEY, ";
        $query .= "      T1.PROCEDUREDATE ";
        $query .= "  FROM";
        $query .= "      ENTEXAM_APPLICANTBASE_DAT T1";
        $query .= "  LEFT JOIN ";
        $query .= "      NAME_MST L1 ON  L1.NAMECD2 = T1.JUDGEMENT ";
        $query .= "                  AND L1.NAMECD1 = 'L013' ";
        $query .= "  LEFT JOIN ";
        $query .= "      ENTEXAM_RECEPT_DAT T2 ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                            AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                            AND T2.TESTDIV      = '".$model->testdiv."' ";
        $query .= "                            AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "  LEFT JOIN ";
        $query .= "      NAME_MST L3 ON  L3.NAMECD2 = T2.JUDGEDIV ";
        $query .= "                  AND L3.NAMECD1 = 'L013' ";
        $query .= "  WHERE";
        $query .= "      T1.ENTEXAMYEAR     = '".$model->ObjYear."' AND";
        $query .= "      T1.APPLICANTDIV    = '".$model->applicantdiv."' AND";
        $query .= "      T1.PROCEDUREDIV    = '1' AND"; //手続済み
        $query .= "      T1.ENTDIV          = '1' AND"; //入学済み
        $query .= "      L1.NAMESPARE1      = '1' AND";  //合格
        $query .= "      L3.NAMESPARE1      = '1'";  //合格
                         //辞退者を除く
        $query .= "      AND NOT EXISTS (";
        $query .= "          SELECT";
        $query .= "              'X' ";
        $query .= "          FROM";
        $query .= "              ENTEXAM_APPLICANTBASE_DAT T0";
        $query .= "          WHERE";
        $query .= "              T0.ENTEXAMYEAR     = '".$model->ObjYear."' AND";
        $query .= "              T0.APPLICANTDIV    = '".$model->applicantdiv."' AND";
        $query .= "             (T0.PROCEDUREDIV    = '2' OR";      //手続無し
        $query .= "              T0.ENTDIV          = '2' ) AND";   //入学辞退
        $query .= "              T0.EXAMNO          = T1.EXAMNO )";
        $query .= "  ORDER BY";
        $query .= "      T1.EXAMNO";
        return $query;
    }

    //合格者
    function GetRightList($model) {
        /* ------------- 合否判定での合格者 ------------- */
        $query  = "  SELECT";
        $query .= "      T1.EXAMNO ,";
        $query .= "      T1.NAME, ";
        $query .= "      VALUE(L2.ENT_MONEY, 0) AS PAY_MONEY ";
        $query .= "  FROM";
        $query .= "      ENTEXAM_APPLICANTBASE_DAT T1";
        $query .= "  LEFT JOIN ";
        $query .= "      NAME_MST L1 ON  L1.NAMECD2 = T1.JUDGEMENT ";
        $query .= "                  AND L1.NAMECD1 = 'L013' ";
        $query .= "  LEFT JOIN ";
        $query .= "      ENTEXAM_PAYMENT_MONEY_MST L2 ON L2.APPLICANTDIV = T1.APPLICANTDIV AND L2.JUDGE_KIND = '0' ";
        $query .= "  LEFT JOIN ";
        $query .= "      ENTEXAM_RECEPT_DAT T2 ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                            AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                            AND T2.TESTDIV      = '".$model->testdiv."' ";
        $query .= "                            AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "  LEFT JOIN ";
        $query .= "      NAME_MST L3 ON  L3.NAMECD2 = T2.JUDGEDIV ";
        $query .= "                  AND L3.NAMECD1 = 'L013' ";
        $query .= "  WHERE";
        $query .= "      T1.ENTEXAMYEAR     = '".$model->ObjYear."' AND";
        $query .= "      T1.APPLICANTDIV    = '".$model->applicantdiv."' AND";
        $query .= "      L1.NAMESPARE1      = '1' AND";  //合格
        $query .= "      L3.NAMESPARE1      = '1'";  //合格
                         //辞退者を除く
        $query .= "      AND NOT EXISTS (";
        $query .= "          SELECT";
        $query .= "              'X' ";
        $query .= "          FROM";
        $query .= "              ENTEXAM_APPLICANTBASE_DAT T0";
        $query .= "          WHERE";
        $query .= "              T0.ENTEXAMYEAR     = '".$model->ObjYear."' AND";
        $query .= "              T0.APPLICANTDIV    = '".$model->applicantdiv."' AND";
        $query .= "             (T0.PROCEDUREDIV    = '2' OR";      //手続無し
        $query .= "              T0.ENTDIV          = '2' ) AND";   //入学辞退
        $query .= "              T0.EXAMNO          = T1.EXAMNO )";
        $query .= "  ORDER BY";
        $query .= "      T1.EXAMNO";
        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        //合格者(右のリスト)
        $array2 = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        switch ($model->appli_type) {
            case "1":  //手続者
                foreach ($array as $examData) {
                    list($examno, $paymoney) = explode(":", $examData);
                    $query  = "UPDATE ENTEXAM_APPLICANTBASE_DAT ";
                    $query .= "   SET PROCEDUREDIV  = '1'";      //手続区分 1:済み/2:無し
                    $query .= "      ,ENTDIV        = '1'";      //入学区分 1:済み/2:辞退
                    $query .= "      ,PROCEDUREDATE = CASE WHEN PROCEDUREDATE IS NULL "; //手続き日付
                    $query .= "                            THEN '". str_replace("/", "-", $model->proceduredate) ."' ";
                    $query .= "                            ELSE PROCEDUREDATE ";
                    $query .= "                       END ";
                    $query .= "      ,PAY_MONEY     = CASE WHEN PAY_MONEY IS NULL "; //手続き日付
                    $query .= "                            THEN ". $paymoney ." ";
                    $query .= "                            ELSE PAY_MONEY ";
                    $query .= "                       END ";
                    $query .= "      ,REGISTERCD = '".STAFFCD."'";
                    $query .= "      ,UPDATED    = SYSDATE()";
                    $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."'";
                    $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
                    $query .= "   AND EXAMNO       = '".$examno."'";
                    $db->query($query);
                                      
                }

                foreach ($array2 as $examData) {
                    list($examno, $paymoney) = explode(":", $examData);
                    $query  = "UPDATE ENTEXAM_APPLICANTBASE_DAT ";
                    $query .= "   SET PROCEDUREDIV  = NULL";
                    $query .= "      ,ENTDIV        = NULL";
                    $query .= "      ,PROCEDUREDATE = NULL";
                    $query .= "      ,PAY_MONEY     = NULL";
                    $query .= "      ,REGISTERCD = '".STAFFCD."'";
                    $query .= "      ,UPDATED    = SYSDATE()";
                    $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."'";
                    $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
                    $query .= "   AND EXAMNO       = '".$examno."'";
                    $db->query($query);
                }
            break;
        }

        $db->commit();

        Query::dbCheckIn($db);
    }
}
?>
