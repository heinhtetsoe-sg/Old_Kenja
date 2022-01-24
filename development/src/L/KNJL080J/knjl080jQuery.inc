<?php

require_once('for_php7.php');

class knjl080jQuery extends Query {

    //名称
    function GetName($namecd1,$ObjYear)
    {
        $query = "SELECT namecd2, name1, name2, namespare1, namespare2, ABBV3, NAMESPARE3 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2";
        return $query;
    }

    //対象者・合格者
    function GetLeftList($model)
    {
        $query  = "";
        $query .= " SELECT  T1.EXAMNO, ";
        if ($model->appli_type == "1") {
            //1：一次手続者
            $query .= "     case when T2.PROCEDUREDIV1 = '1' then '1' end as DIV, ";
            $query .= "     case when T2.PROCEDUREDIV1 = '1' then T2.PROCEDUREDATE1 end as PRODATE, ";
            $query .= "     '' as PRODATE1, ";
//            $query .= "     case when T1.PROCEDUREDIV  = '1' then '(二次) ' ELSE '(　　) ' end as PRODATE1, ";
        } else {
            //2：二次手続者
            $query .= "     case when T1.PROCEDUREDIV  = '1' and T1.ENTDIV = '1' then '1' end as DIV, ";
            $query .= "     case when T1.PROCEDUREDIV  = '1' then T1.PROCEDUREDATE  end as PRODATE, ";
            $query .= "     case when T2.PROCEDUREDIV1 = '1' then '(一次) ' ELSE '(　　) ' end as PRODATE1, ";
        }
        $query .= "         T1.NAME ";
        $query .= "   FROM  ENTEXAM_APPLICANTBASE_DAT T1, ";
        $query .= "         ENTEXAM_RECEPT_DAT T2 ";
        $query .= "  WHERE  T1.ENTEXAMYEAR   = T2.ENTEXAMYEAR ";
        $query .= "    AND  T1.EXAMNO        = T2.EXAMNO ";
        $query .= "    AND  T2.ENTEXAMYEAR   = '".$model->ObjYear."' ";
        $query .= "    AND  T2.TESTDIV       = '".$model->testdiv."' ";
        $query .= "    AND  T2.JUDGEDIV      = '1' ";
        if ($model->appli_type == "2") {
            $query .= "    AND  T2.PROCEDUREDIV1 = '1' ";//一次手続のみ完了した受験者だけを表示
        }
        $query .= "    AND  value(T1.PROCEDUREDIV,'0') != '2' ";
        $query .= "    AND  value(T1.ENTDIV,'0')       != '2' ";
        $query .= " ORDER BY T1.EXAMNO ";
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
            case "1":  //一次手続者
                foreach ($array as $examno)
                {
                    $query  = "UPDATE entexam_recept_dat ";
                    $query .= "   SET procedurediv1  = '1'";//手続区分 1:済み/2:無し
                    $query .= "      ,proceduredate1 = '".str_replace("/","-",$model->pro_date)."'";//手続日付
                    $query .= "      ,registercd     = '".STAFFCD."'";
                    $query .= "      ,updated        = SYSDATE()"; 
                    $query .= " WHERE entexamyear               = '".$model->ObjYear."'";
                    $query .= "   AND testdiv                   = '".$model->testdiv."'"; 
                    $query .= "   AND examno                    = '".$examno."'"; 
                    $query .= "   AND value(procedurediv1,'0') != '1' ";
                    $db->query($query);
                }
            break;
            case "2":  //二次手続者
                foreach ($array as $examno)
                {
                    $query  = "UPDATE entexam_applicantbase_dat ";
                    $query .= "   SET procedurediv  = '1'";//手続区分 1:済み/2:無し
                    $query .= "      ,entdiv        = '1'";//入学区分 1:済み/2:辞退
                    $query .= "      ,proceduredate = '".str_replace("/","-",$model->pro_date)."'";//手続日付
                    $query .= "      ,registercd = '".STAFFCD."'";
                    $query .= "      ,updated    = SYSDATE()"; 
                    $query .= " WHERE entexamyear              = '".$model->ObjYear."'";
                    $query .= "   AND examno                   = '".$examno."'"; 
                    $query .= "   AND value(procedurediv,'0') != '1' ";
                    $query .= "   AND value(entdiv,'0')       != '1' ";
                    $db->query($query);
                }
            break;
        }

        $db->commit(); 

        Query::dbCheckIn($db);
    }

    //Exec
    function &getExecQuery($model)
    {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        //合格者(右のリスト)
        $array2 = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新
        //一次手続者（ログイン日付が一次手続締切日以上の場合入学辞退。但し、一次・二次手続者は対象外）
        //二次手続者（ログイン日付が二次手続締切日以上の場合入学辞退。但し、二次手続者は対象外）
        foreach ($array as $examno)
        {
            //クエリ
            $query  = "UPDATE entexam_applicantbase_dat ";
            $query .= "   SET entdiv     = '2'";//入学区分 1:済み/2:辞退
            $query .= "      ,registercd = '".STAFFCD."'";
            $query .= "      ,updated    = SYSDATE()"; 
            $query .= " WHERE entexamyear              = '".$model->ObjYear."'";
            $query .= "   AND examno                   = '".$examno."'"; 
            $query .= "   AND value(procedurediv,'0') != '1' ";
            $query .= "   AND value(entdiv,'0')       != '1' ";
            if ($model->appli_type == "1") {
                $query .= "   AND examno not in ("; 
                $query .= "         SELECT examno ";
                $query .= "           FROM entexam_recept_dat ";
                $query .= "          WHERE entexamyear   = '".$model->ObjYear."'";
                $query .= "            AND testdiv       = '".$model->testdiv."'"; 
                $query .= "            AND examno        = '".$examno."'"; 
                $query .= "            AND procedurediv1 = '1' ";
                $query .= "                     )"; 
            }
            $db->query($query);
        }
        foreach ($array2 as $examno)
        {
            //クエリ
            $query  = "UPDATE entexam_applicantbase_dat ";
            $query .= "   SET entdiv     = '2'";//入学区分 1:済み/2:辞退
            $query .= "      ,registercd = '".STAFFCD."'";
            $query .= "      ,updated    = SYSDATE()"; 
            $query .= " WHERE entexamyear              = '".$model->ObjYear."'";
            $query .= "   AND examno                   = '".$examno."'"; 
            $query .= "   AND value(procedurediv,'0') != '1' ";
            $query .= "   AND value(entdiv,'0')       != '1' ";
            if ($model->appli_type == "1") {
                $query .= "   AND examno not in ("; 
                $query .= "         SELECT examno ";
                $query .= "           FROM entexam_recept_dat ";
                $query .= "          WHERE entexamyear   = '".$model->ObjYear."'";
                $query .= "            AND testdiv       = '".$model->testdiv."'"; 
                $query .= "            AND examno        = '".$examno."'"; 
                $query .= "            AND procedurediv1 = '1' ";
                $query .= "                     )"; 
            }
            $db->query($query);
        }

        $db->commit(); 

        Query::dbCheckIn($db);
    }
}
?>
