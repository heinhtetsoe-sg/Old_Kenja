<?php

require_once('for_php7.php');

class knjl012oquery extends Query {

    //名称
    function GetName($namecd1,$ObjYear)
    {
        $query = "SELECT namecd2, name1, name2, namespare2 FROM v_name_mst
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

    function ExecuteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "DELETE FROM entexam_desire_dat ";
        $query .= " WHERE entexamyear  = '".$model->ObjYear."'";            //入試年度
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";       //入試制度
        $query .= "   AND testdiv      = '".$model->testdiv."'";            //出願区分
        $db->query($query);

        // 基礎データのAPPLICANTDIVが複数になることはありえないので結合条件にしない //

        $query  = "INSERT INTO entexam_desire_dat ";

        $query .= "SELECT '".$model->ObjYear."'";
        $query .= "      ,'".$model->applicantdiv."'";
        $query .= "      ,'".$model->testdiv."'";
        $query .= "      ,T1.examno ";
        $query .= "      ,'1' AS applicant_div ";         //志願　1:有り/2:無し
        $query .= "      ,T1.examnee_div";                //受験　1:有り/2:欠席
        $query .= "      ,'".STAFFCD."'";
        $query .= "      ,SYSDATE()";
        $query .= " FROM (";

        /* ｎ回目出願者(その出願回に受付していなくても抽出) */
        $fieldName = "T1.testdiv" .$model->testdiv;
        $query .= "SELECT T1.examno,CASE value(W1.receptno,'') WHEN '' THEN '2' ELSE '1' END AS examnee_div";    //受付が無ければ欠席(2)で登録
        $query .= "  FROM entexam_applicantbase_dat T1";
        $query .= "       LEFT OUTER JOIN entexam_recept_dat W1 ";
        $query .= "                    ON W1.entexamyear  = T1.entexamyear AND";
        $query .= "                       W1.testdiv      = ".$fieldName." AND";
        $query .= "                       W1.examno       = T1.examno ";
        $query .= " WHERE T1.entexamyear  = '".$model->ObjYear."' ";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND ".$fieldName."  = '".$model->testdiv."' ";

        /* ｎ回目に受付している志願者(当日受験者) */
        $query .= " UNION ";
        $query .= "SELECT T1.examno,'1' AS examnee_div";                            //受付があるので有り(1)で登録
        $query .= "  FROM entexam_applicantbase_dat T1 ";
        $query .= "      ,entexam_recept_dat        T2 ";
        $query .= " WHERE T1.entexamyear  = '".$model->ObjYear."' ";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND ".$fieldName."  is null ";
        $query .= "   AND T2.entexamyear  = T1.entexamyear ";
        $query .= "   AND T2.examno       = T1.examno ";
        $query .= "   AND T2.testdiv      = '".$model->testdiv."' ";                //今回受付

        $query .= " ) T1";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
