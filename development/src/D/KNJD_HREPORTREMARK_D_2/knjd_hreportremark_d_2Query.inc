<?php

require_once('for_php7.php');

class knjd_hreportremark_d_2Query extends Query {

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

    //生徒情報
    function getSchInfo($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //項目取得
    function getNameMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";

        return $query;
    }

    //行動記録の取得
    function getHrepSpecial($model)
    {
        $query  = " SELECT ";
        $query .= "     CODE, ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND DIV = '01' ";
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        //BEHAVIOR_SEMES_DAT削除
        $query  = " DELETE FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND DIV = '01' ";

        $db->query($query);

        //BEHAVIOR_SEMES_DAT追加
        foreach ($model->itemArray as $key => $val) {

            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["DIV"][TEXT]          = "01";
            $data["CODE"][TEXT]         = sprintf("%02d", $key);
            $data["REMARK1"][TEXT]      = $model->field["REMARK".$key];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        }

        //BEHAVIOR_SEMES_DAT削除
        $query  = " DELETE FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND DIV = '02' ";
        $query .= "     AND CODE in ('01', '02')";
        $db->query($query);

        //BEHAVIOR_SEMES_DAT追加
        $data = array();
        $data["YEAR"][TEXT]         = $model->exp_year;
        $data["SEMESTER"][TEXT]     = "9";
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["DIV"][TEXT]          = "02";
        $data["CODE"][TEXT]         = "01";
        $data["REMARK1"][TEXT]      = $model->field["RECORD_VAL01"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
    
        $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));

        //BEHAVIOR_SEMES_DAT追加
        $data = array();
        $data["YEAR"][TEXT]         = $model->exp_year;
        $data["SEMESTER"][TEXT]     = "9";
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["DIV"][TEXT]          = "02";
        $data["CODE"][TEXT]         = "02";
        $data["REMARK1"][TEXT]      = $model->field["RECORD_VAL02"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        
        Query::dbCheckIn($db);
        return ;
        
    }
    
    //行動の状況および部活動を取得
    function getActivities($model)
    {
        $query  = " SELECT ";
        $query .= "     CODE, ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND DIV = '02' ";
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        return $query;
    }
    
    //いったん保留
    /*
    //活動の状況・様子を取得
    function getActivities($model)
    {
        $query  = " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND DIV = '02' ";
        $query .= "     AND CODE = '01' ";

        return $query;
    }

    //部活動・その他を取得
    function getClubActivities($model)
    {
        $query  = " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND DIV = '02' ";
        $query .= "     AND CODE = '02' ";
        
        return $query;
    }
    */
    
    
}
?>
