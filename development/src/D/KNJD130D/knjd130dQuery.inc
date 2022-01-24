<?php

require_once('for_php7.php');

class knjd130dQuery extends Query {
    //１レコード取得
    function getTrainRow($schregno) {
        $db = Query::dbCheckOut();

        $query  = " SELECT * ";
        $query .= "   FROM HREPORTREMARK_DAT ";
        $query .= "  WHERE YEAR     = '".CTRL_YEAR."'";
        $query .= "    AND SEMESTER = '9' ";
        $query .= "    AND SCHREGNO = '".$schregno."'";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //部活動参照
    function getClub($model) {
        $sdate = CTRL_YEAR . '-04-01';
        $edate = (CTRL_YEAR + 1) . '-03-31';

        $query  = " SELECT ";
        $query .= "     L2.CLUBNAME, ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L3.NAME1 AS EXECUTIVE_NAME, ";
        $query .= "     L1.DETAIL_DATE, ";
        $query .= "     L1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT L1 ON L1.DETAIL_DATE BETWEEN (CASE WHEN T1.SDATE < '{$sdate}' THEN '{$sdate}' ELSE T1.SDATE END) AND  ";
        $query .= "                                                          (CASE WHEN (T1.EDATE > '{$edate}') OR (T1.EDATE IS NULL) THEN '{$edate}' ELSE T1.EDATE END) ";
        $query .= "                                AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                AND L1.CLUBCD = T1.CLUBCD ";
        $query .= " LEFT JOIN ";
        $query .= "     CLUB_MST L2 ON L2.CLUBCD = T1.CLUBCD ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L3 ON  L3.NAMECD1 = 'J001' ";
        $query .= "                 AND L3.NAMECD2 = T1.EXECUTIVECD ";
        $query .= " WHERE ";
        $query .= "     T1.SDATE <= '{$edate}' AND ";
        $query .= "     VALUE(T1.EDATE,'{$edate}') >= '{$sdate}' AND ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //委員会活動参照
    function getCommittee($model) {
        $query  = " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     L2.COMMITTEENAME, ";
        $query .= "     T1.CHARGENAME, ";
        $query .= "     L3.NAME1 AS EXECUTIVE_NAME, ";
        $query .= "     L1.DETAIL_DATE, ";
        $query .= "     L1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_COMMITTEE_HDETAIL_DAT L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                                     AND L1.SEQ = T1.SEQ ";
        $query .= "                                     AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     COMMITTEE_MST L2 ON  L2.COMMITTEE_FLG = T1.COMMITTEE_FLG ";
        $query .= "                      AND L2.COMMITTEECD   = T1.COMMITTEECD ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L3 ON  L3.NAMECD1 = 'J002' ";
        $query .= "                 AND L3.NAMECD2 = T1.EXECUTIVECD ";
        $query .= " WHERE ";
        $query .= "         T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

/******************************************************************************************/
/******************************************************************************************/
/*******                    ***************************************************************/
/******* 一括更新の生徒一覧 ***************************************************************/
/*******                    ***************************************************************/
/******************************************************************************************/
/******************************************************************************************/
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO || '番　' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->grade_hrclass}' ";
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO ";

        return $query;
    }

    function getSpecialactremark($schregno) {
        $query .= " SELECT ";
        $query .= "     SPECIALACTREMARK ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '9' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    function getTotalstudytime($schregno) {
        $query .= " SELECT ";
        $query .= "     TOTALSTUDYTIME ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '9' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

/******************************************************************************************/
/******************************************************************************************/
/*******              *********************************************************************/
/******* 更新時に使う *********************************************************************/
/*******              *********************************************************************/
/******************************************************************************************/
/******************************************************************************************/
    //INSERT
    function &getInsertQuery($fields,$schregno) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = '9';
        $data["SCHREGNO"][TEXT]         = $schregno;
        $data["SPECIALACTREMARK"][TEXT] = $fields["SPECIALACTREMARK"];
        $data["TOTALSTUDYTIME"][TEXT]   = $fields["TOTALSTUDYTIME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));

        Query::dbCheckIn($db);
        return ;
    }

    //DELETE
    function &getDeleteQuery($schregno) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM HREPORTREMARK_DAT ";
        $query .= " WHERE YEAR     = '".CTRL_YEAR."'";
        $query .= "   AND SEMESTER = '9'";
        $query .= "   AND SCHREGNO = '".$schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
    }

    //INSERT(一括更新)
    function &getInsertQuery2($field, $selectdata) {
        $db = Query::dbCheckOut();

        $schregArray = explode(",", $selectdata);
        foreach ($schregArray as $schregno) {
            $query = knjd130dQuery::checkExists($schregno);
            $cnt = $db->getOne($query);

            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = 9;
            $data["SCHREGNO"][TEXT]            = $schregno;
            if ($field["RCHECK1"] == "on") {
                $data["SPECIALACTREMARK"][TEXT]   = $field["SPECIALACTREMARK"];
            }
            if ($field["RCHECK2"] == "on") {
                $data["TOTALSTUDYTIME"][TEXT]  = $field["TOTALSTUDYTIME"];
            }
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][NUMBER]           = "sysdate()";

            if ($cnt > 0) {
                $where  = " WHERE YEAR     = '".CTRL_YEAR."' AND ";
                $where .= "       SEMESTER = '9' AND ";
                $where .= "       SCHREGNO = '{$schregno}' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DAT",$where);
            } else {
                $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            }
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return $result;
    }

    //レコードがあるかのチェック(インサートにするかアップデートにするか)
    function checkExists($schregno) {
         $query .= " SELECT ";
         $query .= "     COUNT(*) ";
         $query .= " FROM ";
         $query .= "     HREPORTREMARK_DAT ";
         $query .= " WHERE ";
         $query .= "     YEAR = '".CTRL_YEAR."' AND ";
         $query .= "     SEMESTER = '9' AND ";
         $query .= "     SCHREGNO = '{$schregno}' ";

         return $query;
    }

}
?>
