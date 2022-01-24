<?php

require_once('for_php7.php');


// kanji=漢字
// $Id: knjg052aindex.php 56800 2017-10-27 06:58:16Z maeshiro $

require_once('knjg052aModel.inc');
require_once('knjg052aQuery.inc');

class knjg052aController extends Controller {
    var $ModelClassName = "knjg052aModel";
    var $ProgramID      = "KNJG052A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "cmbclass":
                case "knjg052a":
                    $sessionInstance->knjg052aModel();
                    $this->callView("knjg052aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg052aCtl = new knjg052aController;
//var_dump($_REQUEST);
?>
