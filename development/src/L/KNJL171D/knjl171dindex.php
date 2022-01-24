<?php

require_once('for_php7.php');

require_once('knjl171dModel.inc');
require_once('knjl171dQuery.inc');

class knjl171dController extends Controller {
    var $ModelClassName = "knjl171dModel";
    var $ProgramID      = "KNJL171D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "read":
                case "main":
                case "reset":
                case "read2":
                case "next":
                case "back":
                case "end":
                    $this->callView("knjl171dForm1");
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
$knjl171dCtl = new knjl171dController;
?>
