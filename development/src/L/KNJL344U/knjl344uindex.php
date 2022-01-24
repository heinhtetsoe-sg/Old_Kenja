<?php

require_once('for_php7.php');

require_once('knjl344uModel.inc');
require_once('knjl344uQuery.inc');

class knjl344uController extends Controller {
    var $ModelClassName = "knjl344uModel";
    var $ProgramID      = "KNJL344U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl344u":
                    $this->callView("knjl344uForm1");
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
$knjl344uCtl = new knjl344uController;
?>
