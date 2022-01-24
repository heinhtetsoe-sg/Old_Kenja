<?php

require_once('for_php7.php');

class knjd137eQuery extends Query {

    //年次取得
    function getGradeCd($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= " AND GRADE = '".$model->grade."' ";

        return $query;
    }

    //観点マスタ
    function getBehaviorSemesMst($model, $gradeCd)
    {
        $query  = " SELECT ";
        $query .= "     CODE AS VALUE, ";
        $query .= "     VIEWNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= " AND GRADE = '{$gradeCd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒の記録
    function getBehavior($model)
    {
        $query  = " SELECT ";
        $query .= "     CODE, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->exp_semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        return $query;
    }

    //所見取得
    function getHreportremarkDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->exp_semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd137eQuery::getHreportremarkDat($model));

        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->exp_semester;
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["SPECIALACTREMARK"][TEXT]     = $model->field["SPECIALACTREMARK"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["SPECIALACTREMARK"][TEXT]     = $model->field["SPECIALACTREMARK"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '{$model->exp_semester}' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }

    //INSERT
    function &getInsertQuery2($model)
    {
        $db = Query::dbCheckOut();

        //行動の記録・特別活動の記録（削除）
        $query  = " DELETE FROM ";
        $query .= "     BEHAVIOR_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '".$model->exp_semester."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";

        $db->query($query);

        for ($i = 1; $i < 11; $i++) {
            $ival = sprintf("%02d", $i);
            if ($model->record["RECORD"][$ival] != "1") continue;

            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = $model->exp_semester;
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["CODE"][TEXT]         = $ival;
            $data["RECORD"][TEXT]       = "1";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "BEHAVIOR_SEMES_DAT");
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return ;
    }

}
?>
