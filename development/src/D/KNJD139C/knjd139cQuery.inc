<?php

require_once('for_php7.php');

class knjd139cQuery extends Query {
    //校種取得
    function getSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '".$grade."' ";

        return $query;
    }

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ取得(HREPORTREMARK_DAT)
    function getHreportremarkDat($model, $schregno, $flg="") {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->semester."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //データ取得(HREPORTREMARK_DETAIL_DAT)
    function getHreportremarkDetailDat($model, $schregno, $flg="") {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->semester."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     DIV         = '01' AND ";
        $query .= "     CODE        = '01' ";


        return $query;
    }


    //更新
    function &getUpdateQuery($model, $fields, $schregno) {
        $db = Query::dbCheckOut();

        //道徳更新
        //存在チェック
        $dataCnt = $db->getOne(knjd139cQuery::getHreportremarkDetailDat($model, $schregno, "cnt"));

        $data = array();
        $data["REMARK1"][TEXT] = $fields["MORAL"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($dataCnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     SEMESTER    = '".$model->semester."' AND ";
            $where .= "     SCHREGNO    = '".$schregno."' AND";
            $where .= "     DIV         = '01' AND ";
            $where .= "     CODE        = '01' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
        } else {
            $data["YEAR"][TEXT]        = CTRL_YEAR;
            $data["SEMESTER"][TEXT]    = $model->semester;
            $data["SCHREGNO"][TEXT]    = $schregno;
            $data["DIV"][TEXT]         = '01';
            $data["CODE"][TEXT]        = '01';

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        }

        //備考更新
        $dataCnt = $db->getOne(knjd139cQuery::getHreportremarkDat($model, $schregno, "cnt"));

        $data = array();
        $data["COMMUNICATION"][TEXT]    = $fields["COMMUNICATION"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($dataCnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     SEMESTER    = '".$model->semester."' AND ";
            $where .= "     SCHREGNO    = '".$schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        } else {
            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = $model->semester;
            $data["SCHREGNO"][TEXT]            = $schregno;

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
