<?php

require_once('for_php7.php');

require_once('knjl371mModel.inc');
require_once('knjl371mQuery.inc');

class knjl371mController extends Controller {
    var $ModelClassName = "knjl371mModel";
    var $ProgramID      = "KNJL371M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl371m":
                    $sessionInstance->knjl371mModel();
                    $this->callView("knjl371mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl371mCtl = new knjl371mController;
?>
