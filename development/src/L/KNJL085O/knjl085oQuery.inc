<?php

require_once('for_php7.php');

class knjl085oQuery extends Query {
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
        //重複受験番号
        $query  = " WITH T_RECOM_EXAMNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         RECOM_EXAMNO1 AS RECOM_EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "         AND RECOM_EXAMNO1 IS NOT NULL ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         RECOM_EXAMNO2 AS RECOM_EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "         AND RECOM_EXAMNO2 IS NOT NULL ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         RECOM_EXAMNO3 AS RECOM_EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "         AND RECOM_EXAMNO3 IS NOT NULL ";
        $query .= "     ) ";
        //特別進学クラス合格
        $query .= " , T_EXAMNO_JUDGE AS ( ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "         AND JUDGEDIV = '1' ";          //1:合格
        $query .= "         AND JUDGECLASS IN ('1','4','6') "; //1:特別進学合格 4:スライド合格 6:非正規合格
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         T2.EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT T1 ";
        $query .= "         INNER JOIN T_RECOM_EXAMNO T2 ON T2.RECOM_EXAMNO = T1.EXAMNO ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "         AND T1.JUDGEDIV = '1' ";
        $query .= "         AND T1.JUDGECLASS IN ('1','4','6') ";
        $query .= "     ) ";

        $query .= "  SELECT";
        $query .= "      T1.examno, ";
        $query .= "      T1.name ";
        $query .= "  FROM";
        $query .= "      entexam_applicantbase_dat T1";
        $query .= "      INNER JOIN (";
        $query .= "          SELECT ";
        $query .= "              EXAMNO ";
        $query .= "          FROM ";
        $query .= "              T_EXAMNO_JUDGE ";
        $query .= "          GROUP BY ";
        $query .= "              EXAMNO ";
        $query .= "          HAVING ";
        $query .= "              2 < COUNT(*) ";
        $query .= "      ) T2 ON T2.examno = T1.examno ";
        if ($model->testdiv != "9") {
            $query .= "  INNER JOIN ENTEXAM_RECEPT_DAT T3 ON  T3.entexamyear = '".$model->ObjYear."' ";
            $query .= "                                   AND T3.applicantdiv = '".$model->applicantdiv."' ";
            $query .= "                                   AND T3.testdiv = '".$model->testdiv."' ";
            $query .= "                                   AND T3.examno = T1.examno ";
        }
        $query .= "  WHERE";
        $query .= "      T1.entexamyear     = '".$model->ObjYear."' AND";
        $query .= "      T1.applicantdiv    = '".$model->applicantdiv."' AND";
        $query .= "      T1.special_measures = '3' AND";    //3:特別アップ合格
        $query .= "      T1.entclass        = '2' AND";     //2:英数特科クラス
        $query .= "      T1.judgement       = '1'";         //1:合格
        $query .= "  ORDER BY";
        $query .= "      T1.examno";

        return $query;
    }

    //合格者
    function GetRightList($model)
    {
        //重複受験番号
        $query  = " WITH T_RECOM_EXAMNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         RECOM_EXAMNO1 AS RECOM_EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "         AND RECOM_EXAMNO1 IS NOT NULL ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         RECOM_EXAMNO2 AS RECOM_EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "         AND RECOM_EXAMNO2 IS NOT NULL ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         RECOM_EXAMNO3 AS RECOM_EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "         AND RECOM_EXAMNO3 IS NOT NULL ";
        $query .= "     ) ";
        //特別進学クラス合格
        $query .= " , T_EXAMNO_JUDGE AS ( ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "         AND JUDGEDIV = '1' ";          //1:合格
        $query .= "         AND JUDGECLASS IN ('1','4','6') "; //1:特別進学合格 4:スライド合格 6:非正規合格
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         T2.EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT T1 ";
        $query .= "         INNER JOIN T_RECOM_EXAMNO T2 ON T2.RECOM_EXAMNO = T1.EXAMNO ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "         AND T1.JUDGEDIV = '1' ";
        $query .= "         AND T1.JUDGECLASS IN ('1','4','6') ";
        $query .= "     ) ";

        $query .= "  SELECT";
        $query .= "      T1.examno, ";
        $query .= "      T1.name ";
        $query .= "  FROM";
        $query .= "      entexam_applicantbase_dat T1";
        $query .= "      INNER JOIN (";
        $query .= "          SELECT ";
        $query .= "              EXAMNO ";
        $query .= "          FROM ";
        $query .= "              T_EXAMNO_JUDGE ";
        $query .= "          GROUP BY ";
        $query .= "              EXAMNO ";
        $query .= "          HAVING ";
        $query .= "              2 < COUNT(*) ";
        $query .= "      ) T2 ON T2.examno = T1.examno ";
        if ($model->testdiv != "9") {
            $query .= "  INNER JOIN ENTEXAM_RECEPT_DAT T3 ON  T3.entexamyear = '".$model->ObjYear."' ";
            $query .= "                                   AND T3.applicantdiv = '".$model->applicantdiv."' ";
            $query .= "                                   AND T3.testdiv = '".$model->testdiv."' ";
            $query .= "                                   AND T3.examno = T1.examno ";
        }
        $query .= "  WHERE";
        $query .= "      T1.entexamyear     = '".$model->ObjYear."' AND";
        $query .= "      T1.applicantdiv    = '".$model->applicantdiv."' AND";
        $query .= "      T1.special_measures IS NULL AND";  //NULL
        $query .= "      T1.entclass        in ('1','2') AND";     //1:特別進学クラス
        $query .= "      T1.judgement       = '1'";         //1:合格
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

        foreach ($array2 as $val) {
            $examno = explode("-", $val);
            //if ($examno[1] != 2) {
                $query  = "UPDATE entexam_applicantbase_dat ";
                $query .= "   SET special_measures  = NULL";
                $query .= "      ,entclass          = '1'";
                $query .= "      ,registercd        = '".STAFFCD."'";
                $query .= "      ,updated           = SYSDATE()"; 
                $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                $query .= "   AND examno       = '".$examno[0]."'"; 
                $db->query($query);
                //重複出願者については両方の受験番号で「NULL」とする
                $query  = "UPDATE entexam_applicantbase_dat ";
                $query .= "   SET special_measures  = NULL";
                $query .= "      ,entclass          = '1'";
                $query .= "      ,registercd        = '".STAFFCD."'";
                $query .= "      ,updated           = SYSDATE()"; 
                $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                $query .= "   AND (recom_examno1 = '".$examno[0]."'"; 
                $query .= "     OR recom_examno2 = '".$examno[0]."'"; 
                $query .= "     OR recom_examno3 = '".$examno[0]."')"; 
                $db->query($query);
            //}
        }

        foreach ($array as $val) {
            $examno = explode("-", $val);
            //if ($examno[1] != 2) {
                $query  = "UPDATE entexam_applicantbase_dat ";
                $query .= "   SET special_measures  = '3'"; //措置区分   1:繰上合格/2:特別合格/3:特別アップ合格
                $query .= "      ,entclass          = '2'"; //入学クラス 1:特別進学クラス/2:英数特科クラス
                $query .= "      ,registercd        = '".STAFFCD."'";
                $query .= "      ,updated           = SYSDATE()"; 
                $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                $query .= "   AND examno       = '".$examno[0]."'"; 
                $db->query($query);
                //重複出願者については両方の受験番号で「特別アップ合格」とする
                $query  = "UPDATE entexam_applicantbase_dat ";
                $query .= "   SET special_measures  = '3'"; //措置区分   1:繰上合格/2:特別合格/3:特別アップ合格
                $query .= "      ,entclass          = '2'"; //入学クラス 1:特別進学クラス/2:英数特科クラス
                $query .= "      ,registercd        = '".STAFFCD."'";
                $query .= "      ,updated           = SYSDATE()"; 
                $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                $query .= "   AND (recom_examno1 = '".$examno[0]."'"; 
                $query .= "     OR recom_examno2 = '".$examno[0]."'"; 
                $query .= "     OR recom_examno3 = '".$examno[0]."')"; 
                $db->query($query);
            //}
        }

        $db->commit(); 

        Query::dbCheckIn($db);
    }
}
?>
