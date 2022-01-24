<?php

require_once('for_php7.php');


class knjhmosiModel extends Model {

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

    function knjhmosiModel()
    {
    }

    function init()
    {
        $this->cmd           = VARS::request("cmd");
        $this->schregno      = VARS::request("SCHREGNO");

        //プロパティファイル
        $this->getPropertiesAll();

        //現年度設定
        if ($this->cmd == "") {
            $this->year = CTRL_YEAR;
            $this->semester = CTRL_SEMESTER;
        } else {
            $this->year     = (VARS::get("YEAR")) ? VARS::get("YEAR") : $this->year;
            $this->semester = (VARS::get("SEMESTER")) ? VARS::get("SEMESTER") : $this->semester;
        }
    }

}
?>
