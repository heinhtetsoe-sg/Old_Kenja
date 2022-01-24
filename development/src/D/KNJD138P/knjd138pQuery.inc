<?php

require_once('for_php7.php');

class knjd138pQuery extends Query {

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
    function getTrainRow($model, $grade_cd)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        if ((int)$grade_cd >= 3) {
            $query .= "     ,T2.REMARK1 AS COMMITTEE ";
            $query .= "     ,T2.REMARK3 AS CLUB ";
        }
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT T1 ";
        if ((int)$grade_cd >= 3) {
            $query .= "     LEFT JOIN HREPORTREMARK_DAT T2 ";
            $query .= "       ON T2.YEAR = T1.YEAR ";
            $query .= "      AND T2.SEMESTER = '9' ";
            $query .= "      AND T2.SCHREGNO = T1.SCHREGNO ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".$model->exp_year."' AND ";
        $query .= "     T1.SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //3年生以上の場合かつ登録時に委員会/部活動の登録件数を確認するための処理
    function getTrainRowSubOnly($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".$model->exp_year."' AND ";
        $query .= "     T1.SEMESTER    = '9' AND ";
        $query .= "     T1.SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $grade_cd = $db->getOne(knjd138pQuery::getGradeCd($model));

        //存在チェック
        $check1 = $db->getCol(knjd138pQuery::getTrainRow($model, $grade_cd));

        $study = ((int)$grade_cd >= 3) ? 1 : "";

        $data = array();
        if(get_count($check1) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            if ($study == 1) $data["TOTALSTUDYTIME"][TEXT] = $model->field["TOTALSTUDYTIME"];
            $data["SPECIALACTREMARK"][TEXT]     = $model->field["SPECIALACTREMARK"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REMARK2"][TEXT]              = $model->field["REMARK2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            if ($study == 1) $data["TOTALSTUDYTIME"][TEXT] = $model->field["TOTALSTUDYTIME"];
            $data["SPECIALACTREMARK"][TEXT]     = $model->field["SPECIALACTREMARK"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REMARK2"][TEXT]              = $model->field["REMARK2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        if ((int)$grade_cd >= 3) {
            $check2 = $db->getCol(knjd138pQuery::getTrainRowSubOnly($model));
            $data = array();
            if(get_count($check2) == 0) {
                $data["YEAR"][TEXT]           = $model->exp_year;
                $data["SEMESTER"][TEXT]       = '9';
                $data["SCHREGNO"][TEXT]       = $model->schregno;
                $data["REMARK1"][TEXT]        = $model->field["COMMITTEE"];
                $data["REMARK3"][TEXT]        = $model->field["CLUB"];
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "sysdate()";

                $query .= Query::insertSQL($data, "HREPORTREMARK_DAT");
                $db->query($query);
            } else {
                $data["REMARK1"][TEXT]        = $model->field["COMMITTEE"];
                $data["REMARK3"][TEXT]        = $model->field["CLUB"];
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "sysdate()";

                $where  = " WHERE ";
                $where .= "     YEAR = '".$model->exp_year."' ";
                $where .= "     AND SEMESTER = '9' ";
                $where .= "     AND SCHREGNO = '".$model->schregno."' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }
}
?>
