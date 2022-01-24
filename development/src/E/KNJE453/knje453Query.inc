<?php

require_once('for_php7.php');

class knje453Query extends Query {
    //定型文取得
    function getRemark($model) {
        $query  =  " SELECT ";
        $query .=  "    REMARK ";
        $query .=  " FROM ";
        $query .=  "    ASSESSMENT_TEMP_MST ";
        $query .=  " WHERE ";
        $query .=  "    YEAR        = '".CTRL_YEAR."' AND ";
        $query .=  "    GRADE       = '00' AND ";
        $query .=  "    DATA_DIV    = '".$model->data_div."' ";

        return $query;
    }

    //更新処理
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  =  " DELETE FROM ";
        $query .=  "    ASSESSMENT_TEMP_MST ";
        $query .=  " WHERE ";
        $query .=  "    YEAR        = '".CTRL_YEAR."' AND ";
        $query .=  "    GRADE       = '00' AND ";
        $query .=  "    DATA_DIV    = '".$model->data_div."' ";
        $db->query($query);

        if (strlen($model->field["REMARK"]) > 0) {
            //追加
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["GRADE"][TEXT]        = '00';
            $data["DATA_DIV"][TEXT]     = $model->data_div;
            $data["REMARK"][TEXT]       = $model->field["REMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $query = Query::insertSQL($data, "ASSESSMENT_TEMP_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //前年度データ取得
    function getCopyData($year, $flg) {
        $query  =  " SELECT ";
        if ($flg == "cnt") {
            $query .=  "    COUNT(*) ";
        } else {
            $query .=  "    * ";
        }
        $query .=  " FROM ";
        $query .=  "    ASSESSMENT_TEMP_MST ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".$year."' ";

        return $query;
    }

    //前年度コピー処理
    function &getCopyQuery() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  =  " DELETE FROM ";
        $query .=  "    ASSESSMENT_TEMP_MST ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".CTRL_YEAR."' ";
        $db->query($query);

        //前年度データ取得
        $pre_year = CTRL_YEAR - 1;
        $query = knje453Query::getCopyData($pre_year, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //追加
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["GRADE"][TEXT]        = $row["GRADE"];
            $data["DATA_DIV"][TEXT]     = $row["DATA_DIV"];
            $data["REMARK"][TEXT]       = $row["REMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $query = Query::insertSQL($data, "ASSESSMENT_TEMP_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
