<?php

require_once('for_php7.php');

class knjl081oQuery extends Query {
    /*
        基礎データと受付データを結合するときにAPPLICANTDIVは条件にしない
        APPLICANTDIVが複数存在することはありえないため
    */

    //名称
    function GetName($namecd1,$ObjYear)
    {
        $query = "SELECT namecd2, name1, name2, namespare1, namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2";
        return $query;
    }

    //入試区分マスタ
    function getTestdivMst($ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS NAMECD2, ";
        $query .= "     NAME AS NAME1, ";
        $query .= "     SHOWDIV AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     TESTDIV ";
        return $query;
    }

    //対象者
    function GetLeftList($model)
    {
        $query  = "";
        /* ------------- 手続者 ------------- */
        if ($model->appli_type == "3") {
            //大宮-作業依頼書20050117-01.docによる修正
            $query  = "  SELECT";
            $query .= "      T1.examno ,";
            $query .= "      T1.name,";
#            $query .= "      CASE WHEN T2.adjournmentdiv = '1' THEN '(延) ' ELSE '(　) ' END AS adjournment";
            $query .= "      '(' || CASE T2.adjournmentdiv WHEN '1' THEN '延' ELSE '　' END ||";
            $query .= "      ''  || CASE T1.special_measures WHEN '1' THEN '繰' WHEN '2' THEN '特' ELSE '　' END || ') ' AS adjournment";
            $query .= "  FROM";
            $query .= "      entexam_applicantbase_dat T1";
            $query .= "      LEFT OUTER JOIN (";
            $query .= "                          SELECT DISTINCT";
            $query .= "                              entexamyear,";
            $query .= "                              examno,";
            $query .= "                              '1' AS adjournmentdiv";
            $query .= "                          FROM";
            $query .= "                              entexam_recept_dat";
            $query .= "                          WHERE";
            $query .= "                              entexamyear    = '".$model->ObjYear."' AND";
            $query .= "                              adjournmentdiv = '1'"; //延期区分有り
            $query .= "                          ) T2 ON T2.entexamyear = T1.entexamyear AND T2.examno = T1.examno ";
            $query .= "  WHERE";
            $query .= "      T1.entexamyear     = '".$model->ObjYear."' AND";
            $query .= "      T1.applicantdiv    = '".$model->applicantdiv."' AND";
            $query .= "      T1.procedurediv    = '1' AND";             //手続済み
#            $query .= "      T1.entdiv          = '1' AND";             //入学済み
#            $query .= "      T1.judgement       IN ('1','2','3')";      //合格、繰上合格、特別合格
            $query .= "      T1.judgement       = '1'";  //合格
            $query .= "  ORDER BY";
            $query .= "      T1.examno";

        /* ------------- 特待対象 or 手続延期者 ------------- */
        } else { 

            $query  = "SELECT T1.examno";
            $query .= "      ,T2.name ";
            $query .= "      ,T1.total_rank4";
            $query .= "      ,T1.TOTAL4";
            $query .= "  FROM entexam_recept_dat T1 ";
            $query .= "      ,entexam_applicantbase_dat T2 ";
            $query .= " WHERE T1.entexamyear  = T2.entexamyear ";
            $query .= "   AND T1.examno       = T2.examno ";
            $query .= "   AND T1.entexamyear  = '".$model->ObjYear."'";
            $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
            $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
            $query .= "   AND T1.judgediv     = '1'";                   //合格
            switch ($model->appli_type) 
            {
                case "1":
                    $query .= " AND T1.honordiv = '1'";                 //特待対象者
                    $query .= " ORDER BY T1.total_rank4, T1.examno";
                    break;
                case "2":
                    $query .= " AND T1.adjournmentdiv = '1'";           //手続延期者
                    $query .= " ORDER BY T1.examno";
                    break; 
            }
        }
        return $query;
    }

    //合格者
    function GetRightList($model)
    {
        //対象者が全体の場合(受付データに登録されていなくても表示する）
        /* ------------- 合否判定での合格者 ------------- */
        if ($model->appli_type == "3") {
            //大宮-作業依頼書20050117-01.docによる修正
            $query  = "  SELECT";
            $query .= "      T1.examno ,";
            $query .= "      T1.name,";
#            $query .= "      CASE WHEN T2.adjournmentdiv = '1' THEN '(延) ' ELSE '(　) ' END AS adjournment,";
            $query .= "      '(' || CASE T2.adjournmentdiv WHEN '1' THEN '延' ELSE '　' END || ";
            $query .= "      ''  || CASE T1.special_measures WHEN '1' THEN '繰' WHEN '2' THEN '特' ELSE '　' END || ') ' AS adjournment";
            $query .= "  FROM";
            $query .= "      entexam_applicantbase_dat T1";
            $query .= "      LEFT OUTER JOIN (";
            $query .= "                          SELECT DISTINCT";
            $query .= "                              entexamyear,";
            $query .= "                              examno,";
            $query .= "                              '1' AS adjournmentdiv";
            $query .= "                          FROM";
            $query .= "                              entexam_recept_dat";
            $query .= "                          WHERE";
            $query .= "                              entexamyear    = '".$model->ObjYear."' AND";
            $query .= "                              adjournmentdiv = '1'"; //延期区分有り
            $query .= "                          ) T2 ON T2.entexamyear = T1.entexamyear AND T2.examno = T1.examno ";
            $query .= "  WHERE";
            $query .= "      T1.entexamyear     = '".$model->ObjYear."' AND";
            $query .= "      T1.applicantdiv    = '".$model->applicantdiv."' AND";
#            $query .= "      T1.judgement       IN ('1','2','3')";  //合格、繰上合格、特別合格
            $query .= "      T1.judgement       = '1'";  //合格
            $query .= "  ORDER BY";
            $query .= "      T1.examno";

        /* ------------- 試験区分での合格者 ------------- */
        } else {
            $query  = "SELECT T1.examno";
            $query .= "      ,T2.name ";
            $query .= "      ,T1.total_rank4 ";
            $query .= "      ,T1.total4 ";
            $query .= "  FROM entexam_recept_dat T1 ";
            $query .= "      ,entexam_applicantbase_dat T2 ";
            $query .= " WHERE T1.entexamyear  = T2.entexamyear ";
            $query .= "   AND T1.examno       = T2.examno ";
            $query .= "   AND T1.entexamyear  = '".$model->ObjYear."'";
            $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
            $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
            $query .= "   AND T1.judgediv     = '1'";               //合格
            if ($model->appli_type == "1") {
                $query .= " ORDER BY T1.total_rank4, T1.examno";    //特待対象者
            } else {
                //あった方がいい？   $query .= "   AND T1.examno NOT IN (SELECT examno FROM entexam_applicantbase_dat WHERE entexamyear = '".$model->ObjYear."' AND applicantdiv = '".$model->applicantdiv."' AND procedurediv = '1')"; //入学手続者は除く
                $query .= " ORDER BY T1.examno ";                   //手続延期者
            }
        }
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
            case "1":  //特待対象者
                foreach ($array as $examno)
                {
                    $query  = "UPDATE entexam_recept_dat ";
                    $query .= "   SET honordiv   = '1'";
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
                    $query  = "UPDATE entexam_recept_dat ";
                    $query .= "   SET honordiv = NULL";
                    $query .= "      ,registercd = '".STAFFCD."'";
                    $query .= "      ,updated    = SYSDATE()"; 
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                    $query .= "   AND testdiv      = '".$model->testdiv."'";
                    $query .= "   AND examno       = '".$examno."'"; 
                    $db->query($query);
                }
            break;

            case "2":  //手続延期者
                foreach ($array as $examno)
                {
                    $query  = "UPDATE entexam_recept_dat ";
                    $query .= "   SET adjournmentdiv = '1'";    //延期区分 1:延期あり
                    $query .= "      ,registercd = '".STAFFCD."'";
                    $query .= "      ,updated    = SYSDATE()"; 
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                    $query .= "   AND testdiv      = '".$model->testdiv."'";
                    $query .= "   AND examno       = '".$examno."'"; 
                    $db->query($query);

                    $query  = "UPDATE entexam_applicantbase_dat ";
                    $query .= "   SET procedurediv = '2'";
                    $query .= "      ,registercd = '".STAFFCD."'";
                    $query .= "      ,updated    = SYSDATE()"; 
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                    $query .= "   AND testdiv      = '".$model->testdiv."'";
                    $query .= "   AND examno       = '".$examno."'"; 
                    $query .= "   AND (procedurediv <> '1' OR procedurediv IS NULL)";   //既に(1：済み)の場合は更新しない
                    $db->query($query);
                }

                foreach ($array2 as $examno)
                {
                    $query  = "UPDATE entexam_recept_dat ";
                    $query .= "   SET adjournmentdiv = NULL";
                    $query .= "      ,registercd = '".STAFFCD."'";
                    $query .= "      ,updated    = SYSDATE()"; 
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                    $query .= "   AND testdiv      = '".$model->testdiv."'";
                    $query .= "   AND examno       = '".$examno."'"; 
                    $db->query($query);

                    $query  = "UPDATE entexam_applicantbase_dat ";
                    $query .= "   SET procedurediv = NULL";
                    $query .= "      ,registercd = '".STAFFCD."'";
                    $query .= "      ,updated    = SYSDATE()"; 
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                    $query .= "   AND testdiv      = '".$model->testdiv."'";
                    $query .= "   AND examno       = '".$examno."'"; 
                    $query .= "   AND procedurediv <> '1'";   //既に(1：済み)の場合は更新しない
                    $db->query($query);
                }
            break;

            case "3":  //手続者
                foreach ($array as $examno)
                {
                    //入試区分にかかわらず受付データのHONORDIVが１が１件以上ある場合は基礎データのHONORDIVも1にする
                    $query  = "SELECT COUNT(*) ";
                    $query .= "  FROM entexam_recept_dat ";
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                    $query .= "   AND honordiv     = '1'";
                    $query .= "   AND examno       = '".$examno."'";
                    $cnt = $db->getOne($query); 

                    $query  = "UPDATE entexam_applicantbase_dat ";
                    $query .= "   SET procedurediv = '1'";      //手続区分 1:済み/2:無し
                    $query .= "      ,entdiv       = '1'";      //入学区分 1:済み/2:辞退
                    if ($cnt > 0) {
                        $query .= "  ,honordiv     = '1'";      //特待区分 1:対象
                    } else {
                        $query .= "  ,honordiv     = NULL";
                    }
                    $query .= "      ,registercd = '".STAFFCD."'";
                    $query .= "      ,updated    = SYSDATE()"; 
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                    $query .= "   AND examno       = '".$examno."'"; 
                    $db->query($query);
                }

                foreach ($array2 as $examno)
                {
                    $query  = "UPDATE entexam_applicantbase_dat ";
                    $query .= "   SET procedurediv = '2'";
                    $query .= "      ,entdiv       = NULL";
                    $query .= "      ,honordiv     = NULL";
                    $query .= "      ,registercd = '".STAFFCD."'";
                    $query .= "      ,updated    = SYSDATE()"; 
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
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
