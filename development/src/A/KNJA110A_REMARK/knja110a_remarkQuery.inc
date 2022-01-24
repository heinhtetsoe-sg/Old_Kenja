<?php

require_once('for_php7.php');

class knja110a_remarkQuery extends Query {

    function getBaseRemark($model) {
        $query .= " SELECT ";
        $query .= "   T1.CODE AS VALUE, ";
        $query .= "   T1.NAME AS LABEL  ";
        $query .= " FROM ";
        $query .= "   BASE_REMARK_MST T1 ";
        return($query);
    }

    function getBaseRemarkDetail($model) {
        $query .= " SELECT ";
        $query .= "   T1.SEQ  AS VALUE, ";
        $query .= "   T1.NAME AS LABEL  ";
        $query .= " FROM ";
        $query .= "   BASE_REMARK_DETAIL_MST T1 ";
        $query .= " WHERE ";
        $query .= "   T1.CODE = '{$model->field["CODE"]}' ";
        return($query);
    }

    function getRemark($model) {
        $query  = " WITH BASE AS( ";
        $query .= "   SELECT ";
        $query .= "     T1.CODE, ";
        $query .= "     T1.NAME AS CODE_NAME, ";
        $query .= "     T2.SEQ, ";
        $query .= "     T2.NAME AS SEQ_NAME, ";
        $query .= "     T2.QUESTION_CONTENTS, ";
        $query .= "     T2.ANSWER_PATTERN, ";
        $query .= "     T2.ANSWER_SELECT_COUNT ";
        $query .= "   FROM ";
        $query .= "     BASE_REMARK_MST T1 ";
        $query .= "     LEFT JOIN BASE_REMARK_DETAIL_MST T2 ";
        $query .= "            ON T2.CODE = T1.CODE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.CODE, ";
        $query .= "   T1.CODE_NAME, ";
        $query .= "   T1.SEQ, ";
        $query .= "   T1.SEQ_NAME, ";
        $query .= "   T1.QUESTION_CONTENTS, ";
        $query .= "   T1.ANSWER_PATTERN, ";
        $query .= "   T1.ANSWER_SELECT_COUNT, ";
        $query .= "   T2.REMARK ";
        $query .= " FROM ";
        $query .= "   BASE T1 ";
        $query .= "   LEFT JOIN SCHREG_BASE_REMARK_DAT T2 ";
        $query .= "          ON T2.CODE     = T1.CODE ";
        $query .= "         AND T2.SEQ      = T1.SEQ  ";
        $query .= "         AND T2.YEAR     = '{$model->exp_year}' ";
        $query .= "         AND T2.SCHREGNO = '{$model->schregno}' ";
        $query .= " WHERE ";
        $query .= "   T1.CODE    = '{$model->field["CODE"]}' ";
        $query .= "   AND T1.SEQ = '{$model->field["SEQ"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CODE, ";
        $query .= "     T1.SEQ ";
        return($query);
    }

    //UPDATE処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        //DELETE
        $query  = " DELETE FROM  ";
        $query .= "   SCHREG_BASE_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR         = '{$model->exp_year}' ";
        $query .= "   AND SCHREGNO = '{$model->schregno}' ";
        $query .= "   AND CODE     = '{$model->field["CODE"]}' ";
        $query .= "   AND SEQ      = '{$model->field["SEQ"]}' ";
        $db->query($query);

        //INSERT
        $data = array();
        $data["YEAR"][TEXT]             = $model->exp_year;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["CODE"][TEXT]             = $model->field["CODE"];
        $data["SEQ"][TEXT]              = $model->field["SEQ"];
        $data["ANSWER_PATTERN"][TEXT]   = $model->field["ANSWER_PATTERN"];
        $data["REMARK"][TEXT]           = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "SCHREG_BASE_REMARK_DAT");
        $db->query($query);

        return;
    }
}
?>
