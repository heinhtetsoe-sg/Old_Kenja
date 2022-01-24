<?php

require_once('for_php7.php');

class knjh186Query extends Query {
    //一覧取得
    function getChildcareDat($model, $date="") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHILDCARE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        if ($date) {
            $query .= " AND CARE_DATE   = '".str_replace("/", "-", $date)."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     CARE_DATE DESC ";
        }

        return $query;
    }

    //預かり金一覧取得
    function getFareList() {
        $query .= " SELECT ";
        $query .= "     T1.FARE_CD, ";
        $query .= "     T1.FARE ";
        $query .= " FROM ";
        $query .= "     CHILDCARE_FARE_MST T1, ";
        $query .= "     CHILDCARE_FARE_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.FARE_CD  = T2.FARE_CD AND ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.FARE_CD ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $date = str_replace("/", "-", $fields["CARE_DATE"]);
        $exists = $db->getOne(knjh186Query::getChildcareDat($model, $date));

        $data = array();
        if (!$exists) {
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["CARE_DATE"][TEXT]        = $date;
        }
        $data["FARE_CD"][TEXT]          = ($fields["FARE_CD"] == "999") ? "" : $fields["FARE_CD"];
        $data["PICK_UP"][TEXT]          = $fields["PICK_UP"];
        $data["REMARK"][TEXT]           = $fields["REMARK"];
        $data["EXTRACURRICULAR_CLASS"][TEXT] = $fields["EXTRACURRICULAR_CLASS"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if (!$exists) {
            //追加
            $query = Query::insertSQL($data, "CHILDCARE_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $where .= "     CARE_DATE   = '".$date."' ";

            //更新
            $query = Query::updateSQL($data, "CHILDCARE_DAT", $where);
        }

        $db->query($query);
        Query::dbCheckIn($db);
        return ;
    }

    // DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     CHILDCARE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CARE_DATE   = '".str_replace("/", "-", $fields["CARE_DATE"])."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
