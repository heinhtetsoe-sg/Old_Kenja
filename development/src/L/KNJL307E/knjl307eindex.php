<?php

require_once('for_php7.php');

require_once('knjl307eModel.inc');
require_once('knjl307eQuery.inc');

class knjl307eController extends Controller {
    var $ModelClassName = "knjl307eModel";
    var $ProgramID      = "KNJL307E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl307e":
                    $this->callView("knjl307eForm1");
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
$knjl307eCtl = new knjl307eController;
?>
