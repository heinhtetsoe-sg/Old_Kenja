<?php

require_once('for_php7.php');

require_once('knjf165aModel.inc');
require_once('knjf165aQuery.inc');

class knjf165aController extends Controller {
    var $ModelClassName = "knjf165aModel";
    var $ProgramID      = "KNJF165A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "knjf165a":
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf165aForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjf165aForm1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjf165a");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("knjf165a");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjf165aForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf165aCtl = new knjf165aController;
?>
