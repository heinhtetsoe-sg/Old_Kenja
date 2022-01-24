<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjg052index.php 56587 2017-10-22 12:54:51Z maeshiro $

require_once('knjg052Model.inc');
require_once('knjg052Query.inc');

class knjg052Controller extends Controller {
    var $ModelClassName = "knjg052Model";
    var $ProgramID      = "KNJG052";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "cmbclass":
                case "knjg052":
                    $sessionInstance->knjg052Model();
                    $this->callView("knjg052Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg052Ctl = new knjg052Controller;
//var_dump($_REQUEST);
?>
