<?php

require_once('for_php7.php');
require_once('knjz211dModel.inc');
require_once('knjz211dQuery.inc');

class knjz211dController extends Controller {
    var $ModelClassName = "knjz211dModel";
    var $ProgramID      = "KNJZ211D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                    $this->callView("knjz211dForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "copy":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getCopyModel();
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
$knjz211dCtl = new knjz211dController;
?>
