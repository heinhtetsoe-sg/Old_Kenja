<?php

require_once('for_php7.php');
class knjl080kQuery extends Query {

    //名称
    function GetName($namecd1,$year)
    {
        $query = "SELECT namecd2, name1, name2 FROM v_name_mst
                   WHERE year    = '".$year."'
                     AND namecd1 = '".$namecd1."'";
        return $query;
    }
    
    //重複データチェック
    function CheckData($model)
    {
        return $query;
    }

    //一覧表示
    function SelectQuery($model)
    {
        if ((int)$model->examno + 19 >= 9999) {
            $end_examno = "9999";
        } else {
            $end_examno = sprintf("%04d", (int)$model->examno + 19);
        }

        $query  = "SELECT T1.examno "; 
        $query .= "      ,T1.name ";
//2005.12.23 alp m-yama
//        $query .= "      ,MEISYOU_GET(T1.sex, 'Z002', 2) AS sex ";
        $query .= "      ,MEISYOU_GET(T1.sex, 'Z002', 4) AS sex ";
        $query .= "      ,T4.a_score ";
        $query .= "      ,T4.b_score ";
        $query .= "      ,T3.a_perfect ";
        $query .= "      ,T3.b_perfect ";       #2005/09/06
        $query .= "      ,T1.desirediv";
        $query .= "      ,T2.examcourse_name";
        $query .= "      ,T3.autocalc ";
        $query .= "      ,T3.inc_magnification ";
        $query .= "  FROM entexam_applicantbase_dat T1 ";
                          /* 学科コース名 */
        $query .= "       LEFT OUTER JOIN (SELECT T1.desirediv, T1.testdiv, T2.examcoursecd, T2.examcourse_name ";
        $query .= "                          FROM entexam_wishdiv_mst T1 ";
        $query .= "                              ,entexam_course_mst T2 ";
        $query .= "                         WHERE T1.entexamyear  = T2.entexamyear ";
        $query .= "                           AND T1.coursecd     = T2.coursecd ";
        $query .= "                           AND T1.majorcd      = T2.majorcd ";
        $query .= "                           AND T1.examcoursecd = T2.examcoursecd ";
        $query .= "                           AND T1.entexamyear  = '".$model->year."') T2 ";
        $query .= "                    ON T1.desirediv = T2.desirediv ";
        $query .= "                   AND T1.testdiv   = T2.testdiv ";
                          /* 受験科目の満点、自動計算 */
        $query .= "       LEFT OUTER JOIN (SELECT T1.desirediv, T2.testdiv, T2.testsubclasscd, T2.a_perfect, T2.autocalc, T2.inc_magnification, T2.b_perfect ";
        $query .= "                          FROM entexam_wishdiv_mst T1  ";
        $query .= "                              ,entexam_perfect_mst T2 ";
        $query .= "                         WHERE T1.entexamyear  = T2.entexamyear ";
        $query .= "                           AND T1.testdiv      = T2.testdiv ";
        $query .= "                           AND T1.examcoursecd = T2.examcoursecd  ";
        $query .= "                           AND T1.coursecd     = T2.coursecd ";
        $query .= "                           AND T1.majorcd      = T2.majorcd  ";
        $query .= "                           AND T1.entexamyear  = '".$model->year."'";
        $query .= "                           AND T1.wishno       = '1') T3 "; //固定でOK
        $query .= "                    ON T1.testdiv        = T3.testdiv ";
        $query .= "                   AND T1.desirediv      = T3.desirediv ";
        $query .= "                   AND T3.testsubclasscd = '".$model->testsubclasscd."'";
                          /* 各受験番号の得点 */
        $query .= "       LEFT OUTER JOIN entexam_score_dat T4 ";
        $query .= "                    ON T1.entexamyear    = T4.entexamyear ";
        $query .= "                   AND T1.testdiv        = T4.testdiv ";
        $query .= "                   AND T1.examno         = T4.examno ";
        $query .= "                   AND T4.testsubclasscd = '".$model->testsubclasscd."' ";
        $query .= " WHERE T1.entexamyear = '".$model->year."'";
        $query .= "   AND T1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "   AND T1.testdiv     = '".$model->testdiv."' ";
        $query .= "   AND T1.examno BETWEEN '".$model->examno."' AND '".$end_examno."'";
        $query .= " ORDER BY T1.examno";
        #echo $query;
        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "DELETE FROM entexam_score_dat";
        $query .= " WHERE entexamyear    = '".$model->year."'";
        $query .= "   AND testdiv        = '".$model->testdiv."'";
        $query .= "   AND testsubclasscd = '".$model->testsubclasscd."'";
        $query .= "   AND examno IN ('".implode("','", array_keys($model->score))."')";
        $db->query($query);

        $idx = 0;
        foreach ($model->score as $examno => $score)
        {
            $query  = "INSERT INTO entexam_score_dat";
            $query .= " (entexamyear, testdiv, examno,testsubclasscd,attend_flg,a_score,b_score,registercd, updated)";
            $query .= " VALUES ('".$model->year."'";
            $query .= "        ,'".$model->testdiv."'";
            $query .= "        ,'".sprintf("%04d",$examno)."'";
            $query .= "        ,'".$model->testsubclasscd."'";
            $query .= "        ,'".(!strlen($score["A_SCORE"]) ? "0" : "1" )."'";
            $query .= "        ,".(!strlen($score["A_SCORE"]) ? "NULL" : $score["A_SCORE"]);
            $query .= "        ,".(!strlen($score["B_SCORE"]) ? "NULL" : $score["B_SCORE"]);
            $query .= "        ,'".STAFFCD."'";
            $query .= "        ,SYSDATE()";
            $query .= ")";
            $db->query($query);
            $idx++;
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>