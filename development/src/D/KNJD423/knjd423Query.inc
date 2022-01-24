<?php

require_once('for_php7.php');

class knjd423Query extends Query {
    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     CASE WHEN ABBV1 = '".CTRL_SEMESTER."' THEN 1 END AS DEF_VALUE_FLG ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= " AND NAMECD1 = 'D078' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //１レコード取得 -- HREPORT_GUIDANCE_SCHREG_IMPPT_DAT
    function getRow($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_IMPPT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //更新
    function &getUpdateQuery($fields, $model) {
        $db = Query::dbCheckOut();

        //データチェック
        $dataCheck = $db->getOne(knjd423Query::getRow($model));

        //UPDATE -- HREPORT_GUIDANCE_SCHREG_IMPPT_DAT
        $data = array();
        $data["REMARK1"][TEXT]          = $fields["REMARK1"];
        $data["REMARK2"][TEXT]          = $fields["REMARK2"];
        $data["REMARK3"][TEXT]          = $fields["REMARK3"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        if (strlen($dataCheck) == 0) {
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $model->schregno;

            $db->query(Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_IMPPT_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORT_GUIDANCE_SCHREG_IMPPT_DAT", $where));
        }

        Query::dbCheckIn($db);
        return;
    }

    //削除
    function &getDeleteQuery($fields, $model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_IMPPT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
}
?>
