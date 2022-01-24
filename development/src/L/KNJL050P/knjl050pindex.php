<?php

require_once('for_php7.php');

require_once('knjl050pModel.inc');
require_once('knjl050pQuery.inc');

class knjl050pController extends Controller {
    var $ModelClassName = "knjl050pModel";
    var $ProgramID      = "KNJL050P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "end":
                case "main":
                case "read":
                case "testsub":
                case "back":
                case "next":
                case "reset":
                    $this->callView("knjl050pForm1");
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
$knjl050pCtl = new knjl050pController;
?>
