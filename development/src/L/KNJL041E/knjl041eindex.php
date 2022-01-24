<?php

require_once('for_php7.php');

require_once('knjl041eModel.inc');
require_once('knjl041eQuery.inc');

class knjl041eController extends Controller {
    var $ModelClassName = "knjl041eModel";
    var $ProgramID      = "KNJL041E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                    $this->callView("knjl041eForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
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
$knjl041eCtl = new knjl041eController;
?>
