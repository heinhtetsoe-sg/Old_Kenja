<?php

require_once('for_php7.php');

class knjl075pQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T2.NAME, ";
        if ($model->field["MENSETU"] == "1") {
            $query .= "     T1.TOTAL4 AS TOTAL, ";
            $query .= "     T1.JUDGE_DEVIATION AS JUDGE_DEVIATION, ";
            $query .= "     T1.TOTAL_RANK4 AS TOTAL_RANK, ";
        } else {
            $query .= "     T1.TOTAL2 AS TOTAL, ";
            $query .= "     T1.LINK_JUDGE_DEVIATION AS JUDGE_DEVIATION, ";
            $query .= "     T1.TOTAL_RANK2 AS TOTAL_RANK, ";
        }
        $query .= "     T1.HONORDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR    = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV        = '".$model->testdiv."' ";
        $query .= "     AND T1.JUDGEDIV       = '1' ";//合格者
        $query .= " ORDER BY ";
        if ($model->field["ORDERDIV"] == "1") {
            $query .= "     T1.TOTAL_RANK4, ";
        }
        $query .= "     T1.RECEPTNO ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $keyList = array();
        $keyList = explode(",", $model->hid_examno);
        //チェックあり：特待   チェックなし：NULL
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];

            //受付データ
            $data = array();
            $data["HONORDIV"][TEXT]         = $model->field["CHK_DATA"][$receptno];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND TESTDIV       = '".$model->testdiv."' ";
            $where .= "   AND RECEPTNO      = '".$receptno."' ";
            $where .= "   AND VALUE(HONORDIV, '1') = '1' "; //特待またはNULL
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //基礎データ
            $data = array();
            $data["JUDGE_KIND"][TEXT]        = $model->field["CHK_DATA"][$receptno];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO        = '".$examno."'";
            $where .= "   AND VALUE(JUDGE_KIND, '1') = '1' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>