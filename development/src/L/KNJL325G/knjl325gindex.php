<?php

require_once('for_php7.php');

require_once('knjl325gModel.inc');
require_once('knjl325gQuery.inc');

class knjl325gController extends Controller {
    var $ModelClassName = "knjl325gModel";
    var $ProgramID      = "KNJL325G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl325g":
                    $sessionInstance->knjl325gModel();
                    $this->callView("knjl325gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl325gCtl = new knjl325gController;
?>
