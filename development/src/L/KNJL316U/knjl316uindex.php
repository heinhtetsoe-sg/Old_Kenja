<?php

require_once('for_php7.php');

require_once('knjl316uModel.inc');
require_once('knjl316uQuery.inc');

class knjl316uController extends Controller {
    var $ModelClassName = "knjl316uModel";
    var $ProgramID      = "KNJL316U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl316u":
                    $this->callView("knjl316uForm1");
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
$knjl316uCtl = new knjl316uController;
?>
