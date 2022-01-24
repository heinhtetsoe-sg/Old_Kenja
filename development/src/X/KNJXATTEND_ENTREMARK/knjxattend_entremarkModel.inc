<?php

require_once('for_php7.php');

class knjxattend_entremarkModel extends Model {
    var $cmd;
    var $schregno; //学籍番号
    var $year;     //年度

    function knjxattend_entremarkModel() {}

    function init() {
        $this->schregno = VARS::request("SCHREGNO");
        $this->year     = VARS::request("YEAR");
    }
}
?>
