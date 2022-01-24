<?php

require_once('for_php7.php');

class knjl171dQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分リスト取得
    function getTestDivList($year) {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = 'L004' ";
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //MAX_TESTDIV取得
    function getMaxTestDiv($year) {
        $query  = " SELECT ";
        $query .= "     max(TESTDIV) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";
        $query .= " GROUP BY ";
        $query .= "     ENTEXAMYEAR ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model, $div) {
        $query  = " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.JUDGEMENT, ";
        $query .= "     N3.NAME1 AS JUDGEMENT_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN NAME_MST N3 ";
        $query .= "          ON N3.NAMECD1  = 'L013' ";
        $query .= "         AND N3.NAMECD2  = B1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND B1.TESTDIV      = '{$model->testdiv}' ";
        if ($div == "list") {
            if ($model->cmd == "next") {
                $query .= "         AND B1.EXAMNO > '{$model->e_examno}' ";
            } else if ($model->cmd == "back") {
                $query .= "         AND B1.EXAMNO < '{$model->s_examno}' ";
            } else {
                if (strlen($model->s_examno)) {
                    $query .= "         AND B1.EXAMNO >= '{$model->s_examno}' ";
                }
            }

            $query .= "     ORDER BY ";
            $query .= "         B1.EXAMNO ";
            $query .= "     FETCH FIRST 40 ROWS ONLY ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     B1.EXAMNO ";
        }

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新
        foreach ($model->judge as $examno => $value) {
            $data = array();
            $data["JUDGEMENT"][TEXT]        = !strlen($value) ? "" : $value;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
            $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
            $where .= "     EXAMNO          = '".$examno."' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>