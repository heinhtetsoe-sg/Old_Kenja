<?php

require_once('for_php7.php');

class knjd130jQuery extends Query {

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
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$semes."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    // HREPORTREMARKDETAIL_DAT取得
    function getHreportremarkDetailDat($model, $code, $semes) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".$model->exp_year."' AND ";
        $query .= "     T1.SEMESTER    = '".$semes."' AND ";
        $query .= "     T1.SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     T1.DIV = '01' AND ";
        $query .= "     T1.CODE = '".$code."' ";

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        // HREPORTREMARK_DAT(総合学習)
        for ($setsems=1;$setsems<=3;$setsems++) {
            //存在チェック
            $chk = $db->getCol(knjd130jQuery::getHreportremarkDat($model, $setsems));

            if (get_count($chk) == 0) {
                $data = array();
                $data["YEAR"][TEXT]            = $model->exp_year;
                $data["SEMESTER"][TEXT]        = $setsems;
                $data["SCHREGNO"][TEXT]        = $model->schregno;
                $data["TOTALSTUDYTIME"][TEXT]  = $model->field["REMARK1_".$setsems];
                $data["REGISTERCD"][TEXT]      = STAFFCD;
                $data["UPDATED"][FUNC]         = "sysdate()";

                $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            } else {
                $data = array();
                $data["TOTALSTUDYTIME"][TEXT]  = $model->field["REMARK1_".$setsems];
                $data["REGISTERCD"][TEXT]      = STAFFCD;
                $data["UPDATED"][FUNC]         = "sysdate()";

                $where  = " WHERE ";
                $where .= "     YEAR     = '".$model->exp_year."' AND ";
                $where .= "     SEMESTER = '".$setsems."' AND ";
                $where .= "     SCHREGNO = '".$model->schregno."' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
            }
            $db->query($query);
        }

        $divval = "01";
        //HREPORTREMARK_DETAIL_DAT(委員会・係)
        $setsems = "9";
        $cdval = "01";
        $chk = $db->getCol(knjd130jQuery::getHreportremarkDetailDat($model, $cdval, $setsems));

        if (get_count($chk) == 0) {
            $data = array();
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = $setsems;
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["DIV"][TEXT]          = $divval;
            $data["CODE"][TEXT]         = $cdval;
            $data["REMARK1"][TEXT]      = $model->field["REMARK2"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
        } else {
            $data = array();
            $data["REMARK1"][TEXT]      = $model->field["REMARK2"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER = '".$setsems."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' AND ";
            $where .= "     DIV = '".$divval."' AND ";
            $where .= "     CODE = '".$cdval."' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
        }
        $db->query($query);

        //HREPORTREMARK_DETAIL_DAT(クラブ・同好会)
        $cdval = "02";
        for ($setsems=1;$setsems<=3;$setsems++) {
            //HREPORTREMARK_DETAIL_DAT(資格)
            $chk = $db->getCol(knjd130jQuery::getHreportremarkDetailDat($model, $cdval, $setsems));

            if (get_count($chk) == 0) {
                $data = array();
                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SEMESTER"][TEXT]     = $setsems;
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = $divval;
                $data["CODE"][TEXT]         = $cdval;
                $data["REMARK1"][TEXT]      = $model->field["REMARK3_".$setsems];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
            } else {
                $data = array();
                $data["REMARK1"][TEXT]      = $model->field["REMARK3_".$setsems];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $where  = " WHERE ";
                $where .= "     YEAR     = '".$model->exp_year."' AND ";
                $where .= "     SEMESTER = '".$setsems."' AND ";
                $where .= "     SCHREGNO = '".$model->schregno."' AND ";
                $where .= "     DIV = '".$divval."' AND ";
                $where .= "     CODE = '".$cdval."' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
            }
            $db->query($query);
        }
        //HREPORTREMARK_DETAIL_DAT(資格)
        $setsems = "9";
        $cdval = "03";
        $chk = $db->getCol(knjd130jQuery::getHreportremarkDetailDat($model, $cdval, $setsems));

        if (get_count($chk) == 0) {
            $data = array();
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = $setsems;
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["DIV"][TEXT]          = $divval;
            $data["CODE"][TEXT]         = $cdval;
            $data["REMARK1"][TEXT]      = $model->field["REMARK4"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
        } else {
            $data = array();
            $data["REMARK1"][TEXT]      = $model->field["REMARK4"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER = '".$setsems."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' AND ";
            $where .= "     DIV = '".$divval."' AND ";
            $where .= "     CODE = '".$cdval."' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
        }
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return ;
    }
}
?>
