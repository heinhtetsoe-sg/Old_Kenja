<?php

require_once('for_php7.php');

class knjl051mModel extends Model {

    var $cmd;
    var $year;  //入試年度

    function knjl051mModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
    }

}
?>