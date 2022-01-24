<?php

require_once('for_php7.php');

class knjl080rQuery extends Query {

    //名称
    function GetName($namecd1,$ObjYear, $namecd2="")
    {
        $query  = " SELECT ";
        $query .= "     namecd2, ";
        $query .= "     name1, ";
        $query .= "     name2, ";
        $query .= "     namespare1, ";
        $query .= "     namespare2, ";
        $query .= "     ABBV3, ";
        $query .= "     NAMESPARE3, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     v_name_mst ";
        $query .= " WHERE ";
        $query .= "     year = '".$ObjYear."' ";
        $query .= "     AND namecd1 = '".$namecd1."' ";
        if ($namecd2 != '') {
            $query .= "     AND namecd2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     namecd2 ";
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
            //2:一般入試のみ(一次)表示
            if ($model->testdiv === '2') {
                $query .= "     case when T2.PROCEDUREDIV1 = '1' then '(一次) ' ELSE '(　　) ' end as PRODATE1, ";
            } else {
                $query .= "     '' as PRODATE1, ";
            }
        }
        $query .= "         T1.NAME ";
        $query .= "   FROM  ENTEXAM_APPLICANTBASE_DAT T1, ";
        $query .= "         ENTEXAM_RECEPT_DAT T2 ";
        $query .= "  WHERE  T1.ENTEXAMYEAR   = T2.ENTEXAMYEAR ";
        $query .= "    AND  T1.APPLICANTDIV  = T2.APPLICANTDIV ";
        $query .= "    AND  T1.EXAMNO        = T2.EXAMNO ";
        $query .= "    AND  T2.ENTEXAMYEAR   = '".$model->ObjYear."' ";
        $query .= "    AND  T2.APPLICANTDIV  = '".$model->applicantdiv."' ";
        $query .= "    AND  T2.TESTDIV       = '".$model->testdiv."' ";
        $query .= "    AND  T1.JUDGEMENT     IN ('1','8') ";
        //2:一般試験の時は一次手続のみ完了した受験者だけを表示
        if ($model->appli_type == "2" && $model->testdiv === '2' ) {
            $query .= "    AND  T2.PROCEDUREDIV1 = '1' ";
        }
        $query .= "    AND  value(T1.PROCEDUREDIV,'0') != '2' ";
        $query .= "    AND  value(T1.ENTDIV,'0')       != '2' ";
        $query .= " ORDER BY T1.EXAMNO ";
        return $query;
    }

    //手続情報、入学コース取得
    function GetEnterData($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     L1.ENTER_COURSECD, ";
        $query .= "     L1.ENTER_MAJORCD, ";
        $query .= "     L1.ENTER_COURSECODE, ";
        $query .= "     T1.PROCEDUREDIV, ";
        $query .= "     T1.ENTDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ENTEXAM_COURSE_MST L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                AND L1.TESTDIV = T1.TESTDIV ";
        $query .= "                                AND L1.COURSECD = T1.SUC_COURSECD ";
        $query .= "                                AND L1.MAJORCD = T1.SUC_MAJORCD ";
        $query .= "                                AND L1.EXAMCOURSECD = T1.SUC_COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= " AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND T1.EXAMNO       = '".$examno."' ";

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
                    $query .= "   AND APPLICANTDIV              = '".$model->applicantdiv."'"; 
                    $query .= "   AND testdiv                   = '".$model->testdiv."'"; 
                    $query .= "   AND examno                    = '".$examno."'"; 
                    $query .= "   AND value(procedurediv1,'0') != '1' ";
                    $db->query($query);
                }
            break;
            case "2":  //二次手続者
                foreach ($array as $examno)
                {
                    //合格コースより入学コース取得
                    $getEnterArray = array();
                    $getEnterArray = $db->getRow(knjl080rQuery::GetEnterData($model, $examno), DB_FETCHMODE_ASSOC);
                    //二次手続者する人を対象に合格コースをセットする
                    if ($getEnterArray["PROCEDUREDIV"] !== '1' && $getEnterArray["ENTDIV"] !== '1') {
                        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
                        $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
                        $query .= "       APPLICANTDIV = '".$model->applicantdiv."' AND ";
                        $query .= "       EXAMNO       = '".$examno."' AND ";
                        $query .= "       SEQ          = '007' ";
                        
                        $db->query($query);
                        
                        $data = array();
                        $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
                        $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                        $data["EXAMNO"][TEXT]       = $examno;
                        $data["SEQ"][TEXT]          = "007";
                        $data["REMARK1"][TEXT]      = $getEnterArray["ENTER_COURSECD"];
                        $data["REMARK2"][TEXT]      = $getEnterArray["ENTER_MAJORCD"];
                        $data["REMARK3"][TEXT]      = $getEnterArray["ENTER_COURSECODE"];
                        $data["REGISTERCD"][TEXT]   = STAFFCD;
                        $data["UPDATED"][FUNC]      = "sysdate()";

                        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                        $db->query($query);
                    }
                
                    $query  = "UPDATE entexam_applicantbase_dat ";
                    $query .= "   SET procedurediv  = '1'";//手続区分 1:済み/2:無し
                    $query .= "      ,entdiv        = '1'";//入学区分 1:済み/2:辞退
                    $query .= "      ,proceduredate = '".str_replace("/","-",$model->pro_date)."'";//手続日付
                    $query .= "      ,registercd = '".STAFFCD."'";
                    $query .= "      ,updated    = SYSDATE()"; 
                    $query .= " WHERE entexamyear              = '".$model->ObjYear."'";
                    $query .= "   AND APPLICANTDIV             = '".$model->applicantdiv."'"; 
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
}
?>
