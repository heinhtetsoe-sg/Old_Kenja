<?php

require_once('for_php7.php');

class knjl050cQuery extends Query {

    //学校コードを取得：五条・和歌山を判断するため
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //名称
    function GetName($namecd1,$ObjYear,$order = "")
    {
       $query = "SELECT namecd2, name1, name2, namespare1, namespare2 FROM v_name_mst
                  WHERE year    = '".$ObjYear."'
                    AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2 ".$order;
        return $query;
    }
    
    //受験科目
    function getTestSubclasscd($ObjYear, $applicantdiv, $testdiv)
    {
        $query  = "";
        $query .= " SELECT  TESTSUBCLASSCD ";
        $query .= "   FROM  ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= "  WHERE  ENTEXAMYEAR  = '$ObjYear' ";
        $query .= "    AND  APPLICANTDIV = '$applicantdiv'";
        $query .= "    AND  TESTDIV      = '$testdiv'";
        $query .= " ORDER BY TESTSUBCLASSCD";
        return $query;
    }

    //会場
    function getHallName($model)
    {
        $query  = "";
        $query .= " SELECT  EXAMHALLCD, ";
        $query .= "         EXAMHALL_NAME, ";
        $query .= "         S_RECEPTNO, ";
        $query .= "         E_RECEPTNO ";
        $query .= " FROM    ENTEXAM_HALL_DAT ";
        $query .= " WHERE   TESTDIV   = '".$model->testdiv."' AND ";
        $query .= "         EXAM_TYPE = '".$model->exam_type."' ";
        $query .= " ORDER BY EXAMHALLCD ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model)
    {
        $query  = "SELECT T1.receptno ";
        $query .= "      ,T1.examno ";
//        $query .= "      ,T2.name";
//        $query .= "      ,T2.name_kana";
//        $query .= "      ,T2.sex AS sexcd ";
//        $query .= "      ,N1.NAME2 AS sex ";
        $query .= "      ,T3.score ";
        $query .= "      ,T5.perfect";
        $query .= "  FROM entexam_recept_dat T1 INNER JOIN entexam_applicantbase_dat T2 ";
        $query .= "                                     ON T1.entexamyear    = T2.entexamyear ";
        $query .= "                                    AND T1.examno         = T2.examno ";
        $query .= "                                    AND T1.applicantdiv   = T2.applicantdiv ";
        $query .= "                                    AND T1.testdiv        = T2.testdiv  ";
        if ($model->select_subclass_div == "1") {
            $query .= "                                AND T2.select_subclass_div = '".$model->testsubclasscd."' ";
        }
        if ($model->isCollege && $model->applicantdiv == "1" && $model->testdiv == "1" && $model->testsubclasscd == "3") {
            $query .= "                                AND T2.SUB_ORDER = '1' "; //1:Ⅰ型(国算理)、2:Ⅱ型(国算)
        }
        $query .= "                              LEFT JOIN entexam_score_dat T3 ";
        $query .= "                                     ON T1.entexamyear    = T3.entexamyear ";
        $query .= "                                    AND T1.applicantdiv   = T3.applicantdiv ";
        $query .= "                                    AND T1.testdiv        = T3.testdiv  ";
        $query .= "                                    AND T1.exam_type      = T3.exam_type ";
        $query .= "                                    AND T1.receptno       = T3.receptno ";
        $query .= "                                    AND T3.testsubclasscd = '".$model->testsubclasscd."' ";
        $query .= "                              LEFT JOIN entexam_perfect_mst T5 ";
        $query .= "                                     ON T1.entexamyear    = T5.entexamyear ";
        $query .= "                                    AND T1.applicantdiv   = T5.applicantdiv ";
        $query .= "                                    AND T1.testdiv        = T5.testdiv  ";
        $query .= "                                    AND T5.testsubclasscd = '".$model->testsubclasscd."' ";
        $query .= "                              LEFT JOIN entexam_applicantbase_dat T12 ";
        $query .= "                                     ON T1.entexamyear   = T12.entexamyear ";
        $query .= "                                    AND T1.examno        = T12.recom_examno ";
        $query .= "                                    AND T1.testdiv       <> T12.testdiv ";
        $query .= "                                    AND T12.applicantdiv  = '1' ";
        $query .= "                              LEFT JOIN NAME_MST N1 ";
        $query .= "                                     ON N1.NAMECD1 = 'Z002' ";
        $query .= "                                    AND N1.NAMECD2 = T2.sex ";
        $query .= " WHERE T1.entexamyear    = '".$model->ObjYear."'";
        $query .= "   AND T1.applicantdiv   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.testdiv        = '".$model->testdiv."' ";
        $query .= "   AND T1.exam_type      = '".$model->exam_type."' ";
        $query .= "   AND EXISTS (";    //志願者数確定処理が実行していないと表示はしない。
        $query .= "                SELECT 'X' FROM entexam_desire_dat w1";
        $query .= "                 WHERE ";
        $query .= "                       w1.entexamyear    =T1.entexamyear AND";
        $query .= "                       w1.applicantdiv   =T1.applicantdiv AND";
        $query .= "                       w1.testdiv        =T1.testdiv AND";
        $query .= "                       w1.examno         =T1.examno";
        $query .= "              )";
        $query .= "   AND EXISTS (";    //試験会場を設定していないと表示はしない。
        $query .= "                SELECT  'X' ";
        $query .= "                FROM    ENTEXAM_HALL_DAT w2 ";
        $query .= "                WHERE   w2.TESTDIV    = T1.testdiv AND ";
        $query .= "                        w2.EXAM_TYPE  = T1.exam_type AND ";
        $query .= "                        w2.EXAMHALLCD = '".$model->examhallcd."' AND ";
        $query .= "                        T1.receptno BETWEEN w2.S_RECEPTNO AND w2.E_RECEPTNO ";
        $query .= "              )";
        $query .= " ORDER BY T1.receptno";


        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        
        //満点チェック
        $result = $db->query(knjl050cQuery::SelectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($model->score[$row["RECEPTNO"]] > (int)$row["PERFECT"]) {
                $model->setWarning("MSG901","満点：".(int)$row["PERFECT"]."以下で入力してください\\n座席番号：".$row["RECEPTNO"]);
                Query::dbCheckIn($db);
                return false;
            }
        }

        $db->autoCommit(false);

        $query  = "DELETE FROM entexam_score_dat";
        $query .= " WHERE entexamyear    = '".$model->ObjYear."'";
        $query .= "   AND applicantdiv   = '".$model->applicantdiv."'";
        $query .= "   AND testdiv        = '".$model->testdiv."'";
        $query .= "   AND exam_type      = '".$model->exam_type."'";
        $query .= "   AND testsubclasscd = '".$model->testsubclasscd."'";
        $query .= "   AND receptno IN ('".implode("','",array_keys($model->score))."')";
        $db->query($query);

        foreach ($model->score as $receptno => $score)
        {
           $query  = "INSERT INTO entexam_score_dat";
           $query .= " (entexamyear, applicantdiv, testdiv, exam_type, receptno, testsubclasscd, attend_flg, score, registercd, updated)";
           $query .= " VALUES ('".$model->ObjYear."'";
           $query .= "        ,'".$model->applicantdiv."'";
           $query .= "        ,'".$model->testdiv."'";
           $query .= "        ,'".$model->exam_type."'";
           $query .= "        ,'".$receptno."'";
           $query .= "        ,'".$model->testsubclasscd."'";
           $query .= "        ,'".(!strlen($score) ? "0" : "1" )."'";
           $query .= "        ,".(!strlen($score) ? "NULL" : $score);
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