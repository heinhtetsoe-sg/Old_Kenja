<?php

require_once('for_php7.php');

class knjd132wQuery extends Query {
    
    //校種取得
    function getSchoolkind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }
    
    //出力可否テーブルの帳票パターン
    function getConditionDatRemark1($model) {
        $query .= " select ";
        $query .= "     REMARK1 ";
        $query .= " from ";
        $query .= "     HREPORT_CONDITION_DAT ";
        $query .= " where ";
        $query .= "     SEQ = '001' ";
        $query .= "     and YEAR='".CTRL_YEAR."' ";

        return $query;
    }

    //学期
    function getSemester() {
        $query  = " select ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= "   , SEMESTERNAME AS LABEL ";
        $query .= " from ";
        $query .= "     SEMESTER_MST ";
        $query .= " where ";
        $query .= "     YEAR='".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " order by";
        $query .= "     SEMESTER ";

        return $query;
    }
  
    //１レコード取得
    function getTrainRow($schregno, $model) {
        $query  = " SELECT ";
        $query .= "     HD1.REMARK1 as CLUB, ";
        $query .= "     HD2.REMARK1 as COMMITTEE, ";
        $query .= "     HREPORTREMARK_DAT.* ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " left join ";
        $query .= "     HREPORTREMARK_DETAIL_DAT HD1 ";
        $query .= "     on HREPORTREMARK_DAT.YEAR = HD1.YEAR ";
        $query .= "     and HREPORTREMARK_DAT.SEMESTER = HD1.SEMESTER ";
        $query .= "     and HREPORTREMARK_DAT.SCHREGNO = HD1.SCHREGNO ";
        $query .= "     and HD1.DIV = '01' ";
        $query .= "     and HD1.CODE = '01' ";
        $query .= " left join ";
        $query .= "     HREPORTREMARK_DETAIL_DAT HD2 ";
        $query .= "     on HREPORTREMARK_DAT.YEAR = HD2.YEAR ";
        $query .= "     and HREPORTREMARK_DAT.SEMESTER = HD2.SEMESTER ";
        $query .= "     and HREPORTREMARK_DAT.SCHREGNO = HD2.SCHREGNO ";
        $query .= "     and HD2.DIV = '01' ";
        $query .= "     and HD2.CODE = '02' ";
        $query .= " WHERE ";
        $query .= "     HREPORTREMARK_DAT.YEAR = '" . CTRL_YEAR . "' ";
        $query .= "     AND HREPORTREMARK_DAT.SEMESTER = '" . $model->field["SEMESTER"] . "' ";
        $query .= "     AND HREPORTREMARK_DAT.SCHREGNO = '" . $schregno . "'";
        
        return $query;
    }

    //部活動参照
    function getClub($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     L2.CLUBNAME, ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.DETAIL_DATE, ";
        $query .= "     L1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                AND L1.CLUBCD = T1.CLUBCD ";
        $query .= " LEFT JOIN ";
        $query .= "     CLUB_MST L2 ON L2.CLUBCD = T1.CLUBCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.DETAIL_DATE ";

        return $query;
    }

    //委員会活動参照
    function getCommittee($model) {
        $query  = " SELECT ";
        $query .= "     L1.SEQ, ";
        $query .= "     L2.COMMITTEENAME, ";
        $query .= "     T1.CHARGENAME, ";
        $query .= "     L1.DETAIL_DATE, ";
        $query .= "     L1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_COMMITTEE_HDETAIL_DAT L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                                     AND L1.SEQ  = T1.SEQ ";
        $query .= " LEFT JOIN ";
        $query .= "     COMMITTEE_MST L2 ON  L2.COMMITTEE_FLG = T1.COMMITTEE_FLG ";
        $query .= "                      AND L2.COMMITTEECD   = T1.COMMITTEECD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.COMMITTEECD, ";
        $query .= "     L1.DETAIL_DATE ";

        return $query;
    }
    
    function &isCheckDatQuery($model,$schregno){
        $db = Query::dbCheckOut();
        
        $query  = " Select ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO = '".$schregno."'";
        
        $row = $db->getRow($query);

        Query::dbCheckIn($db);
        return isset($row);
    }
    
    function &isCheckDetailDatQuery01($model,$schregno){
        $db = Query::dbCheckOut();
        
        $query  = " Select ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO = '".$schregno."'";
        $query .= "     AND DIV = '01'";
        $query .= "     AND CODE = '01'";
        
        $row = $db->getRow($query);

        Query::dbCheckIn($db);
        return isset($row);
    }
    
    function &isCheckDetailDatQuery02($model,$schregno){
        $db = Query::dbCheckOut();
        
        $query  = " Select ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO = '".$schregno."'";
        $query .= "     AND DIV = '01'";
        $query .= "     AND CODE = '02'";
        
        $row = $db->getRow($query);

        Query::dbCheckIn($db);
        return isset($row);
    }

    /********/
    /* 更新 */
    /********/
    //INSERT
    function &getInsertQuery($fields, $schregno) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SEMESTER"][TEXT]             = $fields["SEMESTER"];
        $data["SCHREGNO"][TEXT]             = $schregno;
        
        if (isset($fields['TOTALSTUDYTIME'])) {
            $data['TOTALSTUDYTIME'][TEXT]   = $fields['TOTALSTUDYTIME'];
        }
        if (isset($fields['REMARK1'])) {
            $data['REMARK1'][TEXT]          = $fields['REMARK1'];
        }
        if (isset($fields['COMMUNICATION'])) {
            $data['COMMUNICATION'][TEXT]    = $fields['COMMUNICATION'];
        }
        if (isset($fields['ATTENDREC_REMARK'])) {
            $data['ATTENDREC_REMARK'][TEXT] = $fields['ATTENDREC_REMARK'];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $db->query(Query::insertSQL($data, "hreportremark_dat"));

        Query::dbCheckIn($db);
        return ;
    }
    
    //INSERT
    function &getInsertDetailQuery01($fields, $schregno) {
        if(!isset($fields['CLUB'])){
            return;
        }
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]       = CTRL_YEAR;
        $data["SEMESTER"][TEXT]   = $fields["SEMESTER"];
        $data["SCHREGNO"][TEXT]   = $schregno;
        $data["DIV"][TEXT]        = '01';
        $data["CODE"][TEXT]       = '01';
        $data['REMARK1'][TEXT]    = $fields['CLUB'];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "sysdate()";

        $db->query(Query::insertSQL($data, "hreportremark_detail_dat"));
        
        Query::dbCheckIn($db);
        return ;
    }
    
    //INSERT
    function &getInsertDetailQuery02($fields, $schregno) {
        if(!isset($fields['COMMITTEE'])){
            return;
        }
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]       = CTRL_YEAR;
        $data["SEMESTER"][TEXT]   = $fields["SEMESTER"];
        $data["SCHREGNO"][TEXT]   = $schregno;
        $data["DIV"][TEXT]        = '01';
        $data["CODE"][TEXT]       = '02';
        $data['REMARK1'][TEXT]    = $fields['COMMITTEE'];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "sysdate()";

        $db->query(Query::insertSQL($data, "hreportremark_detail_dat"));
        
        Query::dbCheckIn($db);
        return ;
    }
    
    //UPDATE
    function &getUpdateQuery($fields, $schregno) {
        $db = Query::dbCheckOut();
        
        if (isset($fields['TOTALSTUDYTIME'])) {
            $data['TOTALSTUDYTIME'][TEXT]   = $fields['TOTALSTUDYTIME'];
        }
        if (isset($fields['REMARK1'])) {
            $data['REMARK1'][TEXT]          = $fields['REMARK1'];
        }
        if (isset($fields['COMMUNICATION'])) {
            $data['COMMUNICATION'][TEXT]    = $fields['COMMUNICATION'];
        }
        if (isset($fields['ATTENDREC_REMARK'])) {
            $data['ATTENDREC_REMARK'][TEXT] = $fields['ATTENDREC_REMARK'];
        }
        
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "sysdate()";

        $where  = " WHERE YEAR       = '".CTRL_YEAR."'";
        $where .= "     AND SEMESTER = '".$fields["SEMESTER"]."'";
        $where .= "     AND SCHREGNO = '".$schregno."'";
        
        $db->query(Query::updateSQL($data, "hreportremark_dat", $where));

        Query::dbCheckIn($db);
        return ;
    }
    
    //UPDATE
    function &getUpdateDetailQuery01($fields, $schregno) {
        if(!isset($fields['CLUB'])){
            return;
        }
        $db = Query::dbCheckOut();

        $data['REMARK1'][TEXT]    = $fields['CLUB'];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "sysdate()";

        $where  = " WHERE YEAR       = '".CTRL_YEAR."'";
        $where .= "     AND SEMESTER = '".$fields["SEMESTER"]."'";
        $where .= "     AND SCHREGNO = '".$schregno."'";
        $where .= "     AND DIV      = '01'";
        $where .= "     AND CODE     = '01'";
        
        $db->query(Query::updateSQL($data, "hreportremark_detail_dat", $where));

        Query::dbCheckIn($db);
        return ;
    }
    
    //UPDATE
    function &getUpdateDetailQuery02($fields, $schregno) {
        if(!isset($fields['COMMITTEE'])){
            return;
        }
        $db = Query::dbCheckOut();

        $data['REMARK1'][TEXT]    = $fields['COMMITTEE'];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "sysdate()";

        $where  = " WHERE YEAR       = '".CTRL_YEAR."'";
        $where .= "     AND SEMESTER = '".$fields["SEMESTER"]."'";
        $where .= "     AND SCHREGNO = '".$schregno."'";
        $where .= "     AND DIV      = '01'";
        $where .= "     AND CODE     = '02'";
        
        $db->query(Query::updateSQL($data, "hreportremark_detail_dat", $where));

        Query::dbCheckIn($db);
        return ;
    }

}
?>
