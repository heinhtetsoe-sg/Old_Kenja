<?php

require_once('for_php7.php');

class knjd139rQuery extends Query {
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

    //データ取得
    function getHreportremarkDetailDat($model, $div, $code, $flg="") {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR      = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER  = '9' AND ";
        $query .= "     SCHREGNO  = '".$model->schregno."' AND ";
        $query .= "     DIV       = '".$div."' AND ";
        $query .= "     CODE      = '".$code."' ";

        return $query;
    }

    //更新
    function &getUpdateQuery($model, $fields, $schregno) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //所見・出欠状況備考
        //存在チェック
        $dataCnt = $db->getOne(knjd139rQuery::getHreportremarkDat($model, $schregno, "cnt"));

        $data = array();
        if ($model->school_kind == "J") {
            $data["COMMUNICATION"][TEXT]    = $fields["COMMUNICATION"];
        }
        $data["ATTENDREC_REMARK"][TEXT] = $fields["ATTENDRECREMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($dataCnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     SEMESTER    = '".$model->semester."' AND ";
            $where .= "     SCHREGNO    = '".$schregno."' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
        } else {
            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = $model->semester;
            $data["SCHREGNO"][TEXT]            = $schregno;

            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
        }
        $db->query($query);

        //探求テーマ
        $data = array();
        $div = "02";
        $code = "01";
        $dataCnt = $db->getOne(knjd139rQuery::getHreportremarkDetailDat($model, $div, $code, "cnt"));
        $data["REMARK1"][TEXT]    = $fields["TANKYU_THEME01"];
        $data["REMARK2"][TEXT]    = $fields["TANKYU_THEME02"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        if ($dataCnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     SEMESTER    = '9' AND ";
            $where .= "     SCHREGNO    = '".$schregno."' AND ";
            $where .= "     DIV         = '".$div."' AND ";
            $where .= "     CODE        = '".$code."' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
        } else {
            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = "9";
            $data["SCHREGNO"][TEXT]            = $schregno;
            $data["DIV"][TEXT]                 = $div;
            $data["CODE"][TEXT]                = $code;

            $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
        }
        $db->query($query);

        //生徒会活動
        $data = array();
        $div = "01";
        $code = "02";
        $dataCnt = $db->getOne(knjd139rQuery::getHreportremarkDetailDat($model, $div, $code, "cnt"));
        $data["REMARK1"][TEXT]    = $fields["COMMITTEE01"];
        $data["REMARK2"][TEXT]    = $fields["COMMITTEE02"];
        $data["REMARK3"][TEXT]    = $fields["COMMITTEE03"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        if ($dataCnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     SEMESTER    = '9' AND ";
            $where .= "     SCHREGNO    = '".$schregno."' AND ";
            $where .= "     DIV         = '".$div."' AND ";
            $where .= "     CODE        = '".$code."' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
        } else {
            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = "9";
            $data["SCHREGNO"][TEXT]            = $schregno;
            $data["DIV"][TEXT]                 = $div;
            $data["CODE"][TEXT]                = $code;

            $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
        }
        $db->query($query);

        //学級の活動
        $data = array();
        $div = "01";
        $code = "01";
        $dataCnt = $db->getOne(knjd139rQuery::getHreportremarkDetailDat($model, $div, $code, "cnt"));
        $data["REMARK1"][TEXT]    = $fields["CLASSACT01"];
        $data["REMARK2"][TEXT]    = $fields["CLASSACT02"];
        $data["REMARK3"][TEXT]    = $fields["CLASSACT03"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        if ($dataCnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     SEMESTER    = '9' AND ";
            $where .= "     SCHREGNO    = '".$schregno."' AND ";
            $where .= "     DIV         = '".$div."' AND ";
            $where .= "     CODE        = '".$code."' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
        } else {
            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = "9";
            $data["SCHREGNO"][TEXT]            = $schregno;
            $data["DIV"][TEXT]                 = $div;
            $data["CODE"][TEXT]                = $code;

            $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
        }
        $db->query($query);

        //部活動
        $data = array();
        $div = "01";
        $code = "03";
        $dataCnt = $db->getOne(knjd139rQuery::getHreportremarkDetailDat($model, $div, $code, "cnt"));
        $data["REMARK1"][TEXT]    = $fields["CLUBACT01"];
        $data["REMARK2"][TEXT]    = $fields["CLUBACT02"];
        $data["REMARK3"][TEXT]    = $fields["CLUBACT03"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        if ($dataCnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     SEMESTER    = '9' AND ";
            $where .= "     SCHREGNO    = '".$schregno."' AND ";
            $where .= "     DIV         = '".$div."' AND ";
            $where .= "     CODE        = '".$code."' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
        } else {
            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = "9";
            $data["SCHREGNO"][TEXT]            = $schregno;
            $data["DIV"][TEXT]                 = $div;
            $data["CODE"][TEXT]                = $code;

            $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
        }
        $db->query($query);

        //その他(1-3)
        $data = array();
        $div = "01";
        $code = "05";
        $dataCnt = $db->getOne(knjd139rQuery::getHreportremarkDetailDat($model, $div, $code, "cnt"));
        $data["REMARK1"][TEXT]    = $fields["OTHER01"];
        $data["REMARK2"][TEXT]    = $fields["OTHER02"];
        $data["REMARK3"][TEXT]    = $fields["OTHER03"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        if ($dataCnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     SEMESTER    = '9' AND ";
            $where .= "     SCHREGNO    = '".$schregno."' AND ";
            $where .= "     DIV         = '".$div."' AND ";
            $where .= "     CODE        = '".$code."' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
        } else {
            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = "9";
            $data["SCHREGNO"][TEXT]            = $schregno;
            $data["DIV"][TEXT]                 = $div;
            $data["CODE"][TEXT]                = $code;

            $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
        }
         $db->query($query);

        if ($model->school_kind == "H") {
            //その他(4-6)
            $data = array();
            $div = "01";
            $code = "07";
            $dataCnt = $db->getOne(knjd139rQuery::getHreportremarkDetailDat($model, $div, $code, "cnt"));
            $data["REMARK1"][TEXT]    = $fields["OTHER04"];
            $data["REMARK2"][TEXT]    = $fields["OTHER05"];
            $data["REMARK3"][TEXT]    = $fields["OTHER06"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            if ($dataCnt > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                $where .= "     SEMESTER    = '9' AND ";
                $where .= "     SCHREGNO    = '".$schregno."' AND ";
                $where .= "     DIV         = '".$div."' AND ";
                $where .= "     CODE        = '".$code."' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
            } else {
                $data["YEAR"][TEXT]                = CTRL_YEAR;
                $data["SEMESTER"][TEXT]            = "9";
                $data["SCHREGNO"][TEXT]            = $schregno;
                $data["DIV"][TEXT]                 = $div;
                $data["CODE"][TEXT]                = $code;

                $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
            }
             $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }
}
?>
