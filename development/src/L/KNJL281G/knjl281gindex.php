<?php

require_once('for_php7.php');

require_once('knjl281gModel.inc');
require_once('knjl281gQuery.inc');

class knjl281gController extends Controller {
    var $ModelClassName = "knjl281gModel";
    var $ProgramID      = "KNJL281G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "testsub":
                case "reset":
                case "end":
                    $this->callView("knjl281gForm1");
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
$knjl281gCtl = new knjl281gController;
?>
