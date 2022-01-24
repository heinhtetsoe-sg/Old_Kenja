<?php

require_once('for_php7.php');

class knjl040jQuery extends Query {

    //名称
    function GetName($namecd1,$ObjYear)
    {
        $query = "SELECT namecd2, name1, name2,namespare1 , namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2";

        return $query;
    }
    
    //一覧表示
    function SelectQuery($model)
    {
        $fieldname = "testdiv" .$model->testdiv;
        $query  = " SELECT  T1.APPLICANTDIV, ";
        $query .= "         N1.NAME1 AS APPLICANTDIV_NAME, ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         T2.RECEPTNO, ";
        $query .= "         T3.EXAMHALL_NAME, ";
        $query .= "         T1.".$fieldname." AS EXAM_TYPE, ";
        $query .= "         N2.NAME1 AS EXAM_TYPE_NAME, ";
        $query .= "         T1.NAME, ";
        $query .= "         T1.NAME_KANA, ";
        $query .= "         T1.SEX, ";
        $query .= "         N3.NAME2 AS SEX_NAME ";
        $query .= " FROM    ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "         LEFT JOIN ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR='".$model->ObjYear."' ";
        $query .= "                                        AND T2.APPLICANTDIV=T1.APPLICANTDIV ";
        $query .= "                                        AND T2.TESTDIV='".$model->testdiv."' ";
        $query .= "                                        AND T2.EXAM_TYPE=T1.".$fieldname." ";
        $query .= "                                        AND T2.EXAMNO=T1.EXAMNO ";
        $query .= "         LEFT JOIN ENTEXAM_HALL_DAT T3   ON T3.TESTDIV='".$model->testdiv."' ";
        $query .= "                                        AND T3.EXAM_TYPE='1' ";
        $query .= "                                        AND T2.RECEPTNO BETWEEN T3.S_RECEPTNO AND T3.E_RECEPTNO ";
        $query .= "         LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T1.APPLICANTDIV ";
        $query .= "         LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L005' AND N2.NAMECD2=T1.".$fieldname." ";
        $query .= "         LEFT JOIN NAME_MST N3 ON N3.NAMECD1='Z002' AND N3.NAMECD2=T1.SEX ";
        $query .= " WHERE   T1.ENTEXAMYEAR='".$model->ObjYear."' ";
        $query .= "   AND   T1.".$fieldname." is not null ";
        $query .= "   AND   T1.".$fieldname." = '".$model->exam_type."' ";
        $query .= "   AND   T1.examno BETWEEN '".$model->examno."' AND '".$model->e_examno."' ";
        $query .= "   AND   value(T1.JUDGEMENT,'0') <> '1' ";//合格者は除く（一度でも合格したら受験できない）
        $query .= " ORDER BY T1.EXAMNO ";
        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //１．削除
        for ($i = 0; $i < get_count($model->data["EXAMNO"]); $i++) {
            //変更前後の受付番号の値を比較（変更なし：何もしない）
            if ($model->data["RECEPTNO"][$i] == $model->receptno[$i]) {
            //変更前後の受付番号の値を比較（変更あり：）
            } else {
                //変更前の受付番号の値があれば削除
                if (strlen($model->data["RECEPTNO"][$i])) {
                    //ENTEXAM_RECEPT_DAT
                    $query  = "DELETE FROM entexam_recept_dat";
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND testdiv      = '".$model->testdiv."'";
                    $query .= "   AND receptno     = '".$model->data["RECEPTNO"][$i]."'";
                    $db->query($query);

                    //ENTEXAM_SCORE_DAT
                    $query  = "DELETE FROM ENTEXAM_SCORE_DAT ";
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND testdiv      = '".$model->testdiv."'";
                    $query .= "   AND receptno     = '".$model->data["RECEPTNO"][$i]."'";
                    $db->query($query);

                    //ENTEXAM_JUDGE_TMP
                    $query  = "DELETE FROM ENTEXAM_JUDGE_TMP ";
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND testdiv      = '".$model->testdiv."'";
                    $query .= "   AND receptno     = '".$model->data["RECEPTNO"][$i]."'";
                    $db->query($query);
                }
            }//if else
        }//for

        //２．重複チェック
        for ($i = 0; $i < get_count($model->data["EXAMNO"]); $i++) {
            //変更前後の受付番号の値を比較（変更なし：何もしない）
            if ($model->data["RECEPTNO"][$i] == $model->receptno[$i]) {
            //変更前後の受付番号の値を比較（変更あり：）
            } else {
                //変更後の受付番号の値があれば重複チェック
                if (strlen($model->receptno[$i])) {
                    $query  = "SELECT receptno";
                    $query .= "      ,examno";
                    $query .= "      ,N1.NAME1 AS applicantdiv ";
                    $query .= "      ,N2.NAME1 AS testdiv ";
                    $query .= "      ,N3.NAME1 AS exam_type ";
                    $query .= "  FROM entexam_recept_dat T1 ";
                    $query .= "       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T1.applicantdiv ";
                    $query .= "       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L004' AND N2.NAMECD2=T1.testdiv ";
                    $query .= "       LEFT JOIN NAME_MST N3 ON N3.NAMECD1='L005' AND N3.NAMECD2=T1.exam_type ";
                    $query .= " WHERE entexamyear   = '".$model->ObjYear."'";
                    $query .= "   AND testdiv       = '".$model->testdiv."'";
                    $query .= "   AND receptno      = '".$model->receptno[$i]."' ";
                    $query .= "   AND examno        <> '".$model->data["EXAMNO"][$i]."' ";
                    $query .= " ORDER BY receptno";
                    $existdata = $db->getRow($query, DB_FETCHMODE_ASSOC);

                    if (is_array($existdata)) {
                        $msg  = "この座席番号は既に登録されています。　　\\n\\n";
                        $msg .= "座席番号 ： ".$existdata["RECEPTNO"]."\\n";
                        $msg .= "受験番号 ： ".$existdata["EXAMNO"]."\\n";
                        $msg .= "入試制度 ： ".$existdata["APPLICANTDIV"]."\\n";
                        $msg .= "入試区分 ： ".$existdata["TESTDIV"]."\\n";
                        $msg .= "　 受験型 ： ".$existdata["EXAM_TYPE"];
                        $model->setWarning("MSG302", $msg);
                        $db->rollback();
                        Query::dbCheckIn($db);
                        return false;
                    }
                }
            }//if else
        }//for

        //３．追加
        for ($i = 0; $i < get_count($model->data["EXAMNO"]); $i++) {
            //変更前後の受付番号の値を比較（変更なし：何もしない）
            if ($model->data["RECEPTNO"][$i] == $model->receptno[$i]) {
            //変更前後の受付番号の値を比較（変更あり：）
            } else {
                //変更後の受付番号の値があれば追加
                if (strlen($model->receptno[$i])) {
                    $query  = "INSERT INTO entexam_recept_dat";
                    $query .= " (entexamyear, applicantdiv, testdiv, exam_type, receptno, examno,registercd, updated)";
                    $query .= " VALUES ('".$model->ObjYear."'";
                    $query .= "        ,'".$model->data["APPLICANTDIV"][$i]."'";
                    $query .= "        ,'".$model->testdiv."'";
                    $query .= "        ,'".$model->data["EXAM_TYPE"][$i]."'";
                    $query .= "        ,'".$model->receptno[$i]."'";
                    $query .= "        ,'".$model->data["EXAMNO"][$i]."'";
                    $query .= "        ,'".STAFFCD."'";
                    $query .= "        ,SYSDATE()";
                    $query .= ")";
                    $db->query($query);
                }
            }//if else
        }//for

        $db->commit();
        Query::dbCheckIn($db);
        return true;

    }

}
?>