<?php

require_once('for_php7.php');

require_once('knjl311uModel.inc');
require_once('knjl311uQuery.inc');

class knjl311uController extends Controller {
    var $ModelClassName = "knjl311uModel";
    var $ProgramID      = "KNJL311U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl311u":
                    $this->callView("knjl311uForm1");
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
$knjl311uCtl = new knjl311uController;
?>
