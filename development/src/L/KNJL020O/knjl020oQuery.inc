<?php

require_once('for_php7.php');

class knjl020oQuery extends Query {

    //名称
    function GetName($namecd1,$ObjYear)
    {
        $query = "SELECT namecd2, name1, name2,namespare1 , namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'";

        return $query;
    }
    
    //一覧表示
    function SelectQuery($model)
    {
        $query  = "SELECT T2.* ";
        $query .= "      ,T1.examno ";
        $query .= "      ,substr(T1.name,1,30)         AS name";
        $query .= "      ,substr(T1.name_kana,1,60)    AS name_kana ";
        $query .= "      ,T1.sex AS sexcd ";
        $query .= "      ,MEISYOU_GET(T1.sex,'Z002',2) AS sex ";
        $query .= "  FROM entexam_applicantbase_dat T1 ";
        $query .= "       LEFT OUTER JOIN entexam_applicantconfrpt_dat T2 ";
        $query .= "                    ON T1.entexamyear = T2.entexamyear ";
        $query .= "                   AND T1.examno      = T2.examno ";
        $query .= " WHERE T1.entexamyear    = '".$model->ObjYear."'";
        $query .= "   AND T1.applicantdiv   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.examno BETWEEN '".$model->examno."' AND '".$model->e_examno."' ";
        $query .= " ORDER BY T1.examno";
        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = "DELETE FROM entexam_applicantconfrpt_dat";
        $query .= " WHERE entexamyear    = '".$model->ObjYear."'";
        $query .= "   AND examno IN ('".implode("','",array_keys($model->score))."')";
        $db->query($query);

        foreach ($model->score as $examno => $val)
        {
            $query  = "INSERT INTO entexam_applicantconfrpt_dat";
            $query .= " VALUES ('".$model->ObjYear."'";
            $query .= "        ,'".$examno."'";
            $query .= "        ,".(strlen($val["RPT01"]) ? $val["RPT01"] : "NULL");
            $query .= "        ,".(strlen($val["RPT02"]) ? $val["RPT02"] : "NULL");
            $query .= "        ,".(strlen($val["RPT03"]) ? $val["RPT03"] : "NULL");
            $query .= "        ,".(strlen($val["RPT04"]) ? $val["RPT04"] : "NULL");
            $query .= "        ,".(strlen($val["RPT05"]) ? $val["RPT05"] : "NULL");
            $query .= "        ,".(strlen($val["RPT06"]) ? $val["RPT06"] : "NULL");
            $query .= "        ,".(strlen($val["RPT07"]) ? $val["RPT07"] : "NULL");
            $query .= "        ,".(strlen($val["RPT08"]) ? $val["RPT08"] : "NULL");
            $query .= "        ,".(strlen($val["RPT09"]) ? $val["RPT09"] : "NULL");
            $query .= "        ,".(strlen($val["RPT10"]) ? $val["RPT10"] : "NULL");
            $query .= "        ,".(strlen($val["RPT11"]) ? $val["RPT11"] : "NULL");
            $query .= "        ,".(strlen($val["RPT12"]) ? $val["RPT12"] : "NULL");
            $query .= "        ,".(strlen($val["absence_days"]) ? $val["absence_days"] : "NULL");
            $query .= "        ,".$val["avg5"];         //５教科平均
            $query .= "        ,".$val["avgA"];         //全体平均
            $query .= "        ,'".STAFFCD."'";
            $query .= "        ,SYSDATE()";
            $query .= ")";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>