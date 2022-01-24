<?php

require_once('for_php7.php');

class knjd139iQuery extends Query
{

    //学期取得
    public function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタデータ取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE,";
        $query .= "     NAME1 || ':' || NAME2 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //年次取得
    public function getGradeCd($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= " AND GRADE = '".$model->grade."' ";

        return $query;
    }

    //観点マスタ
    public function getBehaviorSemesMst($model, $gradeCd)
    {
        $query  = " SELECT ";
        $query .= "     CODE AS VALUE, ";
        $query .= "     CODENAME AS DETAIL, ";
        $query .= "     VIEWNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= " AND GRADE = '{$gradeCd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒の記録
    public function getBehavior($model)
    {
        $query  = " SELECT ";
        $query .= "     CODE, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->exp_semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        return $query;
    }

    //所見取得
    public function getHreportremarkDat($model, $seme)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$seme}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //所見明細取得
    public function getHreportremarkDetailDat($model, $div)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        if ($div == "02") {
            $query .= "     AND SEMESTER = '9' ";
        } else {
            $query .= "     AND SEMESTER = '{$model->exp_semester}' ";
        }
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND CODE = '01' ";
        $query .= "     AND DIV = '{$div}' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //統合的な学習
    public function getTotalStudyText($model)
    {
        $query  = " SELECT ";
        $query .= "   D1.TOTALSTUDYTIME AS TEXT01, ";
        $query .= "   D2.REMARK1 AS TITLE";
        $query .= " FROM ";
        $query .= "   HREPORTREMARK_DAT D1 ";
        $query .= "   LEFT JOIN HREPORTREMARK_DETAIL_DAT D2 ";
        $query .= "     ON D2.YEAR = D1.YEAR ";
        $query .= "    AND D2.SEMESTER = D1.SEMESTER ";
        $query .= "    AND D2.SCHREGNO = D1.SCHREGNO";
        $query .= "    AND D2.DIV  = '01' ";
        $query .= "    AND D2.CODE = '01' ";
        $query .= " WHERE ";
        $query .= "   D1.YEAR = '{$model->exp_year}' ";
        $query .= "   AND D1.SEMESTER = '{$model->exp_semester}' ";
        $query .= "   AND D1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //道徳
    public function getMoralText($model)
    {
        $query  = " SELECT ";
        $query .= "   D1.REMARK1 AS TEXT01, ";
        $query .= "   D2.REMARK2 AS TITLE ";
        $query .= " FROM ";
        $query .= "   HREPORTREMARK_DAT D1 ";
        $query .= "   LEFT JOIN HREPORTREMARK_DETAIL_DAT D2 ";
        $query .= "     ON D2.YEAR = D1.YEAR ";
        $query .= "    AND D2.SEMESTER = D1.SEMESTER ";
        $query .= "    AND D2.SCHREGNO = D1.SCHREGNO";
        $query .= "    AND D2.DIV  = '01' ";
        $query .= "    AND D2.CODE = '01' ";
        $query .= " WHERE ";
        $query .= "   D1.YEAR = '{$model->exp_year}' ";
        $query .= "   AND D1.SEMESTER = '{$model->exp_semester}' ";
        $query .= "   AND D1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //特別活動・クラブ活動
    public function getActClubText($model)
    {
        $query  = " SELECT ";
        $query .= "   D2.REMARK1 AS TEXT01, ";
        $query .= "   D2.REMARK2 AS TEXT02, ";
        $query .= "   D2.REMARK3 AS TEXT03 ";
        $query .= " FROM ";
        $query .= "   HREPORTREMARK_DETAIL_DAT D2 ";
        $query .= " WHERE ";
        $query .= "   D2.YEAR = '{$model->exp_year}' ";
        $query .= "   AND D2.SEMESTER = '9' ";
        $query .= "   AND D2.SCHREGNO = '{$model->schregno}' ";
        $query .= "   AND D2.DIV  = '02' ";
        $query .= "   AND D2.CODE = '01' ";

        return $query;
    }

    //出欠・特記事項
    public function getRemarkText($model)
    {
        $query  = " SELECT ";
        $query .= "   D1.ATTENDREC_REMARK AS TEXT01, ";
        $query .= "   D3.COMMUNICATION    AS TEXT02, ";
        $query .= "   D2.REMARK3          AS TEXT03 ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT REGD ";
        $query .= "   LEFT JOIN HREPORTREMARK_DAT D1 ";
        $query .= "     ON D1.YEAR = REGD.YEAR ";
        $query .= "    AND D1.SEMESTER = {$model->exp_semester} ";
        $query .= "    AND D1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   LEFT JOIN HREPORTREMARK_DETAIL_DAT D2 ";
        $query .= "     ON D2.YEAR = REGD.YEAR ";
        $query .= "    AND D2.SEMESTER = '{$model->semeAll}' ";
        $query .= "    AND D2.SCHREGNO = REGD.SCHREGNO";
        $query .= "    AND D2.DIV  = '01' ";
        $query .= "    AND D2.CODE = '01' ";
        $query .= "   LEFT JOIN HREPORTREMARK_DAT D3 ";
        $query .= "     ON D3.YEAR     = REGD.YEAR ";
        $query .= "    AND D3.SEMESTER = '{$model->semeAll}' ";
        $query .= "    AND D3.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   REGD.YEAR = '{$model->exp_year}' ";
        $query .= "   AND REGD.SEMESTER = '{$model->exp_semester}' ";
        $query .= "   AND REGD.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }


    //INSERT
    public function &getInsertQuery($model)
    {
        $retval = false;
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd139iQuery::getHreportremarkDat($model, $model->exp_semester));

        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]              = $model->exp_year;
            $data["SEMESTER"][TEXT]          = $model->exp_semester;
            $data["SCHREGNO"][TEXT]          = $model->schregno;
            $data["TOTALSTUDYTIME"][TEXT]    = $model->field["TOTALSTUDY_EVAL"];
            $data["REMARK1"][TEXT]           = $model->field["MORAL_EVAL"];
            $data["ATTENDREC_REMARK"][TEXT]  = $model->field["REMARK_ATTEND"];
            $data["REGISTERCD"][TEXT]        = STAFFCD;
            $data["UPDATED"][FUNC]           = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["TOTALSTUDYTIME"][TEXT]    = $model->field["TOTALSTUDY_EVAL"];
            $data["REMARK1"][TEXT]           = $model->field["MORAL_EVAL"];
            $data["ATTENDREC_REMARK"][TEXT]  = $model->field["REMARK_ATTEND"];
            $data["REGISTERCD"][TEXT]        = STAFFCD;
            $data["UPDATED"][FUNC]           = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '{$model->exp_semester}' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }
        $message1 = $db->getmessage;

        //存在チェック
        $check = $db->getCol(knjd139iQuery::getHreportremarkDat($model, $model->semeAll));

        $data = array();
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]              = $model->exp_year;
            $data["SEMESTER"][TEXT]          = $model->semeAll;
            $data["SCHREGNO"][TEXT]          = $model->schregno;
            $data["COMMUNICATION"][TEXT]     = $model->field["REMARK_TEACHERCOMMENT"];
            $data["REGISTERCD"][TEXT]        = STAFFCD;
            $data["UPDATED"][FUNC]           = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["COMMUNICATION"][TEXT]     = $model->field["REMARK_TEACHERCOMMENT"];
            $data["REGISTERCD"][TEXT]        = STAFFCD;
            $data["UPDATED"][FUNC]           = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '{$model->semeAll}' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }
        $message2 = $db->getmessage;
        if ($message1 == "" && $message2 == "") {
            $retval = true;
        }

        Query::dbCheckIn($db);
        return $retval;
    }

    //INSERT
    public function &getInsertQuery2($model)
    {
        $retval = false;
        $db = Query::dbCheckOut();

        //行動の記録・特別活動の記録（削除）
        $query  = " DELETE FROM ";
        $query .= "     BEHAVIOR_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '".$model->exp_semester."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";

        $db->query($query);
        if ($db->getmessage != "") {
            $retval = true;
        }
        for ($i = 0; $i < get_count($this->idlist); $i++) {
            $ival = sprintf("%02d", $i+1);

            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = $model->exp_semester;
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["CODE"][TEXT]         = $ival;
            $data["RECORD"][TEXT]       = $model->record["RECORD"][$ival];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "BEHAVIOR_SEMES_DAT");
            $db->query($query);
            if ($db->getmessage != "") {
                $retval = false;
            }
        }

        Query::dbCheckIn($db);
        return ;
    }

    //INSERT
    public function &getInsertQuery3($model)
    {
        $retval = false;
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd139iQuery::getHreportremarkDetailDat($model, "01"));

        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]        = $model->exp_year;
            $data["SEMESTER"][TEXT]    = $model->exp_semester;
            $data["SCHREGNO"][TEXT]    = $model->schregno;
            $data["DIV"][TEXT]         = "01";
            $data["CODE"][TEXT]        = "01";
            $data["REMARK1"][TEXT]     = $model->field["TOTALSTUDY_TITLE"];
            $data["REMARK2"][TEXT]     = $model->field["MORAL_TITLE"];
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][FUNC]     = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $data["REMARK1"][TEXT]     = $model->field["TOTALSTUDY_TITLE"];
            $data["REMARK2"][TEXT]     = $model->field["MORAL_TITLE"];
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][FUNC]     = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '{$model->exp_semester}' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";
            $where .= "     AND DIV      = '01' ";
            $where .= "     AND CODE     = '01' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
        }

        //特記事項の更新
        $data = array();
        $data["REMARK3"][TEXT]     = $model->field["REMARK_TCTTL"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR     = '".$model->exp_year."' ";
        $where .= "     AND SEMESTER = '{$model->semeAll}' ";
        $where .= "     AND SCHREGNO = '".$model->schregno."' ";
        $where .= "     AND DIV      = '01' ";
        $where .= "     AND CODE     = '01' ";

        $check = $db->getCol("SELECT * FROM HREPORTREMARK_DETAIL_DAT ".$where);

        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]        = $model->exp_year;
            $data["SEMESTER"][TEXT]    = $model->semeAll;
            $data["SCHREGNO"][TEXT]    = $model->schregno;
            $data["DIV"][TEXT]         = "01";
            $data["CODE"][TEXT]        = "01";
            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
        }

        if ($db->getmessage == "") {
            $retval = true;
        }

        Query::dbCheckIn($db);
        return ;
    }

    //INSERT
    public function &getInsertQuery4($model)
    {
        $retval = false;
        $db = Query::dbCheckOut();

        //存在チェック
        $query = knjd139iQuery::getHreportremarkDetailDat($model, "02");

        $check = $db->getCol($query);

        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]        = $model->exp_year;
            $data["SEMESTER"][TEXT]    = '9';
            $data["SCHREGNO"][TEXT]    = $model->schregno;
            $data["DIV"][TEXT]         = "02";
            $data["CODE"][TEXT]        = "01";
            $data["REMARK1"][TEXT]     = $model->field["CLUB_FIRST"];
            $data["REMARK2"][TEXT]     = $model->field["CLUB_SCND"];
            $data["REMARK3"][TEXT]     = $model->field["FAVORITE_ACTV"];
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][FUNC]     = "sysdate()";
            $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
            $db->query($query);
        } else {
            $data["REMARK1"][TEXT]     = $model->field["CLUB_FIRST"];
            $data["REMARK2"][TEXT]     = $model->field["CLUB_SCND"];
            $data["REMARK3"][TEXT]     = $model->field["FAVORITE_ACTV"];
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][FUNC]     = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR         = '{$model->exp_year}' ";
            $where .= "     AND SEMESTER = '9' ";
            $where .= "     AND SCHREGNO = '{$model->schregno}' ";
            $where .= "     AND DIV      = '02' ";
            $where .= "     AND CODE     = '01' ";
            $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);

            $db->query($query);
        }
        if ($db->getmessage == "") {
            $retval = true;
        }

        Query::dbCheckIn($db);
        return ;
    }

    /**********************/
    /* 出欠備考参照で使用 */
    /**********************/
    public function getAttendAbsenceRemarkDat($model)
    {
        $query  = " SELECT ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     ATTEND_ABSENCE_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->exp_semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ABSENCE_DATE ";

        return $query;
    }
    /************************/
}
