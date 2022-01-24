<?php

require_once('for_php7.php');

require_once('knjl507jModel.inc');
require_once('knjl507jQuery.inc');

class knjl507jController extends Controller {
    var $ModelClassName = "knjl507jModel";
    var $ProgramID      = "KNJL507J";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl507jForm1");
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
$knjl507jCtl = new knjl507jController;
?>
