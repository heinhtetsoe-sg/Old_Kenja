<?php

require_once('for_php7.php');

require_once('knjl312uModel.inc');
require_once('knjl312uQuery.inc');

class knjl312uController extends Controller {
    var $ModelClassName = "knjl312uModel";
    var $ProgramID      = "KNJL312U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl312u":
                    $this->callView("knjl312uForm1");
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
$knjl312uCtl = new knjl312uController;
?>
