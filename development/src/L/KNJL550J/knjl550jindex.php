<?php

require_once('for_php7.php');

require_once('knjl550jModel.inc');
require_once('knjl550jQuery.inc');

class knjl550jController extends Controller {
    var $ModelClassName = "knjl550jModel";
    var $ProgramID      = "KNJL550J";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "read2":
                case "next":
                case "back":
                case "reset":
                case "end":
                    $this->callView("knjl550jForm1");
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
$knjl550jCtl = new knjl550jController;
?>
