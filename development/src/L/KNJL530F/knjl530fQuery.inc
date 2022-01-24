<?php

require_once('for_php7.php');

class knjl530fQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        if ($namecd1 == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //UPDATE
    function getUpdateQuery($model) {
        $query  = "";
        $query .= " SELECT ";
        //受験番号(RECEPTNO)
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "     L1.REMARK{$model->field["TESTDIV"]} AS EXAM_TYPE, ";
            $query .= "     L2.REMARK{$model->field["TESTDIV"]} AS RECEPTNO, ";
        } else {
            $query .= "     T1.TESTDIV1 AS EXAM_TYPE, ";
            $query .= "     T1.EXAMNO AS RECEPTNO, ";
        }
        $query .= "     T1.EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L0 ";
        $query .= "          ON L0.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L0.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L0.EXAMNO = T1.EXAMNO ";
        $query .= "         AND L0.SEQ = '010' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L1 ";
        $query .= "          ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L1.EXAMNO = T1.EXAMNO ";
        $query .= "         AND L1.SEQ = '011' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L2 ";
        $query .= "          ON L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L2.EXAMNO = T1.EXAMNO ";
        $query .= "         AND L2.SEQ = '012' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "     AND L0.REMARK{$model->field["TESTDIV"]} = '{$model->field["TESTDIV"]}' ";
        } else {
            $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     RECEPTNO ";

        return $query;
    }

    //UPDATE
    function &getUpdate($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //配列
        $recList = array();
        $query = knjl530fQuery::getUpdateQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $recList[] = $row;
        }
        $result->free();

        //DELETE
        $query  = " DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $db->query($query);

        //配列ループ
        foreach ($recList as $key => $row) {
            //INSERT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]      = $model->field["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]    = $row["EXAM_TYPE"];
            $data["RECEPTNO"][TEXT]     = $row["RECEPTNO"];
            $data["EXAMNO"][TEXT]       = $row["EXAMNO"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
            $db->query($query);
        }//foreach

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
