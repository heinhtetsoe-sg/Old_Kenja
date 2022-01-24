<?php

require_once('for_php7.php');

class knjl020tQuery extends Query {

    //名称取得
    function GetName($namecd1, $ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$ObjYear."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";

        return $query;
    }

    //最小受験番号
    function getMinExam($applicantDiv)
    {
        $query  = " SELECT ";
        $query .= "     MIN(T1.EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.APPLICANTDIV = '".$applicantDiv."' ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX AS SEXCD,  ";
        $query .= "     N1.NAME2 AS SEX, ";
        for ($i = 1; $i < 11; $i++) {
            $fieldname = "T2.CONFIDENTIAL_RPT" .sprintf("%02d",$i) .",";
            $query .= $fieldname;
        }
        $query .= "     T2.TOTAL3, ";
        $query .= "     T2.TOTAL5, ";
        $query .= "     T2.TOTAL_ALL, ";
        $query .= "     T2.AVERAGE_ALL, ";
        $query .= "     T2.ABSENCE_DAYS, ";
        $query .= "     T2.ABSENCE_DAYS2, ";
        $query .= "     T2.ABSENCE_DAYS3 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T2 ON ";
        $query .= "             T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "             T1.EXAMNO      = T2.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST N1 ON ";
        $query .= "             N1.NAMECD1 = 'Z002' AND ";
        $query .= "             N1.NAMECD2 = T1.SEX ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     T1.EXAMNO BETWEEN '".$model->examno."' AND '".$model->e_examno."' ";
        $query .= " ORDER BY T1.EXAMNO";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->score as $examno => $val)
        {
            //削除
            $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT";
            $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."'";
            $query .= "   AND EXAMNO = '".sprintf("%05d", $examno)."' ";
            $db->query($query);
            //追加
            $query  = "INSERT INTO ENTEXAM_APPLICANTCONFRPT_DAT";
            $query .= " VALUES ('".$model->ObjYear."'";
            $query .= "        ,'".sprintf("%05d", $examno)."'";
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
            $query .= "        ,NULL";
            $query .= "        ,NULL";
            $query .= "        ,".(strlen($val["ABSENCE_DAYS"])  ? $val["ABSENCE_DAYS"]  : "NULL");
            $query .= "        ,".(strlen($val["ABSENCE_DAYS2"]) ? $val["ABSENCE_DAYS2"] : "NULL");
            $query .= "        ,".(strlen($val["ABSENCE_DAYS3"]) ? $val["ABSENCE_DAYS3"] : "NULL");
            $query .= "        ,".$val["avg3"];         //３教科平均
            $query .= "        ,".$val["avg5"];         //５教科平均
            $query .= "        ,".$val["avgA"];         //全体平均
            $query .= "        ,".$val["sum3"];         //３教科合計
            $query .= "        ,".$val["sum5"];         //５教科合計
            $query .= "        ,".$val["sumA"];         //全体合計
            $query .= "        ,NULL";
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