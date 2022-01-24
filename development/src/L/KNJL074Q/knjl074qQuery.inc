<?php

require_once('for_php7.php');

class knjl074qQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
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

    //一覧表示
    function SelectQuery($model) {
        $query  = " SELECT ";
        $query .= "     BASE_D.EXAMNO, ";
        $query .= "     BASE_D.NAME, ";
        $query .= "     CASE ";
        $query .= "         WHEN DTAL_D.REMARK1 = '1' THEN '特別' ";
        $query .= "         WHEN DTAL_D.REMARK1 = '2' THEN '一般' ";
        $query .= "     END AS REMARK1, ";
        $query .= "     CASE ";
        $query .= "         WHEN DTAL_D.REMARK2 = '1' THEN '採用' ";
        $query .= "     END AS REMARK2, ";
        $query .= "     BASE_D.JUDGEMENT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE_D ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DTAL_D  ";
        $query .= "              ON BASE_D.ENTEXAMYEAR  = DTAL_D.ENTEXAMYEAR  ";
        $query .= "             AND BASE_D.APPLICANTDIV = DTAL_D.APPLICANTDIV ";
        $query .= "             AND BASE_D.EXAMNO       = DTAL_D.EXAMNO ";
        $query .= "             AND DTAL_D.SEQ          = '023' ";
        $query .= "     LEFT JOIN V_NAME_MST NAME_M ";
        $query .= "              ON NAME_M.YEAR         = BASE_D.ENTEXAMYEAR ";
        $query .= "             AND NAME_M.NAMECD1      = 'L013' ";
        $query .= "             AND NAME_M.NAMECD2      = BASE_D.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "         BASE_D.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND BASE_D.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND BASE_D.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND NAME_M.NAMESPARE1   = '1' ";//合格者
        $query .= "     AND DTAL_D.REMARK1      IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     BASE_D.EXAMNO ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->arr_examno as $key => $examno) {
            //受付データ
            $data = array();
            $data["REMARK2"][TEXT]          = ($model->field["CHK_DATA"][$examno] == "on") ? "1" : "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO        = '".$examno."' ";
            $where .= "   AND SEQ           = '023' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            $db->query($query);

        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>