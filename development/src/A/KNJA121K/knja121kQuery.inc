<?php

require_once('for_php7.php');

class knja121kQuery extends Query {

    //通知票所見参照ボタンを非表示とする。(近大中学のみ)---2006/03/24
    function getKindaiJudgment()
    {
        $db = Query::dbCheckOut();
        $query = "SELECT COUNT(*) FROM SCHOOL_MST WHERE YEAR = '".CTRL_YEAR."' AND SCHOOLNAME1 LIKE '%近畿大学%' ";
        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);

        return $cnt;
    }

    //１レコード取得
    function getTrainRow($schregno, $exp_year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT * ";
        $query .= "   FROM htrainremark_dat ";
        $query .= "  WHERE year     = '".$exp_year."'";
        $query .= "    AND schregno = '".$schregno."'";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($fields,$schregno, $exp_year, $exp_semester)
    {
        $db = Query::dbCheckOut();

        //年次の取得
        $query  = "SELECT annual FROM schreg_regd_dat ";
        $query .= " WHERE schregno = '".$schregno."'";
        $query .= "   AND year = '".$exp_year."'";
        $query .= "   AND semester = '".$exp_semester."'";

        $data["YEAR"][TEXT]                 = $exp_year;
        $data["SCHREGNO"][TEXT]             = $schregno;
        $data["ANNUAL"][TEXT]               = $db->getOne($query);
        $data["TOTALSTUDYACT"][TEXT]        = $fields["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]        = $fields["TOTALSTUDYVAL"];
        $data["TOTALREMARK"][TEXT]          = $fields["TOTALREMARK"];
        $data["ATTENDREC_REMARK"][TEXT]     = $fields["ATTENDREC_REMARK"];
        $data["VIEWREMARK"][TEXT]           = $fields["VIEWREMARK"];
        $data["BEHAVEREC_REMARK"][TEXT]     = $fields["BEHAVEREC_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $db->query(Query::insertSQL($data, "htrainremark_dat"));

        Query::dbCheckIn($db);
        return ;
    }

    //DELETE
    function &getDeleteQuery($schregno, $exp_year)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM htrainremark_dat ";
        $query .= " WHERE YEAR = '".$exp_year."'";
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
        $query  = " SELECT t1.totalstudytime,t1.specialactremark,t1.communication, t1.semester ";
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
#        $query .= "    AND year = '".$model->exp_year."' ";
#        $query .= "  ORDER BY semester";

        return $query;
    }

    //行動記録の取得
    function getBehavior($model)
    {
		$query  = "SELECT DIV,CODE,ANNUAL,RECORD ";
		$query .= "FROM   BEHAVIOR_DAT ";
		$query .= "WHERE  YEAR = '" .$model->exp_year ."' AND ";
		$query .= "       SCHREGNO = '" .$model->schregno ."' ";
		$query .= "ORDER BY DIV,CODE ";

        return $query;
    }

    //INSERT
    function &getInsertQuery2($model)
    {
        $db = Query::dbCheckOut();

        //年次の取得
        $query  = "SELECT annual FROM schreg_regd_dat ";
        $query .= " WHERE schregno = '".$model->schregno."'";
        $query .= "   AND year = '".$model->exp_year."'";
        $query .= "   AND semester = '".$model->exp_semester."'";

        $annual = $db->getOne($query);

        //行動の記録
        for($i=1; $i<11; $i++)
        {
            $ival = "1" . sprintf("%02d", $i);
            if ($model->record["RECORD"][$ival] != "1") continue;

            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["DIV"][TEXT]          = "1";
            $data["CODE"][TEXT]         = sprintf("%02d", $i);
            $data["ANNUAL"][TEXT]       = $annual;
            $data["RECORD"][TEXT]       = "1";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "BEHAVIOR_DAT"));
        }
        //特別活動の記録
        for($i=1; $i<4; $i++)
        {
            $ival = "2" . sprintf("%02d", $i);
            if ($model->record["RECORD"][$ival] != "1") continue;

            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["DIV"][TEXT]          = "2";
            $data["CODE"][TEXT]         = sprintf("%02d", $i);
            $data["ANNUAL"][TEXT]       = $annual;
            $data["RECORD"][TEXT]       = "1";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "BEHAVIOR_DAT"));
        }

        Query::dbCheckIn($db);
        return ;
    }

    //DELETE
    function &getDeleteQuery2($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM BEHAVIOR_DAT ";
        $query .= " WHERE YEAR = '".$model->exp_year."'";
        $query .= "   AND SCHREGNO = '".$model->schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
    }
}
?>
