<?php

require_once('for_php7.php');

require_once('knjl050aModel.inc');
require_once('knjl050aQuery.inc');

class knjl050aController extends Controller {
    var $ModelClassName = "knjl050aModel";
    var $ProgramID      = "KNJL050A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "clr":
                case "set":
                case "read":
                case "next":
                case "back":
                case "reset":
                case "end":
                    $this->callView("knjl050aForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "csv":
                    if (!$sessionInstance->getCsvModel()){
                        $this->callView("knjl050aForm1");
                    }
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
$knjl050aCtl = new knjl050aController;
?>
