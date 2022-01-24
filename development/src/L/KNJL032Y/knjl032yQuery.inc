<?php
class knjl032yQuery extends Query {

    //受験科目
    function getTestSubclasscd($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME".$model->applicantdiv." AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.NAMECD1     = 'L009' ";
        $query .= "     AND T1.NAMECD2     NOT IN ('9') ";
//        $query .= "     AND T1.NAMESPARE1  = '1' "; //得点入力科目
        $query .= "     AND T1.NAME".$model->applicantdiv." IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //名称マスタより取得
    function getNameMst($namecd1, $year)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験時間割ヘッダデータ
    function getPtrnList(&$model)
    {
        $query  = "";
        $query .= " WITH T_PERIOD AS ( ";
        $query .= "     SELECT ";
        $query .= "         PATTERN_NO, ";
        $query .= "         COUNT(PERIODCD) AS PERIOD_CNT ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SCH_PTRN_SUB_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "         APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "         TESTDIV         = '{$model->testdiv}' ";
        $query .= "     GROUP BY ";
        $query .= "         PATTERN_NO ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     T1.PATTERN_NO, ";
        $query .= "     T1.PATTERN_NAME, ";
        $query .= "     T2.PERIOD_CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCH_PTRN_HDAT T1 ";
        $query .= "     LEFT JOIN T_PERIOD T2 ON T2.PATTERN_NO = T1.PATTERN_NO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$model->year}' AND ";
        if ($model->cmd == "edit"){
            $query .= " T1.PATTERN_NO   = '{$model->pattern_no}' AND ";
        }
        $query .= "     T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "     T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.PATTERN_NO ";
        return $query;
    }

    //試験時間割科目データ
    function getPtrnSubList(&$model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     PERIODCD, ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     S_HOUR, ";
        $query .= "     S_MINUTE, ";
        $query .= "     E_HOUR, ";
        $query .= "     E_MINUTE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCH_PTRN_SUB_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV         = '{$model->testdiv}' AND ";
        $query .= "     PATTERN_NO      = '{$model->pattern_no}' ";
        $query .= " ORDER BY ";
        $query .= "     PERIODCD ";
        return $query;
    }

    //更新
    function update(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_SCH_PTRN_HDAT
        $query  = " UPDATE ";
        $query .= "     ENTEXAM_SCH_PTRN_HDAT ";
        $query .= " SET ";
        $query .= "     PATTERN_NAME = '" .Query::addquote($model->field["PATTERN_NAME"]) ."', ";
        $query .= "     REGISTERCD = '".STAFFCD ."', ";
        $query .= "     UPDATED  = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV         = '{$model->testdiv}' AND ";
        $query .= "     PATTERN_NO      = '{$model->pattern_no}' ";
        $db->query($query);

        //ENTEXAM_SCH_PTRN_SUB_DAT
        //削除
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCH_PTRN_SUB_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV         = '{$model->testdiv}' AND ";
        $query .= "     PATTERN_NO      = '{$model->pattern_no}' ";
        $db->query($query);
        //追加
        for ($periodcd = 1; $periodcd <= (int) $model->field["PERIOD_CNT"]; $periodcd++) {
            $query  = " INSERT INTO ENTEXAM_SCH_PTRN_SUB_DAT( ";
            $query .= " ENTEXAMYEAR, ";
            $query .= " APPLICANTDIV, ";
            $query .= " TESTDIV, ";
            $query .= " PATTERN_NO, ";
            $query .= " TESTSUBCLASSCD, ";
            $query .= " PERIODCD, ";
            $query .= " S_HOUR, ";
            $query .= " S_MINUTE, ";
            $query .= " E_HOUR, ";
            $query .= " E_MINUTE, ";
            $query .= " REGISTERCD ";
            $query .= " ) ";
            $query .= " VALUES ";
            $query .= " ( ";
            $query .= " '{$model->year}', ";
            $query .= " '{$model->applicantdiv}', ";
            $query .= " '{$model->testdiv}', ";
            $query .= " '{$model->pattern_no}', ";
            $query .= " '{$model->field["TESTSUBCLASSCD".$periodcd]}', ";
            $query .= " '{$periodcd}', ";
            $query .= " '".sprintf("%02d", $model->field["S_HOUR"  .$periodcd])."', ";
            $query .= " '".sprintf("%02d", $model->field["S_MINUTE".$periodcd])."', ";
            $query .= " '".sprintf("%02d", $model->field["E_HOUR"  .$periodcd])."', ";
            $query .= " '".sprintf("%02d", $model->field["E_MINUTE".$periodcd])."', ";
            $query .= " '".STAFFCD ."' ";
            $query .= " ) ";
            $db->query($query);
        }

        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //追加
    function insert(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_SCH_PTRN_HDAT
        $query = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCH_PTRN_HDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV         = '{$model->testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     PATTERN_NO DESC ";

        //時間割パターンＮｏを設定。
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($row)){
            $pattern_no = (int) $row["PATTERN_NO"] + 1;
        } else {
            $pattern_no = 1;
        }

        $query  = " INSERT INTO ENTEXAM_SCH_PTRN_HDAT( ";
        $query .= " ENTEXAMYEAR, ";
        $query .= " APPLICANTDIV, ";
        $query .= " TESTDIV, ";
        $query .= " PATTERN_NO, ";
        $query .= " PATTERN_NAME, ";
        $query .= " REGISTERCD ";
        $query .= " ) ";
        $query .= " VALUES ";
        $query .= " ( ";
        $query .= " '{$model->year}', ";
        $query .= " '{$model->applicantdiv}', ";
        $query .= " '{$model->testdiv}', ";
        $query .= " '{$pattern_no}', ";
        $query .= " '" .Query::addquote($model->field["PATTERN_NAME"]) ."', ";
        $query .= " '".STAFFCD ."' ";
        $query .= " ) ";
        $db->query($query);

        //ENTEXAM_SCH_PTRN_SUB_DAT
        //削除
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCH_PTRN_SUB_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV         = '{$model->testdiv}' AND ";
        $query .= "     PATTERN_NO      = '{$pattern_no}' ";
        $db->query($query);
        //追加
        for ($periodcd = 1; $periodcd <= (int) $model->field["PERIOD_CNT"]; $periodcd++) {
            $query  = " INSERT INTO ENTEXAM_SCH_PTRN_SUB_DAT( ";
            $query .= " ENTEXAMYEAR, ";
            $query .= " APPLICANTDIV, ";
            $query .= " TESTDIV, ";
            $query .= " PATTERN_NO, ";
            $query .= " TESTSUBCLASSCD, ";
            $query .= " PERIODCD, ";
            $query .= " S_HOUR, ";
            $query .= " S_MINUTE, ";
            $query .= " E_HOUR, ";
            $query .= " E_MINUTE, ";
            $query .= " REGISTERCD ";
            $query .= " ) ";
            $query .= " VALUES ";
            $query .= " ( ";
            $query .= " '{$model->year}', ";
            $query .= " '{$model->applicantdiv}', ";
            $query .= " '{$model->testdiv}', ";
            $query .= " '{$pattern_no}', ";
            $query .= " '{$model->field["TESTSUBCLASSCD".$periodcd]}', ";
            $query .= " '{$periodcd}', ";
            $query .= " '".sprintf("%02d", $model->field["S_HOUR"  .$periodcd])."', ";
            $query .= " '".sprintf("%02d", $model->field["S_MINUTE".$periodcd])."', ";
            $query .= " '".sprintf("%02d", $model->field["E_HOUR"  .$periodcd])."', ";
            $query .= " '".sprintf("%02d", $model->field["E_MINUTE".$periodcd])."', ";
            $query .= " '".STAFFCD ."' ";
            $query .= " ) ";
            $db->query($query);
        }

        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //削除
    function delete(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_SCH_PTRN_HDAT
        $query = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCH_PTRN_HDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV         = '{$model->testdiv}' AND ";
        $query .= "     PATTERN_NO IN ('" .implode($model->field["CHECKED"],"','") ."') ";
        $db->query($query);
        //ENTEXAM_SCH_PTRN_SUB_DAT
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCH_PTRN_SUB_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV         = '{$model->testdiv}' AND ";
        $query .= "     PATTERN_NO IN ('" .implode($model->field["CHECKED"],"','") ."') ";
        $db->query($query);

        $db->commit(); 
        Query::dbCheckIn($db);
    }
}
?>