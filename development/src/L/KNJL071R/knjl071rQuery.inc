<?php

require_once('for_php7.php');

class knjl071rQuery extends Query {

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

    //合否区分取得
    function getPassMst($namecd1, $model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1, ";
        if ($model->testdiv == "2") {
            $query .= "     NAMESPARE3 AS NAMESPARE2 ";
        } else {
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$model->year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($model->testdiv == "2") {
            $query .= "    AND NAMESPARE3 = '2' ";
        } else {
            $query .= "    AND NAMESPARE2 = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
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
        $query .= "     R1.TOTAL1 AS RESULT, ";
        $query .= "     L013.NAME1 AS THIS_JUDGEMENT, ";
        $query .= "     BUN012.REMARK{$otherDiv} AS OTHER_RECEPTNO, ";
        $query .= "     L024_2.NAME1 AS OTHER_TESTDIV_NM, ";
        $query .= "     L006_2.NAME2 AS OTHER_SH_NM, ";
        $query .= "     L064_1.NAME1 AS THIS_NAIDAKU_NM, ";
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
        $query .= "     LEFT JOIN NAME_MST L064_1 ON L064_1.NAMECD1 = 'L064' ";
        $query .= "          AND BUN014.REMARK{$thisDiv} = L064_1.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L064_2 ON L064_2.NAMECD1 = 'L064' ";
        $query .= "          AND BUN014.REMARK{$otherDiv} = L064_2.NAMECD2 ";
        //少なくとも成績が入っていないと合否判定できないので、ENTEXAM_RECEPT_DATのデータ登録は必須。
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT R1 ON R1.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "          AND R1.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "          AND R1.TESTDIV = '{$model->testdiv}' ";
        $query .= "          AND R1.EXAMNO = BASE.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ";
        $query .= "          AND R1.JUDGEDIV = L013.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        if ($model->sort == '1') {
            $query .= "     BUN012.REMARK{$thisDiv} ";
        } else {
            $query .= "     R1.TOTAL1 DESC ";
        }

        return $query;
    }

    //更新
    function getUpdateReceptQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $keyList = array();
        $keyList = explode(",", $model->hid_receptno);
        $honordivflg = $db->getOne(knjl071rQuery::getHonorDiv($model->judgement));
        foreach ($keyList as $key => $receptno) {

            //チェックありの人を処理
            if ($model->field["CHK_DATA"][$receptno] != "on") continue;

            //表示データ取得SQLの内容から、必ずENTEXAM_RECEPT_DATにデータがある前提となるので
            //処理としてはupdateのみとなる。

            $data = array();
            $data["JUDGEDIV"][TEXT] = $model->judgement;
            if ($honordivflg == "1") {
                $data["HONORDIV"][TEXT] = "1";
            } else {
                $data["HONORDIV"][TEXT] = "";
            }
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC] = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR  = '".$model->year."'";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
            $where .= "   AND TESTDIV      = '".$model->testdiv."' ";
            $where .= "   AND EXAM_TYPE    = '1' "; //1固定
            $where .= "   AND RECEPTNO     = '".$receptno."' ";

            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    function getHonorDiv($namecd2) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     ABBV2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L013' ";
        $query .= "     AND NAMECD2 = '".$namecd2."' ";
        return $query;
    }

    //更新
    function getUpdateBaseQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //データ取得
        $query = knjl071rQuery::getReceptJudegeAll($model);

        $result = $db->query($query);
        $judgeArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!is_array($judgeArray[$row["EXAMNO"]])) {
                $judgeArray[$row["EXAMNO"]]["SH_DIV"] = $row["SH_DIV"];
                $judgeArray[$row["EXAMNO"]]["PASS_LEVEL"] = $row["PASS_LEVEL"];
                $judgeArray[$row["EXAMNO"]]["JUDGEDIV"] = $row["JUDGEDIV"];
            } else if ($row["SH_DIV"] < $judgeArray[$row["EXAMNO"]]["SH_DIV"]) {
                $judgeArray[$row["EXAMNO"]]["SH_DIV"] = $row["SH_DIV"];
                $judgeArray[$row["EXAMNO"]]["PASS_LEVEL"] = $row["PASS_LEVEL"];
                $judgeArray[$row["EXAMNO"]]["JUDGEDIV"] = $row["JUDGEDIV"];
            } else if ($row["SH_DIV"] == $judgeArray[$row["EXAMNO"]]["SH_DIV"] &&
                       $row["PASS_LEVEL"] <= $judgeArray[$row["EXAMNO"]]["PASS_LEVEL"]
            ) {
                $judgeArray[$row["EXAMNO"]]["SH_DIV"] = $row["SH_DIV"];
                $judgeArray[$row["EXAMNO"]]["PASS_LEVEL"] = $row["PASS_LEVEL"];
                $judgeArray[$row["EXAMNO"]]["JUDGEDIV"] = $row["JUDGEDIV"];
            }
        }

        $keyList = array();
        $keyList = explode(",", $model->hid_receptno);
        foreach ($keyList as $key => $receptno) {

            //チェックありの人を処理
            if ($model->field["CHK_DATA"][$receptno] != "on") continue;

            $setExamno = $model->receptInfo[$receptno]["EXAMNO"];
            $data = array();
            $data["JUDGEMENT"][TEXT] = $judgeArray[$setExamno]["JUDGEDIV"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC] = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR  = '{$model->year}'";
            $where .= "   AND APPLICANTDIV = '{$model->applicantdiv}'";
            $where .= "   AND EXAMNO       = '{$setExamno}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR  = '{$model->year}'";
            $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}'";
            $query .= "   AND EXAMNO       = '{$setExamno}' ";
            $query .= "   AND SEQ          = '030' ";
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $setExamno;
            $data["SEQ"][TEXT]          = '030';
            $data["REMARK1"][TEXT]      = $model->testdiv;
            $data["REMARK2"][TEXT]      = $receptno;
            $data["REMARK3"][TEXT]      = $judgeArray[$setExamno]["JUDGEDIV"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    function getReceptJudegeAll($model) {
        $query  = " SELECT ";
        $query .= "     RECEPT.ENTEXAMYEAR, ";
        $query .= "     RECEPT.APPLICANTDIV, ";
        $query .= "     RECEPT.TESTDIV, ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     RECEPT.EXAMNO, ";
        $query .= "     CASE WHEN RECEPT.TESTDIV = '1' ";
        $query .= "          THEN BUN013.REMARK1 ";
        $query .= "          ELSE BUN013.REMARK2 ";
        $query .= "     END AS SH_DIV, ";
        $query .= "     L013.NAME3 AS PASS_LEVEL, ";
        $query .= "     RECEPT.JUDGEDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN013 ON BUN013.SEQ = '013' ";
        $query .= "          AND BUN013.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "          AND BUN013.EXAMNO = RECEPT.EXAMNO ";
        $query .= "     INNER JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ";
        $query .= "           AND RECEPT.JUDGEDIV = L013.NAMECD2 ";
        $query .= "           AND L013.NAMESPARE1 = '1' ";
        $query .= " WHERE ";
        $query .= "     RECEPT.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND RECEPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND RECEPT.EXAM_TYPE = '1' ";
        $query .= " ORDER BY ";
        $query .= "     RECEPT.EXAMNO, ";
        $query .= "     RECEPT.RECEPTNO ";

        return $query;
    }

    //内諾区分取得
    function getReceptDat($model, $examno) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV = '{$model->testdiv}' ";
        $query .= "     AND EXAMNO = '{$examno}' ";

        return $query;
    }

}
?>
