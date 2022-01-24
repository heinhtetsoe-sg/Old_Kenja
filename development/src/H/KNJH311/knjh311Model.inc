<?php

require_once('for_php7.php');


class knjh311Model extends Model {

    var $cmd;
    var $schregno;
    var $adpara;
    var $cmbIndex;
    var $year;
    var $semester;

    function knjh311Model()
    {
    }

    function init()
    {
        $this->cmd           = VARS::request("cmd");
        $this->schregno      = VARS::request("schregno");
        $this->adpara        = VARS::request("adpara");
        $this->cmbIndex      = VARS::request("cmbIndex");
        $this->year          = VARS::request("year");
        $this->semester      = VARS::request("semester");

    }

}
?>
