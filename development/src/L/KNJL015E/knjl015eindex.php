<?php

require_once('for_php7.php');

require_once('knjl015eModel.inc');
require_once('knjl015eQuery.inc');

class knjl015eController extends Controller {
    var $ModelClassName = "knjl015eModel";
    var $ProgramID      = "KNJL015E";

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
                    $this->callView("knjl015eForm1");
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
$knjl015eCtl = new knjl015eController;
?>
