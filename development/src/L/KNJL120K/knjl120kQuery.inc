<?php

require_once('for_php7.php');

class knjl120kQuery extends Query {

    //名称
    function GetName($namecd1,$ObjYear,$flg=0)
    {
        $query  = "SELECT namecd2, name1, name2 FROM v_name_mst";
        $query .= " WHERE year     = '".$ObjYear."'";
        $query .= "    AND namecd1 = '".$namecd1."'";
        return $query;
    }

    //合格者(手続、辞退者を含め)取得
    function GetList($model)
    {
        $query  = "SELECT T2.examno";
        $query .= "      ,T2.name ";
        $query .= "      ,T2.procedurediv ";
        $query .= "      ,T2.entdiv ";
        $query .= "  FROM entexam_applicantbase_dat T2 ";
        $query .= " WHERE T2.entexamyear  = '".$model->ObjYear."'";
        $query .= "   AND T2.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "   AND T2.testdiv      = '".$model->testdiv."'";
        $query .= "   AND T2.shdiv        = '".$model->shdiv."'";
        $query .= "   AND T2.judgement   IN ('1','2','3','4','5','6','9') ";    //合格、繰上合格、特別合格
        $query .= "   AND T2.applicantdiv NOT IN ('2') ";    //NO001 中高一貫者は除外する
        $query .= " ORDER BY T2.examno";

        return $query;
    }
    //CSV出力用リスト
    function GetCsvList($model)
    {
        //共通部分 2005.12.29 minei 共通部分抜き出し&合格者追加
        $query  = "SELECT T2.examno";
        $query .= "      ,T2.name ";
        $query .= "  FROM entexam_applicantbase_dat T2 ";
        $query .= " WHERE T2.entexamyear    = '".$model->ObjYear."'";
        $query .= "   AND T2.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "   AND T2.testdiv        = '".$model->testdiv."'";
        $query .= "   AND T2.shdiv          = '".$model->shdiv."'";
        $query .= "   AND T2.judgement      IN ('1','2','3','4','5','6','9') "; //合格、繰上合格、特別合格
        $query .= "   AND T2.applicantdiv NOT IN ('2') ";    //NO001 中高一貫者は除外する

        //合格者(未手続き者)
        if ($model->appli_type == "1") {
            $query .= "   AND coalesce(T2.procedurediv,'1') = '1'";         //2:手続き済 以外(null or 1:手続き未 nullは1に置換え)

        //合格者(手続者)
        } elseif ($model->appli_type == "2") {
            $query .= "   AND T2.procedurediv = '2'";                       //手続き済
            $query .= "   AND T2.entdiv       = '2' ";                      //入学あり

        //合格者(手続済で入学辞退者)
        } elseif ($model->appli_type == "3") {
            $query .= "   AND T2.procedurediv = '2' ";                      //手続き済み
            $query .= "   AND T2.entdiv       = '1' ";                      //入学無しまたは未設定
        }

        $query .= " ORDER BY T2.examno";

        return $query;
    }

    //2005.12.29 minei function GetRightList delete

    //UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //入学辞退者
#        if (($model->cmd == "csv" && $model->appli_type == 2) || $model->cmd == "update"){
        if (($model->cmd == "csv" && $model->appli_type == 3) || $model->cmd == "update"){
            $query  = "UPDATE entexam_applicantbase_dat ";
            $query .= "   SET procedurediv = '2'";  //手続区分　(1:未　　2:済)
            $query .= "      ,entdiv       = '1'";  //入学区分　(1:無し　2:有り)
            $query .= "      ,registercd   = '".STAFFCD."'";
            $query .= "      ,updated      = SYSDATE()"; 
            $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
            $query .= "   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "   AND testdiv      = '".$model->testdiv."'";
            /** ADD 2005/12/01 by ameku */
            if ($model->cmd == "csv"){
                $query .= "   AND judgement   IN ('1','2','3','4','5','6','9') ";    //合格、繰上合格、特別合格
                $query .= "   AND applicantdiv NOT IN ('2') ";    //NO001 中高一貫者は除外する
            }
            /** ADD 2005/12/01 by ameku */
            $query .= "   AND shdiv        = '".$model->shdiv."'";
            $query .= "   AND examno       IN ('".str_replace(",","','", $model->selectdata)."')"; //入学辞退者

            $db->query($query);
        }
        //手続き者
#        if (($model->cmd == "csv" && $model->appli_type == 1) || $model->cmd == "update"){
        if (($model->cmd == "csv" && $model->appli_type == 2) || $model->cmd == "update"){
            $query  = "UPDATE entexam_applicantbase_dat ";
            $query .= "   SET procedurediv = '2'";  //手続区分　(1:未　　2:済)
            $query .= "      ,entdiv       = '2'";  //入学区分　(1:無し　2:有り)
            $query .= "      ,registercd   = '".STAFFCD."'";
            $query .= "      ,updated      = SYSDATE()"; 
            $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
            $query .= "   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "   AND testdiv      = '".$model->testdiv."'";
            /** ADD 2005/12/01 by ameku */
            if ($model->cmd == "csv"){
                $query .= "   AND judgement   IN ('1','2','3','4','5','6','9') ";    //合格、繰上合格、特別合格
                $query .= "   AND applicantdiv NOT IN ('2') ";    //NO001 中高一貫者は除外する
            }
            /** ADD 2005/12/01 by ameku */
            $query .= "   AND shdiv        = '".$model->shdiv."'";
            $query .= "   AND examno       IN ('".str_replace(",","','", $model->selectdata2)."')"; //手続き者

            $db->query($query);
        }
        if ($model->cmd == "update"){
            $query  = "UPDATE entexam_applicantbase_dat ";
            $query .= "   SET procedurediv = '1'";    //手続区分　(1:未　　2:済)
            $query .= "      ,entdiv       = NULL ";  //入学区分　(1:無し　2:有り)
            $query .= "      ,registercd   = '".STAFFCD."'";
            $query .= "      ,updated      = SYSDATE()"; 
            $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
            $query .= "   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "   AND testdiv      = '".$model->testdiv."'";
            $query .= "   AND shdiv        = '".$model->shdiv."'";
            $query .= "   AND examno       IN ('".str_replace(",","','", $model->selectdata3)."')"; //合格者

            $db->query($query);
        }
        $db->commit(); 
        Query::dbCheckIn($db);
    }
    /** ADD 2005/12/01 by ameku */
    function &getCheckQuery(&$model,&$data)
    {
        $db = Query::dbCheckOut();

        $query = " SELECT examno FROM entexam_applicantbase_dat ";
        $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
        $query .= "   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "   AND testdiv      = '".$model->testdiv."'";
        $query .= "   AND judgement   IN ('1','2','3','4','5','6','9') ";    //合格、繰上合格、特別合格
        $query .= "   AND applicantdiv NOT IN ('2') ";    //NO001 中高一貫者は除外する
        $query .= "   AND shdiv        = '".$model->shdiv."'";
        $query .= "   AND examno      IN ('".str_replace(",","','", $data)."')"; 

        $result = $db->query($query);
        $arr = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $arr[] = $row["EXAMNO"];
        }
        Query::dbCheckIn($db);
        return $arr;
    }
}
?>
