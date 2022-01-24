<?php

require_once('for_php7.php');

class knjl017rQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        if ($namecd1 == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }
        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $thisDiv = $model->testdiv == "1" ? "1" : "2";
        $otherDiv = $model->testdiv == "1" ? "2" : "1";

        $query  = " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BUN012.REMARK{$thisDiv} AS RECEPTNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     L064.NAME1 AS THIS_NAIDAKU, ";
        $query .= "     BUN012.REMARK{$otherDiv} AS OTHER_RECEPTNO, ";
        $query .= "     L024_2.NAME1 AS OTHER_TESTDIV_NM, ";
        $query .= "     L006_2.NAME2 AS OTHER_SH_NM, ";
        $query .= "     L064_2.NAME1 AS OTHER_NAIDAKU_NM ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN010 ON BASE.ENTEXAMYEAR = BUN010.ENTEXAMYEAR ";
        $query .= "           AND BASE.APPLICANTDIV = BUN010.APPLICANTDIV ";
        $query .= "           AND BASE.EXAMNO = BUN010.EXAMNO ";
        $query .= "           AND BUN010.SEQ = '010' ";
        $query .= "           AND BUN010.REMARK{$thisDiv} = '{$model->testdiv}' ";
        $query .= "     LEFT JOIN NAME_MST L024_2 ON L024_2.NAMECD1 = 'L024' ";
        $query .= "          AND BUN010.REMARK{$otherDiv} = L024_2.NAMECD2 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN012 ON BASE.ENTEXAMYEAR = BUN012.ENTEXAMYEAR ";
        $query .= "           AND BASE.APPLICANTDIV = BUN012.APPLICANTDIV ";
        $query .= "           AND BASE.EXAMNO = BUN012.EXAMNO ";
        $query .= "           AND BUN012.SEQ = '012' ";
        $query .= "           AND BUN012.REMARK{$thisDiv} IS NOT NULL ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN013 ON BASE.ENTEXAMYEAR = BUN013.ENTEXAMYEAR ";
        $query .= "           AND BASE.APPLICANTDIV = BUN013.APPLICANTDIV ";
        $query .= "           AND BASE.EXAMNO = BUN013.EXAMNO ";
        $query .= "           AND BUN013.SEQ = '013' ";
        $query .= "           AND BUN013.REMARK{$thisDiv} = '{$model->shdiv}' ";
        $query .= "     LEFT JOIN NAME_MST L006_2 ON L006_2.NAMECD1 = 'L006' ";
        $query .= "          AND BUN013.REMARK{$otherDiv} = L006_2.NAMECD2 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN014 ON BASE.ENTEXAMYEAR = BUN014.ENTEXAMYEAR ";
        $query .= "          AND BASE.APPLICANTDIV = BUN014.APPLICANTDIV ";
        $query .= "          AND BASE.EXAMNO = BUN014.EXAMNO ";
        $query .= "          AND BUN014.SEQ = '014' ";
        $query .= "     LEFT JOIN NAME_MST L064 ON L064.NAMECD1 = 'L064' ";
        $query .= "          AND BUN014.REMARK{$thisDiv} = L064.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L064_2 ON L064_2.NAMECD1 = 'L064' ";
        $query .= "          AND BUN014.REMARK{$otherDiv} = L064_2.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        if ($model->sort == '1') {
            $query .= "     BUN012.REMARK{$thisDiv} ";
        } else {
            $query .= "     BASE.NAME_KANA ";
        }

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $keyList = array();
        $keyList = explode(",", $model->hid_examno);
        foreach ($keyList as $key => $examno) {

            //チェックありの人を処理
            if ($model->field["CHK_DATA"][$examno] != "on") continue;

            $query = knjl017rQuery::getBun014($model, $examno);
            $bun014 = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$model->year}' ";
            $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "     AND EXAMNO = '{$examno}' ";
            $query .= "     AND SEQ = '014' ";
            $db->query($query);

            $data = array();
            $bun014["ENTEXAMYEAR"] = $model->year;
            $bun014["APPLICANTDIV"] = $model->applicantdiv;
            $bun014["EXAMNO"] = $examno;
            $bun014["SEQ"] = '014';
            foreach ($bun014 as $fieldName => $setVal) {
                if ($fieldName == "REGISTERCD") {
                    $data[$fieldName][TEXT] = STAFFCD;
                }
                if ($fieldName == "UPDATED") {
                    $data[$fieldName][FUNC] = "sysdate()";
                }
                $data[$fieldName][TEXT] = $setVal;
            }
            $thisDiv = $model->testdiv == "1" ? "1" : "2";
            $data["REMARK".$thisDiv][TEXT] = $model->naidaku;

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //内諾区分取得
    function getBun014($model, $examno) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO = '{$examno}' ";
        $query .= "     AND SEQ = '014' ";

        return $query;
    }

}
?>