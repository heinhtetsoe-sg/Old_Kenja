<?php

require_once('for_php7.php');
class knjl050jQuery extends Query {

    //名称
    function GetName($namecd1,$ObjYear,$order = "")
    {
       $query = "SELECT namecd2, name1, name2, namespare1, namespare2 FROM v_name_mst
                  WHERE year    = '".$ObjYear."'
                    AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2 ".$order;
        return $query;
    }

    //一覧表示
    function SelectQuery($model)
    {
        $query  = "SELECT T1.receptno ";
        $query .= "      ,T1.examno ";
        $query .= "      ,T1.applicantdiv ";
        $query .= "      ,N2.NAME1 AS applicantdiv_name ";
        $query .= "      ,T1.exam_type ";
        $query .= "      ,N3.NAME1 AS exam_type_name ";
        $query .= "      ,T2.name";
        $query .= "      ,T2.name_kana";
        $query .= "      ,T2.sex AS sexcd ";
        $query .= "      ,N1.NAME2 AS sex ";
        $query .= "      ,T3.score ";
        $query .= "      ,T4.remark ";
        $query .= "      ,T5.perfect";
        $query .= "      ,T3.UPDATED AS SCORE_UPDATED ";
        $query .= "      ,T4.UPDATED AS REMARK_UPDATED ";
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
        $query .= "                             LEFT OUTER JOIN entexam_score_remark_dat T4 ";
        $query .= "                                     ON T1.entexamyear    = T4.entexamyear ";
        $query .= "                                    AND T1.applicantdiv   = T4.applicantdiv ";
        $query .= "                                    AND T1.testdiv        = T4.testdiv  ";
        $query .= "                                    AND T1.exam_type      = T4.exam_type ";
        $query .= "                                    AND T1.receptno       = T4.receptno ";
        $query .= "                                    AND T4.testsubclasscd = '".$model->testsubclasscd."' ";
        $query .= "                             LEFT OUTER JOIN entexam_perfect_mst T5 ";
        $query .= "                                     ON T5.entexamyear    = T1.entexamyear ";
        $query .= "                                    AND T5.testdiv        = T1.testdiv ";
        $query .= "                                    AND T5.testsubclasscd = '".$model->testsubclasscd."' ";
        $query .= "                             LEFT OUTER JOIN NAME_MST N1 ";
        $query .= "                                     ON N1.NAMECD1 = 'Z002' ";
        $query .= "                                    AND N1.NAMECD2 = T2.sex ";
        $query .= "                             LEFT OUTER JOIN NAME_MST N2 ";
        $query .= "                                     ON N2.NAMECD1 = 'L003' ";
        $query .= "                                    AND N2.NAMECD2 = T1.applicantdiv ";
        $query .= "                             LEFT OUTER JOIN NAME_MST N3 ";
        $query .= "                                     ON N3.NAMECD1 = 'L005' ";
        $query .= "                                    AND N3.NAMECD2 = T1.exam_type ";
        $query .= " WHERE T1.entexamyear    = '".$model->ObjYear."'";
        $query .= "   AND T1.testdiv        = '".$model->testdiv."' ";
        //入試区分が「1:A-1 3:B 4:C 6:D」で受験科目が「4:社会 または 5:理科」の場合、４科型の受験者のみ抽出
        if (($model->testdiv == "1" || $model->testdiv == "3" || $model->testdiv == "4" || $model->testdiv == "6") && 
            ($model->testsubclasscd == "4" || $model->testsubclasscd == "5")) {
            $query .= "   AND T1.exam_type      = '2' ";//2:４科型
        }
        $query .= "   AND T1.receptno BETWEEN '".$model->receptno."' AND '".$model->e_receptno."' ";
        $query .= "   AND EXISTS (";    //志願者数確定処理が実行していないと表示はしない。
        $query .= "                SELECT 'X' FROM entexam_desire_dat w1";
        $query .= "                 WHERE ";
        $query .= "                       w1.entexamyear    =T1.entexamyear AND";
        $query .= "                       w1.applicantdiv   =T1.applicantdiv AND";
        $query .= "                       w1.testdiv        =T1.testdiv AND";
        $query .= "                       w1.exam_type      =T1.exam_type AND";
        $query .= "                       w1.examno         =T1.examno";
        $query .= "              )";
        $query .= " ORDER BY T1.receptno";


        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model)
    {
        //文字数チェック
        foreach ($model->remark as $receptno => $remark)
        {
            if (strlen($remark) > 60) {
                $model->setWarning("MSG901","備考：60バイトまでです。\\n座席番号：".$receptno);
                return false;
            }
        }

        $db = Query::dbCheckOut();

        //受験科目毎の満点
        $query  = "SELECT perfect FROM entexam_perfect_mst ";
        $query .= " WHERE entexamyear    = '".$model->ObjYear."'";
        $query .= "   AND testdiv        = '".$model->testdiv."'";
        $query .= "   AND testsubclasscd = '".$model->testsubclasscd."'";
        $perfect = (int)$db->getOne($query);

        //満点チェック
        foreach ($model->score as $receptno => $score)
        {
            if ($score > $perfect) {
                $model->setWarning("MSG901","満点：".$perfect."以下で入力してください\\n座席番号：".$receptno);
                return false;
            }
        }

        $db->autoCommit(false);

        /*** 得点データ ***/
        foreach ($model->score as $receptno => $score)
        {
            //更新前のデータを取得
            $query  = "SELECT receptno, score, UPDATED";
            $query .= "  FROM entexam_score_dat";
            $query .= " WHERE entexamyear    = '".$model->ObjYear."'";
            $query .= "   AND testdiv        = '".$model->testdiv."'";
            $query .= "   AND testsubclasscd = '".$model->testsubclasscd."'";
            $query .= "   AND receptno       = '".$receptno."'";
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            //更新前のデータが変更されてない場合は何もしない。更新前のレコードがある場合のみ。
            if ($row["SCORE"] == $score && strlen($row["RECEPTNO"])) {
                continue;
            }
            //更新日付チェック
            if ($row["UPDATED"] != $model->scoreUpdated[$receptno] && strlen($row["SCORE"])) {
                continue;
            }
            //削除
            $query  = "DELETE FROM entexam_score_dat";
            $query .= " WHERE entexamyear    = '".$model->ObjYear."'";
            $query .= "   AND testdiv        = '".$model->testdiv."'";
            $query .= "   AND testsubclasscd = '".$model->testsubclasscd."'";
            $query .= "   AND receptno       = '".$receptno."'";
            $db->query($query);
            //追加
            $query  = "INSERT INTO entexam_score_dat";
            $query .= " (entexamyear, applicantdiv, testdiv, exam_type, receptno, testsubclasscd, attend_flg, score, registercd, updated)";
            $query .= " VALUES ('".$model->ObjYear."'";
            $query .= "        ,'".$model->applicantdiv[$receptno]."'";
            $query .= "        ,'".$model->testdiv."'";
            $query .= "        ,'".$model->exam_type[$receptno]."'";
            $query .= "        ,'".$receptno."'";
            $query .= "        ,'".$model->testsubclasscd."'";
            $query .= "        ,'".(!strlen($score) ? "0" : "1" )."'";
            $query .= "        ,".(!strlen($score) ? "NULL" : $score);
            $query .= "        ,'".STAFFCD."'";
            $query .= "        ,SYSDATE()";
            $query .= ")";
            $db->query($query);
        }

        /*** 得点備考データ ***/
        foreach ($model->remark as $receptno => $remark)
        {
            //更新前のデータを取得
            $query  = "SELECT receptno, remark, UPDATED";
            $query .= "  FROM entexam_score_remark_dat";
            $query .= " WHERE entexamyear    = '".$model->ObjYear."'";
            $query .= "   AND testdiv        = '".$model->testdiv."'";
            $query .= "   AND testsubclasscd = '".$model->testsubclasscd."'";
            $query .= "   AND receptno       = '".$receptno."'";
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            //更新前のデータが変更されてない場合は何もしない。更新前のレコードがある場合のみ。
            if ($row["REMARK"] == $remark && strlen($row["RECEPTNO"])) {
                continue;
            }
            //更新日付チェック
            if ($row["UPDATED"] != $model->remarkUpdated[$receptno] && strlen($row["REMARK"])) {
                continue;
            }
            //削除
            $query  = "DELETE FROM entexam_score_remark_dat";
            $query .= " WHERE entexamyear    = '".$model->ObjYear."'";
            $query .= "   AND testdiv        = '".$model->testdiv."'";
            $query .= "   AND testsubclasscd = '".$model->testsubclasscd."'";
            $query .= "   AND receptno       = '".$receptno."'";
            $db->query($query);
            //追加
            $query  = "INSERT INTO entexam_score_remark_dat";
            $query .= " (entexamyear, applicantdiv, testdiv, exam_type, receptno, testsubclasscd, remark, registercd, updated)";
            $query .= " VALUES ('".$model->ObjYear."'";
            $query .= "        ,'".$model->applicantdiv[$receptno]."'";
            $query .= "        ,'".$model->testdiv."'";
            $query .= "        ,'".$model->exam_type[$receptno]."'";
            $query .= "        ,'".$receptno."'";
            $query .= "        ,'".$model->testsubclasscd."'";
            if (!strlen($remark)) {
                $query .= "    ,NULL";
            } else {
                $query .= "    ,'".$remark."'";
            }
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