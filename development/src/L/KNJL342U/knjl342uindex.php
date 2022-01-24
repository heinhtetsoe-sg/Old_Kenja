<?php

require_once('for_php7.php');

require_once('knjl342uModel.inc');
require_once('knjl342uQuery.inc');

class knjl342uController extends Controller {
    var $ModelClassName = "knjl342uModel";
    var $ProgramID      = "KNJL342U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl342u":
                    $this->callView("knjl342uForm1");
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
$knjl342uCtl = new knjl342uController;
?>
