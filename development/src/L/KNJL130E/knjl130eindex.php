<?php

require_once('for_php7.php');

require_once('knjl130eModel.inc');
require_once('knjl130eQuery.inc');

class knjl130eController extends Controller {
    var $ModelClassName = "knjl130eModel";
    var $ProgramID      = "KNJL130E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl130eForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl130eCtl = new knjl130eController;
?>
