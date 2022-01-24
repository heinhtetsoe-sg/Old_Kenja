<?php

require_once('for_php7.php');

require_once('knjl509jModel.inc');
require_once('knjl509jQuery.inc');

class knjl509jController extends Controller {
    var $ModelClassName = "knjl509jModel";
    var $ProgramID      = "KNJL509J";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl509jForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl509jCtl = new knjl509jController;
?>
