<?php

require_once('for_php7.php');

class knjl050oQuery extends Query {

    //名称
#    function GetName($namecd1,$ObjYear,$flg=0)
    function GetName($namecd1,$ObjYear,$order = "")
    {   //namespare1は2科目の絞込みに、namespare2は入試区分の絞込みに使用
       $query = "SELECT namecd2, name1, name2, namespare1, namespare2 FROM v_name_mst
                  WHERE year    = '".$ObjYear."'
                    AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2 ".$order;
#        $query  = "SELECT namecd2, name1, name2 FROM v_name_mst";
#        $query .= " WHERE year     = '".$ObjYear."'";
#        $query .= "    AND namecd1 = '".$namecd1."'";
#        if ($namecd1 == "L009" && $flg == 1) {
#            $query .= " AND namespare1 = '1'";
#        }
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
    
    //一覧表示
    function SelectQuery($model)
    {
        $query  = "SELECT T1.receptno ";
        $query .= "      ,T1.examno ";
#        $query .= "      ,T2.name";
#        $query .= "      ,T2.name_kana";
        $query .= "      ,substr(T2.name,1,30)         AS name";
        $query .= "      ,substr(T2.name_kana,1,60)    AS name_kana ";
        $query .= "      ,T2.sex                       AS sexcd ";
        $query .= "      ,MEISYOU_GET(T2.sex,'Z002',2) AS sex ";
        $query .= "      ,T3.score ";
        $query .= "      ,T2.desirediv ";
        $query .= "      ,T4.examcourse_name ";
        $query .= "      ,T5.perfect";
        $query .= "  FROM entexam_recept_dat T1 INNER JOIN entexam_applicantbase_dat T2 ";
        $query .= "                                     ON T1.entexamyear    = T2.entexamyear ";
        $query .= "                                    AND T1.examno         = T2.examno ";
        $query .= "                             LEFT OUTER JOIN entexam_score_dat T3 ";
        $query .= "                                     ON T1.entexamyear    = T3.entexamyear ";
        $query .= "                                    AND T1.applicantdiv   = T3.applicantdiv ";
        $query .= "                                    AND T1.testdiv        = T3.testdiv  ";
        $query .= "                                    AND T1.exam_type      = T3.exam_type ";
        $query .= "                                    AND T1.receptno       = T3.receptno ";
        $query .= "                                    AND T3.testsubclasscd = '".$model->testsubclasscd."' ";
        $query .= "                             LEFT OUTER JOIN (SELECT T1.desirediv, T2.examcourse_name  ";
        $query .= "                                           FROM entexam_wishdiv_mst T1  ";
        $query .= "                                               ,entexam_course_mst T2 ";
        $query .= "                                          WHERE T1.entexamyear  = T2.entexamyear ";
        $query .= "                                            AND T1.examcoursecd = T2.examcoursecd  ";
        $query .= "                                            AND T1.coursecd     = T2.coursecd ";
        $query .= "                                            AND T1.majorcd      = T2.majorcd  ";
        $query .= "                                            AND T1.entexamyear  = '".$model->ObjYear."' ";
        $query .= "                                            AND T1.wishno       = '1') T4 ";
        $query .= "                                     ON T4.desirediv      = T2.desirediv ";
        $query .= "                             LEFT OUTER JOIN (SELECT T1.desirediv, T3.testdiv, T3.testsubclasscd, T3.perfect";
        $query .= "                                           FROM entexam_wishdiv_mst T1  ";
        $query .= "                                               ,entexam_perfect_mst T3 ";
        $query .= "                                          WHERE T1.entexamyear  = T3.entexamyear ";
        $query .= "                                            AND T1.coursecd     = T3.coursecd ";
        $query .= "                                            AND T1.majorcd      = T3.majorcd ";
        $query .= "                                            AND T1.examcoursecd = T3.examcoursecd ";
        $query .= "                                            AND T1.entexamyear  = '".$model->ObjYear."' ";
        $query .= "                                            AND T1.wishno       = '1') T5 ";
        $query .= "                                     ON T5.desirediv      = T2.desirediv ";
        $query .= "                                    AND T5.testdiv        = T1.testdiv ";
        $query .= "                                    AND T5.testsubclasscd = '".$model->testsubclasscd."' ";
        $query .= " WHERE T1.entexamyear    = '".$model->ObjYear."'";
        $query .= "   AND T1.applicantdiv   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.testdiv        = '".$model->testdiv."' ";
        $query .= "   AND T1.exam_type      = '".$model->exam_type."' ";
        $query .= "   AND T1.receptno BETWEEN '".$model->receptno."' AND '".$model->e_receptno."' ";
        $query .= "   AND EXISTS (";    //志願者数確定処理が実行していないと表示はしない。
        $query .= "                SELECT 'X' FROM entexam_desire_dat w1";
        $query .= "                 WHERE ";
        $query .= "                       w1.entexamyear    =T1.entexamyear AND";
        $query .= "                       w1.applicantdiv   =T1.applicantdiv AND";
        $query .= "                       w1.testdiv        =T1.testdiv AND";
        $query .= "                       w1.examno         =T1.examno";
        $query .= "              )";
        $query .= " ORDER BY T1.receptno";


        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        
        //満点チェック
        $result = $db->query(knjl050oQuery::SelectQuery($model));
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