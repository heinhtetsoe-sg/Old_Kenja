<?php

require_once('for_php7.php');

class knjl215fQuery extends Query {

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
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試回数(2:高校のみ)
    function getTestdiv0($year, $testdiv) {
        $query  = " WITH T_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '1' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '2' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '3' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '4' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '5' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L059' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 || '（' || L1.DATE || '）' AS LABEL, ";
        $query .= "     T1.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     LEFT JOIN T_DATE L1 ON L1.TESTDIV0 = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.NAMECD1 = 'L034' ";
        $query .= "     AND L1.DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //事前相談データ
    function getBeforeDat($model) {
        $query  = " SELECT ";
        $query .= "     RECRUIT_NO, ";
        $query .= "     FINSCHOOLCD AS FS_CD, ";
        $query .= "     NAME_KANA ";
        $query .= " FROM ";
        $query .= "     RECRUIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->examyear}' ";
        $query .= " ORDER BY ";
        $query .= "     RECRUIT_NO ";
        return $query;
    }

    //志願者基礎データ
    function getBaseDat($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.FS_CD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV = '{$model->testdiv}' ";
        if ($model->applicantdiv == "2") {
            $query .= "     AND T1.TESTDIV0 = '{$model->testDiv0}' ";
        }
        return $query;
    }

    function ExecuteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //事前相談データ
        $beforeArray = array();
        $query = knjl215fQuery::getBeforeDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $row["RECRUIT_NO"];
            $beforeArray[$key] = array("RECRUIT_NO"     => $row["RECRUIT_NO"],
                                       "NAME_KANA"      => $row["NAME_KANA"],
                                       "FS_CD"          => $row["FS_CD"]
                                );
        }
        $result->free();

        //志願者基礎データ
        $baseArray = array();
        $query = knjl215fQuery::getBaseDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $row["EXAMNO"];
            $baseArray[$key] = array("EXAMNO"           => $row["EXAMNO"],
                                     "NAME_KANA"        => $row["NAME_KANA"],
                                     "FS_CD"            => $row["FS_CD"]
                                );
        }
        $result->free();

        //条件
        $checkPageno = array();
        $checkExamno = array();
        foreach ($beforeArray as $pageno => $beforeList) {
            foreach ($baseArray as $examno => $baseList) {
                if ($beforeList["NAME_KANA"] == $baseList["NAME_KANA"] && 
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
            $query.= "       AND SEQ = '002' ";
            //チェックOFFの時、事前番号クリアしない
            //つまり、事前番号NULLの人のみ処理する。
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
                //チェックOFFの時、事前番号クリアしない。
                //つまり、事前番号NULLの人のみ処理する。
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
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = '002';
                $data["REMARK1"][TEXT]          = $beforeArray[$pageno]["RECRUIT_NO"];
                $data["registercd"][TEXT]       = STAFFCD;
                $data["updated"][FUNC]          = "sysdate()";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                $db->query($query);
                $shoriKensuu++;
            }
        }

        $db->commit(); 
        Query::dbCheckIn($db);

        //事前相談未処理とは、
        //条件に一致しない。
        //条件に一致するが、1:NまたはN:1である。
        returnget_count($beforeArray) - $shoriKensuu;
    } 
}
?>

