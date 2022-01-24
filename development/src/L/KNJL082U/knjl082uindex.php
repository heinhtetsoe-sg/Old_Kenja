<?php

require_once('for_php7.php');

require_once('knjl082uModel.inc');
require_once('knjl082uQuery.inc');

class knjl082uController extends Controller {
    var $ModelClassName = "knjl082uModel";
    var $ProgramID      = "KNJL082U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "testsub":
                case "reset":
                case "end":
                    $this->callView("knjl082uForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
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
$knjl082uCtl = new knjl082uController;
?>
