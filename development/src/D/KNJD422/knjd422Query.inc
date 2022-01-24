<?php

require_once('for_php7.php');

class knjd422Query extends Query {

    //学期取得
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     CASE WHEN ABBV1 = '".CTRL_SEMESTER."' THEN 1 END AS DEF_VALUE_FLG ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND NAMECD1 = 'D078' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }


    //１レコード取得 -- HREPORTREMARK_DAT
    function getRow($model) {
        $query  = " SELECT ";
        $query .= "     L1.TOTALSTUDYTIME, ";
        $query .= "     L1.SPECIALACTREMARK, ";
        $query .= "     L1.ATTENDREC_REMARK, ";
        $query .= "     L2.REMARK1 AS REMARK01, ";
        $query .= "     L3.REMARK1 AS REMARK02 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                   AND L1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "                                   AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                          AND L2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "                                          AND L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                          AND L2.DIV  = '05' ";
        $query .= "                                          AND L2.CODE = '01' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT L3 ON L3.YEAR = T1.YEAR ";
        $query .= "                                          AND L3.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "                                          AND L3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                          AND L3.DIV  = '05' ";
        $query .= "                                          AND L3.CODE = '02' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR  = '".$model->exp_year."' ";
        $query .= " AND T1.SEMESTER = '".$model->exp_semester."' ";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //部活動参照(Subform2)
    function getClub($model) {
        $sdate = $model->exp_year . '-04-01';
        $edate = ($model->exp_year + 1) . '-03-31';

        $query  = " SELECT ";
        $query .= "     T2.CLUBNAME, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     T3.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= "     LEFT JOIN CLUB_MST T2 ON T1.CLUBCD  = T2.CLUBCD ";
        $query .= "     LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'J001' ";
        $query .= "                          AND T3.NAMECD2 = T1.EXECUTIVECD";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     ((T1.SDATE                      <= '".$sdate."' AND VALUE(T1.EDATE, '9999-12-31') >= '".$edate."') OR ";
        $query .= "      (T1.SDATE                      >= '".$sdate."' AND T1.SDATE                      <= '".$edate."') OR ";
        $query .= "      (VALUE(T1.EDATE, '9999-12-31') >= '".$sdate."' AND VALUE(T1.EDATE, '9999-12-31') <= '".$edate."')) ";
        $query .= " ORDER BY ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.CLUBCD ";

        return $query;
    }

    //委員会活動参照(Subform3)
    function getCommittee($model)
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
        $query .= "     T1.YEAR     = '" .$model->exp_year."' AND ";
        $query .= "     T1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //データチェック
    function getHreportRemarkCount($model, $flg="")
    {
        //テーブル取得
        $table = "";
        if ($flg === '1') {
            $table = "HREPORTREMARK_DAT";
        } else {
            $table = "HREPORTREMARK_DETAIL_DAT";
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     $table ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->exp_year."' ";
        $query .= " AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " AND SCHREGNO = '".$model->schregno."' ";
        if ($flg === '2') {
            $query .= " AND DIV  = '05' ";
            $query .= " AND CODE = '01' ";
        } else if ($flg === '3') {
            $query .= " AND DIV  = '05' ";
            $query .= " AND CODE = '02' ";
        }

        return $query;
    }


    //UPDATE処理
    function &getUpdateQuery($fields, $model) {
        $db = Query::dbCheckOut();

        //データチェック
        $dataCheck1 = $db->getOne(knjd422Query::getHreportRemarkCount($model, "1"));
        //UPDATE -- HTRAINREMARK_DAT
        $data = array();
        $data["TOTALSTUDYTIME"][TEXT]       = $fields["TOTALSTUDYTIME"];
        $data["SPECIALACTREMARK"][TEXT]     = $fields["SPECIALACTREMARK"];
        $data["ATTENDREC_REMARK"][TEXT]     = $fields["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($dataCheck1 == 0) {
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            
            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' ";
            $where .= " AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $where .= " AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }
        
        //データチェック
        $dataCheck2 = $db->getOne(knjd422Query::getHreportRemarkCount($model, "2"));
        //UPDATE -- HREPORTREMARK_DETAIL_DAT
        $data = array();
        $data["REMARK1"][TEXT]              = $fields["REMARK01"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($dataCheck2 == 0) {
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["DIV"][TEXT]              = "05";
            $data["CODE"][TEXT]             = "01";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' ";
            $where .= " AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $where .= " AND SCHREGNO = '".$model->schregno."' ";
            $where .= " AND DIV  = '05' ";
            $where .= " AND CODE = '01' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
        }

        //データチェック
        $dataCheck3 = $db->getOne(knjd422Query::getHreportRemarkCount($model, "3"));
        //UPDATE -- HREPORTREMARK_DETAIL_DAT
        $data = array();
        $data["REMARK1"][TEXT]              = $fields["REMARK02"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($dataCheck3 == 0) {
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["DIV"][TEXT]              = "05";
            $data["CODE"][TEXT]             = "02";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' ";
            $where .= " AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $where .= " AND SCHREGNO = '".$model->schregno."' ";
            $where .= " AND DIV  = '05' ";
            $where .= " AND CODE = '02' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
        }

        Query::dbCheckIn($db);
        return;
    }
}
?>
