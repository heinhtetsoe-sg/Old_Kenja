<?php

require_once('for_php7.php');

require_once('knjm260bModel.inc');
require_once('knjm260bQuery.inc');

class knjm260bController extends Controller {
    var $ModelClassName = "knjm260bModel";
    var $ProgramID      = "KNJM260B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":              //科目（講座）が変わったとき
                case "reset":
                case "main":
                    $this->callView("knjm260bForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjm260bCtl = new knjm260bController;
?>
