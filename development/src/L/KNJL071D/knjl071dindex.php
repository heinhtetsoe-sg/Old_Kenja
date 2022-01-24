<?php

require_once('for_php7.php');

require_once('knjl071dModel.inc');
require_once('knjl071dQuery.inc');

class knjl071dController extends Controller {
    var $ModelClassName = "knjl071dModel";
    var $ProgramID      = "KNJL071D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                    $this->callView("knjl071dForm1");
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
$knjl071dCtl = new knjl071dController;
?>
