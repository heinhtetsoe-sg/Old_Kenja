<?php

require_once('for_php7.php');

class knja121Query extends Query {

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
        $data["TOTALSTUDYACT"][TEXT]        = $fields["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]        = $fields["TOTALSTUDYVAL"];
        $data["SPECIALACTREMARK"][TEXT]     = $fields["SPECIALACTREMARK"];//高校のみ
        $data["TOTALREMARK"][TEXT]          = $fields["TOTALREMARK"];
        $data["ATTENDREC_REMARK"][TEXT]     = $fields["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $db->query(Query::insertSQL($data, "htrainremark_dat"));

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

}
?>
