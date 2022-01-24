<?php

require_once('for_php7.php');

class knjl071dQuery extends Query {
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

    //内部判定マスタより取得
    function getEntexamInternalDecisionMst($model) {
        $query  = " SELECT ";
        $query .= "     DECISION_CD AS VALUE, ";
        $query .= "     DECISION_CD || ':' || DECISION_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_INTERNAL_DECISION_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     F1.FINSCHOOL_NAME, ";
        $query .= "     B1.SUB_ORDER, ";
        $query .= "     B1.SHDIV, ";
        $query .= "     N1.NAME1 AS SHDIV_NAME, ";
        $query .= "     B1.DESIREDIV, ";
        $query .= "     N2.ABBV1 AS DESIREDIV_NAME, ";
        $query .= "     B1.JUDGEMENT, ";
        $query .= "     N3.NAME1 AS JUDGEMENT_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ";
        $query .= "          ON F1.FINSCHOOLCD  = B1.FS_CD ";
        $query .= "     LEFT JOIN NAME_MST N1 ";
        $query .= "          ON N1.NAMECD1  = 'L006' ";
        $query .= "         AND N1.NAMECD2  = B1.SHDIV ";
        $query .= "     LEFT JOIN NAME_MST N2 ";
        $query .= "          ON N2.NAMECD1  = 'L058' ";
        $query .= "         AND N2.NAMECD2  = B1.DESIREDIV ";
        $query .= "     LEFT JOIN NAME_MST N3 ";
        $query .= "          ON N3.NAMECD1  = 'L013' ";
        $query .= "         AND N3.NAMECD2  = B1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND B1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND VALUE(B1.JUDGEMENT,'')   != '4' ";
        if (strlen($model->sub_order)) {
            $query .= "     AND B1.SUB_ORDER = '{$model->sub_order}' ";
        }
        if (strlen($model->desirediv)) {
            $query .= "     AND B1.DESIREDIV = '{$model->desirediv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     B1.EXAMNO ";

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