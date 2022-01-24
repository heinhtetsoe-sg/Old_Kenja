<?php

require_once('for_php7.php');

require_once('knjl674aModel.inc');
require_once('knjl674aQuery.inc');

class knjl674aController extends Controller {
    var $ModelClassName = "knjl674aModel";
    var $ProgramID      = "KNJL674A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl674a":
                    $this->callView("knjl674aForm1");
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
$knjl674aCtl = new knjl674aController;
?>
