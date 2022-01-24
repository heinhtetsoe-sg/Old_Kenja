<?php

require_once('for_php7.php');

require_once('knjl051wModel.inc');
require_once('knjl051wQuery.inc');

class knjl051wController extends Controller {
    var $ModelClassName = "knjl051wModel";
    var $ProgramID      = "KNJL051W";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "end":
                case "copy":
                case "main":
                case "read":
                case "back":
                case "next":
                case "reset":
                    $this->callView("knjl051wForm1");
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
$knjl051wCtl = new knjl051wController;
?>
