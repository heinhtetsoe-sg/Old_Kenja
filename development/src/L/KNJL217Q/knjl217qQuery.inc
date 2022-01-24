<?php

require_once('for_php7.php');

class knjl217qQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //公的テストデータ
    function getBeforeDat($model) {
        $query  = " SELECT ";
        $query .= "     T1.TEST_NO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.FS_CD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PUBLIC_TEST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.TEST_NO ";

        return $query;
    }

    //志願者基礎データ
    function getBaseDat($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.FS_CD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV = '{$model->testdiv}' ";
        return $query;
    }

    function ExecuteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //公的テストデータ
        $beforeArray = array();
        $query = knjl217qQuery::getBeforeDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $row["TEST_NO"];
            $beforeArray[$key] = array("TEST_NO"    => $row["TEST_NO"],
                                       "NAME"       => $row["NAME"],
                                       "FS_CD"      => $row["FS_CD"]
                                );
        }
        $result->free();

        //志願者基礎データ
        $baseArray = array();
        $query = knjl217qQuery::getBaseDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $row["EXAMNO"];
            $baseArray[$key] = array("EXAMNO"   => $row["EXAMNO"],
                                     "NAME"     => $row["NAME"],
                                     "FS_CD"    => $row["FS_CD"]
                                );
        }
        $result->free();

        //条件
        $checkPageno = array();
        $checkExamno = array();
        foreach ($beforeArray as $pageno => $beforeList) {
            foreach ($baseArray as $examno => $baseList) {
                if ($beforeList["NAME"] == $baseList["NAME"] && 
                    $beforeList["FS_CD"] == $baseList["FS_CD"]) 
                {
                    $checkPageno[$pageno][] = $examno;
                    $checkExamno[$examno][] = $pageno;
                }
            }
        }

        //クリア
        foreach ($baseArray as $examno => $baseList) {
            //志願者基礎詳細データの削除
            $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query.= " WHERE ENTEXAMYEAR = '{$model->examyear}' ";
            $query.= "       AND EXAMNO = '{$examno}' ";
            $query.= "       AND SEQ = '003' ";
            //チェックOFFの時、公的テスト番号クリアしない
            //つまり、公的テスト番号NULLの人のみ処理する。
            if (!strlen($model->clear_div)) {
                $query .= "      AND REMARK1 IS NULL ";
            }
            $db->query($query);
        }

        //更新
        $shoriKensuu = 0;
        foreach ($checkPageno as $pageno => $array) {
            if (1 < get_count($array)) continue;
            foreach ($array as $key => $examno) {
                if (1 < get_count($checkExamno[$examno])) continue;
                //チェックOFFの時、公的テスト番号クリアしない。
                //つまり、公的テスト番号NULLの人のみ処理する。
                if (!strlen($model->clear_div)) {
                    $query = " SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
                    $query.= " WHERE ENTEXAMYEAR = '{$model->examyear}' ";
                    $query.= "       AND EXAMNO = '{$examno}' ";
                    $query.= "       AND SEQ = '003' ";
                    $query .= "      AND REMARK1 IS NOT NULL ";
                    if (0 < $db->getOne($query)) continue;
                }
                //志願者基礎詳細データの追加
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = '003';
                $data["REMARK1"][TEXT]          = $beforeArray[$pageno]["TEST_NO"];
                $data["registercd"][TEXT]       = STAFFCD;
                $data["updated"][FUNC]          = "sysdate()";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                $db->query($query);
                $shoriKensuu++;
            }
        }

        $db->commit(); 
        Query::dbCheckIn($db);

        //公的テスト未処理とは、
        //条件に一致しない。
        //条件に一致するが、1:NまたはN:1である。
        returnget_count($beforeArray) - $shoriKensuu;
    } 
}
?>

