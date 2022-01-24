<?php

require_once('for_php7.php');

class knjd138cQuery extends Query {

    //最大学期取得
    function getMaxSemester() {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) AS MAX_SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // HREPORTREMARK_DAT取得
    function getHreportremarkDat($model, $semes) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->exp_year}' AND ";
        $query .= "     SEMESTER    = '{$semes}' AND ";
        $query .= "     SCHREGNO    = '{$model->schregno}' ";

        return $query;
    }

    // HREPORTREMARKDETAIL_DAT取得
    function getHreportremarkDetailDat($model, $semes, $div, $cdval) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '{$model->exp_year}' AND ";
        $query .= "     T1.SEMESTER    = '{$semes}' AND ";
        $query .= "     T1.SCHREGNO    = '{$model->schregno}' AND ";
        $query .= "     T1.DIV = '{$div}' AND ";
        $query .= "     T1.CODE = '{$cdval}' ";

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // HREPORTREMARK_DAT(所見)
        //存在チェック
        $setsems = $model->field["SEMESTER"];
        $chk = $db->getCol(knjd138cQuery::getHreportremarkDat($model, $setsems));

        if (get_count($chk) == 0) {
            $data = array();
            $data["YEAR"][TEXT]              = $model->exp_year;
            $data["SEMESTER"][TEXT]          = $setsems;
            $data["SCHREGNO"][TEXT]          = $model->schregno;
            $data["ATTENDREC_REMARK"][TEXT]  = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]        = STAFFCD;
            $data["UPDATED"][FUNC]           = "sysdate()";

            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
        } else {
            $data = array();
            $data["ATTENDREC_REMARK"][TEXT]  = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]        = STAFFCD;
            $data["UPDATED"][FUNC]           = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER = '".$setsems."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
        }
        $db->query($query);

        //HREPORTREMARK_DETAIL_DAT

        //学級活動
        knjd138cQuery::updateDetailQuery($db, $model, $setsems, "01", "01");

        //生徒会活動
        knjd138cQuery::updateDetailQuery($db, $model, $setsems, "01", "02");

        //学校行事
        knjd138cQuery::updateDetailQuery($db, $model, $setsems, "01", "03");

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return ;
    }

    //更新処理
    function updateDetailQuery($db, $model, $setsems, $divval, $cdval) {
        $chk = $db->getCol(knjd138cQuery::getHreportremarkDetailDat($model, $setsems, $divval, $cdval));

        $setVal = $model->field["REMARK_{$divval}_{$cdval}"];
        if (get_count($chk) == 0) {
            $data = array();
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = $setsems;
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["DIV"][TEXT]          = $divval;
            $data["CODE"][TEXT]         = $cdval;
            $data["REMARK1"][TEXT]      = $setVal;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
        } else {
            $data = array();
            $data["REMARK1"][TEXT]      = $setVal;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '{$model->exp_year}' AND ";
            $where .= "     SEMESTER = '{$setsems}' AND ";
            $where .= "     SCHREGNO = '{$model->schregno}' AND ";
            $where .= "     DIV = '{$divval}' AND ";
            $where .= "     CODE = '{$cdval}' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
        }
        $db->query($query);
    }
}
?>
