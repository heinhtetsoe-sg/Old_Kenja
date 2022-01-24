<?php

require_once('for_php7.php');

class knjd139sQuery extends Query {

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // HREPORTREMARK_DAT取得
    function getHreportremarkDat($model, $semes, $schregNo = '') {
        $schregNo = $schregNo ? $schregNo : $model->schregno;
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$semes."' AND ";
        $query .= "     SCHREGNO    = '".$schregNo."' ";
        return $query;
    }
    
    // HREPORTREMARKDETAIL_DATをCODEの値をもとに取得
    function getHreportremarkDetailDatByCode($model, $semes, $div, $code) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".$model->exp_year."' AND ";
        $query .= "     T1.SEMESTER    = '".$semes."' AND ";
        $query .= "     T1.SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     T1.DIV = '".$div."' AND ";
        $query .= "     T1.CODE = '".$code."' ";
        return $query;
    }

    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // HREPORTREMARK_DAT(所見)
        //存在チェック
        $setsems = $model->field["SEMESTER"];
        $chk = $db->getCol(knjd139sQuery::getHreportremarkDat($model, $setsems));
        if (get_count($chk) == 0) {
            $data = array();
            $data["YEAR"][TEXT]               = $model->exp_year;
            $data["SEMESTER"][TEXT]           = $setsems;
            $data["SCHREGNO"][TEXT]           = $model->schregno;
            $data["ATTENDREC_REMARK"][TEXT]   = $model->field["ATTENDREC_REMARK"];
            $data["COMMUNICATION"][TEXT]      = $model->field["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";

            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
        } else {
            $data = array();
            $data["ATTENDREC_REMARK"][TEXT]   = $model->field["ATTENDREC_REMARK"];
            $data["COMMUNICATION"][TEXT]      = $model->field["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR              = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER          = '".$setsems."' AND ";
            $where .= "     SCHREGNO          = '".$model->schregno."' ";
            
            $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
        }
        $db->query($query);

        //HREPORTREMARK_DETAIL_DAT
        //DIV=01,CODE列が01～03のREMARK1を更新
        $divval = "01";
        $setsems = "9";
        for ($count = 1; $count <= 3; $count++) {
            $cdval = sprintf("%02d", $count);
            $chk = $db->getCol(knjd139sQuery::getHreportremarkDetailDatByCode($model, $setsems, $divval, $cdval));
            if (get_count($chk) == 0) {
                $data = array();
                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SEMESTER"][TEXT]     = $setsems;
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = $divval;
                $data["CODE"][TEXT]         = $cdval;
                $data["REMARK1"][TEXT]      = $model->field["REMARK".$divval."_".$cdval];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
            } else {
                $data = array();
                $data["REMARK1"][TEXT]      = $model->field["REMARK".$divval."_".$cdval];
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

        $divval = "02";
        for ($count = 1; $count <= 4; $count++) {
            //プライマリーの場合は、その他の項目は無いので、skip
            if ($count == 4 && $model->dispPatern == 1) {
                continue;
            }
            $setsems = $count == 2 ? "9" : $model->field["SEMESTER"];
            $cdval = sprintf("%02d", $count);
            $chk = $db->getCol(knjd139sQuery::getHreportremarkDetailDatByCode($model, $setsems, $divval, $cdval));
            if (get_count($chk) == 0) {
                $data = array();
                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SEMESTER"][TEXT]     = $setsems;
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = $divval;
                $data["CODE"][TEXT]         = $cdval;
                $data["REMARK1"][TEXT]      = $model->field["REMARK".$divval."_".$cdval];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
            } else {
                $data = array();
                $data["REMARK1"][TEXT]      = $model->field["REMARK".$divval."_".$cdval];
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

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return ;
    }

/*===================================一括更新画面===================================*/

    //所見取得
    function getHreportremarkDetailDatIkkatsu($model, $seme, $div, $code) {
        $query  = " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$seme."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND";
        $query .= "     DIV = '".$div."' AND ";
        $query .= "     CODE = '".$code."' ";

        return $query;
    }

    //生徒一覧
    function GetStudent($model) {
        $query  = " SELECT ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT OUTER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE    = '".$model->exp_grade."' ";
        $query .= "     AND T1.HR_CLASS = '".$model->hr_class."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //HRクラス取得
    function getHR_Class($model){
        $query  = " SELECT ";
        $query .= "     HR_CLASS ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHREGNO    = '".$model->schregno."' ";
        $query .= "     AND YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->ikkatsuSeme."' ";
        return $query;
    }

    //年組名取得
    function getHR_Name($model) {
        $query  = " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         GRADE    = '".$model->exp_grade."' ";
        $query .= "     AND HR_CLASS = '".$model->hr_class."' ";
        $query .= "     AND YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //学期名取得
    function getSemeName($semester) {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";

        return $query;
    }

    //レコードチェック
    function checkHrepExist($model, $seme, $schregno, $div, $code) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$seme."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";
        $query .= "     AND DIV      = '".$div."'";
        $query .= "     AND CODE     = '".$code."'";

        return $query;
    }

    //一括更新
    function IkkatsuUpdate($db, $model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $schNo = explode(",", $model->ikkatsu_data["selectdata"]);
        for ($cnt = 0; $cnt < get_count($schNo); $cnt++) {

            // HREPORTREMARK_DAT(所見)
            //存在チェック
            $setsems = $model->ikkatsuSeme;
            $chk = $db->getCol(knjd139sQuery::getHreportremarkDat($model, $setsems, $schNo[$cnt]));
            if (get_count($chk) == 0) {
                $data = array();
                $data["YEAR"][TEXT]               = $model->exp_year;
                $data["SEMESTER"][TEXT]           = $setsems;
                $data["SCHREGNO"][TEXT]           = $schNo[$cnt];
                $data["REGISTERCD"][TEXT]         = STAFFCD;
                $data["UPDATED"][FUNC]            = "sysdate()";

                $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
                $db->query($query);
            }

            $seme = "9";
            $divval = "01";
            for ($idxCnt = 1;$idxCnt <= 3;$idxCnt++) {
                //チェックが付いていないなら、skip
                if ($model->ikkatsu_data["check1"][$idxCnt] != "1") continue;
                $cdval = sprintf("%02d", $idxCnt);
                //レコードチェック（無ければインサート）
                $checkExist = $db->getOne(knjd139sQuery::checkHrepExist($model, $seme, $schNo[$cnt], $divval, $cdval));
                $data = array();
                $data["REMARK1"][TEXT] = $model->field["REMARK".$divval."_".$cdval];
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC]    = "sysdate()";
                if ($checkExist == 0) {
                    $data["YEAR"][TEXT]         = CTRL_YEAR;
                    $data["SEMESTER"][TEXT]     = $seme;
                    $data["SCHREGNO"][TEXT]     = $schNo[$cnt];
                    $data["DIV"][TEXT]          = $divval;
                    $data["CODE"][TEXT]         = $cdval;
                    $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
                } else {
                    $where  = " WHERE ";
                    $where .= "         YEAR         = '".CTRL_YEAR."' ";
                    $where .= "     AND SEMESTER     = '".$seme."' ";
                    $where .= "     AND SCHREGNO     = '".$schNo[$cnt]."' ";
                    $where .= "     AND DIV          = '".$divval."' ";
                    $where .= "     AND CODE         = '".$cdval."' ";
                    $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
                }
                $db->query($query);
            }
            $divval = "02";
            for ($idxCnt = 2;$idxCnt <= 3;$idxCnt++) {
                $cdval = sprintf("%02d", $idxCnt);
                //チェックが付いていない場合は更新しない。
                if ($idxCnt == 2 && $model->ikkatsu_data["check1"][4] != "1") continue;
                if ($idxCnt == 3 && $model->ikkatsu_data["check2"][1] != "1") continue;
                $seme = ($idxCnt == 2 ? "9" : $model->ikkatsuSeme);
                //レコードチェック（無ければインサート）
                $checkExist = $db->getOne(knjd139sQuery::checkHrepExist($model, $seme, $schNo[$cnt], $divval, $cdval));
                $data = array();
                $data["REMARK1"][TEXT] = $model->field["REMARK".$divval."_".$cdval];
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC]    = "sysdate()";
                if ($checkExist == 0) {
                    $data["YEAR"][TEXT]         = CTRL_YEAR;
                    $data["SEMESTER"][TEXT]     = $seme;
                    $data["SCHREGNO"][TEXT]     = $schNo[$cnt];
                    $data["DIV"][TEXT]          = $divval;
                    $data["CODE"][TEXT]         = $cdval;
                    $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
                } else {
                    $where  = " WHERE ";
                    $where .= "         YEAR         = '".CTRL_YEAR."' ";
                    $where .= "     AND SEMESTER     = '".$seme."' ";
                    $where .= "     AND SCHREGNO     = '".$schNo[$cnt]."' ";
                    $where .= "     AND DIV          = '".$divval."' ";
                    $where .= "     AND CODE         = '".$cdval."' ";
                    $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
                }
                $db->query($query);
            }
        }
        $db->commit();
        Query::dbCheckIn($db);
    }


}
?>
