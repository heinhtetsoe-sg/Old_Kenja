<?php

require_once('for_php7.php');

require_once('knjl580jModel.inc');
require_once('knjl580jQuery.inc');

class knjl580jController extends Controller {
    var $ModelClassName = "knjl580jModel";
    var $ProgramID      = "KNJL580Js";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "reset":
                case "end":
                    $this->callView("knjl580jForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
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
$knjl580jCtl = new knjl580jController;
?>
