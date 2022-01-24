<?php

require_once('for_php7.php');

class knjd132mQuery extends Query {

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
    
    // HREPORTREMARKDETAIL_DATをCODEの値をもとに取得
    function getHreportremarkDetailDatByCode($model, $semes, $code) {
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

    // HREPORTREMARKDETAIL_DAT取得
    function getHreportremarkDetailDat($model, $semes) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".$model->exp_year."' AND ";
        $query .= "     T1.SEMESTER    = '".$semes."' AND ";
        $query .= "     T1.SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     T1.DIV = '01' AND ";
        $query .= "     T1.CODE = '01' ";

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // HREPORTREMARK_DAT(所見)
        //存在チェック
        $setsems = $model->field["SEMESTER"];
        $chk = $db->getCol(knjd132mQuery::getHreportremarkDat($model, $setsems));
        if (get_count($chk) == 0) {
            $data = array();
            $data["YEAR"][TEXT]               = $model->exp_year;
            $data["SEMESTER"][TEXT]           = $setsems;
            $data["SCHREGNO"][TEXT]           = $model->schregno;
            $data["COMMUNICATION"][TEXT]      = $model->field["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";

            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
        } else {
            $data = array();
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
        $divval = "01";
        
        //CODE列が01～05のREMARK1を更新
        for ($count = 1; $count <= 5; $count++) {
            $cdval = sprintf("%02d", $count);
            $chk = $db->getCol(knjd132mQuery::getHreportremarkDetailDatByCode($model, $setsems, $cdval));
            if (get_count($chk) == 0) {
                $data = array();
                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SEMESTER"][TEXT]     = $setsems;
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = $divval;
                $data["CODE"][TEXT]         = $cdval;
                $data["REMARK1"][TEXT]      = $model->field["REMARK".$count];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
            } else {
                $data = array();
                $data["REMARK1"][TEXT]      = $model->field["REMARK".$count];
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
    function getHreportremarkDatIkkatsu($model, $code) {
        $query  = " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->ikkatsuSeme."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND";
        $query .= "     DIV = '01' AND ";
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
    function checkHrepExist($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->ikkatsuSeme."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";
        $query .= "     AND DIV      = '01'";
        $query .= "     AND CODE     = '04'";

        return $query;
    }

    //一括更新
    function IkkatsuUpdate($db, $model) {
        $db->autoCommit(false);
        $schNo = explode(",", $model->ikkatsu_data["selectdata"]);
        for ($i = 0; $i < get_count($schNo); $i++) {
            //レコードチェック（無ければインサート）
            $checkExist = $db->getOne(knjd132mQuery::checkHrepExist($model, $schNo[$i]));
            foreach ($model->ikkatsu_data["hreport"] as $key => $val) {
                $data[$key][TEXT] = $val;
            }
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "sysdate()";
            if ($checkExist == 0) {
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = $model->ikkatsuSeme;
                $data["SCHREGNO"][TEXT]     = $schNo[$i];
                $data["DIV"][TEXT]          = "01";
                $data["CODE"][TEXT]         = "04";
                $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
            } else {
                $where  = " WHERE ";
                $where .= "         YEAR         = '".CTRL_YEAR."' ";
                $where .= "     AND SEMESTER     = '".$model->ikkatsuSeme."' ";
                $where .= "     AND SCHREGNO     = '".$schNo[$i]."' ";
                $where .= "     AND DIV          = '01' ";
                $where .= "     AND CODE         = '04' ";
                $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
            }
        }
        $db->commit();
    }


}
?>
