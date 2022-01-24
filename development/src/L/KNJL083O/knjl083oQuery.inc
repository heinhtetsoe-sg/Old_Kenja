<?php

require_once('for_php7.php');

class knjl083oQuery extends Query {
    /*
        基礎データと受付データを結合するときにAPPLICANTDIVは条件にしない
        APPLICANTDIVが複数存在することはありえないため
    */

    //クラス確定
    function getEntClassStd($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.ENTCLASS, ";
        $query .= "     T1.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND T1.ENTDIV = '1' ";
        $query .= "     AND T1.PROCEDUREDIV = '1' ";
        $query .= "     AND EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_RECEPT_DAT E1 ";
        $query .= "             WHERE ";
        $query .= "                 E1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "                 AND E1.JUDGECLASS = '5' ";
        $query .= "                 AND E1.EXAMNO = T1.EXAMNO ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";
        return $query;
    }

    //名称マスタ
    function GetName2($namecd1, $ObjYear, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$ObjYear."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //名称
    function GetName($namecd1, $ObjYear, $namecd2 = "")
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
        $query  = "  SELECT";
        $query .= "      T1.examno ,";
        $query .= "      VALUE(T1.entdiv, '0') ENTDIV ,";
        $query .= "      T1.name,";
        $query .= "      '(' || CASE T2.adjournmentdiv WHEN '1' THEN '延' ELSE '　' END ||";
        $query .= "      ''  || CASE WHEN T1.special_measures = '1' THEN '繰' ";
        $query .= "                  WHEN T1.special_measures = '2' THEN '特' ";
        $query .= "                  WHEN L1.JUDGECLASS = '5' AND T1.entdiv = '1' AND T1.ENTCLASS IS NULL THEN '未' ";
        $query .= "                  ELSE '　' END ||";
        $query .= "      ''  || CASE WHEN T1.entdiv = '2' THEN '辞' ";
        $query .= "                  WHEN L1.JUDGECLASS = '5' THEN '正' ";
        $query .= "                  WHEN JUDGE.MAX_HONORDIV = '1' THEN '☆' ";
        $query .= "                  ELSE '　' END || ') ' AS adjournment";
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
        $query .= "      LEFT JOIN ( ";
        $query .= "          SELECT DISTINCT ";
        $query .= "              EXAMNO, ";
        $query .= "              JUDGECLASS ";
        $query .= "          FROM ";
        $query .= "              ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ";
        $query .= "              ENTEXAMYEAR    = '".$model->ObjYear."' AND ";
        $query .= "              APPLICANTDIV   = '".$model->applicantdiv."' AND";
        $query .= "              JUDGECLASS     = '5' "; //5:正規合格
        $query .= "          ) L1 ON L1.EXAMNO = T1.EXAMNO ";
        $query .= "      LEFT JOIN ( ";
        $query .= "          SELECT ";
        $query .= "              EXAMNO, ";
        $query .= "              MAX(VALUE(HONORDIV, '0')) AS MAX_HONORDIV ";
        $query .= "          FROM ";
        $query .= "              ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ";
        $query .= "              ENTEXAMYEAR    = '".$model->ObjYear."' AND ";
        $query .= "              APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "          GROUP BY ";
        $query .= "              EXAMNO ";
        $query .= "          ) JUDGE ON JUDGE.EXAMNO = T1.EXAMNO ";
        $query .= "  WHERE";
        $query .= "      T1.entexamyear     = '".$model->ObjYear."' AND";
        $query .= "      T1.applicantdiv    = '".$model->applicantdiv."' AND";
        $query .= "      T1.procedurediv    = '1' AND";             //手続済み
        $query .= "      T1.judgement       = '1'";  //合格
        $query .= "  ORDER BY";
        $query .= "      T1.examno";

        return $query;
    }

    //合格者
    function GetRightList($model)
    {
        $query  = "  SELECT";
        $query .= "      T1.examno ,";
        $query .= "      VALUE(T1.entdiv, '0') ENTDIV ,";
        $query .= "      T1.name,";
        $query .= "      '(' || CASE T2.adjournmentdiv WHEN '1' THEN '延' ELSE '　' END || ";
        $query .= "      ''  || CASE T1.special_measures WHEN '1' THEN '繰' WHEN '2' THEN '特' ELSE '　' END ||";
        $query .= "      ''  || CASE WHEN T1.entdiv = '2' THEN '辞' ";
        $query .= "                  WHEN L1.JUDGECLASS = '5' THEN '正' ";
        $query .= "                  WHEN JUDGE.MAX_HONORDIV = '1' THEN '☆' ";
        $query .= "                  ELSE '　' END || ') ' AS adjournment";
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
        $query .= "      LEFT JOIN ( ";
        $query .= "          SELECT DISTINCT ";
        $query .= "              EXAMNO, ";
        $query .= "              JUDGECLASS ";
        $query .= "          FROM ";
        $query .= "              ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ";
        $query .= "              ENTEXAMYEAR    = '".$model->ObjYear."' AND ";
        $query .= "              APPLICANTDIV   = '".$model->applicantdiv."' AND";
        $query .= "              JUDGECLASS     = '5' "; //5:正規合格
        $query .= "          ) L1 ON L1.EXAMNO = T1.EXAMNO ";
        $query .= "      LEFT JOIN ( ";
        $query .= "          SELECT ";
        $query .= "              EXAMNO, ";
        $query .= "              MAX(VALUE(HONORDIV, '0')) AS MAX_HONORDIV ";
        $query .= "          FROM ";
        $query .= "              ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ";
        $query .= "              ENTEXAMYEAR    = '".$model->ObjYear."' AND ";
        $query .= "              APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "          GROUP BY ";
        $query .= "              EXAMNO ";
        $query .= "          ) JUDGE ON JUDGE.EXAMNO = T1.EXAMNO ";
        $query .= "  WHERE";
        $query .= "      T1.entexamyear     = '".$model->ObjYear."' AND";
        $query .= "      T1.applicantdiv    = '".$model->applicantdiv."' AND";
        $query .= "      T1.judgement       = '1'";  //合格
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

        foreach ($array as $val) {
            $examno = explode("-", $val);
            if ($examno[1] != 2) {
                //入試区分にかかわらず受付データのHONORDIVが１が１件以上ある場合は基礎データのHONORDIVも1にする
                $query  = "SELECT COUNT(*) ";
                $query .= "  FROM entexam_recept_dat ";
                $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                $query .= "   AND honordiv     = '1'";
                $query .= "   AND examno       = '".$examno[0]."'";
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
                $query .= "   AND examno       = '".$examno[0]."'"; 
                $db->query($query);
            }
        }

        foreach ($array2 as $val) {
            $examno = explode("-", $val);

            if ($examno[1] != 2) {
                $query  = "UPDATE entexam_applicantbase_dat ";
                $query .= "   SET procedurediv = '2'";
                $query .= "      ,entdiv       = NULL";
                $query .= "      ,honordiv     = NULL";
                $query .= "      ,registercd = '".STAFFCD."'";
                $query .= "      ,updated    = SYSDATE()"; 
                $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                $query .= "   AND examno       = '".$examno[0]."'"; 
                $db->query($query);
            }
        }

        $db->commit(); 

        Query::dbCheckIn($db);
    }

    //UPDATE
    function &getUpdateEntClassQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->kakuteiExam as $exam => $entClass) {
            $data = array();
            $data["ENTCLASS"][TEXT] = $entClass;

            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $where .= "     AND EXAMNO  = '".$exam."' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit(); 

        Query::dbCheckIn($db);
    }
}
?>
