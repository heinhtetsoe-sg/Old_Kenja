<?php

require_once('for_php7.php');

class knja120oQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA120O' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //PROGRAMPATH取得
    function getProgrampath() {
        $query  = " WITH MENU AS ( ";
        $query .= "     SELECT ";
        $query .= "         MIN(MENUID) AS MENUID ";
        $query .= "     FROM ";
        $query .= "         MENU_HIGH_SECURITY_MST ";
        $query .= "     WHERE ";
        $query .= "         PROGRAMID = 'KNJA120O' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     PROGRAMPATH ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST T1, ";
        $query .= "     MENU T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID = T2.MENUID AND ";
        $query .= "     T1.PROGRAMID = 'KNJA120O' ";

        return $query;
    }

    //近大判定用
    function getKindaiJudgment()
    {
        $db = Query::dbCheckOut();
        $query = "SELECT COUNT(*) FROM SCHOOL_MST WHERE YEAR = '".CTRL_YEAR."' AND SCHOOLNAME1 LIKE '%近畿大学%' ";
        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);

        return $cnt;
    }

    //１レコード取得
    function getTrainRow($schregno)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT * ";
        $query .= "   FROM htrainremark_dat ";
        $query .= "  WHERE year     = '".CTRL_YEAR."'";
        $query .= "    AND schregno = '".$schregno."'";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    function getTrainHRow($schregno)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM HTRAINREMARK_HDAT ";
        $query .= "WHERE SCHREGNO ='".$schregno."' ";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($fields,$schregno)
    {
        $db = Query::dbCheckOut();

        //年次の取得
        $query  = "SELECT annual FROM schreg_regd_dat ";
        $query .= " WHERE schregno = '".$schregno."'";
        $query .= "   AND year = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";

        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SCHREGNO"][TEXT]             = $schregno;
        $data["ANNUAL"][TEXT]               = $db->getOne($query);
        $data["SPECIALACTREMARK"][TEXT]     = $fields["SPECIALACTREMARK"];
        $data["TOTALREMARK"][TEXT]          = $fields["TOTALREMARK"];
        $data["ATTENDREC_REMARK"][TEXT]     = $fields["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $db->query(Query::insertSQL($data, "htrainremark_dat"));

        Query::dbCheckIn($db);
        return ;
    }
    function &getInsertHdat($fields,$schregno)
    {
        $db = Query::dbCheckOut();

        $data["SCHREGNO"][TEXT]             = $schregno;
        $data["TOTALSTUDYACT"][TEXT]        = $fields["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]       = $fields["TOTALSTUDYVAL"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $db->query(Query::insertSQL($data, "htrainremark_hdat"));

        Query::dbCheckIn($db);
        return ;
    }

    //DELETE
    function &getDeleteQuery($schregno)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM htrainremark_dat ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."'";
        $query .= "   AND SCHREGNO = '".$schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
    }
    function &getDeleteHdat($schregno)
    {
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM HTRAINREMARK_HDAT ";
        $query .= "WHERE SCHREGNO = '".$schregno."' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
    }

    //通知表所見より年度を取得
    function selectQueryYear($model)
    {
        $query  = "SELECT distinct t1.year ";
        $query .= "  FROM hreportremark_dat t1  ";
        $query .= " WHERE t1.schregno = '" .$model->schregno ."'  ";
        $query .= " ORDER BY t1.year desc ";

        return $query;
    }

    //通知表所見より再読込
    function selectQueryGuide($model)
    {
        $query  = " SELECT t1.totalstudytime,t1.communication, t1.semester ";
        $query .= "   FROM hreportremark_dat t1 ";
        if ($model->control["学校区分"] == "0"){           //学年制
                $query .= ",V_REGDYEAR_GRADE_DAT t2 ";
        }else if ($model->control["学校区分"] == "1"){     //単位制
                $query .= ",V_REGDYEAR_UNIT_DAT  t2 ";
        }
        $query .= "  WHERE t1.schregno = '" .$model->schregno ."' ";
        $query .= "    and t1.year     = '" .$model->year_cmb ."' ";
        $query .= "    and t1.year     = t2.year";
        $query .= "    and t1.schregno = t2.schregno";
        $query .= "  ORDER BY t1.semester";

#        $query  = " SELECT communication,semester ";
#        $query .= "   FROM hreportremark_dat ";
#        $query .= "  WHERE schregno = '" .$model->schregno ."' ";
#        $query .= "    AND year = '".CTRL_YEAR."' ";
#        $query .= "  ORDER BY semester";

        return $query;
    }

    //部活動参照
    function getClub($model) {
        $query  = " SELECT ";
        $query .= "     T2.CLUBNAME, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     T3.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     CLUB_MST T2 ON  T1.CLUBCD = T2.CLUBCD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     NAME_MST T3 ON  T3.NAMECD1     = 'J001' ";
        $query .= "                 AND T1.EXECUTIVECD = T3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SDATE ";

        return $query;
    }

    //委員会活動参照
    function getCommittee($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T2.COMMITTEENAME, ";
        $query .= "     T1.CHARGENAME, ";
        $query .= "     T3.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     COMMITTEE_MST T2 ON  T1.COMMITTEE_FLG = T2.COMMITTEE_FLG ";
        $query .= "                      AND T1.COMMITTEECD   = T2.COMMITTEECD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     NAME_MST T3 ON  T3.NAMECD1     = 'J002' ";
        $query .= "                 AND T1.EXECUTIVECD = T3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '" .$model->exp_year."' AND ";
        $query .= "     T1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEQ ";

        return $query;
    }
}
?>
