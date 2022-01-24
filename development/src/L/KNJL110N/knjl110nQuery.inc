<?php

require_once('for_php7.php');

class knjl110nQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year) {
        $query  = " SELECT ";
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
    function SelectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.PROCEDUREDATE, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     L1.REMARK6, ";
        $query .= "     L1.REMARK7, ";
        $query .= "     L1.REMARK8 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                                  AND L1.EXAMNO = T1.EXAMNO ";
        $query .= "                                                  AND L1.SEQ = '006' ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV        = '".$model->testdiv."' ";
        $query .= "     AND T1.JUDGEMENT IN ('1', '3') ";
        $query .= "     AND T1.PROCEDUREDIV = '1' ";
        $query .= "     AND T1.PROCEDUREDATE IS NOT NULL ";
        $query .= "     AND T1.ENTDIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     T1.PROCEDUREDATE, ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        $tmp = explode(",", $model->hid_examno);
        $examno_array = array();
        foreach ($tmp as $key => $examno_count) {
            $examno_array = explode("-", $examno_count);
            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR        = '".$model->ObjYear."' ";
            $query .= "     AND EXAMNO         = '".$examno_array[0]."' ";
            $query .= "     AND SEQ            = '006' ";
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["EXAMNO"][TEXT]               = $examno_array[0];
            $data["SEQ"][TEXT]                  = '006';
            $data["REMARK6"][TEXT]              = $model->remark6[$examno_array[1]];
            $data["REMARK7"][TEXT]              = $model->remark7[$examno_array[1]];
            $data["REMARK8"][TEXT]              = $model->remark8[$examno_array[1]];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>