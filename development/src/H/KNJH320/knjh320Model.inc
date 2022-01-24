<?php

require_once('for_php7.php');


class knjh320Model extends Model {

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
    var $recordTable;

    function knjh320Model()
    {
    }

    function init()
    {

        $this->cmd           = VARS::request("cmd");
        $this->schregno      = VARS::request("SCHREGNO");
        
        $this->Radio = VARS::post("RADIO") != "" ? VARS::post("RADIO") : "4";

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->recordTable = $this->Properties["useRecordDat"];

        //DB接続
        $db = Query::dbCheckOut();

        //現年度設定
        if ($this->cmd == "") {
            $this->year = CTRL_YEAR;
            $this->semester = CTRL_SEMESTER;
        } else {
            $this->year     = (VARS::get("YEAR")) ? VARS::get("YEAR") : $this->year;
            $this->semester = (VARS::get("SEMESTER")) ? VARS::get("SEMESTER") : $this->semester;
        }
        //欠課数換算用データ設定
        $this->setSchollAbsent($db);

        //去年度設定
        $query = knjh320Query::getLastYear($this);
        $this->getYearSem($db, $query, $this->lastyear, $this->lastseme);
        //次年度設定
        $query = knjh320Query::getNextYear($this);
        $this->getYearSem($db, $query, $this->nextyear, $this->nextseme);

        //DB切断
        Query::dbCheckIn($db);
    }

    function getYearSem($db, $query, &$year, &$seme)
    {
        $last = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $year = $last["YEAR"];
        $seme = $last["SEMESTER"];
    }

    function setSchollAbsent($db)
    {
        $absent = $db->getRow(knjh320Query::getSchoolData($this), DB_FETCHMODE_ASSOC);
        $this->absent_cov = $absent["ABSENT_COV"];
        $this->absent_cov_late = $absent["ABSENT_COV_LATE"];
    }
}
?>
