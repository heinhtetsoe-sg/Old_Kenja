<?php

require_once('for_php7.php');

class knjd137pQuery extends Query {

    //学年コード取得
    function getGradeCd($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->exp_year."' AND ";
        $query .= "     GRADE   = '".$model->grade."' AND ";
        $query .= "     SCHOOL_KIND = 'P' ";

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
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd137pQuery::getTrainRow($model));

        $grade_cd = $db->getOne(knjd137pQuery::getGradeCd($model));
        $study = ($grade_cd == '03' || $grade_cd == '04' || $grade_cd == '05' || $grade_cd == '06') ? 1 : "";
        $foreign = ($grade_cd == '05' || $grade_cd == '06') ? 1 : "";

        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            if($study == 1)   $data["TOTALSTUDYTIME"][TEXT] = $model->field["TOTALSTUDYTIME"];
            if($foreign == 1) $data["FOREIGNLANGACT"][TEXT] = $model->field["FOREIGNLANGACT"];
            $data["SPECIALACTREMARK"][TEXT]     = $model->field["SPECIALACTREMARK"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            if($study == 1)   $data["TOTALSTUDYTIME"][TEXT] = $model->field["TOTALSTUDYTIME"];
            if($foreign == 1) $data["FOREIGNLANGACT"][TEXT] = $model->field["FOREIGNLANGACT"];
            $data["SPECIALACTREMARK"][TEXT]     = $model->field["SPECIALACTREMARK"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
