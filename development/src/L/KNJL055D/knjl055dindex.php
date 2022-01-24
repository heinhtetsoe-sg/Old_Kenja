<?php

require_once('for_php7.php');

require_once('knjl055dModel.inc');
require_once('knjl055dQuery.inc');

class knjl055dController extends Controller {
    var $ModelClassName = "knjl055dModel";
    var $ProgramID      = "KNJL055D";

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
                    $this->callView("knjl055dForm1");
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
$knjl055dCtl = new knjl055dController;
?>
