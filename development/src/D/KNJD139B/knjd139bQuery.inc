<?php

require_once('for_php7.php');

class knjd139bQuery extends Query {
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

    //データ取得
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

    //更新
    function &getUpdateQuery($model, $fields, $schregno) {
        $db = Query::dbCheckOut();

        //存在チェック
        $dataCnt = $db->getOne(knjd139bQuery::getHreportremarkDat($model, $schregno, "cnt"));

        $data = array();
        if ($model->school_kind == "J") {
            $data["SPECIALACTREMARK"][TEXT] = $fields["SPECIALACTREMARK"];
        }
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
