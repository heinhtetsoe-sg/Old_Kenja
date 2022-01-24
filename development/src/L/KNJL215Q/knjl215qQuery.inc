<?php

require_once('for_php7.php');

class knjl215qQuery extends Query {

    //中学は固定
    function getTestDivKotei() {
        $query  = " WITH VAL_T(LABEL, VALUE) AS ( ";
        $query .= " VALUES('1:帰国生入試', '1') ";
        $query .= " UNION ";
        $query .= " VALUES('2:一般入試', '2') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     VAL_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

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

    //実践模試データ
    function getBeforeDat($model) {
        $query  = " SELECT ";
        $query .= "     SAT_NO, ";
        $query .= "     KANA1 || '　' || KANA2 AS NAME_KANA, ";
        $query .= "     SEX, ";
        $query .= "     SCHOOLCD AS FS_CD, ";
        $query .= "     GROUPCD ";
        $query .= " FROM ";
        $query .= "     SAT_APP_FORM_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     SAT_NO ";
        return $query;
    }

    //志願者基礎データ
    function getBaseDat($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     TRANSLATE_H_HK(T1.NAME_KANA) AS NAME_KANA, ";
        $query .= "     T1.SEX, ";
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

        //実践模試データ
        $beforeArray = array();
        $query = knjl215qQuery::getBeforeDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $row["SAT_NO"];
            $beforeArray[$key] = array("SAT_NO"         => $row["SAT_NO"],
                                       "NAME_KANA"      => $row["NAME_KANA"],
                                       "SEX"            => $row["SEX"],
                                       "FS_CD"          => $row["FS_CD"],
                                       "GROUPCD"        => $row["GROUPCD"]
                                );
        }
        $result->free();

        //志願者基礎データ
        $baseArray = array();
        $query = knjl215qQuery::getBaseDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $row["EXAMNO"];
            $baseArray[$key] = array("EXAMNO"       => $row["EXAMNO"],
                                     "NAME_KANA"    => $row["NAME_KANA"],
                                     "SEX"          => $row["SEX"],
                                     "FS_CD"        => $row["FS_CD"]
                                );
        }
        $result->free();

        //条件
        $checkPageno = array();
        $checkExamno = array();
        foreach ($beforeArray as $pageno => $beforeList) {
            foreach ($baseArray as $examno => $baseList) {
                if ($beforeList["NAME_KANA"] == $baseList["NAME_KANA"] && 
                    $beforeList["FS_CD"] == $baseList["FS_CD"] && 
                    $beforeList["SEX"] == $baseList["SEX"]) 
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
            $query.= "       AND SEQ = '002' ";
            //チェックOFFの時、実践模試受験番号クリアしない
            //つまり、実践模試受験番号NULLの人のみ処理する。
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
                //チェックOFFの時、実践模試受験番号クリアしない。
                //つまり、実践模試受験番号NULLの人のみ処理する。
                if (!strlen($model->clear_div)) {
                    $query = " SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
                    $query.= " WHERE ENTEXAMYEAR = '{$model->examyear}' ";
                    $query.= "       AND EXAMNO = '{$examno}' ";
                    $query.= "       AND SEQ = '002' ";
                    $query .= "      AND REMARK1 IS NOT NULL ";
                    if (0 < $db->getOne($query)) continue;
                }
                //志願者基礎詳細データの追加
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = '002';
                $data["REMARK1"][TEXT]          = $beforeArray[$pageno]["SAT_NO"];
                $data["REMARK3"][TEXT]          = $beforeArray[$pageno]["GROUPCD"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                $db->query($query);
                $shoriKensuu++;
            }
        }

        $db->commit(); 
        Query::dbCheckIn($db);

        //実践模試未処理とは、
        //条件に一致しない。
        //条件に一致するが、1:NまたはN:1である。
        returnget_count($beforeArray) - $shoriKensuu;
    } 
}
?>

