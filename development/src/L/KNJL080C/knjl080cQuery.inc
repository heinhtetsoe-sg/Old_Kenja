<?php

require_once('for_php7.php');

class knjl080cQuery extends Query {

    //名称
    function GetName($namecd1,$ObjYear,$namecd2 = "")
    {
        $query  = "SELECT namecd2, name1, name2, namespare1, namespare2 FROM v_name_mst ";
        $query .= " WHERE year    = '".$ObjYear."' ";
        $query .= "  AND namecd1 = '".$namecd1."' ";
        if (strlen($namecd2)) {
            $query .= "  AND namecd2 = '".$namecd2."' ";
        }
        $query .= "ORDER BY namecd2";
        return $query;
    }

    //対象者
    function GetLeftList($model)
    {
        /* ------------- 手続者 ------------- */
        $query  = "  SELECT";
        $query .= "      T1.examno ,";
        $query .= "      case when T2.judgediv = '3' then '(補) ' ";
        $query .= "           when T1.special_measures is not null then '(追) ' ";
        $query .= "           when T1.judgement = '7' then '(特) ' ";
        $query .= "           else '(　) ' end as JUDGE, ";
        $query .= "      T1.name ";
        $query .= "  FROM";
        $query .= "      entexam_applicantbase_dat T1";
        $query .= "      LEFT JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "             ON T2.entexamyear  = '".$model->ObjYear."' ";
        $query .= "            AND T2.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "            AND T2.testdiv      = '".$model->testdiv."' ";
        $query .= "            AND T2.judgediv     = '3' "; //補欠合格
        $query .= "            AND T2.examno       = T1.EXAMNO ";
        $query .= "  WHERE";
        $query .= "      T1.entexamyear     = '".$model->ObjYear."' AND";
        $query .= "      T1.applicantdiv    = '".$model->applicantdiv."' AND";
        $query .= "      T1.testdiv         = '".$model->testdiv."' AND";
        $query .= "      T1.procedurediv    = '1' AND"; //手続済み
        $query .= "      T1.entdiv          = '1' AND"; //入学済み
        $query .= "      T1.judgement in ('1','7')";     //合格
                         //移行合格者を除く
        $query .= "      AND NOT EXISTS (";
        $query .= "          SELECT";
        $query .= "              'X' ";
        $query .= "          FROM";
        $query .= "              entexam_recept_dat W1";
        $query .= "          WHERE";
        $query .= "              W1.entexamyear     = '".$model->ObjYear."' AND";
        $query .= "              W1.applicantdiv    = '".$model->applicantdiv."' AND";
        $query .= "              W1.testdiv         = '".$model->testdiv."' AND";
        $query .= "              W1.judgediv        = '4' AND"; //移行合格
        if ($model->isWakayama) {
            $query .= "              T1.special_measures is null AND";   //措置が未登録
        }
        $query .= "              W1.examno          = T1.examno )";
        $query .= "  ORDER BY";
        $query .= "      T1.examno";
        return $query;
    }

    //合格者
    function GetRightList($model)
    {
        /* ------------- 合否判定での合格者 ------------- */
        $query  = "  SELECT";
        $query .= "      T1.examno ,";
        $query .= "      case when T2.judgediv = '3' then '(補) ' ";
        $query .= "           when T1.special_measures is not null then '(追) ' ";
        $query .= "           when T1.judgement = '7' then '(特) ' ";
        $query .= "           else '(　) ' end as JUDGE, ";
        $query .= "      T1.name ";
        $query .= "  FROM";
        $query .= "      entexam_applicantbase_dat T1";
        $query .= "      LEFT JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "             ON T2.entexamyear  = '".$model->ObjYear."' ";
        $query .= "            AND T2.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "            AND T2.testdiv      = '".$model->testdiv."' ";
        $query .= "            AND T2.judgediv     = '3' "; //補欠合格
        $query .= "            AND T2.examno       = T1.EXAMNO ";
        $query .= "  WHERE";
        $query .= "      T1.entexamyear     = '".$model->ObjYear."' AND";
        $query .= "      T1.applicantdiv    = '".$model->applicantdiv."' AND";
        $query .= "      T1.testdiv         = '".$model->testdiv."' AND";
        $query .= "      T1.judgement in ('1','7')";  //合格
                         //辞退者を除く
        $query .= "      AND NOT EXISTS (";
        $query .= "          SELECT";
        $query .= "              'X' ";
        $query .= "          FROM";
        $query .= "              entexam_applicantbase_dat T0";
        $query .= "          WHERE";
        $query .= "              T0.entexamyear     = '".$model->ObjYear."' AND";
        $query .= "              T0.applicantdiv    = '".$model->applicantdiv."' AND";
        $query .= "              T0.testdiv         = '".$model->testdiv."' AND";
        $query .= "             (T0.procedurediv    = '2' OR";      //手続無し
        $query .= "              T0.entdiv          = '2' ) AND";   //入学辞退
        $query .= "              T0.examno          = T1.examno )";
                         //移行合格者を除く
        $query .= "      AND NOT EXISTS (";
        $query .= "          SELECT";
        $query .= "              'X' ";
        $query .= "          FROM";
        $query .= "              entexam_recept_dat W1";
        $query .= "          WHERE";
        $query .= "              W1.entexamyear     = '".$model->ObjYear."' AND";
        $query .= "              W1.applicantdiv    = '".$model->applicantdiv."' AND";
        $query .= "              W1.testdiv         = '".$model->testdiv."' AND";
        $query .= "              W1.judgediv        = '4' AND";   //移行合格
        if ($model->isWakayama) {
            $query .= "              T1.special_measures is null AND";   //措置が未登録
        }
        $query .= "              W1.examno          = T1.examno )";
        $query .= "  ORDER BY";
        $query .= "      T1.examno";
        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        //合格者(右のリスト)
        $array2 = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        switch ($model->appli_type)
        {
            case "1":  //手続者
                foreach ($array as $examno)
                {
                    $query  = "UPDATE entexam_applicantbase_dat ";
                    $query .= "   SET procedurediv = '1'";      //手続区分 1:済み/2:無し
                    $query .= "      ,entdiv       = '1'";      //入学区分 1:済み/2:辞退
                    $query .= "      ,registercd = '".STAFFCD."'";
                    $query .= "      ,updated    = SYSDATE()"; 
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                    $query .= "   AND testdiv      = '".$model->testdiv."'";
                    $query .= "   AND examno       = '".$examno."'"; 
                    $db->query($query);
                }

                foreach ($array2 as $examno)
                {
                    $query  = "UPDATE entexam_applicantbase_dat ";
                    $query .= "   SET procedurediv = NULL";
                    $query .= "      ,entdiv       = NULL";
                    $query .= "      ,registercd = '".STAFFCD."'";
                    $query .= "      ,updated    = SYSDATE()"; 
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                    $query .= "   AND testdiv      = '".$model->testdiv."'";
                    $query .= "   AND examno       = '".$examno."'"; 
                    $db->query($query);
                }
            break;
        }

        $db->commit(); 

        Query::dbCheckIn($db);
    }
}
?>
