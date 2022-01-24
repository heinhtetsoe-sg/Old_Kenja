<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd125findex.php 56581 2017-10-22 12:37:16Z maeshiro $

require_once('knjd125fModel.inc');
require_once('knjd125fQuery.inc');

class knjd125fController extends Controller {
    var $ModelClassName = "knjd125fModel";
    var $ProgramID      = "KNJD125F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subclasscd":
                case "chaircd":
                case "reset":
                    $this->callView("knjd125fForm1");
                   break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knjd125fCtl = new knjd125fController;
?>
