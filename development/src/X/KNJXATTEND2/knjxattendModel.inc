<?php

require_once('for_php7.php');

class knjxattendModel extends Model {

    var $cmd;
    var $schregno;
    var $year;
    var $semester;
    var $lastyear;
    var $lastseme;
    var $nextyear;
    var $nextseme;
    var $virus;
    var $koudome;

    function knjxattendModel()
    {
    }

    function init()
    {
        $this->cmd           = VARS::request("cmd");
        $this->schregno      = VARS::request("SCHREGNO");

        //現年度設定
        if ($this->cmd == "") {
            $this->year     = CTRL_YEAR;
            $this->semester = CTRL_SEMESTER;
        } else {
            $this->year     = (VARS::get("YEAR")) ? VARS::get("YEAR") : $this->year;
            $this->semester = (VARS::get("SEMESTER")) ? VARS::get("SEMESTER") : $this->semester;
        }
        //去年度設定
        $query = knjxattendQuery::getLastYear($this);
        $this->getYearSem($query, $this->lastyear, $this->lastseme);
        //次年度設定
        $query = knjxattendQuery::getNextYear($this);
        $this->getYearSem($query, $this->nextyear, $this->nextseme);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        //初期値の設定
        $this->virus     = ($this->Properties["useVirus"] == 'true') ? true : false;
        $this->koudome   = ($this->Properties["useKoudome"] == 'true') ? true : false;
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
}
?>
