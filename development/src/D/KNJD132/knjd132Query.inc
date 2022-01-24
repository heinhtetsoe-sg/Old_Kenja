<?php

require_once('for_php7.php');

class knjd132Query extends Query {
    //学期コンボ
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND SEMESTER <> '9' ";

        return $query;
    }

    //学期名
    function getSemesterName($semester) {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '{$semester}' ";

        return $query;
    }

    //備考1を取得
    function selectRemark1($model) {
        $query  = " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //備考2を取得
    function selectRemark2($model) {
        $query  = " SELECT ";
        $query .= "     REMARK2 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    /* 更新処理 */
    function update(&$db, $model) {
        //レコードがあるのかどうか確認する(あればアップデート、なければインサート)
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        $cnt = $db->getOne($query);

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["REMARK1"][TEXT]          = $model->field["REMARK1"];
        if ($model->field["SEMESTER"] != '9') {
            $data["REMARK2"][TEXT]      = $model->field["REMARK2"];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE YEAR     = '".CTRL_YEAR."' AND ";
        $where .= "       SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $where .= "       SCHREGNO = '{$model->schregno}' ";

        if ($cnt < 1) {
            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
        } else {
            $query = Query::updateSQL($data, "HREPORTREMARK_DAT",$where);
        }

        $db->query($query);
    }
}
?>
