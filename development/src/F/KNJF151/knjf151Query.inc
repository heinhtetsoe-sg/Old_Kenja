<?php

require_once('for_php7.php');

class knjf151Query extends Query {
    //生徒情報取得
    function getSchInfo($schregno) {
        $query  = " SELECT  ";
        $query .= "     T3.HR_NAME || T1.ATTENDNO || '番　' || T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "          ON T1.YEAR     = T3.YEAR ";
        $query .= "         AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "         AND T1.GRADE    = T3.GRADE ";
        $query .= "         AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //データ一覧取得
    function getList($model, $div, $val="") {
        list ($date, $hour, $minute) = explode(':', $val);

        $query  = " SELECT ";
        if ($div == "data") {
            $query .= "     T1.* ";
        } else {
            $query .= "     T1.VISIT_DATE, ";
            $query .= "     T1.VISIT_HOUR, ";
            $query .= "     T1.VISIT_MINUTE, ";
            $query .= "     T1.RELATIONSHIP, ";
            $query .= "     L1.NAME1 AS RELATIONSHIP_SHOW, ";
            $query .= "     T1.CONSULTATION_METHOD, ";
            $query .= "     L2.NAME1 AS CONSULTATION_METHOD_SHOW, ";
            $query .= "     T1.CONSULTATION_KIND1, ";
            $query .= "     L3.NAME1 AS CONSULTATION_KIND1_SHOW, ";
            $query .= "     T1.CONSULTATION_KIND2, ";
            $query .= "     L4.NAME1 AS CONSULTATION_KIND2_SHOW, ";
            $query .= "     T1.SPECIAL_NOTE ";
        }
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        if ($div == "list") {
            $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'F221' AND T1.RELATIONSHIP = L1.NAMECD2 ";
            $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'F215' AND T1.CONSULTATION_METHOD = L2.NAMECD2 ";
            $query .= "     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'F222' AND T1.CONSULTATION_KIND1 = L3.NAMECD2 ";
            $query .= "     LEFT JOIN NAME_MST L4 ON L4.NAMECD1 = 'F223' AND T1.CONSULTATION_KIND2 = L4.NAMECD2 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     T1.TYPE        = '6' ";
        if ($div == "data") {
            $query .= " AND T1.VISIT_DATE   = '".str_replace("/", "-", $date)."' ";
            $query .= " AND T1.VISIT_HOUR   = '".$hour."' ";
            $query .= " AND T1.VISIT_MINUTE = '".$minute."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.VISIT_DATE, ";
        $query .= "     T1.VISIT_HOUR, ";
        $query .= "     T1.VISIT_MINUTE ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["VISIT_DATE"][TEXT]           = str_replace("/", "-", $model->field["VISIT_DATE"]);
        $data["VISIT_HOUR"][TEXT]           = $model->field["VISIT_HOUR"];
        $data["VISIT_MINUTE"][TEXT]         = $model->field["VISIT_MINUTE"];
        $data["TYPE"][TEXT]                 = '6';
        $data["RELATIONSHIP"][TEXT]         = $model->field["RELATIONSHIP"];
        $data["CONSULTATION_METHOD"][TEXT]  = $model->field["CONSULTATION_METHOD"];
        $data["CONSULTATION_KIND1"][TEXT]   = $model->field["CONSULTATION_KIND1"];
        $data["CONSULTATION_KIND2"][TEXT]   = $model->field["CONSULTATION_KIND2"];
        $data["SPECIAL_NOTE"][TEXT]         = $model->field["SPECIAL_NOTE"];
        $data["REMARK"][TEXT]               = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "NURSEOFF_VISITREC_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["RELATIONSHIP"][TEXT]         = $model->field["RELATIONSHIP"];
        $data["CONSULTATION_METHOD"][TEXT]  = $model->field["CONSULTATION_METHOD"];
        $data["CONSULTATION_KIND1"][TEXT]   = $model->field["CONSULTATION_KIND1"];
        $data["CONSULTATION_KIND2"][TEXT]   = $model->field["CONSULTATION_KIND2"];
        $data["SPECIAL_NOTE"][TEXT]         = $model->field["SPECIAL_NOTE"];
        $data["REMARK"][TEXT]               = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = " WHERE ";
        $where .= "     SCHREGNO        = '".$model->schregno."' AND ";
        $where .= "     VISIT_DATE      = '".str_replace("/", "-", $model->visit_date)."' AND ";
        $where .= "     VISIT_HOUR      = '".$model->visit_hour."' AND ";
        $where .= "     VISIT_MINUTE    = '".$model->visit_minute."' AND ";
        $where .= "     TYPE            = '6' ";

        $query = Query::updateSQL($data, "NURSEOFF_VISITREC_DAT", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO        = '".$model->schregno."' AND ";
        $query .= "     VISIT_DATE      = '".str_replace("/", "-", $model->visit_date)."' AND ";
        $query .= "     VISIT_HOUR      = '".$model->visit_hour."' AND ";
        $query .= "     VISIT_MINUTE    = '".$model->visit_minute."' AND ";
        $query .= "     TYPE            = '6' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
    }
}
?>
