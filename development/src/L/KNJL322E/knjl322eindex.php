<?php

require_once('for_php7.php');

require_once('knjl322eModel.inc');
require_once('knjl322eQuery.inc');

class knjl322eController extends Controller {
    var $ModelClassName = "knjl322eModel";
    var $ProgramID      = "KNJL322E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl322e":
                    $this->callView("knjl322eForm1");
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
$knjl322eCtl = new knjl322eController;
?>
