<?php

require_once('for_php7.php');

class knjl040oQuery extends Query {

    //名称
    function GetName($namecd1, $ObjYear, $namecd2="")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$ObjYear."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

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

    //重複データチェック
    function CheckData($model)
    {
        $query  = "SELECT applicantdiv, testdiv, examno FROM entexam_recept_dat ";
        $query .= " WHERE entexamyear = '".$model->ObjYear."'";
        $query .= "   AND receptno    BETWEEN '".$model->receptno."' AND '".$model->e_receptno."'";
        $query .= "   AND (applicantdiv != '".$model->applicantdiv."' OR testdiv != '".$model->testdiv."' OR exam_type != '".$model->exam_type."')";
        return $query;
    }

    //試験会場
    function getExamHall($model)
    {
        $query  = "SELECT s_receptno, e_receptno, examhall_name FROM entexam_hall_dat ";
        $query .= " WHERE testdiv   = '".$model->testdiv."'";
        $query .= "   AND exam_type = '".$model->exam_type."'";
        return $query;
    }

    //一覧表示
    function SelectQuery($model)
    {
        $query  = "SELECT T1.receptno";
        $query .= "      ,T2.examno";
        $query .= "      ,substr(T2.name,1,30)      AS name";
        $query .= "      ,substr(T2.name_kana,1,60) AS name_kana ";
        $query .= "      ,T2.sex AS sexcd";
        $query .= "      ,MEISYOU_GET(T2.sex,'Z002',2) AS sex";
        $query .= "      ,T2.TESTDIV".$model->testdiv." AS CHECK_TESTDIV ";
        $query .= "  FROM  entexam_recept_dat T1 ";
        $query .= "       ,entexam_applicantbase_dat T2 ";
        $query .= " WHERE T1.entexamyear  = T2.entexamyear ";
        $query .= "   AND T1.examno       = T2.examno ";
        $query .= "   AND T1.entexamyear  = '".$model->ObjYear."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        $query .= "   AND T1.exam_type    = '".$model->exam_type."'";
        $query .= "   AND T1.receptno BETWEEN '".$model->receptno."' AND '".$model->e_receptno."'";
        $query .= " ORDER BY T1.receptno";
        return $query;
    }

    //更新(一度すべて削除してから、必須項目が入力されているレコードのみ追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        $idx = 0;
        //受験番号が志願者マスタに存在するかチェック
        foreach ($model->examno as $val)
        {
            if (!strlen($val)) {
                $idx++;
                continue;
            }
            $query  = "SELECT * FROM entexam_applicantbase_dat ";
            $query .= " WHERE entexamyear = '".$model->ObjYear."'";
            $query .= "   AND examno      = '".$val."'";
            if ((int)$db->getOne($query) == "0") {
                $receptno = (int)$model->receptno + $idx;
                $msg  = "受験番号 ： ".$val."\\n\\n";
                $msg .= "対象座席番号 ： ".sprintf("%04d",$receptno);
                $model->setWarning("MSG303", $msg);
                Query::dbCheckIn($db);
                return false;
            }
            $idx++;
        }

        //重複データチェック(同じ受験番号が違う入試制度、受験型で登録できない）
        $query  = "SELECT T1.receptno";
        $query .= "      ,T1.examno";
        $query .= "      ,MEISYOU_GET(T1.applicantdiv, 'L003',1) AS applicantdiv";
        $query .= "      ,L1.NAME                                AS testdiv";
        $query .= "      ,MEISYOU_GET(T1.exam_type, 'L005',1)    AS exam_type";
        $query .= "  FROM entexam_recept_dat T1 ";
        $query .= "       LEFT JOIN ENTEXAM_TESTDIV_MST L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND L1.TESTDIV = T1.TESTDIV ";
        $query .= " WHERE T1.entexamyear = '".$model->ObjYear."'";
        $query .= "   AND T1.examno      IN ('".implode("','",$model->examno)."')";
        $query .= "   AND T1.testdiv = '".$model->testdiv."'";
        $query .= "   AND (T1.applicantdiv != '".$model->applicantdiv."' OR T1.exam_type != '".$model->exam_type."')";
        $query .= " ORDER BY T1.receptno";
        $existdata = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($existdata)) {
            $msg  = "この受験番号は既に登録されています。　　\\n\\n";
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

        //更新前のデータを取得
        $cur_data = array();
        $result    = $db->query(knjl040oQuery::SelectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $cur_data[$row["RECEPTNO"]] = $row["EXAMNO"];
        }

        $idx = 0;
        $db->autoCommit(false);

        for ($receptno = (int)$model->receptno; $receptno <= (int)$model->e_receptno; $receptno++)
        {
            //更新前の値が変更されてない場合は何もしない
            if ( $cur_data[(sprintf("%04d",$receptno))] == $model->examno[$idx]) {
                $idx++;
                continue;

            } else {

                //同じ受験番号で違う受付データには登録できない
                $query  = "SELECT T1.receptno";
                $query .= "      ,T1.examno";
                $query .= "      ,MEISYOU_GET(T1.applicantdiv, 'L003',1) AS applicantdiv";
                $query .= "      ,L1.NAME                                AS testdiv";
                $query .= "      ,MEISYOU_GET(T1.exam_type, 'L005',1)    AS exam_type";
                $query .= "  FROM entexam_recept_dat T1 ";
                $query .= "       LEFT JOIN ENTEXAM_TESTDIV_MST L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND L1.TESTDIV = T1.TESTDIV ";
                $query .= " WHERE T1.entexamyear  = '".$model->ObjYear."'";
                $query .= "   AND T1.examno       = '".$model->examno[$idx]."'";
                $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
                $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
                $query .= "   AND T1.exam_type    = '".$model->exam_type."'";
                $query .= "   AND T1.receptno    <> '".sprintf("%04d",$receptno)."'";
                $existdata = $db->getRow($query, DB_FETCHMODE_ASSOC);

                if (is_array($existdata)) {
                    $msg  = "この受験番号は既に登録されています。　　\\n\\n";
                    $msg .= "対象座席番号 ： ".sprintf("%04d",$receptno)."\\n\\n";
                    $msg .= "座席番号 ： ".$existdata["RECEPTNO"]."\\n";
                    $msg .= "受験番号 ： ".$existdata["EXAMNO"]."\\n";
                    $msg .= "入試制度 ： ".$existdata["APPLICANTDIV"]."\\n";
                    $msg .= "入試区分 ： ".$existdata["TESTDIV"]."\\n";
                    $msg .= "　受験型 ： ".$existdata["EXAM_TYPE"];
                    $model->setWarning("MSG302", $msg);
                    $db->rollback();
                    Query::dbCheckIn($db);
                    return false;
                }

                $query  = "DELETE FROM entexam_recept_dat";
                $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                $query .= "   AND testdiv      = '".$model->testdiv."'";
                $query .= "   AND exam_type    = '".$model->exam_type."'";
                $query .= "   AND receptno     = '".sprintf("%04d",$receptno)."'";
                $db->query($query);

                //ENTEXAM_SCORE_DAT
                $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
                $query .= "  WHERE entexamyear = '".$model->ObjYear."'";
                $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                $query .= "   AND testdiv      = '".$model->testdiv."'";
                $query .= "   AND exam_type    = '".$model->exam_type."'";
                $query .= "   AND receptno     = '".sprintf("%04d",$receptno)."'";
                $db->query($query);

                //ENTEXAM_JUDGE_TMP
                $query = " DELETE FROM ENTEXAM_JUDGE_TMP ";
                $query .= "  WHERE entexamyear = '".$model->ObjYear."'";
                $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                $query .= "   AND testdiv      = '".$model->testdiv."'";
                $query .= "   AND exam_type    = '".$model->exam_type."'";
                $query .= "   AND receptno     = '".sprintf("%04d",$receptno)."'";
                $db->query($query);

                //受験番号が入力されていたら更新
                if (strlen($model->examno[$idx])) {
                    $query  = "INSERT INTO entexam_recept_dat";
                    $query .= " (entexamyear, applicantdiv, testdiv, exam_type, receptno, examno,registercd, updated)";
                    $query .= " VALUES ('".$model->ObjYear."'";
                    $query .= "        ,'".$model->applicantdiv."'";
                    $query .= "        ,'".$model->testdiv."'";
                    $query .= "        ,'".$model->exam_type."'";
                    $query .= "        ,'".sprintf("%04d",$receptno)."'";
                    $query .= "        ,'".$model->examno[$idx]."'";
                    $query .= "        ,'".STAFFCD."'";
                    $query .= "        ,SYSDATE()";
                    $query .= ")";
                    $db->query($query);
                }
                $idx++;
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>