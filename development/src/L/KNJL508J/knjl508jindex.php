<?php

require_once('for_php7.php');

require_once('knjl508jModel.inc');
require_once('knjl508jQuery.inc');

class knjl508jController extends Controller {
    var $ModelClassName = "knjl508jModel";
    var $ProgramID      = "KNJL508J";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl508jForm1");
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
$knjl508jCtl = new knjl508jController;
?>
