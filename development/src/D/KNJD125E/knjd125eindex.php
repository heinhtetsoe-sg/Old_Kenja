<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd125eindex.php 56581 2017-10-22 12:37:16Z maeshiro $

require_once('knjd125eModel.inc');
require_once('knjd125eQuery.inc');

class knjd125eController extends Controller {
    var $ModelClassName = "knjd125eModel";
    var $ProgramID      = "KNJD125E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subclasscd":
                case "chaircd":
                case "reset":
                    $this->callView("knjd125eForm1");
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
$knjd125eCtl = new knjd125eController;
?>
