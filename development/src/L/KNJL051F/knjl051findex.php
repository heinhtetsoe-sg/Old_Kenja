<?php

require_once('for_php7.php');

require_once('knjl051fModel.inc');
require_once('knjl051fQuery.inc');

class knjl051fController extends Controller {
    var $ModelClassName = "knjl051fModel";
    var $ProgramID      = "KNJL051F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl051f":
                    $sessionInstance->knjl051fModel();
                    $this->callView("knjl051fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl051fCtl = new knjl051fController;
?>
