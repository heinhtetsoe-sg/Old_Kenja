<?php

require_once('for_php7.php');

require_once('knjl532jModel.inc');
require_once('knjl532jQuery.inc');

class knjl532jController extends Controller {
    var $ModelClassName = "knjl532jModel";
    var $ProgramID      = "KNJL532J";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "next":
                case "back":
                case "reset":
                case "end":
                    $this->callView("knjl532jForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("reset");
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
$knjl532jCtl = new knjl532jController;
?>
