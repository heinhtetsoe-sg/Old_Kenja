<?php

require_once('for_php7.php');

class knjd138hQuery extends Query {

    //１レコード取得
    function getTrainRow($model, $schregno, $semester="")
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        if($semester) {
            $query .= "     AND SEMESTER = '".$semester."' ";
        } else {
            $query .= "     AND SEMESTER = '9' ";
        }
        $query .= "     AND SCHREGNO = '".$schregno."' ";

        return $query;
    }
    
    //追加部分　評価
    function getTrain2Row($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";
        $query .= "     AND DIV      = '03' ";
        $query .= "     AND CODE     = '01' ";

        return $query;
    }

    //追加部分　特別活動の記録
    function getTrain3Row($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     REMARK1 AS SPECIALACTREMARK ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";
        $query .= "     AND DIV      = '03' ";
        $query .= "     AND CODE     = '02' ";

        return $query;
    }

    //学期取得
    function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        /**********/
        /* 通  年 */
        /**********/
        //存在チェック
        $check = $db->getCol(knjd138hQuery::getTrainRow($model, $model->schregno));

        $data = array();
        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = "9";
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["TOTALSTUDYTIME"][TEXT]   = $model->field["TOTALSTUDYTIME"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["TOTALSTUDYTIME"][TEXT]   = $model->field["TOTALSTUDYTIME"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '9' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        /**********/
        /* 年  度 */
        /**********/
        //存在チェック
        $check = $db->getCol(knjd138hQuery::getTrainRow($model, $model->schregno, $model->field["SEMESTER"]));

        $data = array();
        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
            $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
            $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }
    
    //追加部分:評価
    function &getInsert2Query($model)
    {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd138hQuery::getTrain2Row($model, $model->schregno));

        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = "9";
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["DIV"][TEXT]          = "03";
            $data["CODE"][TEXT]         = "01";
            $data["REMARK1"][TEXT]      = $model->field["REMARK1"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $data["REMARK1"][TEXT]      = $model->field["REMARK1"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR         = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '9' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";
            $where .= "     AND DIV      = '03' ";
            $where .= "     AND CODE     = '01' ";
            $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }
    
    
    //追加部分:特別活動の記録   
    function &getInsert3Query($model)
    {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd138hQuery::getTrain3Row($model, $model->schregno));

        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = "9";
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["DIV"][TEXT]          = "03";
            $data["CODE"][TEXT]         = "02";
            $data["REMARK1"][TEXT]      = $model->field["SPECIALACTREMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $data["REMARK1"][TEXT]      = $model->field["SPECIALACTREMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR         = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '9' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";
            $where .= "     AND DIV      = '03' ";
            $where .= "     AND CODE     = '02' ";
            $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }

    //一括更新の生徒一覧
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     T1.ATTENDNO || ' ' || T1.SCHREGNO || ' ' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".$model->exp_year."' ";
        $query .= "     AND T1.SEMESTER = '".$model->exp_semester."' ";
        $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->grade_hrclass}' ";
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO ";

        return $query;
    }

   //INSERT
   function &getInsertQuery2($model, $selectdata) {
        $db = Query::dbCheckOut();

        $schregArray = explode(",", $selectdata);
        foreach ($schregArray as $schregno) {

            //存在チェック
            $check = $db->getCol(knjd138hQuery::getTrainRow($model, $schregno));

            if(get_count($check) == 0) {
                $data["YEAR"][TEXT]             = $model->exp_year;
                $data["SEMESTER"][TEXT]         = "9";
                $data["SCHREGNO"][TEXT]         = $schregno;
                if ($model->field["RCHECK1"] == "on") {
                    $data["TOTALSTUDYTIME"][TEXT]   = $model->field["TOTALSTUDYTIME"];
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
            } else {
                if ($model->field["RCHECK1"] == "on") {
                    $data["TOTALSTUDYTIME"][TEXT]   = $model->field["TOTALSTUDYTIME"];
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $where  = " WHERE ";
                $where .= "     YEAR            = '".$model->exp_year."' ";
                $where .= "     AND SEMESTER    = '9' ";
                $where .= "     AND SCHREGNO    = '".$schregno."' ";

                $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
            }
        }    
        Query::dbCheckIn($db);
        return $result;
    }
    
   //INSERT 評価追加部分
   function &getInsert2Query2($model, $selectdata) {
        $db = Query::dbCheckOut();

        $schregArray = explode(",", $selectdata);
        foreach ($schregArray as $schregno) {

            //存在チェック
            $check = $db->getCol(knjd138hQuery::getTrain2Row($model, $schregno));

            if(get_count($check) == 0) {
                $data["YEAR"][TEXT]             = $model->exp_year;
                $data["SEMESTER"][TEXT]         = "9";
                $data["SCHREGNO"][TEXT]         = $schregno;
                $data["DIV"][TEXT]              = "03";
                $data["CODE"][TEXT]             = "01";
                if ($model->field["RCHECK2"] == "on") {
                    $data["REMARK1"][TEXT]      = $model->field["REMARK1"];
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
            } else {
                if ($model->field["RCHECK2"] == "on") {
                    $data["REMARK1"][TEXT]      = $model->field["REMARK1"];
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $where  = " WHERE ";
                $where .= "     YEAR            = '".$model->exp_year."' ";
                $where .= "     AND SEMESTER    = '9' ";
                $where .= "     AND SCHREGNO    = '".$schregno."' ";
                $where .= "     AND DIV         = '03' ";
                $where .= "     AND CODE        = '01' ";
                
                $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
            }
        }

        Query::dbCheckIn($db);
        return $result;
    }
}
?>
