<?php

require_once('for_php7.php');

require_once('knjl052eModel.inc');
require_once('knjl052eQuery.inc');

class knjl052eController extends Controller {
    var $ModelClassName = "knjl052eModel";
    var $ProgramID      = "KNJL052E";

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
                    $this->callView("knjl052eForm1");
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
$knjl052eCtl = new knjl052eController;
?>
