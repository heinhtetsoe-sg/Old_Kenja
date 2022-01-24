<?php

require_once('for_php7.php');

require_once('knjl052dModel.inc');
require_once('knjl052dQuery.inc');

class knjl052dController extends Controller {
    var $ModelClassName = "knjl052dModel";
    var $ProgramID      = "KNJL052D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjl052dForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyYearModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $this->callView("knjl052dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl052dCtl = new knjl052dController;
?>
