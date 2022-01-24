<?php

require_once('for_php7.php');


class knjh312Model extends Model {

    var $cmd;
    var $schregno;
    var $year;
    var $semester;
    var $lastyear;
    var $lastseme;
    var $nextyear;
    var $nextseme;
    var $absent_cov;
    var $absent_cov_late;

    function knjh312Model()
    {
    }

    function init()
    {
        $this->cmd           = VARS::request("cmd");
        $this->schregno      = VARS::request("SCHREGNO");

        //現年度設定
        if ($this->cmd == "") {
            $this->year = CTRL_YEAR;
            $this->semester = CTRL_SEMESTER;
        } else {
            $this->year     = (VARS::get("YEAR")) ? VARS::get("YEAR") : $this->year;
            $this->semester = (VARS::get("SEMESTER")) ? VARS::get("SEMESTER") : $this->semester;
        }
        //欠課数換算用データ設定
        $this->setSchollAbsent();

        //去年度設定
        $query = knjh312Query::getLastYear($this);
        $this->getYearSem($query, $this->lastyear, $this->lastseme);
        //次年度設定
        $query = knjh312Query::getNextYear($this);
        $this->getYearSem($query, $this->nextyear, $this->nextseme);
    }

    function getYearSem($query, &$year, &$seme)
    {
        //DB接続
        $db = Query::dbCheckOut();

        $last = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $year = $last["YEAR"];
        $seme = $last["SEMESTER"];

        //DB切断
        Query::dbCheckIn($db);
    }

    function setSchollAbsent()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $absent = $db->getRow(knjh312Query::getSchoolData($this), DB_FETCHMODE_ASSOC);
        $this->absent_cov = $absent["ABSENT_COV"];
        $this->absent_cov_late = $absent["ABSENT_COV_LATE"];

        //DB切断
        Query::dbCheckIn($db);
    }
}
?>
