<?php

require_once('for_php7.php');

require_once('knjl055eModel.inc');
require_once('knjl055eQuery.inc');

class knjl055eController extends Controller {
    var $ModelClassName = "knjl055eModel";
    var $ProgramID      = "KNJL055E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //CSV取込
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $this->callView("knjl055eForm1");
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
$knjl055eCtl = new knjl055eController;
?>
