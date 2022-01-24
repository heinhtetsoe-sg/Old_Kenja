<?php

require_once('for_php7.php');

class knjd139pQuery extends Query {

    //学年コード取得
    function getGradeCd($model)
    {
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
    function getSemester($model)
    {
        $setYear = $model->exp_year ? $model->exp_year : CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$setYear."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //委員会活動参照
    function getCommittee($model, $frg)
    {
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
        $query .= " LEFT OUTER JOIN  ";
        $query .= "     COMMITTEE_MST T2 ON T1.COMMITTEE_FLG    = T2.COMMITTEE_FLG ";
        $query .= "                     AND T1.COMMITTEECD      = T2.COMMITTEECD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     NAME_MST T3  ON T3.NAMECD1      = 'J002' ";
        $query .= "                 AND T1.EXECUTIVECD  = T3.NAMECD2 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     NAME_MST T4  ON T4.NAMECD1      = 'J004' ";
        $query .= "                 AND T1.SEMESTER     = T4.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '" .$model->exp_year."' ";
        $query .= " AND T1.SCHREGNO = '" .$model->schregno ."' ";
        if ($frg == 1) {
            $query .= " AND T1.COMMITTEE_FLG = '1' ";
        } else {
            $query .= " AND T1.COMMITTEE_FLG = '2' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($model, $flg="")
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        if ($flg) {
            $query .= "     AND SEMESTER = '9' ";
        } else {
            $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        /**********/
        /* 学  期 */
        /**********/
        //存在チェック
        $check = $db->getCol(knjd139pQuery::getTrainRow($model));

        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
            $data["REMARK2"][TEXT]          = $model->field["REMARK2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
            $data["REMARK2"][TEXT]          = $model->field["REMARK2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        /**********/
        /* 通  年 */
        /**********/
        //存在チェック
        $check2 = $db->getCol(knjd139pQuery::getTrainRow($model, "1"));

        if(get_count($check2) == 0) {
            $data2["YEAR"][TEXT]             = $model->exp_year;
            $data2["SEMESTER"][TEXT]         = '9';
            $data2["SCHREGNO"][TEXT]         = $model->schregno;
            $data2["TOTALSTUDYTIME"][TEXT]   = $model->Field["TOTALSTUDYTIME"];
            $data2["REMARK1"][TEXT]          = $model->Field["REMARK1"];
            $data2["REMARK3"][TEXT]          = $model->Field["REMARK3"];
            $data2["REGISTERCD"][TEXT]       = STAFFCD;
            $data2["UPDATED"][FUNC]          = "sysdate()";

            $db->query(Query::insertSQL($data2, "HREPORTREMARK_DAT"));
        } else {
            $data2["TOTALSTUDYTIME"][TEXT]   = $model->Field["TOTALSTUDYTIME"];
            $data2["REMARK1"][TEXT]          = $model->Field["REMARK1"];
            $data2["REMARK3"][TEXT]          = $model->Field["REMARK3"];
            $data2["REGISTERCD"][TEXT]       = STAFFCD;
            $data2["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '9' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data2, "HREPORTREMARK_DAT", $where));
        }
        
        Query::dbCheckIn($db);
        return ;
    }

}
?>
