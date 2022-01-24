<?php

require_once('for_php7.php');

require_once('knjl013aModel.inc');
require_once('knjl013aQuery.inc');

class knjl013aController extends Controller {
    var $ModelClassName = "knjl013aModel";
    var $ProgramID      = "KNJL013A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjl013aForm1");
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
$knjl013aCtl = new knjl013aController;
?>
