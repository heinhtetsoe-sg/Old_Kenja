<?php
class knjh110_2Query extends Query {

    //生徒名
    function getStudentName($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO AS VALUE, ";
        $query .= "     SCHREGNO || ':' || NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregNo}' ";
        return $query;
    }

    //科目名
    function getSubclassName($model)
    {
        $query  = " SELECT ";
        $query .= "     SUBCLASSCD AS VALUE, ";
        $query .= "     SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     SUBCLASSCD = '{$model->subclassCd}' ";
        return $query;
    }

    //リスト表示および入力項目
    function getList($model, $input = "") {
        $query  = " SELECT ";
        $query .= "     SEQ, ";
        $query .= "     REGDDATE, ";
        $query .= "     MINUTES, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_BANK_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHREGNO = '{$model->schregNo}' ";
        $query .= "     AND SUBCLASSCD = '{$model->subclassCd}' ";
        if (strlen($input)) {
            $query .= "     AND SEQ = {$model->seq} ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGDDATE, ";
        $query .= "     SEQ ";
        return $query;
    }

    //MAX_SEQ
    function getMaxSeq($model)
    {
        $query  = " SELECT ";
        $query .= "     MAX(VALUE(SEQ,0)) + 1 AS SEQ ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_BANK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregNo}' AND ";
        $query .= "     SUBCLASSCD = '{$model->subclassCd}' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUBCLASSCD ";
        return $query;
    }

    //追加
    function insertQuery($model)
    {
        $db = Query::dbCheckOut();

        //MAX_SEQ
        $query = knjh110_2Query::getMaxSeq($model);
        $seq = $db->getOne($query);
        if (!strlen($seq)) $seq = 1;
        //項目
        $data = array();
        $data["SCHREGNO"][TEXT]         = $model->schregNo;
        $data["SUBCLASSCD"][TEXT]       = $model->subclassCd;
        $data["SEQ"][NUMBER]            = $seq;
        $data["REGDDATE"][TEXT]         = str_replace("/", "-", $model->field["REGDDATE"]);
        $data["REGDYEAR"][TEXT]         = common::DateConv1(str_replace("-", "/", $model->field["REGDDATE"]), 12);
        $data["MINUTES"][NUMBER]        = $model->field["MINUTES"];
        $data["REMARK"][TEXT]           = $model->field["REMARK"];
        $data["INF_YEAR"][TEXT]         = CTRL_YEAR;
        $data["INF_SEQ"][NUMBER]        = $model->infSeq;
        $data["REGISTERCD"][TEXT]       = STAFFCD ;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //追加
        $query = Query::insertSQL($data, "SCHREG_QUALIFIED_BANK_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return $seq; //MAX_SEQ
    }

    //更新
    function updateQuery($model)
    {
        $db = Query::dbCheckOut();

        //項目
        $data = array();
        $data["REGDDATE"][TEXT]         = str_replace("/", "-", $model->field["REGDDATE"]);
        $data["REGDYEAR"][TEXT]         = common::DateConv1(str_replace("-", "/", $model->field["REGDDATE"]), 12);
        $data["MINUTES"][NUMBER]        = $model->field["MINUTES"];
        $data["REMARK"][TEXT]           = $model->field["REMARK"];
        $data["INF_YEAR"][TEXT]         = CTRL_YEAR;
        $data["INF_SEQ"][NUMBER]        = $model->infSeq;
        $data["REGISTERCD"][TEXT]       = STAFFCD ;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //条件
        $where  = " WHERE ";
        $where .= "         SCHREGNO    = '{$model->schregNo}' ";
        $where .= "     AND SUBCLASSCD  = '{$model->subclassCd}' ";
        $where .= "     AND SEQ         = {$model->seq} ";
        //更新
        $query = Query::updateSQL($data, "SCHREG_QUALIFIED_BANK_DAT", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    //削除
    function deleteQuery($model)
    {
        $db = Query::dbCheckOut();

        //削除
        $query  = " DELETE FROM SCHREG_QUALIFIED_BANK_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHREGNO    = '{$model->schregNo}' ";
        $query .= "     AND SUBCLASSCD  = '{$model->subclassCd}' ";
        $query .= "     AND SEQ         = {$model->seq} ";
        $db->query($query);

        Query::dbCheckIn($db);
    }
}
?>