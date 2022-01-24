<?php

require_once('for_php7.php');

require_once('knjl018aModel.inc');
require_once('knjl018aQuery.inc');

class knjl018aController extends Controller {
    var $ModelClassName = "knjl018aModel";
    var $ProgramID      = "KNJL018A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "reset":
                case "year":
                case "clear":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl018aForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
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
$knjl018aCtl = new knjl018aController;
?>
