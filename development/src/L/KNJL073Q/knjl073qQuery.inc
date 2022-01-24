<?php

require_once('for_php7.php');

class knjl073qQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        if ($namecd1 == "L013") {
            $query .= " AND NAMECD2 <> '4' ";
        }
        if ($namecd1 == "L004" || $namecd1 == "L024") {
            $query .= " AND NAMECD2 <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T2.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.PROCEDUREDIV, ";
        $query .= "     T2.TOTAL4, ";
        $query .= "     T1.JUDGEMENT, ";
        $query .= "     N1.NAME1 AS JUDGE_NAME ";
        $query .= "  FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "              ON T1.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "             AND T1.TESTDIV      = T2.TESTDIV ";
        $query .= "             AND T1.EXAMNO       = T2.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON N1.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "             AND N1.NAMECD1      = 'L013' ";
        $query .= "             AND N1.NAMECD2      = T1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND VALUE(T1.JUDGEMENT, '0') <> '4' ";  //4:欠席は対象外
        $query .= " ORDER BY ";
        //表示順序ラジオボタン 1:成績順 2:受験番号順
        if ($model->sort == "1") $query .= "     VALUE(T2.TOTAL4, -1) DESC, ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //志願コース取得
    function getExamcourse($model, $examno) {
        $query  = " SELECT ";
        $query .= "     REMARK8 AS COURSECD, ";
        $query .= "     REMARK9 AS MAJORCD, ";
        $query .= "     REMARK10 AS COURSECODE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->year."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     EXAMNO          = '".$examno."' AND ";
        $query .= "     SEQ             = '001' ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //合格対象コード取得
        $passJudgediv = array();
        $result = $db->query(knjl073qQuery::getNameMst($model->year, "L013"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMESPARE1"] == "1") {
                $passJudgediv[] = $row["VALUE"];
            }
        }

        $keyList = array();
        $keyList = explode(",", $model->hid_examno);
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];

            //チェックありの人を処理
            if ($model->field["CHK_DATA"][$receptno] != "on") continue;

            //受付データ
            $data = array();
            $data["JUDGEDIV"][TEXT]         = $model->judgediv;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND TESTDIV       = '".$model->testdiv."' ";
            $where .= "   AND RECEPTNO      = '".$receptno."' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //合格のとき志願コースをセット
            $getCourse = array();
            if (in_array($model->judgediv, $passJudgediv)) {
                $getCourse = $db->getRow(knjl073qQuery::getExamcourse($model, $examno), DB_FETCHMODE_ASSOC);
            }

            //基礎データ
            $data = array();
            $data["JUDGEMENT"][TEXT]        = $model->judgediv;
            $data["SUC_COURSECD"][TEXT]     = $getCourse["COURSECD"];
            $data["SUC_MAJORCD"][TEXT]      = $getCourse["MAJORCD"];
            $data["SUC_COURSECODE"][TEXT]   = $getCourse["COURSECODE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO        = '".$examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>