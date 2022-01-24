<?php

require_once('for_php7.php');

class knjd139jQuery extends Query {

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getHreportremarkDat($schregno, $semester) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //部活動参照
    function getClub($model) {

        $sdate = CTRL_YEAR.'-04-01';
        $edate = (CTRL_YEAR + 1).'-03-31';

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     T2.CLUBNAME, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     T3.NAME1 AS EXECUTIVENAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= "     LEFT JOIN CLUB_MST T2 ON T2.CLUBCD = T1.CLUBCD ";
        $query .= "     LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'J001' AND T1.EXECUTIVECD = T3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
        $query .= "   ((T1.SDATE                      <= '{$sdate}' AND VALUE(T1.EDATE, '9999-12-31') >= '{$edate}') OR ";
        $query .= "    (T1.SDATE                      >= '{$sdate}' AND T1.SDATE                      <= '{$edate}') OR ";
        $query .= "    (VALUE(T1.EDATE, '9999-12-31') >= '{$sdate}' AND VALUE(T1.EDATE, '9999-12-31') <= '{$edate}')) ";
        $query .= " ORDER BY ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.CLUBCD ";

        return $query;
    }

    //委員会活動参照
    function getCommittee($model) {

        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T4.NAME1 AS SEMESTERNAME, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T2.COMMITTEENAME, ";
        $query .= "     T1.CHARGENAME, ";
        $query .= "     T3.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "     LEFT JOIN COMMITTEE_MST T2 ON T1.COMMITTEE_FLG = T2.COMMITTEE_FLG AND T1.COMMITTEECD = T2.COMMITTEECD ";
        $query .= "     LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'J002' AND T1.EXECUTIVECD = T3.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'J004' AND T1.SEMESTER = T4.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    /********/
    /* 更新 */
    /********/
    function &getInsertUpdateQuery($model, $schregno, $semester) {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd139jQuery::getHreportremarkDat($schregno, $semester));

        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["SEMESTER"][TEXT]             = $semester;
            $data["SCHREGNO"][TEXT]             = $schregno;

            if($semester == "9") {
                $data["REMARK1"][TEXT]          = $model->field["REMARK1"];
                $data["REMARK2"][TEXT]          = $model->field["REMARK2"];
            } else {
                $data["SPECIALACTREMARK"][TEXT] = $model->field["SPECIALACTREMARK"];
                $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
                $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
            }

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            if($semester == "9") {
                $data["REMARK1"][TEXT]          = $model->field["REMARK1"];
                $data["REMARK2"][TEXT]          = $model->field["REMARK2"];
            } else {
                $data["SPECIALACTREMARK"][TEXT] = $model->field["SPECIALACTREMARK"];
                $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
                $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
            }

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".CTRL_YEAR."' AND ";
            $where .= "     SEMESTER = '".$semester."' AND ";
            $where .= "     SCHREGNO = '".$schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
