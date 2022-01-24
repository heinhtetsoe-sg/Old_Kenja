<?php

require_once('for_php7.php');

require_once('knjl012qModel.inc');
require_once('knjl012qQuery.inc');

class knjl012qController extends Controller {
    var $ModelClassName = "knjl012qModel";
    var $ProgramID      = "KNJL012Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl012q":
                    $sessionInstance->knjl012qModel();
                    $this->callView("knjl012qForm1");
                    exit;
                case "exec":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl012q");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl012qCtl = new knjl012qController;
?>
